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

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;

/**
 * ClipboardNotifier allows an action to be dynamically enabled when the clipboard contains files, and disabled otherwise.
 *
 * <p>ClipboardNotifier requires Java 1.5 and does not work under Mac OS X (tested under Tiger with Java 1.5.0_06).
 *
 * @author Maxence Bernard
 */
public class ClipboardNotifier implements FlavorListener {

    /** The action to dynamically enable/disable */
    private Action action;

    /**
     * Starts monitoring the clipboard for files and dynamically enable/disable the specified action accordingly.
     * The action is initially enabled if the clipboard contains files.
     *
     * @param action the action to dynamically enable/disable when files are present/not present
     */
    public ClipboardNotifier(Action action) {
        this.action = action;

        // Toggle initial state
        toggleActionState();

        // Monitor clipboard changes
        ClipboardSupport.getClipboard().addFlavorListener(this);
    }


    /**
     * Toggle the action depending on the clipboard contents.
     */
    private void toggleActionState() {
        action.setEnabled(ClipboardSupport.getClipboard().isDataFlavorAvailable(DataFlavor.javaFileListFlavor));
    }

    ///////////////////////////////////
    // FlavorListener implementation //
    ///////////////////////////////////

    public void flavorsChanged(FlavorEvent event) {
        toggleActionState();
    }
}
