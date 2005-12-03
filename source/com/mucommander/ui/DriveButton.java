
package com.mucommander.ui;

import com.mucommander.file.*;

import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.connect.ServerConnectDialog;
import com.mucommander.ui.comp.FocusRequester;

import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.bookmark.Bookmark;

import com.mucommander.text.Translator;

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

	public Dimension getPreferredSize() {
		// Limit button's maximum width to something reasonable and leave enough space for location field, as bookmarks name can be as long as users want them to be
		// Note: would be better to use JButton.setMaximumSize() but it doesn't seem to work
		Dimension d = super.getPreferredSize();
		if(d.width > 160)
			d.width = 160;
		return d;
	}


	/**
	 * Creates and add a new item to the popup menu.
	 */
	private JMenuItem addMenuItem(String text) {
		JMenuItem menuItem = new JMenuItem(text);
		menuItem.addActionListener(this);
		menuItems.add(menuItem);
		popupMenu.add(menuItem);
		return menuItem;
	}
	

	/**
	 * Updates this drive button's label with the specified new current folder to match one of the drive button's shortcuts.
	 * <<ul>
	 *	<li>If the specified folder corresponds to a bookmark, the bookmark's name will be displayed
	 *	<li>If the specified folder corresponds to a local file, the enclosing volume's name will be displayed
	 *	<li>If the specified folder corresponds to a remote file, the protocol's name will be displayed
	 * </ul>
	 */
	private void updateLabel(AbstractFile folder) {
		String newLabel = null;
		
		// First tries to find a bookmark matching the specified folder
		Vector bookmarks = BookmarkManager.getBookmarks();
		FileURL currentURL = folder.getURL();
		int nbBookmarks = bookmarks.size();
		Bookmark b;
		for(int i=0; i<nbBookmarks; i++) {
			b = (Bookmark)bookmarks.elementAt(i);
			if(b.getURL().equals(currentURL)) {
				// Note: if several bookmarks match current folder, the first one will be used
				newLabel = b.getName();
				break;
			}
		}
		
		// If no bookmark matched current folder
		if(newLabel == null) {
			String protocol = folder.getURL().getProtocol();
			// Remote file, use protocol's name
			if(!protocol.equals("file")) {
				newLabel = protocol.toUpperCase();
			}
			// Local file, use enclosing volume's name 
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
				newLabel = rootFolders[bestIndex].getName();
			}
		}
		
		setText(newLabel);
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
		
		if(nbBookmarks>0) {
			for(int i=0; i<nbBookmarks; i++)
				addMenuItem(((Bookmark)bookmarks.elementAt(i)).getName());
		}
		else {
			addMenuItem(Translator.get("bookmarks_menu.no_bookmark")).setEnabled(false);
		}

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
		updateLabel(folderPanel.getCurrentFolder());
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
