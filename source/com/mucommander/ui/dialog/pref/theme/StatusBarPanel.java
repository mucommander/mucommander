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

import com.mucommander.text.SizeFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.border.MutableLineBorder;
import com.mucommander.ui.chooser.FontChooser;
import com.mucommander.ui.chooser.PreviewLabel;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.layout.ProportionalGridPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.theme.ThemeData;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Nicolas Rinaudo, Maxence Bernard
 */
class StatusBarPanel extends ThemeEditorPanel implements PropertyChangeListener {
    private static final int OK       = 0;
    private static final int WARNING  = 1;
    private static final int CRITICAL = 2;

    private final static int WARNING_LEVEL_COLOR_IDS[] = {
        ThemeData.STATUS_BAR_OK_COLOR,
        ThemeData.STATUS_BAR_WARNING_COLOR,
        ThemeData.STATUS_BAR_CRITICAL_COLOR
    };

    private final static String WARNING_LEVEL_LABELS[] = {
        Translator.get("theme_editor.free_space.ok"),
        Translator.get("theme_editor.free_space.warning"),
        Translator.get("theme_editor.free_space.critical")
    };

    private final static int VOLUME_INFO_SIZE_FORMAT    = SizeFormat.DIGITS_MEDIUM | SizeFormat.UNIT_SHORT | SizeFormat.INCLUDE_SPACE | SizeFormat.ROUND_TO_KB;
    private final static long TOTAL_SIZE                = 85899345920l;
    private final static long NORMAL_SIZE               = TOTAL_SIZE / 2;
    private final static long WARNING_SIZE              = TOTAL_SIZE / 10;
    private final static long CRITICAL_SIZE             = TOTAL_SIZE / 100;

    private final static int WARNING_DRAW_PERCENTAGE[] = {50, 10, 1};

    private final static String WARNING_LEVEL_TEXT[] = {
        Translator.get("status_bar.volume_free", SizeFormat.format(NORMAL_SIZE, VOLUME_INFO_SIZE_FORMAT) + " / " + SizeFormat.format(TOTAL_SIZE, VOLUME_INFO_SIZE_FORMAT)),
        Translator.get("status_bar.volume_free", SizeFormat.format(WARNING_SIZE, VOLUME_INFO_SIZE_FORMAT) + " / " + SizeFormat.format(TOTAL_SIZE, VOLUME_INFO_SIZE_FORMAT)),
        Translator.get("status_bar.volume_free", SizeFormat.format(CRITICAL_SIZE, VOLUME_INFO_SIZE_FORMAT) + " / " + SizeFormat.format(TOTAL_SIZE, VOLUME_INFO_SIZE_FORMAT))
    };

    private JLabel  normalPreview;
    private Preview okPreview;
    private Preview warningPreview;
    private Preview criticalPreview;
    

    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new file table editor.
     * @param parent   dialog containing the panel.
     * @param themeData themeData being edited.
     */
    public StatusBarPanel(PreferencesDialog parent, ThemeData themeData) {
        super(parent, Translator.get("theme_editor.statusbar_tab"), themeData);
        initUI();
    }

    private JPanel createGeneralPanel(FontChooser chooser, ColorButton foreground) {
        YBoxPanel mainPanel;
        JPanel    colorPanel;
        JPanel    flowPanel;

        // Initialises the color panel.
        colorPanel = new ProportionalGridPanel(2);
        colorPanel.add(createCaptionLabel("theme_editor.text"));
        colorPanel.add(foreground);

        // Wraps the color panel in a flow layout.
        flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flowPanel.add(colorPanel);
        flowPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("theme_editor.colors")));

        // Creates the general panel.
        mainPanel = new YBoxPanel();
        mainPanel.add(chooser);
        mainPanel.addSpace(10);
        mainPanel.add(flowPanel);

        return mainPanel;
    }

    private JPanel createFreeSpacePanel(FontChooser chooser, ColorButton foreground, ColorButton background, ColorButton border) {
        JPanel       colorPanel;
        JPanel       flowPanel;
        PreviewLabel previewLabel;

        colorPanel = new ProportionalGridPanel(2);
        colorPanel.add(new JLabel());
        colorPanel.add(createCaptionLabel("theme_editor.color"));
        colorPanel.add(createCaptionLabel("theme_editor.background"));
        colorPanel.add(background);
        colorPanel.add(createCaptionLabel("theme_editor.border"));
        colorPanel.add(border);

        for(int i=0; i<3; i++) {
            previewLabel = new PreviewLabel();
            previewLabel.setOverlayUnderText(true);
            previewLabel.setTextPainted(true);
            foreground.addUpdatedPreviewComponent(previewLabel);
            background.addUpdatedPreviewComponent(previewLabel);
            border.addUpdatedPreviewComponent(previewLabel);
            addFontChooserListener(chooser, previewLabel);

            colorPanel.add(createCaptionLabel(WARNING_LEVEL_LABELS[i]));
            colorPanel.add(new ColorButton(parent, themeData, WARNING_LEVEL_COLOR_IDS[i], PreviewLabel.OVERLAY_COLOR_PROPERTY_NAME, previewLabel));
            previewLabel.addPropertyChangeListener(this);
        }

        flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flowPanel.add(colorPanel);

        return flowPanel;
    }

    private void addPreviewLabel(YBoxPanel panel, JLabel preview, String label, FontChooser chooser) {
        JPanel wrapper;

        panel.add(createCaptionLabel(label));

        wrapper = new JPanel(new BorderLayout());
        wrapper.add(preview, BorderLayout.NORTH);
        panel.add(wrapper);

        addFontChooserListener(chooser, preview);
    }

    private JPanel createPreviewPanel(FontChooser fontChooser) {
        YBoxPanel previewPanel;
        Insets    insets;

        previewPanel = new YBoxPanel();

        addPreviewLabel(previewPanel, normalPreview = new JLabel(Translator.get("status_bar.selected_files", "3", "14")), "theme_editor.normal", fontChooser);
        normalPreview.setForeground(themeData.getColor(ThemeData.STATUS_BAR_FOREGROUND_COLOR));

        addPreviewLabel(previewPanel, okPreview       = new Preview(OK),       "theme_editor.free_space.ok", fontChooser);
        addPreviewLabel(previewPanel, warningPreview  = new Preview(WARNING),  "theme_editor.free_space.warning", fontChooser);
        addPreviewLabel(previewPanel, criticalPreview = new Preview(CRITICAL), "theme_editor.free_space.critical", fontChooser);

        previewPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("preview")));

        insets = previewPanel.getInsets();
        previewPanel.setInsets(new Insets(insets.top, insets.left + 8, insets.bottom, insets.right + 6));

        return previewPanel;
    }

    /**
     * Initialises the panel's UI.
     */
    private void initUI() {
        JPanel       mainPanel;
        ColorButton  foreground;
        ColorButton  background;
        ColorButton  border;
        PreviewLabel previewLabel;
        PreviewLabel borderPreviewLabel;
        FontChooser  fontChooser;

        JTabbedPane tabbedPane;
        fontChooser = createFontChooser(ThemeData.STATUS_BAR_FONT);

        // Initialises the foreground color button.
        foreground = new ColorButton(parent, themeData, ThemeData.STATUS_BAR_FOREGROUND_COLOR, PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME, previewLabel = new PreviewLabel());
        previewLabel.setTextPainted(true);
        addFontChooserListener(fontChooser, previewLabel);
        previewLabel.addPropertyChangeListener(this);

        // Initialises the background and border color buttons.
        background = new ColorButton(parent, themeData, ThemeData.STATUS_BAR_BACKGROUND_COLOR, PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME, previewLabel = new PreviewLabel());
        border     = new ColorButton(parent, themeData, ThemeData.STATUS_BAR_BORDER_COLOR, PreviewLabel.BORDER_COLOR_PROPERTY_NAME, borderPreviewLabel = new PreviewLabel());

        // Initialises the background color preview.
        previewLabel.setTextPainted(true);
        foreground.addUpdatedPreviewComponent(previewLabel);
        border.addUpdatedPreviewComponent(previewLabel);
        addFontChooserListener(fontChooser, previewLabel);
        previewLabel.addPropertyChangeListener(this);

        // Initialises the border color preview.
        borderPreviewLabel.setTextPainted(true);
        foreground.addUpdatedPreviewComponent(borderPreviewLabel);
        background.addUpdatedPreviewComponent(borderPreviewLabel);
        addFontChooserListener(fontChooser, borderPreviewLabel);
        borderPreviewLabel.addPropertyChangeListener(this);

        tabbedPane   = new JTabbedPane();
        tabbedPane.add(Translator.get("theme_editor.general"), createGeneralPanel(fontChooser, foreground));
        tabbedPane.add(Translator.get("theme_editor.free_space"), createFreeSpacePanel(fontChooser, foreground, background, border));


        // Main layout.
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(createPreviewPanel(fontChooser), BorderLayout.EAST);

        // Aligns everything north.
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.NORTH);
    }


    // - Modification management ---------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Ignored.
     */
    public void commit() {}



    // - Property listening --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Refreshes the UI depending on the property event.
     */
    public void propertyChange(PropertyChangeEvent event) {
        // Repaints previews when the overlay or background color have been changed.
        if(event.getPropertyName().equals(PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME) || event.getPropertyName().equals(PreviewLabel.OVERLAY_COLOR_PROPERTY_NAME)) {
            okPreview.repaint();
            warningPreview.repaint();
            criticalPreview.repaint();
        }

        // Resets the preview labels' foreground color.
        else if(event.getPropertyName().equals(PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME)) {
            Color color;

            color = themeData.getColor(ThemeData.STATUS_BAR_FOREGROUND_COLOR);

            okPreview.setForeground(color);
            warningPreview.setForeground(color);
            criticalPreview.setForeground(color);
            normalPreview.setForeground(color);
        }

        // Resets the preview labels' borders.
        else if(event.getPropertyName().equals(PreviewLabel.BORDER_COLOR_PROPERTY_NAME)) {
            okPreview.refreshBorder();
            warningPreview.refreshBorder();
            criticalPreview.refreshBorder();
        }
    }



    // - Preview labels ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    private class Preview extends JLabel {
        private MutableLineBorder border;
        private int type;

        public Preview(int type) {
            super(WARNING_LEVEL_TEXT[type]);
            setOpaque(false);
            setBorder(border = new MutableLineBorder(Color.BLACK, 1));
            setHorizontalAlignment(CENTER);
            setForeground(themeData.getColor(ThemeData.STATUS_BAR_FOREGROUND_COLOR));
            this.type = type;
        }

        public void refreshBorder() {
            border.setLineColor(themeData.getColor(ThemeData.STATUS_BAR_BORDER_COLOR));
            repaint();
        }

        public void paint(Graphics g) {
            int width;

            width = ((getWidth() - 2) * WARNING_DRAW_PERCENTAGE[type]) / 100;

            if(type == OK)
                g.setColor(themeData.getColor(ThemeData.STATUS_BAR_OK_COLOR));
            else if(type == WARNING)
                g.setColor(themeData.getColor(ThemeData.STATUS_BAR_WARNING_COLOR));
            else
                g.setColor(themeData.getColor(ThemeData.STATUS_BAR_CRITICAL_COLOR));
            g.fillRect(1, 1, width + 1, getHeight() - 2);

            g.setColor(themeData.getColor(ThemeData.STATUS_BAR_BACKGROUND_COLOR));
            g.fillRect(width + 1, 1, getWidth() - width - 1, getHeight() - 2);

            super.paint(g);
        }
    }
}
