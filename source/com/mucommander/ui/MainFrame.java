package com.mucommander.ui;

import com.mucommander.PlatformManager;

import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FileTableModel;
import com.mucommander.ui.comp.FocusRequester;
import com.mucommander.ui.comp.dialog.YBoxPanel;
import com.mucommander.ui.pref.PreferencesDialog;
import com.mucommander.ui.connect.ServerConnectDialog;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.ArchiveFile;

import com.mucommander.job.SendMailJob;

import com.mucommander.conf.ConfigurationManager;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.Vector;


/**
 * This is the main frame, which contains all other UI components visible on a mucommander window.
 * 
 * @author Maxence Bernard
 */
public class MainFrame extends JFrame implements ComponentListener, KeyListener {
	
	private final static String FRAME_TITLE = "muCommander";

	// Variables related to split pane	
	private JSplitPane splitPane;
	private int splitPaneWidth = -1;
	private int dividerLocation;

    private FolderPanel folderPanel1;
    private FolderPanel folderPanel2;
	
    private FileTable table1;
    private FileTable table2;
    
    private FileTable lastActiveTable;

	private ToolBar toolbar;
    
	private JLabel statusBarLabel;
	
	private CommandBarPanel commandBar;
	
	
	/**
	 * Creates a new main frame, set to the given initial folders.
	 */
	public MainFrame(AbstractFile initialFolder1, AbstractFile initialFolder2) {
		super(FRAME_TITLE);
	
		// Resolve the URL of the image within the JAR file
		URL imageURL = getClass().getResource("/icon16.gif");
		// Set frame icon
		setIconImage(new ImageIcon(imageURL).getImage());

		// Enable window resize
		setResizable(true);

//		YBoxPanel contentPane = new YBoxPanel();
//		contentPane.setInsets(new Insets(3, 4, 3, 4));
		JPanel contentPane = new JPanel(new BorderLayout()) {
			// Add an x=3,y=3 gap around content pane
			public Insets getInsets() {
				return new Insets(3, 3, 3, 3);
			}
		};
		setContentPane(contentPane);

		// Start by creating folder panels as they are used
		// below (by Toolbar)
		folderPanel1 = new FolderPanel(this, initialFolder1);
        folderPanel2 = new FolderPanel(this, initialFolder2);

		table1 = folderPanel1.getFileTable();
        table2 = folderPanel2.getFileTable();

		lastActiveTable = table1;

		folderPanel1.addLocationListener(WindowManager.getInstance());
        folderPanel2.addLocationListener(WindowManager.getInstance());

		// Create toolbar and show it only if it hasn't been disabled in the preferences
		this.toolbar = new ToolBar(this);
		this.toolbar.setVisible(ConfigurationManager.getVariable("prefs.show_toolbar", "true").equals("true"));
//		contentPane.add(toolbar);
		contentPane.add(toolbar, BorderLayout.NORTH);

		folderPanel1.addLocationListener(toolbar);
		folderPanel2.addLocationListener(toolbar);
		
		// Create menu bar (has to be created after toolbar)
		MainMenuBar menuBar = new MainMenuBar(this);
		setJMenuBar(menuBar);

        folderPanel1.addLocationListener(menuBar);
        folderPanel2.addLocationListener(menuBar);

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

//		contentPane.add(splitPane);
		// Split pane will be given any extra space
		contentPane.add(splitPane, BorderLayout.CENTER);

		YBoxPanel southPanel = new YBoxPanel();
		// Add a 3-pixel gap between table and status/command bar
		southPanel.setInsets(new Insets(3, 0, 0, 0));
	
		// Create and add status bar
		statusBarLabel = new JLabel("");
		if(ConfigurationManager.getVariable("prefs.show_status_bar", "true").equals("false"))
			statusBarLabel.setVisible(false);
//		contentPane.add(statusBarLabel);
		southPanel.add(statusBarLabel);

		// Show command bar only if it hasn't been disabled in the preferences
		this.commandBar = new CommandBarPanel(this);
		if(ConfigurationManager.getVariable("prefs.show_command_bar", "true").equals("false"))
			commandBar.setVisible(false);
//		contentPane.add(commandBar);
		southPanel.add(commandBar);
		
		contentPane.add(southPanel, BorderLayout.SOUTH);
		
//contentPane.addGlue();
								
		// To monitor resizing actions
		folderPanel1.addComponentListener(this);
		splitPane.addComponentListener(this);

        table1.addKeyListener(this);
        table2.addKeyListener(this);
    
		// Catches window close event
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

// For testing purposes, full screen option could be nice to add someday
//setUndecorated(true);
//java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(this);
	}
	
	
	/**
	 * Shows/hide the toolbar.
	 */
	public void setToolbarVisible(boolean visible) {
		toolbar.setVisible(visible);
		validate();
	}
	
	
	/**
	 * Returns true if the icon toolbar is visible on this frame.
	 */
	public boolean isToolbarVisible() {
		return toolbar.isVisible();
	}
	

	/**
	 * Shows/hide the command bar.
	 */
	public void setCommandBarVisible(boolean visible) {
		this.commandBar.setVisible(visible);
		validate();
	}
	
	
	/**
	 * Returns true if the command bar is visible on this frame.
	 */
	public boolean isCommandBarVisible() {
		return this.commandBar.isVisible();
	}
	

	/**
	 * Shows/hide the status bar.
	 */
	public void setStatusBarVisible(boolean visible) {
		this.statusBarLabel.setVisible(visible);
		validate();
	}
	
	
	/**
	 * Returns true if the status bar is visible on this frame.
	 */
	public boolean isStatusBarVisible() {
		return this.statusBarLabel.isVisible();
	}
	
	
	/**
	 * Sets status bar text label. This method is called by FileTable.
	 */
	public void setStatusBarText(String text) {
if(com.mucommander.Debug.ON) text += " - freeMem="+Runtime.getRuntime().freeMemory()+" - totalMem="+Runtime.getRuntime().totalMemory();
		statusBarLabel.setText(text);
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
        this.lastActiveTable = table;
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
	 * Returns the instance of CommandBarPanel, i.e. the panel that contains
	 * F3, F6... F10 buttons.
	 */
	public CommandBarPanel getCommandBar() {
		return commandBar;
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
		(lastActiveTable==table1?table2:table1).getFolderPanel().setCurrentFolder(lastActiveTable.getCurrentFolder(), false);
	}

	/**
	 * Brings up the properties dialog that displays information about currently marked files (if any).
	 */
	public void showPropertiesDialog() {
		Vector files = getLastActiveTable().getSelectedFiles();
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
	 * Matches a path typed by the user (which can be relative to the current folder or absolute)
	 * to an AbstractFile (folder). The folder returned will always exist.
	 * If the given path doesn't correspond to any existing folder, a null value will be returned.
	 */
	public Object[] resolvePath(String destPath) {
		AbstractFile currentFolder = lastActiveTable.getCurrentFolder();
		// Current path, including trailing separator
		String currentPath = currentFolder.getAbsolutePath(true);
		AbstractFile destFolder;

		String newName = null;

		// Level 0, newName is null
		// destPath points to an absolute and existing folder
		if ((destFolder=AbstractFile.getAbstractFile(destPath))!=null 
		 && destFolder.exists()
		 && destFolder.isDirectory()) {
		}

		// destPath points to an existing folder relative to current folder
		else if ((destFolder=AbstractFile.getAbstractFile(currentPath+destPath))!=null
		 && destFolder.exists()
		 && destFolder.isDirectory()) {
		}

		// Level 1, path includes a new destination filename
		else {
			// Removes ending separator character (if any)
			char c = destPath.charAt(destPath.length()-1);
			// Separator characters can be mixed
			if(c=='/' || c=='\\')
				destPath = destPath.substring(0,destPath.length()-1);
			
			// Extracts the new destination filename
			int pos = Math.max(destPath.lastIndexOf('/'), destPath.lastIndexOf('\\'));
			if (pos!=-1) {
				newName = destPath.substring(pos+1, destPath.length());
				destPath = destPath.substring(0,pos+1);
			}
			else  {
				newName = destPath;
				destPath = "";
			}			

			// destPath points to an absolute and existing folder
			if ((destFolder=AbstractFile.getAbstractFile(destPath))!=null && destFolder.exists()) {
			}
			
			// destPath points to an existing folder relative to current folder
			else if ((destFolder=AbstractFile.getAbstractFile(currentPath+destPath))!=null && destFolder.exists()) {
			}

			else 
				return null;
		}

		return new Object[] {destFolder, newName};
	}

	
	/**
	 * Returns whether or not this MainFrame is active, i.e. if this window is in the foreground.
	 */
	public boolean isActive() {
		return isVisible() && hasFocus();
	}

	
	/*********************
	 * Overriden methods *
	 *********************/

	 public void dispose() {
		// Properly disposes folder panels and releases
		// associated resources
		this.folderPanel1.dispose();
		this.folderPanel2.dispose();

		super.dispose();
	 }
	 

	 /**
	 * Overrides JComponent's requestFocus() method to request focus on the last active FolderPanel.
	 */
	public void requestFocus() {
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

	
	/*****************************
	 * ComponentListener methods *
	 *****************************/
	 
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

    /***********************
     * KeyListener methods *
     ***********************/
    
    public void keyPressed(KeyEvent e) {
        Object source = e.getSource();
        int keyCode = e.getKeyCode();
        if(keyCode == KeyEvent.VK_F3 && !e.isControlDown()) {
			commandBar.getButton(CommandBarPanel.VIEW_INDEX).doClick();
        }
        else if(keyCode == KeyEvent.VK_F4 && e.isAltDown()) {
			commandBar.getButton(CommandBarPanel.CLOSE_INDEX).doClick();
        }
        else if(keyCode == KeyEvent.VK_F4 && !e.isControlDown()) {
			commandBar.getButton(CommandBarPanel.EDIT_INDEX).doClick();
        }
        else if(keyCode == KeyEvent.VK_F5 && !e.isControlDown()) {
			commandBar.getButton(CommandBarPanel.COPY_INDEX).doClick();
        }
        else if(keyCode == KeyEvent.VK_F6 && !e.isControlDown()) {
			commandBar.getButton(CommandBarPanel.MOVE_INDEX).doClick();
        }
        else if(keyCode == KeyEvent.VK_F7 && !e.isControlDown()) {
			commandBar.getButton(CommandBarPanel.MKDIR_INDEX).doClick();
        }
        else if((keyCode == KeyEvent.VK_F8 || keyCode == KeyEvent.VK_DELETE)
		 && !e.isControlDown()) {
			commandBar.getButton(CommandBarPanel.DELETE_INDEX).doClick();
        }
        else if(keyCode == KeyEvent.VK_F9 && !e.isControlDown()) {
			commandBar.getButton(CommandBarPanel.REFRESH_INDEX).doClick();
        }
        else if(keyCode == KeyEvent.VK_F10 && !e.isControlDown()
		 || (PlatformManager.getOSFamily()==PlatformManager.MAC_OS_X && keyCode==KeyEvent.VK_W && e.isMetaDown())) {
			commandBar.getButton(CommandBarPanel.CLOSE_INDEX).doClick();
        }
		else if(keyCode == KeyEvent.VK_F1 && e.isAltDown()) {
			folderPanel1.showRootBox();
        }
        else if(keyCode == KeyEvent.VK_F2 && e.isAltDown()) {
        	folderPanel2.showRootBox();
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
