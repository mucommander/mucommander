
package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.*;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.file.AbstractFile;
import com.mucommander.job.DeleteJob;

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
public class DeleteDialog extends FocusDialog implements ActionListener {
	private MainFrame mainFrame;
	
	private JButton okButton;
	private JButton cancelButton;

	// Dialog size constraints
	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
    // Dialog width should not exceed 360, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(360,10000);	

	private FileTable activeTable;

	
	public DeleteDialog(MainFrame mainFrame) {
	    super(mainFrame, "Delete", mainFrame);
		this.mainFrame = mainFrame;
		
		activeTable = mainFrame.getLastActiveTable();
        if(activeTable.getSelectedFiles().size()==0)
        	return;

        Container contentPane = getContentPane();
        
        YBoxPanel mainPanel = new YBoxPanel();
        mainPanel.add(new JLabel("Permanently delete selected file(s) ?"));

		mainPanel.addSpace(10);
		contentPane.add(mainPanel, BorderLayout.NORTH);
        
        okButton = new JButton("Delete");
		cancelButton = new JButton("Cancel");
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);
        		
		// Escape key disposes dialog
        EscapeKeyAdapter escapeKeyAdapter = new EscapeKeyAdapter(this);
        okButton.addKeyListener(escapeKeyAdapter);
        cancelButton.addKeyListener(escapeKeyAdapter);

		// OK button will receive initial focus
		setInitialFocusComponent(okButton);		

        // Selects OK when enter is pressed
        getRootPane().setDefaultButton(okButton);
        
		setMinimumSize(MINIMUM_DIALOG_DIMENSION);
		setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
		showDialog();
	}



	/**
	 * Delete selected files. This method is trigged by the 'OK' button or return key.
	 */
	public void doDelete() {
        // Figures out which files to delete
        Vector filesToDelete = activeTable.getSelectedFiles();
        if(filesToDelete.size()==0)
        	return;
                    
        // Starts deleting files
		ProgressDialog progressDialog = new ProgressDialog(mainFrame, "Deleting files");
		DeleteJob deleteJob = new DeleteJob(mainFrame, progressDialog, filesToDelete);
        deleteJob.start();
    	progressDialog.start(deleteJob);
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
		if(source == okButton) {
			doDelete();
		}
	}
	
	
}
