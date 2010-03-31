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
 * An abstract class to be subclassed by file viewer implementations.
 *
 * <p><b>Warning:</b> the file viewer/editor API may soon receive a major overhaul.</p>
 *
 * @author Maxence Bernard
 */
public abstract class FileViewer {
	
    /** ViewerFrame instance that contains this viewer (may be null). */
    private ViewerFrame frame;

    /** Menu bar that controls the viewer's frame */
    private JMenuBar menuBar;
	
    /** File currently being viewed. */
    private AbstractFile file;
	
    /**
     * Creates a new FileViewer.
     */
    public FileViewer() {}
	

    /**
     * Returns the frame which contains this viewer.
     * <p>
     * This method may return <code>null</code>if the viewer is not inside a ViewerFrame.
     * </p>
     * @return the frame which contains this viewer.
     * @see    #setFrame(ViewerFrame)
     */
    public ViewerFrame getFrame() {
        return frame;
    }

    /**
     * Sets the ViewerFrame (separate window) that contains this FileViewer.
     * @param frame frame that contains this <code>FileViewer</code>.
     * @see         #getFrame()
     */
    final void setFrame(ViewerFrame frame) {
        this.frame = frame;
    }


    /**
     * Returns the menu bar that controls the viewer's frame. The menu bar should be retrieved using this method and
     * not by calling {@link JFrame#getJMenuBar()}, which may return <code>null</code>.
     *
     * @return the menu bar that controls the viewer's frame.
     */
	public JMenuBar getMenuBar() {
        return menuBar;
    }

    /**
     * Sets the menu bar that controls the viewer's frame.
     *
     * @param menuBar the menu bar that controls the viewer's frame.
     */
    final void setMenuBar(JMenuBar menuBar) {
        this.menuBar = menuBar;
    }


    /**
     * Returns a description of the file currently being viewed which will be used as a window title.
     * This method returns the file's name but it can be overridden to provide more information.
     * @return this dialog's title.
     */
    public String getTitle() {
        return file.getName();
    }
	

    /**
     * Returns the file that is being viewed.
     *
     * @return the file that is being viewed.
     */
    public AbstractFile getCurrentFile() {
        return file;
    }

    /**
     * Sets the file that is to be viewed.
     * This method will automatically be called after a file viewer is created and should not be called directly.
     * @param file file that is to be viewed.
     */
    final void setCurrentFile(AbstractFile file) {
        this.file = file;
    }


    //////////////////////
    // Abstract methods //
    //////////////////////
	
    /**
     * This method is invoked when the specified file is about to be viewed.
     * This method should retrieve the file and do the necessary so that this component can be displayed.
     *
     * @param  file        the file that is about to be viewed.
     * @throws IOException if an I/O error occurs.
     */
    public abstract void view(AbstractFile file) throws IOException;
    
    /**
     * TODO: comment
     * @return
     */
    public abstract JComponent getViewedComponent();
}
