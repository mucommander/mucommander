package com.mucommander.ui;

import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FileTableModel;

import com.mucommander.event.*;

import com.mucommander.text.SizeFormatter;
import com.mucommander.text.Translator;

import com.mucommander.conf.ConfigurationManager;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FSFile;

import com.mucommander.cache.LRUCache;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/**
 * 
 *
 * @author Maxence Bernard
 */
public class StatusBar extends JPanel implements ActionListener, MouseListener, TableChangeListener, LocationListener, ComponentListener {

	private MainFrame mainFrame;

	/** Label that displays info about current selected file(s) */
	private JLabel statusBarFilesLabel;
	
	/** Label that displays info about current volume (free/total space) */
	private JLabel statusBarVolumeLabel;

	/** Right-click popup menu */
	private JPopupMenu popupMenu;
	/** Popup menu item that hides the toolbar */
	private JMenuItem hideMenuItem;	

	/** Number of volume info strings that can be temporarily cached */
	private final static int VOLUME_INFO_CACHE_CAPACITY = 50;

	/** Number of milliseconds before cached volume info strings expire */
	private final static int VOLUME_INFO_TIME_TO_LIVE = 60000;

	/** Caches volume info strings (free/total space) for a while, since it is quite costly and we don't want
	 * to recalculate it each time this information is requested.
	 * Each cache item maps a path to a volume info string */
	private static LRUCache volumeInfoCache = new LRUCache(VOLUME_INFO_CACHE_CAPACITY);
	
	/** SizeFormatter's format used to display volume info in status bar */
	private final static int VOLUME_INFO_SIZE_FORMAT = SizeFormatter.DIGITS_SHORT|SizeFormatter.UNIT_SHORT|SizeFormatter.INCLUDE_SPACE|SizeFormatter.ROUND_TO_KB;


	/**
	 * Creates a new StatusBar instance.
	 */
	public StatusBar(MainFrame mainFrame) {
		// Create and add status bar
		super(new BorderLayout());
		
		this.mainFrame = mainFrame;
		
		this.statusBarFilesLabel = new JLabel("");
		add(statusBarFilesLabel, BorderLayout.CENTER);

		this.statusBarVolumeLabel = new JLabel("");
		add(statusBarVolumeLabel, BorderLayout.EAST);

		// Show/hide this status bar based on user preferences
		if(!ConfigurationManager.getVariableBoolean("prefs.status_bar.visible", true))
			setVisible(false);
		else
			updateStatusInfo();
		
		// Catch location events to update status bar info when folder is changed
		mainFrame.getFolderPanel1().addLocationListener(this);
		mainFrame.getFolderPanel2().addLocationListener(this);
		
		// Catch table change events to update status bar info when current table has changed
		mainFrame.addTableChangeListener(this);
		
		// Catch mouse events to pop up a menu on right-click
		statusBarFilesLabel.addMouseListener(this);
		statusBarVolumeLabel.addMouseListener(this);
		addMouseListener(this);
		
		// Catch component events to be notified when this component is made visible
		// and update status info
		addComponentListener(this);
	}


	/**
	 * Updates info displayed on the status bar (currently selected files and volume info).
	 */
	public void updateStatusInfo() {
		updateSelectedFilesInfo();
		updateVolumeInfo();
	}
	

	/**
	 * Updates info about currently selected files ((nb of selected files, combined size), displayed on the left-side of this status bar.
	 */
	public void updateSelectedFilesInfo() {
		if(!isVisible())
			return;

		FileTable currentFileTable = mainFrame.getLastActiveTable();

		// Currently select file, can be null
		AbstractFile selectedFile = currentFileTable.getSelectedFile();
		FileTableModel tableModel = (FileTableModel)currentFileTable.getModel();
		// Number of marked files, can be 0
		int nbMarkedFiles = tableModel.getNbMarkedFiles();
		// Combined size of marked files, 0 if no file has been marked
		long markedTotalSize = tableModel.getTotalMarkedSize();
		// number of files in folder
		int fileCount = tableModel.getFileCount();

		// Update files info based on marked files if there are some, or currently selected file otherwise
		int nbSelectedFiles = 0;
		if(nbMarkedFiles==0 && selectedFile!=null)
			nbSelectedFiles = 1;
		else
			nbSelectedFiles = nbMarkedFiles;

		boolean compactFileSize = ConfigurationManager.getVariableBoolean("prefs.display_compact_file_size", true);
		String filesInfo;
		
		if(fileCount==0) {
			// Set status bar to a space character, not an empty string
			// otherwise it will disappear
			filesInfo = " ";
		}
		else {
			filesInfo = Translator.get("status_bar.selected_files", ""+nbSelectedFiles, ""+fileCount);
			
			if(nbMarkedFiles>0)
				filesInfo += " - "+SizeFormatter.format(markedTotalSize, (compactFileSize?SizeFormatter.DIGITS_SHORT:SizeFormatter.DIGITS_FULL)|(compactFileSize?SizeFormatter.UNIT_SHORT:SizeFormatter.UNIT_LONG)|SizeFormatter.INCLUDE_SPACE|SizeFormatter.ROUND_TO_KB);
	
			if(selectedFile!=null)
				filesInfo += " - "+selectedFile.getName();
		}		

		// Update label
		statusBarFilesLabel.setText(filesInfo);
	}
	
	
	/**
	 * Updates info about current volume (free space, total space), displayed on the right-side of this status bar.
	 */
	private void updateVolumeInfo() {
		if(!isVisible())
			return;

		final AbstractFile currentFolder = mainFrame.getLastActiveTable().getCurrentFolder();

		if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called, currentFolder="+currentFolder);
		if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("cache state="+volumeInfoCache);

		String cachedVolumeInfo = (String)volumeInfoCache.get(currentFolder.getAbsolutePath());
		if(cachedVolumeInfo!=null) {
			if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Cache hit!");
			statusBarVolumeLabel.setText(cachedVolumeInfo);
		}
		else {
			// Retrieves free and total volume space.
			// Perform volume info retrieval in a separate thread as this method may be called
			// by the event thread and it can take a while, we want to return as soon as possible
			new Thread() {
				public void run() {
					// Free space on current volume, -1 if this information is not available 
					long volumeFree;
					// Total space on current volume, -1 if this information is not available 
					long volumeTotal;

					// Folder is a local file : call getVolumeInfo() instead of separate calls to getFreeSpace()
					// and getTotalSpace() as it is twice as fast
					if(currentFolder instanceof FSFile) {
						long volumeInfo[] = ((FSFile)currentFolder).getVolumeInfo();
						volumeTotal = volumeInfo[0];
						volumeFree = volumeInfo[1];
					}
					// Any other file kind
					else {
						volumeFree = currentFolder.getFreeSpace();
						volumeTotal = currentFolder.getTotalSpace();
					}

					String volumeInfo;
					if(volumeFree!=-1) {
						volumeInfo = SizeFormatter.format(volumeFree, VOLUME_INFO_SIZE_FORMAT);
						if(volumeTotal!=-1)
							volumeInfo += " / "+ SizeFormatter.format(volumeTotal, VOLUME_INFO_SIZE_FORMAT);
						volumeInfo = Translator.get("status_bar.volume_free", volumeInfo);
					}
					else if(volumeTotal!=-1) {
						volumeInfo = SizeFormatter.format(volumeTotal, VOLUME_INFO_SIZE_FORMAT);
						volumeInfo = Translator.get("status_bar.volume_capacity", volumeInfo);
					}
					else {
						volumeInfo = "";
					}
					statusBarVolumeLabel.setText(volumeInfo);
					if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Adding to cache");
					volumeInfoCache.add(currentFolder.getAbsolutePath(), volumeInfo, VOLUME_INFO_TIME_TO_LIVE);
				}
			}.start();
		}
	}


	/**
	 * Displays a message on the left-side of the status bar, discarding current info about currently selected files and volume.
	 *
	 * @param infoMessage the message to display
	 */
	public void setStatusInfo(String infoMessage) {
	    //if(com.mucommander.Debug.ON) text += " - freeMem="+Runtime.getRuntime().freeMemory()+" - totalMem="+Runtime.getRuntime().totalMemory();
		statusBarFilesLabel.setText(infoMessage);
		statusBarVolumeLabel.setText("");
	}
	
	
	
	////////////////////////////
	// ActionListener methods //
	////////////////////////////

    public void actionPerformed(ActionEvent e) {
		// Discard action events while in 'no events mode'
		if(mainFrame.getNoEventsMode())
			return;

        Object source = e.getSource();

		// Hide status bar
		if(source == hideMenuItem) {
			mainFrame.setStatusBarVisible(false);
			this.popupMenu.setVisible(false);
			this.popupMenu = null;
			this.hideMenuItem = null;
			return;
		}        
	}

	/////////////////////////////////
	// TableChangeListener methods //
	/////////////////////////////////
	
	public void tableChanged(FolderPanel folderPanel) {
		if(isVisible())
			updateStatusInfo();
	}

	//////////////////////////////
	// LocationListener methods //
	//////////////////////////////
	
	public void locationChanged(LocationEvent e) {
		if(isVisible())
			updateStatusInfo();
	}

	public void locationChanging(LocationEvent e) {
		// Show a message in the status bar saying that folder is being changed
		// No need to waste precious cycles if status bar is not visible
		if(isVisible())
			setStatusInfo(Translator.get("status_bar.connecting_to_folder"));
	}
	
	public void locationCancelled(LocationEvent e) {
		if(isVisible())
			updateStatusInfo();
	}
	
	
	///////////////////////////
	// MouseListener methods //
	///////////////////////////
	
	public void mouseClicked(MouseEvent e) {
		// Discard mouse events while in 'no events mode'
		if(mainFrame.getNoEventsMode())
			return;

		// Right clicking on the toolbar brings up a popup menu that allows the user to hide this status bar
		int modifiers = e.getModifiers();
		if ((modifiers & MouseEvent.BUTTON2_MASK)!=0 || (modifiers & MouseEvent.BUTTON3_MASK)!=0 || e.isControlDown()) {
//		if (e.isPopupTrigger()) {	// Doesn't work under Mac OS X (CTRL+click doesn't return true)
			if(this.popupMenu==null) {
				popupMenu = new JPopupMenu();
				this.hideMenuItem = new JMenuItem(Translator.get("status_bar.hide_status_bar"));
				hideMenuItem.addActionListener(this);
				popupMenu.add(hideMenuItem);
			}
			popupMenu.show(this, e.getX(), e.getY());
			popupMenu.setVisible(true);
		}
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}
	
	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}	
	
	
	///////////////////////////////
	// ComponentListener methods //
	///////////////////////////////
	
	public void componentShown(ComponentEvent e) {
		// Invoked when the component has been made visible (apparently not called when just created)
		// Status bar needs to be updated sihce it is not updated anymore when not visible
		updateStatusInfo();
	}     

	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentResized(ComponentEvent e) {
	}
}