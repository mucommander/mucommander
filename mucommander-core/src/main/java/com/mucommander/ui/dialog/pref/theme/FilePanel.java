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

package com.mucommander.ui.dialog.pref.theme;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mucommander.commons.util.ui.layout.ProportionalGridPanel;
import com.mucommander.text.Translator;
import com.mucommander.ui.chooser.FontChooser;
import com.mucommander.ui.chooser.PreviewLabel;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.theme.ThemeData;

/**
 * @author Nicolas Rinaudo
 */
class FilePanel extends ThemeEditorPanel {

    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new <code>FilePanel</code>.
     * @param parent   dialog containing the panel
     * @param active whether the color values should be taken from the <i>active</i> or <i>inactive</i> state.
     * @param data     theme to edit.
     * @param fontChooser  File table font chooser.
     */
    public FilePanel(PreferencesDialog parent, boolean active, ThemeData data, FontChooser fontChooser) {
        super(parent, Translator.get(active ? "theme_editor.active_panel" : "theme_editor.inactive_panel"), data);
        initUI(active, fontChooser);
    }



    // - UI initialisation ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    private void addForegroundColor(JPanel to, int colorId, ColorButton background, FontChooser fontChooser, FilePreviewPanel previewPanel) {
        PreviewLabel preview = new PreviewLabel();
        preview.setTextPainted(true);
        background.addUpdatedPreviewComponent(preview);
        addFontChooserListener(fontChooser, preview);
        ColorButton button = new ColorButton(parent, themeData, colorId, PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME, preview);
        to.add(button);
        button.addUpdatedPreviewComponent(previewPanel);
    }

    private void initUI(boolean active, FontChooser fontChooser) {
        JPanel gridPanel = new ProportionalGridPanel(3);
        FilePreviewPanel preview = new FilePreviewPanel(themeData, active);
        addFontChooserListener(fontChooser, preview);

        // Header
        gridPanel.add(new JLabel());
        gridPanel.add(createCaptionLabel("theme_editor.normal"));
        gridPanel.add(createCaptionLabel("theme_editor.selected"));

        // Background
        gridPanel.add(createCaptionLabel("theme_editor.background"));
        ColorButton backgroundButton = new ColorButton(parent, themeData,
                active ? ThemeData.FILE_TABLE_BACKGROUND_COLOR :
                    ThemeData.FILE_TABLE_INACTIVE_BACKGROUND_COLOR,
                    PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME, preview);
        gridPanel.add(backgroundButton);
        ColorButton selectedBackgroundButton = new ColorButton(parent, themeData,
                active ? ThemeData.FILE_TABLE_SELECTED_BACKGROUND_COLOR :
                    ThemeData.FILE_TABLE_INACTIVE_SELECTED_BACKGROUND_COLOR,
                    PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME, preview);
        gridPanel.add(selectedBackgroundButton);

        // Alternate background
        gridPanel.add(createCaptionLabel("theme_editor.alternate_background"));
        gridPanel.add(new ColorButton(parent, themeData,
                                      active ? ThemeData.FILE_TABLE_ALTERNATE_BACKGROUND_COLOR : ThemeData.FILE_TABLE_INACTIVE_ALTERNATE_BACKGROUND_COLOR,
                                      PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME, preview));
        gridPanel.add(new JLabel());

        // Folders.
        gridPanel.add(createCaptionLabel("theme_editor.folder"));
        addForegroundColor(gridPanel, active ? ThemeData.FOLDER_FOREGROUND_COLOR : ThemeData.FOLDER_INACTIVE_FOREGROUND_COLOR,
                           backgroundButton, fontChooser, preview);
        addForegroundColor(gridPanel, active ? ThemeData.FOLDER_SELECTED_FOREGROUND_COLOR : ThemeData.FOLDER_INACTIVE_SELECTED_FOREGROUND_COLOR,
                           selectedBackgroundButton, fontChooser, preview);

        // Plain files.
        gridPanel.add(createCaptionLabel("theme_editor.plain_file"));
        addForegroundColor(gridPanel, active ? ThemeData.FILE_FOREGROUND_COLOR : ThemeData.FILE_INACTIVE_FOREGROUND_COLOR,
                           backgroundButton, fontChooser, preview);
        addForegroundColor(gridPanel, active ? ThemeData.FILE_SELECTED_FOREGROUND_COLOR : ThemeData.FILE_INACTIVE_SELECTED_FOREGROUND_COLOR,
                           selectedBackgroundButton, fontChooser, preview);

        // Archives.
        gridPanel.add(createCaptionLabel("theme_editor.archive_file"));
        addForegroundColor(gridPanel, active ? ThemeData.ARCHIVE_FOREGROUND_COLOR : ThemeData.ARCHIVE_INACTIVE_FOREGROUND_COLOR,
                           backgroundButton, fontChooser, preview);
        addForegroundColor(gridPanel, active ? ThemeData.ARCHIVE_SELECTED_FOREGROUND_COLOR : ThemeData.ARCHIVE_INACTIVE_SELECTED_FOREGROUND_COLOR,
                           selectedBackgroundButton, fontChooser, preview);

        // Hidden files.
        gridPanel.add(createCaptionLabel("theme_editor.hidden_file"));
        addForegroundColor(gridPanel, active ? ThemeData.HIDDEN_FILE_FOREGROUND_COLOR : ThemeData.HIDDEN_FILE_INACTIVE_FOREGROUND_COLOR,
                           backgroundButton, fontChooser, preview);
        addForegroundColor(gridPanel, active ? ThemeData.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR : ThemeData.HIDDEN_FILE_INACTIVE_SELECTED_FOREGROUND_COLOR,
                           selectedBackgroundButton, fontChooser, preview);

        // Symlinks.
        gridPanel.add(createCaptionLabel("theme_editor.symbolic_link"));
        addForegroundColor(gridPanel, active ? ThemeData.SYMLINK_FOREGROUND_COLOR : ThemeData.SYMLINK_INACTIVE_FOREGROUND_COLOR,
                           backgroundButton, fontChooser, preview);
        addForegroundColor(gridPanel, active ? ThemeData.SYMLINK_SELECTED_FOREGROUND_COLOR : ThemeData.SYMLINK_INACTIVE_SELECTED_FOREGROUND_COLOR,
                           selectedBackgroundButton, fontChooser, preview);

        // Marked files.
        gridPanel.add(createCaptionLabel("theme_editor.marked_file"));
        addForegroundColor(gridPanel, active ? ThemeData.MARKED_FOREGROUND_COLOR : ThemeData.MARKED_INACTIVE_FOREGROUND_COLOR,
                           backgroundButton, fontChooser, preview);
        addForegroundColor(gridPanel, active ? ThemeData.MARKED_SELECTED_FOREGROUND_COLOR : ThemeData.MARKED_INACTIVE_SELECTED_FOREGROUND_COLOR,
                           selectedBackgroundButton, fontChooser, preview);

        // Read-only
        gridPanel.add(createCaptionLabel("theme_editor.read_only_file"));
        addForegroundColor(gridPanel,
                active ? ThemeData.READ_ONLY_FOREGROUND_COLOR : ThemeData.READ_ONLY_INACTIVE_FOREGROUND_COLOR,
                backgroundButton,
                fontChooser,
                preview);
        addForegroundColor(gridPanel,
                active ? ThemeData.READ_ONLY_SELECTED_FOREGROUND_COLOR
                        : ThemeData.READ_ONLY_INACTIVE_SELECTED_FOREGROUND_COLOR,
                selectedBackgroundButton, fontChooser, preview);

        // Border.
        gridPanel.add(createCaptionLabel("theme_editor.border"));
        ColorButton borderButton = new ColorButton(parent, themeData, active ? ThemeData.FILE_TABLE_BORDER_COLOR :
            ThemeData.FILE_TABLE_INACTIVE_BORDER_COLOR, PreviewLabel.BORDER_COLOR_PROPERTY_NAME);
        gridPanel.add(borderButton);
        borderButton.addUpdatedPreviewComponent(preview);
        borderButton = new ColorButton(parent, themeData, active ? ThemeData.FILE_TABLE_SELECTED_OUTLINE_COLOR :
            ThemeData.FILE_TABLE_INACTIVE_SELECTED_OUTLINE_COLOR, PreviewLabel.BORDER_COLOR_PROPERTY_NAME);
        gridPanel.add(borderButton);
        borderButton.addUpdatedPreviewComponent(preview);

        setLayout(new BorderLayout());
        add(gridPanel, BorderLayout.WEST);
        add(preview, BorderLayout.EAST);
    }



    // - Misc. ---------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    @Override
    public void commit() {}
}
