
package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.*;
import com.mucommander.text.Translator;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * Abstract Dialog which displays an input field in order to enter a destination path.
 * This dialog is used by CopyDialog, MoveDialog and HttpDownloadDialog.
 *
 * @author Maxence Bernard
 */
public abstract class DestinationDialog extends FocusDialog implements ActionListener {

	protected MainFrame mainFrame;
	
	protected JTextField pathField;
	
	protected JButton okButton;
	protected JButton cancelButton;

	// Dialog size constraints
	protected final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
    // Dialog width should not exceed 360, height is not an issue (always the same)
    protected final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(360,10000);	
	

	/**
	 * Creates a new DestinationDialog.
	 *
	 * @param mainFrame the main frame this dialog is attached to.
	 */
	public DestinationDialog(MainFrame mainFrame) {
		super(mainFrame, null, mainFrame);
		this.mainFrame = mainFrame;
	}
	
	
	/**
	 * Creates a new DestinationDialog.
	 *
	 * @param mainFrame the main frame this dialog is attached to.
	 */
	public DestinationDialog(MainFrame mainFrame, String title, String labelText, String okText) {
		super(mainFrame, null, mainFrame);
		this.mainFrame = mainFrame;

		init(title, labelText, okText);
	}
	
	
	protected void init(String title, String labelText, String okText) {
		setTitle(title);
		
		Container contentPane = getContentPane();
        YBoxPanel mainPanel = new YBoxPanel();
		
		JLabel label = new JLabel(labelText);
        mainPanel.add(label);

		pathField = new JTextField();
        pathField.addActionListener(this);
		
		mainPanel.add(pathField);
		mainPanel.addSpace(10);
		
        contentPane.add(mainPanel, BorderLayout.NORTH);
		
		// OK / Cancel buttons panel
        okButton = new JButton(okText);
        cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);

        // Escape key disposes dialog
		EscapeKeyAdapter escapeKeyAdapter = new EscapeKeyAdapter(this);
		pathField.addKeyListener(escapeKeyAdapter);
		okButton.addKeyListener(escapeKeyAdapter);
		cancelButton.addKeyListener(escapeKeyAdapter);

        // Selects OK when enter is pressed
        getRootPane().setDefaultButton(okButton);
        
		// Path field will receive initial focus
		setInitialFocusComponent(pathField);		
			
		setMinimumSize(MINIMUM_DIALOG_DIMENSION);
		setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
	}


	protected void setTextField(String text) {
		pathField.setText(text);
        // Text is selected so that user can directly type and replace path
        pathField.setSelectionStart(0);
        pathField.setSelectionEnd(text.length());
	}
	
	
	/**
	 * Displays an error message.
	 */
	protected void showErrorDialog(String msg, String title) {
		JOptionPane.showMessageDialog(mainFrame, msg, title, JOptionPane.ERROR_MESSAGE);

		// FileTable lost focus
		mainFrame.getLastActiveTable().requestFocus();
	}


	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		dispose();
		
		// OK Button
		if(source == okButton || source == pathField) {
			okPressed();
		}
	}
	
	/**
	 * This method is invoked when the OK button is pressed.
	 */
	protected abstract void okPressed();
	
}
