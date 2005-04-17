
package com.mucommander.ui;

import com.mucommander.file.*;
import com.mucommander.ui.table.*;
import com.mucommander.ui.comp.FocusRequester;
import com.mucommander.ui.comp.dialog.*;
import com.mucommander.ui.comp.ProgressTextField;

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
	private ChangeFolderThread changeFolderThread;

    // Registered LocationListeners
    private Vector locationListeners = new Vector();
	
	/*  We're NOT using JComboBox anymore because of its strange behavior: 
		it calls actionPerformed() each time an item is highlighted with the arrow (UP/DOWN) keys,
		so there is no way to tell if it's the final selection (ENTER) or not.
	*/
	private DriveButton driveButton;
//	private JTextField locationField;
	private ProgressTextField locationField;
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



	private class ChangeFolderThread extends Thread {
	
		private AbstractFile folder;
		private String folderPath;
		private FileURL folderURL;
		private boolean addToHistory;
//		private AbstractFile fileToSelect;
		
		private boolean isKilled;
		private boolean doNotKill;
		
//		private boolean noWaitDialog;
//		private QuestionDialog waitDialog;
	
		private Object lock = new Object();	

		private boolean hadFocus;
	
		
		public ChangeFolderThread(AbstractFile folder, boolean addToHistory) {
			this.folder = folder;
			this.addToHistory = addToHistory;

			setPriority(MAX_PRIORITY);
		}

		public ChangeFolderThread(String folderPath, boolean addToHistory) {
			this.folderPath = folderPath;
			this.addToHistory = addToHistory;

			setPriority(MAX_PRIORITY);
		}


		public ChangeFolderThread(FileURL folderURL, boolean addToHistory) {
			this.folderURL = folderURL;
			this.addToHistory = addToHistory;

			setPriority(MAX_PRIORITY);
		}
	
	
//		public void setFileToSelect(AbstractFile fileToSelect) {
//			this.fileToSelect = fileToSelect;
//		}
		
	
		public void tryKill() {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");
			if(isKilled)
				return;

			synchronized(lock) {
				isKilled = true;
				if(!doNotKill) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("killing thread");
					super.stop();

/*
	if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("waitdialog = "+waitDialog);
					if(waitDialog!=null) {
	if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("calling dispose() on waitdialog");
						waitDialog.dispose();
						waitDialog = null;
					}
*/
					// post processing as it would normally have been done by run()
					finish();
				}
			}
		}
	
		
	
		private void enableNoEventsMode() {
			// Prevents mouse/keybaord events from reaching the application and display hourglass/wait cursor
			mainFrame.setNoEventsMode(true);

			// Catch escape key clicks and have them close the dialog
			// by mapping the escape keystroke to a custom Action
			JPanel contentPane = (JPanel)mainFrame.getContentPane();
			InputMap inputMap = contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
			ActionMap actionMap = contentPane.getActionMap();
			AbstractAction killAction = new AbstractAction() {
				public void actionPerformed(ActionEvent e){
		if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("escape pressed");
					tryKill();
				}
			};
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "customEscapeAction");
			actionMap.put("customEscapeAction", killAction);
		}
		

		private void disableNoEventsMode() {
			// Restore mouse/keybaord events and default cursor
			mainFrame.setNoEventsMode(false);
			// Remove 'escape' action
			JPanel contentPane = (JPanel)mainFrame.getContentPane();
			contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
			contentPane.getActionMap().remove("customEscapeAction");
		}
		
		
		public void run() {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("run starts");

			// Show some progress in the progress bar to give hope
			locationField.setProgressValue(10);

			mainFrame.setStatusBarText(Translator.get("status_bar.connecting_to_folder"));

/*
			// Start a new thread which will popup a dialog after a number of seconds
			new Thread() {
				public void run() {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("starting and waiting");

					try { sleep(3000); }
					catch(InterruptedException e) {}

					while(noWaitDialog) {
						try { sleep(200); }
						catch(InterruptedException e) {}
					}

					synchronized(lock) {
						if(changeFolderThread==null || changeFolderThread!=ChangeFolderThread.this || isKilled || !ChangeFolderThread.this.isAlive())
							return;

						YBoxPanel panel = new YBoxPanel();
						panel.add(new JLabel(Translator.get("table.connecting_to_folder")));
						panel.addSpace(5);
						JProgressBar fullProgressBar = new JProgressBar();
						fullProgressBar.setValue(50);
						panel.add(fullProgressBar);

						// Download or browse file ?
						waitDialog = new QuestionDialog(mainFrame, 
						null,
						panel,
						mainFrame,
						new String[] {CANCEL_TEXT},
						new int[] {CANCEL_ACTION},
						0);
					}

					int ret = waitDialog.getActionValue();
					if(ret==-1 || ret==CANCEL_ACTION)
						tryKill();
				}
			}.start();
*/			

			// Disable automatic refresh
			fileTable.setAutoRefreshActive(false);

			// Save focus state
			this.hadFocus = fileTable.hasFocus();
			do {
//				noWaitDialog = false;

				// Set cursor to hourglass/wait
				mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

				// Prevents mouse and keybaord events from reaching the main frame and menus
				enableNoEventsMode();

				try {
					if(folder==null) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("calling getAbstractFile()");
						AbstractFile file;
						if(folderURL!=null)
							file = AbstractFile.getAbstractFile(folderURL, true);
						else
							file = AbstractFile.getAbstractFile(folderPath, true);

						// File resolved -> 25% complete
						locationField.setProgressValue(25);

						synchronized(lock) {
							if(isKilled) {
	if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("killed, get out");
								break;
							}
						}

						if(file==null || !file.exists()) {
//							noWaitDialog = true;

							// Restore default cursor
							mainFrame.setCursor(Cursor.getDefaultCursor());

							// Keep the text input by the user (do not restore current path) to give him a change to correct it
							locationFieldTextSet = true;
							JOptionPane.showMessageDialog(mainFrame, Translator.get("table.folder_does_not_exist"), Translator.get("table.folder_access_error_title"), JOptionPane.ERROR_MESSAGE);
							break;
						}

						boolean browse = false;
						if(file.isDirectory()) {
							// Just continue
						}
						else if(file.isBrowsable()) {
//							noWaitDialog = true;

							// Restore default cursor
							mainFrame.setCursor(Cursor.getDefaultCursor());

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
								break;
							
							// Download file
							if(ret==DOWNLOAD_ACTION) {
//								noWaitDialog = true;

								FileSet fileSet = new FileSet(currentFolder);
								fileSet.add(file);
								
								// Show confirmation/path modification dialog
								new DownloadDialog(mainFrame, fileSet);
									
								// Restore current folder's path
								locationField.setText(currentFolder.getAbsolutePath());

								break;
							}
							// Continue if BROWSE_ACTION
							// Set cursor to hourglass/wait
							mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
//							noWaitDialog = false;
						}
					
						this.folder = file;
					}

					// File tested -> 50% complete
					locationField.setProgressValue(50);

if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("calling ls()");
					AbstractFile children[] = folder.ls();

					synchronized(lock) {
						if(isKilled) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("killed, get out");
							break;
						}
						doNotKill = true;
					}

					// files listed -> 75% complete
					locationField.setProgressValue(75);
					
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("calling setCurrentFolder");
					setCurrentFolder(folder, children, addToHistory);

					// folder set -> 100% complete
					locationField.setProgressValue(95);
					
//if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("fileToSelect= ");
//					// Select specified file if there is one
//					if(fileToSelect!=null)
//						fileTable.selectFile(fileToSelect);

//fileTable.center();
//fileTable.selectFile(((FileTableModel)fileTable.getModel()).getFileAtRow(fileTable.getSelectedRow()));
					
					break;
				}
				catch(IOException e) {
//					noWaitDialog = true;

					// Restore default cursor
					mainFrame.setCursor(Cursor.getDefaultCursor());
					
					// Retry (loop) if user authentified
					if(showAccessErrorDialog(e)) {
						if(folderURL!=null)
							folder = AbstractFile.getAbstractFile(folderURL);
						else if(folder!=null)
							folder = AbstractFile.getAbstractFile(folder.getURL());
						else
							folder = AbstractFile.getAbstractFile(folderPath);

						continue;
					}
					break;
				}
			}
			while(true);

//			noWaitDialog = true;

			synchronized(lock) {
/*
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("waitdialog = "+waitDialog);
				if(waitDialog!=null) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("calling dispose() on waitdialog");
					waitDialog.dispose();
					waitDialog = null;
				}
*/				
				// Clean things up
				finish();

				// Reset 
				locationField.setProgressValue(0);
				locationField.paintImmediately(0, 0, locationField.getWidth(), locationField.getHeight());
			}
		}
	
	
		public void finish() {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("cleaning up and restoring focus...");
			mainFrame.setCursor(Cursor.getDefaultCursor());

			// Restore focus on table if it had it before
			if(hadFocus || mainFrame.getLastActiveTable()==fileTable)
				fileTable.requestFocus();

			// Re-enable automatic refresh
			fileTable.setAutoRefreshActive(true);

			changeFolderThread = null;

			// Restore mouse/keybaord events and default cursor
			disableNoEventsMode();
		}
	}


	
	static {
		// Set background color
		backgroundColor = FileTableCellRenderer.getColor("prefs.colors.background", "000084");
	}


	public FolderPanel(MainFrame mainFrame, AbstractFile initialFolder) {
		super(new BorderLayout());

if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(" initialFolder="+initialFolder);
        this.mainFrame = mainFrame;

		XBoxPanel locationPanel = new XBoxPanel();
		locationPanel.setInsets(new Insets(0, 6, 6, 0));

		this.driveButton = new DriveButton(this);
		// Listen to location changed events to update button's text when folder changes
		addLocationListener(driveButton);
		
		locationPanel.add(driveButton);
		locationPanel.addSpace(6);

//		locationField = new JTextField();
		locationField = new ProgressTextField(0, new Color(0, 255, 255, 64));
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
//			_setCurrentFolder(initialFolder, true);
			setCurrentFolder(initialFolder, initialFolder.ls(), true);
		}
		catch(Exception e) {
			AbstractFile rootFolders[] = RootFolders.getRootFolders();
			// If that failed, try to read any other drive
			for(int i=0; i<rootFolders.length; i++) {
				try  {
//					_setCurrentFolder(rootFolders[i], true);
					setCurrentFolder(rootFolders[i], rootFolders[i].ls(), true);
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

		locationField.setText(currentFolder.getAbsolutePath());
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
//		scrollPane.addComponentListener(fileTable);
		add(scrollPane, BorderLayout.CENTER);

if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("scrollPane size="+scrollPane.getSize());

		// Listens to some configuration variables
		ConfigurationManager.addConfigurationListener(this);
	}

	
	public Border getBorder() {
		return null;
	}
	
//  Not needed, default insets seem to be null
//	public Insets getInsets() {
//		return new Insets(0, 0, 0, 0);
//	}

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

	
	/**
	 * Notifies all listeners that have registered interest for notification on this event type.
	 */
	public void fireLocationChanged() {
		for(int i=0; i<locationListeners.size(); i++)
			((LocationListener)locationListeners.elementAt(i)).locationChanged(this);
	}

	
	/**
	 * Depending on the given IOException kind :
	 * - if AuthException : pops up an authentication window where the user can enter a new login and password
	 * - if any other IOException : displays a popup message notifying the user that the request folder couldn't be opened.
	 *
	 * @return <code>true</code> if the exception was due to authentication and the user was asked to authentify, in that case, caller may want to try and read the folder again.
	 */
	private boolean showAccessErrorDialog(IOException e) {
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
	 * Tries to change current folder, adding current folder to history if requested.
	 * If something goes wrong, the user is notified by a dialog.
	 * This method creates a separate thread (which will take care of the actual folder change) and returns immediately.
	 */
	public void trySetCurrentFolder(AbstractFile folder, boolean addToHistory) {
//		trySetCurrentFolder(folder, addToHistory, null);
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("folder="+folder+" ");

if(changeFolderThread!=null && com.mucommander.Debug.ON) com.mucommander.Debug.trace(">>>>>>>>> THREAD NOT NULL = "+changeFolderThread);
		
		if (folder==null || !folder.exists()) {
			JOptionPane.showMessageDialog(mainFrame, Translator.get("table.folder_does_not_exist"), Translator.get("table.folder_access_error_title"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		this.changeFolderThread = new ChangeFolderThread(folder.getURL(), addToHistory);
		changeFolderThread.start();
	}

	
	
	public void trySetCurrentFolder(String folderPath, boolean addToHistory) {
		this.changeFolderThread = new ChangeFolderThread(folderPath, addToHistory);
		changeFolderThread.start();
	}


	public void trySetCurrentFolder(FileURL folderURL, boolean addToHistory) {
		this.changeFolderThread = new ChangeFolderThread(folderURL, addToHistory);
		changeFolderThread.start();
	}
	

	/**
	 * Refreshes file table contents and notifies the user if current folder could not be refreshed.
	 */
	 public void tryRefresh() {
		trySetCurrentFolder(currentFolder, false);
	 }
		

	public void setCurrentFolder(AbstractFile folder, AbstractFile children[], boolean addToHistory) {
//		// Set 'wait' cursor
//		mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

//		try {
			fileTable.setCurrentFolder(folder, children);
			this.currentFolder = folder;

			// Update location field with new current folder's path
			locationField.setText(currentFolder.getAbsolutePath());

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
			if(folder.getURL().getProtocol().equals("file") && folder.isDirectory())
				this.lastFolderOnExit = folder.getAbsolutePath();
			
			// Notifies listeners that location has changed
			fireLocationChanged();
//		}
//		catch(IOException e) {
//			if(com.mucommander.Debug.ON) e.printStackTrace();
//			throw e;
//		}
//		finally {
			// Restore cursor to default, no matter what happened before
//			mainFrame.setCursor(Cursor.getDefaultCursor());
//		}
	}


	public void setCurrentFolder(AbstractFile folder, boolean addToHistory) throws IOException {
		setCurrentFolder(folder, folder.ls(), addToHistory);
	}


	public void refresh() throws IOException {
		setCurrentFolder(currentFolder, false);
	}
	
	
	public void goBack() {

		if (historyIndex==0)
//			return false;
			return;
		
		AbstractFile folder = (AbstractFile)history.elementAt(--historyIndex);
		trySetCurrentFolder(folder, false);

/*
		boolean success = false;
		do {
			try {
				_setCurrentFolder(folder, false);
				success = true;
			}
			catch(IOException e) {
				// Retry (loop) if user authentified
				if(showAccessErrorDialog(e)) {
					folder = AbstractFile.getAbstractFile(folder.getAbsolutePath());
					continue;
				}
			}
			break;
		}
		while(true);
		// Transfer focus from back button back to file table
		fileTable.requestFocus();
*/		

//		return success;
	}
	
	public void goForward() {
		if (historyIndex==history.size()-1)
//			return false;
			return;
		
		AbstractFile folder = (AbstractFile)history.elementAt(++historyIndex);
		trySetCurrentFolder(folder, false);

/*
		boolean success = false;
		do {
			try {
				_setCurrentFolder(folder, false);
				success = true;
			}
			catch(IOException e) {
				// Retry (loop) if user authentified
				if(showAccessErrorDialog(e)) {
					folder = AbstractFile.getAbstractFile(folder.getAbsolutePath());
					continue;
				}
			}
			break;
		} while(true);
			
		// Transfer focus from forward button back to file table
		fileTable.requestFocus();
*/

//		return success;
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
			
			trySetCurrentFolder(location, true);

//			(this.changeFolderThread = new ChangeFolderThread(location, true)).start();

/*
			AbstractFile file = null;
			do {
				try {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("calling getAbstractFile");
					file = AbstractFile.getAbstractFile(location, true);
 				}
				catch(IOException ex) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("exception thrown");
					// Retry (loop) if user authentified
					if(showAccessErrorDialog(ex))
						continue;
					else return;
				}
				break;
			}
			while(true);

if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("file = "+file);

			boolean browse = false;
			if(file==null || !file.exists()) {
				// Keep the text input by the user (do not restore current path) to give him a change to correct it
				locationFieldTextSet = true;
				JOptionPane.showMessageDialog(mainFrame, Translator.get("table.folder_does_not_exist"), Translator.get("table.folder_access_error_title"), JOptionPane.ERROR_MESSAGE);
				return;
			}

			if(file.isDirectory()) {
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
					locationField.setText(currentFolder.getAbsolutePath());
			}
			else {
				FileSet fileSet = new FileSet(currentFolder);
				fileSet.add(file);
				
				// Show confirmation/path modification dialog
				new DownloadDialog(mainFrame, fileSet);
					
				// Restore current folder's path
				locationField.setText(currentFolder.getAbsolutePath());
			}
*/
		}
	}

	/////////////////////////
	// KeyListener methods //
	/////////////////////////

	public void keyPressed(KeyEvent e) {
		if (e.getSource()==locationField) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				// Restore current location string
				locationField.setText(currentFolder.getAbsolutePath());
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
			locationField.setText(currentFolder.getAbsolutePath());
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
    
		// Set new background color and repaint panel
		if (var.equals("prefs.colors.background"))  {
			scrollPane.getViewport().setBackground(backgroundColor=FileTableCellRenderer.getColor(event.getValue()));
			repaint();    		
		}
		// Refresh to show or hide hidden files, depending on new preference
		else if (var.equals("prefs.show_hidden_files")) {
			tryRefresh();
		}
		
    	return true;
    }
}