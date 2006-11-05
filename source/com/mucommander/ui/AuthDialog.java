
package com.mucommander.ui;


import com.mucommander.file.AuthException;
import com.mucommander.file.AuthInfo;
import com.mucommander.file.AuthManager;
import com.mucommander.file.FileURL;
import com.mucommander.text.Translator;
import com.mucommander.ui.comp.dialog.DialogToolkit;
import com.mucommander.ui.comp.dialog.FocusDialog;
import com.mucommander.ui.comp.dialog.TextFieldsPanel;
import com.mucommander.ui.comp.dialog.YBoxPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * This dialog asks the user to provide a login and password. It usually pops up when the user tries to access
 * a folder on a server for which he doesn't have read rights
 *
 * @author Maxence Bernard
 */
public class AuthDialog extends FocusDialog implements ActionListener {

    private JButton okButton;
	
    private JTextField loginField;
    private JPasswordField passwordField;
	
    private boolean dialogOked;
	
    private String publicURL;

    // Dialog size constraints
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(480,10000);	
	
	
    public AuthDialog(MainFrame mainFrame, AuthException authException) {
        super(mainFrame, Translator.get("auth_dialog.title"), mainFrame);
	
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        YBoxPanel yPanel = new YBoxPanel(5);
		
        yPanel.add(new JLabel(Translator.get("auth_dialog.desc")));
        yPanel.addSpace(20);
		
        FileURL fileURL = authException.getFileURL();
        this.publicURL = fileURL.getStringRep(false);

        TextFieldsPanel textFieldsPanel = new TextFieldsPanel(10); 

        // Server URL for which the user has to authentify
        JTextField serverField = new JTextField(publicURL);
        serverField.setEditable(false);
        textFieldsPanel.addTextFieldRow(Translator.get("auth_dialog.server"), serverField, 15);

        // Login field
        String login = fileURL.getLogin();
        this.loginField = new JTextField(login);
        textFieldsPanel.addTextFieldRow(Translator.get("login"), loginField, 10);
		
        // Password field
        this.passwordField = new JPasswordField();
        textFieldsPanel.addTextFieldRow(Translator.get("password"), passwordField, 10);
	
        //		contentPane.add(textFieldsPanel, BorderLayout.CENTER);
        yPanel.add(textFieldsPanel);

        String exceptionMsg = authException.getMessage();
        if(exceptionMsg!=null) {
            yPanel.addSpace(15);
            yPanel.add(new JLabel(Translator.get("auth_dialog.error_was", exceptionMsg)));
        }

        yPanel.addSpace(10);
        contentPane.add(yPanel, BorderLayout.NORTH);
		
		
        // OK / Cancel buttons
        this.okButton = new JButton(Translator.get("ok"));
        JButton cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);
		
        // initial focus
        setInitialFocusComponent(login==null||login.trim().equals("")?loginField:passwordField);
		
        // Selects OK when enter is pressed
        getRootPane().setDefaultButton(okButton);

        // Set minimum dimension
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);

        // Set minimum dimension
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
    }
	
	
    /**
     * Returns true if the user pressed OK (did not cancel the dialog) and presumably authentified.
     */
    public boolean okPressed() {
        return dialogOked;
    }
	

    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////

    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==okButton) {
            this.dialogOked = true;
            // Dialog OKed, add login/password pair to AuthManager, mapped with the server URL
            AuthManager.put(publicURL, new AuthInfo(loginField.getText(), new String(passwordField.getPassword())));
        }

        dispose();
    }
}
