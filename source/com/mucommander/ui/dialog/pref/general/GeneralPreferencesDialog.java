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
import com.mucommander.ui.main.WindowManager;

import java.awt.*;

/**
 * This is the main preferences dialog that contains all preferences panels organized by tabs.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class GeneralPreferencesDialog extends PreferencesDialog {
    // - Singleton --------------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Used to ensure we only have the one preferences dialog open at any given time. */
    private static       GeneralPreferencesDialog singleton;
    /** Used to synchronize calls to the singleton. */
    private static final Object                   singletonLock = new Object();



    // - Dimensions -------------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Dialog's minimum dimensions. */
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(580,0);
    /** Dialog's maximum dimensions. */
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(640,480);



    // - Available tabs ---------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Identifier of the 'general' tab. */
    public static final int GENERAL_TAB    = 0;
    /** Identifier of the 'folders' tab. */
    public static final int FOLDERS_TAB    = 1;
    /** Identifier of the 'appearance' tab. */
    public static final int APPEARANCE_TAB = 2;
    /** Identifier of the 'mail' tab. */
    public static final int MAIL_TAB       = 3;
    /** Identifier of the 'misc' tab. */
    public static final int MISC_TAB       = 4;



    // - Tab icons --------------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Name of the icon used by the 'general' tab. */
    private final static String GENERAL_ICON    = "general.png";
    /** Name of the icon used by the 'folders' tab. */
    private final static String FOLDERS_ICON    = "folders.png";
    /** Name of the icon used by the 'appearance' tab. */
    private final static String APPEARANCE_ICON = "appearance.png";
    /** Name of the icon used by the 'mail' tab. */
    private final static String MAIL_ICON       = "mail.png";
    /** Name of the icon used by the 'misc' tab. */
    private final static String MISC_ICON       = "misc.png";



    // - Misc .fields -----------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Index of the tab that was last selected by the user */
    private static int lastTabIndex = 0;
    /** Whether or not the dialog should take tab selection events into account. */
    private boolean listenToChanges;



    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Returns an instance of <code>GeneralPreferencesDialog</code>.
     * <p>
     * This will not necessarily create a new instance - if a dialog is already in use, it
     * will be returned. This is an attempt to ensure that the preferences dialog is not opened
     * more than once.
     * </p>
     * @return an instance of <code>GeneralPreferencesDialog</code>.
     */
    public static GeneralPreferencesDialog getDialog() {
        synchronized(singletonLock) {
            if(singleton == null)
                singleton = new GeneralPreferencesDialog();
            return singleton;
        }
    }

    /**
     * Creates a new instance of the <code>GeneralPreferencesDialog</code>.
     */
    private GeneralPreferencesDialog() {
        super(WindowManager.getCurrentMainFrame(), Translator.get("prefs_dialog.title"));
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



    // - Misc. ------------------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Commits the changes and writes the configuration file if necessary.
     */
    public void commit() {
        super.commit();
        try {MuConfiguration.write();}
        // TODO: this should pop an error dialog.
        catch(Exception e) {}
    }

    /**
     * Listens to tab selection changes in order to store the last tab selected by the user.
     */
    public void tabSelectionChanged(int newIndex) {
        if(listenToChanges)
            lastTabIndex = newIndex;
    }

    /**
     * Releases the singleton.
     */
    public void dispose() {
        synchronized(singletonLock) {singleton = null;}
        super.dispose();
    }
}
