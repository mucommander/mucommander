package com.mucommander.file;

import com.mucommander.file.filter.FileFilter;
import com.mucommander.file.filter.FilenameFilter;
import com.mucommander.file.impl.ProxyFile;
import com.mucommander.file.impl.SimpleEntry;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 *
 *
 * @author Maxence Bernard
 */
public abstract class AbstractArchiveFile extends ProxyFile {

    /** Archive entries tree */
    private DefaultMutableTreeNode entriesTree;

    /** Date this file had when the entries tree was created. Used to detect if the archive file has changed and entries
     * need to be reloaded */
    private long entriesTreeDate;


    /**
     * Creates an AbstractArchiveFile on top of the given file.
     *
     * @param file the file on top of which to create the archive
     */
    protected AbstractArchiveFile(AbstractFile file) {
        super(file);
    }


    /**
     * Creates the entries tree, used by {@link #ls(ArchiveEntryFile, com.mucommander.file.filter.FilenameFilter, com.mucommander.file.filter.FileFilter)}
     * to quickly list the contents of an archive's subfolder.
     *
     * @throws IOException if an error occured while retrieving this archive's entries
     */
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
        this.entriesTreeDate = getDate();
    }


    /**
     * Checks that the entries tree exists and that this file hasn't changed since it was created. If any of those
     * 2 conditions isn't met, the entries tree is (re)created. 
     *
     * @throws IOException if an error occurred while creating the tree
     */
    private void checkEntriesTree() throws IOException {
        if(this.entriesTree==null || getDate()!=this.entriesTreeDate)
            createEntriesTree();
        
    }


    /**
     * Returns the contents of the specified folder entry.
     */
    AbstractFile[] ls(ArchiveEntryFile entryFile, FilenameFilter filenameFilter, FileFilter fileFilter) throws IOException {
        // Make sure the entries tree is created and up-to-date
        checkEntriesTree();        

        DefaultMutableTreeNode matchNode = findEntryNode(entryFile.getEntry().getPath());
        if(matchNode==null) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Error: no match found for "+entryFile.getEntry().getPath()+" , this is not supposed to happen!");
            throw new IOException();
        }

        return ls(matchNode, entryFile, filenameFilter, fileFilter);
    }


    /**
     * Finds and returns the node that corresponds to the specified entry path, null if no entry matching the path
     * could be found.
     *
     * <p>Important note: the given path's separator character must be '/' and the path must be relative to the
     * archive's root, i.e. not start with a leading '/', otherwise the entry will not be found. Trailing separators
     * are ignored when paths are compared, for example the path 'temp' will match the entry 'temp/'.
     */
    private DefaultMutableTreeNode findEntryNode(String entryPath) {
        int entryDepth = ArchiveEntry.getDepth(entryPath);
        int slashPos = 0;
        DefaultMutableTreeNode currentNode = entriesTree;
        for(int d=0; d<=entryDepth; d++) {
            String subPath = d==entryDepth?entryPath:entryPath.substring(0, (slashPos=entryPath.indexOf('/', slashPos)+1));
            if(subPath.charAt(subPath.length()-1)=='/')     // Remove any trailing slash to compare paths without trailing slashs
                subPath = subPath.substring(0, subPath.length()-1);

            // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("subPath="+subPath+" depth="+d+"("+entryDepth+")");

            int nbChildren = currentNode.getChildCount();
            DefaultMutableTreeNode matchNode = null;
            for(int c=0; c<nbChildren; c++) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)currentNode.getChildAt(c);

                String childNodePath = ((ArchiveEntry)childNode.getUserObject()).getPath();
                if(childNodePath.charAt(childNodePath.length()-1)=='/')     // Remove any trailing slash to compare paths without trailing slashs
                    childNodePath = childNodePath.substring(0, childNodePath.length()-1);

                if(childNodePath.equals(subPath)) {
                    //					if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Found match for "+subPath);
                    matchNode = childNode;
                    break;
                }
            }

            if(matchNode==null)
                return null;    // No node maching the provided path, return null

            currentNode = matchNode;
        }

        return currentNode;
    }


    /**
     * Returns the contents (direct children) of the specified tree node.
     */
    private AbstractFile[] ls(DefaultMutableTreeNode treeNode, AbstractFile parentFile, FilenameFilter filenameFilter, FileFilter fileFilter) throws IOException {
        AbstractFile files[];
        int nbChildren = treeNode.getChildCount();

        // No FilenameFilter, create entry files and store them directly into an array
        if(filenameFilter==null) {
            files = new AbstractFile[nbChildren];

            for(int c=0; c<nbChildren; c++) {
                files[c] = createArchiveEntryFile((ArchiveEntry)(((DefaultMutableTreeNode)treeNode.getChildAt(c)).getUserObject()), parentFile);
            }
        }
        // Use provided FilenameFilter and temporarily store created entry files that match the filter in a Vector
        else {
            Vector filesV = new Vector();
            for(int c=0; c<nbChildren; c++) {
                ArchiveEntry entry = (ArchiveEntry)(((DefaultMutableTreeNode)treeNode.getChildAt(c)).getUserObject());
                if(!filenameFilter.accept(entry.getName()))
                    continue;

                filesV.add(createArchiveEntryFile(entry, parentFile));
            }

            files = new AbstractFile[filesV.size()];
            filesV.toArray(files);
        }

        return fileFilter==null?files:fileFilter.filter(files);
    }


    /**
     * Creates and returns an AbstractFile using the provided entry and parent file. This method takes care of
     * creating the proper AbstractArchiveFile instance if the entry is itself an archive.
     * The entry file's path will use the separator of the underlying file, as returned by {@link #getSeparator()}.
     * That means entries paths of archives located on Windows local filesystems will use '\' as a separator, and
     * '/' for Unix local archives.
     */
    private AbstractFile createArchiveEntryFile(ArchiveEntry entry, AbstractFile parentFile) throws java.net.MalformedURLException {

        String entryPath = entry.getPath();

        // If the parent file's separator is not '/' (the default entry separator), replace '/' occurrences by
        // the parent file's separator. For local files Under Windows, this allows entries' path to have '\' separators.
        String fileSeparator = getSeparator();
        if(!fileSeparator.equals("/"))
            entryPath = entryPath.replace("/", fileSeparator);

        FileURL archiveURL = getURL();
        FileURL entryURL = (FileURL)archiveURL.clone();
        entryURL.setPath(addTrailingSeparator(archiveURL.getPath()) + entryPath);
        
        AbstractFile entryFile = FileFactory.wrapArchive(
          new ArchiveEntryFile(
            this,
            entry,
            entryURL
          )
        );
        entryFile.setParent(parentFile);

        return entryFile;
    }


    /**
     * Creates and returns an AbstractFile that corresponds to the given entry path within the archive.
     * Throws an IOException if the entry does not exist inside this archive.
     *
     * <p>Important note: the given path's separator character must be '/' and the path must be relative to the
     * archive's root, i.e. not start with a leading '/', otherwise the entry will not be found.
     *
     * @param entryPath path to an entry inside this archive
     * @return an AbstractFile that corresponds to the given entry path
     * @throws IOException if the entry does not exist within the archive
     */
    public AbstractFile getEntryFile(String entryPath) throws IOException {
        // Make sure the entries tree is created and up-to-date
        checkEntriesTree();

        entryPath = entryPath.replace('\\', '/');

        // Find the entry node corresponding to the given path
        DefaultMutableTreeNode entryNode = findEntryNode(entryPath);

        if(entryNode==null)
            throw new IOException();    // Entry does not exist

        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)entryNode.getParent();
        // Todo: suboptimal recursion, findEntryNode() is called each time
        return createArchiveEntryFile((ArchiveEntry)entryNode.getUserObject(), parentNode==entriesTree?this:getEntryFile(((ArchiveEntry)parentNode.getUserObject()).getPath()));
    }


    //////////////////////
    // Abstract methods //
    //////////////////////
	
    /**
     * Returns a Vector with all the entries this archive file contains. This method will be called the first time
     * one of the <code>ls()</code> is called. If will not be further called, unless the file's date has changed since
     *
     */
    public abstract Vector getEntries() throws IOException;

    /**
     * Returns an InputStream to read from the given entry.
     */
    public abstract InputStream getEntryInputStream(ArchiveEntry entry) throws IOException;


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public AbstractFile[] ls() throws IOException {
        // Make sure the entries tree is created and up-to-date
        checkEntriesTree();

        return ls(entriesTree, this, null, null);
    }

    public AbstractFile[] ls(FilenameFilter filter) throws IOException {
        // Make sure the entries tree is created and up-to-date
        checkEntriesTree();

        return ls(entriesTree, this, filter, null);
    }

    public AbstractFile[] ls(FileFilter filter) throws IOException {
        // Make sure the entries tree is created and up-to-date
        checkEntriesTree();

        return ls(entriesTree, this, null, filter);
    }


    public boolean isBrowsable() {
        // Archive files are browsable but are not directories
        return true;
    }
	
    public boolean isDirectory() {
        // Archive files are browsable but are not directories
        return false;
    }

    public void mkdir(String name) throws IOException {
        // All archive files are read-only, let's throw an exception
        throw new IOException();
    }

    public long getFreeSpace() {
        // All archive files are read-only, return 0
        return 0;
    }

    public long getTotalSpace() {
        // An archive is considered as a volume by itself, let's return the proxied file's size
        return file.getSize();
    }

    public boolean canRunProcess() {return false;}

    public com.mucommander.process.AbstractProcess runProcess(String[] tokens) throws IOException {throw new IOException();}
}
