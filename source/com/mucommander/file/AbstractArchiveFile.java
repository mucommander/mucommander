/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * AbstractArchiveFile is the superclass of all archive files. It allows archive file to be browsed as if they were
 * regular directories, independently of the protocol used to access the file.
 *
 * <p>AbstractArchiveFile extends {@link ProxyFile} to delegate the AbstractFile implementation to the actual archive
 * file and overrides some methods to provide the added functionality. AbstractArchiveFile implementations only have
 * to implement two methods: one to list the entries contained by the archive in a flat, not hierarchical way, and
 * the other to retrieve a particular entry's contents.
 *
 * <p>The first time one of the <code>ls()</code> methods is called to list the archive's contents, the
 * {@link #getEntries()} method is called to retrieve a list of *all* the entries contained by the archive, not only the
 * ones at the top level but also the ones nested one of several levels below. Using this list of entries, it creates
 * a tree to map the structure of the archive and list the content of any particular directory within the archive.
 * This tree is recreated only if the archive file has changed, i.e. its date has changed since the tree was created.
 *
 * <p>Files returned by the <code>ls()</code> are {@link ArchiveEntryFile} instances which use an {@link ArchiveEntry}
 * object to retrieve the entry's attributes. In turn, these <code>ArchiveEntryFile</code> instances query the
 * mother <code>AbstractArchiveFile</code> to list their content.
 * <br>From an implementation perspective, one only needs to deal with {@link ArchiveEntry} instances, all the nuts
 * and bolts are taken care of by this class.
 *
 * <p>At this time, AbstractArchiveFile only supports read-only archives, which means archive entries can not be
 * added, removed or modified. Read-write support is planned and will be added later.
 * Note that the {@link com.mucommander.file.archiver.Archiver} class can be used to create archives, but entries
 * need to be added linearly.
 *
 * @see FileFactory
 * @see ArchiveEntry
 * @see ArchiveEntryFile
 * @see com.mucommander.file.archiver.Archiver
 * @author Maxence Bernard
 */
public abstract class AbstractArchiveFile extends ProxyFile {

    /** Archive entries tree */
    private ArchiveEntryTree entryTreeRoot;

    /** Date this file had when the entries tree was created. Used to detect if the archive file has changed and entries
     * need to be reloaded */
    private long entryTreeDate;


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
        this.entryTreeDate = getDate();
    }

    /**
     * Checks if the entries tree exists and if this file hasn't changed since it was created. If any of those
     * 2 conditions isn't met, the entries tree is (re)created. 
     *
     * @throws IOException if an error occurred while creating the tree
     */
    private void checkEntriesTree() throws IOException {
        if(this.entryTreeRoot ==null || getDate()!=this.entryTreeDate)
            createEntriesTree();
        
    }

    /**
     * Returns the contents of the specified folder entry.
     */
    protected AbstractFile[] ls(ArchiveEntryFile entryFile, FilenameFilter filenameFilter, FileFilter fileFilter) throws IOException {
        // Make sure the entries tree is created and up-to-date
        checkEntriesTree();        

        DefaultMutableTreeNode matchNode = entryTreeRoot.findEntryNode(entryFile.getEntry().getPath());
        if(matchNode==null) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Error: no match found for "+entryFile.getEntry().getPath()+" , this is not supposed to happen!");
            throw new IOException();
        }

        return ls(matchNode, entryFile, filenameFilter, fileFilter);
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
        DefaultMutableTreeNode entryNode = entryTreeRoot.findEntryNode(entryPath);

        if(entryNode==null)
            throw new IOException();    // Entry does not exist

        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)entryNode.getParent();
        // Todo: suboptimal recursion, findEntryNode() is called each time
        return createArchiveEntryFile((ArchiveEntry)entryNode.getUserObject(), parentNode== entryTreeRoot?this:getEntryFile(((ArchiveEntry)parentNode.getUserObject()).getPath()));
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
     * Always throws an <code>IOException</code> as archive files are currently read-only.
     */
    public void mkdir(String name) throws IOException {
        // All archive files are read-only, throw an exception
        throw new IOException();
    }

    /**
     * Always returns <code>0</code> as archive files are currently read-only.
     */
    public long getFreeSpace() {
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
