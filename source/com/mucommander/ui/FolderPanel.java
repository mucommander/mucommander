
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


public class FolderPanel extends JPanel implements ActionListener, KeyListener, FocusListener, ConfigurationListener {
	private MainFrame mainFrame;
    
    private AbstractFile currentFolder;

    // Registered LocationListeners
    private Vector locationListeners = new Vector();
	
	/*  We're NOT using JComboBox anymore because of its strange behavior: 
		it calls actionPerformed() each time an item is highlighted with the arrow (UP/DOWN) keys,
		so there is no way to tell if it's the final selection (ENTER) or not.
	*/
	private DriveButton driveButton;
	private JTextField locationField;
	private boolean locationFieldTextSet;
	
	private FileTable fileTable;
	private JScrollPane scrollPane;
	
    private static Color backgroundColor;

	private Vector history;
	private int historyIndex;
    
	private String lastFolderOnExit;

	private Object lastFocusedComponent;
	
	private final static int CANCEL_ACTION = 0;
	private final static int BROWSE_ACTION = 1;
	private final static int DOWNLOAD_ACTION = 2;
	
	private final static String CANCEL_TEXT = Translator.get("cancel");
	private final static String BROWSE_TEXT = Translator.get("browse");
	private final static String DOWNLOAD_TEXT = Translator.get("download");

	
	static {
		// Set background color
		backgroundColor = FileTableCellRenderer.getColor("prefs.colors.background", "000084");
	}

	
	public FolderPanel(MainFrame mainFrame, AbstractFile initialFolder) {
		super(new BorderLayout());

        this.mainFrame = mainFrame;

		XBoxPanel locationPanel = new XBoxPanel();
		locationPanel.setInsets(new Insets(0, 6, 6, 0));

		this.driveButton = new DriveButton(this);
		// Listen to location changed events to update button's text when folder changes
		addLocationListener(driveButton);
		
		locationPanel.add(driveButton);
		locationPanel.addSpace(6);

		locationField = new JTextField();
		locationField.addActionListener(this);
		locationField.addKeyListener(this);
		locationField.addFocusListener(this);
		locationPanel.add(locationField);

		add(locationPanel, BorderLayout.NORTH);
		
		fileTable = new FileTable(mainFrame, this);
		this.lastFocusedComponent = fileTable;
		fileTable.addFocusListener(this);
		
		// Initialize history vector
		history = new Vector();
    	historyIndex = -1;
		
		try {
			// Set initial folder to current directory
			_setCurrentFolder(initialFolder, true);
		}
		catch(Exception e) {
			AbstractFile rootFolders[] = RootFolders.getRootFolders();
			// If that failed, try to read any other drive
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
		driveButton.updateText(currentFolder);
				
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
		driveButton.popup();
	}
	

	public AbstractFile getCurrentFolder() {
		return currentFolder;
	}

	
//	public boolean hasFocus() {
//		return super.hasFocus() || locationField.hasFocus() || fileTable.hasFocus() || rootPopup.hasFocus() || rootButton.hasFocus();
//	}
	

	private void _setCurrentFolder(AbstractFile folder, boolean addToHistory) throws IOException {
		// Set 'wait' cursor
		mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

        if(com.mucommander.Debug.ON)
            System.out.println("FolderPanel._setCurrentFolder: "+folder+" ");

		try {
			fileTable.setCurrentFolder(folder);
			this.currentFolder = folder;

			// Update location field with new current folder's path
			locationField.setText(currentFolder.getAbsolutePath(true));
			locationField.repaint();

			// Update drive button to reflect new current folder
			driveButton.updateText(currentFolder);
			
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
//			if(!(folder instanceof RemoteFile))
			if(folder.getProtocol().equals("FILE"))
				this.lastFolderOnExit = folder.getAbsolutePath();
			
			// Notifies listeners that location has changed
			fireLocationChanged();
		}
		catch(IOException e) {
			if(com.mucommander.Debug.ON) e.printStackTrace();
			throw e;
		}
		finally {
			// Restore cursor to default, no matter what happened before
			mainFrame.setCursor(Cursor.getDefaultCursor());
		}
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
	 *
	 * @return <code>true</code> if the exception was due to authentication and the user was asked to authentify, in that case, caller may want to try and read the folder again.
	 */
	private boolean showFolderAccessError(IOException e) {
		if(e instanceof AuthException) {
			AuthDialog authDialog = new AuthDialog(mainFrame, (AuthException)e);
			authDialog.showDialog();
			return authDialog.okPressed();
		}
		else {
			String exceptionMsg = e==null?null:e.getMessage();
			String errorMsg = Translator.get("table.folder_access_error")+(exceptionMsg==null?".":": "+exceptionMsg);
			if(!errorMsg.endsWith("."))
				errorMsg += ".";

			JOptionPane.showMessageDialog(mainFrame, errorMsg, Translator.get("table.folder_access_error_title"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
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

		do {
			try {
				_setCurrentFolder(folder, addToHistory);
				success = true;
			}
			catch(IOException e) {
				// Retry (loop) if user authentified
				if(showFolderAccessError(e)) {
					folder = AbstractFile.getAbstractFile(folder.getAbsolutePath());
					continue;
				}
			}
			break;
		} while(true);
			
        if(hadFocus || mainFrame.getLastActiveTable()==fileTable)
			fileTable.requestFocus();

		return success;
	}
	
	
	public boolean goBack() {

		if (historyIndex==0)
			return false;
		
		boolean success = false;
		AbstractFile folder = (AbstractFile)history.elementAt(--historyIndex);
		do {
			try {
				_setCurrentFolder(folder, false);
				success = true;
			}
			catch(IOException e) {
				// Retry (loop) if user authentified
				if(showFolderAccessError(e)) {
					folder = AbstractFile.getAbstractFile(folder.getAbsolutePath());
					continue;
				}
			}
			break;
		}
		while(true);
	
		// Notifies listeners that location has changed
		fireLocationChanged();

		fileTable.requestFocus();

		return success;
	}
	
	public boolean goForward() {
		if (historyIndex==history.size()-1)
			return false;
		
		boolean success = false;
		AbstractFile folder = (AbstractFile)history.elementAt(++historyIndex);
		do {
			try {
				_setCurrentFolder(folder, false);
				success = true;
			}
			catch(IOException e) {
				// Retry (loop) if user authentified
				if(showFolderAccessError(e)) {
					folder = AbstractFile.getAbstractFile(folder.getAbsolutePath());
					continue;
				}
			}
			break;
		} while(true);
			
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
	 * Refreshes this panel's components: file table, drive button and location field
	 * and notifies the user if current folder could not be refreshed.
	 */
	 public void refresh() {
		boolean retry = false;
		
		// First try FileTable's refresh method to preserve selection
		try {
			fileTable.refresh();
			return;
		}
		catch(IOException e) {
			if(!showFolderAccessError(e))
				return;
		}

		// Retry if user authentified, using setCurrentFolder which will lose selection
		AbstractFile folder = currentFolder;
		do {
			try {
				folder = AbstractFile.getAbstractFile(folder.getAbsolutePath());
				_setCurrentFolder(folder, false);
			}
			catch(IOException e) {
				// Retry (loop) if user authentified
				if(showFolderAccessError(e))
					continue;
			}
			break;
		}
		while(true);

		driveButton.repaint();
		locationField.repaint();
	 }


	/**
	 * This method must be called when this FolderPanel isn't used anymore, otherwise
	 * resources associated to this FolderPanel won't be released.
	 */
	public void dispose() {
		ConfigurationManager.removeConfigurationListener(this);
		removeLocationListener(driveButton);
		fileTable.dispose();
	}

	
	/**
	 * Overrides JComponent's requestFocus() method to request focus
	 * on the last focused component inside this FolderPanel: on the file able or on the location field
	 */
	public void requestFocus() {
		((JComponent)lastFocusedComponent).requestFocus();
	}
	
	
	////////////////////////////
	// ActionListener methods //
	////////////////////////////
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == locationField) {
			String location = locationField.getText();

			AbstractFile file = AbstractFile.getAbstractFile(location);

			boolean browse = false;
			if(file==null || !file.exists()) {
				// Keep the text input by the user (do not restore current path)
				locationFieldTextSet = true;
				JOptionPane.showMessageDialog(mainFrame, Translator.get("table.folder_does_not_exist"), Translator.get("table.folder_access_error_title"), JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if(file.isDirectory()) {
				// Browse directory
				browse = true;
			}
			else if(file.isBrowsable()) {
				// Download or browse file ?
				QuestionDialog dialog = new QuestionDialog(mainFrame, 
				null,
				Translator.get("table.download_or_browse"),
				mainFrame,
				new String[] {BROWSE_TEXT, DOWNLOAD_TEXT, CANCEL_TEXT},
				new int[] {BROWSE_ACTION, DOWNLOAD_ACTION, CANCEL_ACTION},
				0);

				int ret = dialog.getActionValue();
				if(ret==-1 || ret==CANCEL_ACTION)
					return;
				if(ret==BROWSE_ACTION)
					browse = true;
			}
			// else download file
			
			if(browse) {
				// If folder could not be set, restore current folder's path
				if(!setCurrentFolder(file, true))
					locationField.setText(currentFolder.getAbsolutePath(true));
			}
			else {
				Vector fileV = new Vector();
				fileV.add(file);
				
				// Show confirmation/path modification dialog
//				if(file instanceof RemoteFile)	// Does not work coz file can be wrapped inside a ZipArchiveFile -> test is false 
				new DownloadDialog(mainFrame, fileV);
//				else
//					new CopyDialog(mainFrame, fileV, false);
					
				// Restore current folder's path
				locationField.setText(currentFolder.getAbsolutePath(true));
			}
		}
	}

	/////////////////////////
	// KeyListener methods //
	/////////////////////////

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

	 
	///////////////////////////
	// FocusListener methods //
	///////////////////////////
	
	public void focusGained(FocusEvent e) {
		Object source = e.getSource();
		this.lastFocusedComponent = source;
		
		if(source==locationField)
			locationFieldTextSet = false;
		
		// Notify MainFrame that we are in control now! (our table/location field is active)
		mainFrame.setLastActiveTable(fileTable);
	}
	
	public void focusLost(FocusEvent e) {
		Object source = e.getSource();

		// If location field's text was not already set between focus gained and lost 
		if(source==locationField && !locationFieldTextSet) {
			// Restore current folder's path
			locationField.setText(currentFolder.getAbsolutePath(true));
		}
	}
	
	 
	///////////////////////////////////
	// ConfigurationListener methods //
	///////////////////////////////////

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