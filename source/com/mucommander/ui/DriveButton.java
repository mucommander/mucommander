
package com.mucommander.ui;

import com.mucommander.PlatformManager;
import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkListener;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileURL;
import com.mucommander.file.RootFolders;
import com.mucommander.text.Translator;
import com.mucommander.ui.connect.ServerConnectDialog;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


/**
 * DriveButton is a button which when clicked pops up a list of drives and shortcuts which can be used to change the current folder.
 *
 * @author Maxence Bernard
 */
public class DriveButton extends JButton implements ActionListener, PopupMenuListener, LocationListener, BookmarkListener {

    private FolderPanel folderPanel;
	
    private JPopupMenu popupMenu;
    private Vector menuItems;

    /* Time when popup menu was last hidden */
    private long lastPopupTime;
	
    /** Root folders array */
    private static AbstractFile rootFolders[] = RootFolders.getRootFolders();
	
    /** Bookmarks, loaded each time the menu pops up */
    private Vector bookmarks;

    /** Index of the first bookmark in the popup menu */
    private int bookmarksOffset;
    /** Index of the first server shorcut in the popup menu */
    private int serverShortcutsOffset;

    private final static int POPUP_DELAY = 1000;

	
    /**
     * Creates a new drive button which is to be added to the given FolderPanel.
     */
    public DriveButton(FolderPanel folderPanel) {
        this.folderPanel = folderPanel;
		
        // Listen to location events to update drive button when folder changes
        folderPanel.getLocationManager().addLocationListener(this);

        // Listen to bookmark changes to update drive button if a bookmark corresponding
        // to the current folder has been added/edited/removed
        BookmarkManager.addBookmarkListener(this);
		
        // Listen to action events to popup a list of drives when button is pressed
        addActionListener(this);
    }

    public Dimension getPreferredSize() {
        // Limit button's maximum width to something reasonable and leave enough space for location field, 
        // as bookmarks name can be as long as users want them to be.
        // Note: would be better to use JButton.setMaximumSize() but it doesn't seem to work
        Dimension d = super.getPreferredSize();
        if(d.width > 160)
            d.width = 160;
        return d;
    }


    /**
     * Creates and add a new menu item to the popup menu.
     *
     * @param label the menu item's label
     */
    private JMenuItem addMenuItem(String label) {
        return addMenuItem(label, null);
    }

    /**
     * Creates and add a new menu item to the popup menu.
     *
     * @param label the menu item's label
     * @param icon the menu item's icon (can be null)
     */
    private JMenuItem addMenuItem(String label, javax.swing.Icon icon) {
        JMenuItem menuItem = new JMenuItem(label, icon);
        menuItem.addActionListener(this);
        menuItems.add(menuItem);
        popupMenu.add(menuItem);
        return menuItem;
    }
	

    /**
     * Updates this drive button's label to reflect current folder and match one of the drive button's shortcuts.
     * <<ul>
     *	<li>If the specified folder corresponds to a bookmark, the bookmark's name will be displayed
     *	<li>If the specified folder corresponds to a local file, the enclosing volume's name will be displayed
     *	<li>If the specified folder corresponds to a remote file, the protocol's name will be displayed
     * </ul>
     */
    private void updateLabel() {
        AbstractFile currentFolder = folderPanel.getCurrentFolder();
        String currentPath = currentFolder.getAbsolutePath();
        FileURL currentURL = currentFolder.getURL();

        String newLabel = null;
		
        // First tries to find a bookmark matching the specified folder
        Vector bookmarks = BookmarkManager.getBookmarks();
        int nbBookmarks = bookmarks.size();
        Bookmark b;
        for(int i=0; i<nbBookmarks; i++) {
            b = (Bookmark)bookmarks.elementAt(i);
//            if(b.getURL().equals(currentURL)) {
            if(currentPath.equals(b.getLocation())) {
                // Note: if several bookmarks match current folder, the first one will be used
                newLabel = b.getName();
                break;
            }
        }
		
        // If no bookmark matched current folder
        if(newLabel == null) {
            String protocol = currentURL.getProtocol();
            // Remote file, use protocol's name
            if(!protocol.equals("file")) {
                newLabel = protocol.toUpperCase();
            }
            // Local file, use volume's name 
            else {
                // Patch for Windows UNC network paths (weakly characterized by having a host different from 'localhost'):
                // display 'SMB' which is the underlying protocol
                if(PlatformManager.isWindowsFamily() && !FileURL.LOCALHOST.equals(currentURL.getHost())) {
                    newLabel = "SMB";
                }
                else {
                    currentPath = currentFolder.getCanonicalPath(false).toLowerCase();
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
        // Retreive and use system drives icons under Windows only, icons looks like crap under Mac OS X,
        // and most likely also look like crap under Linux (untested though)
        if(PlatformManager.isWindowsFamily()) {
            FileSystemView fileSystemView = FileSystemView.getFileSystemView();
            for(int i=0; i<nbRoots; i++) {
                Icon driveIcon = null;
                // FileSystemView.getSystemIcon is only available in Java 1.4 and up
                if(PlatformManager.JAVA_VERSION>=PlatformManager.JAVA_1_4)
                    driveIcon = fileSystemView.getSystemIcon(new java.io.File(rootFolders[i].getAbsolutePath()));

                addMenuItem(rootFolders[i].getName(), driveIcon);
            }
        }
        // For any OS other than Windows
        else {
            for(int i=0; i<nbRoots; i++)
                addMenuItem(rootFolders[i].getName());
        }

        popupMenu.add(new JSeparator());

        // Add boookmarks
		
        this.bookmarksOffset = menuItems.size();
		
        this.bookmarks = BookmarkManager.getBookmarks();
        int nbBookmarks = bookmarks.size();
		
        if(nbBookmarks>0) {
            for(int i=0; i<nbBookmarks; i++)
                addMenuItem(((Bookmark)bookmarks.elementAt(i)).getName());
        }
        else {
            // No bookmark : add a disabled menu item saying there is no bookmark
            addMenuItem(Translator.get("bookmarks_menu.no_bookmark")).setEnabled(false);
        }

        popupMenu.add(new JSeparator());

        // Add 'connect to server' shortcuts

        this.serverShortcutsOffset = menuItems.size();
        addMenuItem("SMB...");
        addMenuItem("FTP...");
        addMenuItem("SFTP...");
        addMenuItem("HTTP...");

        // Popup up the menu underneath under this button
        popupMenu.show(this, 0, getHeight());

        // Focus MUST NOT be requested on the popup menu because
        // a/ it's not necessary, it grabs focus itself
        // b/ creates a weird bug under windows which prevents enter key from selecting any menu item
        //		FocusRequester.requestFocus(popupMenu);
    }



    //////////////////////////////////
    // Overriden JComponent methods //
    //////////////////////////////////
	
    public boolean isFocusTraversable() {
        // Prevents this button from getting keyboard focus
        return false;
    }

	
    //////////////////////////////
    // LocationListener methods //
    //////////////////////////////
	
    public void locationChanged(LocationEvent e) {
        // Update the button's label to reflect the new current folder
        updateLabel();
    }
	
    public void locationChanging(LocationEvent e) {
    }
	
    public void locationCancelled(LocationEvent e) {
    }

    public void locationFailed(LocationEvent e) {
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
	

    //////////////////////////////
    // BookmarkListener methods //
    //////////////////////////////
	
    public void bookmarksChanged() {
////         If a bookmark has been added/edited/removed, check if the bookmark's location corresponds
////         to the current folder's. If it so, update the button's label to reflect the new current folder
////        if(folderPanel.getCurrentFolder().getURL().equals(b.getURL())) {
//        if(folderPanel.getCurrentFolder().getAbsolutePath().equals(b.getLocation())) {
//            updateLabel();
//        }

        // Refresh label in case a bookmark with the current location was changed
        updateLabel();
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
			
            // GC vector since it won't be needed anymore
            this.menuItems = null;

            // Menu item that corresponds to a root folder
            if(index<bookmarksOffset) {
                // Tries to change current folder
                folderPanel.trySetCurrentFolder(rootFolders[index].getCanonicalPath());
            }
            // Menu item that corresponds to a bookmark
            else if(index<serverShortcutsOffset) {
//                folderPanel.trySetCurrentFolder(((Bookmark)bookmarks.elementAt(index-bookmarksOffset)).getURL());
                folderPanel.trySetCurrentFolder(((Bookmark)bookmarks.elementAt(index-bookmarksOffset)).getLocation());
            }
            // Menu item that corresponds to a server shortcut
            else {
                // Show server connect dialog with corresponding panel
                new ServerConnectDialog(folderPanel.getMainFrame(), index-serverShortcutsOffset).showDialog();
            }
        }
    }
}
