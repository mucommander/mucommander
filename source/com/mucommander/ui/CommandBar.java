

package com.mucommander.ui;

import com.mucommander.ui.table.*;
import com.mucommander.ui.comp.dialog.*;
import com.mucommander.ui.viewer.*;
import com.mucommander.ui.editor.*;
import com.mucommander.ui.icon.IconManager;

import com.mucommander.text.Translator;
import com.mucommander.job.*;
import com.mucommander.file.*;
import com.mucommander.conf.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


/**
 * CommandBar is the button bar that sits at the bottom of the main window and provides access to
 * a number of commands (view, edit, copy, move...).
 *
 * @author Maxence Bernard
 */
public class CommandBar extends JPanel implements ConfigurationListener, ActionListener, MouseListener {

	/** Parent MainFrame instance */
    private MainFrame mainFrame;

	/** True when Shift key is pressed */ 
	private boolean shiftDown;
	
	/** Buttons */
	private JButton buttons[];
    
	/** Right-click popup menu */
	private JPopupMenu popupMenu;
	/** Popup menu item that hides the toolbar */
	private JMenuItem hideMenuItem;	

	////////////////////
	// Button indexes //
	////////////////////
	
	public final static int VIEW_INDEX = 0;
	public final static int EDIT_INDEX = 1;
	public final static int COPY_INDEX = 2;
	public final static int MOVE_INDEX = 3;
	public final static int MKDIR_INDEX = 4;
	public final static int DELETE_INDEX = 5;
	public final static int REFRESH_INDEX = 6;
	public final static int CLOSE_INDEX = 7;
	
	private final static int NB_BUTTONS = 8;

	//////////////////////////
	// Locatlized text keys //
	//////////////////////////

	private final static String MOVE_TEXT = "command_bar.move";
	private final static String RENAME_TEXT = "command_bar.rename";

	private final static String COPY_TEXT = "command_bar.copy";
	private final static String LOCAL_COPY_TEXT = "command_bar.local_copy";
	
	private final static String BUTTONS_DESC[][] =  {
		{"command_bar.view", "[F3]", "view.png"},
		{"command_bar.edit", "[F4]", "edit.png"},
		{COPY_TEXT, "[F5]", "copy.png"},
		{MOVE_TEXT, "[F6]", "move.png"},
		{"command_bar.mkdir", "[F7]", "mkdir.png"},
		{"command_bar.delete", "[F8]", "delete.png"},
		{"command_bar.refresh", "[F9]", "refresh.png"},
		{"command_bar.close", "[F10]", "close.png"}
	};


	/**
	 * Preloads icons if command bar is to become visible after launch. 
	 * Icons will then be in IconManager's cache, ready for use when the first command bar is created.
	 */
	static {
		if(com.mucommander.conf.ConfigurationManager.getVariableBoolean("prefs.command_bar.visible", true)) {
			if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Preloading command bar icons");

			// For each button
			for(int i=0; i<NB_BUTTONS; i++)
				IconManager.getCommandBarIcon(BUTTONS_DESC[i][2]);
		}
	}


	/**
	 * Dummy method which does nothing but trigger static block execution.
	 * Calling this method early enough at launch time makes initialization predictable.
	 */
	public static void init() {
	}


	/**
	 * Creates a new CommandBar instance associated with the given MainFrame.
	 */
	public CommandBar(MainFrame mainFrame) {
		super(new GridLayout(0,8));
        this.mainFrame = mainFrame;

		this.buttons = new JButton[NB_BUTTONS];
		for(int i=0; i<NB_BUTTONS; i++)
			buttons[i] = addButton(
				Translator.get(BUTTONS_DESC[i][0])+" "+BUTTONS_DESC[i][1],
				Translator.get(BUTTONS_DESC[i][0]+"_tooltip")
			);	
	
		addMouseListener(this);

		// Listen to configuration changes to reload command bar buttons when icon size has changed
		ConfigurationManager.addConfigurationListener(this);
	}


	/**
	 * Returns the button correponding to the given index, to be used in conjunction
	 * with final static fields.  
	 */
	public JButton getButton(int buttonIndex) {
		return buttons[buttonIndex];
	}
	
	
	/**
	 * Creates and adds a button to the command bar using the provided label and tooltip text.
	 *
	 * @param label the button's label
	 * @param tooltipText the tooltip text that will get displayed when the mouse stays over the button
	 */
	private JButton addButton(String label, String tooltipText) {
		JButton button = new JButton(label);
        button.setToolTipText(tooltipText);
		button.setMargin(new Insets(3,4,3,4));
		// For Mac OS X whose default minimum width for buttons is enormous
		button.setMinimumSize(new Dimension(40, (int)button.getPreferredSize().getWidth()));
		button.addActionListener(this);
		button.addMouseListener(this);
		add(button);
		return button;
	}


	/**
	 * Sets icons in command bar buttons, called when this command bar is about to become visible.
	 */
	private void setButtonIcons() {
		// For each button
		for(int i=0; i<NB_BUTTONS; i++)
			buttons[i].setIcon(IconManager.getCommandBarIcon(BUTTONS_DESC[i][2]));
	}
	
	/**
	 * Remove icons from command bar buttons, called when this command bar is about to become invisible in order to garbage-collect icon instances.
	 */
	private void removeButtonIcons() {
		// For each button
		for(int i=0; i<NB_BUTTONS; i++)
			buttons[i].setIcon(null);
	}
	
	
	/**
	 * Sets shift mode on or off : some buttons such as 'F6 Move' may want to indicate
	 * the action is different when shift is pressed.
	 */
	public void setShiftMode(boolean on) {
		if(shiftDown!=on) {
			this.shiftDown = on;
			boolean singleFileMode = on&&((FileTableModel)(mainFrame.getLastActiveTable().getModel())).getNbMarkedFiles()<=1;

			// Change Move/Rename button's text and tooltip
			String textKey = singleFileMode?RENAME_TEXT:MOVE_TEXT;
			buttons[MOVE_INDEX].setText(Translator.get(textKey)+" "+BUTTONS_DESC[MOVE_INDEX][1]);
			buttons[MOVE_INDEX].setToolTipText(Translator.get(textKey+"_tooltip"));
			buttons[MOVE_INDEX].repaint();

			// Change Copy/Local copy button's text and tooltip
			textKey = singleFileMode?LOCAL_COPY_TEXT:COPY_TEXT;
			buttons[COPY_INDEX].setText(Translator.get(textKey)+" "+BUTTONS_DESC[COPY_INDEX][1]);
			buttons[COPY_INDEX].setToolTipText(Translator.get(textKey+"_tooltip"));
			buttons[COPY_INDEX].repaint();
		}
	}


	/**
	 * Fires up the appropriate file viewer for the selected file.
	 */
	public void doView() {
		AbstractFile file = mainFrame.getLastActiveTable().getSelectedFile();
		if(file==null || (file.isDirectory() && !file.isSymlink())) {
			mainFrame.requestFocus();
			return;
		}
		
		ViewerFrame frame = new ViewerFrame(mainFrame, file);
		frame.show();
	}
	
	
	/**
	 * Fires up the appropriate file editor for the selected file.
	 */
	public void doEdit() {
		AbstractFile file = mainFrame.getLastActiveTable().getSelectedFile();
		if(file==null || (file.isDirectory() && !file.isSymlink())) {
			mainFrame.requestFocus();
			return;
		}

		EditorFrame frame = new EditorFrame(mainFrame, file);
		frame.show();
	}
	

	////////////////////////
	// Overridden methods //
	////////////////////////

	/**
	 * Overridden method to set/remove toolbar icons depending on the specified new visible state.
	 */
	public void setVisible(boolean visible) {
		if(visible) {
			// Set icons to buttons
			setButtonIcons();
		}
		else {
			// Remove icon from buttons
			removeButtonIcons();
		}
		
		super.setVisible(visible);
	}

	
	///////////////////////////////////
	// ConfigurationListener methods //
	///////////////////////////////////
	
    /**
     * Listens to certain configuration variables.
     */
    public boolean configurationChanged(ConfigurationEvent event) {
    	String var = event.getVariable();

		// Reload toolbar icons if their size has changed and command bar is visible
		if (var.equals(IconManager.COMMAND_BAR_ICON_SCALE_CONF_VAR)) {
			if(isVisible())
				setButtonIcons();
		}

		return true;
	}
	
	
	////////////////////////////
	// ActionListener methods //
	////////////////////////////

    public void actionPerformed(ActionEvent e) {
		// Discard action events while in 'no events mode'
		if(mainFrame.getNoEventsMode())
			return;

        Object source = e.getSource();

		// Hide command bar
		if(source == hideMenuItem) {
			mainFrame.setCommandBarVisible(false);
			this.popupMenu.setVisible(false);
			this.popupMenu = null;
			this.hideMenuItem = null;
			return;
		}        
		// View button
		else if(source == buttons[VIEW_INDEX]) {
        	doView();
		}
		// Edit button
        else if(source == buttons[EDIT_INDEX]) {
			doEdit();
		}
		// Mkdir button
        else if(source == buttons[MKDIR_INDEX]) {
			// Trigger Mkdir action
            new MkdirDialog(mainFrame);
        }
		// Refresh button
        else if(source == buttons[REFRESH_INDEX]) {
			// Try to refresh current folder in a separate thread
			mainFrame.getLastActiveTable().getFolderPanel().tryRefreshCurrentFolder();
		}
		// Close window button
        else if(source == buttons[CLOSE_INDEX]) {
			WindowManager.getInstance().disposeMainFrame(mainFrame);
		}
		else {
			// The following actions need to work on files, 
			// simply return if no file is selected
			FileSet files = mainFrame.getLastActiveTable().getSelectedFiles();
			if(files.size()==0) {
				// Request focus since focus currently belongs to a command bar button
				// and no dialog will request focus
				mainFrame.requestFocus();
				return;
			}
			
			// Copy button
			if(source == buttons[COPY_INDEX]) {
				new CopyDialog(mainFrame, files, shiftDown);
			}
			// Move/Rename button
			else if(source == buttons[MOVE_INDEX]) {
				new MoveDialog(mainFrame, files, shiftDown);
			}
			// Delete button
			else if(source == buttons[DELETE_INDEX]) {
				new DeleteDialog(mainFrame, files);
			}
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
				this.hideMenuItem = new JMenuItem(Translator.get("command_bar.hide_command_bar"));
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