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

/**
 * @author Nicolas Rinaudo, Maxence Bernard
 */
class LocationBarPanel extends ThemeEditorPanel {

    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new file table editor.
     * @param parent   dialog containing the panel.
     * @param template template being edited.
     */
    public LocationBarPanel(PreferencesDialog parent, ThemeData template) {
        super(parent, Translator.get("theme_editor.locationbar_tab"), template);
        initUI();
    }

    /**
     * Initialises the panel's UI.
     */
    private void initUI() {
        YBoxPanel mainPanel = new YBoxPanel();

        FontChooser fontChooser = createFontChooser("theme_editor.font", ThemeData.LOCATION_BAR_FONT);
        mainPanel.add(fontChooser);

        JPanel gridPanel = new ProportionalGridPanel(4);
        addLabelRow(gridPanel);

        PreviewLabel previewLabel = new PreviewLabel();
        previewLabel.setTextPainted(true);
        addFontChooserListener(fontChooser, previewLabel);

        gridPanel.add(createCaptionLabel("theme_editor.normal"));
        ColorButton foregroundColorButton = new ColorButton(parent, template, ThemeData.LOCATION_BAR_FOREGROUND_COLOR, PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME, previewLabel);
        gridPanel.add(foregroundColorButton);
        ColorButton backgroundColorButton = new ColorButton(parent, template, ThemeData.LOCATION_BAR_BACKGROUND_COLOR, PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME, previewLabel);
        gridPanel.add(backgroundColorButton);
        gridPanel.add(previewLabel);

        addColorButtonRow(gridPanel, fontChooser, "theme_editor.selected", ThemeData.LOCATION_BAR_SELECTED_FOREGROUND_COLOR, ThemeData.LOCATION_BAR_SELECTED_BACKGROUND_COLOR);

        previewLabel = new PreviewLabel();
        previewLabel.setTextPainted(true);
        foregroundColorButton.addUpdatedPreviewComponent(previewLabel);
        backgroundColorButton.addUpdatedPreviewComponent(previewLabel);
        addFontChooserListener(fontChooser, previewLabel);

        gridPanel.add(createCaptionLabel("theme_editor.progress"));
        gridPanel.add(new JLabel());
        gridPanel.add(new ColorButton(parent, template, ThemeData.LOCATION_BAR_PROGRESS_COLOR, PreviewLabel.OVERLAY_COLOR_PROPERTY_NAME, previewLabel));
        gridPanel.add(previewLabel);

        gridPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("theme_editor.colors")));
        mainPanel.add(gridPanel);

        add(mainPanel);
    }



    // - Modification management ---------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public void commit() {}
}
