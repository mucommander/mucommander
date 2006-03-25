package com.mucommander.ui;

import com.mucommander.PlatformManager;

import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FileTableModel;
import com.mucommander.ui.comp.FocusRequester;
import com.mucommander.ui.comp.dialog.YBoxPanel;
import com.mucommander.ui.pref.PreferencesDialog;
import com.mucommander.ui.connect.ServerConnectDialog;
import com.mucommander.ui.icon.IconManager;

import com.mucommander.event.TableChangeListener;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileSet;

import com.mucommander.conf.ConfigurationManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


/**
 * This is the main frame, which contains all other UI components visible on a mucommander window.
 * 
 * @author Maxence Bernard
 */
public class MainFrame extends JFrame implements ComponentListener, KeyListener {
	
	// Variables related to split pane	
	private JSplitPane splitPane;
	private int splitPaneWidth = -1;
	private int dividerLocation;

    private FolderPanel folderPanel1;
    private FolderPanel folderPanel2;
	
    private FileTable table1;
    private FileTable table2;
    
	/** Table that that has/had focus last */
    private FileTable lastActiveTable;

	/** Tool bar instance */
	private ToolBar toolbar;

	/** Status bar instance */
	private StatusBar statusBar;
	
	/** Command bar instance */
	private CommandBar commandBar;
	
	/** Is no events mode enabled ? */
	private boolean noEventsMode;

	/** Is this MainFrame active in the foreground ? */
	private boolean foregroundActive;

	/** Contains all registered TableChangeListener instances, stored as weak references */
	private WeakHashMap tableChangeListeners = new WeakHashMap();


	/**
	 * Creates a new main frame, set to the given initial folders.
	 */
	public MainFrame(AbstractFile initialFolder1, AbstractFile initialFolder2) {
		super();
	
		// Set frame icon fetched in an image inside the JAR file
		setIconImage(IconManager.getIcon("/icon16.gif").getImage());

		// Enable window resize
		setResizable(true);

		JPanel contentPane = new JPanel(new BorderLayout()) {
			// Add an x=3,y=3 gap around content pane
			public Insets getInsets() {
				return new Insets(3, 3, 3, 3);
			}
		};
		setContentPane(contentPane);

		// Start by creating folder panels as they are used
		// below (by Toolbar)
		this.folderPanel1 = new FolderPanel(this, initialFolder1);
        this.folderPanel2 = new FolderPanel(this, initialFolder2);

		this.table1 = folderPanel1.getFileTable();
        this.table2 = folderPanel2.getFileTable();

		// Left table is the first to be active
		this.lastActiveTable = table1;

		// Register WindowManager to receive location evvents
		WindowManager windowManager = WindowManager.getInstance();
		folderPanel1.addLocationListener(windowManager);
        folderPanel2.addLocationListener(windowManager);

		// Create toolbar and show it only if it hasn't been disabled in the preferences
		this.toolbar = new ToolBar(this);
		// Note: Toolbar.setVisible() has to be called no matter if Toolbar is visible or not, in order for it to be properly initialized
		this.toolbar.setVisible(ConfigurationManager.getVariableBoolean("prefs.toolbar.visible", true));
			
		contentPane.add(toolbar, BorderLayout.NORTH);

		folderPanel1.addLocationListener(toolbar);
		folderPanel2.addLocationListener(toolbar);

		// Register WindowManager to receive table change events
		addTableChangeListener(windowManager);
		
		// Create menu bar (has to be created after toolbar)
		MainMenuBar menuBar = new MainMenuBar(this);
		setJMenuBar(menuBar);

		// Enables folderPanel window resizing
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, folderPanel1, folderPanel2) {
			public javax.swing.border.Border getBorder() {
				return null;
			}

			// We don't want any extra space around split pane
			public Insets getInsets() {
				return new Insets(0, 0, 0, 0);
			}
		};
			
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(((double)0.5));
		// Cool but way too slow
//		splitPane.setContinuousLayout(true);

		// Split pane will be given any extra space
		contentPane.add(splitPane, BorderLayout.CENTER);

		YBoxPanel southPanel = new YBoxPanel();
		// Add a 3-pixel gap between table and status/command bar
		southPanel.setInsets(new Insets(3, 0, 0, 0));
	
		// Add status bar
		this.statusBar = new StatusBar(this);
		southPanel.add(statusBar);
		
		// Show command bar only if it hasn't been disabled in the preferences
		this.commandBar = new CommandBar(this);
		// Note: CommandBar.setVisible() has to be called no matter if CommandBar is visible or not, in order for it to be properly initialized
		this.commandBar.setVisible(ConfigurationManager.getVariableBoolean("prefs.command_bar.visible", true));

		southPanel.add(commandBar);
		
		contentPane.add(southPanel, BorderLayout.SOUTH);
		
		// To monitor resizing actions
		folderPanel1.addComponentListener(this);
		splitPane.addComponentListener(this);

        table1.addKeyListener(this);
        table2.addKeyListener(this);
    
		// Do nothing on close (default is to hide window),
		// WindowManager takes of catching close events and do the rest
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		// Set window title
		updateWindowTitle();

//		// Used by setNoEventsMode()
//		JComponent glassPane = (JComponent)getGlassPane();
//		glassPane.addMouseListener(new MouseAdapter() {});
//		glassPane.addKeyListener(new KeyAdapter() {});
//		//setNoEventsMode(true);	// for 'no events mode' testing

// For testing purposes, full screen option could be nice to add someday
//setUndecorated(true);
//java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(this);
	}


	/**
	 * Registers the given TableChangeListener to receive events when current table changes.
	 *
	 * <p>Listeners are stored as weak references so {@link #removeLocationListener(LocationListener) removeLocationListener()}
	 * doesn't need to be called for listeners to be garbage collected when they're not used anymore.</p>
	 */
	public void addTableChangeListener(TableChangeListener tableChangeListener) {
		tableChangeListeners.put(tableChangeListener, null);
	}

	/**
	 * Unregisters the given TableChangeListener so that it will no longer receive events when current table changes.
	 */
	public void removeTableChangeListener(TableChangeListener tableChangeListener) {
		tableChangeListeners.remove(tableChangeListener);
	}

	/**
	 * Fires table change events on registered TableChangeListener instances.
	 */
	private void fireTableChanged(FolderPanel folderPanel) {
		Iterator iterator = tableChangeListeners.keySet().iterator();
		while(iterator.hasNext())
			((TableChangeListener)iterator.next()).tableChanged(folderPanel);
	}


	/**
	 * Returns true if 'no events mode' is enabled.
	 */
	public boolean getNoEventsMode() {
		return this.noEventsMode;
	}
	
	/**
	 * Enables/disables 'no events mode' which prevents mouse and keyboard events from being received
	 * by the application (main frame and its subcomponents and menus).
	 */
	public void setNoEventsMode(boolean enabled) {
// Piece of code used in 0.8 beta1 and removed after because it's way too slow, kept here for the record 
//		// Glass pane has empty mouse and key adapters (created in the constructor)
//		// which will catch all mouse and keyboard events 
//		getGlassPane().setVisible(enabled);
//		getJMenuBar().setEnabled(!enabled);
//		// Remove focus from whatever component in FolderPanel which had focus
//		getGlassPane().requestFocus();

		this.noEventsMode = enabled;
	}


	/**
	 * Returns the toolbar where shortcut buttons (go back, go forward, ...) are.
	 */
	public ToolBar getToolBar() {
		return toolbar;
	}

	/**
	 * Shows/hide the toolbar.
	 */
	public void setToolbarVisible(boolean visible) {
		toolbar.setVisible(visible);
		validate();
		ConfigurationManager.setVariableBoolean("prefs.toolbar.visible", visible);		
	}
	
	/**
	 * Returns true if the icon toolbar is visible on this frame.
	 */
	public boolean isToolbarVisible() {
		return toolbar.isVisible();
	}
		

	/**
	 * Returns the command bar, i.e. the panel that contains
	 * F3, F6... F10 buttons.
	 */
	public CommandBar getCommandBar() {
		return commandBar;
	}
	
	/**
	 * Returns true if the command bar is visible on this frame.
	 */
	public boolean isCommandBarVisible() {
		return this.commandBar.isVisible();
	}

	/**
	 * Shows/hide the command bar.
	 */
	public void setCommandBarVisible(boolean visible) {
		this.commandBar.setVisible(visible);
		validate();
		ConfigurationManager.setVariableBoolean("prefs.command_bar.visible", visible);		
	}
	

	/**
	 * Returns the status bar where information about selected files and volume are displayed.
	 */
	public StatusBar getStatusBar() {
		return this.statusBar;
	}

	/**
	 * Returns true if the status bar is visible on this frame.
	 */
	public boolean isStatusBarVisible() {
		return this.statusBar.isVisible();
	}

	/**
	 * Shows/hide the status bar.
	 */
	public void setStatusBarVisible(boolean visible) {
//		// Update status bar info if status bar was previouly hidden and now visible
//		// as FileTable doesn't update status bar info when it is hidden
//		boolean updateStatusBarInfo = visible && !isStatusBarVisible();
		this.statusBar.setVisible(visible);
//		// Status bar needs to be visible before calling updateStatusBar()
//		if(updateStatusBarInfo)
//			this.statusBar.updateStatusInfo();
		validate();
		ConfigurationManager.setVariableBoolean("prefs.status_bar.visible", visible);		
	}
	

	/**
	 * Returns last active FileTable, that is the last FileTable that received focus.
	 */
	public FileTable getLastActiveTable() {
        return lastActiveTable;
    }

	/**
	 * Sets currently active FileTable (called by FolderPanel).
	 */
	void setLastActiveTable(FileTable table) {
        boolean activeTableChanged = lastActiveTable!=table;

		if(activeTableChanged) {
			this.lastActiveTable = table;

			// Fire table change events on registered TableChangeListener instances.
			fireTableChanged(table.getFolderPanel());
		}
    }

	
	/**
	 * Returns the complement to getLastActiveTable().
	 */
    public FileTable getUnactiveTable() {
        return lastActiveTable==table1?table2:table1;
    }
    
	/**
	 * Returns left FolderPanel.
	 */
	public FolderPanel getFolderPanel1() {
		return folderPanel1;
	}

	/**
	 * Returns right FolderPanel.
	 */
	public FolderPanel getFolderPanel2() {
		return folderPanel2;
	}


	/**
	 * After a call to this method, folder1 will be folder2 and vice-versa.
	 */
	public void swapFolders() {
		splitPane.remove(folderPanel1);
		splitPane.remove(folderPanel2);

		FolderPanel tempBrowser = folderPanel1;
		folderPanel1 = folderPanel2;
		folderPanel2 = tempBrowser;

		FileTable tempTable = table1;
		table1 = table2;
		table2 = tempTable;

		splitPane.setLeftComponent(folderPanel1);
		splitPane.setRightComponent(folderPanel2);
		splitPane.doLayout();
		splitPane.setDividerLocation(dividerLocation);

		requestFocus();
	}

	/**
	 * Compares directories: marks files that are missing from a directory or that are newer.
	 */
	public void compareDirectories() {
		AbstractFile tempFile;
		FileTableModel tableModel1 = (FileTableModel)table1.getModel();
		FileTableModel tableModel2 = (FileTableModel)table2.getModel();

        int nbFiles1 = tableModel1.getFileCount();
        int nbFiles2 = tableModel2.getFileCount();
		int fileIndex;
		String tempFileName;
		for(int i=0; i<nbFiles1; i++) {
			tempFile = tableModel1.getFile(i);
			if(tempFile.isDirectory())
				continue;
			
			tempFileName = tempFile.getName();
            fileIndex = -1;
			for(int j=0; j<nbFiles2; j++)
				if (tableModel2.getFile(j).getName().equals(tempFileName)) {
                    fileIndex = j;
					break;
				}
			if (fileIndex==-1 || tableModel2.getFile(fileIndex).getDate()<tempFile.getDate()) {
				tableModel1.setFileMarked(tempFile, true);
				table1.repaint();
			}
		}

		for(int i=0; i<nbFiles2; i++) {
			tempFile = tableModel2.getFile(i);
			if(tempFile.isDirectory())
				continue;

			tempFileName = tempFile.getName();
            fileIndex = -1;
			for(int j=0; j<nbFiles1; j++)
				if (tableModel1.getFile(j).getName().equals(tempFileName)) {
                    fileIndex = j;
					break;
				}
			if (fileIndex==-1 || tableModel1.getFile(fileIndex).getDate()<tempFile.getDate()) {
				tableModel2.setFileMarked(tempFile, true);
				table2.repaint();
			}
		}

	}

	/**
	 * Makes both folders the same, choosing the one which is currently active. 
	 */
	public void setSameFolder() {
		(lastActiveTable==table1?table2:table1).getFolderPanel().trySetCurrentFolder(lastActiveTable.getCurrentFolder(), false);
	}

	/**
	 * Brings up the properties dialog that displays information about currently marked files (if any).
	 */
	public void showPropertiesDialog() {
		FileSet files = getLastActiveTable().getSelectedFiles();
		if(files.size()>0)
			new PropertiesDialog(this, files).showDialog();
	}
	
	/**
	 * Brings up server connect dialog, which allows to connect to remote servers.
	 */
	public void showServerConnectDialog() {
		new ServerConnectDialog(this).showDialog();
	}
	
	/**
	 * Brings up the preferences dialog.
	 */ 
	public void showPreferencesDialog() {
		new PreferencesDialog(this).showDialog();
	}

	/**
	 * Brings up the file selection dialog.
	 */
	public void showSelectionDialog(boolean addToSelection) {
		new FileSelectionDialog(this, addToSelection).showDialog();   
	}

	/**
	 * Returns <code>true</code> if this MainFrame is active in the foreground.
	 */
	public boolean isForegroundActive() {
//if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("foregroundActive="+foregroundActive);
		return foregroundActive;
	}

	/**
	 * Sets whether this MainFrame is active in the foreground. Method to be called solely by WindowManager.
	 */
	void setForegroundActive(boolean foregroundActive) {
//if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("foregroundActive="+foregroundActive);
		this.foregroundActive = foregroundActive;
	}


	/**
	 * Update window title to reflect current active folder. To be called by WindowManager.
	 */
	public void updateWindowTitle() {
		// Update window title
		setTitle(getLastActiveTable().getCurrentFolder().getAbsolutePath()+" - muCommander");
	}
	
	///////////////////////
	// Overriden methods //
	///////////////////////

	/**
	 * Overrides JComponent's requestFocus() method to request focus on the last active FolderPanel.
	 */
	public void requestFocus() {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("focus requested, isVisible="+isVisible());
		// If visible, call requestFocus() directly on the component
		if(isVisible())
			lastActiveTable.getFolderPanel().requestFocus();
		// If not, call requestFocus() later when the component is visible
		else
			FocusRequester.requestFocus(lastActiveTable.getFolderPanel());
	}

	
// Adding custom insets to the Frame causes some weird sizing problems,
// so insets are added to the content pane instead
//	 public Insets getInsets() {
//		 return new Insets(3, 4, 3, 4);
//	 }

	
	///////////////////////////////
	// ComponentListener methods //
	///////////////////////////////
	 
	/**
	 * Sets the divider location when the ContentPane has been resized so that it stays at the
	 * same proportional (not absolute) location.
	 */
	public void componentResized(ComponentEvent e) {
		Object source = e.getSource();
		
		if (source == splitPane) { // The window has been resized
			// First time splitPane is made visible, this method is called
			// so we can set the initial divider location
			if (splitPaneWidth==-1) {
				splitPaneWidth = splitPane.getWidth();
//				splitPane.setDividerLocation(((int)splitPane.getWidth()/2));
				splitPane.setDividerLocation(((double)0.5));
			}
			else {
				float ratio = dividerLocation/(float)splitPaneWidth;
				splitPaneWidth = splitPane.getWidth();
				splitPane.setDividerLocation((int)(ratio*splitPaneWidth));
			}
			validate();
		}
		else if(source==folderPanel1) {		// Browser1 i.e. the divider has been moved OR the window has been resized
			dividerLocation = splitPane.getDividerLocation();
		}
	}

	public void componentHidden(ComponentEvent e) {
	}
	
	public void componentMoved(ComponentEvent e) {
	}
	
	public void componentShown(ComponentEvent e) {
		// never called, weird ...
    }     

    /////////////////////////
	// KeyListener methods //
    /////////////////////////
    
    public void keyPressed(KeyEvent e) {
		// Discard key events while in 'no events mode'
		if(noEventsMode)
			return;

        Object source = e.getSource();
        int keyCode = e.getKeyCode();
        if(keyCode == KeyEvent.VK_F3 && !e.isControlDown()) {
			commandBar.getButton(CommandBar.VIEW_INDEX).doClick();
        }
        else if(keyCode == KeyEvent.VK_F4 && e.isAltDown()) {
			commandBar.getButton(CommandBar.CLOSE_INDEX).doClick();
        }
        else if(keyCode == KeyEvent.VK_F4 && !e.isControlDown()) {
			commandBar.getButton(CommandBar.EDIT_INDEX).doClick();
        }
        else if(keyCode == KeyEvent.VK_F5 && !e.isControlDown()) {
			commandBar.getButton(CommandBar.COPY_INDEX).doClick();
        }
        else if(keyCode == KeyEvent.VK_F6 && !e.isControlDown()) {
			commandBar.getButton(CommandBar.MOVE_INDEX).doClick();
        }
        else if(keyCode == KeyEvent.VK_F7 && !e.isControlDown()) {
			commandBar.getButton(CommandBar.MKDIR_INDEX).doClick();
        }
        else if((keyCode == KeyEvent.VK_F8 || keyCode == KeyEvent.VK_DELETE)
		 && !e.isControlDown()) {
			commandBar.getButton(CommandBar.DELETE_INDEX).doClick();
        }
        else if(keyCode == KeyEvent.VK_F9 && !e.isControlDown()) {
			commandBar.getButton(CommandBar.REFRESH_INDEX).doClick();
        }
        else if(keyCode == KeyEvent.VK_F10 && !e.isControlDown()
		 || (PlatformManager.getOSFamily()==PlatformManager.MAC_OS_X && keyCode==KeyEvent.VK_W && e.isMetaDown())) {
			commandBar.getButton(CommandBar.CLOSE_INDEX).doClick();
        }
		else if(keyCode == KeyEvent.VK_F1 && e.isAltDown()) {
			folderPanel1.popDriveButton();
        }
        else if(keyCode == KeyEvent.VK_F2 && e.isAltDown()) {
        	folderPanel2.popDriveButton();
		}
        else if(e.isControlDown() && keyCode==KeyEvent.VK_LEFT) {
			WindowManager.getInstance().switchToPreviousWindow();
		}
		else if(e.isControlDown() && keyCode==KeyEvent.VK_RIGHT) {
			WindowManager.getInstance().switchToNextWindow();	
		}
        else if(keyCode == KeyEvent.VK_TAB) {
            if(source == table1)
                table2.requestFocus();
            else if(source == table2)
                table1.requestFocus();
        }
        else if(keyCode == KeyEvent.VK_ENTER && e.isAltDown()) {
        	showPropertiesDialog();
		}
		else if(keyCode == KeyEvent.VK_SHIFT) {
			// Set shift mode on : displays Rename instead of Move
			commandBar.setShiftMode(true);
		}
    }

    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();

		if(keyCode == KeyEvent.VK_SHIFT) {
			// Set shift mode back to off : displays Move instead of Rename
			commandBar.setShiftMode(false);
		}
    }

    public void keyTyped(KeyEvent e) {
    }
}
