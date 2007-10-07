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
import com.mucommander.ui.dialog.pref.PreferencesPanel;
import com.mucommander.ui.layout.ProportionalGridPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.theme.ThemeData;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Vector;

/**
 * @author Maxence Bernard
 */
abstract class ThemeEditorPanel extends PreferencesPanel {

    /** Theme being edited. */
    protected ThemeData template;

    /** Hold references to listeners to prevent them from being garbage collected */
    protected Vector listenerReferences = new Vector();

    protected Font captionLabelFont;
    protected Color captionTextColor = new Color(48, 48, 48);


    public ThemeEditorPanel(PreferencesDialog parent, String title, ThemeData template) {
        super(parent, title);
        this.template = template;

        captionLabelFont = new JLabel().getFont();
        captionLabelFont = captionLabelFont.deriveFont(Font.BOLD, captionLabelFont.getSize()-1.5f);
    }


    ////////////////////
    // Helper methods //
    ////////////////////

    protected JLabel createCaptionLabel(String dictionaryKey) {
        JLabel captionLabel = new JLabel(Translator.get(dictionaryKey));
        captionLabel.setFont(captionLabelFont);
        captionLabel.setForeground(captionTextColor);

        return captionLabel;
    }

    protected void addVerticalSeparator(YBoxPanel yBoxpanel) {
        yBoxpanel.addSpace(15);
    }

    protected FontChooser createFontChooser(String borderTitleKey, final int themeFontId) {
        final FontChooser fontChooser = new FontChooser(template.getFont(themeFontId));
        fontChooser.setBorder(BorderFactory.createTitledBorder(Translator.get(borderTitleKey)));

        ChangeListener listener = new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                template.setFont(themeFontId, fontChooser.getCurrentFont());
            }
        };

        fontChooser.addChangeListener(listener);

        // Hold a reference to this listener to prevent garbage collection
        listenerReferences.add(listener);

        return fontChooser;
    }

    protected JPanel createColorPanel(
            String titleKey, FontChooser fontChooser,
            int foregroundId, int backgroundId,
            int selectedForegroundId, int selectedBackgroundId)
    {
        return createColorPanel(
            titleKey, fontChooser,
            foregroundId, backgroundId,
            selectedForegroundId, selectedBackgroundId,
            0, 0,
            0, 0,
            false);
    }

    protected JPanel createColorPanel(
            String titleKey, FontChooser fontChooser,
            int foregroundId, int backgroundId,
            int selectedForegroundId, int selectedBackgroundId,
            int unfocusedForegroundId, int unfocusedBackgroundId,
            int unfocusedSelectedForegroundId, int unfocusedSelectedBackgroundId)
    {
        return createColorPanel(
            titleKey, fontChooser,
            foregroundId, backgroundId,
            selectedForegroundId, selectedBackgroundId,
            unfocusedForegroundId, unfocusedBackgroundId,
            unfocusedSelectedForegroundId, unfocusedSelectedBackgroundId,
            true);
    }

    protected JPanel createColorPanel(
            String titleKey, FontChooser fontChooser,
            int foregroundId, int backgroundId,
            int selectedForegroundId, int selectedBackgroundId,
            int unfocusedForegroundId, int unfocusedBackgroundId,
            int unfocusedSelectedForegroundId, int unfocusedSelectedBackgroundId,
            boolean addUnfocusedColors)
    {
        ProportionalGridPanel gridPanel = new ProportionalGridPanel(4);
        addLabelRow(gridPanel);

        addColorButtonRow(gridPanel, fontChooser, "theme_editor.normal", foregroundId, backgroundId);
        addColorButtonRow(gridPanel, fontChooser, "theme_editor.selected", selectedForegroundId, selectedBackgroundId);

        if(addUnfocusedColors) {
            addColorButtonRow(gridPanel, fontChooser, "theme_editor.normal_unfocused", unfocusedForegroundId, unfocusedBackgroundId);
            addColorButtonRow(gridPanel, fontChooser, "theme_editor.selected_unfocused", unfocusedSelectedForegroundId, unfocusedSelectedBackgroundId);
        }

        gridPanel.setBorder(BorderFactory.createTitledBorder(Translator.get(titleKey)));

        return gridPanel;
    }

    protected void addLabelRow(JPanel gridPanel) {
        gridPanel.add(new JLabel());
        gridPanel.add(createCaptionLabel("theme_editor.text"));
        gridPanel.add(createCaptionLabel("theme_editor.background"));
        gridPanel.add(createCaptionLabel("preview"));
    }

    protected void addFontChooserListener(final FontChooser fontChooser, final JComponent previewComponent) {
        // Update button font when a new font has been chosen in the FontChooser
        if(fontChooser!=null) {
            ChangeListener listener = new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    previewComponent.setFont(fontChooser.getCurrentFont());
                }
            };

            fontChooser.addChangeListener(listener);
            previewComponent.setFont(fontChooser.getCurrentFont());

            // Hold a reference to this listener to prevent garbage collection
            listenerReferences.add(listener);
        }
    }

    protected void addColorButtonRow(JPanel gridPanel, final FontChooser fontChooser, String labelKey, int foregroundId, int backgroundId) {
        final PreviewLabel previewLabel = new PreviewLabel();
        previewLabel.setTextPainted(true);
        addFontChooserListener(fontChooser, previewLabel);

        addColorButtonRow(gridPanel, fontChooser, labelKey, foregroundId, backgroundId, previewLabel);
    }

    protected void addColorButtonRow(JPanel gridPanel, final FontChooser fontChooser, String labelKey, int foregroundId, int backgroundId, JComponent previewComponent) {
        gridPanel.add(createCaptionLabel(labelKey));
        gridPanel.add(new ColorButton(parent, template, foregroundId, PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME, previewComponent));
        gridPanel.add(new ColorButton(parent, template, backgroundId, PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME, previewComponent));
        gridPanel.add(previewComponent);
    }

    /**
     * Adds a foreground and background color buttons to the specified panel.
     * @param gridPanel   panel to which the buttons must be added.
     * @param fontChooser object to listen on for font changes.
     * @param label       label of the buttons.
     * @param foregroundId identifier of the foreground button's color.
     * @param backgroundId identifier of the background button's color.
     */
    protected void createTextButtons(JPanel gridPanel, FontChooser fontChooser, String label, int foregroundId, int backgroundId) {
        ColorButton  colorButton;
        PreviewLabel previewLabel;

        gridPanel.add(createCaptionLabel(label));

        previewLabel = new PreviewLabel();
        previewLabel.setTextPainted(true);
        addFontChooserListener(fontChooser, previewLabel);

        gridPanel.add(colorButton = new ColorButton(parent, template, foregroundId, PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME, previewLabel));
        colorButton.addUpdatedPreviewComponent(this);
        colorButton.addUpdatedPreviewComponent(previewLabel);

        gridPanel.add(colorButton = new ColorButton(parent, template, backgroundId, PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME, previewLabel));
        colorButton.addUpdatedPreviewComponent(this);
        colorButton.addUpdatedPreviewComponent(previewLabel);
    }
}
