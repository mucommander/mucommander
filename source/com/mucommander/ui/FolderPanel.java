
package com.mucommander.ui;

import com.mucommander.file.*;
import com.mucommander.ui.table.*;
import com.mucommander.ui.comp.FocusRequester;
import com.mucommander.ui.comp.dialog.*;

import com.mucommander.conf.*;
import com.mucommander.text.Translator;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.Vector;


public class FolderPanel extends JPanel implements ActionListener, PopupMenuListener, KeyListener, ConfigurationListener {
	private MainFrame mainFrame;
    
    private AbstractFile currentFolder;

    // Registered LocationListeners
    private Vector locationListeners = new Vector();
	
	/*  We're NOT using JComboBox anymore because of its strange behavior: 
		it calls actionPerformed() each time an item is highlighted with the arrow (UP/DOWN) keys,
		so there is no way to tell if it's the final selection (ENTER) or not.
	*/
	private JButton rootButton;
	private JPopupMenu rootPopup;
	private Vector rootMenuItems;
	private JTextField locationField;
	
	private FileTable fileTable;
	private JScrollPane scrollPane;
	
	private static AbstractFile rootFolders[];
	
    private static Color backgroundColor;

	private Vector history;
	private int historyIndex;
    
	private String lastFolderOnExit;

	private final static int CANCEL_ACTION = 0;
	private final static int BROWSE_ACTION = 1;
	private final static int COPY_ACTION = 2;
	
	private final static String CANCEL_TEXT = Translator.get("cancel");
	private final static String BROWSE_TEXT = Translator.get("browse");
	private final static String COPY_TEXT = Translator.get("copy");

	
	static {
		rootFolders = RootFolders.getRootFolders();

		// Set background color
		backgroundColor = FileTableCellRenderer.getColor("prefs.colors.background", "000084");
	}

	
	public FolderPanel(MainFrame mainFrame, AbstractFile initialFolder) {
		super(new BorderLayout());

        this.mainFrame = mainFrame;
/*
		JPanel locationPanel = new JPanel(new BorderLayout()) {
			public Insets getInsets() {
				return new Insets(6, 8, 6, 8);
			}
		
//			public Border getBorder() {
//				return null;
//			}
		};
*/		

		XBoxPanel locationPanel = new XBoxPanel();
		locationPanel.setInsets(new Insets(0, 6, 6, 0));
		
		rootButton = new JButton(rootFolders[0].toString());
		// For Mac OS X whose minimum width for buttons is enormous
		rootButton.setMinimumSize(new Dimension(40, (int)rootButton.getPreferredSize().getWidth()));
		rootButton.setMargin(new Insets(6,8,6,8));
		
		rootButton.addActionListener(this);
		rootPopup = new JPopupMenu();
		rootPopup.addPopupMenuListener(this);
		rootMenuItems = new Vector();
		JMenuItem menuItem;
		for(int i=0; i<rootFolders.length; i++) {
			menuItem = new JMenuItem(rootFolders[i].toString());
			menuItem.addActionListener(this);
			rootMenuItems.add(menuItem);
			rootPopup.add(menuItem);
		}

//		locationPanel.add(rootButton, BorderLayout.WEST);
		locationPanel.add(rootButton);
		locationPanel.addSpace(6);

		locationField = new JTextField();
		locationField.addActionListener(this);
		locationField.addKeyListener(this);
//		locationPanel.add(locationField, BorderLayout.CENTER);
		locationPanel.add(locationField);

		add(locationPanel, BorderLayout.NORTH);
		fileTable = new FileTable(mainFrame, this);

		// Initializes history vector
		history = new Vector();
    	historyIndex = -1;
		
		try {
			// Sets initial folder to current directory
			_setCurrentFolder(initialFolder, true);
		}
		catch(Exception e) {
			// If that failed, tries to read any other drive
				for(int i=0; i<rootFolders.length; i++) {
					try  {
						_setCurrentFolder(rootFolders[i], true);
						break;
					}
					catch(IOException e2) {
						if (i==rootFolders.length-1) {
							// Now we're screwed
							throw new RuntimeException("Unable to read any drive");
						}
					}					
				}
		}

		locationField.setText(currentFolder.getAbsolutePath(true));
				
		scrollPane = new JScrollPane(fileTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
			public Insets getInsets() {
				return new Insets(0, 0, 0, 0);
			}
		
			public Border getBorder() {
				return null;
			}
		};
		scrollPane.getViewport().setBackground(backgroundColor);
		add(scrollPane, BorderLayout.CENTER);
	
		// Listens to some configuration variables
		ConfigurationManager.addConfigurationListener(this);
	}

	
	public Border getBorder() {
		return null;
	}
	

    public FileTable getFileTable() {
        return fileTable;
    }

	public MainFrame getMainFrame() {
		return mainFrame;
	}

	
	public void addLocationListener(LocationListener listener) {
		locationListeners.add(listener);
	}

	public void removeLocationListener(LocationListener listener) {
		locationListeners.remove(listener);
	}

	
	public void showRootBox() {
		rootPopup.show(rootButton, 0, rootButton.getHeight());		
//		rootPopup.requestFocus();
		FocusRequester.requestFocus(rootPopup);
	}
	

	public AbstractFile getCurrentFolder() {
		return currentFolder;
	}

	
	public boolean hasFocus() {
		return super.hasFocus() || locationField.hasFocus() || fileTable.hasFocus() || rootPopup.hasFocus() || rootButton.hasFocus();
	}
	

	private void _setCurrentFolder(AbstractFile folder, boolean addToHistory) throws IOException {
		mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

        if(com.mucommander.Debug.ON)
            System.out.println("FolderPanel._setCurrentFolder: "+folder+" ");

		try {
			fileTable.setCurrentFolder(folder);
			this.currentFolder = folder;

			// Updates root button label if necessary
			String currentPath = currentFolder.getAbsolutePath(false).toLowerCase();
			int bestLength = rootFolders[0].getAbsolutePath(false).length();
			int bestIndex = 0;
			String temp;
			int len;
//System.out.println("currentPath "+currentPath+" rootFolders.length="+rootFolders.length);
			for(int i=0; i<rootFolders.length; i++) {
//System.out.println("rootFolder "+rootFolders[i].getAbsolutePath(false).toLowerCase());
				temp = rootFolders[i].getAbsolutePath(false).toLowerCase();
				len = temp.length();
				if (currentPath.startsWith(temp) && len>bestLength) {
					bestIndex = i;
					bestLength = len;
				}
			}

			rootButton.setText(rootFolders[bestIndex].getName());

			locationField.setText(currentFolder.getAbsolutePath(true));
			locationField.repaint();

			if (addToHistory) {
				historyIndex++;

				// Deletes 'forward' history items if any
				int size = history.size();
				for(int i=historyIndex; i<size; i++) {
					history.removeElementAt(historyIndex);
				}
				// Inserts previous folder in history
				history.add(folder);
			}

			// Saves last folder recallable on startup
			if(!(folder instanceof RemoteFile))
				this.lastFolderOnExit = folder.getAbsolutePath();
			
			// Notifies listeners that location has changed
			fireLocationChanged();
		}
		catch(IOException e) {
			mainFrame.setCursor(Cursor.getDefaultCursor());
			if(com.mucommander.Debug.ON) e.printStackTrace();
			throw e;
		}
		mainFrame.setCursor(Cursor.getDefaultCursor());
	}


	/**
	 * Notifies all listeners that have registered interest for notification on this event type.
	 */
	public void fireLocationChanged() {
		for(int i=0; i<locationListeners.size(); i++)
			((LocationListener)locationListeners.elementAt(i)).locationChanged(this);
	}

	
	/**
	 * Displays a popup message notifying the user that the request folder couldn't be opened.
	 */
	private void showFolderAccessError(IOException e) {
		String exceptionMsg = e==null?null:e.getMessage();
		String errorMsg = Translator.get("table.folder_access_error")+(exceptionMsg==null?".":": "+exceptionMsg);
		if(!errorMsg.endsWith("."))
			errorMsg += ".";
		JOptionPane.showMessageDialog(mainFrame, errorMsg, Translator.get("table.folder_access_error_title"), JOptionPane.ERROR_MESSAGE);
	}

	
	/**
	 * Returns <code>true</code> if the folder was correctly set, <code>false</code> if
	 * an Exception has been thrown and a error message has been displayed to the end user.
	 */
	public boolean setCurrentFolder(AbstractFile folder, boolean addToHistory) {
		boolean success = false;

        if(com.mucommander.Debug.ON)
            System.out.println("FolderPanel.setCurrentFolder: "+folder+" ");
        
		if (folder==null || !folder.exists()) {
			JOptionPane.showMessageDialog(mainFrame, Translator.get("table.folder_does_not_exist"), Translator.get("table.folder_access_error_title"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		boolean hadFocus = fileTable.hasFocus();

		try {
			_setCurrentFolder(folder, addToHistory);
			success = true;
		}
        catch(IOException e) {
        	showFolderAccessError(e);
		}
    
        if(hadFocus || mainFrame.getLastActiveTable()==fileTable)
			fileTable.requestFocus();

		return success;
	}
	
	
	public boolean goBack() {

		if (historyIndex==0)
			return false;
		
		boolean success = false;
		try {
			_setCurrentFolder((AbstractFile)history.elementAt(historyIndex-1), false);
			historyIndex--;

			success = true;
		}
		catch(IOException e) {
//		    JOptionPane.showMessageDialog(mainFrame, "Unable to access folder contents.", "Access error", JOptionPane.ERROR_MESSAGE);
			showFolderAccessError(e);
		}
	
		// Notifies listeners that location has changed
		fireLocationChanged();

		fileTable.requestFocus();

		return success;
	}
	
	public boolean goForward() {
		if (historyIndex==history.size()-1)
			return false;
		
		boolean success = false;
		try {
			_setCurrentFolder((AbstractFile)history.elementAt(historyIndex+1), false);
			historyIndex++;

			success = true;
		}
		catch(IOException e) {
//		    JOptionPane.showMessageDialog(mainFrame, "Unable to access folder contents.", "Access error", JOptionPane.ERROR_MESSAGE);
			showFolderAccessError(e);
		}

		// Notifies listeners that location has changed
		fireLocationChanged();

		fileTable.requestFocus();

		return success;
	}

	/**
	 * Returns <code>true</code> if there is at least one folder 'back' in the history.
	 */
	public boolean hasBackFolder() {
		return historyIndex!=0;
	}

	/**
	 * Returns <code>true</code> if there is at least one folder 'forward' in the history.
	 */
	public boolean hasForwardFolder() {
		return historyIndex!=history.size()-1;
	}
	
	
	/**
	 * Returns the last folder the user went to before quitting the application.
	 * This folder will be loaded on next mucommander startup, so the returned folder
	 * should NOT be a folder on a remote filesystem (likely not to be reachable).
	 */
	public String getLastSavableFolder() {
		return this.lastFolderOnExit;
	}
	
	
	/**
	 * Refreshes this panel's components: file table, root button and location field
	 * and notifies the user if current folder could not be refreshed.
	 */
	public void refresh() {
		rootButton.repaint();
		locationField.repaint();
	
		if(!fileTable.refresh())
			showFolderAccessError(null);
	}


	/**
	 * This method must be called when this FolderPanel isn't used anymore, otherwise
	 * resources associated to this FolderPanel won't be released.
	 */
	public void dispose() {
		ConfigurationManager.removeConfigurationListener(this);
		fileTable.dispose();
	}
	
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == locationField) {
			String location = locationField.getText();

			AbstractFile file = AbstractFile.getAbstractFile(location);
		
			boolean browse = false;
			if(file==null) {
				// Restore current folder's path
				locationField.setText(currentFolder.getAbsolutePath(true));
				showFolderAccessError(null);
				return;
			}
			else if(file.isDirectory()) {
				// Browse directory
				browse = true;
			}
			else if(file.isBrowsable()) {
				// Copy or browse file ?
				QuestionDialog dialog = new QuestionDialog(mainFrame, 
				null,
				Translator.get("table.copy_or_browse"),
				mainFrame,
				new String[] {BROWSE_TEXT, COPY_TEXT, CANCEL_TEXT},
				new int[] {BROWSE_ACTION, COPY_ACTION, CANCEL_ACTION},
				0);

				int ret = dialog.getActionValue();
				if(ret==-1 || ret==CANCEL_ACTION)
					return;
				if(ret==BROWSE_ACTION)
					browse = true;
			}
			// else copy file
			
			if(browse) {
				// If folder could not be set, restore current folder's path
				if(!setCurrentFolder(file, true))
					locationField.setText(currentFolder.getAbsolutePath(true));
			}
			else {
				Vector fileV = new Vector();
				fileV.add(file);
				
				// Show confirmation/path modification dialog
				if(file instanceof HTTPFile)
					new DownloadDialog(mainFrame, fileV);
				else
					new CopyDialog(mainFrame, fileV, false);
					
				// Restore current folder's path
				locationField.setText(currentFolder.getAbsolutePath(true));
			}
		}
		else if (source == rootButton)	 {
			showRootBox();
		}
		// root menu items
		else {		
			if (rootMenuItems.indexOf(source)!=-1) {
				int index = rootMenuItems.indexOf(source);

				// Tries to change current folder
				if (setCurrentFolder(rootFolders[index], true)) {
					// if success, hide popup
					rootPopup.setVisible(false);
					
					// and request focus on this file table
					fileTable.requestFocus();
				}
			}
		}
	}

	/***********************
	 * KeyListener methods *
	 ***********************/

	public void keyPressed(KeyEvent e) {
		if (e.getSource()==locationField) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				// Restore current location string
				locationField.setText(currentFolder.getAbsolutePath(true));
				fileTable.requestFocus();
			}
		}
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}


	/*****************************
	 * PopupMenuListener methods *
	 *****************************/
	 
	 public void popupMenuCanceled(PopupMenuEvent e) {
	 }

	 public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		fileTable.requestFocus();
	 }

	 public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	 }




    /**
     * Listens to certain configuration variables.
     */
    public boolean configurationChanged(ConfigurationEvent event) {
    	String var = event.getVariable();
    
		if (var.equals("prefs.colors.background"))  {
			scrollPane.getViewport().setBackground(backgroundColor=FileTableCellRenderer.getColor(event.getValue()));
			repaint();    		
		}
		
    	return true;
    }
}