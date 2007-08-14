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

import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.pref.PreferencesDialog;

import java.awt.*;


/**
 * This is the main preferences dialog that contains all preferences panels organized by tabs.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class GeneralPreferencesDialog extends PreferencesDialog {
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(580,0);
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(640,480);

    public static final int GENERAL_TAB    = 0;
    public static final int FOLDERS_TAB    = 1;
    public static final int APPEARANCE_TAB = 2;
    public static final int MAIL_TAB       = 3;
    public static final int MISC_TAB       = 4;

    // Tab icons
    private final static String GENERAL_ICON    = "general.png";
    private final static String FOLDERS_ICON    = "folders.png";
    private final static String THEMES_ICON     = "themes.png";
    private final static String APPEARANCE_ICON = "appearance.png";
    private final static String MAIL_ICON       = "mail.png";
    private final static String MISC_ICON       = "misc.png";

    /** Index of the tab that was last selected by the user */
    private static int lastTabIndex = 0;
    private boolean listenToChanges;

    public GeneralPreferencesDialog(Frame parent) {
        super(parent, Translator.get("prefs_dialog.title"));
        initUI();
    }

    public GeneralPreferencesDialog(Dialog parent) {
        super(parent, Translator.get("prefs_dialog.title"));
        initUI();
    }

    private void initUI() {
        listenToChanges = false;

        // Adds the preference tabs.
        addPreferencesPanel(new GeneralPanel(this),    GENERAL_ICON);
        addPreferencesPanel(new FoldersPanel(this),    FOLDERS_ICON);
        addPreferencesPanel(new AppearancePanel(this), APPEARANCE_ICON);
        addPreferencesPanel(new MailPanel(this),       MAIL_ICON);
        addPreferencesPanel(new MiscPanel(this),       MISC_ICON);

        // Sets the dialog's size.
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);

        // Restores the last selected index.
        setActiveTab(lastTabIndex);
        listenToChanges = true;
    }

    public void commit() {
        super.commit();
        try {MuConfiguration.write();}
        // We should probably pop an error dialog here.
        catch(Exception e) {}
    }

    public void tabSelectionChanged(int newIndex) {
        if(listenToChanges)
            lastTabIndex = newIndex;
    }
}
