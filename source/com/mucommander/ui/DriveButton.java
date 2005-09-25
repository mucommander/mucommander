
package com.mucommander.ui;

import com.mucommander.file.*;

import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.connect.ServerConnectDialog;
import com.mucommander.ui.comp.FocusRequester;

import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.bookmark.Bookmark;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import java.util.Vector;


/**
 * DriveButton is a button which when clicked pops up a list of drives and shortcuts which can be used to change the current folder.
 *
 * @author Maxence Bernard
 */
public class DriveButton extends JButton implements ActionListener, PopupMenuListener, LocationListener {

	private FolderPanel folderPanel;
	
	private JPopupMenu popupMenu;
	private Vector menuItems;

	/* Time when popup menu was last hidden */
	private long lastPopupTime;
	
	/** Root folders array */
	private static AbstractFile rootFolders[] = RootFolders.getRootFolders();
	
	/** Bookmarks, loaded each time the menu pops up */
	private Vector bookmarks;

	private int rootFoldersOffset;
	private int bookmarksOffset;
		
	private final static int POPUP_DELAY = 1000;

	
	/**
	 * Creates a new drive button which is to be added to the given FolderPanel.
	 */
	public DriveButton(FolderPanel folderPanel) {
		this.folderPanel = folderPanel;
		
		// For Mac OS X whose minimum width for buttons is enormous
		setMinimumSize(new Dimension(40, (int)getPreferredSize().getHeight()));
		
		addActionListener(this);
	}


	/**
	 * Creates and add a new item to the popup menu.
	 */
	private void addMenuItem(String text) {
		JMenuItem menuItem = new JMenuItem(text);
		menuItem.addActionListener(this);
		menuItems.add(menuItem);
		popupMenu.add(menuItem);
	}
	

	/**
	 * Updates the button's text to reflect the new specified current folder.
	 */
	public void updateText(AbstractFile folder) {
		// Update button text
		String protocol = folder.getURL().getProtocol();
		// Non local file
		if(!protocol.equals("file")) {
			setText(protocol.toUpperCase());
		}
		// Local file
		else {
			String currentPath = folder.getCanonicalPath(false).toLowerCase();
			int bestLength = -1;
			int bestIndex = 0;
			String temp;
			int len;
			for(int i=0; i<rootFolders.length; i++) {
				temp = rootFolders[i].getCanonicalPath(false).toLowerCase();
				len = temp.length();
				if (currentPath.startsWith(temp) && len>bestLength) {
					bestIndex = i;
					bestLength = len;
				}
			}
			setText(rootFolders[bestIndex].getName());
		}
		
		repaint();
	}
	
	
	/**
	 * Pops up the menu and requests focus on the popup menu.
	 */
	public void popup() {
		// Update root folders in case new drives were mounted
		rootFolders = RootFolders.getRootFolders();

		popupMenu = new JPopupMenu();
		popupMenu.addPopupMenuListener(this);

		menuItems = new Vector();

		// Add root 'drives'
		int nbRoots = rootFolders.length;
		for(int i=0; i<nbRoots; i++)
			addMenuItem(rootFolders[i].toString());

		this.rootFoldersOffset = nbRoots;
		
		// Add 'connect to server' shortcuts
		popupMenu.add(new JSeparator());

		addMenuItem("FTP...");
		addMenuItem("SFTP...");
		addMenuItem("SMB...");
		addMenuItem("HTTP...");

		// Add boookmarks
		popupMenu.add(new JSeparator());

		this.bookmarks = BookmarkManager.getBookmarks();
		this.bookmarksOffset = menuItems.size();
		int nbBookmarks = bookmarks.size();
		for(int i=0; i<nbBookmarks; i++)
			addMenuItem(((Bookmark)bookmarks.elementAt(i)).getName());

		popupMenu.show(folderPanel, 0, getHeight());		

		// Focus MUST NOT be requested on the popup menu because
		// a/ it's not necessary, it grabs focus itself
		// b/ creates a weird bug under windows which prevents enter key from selecting any menu item
//		FocusRequester.requestFocus(popupMenu);
	}

	
	//////////////////////////////
	// LocationListener methods //
	//////////////////////////////
	
	public void locationChanged(FolderPanel folderPanel) {
		// Update button text with new location
		updateText(folderPanel.getCurrentFolder());
	}
	
		
	///////////////////////////////
	// PopupMenuListener methods //
	///////////////////////////////
	 
	public void popupMenuCanceled(PopupMenuEvent e) {
		this.menuItems = null;
	}

	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		this.popupMenu = null;
		this.lastPopupTime = System.currentTimeMillis();
		
		folderPanel.getFileTable().requestFocus();
	}

	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	}
	
	
	////////////////////////////
	// ActionListener methods //
	////////////////////////////
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		// The button was clicked
		if (source == this)	 {
			// Return (do not popup menu) if popup menu was last showing less than POPUP_DELAY ms ago.
			// We need this test because popupMenuWillBecomeInvisible() is called before actionPerformed(),
			// so we interpret the button click as a way to close the popup menu
			if((System.currentTimeMillis()-this.lastPopupTime)<POPUP_DELAY) {
				// Reset time stamp
				this.lastPopupTime = 0;
				return;
			}
			
			// Hide popup menu if it is currently showing
			if(this.popupMenu!=null && this.popupMenu.isVisible()) {
				this.popupMenu.setVisible(false);
				// Reset time stamp
				this.lastPopupTime = 0;
			}
			// Show popup menu
			else
				popup();
		}
		// One of the popup menu items was clicked
		else {
			int index = menuItems.indexOf(source);
			
			// Free vector since it won't be needed anymore
			this.menuItems = null;

			// Menu item that corresponds to a root folder
			if(index<rootFoldersOffset) {
				// Tries to change current folder
				folderPanel.trySetCurrentFolder(rootFolders[index], true);
			}
			// Menu item that corresponds to a shortcut to 'server connect' dialog
			else if(index<bookmarksOffset) {
				// Show server connect dialog with corresponding panel
				new ServerConnectDialog(folderPanel.getMainFrame(), index-rootFoldersOffset).showDialog();
			}
			// Menu item that corresponds to a bookmark
			else {
				folderPanel.trySetCurrentFolder(((Bookmark)bookmarks.elementAt(index-bookmarksOffset)).getURL(), true);
			}
		}
	}


	//////////////////////////////////
	// Overriden JComponent methods //
	//////////////////////////////////
	
	public boolean isFocusTraversable() {
		// Prevents this button from getting keyboard focus
		return false;
	}
}
