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

package com.mucommander.ui.dialog.bookmark;

import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * This dialog allows the user to add a bookmark and enter a name for it. User can also
 * choose to store login and password information in the bookmark's URL if the bookmark
 * contains login/password information.
 *
 * @author Maxence Bernard
 */
public class AddBookmarkDialog extends FocusDialog implements ActionListener, DocumentListener {

    private JTextField nameField;
    private JTextField locationField;

    private JButton addButton;
    private JButton cancelButton;

    // Dialog's width has to be at least 320
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	

    // Dialog's width has to be at most 400
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(400,10000);	


    public AddBookmarkDialog(MainFrame mainFrame) {
        super(mainFrame, Translator.get(com.mucommander.ui.action.AddBookmarkAction.class.getName()+".label"), mainFrame);

        Container contentPane = getContentPane();
        YBoxPanel mainPanel = new YBoxPanel(5);

        AbstractFile currentFolder = mainFrame.getActiveTable().getCurrentFolder();

        // Text fields panel
        XAlignedComponentPanel compPanel = new XAlignedComponentPanel();

        // Add name field, editable
        this.nameField = new JTextField(currentFolder.getName());
        nameField.setEditable(true);
        // Monitors text changes to disable 'Add' button if name field is empty
        nameField.getDocument().addDocumentListener(this);
        compPanel.addRow(Translator.get("name")+":", nameField, 10);
		
        // Add URL field, non editable
        this.locationField = new JTextField(currentFolder.getCanonicalPath());
        compPanel.addRow(Translator.get("location")+":", locationField, 10);

        mainPanel.add(compPanel);

        contentPane.add(mainPanel, BorderLayout.NORTH);
				
        addButton = new JButton(Translator.get("add_bookmark_dialog.add"));
        cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(addButton, cancelButton, this), BorderLayout.SOUTH);

        // Select text in name field and transfer focus to it for immediate user change
        nameField.selectAll();
        setInitialFocusComponent(nameField);

        // Selects Add when enter is pressed
        getRootPane().setDefaultButton(addButton);

        // Packs dialog
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
	
        showDialog();
    }

	
    /**
     * Checks if bookmark name is empty (or white space), and enable/disable 'Add' button
     * accordingly, in order to prevent user from adding a bookmark with an empty name.
     */
    private void checkEmptyName() {
        if(nameField.getText().trim().equals("")) {
            if(addButton.isEnabled())
                addButton.setEnabled(false);
        }
        else {
            if(!addButton.isEnabled())
                addButton.setEnabled(true);
        }
    }
	
	
    ///////////////////////////
    // ActionListener method //
    ///////////////////////////
	
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
		
        if (source==addButton)  {
            // Starts by disposing the dialog
            dispose();

            // Add bookmark and write bookmarks file to disk
            BookmarkManager.addBookmark(new Bookmark(nameField.getText(), locationField.getText()));
            BookmarkManager.writeBookmarks(false);
        }
        else if (source==cancelButton)  {
            dispose();			
        }
    }


    //////////////////////////////
    // DocumentListener methods //
    //////////////////////////////
	
    public void changedUpdate(DocumentEvent e) {
        checkEmptyName();
    }
	
	
    public void insertUpdate(DocumentEvent e) {
        checkEmptyName();
    }
	
	
    public void removeUpdate(DocumentEvent e) {
        checkEmptyName();
    }
}
