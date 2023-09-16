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
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.InputStreamReader;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.mucommander.RuntimeConstants;
import com.mucommander.commons.util.ui.layout.ProportionalGridPanel;
import com.mucommander.commons.util.ui.layout.YBoxPanel;
import com.mucommander.text.Translator;
import com.mucommander.ui.chooser.FontChooser;
import com.mucommander.ui.chooser.PreviewLabel;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.theme.ThemeData;

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
     * @param themeData  themeData being edited.
     */
    public FileEditorPanel(PreferencesDialog parent, ThemeData themeData) {
        super(parent, Translator.get("theme_editor.editor_tab"), themeData);
        initUI();
    }



    // - UI initialisation ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates the JPanel that contains all of the color configuration elements.
     * @param fontChooser font chooser used by the editor panel.
     * @return the JPanel that contains all of the color configuration elements.
     */
    private JPanel createColorsPanel(FontChooser fontChooser) {
        // Contains all the color buttons.
        ProportionalGridPanel gridPanel = new ProportionalGridPanel(3);

        // Header.
        addLabelRow(gridPanel, false);

        // Color buttons.
        addColorButtons(gridPanel, fontChooser, "theme_editor.normal",
                        ThemeData.EDITOR_FOREGROUND_COLOR, ThemeData.EDITOR_BACKGROUND_COLOR).addPropertyChangeListener(this);
        addColorButtons(gridPanel, fontChooser, "theme_editor.selected",
                        ThemeData.EDITOR_SELECTED_FOREGROUND_COLOR, ThemeData.EDITOR_SELECTED_BACKGROUND_COLOR).addPropertyChangeListener(this);

        // Wraps everything in a flow layout.
        JPanel colorsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorsPanel.add(gridPanel);
        colorsPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("theme_editor.colors")));

        return colorsPanel;
    }

    /**
     * Initialises the panel's UI.
     */
    private void initUI() {
        // Font chooser and preview initialisation.
        JPanel mainPanel = new JPanel(new BorderLayout());
        FontChooser fontChooser = createFontChooser(ThemeData.EDITOR_FONT);
        mainPanel.add(createPreviewPanel(), BorderLayout.EAST);
        addFontChooserListener(fontChooser, preview);

        // Configuration panel initialisation.
        YBoxPanel configurationPanel = new YBoxPanel(); // Contains all the configuration elements.
        configurationPanel.add(fontChooser);
        configurationPanel.addSpace(10);
        configurationPanel.add(createColorsPanel(fontChooser));
        mainPanel.add(configurationPanel, BorderLayout.CENTER);

        // Layout.
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.NORTH);
    }

    /**
     * Creates the file editor preview panel.
     * @return the file editor preview panel.
     */
    private JPanel createPreviewPanel() {
        // Initialises the preview text area.
        preview = new JTextArea(15, 15);

        // Initialises colors.
        setBackgroundColors();
        setForegroundColors();

        // Creates the panel.
        JPanel previewPanel = new JPanel(new BorderLayout());
        JScrollPane scroll= new JScrollPane(preview, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); // Wraps the preview text.
        previewPanel.add(scroll, BorderLayout.CENTER);
        scroll.getViewport().setPreferredSize(preview.getPreferredSize());
        previewPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("preview")));

        loadText();
        preview.setCaretPosition(0);

        return previewPanel;
    }

    /**
     * Listens on changes on the foreground and background colors.
     */
    public void propertyChange(PropertyChangeEvent event) {
        switch(event.getPropertyName()) {
        // Background color changed.
        case PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME:
            setBackgroundColors();
            break;
         // Foreground color changed.
        case PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME:
            setForegroundColors();
            break;
        }
    }

    private void setBackgroundColors() {
        preview.setBackground(themeData.getColor(ThemeData.EDITOR_BACKGROUND_COLOR));
        preview.setSelectionColor(themeData.getColor(ThemeData.EDITOR_SELECTED_BACKGROUND_COLOR));
    }

    private void setForegroundColors() {
        preview.setForeground(themeData.getColor(ThemeData.EDITOR_FOREGROUND_COLOR));
        preview.setCaretColor(themeData.getColor(ThemeData.EDITOR_FOREGROUND_COLOR));
        preview.setSelectedTextColor(themeData.getColor(ThemeData.EDITOR_SELECTED_FOREGROUND_COLOR));
    }



    // - Misc. ---------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    private void loadText() {
        try (InputStreamReader in = new InputStreamReader(FileEditorPanel.class.getResourceAsStream(RuntimeConstants.LICENSE))){
            char[] buffer = new char[2048];

            int count; // Number of characters read from the last read operation.
            while ((count = in.read(buffer)) != -1)
                preview.append(new String(buffer, 0, count));
        }
        catch(Exception e) {}
    }



    // - Modification management ---------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Ignored.
     */
    @Override
    public void commit() {}
}
