package com.mucommander.ui;

import com.mucommander.conf.*;
import com.mucommander.file.AbstractFile;
import com.mucommander.ui.action.ActionKeymap;
import com.mucommander.ui.comp.dialog.YBoxPanel;
import com.mucommander.ui.event.ActivePanelListener;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.table.FileTable;
import com.mucommander.PlatformManager;
import com.mucommander.Debug;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Iterator;
import java.util.Vector;
import java.util.WeakHashMap;


/**
 * This is the main frame, which contains all other UI components visible on a mucommander window.
 * 
 * @author Maxence Bernard
 */
public class MainFrame extends JFrame implements LocationListener, ComponentListener {
	
    // Variables related to split pane	
    private JSplitPane splitPane;
    private int splitPaneWidth = -1;
    private int dividerLocation;

    private FolderPanel folderPanel1;
    private FolderPanel folderPanel2;
	
    private FileTable table1;
    private FileTable table2;
    
    /** Active table in the MainFrame */
    private FileTable activeTable;

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

    /** Contains all registered ActivePanelListener instances, stored as weak references */
    private WeakHashMap activePanelListeners = new WeakHashMap();


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
        this.activeTable = table1;

        // Create toolbar and show it only if it hasn't been disabled in the preferences
        this.toolbar = new ToolBar(this);
        // Note: Toolbar.setVisible() has to be called no matter if Toolbar is visible or not, in order for it to be properly initialized
        this.toolbar.setVisible(ConfigurationManager.getVariableBoolean(ConfigurationVariables.TOOLBAR_VISIBLE, ConfigurationVariables.DEFAULT_TOOLBAR_VISIBLE));
			
        contentPane.add(toolbar, BorderLayout.NORTH);

        folderPanel1.getLocationManager().addLocationListener(this);
        folderPanel2.getLocationManager().addLocationListener(this);

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
        splitPane.setDividerLocation(0.5);

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
        this.commandBar.setVisible(ConfigurationManager.getVariableBoolean(ConfigurationVariables.COMMAND_BAR_VISIBLE, ConfigurationVariables.DEFAULT_COMMAND_BAR_VISIBLE));

        southPanel.add(commandBar);
		
        contentPane.add(southPanel, BorderLayout.SOUTH);
		
        // To monitor resizing actions
        folderPanel1.addComponentListener(this);
        splitPane.addComponentListener(this);

//        // Do nothing on close (default is to hide window),
//        // WindowManager takes of catching close events and do the rest
//        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // Dispose window on close
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        ActionKeymap.registerActions(this);

        // Fire table change events on registered ActivePanelListener instances, to notify of the intial active table.
        fireActivePanelChanged(activeTable.getFolderPanel());

        // Piece of code used in 0.8 beta1 and removed after because it's way too slow, kept here for the record 
        //		// Used by setNoEventsMode()
        //		JComponent glassPane = (JComponent)getGlassPane();
        //		glassPane.addMouseListener(new MouseAdapter() {});
        //		glassPane.addKeyListener(new KeyAdapter() {});

        // For testing purposes, full screen option could be nice to add someday
        //setUndecorated(true);
        //java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(this);

        // Cool but way too slow
        //		splitPane.setContinuousLayout(true);

//        // Set the custom FocusTraversalPolicy that manages focus for both FolderPanel and their subcomponents.
//        // Reflection is used to instanciate CustomFocusTraversalPolicy in order to get around 'NoClassDefFound' under Java 1.3.
//        if(PlatformManager.JAVA_VERSION >= PlatformManager.JAVA_1_4) {
//            try {
//                setFocusTraversalPolicy((FocusTraversalPolicy)Class.forName("com.mucommander.ui.MainFrame$CustomFocusTraversalPolicy").getDeclaredConstructor(new Class[]{getClass()}).newInstance(new Object[]{this}));
//            }
//            catch(Exception e) {
//                if(Debug.ON) Debug.trace("Exception thrown: "+e);
//            }
//        }

        // Set the custom FocusTraversalPolicy that manages focus for both FolderPanel and their subcomponents.
        setFocusTraversalPolicy(new CustomFocusTraversalPolicy());
    }


    /**
     * Registers the given ActivePanelListener to receive events when current table changes.
     */
    public void addActivePanelListener(ActivePanelListener activePanelListener) {
        activePanelListeners.put(activePanelListener, null);
    }

    /**
     * Unregisters the given ActivePanelListener so that it will no longer receive events when current table changes.
     */
    public void removeActivePanelListener(ActivePanelListener activePanelListener) {
        activePanelListeners.remove(activePanelListener);
    }

    /**
     * Fires table change events on registered ActivePanelListener instances.
     */
    private void fireActivePanelChanged(FolderPanel folderPanel) {
        Iterator iterator = activePanelListeners.keySet().iterator();
        while(iterator.hasNext())
            ((ActivePanelListener)iterator.next()).activePanelChanged(folderPanel);
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
     * Returns the command bar, i.e. the panel that contains
     * F3, F6... F10 buttons.
     */
    public CommandBar getCommandBar() {
        return commandBar;
    }


    /**
     * Returns the status bar where information about selected files and volume are displayed.
     */
    public StatusBar getStatusBar() {
        return this.statusBar;
    }


    /**
     * Returns the active FileTable in this MainFrame.
     *
     * The returned active table doesn't necessarily have focus, the focus can be in some other component
     * of the active {@link FolderPanel}, or nowhere in the MainFrame if the window is not in the foreground.
     *
     * <p>Use {@link FileTable#hasFocus()} to test if the table currently has focus.
     *
     * @see FileTable#isActiveTable()
     */
    public FileTable getActiveTable() {
        return activeTable;
    }

    /**
     * Sets currently active FileTable (called by FolderPanel).
     */
    void setActiveTable(FileTable table) {
        boolean activeTableChanged = activeTable !=table;

        if(activeTableChanged) {
            this.activeTable = table;

            // Update window title to reflect new active table
            updateWindowTitle();

            // Fire table change events on registered ActivePanelListener instances.
            fireActivePanelChanged(table.getFolderPanel());
        }
    }

	
    /**
     * Returns the complement to getActiveTable().
     */
    public FileTable getInactiveTable() {
        return activeTable ==table1?table2:table1;
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
     * Swaps the two FolderPanel instances: after a call to this method, folderPanel1 will be folderPanels2 and vice-versa.
     */
    public void swapFolders() {
        splitPane.remove(folderPanel1);
        splitPane.remove(folderPanel2);

        FolderPanel tempPanel = folderPanel1;
        folderPanel1 = folderPanel2;
        folderPanel2 = tempPanel;

        FileTable tempTable = table1;
        table1 = table2;
        table2 = tempTable;

        splitPane.setLeftComponent(folderPanel1);
        splitPane.setRightComponent(folderPanel2);
        splitPane.doLayout();
        splitPane.setDividerLocation(dividerLocation);

        activeTable.requestFocus();
    }


    /**
     * Makes both folders the same, choosing the one which is currently active. 
     */
    public void setSameFolder() {
        (activeTable ==table1?table2:table1).getFolderPanel().trySetCurrentFolder(activeTable.getCurrentFolder());
    }


    /**
     * Returns <code>true</code> if this MainFrame is active in the foreground.
     */
    public boolean isForegroundActive() {
        return foregroundActive;
    }

    /**
     * Sets whether this MainFrame is active in the foreground. Method to be called solely by WindowManager.
     */
    void setForegroundActive(boolean foregroundActive) {
        this.foregroundActive = foregroundActive;
    }


    /**
     * Updates this window's title to show currently active folder and window number.
     * This method is called by this class and WindowManager.
     */
    public void updateWindowTitle() {
        // Update window title
        String title = activeTable.getCurrentFolder().getAbsolutePath()+" - muCommander";
        Vector mainFrames = WindowManager.getMainFrames();
        if(mainFrames.size()>1)
            title += " ["+(mainFrames.indexOf(this)+1)+"]";
        setTitle(title);
    }
    

    //////////////////////////////
    // LocationListener methods //
    //////////////////////////////
	
    public void locationChanged(LocationEvent e) {
        // Update window title to reflect the new current folder
        updateWindowTitle();
    }

    public void locationChanging(LocationEvent e) {
    }
	
    public void locationCancelled(LocationEvent e) {
    }

    public void locationFailed(LocationEvent e) {
    }


    ///////////////////////
    // Overriden methods //
    ///////////////////////

    /**
     * Overrides java.awt.Window's dispose method to save last MainFrame's attributes in the preferences
     * before disposing this MainFrame.
     */
    public void dispose() {
        // Save last MainFrame's attributes (last folders, window position) in the preferences.
//        if(WindowManager.getMainFrames().size()==1) {
        // Save last folders
        ConfigurationManager.setVariable(ConfigurationVariables.LAST_RIGHT_FOLDER, 
                                         getFolderPanel1().getFolderHistory().getLastRecallableFolder());
        ConfigurationManager.setVariable(ConfigurationVariables.LAST_LEFT_FOLDER, 
                                         getFolderPanel2().getFolderHistory().getLastRecallableFolder());

        // Save window position, size and screen resolution
        Rectangle bounds = getBounds();
        ConfigurationManager.setVariableInt("prefs.last_window.x", (int)bounds.getX());
        ConfigurationManager.setVariableInt("prefs.last_window.y", (int)bounds.getY());
        ConfigurationManager.setVariableInt("prefs.last_window.width", (int)bounds.getWidth());
        ConfigurationManager.setVariableInt("prefs.last_window.height", (int)bounds.getHeight());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        ConfigurationManager.setVariableInt("prefs.last_window.screen_width", screenSize.width);
        ConfigurationManager.setVariableInt("prefs.last_window.screen_height", screenSize.height);
    
        // Finally, dispose the frame
        super.dispose(); 
    }


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
                splitPane.setDividerLocation(0.5);
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


    /**
     * Manages focus for both FolderPanel and their subcomponents.
     *
     * @author Maxence Bernard
     */
    protected class CustomFocusTraversalPolicy extends FocusTraversalPolicy {

        public Component getComponentAfter(Container container, Component component) {
    if(Debug.ON) Debug.trace("container="+container.getClass().getName()+" component="+component.getClass().getName());
            if(component==folderPanel1.getLocationComboBox().getTextField() || component==folderPanel1.getLocationComboBox())
                return table1;
            else if(component==table1)
                return table2;
            if(component==folderPanel2.getLocationComboBox().getTextField() || component==folderPanel2.getLocationComboBox())
                return table2;
            else    // component==table2
                return table1;
        }

        public Component getComponentBefore(Container container, Component component) {
    if(Debug.ON) Debug.trace("container="+container.getClass().getName()+" component="+component.getClass().getName());
            // Completly symetrical with getComponentAfter
            return getComponentAfter(container, component);
       }

        public Component getFirstComponent(Container container) {
    if(Debug.ON) Debug.trace("container="+container.getClass().getName());
            return table1;
        }

        public Component getLastComponent(Container container) {
    if(Debug.ON) Debug.trace("container="+container.getClass().getName());
            return table2;
        }

        public Component getDefaultComponent(Container container) {
    if(Debug.ON) Debug.trace("container="+container.getClass().getName());
            return getActiveTable();
        }
    }
}
