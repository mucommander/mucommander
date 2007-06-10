
package com.mucommander.ui;

import com.mucommander.PlatformManager;
import com.mucommander.bonjour.BonjourMenu;
import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkListener;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileProtocols;
import com.mucommander.file.FileURL;
import com.mucommander.file.RootFolders;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.OpenLocationAction;
import com.mucommander.ui.comp.button.PopupButton;
import com.mucommander.ui.connect.ServerConnectDialog;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Hashtable;
import java.util.Vector;


/**
 * DrivePopupButton is a button which when clicked pops up a menu with a list of drives and shortcuts which can be used
 * to change the current folder.
 *
 * @author Maxence Bernard
 */
public class DrivePopupButton extends PopupButton implements LocationListener, BookmarkListener {

    /** FolderPanel instance that contains this button */
    private FolderPanel folderPanel;
	
    /** Root folders array */
    private static AbstractFile rootFolders[] = RootFolders.getRootFolders();
	

    /**
     * Action that triggers the 'server connection dialog' for a specified protocol.
     */
    private class ServerConnectAction extends AbstractAction {
        private int serverPanelIndex;

        private ServerConnectAction(String label, int serverPanelIndex) {
            super(label);
            this.serverPanelIndex = serverPanelIndex;
        }

        public void actionPerformed(ActionEvent actionEvent) {
            new ServerConnectDialog(folderPanel.getMainFrame(), serverPanelIndex).showDialog();
        }
    }

	
    /**
     * Creates a new drive button which is to be added to the given FolderPanel.
     */
    public DrivePopupButton(FolderPanel folderPanel) {
        this.folderPanel = folderPanel;
		
        // Listen to location events to update drive button when folder changes
        folderPanel.getLocationManager().addLocationListener(this);

        // Listen to bookmark changes to update drive button if a bookmark corresponding
        // to the current folder has been added/edited/removed
        BookmarkManager.addBookmarkListener(this);
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
            if(!protocol.equals(FileProtocols.FILE)) {
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
                    // getCanonicalPath() must be avoided under Windows for the following reasons:
                    // a) it is not necessary, Windows doesn't have symlinks
                    // b) it triggers the dreaded 'No disk in drive' error popup dialog.
                    // c) when network drives are present but not mounted (e.g. X:\ mapped onto an SMB share),
                    // getCanonicalPath which is I/O bound will take a looooong time to execute

                    if(PlatformManager.isWindowsFamily())
                        currentPath = currentFolder.getAbsolutePath(false).toLowerCase();
                    else
                        currentPath = currentFolder.getCanonicalPath(false).toLowerCase();

                    int bestLength = -1;
                    int bestIndex = 0;
                    String temp;
                    int len;
                    for(int i=0; i<rootFolders.length; i++) {
                        if(PlatformManager.isWindowsFamily())
                            temp = rootFolders[i].getAbsolutePath(false).toLowerCase();
                        else
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


    ////////////////////////////////////
    // PopupButton implementation //
    ////////////////////////////////////

    public JPopupMenu getPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        // Update root folders in case new drives were mounted
        rootFolders = RootFolders.getRootFolders();

        // Add root 'drives'
        int nbRoots = rootFolders.length;
        MainFrame mainFrame = folderPanel.getMainFrame();

// Drive icons are disabled under Windows as well because they seem to trigger the dreaded 'No disk in drive' error popup dialog.

//        // Retreive and use system drives icons under Windows only, icons looks like crap under Mac OS X,
//        // and most likely also look like crap under Linux (untested though).
//        // FileSystemView.getSystemIcon is available from Java 1.4 and up.
//        // However, Java 1.4 freaks out when it tries to get the a:\ system icon
//        if(PlatformManager.isWindowsFamily() && PlatformManager.JAVA_VERSION>=PlatformManager.JAVA_1_5) {
//            FileSystemView fileSystemView = FileSystemView.getFileSystemView();
//            for(int i=0; i<nbRoots; i++) {
//                popupMenu.add(new OpenLocationAction(mainFrame, new Hashtable(), rootFolders[i])).setIcon(fileSystemView.getSystemIcon(new java.io.File(rootFolders[i].getAbsolutePath())));
//            }
//        }
//        // For any OS other than Windows or for Windows and Java 1.4
//        else {
            for(int i=0; i<nbRoots; i++)
                popupMenu.add(new OpenLocationAction(mainFrame, new Hashtable(), rootFolders[i]));
//        }

        popupMenu.add(new JSeparator());

        // Add boookmarks
        Vector bookmarks = BookmarkManager.getBookmarks();
        int nbBookmarks = bookmarks.size();

        if(nbBookmarks>0) {
            for(int i=0; i<nbBookmarks; i++)
                popupMenu.add(new OpenLocationAction(mainFrame, new Hashtable(), (Bookmark)bookmarks.elementAt(i)));
        }
        else {
            // No bookmark : add a disabled menu item saying there is no bookmark
            popupMenu.add(Translator.get("bookmarks_menu.no_bookmark")).setEnabled(false);
        }

        popupMenu.add(new JSeparator());

        // Add Bonjour services menu
        popupMenu.add(new BonjourMenu(folderPanel.getMainFrame()));
        popupMenu.add(new JSeparator());

        // Add 'connect to server' shortcuts
        popupMenu.add(new ServerConnectAction("SMB...", ServerConnectDialog.SMB_INDEX));
        popupMenu.add(new ServerConnectAction("FTP...", ServerConnectDialog.FTP_INDEX));
        popupMenu.add(new ServerConnectAction("SFTP...", ServerConnectDialog.SFTP_INDEX));
        popupMenu.add(new ServerConnectAction("HTTP...", ServerConnectDialog.HTTP_INDEX));
        popupMenu.add(new ServerConnectAction("NFS...", ServerConnectDialog.NFS_INDEX));

        // Temporarily make the FileTable which contains this DrivePopupButton the currently active one so that menu actions
        // are triggered on it. The previously active table will be restored when the popup menu is closed (focus is lost).
        // This needed because a DrivePopupButton menu can be poped up from the opposite table.
        // Not doing this would cause the action to be performed from the wrong FileTable.
        mainFrame.setActiveTable(folderPanel.getFileTable());

        return popupMenu;
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

    
    //////////////////////////////
    // BookmarkListener methods //
    //////////////////////////////
	
    public void bookmarksChanged() {
        // Refresh label in case a bookmark with the current location was changed
        updateLabel();
    }
}
