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

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.job.MoveJob;
import com.mucommander.text.CustomDateFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionKeymap;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.dialog.file.FileCollisionDialog;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.event.ActivePanelListener;
import com.mucommander.ui.event.TableSelectionListener;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.menu.TablePopupMenu;
import com.mucommander.ui.theme.*;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import java.util.WeakHashMap;


/**
 * Heavily modified JTable class which displays a folder's contents and allows file mouse and keyboard selection, marking and navigation.
 *
 * <p>JTable (which this class extends) provides the basics for file selection but its behavior has to be extended to allow file marking.</p>
 *
 * @author Maxence Bernard
 */
public class FileTable extends JTable implements Columns, MouseListener, MouseMotionListener, KeyListener, FocusListener, ActivePanelListener, ConfigurationListener, ThemeListener {
    private MainFrame mainFrame;
    private FolderPanel folderPanel;

    /** TableModel instance used by this JTable to get cells' values */
    private FileTableModel tableModel;
    /** TableCellRender instance used by this JTable to render cells */
    private FileTableCellRenderer cellRenderer;
    /** CellEditor used to edit filenames when clicked */
    private FilenameEditor filenameEditor;

    /** Current folder displayed by this FileTable */
    private AbstractFile currentFolder;
    /** Current parent folder */
    private AbstractFile folderParent;

    /** Row currently selected */
    private int currentRow;

    /** Current sort criteria */
    private int sortByCriteria = NAME;
    /** Ascending/Descending order for all columns */
    private boolean ascendingOrder[] =  {false, false, false, false, false};

    /** Visibility flag for each column */
    private boolean columnVisible[] = {true, true, true, true, true};

    // Used when right button is pressed and mouse is dragged
    private boolean markOnRightClick;
    private int lastDraggedRow = -1;

    // For UP/DOWN + shift
    private boolean markOnShift;
    // Used by shift+Click
    private int lastRow;

    //	/** is DOWN key down ? */
    //	private boolean downKeyDown;
    //	/** is UP key down ? */
    //	private boolean upKeyDown;
    //	/** is SPACE key down ? */
    //	private boolean spaceKeyDown;
    //	/** is INSERT key down ? */
    //	private boolean insertKeyDown;

    /** Allows to detect repeated key strokes of mark key (space/insert) */
    private boolean markKeyRepeated;
    /** In case of repeated mark keystrokes, true if last row has already been marked/unmarked */
    private boolean lastRowMarked;

    //	/** Timestamp of last status bar update when up, down, space or insert key is down */
    //	private long lastStatusBarUpdate;

    //	/** Number of milliseconds between 2 refreshes of the status bar when up, down, space or insert key is down */
    //	private final static int STATUS_BAR_REFRESH_RATE = 600;

    /** Timestamp of last row selection change */
    private long selectionChangedTimestamp;

    /** Timestamp of last double click */
    private long lastDoubleClickTimestamp;

    /** Maximum cell size for each column, except 'name' column */
    private int columnMaxWidth[];

    /** Is automatic columns sizing enabled ? */
    private boolean autoSizeColumnsEnabled;

    /** Should folders be displayed first, or mixed with regular files */
    private boolean showFoldersFirst = MuConfiguration.getVariable(MuConfiguration.SHOW_FOLDERS_FIRST, MuConfiguration.DEFAULT_SHOW_FOLDERS_FIRST);

    //	/** Editor component's height */
    //	private int editorRowHeight;

    /** <code>true</code> when current folder is being changed to avoid unnecessary repaints that cause flickering */
    private boolean ignorePaintCalls;

    /** Instance of the inner class that handles quick search */
    private QuickSearch quickSearch = new QuickSearch();

    /** TableSelectionListener instances registered to receive selection change events */
    private WeakHashMap tableSelectionListeners = new WeakHashMap();

    /** True when this table is the current or last active table in the MainFrame */
    private boolean isActiveTable;


    /** Minimum width for 'name' column when in automatic column sizing mode */
    private final static int RESERVED_NAME_COLUMN_WIDTH = 40;
    /** Miniumn column width when in automatic column sizing mode */
    private final static int MIN_COLUMN_AUTO_WIDTH = 20;
//    private final static int MIN_COLUMN_AUTO_WIDTH = 40;

    /** Delay in ms after which filename editor can be triggered when current row's filename cell is clicked */
    private final static int EDIT_NAME_CLICK_DELAY = 500;

    /** 'Mark/unmark selected file' action */
    private final static Class MARK_ACTION_CLASS = com.mucommander.ui.action.MarkSelectedFileAction.class;

    public FileTable(MainFrame mainFrame, FolderPanel folderPanel) {
        ThemeManager.addCurrentThemeListener(this);

        // It might seem that the table is never actually visible (being 'covered' by its cells
        // or the FolderPanel's viewport), and that this call is not at all necessary.
        // However, in some cases - such as a directory change - the table might be repainted
        // while empty, in which case its background color will actually be used. If it's
        // not set to Theme.FILE_TABLE_BACKGROUND_COLOR, flickering will occur.
        setBackground(ThemeManager.getCurrentColor(Theme.FILE_TABLE_BACKGROUND_COLOR));

        this.mainFrame = mainFrame;
        this.folderPanel = folderPanel;

        this.tableModel = new FileTableModel();
        this.cellRenderer = new FileTableCellRenderer(this);

//        // Enable double buffering on the JTable
//        setDoubleBuffered(true);

        setModel(tableModel);
        setTableHeader(new FileTableHeader(this));
        setShowGrid(false);
        setColumnSelectionAllowed(false);
        setIntercellSpacing(new Dimension(0,0));
        getTableHeader().setReorderingAllowed(true);
        // For some reason, MULTIPLE_INTERVAL_SELECTION has a weird behavior when selecting 2 items and switching tables
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        addMouseListener(this);
        folderPanel.addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);

        mainFrame.addActivePanelListener(this);

        // Remove any action mapped to the Escape key, since we need Escape to cancel folder change
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).getParent().remove(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE ,0));

// // Remove all shorcuts registered in InputMap
//InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
//KeyStroke ks[] = inputMap.allKeys();
//for(int i=0; i<ks.length; i++) {
//    if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Removing shortcut "+ks[i]);
//    inputMap.getParent().remove(ks[i]);
//}

// // Lists all InputMap keys used by JTable
//InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
//KeyStroke ks[] = inputMap.allKeys();
//for(int i=0; i<ks.length; i++)
//    System.out.println(ks[i].toString());

        // Setup columns
        TableColumnModel cm = getColumnModel();
        TableColumn column;

        // 'Extension' column
        column = cm.getColumn(EXTENSION);
        column.setHeaderRenderer(new FileTableHeaderRenderer());
        column.setCellEditor(null);
        column.setMinWidth(0);

        // 'Name' column
        FileTableHeaderRenderer hr = new FileTableHeaderRenderer();
        hr.setCurrent(true);
        column = cm.getColumn(NAME);
        column.setHeaderRenderer(hr);
        // Creates a single instance of FilenameEditor and make it the cell editor for the 'name' column.
        // Name cells will only be editable when filename is clicked.
        filenameEditor = new FilenameEditor(new JTextField());
        //		editorRowHeight = (int)filenameEditor.filenameField.getPreferredSize().getHeight();
        column.setCellEditor(filenameEditor);
        column.setMinWidth(0);

        // 'Size' column
        column = cm.getColumn(SIZE);
        column.setHeaderRenderer(new FileTableHeaderRenderer());
        column.setCellEditor(null);
        column.setMinWidth(0);

        // 'Date' column
        column = cm.getColumn(DATE);
        column.setHeaderRenderer(new FileTableHeaderRenderer());
        column.setCellEditor(null);
        column.setMinWidth(0);

        // 'Permissions' column
        column = cm.getColumn(PERMISSIONS);
        column.setHeaderRenderer(new FileTableHeaderRenderer());
        column.setCellEditor(null);
        column.setMinWidth(0);

        // Turn on auto columns sizing if enabled
        this.autoSizeColumnsEnabled = MuConfiguration.getVariable(MuConfiguration.AUTO_SIZE_COLUMNS, MuConfiguration.DEFAULT_AUTO_SIZE_COLUMNS);
        if(autoSizeColumnsEnabled)
            setAutoSizeColumnsEnabled(true);

        // Calculate row height, revalidate and repaint the table
        setRowHeight();

        // Monitor some configuration variables
        MuConfiguration.addConfigurationListener(this);

        // Listen to focus gain/lost events
        addFocusListener(this);

//        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }


    /**
     * Returns the FolderPanel that contains this FileTable.
     */
    public FolderPanel getFolderPanel() {
        return folderPanel;
    }


    /**
     * Returns <code>true/</code> if this table is the active one in the MainFrame.
     * Being the active table doesn't necessarily mean that it currently has focus, the focus can be in some other component
     * of the active {@link FolderPanel}, or nowhere in the MainFrame if the window is not in the foreground.
     *
     * <p>Use {@link #hasFocus()} to test if the table currently has focus.
     *
     * @see com.mucommander.ui.main.MainFrame#getActiveTable()
     */
    public boolean isActiveTable() {
        return this.isActiveTable;
    }


    /**
     * Convenience method that returns this table's model (the one that {@link #getModel()} returns),
     * as a FileTableModel, to avoid having to cast it.
     */
    public FileTableModel getFileTableModel() {
        return tableModel;
    }


    /**
     * Returns the QuickSearch inner class instance used by this FileTable. 
     */
    public FileTable.QuickSearch getQuickSearch() {
        return quickSearch;
    }


    /**
     * Returns the file that is currently selected (highlighted) or null if the parent folder '..' is currently selected.
     */
    public synchronized AbstractFile getSelectedFile() {
        return getSelectedFile(false, false);
    }

    /**
     * Returns the file that is currently selected (highlighted).
     *
     * @param includeParentFolder if <code>true</code> and parent folder '..' is currently selected, the parent folder
     * will be returned.
     */
    public synchronized AbstractFile getSelectedFile(boolean includeParentFolder) {
        return getSelectedFile(includeParentFolder, false);
    }


    /**
     * Returns the file that is currently selected (highlighted).
     *
     * @param includeParentFolder if true and the parent folder '..' is currently selected, the parent folder file
     * will be returned. If false, null will be returned if the parent folder file is currently selected.
     * @param returnCachedFile if true, a CachedFile corresponding to the currently selected file will be returned
     */
    public synchronized AbstractFile getSelectedFile(boolean includeParentFolder, boolean returnCachedFile) {
        if(tableModel.getRowCount()==0 || (!includeParentFolder && isParentFolderSelected()))
            return null;
        return returnCachedFile?tableModel.getCachedFileAtRow(currentRow):tableModel.getFileAtRow(currentRow);
    }


    /**
     * Returns selected files: marked files or currently selected file if no file is marked.
     */
    public FileSet getSelectedFiles() {
        FileSet selectedFiles = tableModel.getMarkedFiles();
        // if no row is marked, then add selected row if there is one, and if it is not parent folder
        if(selectedFiles.size()==0)	{
            AbstractFile selectedFile = getSelectedFile();
            if(selectedFile!=null)
                selectedFiles.add(selectedFile);
        }
        return selectedFiles;
    }


    /**
     * Returns true if the currently selected row/file is the parent folder '..' .
     */
    public boolean isParentFolderSelected() {
        return currentRow==0 && folderParent!=null;
    }


    /**
     * Returns true if the given row is the parent folder '..' .
     */
    public boolean isParentFolder(int row) {
        return row==0 && folderParent!=null;
    }


    /**
     * Returns the folder currently displayed by this FileTable.
     */
    public AbstractFile getCurrentFolder() {
        return currentFolder;
    }


    /**
     * Changes current folder, keeping current selection if folder hasn't changed.
     * Should only be called by FolderPanel!
     */
    public synchronized void setCurrentFolder(AbstractFile folder, AbstractFile children[]) {
        //if(com.mucommander.Debug.ON) com.mucommander.Debug.resetTimer();
        //if(com.mucommander.Debug.ON) com.mucommander.Debug.time();

        // Ignore calls to paint() until the folder is changed in order to avoid flickering and
        // other unwanted visual glitches
        this.ignorePaintCalls = true;

        // Stop quick search in case it was being used before folder change
        quickSearch.cancel();

        FileSet markedFiles = null;
        AbstractFile selectedFile = null;
        // If folder is the same as current folder, keep current selection and marked files after refresh
        if(currentFolder!=null && folder.equals(currentFolder)) {		// Note: currentFolder can be null
            // Save marked files
            markedFiles = tableModel.getMarkedFiles();
            // Save currently selected file
            selectedFile = getSelectedFile();
        }
        // If new folder is current folder's parent, select current folder in parent
        else if(folderParent!=null && folder.equals(folderParent)) {		// Note: folderParent can be null
            selectedFile = currentFolder;
        }
        // else selected row will be the first one

//         if(com.mucommander.Debug.ON) com.mucommander.Debug.time();
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("calling tableModel.setCurrentFolder");

        // Changes folder in TableModel
        tableModel.setCurrentFolder(folder, children);

        // if(com.mucommander.Debug.ON) com.mucommander.Debug.time();

        // Compute columns size once for all
        if(autoSizeColumnsEnabled) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("calling setMaxColumnsWidth()");
            setMaxColumnsWidth();
            //if(com.mucommander.Debug.ON) com.mucommander.Debug.time();
        }

        //if(com.mucommander.Debug.ON) com.mucommander.Debug.time();

        // Set new current folder and parent
        this.currentFolder = folder;
        this.folderParent = folder.getParent();

        // Find the new row to select
        int rowToSelect;
        if(selectedFile!=null) {
            // Find the row corresponding to the file to select
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("restoring previously selected file: "+selectedFile);
            rowToSelect = tableModel.getFileRow(selectedFile);
            // If file could not be found (file doesn't exist anymore), keep same row index
            // (the best in that case would be to find the closest file to the previously selected one,
            // using the current sort criteria and order)
            if(rowToSelect==-1) {
                int rowCount = tableModel.getRowCount();
                rowToSelect = currentRow<rowCount?currentRow:rowCount-1;
            }
        }
        else {
            rowToSelect = 0;
        }

        // Do not select row yet, but make sure the new current row is used
        currentRow = rowToSelect;

        // Restore previously marked files (if any / current folder hasn't changed)
        if(markedFiles!=null) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("restoring previously marked files: "+markedFiles);
            // Restore previsouly marked files
            int nbMarkedFiles = markedFiles.size();
            int fileRow;
            for(int i=0; i<nbMarkedFiles; i++) {
                fileRow = tableModel.getFileRow((AbstractFile)markedFiles.elementAt(i));
                if(fileRow!=-1)
                    tableModel.setRowMarked(fileRow, true);
            }

            // Notify registered listeners that currently marked files have changed on this FileTable
            fireMarkedFilesChangedEvent();
        }

        // Paint calls can be honoured again
        this.ignorePaintCalls = false;

        // Select the new current row.
        // We're calling this from within an invokeLater statement because on some earlier
        // versions of Java, not doing so would result in a corrupt display.
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    selectRow(currentRow);
                    resizeAndRepaint();

                    // Notify registered TableSelectionListener instances that the currently selected file has changed
                    fireSelectedFileChangedEvent();

                    //centerRow();
                }
            });

        //if(com.mucommander.Debug.ON) com.mucommander.Debug.time();

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("finished!");
    }


    /**
     * Calculates the maximum width cells occupy in each column.
     */
    private void setMaxColumnsWidth() {
        columnMaxWidth = new int[tableModel.getColumnCount()];

        FontMetrics fm = getFontMetrics(cellRenderer.getCellFont());
        int columnWidth;
        int stringWidth;
        // Calculate <DIR> string width once for all
        int dirStringWidth = fm.stringWidth(FileTableModel.DIRECTORY_SIZE_STRING);
        int rowCount = tableModel.getRowCount();
        String val;

        // Extension/Icon column has fixed width
        columnMaxWidth[EXTENSION] = (int)FileIcons.getStandardSize().getWidth();

        // Calculate maximum column size for date and size columns
        for(int colNum=SIZE; colNum<=PERMISSIONS; colNum++) {
            columnWidth = MIN_COLUMN_AUTO_WIDTH;	// Sets a minimum column size

            for(int rowNum=0; rowNum<rowCount; rowNum++) {
                val = (String)tableModel.getValueAt(rowNum, colNum);
                // No need to calculate <DIR> string's width each time
                if(colNum==SIZE && val.equals(FileTableModel.DIRECTORY_SIZE_STRING))
                    stringWidth = dirStringWidth;
                else
                    stringWidth = fm.stringWidth(val);

                if(stringWidth>columnWidth)
                    columnWidth = stringWidth;
            }

            columnMaxWidth[colNum] = columnWidth;
//            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Column "+colNum+" width = "+columnWidth+" viewport="+getPreferredScrollableViewportSize());
        }
    }

    /**
     * Sets row height based on current cell's font and border, revalidates and repaints this JTable.
     */
    private void setRowHeight() {
        // JTable.setRowHeight() revalidates and repaints the JTable 
        setRowHeight(2*CellLabel.CELL_BORDER_HEIGHT + Math.max(getFontMetrics(cellRenderer.getCellFont()).getHeight(), (int)FileIcons.getStandardSize().getHeight()));
        // Filename editor's row resize disabled because of Java bug #4398268 which prevents new rows from being visible after setRowHeight(row, height) has been called :/
        //		setRowHeight(Math.max(getFontMetrics(cellRenderer.getCellFont()).getHeight()+cellRenderer.CELL_BORDER_HEIGHT, editorRowHeight));
    }


    /**
     * Resizes columns by trying to give preferred width to size and date columns and all remaining space to name column.
     */
    //	private synchronized void autoSizeColumns() {
    private void autoSizeColumns() {
        if(columnMaxWidth==null)
            setMaxColumnsWidth();

        if(columnMaxWidth==null)
            return;

        TableColumnModel columnModel = getColumnModel();

        int tableWidth = getSize().width;
        int remainingWidth = tableWidth - RESERVED_NAME_COLUMN_WIDTH;
        int columnWidth;

        for(int colNum=EXTENSION; colNum<=PERMISSIONS; colNum++) {
            if(colNum==NAME)
                continue;

            if(columnVisible[colNum]) {
                // Add a column margin and leave a minimum space for the 'name' column
                columnWidth = Math.min(columnMaxWidth[colNum]+2*CellLabel.CELL_BORDER_WIDTH, remainingWidth);
            }
            else {
                // Make column invisible
                columnWidth = 0;
            }

            // Set desired column width
            columnModel.getColumn(convertColumnIndexToView(colNum)).setWidth(columnWidth);

            // Update subtotal
            remainingWidth -= columnWidth;
            if(remainingWidth<0)
                remainingWidth = 0;

            //			if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Remaining width = "+remainingWidth);
        }

//        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Name column width = "+(remainingWidth+RESERVED_NAME_COLUMN_WIDTH)+" jtable width = "+tableWidth);
        // Give remaining space to 'name' column
        columnModel.getColumn(convertColumnIndexToView(NAME)).setWidth(remainingWidth+RESERVED_NAME_COLUMN_WIDTH);
    }


    /**
     * Returns <code>true</code> if the auto-columns sizing feature is enabled.
     */
    public boolean isAutoSizeColumnsEnabled() {
        return this.autoSizeColumnsEnabled;
    }


    /**
     * Enables/disables auto-columns sizing, which automatically resizes columns to fit the table's width.
     */
    public void setAutoSizeColumnsEnabled(boolean enabled) {
        // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("viewportSize="+getPreferredScrollableViewportSize()+" table width="+getWidth()+" table preferred size="+getPreferredSize());
        this.autoSizeColumnsEnabled = enabled;
        if(autoSizeColumnsEnabled) {
            setAutoResizeMode(AUTO_RESIZE_OFF);
            getTableHeader().setResizingAllowed(false);
            setMaxColumnsWidth();
            // Will invoke doLayout()
            resizeAndRepaint();
        }
        else {
            setAutoResizeMode(AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            getTableHeader().setResizingAllowed(true);
        }
    }


    /**
     * Returns true if auto columns sizing is enabled.
     */
    public boolean getAutoSizeColumnsEnabled() {
        return autoSizeColumnsEnabled;
    }


    /**
     * Controls whether folders are displayed first in this FileTable or mixed with regular files.
     * After calling this method, the table is refreshed to reflect the change.
     * 
     * @param enabled if true, folders will be
     */
    public void setShowFoldersFirstEnabled(boolean enabled) {
        if(showFoldersFirst!=enabled) {
            this.showFoldersFirst = enabled;
            sortTable();
        }
    }

    /**
     * Returns true if folders are displayed first, false if they are mixed with regular files.
     */
    public boolean isShowFoldersFirstEnabled() {
        return showFoldersFirst;
    }


    /**
     * Selects the given file.
     */
    public void selectFile(AbstractFile file) {
        int row = tableModel.getFileRow(file);

        if(Debug.ON) Debug.trace("file="+file+" row="+row);

        if(row!=-1)
            selectRow(row);
    }


    /**
     * Makes the given row the currently selected one.
     */
    public void selectRow(int row) {
        this.changeSelection(row,0,false,false);
    }


    /**
     * Sets the given row as marked/unmarked in the table model, repaints the row to reflect the change,
     * and notifies registered {@link com.mucommander.ui.event.TableSelectionListener} that the files currently marked
     * on this FileTable have changed.
     *
     * <p>This method has no effect if the row corresponds to the parent folder row '..'.
     */
    public void setRowMarked(int row, boolean marked) {
        if(isParentFolder(row))
            return;

        tableModel.setRowMarked(row, marked);
        repaintRow(row);

        // Notify registered listeners that currently marked files have changed on this FileTable
        fireMarkedFilesChangedEvent();
    }


    /**
     * Sets the given file as marked/unmarked in the table model, repaints the corresponding row to reflect the change,
     * and notifies registered {@link com.mucommander.ui.event.TableSelectionListener} that currently marked files
     * have changed on this FileTable.
     */
    public void setFileMarked(AbstractFile file, boolean marked) {
        int row = tableModel.getFileRow(file);

        if(row!=-1)
            setRowMarked(row, marked);
    }


    /**
     * Marks or unmarks the current selected file (current row) and advance current row to the next one, 
     * with the following exceptions:
     * <ul>
     * <li>if quick search is active, this method does nothing
     * <li>if '..' file is selected, file is not marked but current row is still advanced to the next one
     * <li>if the {@link com.mucommander.ui.action.MarkSelectedFileAction} key event is repeated and the last file has already
     * been marked/unmarked since the key was last released, the file is not marked in order to avoid
     * marked/unmarked flaps when the mark key is kept pressed.
     * </ul>
     *
     * @see com.mucommander.ui.action.MarkSelectedFileAction
     */
    public void markSelectedFile() {
        // Avoids repeated mark/unmark on last row: return if last row has already been marked/unmarked
        // by repeated mark key strokes
        if(markKeyRepeated && lastRowMarked)
            return;

        // Don't mark '..' file but select next row
        if(!isParentFolderSelected()) {
            setRowMarked(currentRow, !tableModel.isRowMarked(currentRow));
        }

        // Changes selected item to the next one
        if(currentRow!=tableModel.getRowCount()-1) {
            selectRow(currentRow+1);
        }
        else if(!lastRowMarked) {
            // Need an explicit repaint to repaint the last row since select row is not called
            repaintRow(currentRow);

            // Last row has been marked/unmarked, value will be reset by keyReleased()
            lastRowMarked = true;
        }

        // Any further mark key events will be considered as repeated until keyReleased() has been called
        markKeyRepeated = true;
    }


    /**
     * Marks or unmarks a range of rows, delimited by the provided start row index and end row index (inclusive).
     * End row index can be lower, greater or equals to the start row.
     *
     * @param startRow index of the first row to repaint
     * @param endRow index of the last row to mark, can be lower, greater or equals to startRow
     * @param marked if true, the rows will be marked, unmarked otherwise
     */
    public void setRangeMarked(int startRow, int endRow, boolean marked) {
        tableModel.setRangeMarked(startRow, endRow, marked);
        repaintRange(startRow, endRow);
        fireMarkedFilesChangedEvent();
    }


    /**
     * Repaints the given row.
     */
    private void repaintRow(int row) {
        repaint(0, row*getRowHeight(), getWidth(), rowHeight);
    }

    /**
     * Repaints a range of rows, delimited by the provided start row index and end row index (inclusive).
     * End row index can be lower, greater or equals to the start row.
     *
     * @param startRow index of the first row to repaint
     * @param endRow index of the last row to repaint, can be lower, greater or equals to startRow
     */
    private void repaintRange(int startRow, int endRow) {
        int rowHeight = getRowHeight();
        repaint(0, Math.min(startRow, endRow)*rowHeight, getWidth(), (Math.abs(startRow-endRow)+1)*rowHeight);
    }


    /**
     * Returns the number of rows that a page down/page up action should jump, based on this FileTable's viewport size.
     * The returned number doesn't take into account the number of rows available in this FileTable.
     */
    public int getPageRowIncrement() {
        return getScrollableBlockIncrement(getVisibleRect(), SwingConstants.VERTICAL, 1)/getRowHeight() - 1;
    }


    /*
      private void centerRow() {
      JScrollPane jsp = (JScrollPane)SwingUtilities.getAncestorOfClass(JScrollPane.class, getParent());
      if(jsp==null)
      return;

      Rectangle cellRect = getCellRect(currentRow, 0, false);
		
      int viewTop = Math.max(cellRect.y-jsp.getHeight()/2+getRowHeight()/2, 0);
      jsp.getViewport().setViewPosition(new java.awt.Point(0, viewTop));
      }
    */


    /**
     * Sorts this FileTable by the given criteria.
     * Sort order (ascending or descending) is left unchanged by this method.
     */
    public void sortBy(int criteria) {
        if (criteria==sortByCriteria) {
            reverseSortOrder();
            return;
        }

        // Remove arrow icon from old header and put it on the new one
        TableColumnModel cm = getColumnModel();
        ((FileTableHeaderRenderer)cm.getColumn(convertColumnIndexToView(sortByCriteria)).getHeaderRenderer()).setCurrent(false);
        ((FileTableHeaderRenderer)cm.getColumn(convertColumnIndexToView(criteria)).getHeaderRenderer()).setCurrent(true);

        this.sortByCriteria = criteria;

        // Repaint header
        getTableHeader().repaint();

        // Sorts table while keeping current file selected
        sortTable();
    }


    /**
     * Returns the current sort by criterion. (see {@link Columns} for possible values)
     */
    public int getSortByCriteria() {
        return sortByCriteria;
    }


    /**
     * Returns true if the column corresponding to the given column index is visible
     * @param colNum column index (see {@link Columns} for possible values)
     */
    public boolean isColumnVisible(int colNum) {
        return columnVisible[colNum];
    }

    /**
     * Shows/hides the column corresponding to the given column index. The table will be repainted to reflect the new
     * column visible state.
     *
     * @param colNum column index (see {@link Columns} for possible values)
     * @param visible true to show, false to hide
     */
    public void setColumnVisible(int colNum, boolean visible) {
        if(columnVisible[colNum]!=visible) {
            columnVisible[colNum] = visible;
            resizeAndRepaint();
        }
    }


    /**
     * Reverses the sort order.
     */
    public void reverseSortOrder() {
        ascendingOrder[sortByCriteria] = !ascendingOrder[sortByCriteria];

        TableColumnModel cm = getColumnModel();
        FileTableHeaderRenderer currentHeaderRenderer = (FileTableHeaderRenderer)cm.getColumn(convertColumnIndexToView(sortByCriteria)).getHeaderRenderer();

        // Change current header's arrow direction
        currentHeaderRenderer.setOrder(ascendingOrder[sortByCriteria]);

        // Repaint header
        getTableHeader().repaint();

        // Sorts table while keeping current file selected
        sortTable();
    }


    /**
     * Turns on the filename editor on current row.
     */
    public void editCurrentFilename() {
        // Forces CommandBar to return to its normal state as modify key release event is never fired to FileTable
        mainFrame.getCommandBar().setAlternateActionsMode(false);

        // Temporarily enable editing
        tableModel.setNameColumnEditable(true);
        // Filename editor's row resize disabled because of Java bug #4398268 which prevents new rows from being visible after setRowHeight(row, height) has been called :/
        // // Adjust row height to match filename editor's height
        // setRowHeight(row, (int)filenameEditor.filenameField.getPreferredSize().getHeight());
        // Starts editing clicked cell's name column
        editCellAt(currentRow, convertColumnIndexToView(NAME));
        // Saves current/editing row in the filename editor and requests focus on the text field
        filenameEditor.notifyEditingRow(currentRow);
        // Disable editing
        tableModel.setNameColumnEditable(false);
    }


    /**
     * Sorts this FileTable and repaints it. Marked files and selected file will remain the same, only
     * there position will have changed in the newly sorted table. 
     */
    private void sortTable() {
        // Save currently selected file
        AbstractFile selectedFile = tableModel.getFileAtRow(currentRow);

        // Sort table, doesn't affect marked files
        tableModel.sortBy(sortByCriteria, ascendingOrder[sortByCriteria], showFoldersFirst);

        // Restore selected file
        selectFile(selectedFile);

        // Repaint table
        repaint();
    }


    ////////////////////////////////////
    // TableSelectionListener methods //
    ////////////////////////////////////

    /**
     * Adds the given TableSelectionListener to the list of listeners that are registered to receive
     * notifications when the currently selected file changes.
     *
     * @param listener the TableSelectionListener instance to add to the list of registered listeners.
     */
    public void addTableSelectionListener(TableSelectionListener listener) {
        tableSelectionListeners.put(listener, null);
    }

    /**
     * Removes the given TableSelectionListener from the list of listeners that are registered to receive
     * notifications when the currently selected file changes.
     * The listener will not receive any further notification after this method has been called
     * (or soon after if events are pending).
     *
     * @param listener the TableSelectionListener instance to add to the list of registered listeners.
     */
    public void removeTableSelectionListener(TableSelectionListener listener) {
        tableSelectionListeners.remove(listener);
    }


    /**
     * Notifies all registered listeners that the currently selected file has changed on this FileTable.
     */
    public void fireSelectedFileChangedEvent() {
        Iterator iterator = tableSelectionListeners.keySet().iterator();
        while(iterator.hasNext())
            ((TableSelectionListener)iterator.next()).selectedFileChanged(this);
    }

    /**
     * Notifies all registered listeners that the currently marked files have changed on this FileTable.
     */
    public void fireMarkedFilesChangedEvent() {
        Iterator iterator = tableSelectionListeners.keySet().iterator();
        while(iterator.hasNext())
            ((TableSelectionListener)iterator.next()).markedFilesChanged(this);
    }



    ///////////////////////
    // Overriden methods //
    ///////////////////////

    /**
     * Overrides JTable's doLayout() method to use a custom column layout (if auto-column sizing is enabled).
     */
    public void doLayout() {
        // Use JTable's AUTO_RESIZE_SUBSEQUENT_COLUMNS layout
        if(!autoSizeColumnsEnabled)
            super.doLayout();
        // Custom layout
        else
            autoSizeColumns();

        // Ensures that current row is visible (within current viewport), and if not adjusts viewport to center it
        Rectangle visibleRect = getVisibleRect();
        final Rectangle cellRect = getCellRect(currentRow, 0, false);
//        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("visibleRect="+visibleRect+" cellRect="+cellRect);
        if(cellRect.y<visibleRect.y || cellRect.y+getRowHeight()>visibleRect.y+visibleRect.height) {
            final JScrollPane scrollPane = folderPanel.getScrollPane();
            if(scrollPane!=null) {
                    //scrollPane.getViewport().reshape(0, 0, getWidth(), getHeight());

                // At this point JViewport is not yet aware of the new FileTable dimensions, calling setViewPosition
                // would not work. Instead, SwingUtilities.invokeLater is used to delay the call after all pending
                // UI events (including JViewport revalidation) have been processed.
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if(Debug.ON) Debug.trace("calling viewPort.setViewPostion(0, "+(cellRect.y-scrollPane.getHeight()/2-getRowHeight()/2)+")");
                        scrollPane.getViewport().setViewPosition(new java.awt.Point(0, Math.max(0, cellRect.y-scrollPane.getHeight()/2-getRowHeight()/2)));
                        //                        scrollPane.repaint();
                    }
                });
            }
        }
    }


    /**
     * Method overriden to return a custom TableCellRenderer.
     */
    public TableCellRenderer getCellRenderer(int row, int column) {
        return cellRenderer;
    }


    /**
     * Method overriden to consume keyboard events when quick search is active or when a row is being editing
     * in order to prevent registered actions from being fired.
     */
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent ke, int condition, boolean pressed) {
        if(quickSearch.isActive() || isEditing())
            return true;

//        if(ActionKeymap.isKeyStrokeRegistered(ks))
//            return false;

        return super.processKeyBinding(ks, ke, condition, pressed);
    }


    /**
     * Overrides the changeSelection method from JTable to track the current selected row (the one that has focus)
     * and fire a {@link com.mucommander.ui.event.TableSelectionListener#selectedFileChanged(FileTable)} event
     * to registered listeners. 
     */
    public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        // For shift+click
        lastRow = currentRow;
        currentRow = rowIndex;

        super.changeSelection(rowIndex, columnIndex, toggle, extend);

        // If row changed
        if(currentRow!=lastRow) {
            // Update selection changed timestamp
            selectionChangedTimestamp = System.currentTimeMillis();
            // notify registered TableSelectionListener instances that the currently selected file has changed
            fireSelectedFileChangedEvent();
        }

        //		// Don't refresh status bar if up, down, space or insert key is pressed (repeated key strokes).
        //		// Status bar will be refreshed whenever the key is released.
        //		// We need this limit because refreshing status bar takes time.
        //		if(downKeyDown || upKeyDown || spaceKeyDown || insertKeyDown)
        //			return;
    }


    public Dimension getPreferredSize() {
        Container parentComp = getParent();

        // Filename editor's row resize disabled because of Java bug #4398268 which prevents new rows from being visible after setRowHeight(row, height) has been called :/
        /*
          int height;
          if(isEditing())
          height = (tableModel.getRowCount()-1)*getRowHeight() + editorRowHeight;
          else
          height = tableModel.getRowCount()*getRowHeight();

          return new Dimension(parentComp==null?0:parentComp.getWidth(), height);
        */
        return new Dimension(parentComp==null?0:parentComp.getWidth(), tableModel.getRowCount()*getRowHeight());
    }


    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }
    /**
     * Overrides JTable's paint method in order to ignore calls when folder is being changed.
     */
    public void paint(Graphics g) {
        if(this.ignorePaintCalls) {
            if(Debug.ON) Debug.trace("ignoring call");
            return;
        }
        super.paint(g);
    }


    /**
     * Overridden for debugging purposes.
     */
    public String toString() {
        return getClass().getName()+"@"+hashCode() +" currentFolder="+currentFolder+" hasFocus="+hasFocus()+" currentRow="+currentRow;
    }


    ///////////////////////////
    // MouseListener methods //
    ///////////////////////////

    public void mouseClicked(MouseEvent e) {
        // Discard mouse events while in 'no events mode'
        if(mainFrame.getNoEventsMode())
            return;

        Object source = e.getSource();

        // If one of the table cells was left clicked...
        if(source==this && PlatformManager.isLeftMouseButton(e)) {
            int clickCount = e.getClickCount();
            // Clicking on the selected row's :
            //  - name label triggers the filename editor
            //  - date label triggers the change date dialog
            //  - permissions label triggers the change permissions dialog, only if permissions can be changed 
            // Timestamp check is used to make sure that this mouse click did not trigger current row selection
            //com.mucommander.Debug.trace("clickCount="+clickCount+" timeDiff="+(System.currentTimeMillis()-selectionChangedTimestamp));
            if (clickCount == 1 && (System.currentTimeMillis()-selectionChangedTimestamp)>EDIT_NAME_CLICK_DELAY) {
                int clickX = e.getX();
                Point p = new Point(clickX, e.getY());
                final int row = rowAtPoint(p);
                final int viewColumn = columnAtPoint(p);
                final int column = convertColumnIndexToModel(viewColumn);
                // Test if clicked row is current row, if column is name column, and if current row is not '..' file
                //com.mucommander.Debug.trace("row="+row+" currentRow="+currentRow);
                if(row==currentRow && !isParentFolderSelected() && (column==NAME || column==DATE || column==PERMISSIONS)) {
                    // Test if clicked point is inside the label and abort if not
                    FontMetrics fm = getFontMetrics(cellRenderer.getCellFont());
                    int labelWidth = fm.stringWidth((String)tableModel.getValueAt(row, column));
                    int columnX = (int)getTableHeader().getHeaderRect(viewColumn).getX();
                    //com.mucommander.Debug.trace("x="+clickX+" columnX="+columnX+" labelWidth="+labelWidth);
                    if(clickX<columnX+CellLabel.CELL_BORDER_WIDTH || clickX>columnX+labelWidth+CellLabel.CELL_BORDER_WIDTH)
                        return;

                    // Create a new thread and sleep long enough to ensure that this click was not the first of a double click
                    new Thread() {
                        public void run() {
                            try { sleep(800); }
                            catch(InterruptedException e) {}

                            // Do not execute this block (cancel editing) if:
                            // - a double click was made in the last second
                            // - current row changed
                            // - isEditing() is true which could happen if multiple clicks were made
//                            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("row= "+row+" currentRow="+currentRow);
                            if((System.currentTimeMillis()-lastDoubleClickTimestamp)>1000 && row==currentRow) {
                                if(column==NAME) {
                                    if(!isEditing())
                                        editCurrentFilename();
                                }
                                else if(column==DATE) {
                                    ActionManager.performAction(com.mucommander.ui.action.ChangeDateAction.class, mainFrame);
                                }
                                else if(column==PERMISSIONS) {
                                    if(getSelectedFile().getPermissionSetMask()!=0)
                                        ActionManager.performAction(com.mucommander.ui.action.ChangePermissionsAction.class, mainFrame);
                                }
                            }
                        }
                    }.start();
                }
            }
            // Double-clicking on a row opens the file/folder
            else if(e.getClickCount()==2) {
                this.lastDoubleClickTimestamp = System.currentTimeMillis();
                ActionManager.performAction(e.isShiftDown()?com.mucommander.ui.action.OpenNativelyAction.class:com.mucommander.ui.action.OpenAction.class
                    , mainFrame);
            }

        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        // Discard mouse events while in 'no events mode'
        if(mainFrame.getNoEventsMode())
            return;

        if(e.getSource()!=this)
            return;

        // Right-click brings a contextual popup menu
        if(PlatformManager.isRightMouseButton(e)) {
            // Find the row that was right-clicked
            int x = e.getX();
            int y = e.getY();
            int clickedRow = rowAtPoint(new Point(x,y));
            // Does the row correspond to the parent '..' folder ? 
            boolean parentFolderClicked = clickedRow==0 && folderParent!=null;

            // Select clicked row if it is not selected already
            if(currentRow!=clickedRow)
                selectRow(clickedRow);

            // Request focus on this FileTable is focus is somewhere else
            if(!hasFocus())
                requestFocus();

            // Popup menu where the user right-clicked
            new TablePopupMenu(mainFrame, currentFolder, parentFolderClicked?null:tableModel.getFileAtRow(clickedRow), parentFolderClicked, tableModel.getMarkedFiles()).show(this, x, y);
        }
        // Middle-click on a row marks or unmarks it
        // Control left-click also works
        else if (PlatformManager.isMiddleMouseButton(e)) {
            // Used by mouseDragged
            lastDraggedRow = rowAtPoint(e.getPoint());
            markOnRightClick = !tableModel.isRowMarked(lastDraggedRow);

            setRowMarked(lastDraggedRow, markOnRightClick);
        }
        else if(PlatformManager.isLeftMouseButton(e)) {
            // Marks a group of rows, from last current row to clicked row (current row)
            if(e.isShiftDown()) {
                setRangeMarked(currentRow, lastRow, !tableModel.isRowMarked(currentRow));
            }
            // Marks the clicked row
            else if (e.isControlDown()) {
                int rowNum = rowAtPoint(e.getPoint());
                setRowMarked(rowNum, !tableModel.isRowMarked(rowNum));
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
    }


    /////////////////////////////////
    // MouseMotionListener methods //
    /////////////////////////////////

    public void mouseDragged(MouseEvent e) {
        // Discard mouse motion events while in 'no events mode'
        if(mainFrame.getNoEventsMode())
            return;

        // Marks or unmarks every row that was between the last mouseDragged point
        // and the current one
        if (PlatformManager.isMiddleMouseButton(e) && lastDraggedRow!=-1) {
            int draggedRow = rowAtPoint(e.getPoint());

            // Mouse was dragged outside of the FileTable
            if(draggedRow==-1)
                return;

            setRangeMarked(lastRow, draggedRow, markOnRightClick);

            lastDraggedRow = draggedRow;
        }
    }


    public void mouseMoved(MouseEvent e) {
    }


    /////////////////////////
    // KeyListener methods //
    /////////////////////////

    public void keyPressed(KeyEvent e) {
        // Discard key events while in 'no events mode' or editing a row
        if(mainFrame.getNoEventsMode() || isEditing())
            return;

        int keyCode = e.getKeyCode();
        boolean isShiftDown = e.isShiftDown();

        // The following actions must not be triggered when quick searching
        if(quickSearch.isActive())
            return;

        // Determine if shift+UP/DOWN will mark or unmark file(s)
        if (keyCode == KeyEvent.VK_SHIFT) {
            markOnShift = !tableModel.isRowMarked(currentRow);
        }
        // Mark/unmark file
        else if (isShiftDown && (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_UP)) {
            setRowMarked(currentRow, markOnShift);
        }
    }


    public void keyTyped(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        // Discard keyReleased events while quick search is active
        if(quickSearch.isActive())
            return;

        // Test if the event corresponds to the 'Mark/unmark selected file' action keystroke.
        // Note: KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers()) is used instead of KeyStroke.getKeyStrokeForEvent()
        // in order to get a 'pressed' KeyStroke and not a 'released' one, since the action is mapped to the 'pressed' one   
        if(ActionManager.getActionInstance(MARK_ACTION_CLASS, mainFrame).isAccelerator(KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers()))) {
            // Reset variables used to detect repeated key strokes
            markKeyRepeated = false;
            lastRowMarked = false;
        }
    }


    ///////////////////////////
    // FocusListener methods //
    ///////////////////////////

    /**
     * Restores selection when focus is gained.
     */
    public void focusGained(FocusEvent e) {
//if(Debug.ON) Debug.trace("called, this="+this);

        if(isEditing()) {
            filenameEditor.filenameField.requestFocus();
        }
        else {
            // (Re)select current row and repaint it to show the new focused state.
            // An explicit repaint is needed as the row would not
            // be repainted by changeSelection if it was already selected.
            selectRow(currentRow);
            //repaintRow(currentRow);
            repaint();
        }
    }

    /**
     * Hides selection when focus is lost.
     */
    public void focusLost(FocusEvent e) {
//if(Debug.ON) Debug.trace("called, this="+this);

        // Repaint selected row to show the new out of focus state
        //repaintRow(currentRow);
        repaint();
    }


    /////////////////////////////////
    // ActivePanelListener methods //
    /////////////////////////////////

    public void activePanelChanged(FolderPanel folderPanel) {
        isActiveTable = folderPanel==getFolderPanel();
        // Repaint selected row to show the new active/inactive state 
        //repaint(currentRow);
    }


    ///////////////////////////////////
    // ConfigurationListener methods //
    ///////////////////////////////////

    /**
     * Listens to certain configuration variables.
     */
    public void configurationChanged(ConfigurationEvent event) {
        String var = event.getVariable();

        if (var.equals(MuConfiguration.DISPLAY_COMPACT_FILE_SIZE) || var.equals(MuConfiguration.DATE_FORMAT) ||
                 var.equals(MuConfiguration.DATE_SEPARATOR) || var.equals(MuConfiguration.TIME_FORMAT)) {
            // Note: for the update to work properly, CustomDateFormat's configurationChanged() method has to be called
            // before FileTable's, so that CustomDateFormat gets notified of date format first.
            // Since listeners are stored by MuConfiguration in a hash map, order is pretty much random.
            // So CustomDateFormat#updateDateFormat() has to be called before to ensure that is uses the new date format.
            CustomDateFormat.updateDateFormat();
            tableModel.fillCellCache();
            // If auto column sizing enabled, resize columns width with new font, resize columns and repaint
            if(autoSizeColumnsEnabled) {
                setMaxColumnsWidth();
                resizeAndRepaint();
            }
            else
                repaint();
        }
        // Repaint file icons if their size has changed
        else if (var.equals(MuConfiguration.TABLE_ICON_SCALE)) {
            // Recalculate columns' width
            if(autoSizeColumnsEnabled)
                setMaxColumnsWidth();

            // Recalcule row height, revalidate and repaint the table
            setRowHeight();
        }
        // Repaint file icons if the system file icons policy has changed
        else if (var.equals(MuConfiguration.USE_SYSTEM_FILE_ICONS))
            repaint();
    }


    /**
     * <p>A Custom CellEditor which provides the following functionalities:
     * <ul>
     * <li>Filename selection (without extension) when filename starts being edited.
     * <li>Can be cancelled by pressing ESCAPE
     * <li>Starts renaming the file when ENTER is pressed
     * </ul>
     *
     * <p>Only once instance per FileTable is created.
     *
     * <p><b>Implementation note:</b> stopCellEditing() and cancelCellEditing() should not be overridden to detect
     * accept/cancel user events as they are totally unrealiable and often not called, for example when clicking
     * on one of the table's headers (many other cases).
     */
    private class FilenameEditor extends DefaultCellEditor {

        private JTextField filenameField;

        /** Row that is currently being edited */
        private int editingRow;

        /**
         * Creates a new FilenameEditor instance.
         */
        public FilenameEditor(JTextField textField) {
            super(textField);
            this.filenameField = textField;
            // Sets the font to the same one that's used for cell rendering (user-defined)
            filenameField.setFont(cellRenderer.getCellFont());
            textField.addKeyListener(
                new KeyAdapter() {
                    // Cancel editing when escape key pressed, this is unfortunately not DefaultCellEditor's
                    // default behavior
                    public void keyPressed(KeyEvent e) {
                        int keyCode = e.getKeyCode();
                        if(keyCode == KeyEvent.VK_ESCAPE)
                            cancelCellEditing();
                        else if(keyCode == KeyEvent.VK_ENTER)
                            rename();
                    }
                }
            );
        }


        /**
         * Renames the currently edited name cell, only if the filename has changed.
         */
        private void rename() {
            String newName = filenameField.getText();
            AbstractFile fileToRename = tableModel.getFileAtRow(editingRow);

            if(!newName.equals(fileToRename.getName())) {
                // Starts moving files
                ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("move_dialog.moving"));
                FileSet files = new FileSet(currentFolder);
                files.add(fileToRename);
                MoveJob renameJob = new MoveJob(progressDialog, mainFrame, files, currentFolder, newName, FileCollisionDialog.ASK_ACTION, true);
                progressDialog.start(renameJob);
            }
        }


        /**
         * Restores default row height.
         */
/*
        public void restore() {
            // Filename editor's row resize disabled because of Java bug #4398268 which prevents new rows from being visible after setRowHeight(row, height) has been called.
            // Add to that the fact that DefaultCellEditor's stopCellEditing() and cancelCellEditing() are not always called, for instance when table header is clicked.
            //				setRowHeight(currentRow, cellRenderer.getFontMetrics(cellRenderer.getCellFont()).getHeight()+cellRenderer.CELL_BORDER_HEIGHT);
        }
*/

        /**
         * Notifies this editor that the given row's filename cell is being edited. This method has to be called once
         * when a row just started being edited. It will save the row number and select the filename without
         * its extension to make it easier to rename (it happens more often that ones wishes to rename a file's name
         * than its extension).
         *
         * @param row row which is being edited
         */
        public void notifyEditingRow(int row) {
            // The editing row has to be saved as it could change after row editing has been started
            this.editingRow = row;

            // Select filename without extension, only if filename is not empty (unlike '.DS_Store' for example)
            String fieldText = filenameField.getText();
            int extPos = fieldText.indexOf('.');
            // Text is selected so that user can directly type and replace path
            filenameField.setSelectionStart(0);
            filenameField.setSelectionEnd(extPos>0?extPos:fieldText.length());

            // Request focus on text field
            filenameField.requestFocus();
        }
    }


    /**
     * This inner class enables quick search functionality on the FileTable, which allows to
     */
    public class QuickSearch implements Runnable, KeyListener {

        /** Quick search string */
        private String searchString;

        /** Timestamp of the last search string change, used when quick search is active */
        private long lastSearchStringChange;

        /** Thread that's responsible for cancelling the quick search on timeout,
         * has a null value when quick search is not active */
        private Thread timerThread;

        /** Quick search timeout in ms */
        private final static int QUICK_SEARCH_TIMEOUT = 2000;

        /** Icon that is used to indicate in the status bar that quick search has failed */
        private final static String QUICK_SEARCH_KO_ICON = "quick_search_ko.png";

        /** Icon that is used to indicate in the status bar that quick search has found a match */
        private final static String QUICK_SEARCH_OK_ICON = "quick_search_ok.png";


        /**
         * Creates a new QuickSearch instance, only one instance per FileTable should be created.
         */
        private QuickSearch() {
            // Listener to key events to start quick search or update search string when it is active
            FileTable.this.addKeyListener(this);
        }

        /**
         * Turns on quick search mode, {@link #isActive() isActive()} will return <code>true</code>
         * after this call, and until the quick search has timed out or has been cancelled by user.
         */
        public void start() {
            // Reset search string
            searchString = "";
            // Start the thread that's responsible for cancelling the quick search on timeout
            timerThread = new Thread(this);
            timerThread.start();

            // Repaint the table to add the 'dim' effect on non-matching files
            FileTable.this.folderPanel.dimBackground();
            FileTable.this.repaint();
        }

        /**
         * Cancels (stops) the current quick search. This method has no effect if quick search is not
         * currently active.
         */
        public void cancel() {
            if(timerThread != null) {
                mainFrame.getStatusBar().updateSelectedFilesInfo();
                timerThread = null;

                // Removes the 'dim' effect on non-matching files.
                FileTable.this.folderPanel.undimBackground();
                FileTable.this.repaint();
            }
        }

        /**
         * Returns <code>true</code> if a quick search is being performed.
         */
        public boolean isActive() {
            return timerThread != null;
        }


        /**
         * Finds a match (if any) for the current quick search string and selects the corresponding row.
         *
         * @param startRow first row to be tested
         * @param descending specifies whether rows should be tested in ascending or descending order
         * @param findBestMatch if <code>true</code>, all rows will be tested in the specified order, looking for the best match. If not, it will stop to the first match (not necessarily the best).
         */
        private void findMatch(int startRow, boolean descending, boolean findBestMatch) {
//            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("startRow="+startRow+" descending="+descending+" findMatch="+findBestMatch);

            int searchStringLen = searchString.length();

            // If search string is empty, update status bar without any icon and return
            if(searchStringLen==0) {
                mainFrame.getStatusBar().setStatusInfo(searchString);
                return;
            }

            String searchStringLC = searchString.toLowerCase();
            AbstractFile file;
            String filename;
            String filenameLC;
            int filenameLen;
            int startsWithCaseMatch = -1;
            int startsWithNoCaseMatch = -1;
            int containsCaseMatch = -1;
            int containsNoCaseMatch = -1;
            int nbRows = tableModel.getRowCount();

            // Iterate on rows and look the first file to match one of the following tests,
            // in the following order of importance :
            // - search string matches the beginning of the filename with the same case
            // - search string matches the beginning of the filename with a different case
            // - filename contains search string with the same case
            // - filename contains search string with a different case
            for(int i=startRow; descending?i<nbRows:i>=0; i=descending?i+1:i-1) {
                // if findBestMatch was not specified, stop to the first match
                if(!findBestMatch && (startsWithCaseMatch!=-1 || startsWithNoCaseMatch!=-1 || containsCaseMatch!=-1 || containsNoCaseMatch!=-1))
                    break;

                file = tableModel.getFileAtRow(i);
                filename = (i==0 && folderParent!=null)?"..":file.getName();
                filenameLen = filename.length();

                // No need to compare strings if quick search string is longer than filename,
                // they won't match
                if(filenameLen<searchStringLen)
                    continue;

                // Compare quick search string against
                if (filename.startsWith(searchString)) {
                    // We've got the best match we could ever have, let's get out of this loop!
                    startsWithCaseMatch = i;
                    break;
                }

                // If we already have a match on this test case, let's skip to the next file
                if(startsWithNoCaseMatch!=-1)
                    continue;

                filenameLC = filename.toLowerCase();
                if(filenameLC.startsWith(searchStringLC)) {
                    // We've got a match, let's see if we can find a better match on the next file
                    startsWithNoCaseMatch = i;
                }

                // No need to check if filename contains search string if both size are equal,
                // in the case startsWith test yields the same result
                if(filenameLen==searchStringLen)
                    continue;

                // If we already have a match on this test case, let's skip to the next file
                if(containsCaseMatch!=-1)
                    continue;

                if(filename.indexOf(searchString)!=-1) {
                    // We've got a match, let's see if we can find a better match on the next file
                    containsCaseMatch = i;
                    continue;
                }

                // If we already have a match on this test case, let's skip to the next file
                if(containsNoCaseMatch!=-1)
                    continue;

                if(filenameLC.indexOf(searchStringLC)!=-1) {
                    // We've got a match, let's see if we can find a better match on the next file
                    containsNoCaseMatch = i;
                    continue;
                }
            }

            // Determines what the best match is, based on all the matches we found
            int bestMatch = startsWithCaseMatch!=-1?startsWithCaseMatch
                :startsWithNoCaseMatch!=-1?startsWithNoCaseMatch
                :containsCaseMatch!=-1?containsCaseMatch
                :containsNoCaseMatch!=-1?containsNoCaseMatch
                :-1;

//            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("startsWithCaseMatch="+startsWithCaseMatch+" containsCaseMatch="+containsCaseMatch+" startsWithNoCaseMatch="+startsWithNoCaseMatch+" containsNoCaseMatch="+containsNoCaseMatch);
//            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("bestMatch="+bestMatch);

            if(bestMatch!=-1) {
                // Select best match's row
                if(bestMatch!=currentRow) {
                    selectRow(bestMatch);
                    //centerRow();
                }

                // Display the new search string in the status bar
                // that indicates that the search has yielded a match
                mainFrame.getStatusBar().setStatusInfo(searchString, IconManager.getIcon(IconManager.STATUS_BAR_ICON_SET, QUICK_SEARCH_OK_ICON), false);
            }
            else {
                // No file matching the search string, display the new search string with an icon
                // that indicates that the search has failed
                mainFrame.getStatusBar().setStatusInfo(searchString, IconManager.getIcon(IconManager.STATUS_BAR_ICON_SET, QUICK_SEARCH_KO_ICON), false);
            }
        }

        /**
         * Returns <code>true</code> the current quick search string matches the given filename.
         * Always returns <code>false</code> when the quick search is inactive.
         *
         * @param filename the filename to test against the quick search string
         * @return true if the current quick search string matches the given filename
         */
        public boolean matches(String filename) {
            if(!isActive())
                return false;

            return filename.toLowerCase().indexOf(searchString.toLowerCase())!=-1;
        }

        //////////////////////
        // Runnable methods //
        //////////////////////

        public void run() {
            while(true) {
                try { timerThread.sleep(100); }
                catch(InterruptedException e) {}

                if(System.currentTimeMillis()-lastSearchStringChange >= QUICK_SEARCH_TIMEOUT) {
                    cancel();
                    return;
                }
            }
        }

        ///////////////////////////////////
        // Overridden KeyAdapter methods //
        ///////////////////////////////////

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            // Discard key events while in 'no events mode'
            if(mainFrame.getNoEventsMode())
                return;

            char keyChar = e.getKeyChar();

            // If quick search is not active...
            if (!isActive()) {
                // Return (do not start quick search) if the typed key:
                // - has any of the Alt, Ctrl or Meta modifier keys down (Shift is OK)
                // - is a ASCII control character (<32 or 127 for delete)
                // - is not a valid Unicode character
                if((e.getModifiers()&(KeyEvent.ALT_DOWN_MASK|KeyEvent.CTRL_DOWN_MASK|KeyEvent.META_DOWN_MASK))!=0 || keyChar<32 || keyChar==127 || !Character.isDefined(keyChar))
                    return;

                // Return if the typed key corresponds to a registered action's accelerator
                if(ActionKeymap.isKeyStrokeRegistered(KeyStroke.getKeyStrokeForEvent(e)))
                    return;

                // Start the quick search and continue to properly process the key event
                start();
            }

            // At this point, quick search is active
            int keyCode = e.getKeyCode();

            // Backspace removes the last character of the search string
            if(keyCode==KeyEvent.VK_BACK_SPACE) {
                int searchStringLen = searchString.length();

                // Search string is empty already
                if(searchStringLen==0)
                    return;

                // Remove last character from the search string
                // Since the search string has been updated, match information has changed as well
                // and we need to repaint the table.
                // Note that we only repaint if the search string is not empty: if it's empty,
                // the cancel() method will be called, and repainting twice would result in an
                // unpleasant graphical artifact.
                searchString = searchString.substring(0, searchStringLen-1);
                if(searchString.length() != 0)
                    FileTable.this.repaint();

                // Find the row that best matches the new search string and select it
                findMatch(0, true, true);
            }
            // Escape immediately cancels the quick search
            else if(keyCode==KeyEvent.VK_ESCAPE) {
                cancel();
            }
            // Up/Down jumps to previous/next match
            // Shift+Up/Shift+Down marks currently selected file and jumps to previous/next match
            else if(keyCode==KeyEvent.VK_UP || keyCode==KeyEvent.VK_DOWN) {
                // Mark the currently selected file if shift modifier is pressed
                if(e.isShiftDown()) {
                    // Mark/unmark current row before jumping to next search result
                    // but don't mark/unmark '..' file
                    if(!isParentFolderSelected())
                        setRowMarked(currentRow, !tableModel.isRowMarked(currentRow));
                }

                // Find the first row before/after the current row that matches the search string
                if(keyCode==KeyEvent.VK_UP)
                    findMatch(currentRow-1, false, false);
                else
                    findMatch(currentRow+1, true, false);
            }
            // If no modifier other than Shift is pressed and the typed character is not a control character (space is ok)
            // and a valid Unicode character, add it to the current search string
            else if((e.getModifiers()&(KeyEvent.ALT_DOWN_MASK|KeyEvent.CTRL_DOWN_MASK|KeyEvent.META_DOWN_MASK))==0 && keyChar>=32 && keyChar!=127 && Character.isDefined(keyChar)) {
                // Update search string with the key that has just been typed
                // Since the search string has been updated, match information has changed as well
                // and we need to repaint the table.
                searchString += keyChar;
                FileTable.this.repaint();

                // Find the row that best matches the new search string and select it
                findMatch(0, true, true);
            }
            else {
                // Test if the typed key combination corresponds to a registered actions.
                // If that's the case, the quick search is cancelled and the action is performed.
                Class muActionClass = ActionKeymap.getRegisteredActionClassForKeystroke(KeyStroke.getKeyStrokeForEvent(e));
                if(muActionClass!=null) {
                    // Consume the key event otherwise it would be fired again on the FileTable
                    // (or any other KeyListener on this FileTable)
                    e.consume();

                    // Cancel quicksearch
                    cancel();

                    // Perform the action
                    ActionManager.getActionInstance(muActionClass, mainFrame).performAction();
                }

                // Do not update last search string's change timestamp
                return;
            }

            // Update last search string's change timestamp
            lastSearchStringChange = System.currentTimeMillis();
        }


        public void keyReleased(KeyEvent e) {
            // Cancel quick search if backspace key has been pressed and search string is empty.
            // This check is done on key release, so that if backspace key is maintained pressed
            // to remove all the search string, it does not trigger FileTable's back action which is
            // mapped on backspace too
            if(isActive() && e.getKeyCode()==KeyEvent.VK_BACK_SPACE && searchString.equals("")) {
                e.consume();
                cancel();
            }
        }
    }



    // - Theme listening -------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /**
     * Not used.
     */
    public void colorChanged(ColorChangedEvent event) {
        if(event.getColorId() == Theme.FILE_TABLE_BACKGROUND_COLOR)
            setBackground(event.getColor());
    }

    /**
     * Receives theme font changes notifications.
     */
    public void fontChanged(FontChangedEvent event) {
        if(event.getFontId() == Theme.FILE_TABLE_FONT) {
            // Changes filename editor's font
            filenameEditor.filenameField.setFont(event.getFont());

            // If auto column sizing enabled, recalculate columns' width
            if(autoSizeColumnsEnabled)
                setMaxColumnsWidth();

            // Recalcule row height, revalidate and repaint the table
            setRowHeight();
        }
    }
}
