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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.io.bom.BOMWriter;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogOwner;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.encoding.EncodingListener;
import com.mucommander.ui.encoding.EncodingMenu;
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.viewer.FileEditor;
import com.mucommander.ui.viewer.FileFrame;


/**
 * A simple text editor.
 *
 * @author Maxence Bernard, Nicolas Rinaudo, Arik Hadas
 */
class TextEditor extends FileEditor implements DocumentListener, EncodingListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(TextEditor.class);

    /** Menu bar */
    // Menus //
    private JMenu editMenu;
    private JMenu viewMenu;
    // Items //
    private JMenuItem copyItem;
    private JMenuItem cutItem;
    private JMenuItem pasteItem;
    private JMenuItem selectAllItem;
    private JMenuItem findItem;
    private JMenuItem findNextItem;
    private JMenuItem findPreviousItem;
    private JMenuItem toggleLineWrapItem;
    private JMenuItem toggleLineNumbersItem;

    private TextEditorImpl textEditorImpl;
    private TextViewer textViewerDelegate;
    
    public TextEditor() {
    	textViewerDelegate = new TextViewer(textEditorImpl = new TextEditorImpl(true)) {
    		
    		@Override
    		protected void setComponentToPresent(JComponent component) {
    			TextEditor.this.setComponentToPresent(component);
    		}
    		
    		@Override
    		protected void showLineNumbers(boolean show) {
    			TextEditor.this.setRowHeaderView(show ? new TextLineNumbersPanel(textEditorImpl.getTextArea()) : null);
    	    }
    		
    		@Override
    		protected void initMenuBarItems() {
    			// Edit menu
    	        editMenu = new JMenu(Translator.get("text_editor.edit"));
    	        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

    	        copyItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.copy"), menuItemMnemonicHelper, null, TextEditor.this);

    	        cutItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.cut"), menuItemMnemonicHelper, null, TextEditor.this);
    	        pasteItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.paste"), menuItemMnemonicHelper, null, TextEditor.this);

    	        selectAllItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.select_all"), menuItemMnemonicHelper, null, TextEditor.this);
    	        editMenu.addSeparator();

    	        findItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.find"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), TextEditor.this);
    	        findNextItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.find_next"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), TextEditor.this);
    	        findPreviousItem = MenuToolkit.addMenuItem(editMenu, Translator.get("text_editor.find_previous"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK), TextEditor.this);
    	        
    	        viewMenu = new JMenu(Translator.get("text_editor.view"));
    	        
    	        toggleLineWrapItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, Translator.get("text_editor.line_wrap"), menuItemMnemonicHelper, null, TextEditor.this);
    	        toggleLineWrapItem.setSelected(textEditorImpl.isWrap());
    	        toggleLineNumbersItem = MenuToolkit.addCheckBoxMenuItem(viewMenu, Translator.get("text_editor.line_numbers"), menuItemMnemonicHelper, null, TextEditor.this);
    	        toggleLineNumbersItem.setSelected(TextEditor.this.getRowHeader().getView() != null);
    		}
    	};
    	
    	setComponentToPresent(textEditorImpl.getTextArea());
    }
    
    protected void setComponentToPresent(JComponent component) {
		getViewport().add(component);
	}
    
    void loadDocument(InputStream in, String encoding, DocumentListener documentListener) throws IOException {
    	textViewerDelegate.loadDocument(in, encoding, documentListener);
    }
    
    private void write(OutputStream out) throws IOException {
    	textEditorImpl.write(new BOMWriter(out, textViewerDelegate.getEncoding())); 
    }

    @Override
    public JMenuBar getMenuBar() {
    	JMenuBar menuBar = super.getMenuBar();

    	// Encoding menu
         EncodingMenu encodingMenu = new EncodingMenu(new DialogOwner(getFrame()), textViewerDelegate.getEncoding());
         encodingMenu.addEncodingListener(this);

         menuBar.add(editMenu);
         menuBar.add(viewMenu);
         menuBar.add(encodingMenu);
         
    	return menuBar;
    }
    

    ///////////////////////////////
    // FileEditor implementation //
    ///////////////////////////////

    @Override
    protected void saveAs(AbstractFile destFile) throws IOException {
        OutputStream out = null;

        try {
            out = destFile.getOutputStream();
            write(out);
        }
        finally {
            if(out != null) {
                try {out.close();}
                catch(IOException e) {
                    // Ignored
                }
            }
        }

        // We get here only if the destination file was updated successfully
        // so we can set that no further save is needed at this stage 
        setSaveNeeded(false);

        // Change the parent folder's date to now, so that changes are picked up by folder auto-refresh (see ticket #258)
        if(destFile.isFileOperationSupported(FileOperation.CHANGE_DATE)) {
            try {
                destFile.getParent().changeDate(System.currentTimeMillis());
            }
            catch (IOException e) {
                LOGGER.debug("failed to change the date of "+destFile, e);
                // Fail silently
            }
        }
    }

    @Override
    public void setFrame(final FileFrame frame) {
    	super.setFrame(frame);
    	
    	frame.setFullScreen(TextViewer.isFullScreen());

    	getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK), CUSTOM_FULL_SCREEN_EVENT);
    	getActionMap().put(CUSTOM_FULL_SCREEN_EVENT, new AbstractAction() {
    		public void actionPerformed(ActionEvent e){
    			TextViewer.setFullScreen(!frame.isFullScreen());
    			frame.setFullScreen(TextViewer.isFullScreen());
    		}
    	});
    }

    @Override
    public void show(AbstractFile file) throws IOException {
    	textViewerDelegate.startEditing(file, this);
    }
    
    /////////////////////////////////////
    // DocumentListener implementation //
    /////////////////////////////////////
	
    public void changedUpdate(DocumentEvent e) {
        setSaveNeeded(true);
    }
	
    public void insertUpdate(DocumentEvent e) {
        setSaveNeeded(true);
    }

    public void removeUpdate(DocumentEvent e) {
        setSaveNeeded(true);
    }
    
    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if(source == copyItem)
        	textEditorImpl.copy();
        else if(source == cutItem)
        	textEditorImpl.cut();
        else if(source == pasteItem)
        	textEditorImpl.paste();
        else if(source == selectAllItem)
        	textEditorImpl.selectAll();
        else if(source == findItem)
        	textEditorImpl.find();
        else if(source == findNextItem)
        	textEditorImpl.findNext();
        else if(source == findPreviousItem)
        	textEditorImpl.findPrevious();
        else if(source == toggleLineWrapItem)
        	textViewerDelegate.wrapLines(toggleLineWrapItem.isSelected());
        else if(source == toggleLineNumbersItem)
        	textViewerDelegate.showLineNumbers(toggleLineNumbersItem.isSelected());
        else
        	super.actionPerformed(e);
    }
    
    /////////////////////////////////////
    // EncodingListener implementation //
    /////////////////////////////////////

    public void encodingChanged(Object source, String oldEncoding, String newEncoding) {
    	if(!askSave())
    		return;         // Abort if the file could not be saved

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
