/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

import com.mucommander.file.AbstractFile;


/**
 * An abstract class to be subclassed by file editor implementations.
 *
 * <p><b>Warning:</b> the file viewer/editor API may soon receive a major overhaul.</p>
 *
 * @author Maxence Bernard
 */
public abstract class FileEditor {
	
    /** EditorFrame instance that contains this editor (may be null). */
    private EditorFrame frame;

    /** Menu bar that controls the editor's frame */
    private JMenuBar menuBar;

    /** File currently being edited. */
    private AbstractFile file;

    /**
     * Creates a new FileEditor.
     */
    public FileEditor() {}
	

    /**
     * Returns the frame which contains this editor.
     * <p>
     * This method may return <code>null</code> if the editor is not inside a EditorFrame.
     * @return the frame which contains this editor.
     * @see    #setFrame(EditorFrame)
     */
    public EditorFrame getFrame() {
        return frame;
    }

    /**
     * Sets the EditorFrame (separate window) that contains this FileEditor.
     * @param frame frame that contains this <code>FileEditor</code>.
     * @see         #getFrame()
     */
    final void setFrame(EditorFrame frame) {
        this.frame = frame;
    }


    /**
     * Returns the menu bar that controls the editor's frame. The menu bar should be retrieved using this method and
     * not by calling {@link JFrame#getJMenuBar()}, which may return <code>null</code>.
     *
     * @return the menu bar that controls the editor's frame.
     */
	public JMenuBar getMenuBar() {
        return menuBar;
    }

    /**
     * Sets the menu bar that controls the editor's frame.
     *
     * @param menuBar the menu bar that controls the editor's frame.
     */
    final void setMenuBar(JMenuBar menuBar) {
        this.menuBar = menuBar;
    }


    /**
     * Returns a description of the file currently being edited which will be used as a window title.
     * This method returns the file's path but it can be overridden to provide more information.
     * @return the editor's title.
     */
    public String getTitle() {
        return file.getAbsolutePath();
    }
	

    /**
     * Returns the file that is being edited.
     *
     * @return the file that is being edited.
     */
    public AbstractFile getCurrentFile() {
        return file;
    }

    /**
     * Sets the file that is to be edited.
     * This method will automatically be called after a file editor is created and should not be called directly.
     * @param file file that is to be edited.
     */
    final void setCurrentFile(AbstractFile file) {
        this.file = file;
    }

	
    protected void setSaveNeeded(boolean saveNeeded) {
        if(frame!=null)
            frame.setSaveNeeded(saveNeeded);
    }

    //////////////////////
    // Abstract methods //
    //////////////////////
	
    /**
     * This method is invoked when the specified file is about to be edited.
     * This method should retrieve the file and do whatever's necessary for this component can be displayed.
     *
     * @param file the file that is about to be edited.
     * @throws IOException if an I/O error occurs.
     */
    public abstract void edit(AbstractFile file) throws IOException;


    /**
     * This method is invoked when the user asked to save current file to the specified file.
     * 
     *
     * @param saveAsFile the file which should be used to save the file currently being edited
     * (path can be different from current file if the user chose 'Save as').
     * @throws IOException if an I/O error occurs.
     */
    protected abstract void saveAs(AbstractFile saveAsFile) throws IOException;
    
    /**
     * TODO: comment
     * @return
     */
    public abstract JComponent getViewedComponent();
}
