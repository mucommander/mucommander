/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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
import com.mucommander.file.FileOperation;
import com.mucommander.io.EncodingDetector;
import com.mucommander.io.RandomAccessInputStream;
import com.mucommander.io.bom.BOM;
import com.mucommander.io.bom.BOMInputStream;
import com.mucommander.io.bom.BOMWriter;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogOwner;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.encoding.EncodingListener;
import com.mucommander.ui.encoding.EncodingMenu;
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.theme.*;
import com.mucommander.ui.viewer.EditorFrame;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.charset.Charset;

/**
 * Text editor implementation used by {@link TextViewer} and {@link TextEditor}.
 *
 * @author Maxence Bernard, Mariusz Jakubowski, Nicolas Rinaudo
 */
class TextEditorImpl implements ThemeListener, ActionListener, EncodingListener {
    private boolean isEditable;
    private DocumentListener documentListener;

    private String searchString;

    private AbstractFile file;
    private String encoding;
    private BOM bom;

    private JFrame frame;
    private JTextArea textArea;

    private JMenuItem copyItem;
    private JMenuItem cutItem;
    private JMenuItem pasteItem;
    private JMenuItem selectAllItem;
    private JMenuItem findItem;
    private JMenuItem findNextItem;
    private JMenuItem findPreviousItem;


    ////////////////////
    // Initialisation //
    ////////////////////
    public TextEditorImpl(boolean isEditable) {
        this.isEditable = isEditable;

        // Init text area
        initTextArea();
    }

    private void initTextArea() {
        textArea = new JTextArea();
        textArea.setEditable(isEditable);

        // Use theme colors and font
        textArea.setForeground(ThemeManager.getCurrentColor(Theme.EDITOR_FOREGROUND_COLOR));
        textArea.setCaretColor(ThemeManager.getCurrentColor(Theme.EDITOR_FOREGROUND_COLOR));
        textArea.setBackground(ThemeManager.getCurrentColor(Theme.EDITOR_BACKGROUND_COLOR));
        textArea.setSelectedTextColor(ThemeManager.getCurrentColor(Theme.EDITOR_SELECTED_FOREGROUND_COLOR));
        textArea.setSelectionColor(ThemeManager.getCurrentColor(Theme.EDITOR_SELECTED_BACKGROUND_COLOR));
        textArea.setFont(ThemeManager.getCurrentFont(Theme.EDITOR_FONT));
    }



    /////////////////
    // Search code //
    /////////////////
    private void find() {
        FindDialog findDialog = new FindDialog(frame);

        if(findDialog.wasValidated()) {
            searchString = findDialog.getSearchString().toLowerCase();

            if(!searchString.equals(""))
                doSearch(0, true);
        }

        // Request the focus on the text area which could be lost after the Find dialog was disposed
        textArea.requestFocus();
	}

    private void findNext() {
    	doSearch(textArea.getSelectionEnd(), true);
    }
    
    private void findPrevious() {
    	doSearch(textArea.getSelectionStart() - 1, false);
	}

    private String getTextLC() {
    	return textArea.getText().toLowerCase();
    }

    private void doSearch(int startPos, boolean forward) {
    	if (searchString == null || searchString.length() == 0)
    		return;
		int pos;
		if (forward) {
			pos = getTextLC().indexOf(searchString, startPos);
		} else {
			pos = getTextLC().lastIndexOf(searchString, startPos);
		}
		if (pos >= 0) {
            textArea.select(pos, pos + searchString.length());
        } else {
            // Beep when no match has been found.
            // The beep method is called from a separate thread because this method seems to lock until the beep has
            // been played entirely. If the 'Find next' shortcut is left pressed, a series of beeps will be played when
            // the end of the file is reached, and we don't want those beeps to played one after the other as to:
            // 1/ not lock the event thread
            // 2/ have those beeps to end rather sooner than later
            new Thread() {
                @Override
                public void run() {
                    Toolkit.getDefaultToolkit().beep();
                }
            }.start();
        }
    }


    ////////////////////////////
    // Package-access methods //
    ////////////////////////////

    void requestFocus() {
        textArea.requestFocus();
    }

    JTextArea getTextArea() {
        return textArea;
    }

    void startEditing(AbstractFile file, DocumentListener documentListener) throws IOException {
        this.file = file;
        this.documentListener = documentListener;

        // Auto-detect encoding

        // Get a RandomAccessInputStream on the file if possible, if not get a simple InputStream
        InputStream in = null;

        try {
            if(file.isFileOperationSupported(FileOperation.RANDOM_READ_FILE)) {
                try { in = file.getRandomAccessInputStream(); }
                catch(IOException e) {
                    // In that case we simply get an InputStream
                }
            }

            if(in==null)
                in = file.getInputStream();

            String encoding = EncodingDetector.detectEncoding(in);
            // If the encoding could not be detected or the detected encoding is not supported, default to UTF-8
            if(encoding==null || !Charset.isSupported(encoding))
                encoding = "UTF-8";

            if(in instanceof RandomAccessInputStream) {
                // Seek to the beginning of the file and reuse the stream
                ((RandomAccessInputStream)in).seek(0);
            }
            else {
                // TODO: it would be more efficient to use some sort of PushBackInputStream, though we can't use PushBackInputStream because we don't want to keep pushing back for the whole InputStream lifetime

                // Close the InputStream and open a new one
                // Note: we could use mark/reset if the InputStream supports it, but it is almost never implemented by
                // InputStream subclasses and a broken by design anyway.
                in.close();
                in = file.getInputStream();
            }

            // Load the file into the text area
            loadDocument(in, encoding);
        }
        finally {
            if(in != null) {
                try {in.close();}
                catch(IOException e) {
                    // Nothing to do here.
                }
            }
        }
        // Listen to theme changes to update the text area if it is visible
        ThemeManager.addCurrentThemeListener(this);
    }

    void loadDocument(InputStream in, String encoding) throws IOException {
        this.encoding = encoding;

        // If the encoding is UTF-something, wrap the stream in a BOMInputStream to filter out the byte-order mark
        // (see ticket #245)
        if(encoding.toLowerCase().startsWith("utf")) {
            in = new BOMInputStream(in);
            bom = ((BOMInputStream)in).getBOM();
        }

        Reader isr = new BufferedReader(new InputStreamReader(in, encoding));

        // Feed the file's contents to text area
        textArea.read(isr, null);

        // Listen to document changes
        if(documentListener!=null)
            textArea.getDocument().addDocumentListener(documentListener);

        // Move cursor to the top
        textArea.setCaretPosition(0);
    }

    void write(OutputStream out) throws IOException {
        Document document;

        document = textArea.getDocument();
        Writer writer;

        // If there was a BOM originally, preserve it when writing the file.
        if(bom==null)
            writer = new OutputStreamWriter(out, encoding);
        else
            writer = new BOMWriter(out, bom);

        try {textArea.getUI().getEditorKit(textArea).write(new BufferedWriter(writer), document, 0, document.getLength());}
        catch(BadLocationException e) {throw new IOException(e.getMessage());}
    }

    void populateMenus(JFrame frame, JMenuBar menuBar) {
        this.frame = frame;

        // Edit menu
        JMenu menu = new JMenu(Translator.get("text_editor.edit"));
        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

        copyItem = MenuToolkit.addMenuItem(menu, Translator.get("text_editor.copy"), menuItemMnemonicHelper, null, this);

        // These menu items are not available to text viewers
        if(isEditable) {
            cutItem = MenuToolkit.addMenuItem(menu, Translator.get("text_editor.cut"), menuItemMnemonicHelper, null, this);
            pasteItem = MenuToolkit.addMenuItem(menu, Translator.get("text_editor.paste"), menuItemMnemonicHelper, null, this);
        }

        selectAllItem = MenuToolkit.addMenuItem(menu, Translator.get("text_editor.select_all"), menuItemMnemonicHelper, null, this);
        menu.addSeparator();

        findItem = MenuToolkit.addMenuItem(menu, Translator.get("text_viewer.find"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), this);
        findNextItem = MenuToolkit.addMenuItem(menu, Translator.get("text_viewer.find_next"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), this);
        findPreviousItem = MenuToolkit.addMenuItem(menu, Translator.get("text_viewer.find_previous"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK), this);

        // Encoding submenu
        menu.add(new JSeparator());
        EncodingMenu encodingMenu = new EncodingMenu(new DialogOwner(frame), encoding);
        encodingMenu.addEncodingListener(this);
        menu.add(encodingMenu);

        menuBar.add(menu);
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if(source == copyItem)
            textArea.copy();
        else if(source == cutItem)
            textArea.cut();
        else if(source == pasteItem)
            textArea.paste();
        else if(source == selectAllItem)
            textArea.selectAll();
        else if(source == findItem)
        	find();
        else if(source == findNextItem)
        	findNext();
        else if(source == findPreviousItem)
        	findPrevious();
    }


    /////////////////////////////////////
    // EncodingListener implementation //
    /////////////////////////////////////

    public void encodingChanged(Object source, String oldEncoding, String newEncoding) {
        if(isEditable) {
            if(!((EditorFrame)frame).askSave())
                return;         // Abort if the file could not be saved
        }

        try {
            // Reload the file using the new encoding
            // Note: loadDocument closes the InputStream
            loadDocument(file.getInputStream(), newEncoding);
        }
        catch(IOException ex) {
            InformationDialog.showErrorDialog(frame, Translator.get("read_error"), Translator.get("file_editor.cannot_read_file", file.getName()));
        }
    }


    //////////////////////////////////
    // ThemeListener implementation //
    //////////////////////////////////

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
