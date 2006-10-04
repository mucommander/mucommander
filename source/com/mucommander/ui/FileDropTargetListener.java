package com.mucommander.ui;

import com.mucommander.Debug;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.ui.icon.IconManager;

import javax.swing.*;
import java.awt.dnd.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.*;
import java.util.List;
import java.io.File;
import java.io.Reader;

/**
 * Provides file(s) 'drop' support to components that add a <code>DropTarget</code> using
 * this <code>DropTargetListener</code>.
 *
 * <p>Currently, 2 types of data flavors are supported:
 * <ul>
 * <li>File list: only the first file is taken into account
 * <li>File path: a file instance is reconstructed from the path string
 * </ul>
 *
 * <p>When a file or string representing a file path is dropped, the associated FolderPanel's current folder is changed:
 * <ul>
 * <li>If file is a directory, current folder is changed to that directory
 * <li>For any other file kind (archive, regular file...), current folder is changed to the file's parent folder
 * and the file is selected
 * </ul>
 *
 * @author Maxence Bernard
 */
public class FileDropTargetListener implements DropTargetListener {

    /** the FolderPanel instance used to change the current folder when a file is dropped */
    private FolderPanel folderPanel;

    /** Icon used as a cursor when a file is dragged to the component */
    private final static String CHANGE_FOLDER_DRAG_ICON = "drop_change_folder.png";

    /**
     * Creates a new FileDropTargetListener using the provided FolderPanel to change the current folder
     * when a file is dropped.
     *
     * @param folderPanel the FolderPanel instance used to change the current folder when a file is dropped
     */
    public FileDropTargetListener(FolderPanel folderPanel) {
        this.folderPanel = folderPanel;
    }

    public void dragEnter(DropTargetDragEvent event) {
        // Reject drag is DataFlavor is not supported
        if(!(event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
            || (event.isDataFlavorSupported(DataFlavor.getTextPlainUnicodeFlavor())))) {

            event.rejectDrag();
            return;
        }

        // Accept drag event
        event.acceptDrag(DnDConstants.ACTION_LINK);

        // Set a custom icon that represents the drop action
        ImageIcon icon = IconManager.getIcon(IconManager.TABLE_ICON_SET, CHANGE_FOLDER_DRAG_ICON);
        if(icon!=null)
            folderPanel.setCursor(java.awt.Toolkit.getDefaultToolkit().createCustomCursor(icon.getImage(), new Point(0,0), ""));
    }

    public void dragOver(DropTargetDragEvent event) {
    }

    public void dropActionChanged(DropTargetDragEvent event) {
    }

    public void dragExit(DropTargetEvent event) {
        folderPanel.setCursor(Cursor.getDefaultCursor());
    }

    public void drop(DropTargetDropEvent event) {
        // Restore default cursor, no matter what
        folderPanel.setCursor(Cursor.getDefaultCursor());

        // File list DataFlavor
        boolean isFileList = event.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        // Text plain DataFlavor
        boolean isTextPlain = event.isDataFlavorSupported(DataFlavor.getTextPlainUnicodeFlavor());

        // Reject drop is DataFlavor is not supported: the drop() method is called even if
        // a DropTargetDropEvent has been rejected before, so this check is really necessary
        if(!(isFileList || isTextPlain)) {
            event.rejectDrop();
            return;
        }

        // Accept drop event
        event.acceptDrop(DnDConstants.ACTION_LINK);

        Transferable transferable = event.getTransferable();
        try {
            AbstractFile file;

            // File list DataFlavor, only the first file is taken into account
            if(isFileList) {
                List fileList = (List)transferable.getTransferData(DataFlavor.javaFileListFlavor);
                // Create an AbstractFile instance
                file = FileFactory.getFile(((File)fileList.get(0)).getAbsolutePath());
            }
            // Text plain DataFlavor: assumes that the text represents a file path
            else {
                // Read text fully
                Reader reader = DataFlavor.getTextPlainUnicodeFlavor().getReaderForText(transferable);
                StringBuffer sb = new StringBuffer();
                int i;
                while((i=reader.read())!=-1)
                    sb.append((char)i);
                reader.close();

                // Try to create an AbstractFile instance, returned instance may be null
                file = FileFactory.getFile(sb.toString());
            }

            // Report drop failure if file is null
            if(file==null) {
                event.dropComplete(false);
                return;
            }

            // If file is a directory, change current folder to that directory
            if(file.isDirectory())
                folderPanel.trySetCurrentFolder(file);
            // For any other file kind (archive, regular file...), change directory to the file's parent folder
            // and select the file
            else
                folderPanel.trySetCurrentFolder(file.getParent(), file);

            // Report that the drop event has been successfully handled
            event.dropComplete(true);
        }
        catch(Exception e) {
            if(Debug.ON) Debug.trace("Exception caught while processing dropped transferable: "+e);

            // Report drop failure
            event.dropComplete(false);
        }
    }
}
