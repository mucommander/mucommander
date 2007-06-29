/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.ui.dnd;

import com.mucommander.file.util.FileSet;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;

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
     * Returns the files contained by the system clipboard as a {@link com.mucommander.file.util.FileSet}, <code>null</code>
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
     * Transfers the files contained in the specified {@link com.mucommander.file.util.FileSet} to the system clipboard.
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
