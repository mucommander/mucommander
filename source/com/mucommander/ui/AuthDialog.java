
package com.mucommander.ui;


import com.mucommander.ui.MainFrame;
import com.mucommander.ui.comp.dialog.*;

import com.mucommander.text.Translator;

import com.mucommander.file.FileURL;
import com.mucommander.file.AuthException;
import com.mucommander.file.AuthManager;
import com.mucommander.file.AuthInfo;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;


public class AuthDialog extends FocusDialog implements ActionListener {

	private JButton okButton;
	
	private JTextField loginField;
	private JPasswordField passwordField;
	
	private boolean dialogOked;
	
	private String publicURL;

	
	public AuthDialog(MainFrame mainFrame, AuthException authException, String text) {
		super(mainFrame, Translator.get("server_connect_dialog.server_connect"), mainFrame);
	
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		
		contentPane.add(new JLabel(Translator.get("auth_dialog.desc")), BorderLayout.NORTH);

		FileURL fileURL = authException.getFileURL();
		this.publicURL = fileURL.getURL(false);

		TextFieldsPanel textFieldsPanel = new TextFieldsPanel(5); 

		textFieldsPanel.addTextFieldRow(Translator.get("auth_dialog.server"), new JLabel(publicURL), 15);

		this.loginField = new JTextField(fileURL.getLogin());
		textFieldsPanel.addTextFieldRow(Translator.get("auth_dialog.login"), loginField, 10);
		
		this.passwordField = new JPasswordField();
		textFieldsPanel.addTextFieldRow(Translator.get("auth_dialog.password"), passwordField, 10);
	
		contentPane.add(textFieldsPanel, BorderLayout.CENTER);

		this.okButton = new JButton(Translator.get("ok"));
		JButton cancelButton = new JButton(Translator.get("cancel"));
		contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);
		
		// initial focus
		setInitialFocusComponent(textFieldsPanel);
		
		// Selects OK when enter is pressed
		getRootPane().setDefaultButton(okButton);

//		// Packs dialog
//		setMinimumSize(MINIMUM_DIALOG_DIMENSION);
	}
	
	
	public boolean okPressed() {
		return dialogOked;
	}
	

	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==okButton) {
			this.dialogOked = true;
			AuthManager.put(publicURL, new AuthInfo(loginField.getText(), new String(passwordField.getPassword())));
		}

		dispose();
	}
}
