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
import com.mucommander.ui.chooser.PreviewLabel;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.layout.ProportionalGridPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.theme.ThemeData;

import javax.swing.*;
import java.awt.*;

/**
 * @author Nicolas Rinaudo, Maxence Bernard
 */
class FolderPanePanel extends ThemeEditorPanel {

    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new file table editor.
     * @param parent dialog containing the panel.
     * @param template template being edited.
     */
    public FolderPanePanel(PreferencesDialog parent, ThemeData template) {
        super(parent, Translator.get("theme_editor.folder_tab"), template);
        initUI();
    }

    /**
     * Initialises the panel's UI.
     */
    private void initUI() {
        JTabbedPane tabbedPane;
        FontChooser fontChooser;
        FilePanel   filePanel;


        tabbedPane = new JTabbedPane(JTabbedPane.TOP);

        // Adds the general panel.
        tabbedPane.add(Translator.get("theme_editor.general_panel"),
                       createScrollPane(createGeneralPanel(fontChooser = createFontChooser("theme_editor.font", ThemeData.FILE_TABLE_FONT))));

        // Adds the active panel.
        filePanel = new FilePanel(parent, true, template, fontChooser);
        tabbedPane.add(filePanel.getTitle(), createScrollPane(filePanel));

        // Adds the inactive panel.
        filePanel = new FilePanel(parent, false, template, fontChooser);
        tabbedPane.add(filePanel.getTitle(), createScrollPane(filePanel));

        // Creates the layout.
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
    }



    // - Helper methods ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates the 'general' theme.
     */
    private JPanel createGeneralPanel(FontChooser chooser) {
        YBoxPanel generalPanel;
        JPanel    gridPanel;
        JPanel    panel;

        // Initialises the panel.
        generalPanel = new YBoxPanel();

        // Initialises the quicksearch panel.
        gridPanel = new ProportionalGridPanel(4);
        addLabelRow(gridPanel);
        addColorButtonRow(gridPanel, null, "theme_editor.quick_search.unmatched_file",
                          ThemeData.FILE_TABLE_UNMATCHED_FOREGROUND_COLOR,
                          ThemeData.FILE_TABLE_UNMATCHED_BACKGROUND_COLOR);
        gridPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("theme_editor.quick_search")));

        // Creates the layout.
        generalPanel.add(chooser);
        generalPanel.add(gridPanel);

        // Wraps the whole thing in a flow layout.
        panel = new JPanel();
        panel.add(generalPanel);
        return panel;
    }

    /**
     * Wraps the specified panel within a scroll pane.
     */
    private JComponent createScrollPane(JPanel panel) {
        JScrollPane scrollPane;

        scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);

        return scrollPane;
    }



    // - Modification management ---------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public void commit() {}
}

