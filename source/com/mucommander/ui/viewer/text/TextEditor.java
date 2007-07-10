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

package com.mucommander.ui.viewer.text;

import com.mucommander.file.AbstractFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.theme.*;
import com.mucommander.ui.viewer.EditorFrame;
import com.mucommander.ui.viewer.FileEditor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;


/**
 * 
 *
 * @author Maxence Bernard
 */
public class TextEditor extends FileEditor implements ActionListener, DocumentListener, ThemeListener {

    private JTextArea textArea;

    private JMenuItem copyItem;
    private JMenuItem cutItem;
    private JMenuItem pasteItem;
    private JMenuItem selectAllItem;

	
    public TextEditor() {
        setLayout(new BorderLayout());
        textArea = new JTextArea();
        textArea.setEditable(true);
        initTheme();
        add(textArea, BorderLayout.NORTH);
        ThemeManager.addCurrentThemeListener(this);
    }

    private void initTheme() {
        textArea.setForeground(ThemeManager.getCurrentColor(Theme.EDITOR_FOREGROUND_COLOR));
        textArea.setCaretColor(ThemeManager.getCurrentColor(Theme.EDITOR_FOREGROUND_COLOR));
        textArea.setBackground(ThemeManager.getCurrentColor(Theme.EDITOR_BACKGROUND_COLOR));
        textArea.setSelectedTextColor(ThemeManager.getCurrentColor(Theme.EDITOR_SELECTED_FOREGROUND_COLOR));
        textArea.setSelectionColor(ThemeManager.getCurrentColor(Theme.EDITOR_SELECTED_BACKGROUND_COLOR));
        textArea.setFont(ThemeManager.getCurrentFont(Theme.EDITOR_FONT));
    }


    public Insets getInsets() {
        return new Insets(4, 3, 4, 3);
    }
			
    public void edit(AbstractFile file) throws IOException {
        InputStreamReader isr = new InputStreamReader(file.getInputStream());
        textArea.read(isr, null);
        isr.close();

        textArea.setCaretPosition(0);
        textArea.getDocument().addDocumentListener(this);

        EditorFrame frame = getFrame();
        if(frame!=null) {
            MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();
			
            // Edit menu
            JMenu menu = frame.addMenu(Translator.get("text_editor.edit"));
            copyItem = MenuToolkit.addMenuItem(menu, Translator.get("text_editor.copy"), menuItemMnemonicHelper, null, this);
            cutItem = MenuToolkit.addMenuItem(menu, Translator.get("text_editor.cut"), menuItemMnemonicHelper, null, this);
            pasteItem = MenuToolkit.addMenuItem(menu, Translator.get("text_editor.paste"), menuItemMnemonicHelper, null, this);
            selectAllItem = MenuToolkit.addMenuItem(menu, Translator.get("text_editor.select_all"), menuItemMnemonicHelper, null, this);
        }
    }
		
	
    public static boolean canEditFile(AbstractFile file) {
        return true;
    }

    public long getMaxRecommendedSize() {
        return 131072;
    }

	
    protected void saveAs(AbstractFile destFile) throws IOException {
        OutputStream out = destFile.getOutputStream(false);
        out.write(textArea.getText().getBytes());
        out.close();
		
        setSaveNeeded(false);
    }


    public void requestFocus() {
        textArea.requestFocus();
    }
	
	
    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////
	
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
		
        // Edit menu
        if(source == copyItem)
            textArea.copy();
        else if(source == cutItem)
            textArea.cut();
        else if(source == pasteItem)
            textArea.paste();
        else if(source == selectAllItem)
            textArea.selectAll();
    }


    //////////////////////////////
    // DocumentListener methods //
    //////////////////////////////
	
    public void changedUpdate(DocumentEvent e) {
        setSaveNeeded(true);
    }
	
    public void insertUpdate(DocumentEvent e) {
        setSaveNeeded(true);
    }

    public void removeUpdate(DocumentEvent e) {
        setSaveNeeded(true);
    }



    // - Theme listening -------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /**
     * Receives theme color changes notifications.
     */
    public void colorChanged(ColorChangedEvent event) {
        switch(event.getColorId()) {
        case Theme.EDITOR_FOREGROUND_COLOR:
            textArea.setForeground(event.getColor());
            break;

        case Theme.EDITOR_BACKGROUND_COLOR:
            textArea.setBackground(event.getColor());
            break;

        case Theme.EDITOR_SELECTED_FOREGROUND_COLOR:
            textArea.setSelectedTextColor(event.getColor());
            break;

        case Theme.EDITOR_SELECTED_BACKGROUND_COLOR:
            textArea.setSelectionColor(event.getColor());
            break;
        }
    }

    /**
     * Receives theme font changes notifications.
     */
    public void fontChanged(FontChangedEvent event) {
        if(event.getFontId() == Theme.EDITOR_FONT)
            textArea.setFont(event.getFont());
    }
}
