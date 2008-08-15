package com.mucommander.ui.action;

import java.util.Hashtable;

import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

/**
 * This action shows RecentlyAccessedLocationsPopup on the current active FileTable.
 * 
 * @author Arik Hadas
 */
public class ShowRecentlyAccessedLocationsAction extends ShowFileTablePopupAction {
	
	public ShowRecentlyAccessedLocationsAction(MainFrame mainFrame, Hashtable properties) {
		super(mainFrame, properties);
	}
	
	public void performAction() {
		openPopup(FolderPanel.RECENTLY_ACCESSED_LOCATIONS_POPUP_INDEX);
	}
}
