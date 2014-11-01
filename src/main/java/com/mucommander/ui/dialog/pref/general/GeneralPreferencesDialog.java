/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import java.awt.Dimension;
import java.util.LinkedHashSet;
import java.util.Set;

import com.mucommander.conf.MuConfigurations;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.component.PrefComponent;
import com.mucommander.ui.main.WindowManager;

/**
 * This is the main preferences dialog that contains all preferences panels organized by tabs.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class GeneralPreferencesDialog extends PreferencesDialog {
    // - Singleton --------------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Used to ensure we only have the one preferences dialog open at any given time. */
    private static GeneralPreferencesDialog singleton;
    /** Stores the components in the dialog that were changed and their current value is different 
     *  then their saved value at MuConfiguration **/
    private Set<PrefComponent> modifiedComponents;


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
    /** Name of the icon used by the 'shortucts' tab. */
    private final static String SHORTCUTS_ICON  = "shortcuts.png";



    // - Misc .fields -----------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Index of the tab that was last selected by the user. */
    private static int      lastTabIndex = 0;



    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Creates a new instance of the <code>GeneralPreferencesDialog</code>.
     */
    private GeneralPreferencesDialog() {
        super(WindowManager.getCurrentMainFrame(), Translator.get("prefs_dialog.title"));
        modifiedComponents = new LinkedHashSet<PrefComponent>();

        // Adds the preference tabs.
        addPreferencesPanel(new GeneralPanel(this),    GENERAL_ICON);
        addPreferencesPanel(new FoldersPanel(this),    FOLDERS_ICON);
        addPreferencesPanel(new AppearancePanel(this), APPEARANCE_ICON);
        addPreferencesPanel(new ShortcutsPanel(this),  SHORTCUTS_ICON);
        addPreferencesPanel(new MailPanel(this),       MAIL_ICON);
        addPreferencesPanel(new MiscPanel(this),       MISC_ICON);

        // Sets the dialog's size.
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);

        // Restores the last selected index.
        setActiveTab(lastTabIndex);
    }



    // - Misc. ------------------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Commits the changes and writes the configuration file if necessary.
     */
    @Override
    public void commit() {
        super.commit();
        try {
            MuConfigurations.savePreferences();
        }
        catch(Exception e) {
            InformationDialog.showErrorDialog(this);
        }
    }

    /**
     * Releases the singleton.
     */
    @Override
    public void dispose() {
        releaseSingleton(getSelectedPanelIndex());
        super.dispose();
    }



    // - Singleton management ---------------------------------------------------
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
    public static synchronized GeneralPreferencesDialog getDialog() {
        // If no instance already exists, create a new one.
        if(singleton == null)
            singleton = new GeneralPreferencesDialog();

        return singleton;
    }

    /**
     * Releases the singleton.
     * <p>
     * After this method has been called, calls to {@link #getDialog()} will
     * result in creating a new instance of <code>GeneralPreferencesDialog</code>.
     * </p>
     * @param lastTab index of the last selected panel.
     */
    private static synchronized void releaseSingleton(int lastTab) {
        singleton    = null;
        lastTabIndex = lastTab;
    }

    @Override
    public void componentChanged(PrefComponent component) {
		if (component.hasChanged())
			modifiedComponents.add(component);
		else
			modifiedComponents.remove(component);
		
		setCommitButtonsEnabled(modifiedComponents.size() != 0);
	}
    
    @Override
    protected void setCommitButtonsEnabled(boolean enable) {
    	super.setCommitButtonsEnabled(enable);
    	// if "commit buttons" are disabled that's mean that there is no change in any component
    	// located in this dialog => we can clear the list of modified components in this dialog.
    	if (!enable)
    		modifiedComponents.clear();
    }
}
