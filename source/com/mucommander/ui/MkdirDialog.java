
package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.*;
import com.mucommander.ui.table.FileTable;
import com.mucommander.file.AbstractFile;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.Vector;
import java.io.IOException;


/**
 * Dialog invoked when the user wants to create a new folder (F7).
 *
 * @author Maxence Bernard
 */
public class MkdirDialog extends FocusDialog implements ActionListener {
	private MainFrame mainFrame;
	
	private JTextField mkdirPathField;
	
	private JButton okButton;
	private JButton cancelButton;

	// Dialog size constrains
	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
    // Dialog width should not exceed 360, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(360,10000);	

	private FileTable activeTable;

	public MkdirDialog(MainFrame mainFrame) {
	    super(mainFrame, "Make directory", mainFrame);
		this.mainFrame = mainFrame;
		
		activeTable = mainFrame.getLastActiveTable();

		Container contentPane = getContentPane();

		YBoxPanel mainPanel = new YBoxPanel();
        mainPanel.add(new JLabel("Create directory"));
        mkdirPathField = new JTextField();
        mkdirPathField.addActionListener(this);
        mainPanel.add(mkdirPathField);
		
		mainPanel.addSpace(10);
        contentPane.add(mainPanel, BorderLayout.NORTH);
        
        okButton = new JButton("Create");
        cancelButton = new JButton("Cancel");
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);

        // Escape key disposes dialog
        EscapeKeyAdapter escapeKeyAdapter = new EscapeKeyAdapter(this);
        mkdirPathField.addKeyListener(escapeKeyAdapter);
        okButton.addKeyListener(escapeKeyAdapter);
        cancelButton.addKeyListener(escapeKeyAdapter);

		// Path field will receive initial focus
		setInitialFocusComponent(mkdirPathField);		

        // Selects OK when enter is pressed
        getRootPane().setDefaultButton(okButton);
        
		setMinimumSize(MINIMUM_DIALOG_DIMENSION);
		setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
		showDialog();
	}



	/**
	 * Creates a new directory. This method is trigged by the 'OK' button or return key.
	 */
	public void doMkdir() {
		String dirPath = mkdirPathField.getText();
		
	    try {
		    // Resolves destination folder
		    Object ret[] = mainFrame.resolvePath(dirPath);
		    // The path entered doesn't correspond to any existing folder
		    if (ret==null) {
		    	showErrorDialog("Incorrect path "+dirPath, "mkdir error");
		    	return;
		    }

			if(ret[1]==null) {
		    	showErrorDialog("Directory "+dirPath+" already exists.", "mkdir error");
		    	return;
		    }

		    AbstractFile folder = (AbstractFile)ret[0];
		    String newName = (String)ret[1];

	        folder.mkdir(newName);
	        activeTable.refresh();
						
			// Finds the row corresponding to the newly created folder
			// and makes it the current row.
			if (activeTable.getCurrentFolder().equals(folder)) {
				AbstractFile createdFolder = AbstractFile.getAbstractFile(folder.getAbsolutePath()+folder.getSeparator()+newName);
				activeTable.selectFile(createdFolder);
			}
		}
	    catch(IOException ex) {
	        showErrorDialog("Unable to create directory "+dirPath, "mkdir error");
	    }    

/*		
		FileTable table1 = mainFrame.getBrowser1().getFileTable();
		FileTable table2 = mainFrame.getBrowser2().getFileTable();
	    FileTable unactiveTable = activeTable==table1?table2:table1;
	    if (unactiveTable.getCurrentFolder().equals(activeTable.getCurrentFolder()))
	    	try {
	    		unactiveTable.refresh();
			}
		    catch(IOException e) {
		    	// Probably should do something when a folder becomes unreadable (probably doesn't exist anymore)
		    	// like switching to a root folder        
		    }
*/	
		activeTable.requestFocus();
	}

	
	private void showErrorDialog(String msg, String title) {
		JOptionPane.showMessageDialog(mainFrame, msg, title, JOptionPane.ERROR_MESSAGE);

		// FileTable lost focus
		activeTable.requestFocus();
	}


	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		dispose();
		
		// OK Button
		if(source == okButton || source == mkdirPathField) {
			doMkdir();
		}
	}
	
	
}
