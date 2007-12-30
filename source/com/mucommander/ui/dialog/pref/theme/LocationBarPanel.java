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

import com.mucommander.RuntimeConstants;
import com.mucommander.text.Translator;
import com.mucommander.ui.chooser.FontChooser;
import com.mucommander.ui.chooser.PreviewLabel;
import com.mucommander.ui.combobox.EditableComboBox;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.layout.ProportionalGridPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.theme.ThemeData;
import com.mucommander.ui.progress.ProgressTextField;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import javax.swing.*;
import java.awt.*;

/**
 * @author Nicolas Rinaudo, Maxence Bernard
 */
class LocationBarPanel extends ThemeEditorPanel implements PropertyChangeListener {
    private EditableComboBox  normalPreview;
    private EditableComboBox  progressPreview;
    private ProgressTextField progressField;

    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new file table editor.
     * @param parent   dialog containing the panel.
     * @param themeData themeData being edited.
     */
    public LocationBarPanel(PreferencesDialog parent, ThemeData themeData) {
        super(parent, Translator.get("theme_editor.locationbar_tab"), themeData);
        initUI();
    }

    private JPanel createConfigurationPanel() {
        FontChooser           fontChooser;
        YBoxPanel             mainPanel;
        JPanel                flowPanel;
        ProportionalGridPanel colorsPanel;
        PreviewLabel          label;

        fontChooser = createFontChooser(ThemeData.LOCATION_BAR_FONT);
        addFontChooserListener(fontChooser, normalPreview);
        addFontChooserListener(fontChooser, progressPreview);

        addLabelRow(colorsPanel = new ProportionalGridPanel(3), false);

        label = new PreviewLabel();
        addColorButtons(colorsPanel, fontChooser, "theme_editor.normal",
                          ThemeData.LOCATION_BAR_FOREGROUND_COLOR, ThemeData.LOCATION_BAR_BACKGROUND_COLOR, label).addPropertyChangeListener(this);
        addColorButtons(colorsPanel, fontChooser, "theme_editor.selected",
                          ThemeData.LOCATION_BAR_SELECTED_FOREGROUND_COLOR, ThemeData.LOCATION_BAR_SELECTED_BACKGROUND_COLOR).addPropertyChangeListener(this);

        label.setTextPainted(true);
        addFontChooserListener(fontChooser, label);
        colorsPanel.add(createCaptionLabel("theme_editor.progress"));
        colorsPanel.add(new JLabel());
        colorsPanel.add(new ColorButton(parent, themeData, ThemeData.LOCATION_BAR_PROGRESS_COLOR, PreviewLabel.OVERLAY_COLOR_PROPERTY_NAME, label));
        label.addPropertyChangeListener(this);

        flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flowPanel.add(colorsPanel);
        flowPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("theme_editor.colors")));

        mainPanel = new YBoxPanel();
        mainPanel.add(fontChooser);
        mainPanel.addSpace(10);
        mainPanel.add(flowPanel);

        /*
        normalPreview.setPreferredSize(new Dimension((normalPreview.getPreferredSize().width * 3) / 2, normalPreview.getPreferredSize().height));
        progressPreview.setPreferredSize(new Dimension((progressPreview.getPreferredSize().width * 3) / 2, progressPreview.getPreferredSize().height));
        */

        return mainPanel;
    }

    private JPanel createPreviewPanel() {
        YBoxPanel panel;
        JPanel    borderPanel;

        panel = new YBoxPanel();

        //        panel.add(new JLabel(Translator.get("theme_editor.normal")));
        panel.add(createCaptionLabel("theme_editor.normal"));
        panel.add(normalPreview = new EditableComboBox(new JTextField(System.getProperty("user.home"))));
        normalPreview.addItem(System.getProperty("user.home"));
        normalPreview.addItem(RuntimeConstants.HOMEPAGE_URL);

        panel.addSpace(10);
        panel.add(createCaptionLabel("theme_editor.progress"));
        panel.add(progressPreview = new EditableComboBox(progressField = new ProgressTextField(50, themeData.getColor(ThemeData.LOCATION_BAR_PROGRESS_COLOR))));
        progressField.setComboBox(progressPreview);
        progressField.setText(System.getProperty("user.home"));
        progressPreview.setEnabled(false);

        borderPanel = new JPanel(new BorderLayout());
        borderPanel.add(panel, BorderLayout.NORTH);
        borderPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("preview")));

        setBackgroundColors();
        setForegroundColors();
        setProgressColors();

        return borderPanel;
    }

    /**
     * Initialises the panel's UI.
     */
    private void initUI() {
        JPanel panel;

        panel = new JPanel(new BorderLayout());
        panel.add(createPreviewPanel(), BorderLayout.EAST);
        panel.add(createConfigurationPanel(), BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.NORTH);
    }

    public void propertyChange(PropertyChangeEvent event) {
        // Background color changed.
        if(event.getPropertyName().equals(PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME))
            setBackgroundColors();

        // Foreground color changed.
        else if(event.getPropertyName().equals(PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME))
            setForegroundColors();

        // Overlay color changed.
        else if(event.getPropertyName().equals(PreviewLabel.OVERLAY_COLOR_PROPERTY_NAME))
            setProgressColors();
    }

    private void setBackgroundColors() {
        normalPreview.setBackground(themeData.getColor(ThemeData.LOCATION_BAR_BACKGROUND_COLOR));
        normalPreview.setSelectionBackground(themeData.getColor(ThemeData.LOCATION_BAR_SELECTED_BACKGROUND_COLOR));
        progressPreview.setBackground(themeData.getColor(ThemeData.LOCATION_BAR_BACKGROUND_COLOR));
        progressPreview.setSelectionBackground(themeData.getColor(ThemeData.LOCATION_BAR_SELECTED_BACKGROUND_COLOR));
    }

    private void setForegroundColors() {
        normalPreview.setForeground(themeData.getColor(ThemeData.LOCATION_BAR_FOREGROUND_COLOR));
        normalPreview.setSelectionForeground(themeData.getColor(ThemeData.LOCATION_BAR_SELECTED_FOREGROUND_COLOR));
        progressPreview.setForeground(themeData.getColor(ThemeData.LOCATION_BAR_FOREGROUND_COLOR));
        progressPreview.setSelectionForeground(themeData.getColor(ThemeData.LOCATION_BAR_SELECTED_FOREGROUND_COLOR));
        progressField.setDisabledTextColor(themeData.getColor(ThemeData.LOCATION_BAR_FOREGROUND_COLOR));
    }

    private void setProgressColors() {progressField.setProgressColor(themeData.getColor(ThemeData.LOCATION_BAR_PROGRESS_COLOR));}

    // - Modification management ---------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public void commit() {}
}
