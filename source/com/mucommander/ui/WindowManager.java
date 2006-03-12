
package com.mucommander.ui;

import com.mucommander.conf.*;
import com.mucommander.PlatformManager;

import com.mucommander.file.AbstractFile;

import com.mucommander.event.*;

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
public class WindowManager implements ActionListener, WindowListener, TableChangeListener, LocationListener, ConfigurationListener {

	/** MainFrame (main muCommander window) instances */
	private static Vector mainFrames;
    
	/** MainFrame currently being used (that has focus),
	 * or last frame to have been used if muCommander doesn't have focus */	
	private static MainFrame currentMainFrame;

	/** Time at which the last focus request was made */	
	private long lastFocusRequest;

	/** Last main frame on which focus has been explicitely requested */
	private MainFrame lastFocusedMainFrame;
	
	private static WindowManager instance;

	private final static int MENU_ITEM_VK_TABLE[] = {
		KeyEvent.VK_1,KeyEvent.VK_2,KeyEvent.VK_3,KeyEvent.VK_4,
		KeyEvent.VK_5,KeyEvent.VK_6,KeyEvent.VK_7,KeyEvent.VK_8,
		KeyEvent.VK_9, KeyEvent.VK_0};

	/** Minimum delay between 2 focus requests, so that 2 windows do not fight over focus */
	private final static int FOCUS_REQUEST_DELAY = 1000;

	
	static {
		instance = new WindowManager();
		instance.init();
	}
	
	
	/**
	 * Dummy method that does nothing but trigger static fields initialization.
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
	 * Returns a Vector of all MainFrame instances the application has.
	 */
	public Vector getMainFrames() {
		return mainFrames;
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
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

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
			
			// Set same window bounds as last run if resolution hasn't changed since then and if
			// window size is not substantially larger than screen size (safety check)
			if((x!=-1 || y!=-1) && width!=-1 && height!=-1
				&& screenSize.width==lastScreenWidth && screenSize.height==lastScreenHeight
				&& width+x<=screenSize.width+5 && height+y<=screenSize.height+5) {
				
				newMainFrame.setBounds(x, y, width, height);
			}
			// No saved window bounds or resolution has changed, use defaults
			else {		
//				// Set window size and location to use as much screen space as possible
//				newMainFrame.setBounds(PlatformManager.getFullScreenBounds(newMainFrame));

				// Full screen bounds are not reliable enough, in particular under Linux+Gnome
				// so we simply make the initial window 4/5 of screen's size, and center it.
				// This should fit under any window manager / platform
				newMainFrame.setBounds(new Rectangle(screenSize.width/10, screenSize.height/10, (int)(screenSize.width*0.8), (int)(screenSize.height*0.8)));
			}
		}
		else {
			// Use current's main frame size and location
			// and shift new frame a little so we can see
			// the previous one underneath
			Rectangle bounds = currentMainFrame.getBounds();
			int x = bounds.x + 22;
			int y = bounds.y + 22;
			// If frame is outside window (on x or y), set corresponding coordinate to 0
			if(!PlatformManager.isInsideUsableScreen(currentMainFrame, x+bounds.width, -1))
				x = 0;
			if(!PlatformManager.isInsideUsableScreen(currentMainFrame, -1, y+bounds.height))
				y = 0;

			// Set new frame bounds and make sure that they don't exceed screen size
			// (check is necessary in particular under Linux+Gnome where getBounds returns weird values)
			newMainFrame.setBounds(new Rectangle(x, y, bounds.width+x>screenSize.width?screenSize.width-x:bounds.width, bounds.height+y>screenSize.height?screenSize.height-y:bounds.height));
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

		// Make this new frame visible
		newMainFrame.show();
		
		return newMainFrame;
	}

	
	/**
	 * Returns the initial left/right folder according to user preferences.
	 */ 
	private AbstractFile getInitialFolder(boolean left) {
		
//		// Default path (if home folder or last folder doesn't exist or fails) is current drive
//		// for left folder and user home for right folder
		String defaultPath;
//		if (left) {				
//			AbstractFile tempFolder = new FSFile(new java.io.File(""));
//			AbstractFile tempParent;
//			while((tempParent=tempFolder.getParent())!=null)
//				tempFolder = tempParent;

//			defaultPath = tempFolder.getAbsolutePath(true);
//		}		
//		else {
			defaultPath = System.getProperty("user.home");
//		}

		// Initial path according to user preferences: either last folder or custom folder
		String goTo = ConfigurationManager.getVariable("prefs.startup_folder."+(left?"left":"right")+".on_startup", "lastFolder");
		String folderPath = null;

		// go to home folder
		if (goTo.equals("customFolder")) {
			folderPath = ConfigurationManager.getVariable("prefs.startup_folder."+(left?"left":"right")+".custom_folder", defaultPath);
		}
		// go to last folder
		else {
			folderPath = ConfigurationManager.getVariable("prefs.startup_folder."+(left?"left":"right")+".last_folder", defaultPath);
		}

		if(Debug.ON) Debug.trace("defaultPath "+defaultPath);
		
		AbstractFile folder = null;
		if(folderPath!=null)
			folder = AbstractFile.getAbstractFile(folderPath);
		
		if(folder==null || !folder.exists())
			folder = AbstractFile.getAbstractFile(defaultPath);

		if(Debug.ON) Debug.trace("initial folder= "+folder);
		
		return folder;
	}
	
	
	/**
	 * Properly disposes the given MainFrame.
	 */
	public void disposeMainFrame(MainFrame mainFrameToDispose) {
		if(com.mucommander.Debug.ON) Debug.trace("");

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
		int frameIndex = mainFrames.indexOf(mainFrameToDispose);
		mainFrameToDispose.dispose();
		mainFrames.remove(mainFrameToDispose);
		
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
	 * Disposes all opened windows, ending with the one that is currently active if there is one, 
	 * or the last one which was activated.
	 */
	public void quit() {
		int nbFrames = mainFrames.size();
		// Retrieve current MainFrame's index
		int currentMainFrameIndex = mainFrames.indexOf(this.currentMainFrame);
		MainFrame mainFrame;
		int index = 0;
		
		// Dispose all MainFrame but the current one
		for(int i=0; i<nbFrames; i++) {
			if(i==currentMainFrameIndex) {
				index++;
				continue;
			}
			disposeMainFrame((MainFrame)mainFrames.elementAt(index));
		}

		// There should normally be one and only one MainFrame remaining
		nbFrames = mainFrames.size();
		for(int i=0; i<nbFrames; i++)
			disposeMainFrame((MainFrame)mainFrames.elementAt(i));
	}
	
	
	/**
	 * Switches to the next MainFrame, in the order of which they were created.
	 */
	public void switchToNextWindow() {
		int frameIndex = mainFrames.indexOf(currentMainFrame);
		MainFrame mainFrame = (MainFrame)mainFrames.elementAt(frameIndex==mainFrames.size()-1?0:frameIndex+1);
		mainFrame.toFront();
	}

	/**
	 * Switches to previous MainFrame, in the order of which they were created.
	 */
	public void switchToPreviousWindow() {
		int frameIndex = mainFrames.indexOf(currentMainFrame);
		MainFrame mainFrame = (MainFrame)mainFrames.elementAt(frameIndex==0?mainFrames.size()-1:frameIndex-1);
		mainFrame.toFront();
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
	 * Updates the content of Window menus of every MainFrame instances,
	 * to reflect a folder or table change on a MainFrame.
	 *
	 * param folderPanel the folderPanel which change needs to be reflected.
	 */
	private void updateWindowMenus(FolderPanel folderPanel) {
		JMenu windowMenu;
		MainFrame mainFrame = folderPanel.getMainFrame();
		int frameIndex = mainFrames.indexOf(mainFrame);		
		// frameIndex==-1 each time a new MainFrame is created by createNewMainFrame()
		// since it is not yet in the mainFrames vector
		if (frameIndex!=-1) {
			for(int i=0; i<mainFrames.size(); i++) {
				windowMenu = ((MainMenuBar)((MainFrame)mainFrames.elementAt(i)).getJMenuBar()).getWindowMenu();
				windowMenu.getItem(frameIndex).setText((frameIndex+1)+" "+folderPanel.getCurrentFolder().getAbsolutePath());
			}
		}
		
		// Update window title to reflect new active folder
		mainFrame.updateWindowTitle();
	}

	/////////////////////////////////
	// TableChangeListener methods //
	/////////////////////////////////
	
	public void tableChanged(FolderPanel folderPanel) {
		// Update main frames' window menus to reflect current table's folder
		updateWindowMenus(folderPanel);
	}

	//////////////////////////////
	// LocationListener methods //
	//////////////////////////////
	
	public void locationChanged(LocationEvent e) {
		// Update main frames' window menus to reflect new folder's location
		updateWindowMenus(e.getFolderPanel());
	}	

	public void locationChanging(LocationEvent e) {
	}
	
	public void locationCancelled(LocationEvent e) {
	}
	

	////////////////////////////
	// ActionListener methods //
	////////////////////////////
	
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
			}
		}
	}

	////////////////////////////
	// WindowListener methods //
	////////////////////////////

	/**
	 * Requests focus on the activated MainFrame if it doesn't already have focus, and
	 * if another child window (dialog) doesn't have focus and if focus was not recently 
	 * requested on another MainFrame (to avoid having 2 main frames fight over focus).
	 */
	public void windowActivated(WindowEvent e) {
		this.currentMainFrame = (MainFrame)e.getSource();
		// Let MainFrame know that is active in the foreground
		currentMainFrame.setForegroundActive(true);

		// Resets shift mode to false, since keyReleased events may have been lost during window switching
		CommandBar commandBar = currentMainFrame.getCommandBar();
		if(commandBar!=null)
			commandBar.setShiftMode(false);

		// Requests focus if last active table doesn't already have focus
		// Delay check is to avoid that 2 main frames fight over focus.
		long now = System.currentTimeMillis();

		// Do not request focus if this MainFrame already has focus
		if(currentMainFrame.hasFocus())
			return;
	
		// /!\ Some already disposed window may be returned by getOwnedWindows
		// but that's apparently normal : some weak references to the window 
		// remain for a while before they are garbage collected (found that out
		// after first freaking out and then running the app through a profiler)
		Window ownedWindows[] = currentMainFrame.getOwnedWindows();

		// Do not request focus if another child window has focus
		if(ownedWindows!=null)
			for(int i=0; i<ownedWindows.length; i++)
				if(ownedWindows[i].isShowing())
					return;
		
		// Do not request focus if focus was requested on another MainFrame less than FOCUS_REQUEST_DELAY milliseconds ago
		if (lastFocusedMainFrame==currentMainFrame || (now-lastFocusRequest>FOCUS_REQUEST_DELAY)) {
			if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("requesting focus");
			currentMainFrame.requestFocus();
			lastFocusRequest = now;
			lastFocusedMainFrame = currentMainFrame;
		}
	}

	public void windowDeactivated(WindowEvent e) {
		((MainFrame)e.getSource()).setForegroundActive(false);
	}

	public void windowClosing(WindowEvent e) {
		// Dispose MainFrame instance
		disposeMainFrame((MainFrame)e.getSource());
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}


	///////////////////////////////////
	// ConfigurationListener methods //
	///////////////////////////////////

    /**
     * Listens to certain configuration variables.
     */
    public boolean configurationChanged(ConfigurationEvent event) {
    	String var = event.getVariable();
    
    	// /!\ font.size is set after font.family in AppearancePrefPanel
    	// that's why we only listen to this one in order not to change Font twice
    	if (var.equals("prefs.lookAndFeel")) {
			if(Debug.ON) Debug.trace("LookAndFeel changed! "+event.getValue());
    		String lnfName = event.getValue();
			
			setLookAndFeel(lnfName);
		}
    
    	return true;
    }
}
