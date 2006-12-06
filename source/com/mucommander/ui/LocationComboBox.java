
package com.mucommander.ui;

import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.RootFolders;
import com.mucommander.file.FileURL;
import com.mucommander.ui.comp.combobox.EditableComboBox;
import com.mucommander.ui.comp.combobox.EditableComboBoxListener;
import com.mucommander.ui.comp.combobox.SaneComboBox;
import com.mucommander.ui.comp.progress.ProgressTextField;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.Debug;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;


public class LocationComboBox extends EditableComboBox implements LocationListener, EditableComboBoxListener, FocusListener {

    /** FolderPanel this combo box is displayed in */
    private FolderPanel folderPanel;

    /** Text field used to type in a location */
    private ProgressTextField locationField;

    /** Semi-transparent color used to display progress in the location field */
    private final static Color PROGRESS_COLOR = new Color(0, 255, 255, 64);

    /** True while a folder is being changed after a path was entered in the location field and validated by the user */
    private boolean folderChangedInitiatedByLocationField;

    /** Used to save the path that was entered by the user after validation of the location textfield */
    private String locationFieldTextSave;


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


    private void populateParentFolders() {
        // Remove all choices corresponding to previous current folder
        removeAllItems();

        // Add choices corresponding to the new current folder
        AbstractFile folder = folderPanel.getCurrentFolder();
        // Start by adding current folder, and all parent folders up to root
        do {
            addItem(folder);
        }
        while((folder=folder.getParent())!=null);
    }


    /**
     * Re-enable this combo box after a folder change was completed, cancelled by the user or has failed.
     *
     * <p>If the folder change was the result of the user manually entering a path in the location field and the folder
     * change failed or was cancelled, keeps the path intact and request focus on the text field so the user can modify it.
     */
    private void folderChangeCompleted(boolean folderChangedSuccessfully) {
        if(folderChangedSuccessfully || !folderChangedInitiatedByLocationField) {
            // Set the location field's contents to the new current folder's path
            locationField.setText(folderPanel.getCurrentFolder().getAbsolutePath());
        }

        // Re-enable this combo box
        setEnabled(true);

        // If the location was entered and validated in the location field and the folder change failed or was cancelled...
        if(!folderChangedSuccessfully && folderChangedInitiatedByLocationField) {
            // Restore the text that was entered by the user
            locationField.setText(locationFieldTextSave);
            // Select the text to grab user's attention and make it easier to modify
            locationField.selectAll();
            // Request focus (focus was on FileTable)
            locationField.requestFocus();
        }

        // Reset field for next folder change
        folderChangedInitiatedByLocationField = false;
    }


    //////////////////////////////
    // LocationListener methods //
    //////////////////////////////

    public void locationChanging(LocationEvent e) {
        // Change the location field's text to the folder being changed, only if the folder change was not initiated
        // by the location field (to preserve the path entered by the user while the folder is being changed) 
        if(!folderChangedInitiatedByLocationField) {
            FileURL folderURL = e.getFolderURL();

            // Do not display the URL's protocol for local files
            locationField.setText(folderURL.getProtocol().equals("file")?folderURL.getPath():folderURL.getStringRep(false));
        }

        // Disable component until the folder has been changed, cancelled or failed.
        // Note: if the focus currently is in the location field, the focus manager will release focus and give it
        // to the next component (i.e. FileTable)
        setEnabled(false);
    }


    public void locationChanged(LocationEvent e) {
        // Add current folder's parents to combo box
        populateParentFolders();

        // Re-enable component and change the location field's text to the new current folder's path
        folderChangeCompleted(true);
    }

    public void locationCancelled(LocationEvent e) {
        // Re-enable component and change the location field's text to the new current folder's path.
        // If the path was entered in the location field, keep the path to give the user a chance to correct it.
        folderChangeCompleted(false);
    }

    public void locationFailed(LocationEvent e) {
        // Re-enable component and change the location field's text to the new current folder's path.
        // If the path was entered in the location field, keep the path to give the user a chance to correct it.
        folderChangeCompleted(false);
    }

    
    /////////////////////////////////////
    // EditableComboBox implementation //
    /////////////////////////////////////

    public void comboBoxSelectionChanged(SaneComboBox source) {
        AbstractFile folder = (AbstractFile)getSelectedItem();
        if(folder!=null)
            folderPanel.tryChangeCurrentFolder(folder);    
    }

    public void textFieldValidated(EditableComboBox source) {
        String locationText = locationField.getText();

        // Look for a bookmark which name is the entered string (case insensitive)
        Bookmark b = BookmarkManager.getBookmark(locationText);
        if(b!=null) {
            // Change the current folder to the bookmark's location
            folderPanel.tryChangeCurrentFolder(b.getLocation());
            return;
        }

        // Look for a root folder which name is the entered string (case insensitive)
        AbstractFile rootFolders[] = RootFolders.getRootFolders();
        for(int i=0; i<rootFolders.length; i++) {
            if(rootFolders[i].getName().equalsIgnoreCase(locationText)) {
                // Change the current folder to the root folder
                folderPanel.tryChangeCurrentFolder(rootFolders[i]);
                return;
            }
        }

        // Remember that the folder change was initiated by the location field
        folderChangedInitiatedByLocationField = true;
        // Save the path that was entered in case the location change fails or is cancelled 
        locationFieldTextSave = locationText;

        // Change folder
        folderPanel.tryChangeCurrentFolder(locationText);
    }

    public void textFieldCancelled(EditableComboBox source) {
        locationField.setText(folderPanel.getCurrentFolder().getAbsolutePath());
        transferFocus();
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
