package com.mucommander.ui.viewer;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;

import com.mucommander.commons.file.AbstractFile;

/**
 * Abstract class that serves as a common base for the file presenter objects (FileViewer, FileEditor).
 * The interface declares one method:
 * open - method that is used to open the given AbstraceFile for display.
 * 
 * @author Arik Hadas
 */
public abstract class FilePresenter extends JScrollPane {
	
	public FilePresenter() {
		super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}
	
	/**
	 * Open a given AbstraceFile for display.
	 * 
	 * @param file the file to be presented
	 * @throws IOException in case of an I/O problem
	 */
	public abstract void open(AbstractFile file) throws IOException;
	
	/**
     * Returns the menu bar that controls the presenter's frame. The menu bar should be retrieved using this method and
     * not by calling {@link JFrame#getJMenuBar()}, which may return <code>null</code>.
     *
     * @return the menu bar that controls the presenter's frame.
     */
	public abstract JMenuBar getMenuBar();
}
