/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.commons.file;

import com.mucommander.commons.file.filter.FileFilter;
import com.mucommander.commons.file.filter.FilenameFilter;
import com.mucommander.commons.file.impl.ProxyFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import java.util.WeakHashMap;

/**
 * <code>AbstractArchiveFile</code> is the superclass of all archive files. It allows archive file to be browsed as if
 * they were regular directories, independently of the underlying protocol used to access the actual file.
 * <p>
 * <code>AbstractArchiveFile</code> extends {@link ProxyFile} to delegate the <code>AbstractFile</code>
 * implementation to the actual archive file and overrides some methods to provide the added functionality.<br>
 * There are two kinds of <code>AbstractArchiveFile</code>, both of which extend this class:
 * <ul>
 *  <li>{@link AbstractROArchiveFile}: read-only archives, these are only able to perform read operations such as
 * listing the archive's contents or retrieving a particular entry's contents.
 *  <li>{@link AbstractRWArchiveFile}: read-write archives, these are also able to modify the archive by adding or
 * deleting an entry from the archive. These operations usually require random access to the underlying file,
 * so write operations may not be available on all underlying file types. The {@link #isWritable()} method allows
 * to determine whether the archive file is able to carry out write operations or not.
 * </ul>
 * When implementing a new archive file/format, either <code>AbstractROArchiveFile</code> or <code>AbstractRWArchiveFile</code>
 * should be subclassed, but not this class.
 * </p>
 *
 * <p>The first time one of the <code>ls()</code> methods is called to list the archive's contents,
 * {@link #getEntryIterator()} is called to retrieve a list of *all* the entries contained by the archive, not only the
 * ones at the top level but also the ones nested one of several levels below. Using this list of entries, it creates
 * a tree to map the structure of the archive and list the content of any particular directory within the archive.
 * This tree is recreated (<code>getEntryIterator()</code> is called again) only if the archive file has changed, i.e.
 * if its date has changed since the tree was created.</p>
 *
 * <p>Files returned by the <code>ls()</code> are {@link AbstractArchiveEntryFile} instances which use an {@link ArchiveEntry}
 * object to retrieve the entry's attributes. In turn, these <code>AbstractArchiveEntryFile</code> instances query the
 * associated <code>AbstractArchiveFile</code> to list their content.
 * <br>From an implementation perspective, one only needs to deal with {@link ArchiveEntry} instances, all the nuts
 * and bolts are taken care of by this class.</p>
 *
 * <p>Note that an instance of <code>AbstractArchiveFile</code> may or may not actually be an archive:
 * {@link #isArchive()} returns <code>true</code> only if the file currently exists and is not a directory. The value
 * returned by {@link #isArchive()} may change over time as the file is modified. When an
 * <code>AbstractArchiveFile</code> is not currently an archive, it acts just as a 'normal' file and delegates
 * <code>ls()</code> methods to the underlying {@link AbstractFile}</p>
 *
 * @see com.mucommander.commons.file.FileFactory
 * @see com.mucommander.commons.file.ArchiveFormatProvider
 * @see com.mucommander.commons.file.ArchiveEntry
 * @see AbstractArchiveEntryFile
 * @see com.mucommander.commons.file.archiver.Archiver
 * @author Maxence Bernard
 */
public abstract class AbstractArchiveFile extends ProxyFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractArchiveFile.class);

    /** Archive entries tree */
    protected ArchiveEntryTree entryTreeRoot;

    /** Date this file had when the entries tree was created. Used to detect if the archive file has changed and entries
     * need to be reloaded */
    protected long entryTreeDate;

    /** Caches {@link AbstractArchiveEntryFile} instances so that there is only one AbstractArchiveEntryFile
     * corresponding to the same entry at any given time, to avoid attribute inconsistencies. The key is the
     * corresponding ArchiveEntry. */
    protected WeakHashMap<ArchiveEntry, AbstractArchiveEntryFile> archiveEntryFiles;

    /**
     * Creates an AbstractArchiveFile on top of the given file.
     *
     * @param file the file on top of which to create the archive
     */
    protected AbstractArchiveFile(AbstractFile file) {
        super(file);
    }

    /**
     * Creates the entries tree, used by {@link #ls(AbstractArchiveEntryFile , com.mucommander.commons.file.filter.FilenameFilter, com.mucommander.commons.file.filter.FileFilter)}
     * to quickly list the contents of an archive's subfolder.
     *
     * @throws IOException if an error occured while retrieving this archive's entries
     * @throws UnsupportedFileOperationException if {@link FileOperation#READ_FILE} operations are not supported by the
     * underlying file protocol.
     */
    protected void createEntriesTree() throws IOException, UnsupportedFileOperationException {
        // TODO: this method is not thread-safe and needs to be synchronized
        ArchiveEntryTree treeRoot = new ArchiveEntryTree();
        archiveEntryFiles = new WeakHashMap<ArchiveEntry, AbstractArchiveEntryFile>();

        long start = System.currentTimeMillis();
        ArchiveEntryIterator entries = getEntryIterator();
        try {
            ArchiveEntry entry;
            while((entry=entries.nextEntry())!=null)
                treeRoot.addArchiveEntry(entry);

            LOGGER.info("entries tree created in "+(System.currentTimeMillis()-start)+" ms");

            this.entryTreeRoot = treeRoot;
            declareEntriesTreeUpToDate();
        }
        finally {
            try { entries.close(); }
            catch(IOException e) {
                // Not much we can do about it
            }
        }
    }

    /**
     * Checks if the entries tree exists and if this file hasn't been modified since the tree was last created.
     * If any of those 2 conditions isn't met, the entries tree is (re)created.
     *
     * @throws IOException if an error occurred while creating the tree
     * @throws UnsupportedFileOperationException if {@link FileOperation#READ_FILE} operations are not supported by the
     * underlying file protocol.
     */
    protected void checkEntriesTree() throws IOException, UnsupportedFileOperationException {
        if(this.entryTreeRoot==null || getDate()!=this.entryTreeDate)
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
     * @throws UnsupportedFileOperationException if {@link FileOperation#READ_FILE} operations are not supported by the
     * underlying file protocol.
     */
    protected void addToEntriesTree(ArchiveEntry entry) throws IOException, UnsupportedFileOperationException {
        checkEntriesTree();
        entryTreeRoot.addArchiveEntry(entry);
    }

    /**
     * Removes the given {@link ArchiveEntry} from the entries tree. This method will create the tree if it doesn't
     * already exist, or re-create it if the archive file has changed since it was last created.
     *
     * @param entry the ArchiveEntry to remove from the tree
     * @throws IOException if an error occurred while creating the entries tree
     * @throws UnsupportedFileOperationException if {@link FileOperation#READ_FILE} operations are not supported by the
     * underlying file protocol.
     */
    protected void removeFromEntriesTree(ArchiveEntry entry) throws IOException, UnsupportedFileOperationException {
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
     *
     * @throws UnsupportedFileOperationException if {@link FileOperation#READ_FILE} operations are not supported by the
     * underlying file protocol.
     */
    protected AbstractFile[] ls(AbstractArchiveEntryFile entryFile, FilenameFilter filenameFilter, FileFilter fileFilter) throws IOException, UnsupportedFileOperationException {
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
     *
     * @throws UnsupportedFileOperationException if {@link FileOperation#READ_FILE} operations are not supported by the
     * underlying file protocol.
     */
    private AbstractFile[] ls(DefaultMutableTreeNode treeNode, AbstractFile parentFile, FilenameFilter filenameFilter, FileFilter fileFilter) throws IOException, UnsupportedFileOperationException {
        AbstractFile files[];
        int nbChildren = treeNode.getChildCount();

        // No FilenameFilter, create entry files and store them directly into an array
        if(filenameFilter==null) {
            files = new AbstractFile[nbChildren];

            for(int c=0; c<nbChildren; c++) {
                files[c] = getArchiveEntryFile((ArchiveEntry)(((DefaultMutableTreeNode)treeNode.getChildAt(c)).getUserObject()), parentFile);
            }
        }
        // Use provided FilenameFilter and temporarily store created entry files that match the filter in a Vector
        else {
            Vector<AbstractFile> filesV = new Vector<AbstractFile>();
            for(int c=0; c<nbChildren; c++) {
                ArchiveEntry entry = (ArchiveEntry)(((DefaultMutableTreeNode)treeNode.getChildAt(c)).getUserObject());
                if(!filenameFilter.accept(entry.getName()))
                    continue;

                filesV.add(getArchiveEntryFile(entry, parentFile));
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
    protected AbstractFile getArchiveEntryFile(ArchiveEntry entry, AbstractFile parentFile) throws IOException {

        String entryPath = entry.getPath();

        // If the parent file's separator is not '/' (the default entry separator), replace '/' occurrences by
        // the parent file's separator. For local files Under Windows, this allows entries' path to have '\' separators.
        String fileSeparator = getSeparator();
        if(!fileSeparator.equals("/"))
            entryPath = entryPath.replace("/", fileSeparator);

        // Cache AbstractArchiveEntryFile instances so that there is only one AbstractArchiveEntryFile corresponding to 
        // the same entry at any given time, to avoid attribute inconsistencies.

        AbstractArchiveEntryFile entryFile = archiveEntryFiles.get(entry);
        if(entryFile==null) {
            FileURL archiveURL = getURL();
            FileURL entryURL = (FileURL)archiveURL.clone();
            entryURL.setPath(addTrailingSeparator(archiveURL.getPath()) + entryPath);

            // Create an RO and RW entry file, depending on whether this archive file is RO or RW
            entryFile = this instanceof AbstractRWArchiveFile
                ?new RWArchiveEntryFile(
                  entryURL,
                  this,
                  entry
                )
                :new ROArchiveEntryFile(
                      entryURL,
                      this,
                      entry
                );

            entryFile.setParent(parentFile);

            archiveEntryFiles.put(entry, entryFile);
        }
        return FileFactory.wrapArchive(entryFile);
    }


    /**
     * Shorthand for {@link #getArchiveEntryFile(String)} called with the given entry's path.
     *
     * @param entry an entry contained by this archive
     * @return an AbstractFile that corresponds to the given entry
     * @throws IOException if neither the entry nor its parent exist within the archive
     * @throws UnsupportedFileOperationException if {@link FileOperation#READ_FILE} operations are not supported by the
     * underlying file protocol.
     */
    public AbstractFile getArchiveEntryFile(ArchiveEntry entry) throws IOException, UnsupportedFileOperationException {
        return getArchiveEntryFile(entry.getPath());
    }

    /**
     * Creates and returns an AbstractFile that corresponds to the given entry path within the archive.
     * The requested entry may or may not exist in the archive, the {@link #exists()} method of the returned entry file
     * can be used to find this out. However, if the requested entry does not exist in the archive and is
     * not located at the top level (i.e. is located in a subfolder), its parent folder must exist in the archive or
     * else an <code>IOException</code> will be thrown.
     *
     * <p>Important note: the given path's separator character must be '/' and the path must be relative to the
     * archive's root, i.e. not start with a leading '/', otherwise the entry will not be found.</p>
     *
     * @param entryPath path to an entry within this archive
     * @return an AbstractFile that corresponds to the given entry path
     * @throws IOException if neither the entry nor its parent exist within the archive
     * @throws UnsupportedFileOperationException if {@link FileOperation#READ_FILE} operations are not supported by the
     * underlying file protocol.
     */
    public AbstractFile getArchiveEntryFile(String entryPath) throws IOException, UnsupportedFileOperationException {
        // Make sure the entries tree is created and up-to-date
        checkEntriesTree();

        // Todo: check if that's really necessary / if there is a way to remove this
        entryPath = entryPath.replace('\\', '/');

        // Find the entry node corresponding to the given path
        DefaultMutableTreeNode entryNode = entryTreeRoot.findEntryNode(entryPath);

        if(entryNode==null) {
            int depth = ArchiveEntry.getDepth(entryPath);

            AbstractFile parentFile;
            if(depth==1)
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

            return getArchiveEntryFile(new ArchiveEntry(entryPath, false, 0, 0, false), parentFile);
        }

        return getArchiveEntryFile(entryNode);
    }

    /**
     * Creates and returns an {@link AbstractFile} instance corresponding to the given entry node.
     * This method recurses to resolve the entry's parent file.
     *
     * @param entryNode tree node corresponding to the entry for which to return a file
     * @return an {@link AbstractFile} instance corresponding to the given entry node
     */
    protected AbstractFile getArchiveEntryFile(DefaultMutableTreeNode entryNode) throws IOException {
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)entryNode.getParent();
        return getArchiveEntryFile(
                (ArchiveEntry)entryNode.getUserObject(),
                parentNode==entryTreeRoot
                    ?this
                    :getArchiveEntryFile(parentNode)
        );
    }

    
    //////////////////////
    // Abstract methods //
    //////////////////////
	
    /**
     * Returns an iterator of {@link ArchiveEntry} that iterates through all the entries of this archive.
     * Implementations of this method should as much as possible return entries in their "natural order", i.e. the order
     * in which they are stored in the archive.
     * <p>
     * This method is called the first time one of the <code>ls()</code> is called. It will not be called anymore,
     * unless the file's date has changed since the last time one of the <code>ls()</code> methods was called.
     * </p>
     *
     * @return an iterator of {@link ArchiveEntry} that iterates through all the entries of this archive
     * @throws IOException if an error occurred while reading the archive, either because the archive is corrupt or
     * because of an I/O error
     * @throws UnsupportedFileOperationException if {@link FileOperation#READ_FILE} operations are not supported by the
     * underlying file protocol.
     */
    public abstract ArchiveEntryIterator getEntryIterator() throws IOException, UnsupportedFileOperationException;

    /**
     * Returns an <code>InputStream</code> to read from the given archive entry. The specified {@link ArchiveEntry}
     * instance must be one of the entries that were returned by the {@link ArchiveEntryIterator} returned by
     * {@link #getEntryIterator()}.
     *
     * @param entry the archive entry to read
     * @param entryIterator the iterator that is used to iterate through entries by the caller (if any). This parameter
     * may be <code>null</code>, but when it is known, specifying may improve the performance of this method
     * by an order of magnitude.
     * @return an <code>InputStream</code> to read from the given archive entry
     * @throws IOException if an error occurred while reading the archive, either because the archive is corrupt or
     * because of an I/O error, or if the given entry wasn't found in the archive
     * @throws UnsupportedFileOperationException if {@link FileOperation#READ_FILE} operations are not supported by the 
     * underlying file protocol.
     */
    public abstract InputStream getEntryInputStream(ArchiveEntry entry, ArchiveEntryIterator entryIterator) throws IOException, UnsupportedFileOperationException;

    /**
     * Returns <code>true</code> if this archive file is writable, i.e. is capable of adding and deleting entries from
     * the underlying archive file.
     *
     * <p>
     * This method is implemented by {@link com.mucommander.commons.file.AbstractROArchiveFile} and
     * {@link com.mucommander.commons.file.AbstractRWArchiveFile} to respectively return <code>false</code> and
     * <code>true</code>. This method may be overridden by <code>AbstractRWArchiveFile</code> implementations if write
     * access is only available under certain conditions, for example if it requires random write access to the
     * proxied archive file (which may not always be available).
     * Therefore, this method should be used to test if an <code>AbstractArchiveFile</code> is writable, rather than
     * testing if it is an instance of <code>AbstractRWArchiveFile</code>.
     * </p>
     *
     * @return <code>true</code> if this archive is writable, i.e. is capable of adding and deleting entries from
     * the underlying archive file.
     */
    public abstract boolean isWritable();


    /////////////////////////////////////////
    // Partial AbstractFile implementation //
    /////////////////////////////////////////

    @Override
    public boolean isArchive() {
        return exists() && !isDirectory();
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * This method is overridden to list and return the topmost entries contained by this archive.
     * The returned files are {@link AbstractArchiveEntryFile} instances.
     *
     * @return the topmost entries contained by this archive
     * @throws IOException if the archive entries could not be listed
     * @throws UnsupportedFileOperationException if {@link FileOperation#READ_FILE} operations are not supported by the
     * underlying file protocol.
     */
    @Override
    public AbstractFile[] ls() throws IOException, UnsupportedFileOperationException {
        // Delegate to the ancestor if this file isn't actually an archive
        if(!isArchive())
            return super.ls();

        // Make sure the entries tree is created and up-to-date
        checkEntriesTree();

        return ls(entryTreeRoot, this, null, null);
    }

    /**
     * This method is overridden to list and return the topmost entries contained by this archive, filtering out
     * the ones that do not match the specified {@link FilenameFilter}. The returned files are {@link AbstractArchiveEntryFile}
     * instances.
     *
     * @param filter the FilenameFilter to be used to filter files out from the list, may be <code>null</code>
     * @return the topmost entries contained by this archive
     * @throws IOException if the archive entries could not be listed
     * @throws UnsupportedFileOperationException if {@link FileOperation#READ_FILE} operations are not supported by the
     * underlying file protocol.
     */
    @Override
    public AbstractFile[] ls(FilenameFilter filter) throws IOException, UnsupportedFileOperationException {
        // Delegate to the ancestor if this file isn't actually an archive
        if(!isArchive())
            return super.ls(filter);

        // Make sure the entries tree is created and up-to-date
        checkEntriesTree();

        return ls(entryTreeRoot, this, filter, null);
    }

    /**
     * This method is overridden to list and return the topmost entries contained by this archive, filtering out
     * the ones that do not match the specified {@link FileFilter}. The returned files are {@link AbstractArchiveEntryFile} instances.
     *
     * @param filter the FilenameFilter to be used to filter files out from the list, may be <code>null</code>
     * @return the topmost entries contained by this archive
     * @throws IOException if the archive entries could not be listed
     * @throws UnsupportedFileOperationException if {@link FileOperation#READ_FILE} operations are not supported by the
     * underlying file protocol.
     */
    @Override
    public AbstractFile[] ls(FileFilter filter) throws IOException, UnsupportedFileOperationException {
        // Delegate to the ancestor if this file isn't actually an archive
        if(!isArchive())
            return super.ls(filter);

        // Make sure the entries tree is created and up-to-date
        checkEntriesTree();

        return ls(entryTreeRoot, this, null, filter);
    }

    // Note: do not override #isDirectory() to always return true, as AbstractArchiveFile instances may be created when
    // the file does not exist yet, and then be mkdir(): in that case, the file will be a directory and not an archive.
}
