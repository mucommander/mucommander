package com.mucommander.ui.viewer;

import javax.swing.*;
import com.mucommander.file.AbstractFile;
import java.io.IOException;

public abstract class FileViewer extends JPanel {
	
	protected ViewerFrame frame;

	
	/**
	 * Creates a new instance of a FileViewer.
	 */
	public FileViewer(ViewerFrame frame) {
		this.frame = frame;
	}

	/**
	 * Returns the frame which contains this viewer.
	 */
	public ViewerFrame getFrame() {
		return frame;
	}
	
	/**
	 * Starts viewing the given file.
	 *
	 * @param fileToView the file that needs to be displayed.
	 * @param isSeparateWindow <code>true</code> if the panel is put in a separate window
	 */
	public abstract void startViewing(AbstractFile fileToView, boolean isSeparateWindow) throws IOException;
	
	
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
}