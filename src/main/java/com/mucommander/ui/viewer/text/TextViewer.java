/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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
import java.nio.charset.Charset;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
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
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuSnapshot;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogOwner;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.encoding.EncodingListener;
import com.mucommander.ui.encoding.EncodingMenu;
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.viewer.FileFrame;
import com.mucommander.ui.viewer.FileViewer;

/**
 * A simple text viewer. Most of the implementation is located in {@link TextEditorImpl}.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class TextViewer extends FileViewer implements EncodingListener {

	public final static String CUSTOM_FULL_SCREEN_EVENT = "CUSTOM_FULL_SCREEN_EVENT";

	private TextEditorImpl textEditorImpl;

	private static boolean fullScreen = MuConfigurations.getSnapshot().getBooleanVariable(MuSnapshot.TEXT_FILE_PRESENTER_FULL_SCREEN);

	private static boolean lineWrap = MuConfigurations.getSnapshot().getVariable(MuSnapshot.TEXT_FILE_PRESENTER_LINE_WRAP, MuSnapshot.DEFAULT_LINE_WRAP);

	private static boolean lineNumbers = MuConfigurations.getSnapshot().getVariable(MuSnapshot.TEXT_FILE_PRESENTER_LINE_NUMBERS, MuSnapshot.DEFAULT_LINE_NUMBERS);

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
    private JMenuItem toggleLineWrapItem;
    private JMenuItem toggleLineNumbersItem;
    
    private String encoding;
    
    TextViewer() {
    	this(new TextEditorImpl(false));
    }
    
    TextViewer(TextEditorImpl textEditorImpl) {
    	this.textEditorImpl = textEditorImpl;

    	setComponentToPresent(textEditorImpl.getTextArea());
    	
    	showLineNumbers(lineNumbers);
    	textEditorImpl.wrap(lineWrap);

    	initMenuBarItems();
    }
    
    @Override
    public void setFrame(final FileFrame frame) {
        super.setFrame(frame);

        frame.setFullScreen(isFullScreen());

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK), CUSTOM_FULL_SCREEN_EVENT);
    	getActionMap().put(CUSTOM_FULL_SCREEN_EVENT, new AbstractAction() {
    		public void actionPerformed(ActionEvent e){
    			setFullScreen(!frame.isFullScreen());
    			frame.setFullScreen(isFullScreen());
    		}
    	});
    }

    static void setFullScreen(boolean fullScreen) {
		TextViewer.fullScreen = fullScreen;
	}

	public static boolean isFullScreen() {
		return fullScreen;
	}

	static void setLineWrap(boolean lineWrap) {
		TextViewer.lineWrap = lineWrap;
	}

	public static boolean isLineWrap() {
		return lineWrap;
	}

	static void setLineNumbers(boolean lineNumbers) {
		TextViewer.lineNumbers = lineNumbers;
	}

	public static boolean isLineNumbers() {
		return lineNumbers;
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

    void loadDocument(InputStream in, final String encoding, DocumentListener documentListener) throws IOException {
        // If the encoding is UTF-something, wrap the stream in a BOMInputStream to filter out the byte-order mark
        // (see ticket #245)
        if(encoding != null && encoding.toLowerCase().startsWith("utf")) {
            in = new BOMInputStream(in);
        }

        // If the given encoding is invalid (null or not supported), default to "UTF-8" 
        this.encoding = encoding==null || !Charset.isSupported(encoding) ? "UTF-8" : encoding;

        textEditorImpl.read(new BufferedReader(new InputStreamReader(in, this.encoding)));
        
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
    
    String getEncoding() {
    	return encoding;
    }
    
    protected void showLineNumbers(boolean show) {
    	setRowHeaderView(show ? new TextLineNumbersPanel(textEditorImpl.getTextArea()) : null);
    	setLineNumbers(show);
    }

    protected void wrapLines(boolean wrap) {
    	textEditorImpl.wrap(wrap);
    	setLineWrap(wrap);
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
    	
    	toggleLineWrapItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, Translator.get("text_viewer.line_wrap"), menuItemMnemonicHelper, null, this);
    	toggleLineWrapItem.setSelected(textEditorImpl.isWrap());
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
        else if(source == toggleLineWrapItem)
        	setLineWrap(toggleLineWrapItem.isSelected());
        else if(source == toggleLineNumbersItem)
        	showLineNumbers(toggleLineNumbersItem.isSelected());
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
