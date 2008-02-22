/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.extension.ClassFinder;
import com.mucommander.extension.ExtensionManager;
import com.mucommander.extension.LookAndFeelFilter;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.job.FileCollisionChecker;
import com.mucommander.runtime.OsFamilies;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.dialog.file.FileCollisionDialog;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;
import com.mucommander.ui.dialog.pref.theme.ThemeEditorDialog;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.icon.SpinningDial;
import com.mucommander.ui.layout.ProportionalGridPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

/**
 * 'Appearance' preferences panel.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
class AppearancePanel extends PreferencesPanel implements ActionListener, Runnable {
    // - Look and feel fields ------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Combo box containing the list of available look&feels. */
    private JComboBox                 lookAndFeelComboBox;
    /** All available look&feels. */
    private UIManager.LookAndFeelInfo lookAndFeels[];
    /** 'Use brushed metal look' checkbox */
    private JCheckBox                 brushedMetalCheckBox;
    /** Triggers look and feel importing. */
    private JButton                   importLookAndFeelButton;
    /** Triggers look and feel deletion. */
    private JButton                   deleteLookAndFeelButton;
    /** Used to notify the user that the system is working. */
    private SpinningDial              dial;
    /** File from which to import looks and feels. */
    private AbstractFile              lookAndFeelLibrary;



    // - Icon size fields ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Displays the list of available sizes for toolbar icons. */
    private JComboBox           toolbarIconsSizeComboBox;
    /** Displays the list of available sizes for command bar icons. */
    private JComboBox           commandBarIconsSizeComboBox;
    /** Displays the list of available sizes for file icons. */
    private JComboBox           fileIconsSizeComboBox;
    /** All icon sizes label. */
    private final static String ICON_SIZES[]                = {"100%", "125%", "150%", "175%", "200%", "300%"};
    /** All icon sizes scale factors. */
    private final static float  ICON_SCALE_FACTORS[]        = {1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 3.0f};



    // - Icons ---------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Icon used to identify 'locked' themes. */
    private ImageIcon lockIcon;
    /** Transparent icon used to align non-locked themes with the others. */
    private ImageIcon transparentIcon;



    // - Theme fields --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Lists all available themes. */
    private JComboBox    themeComboBox;
    /** Triggers the theme editor. */
    private JButton      editThemeButton;
    /** Triggers the theme duplication dialog. */
    private JButton      duplicateThemeButton;
    /** Triggers the theme import dialog. */
    private JButton      importThemeButton;
    /** Triggers the theme export dialog. */
    private JButton      exportThemeButton;
    /** Triggers the theme rename dialog. */
    private JButton      renameThemeButton;
    /** Triggers the theme delete dialog. */
    private JButton      deleteThemeButton;
    /** Used to display the currently selected theme's type. */
    private JLabel       typeLabel;
    /** Whether or not to ignore theme comobox related events. */
    private boolean      ignoreComboChanges;
    /** Last folder that was selected in import or export operations. */
    private AbstractFile lastSelectedFolder;



    // - Misc. fields --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** System icon combobox. */
    private              JComboBox useSystemFileIconsComboBox;
    /** Identifier of 'yes' actions in question dialogs. */
    private final static int       YES_ACTION = 0;
    /** Identifier of 'no' actions in question dialogs. */
    private final static int       NO_ACTION = 1;
    /** Identifier of 'cancel' actions in question dialogs. */
    private final static int       CANCEL_ACTION = 2;
    /** All known custom look and feels. */
    private              Vector    customLookAndFeels;



    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Creates a new appearance panel with the specified parent.
     * @param parent dialog in which this panel is placed.
     */
    public AppearancePanel(PreferencesDialog parent) {
        super(parent, Translator.get("prefs_dialog.appearance_tab"));
        initUI();

        // Initialises the known custom look and feels
        initialiseCustomLookAndFeels();
    }



    // - UI initialisation ------------------------------------------------------
    // --------------------------------------------------------------------------
    private void initUI() {
        YBoxPanel mainPanel;

        mainPanel = new YBoxPanel();

        // Look and feel.
        mainPanel.add(createLookAndFeelPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Themes.
        mainPanel.add(createThemesPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Icon size.
        mainPanel.add(createIconSizePanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // System icons.
        mainPanel.add(createSystemIconsPanel());
        mainPanel.add(Box.createVerticalGlue());

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.NORTH);
    }

    private void showGenericError() {JOptionPane.showMessageDialog(this, Translator.get("generic_error"), Translator.get("error"), JOptionPane.ERROR_MESSAGE);}

    /**
     * Populates the look&feel combo box will all available look&feels.
     */
    private void populateLookAndFeels() {
        int    currentIndex;
        String currentName;
        String buffer;

        // Resets the content of the combo box and retrieves all available look&feels.
        lookAndFeelComboBox.removeAllItems();
        initialiseAvailableLookAndFeels();

        // Populates the combo box.
        currentIndex = -1;
        currentName  = UIManager.getLookAndFeel().getName();
        for(int i = 0; i < lookAndFeels.length; i++) {
            buffer = lookAndFeels[i].getName();
			
            // Tries to select current L&F
            if(currentName.equals(buffer))
                currentIndex = i;

            // Under Mac OS X, Mac L&F is either reported as 'MacOS' or 'MacOS Adaptative'
            // so we need this test
            else if(currentIndex == -1
                    && (currentName.startsWith(buffer) || buffer.startsWith(currentName)))
                currentIndex = i;

            lookAndFeelComboBox.addItem(buffer);
        }

        // Sets the initial selection.
        if(currentIndex == -1)
            currentIndex = 0;
        lookAndFeelComboBox.setSelectedIndex(currentIndex);
    }

    /**
     * Creates the look and feel panel.
     * @return the look and feel panel.
     */
    private JPanel createLookAndFeelPanel() {
        JPanel lnfPanel;

        // Creates the panel.
        lnfPanel = new YBoxPanel();
        lnfPanel.setAlignmentX(LEFT_ALIGNMENT);
        lnfPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.look_and_feel")));

        // Creates the look and feel combo box.
        lookAndFeelComboBox = new JComboBox();
        lookAndFeelComboBox.setRenderer(new BasicComboBoxRenderer() {
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel label;

                    label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                    // Works around a strange JComboBox issue: index is often set to -1, which really doesn't make any sense.
                    if(index < 0) {
                        for(index = 0; index < lookAndFeels.length; index++)
                            if(lookAndFeels[index].getName().equals(value))
                                break;
                    }

                    // All look and feels that are not modifiable must be flagged with a lock icon.
                    if(isLookAndFeelModifiable(lookAndFeels[index]))
                        label.setIcon(transparentIcon);
                    else
                        label.setIcon(lockIcon);

                    return label;
                }
            });

        // Populates the look and feel combo box.
        populateLookAndFeels();

        // Initialises buttons and event listening.
        importLookAndFeelButton = new JButton(Translator.get("prefs_dialog.import") + "...");
        deleteLookAndFeelButton = new JButton(Translator.get("delete"));
        importLookAndFeelButton.addActionListener(this);
        deleteLookAndFeelButton.addActionListener(this);
        resetLookAndFeelButtons();
        lookAndFeelComboBox.addActionListener(this);

        // Adds the look and feel list and the action buttons to the panel.
        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flowPanel.add(lookAndFeelComboBox);
        flowPanel.add(importLookAndFeelButton);
        flowPanel.add(deleteLookAndFeelButton);
        flowPanel.add(new JLabel(dial = new SpinningDial()));
        lnfPanel.add(flowPanel);

        // For Mac OS X only, creates the 'brushed metal' checkbox.
        if(OsFamilies.MAC_OS_X.isCurrent()) {
            // 'Use brushed metal look' option
            brushedMetalCheckBox = new JCheckBox(Translator.get("prefs_dialog.use_brushed_metal"));
            brushedMetalCheckBox.setSelected(MuConfiguration.getVariable(MuConfiguration.USE_BRUSHED_METAL,
                                                                              MuConfiguration.DEFAULT_USE_BRUSHED_METAL));
            flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            flowPanel.add(brushedMetalCheckBox);
            lnfPanel.add(flowPanel);
        }

        return lnfPanel;
    }

    /**
     * Creates the icon size panel.
     * @return the icon size panel.
     */
    private JPanel createIconSizePanel() {
        ProportionalGridPanel gridPanel = new ProportionalGridPanel(2);

        gridPanel.add(new JLabel(Translator.get("prefs_dialog.toolbar_icons")));
        gridPanel.add(toolbarIconsSizeComboBox = createIconSizeCombo(MuConfiguration.TOOLBAR_ICON_SCALE, MuConfiguration.DEFAULT_TOOLBAR_ICON_SCALE));

        gridPanel.add(new JLabel(Translator.get("prefs_dialog.command_bar_icons")));
        gridPanel.add(commandBarIconsSizeComboBox = createIconSizeCombo(MuConfiguration.COMMAND_BAR_ICON_SCALE, MuConfiguration.DEFAULT_COMMAND_BAR_ICON_SCALE));

        gridPanel.add(new JLabel(Translator.get("prefs_dialog.file_icons")));
        gridPanel.add(fileIconsSizeComboBox = createIconSizeCombo(MuConfiguration.TABLE_ICON_SCALE, MuConfiguration.DEFAULT_TABLE_ICON_SCALE));

        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        flowPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.icons_size")));
        flowPanel.add(gridPanel);

        return flowPanel;
    }

    /**
     * Creates the themes panel.
     * @return the themes panel.
     */
    private JPanel createThemesPanel() {
        JPanel gridPanel = new ProportionalGridPanel(4);

        // Creates the various panel's buttons.
        editThemeButton      = new JButton(Translator.get("edit") + "...");
        importThemeButton    = new JButton(Translator.get("prefs_dialog.import") + "...");
        exportThemeButton    = new JButton(Translator.get("prefs_dialog.export") + "...");
        renameThemeButton    = new JButton(Translator.get("rename"));
        deleteThemeButton    = new JButton(Translator.get("delete"));
        duplicateThemeButton = new JButton(Translator.get("duplicate"));
        editThemeButton.addActionListener(this);
        importThemeButton.addActionListener(this);
        exportThemeButton.addActionListener(this);
        renameThemeButton.addActionListener(this);
        deleteThemeButton.addActionListener(this);
        duplicateThemeButton.addActionListener(this);

        // Creates the panel's 'type label'.
        typeLabel = new JLabel("");

        // Creates the theme combo box.
        themeComboBox   = new JComboBox();
        themeComboBox.addActionListener(this);

        // Sets the combobox's renderer.
        lockIcon        = IconManager.getIcon(IconManager.PREFERENCES_ICON_SET, "lock.png");
        transparentIcon = new ImageIcon(new BufferedImage(lockIcon.getIconWidth(), lockIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB));
        themeComboBox.setRenderer(new BasicComboBoxRenderer() {
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel label;
                    Theme  theme;

                    label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    theme = (Theme)value;

                    if(ThemeManager.isCurrentTheme(theme))
                        label.setText(theme.getName() +  " (" + Translator.get("theme.current") + ")");
                    else
                        label.setText(theme.getName());

                    if(theme.getType() != Theme.CUSTOM_THEME)
                        label.setIcon(lockIcon);
                    else
                        label.setIcon(transparentIcon);

                    return label;
                }
            });

        // Initialises the content of the combo box.
        populateThemes(ThemeManager.getCurrentTheme());

        gridPanel.add(themeComboBox);
        gridPanel.add(editThemeButton);
        gridPanel.add(importThemeButton);
        gridPanel.add(exportThemeButton);

        gridPanel.add(typeLabel);
        gridPanel.add(renameThemeButton);
        gridPanel.add(deleteThemeButton);
        gridPanel.add(duplicateThemeButton);

        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        flowPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.themes")));
        flowPanel.add(gridPanel);

        return flowPanel;
    }

    private void populateThemes(Theme currentTheme) {
        Iterator  themes;

        ignoreComboChanges = true;

        themeComboBox.removeAllItems();
        themes = ThemeManager.availableThemes();
        while(themes.hasNext())
            themeComboBox.addItem(themes.next());

        ignoreComboChanges = false;

        themeComboBox.setSelectedItem(currentTheme);
    }

    /**
     * Creates the system icons panel.
     * @return the system icons panel.
     */
    private JPanel createSystemIconsPanel() {
        /* 'Use system file icons' combo box */
        this.useSystemFileIconsComboBox = new JComboBox();
        useSystemFileIconsComboBox.addItem(Translator.get("prefs_dialog.use_system_file_icons.never"));
        useSystemFileIconsComboBox.addItem(Translator.get("prefs_dialog.use_system_file_icons.applications"));
        useSystemFileIconsComboBox.addItem(Translator.get("prefs_dialog.use_system_file_icons.always"));
        String systemIconsPolicy = FileIcons.getSystemIconsPolicy();
        useSystemFileIconsComboBox.setSelectedIndex(FileIcons.USE_SYSTEM_ICONS_ALWAYS.equals(systemIconsPolicy)?2:FileIcons.USE_SYSTEM_ICONS_APPLICATIONS.equals(systemIconsPolicy)?1:0);

        return createComboPanel("prefs_dialog.use_system_file_icons", useSystemFileIconsComboBox);
    }

    /**
     * Creates a combo box that allows to choose a size for a certain type of icon. The returned combo box is filled
     * with allowed choices, and the current configuration value is selected.
     *
     * @param confVar the name of the configuration variable that contains the icon scale factor
     * @param defaultValue the default value for the icon scale factor if the configuration variable has no value
     * @return a combo box that allows to choose a size for a certain type of icon
     */
    private JComboBox createIconSizeCombo(String confVar, float defaultValue) {
        JComboBox iconSizeCombo = new JComboBox();

        for(int i=0; i<ICON_SIZES.length; i++)
            iconSizeCombo.addItem(ICON_SIZES[i]);

        float scaleFactor = MuConfiguration.getVariable(confVar, defaultValue);
        int index = 0;
        for(int i=0; i<ICON_SCALE_FACTORS.length; i++) {
            if(scaleFactor==ICON_SCALE_FACTORS[i]) {
                index = i;
                break;
            }
        }
        iconSizeCombo.setSelectedIndex(index);

        return iconSizeCombo;
    }

    private JPanel createComboPanel(String labelKey, JComboBox comboBox) {
        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comboPanel.add(new JLabel(Translator.get(labelKey)+": "));
        comboPanel.add(comboBox);

        return comboPanel;
    }


    ///////////////////////
    // PrefPanel methods //
    ///////////////////////
    protected void commit() {
        // Look and Feel
        if(MuConfiguration.setVariable(MuConfiguration.LOOK_AND_FEEL, lookAndFeels[lookAndFeelComboBox.getSelectedIndex()].getClassName())) {
            resetLookAndFeelButtons();
            SwingUtilities.updateComponentTreeUI(parent);
        }

        if(OsFamilies.MAC_OS_X.isCurrent())
            MuConfiguration.setVariable(MuConfiguration.USE_BRUSHED_METAL,  brushedMetalCheckBox.isSelected());

        // Set ToolBar's icon size
        float scaleFactor = ICON_SCALE_FACTORS[toolbarIconsSizeComboBox.getSelectedIndex()];
        MuConfiguration.setVariable(MuConfiguration.TOOLBAR_ICON_SCALE, scaleFactor);

        // Set CommandBar's icon size
        scaleFactor = ICON_SCALE_FACTORS[commandBarIconsSizeComboBox.getSelectedIndex()];
        MuConfiguration.setVariable(MuConfiguration.COMMAND_BAR_ICON_SCALE , scaleFactor);

        // Set file icon size
        scaleFactor = ICON_SCALE_FACTORS[fileIconsSizeComboBox.getSelectedIndex()];
        // Set scale factor in FileIcons first so that it has the new value when ConfigurationListener instances call it
        FileIcons.setScaleFactor(scaleFactor);
        MuConfiguration.setVariable(MuConfiguration.TABLE_ICON_SCALE , scaleFactor);

        // Sets the current theme.
        if(!ThemeManager.isCurrentTheme((Theme)themeComboBox.getSelectedItem())) {
            ThemeManager.setCurrentTheme((Theme)themeComboBox.getSelectedItem());
            resetThemeButtons((Theme)themeComboBox.getSelectedItem());
            themeComboBox.repaint();
        }

        // Set system icons policy
        int comboIndex = useSystemFileIconsComboBox.getSelectedIndex();
        String systemIconsPolicy = comboIndex==0?FileIcons.USE_SYSTEM_ICONS_NEVER:comboIndex==1?FileIcons.USE_SYSTEM_ICONS_APPLICATIONS:FileIcons.USE_SYSTEM_ICONS_ALWAYS;
        FileIcons.setSystemIconsPolicy(systemIconsPolicy);
        MuConfiguration.setVariable(MuConfiguration.USE_SYSTEM_FILE_ICONS, systemIconsPolicy);
    }



    // - Look and feel actions --------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Initialises the list of custom look&feels.
     */
    private void initialiseCustomLookAndFeels() {
        customLookAndFeels = MuConfiguration.getListVariable(MuConfiguration.CUSTOM_LOOK_AND_FEELS, MuConfiguration.CUSTOM_LOOK_AND_FEELS_SEPARATOR);
    }

    /**
     * Initialises the list of available look&feels.
     */
    private void initialiseAvailableLookAndFeels() {
        // Loads all available look and feels.
        lookAndFeels = UIManager.getInstalledLookAndFeels();

        // Sorts them.
        Arrays.sort(lookAndFeels, new Comparator() {
                public int compare(Object a, Object b) {return ((UIManager.LookAndFeelInfo)a).getName().compareTo(((UIManager.LookAndFeelInfo)b).getName());}
                public boolean equals(Object a) {return false;}
            });
    }

    /**
     * Returns <code>true</code> if the specified class name is that of a custom look and feel.
     * @return <code>true</code> if the specified class name is that of a custom look and feel, <code>false</code> otherwise.
     */
    private boolean isCustomLookAndFeel(String className) {return customLookAndFeels == null ? false : customLookAndFeels.contains(className);}

    /**
     * Returns <code>true</code> if the specified look and feel is modifiable.
     * <p>
     * To be modifiable, a look and feel must meet all of the following conditions:
     * <ul>
     *   <li>It must be a custom look and feel.</li>
     *   <li>It cannot be the application's current look and feel.</li>
     * </ul>
     * </p>
     * @return <code>true</code> if the specified look and feel is modifiable, <code>false</code> otherwise.
     */
    private boolean isLookAndFeelModifiable(UIManager.LookAndFeelInfo laf) {
        if(isCustomLookAndFeel(laf.getClassName()))
            return !laf.getClassName().equals(UIManager.getLookAndFeel().getClass().getName());
        return false;
    }

    /**
     * Resets the enabled status of the various look and feel buttons depending on the current selection.
     */
    private void resetLookAndFeelButtons() {
        // If the dial is animated, we're currently loading look&feels and should ignore this call.
        if(dial == null || !dial.isAnimated())
            deleteLookAndFeelButton.setEnabled(isLookAndFeelModifiable(lookAndFeels[lookAndFeelComboBox.getSelectedIndex()]));
    }

    /**
     * Uninstalls the specified look and feel.
     * @param selection look and feel to uninstall.
     */
    private void uninstallLookAndFeel(UIManager.LookAndFeelInfo selection) {
        UIManager.LookAndFeelInfo[] buffer;      // New array of installed look and feels.
        int                         bufferIndex; // Current index in buffer.

        // Copies the content of lookAndFeels into buffer, skipping over the look and feel to uninstall.
        buffer      = new UIManager.LookAndFeelInfo[lookAndFeels.length - 1];
        bufferIndex = 0;
        for(int i = 0; i < lookAndFeels.length; i++) {
            if(!selection.getClassName().equals(lookAndFeels[i].getClassName())) {
                buffer[bufferIndex] = lookAndFeels[i];
                bufferIndex++;
            }
        }

        // Resets the list of installed look and feels.
        UIManager.setInstalledLookAndFeels(lookAndFeels = buffer);
    }

    /**
     * Deletes the specified look and feel from the list of custom look and feels.
     * @param selection currently selection look and feel.
     */
    private void deleteCustomLookAndFeel(UIManager.LookAndFeelInfo selection) {
        if(customLookAndFeels != null)
            if(customLookAndFeels.remove(selection.getClassName()))
                MuConfiguration.setVariable(MuConfiguration.CUSTOM_LOOK_AND_FEELS, customLookAndFeels, MuConfiguration.CUSTOM_LOOK_AND_FEELS_SEPARATOR);
    }

    /**
     * Deletes the currently selected look and feel.
     * <p>
     * After receiving user confirmation, this method will:
     * <ul>
     *   <li>Remove the look and feel from the combobox.</li>
     *   <li>Remove the look and feel from <code>UIManager</code>'s list of installed look and feels.</li>
     *   <li>Remove the look and feel from the list of custom look and feels.</li>
     * </ul>
     * </p>
     */
    private void deleteSelectedLookAndFeel() {
        UIManager.LookAndFeelInfo selection; // Currently selected look and feel.

        selection = lookAndFeels[lookAndFeelComboBox.getSelectedIndex()];

        // Asks the user whether he's sure he wants to delete the selected look and feel.
        if(new QuestionDialog(parent, null, Translator.get("prefs_dialog.delete_look_and_feel", selection.getName()), parent,
                              new String[] {Translator.get("yes"), Translator.get("no")},
                              new int[]  {YES_ACTION, NO_ACTION},
                              0).getActionValue() != YES_ACTION)
            return;

        // Removes the selected look and feel from the combo box.
        lookAndFeelComboBox.removeItem(selection.getName());

        // Removes the selected look and feel from the list of installed look and feels.
        uninstallLookAndFeel(selection);

        // Removes the selected look and feel from the list of custom look and feels.
        deleteCustomLookAndFeel(selection);
    }

    /**
     * Updates the different look&feel related UI widgets depending on whether they are busy or not.
     * @param loading whether look&feels are loading.
     */
    private void setLookAndFeelsLoading(boolean loading) {
        // Starts / stops the loading animation.
        dial.setAnimated(loading);

        // Disables / enables the import button and the combo box.
        importLookAndFeelButton.setEnabled(!loading);
        deleteLookAndFeelButton.setEnabled(!loading);
        lookAndFeelComboBox.setEnabled(!loading);

        // A special case must be made for the delete button
        // as it might not need to be re-enabled.
        if(loading)
            deleteLookAndFeelButton.setEnabled(false);
        else
            resetLookAndFeelButtons();
    }

    /**
     * Tries to import the specified library in the extensions folder.
     * <p>
     * If there is already a file with the same name in the extensions folder,
     * this method will ask the user for confirmation before overwriting it.
     * </p>
     * @param  library     library to import in the extensions folder.
     * @return             <code>true</code> if the library was imported, <code>false</code> if the user cancelled the operation.
     * @throws IOException if an I/O error occurred while importing the library
     */
    private boolean importLookAndFeelLibrary(AbstractFile library) throws IOException {
        // Tries to import the file, but if a version of it is already present in the extensions folder,
        // asks the user for confirmation.

        AbstractFile destFile = ExtensionManager.getExtensionsFile(library.getName());

        int collision = FileCollisionChecker.checkForCollision(library, destFile);
        if(collision!=FileCollisionChecker.NO_COLLOSION) {
            // Do not offer the multiple files mode options such as 'skip' and 'apply to all'
            int action = new FileCollisionDialog(parent, parent, collision, library, destFile, false, false).getActionValue();

            // User chose to overwrite the file
            if(action==FileCollisionDialog.OVERWRITE_ACTION) {
                // Simply continue and file will be overwritten
            }
            else if(action==FileCollisionDialog.OVERWRITE_IF_OLDER_ACTION) {
                // Overwrite if the source is more recent than the destination
                if(library.getDate()<=destFile.getDate())
                    return false;
                // Simply continue and file will be overwritten
            }
            // User chose to cancel or closed the dialog
            else {
                return false;
            }
        }

        return ExtensionManager.importLibrary(library, true);
    }

    public void run() {
        Vector newLookAndFeels;

        setLookAndFeelsLoading(true);
        try {
            // Identifies all the look&feels contained by the new library and adds them to the list of custom
            // If no look&feel was found, notifies the user.
            if((newLookAndFeels = new ClassFinder().find(lookAndFeelLibrary, new LookAndFeelFilter())).isEmpty())
                JOptionPane.showMessageDialog(this, Translator.get("prefs_dialog.no_look_and_feel"), Translator.get("warning"), JOptionPane.WARNING_MESSAGE);
            else if(importLookAndFeelLibrary(lookAndFeelLibrary)) {
                String currentName;

                if(customLookAndFeels == null)
                    customLookAndFeels = new Vector();

                // Adds all new instances to the list of custom look&feels.
                for(int i = 0; i < newLookAndFeels.size(); i++) {
                    currentName = (String)newLookAndFeels.elementAt(i);
                    if(!customLookAndFeels.contains(currentName)) {
                        customLookAndFeels.add(currentName);
                        try {WindowManager.installLookAndFeel(currentName);}
                        catch(Throwable e) {}
                    }
                }

                if(customLookAndFeels.isEmpty())
                    customLookAndFeels = null;
                else
                    MuConfiguration.setVariable(MuConfiguration.CUSTOM_LOOK_AND_FEELS, customLookAndFeels, MuConfiguration.CUSTOM_LOOK_AND_FEELS_SEPARATOR);

                populateLookAndFeels();
            }
        }
        catch(Exception e) {showGenericError();}
        setLookAndFeelsLoading(false);
    }

    private void importLookAndFeel() {
        JFileChooser chooser; // Used to select the theme to import.
        AbstractFile file;    // Path to the theme to import.

        // Initialises the file chooser.
        chooser = createFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter(new ExtensionFileFilter("jar", Translator.get("prefs_dialog.jar_file")));
        chooser.setDialogTitle(Translator.get("prefs_dialog.import_look_and_feel"));
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);

        if(chooser.showDialog(parent, Translator.get("prefs_dialog.import")) == JFileChooser.APPROVE_OPTION) {
            file               = FileFactory.getFile(chooser.getSelectedFile().getAbsolutePath());
            lastSelectedFolder = file.getParentSilently();

            // Makes sure the file actually exists - JFileChooser apparently doesn't enforce that properly in all look&feels.
            if(!file.exists()) {
                JOptionPane.showMessageDialog(this, Translator.get("this_file_does_not_exist", file.getName()), Translator.get("error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Imports the JAR in a separate thread.
            lookAndFeelLibrary = file;
            new Thread(this).start();
        }
    }



    // - Theme actions ----------------------------------------------------------
    // --------------------------------------------------------------------------
    private void setTypeLabel(Theme theme) {
        String label;

        if(theme.getType() == Theme.USER_THEME)
            label = Translator.get("theme.custom");
        else if(theme.getType() == Theme.PREDEFINED_THEME)
            label = Translator.get("theme.built_in");
        else
            label = Translator.get("theme.add_on");

        typeLabel.setText(Translator.get("prefs_dialog.theme_type", label));
    }

    private void resetThemeButtons(Theme theme) {
        if(ignoreComboChanges)
            return;

        setTypeLabel(theme);

        if(theme.getType() != Theme.CUSTOM_THEME) {
            renameThemeButton.setEnabled(false);
            deleteThemeButton.setEnabled(false);
        }
        else {
            renameThemeButton.setEnabled(true);
            if(ThemeManager.isCurrentTheme(theme))
                deleteThemeButton.setEnabled(false);
            else 
                deleteThemeButton.setEnabled(true);
        }
    }

    /**
     * Renames the specified theme.
     * @param theme theme to rename.
     */
    private void renameTheme(Theme theme) {
        ThemeNameDialog dialog;

        if((dialog = new ThemeNameDialog(parent, theme.getName())).wasValidated()) {
            // If the rename operation was a success, makes sure the theme is located at its proper position.
            try {
                if(ThemeManager.renameCustomTheme(theme, dialog.getText())) {
                    themeComboBox.removeItem(theme);
                    insertTheme(theme);
                    return;
                }
            }
            catch(Exception e) {}
            // Otherwise, notifies the user.
            JOptionPane.showMessageDialog(this, Translator.get("prefs_dialog.rename_failed", theme.getName()), Translator.get("error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Deletes the specified theme.
     * @param theme theme to delete.
     */
    private void deleteTheme(Theme theme) {
        // Asks the user whether he's sure he wants to delete the selected theme.
        if(new QuestionDialog(parent, null, Translator.get("prefs_dialog.delete_theme", theme.getName()), parent,
                              new String[] {Translator.get("yes"), Translator.get("no")},
                              new int[]  {YES_ACTION, NO_ACTION},
                              0).getActionValue() != YES_ACTION)
            return;

        // Deletes the selected theme and removes it from the list.
        try {
            ThemeManager.deleteCustomTheme(theme.getName());
            themeComboBox.removeItem(theme);
        }
        catch(Exception e) {showGenericError();}
    }

    /**
     * Starts the theme editor on the specified theme.
     * @param theme to edit.
     */
    private void editTheme(Theme theme) {
        // If the edited theme was modified, we must re-populate the list.
        if(new ThemeEditorDialog(parent, theme).editTheme())
            populateThemes(ThemeManager.getCurrentTheme());
    }

    /**
     * Creates a file chooser initialised on the last selected folder.
     */
    private JFileChooser createFileChooser() {
        if(lastSelectedFolder == null)
            return new JFileChooser();
        return new JFileChooser((java.io.File)lastSelectedFolder.getUnderlyingFileObject());
    }

    private void insertTheme(Theme theme) {
        int count;
        int i;

        count = themeComboBox.getItemCount();
        for(i = 0; i < count; i++) {
            if(((Theme)themeComboBox.getItemAt(i)).getName().compareTo(theme.getName()) >= 0) {
                themeComboBox.insertItemAt(theme, i);
                break;
            }
        }
        if(i == count)
            themeComboBox.addItem(theme);
        themeComboBox.setSelectedItem(theme);
    }

    /**
     * Imports a new theme in muCommander.
     */
    private void importTheme() {
        JFileChooser chooser; // Used to select the theme to import.
        AbstractFile         file;    // Path to the theme to import.

        // Initialises the file chooser.
        chooser = createFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter(new ExtensionFileFilter("xml", Translator.get("prefs_dialog.xml_file")));
        chooser.setDialogTitle(Translator.get("prefs_dialog.import_theme"));
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);

        if(chooser.showDialog(parent, Translator.get("prefs_dialog.import")) == JFileChooser.APPROVE_OPTION) {
            // Makes sure the file actually exists - JFileChooser apparently doesn't enforce that properly in all look&feels.
            file               = FileFactory.getFile(chooser.getSelectedFile().getAbsolutePath());
            lastSelectedFolder = file.getParentSilently();
            if(!file.exists()) {
                JOptionPane.showMessageDialog(this, Translator.get("this_file_does_not_exist", file.getName()), Translator.get("error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Imports the theme and makes sure it appears in the combobox.
            try {insertTheme(ThemeManager.importTheme((java.io.File)file.getUnderlyingFileObject()));}
            // Notifies the user that something went wrong.
            catch(Exception ex) {
                JOptionPane.showMessageDialog(this, Translator.get("prefs_dialog.error_in_import", file.getName()),
                                              Translator.get("error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Exports the specified theme.
     * @param theme theme to export.
     */
    private void exportTheme(Theme theme) {
        JFileChooser chooser;
        AbstractFile         file;

        chooser = createFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter(new ExtensionFileFilter("xml", Translator.get("prefs_dialog.xml_file")));

        chooser.setDialogTitle(Translator.get("prefs_dialog.export_theme", theme.getName()));
        if(chooser.showDialog(parent, Translator.get("prefs_dialog.export")) == JFileChooser.APPROVE_OPTION) {

            file               = FileFactory.getFile(chooser.getSelectedFile().getAbsolutePath());
            lastSelectedFolder = file.getParentSilently();

            // Makes sure the file's extension is .xml.
            try {
                if(!"xml".equalsIgnoreCase(file.getExtension()))    // Note: getExtension() may return null if no extension
                    file = lastSelectedFolder.getDirectChild(file.getName()+".xml");

                int collision = FileCollisionChecker.checkForCollision(null, file);
                if(collision!=FileCollisionChecker.NO_COLLOSION) {
                    // Do not offer the multiple files mode options such as 'skip' and 'apply to all'
                    int action = new FileCollisionDialog(parent, parent, collision, null, file, false, false).getActionValue();

                    // User chose to overwrite the file
                    if(action==FileCollisionDialog.OVERWRITE_ACTION) {
                        // Simply continue and file will be overwritten
                    }
                    // User chose to cancel or closed the dialog
                    else {
                        return;
                    }
                }

                // Exports the theme.
                ThemeManager.exportTheme(theme, (java.io.File)file.getUnderlyingFileObject());

                // If it was exported to the custom themes folder, reload the theme combobox to reflect the
                // changes.
                if(lastSelectedFolder.equals(ThemeManager.getCustomThemesFolder()))
                    populateThemes(theme);
            }
            // Notifies users of errors.
            catch(Exception exception) {
                JOptionPane.showMessageDialog(this, Translator.get("cannot_write_file", file.getName()), Translator.get("write_error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Duplicates the specified theme.
     */
    private void duplicateTheme(Theme theme) {
        try {insertTheme(ThemeManager.duplicateTheme(theme));}
        catch(Exception e) {showGenericError();}
    }




    // - Listener code ----------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Called when a button is pressed.
     */
    public void actionPerformed(ActionEvent e) {
        Theme theme;

        theme = (Theme)themeComboBox.getSelectedItem();

        // Theme combobox selection changed.
        if(e.getSource() == themeComboBox)
            resetThemeButtons(theme);

        // Look and feel combobox selection changed.
        else if(e.getSource() == lookAndFeelComboBox)
            resetLookAndFeelButtons();

        // Delete look and feel button has been pressed.
        else if(e.getSource() == deleteLookAndFeelButton)
            deleteSelectedLookAndFeel();

        // Import look and feel button has been pressed.
        else if(e.getSource() == importLookAndFeelButton)
            importLookAndFeel();

        // Rename button was pressed.
        else if(e.getSource() == renameThemeButton)
            renameTheme(theme);

        // Delete button was pressed.
        else if(e.getSource() == deleteThemeButton)
            deleteTheme(theme);

        // Edit button was pressed.
        else if(e.getSource() == editThemeButton)
            editTheme(theme);

        // Import button was pressed.
        else if(e.getSource() == importThemeButton)
            importTheme();

        // Export button was pressed.
        else if(e.getSource() == exportThemeButton)
            exportTheme(theme);

        // Export button was pressed.
        else if(e.getSource() == duplicateThemeButton)
            duplicateTheme(theme);
    }



    // - File filter ------------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Filter used to only display XML files in the JFileChooser.
     * @author Nicolas Rinaudo
     */
    private static class ExtensionFileFilter extends javax.swing.filechooser.FileFilter {
        /** Extension to match. */
        private String extension;
        /** Filter's description. */
        private String description;

        /**
         * Creates a new extension file filter that will match files with the specified extension.
         * @param extension extension to match.
         */
        public ExtensionFileFilter(String extension, String description) {
            this.extension   = extension;
            this.description = description;
        }

        /**
         * Returns <code>true</code> if the specified file should be displayed in the chooser.
         */
        public boolean accept(java.io.File file) {
            String ext;

            // Directories are always displayed.
            if(file.isDirectory())
                return true;

            // If the file has an extension, and it matches .xml, return true.
            // Otherwise, return false.
            if((ext = AbstractFile.getExtension(file.getName())) != null)
                return extension.equalsIgnoreCase(ext);
            return false;
        }

        public String getDescription() {return description;}
    }
}
