package com.mucommander;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.MainMenuBar;
import com.mucommander.ui.FolderPanel;
import com.mucommander.ui.LocationListener;
import com.mucommander.ui.CheckVersionDialog;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FSFile;
import com.mucommander.conf.*;
import com.mucommander.ui.macosx.FinderIntegration;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.io.File;
import java.util.Vector;
import java.awt.event.*;

import com.mucommander.Debug;

public class Launcher implements ActionListener, WindowListener, LocationListener, ConfigurationListener {
	/** Version string */
	public final static String MUCOMMANDER_VERSION = "0.6";
		
	private static Vector mainFrames;
    	
	private MainFrame currentMainFrame;

	private static Launcher launcher;
	
	private final static int MENU_ITEM_VK_TABLE[] = {
		KeyEvent.VK_1,KeyEvent.VK_2,KeyEvent.VK_3,KeyEvent.VK_4,
		KeyEvent.VK_5,KeyEvent.VK_6,KeyEvent.VK_7,KeyEvent.VK_8,
		KeyEvent.VK_9, KeyEvent.VK_0};
	
	public static void main(String args[]) {
		launcher = new Launcher();
		launcher.init();
	}

	public static Launcher getLauncher() {
		return launcher;
	}

	private Launcher() {
	}

	private void init() {
		// Show splash screen before anything else
		JWindow splashScreen = showSplashScreen();

		// Sets custom lookAndFeel if different from current lookAndFeel
		String lnfName = ConfigurationManager.getVariable("prefs.lookAndFeel");
		if(lnfName!=null && !lnfName.equals(UIManager.getLookAndFeel().getName()))
			setLookAndFeel(lnfName);
		
		// Create a MainFrame
		mainFrames = new Vector();
		MainFrame mainFrame = createNewMainFrame();

//		// Turns on dynamic layout
//		setDynamicLayout(true);

		// Traps VM shutdown
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());

		// Listens to certain configuration events
		ConfigurationManager.addConfigurationListener(this);
		
        // Check for newer version unless it was disabled
        String checkForUpdates = ConfigurationManager.getVariable("prefs.check_for_updates_on_startup", "true");
        if(!(checkForUpdates==null || checkForUpdates.equals("false")))
            new CheckVersionDialog(mainFrame, false);
        
		
		if(PlatformManager.getOsType()==PlatformManager.MAC_OS_X) {
			try {
				FinderIntegration finderIntegration = new FinderIntegration();
			}
			catch(Exception e) {
				if(com.mucommander.Debug.TRACE)
					System.out.println("Launcher.init: exception thrown while initializing Mac Finder integration");
			}
		}
		
		// Dispose splash screen
		splashScreen.dispose();
	}

//	/**
//	 * Turns on or off dynamic layout which updates layout while resizing a frame. This
//	 * is a 1.4 only feature and may not be supported by the underlying OS and window manager.
//	 */
//	public static void setDynamicLayout(boolean b) {
//		try {
//			java.awt.Toolkit.getDefaultToolkit().setDynamicLayout(b);
//		}
//		catch(NoSuchMethodError e) {
//		}
//	}


	/**
	 * Returns the initial left/right folder according to user preferences.
	 */ 
	private AbstractFile getInitialFolder(boolean left) {
		
		// Default path (if home folder or last folder doesn't exist or fails) is current drive
		// for left folder and user home for right folder
		String defaultPath;
		if (left) {				
			AbstractFile tempFolder = new FSFile(new File(""));
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

		if(Debug.TRACE)
			System.out.println("defaultPath "+defaultPath);
		
		AbstractFile folder = null;
		if(folderPath!=null)
			folder = AbstractFile.getAbstractFile(folderPath);
		
		if(folder==null || !folder.exists())
			folder = AbstractFile.getAbstractFile(defaultPath);

		if(Debug.TRACE)
			System.out.println("initial folder "+folder.getAbsolutePath());
		
		return folder;
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
		
		AbstractFile tempFile;
		if (mainFrames.size()==0) {
			folder1 = getInitialFolder(true);
			folder2 = getInitialFolder(false);
		}
		else {
			folder1 = currentMainFrame.getBrowser1().getFileTable().getCurrentFolder();
			folder2 = currentMainFrame.getBrowser2().getFileTable().getCurrentFolder();
		}
		
		MainFrame newMainFrame = new MainFrame(folder1, folder2);
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
	 * Properly disposes the given MainFrame.
	 */
	public void disposeMainFrame(MainFrame mainFrameToDispose) {
		// Saves last folders
		ConfigurationManager.setVariable("prefs.startup_folder.left.last_folder", 
			mainFrameToDispose.getBrowser1().getCurrentFolder().getAbsolutePath(true));
		ConfigurationManager.setVariable("prefs.startup_folder.right.last_folder", 
			mainFrameToDispose.getBrowser2().getCurrentFolder().getAbsolutePath(true));
				
		JMenu windowMenu;
		int frameIndex = mainFrames.indexOf(mainFrameToDispose);

		// Disposes the MainFrame
		mainFrameToDispose.dispose();
		mainFrames.remove(mainFrameToDispose);
		
		MainFrame mainFrame;
		JMenuItem item;
		int nbFrames = mainFrames.size();
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
	 * Notifies this MainFrame's currentFolder has changed so
	 * that window title and menu items can be updated.
	 */
	public void locationChanged(FolderPanel folderPanel) {
		AbstractFile currentFolder = folderPanel.getCurrentFolder();
		MainFrame mainFrame = folderPanel.getMainFrame();
		String currentPath = currentFolder.getAbsolutePath()+currentFolder.getSeparator();
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


    /**
	 * Shows a spash screen
	 */
	private JWindow showSplashScreen() {
		JWindow splashScreen = new JWindow();

		// Resolves the URL of the image within the JAR file
		URL imageURL = getClass().getResource("/logo.gif");

		splashScreen.getContentPane().add(new JLabel(new ImageIcon(imageURL)));
		splashScreen.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		splashScreen.setLocation(screenSize.width/2 - splashScreen.getSize().width/2,
					     screenSize.height/2 - splashScreen.getSize().height/2);
	    splashScreen.show();
	
		return splashScreen;
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
		Launcher.getLauncher().disposeMainFrame((MainFrame)e.getSource());
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
     * Listens to certain configuration variables.
     */
    public boolean configurationChanged(ConfigurationEvent event) {
    	String var = event.getVariable();
    
    	// /!\ font.size is set after font.family in AppearancePrefPanel
    	// that's why we only listen to this one in order not to change Font twice
    	if (var.equals("prefs.lookAndFeel")) {
			if(Debug.TRACE)
				System.out.println("LookAndFeel changed! "+event.getValue());
    		String lnfName = event.getValue();
			
			setLookAndFeel(lnfName);
		}
    
    	return true;
    }
}