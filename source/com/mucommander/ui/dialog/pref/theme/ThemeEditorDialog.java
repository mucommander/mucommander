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

package com.mucommander.ui.dialog.pref.theme;

import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.theme.Theme;
import com.mucommander.ui.theme.ThemeData;
import com.mucommander.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * Main dialog for the theme editor.
 * @author Nicolas Rinaudo
 */
public class ThemeEditorDialog extends PreferencesDialog {
    // - Action listening -------------------------------------------------------
    // --------------------------------------------------------------------------
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(580,0);
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(620,480);

    private ThemeData data;
    private Theme     theme;
    private boolean   wasThemeModified;

    public ThemeEditorDialog(Dialog parent, Theme theme) {
        super(parent, createTitle(theme));
        initUI(theme);
    }

    public ThemeEditorDialog(Frame parent, Theme theme) {
        super(parent, createTitle(theme));
        initUI(theme);
    }

    private static String createTitle(Theme theme) {return Translator.get("theme_editor.title") + ": " + theme.getName();}

    private void initUI(Theme theme) {
        this.theme       = theme;
        data             = theme.cloneData();
        wasThemeModified = false;

        addPreferencesPanel(new FolderPanePanel(this, data), false);
        addPreferencesPanel(new LocationBarPanel(this, data));
        addPreferencesPanel(new StatusBarPanel(this, data));
        addPreferencesPanel(new ShellPanel(this, data));
        addPreferencesPanel(new FileEditorPanel(this, data));

        // Sets the dialog's size.
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
    }

    public boolean editTheme() {
        showDialog();
        return wasThemeModified;
    }

    public boolean checkCommit() {
        super.checkCommit();

        // If the theme has been modified and is not the user theme, asks the user to confirm
        // whether it's ok to overwrite his user theme.
        if(!theme.isIdentical(data) && !theme.canModify())
            if(new QuestionDialog(this, Translator.get("warning"), Translator.get("theme_editor.theme_warning"),
                                  this, new String[]{Translator.get("yes"), Translator.get("no")}, new int[]{0,1}, 0).getActionValue() != 0)
                return false;
        return true;
    }

    public void commit() {
        super.commit();

        if(!theme.isIdentical(data)) {
            wasThemeModified = true;

            try {
                // If the theme cannot be modified, overwrites the user theme with the new data.
                if(!theme.canModify()) {
                    boolean updateCurrentTheme;

                    updateCurrentTheme = ThemeManager.isCurrentTheme(theme);

                    // Overwrites the user theme and changes the dialog's title to reflect the change.
                    theme = ThemeManager.overwriteUserTheme(data);
                    setTitle(createTitle(theme));

                    // If the old theme was the current one, switch to 'user theme'.
                    if(updateCurrentTheme)
                        ThemeManager.setCurrentTheme(theme);
                }

                // Otherwise, imports the new data in the user theme and saves it.
                else {
                    theme.importData(data);
                    ThemeManager.writeTheme(theme);
                }
            }
            catch(Exception exception) {
                JOptionPane.showMessageDialog(this, Translator.get("cannot_write_file", ThemeManager.getUserThemeFile().getAbsolutePath()),
                                              Translator.get("write_error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
