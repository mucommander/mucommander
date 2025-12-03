/*
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.TableColumnModel;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.archive.AbstractArchiveEntryFile;
import com.mucommander.commons.file.protocol.local.LocalFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.util.ui.layout.YBoxPanel;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.desktop.ActionType;
import com.mucommander.preload.PreloadedJFrame;
import com.mucommander.snapshot.MuSnapshot;
import com.mucommander.ui.action.ActionKeymap;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.button.ToolbarMoreButton;
import com.mucommander.ui.event.ActivePanelListener;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.layout.ProportionalSplitPane;
import com.mucommander.ui.main.commandbar.CommandBar;
import com.mucommander.ui.main.menu.MainMenuBar;
import com.mucommander.ui.main.table.Column;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.FileTableConfiguration;
import com.mucommander.ui.main.table.SortInfo;
import com.mucommander.ui.main.tabs.ConfFileTableTab;
import com.mucommander.ui.main.toolbar.ToolBar;
import com.mucommander.ui.notifier.NotifierProvider;
import com.mucommander.ui.terminal.TerminalIntegration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main frame, which contains all other UI components visible on a mucommander window.
 * 
 * @author Maxence Bernard
 */
public class MainFrame implements LocationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainFrame.class);

    private final JFrame frameInstance;

    private ProportionalSplitPane foldersSplitPane;

    private JSplitPane verticalSplitPane;

    private FolderPanel leftFolderPanel;
    private FolderPanel rightFolderPanel;

    private FileTable leftTable;
    private FileTable rightTable;
    
    /** Active table in the MainFrame */
    private FileTable activeTable;

    /** The main panel (i.e. in the center) */
    private JPanel insetsPane;

    /** Toolbar panel */
    private JPanel toolbarPanel;

    /** Toolbar component */
    private ToolBar toolbar;

    /** Status bar instance */
    private StatusBar statusBar;

    /** Command bar instance */
    private CommandBar commandBar;

    /** Is no events mode enabled ? */
    private boolean noEventsMode;

    /** Is this MainFrame active in the foreground ? */
    private boolean foregroundActive;

    /** Is single panel view? */
    private boolean singlePanel;

    /** Terminal integration instance */
    private TerminalIntegration terminalIntegration;

    /** Contains all registered ActivePanelListener instances, stored as weak references */
    private final Map<ActivePanelListener, ?> activePanelListeners = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Sets the window icon, using the best method (Java 1.6's Window#setIconImages when available, Window#setIconImage
     * otherwise) and icon resolution(s) (OS-dependent).
     */
    private void setWindowIcon() {
        // TODO: this code should probably be moved to the desktop API
        // - Mac OS X completely ignores calls to #setIconImage/setIconImages, no need to waste time
        if(!OsFamily.MAC_OS.isCurrent()) {

            List<Image> icons = new ArrayList<>();

            // Start by adding a 16x16 image with 1-bit transparency, any OS should support that.
            icons.add(IconManager.getIcon(IconManager.MUCOMMANDER_ICON_SET, "icon16_8.png").getImage());

            // Add PNG 24 images (8-bit transparency)
            icons.add(IconManager.getIcon(IconManager.MUCOMMANDER_ICON_SET, "icon16_24.png").getImage());
            icons.add(IconManager.getIcon(IconManager.MUCOMMANDER_ICON_SET, "icon32_24.png").getImage());
            icons.add(IconManager.getIcon(IconManager.MUCOMMANDER_ICON_SET, "icon48_24.png").getImage());
            icons.add(IconManager.getIcon(IconManager.MUCOMMANDER_ICON_SET, "icon128_24.png").getImage());
            icons.add(IconManager.getIcon(IconManager.MUCOMMANDER_ICON_SET, "icon256_24.png").getImage());

            getJFrame().setIconImages(icons);
        }
    }

    private void init(Future<FolderPanel> leftFolderPanel, Future<FolderPanel> rightFolderPanel, ExecutorService executor) throws ExecutionException, InterruptedException {
        // Set the window icon
        setWindowIcon();
        // Register jobs listeners for UI notification purposes
        NotifierProvider.registerJobsListeners();

        DesktopManager.customizeMainFrame(getJFrame());

        // Enable window resize
        getJFrame().setResizable(true);

        // The toolbar should have no inset, this is why it is left out of the insetsPane
        JPanel contentPane = new JPanel(new BorderLayout());
        getJFrame().setContentPane(contentPane);

        insetsPane = new JPanel(new BorderLayout()) {
            // Add an x=3,y=3 gap around content pane
            @Override
            public Insets getInsets() {
                return new Insets(0, 3, 3, 3);      // No top inset
            }
        };
        // Below the toolbar there is the pane with insets
        contentPane.add(insetsPane, BorderLayout.CENTER);

        // Initializes the folder panels and file tables.
        this.leftFolderPanel = leftFolderPanel.get();
        this.rightFolderPanel = rightFolderPanel.get();
        leftTable = this.leftFolderPanel.getFileTable();
        rightTable = this.rightFolderPanel.getFileTable();
        activeTable = leftTable;

        // Listen to location change events to display the current folder in the window's title
        this.leftFolderPanel.getLocationManager().addLocationListener(this);
        this.rightFolderPanel.getLocationManager().addLocationListener(this);

        // Create the toolbar and corresponding panel wrapping it, and show it only if it hasn't been disabled in the
        // preferences.
        // Note: Toolbar.setVisible() has to be called no matter if Toolbar is visible or not, in order for it to be
        // properly initialized
        executor.execute(() -> {
            this.toolbar = new ToolBar(this);
            this.toolbarPanel = ToolbarMoreButton.wrapToolBar(toolbar);
            this.toolbarPanel.setVisible(MuConfigurations.getPreferences().getVariable(MuPreference.TOOLBAR_VISIBLE, MuPreferences.DEFAULT_TOOLBAR_VISIBLE));
            contentPane.add(toolbarPanel, BorderLayout.NORTH);
        });

        executor.execute(() -> {
            // Create menu bar (has to be created after toolbar) - ok, but why?
            // PSko - I guess it is related to loading Actions and that icons
            // for toolbar action should have icons, but in menu they should not.
            // However, still I don't get how setting the icon to null here in MenuToolkit#addMenuItem
            // impacts menu icons....... if nullify is commented-out there, then icons all of sudden
            // show in the menu causing this: https://github.com/mucommander/mucommander/issues/1178
            MainMenuBar menuBar = new MainMenuBar(this);
            SwingUtilities.invokeLater(() -> {
                getJFrame().setJMenuBar(menuBar);
                getJFrame().revalidate();
            });
        });

        // Create the split pane that separates folder panels and allows to resize how much space is allocated to the
        // both of them. The split orientation is loaded from and saved to the preferences.
        // Note: the vertical/horizontal terminology used in muCommander is just the opposite of the one used
        // in JSplitPane which is anti-natural / confusing.
        foldersSplitPane = new ProportionalSplitPane(getJFrame(),
                MuSnapshot.getSnapshot().getVariable(
                        MuSnapshot.getSplitOrientation(0),
                        MuSnapshot.DEFAULT_SPLIT_ORIENTATION).equals(MuSnapshot.VERTICAL_SPLIT_ORIENTATION) ?
                        JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT,
                        false,
                        MainFrame.this.leftFolderPanel.getPanel(),
                        MainFrame.this.rightFolderPanel.getPanel()) {
            // We don't want any extra space around split pane
            @Override
            public Insets getInsets() {
                return new Insets(0, 0, 0, 0);
            }
        };

        // Remove any default border the split pane has
        foldersSplitPane.setBorder(null);

        // Add buttons that allow to collapse and expand the split pane in both directions
        foldersSplitPane.setOneTouchExpandable(true);

        // Disable all the JSplitPane accessibility shortcuts that are registered by default, as some of them
        // conflict with default mucommander action shortcuts (e.g. F6 and F8) 
        foldersSplitPane.disableAccessibilityShortcuts();

        verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true) {
            @Override
            public Insets getInsets() {
                return new Insets(0, 0, 0, 0);
            }
        };
        verticalSplitPane.setBorder(null);
        verticalSplitPane.setOneTouchExpandable(true);
        {   // Do the same as in ProportionalSplitPane#disableAccessibilityShortcuts (foldersSplitPane)
            InputMap inputMap = verticalSplitPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            inputMap.clear();
            inputMap.setParent(null);
        }
        verticalSplitPane.setTopComponent(foldersSplitPane);
        verticalSplitPane.setBottomComponent(null);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(new BorderLayout());
        layeredPane.add(verticalSplitPane, BorderLayout.CENTER);
        layeredPane.setLayer(verticalSplitPane, JLayeredPane.DEFAULT_LAYER);
        // Split pane will be given any extra space
        insetsPane.add(layeredPane, BorderLayout.CENTER);

        terminalIntegration = new TerminalIntegration(this, verticalSplitPane);

        // Add a 2-pixel gap between the file table and status bar
        YBoxPanel southPanel = new YBoxPanel();
        southPanel.addSpace(2);

        executor.execute(() -> {
            // Add status bar
            this.statusBar = new StatusBar(this);
            southPanel.add(statusBar);

            // Show command bar only if it hasn't been disabled in the preferences
            this.commandBar = new CommandBar(this);
            // Note: CommandBar.setVisible() has to be called no matter if CommandBar is visible or not, in order for it to be properly initialized
            this.commandBar.setVisible(MuConfigurations.getPreferences().getVariable(MuPreference.COMMAND_BAR_VISIBLE, MuPreferences.DEFAULT_COMMAND_BAR_VISIBLE));
            southPanel.add(commandBar);
            insetsPane.add(southPanel, BorderLayout.SOUTH);
        });

        // Perform CloseAction when the user asked the window to close
        getJFrame().setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        getJFrame().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ActionManager.performAction(ActionType.CloseWindow, MainFrame.this);
            }
        });

        ActionKeymap.registerActions(this);

        // Fire table change events on registered ActivePanelListener instances, to notify of the initial active table.
        fireActivePanelChanged(activeTable.getFolderPanel());

        // Set the custom FocusTraversalPolicy that manages focus for both FolderPanel and their sub components.
        getJFrame().setFocusTraversalPolicy(new CustomFocusTraversalPolicy());
    }

    public MainFrame(ConfFileTableTab leftTab, FileTableConfiguration leftTableConf,
                     ConfFileTableTab rightTab, FileTableConfiguration rightTableConf) {
        this(new ConfFileTableTab[] {leftTab}, 0, leftTableConf, new ConfFileTableTab[] {rightTab}, 0, rightTableConf);
    }

    /**
     * Creates a new main frame set to the given initial folders.
     */
    public MainFrame(ConfFileTableTab[] leftTabs, int indexOfLeftSelectedTab, FileTableConfiguration leftTableConf,
                     ConfFileTableTab[] rightTabs, int indexOfRightSelectedTab, FileTableConfiguration rightTableConf) {
        super();    // left to easily debug the performance
        frameInstance = PreloadedJFrame.getJFrame(this);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            var leftFolderPanel = executor.submit(() -> new FolderPanel(this, leftTabs, indexOfLeftSelectedTab, leftTableConf));
            var rightFolderPanel = executor.submit(() -> new FolderPanel(this, rightTabs, indexOfRightSelectedTab, rightTableConf));
            init(leftFolderPanel, rightFolderPanel, executor);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }

        for (boolean isLeft = true; ; isLeft=false) {
            FileTable fileTable = isLeft ? leftTable : rightTable;
            fileTable.sortBy(Column.valueOf(MuSnapshot.getSnapshot().getVariable(MuSnapshot.getFileTableSortByVariable(0, isLeft), MuSnapshot.DEFAULT_SORT_BY).toUpperCase()),
                    !MuSnapshot.getSnapshot().getVariable(MuSnapshot.getFileTableSortOrderVariable(0, isLeft), MuSnapshot.DEFAULT_SORT_ORDER).equals(MuSnapshot.SORT_ORDER_DESCENDING));

            FolderPanel folderPanel = isLeft ? leftFolderPanel : rightFolderPanel;
            folderPanel.setTreeWidth(MuSnapshot.getSnapshot().getVariable(MuSnapshot.getTreeWidthVariable(0, isLeft), 150));
            folderPanel.setTreeVisible(MuSnapshot.getSnapshot().getVariable(MuSnapshot.getTreeVisiblityVariable(0, isLeft), false));

            if (!isLeft) {
                break;
            }
        }
    }

    /**
     * Copy constructor
     */
    public MainFrame(MainFrame mainFrame) {
        frameInstance = PreloadedJFrame.getJFrame(this);

        FolderPanel leftFolderPanel = mainFrame.getLeftPanel();
        FolderPanel rightFolderPanel = mainFrame.getRightPanel();
        FileTable leftFileTable = leftFolderPanel.getFileTable();
        FileTable rightFileTable = rightFolderPanel.getFileTable();

        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            init(CompletableFuture.completedFuture( // non-async
                        new FolderPanel(this, new ConfFileTableTab[] {
                                new ConfFileTableTab(leftFolderPanel.getCurrentFolder().getURL())},
                                0, leftFileTable.getConfiguration())),
                    CompletableFuture.completedFuture(
                            new FolderPanel(this, new ConfFileTableTab[] {
                                    new ConfFileTableTab(rightFolderPanel.getCurrentFolder().getURL())},
                                    0, rightFileTable.getConfiguration())),
                    executor);

            // TODO: Sorting should be part of the FileTable configuration
            this.leftTable.sortBy(leftFileTable.getSortInfo());
            this.rightTable.sortBy(rightFileTable.getSortInfo());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }
    }

    public JFrame getJFrame() {
        return frameInstance;
    }

    /**
     * Registers the given ActivePanelListener to receive events when the active table changes.
     *
     * @param activePanelListener the ActivePanelListener to add
     */
    public void addActivePanelListener(ActivePanelListener activePanelListener) {
        activePanelListeners.put(activePanelListener, null);
    }

    /**
     * Unregisters the given ActivePanelListener so that it no longer receives events when the active table changes.
     *
     * @param activePanelListener the ActivePanelListener to remove
     */
    public void removeActivePanelListener(ActivePanelListener activePanelListener) {
        activePanelListeners.remove(activePanelListener);
    }

    /**
     * Fires table change events on all registered ActivePanelListener instances.
     *
     * @param folderPanel the new active panel
     */
    private void fireActivePanelChanged(FolderPanel folderPanel) {
        activePanelListeners.keySet().forEach(listener -> listener.activePanelChanged(folderPanel));
    }


    /**
     * Returns <code>true</code> if 'no events mode' is currently enabled.
     *
     * @return <code>true</code> if 'no events mode' is currently enabled
     */
    public boolean getNoEventsMode() {
        return this.noEventsMode;
    }

    /**
     * Enables/disables the 'no events mode' which prevents mouse and keyboard events from being received
     * by the application (MainFrame, its subcomponents and the menu bar).
     *
     * @param enabled <code>true</code> to enable 'no events mode', <code>false</code> to disable it
     */
    public void setNoEventsMode(boolean enabled) {
        // Piece of code used in 0.8 beta1 and removed after because it's way too slow, kept here for the record 
        //  // Glass pane has empty mouse and key adapters (created in the constructor)
        //  // which will catch all mouse and keyboard events
        //  getGlassPane().setVisible(enabled);
        //  getJMenuBar().setEnabled(!enabled);
        //  // Remove focus from whatever component in FolderPanel which had focus
        //  getGlassPane().requestFocus();

        if (frameInstance.isDisplayable()) {
            this.noEventsMode = enabled;
        }
    }

    public JPanel getMainPanel() {
        return insetsPane;
    }

    /**
     * Returns the {@link ToolBar} where shortcut buttons (go back, go forward, ...) are.
     * Note that a non-null instance of {@link ToolBar} is returned even if it is currently hidden.
     *
     * @return the toolbar component
     */
    public ToolBar getToolBar() {
        return toolbar;
    }

    /**
     * Returns the panel where the {@link ToolBar} component is.
     * Note that a non-null instance of {@link ToolBar} is returned even if it is currently hidden.
     *
     * @return the toolbar component
     */
    public JPanel getToolBarPanel() {
        return toolbarPanel;
    }

    /**
     * Returns the {@link CommandBar}, i.e. the component that contains shortcuts to certain actions such as
     * View, Edit, Copy, Move, etc...
     * Note that a non-null instance of {@link CommandBar} is returned even if it is currently hidden.
     *
     * @return the command bar component
     */
    public CommandBar getCommandBar() {
        return commandBar;
    }


    /**
     * Returns the status bar, where information about selected files and volume are displayed.
     * Note that a non-null instance of {@link StatusBar} is returned even if it is currently hidden.
     *
     * @return the status bar
     */
    public StatusBar getStatusBar() {
        return this.statusBar;
    }


    /**
     * Returns the currently active table.
     *
     * <p>The returned table doesn't necessarily have focus, the focus can be in some other component
     * of the active {@link FolderPanel}, or nowhere in the MainFrame if it is currently not in the foreground.</p>
     *
     * <p>Use {@link FileTable#hasFocus()} to test if the table currently has focus.</p>
     *
     * @return the currently active table
     * @see FileTable#isActiveTable()
     */
    public FileTable getActiveTable() {
        return activeTable;
    }

    /**
     * Returns the currently active panel.
     *
     * <p>The returned panel doesn't necessarily have focus, for example if the MainFrame is currently not in the
     * foreground.</p>
     *
     * @return the currently active panel
     */
    public FolderPanel getActivePanel() {
        return activeTable.getFolderPanel();
    }

    /**
     * Sets the currently active FileTable. This method is to be called by FolderPanel only.
     *
     * @param table the currently active FileTable
     */
    void setActiveTable(FileTable table) {
        boolean activeTableChanged = activeTable != table;
        if (activeTableChanged) {
            this.activeTable = table;

            // Update window title to reflect new active table
            updateWindowTitle();

            // Fire table change events on registered ActivePanelListener instances.
            fireActivePanelChanged(table.getFolderPanel());
        }
    }


    /**
     * Returns the inactive table, i.e. the complement of {@link #getActiveTable()}.
     *
     * @return the inactive table
     */
    public FileTable getInactiveTable() {
        return activeTable == leftTable ? rightTable : leftTable;
    }
    
    /**
     * Returns the inactive panel, i.e. the complement of {@link #getActivePanel()}.
     *
     * @return the inactive panel
     */
    public FolderPanel getInactivePanel() {
        return getInactiveTable().getFolderPanel();
    }

    /**
     * Returns the FolderPanel instance corresponding to the left panel.
     *
     * @return the FolderPanel instance corresponding to the left panel
     */
    public FolderPanel getLeftPanel() {
        return leftFolderPanel;
    }

    /**
     * Returns the FolderPanel instance corresponding to the right panel.
     *
     * @return the FolderPanel instance corresponding to the right panel
     */
    public FolderPanel getRightPanel() {
        return rightFolderPanel;
    }


    /**
     * Returns the ProportionalSplitPane component that splits the two folder panels.
     *
     * @return the ProportionalSplitPane component that splits the two folder panels
     */
    public ProportionalSplitPane getFoldersSplitPane() {
        return foldersSplitPane;
    }

    /**
     * Toggles the Terminal, i.e., shows (maximized) or hides it (minimized)
     */
    public void toggleTerminal() {
        terminalIntegration.toggleTerminal();
    }

    /**
     * Specifies how folder panels are split: if true is passed, the folder panels will be split vertically
     * (default), horizontally otherwise.
     *
     * @param vertical if true, the folder panels will be split horizontally (default), vertically otherwise.
     */
    public void setSplitPaneOrientation(boolean vertical) {
        // Note: the vertical/horizontal terminology used in muCommander is just the opposite of the one used
        // in JSplitPane which is anti-natural / confusing
        foldersSplitPane.setOrientation(vertical?JSplitPane.HORIZONTAL_SPLIT:JSplitPane.VERTICAL_SPLIT);
    }

    /**
     * Returns how folder panels are currently split: if <code>true</code> is returned, panels are split vertically
     * (default), horizontally otherwise.
     *
     * @return <code>true</code> if folder panels are split vertically
     */
    public boolean getSplitPaneOrientation() {
        // Note: the vertical/horizontal terminology used in muCommander is just the opposite of the one used
        // in JSplitPane which is anti-natural / confusing
        return foldersSplitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT;
    }


    /**
     * Swaps the two FolderPanel instances: after a call to this method, the left FolderPanel will be the right one and
     * vice-versa.
     */
    public void swapFolders() {
        foldersSplitPane.remove(leftFolderPanel.getPanel());
        foldersSplitPane.remove(rightFolderPanel.getPanel());

        // Swaps the folder panels.
        FolderPanel tempPanel = leftFolderPanel;
        leftFolderPanel = rightFolderPanel;
        rightFolderPanel = tempPanel;

        // swaps folders trees
        int tempTreeWidth = leftFolderPanel.getTreeWidth();
        leftFolderPanel.setTreeWidth(rightFolderPanel.getTreeWidth());
        rightFolderPanel.setTreeWidth(tempTreeWidth);
        boolean tempTreeVisible = leftFolderPanel.isTreeVisible();
        leftFolderPanel.setTreeVisible(rightFolderPanel.isTreeVisible());
        rightFolderPanel.setTreeVisible(tempTreeVisible);
        

        // Resets the tables.
        FileTable tempTable = leftTable;
        leftTable = rightTable;
        rightTable = tempTable;

        // Preserve the sort order and columns visibility.
        TableColumnModel model = leftTable.getColumnModel();
        leftTable.setColumnModel(rightTable.getColumnModel());
        rightTable.setColumnModel(model);

        SortInfo sortInfo = (SortInfo)leftTable.getSortInfo().clone();

        leftTable.sortBy(rightTable.getSortInfo());
        leftTable.updateColumnsVisibility();

        rightTable.sortBy(sortInfo);
        rightTable.updateColumnsVisibility();

        // Do the swap and update the split pane
        foldersSplitPane.setLeftComponent(leftFolderPanel.getPanel());
        foldersSplitPane.setRightComponent(rightFolderPanel.getPanel());

        foldersSplitPane.doLayout();

        // Update split pane divider's location
        foldersSplitPane.updateDividerLocation();

        activeTable.requestFocus();
    }

    /**
     * Makes both folders the same, choosing the one which is currently active. 
     */
    public void setSameFolder() {
        getInactiveTable().getFolderPanel().tryChangeCurrentFolder(activeTable.getFolderPanel().getCurrentFolder());
    }

    /**
     * Returns <code>true</code> if this MainFrame is currently active in the foreground.
     *
     * @return <code>true</code> if this MainFrame is currently active in the foreground
     */
    public boolean isForegroundActive() {
        return foregroundActive;
    }

    /**
     * Sets whether this MainFrame is currently active in the foreground. This method is to be called by WindowManager
     * only.
     *
     * @param foregroundActive true if this MainFrame is currently active in the foreground
     */
    void setForegroundActive(boolean foregroundActive) {
        this.foregroundActive = foregroundActive;
    }

    /**
     * Forces a refresh of the frame's folder panel.
     */
    public void tryRefreshCurrentFolders() {
        leftFolderPanel.tryRefreshCurrentFolder();
        rightFolderPanel.tryRefreshCurrentFolder();
    }


    /**
     * Returns <code>true</code> if this MainFrame is active, or is an ancestor of a Window that is currently active.
     *
     * @return <code>true</code> if this MainFrame is active, or is an ancestor of a Window that is currently active
     */
    public boolean isAncestorOfActiveWindow() {
        if (getJFrame().isActive()) {
            return true;
        }

        Window ownedWindows[] = getJFrame().getOwnedWindows();

        int nbWindows = ownedWindows.length;
        for (int i = 0; i < nbWindows; i++) {
            if (ownedWindows[i].isActive()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Updates this window's title to show currently active folder and window number.
     * This method is called by this class and WindowManager.
     */
    public void updateWindowTitle() {
        // Update window title
        String title = activeTable.getFolderPanel().getCurrentFolder().getAbsolutePath();

        // Add the application name to window title on all OSs except MAC
        if (!OsFamily.MAC_OS.isCurrent()) {
            title += " - muCommander";
        }

        java.util.List<MainFrame> mainFrames = WindowManager.getMainFrames();
        if (mainFrames.size() > 1) {
            title += " [" + (mainFrames.indexOf(this) + 1) + "]";
        }
        getJFrame().setTitle(title);

        if (OsFamily.MAC_OS.isCurrent()) {
            // Displays the document icon in the window title bar, works only for local files
            AbstractFile currentFolder = activeTable.getFolderPanel().getCurrentFolder();
            Object javaIoFile;
            if (currentFolder.getURL().getScheme().equals(LocalFile.SCHEMA)) {
                // If the current folder is an archive entry, display the archive file, this is the closest we can get
                // with a java.io.File
                if (currentFolder.hasAncestor(AbstractArchiveEntryFile.class)) {
                    javaIoFile = currentFolder.getParentArchive().getUnderlyingFileObject();
                } else {
                    javaIoFile = currentFolder.getUnderlyingFileObject();
                }
            } else {
                // If the current folder is not a local file, use the special /Network directory which is sort of
                // 'Network Neighborhood'.
                javaIoFile = new java.io.File("/Network");
            }

            // Note that for some strange reason (looks like a bug), setting the property to null won't remove
            // the previous icon.
            getJFrame().getRootPane().putClientProperty("Window.documentFile", javaIoFile);
        }
    }

    /**
     * Returns <code>true</code> if only one panel is show
     *
     * @return <code>true</code> if only one panel is show
     */
    public boolean isSinglePanel() {
        return singlePanel;
    }

    /**
     * Toggles single panel view state and returns new one
     *
     * @return new state for singlePanel boolean
     */

    public boolean toggleSinglePanel() {
        singlePanel = !singlePanel;
        return singlePanel;
    }

    ///////////////////////
    // Overridden methods //
    ///////////////////////

    /**
     * Overrides <code>java.awt.Window#toFront</code> to have the window return to a normal state if it is minimized.
     */

    public void toFront() {
        if((getJFrame().getExtendedState()&Frame.ICONIFIED)!=0)
            getJFrame().setExtendedState(Frame.NORMAL);
        getJFrame().toFront();
    }

    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * Manages focus for both FolderPanel and their subcomponents.
     *
     * @author Maxence Bernard
     */
    protected class CustomFocusTraversalPolicy extends FocusTraversalPolicy {

        @Override
        public Component getComponentAfter(Container container, Component component) {
            if (component == leftFolderPanel.getFoldersTreePanel().getTree()) {
                return leftTable;
            }
            if (component == rightFolderPanel.getFoldersTreePanel().getTree()) {
                return rightTable;
            }
            if (component == leftFolderPanel.getLocationTextField()) {
                return leftTable;
            }
            if (component == leftTable) {
                return rightTable;
            }
            if (component== rightFolderPanel.getLocationTextField()) {
                return rightTable;
            }
            // otherwise (component==table2)
            return leftTable;
        }

        @Override
        public Component getComponentBefore(Container container, Component component) {
            // Completely symmetrical with getComponentAfter
            return getComponentAfter(container, component);
        }

        @Override
        public Component getFirstComponent(Container container) {
            return leftTable;
        }

        @Override
        public Component getLastComponent(Container container) {
            return rightTable;
        }

        @Override
        public Component getDefaultComponent(Container container) {
            return getActiveTable();
        }
    }

    public boolean isAutoSizeColumnsEnabled() {
        return leftTable.isAutoSizeColumnsEnabled();
    }

    public void setAutoSizeColumnsEnabled(boolean b) {
        leftTable.setAutoSizeColumnsEnabled(b);
        rightTable.setAutoSizeColumnsEnabled(b);
    }

    /**
     * Update the header renderer of both tables according to {@link FileTable#createHeaderRenderer()}}
     */
    public void updateFileTablesHeaderRenderer() {
        leftTable.updateHeaderRenderer();
        rightTable.updateHeaderRenderer();
    }
    
    /**********************************
     * LocationListener Implementation
     **********************************/

    public void locationChanged(LocationEvent e) {
        // Update window title to reflect the new current folder
        updateWindowTitle();
    }
}
