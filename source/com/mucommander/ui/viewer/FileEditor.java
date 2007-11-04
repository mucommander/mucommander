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

package com.mucommander.ui.viewer;

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.theme.*;

import javax.swing.*;
import java.io.IOException;


/**
 * An abstract class to be subclassed by file editor implementations.
 *
 * <p><b>Warning:</b> the file viewer/editor API may soon receive a major overhaul.</p>
 *
 * @author Maxence Bernard
 */
public abstract class FileEditor extends JPanel implements ThemeListener {
	
    /** EditorFrame instance that contains this editor (may be null) */
    protected EditorFrame frame;
	
    /** File currently being edited */
    protected AbstractFile file;

    /**
     * Creates a new FileEditor.
     */
    public FileEditor() {
        setBackground(ThemeManager.getCurrentColor(Theme.EDITOR_BACKGROUND_COLOR));
        ThemeManager.addCurrentThemeListener(this);
    }
	

    /**
     * Sets the EditorFrame (separate window) that contains this FileEditor.
     */
    public void setFrame(EditorFrame frame) {
        this.frame = frame;
    }
	
	
    /**
     * Returns the frame which contains this editor, may return <code>null</code>
     * if the editor is not inside a EditorFrame.
     */
    protected EditorFrame getFrame() {
        return frame;
    }
	
	
    /**
     * Returns <code>true</code> if the given file can be handled by this FileEditor.<br>
     * The FileEditor may base its decision only upon the filename and its extension or may
     * wish to read some of the file and compare it to a magic number.
     */
    public static boolean canEditFile(AbstractFile file) {
        return false;
    }
	
	
    /**
     * Returns maximum file size this FileEditor can handle for sure, -1 if there is no such limit.
     * If a user wish to edit a file that exceeds this size, he/she will be asked if he/she still
     * wants to edit it.
     */
    public long getMaxRecommendedSize() {
        return -1;
    }


    /**
     * Returns a description of the file currently being edited which will be used as a window title.
     * This method returns the file's path but it can be overridden to provide more information.
     */
    public String getTitle() {
        return file.getAbsolutePath();
    }
	

    /**
     * Sets the file that is to be edited.
     * This method will automatically be called after a file editor is created and should not be called directly.
     */
    public final void setCurrentFile(AbstractFile file) {
        this.file = file;
    }

	
    protected void setSaveNeeded(boolean saveNeeded) {
        if(frame!=null)
            frame.setSaveNeeded(saveNeeded);
    }


    ///////////////////////////
    // ThemeListener methods //
    ///////////////////////////

    /**
     * Receives theme color changes notifications.
     */
    public void colorChanged(ColorChangedEvent event) {
        if(event.getColorId() == Theme.EDITOR_BACKGROUND_COLOR)
            setBackground(event.getColor());

        repaint();
    }

    /**
     * Not used, implemented as a no-op.
     */
    public void fontChanged(FontChangedEvent event) {
    }


    //////////////////////
    // Abstract methods //
    //////////////////////
	
    /**
     * This method is invoked when the specified file is about to be edited.
     * This method should retrieve the file and do whatever's necessary for this component can be displayed.
     *
     * @param file the file that is about to be edited.
     */
    public abstract void edit(AbstractFile file) throws IOException;


    /**
     * This method is invoked when the user asked to save current file to the specified file.
     * 
     *
     * @param saveAsFile the file which should be used to save the file currently being edited
     * (path can be different from current file if the user chose 'Save as').
     */
    protected abstract void saveAs(AbstractFile saveAsFile) throws IOException;
}
