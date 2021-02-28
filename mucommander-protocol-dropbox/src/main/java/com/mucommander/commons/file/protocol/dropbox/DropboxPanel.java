package com.mucommander.commons.file.protocol.dropbox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.protocol.dropbox.authentication.PkceAuthorize;
import com.mucommander.protocol.ui.ServerPanel;
import com.mucommander.protocol.ui.ServerPanelListener;

public class DropboxPanel extends ServerPanel implements ActionListener {

	public static final String SCHEMA = "dropbox";

    private JTextField token;

    protected DropboxPanel(ServerPanelListener listener, JFrame mainFrame) {
        super(listener, mainFrame);

        JButton button = new JButton("arik");
        button.addActionListener(this);
        add(button);

        token = new JTextField();
        add(token);
    }

    private void updateValues() {
//        code = token.getText().trim();
    }

    @Override
    public FileURL getServerURL() throws MalformedURLException {
    	updateValues();
    	FileURL url = FileURL.getFileURL(String.format("%s://%s", SCHEMA, "arik"));
    	url.setProperty("token", token.getText());
    	return url;
    }

    @Override
    public boolean usesCredentials() {
        return false;
    }

    @Override
    public void dialogValidated() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	// Read app info file (contains app key and app secret)
    	DbxAppInfo appInfo = new DbxAppInfo("", "");

    	// Run through Dropbox API authorization process
    	DbxAuthFinish authFinish;
		try {
			authFinish = new PkceAuthorize().authorize(appInfo);
		} catch (IOException e1) {
			return;
		}

    	// Save auth information the new DbxCredential instance. It also contains app_key and
    	// app_secret which is required to do refresh call.
    	DbxCredential credential = new DbxCredential(authFinish.getAccessToken(), authFinish
    			.getExpiresAt(), authFinish.getRefreshToken(), appInfo.getKey(), appInfo.getSecret());
    	File output = new File("/tmp/arik.txt");
    	try {
    		DbxCredential.Writer.writeToFile(credential, output);
    		System.out.println("Saved authorization information to \"" + output.getCanonicalPath() + "\".");
    	} catch (IOException ex) {
    		System.err.println("Error saving to <auth-file-out>: " + ex.getMessage());
    		System.err.println("Dumping to stderr instead:");
    		try {
				DbxCredential.Writer.writeToStream(credential, System.err);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		System.exit(1); return;
    	}

    	// Create a DbxClientV2, which is what you use to make API calls.
    	DbxRequestConfig requestConfig = new DbxRequestConfig("examples-account-info");
    	// Use DbxCredential to create dbx client.
    	DbxClientV2 dbxClient = new DbxClientV2(requestConfig, credential);

//    	// Make the /account/info API call.
//    	FullAccount dbxAccountInfo;
//    	try {
//    		dbxAccountInfo = dbxClient.users()
//    				.getCurrentAccount();
//    	}
//    	catch (DbxException ex) {
//    		System.err.println("Error making API call: " + ex.getMessage());
//    		System.exit(1); return;
//    	}

    	/*ListFolderResult result;
    	try {
			result = dbxClient.files().listFolder("");
		} catch (DbxException e1) {
			e1.printStackTrace();
			return;
		}

    	for (Metadata metadata : result.getEntries()) {
    		System.out.println(metadata.toStringMultiline());
    	}*/
    }

}
