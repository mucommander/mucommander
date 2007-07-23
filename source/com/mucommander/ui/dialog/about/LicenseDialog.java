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

package com.mucommander.ui.dialog.about;

import com.mucommander.Debug;
import com.mucommander.RuntimeConstants;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStreamReader;

/**
 * Dialog used to display muCommander's license file.
 * @author Nicolas Rinaudo
 */
public class LicenseDialog extends FocusDialog implements ActionListener {
    // - UI components ----------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Button used to close the dialog. */
    private JButton     okButton;
    /** Panel in which to display the license. */
    private JScrollPane licensePanel;



    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Creates a new license dialog centered on the specified window.
     * @param dialog window on which to center the new dialog.
     */
    public LicenseDialog(Dialog dialog) {
        super(dialog, Translator.get("license"), dialog);
        initUI();
    }

    /**
     * Creates a new license dialog centered on the specified window.
     * @param frame window on which to center the new dialog.
     */
    public LicenseDialog(Frame frame) {
        super(frame, Translator.get("license"), frame);
        initUI();
    }


    // - UI initialisation ------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Creates the 'ok' button panel.
     * @return the 'ok' button panel.
     */
    private JPanel createButtonPanel() {
        JPanel panel;

        panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        okButton = new JButton(Translator.get("ok"));
        okButton.addActionListener(this);
        panel.add(okButton);

        return panel;
    }

    /**
     * Creates the panel in which the license text is displayed.
     * @return the panel in which the license text is displayed.
     */
    private JScrollPane createLicensePanel() {
        JTextArea license;

        license = new JTextArea();
        license.setEditable(false);

        // Applies the file editor's theme to the license text.
        license.setForeground(ThemeManager.getCurrentColor(Theme.EDITOR_FOREGROUND_COLOR));
        license.setBackground(ThemeManager.getCurrentColor(Theme.EDITOR_BACKGROUND_COLOR));
        license.setSelectedTextColor(ThemeManager.getCurrentColor(Theme.EDITOR_SELECTED_FOREGROUND_COLOR));
        license.setSelectionColor(ThemeManager.getCurrentColor(Theme.EDITOR_SELECTED_BACKGROUND_COLOR));
        license.setFont(ThemeManager.getCurrentFont(Theme.EDITOR_FONT));

        license.setText(getLicenseText());

        // Sets the scroll policy and preferred dimensions.
        licensePanel = new JScrollPane(license, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        licensePanel.getViewport().setPreferredSize(new Dimension((int)license.getPreferredSize().getWidth(), 400));

        return licensePanel;
    }

    /**
     * Initialises the dialog's UI.
     */
    private void initUI() {
        Container   contentPane;

        contentPane = getContentPane();

        // Adds the UI components.
        contentPane.add(createLicensePanel(), BorderLayout.CENTER);
        contentPane.add(createButtonPanel(), BorderLayout.SOUTH);

        // Makes OK the default action.
        setInitialFocusComponent(okButton);
        getRootPane().setDefaultButton(okButton);

        // Makes sure the scroll pane is initialises on its first line.
        SwingUtilities.invokeLater(new Runnable() {public void run() {licensePanel.getViewport().setViewPosition(new Point(0,0));}});
        pack();
    }



    // - IO code ----------------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Loads the license text.
     * @return the license text.
     */
    private String getLicenseText() {
        StringBuffer      text;   // Stores the license text.
        char[]            buffer; // Buffer for each chunk of data read from the license file.
        int               count;  // Number of characters read from the last read operation.
        InputStreamReader in;     // Stream on the license file.

        in   = null;
        text = new StringBuffer();
        try {
            in     = new InputStreamReader(LicenseDialog.class.getResourceAsStream(RuntimeConstants.LICENSE));
            buffer = new char[2048];

            while((count = in.read(buffer)) != -1)
                text.append(buffer, 0, count);
        }
        catch(Exception e) {if(Debug.ON) Debug.trace("Failed to read /license.txt");}
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }
        }
        return text.toString();
    }



    // - Listener code ----------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * If the event originates from the OK button, closes the window.
     */
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == okButton)
            dispose();
    }
}
