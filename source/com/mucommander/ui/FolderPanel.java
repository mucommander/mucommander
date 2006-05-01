
package com.mucommander.ui;

import com.mucommander.ui.table.*;
import com.mucommander.ui.comp.FocusRequester;
import com.mucommander.ui.comp.dialog.*;
import com.mucommander.ui.comp.progress.ProgressTextField;

import com.mucommander.event.*;
import com.mucommander.file.*;
import com.mucommander.conf.*;
import com.mucommander.text.Translator;
import com.mucommander.PlatformManager;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.*;

/**
 * 
 *
 * @author Maxence Bernard
 */
public class FolderPanel extends JPanel implements ActionListener, KeyListener, FocusListener, ConfigurationListener {
	
	private MainFrame mainFrame;

    private AbstractFile currentFolder;
	private ChangeFolderThread changeFolderThread;

    /** Contains all registered location listeners, stored as weak references */
    private WeakHashMap locationListeners = new WeakHashMap();
	
	private XBoxPanel locationPanel;
	/*  We're NOT using JComboBox anymore because of its strange behavior: 
		it calls actionPerformed() each time an item is highlighted with the arrow (UP/DOWN) keys,
		so there is no way to tell if it's the final selection (ENTER) or not.
	*/
	private DriveButton driveButton;
	private ProgressTextField locationField;
	
	private FileTable fileTable;
	private JScrollPane scrollPane;
	
    private static Color backgroundColor;

	private Vector history;
	private int historyIndex;
    
	private String lastSavableFolder;

	private Object lastFocusedComponent;
	
	private final static int CANCEL_ACTION = 0;
	private final static int BROWSE_ACTION = 1;
	private final static int DOWNLOAD_ACTION = 2;
	
	private final static String CANCEL_TEXT = Translator.get("cancel");
	private final static String BROWSE_TEXT = Translator.get("browse");
	private final static String DOWNLOAD_TEXT = Translator.get("download");


	/**
	 * This thread takes care of changing current folder without locking the main
	 * thread. The folder change can be cancelled.
	 * 
	 * <p>A little note out of nowhere: never ever call JComponent.paintImmediately() from a thread
	 * other than the Event Dispatcher Thread, as will create nasty repaint glitches that
	 * then become very hard to track. Sun's Javadoc doesn't make it clear enough... just don't!
	 * 
	 * @author Maxence Bernard
	 */
	public class ChangeFolderThread extends Thread {
	
		private AbstractFile folder;
		private String folderPath;
		private FileURL folderURL;
		private boolean addToHistory;
		private AbstractFile fileToSelect;
		
		private boolean isKilled;
		private boolean doNotKill;
		
//		private boolean noWaitDialog;
//		private QuestionDialog waitDialog;
	
		private Object lock = new Object();	

//		private boolean hadFocus;
	
		
		public ChangeFolderThread(AbstractFile folder, boolean addToHistory) {
			this.folder = folder;
			this.addToHistory = addToHistory;
			setPriority(Thread.MAX_PRIORITY);
		}

		public ChangeFolderThread(String folderPath, boolean addToHistory) {
			this.folderPath = folderPath;
			this.addToHistory = addToHistory;
			setPriority(Thread.MAX_PRIORITY);
		}

		public ChangeFolderThread(FileURL folderURL, boolean addToHistory) {
			this.folderURL = folderURL;
			this.addToHistory = addToHistory;
			setPriority(Thread.MAX_PRIORITY);
		}
	
		public void selectThisFileAfter(AbstractFile fileToSelect) {
			this.fileToSelect = fileToSelect;
		}	
	
		public void tryKill() {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");
			synchronized(lock) {
				if(isKilled)
					return;

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
					finish(false);
				}
			}
		}
	
	
		private void enableNoEventsMode() {
			// Prevents mouse/keybaord events from reaching the application and display hourglass/wait cursor
			mainFrame.setNoEventsMode(true);

			// Register a cutom action for the ESCAPE key which stops current folder change
			JRootPane rootPane = (JRootPane)mainFrame.getRootPane();
			InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
			ActionMap actionMap = rootPane.getActionMap();
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
			JRootPane rootPane = (JRootPane)mainFrame.getRootPane();
			rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
			rootPane.getActionMap().remove("customEscapeAction");
		}
		
		
		public void run() {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("run starts");
			boolean folderChangedSuccessfully = false;

			// Notify listeners that location is changing
			fireLocationChanging();

			// Set new folder's path in location field
			locationField.setText(folder==null?folderPath==null?folderURL.getStringRep(false):folderPath:folder.getAbsolutePath());

			// Show some progress in the progress bar to give hope
			locationField.setProgressValue(10);

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

//			// Save focus state
//			this.hadFocus = fileTable.hasFocus();
			do {
//				noWaitDialog = false;

				// Set cursor to hourglass/wait
				mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

				// Prevents mouse and keybaord events from reaching the main frame and menus
				enableNoEventsMode();

				try {
					// 2 cases here :
					// - Thread was created using an AbstractFile instance
					// - Thread was created using a FileURL or FolderPath, corresponding AbstractFile needs to be resolved

					// Thread was created using a FileURL or FolderPath
					if(folder==null) {
						AbstractFile file;
						if(folderURL!=null)
							file = AbstractFile.getAbstractFile(folderURL, true);
						else
							file = AbstractFile.getAbstractFile(folderPath, true);

						synchronized(lock) {
							if(isKilled) {
								if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("killed, get out");
								break;
							}
						}

						// File resolved -> 25% complete
						locationField.setProgressValue(25);

						if(file==null || !file.exists()) {
//							noWaitDialog = true;

							// Restore default cursor
							mainFrame.setCursor(Cursor.getDefaultCursor());

							JOptionPane.showMessageDialog(mainFrame, Translator.get("table.folder_does_not_exist"), Translator.get("table.folder_access_error_title"), JOptionPane.ERROR_MESSAGE);
							break;
						}

						// File is a regualar directory, all good
						if(file.isDirectory()) {
							// Just continue
						}
						// File is a browsable file (Zip archive for instance) but not a directory : Browse or Download ? => ask the user
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
								
								showDownloadDialog(file);
								break;

/*
								FileSet fileSet = new FileSet(currentFolder);
								fileSet.add(file);
								
								// Show confirmation/path modification dialog
								new DownloadDialog(mainFrame, fileSet);

								break;
*/
							}
							// Continue if BROWSE_ACTION
							// Set cursor to hourglass/wait
							mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
//							noWaitDialog = false;
						}
						// File is a regular file: show download dialog
						else {
							showDownloadDialog(file);
							break;
						}
					
						this.folder = file;
					}
					// Thread was created using an AbstractFile instance, check for existence
					else if(!folder.exists()) {
						JOptionPane.showMessageDialog(mainFrame, Translator.get("table.folder_does_not_exist"), Translator.get("table.folder_access_error_title"), JOptionPane.ERROR_MESSAGE);
						break;
					}

					synchronized(lock) {
						if(isKilled) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("killed, get out");
							break;
						}
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
						// From now on, thread cannot be killed (would comprise table integrity)
						doNotKill = true;
					}

					// files listed -> 75% complete
					locationField.setProgressValue(75);
					
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("calling setCurrentFolder");
					setCurrentFolder(folder, children, addToHistory, fileToSelect);

					// folder set -> 95% complete
					locationField.setProgressValue(95);
					
					// All good !
					folderChangedSuccessfully = true;

					break;
				}
				catch(IOException e) {
if(com.mucommander.Debug.ON) { com.mucommander.Debug.trace("IOException caught: "); e.printStackTrace(); }
//					noWaitDialog = true;
					
					// Restore default cursor
					mainFrame.setCursor(Cursor.getDefaultCursor());

					if(e instanceof AuthException) {
						AuthException authException = (AuthException)e;
						// Retry (loop) if user authentified
						if(showAuthDialog(authException)) {
							folder = AbstractFile.getAbstractFile(authException.getFileURL().getStringRep(false));
							continue;
						}
					}
					else {
						showAccessErrorDialog(e);				
					}
					
					// Break!
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
				finish(folderChangedSuccessfully);
			}
		}
	
	
		public void finish(boolean folderChangedSuccessfully) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("cleaning up and restoring focus...");
			// Reset location field's progress bar
			locationField.setProgressValue(0);

			// Restore normal mouse cursor
			mainFrame.setCursor(Cursor.getDefaultCursor());

			// Re-enable automatic refresh
			fileTable.setAutoRefreshActive(true);

			changeFolderThread = null;

			// Restore mouse/keybaord events and default cursor
			disableNoEventsMode();

			if(!folderChangedSuccessfully) {
				// Restore current folder's path
				locationField.setText(currentFolder.getAbsolutePath());

				// Notifies listeners that location change has been cancelled
				fireLocationCancelled();
			}

			// /!\ Focus should be restored after disableNoEventsMode has been called
			// Use FocusRequester to request focus after all other UI events have been processed,
			// calling requestFocus() on table directly could get ignored 
			FocusRequester.requestFocus(mainFrame.getLastActiveTable());
		}
	}


	
	static {
		// Set background color
		backgroundColor = ConfigurationManager.getVariableColor("prefs.colors.background", "000084");
	}


	public FolderPanel(MainFrame mainFrame, AbstractFile initialFolder) {
		super(new BorderLayout());

if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(" initialFolder="+initialFolder);
        this.mainFrame = mainFrame;

		this.locationPanel = new XBoxPanel();
		locationPanel.setInsets(new Insets(0, 6, 6, 0));

		// Create and add drive button
		this.driveButton = new DriveButton(this);
		locationPanel.add(driveButton);
		locationPanel.addSpace(6);

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
			setCurrentFolder(initialFolder, initialFolder.ls(), true, null);
		}
		catch(Exception e) {
			AbstractFile rootFolders[] = RootFolders.getRootFolders();
			// If that failed, try to read any other drive
			for(int i=0; i<rootFolders.length; i++) {
				try  {
					setCurrentFolder(rootFolders[i], rootFolders[i].ls(), true, null);
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

//		// Change location field's text to reflect new current folder's path
//		locationField.setText(currentFolder.getAbsolutePath());
				
		scrollPane = new JScrollPane(fileTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
			public Insets getInsets() {
				return new Insets(0, 0, 0, 0);
			}
		
			public Border getBorder() {
				return null;
			}
		};
//		// Enable double buffering on scroll pane
//		scrollPane.setDoubleBuffered(true);
		// Set scroll pane's background color to match the one of this panel and FileTable
		scrollPane.getViewport().setBackground(backgroundColor);		

		// Catch mouse events to popup a contextual 'folder' menu
		scrollPane.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int modifiers = e.getModifiers();

				// Right-click brings a contextual popup menu
				if ((modifiers & MouseEvent.BUTTON3_MASK)!=0 || (com.mucommander.PlatformManager.getOSFamily()==com.mucommander.PlatformManager.MAC_OS_X && (modifiers & MouseEvent.BUTTON1_MASK)!=0 && e.isControlDown())) {
					AbstractFile currentFolder = getCurrentFolder();
					new TablePopupMenu(FolderPanel.this.mainFrame, currentFolder, null, false, ((FileTableModel)fileTable.getModel()).getMarkedFiles()).show(scrollPane, e.getX(), e.getY());
				}
			}
		});

		add(scrollPane, BorderLayout.CENTER);

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


	/**
	 * Notifies all registered listeners that current folder has changed on this FolderPanel.
	 */
	private void fireLocationChanged() {
		Iterator iterator = locationListeners.keySet().iterator();
		while(iterator.hasNext())
			((LocationListener)iterator.next()).locationChanged(new LocationEvent(this));
	}

	/**
	 * Notifies all registered listeners that folder change has been cancelled.
	 */
	private void fireLocationCancelled() {
		Iterator iterator = locationListeners.keySet().iterator();
		while(iterator.hasNext())
			((LocationListener)iterator.next()).locationCancelled(new LocationEvent(this));
	}


	/**
	 * Causes the DriveButton to popup and show root folder, bookmarks, server shortcuts...
	 */
	public void popDriveButton() {
		driveButton.popup();
	}
	
	
	/**
	 * Allows the user to easily change the current folder and type a new one: requests focus 
	 * on the location field and selects the folder string.
	 */
	public void changeFolder() {
		locationField.setSelectionStart(0);
		locationField.setSelectionEnd(locationField.getText().length());
		locationField.requestFocus();
	}
	

	/**
	 * Returns the folder that is currently being displayed by this FolderPanel.
	 */
	public synchronized AbstractFile getCurrentFolder() {
		return currentFolder;
	}

	
	/**
	 * Displays a popup message notifying the user that the request folder couldn't be opened.
	 */
	private void showAccessErrorDialog(IOException e) {
		String exceptionMsg = e==null?null:e.getMessage();
		String errorMsg = Translator.get("table.folder_access_error")+(exceptionMsg==null?".":": "+exceptionMsg);
		if(!errorMsg.endsWith("."))
			errorMsg += ".";

		JOptionPane.showMessageDialog(mainFrame, errorMsg, Translator.get("table.folder_access_error_title"), JOptionPane.ERROR_MESSAGE);
	}


	/**
	 * Pops up an authentication dialog where the user can enter a new login and password.
	 *
	 * @return true if the user entered a (potentially) new login and password and pressed OK.
	 */
	private boolean showAuthDialog(AuthException e) {
		AuthDialog authDialog = new AuthDialog(mainFrame, e);
		authDialog.showDialog();
		return authDialog.okPressed();
	}


	/**
	 * Displays a download dialog box where the user can choose where to download the given file
	 * or cancel the operation.
	 */
	private void showDownloadDialog(AbstractFile file) {
		FileSet fileSet = new FileSet(currentFolder);
		fileSet.add(file);
		
		// Show confirmation/path modification dialog
		new DownloadDialog(mainFrame, fileSet);
	}
	


	/**
	 * Tries to change current folder, adding current folder to history if specified.
	 * The user is notified by a dialog if the folder could not be changed.
	 * 
	 * <p>This method creates a separate thread (which will take care of the actual folder change) and returns immediately.
	 *
	 * @param folder folder to be made current folder. If folder is null or doesn't exist, a dialog will popup and inform the user
	 * @param addToHistory if true, folder will be added to history
	 */
	public synchronized void trySetCurrentFolder(AbstractFile folder, boolean addToHistory) {
		trySetCurrentFolder(folder, addToHistory, null);
	}

	/**
	 * Tries to change current folder, adding current folder to history if specified.
	 * The user is notified by a dialog if the folder could not be changed.
	 * 
	 * <p>This method creates a separate thread (which will take care of the actual folder change) and returns immediately.
	 *
	 * @param folder folder to be made current folder. If folder is null or doesn't exist, a dialog will popup and inform the user
	 * @param addToHistory if true, folder will be added to history
	 * @param selectThisFileAfter file to be selected after the folder has been changed (if it exists in the folder), can be null in which case FileTable rules will be used to select current file 
	 */
	public synchronized void trySetCurrentFolder(AbstractFile folder, boolean addToHistory, AbstractFile selectThisFileAfter) {
		if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("folder="+folder);

		// Make sure there is not an existing thread running,
		// this should not normally happen but if it does, report the error
		if(changeFolderThread!=null) {
			if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(">>>>>>>>> THREAD NOT NULL = "+changeFolderThread);
			return;
		}
		
//		if (folder==null || !folder.exists()) {
//			JOptionPane.showMessageDialog(mainFrame, Translator.get("table.folder_does_not_exist"), Translator.get("table.folder_access_error_title"), JOptionPane.ERROR_MESSAGE);
//			return;
//		}

		this.changeFolderThread = new ChangeFolderThread(folder, addToHistory);

		if(selectThisFileAfter!=null)
			this.changeFolderThread.selectThisFileAfter(selectThisFileAfter);

		changeFolderThread.start();
	}
	
	/**
	 * Tries to change current folder, adding current folder to history if specified.
	 * The user is notified by a dialog if the folder could not be changed.
	 * 
	 * <p>This method creates a separate thread (which will take care of the actual folder change) and returns immediately.
	 *
	 * @param folderPath folder's path to be made current folder. If this path does not resolve into an existing file, an error message will be displayed
	 * @param addToHistory if true, folder will be added to history
	 */
	public synchronized void trySetCurrentFolder(String folderPath, boolean addToHistory) {
		if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("folderPath="+folderPath);

		// Make sure there is not an existing thread running,
		// this should not normally happen but if it does, report the error
		if(changeFolderThread!=null) {
			if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(">>>>>>>>> THREAD NOT NULL = "+changeFolderThread);
			return;
		}

		this.changeFolderThread = new ChangeFolderThread(folderPath, addToHistory);
		changeFolderThread.start();
	}

	/**
	 * Tries to change current folder, adding current folder to history if specified.
	 * The user is notified by a dialog if the folder could not be changed.
	 * 
	 * <p>This method creates a separate thread (which will take care of the actual folder change) and returns immediately.
	 *
	 * @param folderURL folder's URL to be made current folder. If this URL does not resolve into an existing file, an error message will be displayed
	 * @param addToHistory if true, folder will be added to history
	 */
	public synchronized void trySetCurrentFolder(FileURL folderURL, boolean addToHistory) {
		if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("folderURL="+folderURL);

		// Make sure there is not an existing thread running,
		// this should not normally happen but if it does, report the error
		if(changeFolderThread!=null) {
			if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(">>>>>>>>> THREAD NOT NULL = "+changeFolderThread);
			return;
		}

		this.changeFolderThread = new ChangeFolderThread(folderURL, addToHistory);
		changeFolderThread.start();
	}

	/**
	 * Refreshes current folder's contents and notifies the user if current folder could not be refreshed.
	 *
	 * <p>This method creates a separate thread (which will take care of the actual folder change) and returns immediately.
	 */
	public synchronized void tryRefreshCurrentFolder() {
		trySetCurrentFolder(currentFolder, false, null);
	}

	/**
	 * Refreshes current folder's contents and notifies the user if current folder could not be refreshed.
	 *
	 * <p>This method creates a separate thread (which will take care of the actual folder change) and returns immediately.
	 *
	 * @param selectThisFileAfter file to be selected after the folder has been refreshed (if it exists in the folder), can be null in which case FileTable rules will be used to select current file 
	 */
	public synchronized void tryRefreshCurrentFolder(AbstractFile selectThisFileAfter) {
		trySetCurrentFolder(currentFolder, false, selectThisFileAfter);
	}
		
	/**
	 * Refreshes current folder's contents in the same thread and throws an IOException if current folder could not be refreshed.
	 *
	 * @throws IOException if current folder could not be refreshed.
	 */
	public synchronized void refreshCurrentFolder() throws IOException {
		setCurrentFolder(currentFolder, currentFolder.ls(), false, null);
	}


	/**
	 * Changes current folder using the given folder and files.
	 *
	 * @param folder folder to be made current folder
	 * @param children current folder's files (value of folder.ls())
	 * @param addToHistory if true, folder will be added to history
	 * @param fileToSelect file to be selected after the folder has been refreshed (if it exists in the folder), can be null in which case FileTable rules will be used to select current file
	 */
	private void setCurrentFolder(AbstractFile folder, AbstractFile children[], boolean addToHistory, AbstractFile fileToSelect) {
		fileTable.setCurrentFolder(folder, children);

		// Select given file if not null
		if(fileToSelect!=null)
			fileTable.selectFile(fileToSelect);

		this.currentFolder = folder;

		// Change location field's text to reflect new current folder's path
		locationField.setText(folder.getAbsolutePath());

		if (addToHistory) {
			historyIndex++;

			// Delete 'forward' history items if any
			int size = history.size();
			for(int i=historyIndex; i<size; i++) {
				history.removeElementAt(historyIndex);
			}
			// Insert previous folder in history
			history.add(folder);
		}

		// Save last folder recallable on startup only if :
		//  - it is a directory on a local filesytem
		//  - it doesn't look like a removable media drive (cd/dvd/floppy), especially in order to prevent
		// Java from triggering that dreaded 'Drive not ready' popup.
		if(folder.getURL().getProtocol().equals("file") && folder.isDirectory() && !((FSFile)folder.getRoot()).guessRemovableDrive()) {
			this.lastSavableFolder = folder.getAbsolutePath();
			if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("lastSavableFolder= "+lastSavableFolder);
		}

		// Notify listeners that location has changed
		fireLocationChanged();
	}


	public ChangeFolderThread getChangeFolderThread() {
		return changeFolderThread;
	}


	public synchronized void goBack() {
		if (historyIndex==0)
			return;
		
		AbstractFile folder = (AbstractFile)history.elementAt(--historyIndex);
		trySetCurrentFolder(folder, false);
	}
	
	public synchronized void goForward() {
		if (historyIndex==history.size()-1)
			return;
		
		AbstractFile folder = (AbstractFile)history.elementAt(++historyIndex);
		trySetCurrentFolder(folder, false);
	}

	public synchronized void goToParent() {
		AbstractFile parent = getCurrentFolder().getParent();
		if(parent!=null)
			trySetCurrentFolder(parent, true);	
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
	 * This folder will be loaded on next mucommander startup, so the returned folder should NOT be
	 * a folder on a remote filesystem (likely not to be reachable next time the app is started)
	 * or a removable media drive (cd/dvd/floppy) if under Windows, as it would trigger a nasty 
	 * 'drive not ready' popup dialog.
	 */
	public String getLastSavableFolder() {
		return this.lastSavableFolder;
	}
	
	
	/**
	 * Overrides JComponent's requestFocus() method to request focus
	 * on the last focused component inside this FolderPanel: on the file able or on the location field
	 */
	public void requestFocus() {
		if(!mainFrame.getNoEventsMode())
			((JComponent)lastFocusedComponent).requestFocus();
	}


	/**
	 * Registers a LocationListener to receive LocationEvents whenever the current folder
	 * of this FolderPanel has or is being changed. 
	 *
	 * <p>Listeners are stored as weak references so {@link #removeLocationListener(LocationListener) removeLocationListener()}
	 * doesn't need to be called for listeners to be garbage collected when they're not used anymore.</p>
	 */
	public synchronized void addLocationListener(LocationListener listener) {
		locationListeners.put(listener, null);
	}

	/**
	 * Unsubscribes the LocationListener as to not receive LocationEvents anymore. 
	 */
	public synchronized void removeLocationListener(LocationListener listener) {
		locationListeners.remove(listener);
	}

	/**
	 * Notifies all registered listeners that current folder is being changed on this FolderPanel.
	 */
	private synchronized void fireLocationChanging() {
		Iterator iterator = locationListeners.keySet().iterator();
		while(iterator.hasNext())
			((LocationListener)iterator.next()).locationChanging(new LocationEvent(this));
	}

	////////////////////////////
	// ActionListener methods //
	////////////////////////////
	
	public void actionPerformed(ActionEvent e) {
		// Discard action events while in 'no events mode'
		if(mainFrame.getNoEventsMode())
			return;

		Object source = e.getSource();

		if (source == locationField) {
			String location = locationField.getText();
			
			trySetCurrentFolder(location, true);
		}
	}

	/////////////////////////
	// KeyListener methods //
	/////////////////////////

	public void keyPressed(KeyEvent e) {
		if (e.getSource()==locationField) {
			// Restore current location string if ESC was pressed
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
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
		
		// Notify MainFrame that we are in control now! (our table/location field is active)
		mainFrame.setLastActiveTable(fileTable);
	}
	
	public void focusLost(FocusEvent e) {
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
			scrollPane.getViewport().setBackground(backgroundColor=event.getColorValue());
			repaint();    		
		}
		// Refresh to show or hide hidden files, depending on new preference
		else if (var.equals("prefs.file_table.show_hidden_files")) {
			// Refresh current folder in a separate thread
			tryRefreshCurrentFolder();
		}
		
    	return true;
    }
}