
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
	private int rootsOffset;
	
	private static AbstractFile rootFolders[];

	
	static {
		rootFolders = RootFolders.getRootFolders();
	}

	
	/**
	 * Creates a new drive button which is to be added to the given FolderPanel.
	 */
	public DriveButton(FolderPanel folderPanel) {
		super(rootFolders[0].toString());

		this.folderPanel = folderPanel;
		
		// For Mac OS X whose minimum width for buttons is enormous
		setMinimumSize(new Dimension(40, (int)getPreferredSize().getHeight()));
//		setMargin(new Insets(6,8,6,8));
		
		addActionListener(this);
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
		addMenuItem("SMB...");
		addMenuItem("HTTP...");
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
	private void updateText(AbstractFile folder) {
		// Update button text
		if(folder instanceof RemoteFile) {
			setText(((RemoteFile)folder).getProtocol());
		}
		// FSFile
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
	}
	
	
	/**
	 * Pops up the menu and requests focus on the popup menu.
	 */
	public void popup() {
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
	}

	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		folderPanel.getFileTable().requestFocus();
	}

	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	}
	
	
	////////////////////////////
	// ActionListener methods //
	////////////////////////////
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source == this)	 {
			popup();
		}
		// root menu items
		else {
			int index = menuItems.indexOf(source);
			
			// Hide popup
			popupMenu.setVisible(false);

			// Menu item corresponds to a root folder
			if(index<rootsOffset) {
				// Tries to change current folder
				folderPanel.setCurrentFolder(rootFolders[index], true);
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
