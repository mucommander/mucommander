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

package com.mucommander.ui.dialog.file;

import javax.swing.*;

/**
 * This class wraps a path, and start and end offsets for the portion of the text to be selected in a text field.
 *
 * @author Maxence Bernard
 */
public class PathFieldContent {

    protected String path;
    protected int selectionStart;
    protected int selectionEnd;

    public PathFieldContent(String path) {
        this(path, 0, path.length());
    }

    public PathFieldContent(String path, int selectionStart, int selectionEnd) {
        this.path = path;
        this.selectionStart = selectionStart;
        this.selectionEnd = selectionEnd;
    }

    /**
     * Sets the given {@link JTextField}'s text, selection start and end with that contained by this
     * <code>PathFieldContent</code>.
     *
     * @param pathField instance of {@link JTextField} to update   
     */
    public void feedToPathField(JTextField pathField) {
        // Set the initial path
        pathField.setText(path);
        // Text is selected so that user can directly type and replace path
        pathField.setSelectionStart(selectionStart);
        pathField.setSelectionEnd(selectionEnd);
    }
}
