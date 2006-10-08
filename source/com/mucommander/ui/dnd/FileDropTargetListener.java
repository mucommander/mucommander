package com.mucommander.ui.dnd;

import com.mucommander.Debug;
import com.mucommander.job.MoveJob;
import com.mucommander.job.CopyJob;
import com.mucommander.text.Translator;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileSet;
import com.mucommander.ui.FolderPanel;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.FileExistsDialog;

import java.awt.dnd.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.*;
import java.util.List;
import java.io.File;
import java.io.BufferedReader;

/**
 * Provides file(s) 'drop' support to components that add a <code>DropTarget</code> using this <code>DropTargetListener</code>.
 * A {@link com.mucommander.ui.FolderPanel} instance has to be specified at creation time, this instance will be
 * used to change the current folder, or copy/move files to the current folder.
 *
 * <p>There are 2 different modes this class can operate in. The mode to be used has to be specified when this class is
 * instanciated.
 *
 * <p>In 'folder change mode', when a file or string representing a file path is dropped, the associated FolderPanel's
 * current folder is changed:
 * <ul>
 * <li>If the file is a directory, the current folder is changed to that directory
 * <li>For any other file kind (archive, regular file...), current folder is changed to the file's parent folder
 * and the file is selected
 * </ul>
 * If more than one file (or file path) is dropped, only the first one is taken into account.
 *
 * <p>In the normal mode, files (or file paths) that are dropped can also be moved or copied to the associated FolderPanel's
 * current folder, on top of the change current folder action. The actual drop action performed (move, copy or change current folder)
 * depends on the keyboard modifiers typed by the user when dragging the files.
 *
 * <p>When the mouse cursor enters the drop-enabled component's area, it is changed to symbolize the action to be performed.
 *
 * <p>Drop events originating from the same FolderPanel are on purpose not accepted as spring-loaded folders are not
 * (yet) supported which would make the drop operation ambiguous and confusing.
 *
 * <p>3 types of dropped data flavors are supported and used in this order of priority:
 * <ul>
 * <li>FileSet: the local DataFlavor used when files are dragged from muCommander
 * <li>File list: used when files are dragged from an external application
 * <li>File paths: alternate flavor used when some text representing one or several file paths is dragged
 * from an external application
 * </ul>
 *
 *
 * @author Maxence Bernard
 */
public class FileDropTargetListener implements DropTargetListener {

    /** the FolderPanel instance used to change the current folder when a file is dropped */
    private FolderPanel folderPanel;

    /** Mode that specifies what to do when files are dropped */
    private boolean changeFolderOnlyMode;

    /** Drop action (copy or move) currenlty specified by the user */
    private int currentDropAction;

    /** Has last drag event been accepted ? */
    private boolean dragAccepted;


    /**
     * Creates a new FileDropTargetListener using the provided FolderPanel that will be used to either change the
     * current folder or copy/move when files are dropped, depending on the specified operating mode and drop action.
     *
     * @param folderPanel the FolderPanel instance used to change the current folder or copy/move when files are dropped
     * @param changeFolderOnlyMode if <code>true</code>, the FolderPanel's current folder can only be changed when file(s)
     * are dropped, files cannot be copied or moved.
     */
    public FileDropTargetListener(FolderPanel folderPanel, boolean changeFolderOnlyMode) {
        this.folderPanel = folderPanel;
        this.changeFolderOnlyMode = changeFolderOnlyMode;
    }


    /**
     * Accepts or rejects the specified event and changes the mouse cursor to match the current drop action.
     * The event will be accepted if it does not originate from the associated FolderPanel instance and supports
     * at least one of the supported DataFlavors.
     *
     * @return <code>true</code> if the event was accepted, false otherwise
     */
    private boolean acceptOrRejectDragEvent(DropTargetDragEvent event) {
        this.dragAccepted = !folderPanel.getFileDragSourceListener().isDragging()
                && (event.isDataFlavorSupported(TransferableFileSet.getFileSetDataFlavor())
                || event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                || event.isDataFlavorSupported(DataFlavor.getTextPlainUnicodeFlavor()));

        this.currentDropAction = event.getDropAction();

        Cursor cursor;
        if(dragAccepted) {
            cursor = currentDropAction==DnDConstants.ACTION_COPY?DragSource.DefaultCopyDrop
                    :currentDropAction==DnDConstants.ACTION_MOVE?DragSource.DefaultMoveDrop
                    :DragSource.DefaultLinkDrop;

            // Accept drag event
            event.acceptDrag(currentDropAction);
        }
        else {
            cursor = currentDropAction==DnDConstants.ACTION_COPY?DragSource.DefaultCopyNoDrop
                    :currentDropAction==DnDConstants.ACTION_MOVE?DragSource.DefaultMoveNoDrop
                    :DragSource.DefaultLinkNoDrop;

            // Reject drag event
            event.rejectDrag();
        }

        // Change the mouse cursor to symbolizes the drop action
        folderPanel.setCursor(cursor);

        return dragAccepted;
    }


    ////////////////////////////////////
    // FileDropTargetListener methods //
    ////////////////////////////////////

    public void dragEnter(DropTargetDragEvent event) {
        acceptOrRejectDragEvent(event);
    }


    public void dragOver(DropTargetDragEvent event) {
        // Although it doesn't look necessary, cursor needs to be set each time this method is called otherwise
        // it returns to the default one (at least under Mac OS X w/ Java 1.5)
        acceptOrRejectDragEvent(event);
    }


    public void dropActionChanged(DropTargetDragEvent event) {
        acceptOrRejectDragEvent(event);
    }


    public void dragExit(DropTargetEvent event) {
        // Restore default cursor
        folderPanel.setCursor(Cursor.getDefaultCursor());
    }


    public void drop(DropTargetDropEvent event) {
        // Restore default cursor, no matter what
        folderPanel.setCursor(Cursor.getDefaultCursor());

        // The drop() method is called even if a DropTargetDropEvent was rejected before,
        // so this test is really necessary
        if(!dragAccepted) {
            event.rejectDrop();
            return;
        }

        // Accept drop event
        event.acceptDrop(currentDropAction);

        // FileSetDataFlavor supported ?
        boolean isFileSet = event.isDataFlavorSupported(TransferableFileSet.getFileSetDataFlavor());
        // File list DataFlavor supported ?
        boolean isFileList = event.isDataFlavorSupported(DataFlavor.javaFileListFlavor);

        Transferable transferable = event.getTransferable();
        AbstractFile file;
        try {
            // If in 'change folder mode' or if the drop action is 'ACTION_LINK' in normal mode:
            // change the FolderPanel's current folder to the dropped file/folder :
            // - If the file is a directory, the current folder is changed to that directory
            // - For any other file kind (archive, regular file...), current folder is changed to the file's parent folder and the file is selected
            // If more than one file is dropped, only the first one is used
            if(changeFolderOnlyMode || currentDropAction==DnDConstants.ACTION_LINK) {
                // FileSet DataFlavor, only the first file is used
                if(isFileSet) {
                    file = ((FileSet)transferable.getTransferData(TransferableFileSet.getFileSetDataFlavor())).fileAt(0);
                }
                // File list DataFlavor, only the first file is used
                else if(isFileList) {
                    List fileList = (List)transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    // Create an AbstractFile instance
                    file = FileFactory.getFile(((File)fileList.get(0)).getAbsolutePath());
                }
                // Text plain DataFlavor: assume that the text represents a file path
                else {
                    // Read a single line only (there may be more)
                    BufferedReader br = new BufferedReader(DataFlavor.getTextPlainUnicodeFlavor().getReaderForText(transferable));

                    String path = br.readLine();
                    br.close();

                    if(path==null) {
                        file = null;
                    }
                    else {
                        // Try to create an AbstractFile instance, returned instance may be null
                        file = FileFactory.getFile(path);
                    }
                }

                // Do nothing if file is null
                if(file!=null) {
                    // If file is a directory, change current folder to that directory
                    if(file.isDirectory())
                        folderPanel.trySetCurrentFolder(file);
                    // For any other file kind (archive, regular file...), change directory to the file's parent folder
                    // and select the file
                    else
                        folderPanel.trySetCurrentFolder(file.getParent(), file);

                    // Request focus:
                    folderPanel.requestFocus();
                }

                // Report that the drop event has been successfully handled
                event.dropComplete(true);
            }
            // Normal mode: copy or move dropped files to the FolderPanel's current folder
            else {
                FileSet droppedFiles;

                // FileSet DataFlavor
                if(isFileSet) {
                    droppedFiles = (FileSet)transferable.getTransferData(TransferableFileSet.getFileSetDataFlavor());
                }
                // File list DataFlavor
                else if(isFileList) {
                    List fileList = (List)transferable.getTransferData(DataFlavor.javaFileListFlavor);

                    int nbFiles = fileList.size();
                    droppedFiles = new FileSet();
                    for(int i=0; i<nbFiles; i++) {
                        file = FileFactory.getFile(((File)fileList.get(i)).getAbsolutePath());

                        if(file!=null)
                            droppedFiles.add(file);
                    }
                }
                // Text plain DataFlavor: assume that lines designate file paths
                else {
                    BufferedReader br = new BufferedReader(DataFlavor.getTextPlainUnicodeFlavor().getReaderForText(transferable));

                    // Read input line by line and try to create AbstractFile instances
                    String path;
                    droppedFiles = new FileSet();
                    while((path=br.readLine())!=null) {
                        // Try to create an AbstractFile instance, returned instance may be null
                        file = FileFactory.getFile(path);

                        if(file!=null)
                            droppedFiles.add(file);
                    }

                    br.close();
                }

                // Do nothing if there isn't any file in the file set
                if(droppedFiles.size()>0) {
                    MainFrame mainFrame = folderPanel.getMainFrame();
                    AbstractFile destFolder = folderPanel.getCurrentFolder();
                    if(currentDropAction==DnDConstants.ACTION_MOVE) {
                        // Start moving files
                        ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("move_dialog.moving"));
                        MoveJob moveJob = new MoveJob(progressDialog, mainFrame, droppedFiles, destFolder, null, FileExistsDialog.ASK_ACTION);
                        progressDialog.start(moveJob);
                    }
                    else {
                        // Start copying files
                        ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
                        CopyJob job = new CopyJob(progressDialog, mainFrame, droppedFiles, destFolder, null, CopyJob.COPY_MODE, FileExistsDialog.ASK_ACTION);
                        progressDialog.start(job);
                    }
                }

                // Report that the drop event has been successfully handled
                event.dropComplete(true);
            }
        }
        catch(Exception e) {
            if(Debug.ON) Debug.trace("Exception caught while processing dropped transferable: "+e);

            // Report drop failure
            event.dropComplete(false);
        }
    }
}
