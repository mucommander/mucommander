/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

import com.mucommander.file.FileProtocols;
import com.mucommander.file.FileURL;
import com.mucommander.file.impl.nfs.NFSFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.net.MalformedURLException;


class NFSPanel extends ServerPanel {

    private JTextField serverField;
    private JTextField shareField;
    private JTextField portField;
    private JComboBox nfsVersionComboBox;
    private JComboBox nfsProtocolComboBox;

    private static String lastServer = "";
    private static String lastShare = "";
    private static int lastPort = 2049;
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
        portField = new JTextField(""+lastPort, 5);
        portField.selectAll();
        addTextFieldListeners(portField, true);
        addRow(Translator.get("server_connect_dialog.port"), portField, 15);

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

        lastPort = 2049;
        try {
            lastPort = Integer.parseInt(portField.getText());
        }
        catch(NumberFormatException e) {
            // Port is a malformed number
        }

        lastNfsVersion = (String)nfsVersionComboBox.getSelectedItem();
        lastNfsProtocol = (String)nfsProtocolComboBox.getSelectedItem();
    }


    ////////////////////////////////
    // ServerPanel implementation //
    ////////////////////////////////

    FileURL getServerURL() throws MalformedURLException {
        updateValues();

        FileURL url = new FileURL(FileProtocols.NFS+"://"+lastServer+(lastShare.startsWith("/")?"":"/")+lastShare);

        // Set port
        if(lastPort>0 && lastPort!=2049)
            url.setPort(lastPort);

        // Set NFS version
        url.setProperty(NFSFile.NFS_VERSION_PROPERTY_NAME, lastNfsVersion);

        // Set NFS protocol
        url.setProperty(NFSFile.NFS_PROTOCOL_PROPERTY_NAME, lastNfsProtocol);

        return url;
    }

    boolean usesCredentials() {
        return false;
    }

    public void dispose() {
        updateValues();
    }
}
