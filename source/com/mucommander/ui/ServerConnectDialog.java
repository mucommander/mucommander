
package com.mucommander.ui;

import com.mucommander.ui.comp.dialog.*;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.SMBFile;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ServerConnectDialog extends FocusDialog implements ActionListener, KeyListener {
	private MainFrame mainFrame;
	
	private JButton okButton;
	private JButton cancelButton;
	private JPanel serverDetailsPanel;

	private EscapeKeyAdapter escapeKeyAdapter;

	// Dialog's width has to be at least 320
	private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
	
	private JComboBox serverTypeCombo;

	/* SMB related fields */
	private JTextField smbServerField;
	private JTextField smbUsernameField;
	private JPasswordField smbPasswordField;
	private JLabel smbURLLabel;
	
	private static String lastSmbServer = "";
	private static String lastSmbUsername = "";
	private static String lastSmbPassword = "";
	
	public ServerConnectDialog(MainFrame mainFrame) {
		super(mainFrame, "Connect to Server", mainFrame);
		this.mainFrame = mainFrame;
		
		Container contentPane = getContentPane();
		escapeKeyAdapter = new EscapeKeyAdapter(this);
		
		YBoxPanel mainPanel = new YBoxPanel();
		JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tempPanel.add(new JLabel("Server type  "));
		serverTypeCombo = new JComboBox();
		serverTypeCombo.addItem("SMB");
		serverTypeCombo.addKeyListener(escapeKeyAdapter);
		tempPanel.add(serverTypeCombo);
		mainPanel.add(tempPanel);

		serverDetailsPanel = getSMBPanel();
		mainPanel.add(serverDetailsPanel);
		contentPane.add(mainPanel, BorderLayout.NORTH);
		
		okButton = new JButton("OK");
		cancelButton = new JButton("Cancel");
		// Escape key disposes dialog
		okButton.addKeyListener(escapeKeyAdapter);
		cancelButton.addKeyListener(escapeKeyAdapter);
		contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);

		// server field will receive initial focus
		setInitialFocusComponent(smbServerField);		
		
		// Selects OK when enter is pressed
		getRootPane().setDefaultButton(okButton);

		// Packs dialog
		setMinimumSize(MINIMUM_DIALOG_DIMENSION);
	}


	private JPanel getSMBPanel() {
		YBoxPanel smbPanel = new YBoxPanel(15);

        // Init grid bag layout and panel.
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;

        JPanel textFieldsPanel = new JPanel();
        textFieldsPanel.setLayout(gridbag);

		// Server field
		smbServerField = new JTextField(lastSmbServer);
		addTextFieldRow(textFieldsPanel, c, "Server", smbServerField, 10, 20);

		// Username field
		smbUsernameField = new JTextField(lastSmbUsername);
		addTextFieldRow(textFieldsPanel, c, "Username", smbUsernameField, 10, 5);

		// Password field
		smbPasswordField = new JPasswordField(lastSmbPassword);
		addTextFieldRow(textFieldsPanel, c, "Password", smbPasswordField, 10, 20);

		smbPanel.add(textFieldsPanel);
		
		XBoxPanel tempPanel = new XBoxPanel();
		tempPanel.add(new JLabel("Server URL:"));
		tempPanel.addSpace(10);
		smbURLLabel = new JLabel(getSmbURL());
		tempPanel.add(smbURLLabel, BorderLayout.CENTER);
		smbPanel.add(tempPanel);

		smbPanel.addSpace(10);

		return smbPanel;
	}

	
	private void addTextFieldRow(JPanel gridBagPanel, GridBagConstraints c, String labelText, JTextField textField, int xSpaceAfter, int ySpaceAfter) {
		c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
		c.fill = GridBagConstraints.NONE;      //reset to default
		c.weightx = 0.0;                       //reset to default
		c.insets = new Insets(0, 0, ySpaceAfter, xSpaceAfter);
		
		gridBagPanel.add(new JLabel(labelText), c);

		c.gridwidth = GridBagConstraints.REMAINDER;     //end row
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.insets = new Insets(0, 0, ySpaceAfter, 0);

		textField.addKeyListener(escapeKeyAdapter);
		textField.addKeyListener(this);
		textField.addActionListener(this);

		gridBagPanel.add(textField, c);
	}


	private String getSmbURL() {
		String server = smbServerField.getText().trim();
		return "smb://"+server;
	}


	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source==okButton || source==smbServerField || source==smbUsernameField || source==smbPasswordField)  {
			lastSmbServer = smbServerField.getText().trim();
			lastSmbUsername = smbUsernameField.getText().trim();
			lastSmbPassword = new String(smbPasswordField.getPassword());

			dispose();
		
			mainFrame.getLastActiveTable().getBrowser().setCurrentFolder(AbstractFile.getAbstractFile(SMBFile.getPrivateURL(getSmbURL(), lastSmbUsername, lastSmbPassword)), true);
		}
		else if (source==cancelButton)  {
			dispose();			
		}
	}


	/***********************
	 * KeyListener methods *
	 ***********************/

	public void keyPressed(KeyEvent e) {
		smbURLLabel.setText(getSmbURL());
//		okButton.setEnabled(!smbServerField.getText().trim().equals(""));
	}

	public void keyReleased(KeyEvent e) {
		smbURLLabel.setText(getSmbURL());
//		okButton.setEnabled(!smbServerField.getText().trim().equals(""));
	}

	public void keyTyped(KeyEvent e) {
		smbURLLabel.setText(getSmbURL());
//		okButton.setEnabled(!smbServerField.getText().trim().equals(""));
	}
}