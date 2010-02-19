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

package com.mucommander.ui.main;

import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileProtocols;
import com.mucommander.file.FileURL;
import com.mucommander.file.impl.local.LocalFile;
import com.mucommander.file.util.PathUtils;
import com.mucommander.runtime.OsFamilies;
import com.mucommander.ui.autocomplete.AutocompleterTextComponent;
import com.mucommander.ui.autocomplete.CompleterFactory;
import com.mucommander.ui.autocomplete.TextFieldCompletion;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.progress.ProgressTextField;
import com.mucommander.ui.theme.*;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Maxence Bernard, Arik Hadas
 */

public class LocationTextField extends ProgressTextField implements LocationListener, FocusListener, ThemeListener {
	/** FolderPanel this text field is displayed in */
    private FolderPanel folderPanel;

    /** True while a folder is being changed after a path was entered in the location field and validated by the user */
    private boolean folderChangeInitiatedByLocationField;

    /** Used to save the path that was entered by the user after validation of the location textfield */
    private String locationFieldTextSave;

    /** For windows path, regex that finds trailing space characters at the end of a path */
    private static Pattern windowsTrailingSpacePattern;

    static {
        if(OsFamilies.WINDOWS.isCurrent())
            windowsTrailingSpacePattern = Pattern.compile("[ ]+[\\\\]*$");
    }


    /**
     * Creates a new LocationTextField for use in the given FolderPanel.
     *
     * @param folderPanel FolderPanel this text field is displayed in
     */
    public LocationTextField(FolderPanel folderPanel) {
        // Use a custom text field that can display loading progress when changing folders
        super(0, ThemeManager.getCurrentColor(Theme.LOCATION_BAR_PROGRESS_COLOR));

        this.folderPanel = folderPanel;

    	// Applies theme values.
        setFont(ThemeManager.getCurrentFont(Theme.LOCATION_BAR_FONT));
        setDisabledTextColor(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_FOREGROUND_COLOR));
        setForeground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_FOREGROUND_COLOR));
        setBackground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_BACKGROUND_COLOR));
        setSelectedTextColor(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_SELECTED_FOREGROUND_COLOR));
        setSelectionColor(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_SELECTED_BACKGROUND_COLOR));

        // Listen to location changes to update popup menu choices and disable this component while the location is
        // being changed
        folderPanel.getLocationManager().addLocationListener(this);

        // Listen to focus events to temporarily disable the MainFrame's JMenuBar when this component has the keyboard focus.
        // Not doing so would trigger unwanted menu bar actions when typing.
        addFocusListener(this);
        
        enableAutoCompletion();
        
        ThemeManager.addCurrentThemeListener(this);
    }
    
    /**
     * Adds auto-completion capabilities to this text field.
     */
    private void enableAutoCompletion() {
    	new TextFieldCompletion(new AutocompleterTextComponent(this) {

			@Override
            public void OnEnterPressed(KeyEvent keyEvent) {
				if (textFieldValidated()) // if a malformed url was entered.
					folderChangeCompleted(false);		        
				
				// /!\ Consume the event so to prevent JTextField from firing an ActionEvent
				keyEvent.consume();
			}

			@Override
            public void OnEscPressed(KeyEvent keyEvent) {
				textFieldCancelled();
			}
        }, CompleterFactory.getLocationCompleter());
    }

    /**
     * Re-enable this text field after a folder change was completed, cancelled by the user or has failed.
     *
     * <p>If the folder change was the result of the user manually entering a path in the location field and the folder
     * change failed or was cancelled, keeps the path intact and request focus on the text field so the user can modify it.
     */
    private void folderChangeCompleted(boolean folderChangedSuccessfully) {
        if(folderChangedSuccessfully || !folderChangeInitiatedByLocationField) {
            // Set the location field's contents to the new current folder's path
            setText(folderPanel.getCurrentFolder().getAbsolutePath());
        }

        // Re-enable this text field
        setEnabled(true);

        // If the location was entered and validated in the location field and the folder change failed or was cancelled...
        if(!folderChangedSuccessfully && folderChangeInitiatedByLocationField) {
            // Restore the text that was entered by the user
            setText(locationFieldTextSave);
            // Select the text to grab user's attention and make it easier to modify
            selectAll();
            // Request focus (focus was on FileTable)
            requestFocus();
        }

        // Reset field for next folder change
        folderChangeInitiatedByLocationField = false;
    }


    //////////////////////////////
    // LocationListener methods //
    //////////////////////////////

    public void locationChanging(LocationEvent e) {
        // Change the location field's text to the folder being changed, only if the folder change was not initiated
        // by the location field (to preserve the path entered by the user while the folder is being changed) 
        if(!folderChangeInitiatedByLocationField) {
            FileURL folderURL = e.getFolderURL();

            String locationText;
            // Do not display the URL's scheme for local files
            if(folderURL.getScheme().equals(FileProtocols.FILE)) {
                locationText = folderURL.getPath();
                // Under for OSes with 'root drives' (Windows, OS/2), remove the leading '/' character
                if(LocalFile.hasRootDrives())
                    locationText = PathUtils.removeLeadingSeparator(locationText, "/");
            }
            // Display the full URL for protocols other than 'file'
            else {
                locationText = folderURL.toString(false);
            }
            setText(locationText);
        }

        // Disable component until the folder has been changed, cancelled or failed.
        // Note: if the focus currently is in the location field, the focus manager will release focus and give it
        // to the next component (i.e. FileTable)
        setEnabled(false);
    }


    public void locationChanged(LocationEvent e) {
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

    /**
     * 
     * @return true if a malformed url was entered, false otherwise.
     */
    public boolean textFieldValidated() {
        String location = getText();

        // Under Windows, trim the entered path for the following reason.
        // If a file 'A' (e.g. "C:\temp") exists and 'A ' (e.g. "C:\temp ") is requested, the java.io.File will resolve
        // (file.exists() will  return true), but this file will be a strange one, listing bogus children files with
        // weird attributes (in the case of a directory).
        // Windows (or java.io.File under Windows) is somehow space-tolerant but then unable to deal with
        // those files properly. So if a path ends with space characters, we remove them to prevent those weirdnesses.
        // Note that Win32 doesn't allow creating files with trailing spaces (in Explorer, command prompt...), but
        // those files can still be manually crafted and thus exist on one's hard drive.
        // Mucommander should in theory be able to access such files without any problem but this hasn't been tested.
        if(OsFamilies.WINDOWS.isCurrent() && location.indexOf(":\\")==1) {
            // Looks for trailing spaces and if some 
            Matcher matcher = windowsTrailingSpacePattern.matcher(location);
            if(matcher.find())
                location = location.substring(0, matcher.start());
        }
                
        // Save the path that was entered in case the location change fails or is cancelled 
        locationFieldTextSave = location;
        
        // Indicate we search for location corresponding to the given string. 
        // it will be false we'll find one.
        boolean tryToInterpretEnteredString = true;

        // Look for a bookmark which name is the entered string (case insensitive)
        Bookmark b = BookmarkManager.getBookmark(location);
        if(b!=null) {
        	// Change the current folder to the bookmark's location
        	setText(location = b.getLocation());
        	tryToInterpretEnteredString = false;
        }

        // Look for a volume whose name is the entered string (case insensitive)
        AbstractFile volumes[] = LocalFile.getVolumes();
        for(int i=0; tryToInterpretEnteredString && i<volumes.length; i++) {
            if(volumes[i].getName().equalsIgnoreCase(location)) {
                // Change the current folder to the volume folder
            	setText(location = volumes[i].getAbsolutePath());
            	tryToInterpretEnteredString = false;
            }
        }

        // Todo: replace this by env:// filesystem ?
        // Look for a system variable which name is the entered string (case insensitive)
        if (tryToInterpretEnteredString && location.startsWith("$")) {
        	String variableKey = location.substring(1);
        	String variableValue = System.getenv(variableKey);
        	if (variableValue != null)
        		setText(location = variableValue);        	
        }        
        
        // Remember that the folder change was initiated by the location field
        folderChangeInitiatedByLocationField = true;
        
        // Change folder
        return folderPanel.tryChangeCurrentFolder(location) == null;
    }

    public void textFieldCancelled() {
        setText(folderPanel.getCurrentFolder().getAbsolutePath());
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
//    	// If we are not in the middle of a folder change, and focus has been
//    	// lost then ensure location field's text is set to the current directory.
//    	if (!folderPanel.isFolderChanging())
//    		locationField.setText(folderPanel.getCurrentFolder().getAbsolutePath());
    	
        // Enable menu bar when this component has lost focus
        folderPanel.getMainFrame().getJMenuBar().setEnabled(true);
    }



    // - Theme listening -------------------------------------------------------------
    // -------------------------------------------------------------------------------
    /**
     * Receives theme color changes notifications.
     */
    public void colorChanged(ColorChangedEvent event) {
        switch(event.getColorId()) {
        case Theme.LOCATION_BAR_PROGRESS_COLOR:
            setProgressColor(event.getColor());
            break;

        case Theme.LOCATION_BAR_FOREGROUND_COLOR:
            setDisabledTextColor(event.getColor());
            setForeground(event.getColor());
            break;

        case Theme.LOCATION_BAR_BACKGROUND_COLOR:
            setBackground(event.getColor());
            break;

        case Theme.LOCATION_BAR_SELECTED_FOREGROUND_COLOR:
        	setSelectedTextColor(event.getColor());
            break;

        case Theme.LOCATION_BAR_SELECTED_BACKGROUND_COLOR:
        	setSelectionColor(event.getColor());
            break;
        }
    }

    /**
     * Receives theme font changes notifications.
     */
    public void fontChanged(FontChangedEvent event) {
        if(event.getFontId() == Theme.LOCATION_BAR_FONT)
            setFont(event.getFont());
    }
}
