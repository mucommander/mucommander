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

package com.mucommander.ui.main.table;

import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.filter.FileFilter;
import com.mucommander.file.impl.CachedFile;
import com.mucommander.file.util.FileComparator;
import com.mucommander.file.util.FileSet;
import com.mucommander.text.CustomDateFormat;
import com.mucommander.text.SizeFormat;
import com.mucommander.text.Translator;

import javax.swing.table.AbstractTableModel;
import java.util.Date;


/**
 * This class maps table cells onto file attributes.
 *
 * @author Maxence Bernard
 */
public class FileTableModel extends AbstractTableModel implements Columns, ConfigurationListener {

    /** Cached file instances */
    private AbstractFile cachedFiles[];

    /** Index array */
    private int fileArrayIndex[];

    /** Cell values cache */
    private Object cellValuesCache[][];

    /** Marked rows array */
    private boolean rowMarked[];
	
    /** Marked files FileSet */
    private FileSet markedFiles;
    /** Combined size of files currently marked */
    private long markedTotalSize;

    private AbstractFile currentFolder;
    private AbstractFile parent;

    private int sortByCriterion = NAME;
    private boolean ascendingOrder = false;
    private boolean foldersFirst = MuConfiguration.getVariable(MuConfiguration.SHOW_FOLDERS_FIRST, MuConfiguration.DEFAULT_SHOW_FOLDERS_FIRST);

    /** True if name column temporarily editable */
    private boolean nameColumnEditable;

    private static boolean displayCompactSize = MuConfiguration.getVariable(MuConfiguration.DISPLAY_COMPACT_FILE_SIZE,
                                                                                 MuConfiguration.DEFAULT_DISPLAY_COMPACT_FILE_SIZE);
    /** String used as size information for directories */
    public final static String DIRECTORY_SIZE_STRING = "<DIR>";

    static final String[] COLUMN_LABELS;

    static {
        COLUMN_LABELS = new String[5];
        COLUMN_LABELS[EXTENSION]   = Translator.get("extension");
        COLUMN_LABELS[NAME]        = Translator.get("name");
        COLUMN_LABELS[SIZE]        = Translator.get("size");
        COLUMN_LABELS[DATE]        = Translator.get("date");
        COLUMN_LABELS[PERMISSIONS] = Translator.get("permissions");
    }

    public FileTableModel() {
        MuConfiguration.addConfigurationListener(this);

        // Arrays init to avoid NullPointerExcepions until setCurrentFolder()
        // gets called for the first time
        cachedFiles = new AbstractFile[0];
        fileArrayIndex = new int[0];
        cellValuesCache = new Object[0][4];
        rowMarked = new boolean[0];
        markedFiles = new FileSet();
    }

	
    public synchronized AbstractFile getCurrentFolder() {
        return currentFolder;
    }
	
    public synchronized boolean hasParentFolder() {
        return parent!=null;
    }

    public synchronized AbstractFile getParentFolder() {return parent;}
	
	
    public synchronized void setCurrentFolder(AbstractFile folder, AbstractFile children[]) {	
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(""+folder);
        int nbFiles = children.length;
        this.currentFolder = folder;
        this.parent = folder.getParentSilently();

        // Initialize file indexes and create CachedFile instances to speed up table display and navigation
        this.cachedFiles = children;
        this.fileArrayIndex = new int[nbFiles];
        for(int i=0; i<nbFiles; i++) {
            fileArrayIndex[i] = i;
            cachedFiles[i] = new CachedFile(children[i], true);
        }

        // Reset marked files
        int nbRows = getRowCount();
        this.rowMarked = new boolean[nbRows];
        this.markedFiles = new FileSet(folder);
        this.markedTotalSize = 0;

//if(com.mucommander.Debug.ON) com.mucommander.Debug.time();

        // Init and fill cell cache to speed up table even more
        this.cellValuesCache = new Object[nbRows][4];
        fillCellCache();

//if(com.mucommander.Debug.ON) com.mucommander.Debug.time();

        // Sort the new folder using the current sort criteria, ascending/descending order and 'folders first' value
        sortBy(sortByCriterion, ascendingOrder, foldersFirst);

//if(com.mucommander.Debug.ON) com.mucommander.Debug.time();
    }

	
    /**
     * Retrieves all cell values and stores them in an array for fast access.
     */
    public synchronized void fillCellCache() {
        int len = cellValuesCache.length;
        if(len==0)
            return;
		
        // Special '..' file
        if(parent!=null) {
            cellValuesCache[0][NAME-1] = "..";
            cellValuesCache[0][SIZE-1] = DIRECTORY_SIZE_STRING;
            cellValuesCache[0][DATE-1] =	CustomDateFormat.format(new Date(currentFolder.getDate()));
//            cellValuesCache[0][PERMISSIONS-1] =	parent.getPermissionsString();
            // Don't display parent's permissions as they can have a different format from the folder contents
            // (e.g. for archives) and this looks weird
            cellValuesCache[0][PERMISSIONS-1] =	"";
        }
		
        AbstractFile file;
        int fileIndex = 0;
        for(int i=parent==null?0:1; i<len; i++) {
            file = getCachedFileAtRow(i);
            int cellIndex = fileArrayIndex[fileIndex]+(parent==null?0:1);
            cellValuesCache[cellIndex][NAME-1] = file.getName();
            cellValuesCache[cellIndex][SIZE-1] = file.isDirectory()?DIRECTORY_SIZE_STRING: SizeFormat.format(file.getSize(), (displayCompactSize? SizeFormat.DIGITS_SHORT: SizeFormat.DIGITS_FULL)|(displayCompactSize? SizeFormat.UNIT_SHORT: SizeFormat.UNIT_NONE)| SizeFormat.INCLUDE_SPACE| SizeFormat.ROUND_TO_KB);
            cellValuesCache[cellIndex][DATE-1] = CustomDateFormat.format(new Date(file.getDate()));
            cellValuesCache[cellIndex][PERMISSIONS-1] = file.getPermissionsString();
            fileIndex++;
        }
    }
	
	
    /**
     * Returns a CachedFile instance of the file located at the given row index.
     * This method can return the parent folder file ('..') if a parent exists and rowIndex is 0.
     * 
     * <p>Returns <code>null</code> if rowIndex is lower than 0 or is greater than or equals
     * {@link #getRowCount() getRowCount()}.
     */
    synchronized AbstractFile getCachedFileAtRow(int rowIndex) {
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
     * Returns the file located at the given row index. 
     * This method can return the parent folder file ('..') if a parent exists and rowIndex is 0.
     *
     * <p>Returns <code>null</code> if rowIndex is lower than 0 or is greater than or equals
     * {@link #getRowCount() getRowCount()}.
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
     * Returns the row at which the given file is located, <code>-1<code> if the file is not in the current folder.
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
        FileComparator fc = new FileComparator(getComparatorCriterion(sortByCriterion), ascendingOrder, foldersFirst);
        while(left<=right) {
            mid = (right-left)/2 + left;
            midFile = getCachedFileAtRow(mid);
            // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("left="+left+" right="+right+" mid="+mid+" midFile="+midFile);
            if(midFile.equals(file))
                return mid;
//            if(compareTo(file, midFile, sortByCriterion, ascendingOrder)>0)
            if(fc.compare(file, midFile)>0)
                right = mid-1;
            else
                left = mid+1;
        }
		
        return -1;
    }

	
    /**
     * Returns the file located at the given index, not including the parent file.
     * Returns <code>null</code> if fileIndex is lower than 0 or is greater than or equals {@link #getFileCount() getFileCount()}.
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
     * Returns the actual number of files the current folder contains, excluding the parent '..' file
     * from the returned count.
     */
    public synchronized int getFileCount() {
        return cachedFiles.length;
    }

	
    /**
     * Returns if the given row is marked (!= selected). If the specified row corresponds to the special '..' parent
     * file, <code>false</code> will always be returned.
     */
    public synchronized boolean isRowMarked(int row) {
        if(row==0 && parent!=null)
            return false;

        return row<getRowCount() && rowMarked[fileArrayIndex[parent==null?row:row-1]];
    }


    /**
     * Marks the given row. If the specified row corresponds to the special '..' parent file, the row won't be marked.
     */
    public synchronized void setRowMarked(int row, boolean marked) {
        //		if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("row="+row+" marked="+marked+" rowMarked="+rowMarked[row]);

        if(row==0 && parent!=null)
            return;
			
        int rowIndex = parent==null?row:row-1;
		
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
            markedFiles.add(file);
        }
        else {
            // File size can equal -1 if not available, do not count that in total
            if(fileSize>0)
                markedTotalSize -= fileSize;
            markedFiles.remove(file);
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
     * Marks/unmarks the given file.
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
     * Returns a {@link com.mucommander.file.util.FileSet FileSet} with all currently marked files.
     * The returned FileSet is a clone of the internal FileSet, so it can be safely modified.
     * 
     * <p>However, it won't be kept current : the returned FileSet is just a snapshot
     * which might not reflect the current marked files state after this method has returned and additional
     * files have been marked/unmarked.
     *
     * @return the FileSet containing all the files that are currently marked
     */
    public synchronized FileSet getMarkedFiles() {
        return getMarkedFiles(true);
    }


    /**
     * Same usage as {@link #getMarkedFiles()} except that it can be specified whether the returned
     * {@link com.mucommander.file.util.FileSet} should be cloned or not.
     *
     * <p>Not cloning the FileSet can be used for efficiency reasons when the FileSet is only accessed,
     * but great precaution must be taken to ensure that the FileSet is never modified.</p>
     *
     * @param cloneFileSet specifies whether the internal marked files FileSet should be cloned or not
     * @return the FileSet containing all the files currently marked
     */
    public synchronized FileSet getMarkedFiles(boolean cloneFileSet) {
        return cloneFileSet?(FileSet)markedFiles.clone():markedFiles;
    }


    /**
     * Return the (already computed) number of marked files, much faster than retrieving marked files
     * and counting them.
     */
    public int getNbMarkedFiles() {
        return markedFiles.size();
    }

	
    /**
     * Return the (already computed) combined size of marked files, much faster than retrieving marked files
     * and calculating the total size.
     */
    public long getTotalMarkedSize() {
        return markedTotalSize;
    }

    /**
     * Makes name column temporarily editable, should only be called by FileTable.
     */
    void setNameColumnEditable(boolean editable) {
        this.nameColumnEditable = editable;
    }

	
    //////////////////
    // Sort methods //
    //////////////////

    /**
     * Translates {@link Columns} int values into {@link FileComparator} criteria.
     */
    private static int getComparatorCriterion(int sortByCriterion) {
        int comparatorCriterion;
        switch(sortByCriterion) {
            case NAME:
                comparatorCriterion = FileComparator.NAME_CRITERION;
                break;
            case SIZE:
                comparatorCriterion = FileComparator.SIZE_CRITERION;
                break;
            case DATE:
                comparatorCriterion = FileComparator.DATE_CRITERION;
                break;
            case EXTENSION:
                comparatorCriterion = FileComparator.EXTENSION_CRITERION;
                break;
            case PERMISSIONS:
                comparatorCriterion = FileComparator.PERMISSIONS_CRITERION;
                break;
            default:
                comparatorCriterion = FileComparator.NAME_CRITERION;
        }

        return comparatorCriterion;
    }


    /**
     * Sorts rows by the given criterion, by ascending or descending order, showing folders first or mixing them with
     * regular files.
     */
    public synchronized void sortBy(int criterion, boolean ascending, boolean foldersFirst)  {		// boolean ascending
        this.sortByCriterion = criterion;
        this.ascendingOrder = ascending;
        this.foldersFirst = foldersFirst;

        sort(new FileComparator(getComparatorCriterion(criterion), ascending, foldersFirst), 0, fileArrayIndex.length-1);
    }


    /**
     * Quick sort implementation (95/01/31 James Gosling).
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
            if (fc.compare(cachedFiles[fileArrayIndex[lo]],cachedFiles[fileArrayIndex[hi]])<0) {
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
            while (fc.compare(cachedFiles[fileArrayIndex[lo]], pivot)>=0 && lo < hi) {
                lo++;
            }

            // Search backward from files[hi] until element is found that
            // is less than the pivot, or lo >= hi
            while (fc.compare(pivot, cachedFiles[fileArrayIndex[hi]])>=0 && lo < hi ) {
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
        return 5;	// icon, name, size, date and permissions
    }

    public String getColumnName(int columnIndex) {return COLUMN_LABELS[columnIndex];}

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
        if(columnIndex==EXTENSION)
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
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // Name column can temporarily be made editable by FileTable
        // but parent file '..' name should never be editable
        if(columnIndex==NAME && (parent==null || rowIndex!=0))
            return nameColumnEditable;
	
        return false;
    }


    ///////////////////////////////////
    // ConfigurationListener methods //
    ///////////////////////////////////
	
    /**
     * Listens to some configuration variables.
     */
    public synchronized void configurationChanged(ConfigurationEvent event) {
        String var = event.getVariable();
		
        if (var.equals(MuConfiguration.DISPLAY_COMPACT_FILE_SIZE))
            displayCompactSize = event.getBooleanValue();
    }
}
