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
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Vector;

/**
 * Base class for theme editor panels.
 * <p>
 * A <code>ThemeEditorPanel</code> is a {@link com.mucommander.ui.dialog.pref.PreferencesPanel} with some
 * theme specific features:
 * <ul>
 *   <li>Access to the {@link #themeData ThemeData} being edited.</li>
 *   <li>Helper methods for theme-specific layout creation.</li>
 * </ul>
 * </p>
 * @author Maxence Bernard, Nicolas Rinaudo
 */
abstract class ThemeEditorPanel extends PreferencesPanel {
    // - Instance fields -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Edited theme data. */
    protected ThemeData themeData;
    /** Holds references to listeners to prevent them from being garbage collected. */
    private   Vector    listenerReferences = new Vector();
    /** Font used to display caption labels. */
    private   Font      captionLabelFont;
    /** Color used to display caption labels. */
    private   Color     captionTextColor = new Color(48, 48, 48);



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new <code>ThemeEditorPanel</code>.
     * @param parent    dialog in which the panel is stored.
     * @param title     title of the panel.
     * @param themeData data that is being edited.
     */
    public ThemeEditorPanel(PreferencesDialog parent, String title, ThemeData themeData) {
        super(parent, title);

        this.themeData = themeData;

        // Initialises the caption label font.
        captionLabelFont = new JLabel().getFont();
        captionLabelFont = captionLabelFont.deriveFont(Font.BOLD, captionLabelFont.getSize()-1.5f);
    }



    // - Caption label methods -----------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a caption label containing the specified localised entry.
     * @param  dictionaryKey name of the dictionary entry to use in the label.
     * @return               a caption label containing the specified localised entry.
     */
    protected JLabel createCaptionLabel(String dictionaryKey) {
        JLabel captionLabel;

        captionLabel = new JLabel(Translator.get(dictionaryKey));
        captionLabel.setFont(captionLabelFont);
        captionLabel.setForeground(captionTextColor);

        return captionLabel;
    }


    /**
     * Adds a row with standard color type labels.
     * <p>
     * This is a convenience method and is strictly equivalent to calling
     * <code>{@link #addLabelRow(ProportionalGridPanel,boolean) addLabelRow}(pane, true)</code>.
     * </p>
     * @param panel panel in which to add the label row.
     */
    protected void addLabelRow(ProportionalGridPanel panel) {addLabelRow(panel, true);}

    /**
     * Adds a row with standard color type labels.
     * <p>
     * The labels that will be created are:
     * <pre>
     *    &lt;EMPTY&gt; | Text | Background | (Preview)
     * </pre>
     * </p>
     * @param panel          panel in which to add the label row.
     * @param includePreview whether or not to add the <code>preview</code> label.
     */
    protected void addLabelRow(ProportionalGridPanel panel, boolean includePreview) {
        // Skips first column.
        panel.add(new JLabel());

        // Creates the standard labels.
        panel.add(createCaptionLabel("theme_editor.text"));
        panel.add(createCaptionLabel("theme_editor.background"));

        // Adds the preview label if requested.
        if(includePreview)
            panel.add(createCaptionLabel("preview"));
    }



    // - Font chooser code ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a font chooser that will keep the specified font up-to-date in the current theme data.
     * @param fontId identifier of the font this chooser will be editing.
     */
    protected FontChooser createFontChooser(int fontId) {
        FontChooser    fontChooser; // Font chooser that will be returned.
        ChangeListener listener;    // Internal listener.

        // Initialises the font chooser.
        fontChooser = new FontChooser(themeData.getFont(fontId));
        fontChooser.setBorder(BorderFactory.createTitledBorder(Translator.get("theme_editor.font")));
        fontChooser.addChangeListener(listener = new ThemeFontChooserListener(themeData, fontId));

        // Hold a reference to this listener to prevent garbage collection
        listenerReferences.add(listener);

        return fontChooser;
    }

    /**
     * Registers a listener on the specified font chooser.
     * <p>
     * The specified listener will receive calls to its <code>setFont</code> method whenever
     * the font chooser has been updated.
     * </p>
     * @param fontChooser      chooser to monitor.
     * @param previewComponent component whose font should be tied to that of the chooser
     */
    protected void addFontChooserListener(FontChooser fontChooser, JComponent previewComponent) {
        // Update button font when a new font has been chosen in the FontChooser
        if(fontChooser!=null) {
            ChangeListener listener;
            fontChooser.addChangeListener(listener = new PreviewFontChooserListener(previewComponent));
            previewComponent.setFont(fontChooser.getCurrentFont());

            // Hold a reference to this listener to prevent garbage collection
            listenerReferences.add(listener);
        }
    }



    // - Scroll pane methods -------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Wraps the specified panel within a scroll pane.
     * <p>
     * The resulting scroll pane will have a vertical bar as needed, no horizontal scroll bar policy.
     * </p>
     * @param panel panel to wrap in a <code>JScrollPane</code>.
     */
    protected JComponent createScrollPane(JPanel panel) {
        JScrollPane scrollPane;

        scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);

        return scrollPane;
    }


    // - Color buttons methods -----------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Adds color buttons to the specified panel.
     * <p>
     * This is a convenience method and is strictly equivalent to calling
     * <code>addColorButtons(gridPanel, fontChooser, label, foregroundId, backgroundId, null)</code>.
     * </p>
     * @param gridPanel    a 3 columns proportinal grid panel in which to add the buttons.
     * @param fontChooser  used to decide which font to use in each color button's preview.
     * @param label        label for the row.
     * @param foregroundId identifier of the color to display in the foreground button.
     * @param backgroundId identifier of the color to display in the background button.
     */
    protected PreviewLabel addColorButtons(ProportionalGridPanel gridPanel, FontChooser fontChooser, String label, int foregroundId, int backgroundId) {
        return addColorButtons(gridPanel, fontChooser, label, foregroundId, backgroundId, null);
    }

    /**
     * Adds color buttons to the specified panel.
     * <p>
     * This method will create a row containing the following items:
     * <pre>
     * LABEL | COLOR (foreground) | COLOR (background)
     * </pre>
     * </p>
     * @param gridPanel    a 3 columns proportinal grid panel in which to add the buttons.
     * @param fontChooser  used to decide which font to use in each color button's preview.
     * @param label        label for the row.
     * @param foregroundId identifier of the color to display in the foreground button.
     * @param backgroundId identifier of the color to display in the background button.
     * @param comp         component to register as a listener on the color buttons.
     */
    protected PreviewLabel addColorButtons(ProportionalGridPanel gridPanel, FontChooser fontChooser, String label, int foregroundId, int backgroundId, JComponent comp) {
        ColorButton  colorButton;
        PreviewLabel previewLabel;

        // Adds the row's caption label.
        gridPanel.add(createCaptionLabel(label));

        // Initialises the color buttons' preview label.
        previewLabel = new PreviewLabel();
        previewLabel.setTextPainted(true);
        addFontChooserListener(fontChooser, previewLabel);

        // Creates the foreground color button.
        gridPanel.add(colorButton = new ColorButton(parent, themeData, foregroundId, PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME, previewLabel));
        if(comp != null)
            colorButton.addUpdatedPreviewComponent(comp);

        // Creates the background color button.
        gridPanel.add(colorButton = new ColorButton(parent, themeData, backgroundId, PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME, previewLabel));
        if(comp != null)
            colorButton.addUpdatedPreviewComponent(comp);

        return previewLabel;
    }



    // - Ad-hoc FontChooser listeners ----------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Used to listen on <code>FontChoosers</code> and update theme data when the font is changed.
     * @author Nicolas Rinaudo
     */
    private class ThemeFontChooserListener implements ChangeListener {
        // - Instance fields -------------------------------------------------------------
        // -------------------------------------------------------------------------------
        /** Theme data in which to update the font when it changes. */
        private ThemeData data;
        /** Identifier of the font we're listening on. */
        private int       fontId;



        // - Initialisation --------------------------------------------------------------
        // -------------------------------------------------------------------------------
        /**
         * Creates a new <code>ThemeFontChooserListener</code>.
         * @param data   theme data to modify when change events are received.
         * @param fontId identifier of the font that is being listened on.
         */
        public ThemeFontChooserListener(ThemeData data, int fontId) {
            this.data   = data;
            this.fontId = fontId;
        }



        // - Changes listening -----------------------------------------------------------
        // -------------------------------------------------------------------------------
        /**
         * Updates the theme data with the new font value.
         */
        public void stateChanged(ChangeEvent event) {data.setFont(fontId, ((FontChooser)event.getSource()).getCurrentFont());}
    }

    /**
     * Used to listen on <code>FontChoosers</code> and update preview components when the font is changed.
     * @author Nicolas Rinaudo
     */
    private class PreviewFontChooserListener implements ChangeListener {
        // - Instance fields -------------------------------------------------------------
        // -------------------------------------------------------------------------------
        /** Component to update when the font has changed. */
        private JComponent preview;



        // - Initialisation --------------------------------------------------------------
        // -------------------------------------------------------------------------------
        /**
         * Creates a new instance of <code>PreviewFontChooserListener</code>.
         * @param preview component to update when the font has changed.
         */
        public PreviewFontChooserListener(JComponent preview) {this.preview = preview;}



        // - Changes listening -----------------------------------------------------------
        // -------------------------------------------------------------------------------
        /**
         * Updates the preview component.
         */
        public void stateChanged(ChangeEvent event) {preview.setFont(((FontChooser)event.getSource()).getCurrentFont());}
    }
}
