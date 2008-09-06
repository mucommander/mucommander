package com.mucommander.ui.main;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MuAction;
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
	private JPopupMenu popupMenu;
	
	public PopupsPopupButton(FolderPanel panel) {
		folderPanel = panel;
		setPopupMenuLocation(PopupButton.BUTTOM_LEFT_ORIENTED);
		
		popupMenu = new JPopupMenu();
		final MainFrame mainFrame = folderPanel.getMainFrame();
		// Add items to popupMenu:
		JMenuItem item;
		// add item for ShowParentFoldersPopupAction.
		item = new JMenuItem(MuAction.getStandardLabel(ShowParentFoldersPopupAction.class));
		item.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				ActionManager.performAction(ShowParentFoldersPopupAction.class, mainFrame);
			}});
		popupMenu.add(item);
		// add item for ShowRecentlyAccessedLocationsAction.
		item = new JMenuItem(MuAction.getStandardLabel(ShowRecentlyAccessedLocationsAction.class));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ActionManager.performAction(ShowRecentlyAccessedLocationsAction.class, mainFrame);
			}
		});
		popupMenu.add(item);
	}
	
	public JPopupMenu getPopupMenu() {
		folderPanel.getFileTable().requestFocus();
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
