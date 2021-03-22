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

package com.mucommander.ui.dialog.startup;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.mucommander.commons.util.ui.dialog.FocusDialog;
import com.mucommander.commons.util.ui.layout.YBoxPanel;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.text.Translator;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;

/**
 * Dialog box allowing users to select misc. setup options for muCommander.
 * @author Nicolas Rinaudo
 */
public class InitialSetupDialog extends FocusDialog implements ActionListener {
    // - Instance fields -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** All available look and feels. */
    private UIManager.LookAndFeelInfo lfInfo[];
    /** Used to select a startup theme. */
    private JComboBox<Theme> themeComboBox;
    /** Used to select a look and feel. */
    private JComboBox<String> lfComboBox;
    /** Used to validate the user's choice. */
    private JButton   okButton;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates the dialog's theme panel.
     * @return the dialog's theme panel.
     */
    private JPanel createThemePanel() {
        // Initialises the theme panel.
        JPanel themePanel = new YBoxPanel(); // Theme panel.
        themePanel.setAlignmentX(LEFT_ALIGNMENT);
        themePanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.themes")));

        // Adds the panel description.
        JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Temporary panel used to hold the dialog's description.
        tempPanel.add(new JLabel(Translator.get("setup.theme") + ':'));
        themePanel.add(tempPanel);

        // Adds the theme combo box.
        themeComboBox = new JComboBox<>();
        Iterator<Theme> themes = ThemeManager.availableThemes(); // All available themes.
        int index = 0; // Index of the currently analyzed theme.
        int selectedIndex = 0; // Index of the current theme in the combo box.

        // Adds all themes to the combo box.
        while (themes.hasNext()) {
            Theme theme = themes.next();
            themeComboBox.addItem(theme);
            if (ThemeManager.isCurrentTheme(theme))
                selectedIndex = index;
            index++;
        }
        // Selects the current theme.
        themeComboBox.setSelectedIndex(selectedIndex);
        themeComboBox.addActionListener(this);
        themePanel.add(themeComboBox);

        return themePanel;
    }

    /**
     * Creates the dialog's look and feel panel.
     * @return the dialog's look and feel panel.
     */
    private JPanel createLookAndFeelPanel() {
        // Initialises the theme panel.
        JPanel lfPanel = new YBoxPanel(); // Look and feel panel.
        lfPanel.setAlignmentX(LEFT_ALIGNMENT);
        lfPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.look_and_feel")));

        // Adds the panel description.
        JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));  // Temporary panel used to hold the dialog's description.
        tempPanel.add(new JLabel(Translator.get("setup.look_and_feel") + ':'));
        lfPanel.add(tempPanel);

        // Initializes the L&F combo box.
        lfComboBox = new JComboBox<>();
        lfInfo = UIManager.getInstalledLookAndFeels();
        String currentLf = UIManager.getLookAndFeel().getClass().getName(); // Name of the current look&feel.
        int selectedIndex = -1; // Index of the current look and feel in the list.
        // Goes through all available look&feels and selects the current one.
        for (int i = 0; i < lfInfo.length; i++) {
            String className = lfInfo[i].getClassName(); // Buffer for look&feel names.

            // Tries to select current L&F
            if (currentLf.equals(className))
                selectedIndex = i;
            // Under Mac OS X, Mac L&F is either reported as 'MacOS' or 'MacOS Adaptative'
            // so we need this test
            else if (selectedIndex == -1 && (currentLf.startsWith(className) || className.startsWith(currentLf)))
                selectedIndex = i;

            lfComboBox.addItem(lfInfo[i].getName());
        }

        // If no match, selects first one
        if (selectedIndex == -1)
            selectedIndex = 0;
        lfComboBox.setSelectedIndex(selectedIndex);
        lfComboBox.addActionListener(this);
        lfPanel.add(lfComboBox);

        return lfPanel;
    }

    /**
     * Creates the dialog's main panel.
     * @return the dialog's main panel.
     */
    private JPanel createMainPanel() {
        YBoxPanel mainPanel;
        JPanel    okPanel;

        mainPanel   = new YBoxPanel();
        mainPanel.add(new JLabel(Translator.get("setup.intro")));
        mainPanel.addSpace(10);
        mainPanel.add(createThemePanel());
        mainPanel.addSpace(10);
        mainPanel.add(createLookAndFeelPanel());
        mainPanel.addSpace(10);

        okPanel = new JPanel();
        okPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
        okPanel.add(okButton = new JButton(Translator.get("ok")));
        okButton.addActionListener(this);

        mainPanel.add(okPanel);

        return mainPanel;
    }

    /**
     * Creates a new InitialSetupDialog.
     * @param owner dialog's owner.
     */
    public InitialSetupDialog(Frame owner) {
        super(owner, Translator.get("setup.title"), owner);

        getContentPane().add(createMainPanel(), BorderLayout.CENTER);
        setResizable(false);
        setInitialFocusComponent(themeComboBox);
        setKeyboardDisposalEnabled(false);
        getRootPane().setDefaultButton(okButton);
    }


    // - ActionListener code -------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == themeComboBox)
            ThemeManager.setCurrentTheme((Theme)themeComboBox.getSelectedItem());
        else if(e.getSource() == lfComboBox)
            MuConfigurations.getPreferences().setVariable(MuPreference.LOOK_AND_FEEL, lfInfo[lfComboBox.getSelectedIndex()].getClassName());
        else if(e.getSource() == okButton) {
            ThemeManager.setCurrentTheme((Theme)themeComboBox.getSelectedItem());
            MuConfigurations.getPreferences().setVariable(MuPreference.LOOK_AND_FEEL, lfInfo[lfComboBox.getSelectedIndex()].getClassName());
            dispose();
        }
    }
}
