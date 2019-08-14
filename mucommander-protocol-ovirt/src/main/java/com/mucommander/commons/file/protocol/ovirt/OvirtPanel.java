/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2016 Maxence Bernard
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

package com.mucommander.commons.file.protocol.ovirt;

import java.net.MalformedURLException;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURL;
import com.mucommander.protocol.ui.ServerPanel;
import com.mucommander.protocol.ui.ServerPanelListener;
import com.mucommander.text.Translator;

/**
 * This ServerPanel helps initiate oVirt connections.
 *
 * @author Arik Hadas
 */
public class OvirtPanel extends ServerPanel {

	private JTextField serverField;
	private JSpinner portSpinner;
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JCheckBox proxyCheckBox;

	private static boolean useProxy;
	private static int lastPort = OvirtConnHandler.STANDARD_PORT;

	protected OvirtPanel(ServerPanelListener listener, JFrame mainFrame) {
		super(listener, mainFrame);

		serverField = new JTextField();
		addTextFieldListeners(serverField, true);
		addRow(Translator.get("server_connect_dialog.server"), serverField, 5);

		portSpinner = createPortSpinner(lastPort);
		addRow(Translator.get("server_connect_dialog.port"), portSpinner, 15);

		usernameField = new JTextField();
		addTextFieldListeners(usernameField, false);
		addRow(Translator.get("server_connect_dialog.username"), usernameField, 5);

		passwordField = new JPasswordField();
		addTextFieldListeners(passwordField, false);
		addRow(Translator.get("password"), passwordField, 5);

		proxyCheckBox = new JCheckBox(Translator.get("server_connect_dialog.use_proxy"), useProxy);
		proxyCheckBox.addActionListener(e -> useProxy = proxyCheckBox.isSelected());
		addRow("", proxyCheckBox, 0);
	}

	@Override
	public FileURL getServerURL() throws MalformedURLException {
		FileURL url = FileURL.getFileURL(String.format("ovirt://%s:%s/",
				serverField.getText(),
				lastPort = (int) portSpinner.getValue()));
		url.setCredentials(new Credentials(usernameField.getText(),  new String(passwordField.getPassword())));
		url.setProperty("proxy", Boolean.toString(useProxy));
		return url;
	}

	@Override
	public boolean usesCredentials() {
		return true;
	}

	@Override
	public void dialogValidated() {
	}
}
