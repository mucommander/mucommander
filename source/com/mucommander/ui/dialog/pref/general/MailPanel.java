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

package com.mucommander.ui.dialog.pref.general;

import com.mucommander.conf.ConfigurationManager;
import com.mucommander.conf.impl.ConfigurationVariables;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.YBoxPanel;

import javax.swing.*;
import java.awt.*;


/**
 * 'Mail' preferences panel.
 *
 * @author Maxence Bernard
 */
class MailPanel extends PreferencesPanel {

    /** Name of the user */
    private JTextField nameField;
	
    /** Email address of the user */
    private JTextField emailField;
	
    /** SMTP used to send emails */
    private JTextField smtpField;
	
	
    public MailPanel(PreferencesDialog parent) {
        super(parent, Translator.get("prefs_dialog.mail_tab"));

        setLayout(new BorderLayout());

        YBoxPanel mainPanel = new YBoxPanel(5);
        mainPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.mail_settings")));

        // Text fields panel
        XAlignedComponentPanel compPanel = new XAlignedComponentPanel();

        // Name field
        nameField = new JTextField(ConfigurationManager.getVariable(ConfigurationVariables.MAIL_SENDER_NAME, ""));
        compPanel.addRow(Translator.get("prefs_dialog.mail_name"), nameField, 10);
		
        // Email field
        emailField = new JTextField(ConfigurationManager.getVariable(ConfigurationVariables.MAIL_SENDER_ADDRESS, ""));
        compPanel.addRow(Translator.get("prefs_dialog.mail_address"), emailField, 10);

        // SMTP field
        smtpField = new JTextField(ConfigurationManager.getVariable(ConfigurationVariables.SMTP_SERVER, ""));
        compPanel.addRow(Translator.get("prefs_dialog.mail_server"), smtpField, 10);

        mainPanel.add(compPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.NORTH);
    }


    ///////////////////////
    // PrefPanel methods //
    ///////////////////////
    protected void commit() {
        ConfigurationManager.setVariable(ConfigurationVariables.MAIL_SENDER_NAME, nameField.getText());
        ConfigurationManager.setVariable(ConfigurationVariables.MAIL_SENDER_ADDRESS, emailField.getText());
        ConfigurationManager.setVariable(ConfigurationVariables.SMTP_SERVER, smtpField.getText());
    }
}
