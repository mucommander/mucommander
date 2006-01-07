package com.mucommander.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import com.mucommander.text.Translator;

import com.mucommander.conf.ConfigurationManager;


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
	 * Sets the status bar info. This method is called by FileTable.
	 *
	 * @param filesInfo String that contains info about current selected file(s)
	 * @param volumeInfo String that contains info about current volume (free/total space) 
	 */
	public void setStatusInfo(String filesInfo, String volumeInfo) {
	    //if(com.mucommander.Debug.ON) text += " - freeMem="+Runtime.getRuntime().freeMemory()+" - totalMem="+Runtime.getRuntime().totalMemory();
		statusBarFilesLabel.setText(filesInfo);
		statusBarVolumeLabel.setText(volumeInfo);
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