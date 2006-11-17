
package com.mucommander.ui;

import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.RootFolders;
import com.mucommander.ui.comp.combobox.EditableComboBox;
import com.mucommander.ui.comp.combobox.EditableComboBoxListener;
import com.mucommander.ui.comp.progress.ProgressTextField;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;


public class LocationComboBox extends EditableComboBox implements LocationListener, EditableComboBoxListener, FocusListener {

    /** FolderPanel this combo box is displayed in */
    private FolderPanel folderPanel;
    /** Text field used to type in a location */
    private ProgressTextField locationField;

    /** When true, any action or key events received will be ignored */
    private boolean ignoreEvents = true;    // Events are ignored until location is changed for the first time

    /** Semi-transparent color used to display progress in the location field */
    private final static Color PROGRESS_COLOR = new Color(0, 255, 255, 64);


    /**
     * Creates a new LocationComboBox for use in the given FolderPanel.
     *
     * @param folderPanel FolderPanel this combo box is displayed in
     */
    public LocationComboBox(FolderPanel folderPanel) {
        // Use a custom text field that can display loading progress when changing folders
        super(new ProgressTextField(0, PROGRESS_COLOR));

        this.folderPanel = folderPanel;
        this.locationField = (ProgressTextField)getTextField();
        locationField.setComboBox(this);

        // Automatically update the text field's contents when an item is selected in this combo box
        setComboSelectionUpdatesTextField(true);

        // Listener to actions fired by this EditableComboBox
        addEditableComboBoxListener(this);

        // Listen to location changes to update popup menu choices and disable this component while the location is
        // being changed
        folderPanel.getLocationManager().addLocationListener(this);

        // Listen to focus events to temporarily disable the MainFrame's JMenuBar when this component has the keyboard focus.
        // Not doing so would trigger unwanted menu bar actions when typing.
        locationField.addFocusListener(this);
        addFocusListener(this);
    }


    //////////////////////////////
    // LocationListener methods //
    //////////////////////////////

    public void locationChanged(LocationEvent e) {
        // Remove all choices corresponding to previous current folder
        removeAllItems();

        // Add choices corresponding to the new current folder
        // /!\ Important note: combo box seems to fire action events when items
        // are added so it's necessary to ignore events while items are being added
        AbstractFile folder = e.getFolderPanel().getCurrentFolder();
        // Start by adding current folder, and all parent folders up to root
        do {
            addItem(folder);
        }
        while((folder=folder.getParent())!=null);

        // Re-enable component and stop ignoring events
        setEnabled(true);
    }

    public void locationChanging(LocationEvent e) {
        // Disable component and ignore events until folder has been changed (or cancelled)
        // Note: setEnabled(false) will have already been called if folder was changed by this component 
        if(isEnabled())
            setEnabled(false);
    }

    public void locationCancelled(LocationEvent e) {
        // Re-enable component and stop ignoring events
        setEnabled(true);
    }


    /////////////////////////////////////
    // EditableComboBox implementation //
    /////////////////////////////////////

    public void comboBoxSelectionChanged(EditableComboBox source) {
    }

    public void textFieldValidated(EditableComboBox source) {
        // Disable component and ignore events until folder has been changed (or cancelled)
        setEnabled(false);

        String locationText = locationField.getText();

        // Look for a bookmark which name is the entered string (case insensitive)
        Bookmark b = BookmarkManager.getBookmark(locationText);
        if(b!=null) {
            // Change the current folder to the bookmark's location
            folderPanel.trySetCurrentFolder(b.getLocation());
            return;
        }

        // Look for a root folder which name is the entered string (case insensitive)
        AbstractFile rootFolders[] = RootFolders.getRootFolders();
        for(int i=0; i<rootFolders.length; i++) {
            if(rootFolders[i].getName().equalsIgnoreCase(locationText)) {
                // Change the current folder to the root folder
                folderPanel.trySetCurrentFolder(rootFolders[i]);
                return;
            }
        }

        // Change folder
        folderPanel.trySetCurrentFolder(locationText);
    }

    public void textFieldCancelled(EditableComboBox source) {
        locationField.setText(folderPanel.getCurrentFolder().getAbsolutePath());
        folderPanel.getFileTable().requestFocus();
    }


    ///////////////////////////
    // FocusListener methods //
    ///////////////////////////

    public void focusGained(FocusEvent e) {
        // Disable menu bar when this component has gained focus
        folderPanel.getMainFrame().getJMenuBar().setEnabled(false);
    }

    public void focusLost(FocusEvent e) {
        // Enable menu bar when this component has lost focus
        folderPanel.getMainFrame().getJMenuBar().setEnabled(true);
    }



}
