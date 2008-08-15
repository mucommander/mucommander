package com.mucommander.ui.main;

import java.awt.Dimension;
import java.util.Hashtable;

import javax.swing.JPopupMenu;

import com.mucommander.ui.action.ShowParentFoldersPopupAction;
import com.mucommander.ui.action.ShowRecentlyAccessedLocationsAction;
import com.mucommander.ui.button.PopupButton;

/**
 * This button contains the existing pop ups for the FileTable.
 * 
 * @author Arik Hadas
 */
public class PopupsPopupButton extends PopupButton {
	private FolderPanel folderPanel;
	
	public PopupsPopupButton(FolderPanel panel) {
		folderPanel = panel;
		setPopupMenuLocation(PopupButton.BUTTOM_LEFT_ORIENTED);
	}
	
	public JPopupMenu getPopupMenu() {
		final JPopupMenu popupMenu = new JPopupMenu();
		final MainFrame mainFrame = folderPanel.getMainFrame();
		popupMenu.add(new ShowParentFoldersPopupAction(mainFrame, new Hashtable()));
		popupMenu.add(new ShowRecentlyAccessedLocationsAction(mainFrame, new Hashtable()));
		return popupMenu;
	}
	
	public Dimension getPreferredSize() {
        // Limit button's maximum width to something reasonable and leave enough space for location field, 
        // as bookmarks name can be as long as users want them to be.
        // Note: would be better to use JButton.setMaximumSize() but it doesn't seem to work
        Dimension d = super.getPreferredSize();
        if(d.width > 20)
            d.width = 20;
        d.height = d.width;
        return d;
    }
}
