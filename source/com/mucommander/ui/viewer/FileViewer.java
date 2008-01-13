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
 * An abstract class to be subclassed by file viewer implementations.
 *
 * <p><b>Warning:</b> the file viewer/editor API may soon receive a major overhaul.</p>
 *
 * @author Maxence Bernard
 */
public abstract class FileViewer extends JPanel implements ThemeListener {
	
    /** ViewerFrame instance that contains this viewer (may be null). */
    protected ViewerFrame frame;
	
    /** File currently being viewed. */
    protected AbstractFile file;
	
    /**
     * Creates a new FileViewer.
     */
    public FileViewer() {
        setBackground(ThemeManager.getCurrentColor(Theme.EDITOR_BACKGROUND_COLOR));
        ThemeManager.addCurrentThemeListener(this);
    }
	

    /**
     * Sets the ViewerFrame (separate window) that contains this FileViewer.
     * @param frame frame that contains this <code>FileViewer</code>.
     * @see         #getFrame()
     */
    public void setFrame(ViewerFrame frame) {
        this.frame = frame;
    }
	
	
    /**
     * Returns the frame which contains this viewer.
     * <p>
     * This method may return <code>null</code>if the viewer is not inside a ViewerFrame.
     * </p>
     * @return the frame which contains this viewer.
     * @see    #setFrame(ViewerFrame)
     */
    protected ViewerFrame getFrame() {
        return frame;
    }
	
	
    /**
     * Returns <code>true</code> if the given file can be handled by this FileViewer.
     * <p>
     * The FileViewer may base its decision only upon the filename and its extension or may
     * wish to read some of the file and compare it to a magic number.
     * </p>
     * @param  file file that must be checked.
     * @return      <code>true</code> if the given file can be handled by this <code>FileViewer</code>, <code>false</code> otherwise.
     */
    public static boolean canViewFile(AbstractFile file) {
        return false;
    }
	
	
    /**
     * Returns maximum file size this FileViewer can handle for sure.
     * <p>
     * If there is no maximum limit, returns <code>-1</code>.
     * </p>
     * <p>
     * If a user wish to view a file that exceeds this size, he/she will be asked if he/she still
     * wants to view it.
     * </p>
     * @return the maximum file size this file viewer can handle.
     */
    public long getMaxRecommendedSize() {
        return -1;
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
     * Sets the file that is to be viewed.
     * This method will automatically be called after a file viewer is created and should not be called directly.
     * @param file file that is to be viewed.
     */
    public final void setCurrentFile(AbstractFile file) {
        this.file = file;
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
     * This method is invoked when the specified file is about to be viewed.
     * This method should retrieve the file and do the necessary so that this component can be displayed.
     *
     * @param  file        the file that is about to be viewed.
     * @throws IOException if an I/O error occurs.
     */
    public abstract void view(AbstractFile file) throws IOException;

}
