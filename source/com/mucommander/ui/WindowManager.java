
package com.mucommander.ui;


import com.mucommander.conf.*;
import com.mucommander.PlatformManager;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FSFile;

import com.mucommander.Debug;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import java.util.Vector;


/**
 * Window Manager is responsible for creating, disposing, switching,
 * in other words managing :) muCommander windows.
 *
 * @author Maxence Bernard
 */
public class WindowManager implements ActionListener, WindowListener, LocationListener, ConfigurationListener {

	/** MainFrame (main muCommander window) instances */
	private static Vector mainFrames;
    
	/** MainFrame currently being used (that has focus),
	 * or last frame to have been used if muCommander doesn't have focus */	
	private static MainFrame currentMainFrame;

	
	private final static int MENU_ITEM_VK_TABLE[] = {
		KeyEvent.VK_1,KeyEvent.VK_2,KeyEvent.VK_3,KeyEvent.VK_4,
		KeyEvent.VK_5,KeyEvent.VK_6,KeyEvent.VK_7,KeyEvent.VK_8,
		KeyEvent.VK_9, KeyEvent.VK_0};
	
	private static WindowManager instance;

	
	static {
		instance = new WindowManager();
		instance.init();
	}
	
	
	/**
	 * Method that does nothing but ensures that static initialization has
	 * been done.
	 */
	public static void checkInit() {
	}
	
	
	/**
	 * Empty no-arg constructor.
	 */
	private WindowManager() {
		// do nothing here so that
		// getInstance() returns a non-null value
		// during initialization (performed in init method)
	}
	
	
	/**
	 * Performs first-time initialization and creates a first frame.
	 */
	private void init() {
		// Sets custom lookAndFeel if different from current lookAndFeel
		String lnfName = ConfigurationManager.getVariable("prefs.lookAndFeel");
		if(lnfName!=null && !lnfName.equals(UIManager.getLookAndFeel().getName()))
			setLookAndFeel(lnfName);
		
		// Listens to certain configuration events
		ConfigurationManager.addConfigurationListener(this);
		
		// Create a MainFrame
		this.mainFrames = new Vector();
		currentMainFrame = createNewMainFrame();
	}
	
	
	public static WindowManager getInstance() {
		return instance;
	}
	
	
	/**
	 * Returns the MainFrame instance that currently is active.
	 */
	public MainFrame getCurrentMainFrame() {
		return currentMainFrame;
	}
	
	
	/**
	 * Creates a new MainFrame
	 */
	public MainFrame createNewMainFrame() {
		// Initial folders are home and/or last folder if it's the first MainFrame,
		// the same folders as the current MainFrame otherwise
		AbstractFile folder1;
		AbstractFile folder2;
		
		// If first window, set initial folders
		boolean firstWindow = mainFrames.size()==0;
		if (firstWindow) {
			folder1 = getInitialFolder(true);
			folder2 = getInitialFolder(false);
		}
		// If not, use previous window's folders 
		else {
			folder1 = currentMainFrame.getFolderPanel1().getFileTable().getCurrentFolder();
			folder2 = currentMainFrame.getFolderPanel2().getFileTable().getCurrentFolder();
		}
		
		// Create frame
		MainFrame newMainFrame = new MainFrame(folder1, folder2);

		// Set window size and location

		// If first window
		if(firstWindow) {
			// Retrieve last saved window bounds
			int x = ConfigurationManager.getVariableInt("prefs.last_window.x");
			int y = ConfigurationManager.getVariableInt("prefs.last_window.y");
			int width = ConfigurationManager.getVariableInt("prefs.last_window.width");
			int height = ConfigurationManager.getVariableInt("prefs.last_window.height");
			int lastScreenWidth = ConfigurationManager.getVariableInt("prefs.last_window.screen_width");
			int lastScreenHeight = ConfigurationManager.getVariableInt("prefs.last_window.screen_height");
			
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			// Set same window bounds as before if resolution hasn't changed since then and if
			// window size is not substantially larger than screen size (safety check)
			if(x!=-1 && y!=-1 && width!=-1 && height!=-1
				&& screenSize.width==lastScreenWidth && screenSize.height==lastScreenHeight
				&& width<=screenSize.width+5 && height<=screenSize.height+5) {
				
				newMainFrame.setBounds(x, y, width, height);
			}
			else {
				// Set window size and location to use as much screen space as possible
				newMainFrame.setBounds(PlatformManager.getFullScreenBounds(newMainFrame));
			}
		}
		else {
			// Use current's main frame size and location
			// and shift new frame a little so the we can see
			// previous one underneath
			Rectangle bounds = currentMainFrame.getBounds();
			int x = bounds.x + 22;
			int y = bounds.y + 22;
			// If frame is outside window (on x or y), set corresponding coordinate to 0
			if(!PlatformManager.isInsideUsableScreen(currentMainFrame, x+bounds.width, -1))
				x = 0;
			if(!PlatformManager.isInsideUsableScreen(currentMainFrame, -1, y+bounds.height))
				y = 0;

			newMainFrame.setBounds(new Rectangle(x, y, bounds.width, bounds.height));
		}
		// To catch user window closing actions
		newMainFrame.addWindowListener(this);
		
		// Adds a new menu item in each existing MainFrame's window menu
		JMenu windowMenu;
		JCheckBoxMenuItem checkBox;
		int nbFrames = mainFrames.size();
		for(int i=0; i<nbFrames; i++) {
			windowMenu = ((MainMenuBar)((MainFrame)mainFrames.elementAt(i)).getJMenuBar()).getWindowMenu();
			checkBox = new JCheckBoxMenuItem((nbFrames)+" "+newMainFrame.getLastActiveTable().getCurrentFolder().getAbsolutePath(), false);
			checkBox.addActionListener(this);
			if(nbFrames<10)
				checkBox.setAccelerator(KeyStroke.getKeyStroke(MENU_ITEM_VK_TABLE[nbFrames], ActionEvent.CTRL_MASK));
			windowMenu.add(checkBox);
		}
		
		// Adds the new MainFrame to the vector
		mainFrames.add(newMainFrame);
		nbFrames++;

		// Sets the 'window menu' items of the new MainFrame with an item
		// for each MainFrame (including the new one)
		windowMenu = ((MainMenuBar)newMainFrame.getJMenuBar()).getWindowMenu();
		for(int i=0; i<nbFrames; i++) {
			checkBox = new JCheckBoxMenuItem((i+1)+" "+((MainFrame)mainFrames.elementAt(i)).getLastActiveTable().getCurrentFolder().getAbsolutePath(), i==nbFrames-1);
			checkBox.addActionListener(this);
			if(i<10)
				checkBox.setAccelerator(KeyStroke.getKeyStroke(MENU_ITEM_VK_TABLE[i], ActionEvent.CTRL_MASK));
			windowMenu.add(checkBox);
		}

		// To catch user clicks on window menu items and change current MainFrame accordingly
		windowMenu.addActionListener(this);

		newMainFrame.show();
		return newMainFrame;
	}

	
	/**
	 * Returns the initial left/right folder according to user preferences.
	 */ 
	private AbstractFile getInitialFolder(boolean left) {
		
		// Default path (if home folder or last folder doesn't exist or fails) is current drive
		// for left folder and user home for right folder
		String defaultPath;
		if (left) {				
			AbstractFile tempFolder = new FSFile(new java.io.File(""));
			AbstractFile tempParent;
			while((tempParent=tempFolder.getParent())!=null)
				tempFolder = tempParent;

			defaultPath = tempFolder.getAbsolutePath(true);
		}		
		else {
			defaultPath = System.getProperty("user.home");
		}

		// Initial path according to user preferences: either last folder or custom folder
		String goTo = ConfigurationManager.getVariable("prefs.startup_folder."+(left?"left":"right")+".on_startup");
		String folderPath = null;
		if (goTo==null) {
			ConfigurationManager.setVariable("prefs.startup_folder."+(left?"left":"right")+".on_startup", "lastFolder");
		}
		else {
			// go to home folder
			if (goTo.equals("customFolder")) {
				folderPath = ConfigurationManager.getVariable("prefs.startup_folder."+(left?"left":"right")+".custom_folder", defaultPath);
			}
			// go to last folder
			else {
				folderPath = ConfigurationManager.getVariable("prefs.startup_folder."+(left?"left":"right")+".last_folder", defaultPath);
			}
		}

		if(Debug.ON)
			System.out.println("defaultPath "+defaultPath);
		
		AbstractFile folder = null;
		if(folderPath!=null)
			folder = AbstractFile.getAbstractFile(folderPath);
		
		if(folder==null || !folder.exists())
			folder = AbstractFile.getAbstractFile(defaultPath);

		if(Debug.ON)
			System.out.println("initial folder "+folder.getAbsolutePath());
		
		return folder;
	}
	
	
	/**
	 * Properly disposes the given MainFrame.
	 */
	public void disposeMainFrame(MainFrame mainFrameToDispose) {
		// Saves last folders
		ConfigurationManager.setVariable("prefs.startup_folder.left.last_folder", 
			mainFrameToDispose.getFolderPanel1().getLastSavableFolder());
		ConfigurationManager.setVariable("prefs.startup_folder.right.last_folder", 
			mainFrameToDispose.getFolderPanel2().getLastSavableFolder());

		// Saves window position, size and screen resolution
		Rectangle bounds = mainFrameToDispose.getBounds();
		ConfigurationManager.setVariableInt("prefs.last_window.x", (int)bounds.getX());
		ConfigurationManager.setVariableInt("prefs.last_window.y", (int)bounds.getY());
		ConfigurationManager.setVariableInt("prefs.last_window.width", (int)bounds.getWidth());
		ConfigurationManager.setVariableInt("prefs.last_window.height", (int)bounds.getHeight());
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		ConfigurationManager.setVariableInt("prefs.last_window.screen_width", screenSize.width);
		ConfigurationManager.setVariableInt("prefs.last_window.screen_height", screenSize.height);

		// Disposes the MainFrame
		mainFrameToDispose.dispose();
		mainFrames.remove(mainFrameToDispose);
		
		int frameIndex = mainFrames.indexOf(mainFrameToDispose);
		int nbFrames = mainFrames.size();
		MainFrame mainFrame;
		JMenuItem item;
		JMenu windowMenu;
		for(int i=0; i<nbFrames; i++) {
			mainFrame = (MainFrame)mainFrames.elementAt(i);
			windowMenu = ((MainMenuBar)mainFrame.getJMenuBar()).getWindowMenu();
			// Removes the MainFrame disposed for every MainFrame's window menu
			windowMenu.remove(frameIndex);
			// and renames menu items that were after the disposed MainFrame (their index has changed)			
			for(int j=frameIndex; j<nbFrames; j++) {
				item = windowMenu.getItem(j);
				item.setText((j+1)+" "+mainFrame.getLastActiveTable().getCurrentFolder().getAbsolutePath());
				if(j<10)
					item.setAccelerator(KeyStroke.getKeyStroke(MENU_ITEM_VK_TABLE[j], ActionEvent.CTRL_MASK));
			}
		}

		// If no mainFrame is currently visible, exit
		if(mainFrames.size()==0)
			System.exit(0);		
	}

	
	/**
	 * Switches to the next MainFrame, in the order of which they were created.
	 */
	public void switchToNextWindow() {
		int frameIndex = mainFrames.indexOf(currentMainFrame);
		MainFrame mainFrame = (MainFrame)mainFrames.elementAt(frameIndex==mainFrames.size()-1?0:frameIndex+1);
		mainFrame.toFront();
		mainFrame.getLastActiveTable().requestFocus();
	}

	/**
	 * Switches to previous MainFrame, in the order of which they were created.
	 */
	public void switchToPreviousWindow() {
		int frameIndex = mainFrames.indexOf(currentMainFrame);
		MainFrame mainFrame = (MainFrame)mainFrames.elementAt(frameIndex==0?mainFrames.size()-1:frameIndex-1);
		mainFrame.toFront();
		mainFrame.getLastActiveTable().requestFocus();
	}

	
	/**
	 * Changes LooknFeel to the given one, updating the UI of each MainFrame.
	 */
	private void setLookAndFeel(String lnfName) {
		try {
			UIManager.setLookAndFeel(lnfName);

			for(int i=0; i<mainFrames.size(); i++) {
				SwingUtilities.updateComponentTreeUI((MainFrame)(mainFrames.elementAt(i)));
			}
		}
		catch(Exception e) {
		}
	}


	/**
	 * Notifies this MainFrame's currentFolder has changed so
	 * that window title and menu items can be updated.
	 */
	public void locationChanged(FolderPanel folderPanel) {
		AbstractFile currentFolder = folderPanel.getCurrentFolder();
		MainFrame mainFrame = folderPanel.getMainFrame();
		String currentPath = currentFolder.getAbsolutePath(true);
		mainFrame.setTitle(currentPath+" - muCommander");

		JMenu windowMenu;
		int frameIndex = mainFrames.indexOf(mainFrame);
		
		// frameIndex==-1 each time a new MainFrame is created by createNewMainFrame()
		// since it is not yet in the mainFrames vector
		if (frameIndex!=-1) {
			for(int i=0; i<mainFrames.size(); i++) {
				windowMenu = ((MainMenuBar)((MainFrame)mainFrames.elementAt(i)).getJMenuBar()).getWindowMenu();
				windowMenu.getItem(frameIndex).setText((frameIndex+1)+" "+currentPath);
			}
		}
	}	

	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
				
		if (source instanceof JCheckBoxMenuItem) {
			// Leaves check box as it was
			JCheckBoxMenuItem checkBox = ((JCheckBoxMenuItem)source);
			checkBox.setState(!checkBox.getState());
			
			// Finds item index
			JMenu windowMenu = ((MainMenuBar)currentMainFrame.getJMenuBar()).getWindowMenu();
			int itemIndex = -1;
			for(int i=0; i<mainFrames.size(); i++)
				if(windowMenu.getItem(i)==source)
					itemIndex = i;
			// Should never happen
			if(itemIndex==-1)
				return;
		
			// Request focus on the corresponding MainFrame
			MainFrame mainFrameToFront = (MainFrame)mainFrames.elementAt(itemIndex);
			if (mainFrameToFront != currentMainFrame) {
				mainFrameToFront.toFront();
				mainFrameToFront.getLastActiveTable().requestFocus();
			}
		}
	}

	/**************************
	 * WindowListener methods *
	 **************************/	

	public void windowClosing(WindowEvent e) {
		disposeMainFrame((MainFrame)e.getSource());
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
		this.currentMainFrame = (MainFrame)e.getSource();
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}


    /**
     * Listens to certain configuration variables.
     */
    public boolean configurationChanged(ConfigurationEvent event) {
    	String var = event.getVariable();
    
    	// /!\ font.size is set after font.family in AppearancePrefPanel
    	// that's why we only listen to this one in order not to change Font twice
    	if (var.equals("prefs.lookAndFeel")) {
			if(Debug.ON)
				System.out.println("LookAndFeel changed! "+event.getValue());
    		String lnfName = event.getValue();
			
			setLookAndFeel(lnfName);
		}
    
    	return true;
    }
}
