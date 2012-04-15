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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentListener;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.io.EncodingDetector;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.bom.BOMInputStream;
import com.mucommander.conf.MuConfiguration;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogOwner;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.encoding.EncodingListener;
import com.mucommander.ui.encoding.EncodingMenu;
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.viewer.FileViewer;

/**
 * A simple text viewer. Most of the implementation is located in {@link TextEditorImpl}.
 *
 * @author Maxence Bernard, Arik Hadas
 */
class TextViewer extends FileViewer implements EncodingListener {

    private TextEditorImpl textEditorImpl;
    
    /** Menu items */
    // Menus //
    private JMenu editMenu;
    private JMenu viewMenu;
    // Items //
    private JMenuItem copyItem;
    private JMenuItem selectAllItem;
    private JMenuItem findItem;
    private JMenuItem findNextItem;
    private JMenuItem findPreviousItem;
    private JMenuItem toggleWordWrapItem;
    private JMenuItem toggleLineNumbersItem;
    
    private String encoding;
    
    TextViewer() {
    	this(new TextEditorImpl(false));
    }
    
    TextViewer(TextEditorImpl textEditorImpl) {
    	this.textEditorImpl = textEditorImpl;

    	addComponentToPresent(textEditorImpl.getTextArea());
    	
    	showLineNumbers(MuConfiguration.getVariable(MuConfiguration.LINE_NUMBERS, MuConfiguration.DEFAULT_LINE_NUMBERS));
    	textEditorImpl.wrap(MuConfiguration.getVariable(MuConfiguration.WORD_WRAP, MuConfiguration.DEFAULT_WORD_WRAP));

    	initMenuBarItems();
    }
    
    void startEditing(AbstractFile file, DocumentListener documentListener) throws IOException {
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
            loadDocument(in, encoding, documentListener);
        }
        finally {
            if(in != null) {
                try {in.close();}
                catch(IOException e) {
                    // Nothing to do here.
                }
            }
        }
    }

    void loadDocument(InputStream in, String encoding, DocumentListener documentListener) throws IOException {
        this.encoding = encoding;

        // If the encoding is UTF-something, wrap the stream in a BOMInputStream to filter out the byte-order mark
        // (see ticket #245)
        if(encoding.toLowerCase().startsWith("utf")) {
            in = new BOMInputStream(in);
        }

        Reader isr = new BufferedReader(new InputStreamReader(in, encoding));

        textEditorImpl.read(isr);
        
        // Listen to document changes
        if(documentListener!=null)
            textEditorImpl.addDocumentListener(documentListener);
    }
    
    @Override
    public JMenuBar getMenuBar() {
    	JMenuBar menuBar = super.getMenuBar();
    	
    	// Encoding menu
    	EncodingMenu encodingMenu = new EncodingMenu(new DialogOwner(getFrame()), encoding);
        encodingMenu.addEncodingListener(this);

        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(encodingMenu);
        
        return menuBar;
    }
    
    @Override
    public void beforeCloseHook() {
    	MuConfiguration.setVariable(MuConfiguration.WORD_WRAP, textEditorImpl.isWrap());
    	MuConfiguration.setVariable(MuConfiguration.LINE_NUMBERS, getRowHeader().getView() != null);
    }

    String getEncoding() {
    	return encoding;
    }
    
    protected void showLineNumbers(boolean show) {
    	setRowHeaderView(show ? new TextLineNumbersPanel(textEditorImpl.getTextArea()) : null);
    }
    
    protected void initMenuBarItems() {
    	// Edit menu
    	editMenu = new JMenu(Translator.get("text_viewer.edit"));
    	MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

    	copyItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_viewer.copy"), menuItemMnemonicHelper, null, this);

    	selectAllItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_viewer.select_all"), menuItemMnemonicHelper, null, this);
    	editMenu.addSeparator();

    	findItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_viewer.find"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), this);
    	findNextItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_viewer.find_next"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), this);
    	findPreviousItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_viewer.find_previous"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK), this);
    	
    	// View menu
    	viewMenu = new JMenu(Translator.get("text_viewer.view"));
    	
    	toggleWordWrapItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, Translator.get("text_viewer.word_wrap"), menuItemMnemonicHelper, null, this);
    	toggleWordWrapItem.setSelected(textEditorImpl.isWrap());
    	toggleLineNumbersItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, Translator.get("text_viewer.line_numbers"), menuItemMnemonicHelper, null, this);
    	toggleLineNumbersItem.setSelected(getRowHeader().getView() != null);
    }
    
    ///////////////////////////////
    // FileViewer implementation //
    ///////////////////////////////

    @Override
    public void show(AbstractFile file) throws IOException {
        startEditing(file, null);
    }
    
    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if(source == copyItem)
        	textEditorImpl.copy();
        else if(source == selectAllItem)
        	textEditorImpl.selectAll();
        else if(source == findItem)
        	textEditorImpl.find();
        else if(source == findNextItem)
        	textEditorImpl.findNext();
        else if(source == findPreviousItem)
        	textEditorImpl.findPrevious();
        else if(source == toggleWordWrapItem)
        	textEditorImpl.wrap(toggleWordWrapItem.isSelected());
        else if(source == toggleLineNumbersItem)
        	setRowHeaderView(toggleLineNumbersItem.isSelected() ? new TextLineNumbersPanel(textEditorImpl.getTextArea()) : null);
        else
        	super.actionPerformed(e);
    }

    /////////////////////////////////////
    // EncodingListener implementation //
    /////////////////////////////////////

    public void encodingChanged(Object source, String oldEncoding, String newEncoding) {
    	try {
    		// Reload the file using the new encoding
    		// Note: loadDocument closes the InputStream
    		loadDocument(getCurrentFile().getInputStream(), newEncoding, null);
    	}
    	catch(IOException ex) {
    		InformationDialog.showErrorDialog(getFrame(), Translator.get("read_error"), Translator.get("file_editor.cannot_read_file", getCurrentFile().getName()));
    	}   
    }
}
