package com.mucommander.commons.file.protocol.dropbox;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.protocol.dropbox.authentication.PkceAuthorize;
import com.mucommander.commons.file.util.ResourceLoader;
import com.mucommander.protocol.ui.ServerPanel;
import com.mucommander.protocol.ui.ServerPanelListener;
import com.mucommander.text.Translator;

public class DropboxPanel extends ServerPanel implements ActionListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(DropboxPanel.class);

	public static final String SCHEMA = "dropbox";
	private static final String DROPBOX_ICON_PATH = "/images/file/dropbox.png";
	// Read app info file (contains app key and app secret)
	private static DbxAppInfo appInfo = new DbxAppInfo("", "");

	private DbxCredential credential;

	private JButton signingIn;
	private JTextField token;
	private LoginPhase loginPhase;
	private ImageIcon dropboxIcon;
	private JButton loadButton;
	private JLabel tokenLabel;
	private JLabel loadLabel;
	private JLabel nameLabel;
	private JLabel name;
	private JLabel accountAliasLabel;
	private JTextField accountAlias;

	enum LoginPhase {
		SIGN_IN,
		CANCEL_SIGN_IN,
	}

	protected DropboxPanel(ServerPanelListener listener, JFrame mainFrame) {
		super(listener, mainFrame);

		URL resourceURL = ResourceLoader.getResourceAsURL(DROPBOX_ICON_PATH);
		dropboxIcon = new ImageIcon(resourceURL);
		signingIn = new JButton();
		signingIn.addActionListener(this);
		addRow(wrapWithJPanel(signingIn), 5);

		token = new JTextField();
		tokenLabel = new JLabel(Translator.get("server_connect_dialog.dropbox.code"));
		addRow(tokenLabel, token, 5);

		loadButton = new JButton(Translator.get("server_connect_dialog.dropbox.load_account"));
		loadLabel = new JLabel();
		addRow(loadLabel, loadButton, 20);

		name = new JLabel();
		nameLabel = new JLabel(Translator.get("server_connect_dialog.dropbox.username"));
		addRow(nameLabel, name, 5);

		accountAlias = new JTextField();
		addTextFieldListeners(accountAlias, true);
		accountAliasLabel = new JLabel(Translator.get("server_connect_dialog.dropbox.account"));
		addRow(accountAliasLabel, accountAlias, 5);

		setAccountFieldsVisible(null);
		setAuthorizationFieldsVisible(null);
		setLoginPhase(LoginPhase.SIGN_IN);
	}

	private static JPanel wrapWithJPanel(JComponent component) {
		JPanel panel = new JPanel();
		panel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(component);
		return panel;
	}

	@Override
	public FileURL getServerURL() throws MalformedURLException {
		FileURL url = FileURL.getFileURL(String.format("%s://%s", SCHEMA, accountAlias.getText()));
		url.setCredentials(new Credentials(accountAlias.getText(), token.getText()));
		return url;
	}

	@Override
	public boolean usesCredentials() {
		return false;
	}

	@Override
	public void dialogValidated() {
		File output;
		try {
			output = new File(DropboxConnectionHandler.getCredentialFileURL(accountAlias.getText()).getPath());
		} catch (IOException e) {
			return;
		}
		try {
			DbxCredential.Writer.writeToFile(credential, output);
		} catch (IOException e) {
			LOGGER.error("failed to persist credentials to dropbox", e);
			return;
		}
	}

	private void setLoginPhase(LoginPhase loginPhase) {
		switch(loginPhase) {
		case CANCEL_SIGN_IN:
			signingIn.setText(Translator.get("cancel"));
			signingIn.setIcon(null);
			break;
		case SIGN_IN:
			signingIn.setText(Translator.get("server_connect_dialog.dropbox.sign_in"));
			signingIn.setIcon(dropboxIcon);
		}
		this.loginPhase = loginPhase;
	}

	private void setAccountFieldsVisible(FullAccount dbxAccountInfo) {
		if (dbxAccountInfo != null) {
			name.setText(dbxAccountInfo.getName().getDisplayName());
			accountAlias.setText(dbxAccountInfo.getName().getAbbreviatedName());
		}
		name.setVisible(dbxAccountInfo != null);
		nameLabel.setVisible(dbxAccountInfo != null);
		accountAlias.setVisible(dbxAccountInfo != null);
		accountAliasLabel.setVisible(dbxAccountInfo != null);
	}

	private void setAuthorizationFieldsVisible(PkceAuthorize pkceAuthorize) {
		if (pkceAuthorize != null) {
			loadButton.addActionListener(event -> {
				setLoginPhase(LoginPhase.SIGN_IN);

				DbxAuthFinish authFinish = null;
				try {
					authFinish = pkceAuthorize.authorize(token.getText());
				} catch (IOException e) {
					LOGGER.error("failed to authorize dropbox", e);
					return;
				}

				credential = new DbxCredential(authFinish.getAccessToken(), authFinish
						.getExpiresAt(), authFinish.getRefreshToken(), appInfo.getKey(), appInfo.getSecret());

				// Create a DbxClientV2, which is what you use to make API calls.
				DbxRequestConfig requestConfig = new DbxRequestConfig("mucommander-authorization");
				// Use DbxCredential to create dbx client.
				DbxClientV2 dbxClient = new DbxClientV2(requestConfig, credential);

				FullAccount dbxAccountInfo;
				try {
					dbxAccountInfo = dbxClient.users().getCurrentAccount();
				}
				catch (DbxException ex) {
					LOGGER.error("failed to get account info from dropbox", ex);
					return;
				}

				setAccountFieldsVisible(dbxAccountInfo);
			});
		} else {
			Arrays.stream(loadButton.getActionListeners()).forEach(loadButton::removeActionListener);
		}
		token.setVisible(pkceAuthorize != null);
		tokenLabel.setVisible(pkceAuthorize != null);
		loadButton.setVisible(pkceAuthorize != null);
		loadLabel.setVisible(pkceAuthorize != null);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch(loginPhase) {
		case SIGN_IN:
			setLoginPhase(LoginPhase.CANCEL_SIGN_IN);
			token.setText("");
			SwingUtilities.invokeLater(() -> {
				new Thread(() ->  {
					PkceAuthorize pkceAuthorize = new PkceAuthorize(appInfo);
					String authUrl = pkceAuthorize.getAuthorizationUrl();
					listener.browse(authUrl);
					setAuthorizationFieldsVisible(pkceAuthorize);
					setAccountFieldsVisible(null);
				}).start();
			});
			break;
		case CANCEL_SIGN_IN:
			setLoginPhase(LoginPhase.SIGN_IN);
			setAuthorizationFieldsVisible(null);
			setAccountFieldsVisible(null);
		}
	}
}
