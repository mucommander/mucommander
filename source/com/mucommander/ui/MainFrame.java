package com.mucommander.ui;

import com.mucommander.Launcher;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FileTableModel;
import com.mucommander.ui.comp.FocusRequester;
import com.mucommander.ui.pref.PreferencesDialog;
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
 * This is the main frame, which contains all other UI components.
 * 
 * @@author Maxence Bernard
 */
public class MainFrame extends JFrame implements ComponentListener, KeyListener, FocusListener, WindowListener {
	private final static String FRAME_TITLE = "muCommander";
	
	private JSplitPane splitPane;
	private int splitPaneWidth = -1;
	private int dividerLocation;

    private FolderPanel folderPanel1;
    private FolderPanel folderPanel2;
	
    private FileTable table1;
    private FileTable table2;
    
    private FileTable lastActiveTable;
    
    private CommandBarPanel commandBar;

    /** False until this window has been activated */
	private boolean firstTimeActivated;

	/** Used to determine whether or not this MainFrame is active, i.e. muCommander window is in the foreground */
	private boolean isActive; 
	
	
	public MainFrame(AbstractFile initialFolder1, AbstractFile initialFolder2) {
		super(FRAME_TITLE);
	
		// Resolves the URL of the image within the JAR file
		URL imageURL = getClass().getResource("/icon16.gif");
		// Sets frame icon
		setIconImage(new ImageIcon(imageURL).getImage());
	
		// Sets frame to a decent size without exceeding screen size
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(0, 0, Math.min(d.width-44,800-44), Math.min(d.height-33, 600-33));		
		setResizable(true);

		// Menu bar
		MainMenuBar menuBar = new MainMenuBar(this);
		setJMenuBar(menuBar);

		Container contentPane = getContentPane();

		// Icon toolbar
		ToolBar toolbar = new ToolBar(this);
		contentPane.add(toolbar, BorderLayout.NORTH);

		// Browsers
		folderPanel1 = new FolderPanel(this, initialFolder1);
        folderPanel2 = new FolderPanel(this, initialFolder2);

        folderPanel1.addLocationListener(Launcher.getLauncher());
        folderPanel2.addLocationListener(Launcher.getLauncher());

        folderPanel1.addLocationListener(toolbar);
        folderPanel2.addLocationListener(toolbar);

        folderPanel1.addLocationListener(menuBar);
        folderPanel2.addLocationListener(menuBar);

        table1 = folderPanel1.getFileTable();
        table2 = folderPanel2.getFileTable();

		lastActiveTable = table1;

		// Enables folderPanel window resizing
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, folderPanel1, folderPanel2) {
//			public Insets getInsets() {
//				return new Insets(0, 0, 0, 0);
//			}
		
			public javax.swing.border.Border getBorder() {
				return null;
			}
		};
			
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(((double)0.5));
		// Cool but way too slow
//		splitPane.setContinuousLayout(true);

		contentPane.add(splitPane, BorderLayout.CENTER);

		// To monitor resizing actions
		folderPanel1.addComponentListener(this);
		splitPane.addComponentListener(this);

        commandBar = new CommandBarPanel(this);
		contentPane.add(commandBar, BorderLayout.SOUTH);

        table1.addKeyListener(this);
        table2.addKeyListener(this);
    
        table1.addFocusListener(this);
		table2.addFocusListener(this);
    
		// Catches window close event
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
	}

	/**
	 * Returns last active FileTable, that is the last FileTable that received focus.
	 */
	public FileTable getLastActiveTable() {
        return lastActiveTable;
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
	public FolderPanel getBrowser1() {
		return folderPanel1;
	}

	/**
	 * Returns right FolderPanel.
	 */
	public FolderPanel getBrowser2() {
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
		FolderPanel tempBrowser = folderPanel1;
		folderPanel1 = folderPanel2;
		folderPanel2 = tempBrowser;

		FileTable tempTable = table1;
		table1 = table2;
		table2 = tempTable;
		
		Container contentPane = getContentPane();
		splitPane.removeAll();
		contentPane.remove(splitPane);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, folderPanel1, folderPanel2);
		splitPane.setOneTouchExpandable(true);

		contentPane.add(splitPane, BorderLayout.CENTER);
	
		contentPane.doLayout();
		splitPane.setDividerLocation(dividerLocation);

		(lastActiveTable==table1?table2:table1).requestFocus();

	// This should also work but splitPane becomes crazy (does not want to resize anymore)

//		FolderPanel tempBrowser = folderPanel1;
//		folderPanel1 = folderPanel2;
//		folderPanel2 = tempBrowser;
//
//		FileTable tempTable = table1;
//		table1 = table2;
//		table2 = tempTable;
//
//		splitPane.removeAll();
//		splitPane.setLeftComponent(folderPanel1);
//		splitPane.setRightComponent(folderPanel2);
//		splitPane.doLayout();
//		splitPane.repaint();
	}

	/**
	 * Compares directories: marks files that are missing from a directory or that are newer.
	 */
	public void compareDirectories() {
		AbstractFile tempFile;
		AbstractFile files1[] = ((FileTableModel)table1.getModel()).getFileArray();
		AbstractFile files2[] = ((FileTableModel)table2.getModel()).getFileArray();
        int fileIndex;
		String tempFileName;
		for(int i=table1.getCurrentFolder().getParent()==null?0:1; i<files1.length; i++) {
			tempFile = files1[i];
//			if(tempFile.isFolder() && !(tempFile instanceof ArchiveFile))
			if(tempFile.isDirectory())
				continue;
			
			tempFileName = tempFile.getName();
            fileIndex = -1;
			for(int j=table2.getCurrentFolder().getParent()==null?0:1; j<files2.length; j++)
				if (files2[j].getName().equals(tempFileName)) {
                    fileIndex = j;
					break;
				}
			if (fileIndex==-1 || files2[fileIndex].getDate()<tempFile.getDate()) {
				table1.setFileMarked(tempFile, true);
				table1.repaint();
			}
		}

		for(int i=table2.getCurrentFolder().getParent()==null?0:1; i<files2.length; i++) {
			tempFile = files2[i];
//			if(tempFile.isFolder() && !(tempFile instanceof ArchiveFile))
			if(tempFile.isDirectory())
				continue;

			tempFileName = tempFile.getName();
            fileIndex = -1;
			for(int j=table1.getCurrentFolder().getParent()==null?0:1; j<files1.length; j++)
				if (files1[j].getName().equals(tempFileName)) {
                    fileIndex = j;
					break;
				}
			if (fileIndex==-1 || files1[fileIndex].getDate()<tempFile.getDate()) {
				table2.setFileMarked(tempFile, true);
				table2.repaint();
			}
		}

	}

	/**
	 * Makes both folders the same, choosing the one which is currently active. 
	 */
	public void setSameFolder() {
		(lastActiveTable==table1?table2:table1).getBrowser().setCurrentFolder(lastActiveTable.getCurrentFolder(), false);
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
		String currentPath = currentFolder.getAbsolutePath();
		String currentSeparator = currentFolder.getSeparator();
		AbstractFile destFolder;

		String newName = null;

		// Level 0, newName is null
		// destPath points to an absolute and existing folder
		if ((destFolder=AbstractFile.getAbstractFile(destPath))!=null 
		 && destFolder.exists()
//		 && destFolder.isFolder() && !(destFolder instanceof ArchiveFile)) {
		 && destFolder.isDirectory()) {
		}

		// destPath points to an existing folder relative to current folder
		else if ((destFolder=AbstractFile.getAbstractFile(currentPath+currentSeparator+destPath))!=null
		 && destFolder.exists()
//		 && destFolder.isFolder() && !(destFolder instanceof ArchiveFile)) {
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
			else if ((destFolder=AbstractFile.getAbstractFile(currentPath+currentSeparator+destPath))!=null && destFolder.exists()) {
			}

			else 
				return null;
		}

		return new Object[] {destFolder, newName};
	}

	
	/**
	 * Returns whether or not this MainFrame is active, i.e. if muCommander window is in the foreground.
	 */
	public boolean isActive() {
		return isActive;
	}


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
            commandBar.doView();
        }
        else if(keyCode == KeyEvent.VK_F4 && e.isAltDown()) {
            commandBar.doExit();
        }
        else if(keyCode == KeyEvent.VK_F4 && !e.isControlDown()) {
            commandBar.doEdit();
        }
        else if(keyCode == KeyEvent.VK_F5 && !e.isControlDown()) {
			new CopyDialog(this, false, e.isShiftDown());
        }
        else if(keyCode == KeyEvent.VK_F6 && !e.isControlDown()) {
			new MoveDialog(this, e.isShiftDown());
        }
        else if(keyCode == KeyEvent.VK_F7 && !e.isControlDown()) {
			new MkdirDialog(this);
        }
        else if((keyCode == KeyEvent.VK_F8 || keyCode == KeyEvent.VK_DELETE)
		 && !e.isControlDown()) {
			new DeleteDialog(this);
        }
        else if(keyCode == KeyEvent.VK_F9 && !e.isControlDown()) {
        	commandBar.doRefresh();
        }
        else if(keyCode == KeyEvent.VK_F10 && !e.isControlDown()) {
            commandBar.doExit();
        }
		else if(keyCode == KeyEvent.VK_F1 && e.isAltDown()) {
			folderPanel1.showRootBox();
        }
        else if(keyCode == KeyEvent.VK_F2 && e.isAltDown()) {
        	folderPanel2.showRootBox();
		}
        else if(e.isControlDown() && keyCode==KeyEvent.VK_LEFT) {
			Launcher.getLauncher().switchToPreviousWindow();
		}
		else if(e.isControlDown() && keyCode==KeyEvent.VK_RIGHT) {
			Launcher.getLauncher().switchToNextWindow();	
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

    /*************************
     * FocusListener methods *
     *************************/
    
    /**
     * Listens to focus events coming from FileTable instances to keep track of the last active table
     */
    public void focusGained(FocusEvent e) {
        this.lastActiveTable = e.getSource()==table1?table1:table2;
    }
    
    public void focusLost(FocusEvent e) {
    }    


    /**************************
     * WindowListener methods *
     **************************/	

    public void windowActivated(WindowEvent e) {
		this.isActive = true;

    	if(com.mucommander.Debug.TRACE)
			System.out.println("MainFrame.windowActivated");
		
		// Requests focus first time MainFrame is activated
    	// (this method is called each time MainFrame is activated)
    	if (!firstTimeActivated) {
    		FocusRequester.requestFocus(table1);
    		firstTimeActivated = true;
    	}
    }

    public void windowDeactivated(WindowEvent e) {
		this.isActive = false;
		
    	if(com.mucommander.Debug.TRACE)
			System.out.println("MainFrame.windowDeactivated");
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
		// Properly disposes folder panels and releases
		// associated resources
		this.folderPanel1.dispose();
		this.folderPanel2.dispose();
	}
}
