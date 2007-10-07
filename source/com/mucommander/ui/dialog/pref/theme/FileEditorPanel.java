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
import com.mucommander.ui.layout.ProportionalGridPanel;
import com.mucommander.ui.theme.ThemeData;
import com.mucommander.ui.chooser.PreviewLabel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * @author Nicolas Rinaudo, Maxence Bernard
 */
class FileEditorPanel extends ThemeEditorPanel implements PropertyChangeListener {
    // - Instance fields -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Used to preview the editor's theme. */
    private JTextArea preview;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new file table editor.
     * @param parent    dialog containing the panel.
     * @param template  template being edited.
     */
    public FileEditorPanel(PreferencesDialog parent, ThemeData template) {
        super(parent, Translator.get("theme_editor.editor_tab"), template);
        initUI();
        addPropertyChangeListener(this);
    }



    // - UI initialisation ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Initialises the panel's UI.
     */
    private void initUI() {
        YBoxPanel             mainPanel;
        ProportionalGridPanel gridPanel;
        JPanel                colorsPanel;
        FontChooser           fontChooser;
        JPanel                wrapper;
        JPanel                previewPanel;
        JScrollPane           scroll;

        // Font chooser.
        fontChooser = createFontChooser("theme_editor.font", ThemeData.EDITOR_FONT);

        // Colors.
        gridPanel = new ProportionalGridPanel(3);
        gridPanel.add(new JLabel());
        gridPanel.add(createCaptionLabel("theme_editor.text"));
        gridPanel.add(createCaptionLabel("theme_editor.background"));
        createTextButtons(gridPanel, fontChooser, "theme_editor.normal", ThemeData.EDITOR_FOREGROUND_COLOR, ThemeData.EDITOR_BACKGROUND_COLOR);
        createTextButtons(gridPanel, fontChooser, "theme_editor.selected", ThemeData.EDITOR_SELECTED_FOREGROUND_COLOR, ThemeData.EDITOR_SELECTED_BACKGROUND_COLOR);
        colorsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorsPanel.add(gridPanel);
        colorsPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("theme_editor.colors")));

        // Main panel.
        mainPanel = new YBoxPanel();
        mainPanel.add(fontChooser);
        mainPanel.addSpace(10);
        mainPanel.add(colorsPanel);

        // Preview.
        previewPanel = new JPanel();
        previewPanel.add(createPreviewPanel(mainPanel.getPreferredSize().height));
        previewPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("preview")));
        addFontChooserListener(fontChooser, preview);

        // Wrapper.
        wrapper = new JPanel(new BorderLayout());
        wrapper.add(mainPanel, BorderLayout.CENTER);
        wrapper.add(previewPanel, BorderLayout.EAST);

        // Global layout.
        setLayout(new BorderLayout());
        add(wrapper, BorderLayout.NORTH);
    }

    /**
     * Creates the file editor preview panel.
     */
    private JScrollPane createPreviewPanel(int preferredHeight) {
        JScrollPane scroll;

        preview = new JTextArea(15, 15);

        scroll  = new JScrollPane(preview, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getViewport().setPreferredSize(new Dimension(preview.getPreferredSize().width, preferredHeight));

        preview.setText(Translator.get("sample_text"));
        preview.setBackground(template.getColor(ThemeData.EDITOR_BACKGROUND_COLOR));
        preview.setSelectionColor(template.getColor(ThemeData.EDITOR_SELECTED_BACKGROUND_COLOR));
        preview.setForeground(template.getColor(ThemeData.EDITOR_FOREGROUND_COLOR));
        preview.setSelectedTextColor(template.getColor(ThemeData.EDITOR_SELECTED_FOREGROUND_COLOR));
        preview.setCaretColor(template.getColor(ThemeData.EDITOR_FOREGROUND_COLOR));
        preview.setFont(template.getFont(ThemeData.EDITOR_FONT));

        return scroll;
    }

    /**
     * Listens on changes on the foreground and background colors.
     */
    public void propertyChange(PropertyChangeEvent event) {
        // Background color changed.
        if(event.getPropertyName().equals(PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME)) {
            preview.setBackground(template.getColor(ThemeData.EDITOR_BACKGROUND_COLOR));
            preview.setSelectionColor(template.getColor(ThemeData.EDITOR_SELECTED_BACKGROUND_COLOR));
        }

        // Foreground color changed.
        else if(!event.getPropertyName().equals(PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME)) {
            preview.setForeground(template.getColor(ThemeData.EDITOR_FOREGROUND_COLOR));
            preview.setSelectedTextColor(template.getColor(ThemeData.EDITOR_SELECTED_FOREGROUND_COLOR));
            preview.setCaretColor(template.getColor(ThemeData.EDITOR_FOREGROUND_COLOR));
        }
        else
            return;
        preview.repaint();
    }



    // - Modification management ---------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Ignored.
     */
    public void commit() {}
}
