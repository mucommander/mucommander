
package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.*;
import com.mucommander.ui.table.FileTable;
import com.mucommander.file.AbstractFile;
import com.mucommander.job.CopyJob;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.Vector;


/**
 * Dialog invoked when the user wants to copy (F5) or unzip (thru file menu) files.
 *
 * @author Maxence Bernard
 */
public class CopyDialog extends FocusDialog implements ActionListener {
	private MainFrame mainFrame;
	private boolean unzipDialog;
	
	private JTextField copyPathField;
	
	private JButton okButton;
	private JButton cancelButton;

	private Vector filesToCopy;

	// Dialog size constrains
	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
    // Dialog width should not exceed 360, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(360,10000);	
	
	/**
	 * Creates and displays a new CopyDialog.
	 *
	 * @param mainFrame the main frame this dialog is attached to.
	 * @param unzipDialog true if this dialog has been invoked by the 'unzip' action.
	 */
	public CopyDialog(MainFrame mainFrame, boolean unzipDialog) {
		super(mainFrame, (unzipDialog?"Unzip":"Copy"), mainFrame);
		this.mainFrame = mainFrame;
	    this.unzipDialog = unzipDialog;
		
		FileTable activeTable = mainFrame.getLastActiveTable();
		FileTable table1 = mainFrame.getBrowser1().getFileTable();
		FileTable table2 = mainFrame.getBrowser2().getFileTable();
    	this.filesToCopy = activeTable.getSelectedFiles();
		int nbFiles = filesToCopy.size();
		if(nbFiles==0)
    		return;
        
		Container contentPane = getContentPane();
        JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JLabel label = new JLabel((unzipDialog?"Unzip":"Copy")+" selected file(s) to");
		label.setAlignmentX(LEFT_ALIGNMENT);
        mainPanel.add(label);

		AbstractFile destFolder = (activeTable==table1?table2:table1).getCurrentFolder();
        String fieldText = destFolder.getAbsolutePath()+destFolder.getSeparator();
        if(nbFiles==1 && !unzipDialog)
			fieldText += ((AbstractFile)filesToCopy.elementAt(0)).getName();

		copyPathField = new JTextField(fieldText);
        // Text is selected so that user can directly type and replace path
        copyPathField.setSelectionStart(0);
        copyPathField.setSelectionEnd(fieldText.length());
        copyPathField.addActionListener(this);
		
		copyPathField.setAlignmentX(LEFT_ALIGNMENT);
		mainPanel.add(copyPathField);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
        contentPane.add(mainPanel, BorderLayout.NORTH);
		
			// OK / Cancel buttons panel
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);

        // Escape key disposes dialog
		EscapeKeyAdapter escapeKeyAdapter = new EscapeKeyAdapter(this);
		copyPathField.addKeyListener(escapeKeyAdapter);
		okButton.addKeyListener(escapeKeyAdapter);
		cancelButton.addKeyListener(escapeKeyAdapter);

        // Selects OK when enter is pressed
        getRootPane().setDefaultButton(okButton);
        
		// Path field will receive initial focus
		setInitialFocusComponent(copyPathField);		
			
		setResizable(true);
		setMinimumSize(MINIMUM_DIALOG_DIMENSION);
		setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
		
		showDialog();
	}


	/**
	 * Starts a CopyJob. This method is trigged by the 'OK' button or return key.
	 */
	public void doCopy() {
		String destPath = copyPathField.getText();
		FileTable activeTable = mainFrame.getLastActiveTable();

		// Resolves destination folder
		Object ret[] = mainFrame.resolvePath(destPath);
		// The path entered doesn't correspond to any existing folder
		if (ret==null || ((filesToCopy.size()>1 || unzipDialog) && ret[1]!=null)) {
			showErrorDialog("Folder "+destPath+" doesn't exist.", (unzipDialog?"Unzip":"Copy")+" error");
			return;
		}

		AbstractFile destFolder = (AbstractFile)ret[0];
		String newName = (String)ret[1];

		if (!unzipDialog && newName==null && activeTable.getCurrentFolder().equals(destFolder)) {
			showErrorDialog("Source and destination are the same.", (unzipDialog?"Unzip":"Copy")+" error");
			return;
		}

		// Starts moving files
		ProgressDialog progressDialog = new ProgressDialog(mainFrame, (unzipDialog?"Unzipping":"Copying")+" files");
		CopyJob copyJob = new CopyJob(mainFrame, progressDialog, filesToCopy, newName, destFolder, unzipDialog);
		copyJob.start();
		progressDialog.start(copyJob);
	}


	/**
	 * Displays an error message.
	 */
	private void showErrorDialog(String msg, String title) {
		JOptionPane.showMessageDialog(mainFrame, msg, title, JOptionPane.ERROR_MESSAGE);

		// FileTable lost focus
		mainFrame.getLastActiveTable().requestFocus();
	}


	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		dispose();
		
		// OK Button
		if(source == okButton || source == copyPathField) {
			doCopy();
		}
	}
	
	
}