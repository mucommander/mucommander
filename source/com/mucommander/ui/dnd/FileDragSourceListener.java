package com.mucommander.ui.dnd;

import com.mucommander.ui.FolderPanel;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FileTableModel;
import com.mucommander.file.FileSet;
import com.mucommander.file.AbstractFile;
import com.mucommander.Debug;

import java.awt.dnd.*;
import java.awt.*;

/**
 * This class adds 'drag' support to components that are registered using the {@link #enableDrag(java.awt.Component)}
 * method.
 *
 * <p>A {@link com.mucommander.ui.FolderPanel} instance has to be specified at creation time, this instance will be
 * used to retrieve the list of selected/marked file(s) that are dragged, whenever a drag operation is initiated on
 * of the registered components.
 *
 * @author Maxence Bernard
 */
public class FileDragSourceListener implements DragSourceListener, DragGestureListener {

    /** the FolderPanel instance used to retrieve dragged files */
    private FolderPanel folderPanel;

    /** Is a drag operation currently being performed ? */
    private boolean isDragging;


    /**
     * Creates a new FileDragSourceListener using the specified FolderPanel that will be used to retreive the dragged files
     * based on the current file selection.
     *
     * @param folderPanel the FolderPanel used to retrieve the list of selected/marked file(s) that are dragged
     */
    public FileDragSourceListener(FolderPanel folderPanel) {
        this.folderPanel = folderPanel;
    }


    /**
     * Enables drag operations on the specified component. This class will be notified wheneven drag operations
     * are performed on the component.
     *
     * @param c the component for which to add 'drag' support
     */
    public void enableDrag(Component c) {
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(c, DnDConstants.ACTION_COPY|DnDConstants.ACTION_MOVE|DnDConstants.ACTION_LINK, this);
    }


    /**
     * Returns <code>true</code> if one or several files are currently being dragged.
     */
    public boolean isDragging() {
        return this.isDragging;
    }

    /////////////////////////////////
    // DragGestureListener methods //
    /////////////////////////////////

    public void dragGestureRecognized(DragGestureEvent event) {
        if(folderPanel.getMainFrame().getNoEventsMode())
            return;

        this.isDragging = true;

        FileTable fileTable = folderPanel.getFileTable();
        FileSet draggedFiles;

        AbstractFile selectedFile = fileTable.getSelectedFile(false);
        // Return if selected file is null (could happen if '..' is selected)
        if(selectedFile==null)
            return;

        // Find out which files are to be dragged, based on the selected file and currenlty marked files.
        // If there are some files marked, drag marked files only if the selected file is one of the marked files.
        // In any other case, only drag the selected file.
        FileSet markedFiles;
        if(((FileTableModel)fileTable.getModel()).getNbMarkedFiles()>0 && (markedFiles=fileTable.getSelectedFiles()).contains(selectedFile)) {
            draggedFiles = markedFiles;
        }
        else {
            draggedFiles = new FileSet(fileTable.getCurrentFolder(), selectedFile);
        }

        // Start dragging
        DragSource.getDefaultDragSource().startDrag(event, null, new TransferableFileSet(draggedFiles), this);
    }


    ////////////////////////////////
    // DragSourceListener methods //
    ////////////////////////////////

    public void dragEnter(DragSourceDragEvent event) {
        this.isDragging = true;
    }

    public void dragOver(DragSourceDragEvent event) {
    }

    public void dropActionChanged(DragSourceDragEvent event) {
    }

    public void dragExit(DragSourceEvent event) {
        this.isDragging = false;
    }

    public void dragDropEnd(DragSourceDropEvent event) {
        this.isDragging = false;
    }
}
