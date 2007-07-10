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
        YBoxPanel mainPanel = new YBoxPanel();

        FontChooser fontChooser = createFontChooser("theme_editor.font", ThemeData.FILE_TABLE_FONT);
        mainPanel.add(fontChooser);

        // Normal files
        mainPanel.add(createColorPanel(
                "theme_editor.plain_file", fontChooser,
                ThemeData.FILE_FOREGROUND_COLOR, ThemeData.FILE_BACKGROUND_COLOR,
                ThemeData.FILE_SELECTED_FOREGROUND_COLOR, ThemeData.FILE_SELECTED_BACKGROUND_COLOR,
                ThemeData.FILE_UNFOCUSED_FOREGROUND_COLOR, ThemeData.FILE_UNFOCUSED_BACKGROUND_COLOR,
                ThemeData.FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR, ThemeData.FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR
        ));

        // Folders
        mainPanel.add(createColorPanel(
                "theme_editor.folder", fontChooser,
                ThemeData.FOLDER_FOREGROUND_COLOR, ThemeData.FOLDER_BACKGROUND_COLOR,
                ThemeData.FOLDER_SELECTED_FOREGROUND_COLOR, ThemeData.FOLDER_SELECTED_BACKGROUND_COLOR,
                ThemeData.FOLDER_UNFOCUSED_FOREGROUND_COLOR, ThemeData.FOLDER_UNFOCUSED_BACKGROUND_COLOR,
                ThemeData.FOLDER_SELECTED_UNFOCUSED_FOREGROUND_COLOR, ThemeData.FOLDER_SELECTED_UNFOCUSED_BACKGROUND_COLOR
        ));

        // Archives
        mainPanel.add(createColorPanel(
                "theme_editor.archive_file", fontChooser,
                ThemeData.ARCHIVE_FOREGROUND_COLOR, ThemeData.ARCHIVE_BACKGROUND_COLOR,
                ThemeData.ARCHIVE_SELECTED_FOREGROUND_COLOR, ThemeData.ARCHIVE_SELECTED_BACKGROUND_COLOR,
                ThemeData.ARCHIVE_UNFOCUSED_FOREGROUND_COLOR, ThemeData.ARCHIVE_UNFOCUSED_BACKGROUND_COLOR,
                ThemeData.ARCHIVE_SELECTED_UNFOCUSED_FOREGROUND_COLOR, ThemeData.ARCHIVE_SELECTED_UNFOCUSED_BACKGROUND_COLOR
        ));

        // Hidden files
        mainPanel.add(createColorPanel(
                "theme_editor.hidden_file", fontChooser,
                ThemeData.HIDDEN_FILE_FOREGROUND_COLOR, ThemeData.HIDDEN_FILE_BACKGROUND_COLOR,
                ThemeData.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR, ThemeData.HIDDEN_FILE_SELECTED_BACKGROUND_COLOR,
                ThemeData.HIDDEN_FILE_UNFOCUSED_FOREGROUND_COLOR, ThemeData.HIDDEN_FILE_UNFOCUSED_BACKGROUND_COLOR,
                ThemeData.HIDDEN_FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR, ThemeData.HIDDEN_FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR
        ));

        // Symlinks
        mainPanel.add(createColorPanel(
                "theme_editor.symbolic_link", fontChooser,
                ThemeData.SYMLINK_FOREGROUND_COLOR, ThemeData.SYMLINK_BACKGROUND_COLOR,
                ThemeData.SYMLINK_SELECTED_FOREGROUND_COLOR, ThemeData.SYMLINK_SELECTED_BACKGROUND_COLOR,
                ThemeData.SYMLINK_UNFOCUSED_FOREGROUND_COLOR, ThemeData.SYMLINK_UNFOCUSED_BACKGROUND_COLOR,
                ThemeData.SYMLINK_SELECTED_UNFOCUSED_FOREGROUND_COLOR, ThemeData.SYMLINK_SELECTED_UNFOCUSED_BACKGROUND_COLOR
        ));

        // Marked files
        mainPanel.add(createColorPanel(
                "theme_editor.marked_file", fontChooser,
                ThemeData.MARKED_FOREGROUND_COLOR, ThemeData.MARKED_BACKGROUND_COLOR,
                ThemeData.MARKED_SELECTED_FOREGROUND_COLOR, ThemeData.MARKED_SELECTED_BACKGROUND_COLOR,
                ThemeData.MARKED_UNFOCUSED_FOREGROUND_COLOR, ThemeData.MARKED_UNFOCUSED_BACKGROUND_COLOR,
                ThemeData.MARKED_SELECTED_UNFOCUSED_FOREGROUND_COLOR, ThemeData.MARKED_SELECTED_UNFOCUSED_BACKGROUND_COLOR
        ));

        addVerticalSeparator(mainPanel);

        // File table
        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel gridPanel = new ProportionalGridPanel(2);

        PreviewLabel previewLabel = new PreviewLabel();

        gridPanel.add(createCaptionLabel("theme_editor.background"));
        gridPanel.add(new ColorButton(parent, template, ThemeData.FILE_TABLE_BACKGROUND_COLOR, PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME, previewLabel));

        gridPanel.add(createCaptionLabel("theme_editor.unfocused_background"));
        gridPanel.add(new ColorButton(parent, template, ThemeData.FILE_TABLE_UNFOCUSED_BACKGROUND_COLOR, PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME, previewLabel));

        gridPanel.add(createCaptionLabel("theme_editor.border"));
        gridPanel.add(new ColorButton(parent, template, ThemeData.FILE_TABLE_BORDER_COLOR, PreviewLabel.BORDER_COLOR_PROPERTY_NAME, previewLabel));

        flowPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("theme_editor.folder_tab")));
        flowPanel.add(gridPanel);

        mainPanel.add(flowPanel);

        // Quicksearch
        flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gridPanel = new ProportionalGridPanel(4);
        addLabelRow(gridPanel);

        addColorButtonRow(gridPanel, null, "theme_editor.quick_search.unmatched_file", ThemeData.FILE_TABLE_UNMATCHED_FOREGROUND_COLOR, ThemeData.FILE_TABLE_UNMATCHED_BACKGROUND_COLOR);

        flowPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("theme_editor.quick_search")));
        flowPanel.add(gridPanel);

        mainPanel.add(flowPanel);

        add(mainPanel);
    }


    // - Modification management ---------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public void commit() {}
}

