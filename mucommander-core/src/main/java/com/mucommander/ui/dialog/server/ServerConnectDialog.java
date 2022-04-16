/*
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.ui.dialog.server;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.auth.CredentialsMapping;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.util.ui.dialog.DialogToolkit;
import com.mucommander.commons.util.ui.dialog.FocusDialog;
import com.mucommander.commons.util.ui.helper.FocusRequester;
import com.mucommander.commons.util.ui.layout.XBoxPanel;
import com.mucommander.commons.util.ui.layout.YBoxPanel;
import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.protocol.ui.ProtocolPanelProvider;
import com.mucommander.protocol.ui.ServerPanel;
import com.mucommander.protocol.ui.ServerPanelListener;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.ConnectToServerAction;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;


/**
 * Dialog that assists the user in connecting to a filesystem. It contains tabs and associated panels for each of the
 * supported protocols.
 *
 * @author Maxence Bernard
 */
public class ServerConnectDialog extends FocusDialog implements ServerPanelListener, ChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerConnectDialog.class);
    private FolderPanel folderPanel;
	
    private JButton cancelButton;
    private ServerPanel currentServerPanel;

    private JTabbedPane tabbedPane;
    private java.util.List<ServerPanel> serverPanels = new Vector<ServerPanel>();

    private JLabel urlLabel;
    private JCheckBox saveCredentialsCheckBox;

    // Dialog's width has to be at least 480
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(480,0);	
	
    private static Class<? extends ServerPanel> lastPanelClass;

    private static Map<String, ProtocolPanelProvider> schemaToPanelProvider = new HashMap<>();

    public static void register(ProtocolPanelProvider panelProvider) {
        schemaToPanelProvider.put(panelProvider.getSchema(), panelProvider);
    }

    public static void unregister(ProtocolPanelProvider panelProvider) {
        schemaToPanelProvider.remove(panelProvider.getSchema());
    }

    /**
     * Creates a new <code>ServerConnectDialog</code> that changes the current folder on the specified {@link FolderPanel}.
     *
     * @param folderPanel the panel on which to change the current folder
     */
    public ServerConnectDialog(FolderPanel folderPanel) {
        this(folderPanel, lastPanelClass);
    }
	
		
    /**
     * Creates a new <code>ServerConnectDialog</code> that changes the current folder on the specified {@link FolderPanel}.
     * The specified panel is selected when the dialog appears.
     *
     * @param folderPanel the panel on which to change the current folder
     * @param selectPanelClass class of the ServerPanel to select
     */
    public ServerConnectDialog(FolderPanel folderPanel, Class<? extends ServerPanel> selectPanelClass) {
        super(folderPanel.getMainFrame(), ActionProperties.getActionLabel(ConnectToServerAction.Descriptor.ACTION_ID), folderPanel.getMainFrame());
        this.folderPanel = folderPanel;
        lastPanelClass = selectPanelClass;

        Container contentPane = getContentPane();
		
        this.tabbedPane = new JTabbedPane(JTabbedPane.TOP);

        schemaToPanelProvider.values().stream().sorted(Comparator.comparing(ProtocolPanelProvider::priority)).forEach(this::addTab);

        currentServerPanel = getCurrentServerPanel();

        // Listen to tab change events
        tabbedPane.addChangeListener(this);
        contentPane.add(tabbedPane, BorderLayout.CENTER);
		
        YBoxPanel yPanel = new YBoxPanel();
        XBoxPanel xPanel = new XBoxPanel();
        xPanel.add(new JLabel(Translator.get("server_connect_dialog.server_url")+":"));
        xPanel.addSpace(5);
        urlLabel = new JLabel("");
        updateURLLabel();
        xPanel.add(urlLabel);
        yPanel.add(xPanel);

        yPanel.addSpace(10);

        this.saveCredentialsCheckBox = new JCheckBox(Translator.get("auth_dialog.store_credentials"), false);
        // Enables 'save credentials' checkbox only if server panel/protocol uses credentials
        saveCredentialsCheckBox.setEnabled(currentServerPanel.usesCredentials());
        yPanel.add(saveCredentialsCheckBox);

        JButton okButton = new JButton(Translator.get("server_connect_dialog.connect"));
        cancelButton = new JButton(Translator.get("cancel"));
        yPanel.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, getRootPane(), this));

        contentPane.add(yPanel, BorderLayout.SOUTH);
		
        // initial focus
        setInitialFocusComponent(currentServerPanel);		
		
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
    }

    private void addTab(ProtocolPanelProvider provider) {
        addTab(provider.getSchema(), provider.get(this, folderPanel.getMainFrame()));
    }

    private void addTab(String protocol, ServerPanel serverPanel) {
        if(!FileFactory.isRegisteredProtocol(protocol))
            return;

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(serverPanel, BorderLayout.NORTH);
        if (serverPanel.privacyPolicyApplicable())
            northPanel.add(createPrivacyPolicyPanel(), BorderLayout.SOUTH);
        tabbedPane.addTab(protocol.toUpperCase(), northPanel);

        if (serverPanel.getClass().equals(lastPanelClass))
            tabbedPane.setSelectedComponent(northPanel);

        serverPanels.add(serverPanel);
    }

    private JPanel createPrivacyPolicyPanel() {
        String privacyPolicyText = Translator.get("server_connect_dialog.privacy_policy");
        JLabel privacyPolicyLabel = new JLabel(privacyPolicyText);
        privacyPolicyLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        privacyPolicyLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                try {
                    DesktopManager.browse("https://www.mucommander.com/privacy");
                } catch (UnsupportedOperationException | IOException e) {
                    LOGGER.error("failed to browse privacy policy");
                }
            }
            @Override
            public void mouseEntered(MouseEvent event) {
                privacyPolicyLabel.setText("<html><a href=''>" + privacyPolicyText + "</a></html>");
            }
            @Override
            public void mouseExited(MouseEvent event) {
                privacyPolicyLabel.setText(privacyPolicyText);
            }
        });
        JPanel privacyPolicyPanel = new JPanel();
        privacyPolicyPanel.add(privacyPolicyLabel);
        return privacyPolicyPanel;
    }

    @Override
    public void browse(String url) {
        try {DesktopManager.browse(url);}
        catch(IOException e) {}
    }

    @Override
    public void updateURLLabel() {
        try {
            FileURL url = currentServerPanel.getServerURL();
            urlLabel.setText(url==null?" ":url.toString(false));
        }
        catch(MalformedURLException ex) {
            urlLabel.setText(" ");
        }
    }

    private ServerPanel getCurrentServerPanel() {
        return serverPanels.get(tabbedPane.getSelectedIndex());
    }
	
    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////
	
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source==cancelButton) {
            dispose();
            return;
        }

        try {
            currentServerPanel.dialogValidated();

            FileURL serverURL = currentServerPanel.getServerURL();	// Can throw a MalformedURLException

            // Create a CredentialsMapping instance and pass to Folder so that it uses it to connect to the folder and
            // adds to CredentialsManager once the folder has been successfully changed
            Credentials credentials = serverURL.getCredentials();
            CredentialsMapping credentialsMapping;
            if(credentials!=null) {
                credentialsMapping = new CredentialsMapping(credentials, serverURL, saveCredentialsCheckBox.isSelected());
            }
            else {
                credentialsMapping = null;
            }

            dispose();

            // Change the current folder
            folderPanel.tryChangeCurrentFolder(serverURL, credentialsMapping);
        }
        catch(IOException ex) {
            InformationDialog.showErrorDialog(this, Translator.get("table.folder_access_error_title"), Translator.get("folder_does_not_exist"));
        }
    }
	
	
    ///////////////////////////
    // ChangeListener method //
    ///////////////////////////
	
    public void stateChanged(ChangeEvent e) {
        currentServerPanel = getCurrentServerPanel();
        lastPanelClass = currentServerPanel.getClass();

        // Enables 'save credentials' checkbox only if server panel/protocol uses credentials
        saveCredentialsCheckBox.setEnabled(currentServerPanel.usesCredentials());

        updateURLLabel();
        FocusRequester.requestFocus(currentServerPanel);
    }
}
