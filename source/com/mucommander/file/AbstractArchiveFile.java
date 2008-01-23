/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.file;

import com.mucommander.file.filter.FileFilter;
import com.mucommander.file.filter.FilenameFilter;
import com.mucommander.file.impl.ProxyFile;
import com.mucommander.util.StringUtils;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * <code>AbstractArchiveFile</code> is the superclass of all archive files. It allows archive file to be browsed as if
 * they were regular directories, independently of the underlying protocol used to access the actual file.
 *
 * <p><code>AbstractArchiveFile</code> extends {@link ProxyFile} to delegate the <code>AbstractFile</code>
 * implementation to the actual archive file and overrides some methods to provide the added functionality.<br>
 * There are two kinds of <code>AbstractArchiveFile</code>, both of which extend this class:
 * <ul>
 *  <li>{@link AbstractROArchiveFile}: read-only archives, these are only able to perform read operations such as
 * listing the archive's contents or retrieving a particular entry's contents.
 *  <li>{@link AbstractRWArchiveFile}: read-write archives, these are also able to modify the archive by adding or
 * deleting an entry from the archive. These operations usually require random access to the underlying file,
 * so write operations may not be available on all underlying file types. The {@link #isWritableArchive()} method allows
 * to determine whether the archive file is able to carry out write operations or not.
 * </ul>
 * When implementing a new archive file/format, either <code>AbstractROArchiveFile</code> or <code>AbstractRWArchiveFile</code>
 * should be subclassed, but not this class.
 * </p>
 *
 * <p>The first time one of the <code>ls()</code> methods is called to list the archive's contents, the
 * {@link #getEntries()} method is called to retrieve a list of *all* the entries contained by the archive, not only the
 * ones at the top level but also the ones nested one of several levels below. Using this list of entries, it creates
 * a tree to map the structure of the archive and list the content of any particular directory within the archive.
 * This tree is recreated (<code>getEntries()</code> is called again) only if the archive file has changed, i.e. its
 * date has changed since the tree was created.</p>
 *
 * <p>Files returned by the <code>ls()</code> are {@link ArchiveEntryFile} instances which use an {@link ArchiveEntry}
 * object to retrieve the entry's attributes. In turn, these <code>ArchiveEntryFile</code> instances query the
 * associated <code>AbstractArchiveFile</code> to list their content.
 * <br>From an implementation perspective, one only needs to deal with {@link ArchiveEntry} instances, all the nuts
 * and bolts are taken care of by this class.</p>

 * @see FileFactory
 * @see ArchiveEntry
 * @see ArchiveEntryFile
 * @see com.mucommander.file.archiver.Archiver
 * @author Maxence Bernard
 */
public abstract class AbstractArchiveFile extends ProxyFile {

    /** Archive entries tree */
    protected ArchiveEntryTree entryTreeRoot;

    /** Date this file had when the entries tree was created. Used to detect if the archive file has changed and entries
     * need to be reloaded */
    protected long entryTreeDate;


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
    protected void createEntriesTree() throws IOException {
        ArchiveEntryTree treeRoot = new ArchiveEntryTree();

        long start = System.currentTimeMillis();

        Vector entries = getEntries();

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("entries loaded in "+(System.currentTimeMillis()-start)+" ms, nbEntries="+entries.size());
        start = System.currentTimeMillis();

        int nbEntries = entries.size();
        for(int i=0; i<nbEntries; i++) {
            treeRoot.addArchiveEntry((ArchiveEntry)entries.elementAt(i));
        }

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("entries tree created in "+(System.currentTimeMillis()-start)+" ms");
		
        this.entryTreeRoot = treeRoot;
        declareEntriesTreeUpToDate();
    }

    /**
     * Checks if the entries tree exists and if this file hasn't been modified since the tree was last created.
     * If any of those 2 conditions isn't met, the entries tree is (re)created.
     *
     * @throws IOException if an error occurred while creating the tree
     */
    protected void checkEntriesTree() throws IOException {
        if(this.entryTreeRoot ==null || getDate()!=this.entryTreeDate)
            createEntriesTree();
    }

    /**
     * Declares the entries tree up-to-date by setting the current tree date to the archive file's.
     * This method should be called by {@link AbstractRWArchiveFile} implementations when the archive file has been
     * modified and the entries propagated in the tree, to avoid the tree from being automatically re-created when
     * {@link #checkEntriesTree()} is called.
     */
    protected void declareEntriesTreeUpToDate() {
        this.entryTreeDate = getDate();
    }

    /**
     * Adds the given {@link ArchiveEntry} to the entries tree. This method will create the tree if it doesn't already
     * exist, or re-create it if the archive file has changed since it was last created.
     *
     * @param entry the ArchiveEntry to add to the tree
     * @throws IOException if an error occurred while creating the entries tree
     */
    protected void addToEntriesTree(ArchiveEntry entry) throws IOException {
        checkEntriesTree();
        entryTreeRoot.addArchiveEntry(entry);
    }

    /**
     * Removes the given {@link ArchiveEntry} from the entries tree. This method will create the tree if it doesn't
     * already exist, or re-create it if the archive file has changed since it was last created.
     *
     * @param entry the ArchiveEntry to remove from the tree
     * @throws IOException if an error occurred while creating the entries tree
     */
    protected void removeFromEntriesTree(ArchiveEntry entry) throws IOException {
        checkEntriesTree();
        DefaultMutableTreeNode entryNode = entryTreeRoot.findEntryNode(entry.getPath());

        if(entryNode!=null) {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)entryNode.getParent();
            parentNode.remove(entryNode);
        }
    }

    /**
     * Returns the {@link ArchiveEntryTree} instance corresponding to the root of the archive entry tree.
     * The returned value can be <code>null</code> if the tree hasn't been intialized yet.
     *
     * @return the ArchiveEntryTree instance corresponding to the root of the archive entry tree
     */
    ArchiveEntryTree getArchiveEntryTree() {
        return entryTreeRoot;
    }

    /**
     * Returns the contents of the specified folder entry.
     */
    protected AbstractFile[] ls(ArchiveEntryFile entryFile, FilenameFilter filenameFilter, FileFilter fileFilter) throws IOException {
        // Make sure the entries tree is created and up-to-date
        checkEntriesTree();        

        if(!entryFile.isBrowsable())
            throw new IOException();

        DefaultMutableTreeNode matchNode = entryTreeRoot.findEntryNode(entryFile.getEntry().getPath());
        if(matchNode==null)
            throw new IOException();

        return ls(matchNode, entryFile, filenameFilter, fileFilter);
    }

    /**
     * Returns the contents (direct children) of the specified tree node.
     */
    protected AbstractFile[] ls(DefaultMutableTreeNode treeNode, AbstractFile parentFile, FilenameFilter filenameFilter, FileFilter fileFilter) throws IOException {
        AbstractFile files[];
        int nbChildren = treeNode.getChildCount();

        // No FilenameFilter, create entry files and store them directly into an array
        if(filenameFilter==null) {
            files = new AbstractFile[nbChildren];

            for(int c=0; c<nbChildren; c++) {
                files[c] = getArchiveEntryFile((ArchiveEntry)(((DefaultMutableTreeNode)treeNode.getChildAt(c)).getUserObject()), parentFile, true);
            }
        }
        // Use provided FilenameFilter and temporarily store created entry files that match the filter in a Vector
        else {
            Vector filesV = new Vector();
            for(int c=0; c<nbChildren; c++) {
                ArchiveEntry entry = (ArchiveEntry)(((DefaultMutableTreeNode)treeNode.getChildAt(c)).getUserObject());
                if(!filenameFilter.accept(entry.getName()))
                    continue;

                filesV.add(getArchiveEntryFile(entry, parentFile, true));
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
    protected AbstractFile getArchiveEntryFile(ArchiveEntry entry, AbstractFile parentFile, boolean exists) throws IOException {

        String entryPath = entry.getPath();

        // If the parent file's separator is not '/' (the default entry separator), replace '/' occurrences by
        // the parent file's separator. For local files Under Windows, this allows entries' path to have '\' separators.
        String fileSeparator = getSeparator();
        if(!fileSeparator.equals("/"))
            entryPath = StringUtils.replaceCompat(entryPath, "/", fileSeparator);

        FileURL archiveURL = getURL();
        FileURL entryURL = (FileURL)archiveURL.clone();
        entryURL.setPath(addTrailingSeparator(archiveURL.getPath()) + entryPath);
        
        AbstractFile entryFile = FileFactory.wrapArchive(
          new ArchiveEntryFile(
            entryURL,
            this,
            entry,
            exists            
          )
        );
        entryFile.setParent(parentFile);

        return entryFile;
    }

    /**
     * Creates and returns an AbstractFile that corresponds to the given entry path within the archive.
     * The requested entry may or may not exist in the archive, the {@link #exists()} method of the returned entry file
     * can be used used to get this information. However, if the requested entry does not exist in the archive and is
     * not located at the top level (i.e. is located in a subfolder), its parent folder must exist in the archive or
     * else an <code>IOException</code> will be thrown.
     *
     * <p>Important note: the given path's separator character must be '/' and the path must be relative to the
     * archive's root, i.e. not start with a leading '/', otherwise the entry will not be found.</p>
     *
     * @param entryPath path to an entry within this archive
     * @return an AbstractFile that corresponds to the given entry path
     * @throws IOException if neither the entry nor its parent exist within the archive
     */
    public AbstractFile getArchiveEntryFile(String entryPath) throws IOException {
        // Make sure the entries tree is created and up-to-date
        checkEntriesTree();

        entryPath = entryPath.replace('\\', '/');

        // Find the entry node corresponding to the given path
        DefaultMutableTreeNode entryNode = entryTreeRoot.findEntryNode(entryPath);

        if(entryNode==null) {
            int depth = ArchiveEntry.getDepth(entryPath);

            AbstractFile parentFile;
            if(depth==0)
                parentFile = this;
            else {
                String parentPath = entryPath;
                if(parentPath.endsWith("/"))
                    parentPath = parentPath.substring(0, parentPath.length()-1);

                parentPath = parentPath.substring(0, parentPath.lastIndexOf('/'));

                parentFile = getArchiveEntryFile(parentPath);
                if(parentFile==null)    // neither the entry nor the parent exist
                    throw new IOException();
            }

            return getArchiveEntryFile(new ArchiveEntry(entryPath, false), parentFile, false);
        }

        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)entryNode.getParent();
        // Todo: suboptimal recursion, findEntryNode() is called each time
        return getArchiveEntryFile((ArchiveEntry)entryNode.getUserObject(), parentNode== entryTreeRoot?this: getArchiveEntryFile(((ArchiveEntry)parentNode.getUserObject()).getPath()), true);
    }


    //////////////////////
    // Abstract methods //
    //////////////////////
	
    /**
     * Returns a Vector of {@link ArchiveEntry}, representing all the entries this archive file contains.
     * This method will be called the first time one of the <code>ls()</code> is called. If will not be further called,
     * unless the file's date has changed since the last time one of the <code>ls()</code> methods was called.
     */
    public abstract Vector getEntries() throws IOException;

    /**
     * Returns an InputStream to read from the given archive entry. The specified {@link ArchiveEntry} instance is
     * necessarily one of the entries that were returned by {@link #getEntries()}. 
     */
    public abstract InputStream getEntryInputStream(ArchiveEntry entry) throws IOException;

    /**
     * Returns <code>true</code> if this archive file is writable, i.e. is capable of adding and deleting entries to
     * the underlying archive file.
     *
     * <p>
     * This method is implemented by {@link com.mucommander.file.AbstractROArchiveFile} and
     * {@link com.mucommander.file.AbstractRWArchiveFile} to respectively return <code>false</code> and
     * <code>true</code>. This method may be overridden by <code>AbstractRWArchiveFile</code> implementations if write
     * access is only available under certain conditions, for example if it requires random write access to the
     * proxied archive file (which may not always be available).
     * Therefore, this method should be used to test if an <code>AbstractArchiveFile</code> is writable, rather than
     * testing if it is an instance of <code>AbstractRWArchiveFile</code>.
     * </p>
     *
     * @return true if this archive is writable, i.e. is capable of adding and deleting entries to
     * the underlying archive file.
     */
    public abstract boolean isWritableArchive();


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * This method is overridden to list and return the topmost entries contained by this archive.
     * The returned files are {@link ArchiveEntryFile} instances.
     *
     * @return the topmost entries contained by this archive
     * @throws IOException if the archive entries could not be listed
     */
    public AbstractFile[] ls() throws IOException {
        // Make sure the entries tree is created and up-to-date
        checkEntriesTree();

        return ls(entryTreeRoot, this, null, null);
    }

    /**
     * This method is overridden to list and return the topmost entries contained by this archive, filtering out
     * the ones that do not match the specified {@link FilenameFilter}. The returned files are {@link ArchiveEntryFile}
     * instances.
     *
     * @param filter the FilenameFilter to be used to filter files out from the list, may be <code>null</code>
     * @return the topmost entries contained by this archive
     * @throws IOException if the archive entries could not be listed
     */
    public AbstractFile[] ls(FilenameFilter filter) throws IOException {
        // Make sure the entries tree is created and up-to-date
        checkEntriesTree();

        return ls(entryTreeRoot, this, filter, null);
    }

    /**
     * This method is overridden to list and return the topmost entries contained by this archive, filtering out
     * the ones that do not match the specified {@link FileFilter}. The returned files are {@link ArchiveEntryFile} instances.
     *
     * @param filter the FilenameFilter to be used to filter files out from the list, may be <code>null</code>
     * @return the topmost entries contained by this archive
     * @throws IOException if the archive entries could not be listed
     */
    public AbstractFile[] ls(FileFilter filter) throws IOException {
        // Make sure the entries tree is created and up-to-date
        checkEntriesTree();

        return ls(entryTreeRoot, this, null, filter);
    }

    /**
     * Always returns <code>true</code>, archive files can be browsed even though they are not directories.
     */
    public boolean isBrowsable() {
        return true;
    }
	
    /**
     * Always returns <code>false</code>, archive files can be browsed but they are not directiories.
     */
    public boolean isDirectory() {
        return false;
    }

    /**
     * Returns the proxied file's free space if this archive is writable (as reported by {@link #isWritableArchive()},
     * else returns <code>0</code>. 
     *
     * @return the proxied file's free space is this archive is writable, 0 otherwise.
     */
    public long getFreeSpace() {
        if(isWritableArchive())
            return file.getFreeSpace();
        else
            return 0;
    }

    /**
     * Always returns <code>false</code>.
     */
    public boolean canRunProcess() {
        return false;
    }

    /**
     * Always throws an <code>IOException</code>.
     */
    public com.mucommander.process.AbstractProcess runProcess(String[] tokens) throws IOException {
        throw new IOException();
    }
}
