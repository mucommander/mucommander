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

package com.mucommander.ui.dialog.pref.theme;

import com.mucommander.text.Translator;
import com.mucommander.ui.chooser.FontChooser;
import com.mucommander.ui.chooser.PreviewLabel;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.layout.ProportionalGridPanel;
import com.mucommander.ui.theme.ThemeData;

import javax.swing.*;
import java.awt.*;

/**
 * @author Nicolas Rinaudo
 */
class FilePanel extends ThemeEditorPanel {
    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new <code>FilePanel</code>.
     * @param parent   dialog containing the panel
     * @param isActive whether the color values should be taken from the <i>active</i> or <i>inactive</i> state.
     * @param data     theme to edit.
     * @param fontChooser  File table font chooser.
     */
    public FilePanel(PreferencesDialog parent, boolean isActive, ThemeData data, FontChooser fontChooser) {
        super(parent, Translator.get(isActive ? "theme_editor.active_panel" : "theme_editor.inactive_panel"), data);
        initUI(isActive, fontChooser);
    }



    // - UI initialisation ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    private void addForegroundColor(JPanel to, int colorId, ColorButton background, FontChooser fontChooser, FilePreviewPanel previewPanel) {
        PreviewLabel preview;
        ColorButton  button;

        preview = new PreviewLabel();
        preview.setTextPainted(true);
        background.addUpdatedPreviewComponent(preview);
        addFontChooserListener(fontChooser, preview);
        to.add(button = new ColorButton(parent, themeData, colorId, PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME, preview));
        button.addUpdatedPreviewComponent(previewPanel);
    }

    private void initUI(boolean isActive, FontChooser fontChooser) {
        JPanel           gridPanel;
        ColorButton      backgroundButton;
        ColorButton      selectedBackgroundButton;
        ColorButton      borderButton;
        FilePreviewPanel preview;


        gridPanel = new ProportionalGridPanel(3);
        preview   = new FilePreviewPanel(themeData, isActive);
        addFontChooserListener(fontChooser, preview);

        // Header
        gridPanel.add(new JLabel());
        gridPanel.add(createCaptionLabel("theme_editor.normal"));
        gridPanel.add(createCaptionLabel("theme_editor.selected"));

        // Background
        gridPanel.add(createCaptionLabel("theme_editor.background"));
        gridPanel.add(backgroundButton = new ColorButton(parent, themeData, isActive ? ThemeData.FILE_TABLE_BACKGROUND_COLOR :
                                                         ThemeData.FILE_TABLE_INACTIVE_BACKGROUND_COLOR,
                                                         PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME, preview));
        gridPanel.add(selectedBackgroundButton = new ColorButton(parent, themeData,
                                                                 isActive ? ThemeData.FILE_TABLE_SELECTED_BACKGROUND_COLOR :
                                                                 ThemeData.FILE_TABLE_INACTIVE_SELECTED_BACKGROUND_COLOR,
                                                                 PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME, preview));

        // Alternate background
        gridPanel.add(createCaptionLabel("theme_editor.alternate_background"));
        gridPanel.add(new ColorButton(parent, themeData,
                                      isActive ? ThemeData.FILE_TABLE_ALTERNATE_BACKGROUND_COLOR : ThemeData.FILE_TABLE_INACTIVE_ALTERNATE_BACKGROUND_COLOR,
                                      PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME, preview));
        gridPanel.add(new JLabel());

        // Folders.
        gridPanel.add(createCaptionLabel("theme_editor.folder"));
        addForegroundColor(gridPanel, isActive ? ThemeData.FOLDER_FOREGROUND_COLOR : ThemeData.FOLDER_INACTIVE_FOREGROUND_COLOR,
                           backgroundButton, fontChooser, preview);
        addForegroundColor(gridPanel, isActive ? ThemeData.FOLDER_SELECTED_FOREGROUND_COLOR : ThemeData.FOLDER_INACTIVE_SELECTED_FOREGROUND_COLOR,
                           selectedBackgroundButton, fontChooser, preview);

        // Plain files.
        gridPanel.add(createCaptionLabel("theme_editor.plain_file"));
        addForegroundColor(gridPanel, isActive ? ThemeData.FILE_FOREGROUND_COLOR : ThemeData.FILE_INACTIVE_FOREGROUND_COLOR,
                           backgroundButton, fontChooser, preview);
        addForegroundColor(gridPanel, isActive ? ThemeData.FILE_SELECTED_FOREGROUND_COLOR : ThemeData.FILE_INACTIVE_SELECTED_FOREGROUND_COLOR,
                           selectedBackgroundButton, fontChooser, preview);

        // Archives.
        gridPanel.add(createCaptionLabel("theme_editor.archive_file"));
        addForegroundColor(gridPanel, isActive ? ThemeData.ARCHIVE_FOREGROUND_COLOR : ThemeData.ARCHIVE_INACTIVE_FOREGROUND_COLOR,
                           backgroundButton, fontChooser, preview);
        addForegroundColor(gridPanel, isActive ? ThemeData.ARCHIVE_SELECTED_FOREGROUND_COLOR : ThemeData.ARCHIVE_INACTIVE_SELECTED_FOREGROUND_COLOR,
                           selectedBackgroundButton, fontChooser, preview);

        // Hidden files.
        gridPanel.add(createCaptionLabel("theme_editor.hidden_file"));
        addForegroundColor(gridPanel, isActive ? ThemeData.HIDDEN_FILE_FOREGROUND_COLOR : ThemeData.HIDDEN_FILE_INACTIVE_FOREGROUND_COLOR,
                           backgroundButton, fontChooser, preview);
        addForegroundColor(gridPanel, isActive ? ThemeData.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR : ThemeData.HIDDEN_FILE_INACTIVE_SELECTED_FOREGROUND_COLOR,
                           selectedBackgroundButton, fontChooser, preview);

        // Symlinks.
        gridPanel.add(createCaptionLabel("theme_editor.symbolic_link"));
        addForegroundColor(gridPanel, isActive ? ThemeData.SYMLINK_FOREGROUND_COLOR : ThemeData.SYMLINK_INACTIVE_FOREGROUND_COLOR,
                           backgroundButton, fontChooser, preview);
        addForegroundColor(gridPanel, isActive ? ThemeData.SYMLINK_SELECTED_FOREGROUND_COLOR : ThemeData.SYMLINK_INACTIVE_SELECTED_FOREGROUND_COLOR,
                           selectedBackgroundButton, fontChooser, preview);

        // Marked files.
        gridPanel.add(createCaptionLabel("theme_editor.marked_file"));
        addForegroundColor(gridPanel, isActive ? ThemeData.MARKED_FOREGROUND_COLOR : ThemeData.MARKED_INACTIVE_FOREGROUND_COLOR,
                           backgroundButton, fontChooser, preview);
        addForegroundColor(gridPanel, isActive ? ThemeData.MARKED_SELECTED_FOREGROUND_COLOR : ThemeData.MARKED_INACTIVE_SELECTED_FOREGROUND_COLOR,
                           selectedBackgroundButton, fontChooser, preview);

        // Border.
        gridPanel.add(createCaptionLabel("theme_editor.border"));
        gridPanel.add(borderButton = new ColorButton(parent, themeData, isActive ? ThemeData.FILE_TABLE_BORDER_COLOR :
                                                     ThemeData.FILE_TABLE_INACTIVE_BORDER_COLOR, PreviewLabel.BORDER_COLOR_PROPERTY_NAME));
        borderButton.addUpdatedPreviewComponent(preview);
        gridPanel.add(borderButton = new ColorButton(parent, themeData, isActive ? ThemeData.FILE_TABLE_SELECTED_OUTLINE_COLOR :
                                                     ThemeData.FILE_TABLE_INACTIVE_SELECTED_OUTLINE_COLOR, PreviewLabel.BORDER_COLOR_PROPERTY_NAME));
        borderButton.addUpdatedPreviewComponent(preview);

        setLayout(new BorderLayout());
        add(gridPanel, BorderLayout.WEST);
        add(preview, BorderLayout.EAST);
    }



    // - Misc. ---------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public void commit() {}
}
