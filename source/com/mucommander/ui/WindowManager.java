
package com.mucommander.ui;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.ShutdownHook;
import com.mucommander.ui.auth.AuthDialog;
import com.mucommander.auth.AuthException;
import com.mucommander.auth.MappedCredentials;
import com.mucommander.conf.*;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.ui.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Vector;


/**
 * Window Manager is responsible for creating, disposing, switching,
 * in other words managing :) muCommander windows.
 *
 * @author Maxence Bernard
 */
//public class WindowManager implements ActionListener, WindowListener, ActivePanelListener, LocationListener, ConfigurationListener {
public class WindowManager implements WindowListener, ConfigurationListener {
    // - Folder frame identifiers -----------------------------------------------
    // --------------------------------------------------------------------------
    // The following constants are used to identify the left and right folder frames
    // in the configuration file.

    /** Configuration identifier for the left folder frame. */
    private static final int LEFT_FRAME  = 0;
    /** Configuration identifier for the right folder frame. */
    private static final int RIGHT_FRAME = 1;



    // - MainFrame positioning --------------------------------------------------
    // --------------------------------------------------------------------------
    // The following constants are used to compute the proper position of a new MainFrame.

    /** Number of pixels a new MainFrame will be moved to the left from its parent. */
    private static final int X_OFFSET = 22;
    /** Number of pixels a new MainFrame will be moved down from its parent. */
    private static final int Y_OFFSET = 22;



    /** MainFrame (main muCommander window) instances */
    private static Vector mainFrames;
    
    /** MainFrame currently being used (that has focus),
     * or last frame to have been used if muCommander doesn't have focus */	
    private static MainFrame currentMainFrame;

    private static WindowManager instance = new WindowManager();


    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------

    static {
        // Sets custom lookAndFeel if different from current lookAndFeel
        String lnfName = ConfigurationManager.getVariable(ConfigurationVariables.LOOK_AND_FEEL);
        if(lnfName!=null && !lnfName.equals(UIManager.getLookAndFeel().getName()))
            setLookAndFeel(lnfName);

        // In debug mode, trace un-initialised preference files.
        if(Debug.ON)
            if(lnfName == null)
                Debug.trace("Could load look'n feel from preferences");

        mainFrames = new Vector();
        instance   = new WindowManager();
    }

    /**
     * Creates a new instance of WindowManager.
     */
    private WindowManager() {ConfigurationManager.addConfigurationListener(this);}

    /**
     * Retrieves the user's initial path for the specified frame.
     * <p>
     * If the path found in preferences is either illegal or does not exist, this method will
     * return the user's home directory - we assume this will always exist, which might be a bit
     * of a leap of faith.
     * </p>
     * @param  frame frame for which the initial path should be returned (either {@link #LEFT_FRAME} or
     *               {@link #RIGHT_FRAME}).
     * @return       the user's initial path for the specified frame.
     */ 
    private static AbstractFile getInitialPath(int frame) {
        boolean      isCustom;   // Whether the initial path is a custom one or the last used folder.
        String       folderPath; // Path to the initial folder.
        AbstractFile folder;     // Initial folder.

        // Checks which kind of initial path we're dealing with.
        isCustom = (frame == LEFT_FRAME ? ConfigurationManager.getVariable(ConfigurationVariables.LEFT_STARTUP_FOLDER, ConfigurationVariables.DEFAULT_STARTUP_FOLDER) :
                    ConfigurationManager.getVariable(ConfigurationVariables.RIGHT_STARTUP_FOLDER, ConfigurationVariables.DEFAULT_STARTUP_FOLDER)).equals(ConfigurationVariables.STARTUP_FOLDER_CUSTOM);

        // Handles custom initial paths.
        if (isCustom)
            folderPath = (frame == LEFT_FRAME ? ConfigurationManager.getVariable(ConfigurationVariables.LEFT_CUSTOM_FOLDER) :
                          ConfigurationManager.getVariable(ConfigurationVariables.RIGHT_CUSTOM_FOLDER));

        // Handles "last folder" initial paths.
        else
            folderPath = (frame == LEFT_FRAME ? ConfigurationManager.getVariable(ConfigurationVariables.LAST_LEFT_FOLDER) :
                          ConfigurationManager.getVariable(ConfigurationVariables.LAST_RIGHT_FOLDER));

        // If the initial path is not legal or does not exist, defaults to the user's home.
        if(folderPath == null || (folder = FileFactory.getFile(folderPath)) == null || !folder.exists())
            folder = FileFactory.getFile(System.getProperty("user.home"));

        if(Debug.ON) Debug.trace("initial folder= "+folder);
        return folder;
    }

    /**
     * Returns a valid initial abstract path for the specified frame.
     * <p>
     * This method does its best to interpret <code>path</code> properly, or to fail
     * politely if it can't. This means that:<br/>
     * - we first try to see whether <code>path</code> is a legal, existing URI.<br/>
     * - if it's not, we check whether it might be a legal local, existing file path.<br/>
     * - if it's not, we'll just use the default initial path for the frame.<br/>
     * - if <code>path</code> is browsable (eg directory, archive, ...), use it as is.<br/>
     * - if it's not, use its parent.<br/>
     * - if it does not have a parent, use the default initial path for the frame.<br/>
     * </p>
     * @param  path  path to the folder we want to open in <code>frame</code>.
     * @param  frame identifer of the frame we want to compute the path for (either {@link #LEFT_FRAME} or
     *               {@link #RIGHT_FRAME}).
     * @return       our best shot at what was actually requested.
     */
    private static AbstractFile getInitialAbstractPath(String path, int frame) {
        // This is one of those cases where a null value actually has a proper meaning.
        if(path == null)
            return getInitialPath(frame);

        // Tries the specified path as-is.
        AbstractFile file;
        MappedCredentials newCredentials = null;

        while(true) {
            try {
                file = FileFactory.getFile(path, true);
                if(!file.exists())
                    file = null;
                break;
            }
            // If an AuthException occured, gets login credential from the user.
            catch(Exception e) {
                if(e instanceof AuthException) {
                    // Prompts the user for a login and password.
                    AuthException authException = (AuthException)e;
                    AuthDialog authDialog = new AuthDialog(currentMainFrame, authException.getFileURL(), authException.getMessage());
                    authDialog.showDialog();
                    newCredentials = authDialog.getCredentials();
                    if(newCredentials!=null) {
                        path = newCredentials.getRealm().getStringRep(true);
                    }
                    // If the user cancels, we fall back to the default path.
                    else {
                        return getInitialPath(frame);
                    }
                }
                else {
                    file = null;
                    break;
                }
            }
        }

        // If the specified path does not work out,
        if(file == null)
            // Tries the specified path as a relative path.
            if((file = FileFactory.getFile(new File(path).getAbsolutePath())) == null || !file.exists())
                // Defaults to home.
                return getInitialPath(frame);

        // If the specified path is a non-browsable, uses its parent.
        if(!file.isBrowsable())
            // This is just playing things safe, as I doubt there might ever be a case of
            // a file without a parent directory.
            if((file = file.getParent()) == null)
                return getInitialPath(frame);

        return file;
    }
    
    
    /**
     * Returns the sole instance of WindowManager.
     */
    public static WindowManager getInstance() {
        return instance;
    }
    
	
    /**
     * Returns the MainFrame instance that currently is active.
     */
    public static MainFrame getCurrentMainFrame() {
        return currentMainFrame;
    }
	
    /**
     * Returns a Vector of all MainFrame instances the application has.
     */
    public static Vector getMainFrames() {
        return mainFrames;
    }

    /**
     * Creates a new MainFrame and makes it visible on the screen, on top of any other frames.
     * <p>
     * The initial path of each frame will differ depending on whether this is the first mainframe
     * we create or not.<br/>
     * If it is, we'll use the user's default paths. If it's not, the current mainframe's paths will
     * be used.
     * </p>
     * @return a fully initialised mainframe.
     */	
    public static synchronized MainFrame createNewMainFrame() {
        if(currentMainFrame == null)
            return createNewMainFrame(getInitialPath(LEFT_FRAME), getInitialPath(RIGHT_FRAME));
        return createNewMainFrame(currentMainFrame.getFolderPanel1().getFileTable().getCurrentFolder(),
                                  currentMainFrame.getFolderPanel2().getFileTable().getCurrentFolder());
    }

    /**
     * Creates a new MainFrame and makes it visible on the screen, on top of any other frame.
     * @param  folder1 path on which the left frame will be opened.
     * @param  folder2 path on which the right frame will be opened.
     * @return         a fully initialised mainframe.
     */
    public static synchronized MainFrame createNewMainFrame(String folder1, String folder2) {
        return createNewMainFrame(getInitialAbstractPath(folder1, LEFT_FRAME),
                                  getInitialAbstractPath(folder2, RIGHT_FRAME));
    }

    /**
     * Creates a new MainFrame and makes it visible on the screen, on top of any other frames.
     * @param folder1 initial path for the left frame.
     * @param folder2 initial path for the right frame.
     */
    public static synchronized MainFrame createNewMainFrame(AbstractFile folder1, AbstractFile folder2) {
        MainFrame newMainFrame; // New MainFrame.
        Dimension screenSize;   // Used to compute the new MainFrame's proper location.
        int       x;            // Horizontal position of the new MainFrame.
        int       y;            // Vertical position of the new MainFrame.
        int       width;        // Width of the new MainFrame.
        int       height;       // Height of the new MainFrame.

        // Initialisation.
        newMainFrame = new MainFrame(folder1, folder2);
        screenSize   = Toolkit.getDefaultToolkit().getScreenSize();


        // - Initial window dimensions --------------------------
        // ------------------------------------------------------
        // If this is the first window, retrieve initial dimensions from preferences.
        if(mainFrames.isEmpty()) {
            currentMainFrame = newMainFrame;
            // Retrieve last saved window bounds
            x      = ConfigurationManager.getVariableInt(ConfigurationVariables.LAST_X);
            y      = ConfigurationManager.getVariableInt(ConfigurationVariables.LAST_Y);
            width  = ConfigurationManager.getVariableInt(ConfigurationVariables.LAST_WIDTH);
            height = ConfigurationManager.getVariableInt(ConfigurationVariables.LAST_HEIGHT);

            // Retrieves the last known size of the screen.
            int lastScreenWidth  = ConfigurationManager.getVariableInt(ConfigurationVariables.SCREEN_WIDTH);
            int lastScreenHeight = ConfigurationManager.getVariableInt(ConfigurationVariables.SCREEN_HEIGHT);

            // If no previous location was saved, or if the resolution has changed,
            // reset the window's dimensions to their default values.
            if(x == -1 || y == -1 || width == -1 || height == -1 ||
               screenSize.width != lastScreenWidth ||  screenSize.height != lastScreenHeight
               || width + x > screenSize.width + 5 || height + y > screenSize.height + 5) {

                // Full screen bounds are not reliable enough, in particular under Linux+Gnome
                // so we simply make the initial window 4/5 of screen's size, and center it.
                // This should fit under any window manager / platform
                x      = screenSize.width / 10;
                y      = screenSize.height / 10;
                width  = (int)(screenSize.width * 0.8);
                height = (int)(screenSize.height * 0.8);
            }
        }

        // If this is *not* the first window, use the same dimensions as the previous MainFrame, with
        // a slight horizontal and vertical offset to make sure we keep both of them visible.
        else {
            x             = currentMainFrame.getX() + X_OFFSET;
            y             = currentMainFrame.getY() + Y_OFFSET;
            width         = currentMainFrame.getWidth();
            height        = currentMainFrame.getHeight();

            // Make sure we're still within the screen.
            // Note that while the width and height tests look redundant, they are required. Some
            // window managers, such as Gnome, return rather peculiar results.
            if(!isInsideUsableScreen(currentMainFrame, x + width, -1))
                x = 0;
            if(!isInsideUsableScreen(currentMainFrame, -1, y + height))
                y = 0;
            if(width + x > screenSize.width)
                width = screenSize.width - x;
            if(height + y > screenSize.height)
                height = screenSize.height - y;
        }
        newMainFrame.setBounds(new Rectangle(x, y, width, height));

        // To catch user window closing actions
        newMainFrame.addWindowListener(instance);

        // Adds the new MainFrame to the vector
        mainFrames.add(newMainFrame);

        // Set new window's title. Window titles show window number only if there is more than one window.
        // So if a second window was just created, we update first window's title so that it shows window number (#1).
        newMainFrame.updateWindowTitle();
        if(mainFrames.size()==2)
            ((MainFrame)mainFrames.elementAt(0)).updateWindowTitle();

        // Make this new frame visible
        newMainFrame.setVisible(true);

        return newMainFrame;
    }

    /**
     * Properly disposes the given MainFrame.
     */
/*
    public static synchronized void disposeMainFrame(MainFrame mainFrameToDispose) {
        // Saves last folders
        ConfigurationManager.setVariable("prefs.startup_folder.left.last_folder", 
                                         mainFrameToDispose.getFolderPanel1().getFolderHistory().getLastRecallableFolder());
        ConfigurationManager.setVariable("prefs.startup_folder.right.last_folder", 
                                         mainFrameToDispose.getFolderPanel2().getFolderHistory().getLastRecallableFolder());

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

        // Update following window titles to reflect the MainFrame's disposal.
        // Window titles show window number only if there is more than one window.
        // So if there is only one window left, we update first window's title so that it removes window number (#1).
        int nbFrames = mainFrames.size();
        if(nbFrames==1) {
            ((MainFrame)mainFrames.elementAt(0)).updateWindowTitle();
        }
        else {
            for(int i=frameIndex; i<nbFrames; i++)
                ((MainFrame)mainFrames.elementAt(i)).updateWindowTitle();
        }
    }
*/

    /**
     * Disposes all opened windows, ending with the one that is currently active if there is one, 
     * or the last one which was activated.
     */
    public static synchronized void quit() {
        // Retrieve current MainFrame's index
        int currentMainFrameIndex = mainFrames.indexOf(currentMainFrame);
		
        // Dispose all MainFrames but the current one
        int nbFrames = mainFrames.size();
        for(int i=0; i<nbFrames; i++) {
            if(i!=currentMainFrameIndex)
                ((MainFrame)mainFrames.elementAt(i)).dispose();
        }
        
        // Dispose current MainFrame last so that its attributes (last folders, window position...) are saved last
        // in the preferences
        ((MainFrame)mainFrames.elementAt(currentMainFrameIndex)).dispose();

        // Dispose all other frames (viewers, editors...)
        Frame frames[] = Frame.getFrames();
        nbFrames = frames.length;
        Frame frame;
        for(int i=0; i<nbFrames; i++) {
            frame = frames[i];
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("frame#"+i+"= "+frame);
            if(frame.isShowing()) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("disposing frame#"+i);
                frame.dispose();
            }
        }

        // Initiate shutdown sequence.
        // Important note: we cannot rely on windowClosed() triggering the shutdown sequence as
        // Quit under OS X shuts down the app as soon as this method returns and as a result, 
        // windowClosed() events are never dispatched to the MainFrames
        ShutdownHook.initiateShutdown();
    }
	
	
    /**
     * Switches to the next MainFrame, in the order of which they were created.
     */
    public static void switchToNextWindow() {
        int frameIndex = mainFrames.indexOf(currentMainFrame);
        MainFrame mainFrame = (MainFrame)mainFrames.elementAt(frameIndex==mainFrames.size()-1?0:frameIndex+1);
        mainFrame.toFront();
    }

    /**
     * Switches to previous MainFrame, in the order of which they were created.
     */
    public static void switchToPreviousWindow() {
        int frameIndex = mainFrames.indexOf(currentMainFrame);
        MainFrame mainFrame = (MainFrame)mainFrames.elementAt(frameIndex==0?mainFrames.size()-1:frameIndex-1);
        mainFrame.toFront();
    }


    /**
     * Changes LooknFeel to the given one, updating the UI of each MainFrame.
     */
    private static void setLookAndFeel(String lnfName) {
        try {
            UIManager.setLookAndFeel(lnfName);

            for(int i=0; i<mainFrames.size(); i++) {
                SwingUtilities.updateComponentTreeUI((MainFrame)(mainFrames.elementAt(i)));
            }
        }
        catch(Exception e) {
        }
    }


    ////////////////////////////
    // WindowListener methods //
    ////////////////////////////

    public void windowActivated(WindowEvent e) {
        Object source = e.getSource();
        
        // Return if event doesn't originate from a MainFrame (e.g. ViewerFrame or EditorFrame)
        if(!(source instanceof MainFrame))
            return;

        currentMainFrame = (MainFrame)e.getSource();
        // Let MainFrame know that it is active in the foreground
        currentMainFrame.setForegroundActive(true);

        // Resets shift mode to false, since keyReleased events may have been lost during window switching
        CommandBar commandBar = currentMainFrame.getCommandBar();
        if(commandBar!=null)
            commandBar.setAlternateActionsMode(false);
    }

    public void windowDeactivated(WindowEvent e) {
        Object source = e.getSource();

        // Return if event doesn't originate from a MainFrame (e.g. ViewerFrame or EditorFrame)
        if(!(source instanceof MainFrame))
            return;

        ((MainFrame)e.getSource()).setForegroundActive(false);
    }

    public void windowClosing(WindowEvent e) {
  //      Object source = e.getSource();

//        // Return if event doesn't originate from a MainFrame (e.g. ViewerFrame or EditorFrame)
//        if(!(source instanceof MainFrame))
//            return;

//        if(source instanceof MainFrame)
//            disposeMainFrame((MainFrame)source);
    }
 
    
    /**
     * windowClosed is synchronized so that it doesn't get called while quit() is executing.
     */
    public synchronized void windowClosed(WindowEvent e) {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");

        Object source = e.getSource();

        if(source instanceof MainFrame) {
            // Remove disposed MainFrame from the MainFrame list
            int frameIndex = mainFrames.indexOf(source);

            mainFrames.remove(source);

            // Update following windows titles to reflect the MainFrame's disposal.
            // Window titles show window number only if there is more than one window.
            // So if there is only one window left, we update first window's title so that it removes window number (#1).
            int nbFrames = mainFrames.size();
            if(nbFrames==1) {
                ((MainFrame)mainFrames.elementAt(0)).updateWindowTitle();
            }
            else {
                if(frameIndex!=-1) {
                    for(int i=frameIndex; i<nbFrames; i++)
                        ((MainFrame)mainFrames.elementAt(i)).updateWindowTitle();
                }
            }
        }

        // Test if there is at least one MainFrame still showing
        if(mainFrames.size()>0)
            return;

        // Test if there is at least one window (viewer, editor...) still showing
        Frame frames[] = Frame.getFrames();
        int nbFrames = frames.length;
        Frame frame;
        for(int i=0; i<nbFrames; i++) {
            frame = frames[i];
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("frame#"+i+"= "+frame);
            if(frame.isShowing()) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("found active frame#"+i);
                return;
            }
        }

        // No more window showing, initiate shutdown sequence
        ShutdownHook.initiateShutdown();
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
    	if (var.equals(ConfigurationVariables.LOOK_AND_FEEL)) {
            if(Debug.ON) Debug.trace("LookAndFeel changed! "+event.getValue());
            String lnfName = event.getValue();

	    if(!UIManager.getLookAndFeel().getClass().getName().equals(lnfName)) {
		setLookAndFeel(lnfName);
		// Forces the whole UI to refresh is colors using the current theme values.
		ThemeManager.forceRefresh();
	    }
        }
    
    	return true;
    }



    // - Screen handling --------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Computes the screen's insets for the specified window.
     * <p>
     * While this might seem strange, screen insets can change from one window
     * to another. For example, on X11 windowing systems, there is no guarantee that
     * a window will be displayed on the same screen, let alone computer, as the one
     * the application is running on.
     * </p>
     * @param frame frame for which screen insets should be computed.
     */
    private static Insets getScreenInsets(Frame frame) {
        // Code for Java 1.4 and up
        if(PlatformManager.JAVA_VERSION >= PlatformManager.JAVA_1_4)
            // Java 1.4 has a method which returns real screen insets
            return Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());
        // Java 1.3
        return new Insets(PlatformManager.OS_FAMILY == PlatformManager.MAC_OS_X ? 22 : 0, 0, 0, 0);		
    }
	
	
    /**
     * Checks whether the specified frame can be moved to the specified coordinates and still
     * be fully visible.
     * <p>
     * Note that while this method is fairly accurate if we're running under Java. 1.4 or greater,
     * but any older version involves a lot of guesswork.
     * </p>
     * <p>
     * If <code>x</code> (resp. <code>y</code>) is <code>null</code>, this method won't test
     * whether the frame is within horizontal (resp. vertical) bounds.
     * </p>
     * @param frame frame who's visibility should be tested.
     * @param x     horizontal coordinate of the upper-leftmost corner of the area to check for.
     * @param y     vertical coordinate of the upper-leftmost corner of the area to check for.
     * @return      <code>true</code> if the frame can be moved at the specified location,
     *              <code>false</code> otherwise.
     */
    public static boolean isInsideUsableScreen(Frame frame, int x, int y) {
        Insets    screenInsets;
        Dimension screenSize;

        screenInsets = getScreenInsets(frame);
        screenSize   = Toolkit.getDefaultToolkit().getScreenSize();

        return (x < 0 || (x >= screenInsets.left && x < screenSize.width - screenInsets.right))
            && (y < 0 || (y >= screenInsets.top && y < screenSize.height - screenInsets.bottom));
    }
	
	
    /**
     * Returns the maximum dimensions for a full-screen window.
     * <p>
     * Note that while this method is fairly accurate if we're running under Java. 1.4 or greater,
     * but any older version involves a lot of guesswork.
     * </p>
     * @param window window who's full screen size should be computed.
     */
    public static Rectangle getFullScreenBounds(Window window) {
        Toolkit   toolkit;
        Dimension screenSize;

        toolkit    = Toolkit.getDefaultToolkit();
        screenSize = toolkit.getScreenSize();

        // Code for Java 1.4 and up
        if(PlatformManager.JAVA_VERSION >= PlatformManager.JAVA_1_4) {
            // Java 1.4 makes it easy to get full screen bounds
            Insets screenInsets = toolkit.getScreenInsets(window.getGraphicsConfiguration());		
            return new Rectangle(screenInsets.left, screenInsets.top, screenSize.width-screenInsets.left-screenInsets.right, screenSize.height-screenInsets.top-screenInsets.bottom);		
        }
        // Code for Java 1.3
        else {
            int x = 0;
            int y = 0;
            int width = screenSize.width;
            int height = screenSize.height;
			
            // Mac OS X, assuming that dock is at the bottom of the screen
            if(PlatformManager.OS_FAMILY == PlatformManager.MAC_OS_X) {
                // Menu bar height
                y += 22;
                height -= 22;
            }

            // Try to give enough space for 'everyone' with a 4/3 pixel ratio:
            // - for Window's task bar (ok)
            // - for Mac OS X's dock (not so sure)  
            width -= 60;
            height -= 45;
			
            return new Rectangle(x, y, width, height);		
        }
    }
}
