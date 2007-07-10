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
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.list.DynamicList;
import com.mucommander.ui.list.SortableListPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.util.AlteredVector;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * This dialog contains a list of all bookmarks and allows the user to edit, remove, duplicate, go to and reorder them.
 *
 * <p>If the contents of this list is modified, bookmarks will be saved to disk when this dialog is disposed.</p>
 *
 * @author Maxence Bernard
 */
public class EditBookmarksDialog extends FocusDialog implements ActionListener, ListSelectionListener, DocumentListener {

    private MainFrame mainFrame;

    private JButton newButton;
    private JButton duplicateButton;
    private JButton removeButton;
    private JButton goToButton;
    private JButton closeButton;

    private JTextField nameField;
    private JTextField locationField;

    private AlteredVector bookmarks;
    private DynamicList bookmarkList;

    private int currentListIndex;
    private Bookmark currentBookmarkSave;

    private boolean ignoreDocumentListenerEvents;

    // Dialog's size has to be at least 400x300
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(440,330);	

    // Dialog's size has to be at most 600x400
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(600,400);



    public EditBookmarksDialog(MainFrame mainFrame) {
        super(mainFrame, Translator.get(com.mucommander.ui.action.EditBookmarksAction.class.getName()+".label"), mainFrame);

        this.mainFrame = mainFrame;

        Container contentPane = getContentPane();

        // Retrieve bookmarks list
        this.bookmarks = BookmarkManager.getBookmarks();

        // Temporarily suspend bookmark change events, otherwise an event would be fired for each character
        // typed in the name / location fields. Events will be resumed when this dialog is disposed
        BookmarkManager.setFireEvents(false);

        // Create the sortable bookmarks list panel
        SortableListPanel listPanel = new SortableListPanel(bookmarks);
        this.bookmarkList = listPanel.getDynamicList();

        contentPane.add(listPanel, BorderLayout.CENTER);

        // Text fields panel
        XAlignedComponentPanel compPanel = new XAlignedComponentPanel();

        // Add bookmark name field
        this.nameField = new JTextField();
        nameField.getDocument().addDocumentListener(this);
        compPanel.addRow(Translator.get("name")+":", nameField, 5);

        // Add bookmark location field
        this.locationField = new JTextField();
        locationField.getDocument().addDocumentListener(this);
        compPanel.addRow(Translator.get("location")+":", locationField, 10);

        YBoxPanel yPanel = new YBoxPanel(10);
        yPanel.add(compPanel);

        // Add buttons: 'remove', 'move up' and 'move down' buttons are enabled
        // only if there is at least one bookmark in the table
        XBoxPanel buttonsPanel = new XBoxPanel();
        JPanel buttonGroupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        MnemonicHelper mnemonicHelper = new MnemonicHelper();

        // New bookmark button
        newButton = new JButton(Translator.get("edit_bookmarks_dialog.new"));
        newButton.setMnemonic(mnemonicHelper.getMnemonic(newButton));
        newButton.addActionListener(this);
        buttonGroupPanel.add(newButton);

        // Duplicate bookmark button
        duplicateButton = new JButton(Translator.get("duplicate"));
        duplicateButton.setMnemonic(mnemonicHelper.getMnemonic(duplicateButton));
        duplicateButton.addActionListener(this);
        buttonGroupPanel.add(duplicateButton);

        // Remove bookmark button
        removeButton = new JButton(bookmarkList.getRemoveAction());
        removeButton.setMnemonic(mnemonicHelper.getMnemonic(removeButton));
        buttonGroupPanel.add(removeButton);

        // Go to bookmark button
        goToButton = new JButton(Translator.get("go_to"));
        goToButton.setMnemonic(mnemonicHelper.getMnemonic(goToButton));
        goToButton.addActionListener(this);
        buttonGroupPanel.add(goToButton);

        buttonsPanel.add(buttonGroupPanel);

        // Button that closes the window
        closeButton = new JButton(Translator.get("close"));
        closeButton.setMnemonic(mnemonicHelper.getMnemonic(closeButton));
        closeButton.addActionListener(this);

        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(closeButton);

        yPanel.add(buttonsPanel);

        contentPane.add(yPanel, BorderLayout.SOUTH);

        // Set initial text components and buttons' enabled state
        updateComponents();

        // Listen to selection changes to reflect the change
        bookmarkList.addListSelectionListener(this);

        // table will receive initial focus
        setInitialFocusComponent(bookmarkList);
		
        // Selects OK when enter is pressed
        getRootPane().setDefaultButton(closeButton);

        // Packs dialog
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
		
        // Call dispose() on close and write bookmarks file
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        showDialog();
    }


    /**
     * Updates text fields and buttons' enabled state based on the current selection. Should be called
     * whenever the list selection has changed.
     */
    private void updateComponents() {
        String nameValue = null;
        String locationValue = null;

        boolean componentsEnabled = false;

        if(!bookmarkList.isSelectionEmpty() && bookmarks.size()>0) {
            componentsEnabled = true;

            Bookmark b = (Bookmark)bookmarkList.getSelectedValue();
            nameValue = b.getName();
            locationValue = b.getLocation();
        }

        // Ignore text field events while setting values
        ignoreDocumentListenerEvents = true;

        nameField.setText(nameValue);
        nameField.setEnabled(componentsEnabled);

        locationField.setText(locationValue);
        locationField.setEnabled(componentsEnabled);

        ignoreDocumentListenerEvents = false;

        goToButton.setEnabled(componentsEnabled);
        duplicateButton.setEnabled(componentsEnabled);
        removeButton.setEnabled(componentsEnabled);
    }


    /**
     * Called whenever a value in one of the text fields has been modified, and updates the current Bookmark instance to
     * use the new value.
     *
     * @param sourceDocument the javax.swing.text.Document of the JTextField that was modified
     */
    private void modifyBookmark(Document sourceDocument) {
//if(Debug.ON) Debug.trace("starts, currentBookmarkSave="+currentBookmarkSave+" currentListIndex="+currentListIndex+" ignoreDocumentListenerEvents="+ignoreDocumentListenerEvents+", selectedIndex="+bookmarkList.getSelectedIndex());


        if(ignoreDocumentListenerEvents || bookmarks.size()==0)
            return;

        int selectedIndex = bookmarkList.getSelectedIndex();

        // Make sure that the selected index is not out of bounds
        if(!bookmarkList.isIndexValid(selectedIndex))
            return;

        Bookmark selectedBookmark = (Bookmark)bookmarks.elementAt(selectedIndex);

        if(currentBookmarkSave==null) {
            // Create a clone of the current bookmark in order to cancel any modifications made to it if the dialog
            // is cancelled.
            try { currentBookmarkSave = (Bookmark)selectedBookmark.clone(); }
            catch(CloneNotSupportedException ex) {}

            this.currentListIndex = selectedIndex;
        }

        // Update name
        if(sourceDocument==nameField.getDocument()) {
            String name = nameField.getText();
            if(name.trim().equals(""))
                name = getFreeNameVariation(Translator.get("untitled"));

            selectedBookmark.setName(name);
            bookmarkList.itemModified(selectedIndex, false);
        }
        // Update location
        else {
            selectedBookmark.setLocation(locationField.getText());
        }

//if(Debug.ON) Debug.trace("ends, currentBookmarkSave="+currentBookmarkSave+" currentListIndex="+currentListIndex);
    }


    /**
     * Returns the first variation of the given name that is not already used by another bookmark, e.g. :
     * <br>"music" -> "music (2)" if there already is bookmark with the "music" name
     * <br>"music (2)" -> "music (3)" and so on...
     */
    private String getFreeNameVariation(String name) {

        if(!containsName(name))
            return name;

        int len = name.length();
        char c;
        int num = 2;
        if(len>4 && name.charAt(len-1)==')'
                    && (c=name.charAt(len-2))>='0' && c<='9'
                    && name.charAt(len-3)=='('
                    && name.charAt(len-4)==' ')
        {
            num = (c-'0')+1;
            name = name.substring(0, len-4);
        }


        String newName = null;
        while(containsName(newName=(name+" ("+num+++")")));

        return newName;
    }


    /**
     * Returns true if the bookmarks list contains a bookmark that has the specified name.
     */
    private boolean containsName(String name) {
        int nbBookmarks = bookmarks.size();
        for(int i=0; i<nbBookmarks; i++) {
            if(((Bookmark)bookmarks.elementAt(i)).getName().equals(name))
                return true;
        }

        return false;
    }



    /**
     * Overrides dispose() to write bookmarks to disk (if needed).
     */
    public void dispose() {
        super.dispose();

        // Rollback current bookmark's modifications if the dialog was cancelled
        if(currentBookmarkSave!=null) {
//if(Debug.ON) Debug.trace("currentBookmarkSave="+currentBookmarkSave+" currentListIndex="+currentListIndex);

            bookmarks.setElementAt(currentBookmarkSave, currentListIndex);
            currentBookmarkSave = null;
        }

        // Resume bookmark change events
        BookmarkManager.setFireEvents(true);

        // Write bookmarks file to disk, only if changes were made to bookmarks
        BookmarkManager.writeBookmarks(false);
    }

	
    ///////////////////////////
    // ActionListener method //
    ///////////////////////////
	
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
		
        // Dispose the dialog (bookmarks save is performed in dispose())
        if (source== closeButton)  {
            // Do not rollback current bookmark's modifications on dispose()
            currentBookmarkSave = null;

            dispose();
        }
        // Create a new empty bookmark / duplicate the currently selected bookmark
        else if (source==newButton || source==duplicateButton) {
            Bookmark newBookmark;
            if(source==newButton) {
                newBookmark = new Bookmark(getFreeNameVariation(Translator.get("untitled")), "");
            }
            else {      // Duplicate button
                try {
                    Bookmark currentBookmark = (Bookmark)bookmarkList.getSelectedValue();
                    newBookmark = (Bookmark)currentBookmark.clone();
                    newBookmark.setName(getFreeNameVariation(currentBookmark.getName()));
                }
                catch(CloneNotSupportedException ex) { return; }
            }

            bookmarks.add(newBookmark);

            int newBookmarkIndex = bookmarks.size()-1;
            bookmarkList.selectAndScroll(newBookmarkIndex);

            updateComponents();

            nameField.selectAll();
            nameField.requestFocus();
        }
        else if(source==goToButton) {
            // Dispose dialog first
            dispose();
            // Change active panel's folder
            mainFrame.getActiveTable().getFolderPanel().tryChangeCurrentFolder(((Bookmark)bookmarkList.getSelectedValue()).getLocation());
        }
    }


    ///////////////////////////////////
    // ListSelectionListener methods //
    ///////////////////////////////////
	
    public void valueChanged(ListSelectionEvent e) {
        if(e.getValueIsAdjusting())
            return;

        // Reset current bookmark's save
        currentBookmarkSave = null;
//        currentListIndex = bookmarkList.getSelectedIndex();

        // Update components to reflect the new selection
        updateComponents();
    }


    //////////////////////////////
    // DocumentListener methods //
    //////////////////////////////

    public void changedUpdate(DocumentEvent e) {
        modifyBookmark(e.getDocument());
    }

    public void insertUpdate(DocumentEvent e) {
        modifyBookmark(e.getDocument());
    }

    public void removeUpdate(DocumentEvent e) {
        modifyBookmark(e.getDocument());
    }
}
