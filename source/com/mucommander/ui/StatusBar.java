package com.mucommander.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import com.mucommander.text.SizeFormatter;
import com.mucommander.text.Translator;

import com.mucommander.conf.ConfigurationManager;

import com.mucommander.file.AbstractFile;

/**
 * 
 *
 * @author Maxence Bernard
 */
public class StatusBar extends JPanel implements ActionListener, MouseListener {

	private MainFrame mainFrame;

	/** Label that displays info about current selected file(s) */
	private JLabel statusBarFilesLabel;
	
	/** Label that displays info about current volume (free/total space) */
	private JLabel statusBarVolumeLabel;

	/** Right-click popup menu */
	private JPopupMenu popupMenu;
	/** Popup menu item that hides the toolbar */
	private JMenuItem hideMenuItem;	

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

		if(ConfigurationManager.getVariable("prefs.show_status_bar", "true").equals("false"))
			setVisible(false);
		
		statusBarFilesLabel.addMouseListener(this);
		statusBarVolumeLabel.addMouseListener(this);
		addMouseListener(this);
	}


	/**
	 * Sets info (nb of selected files, total size) about currently selected files, displayed on the left-side of this status bar.
	 * 
	 * <p>This method should only be called by FileTable.</p>
	 *
	 * @param selectedFile currently select file, can be <code>null</code>
	 * @param nbMarkedFiles number of marked files, can be 0
	 * @param markedTotalSize combined size of marked files, 0 if no file has been marked
	 * @param fileCount number of files in folder
	 */
	public void setSelectedFilesInfo(AbstractFile selectedFile, int nbMarkedFiles, long markedTotalSize, int fileCount) {
		// Update files info based on marked files if there are some, or currently selected file otherwise
		int nbSelectedFiles = 0;
		if(nbMarkedFiles==0 && selectedFile!=null)
			nbSelectedFiles = 1;
		else
			nbSelectedFiles = nbMarkedFiles;

		boolean compactFileSize = ConfigurationManager.getVariable("prefs.display_compact_file_size", "true").equals("true");
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
	 * Sets info (free space, total space) about current volume, displayed on the right-side of this status bar.
	 *
	 * <p>This method should only be called by FileTable.</p>
	 *
	 * @param volumeFree free space on current volume, -1 if this information is not available 
	 * @param volumeTotal total space on current volume, -1 if this information is not available 
	 */
	public void setVolumeInfo(long volumeFree, long volumeTotal) {
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

	
	///////////////////////////
	// MouseListener methods //
	///////////////////////////
	
	public void mouseClicked(MouseEvent e) {
		// Discard mouse events while in 'no events mode'
		if(mainFrame.getNoEventsMode())
			return;

		// Right clicking on the toolbar brings up a popup menu
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
}