
package com.mucommander.ui;

import com.mucommander.PlatformManager;
import com.mucommander.ui.comp.dialog.*;
import com.mucommander.ui.table.FileTable;
import com.mucommander.file.AbstractFile;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;



/**
 * Dialog used to execute a user-defined command.
 *
 * @author Maxence Bernard
 */
public class RunDialog extends FocusDialog implements ActionListener {
	private MainFrame mainFrame;
	
	private JTextField commandField;
	
	private JButton okButton;
	private JButton cancelButton;

	// Dialog size constrains
	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
    // Dialog width should not exceed 360, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(360,10000);	

	private static String lastCommand = "";
	
	/**
	 * Creates and displays a new RunDialog.
	 *
	 * @param mainFrame the main frame this dialog is attached to.
	 */
	public RunDialog(MainFrame mainFrame) {
		super(mainFrame, "Run", mainFrame);
		this.mainFrame = mainFrame;
		
		Container contentPane = getContentPane();
        JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JLabel label = new JLabel("Run in current folder:");
		label.setAlignmentX(LEFT_ALIGNMENT);
        mainPanel.add(label);

		commandField = new JTextField(lastCommand);
        // Text is selected so that user can directly type and replace path
        commandField.setSelectionStart(0);
        commandField.setSelectionEnd(lastCommand.length());
        commandField.addActionListener(this);
		
		commandField.setAlignmentX(LEFT_ALIGNMENT);
		mainPanel.add(commandField);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
        contentPane.add(mainPanel, BorderLayout.NORTH);
		
			// OK / Cancel buttons panel
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);

        // Escape key disposes dialog
		EscapeKeyAdapter escapeKeyAdapter = new EscapeKeyAdapter(this);
		commandField.addKeyListener(escapeKeyAdapter);
		okButton.addKeyListener(escapeKeyAdapter);
		cancelButton.addKeyListener(escapeKeyAdapter);

        // Selects OK when enter is pressed
        getRootPane().setDefaultButton(okButton);
        
		// Path field will receive initial focus
		setInitialFocusComponent(commandField);		
			
		setResizable(true);
		setMinimumSize(MINIMUM_DIALOG_DIMENSION);
		setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
		
		showDialog();
	}


	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		dispose();
		
		// OK Button
		if(source == okButton || source == commandField) {
			String command = commandField.getText();
			this.lastCommand = command;
			PlatformManager.execute(command, mainFrame.getLastActiveTable().getCurrentFolder());
		}
	}
	
	
}