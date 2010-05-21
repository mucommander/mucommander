package com.mucommander.ui.viewer;

import java.io.IOException;

import com.mucommander.commons.file.AbstractFile;

/**
 *Interface that serves as a common base for the file presenter objects (FileViewer, FileEditor).
 * The interface declares one method:
 * open - method that is used to open the given AbstraceFile for display.
 * 
 * @author Arik Hadas
 */
public interface FilePresenter {
	
	public void open(AbstractFile file) throws IOException;
}
