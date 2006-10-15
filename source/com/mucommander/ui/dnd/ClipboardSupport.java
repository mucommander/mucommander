package com.mucommander.ui.dnd;

import com.mucommander.file.FileSet;

import java.awt.datatransfer.*;
import java.awt.*;

/**
 * This class provides methods to more easily interact with the system clipboard.
 *
 * @author Maxence Bernard
 */
public class ClipboardSupport {

    /**
     * Returns the system clipboard's contents as a <code>Transferable</code>, <code>null</code>
     * if it currently has no contents.
     */
    public static Transferable getClipboardContents() {
        try {
            return getClipboard().getContents(null);
        }
        catch(IllegalStateException e) {
            return null;
        }
    }

    /**
     * Sets the contents of the system clipboard.
     *
     * @param transferable the data to transfer to the clipboard
     */
    public static void setClipboardContents(Transferable transferable) {
        try {
            getClipboard().setContents(transferable, null);
        }
        catch(IllegalStateException e) {}
    }


    /**
     * Returns the files contained by the system clipboard as a {@link com.mucommander.file.FileSet}, <code>null</code>
     * if it currently has no contents or if the item(s) contained are not files.
     */
    public static FileSet getClipboardFiles() {
        Transferable transferable = getClipboardContents();
        // Return null if Clipboard has no contents
        if(transferable==null)
            return null;

        // May return null if no file could be retrieved from the transferable instance
        return TransferableFileSet.getTransferFiles(transferable);
    }

    /**
     * Transfers the files contained in the specified {@link com.mucommander.file.FileSet} to the system clipboard.
     * The data will be transferred as a {@link TransferableFileSet}.
     *
     * @param fileSet the files to transfer to the system clipboard.
     */
    public static void setClipboardFiles(FileSet fileSet) {
        TransferableFileSet tfs = new TransferableFileSet(fileSet);

        // Disable FileSetDataFlavor support which would otherwise throw an exception because the data is not serializable
        tfs.setFileSetDataFlavorSupported(false);

        setClipboardContents(tfs);
    }


    /**
     * Returns an instance of the system clipboard.
     */
    public static Clipboard getClipboard() {
        return Toolkit.getDefaultToolkit().getSystemClipboard();
    }
}
