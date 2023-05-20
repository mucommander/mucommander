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

import com.mucommander.RuntimeConstants;
import com.mucommander.commons.util.ui.layout.ProportionalGridPanel;
import com.mucommander.commons.util.ui.layout.YBoxPanel;
import com.mucommander.text.Translator;
import com.mucommander.ui.chooser.FontChooser;
import com.mucommander.ui.chooser.PreviewLabel;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.theme.ThemeData;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Nicolas Rinaudo, Maxence Bernard
 */
class ShellPanel extends ThemeEditorPanel implements PropertyChangeListener {
    private JTextArea terminalPreview;

    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new file table editor.
     * @param parent   dialog containing the panel.
     * @param themeData themeData being edited.
     */
    public ShellPanel(PreferencesDialog parent, ThemeData themeData) {
        super(parent, Translator.get("theme_editor.terminal_tab"), themeData);
        initUI();
    }

    private JComponent createConfigurationPanel(int fontId, int foregroundId, int backgroundId, int selectedForegroundId, int selectedBackgroundId, JComponent fontListener) {
        YBoxPanel             mainPanel;
        ProportionalGridPanel colorPanel;
        JPanel                flowPanel;
        FontChooser           fontChooser;

        mainPanel = new YBoxPanel();

        fontChooser = createFontChooser(fontId);
        mainPanel.add(fontChooser);
        mainPanel.addSpace(10);
        addFontChooserListener(fontChooser, fontListener);

        colorPanel = new ProportionalGridPanel(3);
        addLabelRow(colorPanel, false);

        addColorButtons(colorPanel, fontChooser, "theme_editor.normal", foregroundId, backgroundId).addPropertyChangeListener(this);
        addColorButtons(colorPanel, fontChooser, "theme_editor.selected", selectedForegroundId, selectedBackgroundId).addPropertyChangeListener(this);

        flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flowPanel.add(colorPanel);
        flowPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("theme_editor.colors")));

        mainPanel.add(flowPanel);

        return createScrollPane(mainPanel);
    }

    private JPanel createPreviewPanel() {
        JPanel      panel;
        JScrollPane scroll;

        panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(Translator.get("preview")));

        terminalPreview = new JTextArea(15, 15);
        panel.add(scroll = new JScrollPane(terminalPreview, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        scroll.getViewport().setPreferredSize(terminalPreview.getPreferredSize());
        terminalPreview.append(RuntimeConstants.APP_STRING);
        terminalPreview.append("\nThis is free software, distributed under the terms of the GNU General Public License.");
        terminalPreview.setCaretPosition(0);

        setForegroundColors();
        setBackgroundColors();

        return panel;
    }

    /**
     * Initialises the panel's UI.
     */
    private void initUI() {
        JPanel      mainPanel;
        JPanel      previewPanel;

        setLayout(new BorderLayout());

        previewPanel = createPreviewPanel();

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(Translator.get("theme_editor.terminal_tab"),
                createConfigurationPanel(ThemeData.TERMINAL_FONT, ThemeData.TERMINAL_FOREGROUND_COLOR, ThemeData.TERMINAL_BACKGROUND_COLOR,
                        ThemeData.TERMINAL_SELECTED_FOREGROUND_COLOR, ThemeData.TERMINAL_SELECTED_BACKGROUND_COLOR, terminalPreview));

        mainPanel.add(previewPanel, BorderLayout.EAST);

        add(mainPanel, BorderLayout.NORTH);
    }

    public void propertyChange(PropertyChangeEvent event) {
        // Background color changed.
        if(event.getPropertyName().equals(PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME))
            setBackgroundColors();

        // Foreground color changed.
        else if(event.getPropertyName().equals(PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME))
            setForegroundColors();
    }

    private void setBackgroundColors() {
        terminalPreview.setBackground(themeData.getColor(ThemeData.TERMINAL_BACKGROUND_COLOR));
        terminalPreview.setSelectionColor(themeData.getColor(ThemeData.TERMINAL_SELECTED_BACKGROUND_COLOR));
    }

    private void setForegroundColors() {
        terminalPreview.setForeground(themeData.getColor(ThemeData.TERMINAL_FOREGROUND_COLOR));
        terminalPreview.setSelectedTextColor(themeData.getColor(ThemeData.TERMINAL_SELECTED_FOREGROUND_COLOR));
        terminalPreview.setCaretColor(themeData.getColor(ThemeData.TERMINAL_FOREGROUND_COLOR));
    }



    // - Modification management ---------------------------------------------------------
    // -----------------------------------------------------------------------------------
    @Override
    public void commit() {}
}
