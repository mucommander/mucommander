package com.mucommander.ui.viewer;

import com.mucommander.file.AbstractFile;
//import com.mucommander.ui.MainFrame;
//import com.mucommander.ui.table.FileTable;
//import com.mucommander.ui.table.FileTableModel;

import javax.swing.*;
import java.awt.Color;

import java.io.IOException;

public abstract class FileViewer extends JPanel {
	
	protected ViewerFrame frame;
	protected AbstractFile file;
	
	public final static Color BG_COLOR = new Color(0xFFFFFF);

	
	/**
	 * Creates a new FileViewer inside the given ViewerFrame.
	 */
//	public FileViewer(ViewerFrame frame) {
//		this.frame = frame;
	public FileViewer() {
		setBackground(BG_COLOR);
	}
	

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
	
	
	public void setCurrentFile(AbstractFile file) throws IOException {
		this.file = file;
		try {
			view(file);
		} catch(OutOfMemoryError e) {
			throw new IOException();
		}
	}
	
/*
	protected AbstractFile getNextFileInFolder(AbstractFile file, boolean loop) {
//System.out.println("getNextFileInFolder");
		return getNextFile(file, true, loop);
	}
	
	protected AbstractFile getPreviousFileInFolder(AbstractFile file, boolean loop) {
//System.out.println("getPreviousFileInFolder");
		return getNextFile(file, false, loop);
	}
	
	private AbstractFile getNextFile(AbstractFile file, boolean forward, boolean loop) {
		AbstractFile folder = file.getParent();
		MainFrame mainFrame = frame.getMainFrame();
		FileTable table = mainFrame.getLastActiveTable();

//System.out.println("getNextFile : 0");
		
		if(!table.getCurrentFolder().equals(folder)) {
			table = mainFrame.getUnactiveTable();
//System.out.println("getNextFile : 1");
			if(!table.getCurrentFolder().equals(folder))
				return null;
		}
//System.out.println("getNextFile : 2");
		
		
		FileTableModel model = (FileTableModel)table.getModel();
		int rowCount = model.getRowCount();
		int fileRow = table.getFileRow(file);
		
		int newFileRow = fileRow;
		AbstractFile newFile;
		if(forward) {
			do {
//System.out.println("forward1 "+fileRow+" "+newFileRow+" "+rowCount);

				if(newFileRow==rowCount-1) {
					if(loop)
						newFileRow = 0;
					else
						return null;
				}
				else
					newFileRow++;

				newFile = model.getFileAtRow(newFileRow);

//System.out.println("forward2 "+fileRow+" "+newFileRow+" "+newFile);
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
	 * This method should retrieve the file so that this component can be displayed.
	 *
	 * @param fileToView the file that needs to be displayed.
	 */
	protected abstract void view(AbstractFile fileToView) throws IOException;

}