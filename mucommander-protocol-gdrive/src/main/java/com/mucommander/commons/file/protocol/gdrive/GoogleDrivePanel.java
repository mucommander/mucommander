/**
 * This file is part of muCommander, http://www.mucommander.com
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
package com.mucommander.commons.file.protocol.gdrive;

import java.net.MalformedURLException;

import javax.swing.JFrame;
import javax.swing.JTextField;

import com.mucommander.commons.file.FileURL;
import com.mucommander.protocol.ui.ServerPanel;
import com.mucommander.protocol.ui.ServerPanelListener;
import com.mucommander.text.Translator;

/**
 * This ServerPanel helps initiate Google Drive connections.
 * 
 * @author Arik Hadas
 */
public class GoogleDrivePanel extends ServerPanel {

	private static final long serialVersionUID = -3850165192515539062L;
	public static final String SCHEMA = "gdrive";
	
	private JTextField accountField;

	GoogleDrivePanel(ServerPanelListener listener, JFrame mainFrame) {
		super(listener, mainFrame);

		// Server field
		accountField = new JTextField();
		addTextFieldListeners(accountField, true);
		addRow(Translator.get("server_connect_dialog.account_alias"), accountField, 5);
	}

	// //////////////////////////////
	// ServerPanel implementation //
	// //////////////////////////////

	@Override
	public FileURL getServerURL() throws MalformedURLException {
		FileURL url = FileURL.getFileURL(String.format("%s://%s", SCHEMA, accountField.getText()));
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
