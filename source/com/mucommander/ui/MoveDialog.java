
package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.*;
import com.mucommander.ui.table.FileTable;
import com.mucommander.file.AbstractFile;
import com.mucommander.job.MoveJob;
import com.mucommander.text.Translator;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.Vector;


/**
 * Dialog invoked when the user wants to move or rename (F6) files.
 *
 * @author Maxence Bernard
 */
public class MoveDialog extends FocusDialog implements ActionListener {
	private MainFrame mainFrame;
	
	private JTextField movePathField;
	
	private JButton okButton;
	private JButton cancelButton;

	private Vector filesToMove;

	// Dialog size constraints
	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
    // Dialog width should not exceed 360, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(360,10000);	
	
	
	public MoveDialog(MainFrame mainFrame, boolean isShiftDown) {
		super(mainFrame, null, mainFrame);
		this.mainFrame = mainFrame;
		
		FileTable activeTable = mainFrame.getLastActiveTable();
		FileTable table1 = mainFrame.getBrowser1().getFileTable();
		FileTable table2 = mainFrame.getBrowser2().getFileTable();
    	this.filesToMove = activeTable.getSelectedFiles();
		int nbFiles = filesToMove.size();
		if(nbFiles==0)
    		return;

		boolean rename = isShiftDown && nbFiles==1;
		setTitle(Translator.get(rename?"move_dialog.rename":"move_dialog.move"));
        
		Container contentPane = getContentPane();
        YBoxPanel mainPanel = new YBoxPanel();
		
		JLabel label = new JLabel(Translator.get(rename?"move_dialog.rename_description":"move_dialog.move_description"));
        mainPanel.add(label);

		String fieldText;
		if(isShiftDown && nbFiles==1) {
			fieldText = ((AbstractFile)filesToMove.elementAt(0)).getName();
		}
		else {
			AbstractFile destFolder = (activeTable==table1?table2:table1).getCurrentFolder();
			fieldText = destFolder.getAbsolutePath(true);
			if(nbFiles==1)
				fieldText += ((AbstractFile)filesToMove.elementAt(0)).getName();
		}

		movePathField = new JTextField(fieldText);
        // Text is selected so that user can directly type and replace path
        movePathField.setSelectionStart(0);
        movePathField.setSelectionEnd(fieldText.length());
        movePathField.addActionListener(this);
		
		mainPanel.add(movePathField);
		mainPanel.addSpace(10);
		
        contentPane.add(mainPanel, BorderLayout.NORTH);
		
		// OK / Cancel buttons panel
        okButton = new JButton(Translator.get(rename?"move_dialog.rename":"move_dialog.move"));
        cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);

        // Escape key disposes dialog
		EscapeKeyAdapter escapeKeyAdapter = new EscapeKeyAdapter(this);
		movePathField.addKeyListener(escapeKeyAdapter);
		okButton.addKeyListener(escapeKeyAdapter);
		cancelButton.addKeyListener(escapeKeyAdapter);

        // Selects OK when enter is pressed
        getRootPane().setDefaultButton(okButton);
        
		// Path field will receive initial focus
		setInitialFocusComponent(movePathField);		
			
		setMinimumSize(MINIMUM_DIALOG_DIMENSION);
		setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
		
		showDialog();
	}



	/**
	 * Starts a MoveJob. This method is trigged by the 'OK' button or return key.
	 */
	private void doMove() {
		String destPath = movePathField.getText();
	    FileTable activeTable = mainFrame.getLastActiveTable();

		// Resolves destination folder
		Object ret[] = mainFrame.resolvePath(destPath);
		// The path entered doesn't correspond to any existing folder
		if (ret==null || (filesToMove.size()>1 && ret[1]!=null)) {
			showErrorDialog(Translator.get("move_dialog.folder_does_not_exist", destPath), Translator.get("move_dialog.error_title"));
			return;
		}

		AbstractFile destFolder = (AbstractFile)ret[0];
		String newName = (String)ret[1];

		if (newName==null && activeTable.getCurrentFolder().equals(destFolder)) {
			showErrorDialog(Translator.get("move_dialog.same_source_destination"), Translator.get("move_dialog.error_title"));
			return;
		}

		if (filesToMove.contains(destFolder)) {
			showErrorDialog(Translator.get("move_dialog.cannot_move_to_itself"), Translator.get("move_dialog.error_title"));
			return;
		}
		
		// Starts moving files
		ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("move_dialog.moving"));
		MoveJob moveJob = new MoveJob(mainFrame, progressDialog, filesToMove, newName, destFolder);
	    moveJob.start();
	    progressDialog.start(moveJob);
	}


	private void showErrorDialog(String msg, String title) {
		JOptionPane.showMessageDialog(mainFrame, msg, title, JOptionPane.ERROR_MESSAGE);

		// FileTable lost focus
		mainFrame.getLastActiveTable().requestFocus();
	}


	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		dispose();
		
		// OK Button
		if(source == okButton || source == movePathField) {
			doMove();
		}
	}
	
	
}