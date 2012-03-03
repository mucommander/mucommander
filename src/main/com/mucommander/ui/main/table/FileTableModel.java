/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.ui.main.table;

import java.util.Date;

import javax.swing.table.AbstractTableModel;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.filter.FileFilter;
import com.mucommander.commons.file.impl.CachedFile;
import com.mucommander.commons.file.util.FileComparator;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.text.CustomDateFormat;
import com.mucommander.text.SizeFormat;


/**
 * This class maps table cells onto file attributes.
 *
 * @author Maxence Bernard
 */
public class FileTableModel extends AbstractTableModel {

    /** The current folder */
    private AbstractFile currentFolder;

    /** Date of the current folder when it was changed */
    private long currentFolderDateSnapshot;

    /** The current folder's parent folder, may be null */
    private AbstractFile parent;

    /** Cached file instances */
    private AbstractFile cachedFiles[];

    /** Index array */
    private int fileArrayIndex[];

    /** Cell values cache */
    private Object cellValuesCache[][];

    /** Marked rows array */
    private boolean rowMarked[];
	
    /** Combined size of files currently marked */
    private long markedTotalSize;

    /** Number of files currently marked */
    private int nbRowsMarked;

    /** Contains sort-related variables */
    private SortInfo sortInfo;

    /** True if the name column is temporarily editable */
    private boolean nameColumnEditable;

    /** SizeFormat format used to create the size column's string */
    private static int sizeFormat;

    /** String used as size information for directories */
    public final static String DIRECTORY_SIZE_STRING = "<DIR>";


    static {
        // Initialize the size column format based on the configuration
        setSizeFormat(MuConfigurations.getPreferences().getVariable(MuPreference.DISPLAY_COMPACT_FILE_SIZE,
                                                  MuPreferences.DEFAULT_DISPLAY_COMPACT_FILE_SIZE));
    }


    /**
     * Sets the SizeFormat format used to create the size column's string.
     *
     * @param compactSize true to use a compact size format, false for full size in bytes 
     */
    static void setSizeFormat(boolean compactSize) {
        if(compactSize)
            sizeFormat = SizeFormat.DIGITS_MEDIUM | SizeFormat.UNIT_SHORT | SizeFormat.ROUND_TO_KB;
        else
            sizeFormat = SizeFormat.DIGITS_FULL | SizeFormat.UNIT_NONE;

        sizeFormat |= SizeFormat.INCLUDE_SPACE;
    }


    /**
     * Creates a new FileTableModel, without any initial current folder.
     */
    public FileTableModel() {
        // Init arrays to avoid NullPointerExceptions until setCurrentFolder() gets called for the first time
        cachedFiles = new AbstractFile[0];
        fileArrayIndex = new int[0];
        cellValuesCache = new Object[0][Column.values().length-1];
        rowMarked = new boolean[0];
    }

    /**
     * Sets the {@link com.mucommander.ui.main.table.SortInfo} instance that describes how the associated table is
     * sorted.
     *
     * @param sortInfo SortInfo instance that describes how the associated table is sorted
     */
    void setSortInfo(SortInfo sortInfo) {
        this.sortInfo = sortInfo;
    }

    /**
     * Returns the current folder, i.e. the last folder set using {@link #setCurrentFolder(com.mucommander.commons.file.AbstractFile, com.mucommander.commons.file.AbstractFile[])}.
     *
     * @return the current folder
     */
    public synchronized AbstractFile getCurrentFolder() {
        return currentFolder;
    }

    /**
     * Returns the date of the current folder, when it was set using {@link #setCurrentFolder(com.mucommander.commons.file.AbstractFile, com.mucommander.commons.file.AbstractFile[])}.
     * In other words, the returned date is a snapshot of the current folder's date which is never updated.
     *
     * @return Returns the date of the current folder, when it was set using #setCurrentFolder(Abstract, Abstract[])
     */
    public synchronized long getCurrentFolderDateSnapshot() {
        return currentFolderDateSnapshot;
    }

    /**
     * Returns <code>true</code> if the current folder has a parent.
     *
     * @return <code>true</code> if the current folder has a parent
     */
    public synchronized boolean hasParentFolder() {
        return parent!=null;
    }

    /**
     * Returns the current folder's parent if there is one, <code>null</code> otherwise.
     *
     * @return the current folder's parent if there is one, <code>null</code> otherwise
     */
    public synchronized AbstractFile getParentFolder() {
        return parent;
    }

    /**
     * Returns the index of the first row that can be marked/unmarked : <code>1</code> if the current folder has a
     * parent folder, <code>0</code> otherwise (parent folder row '..' cannot be marked).
     *
     * @return the index of the first row that can be marked/unmarked
     */
    public int getFirstMarkableRow() {
        return parent==null?0:1;
    }

    /**
     * Sets the current folder and its children.
     *
     * @param folder the current folder
     * @param children the current folder's children
     */
    synchronized void setCurrentFolder(AbstractFile folder, AbstractFile children[]) {
        int nbFiles = children.length;

        this.currentFolder = (folder instanceof CachedFile)?folder:new CachedFile(folder, true);

        this.parent = currentFolder.getParent();    // Note: the returned parent is a CachedFile instance
        if(parent!=null) {
            // Pre-fetch the attributes that are used by the table renderer and some actions.
            prefetchCachedFileAttributes(parent);
        }

        // Initialize file indexes and create CachedFile instances to speed up table display and navigation
        this.cachedFiles = children;
        this.fileArrayIndex = new int[nbFiles];
        AbstractFile file;
        for(int i=0; i<nbFiles; i++) {
            file = new CachedFile(children[i], true);

            // Pre-fetch the attributes that are used by the table renderer and some actions.
            prefetchCachedFileAttributes(file);

            cachedFiles[i] = file;
            fileArrayIndex[i] = i;
        }

        // Reset marked files
        int nbRows = getRowCount();
        this.rowMarked = new boolean[nbRows];
        this.markedTotalSize = 0;
        this.nbRowsMarked = 0;

        // Init and fill cell cache to speed up table even more
        this.cellValuesCache = new Object[nbRows][Column.values().length-1];

        fillCellCache();
    }

    /**
     * Pre-fetch the attributes that are used by the table renderer and some actions from the given CachedFile.
     * By doing so, the attributes will be available when the associated getters are called and thus the methods won't
     * be I/O bound and will not lock.
     *
     * @param cachedFile a CachedFile instance from which to pre-fetch attributes
     */
    private static void prefetchCachedFileAttributes(AbstractFile cachedFile) {
        cachedFile.isDirectory();
        cachedFile.isBrowsable();
        cachedFile.isHidden();
        // Pre-fetch isSymlink attribute and if the file is a symlink, pre-fetch the canonical file and its attributes
        if(cachedFile.isSymlink()) {
            AbstractFile canonicalFile = cachedFile.getCanonicalFile();
            if(canonicalFile!=cachedFile)   // Cheap test to prevent infinite recursion on bogus file implementations
                prefetchCachedFileAttributes(canonicalFile);
        }
    }

	
    /**
     * Retrieves all cell values and stores them in an array for fast access.
     */
    synchronized void fillCellCache() {
        int len = cellValuesCache.length;
        if(len==0)
            return;
		
        // Special '..' file
        if(parent!=null) {
            cellValuesCache[0][Column.NAME.ordinal()-1] = "..";
            cellValuesCache[0][Column.SIZE.ordinal()-1] = DIRECTORY_SIZE_STRING;
            currentFolderDateSnapshot = currentFolder.getDate();
            cellValuesCache[0][Column.DATE.ordinal()-1] =	CustomDateFormat.format(new Date(currentFolderDateSnapshot));
            // Don't display parent's permissions as they can have a different format from the folder contents
            // (e.g. for archives) and this looks weird
            cellValuesCache[0][Column.PERMISSIONS.ordinal()-1] = "";
            cellValuesCache[0][Column.OWNER.ordinal()-1] = "";
            cellValuesCache[0][Column.GROUP.ordinal()-1] = "";
        }
		
        AbstractFile file;
        int fileIndex = 0;

        for(int i=parent==null?0:1; i<len; i++) {
            file = getCachedFileAtRow(i);
            int cellIndex = fileArrayIndex[fileIndex]+(parent==null?0:1);
            cellValuesCache[cellIndex][Column.NAME.ordinal()-1] = file.getName();
            cellValuesCache[cellIndex][Column.SIZE.ordinal()-1] = file.isDirectory()?DIRECTORY_SIZE_STRING:SizeFormat.format(file.getSize(), sizeFormat);
            cellValuesCache[cellIndex][Column.DATE.ordinal()-1] = CustomDateFormat.format(new Date(file.getDate()));
            cellValuesCache[cellIndex][Column.PERMISSIONS.ordinal()-1] = file.getPermissionsString();
            cellValuesCache[cellIndex][Column.OWNER.ordinal()-1] = file.getOwner();
            cellValuesCache[cellIndex][Column.GROUP.ordinal()-1] = file.getGroup();

            fileIndex++;
        }
    }
	
	
    /**
     * Returns a CachedFile instance of the file located at the given row index.
     * This method can return the parent folder file ('..') if a parent exists and rowIndex is 0.
     * 
     * <p>Returns <code>null</code> if rowIndex is lower than 0 or is greater than or equals
     * {@link #getRowCount() getRowCount()}.</p>
     *
     * @param rowIndex a row index, comprised between 0 and #getRowCount()
     * @return a CachedFile instance of the file located at the given row index
     */
    public synchronized AbstractFile getCachedFileAtRow(int rowIndex) {
        if(rowIndex==0 && parent!=null)
            return parent;
		
        if(parent!=null)
            rowIndex--;
		
        // Need to check that row index is not larger than actual number of rows
        // because if table has just been changed (rows have been removed),
        // JTable may have an old row count value and may try to repaint rows that are out of bounds.
        if(rowIndex>=0 && rowIndex<fileArrayIndex.length)
            return cachedFiles[fileArrayIndex[rowIndex]];
        return null;
    }

    /**
     * Returns the current folder's children. The returned array contains {@link CachedFile} instances, where
     * most attributes have already been fetched and cached.
     *
     * @return the current folder's children, as an array of CachedFile instances
     * @see #getFiles()
     */
    public synchronized AbstractFile[] getCachedFiles() {
        // Clone the array to make sure it can't be modified outside of this class
        AbstractFile[] cachedFilesCopy = new AbstractFile[cachedFiles.length];
        System.arraycopy(cachedFiles, 0, cachedFilesCopy, 0, cachedFiles.length);

        return cachedFilesCopy;
    }


    /**
     * Returns the file located at the given row index. 
     * This method can return the parent folder file ('..') if a parent exists and rowIndex is 0.
     *
     * <p>Returns <code>null</code> if rowIndex is lower than 0 or is greater than or equals
     * {@link #getRowCount() getRowCount()}.</p>
     *
     * @param rowIndex a row index, comprised between 0 and #getRowCount()
     * @return the file located at the given row index
     */
    public synchronized AbstractFile getFileAtRow(int rowIndex) {
        AbstractFile file = getCachedFileAtRow(rowIndex);
	
        if(file==null)
            return null;
        else if(file instanceof CachedFile)
            return ((CachedFile)file).getProxiedFile();
        else
            return file;
    }
	
    /**
     * Returns the current folder's children. The returned array contains {@link AbstractFile} instances, and not
     * CachedFile instances contrary to {@link #getCachedFiles()}.
     *
     * @return the current folder's children
     * @see #getCachedFiles()
     */
    public synchronized AbstractFile[] getFiles() {
        int nbFiles = cachedFiles.length;
        AbstractFile[] files = new AbstractFile[nbFiles];
        for(int i=0; i<nbFiles; i++)
            files[i] = cachedFiles[i]==null?null:((CachedFile)cachedFiles[i]).getProxiedFile();

        return files;
    }

    /**
     * Returns the index of the row where the given file is located, <code>-1<code> if the file is not in the
     * current folder.
     *
     * @param file the file for which to find the row index
     * @return the index of the row where the given file is located, <code>-1<code> if the file is not in the
     * current folder
     */
    public synchronized int getFileRow(AbstractFile file) {
        // Handle parent folder file
        if(parent!=null && file.equals(parent))
            return 0;

        // Use dichotomic binary search rather than a dumb linear search since file array is sorted,
        // complexity is reduced to O(log n) instead of O(n^2)
        int left = parent==null?0:1;
        int right = getRowCount()-1;
        int mid;
        AbstractFile midFile;
        FileComparator fc = getFileComparator(sortInfo);

        while(left<=right) {
            mid = (right-left)/2 + left;
            midFile = getCachedFileAtRow(mid);
            if(midFile.equals(file))
                return mid;
            if(fc.compare(file, midFile)<0)
                right = mid-1;
            else
                left = mid+1;
        }
		
        return -1;
    }

	
    /**
     * Returns the file located at the given index, not including the parent file.
     * Returns <code>null</code> if fileIndex is lower than 0 or is greater than or equals {@link #getFileCount() getFileCount()}.
     *
     * @param fileIndex index of a file, comprised between 0 and #getFileCount()
     * @return the file located at the given index, not including the parent file
     */
    public synchronized AbstractFile getFileAt(int fileIndex) {
        // Need to check that row index is not larger than actual number of rows
        // because if table has just been changed (rows have been removed),
        // JTable may have an old row count value and may try to repaint rows that are out of bounds.
        if(fileIndex>=0 && fileIndex<fileArrayIndex.length) {
            return ((CachedFile)cachedFiles[fileArrayIndex[fileIndex]]).getProxiedFile();
        }
    	return null;
    }

	
    /**
     * Returns the actual number of files the current folder contains, excluding the parent '..' file (if any).
     *
     * @return the actual number of files the current folder contains, excluding the parent '..' file (if any)
     */
    public synchronized int getFileCount() {
        return cachedFiles.length;
    }

	
    /**
     * Returns <code>true</code> if the given row is marked (/!\ not selected). If the specified row corresponds to the
     * special '..' parent file, <code>false</code> is always returned.
     *
     * @param row index of a row to test
     * @return <code>true</code> if the given row is marked
     */
    public synchronized boolean isRowMarked(int row) {
        if(row==0 && parent!=null)
            return false;

        return row<getRowCount() && rowMarked[fileArrayIndex[parent==null?row:row-1]];
    }


    /**
     * Marks/Unmarks the given row. If the specified row corresponds to the special '..' parent file, the row won't
     * be marked.
     *
     * @param row the row to mark/unmark
     * @param marked <code>true</code> to mark the row, <code>false</code> to unmark it
     */
    public synchronized void setRowMarked(int row, boolean marked) {
        if(row==0 && parent!=null)
            return;
			
        int rowIndex = parent==null?row:row-1;

        // Return if the row is already marked/unmarked
        if((marked && rowMarked[fileArrayIndex[rowIndex]]) || (!marked && !rowMarked[fileArrayIndex[rowIndex]]))
            return;

        AbstractFile file = getCachedFileAtRow(row);

        // Do not call getSize() on directories, it's unnecessary and the value is most likely not cached by CachedFile yet
        long fileSize = file.isDirectory()?0:file.getSize();

        // Update :
        // - Combined size of marked files
        // - marked files FileSet 
        if(marked) {
            // File size can equal -1 if not available, do not count that in total
            if(fileSize>0)
                markedTotalSize += fileSize;

            nbRowsMarked++;
        }
        else {
            // File size can equal -1 if not available, do not count that in total
            if(fileSize>0)
                markedTotalSize -= fileSize;

            nbRowsMarked--;
        }

        rowMarked[fileArrayIndex[rowIndex]] = marked;
    }


    /**
     * Marks/unmarks the given row range, delimited by the provided start row index and end row index (inclusive).
     * End row may be less, greater or equal to the start row.
     *
     * @param startRow index of the first row to mark/unmark
     * @param endRow index of the last row to mark/ummark, startRow may be less or greater than startRow
     * @param marked if true, all the rows within the range will be marked, unmarked otherwise
     */
    public void setRangeMarked(int startRow, int endRow, boolean marked) {
        if(endRow >= startRow) {
            for(int i= startRow; i<= endRow; i++)
                setRowMarked(i, marked);
        }
        else {
            for(int i= startRow; i>= endRow; i--)
                setRowMarked(i, marked);
        }
    }


    /**
     * Marks/Unmarks the given file.
     *
     * @param file the file to mark/unmark
     * @param marked <code>true</code> to mark the row, <code>false</code> to unmark it.
     */
    public synchronized void setFileMarked(AbstractFile file, boolean marked) {
        int row = getFileRow(file);

    	if(row!=-1)
            setRowMarked(row, marked);
    }


    /**
     * Marks/unmarks the files that match the given {@link FileFilter}.
     *
     * @param filter the FileFilter to match the files against
     * @param marked if true, matching files will be marked, if false, they will be unmarked
     */
    public synchronized void setFilesMarked(FileFilter filter, boolean marked) {
        int nbFiles = getRowCount();
        for(int i=parent==null?0:1; i<nbFiles; i++) {
            if(filter.match(getCachedFileAtRow(i)))
                setRowMarked(i, marked);
        }
    }


    /**
     * Returns a {@link com.mucommander.commons.file.util.FileSet FileSet} with all currently marked files.
     * <p>
     * The returned <code>FileSet</code> is a freshly created instance, so it can be safely modified.
     & However, it won't be kept current : the returned FileSet is just a snapshot
     * which might not reflect the current marked files state after this method has returned and additional
     * files have been marked/unmarked.
     * </p>
     *
     * @return a FileSet containing all the files that are currently marked
     */
    public synchronized FileSet getMarkedFiles() {
        FileSet markedFiles = new FileSet(currentFolder, nbRowsMarked);
        int nbRows = getRowCount();

        if(parent==null) {
            for(int i=0; i<nbRows; i++) {
                if(rowMarked[fileArrayIndex[i]])
                    markedFiles.add(getFileAtRow(i));
            }
        }
        else {
            for(int i=1, iMinusOne=0; i<nbRows; i++) {
                if(rowMarked[fileArrayIndex[iMinusOne]])
                    markedFiles.add(getFileAtRow(i));

                iMinusOne = i;
            }
        }

        return markedFiles;
    }

    /**
     * Returns the number of marked files. This number is pre-calculated so calling this method is much faster than
     * retrieving the list of marked files and counting them.
     *
     * @return the number of marked files
     */
    public int getNbMarkedFiles() {
        return nbRowsMarked;
    }

	
    /**
     * Returns the combined size of marked files. This number is pre-calculated so calling this method is much faster
     * than retrieving the list of marked files and calculating their combined size.
     *
     * @return the combined size of marked files
     */
    public long getTotalMarkedSize() {
        return markedTotalSize;
    }

    /**
     * Makes the name column temporarily editable. This method should only be called by FileTable.
     *
     * @param editable <code>true</code> to make the name column editable, false to prevent it from being edited
     */
    void setNameColumnEditable(boolean editable) {
        this.nameColumnEditable = editable;
    }

	
    //////////////////
    // Sort methods //
    //////////////////

    private static FileComparator getFileComparator(SortInfo sortInfo) {
        return new FileComparator(sortInfo.getCriterion().getFileComparatorCriterion(), sortInfo.getAscendingOrder(), sortInfo.getFoldersFirst());
    }


    /**
     * Sorts rows by the current criterion, ascending/descending order and 'folders first' value.
     */
    synchronized void sortRows()  {
        sort(getFileComparator(sortInfo), 0, fileArrayIndex.length-1);
    }


    /**
     * Quick sort implementation, based on James Gosling's implementation.
     */
    private void sort(FileComparator fc, int lo0, int hi0) {
        int lo = lo0;
        int hi = hi0;

        int temp;

        if (lo >= hi) {
            return;
        }
        else if( lo == hi - 1 ) {
            // sort a two element list by swapping if necessary
            if (fc.compare(cachedFiles[fileArrayIndex[lo]],cachedFiles[fileArrayIndex[hi]])>0) {
                temp = fileArrayIndex[lo];
                fileArrayIndex[lo] = fileArrayIndex[hi];
                fileArrayIndex[hi] = temp;
            }
            return;
        }

        // Pick a pivot and move it out of the way
        int pivotIndex = fileArrayIndex[(lo + hi) / 2];
        fileArrayIndex[(lo + hi) / 2] = fileArrayIndex[hi];
        fileArrayIndex[hi] = pivotIndex;
        AbstractFile pivot = cachedFiles[pivotIndex];

        while( lo < hi ) {
            // Search forward from files[lo] until an element is found that
            // is greater than the pivot or lo >= hi
            while (fc.compare(cachedFiles[fileArrayIndex[lo]], pivot)<=0 && lo < hi) {
                lo++;
            }

            // Search backward from files[hi] until element is found that
            // is less than the pivot, or lo >= hi
            while (fc.compare(pivot, cachedFiles[fileArrayIndex[hi]])<=0 && lo < hi ) {
                hi--;
            }

            // Swap elements files[lo] and files[hi]
            if( lo < hi ) {
                temp = fileArrayIndex[lo];
                fileArrayIndex[lo] = fileArrayIndex[hi];
                fileArrayIndex[hi] = temp;
            }
        }

        // Put the median in the "center" of the list
        fileArrayIndex[hi0] = fileArrayIndex[hi];
        fileArrayIndex[hi] = pivotIndex;

        // Recursive calls, elements files[lo0] to files[lo-1] are less than or
        // equal to pivot, elements files[hi+1] to files[hi0] are greater than
        // pivot.
        sort(fc, lo0, lo-1);
        sort(fc, hi+1, hi0);
    }


    //////////////////////////////////////////
    // Overriden AbstractTableModel methods //
    //////////////////////////////////////////
	
    public int getColumnCount() {
        return Column.values().length; // icon, name, size, date, permissions, owner, group
    }

    @Override
    public String getColumnName(int columnIndex) {
        return Column.valueOf(columnIndex).getLabel();
    }

    /**
     * Returns the total number of rows, including the special parent folder file '..', if there is one.
     */
    public synchronized int getRowCount() {
        return fileArrayIndex.length + (parent==null?0:1);
    }

		
    //	public Object getValueAt(int rowIndex, int columnIndex) {
    public synchronized Object getValueAt(int rowIndex, int columnIndex) {
        // Need to check that row index is not larger than actual number of rows
        // because if table has just been changed (rows have been removed),
        // JTable may have an old row count value and may try to repaint rows that are out of bounds.
        if(rowIndex>=getRowCount()) {
            // Returning null will have JTable ignore this row
            return null;
        }

        // Icon/extension column, return a null value
        Column column = Column.valueOf(columnIndex);
        if(column==Column.EXTENSION)
            return null;
		
        // Decrement column index for cellValuesCache array
        columnIndex--;
        // Handle special '..' file
        if(rowIndex==0 && parent!=null)
            return cellValuesCache[0][columnIndex];
        int fileIndex = parent==null?rowIndex:rowIndex-1;
        return cellValuesCache[fileArrayIndex[fileIndex]+(parent==null?0:1)][columnIndex];
    }

	
    /**
     * Returns <code>true</code> if name column has temporarily be made editable by FileTable
     * and given row doesn't correspond to parent file '..', <code>false</code> otherwise.
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // Name column can temporarily be made editable by FileTable
        // but parent file '..' name should never be editable
        if(Column.valueOf(columnIndex)==Column.NAME && (parent==null || rowIndex!=0))
            return nameColumnEditable;
	
        return false;
    }
}
