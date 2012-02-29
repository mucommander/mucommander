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

package com.mucommander.ui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;

import javax.swing.Action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClipboardNotifier allows an action to be dynamically enabled when the clipboard contains files, and disabled otherwise.
 *
 * <p>ClipboardNotifier requires Java 1.5 and does not work under Mac OS X (tested under Tiger with Java 1.5.0_06).
 *
 * @author Maxence Bernard
 */
public class ClipboardNotifier implements FlavorListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClipboardNotifier.class);
	
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
        try {
            action.setEnabled(ClipboardSupport.getClipboard().isDataFlavorAvailable(DataFlavor.javaFileListFlavor));
        }
        catch(Exception e) {
            // Works around "java.lang.IllegalStateException: cannot open system clipboard" thrown when the clipboard
            // is currently unavailable (ticket #164).

            LOGGER.debug("Caught an exception while querying the clipboard for files", e);
        }
    }

    ///////////////////////////////////
    // FlavorListener implementation //
    ///////////////////////////////////

    public void flavorsChanged(FlavorEvent event) {
        toggleActionState();
    }
}
