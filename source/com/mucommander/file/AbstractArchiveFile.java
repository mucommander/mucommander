package com.mucommander.file;

import java.io.*;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 *
 * @author Maxence Bernard
 */
public abstract class AbstractArchiveFile extends AbstractFile {

    /** Underlying file */
    protected AbstractFile file;

    private DefaultMutableTreeNode entriesTree;
	

    /**
     * Creates an AbstractArchiveFile on top of the given file.
     */
    protected AbstractArchiveFile(AbstractFile file) {
        super(file.getURL());
        this.file = file;
    }


    /**
     * Returns the AbstractFile instance this archive is wrapped around.
     *
     * @return the AbstractFile instance this archive is wrapped around
     */
    public AbstractFile getEnclosedFile() {
        return file;
    }

    private void createEntriesTree() throws IOException {
        DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode();

        long start = System.currentTimeMillis();

        Vector entries = getEntries();
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("entries loaded in "+(System.currentTimeMillis()-start)+" ms, nbEntries="+entries.size());
        start = System.currentTimeMillis();

        int nbEntries = entries.size();
        for(int i=0; i<nbEntries; i++) {
            ArchiveEntry entry = (ArchiveEntry)entries.elementAt(i);
            // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Processing entry "+entry.getPath());

            String entryPath = entry.getPath();
            int entryDepth = entry.getDepth();
            int slashPos = 0;
            DefaultMutableTreeNode node = treeRoot;
            for(int d=0; d<=entryDepth; d++) {
                if(d==entryDepth && !entry.isDirectory()) {
                    // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Creating leaf for "+entryPath);
                    node.add(new DefaultMutableTreeNode(entry, true));
                    break;
                }

                String subPath = d==entryDepth?entryPath:entryPath.substring(0, (slashPos=entryPath.indexOf('/', slashPos)+1));
                // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("subPath="+subPath+" depth="+d+"("+entryDepth+")");
				
                int nbChildren = node.getChildCount();
                DefaultMutableTreeNode childNode = null;
                boolean matchFound = false;
                for(int c=0; c<nbChildren; c++) {
                    childNode = (DefaultMutableTreeNode)node.getChildAt(c);
                    if(((ArchiveEntry)childNode.getUserObject()).getPath().equals(subPath)) {
                        // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Found match for "+subPath);
                        matchFound = true;
                        break;
                    }
                }
				
                if(matchFound) {
                    if(d==entryDepth) {
                        // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Replacing entry for node "+childNode);
                        // Replace existing entry
                        childNode.setUserObject(entry);
                    }
                    else {
                        node = childNode;
                    }
                }
                else {
                    if(d==entryDepth) {		// Leaf
                        // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Creating node for "+entryPath);
                        node.add(new DefaultMutableTreeNode(entry, true));
                    }
                    else {
                        // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Creating node for "+subPath);
                        childNode = new DefaultMutableTreeNode(new SimpleEntry(subPath, entry.getDate(), 0, true), true);
                        node.add(childNode);
                        node = childNode;
                    }
                }
            }
        }

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("entries tree created in "+(System.currentTimeMillis()-start)+" ms");
		
        this.entriesTree = treeRoot;
    }

	
    public AbstractFile[] ls() throws IOException {
        if(this.entriesTree == null)
            createEntriesTree();
		
        return ls(entriesTree, this);
    }
	
	
    AbstractFile[] ls(ArchiveEntryFile entryFile) throws IOException {
        if(this.entriesTree == null)
            createEntriesTree();

        // Find the node that corresponds to the specified entry
        ArchiveEntry entry = entryFile.getEntry();
        String entryPath = entry.getPath();
        int entryDepth = entry.getDepth();
        int slashPos = 0;
        DefaultMutableTreeNode currentNode = entriesTree;
        for(int d=0; d<=entryDepth; d++) {
            String subPath = d==entryDepth?entryPath:entryPath.substring(0, (slashPos=entryPath.indexOf('/', slashPos)+1));
            // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("subPath="+subPath+" depth="+d+"("+entryDepth+")");
			
            int nbChildren = currentNode.getChildCount();
            DefaultMutableTreeNode matchNode = null;
            for(int c=0; c<nbChildren; c++) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)currentNode.getChildAt(c);
                if(((ArchiveEntry)childNode.getUserObject()).getPath().equals(subPath)) {
                    //					if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Found match for "+subPath);
                    matchNode = childNode;
                    break;
                }
            }
			
            if(matchNode==null) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Error: no match found for "+subPath+" , this is not supposed to happen!");
                throw new IOException();
            }
            currentNode = matchNode;
        }
		
        return ls(currentNode, entryFile);
    }


    private AbstractFile[] ls(DefaultMutableTreeNode treeNode, AbstractFile parentFile) {
        int nbChildren = treeNode.getChildCount();
        AbstractFile files[] = new AbstractFile[nbChildren];
		
        for(int c=0; c<nbChildren; c++) {
            AbstractFile entryFile = AbstractFile.wrapArchive(
                                                              new ArchiveEntryFile(
                                                                                   this, 
                                                                                   (ArchiveEntry)(((DefaultMutableTreeNode)treeNode.getChildAt(c)).getUserObject())
                                                                                   )
                                                              );
            entryFile.setParent(parentFile);
            files[c] = entryFile;
        }

        return files;
    }


    /**
     * Checks all the given entries below top level (depth>0) and make sure they have a corresponding parent directory
     * entry, and if not create it and add it to the entries Vector.
     */
    /*
      protected static void addMissingDirectoryEntries(Vector entriesV) {
      long start = System.currentTimeMillis();

      int nbEntries = entriesV.size();
      for(int i=0; i<nbEntries; i++) {
      ArchiveEntry currentEntry = ((ArchiveEntry)entriesV.elementAt(i));
      String entryPath = currentEntry.getPath();	// entry path will include a trailing '/' if entry is a directory
      int entryDepth = currentEntry.getDepth();
      if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("checking entry #"+i+" "+entryPath+" depth="+entryDepth);
      // Entry is not at the top level
      if (entryDepth>0) {
      int slashPos = 0;
      for(int l=0; l<entryDepth; l++) {
      // Extract directory name at depth l
      String dirName = entryPath.substring(0, (slashPos=entryPath.indexOf('/', slashPos)+1));

      if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("checking for an existing entry for directory "+dirName);
      boolean entryFound = false;
      // Is there an entry for this directory ?
      for(int j=0; j<entriesV.size(); j++)
      if(((ArchiveEntry)entriesV.elementAt(j)).getPath().equals(dirName))
      entryFound = true;
	
      // An entry for this directory has been found, nothing to do, go to the next directory
      if(entryFound)
      continue;

      // Directory has no entry, let's manually create and add an ArchiveEntry for it
      if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("creating new entry for directory "+dirName);
      ArchiveEntry newEntry = currentEntry.createDirectoryEntry(dirName);
      newEntry.setDate(currentEntry.getDate());	// Use current entry's time, not accurate
      entriesV.add(newEntry);
      }
      }
      }

      if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("missing directory entries added in "+(System.currentTimeMillis()-start)+" ms, nbEntries="+entriesV.size());
      }
    */

    /**
     *  Returns top level (depth==0) entries containted by this archive.
     */
    /*
      public AbstractFile[] ls() throws IOException {
      long start = System.currentTimeMillis();
      // Load entries
      ArchiveEntry entries[] = getEntries();

      if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("entries loaded in "+(System.currentTimeMillis()-start)+" ms, nbEntries="+entries.length);
      start = System.currentTimeMillis();

      // Create entry files
      Vector subFiles = new Vector();		
      AbstractFile entryFile;
      for(int i=0; i<entries.length; i++) {
      if (entries[i].getDepth()==0) {
      entryFile = AbstractFile.wrapArchive(new ArchiveEntryFile(this, entries[i]));
      entryFile.setParent(this);
      subFiles.add(entryFile);
      }
      }

      if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("entry files created in "+(System.currentTimeMillis()-start)+" ms, nbEntries="+entries.length);

      AbstractFile subFilesArray[] = new AbstractFile[subFiles.size()];
      subFiles.toArray(subFilesArray);
      return subFilesArray;
      }
    */


    //////////////////////
    // Abstract methods //
    //////////////////////
	
    /**
     * Returns a Vector with all the entries this archive file contains. This method will be called once at most
     * during the lifetime of an archive file.
     */
    abstract Vector getEntries() throws IOException;

    /**
     * Returns an InputStream to read from the given entry.
     */
    abstract InputStream getEntryInputStream(ArchiveEntry entry) throws IOException;


    /////////////////////////////////////////
    // AbstractFile methods implementation //
    /////////////////////////////////////////
	
    public String getName() {
        return file.getName();
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    public String getSeparator() {
        return file.getSeparator();
    }

    public long getDate() {
        return file.getDate();
    }
	
    public boolean changeDate(long date) {
        return file.changeDate(date);
    }
	
    public long getSize() {
        return file.getSize();
    }
	
    public AbstractFile getParent() {
        return file.getParent();
    }
	
    public void setParent(AbstractFile parent) {
        this.file.setParent(parent);	
    }	

    public boolean exists() {
        return file.exists();
    }
	
    public boolean canRead() {
        return file.canRead();
    }
	
    public boolean canWrite() {
        return file.canWrite();
    }

    public boolean isBrowsable() {
        return true;
    }
	
    public boolean isDirectory() {
        return false;
    }

    public boolean isHidden() {
        return file.isHidden();
    }

    public boolean isSymlink() {
        return file.isSymlink();
    }

    public InputStream getInputStream() throws IOException {
        return file.getInputStream();
    }
	
    public InputStream getInputStream(long skipBytes) throws IOException {
        return file.getInputStream(skipBytes);
    }
	
    public OutputStream getOutputStream(boolean append) throws IOException {
        return file.getOutputStream(append);
    }
		
    public boolean moveTo(AbstractFile dest) throws IOException  {
        return file.moveTo(dest);
    }

    public void delete() throws IOException {
        file.delete();
    }

    public void mkdir(String name) throws IOException {
        // All archive files are read-only (for now), let's throw an exception
        throw new IOException();
    }

    public long getFreeSpace() {
        // All archive files are read-only (for now), return 0
        return 0;
    }

    public long getTotalSpace() {
        // An archive is considered as a volume by itself, let's return the archive's size
        return file.getSize();
    }	
}
