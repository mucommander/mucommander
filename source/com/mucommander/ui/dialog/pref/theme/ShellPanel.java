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
import com.mucommander.ui.chooser.FontChooser;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.theme.ThemeData;

/**
 * @author Nicolas Rinaudo, Maxence Bernard
 */
class ShellPanel extends ThemeEditorPanel {

    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new file table editor.
     * @param parent   dialog containing the panel.
     * @param template template being edited.
     */
    public ShellPanel(PreferencesDialog parent, ThemeData template) {
        super(parent, Translator.get("theme_editor.shell_tab"), template);
        initUI();
    }

    /**
     * Initialises the panel's UI.
     */
    private void initUI() {
        YBoxPanel mainPanel = new YBoxPanel();

        FontChooser fontChooser = createFontChooser("theme_editor.shell_font", ThemeData.SHELL_FONT);
        mainPanel.add(fontChooser);

        mainPanel.add(createColorPanel(
                "theme_editor.shell_colors", fontChooser,
                ThemeData.SHELL_FOREGROUND_COLOR, ThemeData.SHELL_BACKGROUND_COLOR,
                ThemeData.SHELL_SELECTED_FOREGROUND_COLOR, ThemeData.SHELL_SELECTED_BACKGROUND_COLOR
        ));

        addVerticalSeparator(mainPanel);

        fontChooser = createFontChooser("theme_editor.history_font", ThemeData.SHELL_HISTORY_FONT);
        mainPanel.add(fontChooser);

        mainPanel.add(createColorPanel(
                "theme_editor.history_colors", fontChooser,
                ThemeData.SHELL_HISTORY_FOREGROUND_COLOR, ThemeData.SHELL_HISTORY_BACKGROUND_COLOR,
                ThemeData.SHELL_HISTORY_SELECTED_FOREGROUND_COLOR, ThemeData.SHELL_HISTORY_SELECTED_BACKGROUND_COLOR
        ));

        add(mainPanel);
    }



    // - Modification management ---------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public void commit() {}
}
