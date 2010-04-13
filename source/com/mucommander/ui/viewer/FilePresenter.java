package com.mucommander.ui.viewer;

import java.io.IOException;

import javax.swing.JComponent;

import com.mucommander.file.AbstractFile;

/**
 * Interface that serves as a common base for the file presenter objects (above FileViewer, FileEditor).
 * The interface declares two methods:
 * 1. open - method that is used to open the given AbstraceFile for display.
 * 2. getViewedComponent - method that returns the swing component in which the file is presented.
 * 
 * @author Arik Hadas
 */
public interface FilePresenter {
	
	public void open(AbstractFile file) throws IOException;
	
	/**
     * This method returns the JComponent in which the file is presented.
     * 
     * @return The UI component in which the file is presented.
     */
    public abstract JComponent getViewedComponent();
}
