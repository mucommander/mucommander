
package com.mucommander.ui;

import com.mucommander.file.*;

import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.connect.ServerConnectDialog;
import com.mucommander.ui.comp.FocusRequester;

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
	
	private int rootsOffset;
	
	private static AbstractFile rootFolders[] = RootFolders.getRootFolders();
	
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

		this.rootsOffset = nbRoots;
		
		// Add 'connect to server' shortcuts
		popupMenu.add(new JSeparator());

		addMenuItem("FTP...");
		addMenuItem("SFTP...");
		addMenuItem("SMB...");
		addMenuItem("HTTP...");

		popupMenu.show(this, 0, getHeight());		
		FocusRequester.requestFocus(popupMenu);
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
//System.out.println("DriveButton.actionPerformed "+this.popupMenu+" "+(this.popupMenu==null?"":""+this.popupMenu.isVisible()));
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

			// Menu item corresponds to a root folder
			if(index<rootsOffset) {
				// Tries to change current folder
				folderPanel.trySetCurrentFolder(rootFolders[index], true);
				// Request focus on this file table
				folderPanel.getFileTable().requestFocus();
			}
			// Menu item corresponds to a shortcut to 'server connect' dialog
			else {
				// Show server connect dialog with corresponding panel
				new ServerConnectDialog(folderPanel.getMainFrame(), index-rootsOffset).showDialog();
			}
		}
	}


	//////////////////////////////////
	// Overriden JComponent methods //
	//////////////////////////////////
	
	public boolean isFocusTraversable() {
		// This button will not get keyboard focus
		return false;
	}
}
