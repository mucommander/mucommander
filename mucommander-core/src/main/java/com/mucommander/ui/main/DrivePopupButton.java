/*
 * This file is part of muCommander, http://www.mucommander.com
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

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.bonjour.BonjourMenu;
import com.mucommander.bonjour.BonjourService;
import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkListener;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.bookmark.file.BookmarkProtocolProvider;
import com.mucommander.commons.conf.ConfigurationEvent;
import com.mucommander.commons.conf.ConfigurationListener;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.filter.PathFilter;
import com.mucommander.commons.file.filter.RegexpPathFilter;
import com.mucommander.commons.file.protocol.FileProtocols;
import com.mucommander.commons.file.protocol.local.LocalFile;
import com.mucommander.commons.file.protocol.search.SearchFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.util.ui.helper.MnemonicHelper;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.protocol.ui.ProtocolPanelProvider;
import com.mucommander.protocol.ui.ServerPanel;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.impl.OpenLocationAction;
import com.mucommander.ui.button.PopupButton;
import com.mucommander.ui.dialog.server.ServerConnectDialog;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.icon.CustomFileIconProvider;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.icon.IconManager;

/**
 * <code>DrivePopupButton</code> is a button which, when clicked, pops up a menu with a list of volumes items that be used
 * to change the current folder.
 *
 * @author Maxence Bernard
 */
public class DrivePopupButton extends PopupButton implements BookmarkListener, ConfigurationListener, LocationListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(DrivePopupButton.class);
	
    /** FolderPanel instance that contains this button */
    private FolderPanel folderPanel;
	
    /** Current volumes */
    private static AbstractFile volumes[];

    /** static FileSystemView instance, has a (non-null) value only under Windows */
    private static FileSystemView fileSystemView;

    /** Caches extended drive names, has a (non-null) value only under Windows */
    private static Map<AbstractFile, String> extendedNameCache;
    
    /** Caches drive icons */
    private static Map<AbstractFile, Icon> iconCache = new Hashtable<AbstractFile, Icon>();
    

    /** Filters out volumes from the list based on the exclude regexp defined in the configuration, null if the regexp
     * is not defined. */
    private static PathFilter volumeFilter;

    /** ProtocolPanelProviders that we should make shortcuts for */
    private static Map<String, ProtocolPanelProvider> schemaToPanelProvider  = new HashMap<>();

    static {
        if(OsFamily.WINDOWS.isCurrent()) {
            fileSystemView = FileSystemView.getFileSystemView();
            extendedNameCache = new Hashtable<AbstractFile, String>();
        }

        try {
            String excludeRegexp = MuConfigurations.getPreferences().getVariable(MuPreference.VOLUME_EXCLUDE_REGEXP);
            if(excludeRegexp!=null) {
                volumeFilter = new RegexpPathFilter(excludeRegexp, true);
                volumeFilter.setInverted(true);
            }
        }
        catch(PatternSyntaxException e) {
            LOGGER.info("Invalid regexp for conf variable "+MuPreferences.VOLUME_EXCLUDE_REGEXP, e);
        }

        // Initialize the volumes list
        volumes = getDisplayableVolumes();
    }


    /**
     * Creates a new <code>DrivePopupButton</code> which is to be added to the given FolderPanel.
     *
     * @param folderPanel the FolderPanel instance this button will be added to
     */
    public DrivePopupButton(FolderPanel folderPanel) {
        this.folderPanel = folderPanel;
		
        // Listen to location events to update the button when the current folder changes
        folderPanel.getLocationManager().addLocationListener(this);

        // Listen to bookmark changes to update the button if a bookmark corresponding to the current folder
        // has been added/edited/removed
        BookmarkManager.addBookmarkListener(this);

        // Listen to configuration changes to update the button if the system file icons policy has changed
        MuConfigurations.addPreferencesListener(this);

        if(OsFamily.MAC_OS.isCurrent()) {
            setMargin(new Insets(1,1,1,1));
            putClientProperty("JComponent.sizeVariant", "small");
            putClientProperty("JButton.buttonType", "textured");
        }
    }

    /**
     * Updates the button's label and icon to reflect the current folder and match one of the current volumes:
     * <<ul>
     *	<li>If the specified folder corresponds to a bookmark, the bookmark's name will be displayed
     *	<li>If the specified folder corresponds to a local file, the enclosing volume's name will be displayed
     *	<li>If the specified folder corresponds to a remote file, the protocol's name will be displayed
     * </ul>
     * The button's icon will be the current folder's one.
     */
    private void updateButton() {
        AbstractFile currentFolder = folderPanel.getCurrentFolder();
        String currentPath = currentFolder.getAbsolutePath();
        FileURL currentURL = currentFolder.getURL();

        // First try to find a bookmark matching the specified folder
        for(Bookmark bookmark : BookmarkManager.getBookmarks()) {
            if(currentPath.equals(bookmark.getLocation())) {
                // Note: if several bookmarks match current folder, the first one will be used
                setText(bookmark.getName());
                setIcon(IconManager.getIcon(IconManager.FILE_ICON_SET, CustomFileIconProvider.BOOKMARK_ICON_NAME));
                return;
            }
        }

        // If no bookmark matched current folder
        String protocol = currentURL.getScheme();
        switch (protocol) {
        // Local file, use volume's name
        case LocalFile.SCHEMA:
        	String newLabel = null;
        	// Patch for Windows UNC network paths (weakly characterized by having a host different from 'localhost'):
        	// display 'SMB' which is the underlying protocol
        	if(OsFamily.WINDOWS.isCurrent() && !FileURL.LOCALHOST.equals(currentURL.getHost())) {
        		newLabel = "SMB";
        	}
        	else {
        		// getCanonicalPath() must be avoided under Windows for the following reasons:
        		// a) it is not necessary, Windows doesn't have symlinks
        		// b) it triggers the dreaded 'No disk in drive' error popup dialog.
        		// c) when network drives are present but not mounted (e.g. X:\ mapped onto an SMB share),
        		// getCanonicalPath which is I/O bound will take a looooong time to execute

        		if(OsFamily.WINDOWS.isCurrent())
        			currentPath = currentFolder.getAbsolutePath(false).toLowerCase();
        		else
        			currentPath = currentFolder.getCanonicalPath(false).toLowerCase();

        		int bestLength = -1;
        		int bestIndex = 0;
        		String temp;
        		int len;
        		for(int i=0; i< volumes.length; i++) {
        			if(OsFamily.WINDOWS.isCurrent())
        				temp = volumes[i].getAbsolutePath(false).toLowerCase();
        			else
        				temp = volumes[i].getCanonicalPath(false).toLowerCase();

        			len = temp.length();
        			if (currentPath.startsWith(temp) && len>bestLength) {
        				bestIndex = i;
        				bestLength = len;
        			}
        		}
        		newLabel = volumes[bestIndex].getName();

        		// Not used because the call to FileSystemView is slow
        		//                    if(fileSystemView!=null)
        		//                        newToolTip = getWindowsExtendedDriveName(volumes[bestIndex]);

        	}
        	setText(newLabel);
    		// Set the folder icon based on the current system icons policy
    		setIcon(FileIcons.getFileIcon(currentFolder));
        	break;

        case BookmarkProtocolProvider.BOOKMARK:
        	String currentFolderName = currentFolder.getName();
        	setText(currentFolderName.isEmpty() ? Translator.get("bookmarks_menu") : currentFolderName);
        	setIcon(IconManager.getIcon(IconManager.FILE_ICON_SET, CustomFileIconProvider.BOOKMARKS_ICON_NAME));
        	break;

        case SearchFile.SCHEMA:
            setText(Translator.get("find"));
            setIcon(IconManager.getIcon(IconManager.FILE_ICON_SET, CustomFileIconProvider.FIND_RESULT_ICON_NAME));
            break;

        case "gdrive":
            setText(Translator.get("gdrive"));
            setIcon(IconManager.getIcon(IconManager.FILE_ICON_SET, CustomFileIconProvider.GOOGLE_DRIVE_ICON_NAME));
            break;

        case "dropbox":
            setText(Translator.get("dropbox"));
            setIcon(IconManager.getIcon(IconManager.FILE_ICON_SET, CustomFileIconProvider.DROPBOX_ICON_NAME));
            break;

        default:
        	// Remote file, use the protocol's name
    		setText(protocol.toUpperCase());
    		// Set the folder icon based on the current system icons policy
    		setIcon(FileIcons.getFileIcon(currentFolder));
        }
		
    }


    /**
     * Returns the extended name of the given local file, e.g. "Local Disk (C:)" for C:\. The returned value is
     * interesting only under Windows. This method is I/O bound and very slow so it should not be called from the main
     * event thread.
     *
     * @param localFile the file for which to return the extended name
     * @return the extended name of the given local file
     */
    private static String getExtendedDriveName(AbstractFile localFile) {
        // Note: fileSystemView.getSystemDisplayName(java.io.File) is unfortunately very very slow
        String name = fileSystemView.getSystemDisplayName((java.io.File)localFile.getUnderlyingFileObject());

        if(name==null || name.equals(""))   // This happens for CD/DVD drives when they don't contain any disc
            return localFile.getName();

        return name;
    }


    /**
     * Returns the list of volumes to be displayed in the popup menu.
     *
     * <p>The raw list of volumes is fetched using {@link LocalFile#getVolumes()} and then
     * filtered using the regexp defined in the {@link MuPreferences#VOLUME_EXCLUDE_REGEXP} configuration variable
     * (if defined).</p>
     *
     * @return the list of volumes to be displayed in the popup menu
     */
    public static AbstractFile[] getDisplayableVolumes() {
        AbstractFile[] volumes = LocalFile.getVolumes();

        if(volumeFilter!=null)
            return volumeFilter.filter(volumes);

        return volumes;
    }


    ////////////////////////////////
    // PopupButton implementation //
    ////////////////////////////////

    @Override
    public JPopupMenu getPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        // Update the list of volumes in case new ones were mounted
        volumes = getDisplayableVolumes();

        // Add volumes
        int nbVolumes = volumes.length;
        final MainFrame mainFrame = folderPanel.getMainFrame();

        MnemonicHelper mnemonicHelper = new MnemonicHelper();   // Provides mnemonics and ensures uniqueness
        JMenuItem item;
        MuAction action;
        String volumeName;

        boolean useExtendedDriveNames = fileSystemView!=null;
        ArrayList<JMenuItem> itemsV = new ArrayList<JMenuItem>();

        for(int i=0; i<nbVolumes; i++) {
            action = new CustomOpenLocationAction(mainFrame, new Hashtable<String, Object>(), volumes[i]);
            volumeName = volumes[i].getName();

            // If several volumes have the same filename, use the volume's path for the action's label instead of the
            // volume's path, to disambiguate
            for(int j=0; j<nbVolumes; j++) {
                if(j!=i && volumes[j].getName().equalsIgnoreCase(volumeName)) {
                    action.setLabel(volumes[i].getAbsolutePath());
                    break;
                }
            }

            item = popupMenu.add(action);
            setMnemonic(item, mnemonicHelper);

            // Set icon from cache
            Icon icon = iconCache.get(volumes[i]);
            if (icon!=null) {
                item.setIcon(icon);
            }

            if(useExtendedDriveNames) {
                // Use the last known value (if any) while we update it in a separate thread
                String previousExtendedName = extendedNameCache.get(volumes[i]);
                if(previousExtendedName!=null)
                    item.setText(previousExtendedName);

            }
            itemsV.add(item);   // JMenu offers no way to retrieve a particular JMenuItem, so we have to keep them
        }

        new RefreshDriveNamesAndIcons(popupMenu, itemsV).start();

        popupMenu.add(new JSeparator());

        // Add boookmarks
        java.util.List<Bookmark> bookmarks = BookmarkManager.getBookmarks();

        if (!bookmarks.isEmpty()) {
            for(Bookmark bookmark : bookmarks) {
                item = popupMenu.add(new CustomOpenLocationAction(mainFrame, new Hashtable<String, Object>(), bookmark));
                setMnemonic(item, mnemonicHelper);
            }
        }
        else {
            // No bookmark : add a disabled menu item saying there is no bookmark
            popupMenu.add(Translator.get("bookmarks_menu.no_bookmark")).setEnabled(false);
        }

        popupMenu.add(new JSeparator());

        // Add 'Network shares' shortcut
        if(FileFactory.isRegisteredProtocol(FileProtocols.SMB)) {
            action = new CustomOpenLocationAction(mainFrame, new Hashtable<String, Object>(), new Bookmark(Translator.get("drive_popup.network_shares"), "smb:///"));
            action.setIcon(IconManager.getIcon(IconManager.FILE_ICON_SET, CustomFileIconProvider.NETWORK_ICON_NAME));
            setMnemonic(popupMenu.add(action), mnemonicHelper);
        }

        // Add Bonjour services menu
        setMnemonic(popupMenu.add(new BonjourMenu() {
            @Override
            public MuAction getMenuItemAction(BonjourService bs) {
                return new CustomOpenLocationAction(mainFrame, new Hashtable<String, Object>(), bs);
            }
        }) , mnemonicHelper);
        popupMenu.add(new JSeparator());

        // Add 'connect to server' shortcuts
        schemaToPanelProvider.values().stream()
        .sorted(Comparator.comparing(ProtocolPanelProvider::priority))
        .map(this::toServerConnectAction)
        .map(popupMenu::add)
        .forEach(menuItem -> setMnemonic(menuItem, mnemonicHelper));

        return popupMenu;
    }

    /**
     * Registers an instance of {@link ProtocolPanelProvider} to the drive popup buttons,
     * this will add shortcut to the relevant 'connect to server' dialog.
     * @param protocolPanelProvider the {@link ProtocolPanelProvider} to register.
     */
    public static void register(ProtocolPanelProvider protocolPanelProvider) {
        schemaToPanelProvider.put(protocolPanelProvider.getSchema(), protocolPanelProvider);
    }

    /**
     * Unregisters an instance of {@link ProtocolPanelProvider}.
     * @see {@link #register(ProtocolPanelProvider)}}
     * @param protocolPanelProvider an instance of {@link ProtocolPanelProvider} to unregister.
     */
    public static void unregister(ProtocolPanelProvider protocolPanelProvider) {
        schemaToPanelProvider.remove(protocolPanelProvider.getSchema());
    }

    /**
     * Converts an instance of {@link ProtocolPanelProvider} to an instance of {@link ServerConnectAction}.
     * @param service an instance of {@link ProtocolPanelProvider} to convert.
     * @return an instance of of {@link ProtocolPanelProvider} that corresponds to the given instance of {@link ServerConnectAction}.
     */
    private ServerConnectAction toServerConnectAction(ProtocolPanelProvider service) {
        String label = service.getSchema().toUpperCase() + "...";
        return new ServerConnectAction(label, service.getPanelClass());
    }

    /**
     *  Calls to getExtendedDriveName(String) are very slow, so they are performed in a separate thread so as
     *  to not lock the main even thread. The popup menu gets first displayed with the short drive names, and
     * then refreshed with the extended names as they are retrieved.        
     */
    private class RefreshDriveNamesAndIcons extends Thread {
        
        private JPopupMenu popupMenu;
        private ArrayList<JMenuItem> items;

        public RefreshDriveNamesAndIcons(JPopupMenu popupMenu, ArrayList<JMenuItem> items) {
            super("RefreshDriveNamesAndIcons");
            this.popupMenu = popupMenu;
            this.items = items;
        }
        
        @Override
        public void run() {
            final boolean useExtendedDriveNames = fileSystemView!=null;
            for(int i=0; i<items.size(); i++) {
                final JMenuItem item = items.get(i);

                String extendedName = null;
                if (useExtendedDriveNames) {
                    // Under Windows, show the extended drive name (e.g. "Local Disk (C:)" instead of just "C:") but use
                    // the simple drive name for the mnemonic (i.e. 'C' instead of 'L').
                    extendedName = getExtendedDriveName(volumes[i]);

                    // Keep the extended name for later (see above)
                    extendedNameCache.put(volumes[i], extendedName);
                }
                final String extendedNameFinal = extendedName;

                // Set system icon for volumes, only if system icons are available on the current platform
                final Icon icon = FileIcons.hasProperSystemIcons()?FileIcons.getSystemFileIcon(volumes[i]):null;
                if (icon!=null) {
                    iconCache.put(volumes[i], icon);
                }

                SwingUtilities.invokeLater(() -> {
                    if (useExtendedDriveNames) {
                        item.setText(extendedNameFinal);
                    }
                    if (icon!=null) {
                        item.setIcon(icon);
                    }
                });
            }

            // Re-calculate the popup menu's dimensions
            SwingUtilities.invokeLater(() -> {
                popupMenu.invalidate();
                popupMenu.pack();
            });
        }
        
    }


    /**
     * Convenience method that sets a mnemonic to the given JMenuItem, using the specified MnemonicHelper.
     *
     * @param menuItem the menu item for which to set a mnemonic
     * @param mnemonicHelper the MnemonicHelper instance to be used to determine the mnemonic's character.
     */
    private void setMnemonic(JMenuItem menuItem, MnemonicHelper mnemonicHelper) {
        menuItem.setMnemonic(mnemonicHelper.getMnemonic(menuItem.getText()));
    }


    //////////////////////////////
    // BookmarkListener methods //
    //////////////////////////////
	
    public void bookmarksChanged() {
        // Refresh label in case a bookmark with the current location was changed
        updateButton();
    }


    ///////////////////////////////////
    // ConfigurationListener methods //
    ///////////////////////////////////

    /**
     * Listens to certain configuration variables.
     */
    public void configurationChanged(ConfigurationEvent event) {
        String var = event.getVariable();

        // Update the button's icon if the system file icons policy has changed
        if (var.equals(MuPreferences.USE_SYSTEM_FILE_ICONS))
            updateButton();
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public Dimension getPreferredSize() {
        // Limit button's maximum width to something reasonable and leave enough space for location field,
        // as bookmarks name can be as long as users want them to be.
        // Note: would be better to use JButton.setMaximumSize() but it doesn't seem to work
        Dimension d = super.getPreferredSize();
        if(d.width > 160)
            d.width = 160;
        return d;
    }


    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * This action pops up {@link com.mucommander.ui.dialog.server.ServerConnectDialog} for a specified
     * protocol.
     */
    private class ServerConnectAction extends AbstractAction {
        private Class<? extends ServerPanel> serverPanelClass;

        private ServerConnectAction(String label, Class<? extends ServerPanel> serverPanelClass) {
            super(label);
            this.serverPanelClass = serverPanelClass;
        }

        public void actionPerformed(ActionEvent actionEvent) {
            new ServerConnectDialog(folderPanel, serverPanelClass).showDialog();
        }
    }


    /**
     * This modified {@link OpenLocationAction} changes the current folder on the {@link FolderPanel} that contains
     * this button, instead of the currently active {@link FolderPanel}.  
     */
    private class CustomOpenLocationAction extends OpenLocationAction {

        public CustomOpenLocationAction(MainFrame mainFrame, Map<String,Object> properties, Bookmark bookmark) {
            super(mainFrame, properties, bookmark);
        }

        public CustomOpenLocationAction(MainFrame mainFrame, Map<String,Object> properties, AbstractFile file) {
            super(mainFrame, properties, file);
        }

        public CustomOpenLocationAction(MainFrame mainFrame, Map<String,Object> properties, BonjourService bs) {
            super(mainFrame, properties, bs);
        }

        ////////////////////////
        // Overridden methods //
        ////////////////////////

        @Override
        protected FolderPanel getFolderPanel() {
            return folderPanel;
        }
    }

	/**********************************
	 * LocationListener Implementation
	 **********************************/
	
	public void locationChanged(LocationEvent e) {
        // Update the button's label to reflect the new current folder
        updateButton();
    }
}
