
package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.*;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.job.DeleteJob;
import com.mucommander.text.Translator;

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

	/** Files to delete */
	private Vector files;
	
	private JButton okButton;
	private JButton cancelButton;

	// Dialog size constraints
	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
    // Dialog width should not exceed 360, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(360,10000);	

	
	public DeleteDialog(MainFrame mainFrame, Vector files) {
	    super(mainFrame, Translator.get("delete_dialog.delete"), mainFrame);
		this.mainFrame = mainFrame;
		this.files = files;
		
        Container contentPane = getContentPane();
        
        YBoxPanel mainPanel = new YBoxPanel();
        mainPanel.add(new JLabel(Translator.get("delete_dialog.confirmation")));

		mainPanel.addSpace(10);
		contentPane.add(mainPanel, BorderLayout.NORTH);
        
        okButton = new JButton(Translator.get("delete_dialog.delete"));
		cancelButton = new JButton(Translator.get("cancel"));
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
        // Starts deleting files
		ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("delete_dialog.deleting"));
		DeleteJob deleteJob = new DeleteJob(progressDialog, mainFrame, files);
    	progressDialog.start(deleteJob);
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
