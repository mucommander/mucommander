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
import com.mucommander.ui.layout.ProportionalGridPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.theme.ThemeData;
import com.mucommander.ui.combobox.EditableComboBox;
import com.mucommander.ui.chooser.PreviewLabel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.*;
import java.awt.*;

/**
 * @author Nicolas Rinaudo, Maxence Bernard
 */
class ShellPanel extends ThemeEditorPanel implements PropertyChangeListener {
    private JTextArea        shellPreview;
    private EditableComboBox historyPreview;
    private JTextField       inputPreview;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new file table editor.
     * @param parent   dialog containing the panel.
     * @param template template being edited.
     */
    public ShellPanel(PreferencesDialog parent, ThemeData template) {
        super(parent, Translator.get("theme_editor.shell_tab"), template);
        initUI();
        addPropertyChangeListener(this);
    }

    private JComponent createConfigurationPanel(int fontId, int foregroundId, int backgroundId, int selectedForegroundId, int selectedBackgroundId, JComponent fontListener) {
        YBoxPanel   mainPanel;
        JPanel      colorPanel;
        JPanel      flowPanel;
        FontChooser fontChooser;

        mainPanel = new YBoxPanel();

        fontChooser = createFontChooser("theme_editor.font", fontId);
        mainPanel.add(fontChooser);
        mainPanel.addSpace(10);
        addFontChooserListener(fontChooser, fontListener);

        colorPanel = new ProportionalGridPanel(3);
        colorPanel.add(new JLabel());
        colorPanel.add(createCaptionLabel("theme_editor.text"));
        colorPanel.add(createCaptionLabel("theme_editor.background"));
        createTextButtons(colorPanel, fontChooser, "theme_editor.normal", foregroundId, backgroundId);
        createTextButtons(colorPanel, fontChooser, "theme_editor.selected", selectedForegroundId, selectedBackgroundId);

        flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flowPanel.add(colorPanel);
        flowPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("theme_editor.colors")));

        mainPanel.add(flowPanel);

        return createScrollPane(mainPanel);
    }


    private JComponent createScrollPane(JPanel panel) {
        JScrollPane scrollPane;

        scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);

        return scrollPane;
    }

    private JPanel createPreviewPanel() {
        JPanel      panel;
        YBoxPanel   headerPanel;
        JScrollPane scroll;

        panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(Translator.get("preview")));

        headerPanel = new YBoxPanel();
        headerPanel.add(new JLabel(Translator.get("run_dialog.run_command_description") + ":"));
        headerPanel.add(historyPreview = new EditableComboBox(inputPreview = new JTextField(Translator.get("sample_text"))));
        historyPreview.addItem(Translator.get("sample_text"));
        historyPreview.addItem(Translator.get("sample_text"));
        historyPreview.setForeground(template.getColor(ThemeData.SHELL_HISTORY_FOREGROUND_COLOR));
        historyPreview.setBackground(template.getColor(ThemeData.SHELL_HISTORY_BACKGROUND_COLOR));
        historyPreview.setSelectionForeground(template.getColor(ThemeData.SHELL_HISTORY_SELECTED_FOREGROUND_COLOR));
        historyPreview.setSelectionBackground(template.getColor(ThemeData.SHELL_HISTORY_SELECTED_BACKGROUND_COLOR));

        headerPanel.addSpace(10);
        headerPanel.add(new JLabel(Translator.get("run_dialog.command_output")+":"));

        panel.add(headerPanel, BorderLayout.NORTH);

        shellPreview = new JTextArea(15, 15);
        shellPreview.setText(Translator.get("sample_text"));
        shellPreview.setLineWrap(true);
        shellPreview.setCaretPosition(0);
        shellPreview.setForeground(template.getColor(ThemeData.SHELL_FOREGROUND_COLOR));
        shellPreview.setCaretColor(template.getColor(ThemeData.SHELL_FOREGROUND_COLOR));
        shellPreview.setBackground(template.getColor(ThemeData.SHELL_BACKGROUND_COLOR));
        shellPreview.setSelectedTextColor(template.getColor(ThemeData.SHELL_SELECTED_FOREGROUND_COLOR));
        shellPreview.setSelectionColor(template.getColor(ThemeData.SHELL_SELECTED_BACKGROUND_COLOR));

        panel.add(scroll = new JScrollPane(shellPreview, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        scroll.getViewport().setPreferredSize(shellPreview.getPreferredSize());

        return panel;
    }

    /**
     * Initialises the panel's UI.
     */
    private void initUI() {
        JPanel      mainPanel;
        JTabbedPane tabbedPane;
        JPanel      previewPanel;

        setLayout(new BorderLayout());

        tabbedPane   = new JTabbedPane();
        previewPanel = createPreviewPanel();

        tabbedPane.add(Translator.get("theme_editor.shell_tab"),
                       createConfigurationPanel(ThemeData.SHELL_FONT, ThemeData.SHELL_FOREGROUND_COLOR, ThemeData.SHELL_BACKGROUND_COLOR,
                                                ThemeData.SHELL_SELECTED_FOREGROUND_COLOR, ThemeData.SHELL_SELECTED_BACKGROUND_COLOR, shellPreview));
        tabbedPane.add(Translator.get("theme_editor.shell_history_tab"),
                       createConfigurationPanel(ThemeData.SHELL_HISTORY_FONT, ThemeData.SHELL_HISTORY_FOREGROUND_COLOR, ThemeData.SHELL_HISTORY_BACKGROUND_COLOR,
                                                ThemeData.SHELL_HISTORY_SELECTED_FOREGROUND_COLOR, ThemeData.SHELL_HISTORY_SELECTED_BACKGROUND_COLOR, historyPreview));

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(previewPanel, BorderLayout.EAST);

        add(mainPanel, BorderLayout.NORTH);
    }

    public void propertyChange(PropertyChangeEvent event) {
        // Background color changed.
        if(event.getPropertyName().equals(PreviewLabel.BACKGROUND_COLOR_PROPERTY_NAME)) {
            shellPreview.setBackground(template.getColor(ThemeData.SHELL_BACKGROUND_COLOR));
            shellPreview.setSelectionColor(template.getColor(ThemeData.SHELL_SELECTED_BACKGROUND_COLOR));
            historyPreview.setBackground(template.getColor(ThemeData.SHELL_HISTORY_BACKGROUND_COLOR));
            historyPreview.setSelectionForeground(template.getColor(ThemeData.SHELL_HISTORY_SELECTED_BACKGROUND_COLOR));
        }

        // Foreground color changed.
        else if(!event.getPropertyName().equals(PreviewLabel.FOREGROUND_COLOR_PROPERTY_NAME)) {
            shellPreview.setForeground(template.getColor(ThemeData.SHELL_FOREGROUND_COLOR));
            shellPreview.setSelectedTextColor(template.getColor(ThemeData.SHELL_SELECTED_FOREGROUND_COLOR));
            shellPreview.setCaretColor(template.getColor(ThemeData.SHELL_FOREGROUND_COLOR));
            historyPreview.setForeground(template.getColor(ThemeData.SHELL_HISTORY_FOREGROUND_COLOR));
            historyPreview.setSelectionForeground(template.getColor(ThemeData.SHELL_HISTORY_SELECTED_FOREGROUND_COLOR));
        }
    }



    // - Modification management ---------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public void commit() {}
}
