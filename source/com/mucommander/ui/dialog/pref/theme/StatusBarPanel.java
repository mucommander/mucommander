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
class StatusBarPanel extends ThemeEditorPanel {

    private final static int WARNING_LEVEL_COLOR_IDS[] = {
        ThemeData.STATUS_BAR_OK_COLOR,
        ThemeData.STATUS_BAR_WARNING_COLOR,
        ThemeData.STATUS_BAR_CRITICAL_COLOR
    };

    private final static String WARNING_LEVEL_LABELS[] = {
        Translator.get("theme_editor.free_space", Translator.get("theme_editor.free_space.ok")),
        Translator.get("theme_editor.free_space", Translator.get("theme_editor.free_space.warning")),
        Translator.get("theme_editor.free_space", Translator.get("theme_editor.free_space.critical"))
    };
    

    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new file table editor.
     * @param parent   dialog containing the panel.
     * @param template template being edited.
     */
    public StatusBarPanel(PreferencesDialog parent, ThemeData template) {
        super(parent, Translator.get("theme_editor.statusbar_tab"), template);
        initUI();
    }

    /**
     * Initialises the panel's UI.
     */
    private void initUI() {
        YBoxPanel mainPanel;
        JPanel    gridPanel;

        mainPanel = new YBoxPanel();

        FontChooser fontChooser = createFontChooser("theme_editor.font", ThemeData.STATUS_BAR_FONT);
        mainPanel.add(fontChooser);

        gridPanel = new ProportionalGridPanel(3);
        gridPanel.add(new JLabel());
        gridPanel.add(createCaptionLabel("theme_editor.color"));
        gridPanel.add(createCaptionLabel("preview"));

        gridPanel.add(createCaptionLabel("theme_editor.text"));
        ColorButton foregroundColorButton = new ColorButton(parent, template, ThemeData.STATUS_BAR_FOREGROUND_COLOR, PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME);
        gridPanel.add(foregroundColorButton);
        gridPanel.add(new JLabel());

        gridPanel.add(createCaptionLabel(Translator.get("theme_editor.free_space", Translator.get("theme_editor.background"))));
        ColorButton backgroundColorButton = new ColorButton(parent, template, ThemeData.STATUS_BAR_BACKGROUND_COLOR, PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME);
        gridPanel.add(backgroundColorButton);
        gridPanel.add(new JLabel());

        gridPanel.add(createCaptionLabel(Translator.get("theme_editor.free_space", Translator.get("theme_editor.border"))));
        ColorButton borderColorButton = new ColorButton(parent, template, ThemeData.STATUS_BAR_BORDER_COLOR, PreviewLabel.BORDER_COLOR_PROPERTY_NAME);
        gridPanel.add(borderColorButton);
        gridPanel.add(new JLabel(""));

        PreviewLabel previewLabel;
        for(int i=0; i<3; i++) {
            previewLabel = new PreviewLabel();
            previewLabel.setTextPainted(true);
            previewLabel.setOverlayUnderText(true);
            foregroundColorButton.addUpdatedPreviewComponent(previewLabel);
            backgroundColorButton.addUpdatedPreviewComponent(previewLabel);
            borderColorButton.addUpdatedPreviewComponent(previewLabel);
            addFontChooserListener(fontChooser, previewLabel);

            gridPanel.add(createCaptionLabel(WARNING_LEVEL_LABELS[i]));
            gridPanel.add(new ColorButton(parent, template, WARNING_LEVEL_COLOR_IDS[i], PreviewLabel.OVERLAY_COLOR_PROPERTY_NAME, previewLabel));
            gridPanel.add(previewLabel);
        }

        gridPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("theme_editor.colors")));
        mainPanel.add(gridPanel);

        add(mainPanel);
    }


    // - Modification management ---------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public void commit() {}
}
