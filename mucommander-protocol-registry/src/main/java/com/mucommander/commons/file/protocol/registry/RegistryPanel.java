/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2019
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.file.protocol.registry;

import java.awt.Color;
import java.net.MalformedURLException;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURL;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.server.ServerConnectDialog;
import com.mucommander.ui.dialog.server.ServerPanel;

/**
 * This ServerPanel helps initiate a connection to a registry image.
 *
 * @author Daniel Erez
 */
public class RegistryPanel extends ServerPanel {

	private JTextField serverField;
	private JTextField imageField;
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JComboBox<String> typeComboBox;

	RegistryPanel(ServerConnectDialog dialog, JFrame mainFrame, boolean isSkopeoAvailable) {
		super(dialog, mainFrame);

		serverField = new JTextField();
		addTextFieldListeners(serverField, true);
		addRow(Translator.get("server_connect_dialog.server"), serverField, 5);

		imageField = new JTextField();
		addTextFieldListeners(imageField, true);
		addRow(Translator.get("registry_connect_dialog.image"), imageField, 10);

		typeComboBox = new JComboBox<>();
		typeComboBox.addItem(AbstractRegistryImage.REGISTRY_PROTOCOL_DOCKER);
		typeComboBox.addItem(AbstractRegistryImage.REGISTRY_PROTOCOL_OCI);
		typeComboBox.addItem(AbstractRegistryImage.REGISTRY_PROTOCOL_DIR);
		addComboBoxListeners(typeComboBox);
		addRow(Translator.get("registry_connect_dialog.type"), typeComboBox, 20);

		// Credentials fields
		addRow(new JLabel(Translator.get("registry_connect_dialog.credentials")), 10);

		usernameField = new JTextField();
		addTextFieldListeners(usernameField, false);
		addRow(Translator.get("server_connect_dialog.username"), usernameField, 5);

		passwordField = new JPasswordField();
		addTextFieldListeners(passwordField, false);
		addRow(Translator.get("password"), passwordField, 50);

		if (!isSkopeoAvailable) {
			serverField.setEnabled(false);
			imageField.setEnabled(false);
			typeComboBox.setEnabled(false);
			usernameField.setEnabled(false);
			passwordField.setEnabled(false);
			JLabel warnLabel = new JLabel(Translator.get("registry_connect_dialog.noskopeo"));
			warnLabel.setForeground(Color.red);
			addRow(warnLabel, 10);
		}
	}

	@Override
	public FileURL getServerURL() throws MalformedURLException {
		FileURL url = FileURL.getFileURL(String.format("%s://%s/%s",
				typeComboBox.getSelectedItem(),
				serverField.getText(),
				imageField.getText()));
		url.setCredentials(new Credentials(usernameField.getText(),  new String(passwordField.getPassword())));
		return url;
	}

	@Override
	public boolean usesCredentials() {
		return false;
	}

	@Override
	public void dialogValidated() {
	}
}
