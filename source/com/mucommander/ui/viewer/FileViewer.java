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

package com.mucommander.ui.viewer;

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.theme.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * 
 *
 * @author Maxence Bernard
 */
public abstract class FileViewer extends JPanel implements ThemeListener {
	
    /** ViewerFrame instance that contains this viewer (may be null) */
    protected ViewerFrame frame;
	
    /** File currently being viewed */
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
     */
    public void setFrame(ViewerFrame frame) {
        this.frame = frame;
    }
	
	
    /**
     * Returns the frame which contains this viewer, may return <code>null</code>
     * if the viewer is not inside a ViewerFrame.
     */
    protected ViewerFrame getFrame() {
        return frame;
    }
	
	
    /**
     * Returns <code>true</code> if the given file can be handled by this FileViewer.<br>
     * The FileViewer may base its decision only upon the filename and its extension or may
     * wish to read some of the file and compare it to a magic number.
     */
    public static boolean canViewFile(AbstractFile file) {
        return false;
    }
	
	
    /**
     * Returns maximum file size this FileViewer can handle for sure, -1 if there is no such limit.
     * If a user wish to view a file that exceeds this size, he/she will be asked if he/she still
     * wants to view it.
     */
    public long getMaxRecommendedSize() {
        return -1;
    }


    /**
     * Returns a description of the file currently being viewed which will be used as a window title.
     * This method returns the file's name but it can be overridden to provide more information.
     */
    public String getTitle() {
        return file.getName();
    }
	

    /**
     * Sets the file that is to be viewed.
     * This method will automatically be called after a file viewer is created and should not be called directly.
     */
    public final void setCurrentFile(AbstractFile file) {
        this.file = file;
    }
	
    /*
      protected AbstractFile getNextFileInFolder(AbstractFile file, boolean loop) {
      return getNextFile(file, true, loop);
      }
	
      protected AbstractFile getPreviousFileInFolder(AbstractFile file, boolean loop) {
      return getNextFile(file, false, loop);
      }
	
      private AbstractFile getNextFile(AbstractFile file, boolean forward, boolean loop) {
      AbstractFile folder = file.getParent();
      MainFrame mainFrame = frame.getMainFrame();
      FileTable table = mainFrame.getActiveTable();

      if(!table.getCurrentFolder().equals(folder)) {
      table = mainFrame.getInactiveTable();
      if(!table.getCurrentFolder().equals(folder))
      return null;
      }
		
      FileTableModel model = table.getFileTableModel();
      int rowCount = model.getRowCount();
      int fileRow = table.getFileRow(file);
		
      int newFileRow = fileRow;
      AbstractFile newFile;
      if(forward) {
      do {
      if(newFileRow==rowCount-1) {
      if(loop)
      newFileRow = 0;
      else
      return null;
      }
      else
      newFileRow++;

      newFile = model.getFileAtRow(newFileRow);
      }
      while(!canViewFile(newFile) && fileRow!=newFileRow);
      }
      else {
      do {
      if(newFileRow==0) {
      if(loop)
      newFileRow = rowCount-1;
      else
      return null;
      }
      else
      newFileRow--;

      newFile = model.getFileAtRow(newFileRow);
      }
      while(!canViewFile(newFile) && fileRow!=newFileRow);
      }

      if(fileRow==newFileRow)
      return null;
		
      return newFile;
      }

    */
    //////////////////////
    // Abstract methods //
    //////////////////////
	
    /**
     * This method is invoked when the specified file is about to be viewed.
     * This method should retrieve the file and do the necessary so that this component can be displayed.
     *
     * @param file the file that is about to be viewed.
     */
    public abstract void view(AbstractFile file) throws IOException;


    // - Theme listening -------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /**
     * Receives theme color changes notifications.
     * @param colorId identifier of the color that has changed.
     * @param color   new value for the color.
     */
    public void colorChanged(int colorId, Color color) {
        if(colorId == Theme.EDITOR_BACKGROUND_COLOR)
            setBackground(color);

        repaint();
    }

    /**
     * Not used.
     */
    public void fontChanged(int fontId, Font font) {}
}
