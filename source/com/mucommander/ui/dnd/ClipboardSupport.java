package com.mucommander.ui.dnd;

import com.mucommander.file.FileSet;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.Clipboard;
import java.awt.*;

/**
 * @author Maxence Bernard
 */
public class ClipboardSupport {

    public static Transferable getClipboardContents() {
        try {
            return getClipboard().getContents(null);
        }
        catch(IllegalStateException e) {
            return null;
        }
    }

    public static void setClipboardContents(Transferable transferable) {
        try {
            getClipboard().setContents(transferable, null);
        }
        catch(IllegalStateException e) {}
    }


    public static FileSet getClipboardFiles() {
        Transferable transferable = getClipboardContents();
        // Return null if Clipboard has no contents
        if(transferable==null)
            return null;

        // May return null if no file could be retrieved from the transferable instance
        return TransferableFileSet.getTransferFiles(transferable);
    }

    public static void setClipboardFiles(FileSet fileSet) {
        TransferableFileSet tfs = new TransferableFileSet(fileSet);

        // Disable FileSetDataFlavor support which would otherwise throw an exception because the data is not serializable
        tfs.setFileSetDataFlavorSupported(false);

        setClipboardContents(tfs);
    }


    private static Clipboard getClipboard() {
        return Toolkit.getDefaultToolkit().getSystemClipboard();
    }
}
