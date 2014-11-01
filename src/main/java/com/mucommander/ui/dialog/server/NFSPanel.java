/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import com.mucommander.commons.file.FileProtocols;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.impl.nfs.NFSFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.net.MalformedURLException;
import java.text.ParseException;


/**
 * This ServerPanel helps initiate NFS connections.
 *
 * @author Maxence Bernard
 */
public class NFSPanel extends ServerPanel {

    private final static int STANDARD_PORT = FileURL.getRegisteredHandler(FileProtocols.NFS).getStandardPort(); 

    private JTextField serverField;
    private JTextField shareField;
    private JSpinner portSpinner;
    private JComboBox nfsVersionComboBox;
    private JComboBox nfsProtocolComboBox;

    private static String lastServer = "";
    private static String lastShare = "";
    private static int lastPort = STANDARD_PORT;
    private static String lastNfsVersion = NFSFile.DEFAULT_NFS_VERSION;
    private static String lastNfsProtocol = NFSFile.DEFAULT_NFS_PROTOCOL;

    NFSPanel(ServerConnectDialog dialog, MainFrame mainFrame) {
        super(dialog, mainFrame);

        // Server field, initialized to last value
        serverField = new JTextField(lastServer);
        serverField.selectAll();
        addTextFieldListeners(serverField, true);
        addRow(Translator.get("server_connect_dialog.server"), serverField, 5);

        // NFS share, initialized to ""
        shareField = new JTextField(lastShare);
        shareField.selectAll();
        addTextFieldListeners(shareField, true);
        addRow(Translator.get("server_connect_dialog.share"), shareField, 15);

        // Port field, initialized to last value (default is 2049)
        portSpinner = createPortSpinner(lastPort);
        addRow(Translator.get("server_connect_dialog.port"), portSpinner, 15);

        // NFS version, initialized to last value (default is NFSFile's default)
        nfsVersionComboBox = new JComboBox();
        nfsVersionComboBox.addItem(NFSFile.NFS_VERSION_2);
        nfsVersionComboBox.addItem(NFSFile.NFS_VERSION_3);
        nfsVersionComboBox.setSelectedItem(lastNfsVersion);
        addRow(Translator.get("server_connect_dialog.nfs_version"), nfsVersionComboBox, 5);

        // NFS protocol, initialized to last value (default is NFSFile's default)
        nfsProtocolComboBox = new JComboBox();
        nfsProtocolComboBox.addItem(NFSFile.NFS_PROTOCOL_AUTO);
        nfsProtocolComboBox.addItem(NFSFile.NFS_PROTOCOL_TCP);
        nfsProtocolComboBox.addItem(NFSFile.NFS_PROTOCOL_UDP);
        nfsProtocolComboBox.setSelectedItem(lastNfsProtocol);
        addRow(Translator.get("server_connect_dialog.protocol"), nfsProtocolComboBox, 15);
    }


    private void updateValues() {
        lastServer = serverField.getText();
        lastShare = shareField.getText();

        lastPort = (Integer) portSpinner.getValue();

        lastNfsVersion = (String)nfsVersionComboBox.getSelectedItem();
        lastNfsProtocol = (String)nfsProtocolComboBox.getSelectedItem();
    }


    ////////////////////////////////
    // ServerPanel implementation //
    ////////////////////////////////

    @Override
    FileURL getServerURL() throws MalformedURLException {
        updateValues();

        FileURL url = FileURL.getFileURL(FileProtocols.NFS+"://"+lastServer+(lastShare.startsWith("/")?"":"/")+lastShare);

        // Set port
        url.setPort(lastPort);

        // Set NFS version
        url.setProperty(NFSFile.NFS_VERSION_PROPERTY_NAME, lastNfsVersion);

        // Set NFS protocol
        url.setProperty(NFSFile.NFS_PROTOCOL_PROPERTY_NAME, lastNfsProtocol);

        return url;
    }

    @Override
    boolean usesCredentials() {
        return false;
    }

    @Override
    public void dialogValidated() {
        // Commits the current spinner value in case it was being edited and 'enter' was pressed
        // (the spinner value would otherwise not be committed)
        try { portSpinner.commitEdit(); }
        catch(ParseException e) { }

        updateValues();
    }
}
