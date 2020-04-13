/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.ri.common;

import apple.dts.samplecode.osxadapter.OSXAdapter;
import org.icepdf.core.util.Defs;
import org.icepdf.ri.common.utility.annotation.AnnotationPanel;
import org.icepdf.ri.common.utility.attachment.AttachmentPanel;
import org.icepdf.ri.common.utility.layers.LayersPanel;
import org.icepdf.ri.common.utility.outline.OutlinesTree;
import org.icepdf.ri.common.utility.search.SearchPanel;
import org.icepdf.ri.common.utility.signatures.SignaturesPanel;
import org.icepdf.ri.common.utility.thumbs.ThumbnailsPanel;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.common.views.DocumentViewControllerImpl;
import org.icepdf.ri.images.Images;
import org.icepdf.ri.util.PropertiesManager;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>The purpose of this class is to facilitate in the building of user interface components
 * that are used to view and interact with PDF Documents.</p>
 * <p/>
 * <p>As such, there are three main scenarios that are covered.
 * <ol>
 * <li>Building a standalone Viewer JFrame that will behave much as a full-featured PDF viewing application.
 * <li>Building an embeddable PDF Viewer JPanel, which can easily be added into any existing client application,
 * augmenting its cababilities to include the capacity for viewing PDF Documents.
 * <li>Building a subset of the above, using the various building block methods in this class to construct GUI
 * components that provide certain aspects of the overall functionality, and adding those into
 * your application as desired.
 * </ol></p>
 * <p/>
 * <h2>Building a standalone window</h2>
 * <p>This is the main entry point for building a JFrame containing all of the graphical user interface
 * elements necessary for viewing PDF files.</p>
 * <p>The hierarchy of the methods that are invoked to construct the complete GUI
 * is provided below for your reference.
 * You may choose to use any of the individual methods to construct a sub-set of the complete
 * GUI that meets your specific requirements. This also provides you flexibility in the containers
 * that you add the components into and the overall layout of the application, etc.</p>
 * <br>
 * <b>public JFrame buildViewerFrame()</b>
 * <ul>
 * <li>public JMenuBar buildCompleteMenuBar()
 * <ul>
 * <li>public JMenu buildFileMenu()
 * <ul>
 * <li>public JMenuItem buildOpenFileMenuItem()</li>
 * <li>public JMenuItem buildOpenURLMenuItem()</li>
 * <li>public JMenuItem buildCloseMenuItem()</li>
 * <li>public JMenuItem buildSaveAsFileMenuItem()</li>
 * <li>public JMenuItem buildExportTextMenuItem()</li>
 * <li>public JMenuItem buildExportSVGMenuItem()</li>
 * <li>public JMenuItem buildPermissionsMenuItem()</li>
 * <li>public JMenuItem buildInformationMenuItem()</li>
 * <li>public JMenuItem buildPrintSetupMenuItem()</li>
 * <li>public JMenuItem buildPrintMenuItem()</li>
 * <li>public JMenuItem buildExitMenuItem()</li>
 * </ul>
 * </li>  <!-- buildFileMenu() -->
 * <p/>
 * <li>public JMenu buildViewMenu()
 * <ul>
 * <li>public JMenuItem buildFitActualSizeMenuItem()</li>
 * <li>public JMenuItem buildFitPageMenuItem()</li>
 * <li>public JMenuItem buildFitWidthMenuItem()</li>
 * <li>public JMenuItem buildZoomInMenuItem()</li>
 * <li>public JMenuItem buildZoomOutMenuItem()</li>
 * <li>public JMenuItem buildRotateLeftMenuItem()</li>
 * <li>public JMenuItem buildRotateRightMenuItem()</li>
 * <li>public JMenuItem buildShowHideToolBarMenuItem()</li>
 * <li>public JMenuItem buildShowHideUtilityPaneMenuItem()</li>
 * </ul>
 * </li>  <!-- buildViewMenu() -->
 * <p/>
 * <li>public JMenu buildDocumentMenu()
 * <ul>
 * <li>public JMenuItem buildFirstPageMenuItem()</li>
 * <li>public JMenuItem buildPreviousPageMenuItem()</li>
 * <li>public JMenuItem buildNextPageMenuItem()</li>
 * <li>public JMenuItem buildLastPageMenuItem()</li>
 * <li>public JMenuItem buildSearchMenuItem()</li>
 * <li>public JMenuItem buildGoToPageMenuItem()</li>
 * </ul>
 * </li>  <!-- buildDocumentMenu() -->
 * <p/>
 * <li>public JMenu buildWindowMenu()
 * <ul>
 * <li>public JMenuItem buildMinimiseAllMenuItem()</li>
 * <li>public JMenuItem buildBringAllToFrontMenuItem()</li>
 * <li>public void buildWindowListMenuItems(JMenu menu)</li>
 * </ul>
 * </li>  <!-- buildWindowMenu() -->
 * <p/>
 * <li>public JMenu buildHelpMenu()
 * <ul>
 * <li>public JMenuItem buildAboutMenuItem()</li>
 * </ul>
 * </li>  <!-- buildHelpMenu() -->
 * </ul>
 * </li>  <!-- buildCompleteMenuBar() -->
 * <p/>
 * <p/>
 * <li>public void buildContents(Container cp, boolean embeddableComponent)
 * <ul>
 * <p/>
 * <li>public JToolBar buildCompleteToolBar(boolean embeddableComponent)
 * <ul>
 * <li>public JToolBar buildUtilityToolBar(boolean embeddableComponent)
 * <ul>
 * <li>public JButton buildOpenFileButton()</li>
 * <li>public JButton buildSaveAsFileButton()</li>
 * <li>public JButton buildPrintButton()</li>
 * <li>public JButton buildSearchButton()</li>
 * <li>public JButton buildShowHideUtilityPaneButton()</li>
 * </ul>
 * </li>  <!-- buildUtilityToolBar(boolean embeddableComponent) -->
 * <p/>
 * <li>public JToolBar buildPageNavigationToolBar()
 * <ul>
 * <li>public JButton buildFirstPageButton()</li>
 * <li>public JButton buildPreviousPageButton()</li>
 * <li>public JButton buildNextPageButton()</li>
 * <li>public JButton buildLastPageButton()</li>
 * <li>public JTextField buildCurrentPageNumberTextField()</li>
 * <li>public JLabel buildNumberOfPagesLabel()</li>
 * </ul>
 * </li>  <!-- buildPageNavigationToolBar() -->
 * <p/>
 * <li>public JToolBar buildZoomToolBar()
 * <ul>
 * <li>public JButton buildZoomOutButton()</li>
 * <li>public JComboBox buildZoomCombBox()</li>
 * <li>public JButton buildZoomInButton()</li>
 * </ul>
 * </li>  <!-- buildZoomToolBar() -->
 * <p/>
 * <li>public JToolBar buildFitToolBar()
 * <ul>
 * <li>public JToggleButton buildFitActualSizeButton()</li>
 * <li>public JToggleButton buildFitPageButton()</li>
 * <li>public JToggleButton buildFitWidthButton()</li>
 * </ul>
 * </li>  <!-- buildFitToolBar() -->
 * <p/>
 * <li>public JToolBar buildRotateToolBar()
 * <ul>
 * <li>public JButton buildRotateLeftButton()</li>
 * <li>public JButton buildRotateRightButton()</li>
 * </ul>
 * </li>  <!-- buildRotateToolBar() -->
 * <p/>
 * <li>public JToolBar buildToolToolBar()
 * <ul>
 * <li>public JToggleButton buildPanToolButton()</li>
 * <li>public JToggleButton buildZoomInToolButton()</li>
 * <li>public JToggleButton buildZoomOutToolButton()</li>
 * </ul>
 * </li>  <!-- buildToolToolBar() -->
 * </ul>
 * </li>  <!-- buildCompleteToolBar(boolean embeddableComponent) -->
 * <p/>
 * <p/>
 * <li>public JSplitPane buildUtilityAndDocumentSplitPane(boolean embeddableComponent)
 * <ul>
 * <li>public JTabbedPane buildUtilityTabbedPane()
 * <ul>
 * <li>public JComponent buildOutlineComponents()</li>
 * <li>public SearchPanel buildSearchPanel()</li>
 * </ul>
 * </li>  <!-- buildUtilityTabbedPane() -->
 * <p/>
 * <li>public JScrollPane buildDocumentComponents(boolean embeddableComponent)</li>
 * </ul>
 * </li>  <!-- buildUtilityAndDocumentSplitPane(boolean embeddableComponent) -->
 * <p/>
 * <li>public JLabel buildStatusPanel()</li>
 * <p/>
 * </ul>
 * </li>  <!-- buildContents(Container cp, boolean embeddableComponent) -->
 * <p/>
 * </ul>
 * <p/>
 * <p/>
 * <h2>Building an embeddable component</h2>
 * <p>This is the main entry point for building a JPanel containing all of the GUI elements
 * necessary for viewing PDF files. The main differences between this and buildViewerFrame() are:
 * <ul>
 * <li>The buildViewerPanel method returns a JPanel which you may then embed anywhere into your GUI
 * <li>The JPanel will not contain a menu bar.
 * <li>The JPanel uses the sub-set of the GUI components available in buildViewerFrame that are
 * suited to an embedded component scenario.
 * </ul>
 * </p>
 * <p/>
 * <p>The following hierarchy of methods that are invoked to construct the complete Component GUI
 * is provided for your reference.
 * You may choose to use any of the individual methods below to construct a sub-set of the complete
 * GUI that meets your specific requirements. This also provides you flexibility in the containers
 * that you add the components into and the overall layout of the application, etc.</p>
 * <br>
 * <b>public JPanel buildViewerPanel()</b>
 * <ul>
 * <p/>
 * <li>public void buildContents(Container cp, boolean embeddableComponent)
 * <ul>
 * <p/>
 * <li>public JToolBar buildCompleteToolBar(boolean embeddableComponent)
 * <ul>
 * <li>public JToolBar buildUtilityToolBar(boolean embeddableComponent)
 * <ul>
 * <li>public JButton buildSaveAsFileButton()</li>
 * <li>public JButton buildPrintButton()</li>
 * <li>public JButton buildSearchButton()</li>
 * <li>public JButton buildShowHideUtilityPaneButton()</li>
 * </ul>
 * </li>  <!-- buildUtilityToolBar(boolean embeddableComponent) -->
 * <p/>
 * <li>public JToolBar buildPageNavigationToolBar()
 * <ul>
 * <li>public JButton buildFirstPageButton()</li>
 * <li>public JButton buildPreviousPageButton()</li>
 * <li>public JButton buildNextPageButton()</li>
 * <li>public JButton buildLastPageButton()</li>
 * <li>public JTextField buildCurrentPageNumberTextField()</li>
 * <li>public JLabel buildNumberOfPagesLabel()</li>
 * </ul>
 * </li>  <!-- buildPageNavigationToolBar() -->
 * <p/>
 * <li>public JToolBar buildZoomToolBar()
 * <ul>
 * <li>public JButton buildZoomOutButton()</li>
 * <li>public JComboBox buildZoomCombBox()</li>
 * <li>public JButton buildZoomInButton()</li>
 * </ul>
 * </li>  <!-- buildZoomToolBar() -->
 * <p/>
 * <li>public JToolBar buildFitToolBar()
 * <ul>
 * <li>public JToggleButton buildFitActualSizeButton()</li>
 * <li>public JToggleButton buildFitPageButton()</li>
 * <li>public JToggleButton buildFitWidthButton()</li>
 * </ul>
 * </li>  <!-- buildFitToolBar() -->
 * <p/>
 * <li>public JToolBar buildRotateToolBar()
 * <ul>
 * <li>public JButton buildRotateLeftButton()</li>
 * <li>public JButton buildRotateRightButton()</li>
 * </ul>
 * </li>  <!-- buildRotateToolBar() -->
 * <p/>
 * <li>public JToolBar buildToolToolBar()
 * <ul>
 * <li>public JToggleButton buildPanToolButton()</li>
 * <li>public JToggleButton buildZoomInToolButton()</li>
 * <li>public JToggleButton buildZoomOutToolButton()</li>
 * </ul>
 * </li>  <!-- buildToolToolBar() -->
 * </ul>
 * </li>  <!-- buildCompleteToolBar(boolean embeddableComponent) -->
 * <p/>
 * <p/>
 * <li>public JSplitPane buildUtilityAndDocumentSplitPane(boolean embeddableComponent)
 * <ul>
 * <li>public JTabbedPane buildUtilityTabbedPane()
 * <ul>
 * <li>public JComponent buildOutlineComponents()</li>
 * <li>public SearchPanel buildSearchPanel()</li>
 * </ul>
 * </li>  <!-- buildUtilityTabbedPane() -->
 * <p/>
 * <li>public JScrollPane buildDocumentComponents(boolean embeddableComponent)</li>
 * </ul>
 * </li>  <!-- buildUtilityAndDocumentSplitPane(boolean embeddableComponent) -->
 * <p/>
 * <p/>
 * <li>public JLabel buildStatusPanel()</li>
 * <p/>
 * <p/>
 * </ul>
 * </li>  <!-- buildContents(Container cp, boolean embeddableComponent) -->
 * <p/>
 * <p/>
 * <p/>
 * </ul>
 *
 * @author Mark Collette
 * @since 2.0
 */
public class SwingViewBuilder {

    private static final Logger logger =
            Logger.getLogger(SwingViewBuilder.class.toString());

    public static final int TOOL_BAR_STYLE_FLOATING = 1;
    public static final int TOOL_BAR_STYLE_FIXED = 2;
    protected static final float[] DEFAULT_ZOOM_LEVELS = {
            0.05f, 0.10f, 0.25f, 0.50f, 0.75f,
            1.0f, 1.5f, 2.0f, 3.0f,
            4.0f, 8.0f, 16.0f, 24.0f, 32.0f, 64.0f};

    protected SwingController viewerController;
    protected Font buttonFont;
    protected boolean showButtonText;
    protected int toolbarStyle;
    protected float[] zoomLevels;
    protected boolean haveMadeAToolBar;
    protected int documentViewType;
    protected int documentPageFitMode;
    protected ResourceBundle messageBundle;
    protected PropertiesManager propertiesManager;

    public static boolean isMacOs;

    private static boolean isDemo;

    static {
        isMacOs = (Defs.sysProperty("mrj.version") != null);
        // check for demo system property
        isDemo = Defs.sysPropertyBoolean("org.icepdf.ri.viewer.demo", false);
    }

    /**
     * Construct a SwingVewBuilder with all of the default settings
     *
     * @param c SwingController that will interact with the GUI
     */
    public SwingViewBuilder(SwingController c) {
        // Use all the defaults
        this(c, null, null, false, SwingViewBuilder.TOOL_BAR_STYLE_FIXED, null,
                DocumentViewControllerImpl.ONE_PAGE_VIEW,
                DocumentViewController.PAGE_FIT_WINDOW_HEIGHT);
    }

    /**
     * Constructor that accepts a different PropertiesManager and otherwise
     * defaults the remaining settings
     *
     * @param c          SwingController that will interact with the GUI
     * @param properties PropertiesManager that can customize the UI
     */
    public SwingViewBuilder(SwingController c, PropertiesManager properties) {
        this(c, properties, null, false, SwingViewBuilder.TOOL_BAR_STYLE_FIXED, null,
                DocumentViewControllerImpl.ONE_PAGE_VIEW,
                DocumentViewController.PAGE_FIT_WINDOW_HEIGHT);
    }

    /**
     * Construct a SwingVewBuilder with all of the default settings
     *
     * @param c                   SwingController that will interact with the GUI
     * @param documentViewType    view type to build , single page, single column etc.
     * @param documentPageFitMode fit mode to initially load document with.
     */
    public SwingViewBuilder(SwingController c, int documentViewType,
                            int documentPageFitMode) {
        // Use all the defaults
        this(c, null, null, false, SwingViewBuilder.TOOL_BAR_STYLE_FIXED,
                null, documentViewType, documentPageFitMode);
    }

    /**
     * Construct a SwingVewBuilder with whichever settings you desire
     *
     * @param c SwingController that will interact with the GUI
     */
    public SwingViewBuilder(SwingController c, Font bf, boolean bt, int ts,
                            float[] zl, final int documentViewType,
                            final int documentPageFitMode) {
        this(c, null, bf, bt, ts, zl, documentViewType, documentPageFitMode);
    }

    /**
     * Construct a SwingVewBuilder with whichever settings you desire
     *
     * @param c SwingController that will interact with the GUI
     */
    public SwingViewBuilder(SwingController c, PropertiesManager properties,
                            Font bf, boolean bt, int ts,
                            float[] zl, final int documentViewType,
                            final int documentPageFitMode) {
        viewerController = c;

        messageBundle = viewerController.getMessageBundle();

        if (properties != null) {
            viewerController.setPropertiesManager(properties);
            this.propertiesManager = properties;
        }

        // Attempt to override the highlight color from the properties file
        overrideHighlightColor();

        // update View Controller with previewer document page fit and view type info
        DocumentViewControllerImpl documentViewController = (DocumentViewControllerImpl) viewerController.getDocumentViewController();
        documentViewController.setDocumentViewType(documentViewType, documentPageFitMode);

        buttonFont = bf;
        if (buttonFont == null)
            buttonFont = buildButtonFont();
        showButtonText = bt;
        toolbarStyle = ts;
        zoomLevels = zl;
        if (zoomLevels == null)
            zoomLevels = DEFAULT_ZOOM_LEVELS;
        // set default doc view type, single page, facing page, etc.
        this.documentViewType = documentViewType;
        // set default view mode type, fit page, fit width, no-fit.
        this.documentPageFitMode = documentPageFitMode;
    }

    /**
     * This is a standard method for creating a standalone JFrame, that would
     * behave as a fully functional PDF Viewer application.
     *
     * @return a JFrame containing the PDF document's current page visualization,
     * menu bar, accelerator buttons, and document outline if available.
     * @see #buildViewerPanel
     */
    public JFrame buildViewerFrame() {
        JFrame viewer = new JFrame();
        viewer.setIconImage(new ImageIcon(Images.get("icepdf-app-icon-64x64.png")).getImage());
        viewer.setTitle(messageBundle.getString("viewer.window.title.default"));
        viewer.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        JMenuBar menuBar = buildCompleteMenuBar();
        if (menuBar != null)
            viewer.setJMenuBar(menuBar);
        Container contentPane = viewer.getContentPane();
        buildContents(contentPane, false);
        if (viewerController != null)
            viewerController.setViewerFrame(viewer);
        return viewer;
    }

    /**
     * Used by the Applet and Pilot RI code to create an embeddable panel
     * for viewing PDF files, as opposed to buildViewerFrame(), which makes a
     * standalone JFrame
     *
     * @return JPanel containing the PDF document's current page visualization,
     * menu bar, accelerator buttons, and document outline if available.
     * @see #buildViewerFrame
     */
    public JPanel buildViewerPanel() {
        JPanel panel = new JPanel();
        buildContents(panel, true);
        return panel;
    }

    /**
     * The Container will contain the PDF document's current page visualization
     * and document outline if available.
     *
     * @param cp Container in which to put components for viewing PDF documents
     */
    public void buildContents(Container cp, boolean embeddableComponent) {
        cp.setLayout(new BorderLayout());
        JToolBar toolBar = buildCompleteToolBar(embeddableComponent);
        if (toolBar != null)
            cp.add(toolBar, BorderLayout.NORTH);
        // Builds the utility pane as well as the main document View, important
        // code entry point.
        JSplitPane utilAndDocSplit =
                buildUtilityAndDocumentSplitPane(embeddableComponent);
        if (utilAndDocSplit != null)
            cp.add(utilAndDocSplit, BorderLayout.CENTER);
        JPanel statusPanel = buildStatusPanel();
        if (statusPanel != null)
            cp.add(statusPanel, BorderLayout.SOUTH);
    }


    public JMenuBar buildCompleteMenuBar() {

        JMenuBar menuBar = new JMenuBar();
        addToMenuBar(menuBar, buildFileMenu());
        addToMenuBar(menuBar, buildEditMenu());
        addToMenuBar(menuBar, buildViewMenu());
        addToMenuBar(menuBar, buildDocumentMenu());
        addToMenuBar(menuBar, buildWindowMenu());
        addToMenuBar(menuBar, buildHelpMenu());

        // If running on MacOS, setup the native app. menu item handlers
        if (isMacOs) {
            try {
                // Generate and register the OSXAdapter, passing it a hash of all the methods we wish to
                // use as delegates for various com.apple.eawt.ApplicationListener methods
                OSXAdapter.setQuitHandler(viewerController, viewerController.getClass().getDeclaredMethod("exit", (Class[]) null));
                OSXAdapter.setAboutHandler(viewerController, viewerController.getClass().getDeclaredMethod("showAboutDialog", (Class[]) null));
            } catch (Exception e) {
                logger.log(Level.FINE, "Error occurred while loading the OSXAdapter:", e);
            }
        }

        return menuBar;
    }

    protected KeyStroke buildKeyStroke(int keyCode, int modifiers) {
        return buildKeyStroke(keyCode, modifiers, false);
    }

    /**
     * Create and return a KeyStroke with the specified code and modifier
     * Note this will automatically return null if the PROPERTY_SHOW_KEYBOARD_SHORTCUTS
     * property is 'false'
     *
     * @param keyCode   to build
     * @param modifiers to build
     * @param onRelease to build
     * @return built KeyStroke
     */
    protected KeyStroke buildKeyStroke(int keyCode, int modifiers, boolean onRelease) {
        doubleCheckPropertiesManager();

        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_KEYBOARD_SHORTCUTS,
                true)) {
            return KeyStroke.getKeyStroke(keyCode, modifiers, onRelease);
        }

        return null;
    }

    /**
     * Return a valid mnemonic for the passed character, unless the
     * PropertiesManager.PROPERTY_SHOW_KEYBOARD_SHORTCUTS property is 'false',
     * in which case we'll return -1
     *
     * @param mnemonic to build
     * @return built mnemonic
     */
    protected int buildMnemonic(char mnemonic) {
        doubleCheckPropertiesManager();

        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_KEYBOARD_SHORTCUTS,
                true)) {
            return mnemonic;
        }

        return -1;
    }

    public JMenu buildFileMenu() {
        JMenu fileMenu = new JMenu(messageBundle.getString("viewer.menu.file.label"));
        fileMenu.setMnemonic(buildMnemonic(messageBundle.getString("viewer.menu.file.mnemonic").charAt(0)));
        JMenuItem openFileMenuItem = buildOpenFileMenuItem();
        JMenuItem openURLMenuItem = buildOpenURLMenuItem();
        if (openFileMenuItem != null && openURLMenuItem != null) {
            JMenu openSubMenu = new JMenu(messageBundle.getString("viewer.menu.open.label"));
            openSubMenu.setIcon(new ImageIcon(Images.get("open_a_24.png")));
            openSubMenu.setDisabledIcon(new ImageIcon(Images.get("open_i_24.png")));
            openSubMenu.setRolloverIcon(new ImageIcon(Images.get("open_r_24.png")));
            addToMenu(openSubMenu, openFileMenuItem);
            addToMenu(openSubMenu, openURLMenuItem);
            addToMenu(fileMenu, openSubMenu);
        } else if (openFileMenuItem != null || openURLMenuItem != null) {
            addToMenu(fileMenu, openFileMenuItem);
            addToMenu(fileMenu, openURLMenuItem);
        }
        fileMenu.addSeparator();
        addToMenu(fileMenu, buildCloseMenuItem());
        addToMenu(fileMenu, buildSaveAsFileMenuItem());
        addToMenu(fileMenu, buildExportTextMenuItem());
        addToMenu(fileMenu, buildExportSVGMenuItem());
        fileMenu.addSeparator();
        addToMenu(fileMenu, buildPermissionsMenuItem());
        addToMenu(fileMenu, buildInformationMenuItem());
        addToMenu(fileMenu, buildFontInformationMenuItem());
        fileMenu.addSeparator();
        addToMenu(fileMenu, buildPrintSetupMenuItem());
        addToMenu(fileMenu, buildPrintMenuItem());
        if (!isMacOs) {
            // Not on a Mac, so create the Exit menu item.
            fileMenu.addSeparator();
            addToMenu(fileMenu, buildExitMenuItem());
        }
        return fileMenu;
    }

    public JMenuItem buildOpenFileMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.open.file.label"),
                buildKeyStroke(KeyEventConstants.KEY_CODE_OPEN_FILE, KeyEventConstants.MODIFIER_OPEN_FILE));
        if (viewerController != null && mi != null)
            viewerController.setOpenFileMenuItem(mi);
        return mi;
    }

    public JMenuItem buildOpenURLMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.open.URL.label"),
                buildKeyStroke(KeyEventConstants.KEY_CODE_OPEN_URL, KeyEventConstants.MODIFIER_OPEN_URL));
        if (viewerController != null && mi != null)
            viewerController.setOpenURLMenuItem(mi);
        return mi;
    }

    public JMenuItem buildCloseMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.close.label"), null, null,
                buildKeyStroke(KeyEventConstants.KEY_CODE_CLOSE, KeyEventConstants.MODIFIER_CLOSE));
        if (viewerController != null && mi != null)
            viewerController.setCloseMenuItem(mi);
        return mi;
    }

    public JMenuItem buildSaveAsFileMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.saveAs.label"), "save",
                Images.SIZE_MEDIUM,
                buildKeyStroke(KeyEventConstants.KEY_CODE_SAVE_AS, KeyEventConstants.MODIFIER_SAVE_AS, false));
        if (viewerController != null && mi != null)
            viewerController.setSaveAsFileMenuItem(mi);
        return mi;
    }

    public JMenuItem buildExportTextMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.exportText.label"), null, null, null);
        if (viewerController != null && mi != null)
            viewerController.setExportTextMenuItem(mi);
        return mi;
    }

    public JMenuItem buildExportSVGMenuItem() {
        JMenuItem mi = null;
        // Check to make sure SVG libraries are available
        try {
            Class.forName("org.apache.batik.dom.GenericDOMImplementation");

            mi = makeMenuItem(
                    messageBundle.getString("viewer.menu.exportSVG.label"), null, null, null);
            if (viewerController != null && mi != null)
                viewerController.setExportSVGMenuItem(mi);
        } catch (ClassNotFoundException e) {
            logger.warning("SVG Support Not Found");
        }
        return mi;
    }

    public JMenuItem buildPermissionsMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.documentPermission.label"), null, null, null);
        if (viewerController != null && mi != null)
            viewerController.setPermissionsMenuItem(mi);
        return mi;
    }

    public JMenuItem buildInformationMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.documentInformation.label"), null, null, null);
        if (viewerController != null && mi != null)
            viewerController.setInformationMenuItem(mi);
        return mi;
    }

    public JMenuItem buildFontInformationMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.documentFonts.label"), null, null, null);
        if (viewerController != null && mi != null)
            viewerController.setFontInformationMenuItem(mi);
        return mi;
    }

    public JMenuItem buildPrintSetupMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.printSetup.label"), null, null,
                buildKeyStroke(KeyEventConstants.KEY_CODE_PRINT_SETUP, KeyEventConstants.MODIFIER_PRINT_SETUP, false));
        if (viewerController != null && mi != null)
            viewerController.setPrintSetupMenuItem(mi);
        return mi;
    }

    public JMenuItem buildPrintMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.print.label"), "print",
                Images.SIZE_MEDIUM,
                buildKeyStroke(KeyEventConstants.KEY_CODE_PRINT, KeyEventConstants.MODIFIER_PRINT));
        if (viewerController != null && mi != null)
            viewerController.setPrintMenuItem(mi);
        return mi;
    }

    public JMenuItem buildExitMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.exit.label"), null, null,
                buildKeyStroke(KeyEventConstants.KEY_CODE_EXIT, KeyEventConstants.MODIFIER_EXIT));
        if (viewerController != null && mi != null)
            viewerController.setExitMenuItem(mi);
        return mi;
    }

    public JMenu buildEditMenu() {
        JMenu viewMenu = new JMenu(messageBundle.getString("viewer.menu.edit.label"));
        viewMenu.setMnemonic(buildMnemonic(messageBundle.getString("viewer.menu.edit.mnemonic").charAt(0)));
        addToMenu(viewMenu, buildUndoMenuItem());
        addToMenu(viewMenu, buildRedoMenuItem());
        viewMenu.addSeparator();
        addToMenu(viewMenu, buildCopyMenuItem());
        addToMenu(viewMenu, buildDeleteMenuItem());
        viewMenu.addSeparator();
        addToMenu(viewMenu, buildSelectAllMenuItem());
        addToMenu(viewMenu, buildDeselectAllMenuItem());
        return viewMenu;
    }

    public JMenuItem buildUndoMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.edit.undo.label"),
                null, null, buildKeyStroke(KeyEventConstants.KEY_CODE_UNDO,
                        KeyEventConstants.MODIFIER_UNDO));
        if (viewerController != null && mi != null)
            viewerController.setUndoMenuItem(mi);
        return mi;
    }

    public JMenuItem buildRedoMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.edit.redo.label"),
                null, null, buildKeyStroke(KeyEventConstants.KEY_CODE_REDO,
                        KeyEventConstants.MODIFIER_REDO));
        if (viewerController != null && mi != null)
            viewerController.setReduMenuItem(mi);
        return mi;
    }

    public JMenuItem buildCopyMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.edit.copy.label"),
                null, null, buildKeyStroke(KeyEventConstants.KEY_CODE_COPY,
                        KeyEventConstants.MODIFIER_COPY));
        if (viewerController != null && mi != null)
            viewerController.setCopyMenuItem(mi);
        return mi;
    }

    public JMenuItem buildDeleteMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.edit.delete.label"),
                null, null, buildKeyStroke(KeyEventConstants.KEY_CODE_DELETE,
                        KeyEventConstants.MODIFIER_DELETE));
        if (viewerController != null && mi != null)
            viewerController.setDeleteMenuItem(mi);
        return mi;
    }

    public JMenuItem buildSelectAllMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.edit.selectAll.label"),
                null, null, buildKeyStroke(KeyEventConstants.KEY_CODE_SELECT_ALL,
                        KeyEventConstants.MODIFIER_SELECT_ALL));
        if (viewerController != null && mi != null)
            viewerController.setSelectAllMenuItem(mi);
        return mi;
    }

    public JMenuItem buildDeselectAllMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.edit.deselectAll.label"),
                null, null, buildKeyStroke(KeyEventConstants.KEY_CODE_DESELECT_ALL,
                        KeyEventConstants.MODIFIER_DESELECT_ALL));
        if (viewerController != null && mi != null)
            viewerController.setDselectAllMenuItem(mi);
        return mi;
    }

    public JMenu buildViewMenu() {
        JMenu viewMenu = new JMenu(messageBundle.getString("viewer.menu.view.label"));
        viewMenu.setMnemonic(buildMnemonic(messageBundle.getString("viewer.menu.view.mnemonic").charAt(0)));
        addToMenu(viewMenu, buildFitActualSizeMenuItem());
        addToMenu(viewMenu, buildFitPageMenuItem());
        addToMenu(viewMenu, buildFitWidthMenuItem());
        viewMenu.addSeparator();
        addToMenu(viewMenu, buildZoomInMenuItem());
        addToMenu(viewMenu, buildZoomOutMenuItem());
        viewMenu.addSeparator();
        addToMenu(viewMenu, buildRotateLeftMenuItem());
        addToMenu(viewMenu, buildRotateRightMenuItem());
        viewMenu.addSeparator();
        addToMenu(viewMenu, buildShowHideToolBarMenuItem());
        addToMenu(viewMenu, buildShowHideUtilityPaneMenuItem());
        return viewMenu;
    }

    public JMenuItem buildFitActualSizeMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.view.actualSize.label"),
                "actual_size", Images.SIZE_MEDIUM,
                buildKeyStroke(KeyEventConstants.KEY_CODE_FIT_ACTUAL, KeyEventConstants.MODIFIER_FIT_ACTUAL));
        if (viewerController != null && mi != null)
            viewerController.setFitActualSizeMenuItem(mi);
        return mi;
    }

    public JMenuItem buildFitPageMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.view.fitInWindow.label"),
                "fit_window", Images.SIZE_MEDIUM,
                buildKeyStroke(KeyEventConstants.KEY_CODE_FIT_PAGE, KeyEventConstants.MODIFIER_FIT_PAGE));
        if (viewerController != null && mi != null)
            viewerController.setFitPageMenuItem(mi);
        return mi;
    }

    public JMenuItem buildFitWidthMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.view.fitWidth.label"),
                null, null,
                buildKeyStroke(KeyEventConstants.KEY_CODE_FIT_WIDTH, KeyEventConstants.MODIFIER_FIT_WIDTH));
        if (viewerController != null && mi != null)
            viewerController.setFitWidthMenuItem(mi);
        return mi;
    }

    public JMenuItem buildZoomInMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.view.zoomIn.label"),
                "zoom_in", Images.SIZE_MEDIUM,
                buildKeyStroke(KeyEventConstants.KEY_CODE_ZOOM_IN, KeyEventConstants.MODIFIER_ZOOM_IN, false));
        if (viewerController != null && mi != null)
            viewerController.setZoomInMenuItem(mi);
        return mi;
    }

    public JMenuItem buildZoomOutMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.view.zoomOut.label"),
                "zoom_out", Images.SIZE_MEDIUM,
                buildKeyStroke(KeyEventConstants.KEY_CODE_ZOOM_OUT, KeyEventConstants.MODIFIER_ZOOM_OUT, false));
        if (viewerController != null && mi != null)
            viewerController.setZoomOutMenuItem(mi);
        return mi;
    }

    public JMenuItem buildRotateLeftMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.view.rotateLeft.label"),
                "rotate_left", Images.SIZE_MEDIUM,
                buildKeyStroke(KeyEventConstants.KEY_CODE_ROTATE_LEFT, KeyEventConstants.MODIFIER_ROTATE_LEFT));
        if (viewerController != null && mi != null)
            viewerController.setRotateLeftMenuItem(mi);
        return mi;
    }

    public JMenuItem buildRotateRightMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.view.rotateRight.label"),
                "rotate_right", Images.SIZE_MEDIUM,
                buildKeyStroke(KeyEventConstants.KEY_CODE_ROTATE_RIGHT, KeyEventConstants.MODIFIER_ROTATE_RIGHT));
        if (viewerController != null && mi != null)
            viewerController.setRotateRightMenuItem(mi);
        return mi;
    }

    public JMenuItem buildShowHideToolBarMenuItem() {
        JMenuItem mi = makeMenuItem("", null);
        if (viewerController != null && mi != null)
            viewerController.setShowHideToolBarMenuItem(mi);
        return mi;
    }

    public JMenuItem buildShowHideUtilityPaneMenuItem() {
        JMenuItem mi = makeMenuItem("", null);
        if (viewerController != null && mi != null)
            viewerController.setShowHideUtilityPaneMenuItem(mi);
        return mi;
    }

    public JMenu buildDocumentMenu() {
        JMenu documentMenu = new JMenu(messageBundle.getString("viewer.menu.document.label"));
        documentMenu.setMnemonic(buildMnemonic(messageBundle.getString("viewer.menu.document.mnemonic").charAt(0)));
        addToMenu(documentMenu, buildFirstPageMenuItem());
        addToMenu(documentMenu, buildPreviousPageMenuItem());
        addToMenu(documentMenu, buildNextPageMenuItem());
        addToMenu(documentMenu, buildLastPageMenuItem());
        documentMenu.addSeparator();
        addToMenu(documentMenu, buildSearchMenuItem());
        addToMenu(documentMenu, buildGoToPageMenuItem());
        return documentMenu;
    }

    public JMenuItem buildFirstPageMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.document.firstPage.label"),
                "page_first", Images.SIZE_MEDIUM,
                buildKeyStroke(KeyEventConstants.KEY_CODE_FIRST_PAGE, KeyEventConstants.MODIFIER_FIRST_PAGE));
        if (viewerController != null && mi != null)
            viewerController.setFirstPageMenuItem(mi);
        return mi;
    }

    public JMenuItem buildPreviousPageMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.document.previousPage.label"),
                "page_up", Images.SIZE_MEDIUM,
                buildKeyStroke(KeyEventConstants.KEY_CODE_PREVIOUS_PAGE, KeyEventConstants.MODIFIER_PREVIOUS_PAGE));
        if (viewerController != null && mi != null)
            viewerController.setPreviousPageMenuItem(mi);
        return mi;
    }

    public JMenuItem buildNextPageMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.document.nextPage.label"),
                "page_down", Images.SIZE_MEDIUM,
                buildKeyStroke(KeyEventConstants.KEY_CODE_NEXT_PAGE, KeyEventConstants.MODIFIER_NEXT_PAGE));
        if (viewerController != null && mi != null)
            viewerController.setNextPageMenuItem(mi);
        return mi;
    }

    public JMenuItem buildLastPageMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.document.lastPage.label"),
                "page_last", Images.SIZE_MEDIUM,
                buildKeyStroke(KeyEventConstants.KEY_CODE_LAST_PAGE, KeyEventConstants.MODIFIER_LAST_PAGE));
        if (viewerController != null && mi != null)
            viewerController.setLastPageMenuItem(mi);
        return mi;
    }

    public JMenuItem buildSearchMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.document.search.label"),
                buildKeyStroke(KeyEventConstants.KEY_CODE_SEARCH, KeyEventConstants.MODIFIER_SEARCH));
        if (viewerController != null && mi != null)
            viewerController.setSearchMenuItem(mi);
        return mi;
    }

    public JMenuItem buildGoToPageMenuItem() {
        JMenuItem mi = makeMenuItem(
                messageBundle.getString("viewer.menu.document.gotToPage.label"),
                buildKeyStroke(KeyEventConstants.KEY_CODE_GOTO, KeyEventConstants.MODIFIER_GOTO));
        if (viewerController != null && mi != null)
            viewerController.setGoToPageMenuItem(mi);
        return mi;
    }

    public JMenu buildWindowMenu() {
        final JMenu windowMenu = new JMenu(messageBundle.getString("viewer.menu.window.label"));
        windowMenu.setMnemonic(buildMnemonic(messageBundle.getString("viewer.menu.window.mnemonic").charAt(0)));
        addToMenu(windowMenu, buildMinimiseAllMenuItem());
        addToMenu(windowMenu, buildBringAllToFrontMenuItem());
        windowMenu.addSeparator();
        final int allowedCount = windowMenu.getItemCount();
        windowMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent e) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent e) {
            }

            public void menuSelected(javax.swing.event.MenuEvent e) {
                int count = windowMenu.getItemCount();
                while (count > allowedCount) {
                    windowMenu.remove(count - 1);
                    count--;
                }
                buildWindowListMenuItems(windowMenu);
            }
        });
        return windowMenu;
    }

    public JMenuItem buildMinimiseAllMenuItem() {
        JMenuItem mi = makeMenuItem(messageBundle.getString("viewer.menu.window.minAll.label"), null);
        mi.setMnemonic(buildMnemonic(messageBundle.getString("viewer.menu.window.minAll.mnemonic").charAt(0)));
        if (viewerController != null)
            viewerController.setMinimiseAllMenuItem(mi);
        return mi;
    }

    public JMenuItem buildBringAllToFrontMenuItem() {
        JMenuItem mi = makeMenuItem(messageBundle.getString("viewer.menu.window.frontAll.label"), null);
        mi.setMnemonic(buildMnemonic(messageBundle.getString("viewer.menu.window.frontAll.mnemonic").charAt(0)));
        if (viewerController != null)
            viewerController.setBringAllToFrontMenuItem(mi);
        return mi;
    }

    @SuppressWarnings("unchecked")
    public void buildWindowListMenuItems(JMenu menu) {
        if (viewerController != null &&
                viewerController.getWindowManagementCallback() != null) {
            WindowManagementCallback winMgr = viewerController.getWindowManagementCallback();
            List<Object> windowDocOriginList = (List<Object>) winMgr.getWindowDocumentOriginList(viewerController);

            // Get the current window index, if it's given, and remove it from the list
            int currWindowIndex = -1;
            int count = windowDocOriginList.size();
            if (count > 0 && windowDocOriginList.get(count - 1) instanceof Integer) {
                currWindowIndex = (Integer) windowDocOriginList.remove(--count);
            }

            shortenDocumentOrigins(windowDocOriginList);

            List<JMenuItem> windowListMenuItems =
                    new ArrayList<JMenuItem>(Math.max(count, 1));
            for (int i = 0; i < count; i++) {
                String number = Integer.toString(i + 1);
                String label = null;
                String mnemonic = null;
                try {
                    label = messageBundle.getString("viewer.menu.window." + number + ".label");
                    mnemonic = messageBundle.getString("viewer.menu.window." + number + ".mnemonic");
                } catch (Exception e) {
                    logger.log(Level.FINER,
                            "Error setting viewer window window title", e);
                }
                // Allows the user to have an arbitrary number of predefined entries
                String identifier = (String) windowDocOriginList.get(i);
                if (identifier == null)
                    identifier = "";
                String miText;
                if (label != null && label.length() > 0)
                    miText = number + "  " + identifier;
                else
                    miText = "    " + identifier;
                JMenuItem mi = new JMenuItem(miText);
                if (mnemonic != null && number.length() == 1)
                    mi.setMnemonic(buildMnemonic(number.charAt(0)));
                if (currWindowIndex == i)
                    mi.setEnabled(false);
                menu.add(mi);
                windowListMenuItems.add(mi);
            }
            viewerController.setWindowListMenuItems(windowListMenuItems);
        }
    }

    protected void shortenDocumentOrigins(List<Object> windowDocOriginList) {
        // At some point we should detect the same filename
        //   in different subdirectories, and keep some of the
        //   directory information, to help differentiate them
        for (int i = windowDocOriginList.size() - 1; i >= 0; i--) {
            String identifier = (String) windowDocOriginList.get(i);
            if (identifier == null)
                continue;
            int separatorIndex = identifier.lastIndexOf(java.io.File.separator);
            int forewardSlashIndex = identifier.lastIndexOf("/");
            int backwardSlashIndex = identifier.lastIndexOf("\\");
            int cutIndex = Math.max(separatorIndex, Math.max(forewardSlashIndex, backwardSlashIndex));
            if (cutIndex >= 0) {
                identifier = identifier.substring(cutIndex);
                windowDocOriginList.set(i, identifier);
            }
        }
    }

    public JMenu buildHelpMenu() {
        JMenu helpMenu = new JMenu(messageBundle.getString("viewer.menu.help.label"));
        helpMenu.setMnemonic(buildMnemonic(messageBundle.getString("viewer.menu.help.mnemonic").charAt(0)));

        if (!isMacOs) {
            // Not on a Mac, so create the About menu item.
            addToMenu(helpMenu, buildAboutMenuItem());
        }
        return helpMenu;
    }

    public JMenuItem buildAboutMenuItem() {

        JMenuItem mi = makeMenuItem(messageBundle.getString("viewer.menu.help.about.label"), null);
        if (viewerController != null && mi != null)
            viewerController.setAboutMenuItem(mi);
        return mi;
    }


    public JToolBar buildCompleteToolBar(boolean embeddableComponent) {
        JToolBar toolbar = new JToolBar();
        toolbar.setLayout(new ToolbarLayout(ToolbarLayout.LEFT, 0, 0));
        commonToolBarSetup(toolbar, true);

        // Attempt to get the properties manager so we can configure which toolbars are visible
        doubleCheckPropertiesManager();

        // Build the main set of toolbars based on the property file configuration
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager, PropertiesManager.PROPERTY_SHOW_TOOLBAR_UTILITY))
            addToToolBar(toolbar, buildUtilityToolBar(embeddableComponent, propertiesManager));
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager, PropertiesManager.PROPERTY_SHOW_TOOLBAR_PAGENAV))
            addToToolBar(toolbar, buildPageNavigationToolBar());
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager, PropertiesManager.PROPERTY_SHOW_TOOLBAR_ZOOM))
            addToToolBar(toolbar, buildZoomToolBar());
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager, PropertiesManager.PROPERTY_SHOW_TOOLBAR_FIT))
            addToToolBar(toolbar, buildFitToolBar());
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager, PropertiesManager.PROPERTY_SHOW_TOOLBAR_ROTATE))
            addToToolBar(toolbar, buildRotateToolBar());
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager, PropertiesManager.PROPERTY_SHOW_TOOLBAR_TOOL))
            addToToolBar(toolbar, buildToolToolBar());
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager, PropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION))
            addToToolBar(toolbar, buildAnnotationlToolBar());
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager, PropertiesManager.PROPERTY_SHOW_TOOLBAR_FORMS))
            addToToolBar(toolbar, buildFormsToolBar());

        // we only add the configurable font engin in the demo version
        if (isDemo) {
            addToToolBar(toolbar, buildDemoToolBar());
        }

        // Set the toolbar back to null if no components were added
        // The result of this will properly disable the necessary menu items for controlling the toolbar
        if (toolbar.getComponentCount() == 0) {
            toolbar = null;
        }

        if ((viewerController != null) && (toolbar != null))
            viewerController.setCompleteToolBar(toolbar);

        return toolbar;
    }

    public JToolBar buildUtilityToolBar(boolean embeddableComponent) {
        return buildUtilityToolBar(embeddableComponent, null);
    }

    public JToolBar buildUtilityToolBar(boolean embeddableComponent, PropertiesManager propertiesManager) {
        JToolBar toolbar = new JToolBar();
        commonToolBarSetup(toolbar, false);
        // if embeddable component, we don't want to create the open dialog, as we
        // have no window manager for this case.
        if ((!embeddableComponent) &&
                (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager, PropertiesManager.PROPERTY_SHOW_UTILITY_OPEN)))
            addToToolBar(toolbar, buildOpenFileButton());
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager, PropertiesManager.PROPERTY_SHOW_UTILITY_SAVE))
            addToToolBar(toolbar, buildSaveAsFileButton());
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager, PropertiesManager.PROPERTY_SHOW_UTILITY_PRINT))
            addToToolBar(toolbar, buildPrintButton());
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager, PropertiesManager.PROPERTY_SHOW_UTILITY_SEARCH))
            addToToolBar(toolbar, buildSearchButton());
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager, PropertiesManager.PROPERTY_SHOW_UTILITY_UPANE))
            addToToolBar(toolbar, buildShowHideUtilityPaneButton());

        // Don't bother with this toolbar if we don't have any visible buttons
        if (toolbar.getComponentCount() == 0) {
            return null;
        }

        return toolbar;
    }

    public JButton buildOpenFileButton() {
        JButton btn = makeToolbarButton(
                messageBundle.getString("viewer.toolbar.open.label"),
                messageBundle.getString("viewer.toolbar.open.tooltip"),
                "open", Images.SIZE_LARGE, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setOpenFileButton(btn);
        return btn;
    }

    public JButton buildSaveAsFileButton() {
        JButton btn = makeToolbarButton(
                messageBundle.getString("viewer.toolbar.saveAs.label"),
                messageBundle.getString("viewer.toolbar.saveAs.tooltip"),
                "save", Images.SIZE_LARGE,
                buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setSaveAsFileButton(btn);
        return btn;
    }

    public JButton buildPrintButton() {
        JButton btn = makeToolbarButton(
                messageBundle.getString("viewer.toolbar.print.label"),
                messageBundle.getString("viewer.toolbar.print.tooltip"),
                "print", Images.SIZE_LARGE,
                buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setPrintButton(btn);
        return btn;
    }

    public JButton buildSearchButton() {
        JButton btn = makeToolbarButton(
                messageBundle.getString("viewer.toolbar.search.label"),
                messageBundle.getString("viewer.toolbar.search.tooltip"),
                "search", Images.SIZE_LARGE, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setSearchButton(btn);
        return btn;
    }

    public JToggleButton buildShowHideUtilityPaneButton() {
        JToggleButton btn = makeToolbarToggleButton(
                messageBundle.getString("viewer.toolbar.utilityPane.label"),
                messageBundle.getString("viewer.toolbar.utilityPane.tooltip"),
                "utility_pane", Images.SIZE_LARGE, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setShowHideUtilityPaneButton(btn);
        return btn;
    }

    public JToolBar buildPageNavigationToolBar() {
        JToolBar toolbar = new JToolBar();
        commonToolBarSetup(toolbar, false);
        addToToolBar(toolbar, buildFirstPageButton());
        addToToolBar(toolbar, buildPreviousPageButton());
        addToToolBar(toolbar, buildCurrentPageNumberTextField());
        addToToolBar(toolbar, buildNumberOfPagesLabel());
        addToToolBar(toolbar, buildNextPageButton());
        addToToolBar(toolbar, buildLastPageButton());
        return toolbar;
    }

    public JButton buildFirstPageButton() {
        JButton btn = makeToolbarButton(
                messageBundle.getString("viewer.toolbar.navigation.firstPage.label"),
                messageBundle.getString("viewer.toolbar.navigation.firstPage.tooltip"),
                "first", Images.SIZE_LARGE, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setFirstPageButton(btn);
        return btn;
    }

    public JButton buildPreviousPageButton() {
        JButton btn = makeToolbarButton(
                messageBundle.getString("viewer.toolbar.navigation.previousPage.label"),
                messageBundle.getString("viewer.toolbar.navigation.previousPage.tooltip"),
                "back", Images.SIZE_LARGE, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setPreviousPageButton(btn);
        return btn;
    }

    public JButton buildNextPageButton() {
        JButton btn = makeToolbarButton(
                messageBundle.getString("viewer.toolbar.navigation.nextPage.label"),
                messageBundle.getString("viewer.toolbar.navigation.nextPage.tooltip"),
                "forward", Images.SIZE_LARGE, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setNextPageButton(btn);
        return btn;
    }

    public JButton buildLastPageButton() {
        JButton btn = makeToolbarButton(
                messageBundle.getString("viewer.toolbar.navigation.lastPage.label"),
                messageBundle.getString("viewer.toolbar.navigation.lastPage.tooltip"),
                "last", Images.SIZE_LARGE, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setLastPageButton(btn);
        return btn;
    }

    public JTextField buildCurrentPageNumberTextField() {
        JTextField pageNumberTextField = new JTextField("", 3);
        pageNumberTextField.setToolTipText(messageBundle.getString("viewer.toolbar.navigation.current.tooltip"));
        pageNumberTextField.setInputVerifier(new PageNumberTextFieldInputVerifier());

        /**
         * Add a key listener and check to make sure the character intered
         * is a digit, period, the back_space or delete keys. If not the
         * invalid character is ignored and a system beep is triggered.
         */
        pageNumberTextField.addKeyListener(new PageNumberTextFieldKeyListener());
        if (viewerController != null)
            viewerController.setCurrentPageNumberTextField(pageNumberTextField);
        return pageNumberTextField;
    }

    public JLabel buildNumberOfPagesLabel() {
        JLabel lbl = new JLabel();
        lbl.setToolTipText(messageBundle.getString("viewer.toolbar.navigation.pages.tooltip"));
        if (viewerController != null)
            viewerController.setNumberOfPagesLabel(lbl);
        return lbl;
    }

    public JToolBar buildZoomToolBar() {
        JToolBar toolbar = new JToolBar();
        commonToolBarSetup(toolbar, false);
        addToToolBar(toolbar, buildZoomOutButton());
        addToToolBar(toolbar, buildZoomCombBox());
        addToToolBar(toolbar, buildZoomInButton());
        return toolbar;
    }

    public JButton buildZoomOutButton() {
        JButton btn = makeToolbarButton(
                messageBundle.getString("viewer.toolbar.zoom.out.label"),
                messageBundle.getString("viewer.toolbar.zoom.out.tooltip"),
                "zoom_out", Images.SIZE_LARGE, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setZoomOutButton(btn);
        return btn;
    }

    public JComboBox buildZoomCombBox() {
        // Get the properties manager in preparation for trying to get the zoom levels
        doubleCheckPropertiesManager();

        // Assign any different zoom ranges from the properties file if possible
        zoomLevels = PropertiesManager.checkAndStoreFloatArrayProperty(propertiesManager,
                PropertiesManager.PROPERTY_ZOOM_RANGES,
                zoomLevels);

        JComboBox tmp = new JComboBox();
        tmp.setToolTipText(messageBundle.getString("viewer.toolbar.zoom.tooltip"));
        tmp.setPreferredSize(new Dimension(75, tmp.getHeight()));
        for (float zoomLevel : zoomLevels)
            tmp.addItem(NumberFormat.getPercentInstance().format(zoomLevel));
        tmp.setEditable(true);
        if (viewerController != null)
            viewerController.setZoomComboBox(tmp, zoomLevels);
        return tmp;
    }

    public JButton buildZoomInButton() {
        JButton btn = makeToolbarButton(
                messageBundle.getString("viewer.toolbar.zoom.in.label"),
                messageBundle.getString("viewer.toolbar.zoom.in.tooltip"),
                "zoom_in", Images.SIZE_LARGE, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setZoomInButton(btn);
        return btn;
    }

    public JToolBar buildFitToolBar() {
        JToolBar toolbar = new JToolBar();
        commonToolBarSetup(toolbar, false);
        addToToolBar(toolbar, buildFitActualSizeButton());
        addToToolBar(toolbar, buildFitPageButton());
        addToToolBar(toolbar, buildFitWidthButton());
        return toolbar;
    }

    public JToggleButton buildFitActualSizeButton() {
        JToggleButton btn = makeToolbarToggleButton(
                messageBundle.getString("viewer.toolbar.pageFit.actualsize.label"),
                messageBundle.getString("viewer.toolbar.pageFit.actualsize.tooltip"),
                "actual_size", Images.SIZE_LARGE, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setFitActualSizeButton(btn);
        return btn;
    }

    public JToggleButton buildFitPageButton() {
        JToggleButton btn = makeToolbarToggleButton(
                messageBundle.getString("viewer.toolbar.pageFit.fitWindow.label"),
                messageBundle.getString("viewer.toolbar.pageFit.fitWindow.tooltip"),
                "fit_window", Images.SIZE_LARGE, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setFitHeightButton(btn);
        return btn;
    }

    public JToggleButton buildFontEngineButton() {
        JToggleButton btn = makeToolbarToggleButton(
                messageBundle.getString("viewer.toolbar.pageFit.fontEngine.label"),
                messageBundle.getString("viewer.toolbar.pageFit.fontEngine.tooltip"),
                "font-engine", 118, 25, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setFontEngineButton(btn);
        return btn;
    }

    public JToggleButton buildFitWidthButton() {
        JToggleButton btn = makeToolbarToggleButton(
                messageBundle.getString("viewer.toolbar.pageFit.fitWidth.label"),
                messageBundle.getString("viewer.toolbar.pageFit.fitWidth.tooltip"),
                "fit_width", Images.SIZE_LARGE, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setFitWidthButton(btn);
        return btn;
    }

    public JToolBar buildRotateToolBar() {
        JToolBar toolbar = new JToolBar();
        commonToolBarSetup(toolbar, false);
        addToToolBar(toolbar, buildRotateRightButton());
        addToToolBar(toolbar, buildRotateLeftButton());
        return toolbar;
    }

    public JButton buildRotateLeftButton() {
        JButton btn = makeToolbarButton(
                messageBundle.getString("viewer.toolbar.rotation.left.label"),
                messageBundle.getString("viewer.toolbar.rotation.left.tooltip"),
                "rotate_left", Images.SIZE_LARGE, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setRotateLeftButton(btn);
        return btn;
    }

    public JButton buildRotateRightButton() {
        JButton btn = makeToolbarButton(
                messageBundle.getString("viewer.toolbar.rotation.right.label"),
                messageBundle.getString("viewer.toolbar.rotation.right.tooltip"),
                "rotate_right", Images.SIZE_LARGE, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setRotateRightButton(btn);
        return btn;
    }

    public JToolBar buildToolToolBar() {
        JToolBar toolbar = new JToolBar();
        commonToolBarSetup(toolbar, false);
        addToToolBar(toolbar, buildPanToolButton());
        addToToolBar(toolbar, buildTextSelectToolButton());
        addToToolBar(toolbar, buildZoomInToolButton());
        addToToolBar(toolbar, buildZoomOutToolButton());
        return toolbar;
    }

    public JToolBar buildAnnotationlToolBar() {
        JToolBar toolbar = new JToolBar();
        commonToolBarSetup(toolbar, false);
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION_SELECTION)) {
            addToToolBar(toolbar, buildSelectToolButton(Images.SIZE_LARGE));
        }
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION_HIGHLIGHT)) {
            addToToolBar(toolbar, buildHighlightAnnotationToolButton(Images.SIZE_LARGE));
        }
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION_TEXT)) {
            addToToolBar(toolbar, buildTextAnnotationToolButton(Images.SIZE_LARGE));
        }
        return toolbar;
    }

    public JToolBar buildFormsToolBar() {
        JToolBar toolbar = new JToolBar();
        commonToolBarSetup(toolbar, false);
        addToToolBar(toolbar, buildFormHighlightButton(Images.SIZE_LARGE));
        return toolbar;
    }

    public JToolBar buildAnnotationUtilityToolBar() {
        JToolBar toolbar = new JToolBar();
        commonToolBarSetup(toolbar, true);
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_UTILITY_ANNOTATION_HIGHLIGHT)) {
            addToToolBar(toolbar, buildHighlightAnnotationUtilityToolButton(Images.SIZE_MEDIUM));
        }
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_UTILITY_ANNOTATION_STRIKE_OUT)) {
            addToToolBar(toolbar, buildStrikeOutAnnotationToolButton());
        }
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_UTILITY_ANNOTATION_UNDERLINE)) {
            addToToolBar(toolbar, buildUnderlineAnnotationToolButton());
        }
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_UTILITY_ANNOTATION_LINE)) {
            addToToolBar(toolbar, buildLineAnnotationToolButton());
        }
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_UTILITY_ANNOTATION_LINK)) {
            addToToolBar(toolbar, buildLinkAnnotationToolButton());
        }
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_UTILITY_ANNOTATION_ARROW)) {
            addToToolBar(toolbar, buildLineArrowAnnotationToolButton());
        }
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_UTILITY_ANNOTATION_RECTANGLE)) {
            addToToolBar(toolbar, buildSquareAnnotationToolButton());
        }
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_UTILITY_ANNOTATION_CIRCLE)) {
            addToToolBar(toolbar, buildCircleAnnotationToolButton());
        }
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_UTILITY_ANNOTATION_INK)) {
            addToToolBar(toolbar, buildInkAnnotationToolButton());
        }
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_UTILITY_ANNOTATION_FREE_TEXT)) {
            addToToolBar(toolbar, buildFreeTextAnnotationToolButton());
        }
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_UTILITY_ANNOTATION_TEXT)) {
            addToToolBar(toolbar, buildTextAnnotationUtilityToolButton(Images.SIZE_MEDIUM));
        }
        return toolbar;
    }

    public JToolBar buildDemoToolBar() {
        JToolBar toolbar = new JToolBar();
        commonToolBarSetup(toolbar, false);
        addToToolBar(toolbar, buildFontEngineButton());
        return toolbar;
    }

    public JToggleButton buildPanToolButton() {
        JToggleButton btn = makeToolbarToggleButton(
                messageBundle.getString("viewer.toolbar.tool.pan.label"),
                messageBundle.getString("viewer.toolbar.tool.pan.tooltip"),
                "pan", Images.SIZE_LARGE, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setPanToolButton(btn);
        return btn;
    }

    public JToggleButton buildTextSelectToolButton() {
        JToggleButton btn = makeToolbarToggleButton(
                messageBundle.getString("viewer.toolbar.tool.text.label"),
                messageBundle.getString("viewer.toolbar.tool.text.tooltip"),
                "selection_text", Images.SIZE_LARGE, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setTextSelectToolButton(btn);
        return btn;
    }

    public JToggleButton buildSelectToolButton(final String imageSize) {
        JToggleButton btn = makeToolbarToggleButton(
                messageBundle.getString("viewer.toolbar.tool.select.label"),
                messageBundle.getString("viewer.toolbar.tool.select.tooltip"),
                "select", imageSize, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setSelectToolButton(btn);
        return btn;
    }

    public JToggleButton buildLinkAnnotationToolButton() {
        JToggleButton btn = makeToolbarToggleButtonSmall(
                messageBundle.getString("viewer.toolbar.tool.link.label"),
                messageBundle.getString("viewer.toolbar.tool.link.tooltip"),
                "link_annot", Images.SIZE_MEDIUM, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setLinkAnnotationToolButton(btn);
        return btn;
    }

    public JToggleButton buildHighlightAnnotationToolButton(final String imageSize) {
        JToggleButton btn = makeToolbarToggleButton(
                messageBundle.getString("viewer.toolbar.tool.highlight.label"),
                messageBundle.getString("viewer.toolbar.tool.highlight.tooltip"),
                "highlight_annot", imageSize, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setHighlightAnnotationToolButton(btn);
        return btn;
    }

    public JToggleButton buildHighlightAnnotationUtilityToolButton(final String imageSize) {
        JToggleButton btn = makeToolbarToggleButtonSmall(
                messageBundle.getString("viewer.toolbar.tool.highlight.label"),
                messageBundle.getString("viewer.toolbar.tool.highlight.tooltip"),
                "highlight_annot", imageSize, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setHighlightAnnotationUtilityToolButton(btn);
        return btn;
    }

    public JToggleButton buildStrikeOutAnnotationToolButton() {
        JToggleButton btn = makeToolbarToggleButtonSmall(
                messageBundle.getString("viewer.toolbar.tool.strikeOut.label"),
                messageBundle.getString("viewer.toolbar.tool.strikeOut.tooltip"),
                "strikeout", Images.SIZE_MEDIUM, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setStrikeOutAnnotationToolButton(btn);
        return btn;
    }

    public JToggleButton buildUnderlineAnnotationToolButton() {
        JToggleButton btn = makeToolbarToggleButtonSmall(
                messageBundle.getString("viewer.toolbar.tool.underline.label"),
                messageBundle.getString("viewer.toolbar.tool.underline.tooltip"),
                "underline", Images.SIZE_MEDIUM, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setUnderlineAnnotationToolButton(btn);
        return btn;
    }

    public JToggleButton buildLineAnnotationToolButton() {
        JToggleButton btn = makeToolbarToggleButtonSmall(
                messageBundle.getString("viewer.toolbar.tool.line.label"),
                messageBundle.getString("viewer.toolbar.tool.line.tooltip"),
                "line", Images.SIZE_MEDIUM, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setLineAnnotationToolButton(btn);
        return btn;
    }

    public JToggleButton buildLineArrowAnnotationToolButton() {
        JToggleButton btn = makeToolbarToggleButtonSmall(
                messageBundle.getString("viewer.toolbar.tool.lineArrow.label"),
                messageBundle.getString("viewer.toolbar.tool.lineArrow.tooltip"),
                "arrow", Images.SIZE_MEDIUM, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setLineArrowAnnotationToolButton(btn);
        return btn;
    }

    public JToggleButton buildSquareAnnotationToolButton() {
        JToggleButton btn = makeToolbarToggleButtonSmall(
                messageBundle.getString("viewer.toolbar.tool.rectangle.label"),
                messageBundle.getString("viewer.toolbar.tool.rectangle.tooltip"),
                "square", Images.SIZE_MEDIUM, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setSquareAnnotationToolButton(btn);
        return btn;
    }

    public JToggleButton buildCircleAnnotationToolButton() {
        JToggleButton btn = makeToolbarToggleButtonSmall(
                messageBundle.getString("viewer.toolbar.tool.circle.label"),
                messageBundle.getString("viewer.toolbar.tool.circle.tooltip"),
                "circle", Images.SIZE_MEDIUM, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setCircleAnnotationToolButton(btn);
        return btn;
    }

    public JToggleButton buildInkAnnotationToolButton() {
        JToggleButton btn = makeToolbarToggleButtonSmall(
                messageBundle.getString("viewer.toolbar.tool.ink.label"),
                messageBundle.getString("viewer.toolbar.tool.ink.tooltip"),
                "ink", Images.SIZE_MEDIUM, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setInkAnnotationToolButton(btn);
        return btn;
    }

    public JToggleButton buildFreeTextAnnotationToolButton() {
        JToggleButton btn = makeToolbarToggleButtonSmall(
                messageBundle.getString("viewer.toolbar.tool.freeText.label"),
                messageBundle.getString("viewer.toolbar.tool.freeText.tooltip"),
                "freetext_annot", Images.SIZE_MEDIUM, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setFreeTextAnnotationToolButton(btn);
        return btn;
    }

    public JToggleButton buildTextAnnotationToolButton(final String imageSize) {
        JToggleButton btn = makeToolbarToggleButton(
                messageBundle.getString("viewer.toolbar.tool.textAnno.label"),
                messageBundle.getString("viewer.toolbar.tool.textAnno.tooltip"),
                "text_annot", Images.SIZE_MEDIUM, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setTextAnnotationToolButton(btn);
        return btn;
    }

    public JToggleButton buildFormHighlightButton(final String imageSize) {
        JToggleButton btn = makeToolbarToggleButton(
                messageBundle.getString("viewer.toolbar.tool.forms.highlight.label"),
                messageBundle.getString("viewer.toolbar.tool.forms.highlight.tooltip"),
                "form_highlight", Images.SIZE_LARGE, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setFormHighlightButton(btn);
        return btn;
    }

    public JToggleButton buildTextAnnotationUtilityToolButton(final String imageSize) {
        JToggleButton btn = makeToolbarToggleButtonSmall(
                messageBundle.getString("viewer.toolbar.tool.textAnno.label"),
                messageBundle.getString("viewer.toolbar.tool.textAnno.tooltip"),
                "text_annot", Images.SIZE_MEDIUM, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setTextAnnotationUtilityToolButton(btn);
        return btn;
    }

    public JToggleButton buildZoomInToolButton() {
        JToggleButton btn = makeToolbarToggleButton(
                messageBundle.getString("viewer.toolbar.tool.zoomMarquis.label"),
                messageBundle.getString("viewer.toolbar.tool.zoomMarquis.tooltip"),
                "zoom_marquis", Images.SIZE_LARGE, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setZoomInToolButton(btn);
        return btn;
    }

    public JToggleButton buildZoomOutToolButton() {
        JToggleButton btn = makeToolbarToggleButton(
                messageBundle.getString("viewer.toolbar.tool.zoomDynamic.label"),
                messageBundle.getString("viewer.toolbar.tool.zoomDynamic.tooltip"),
                "zoom_dynamic", Images.SIZE_LARGE, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setZoomDynamicToolButton(btn);
        return btn;
    }


    public JSplitPane buildUtilityAndDocumentSplitPane(boolean embeddableComponent) {
        JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitpane.setOneTouchExpandable(false);
        splitpane.setDividerSize(8);
        splitpane.setContinuousLayout(true);
        // set the utility pane the left of the split pane
        splitpane.setLeftComponent(buildUtilityTabbedPane());

        // set the viewController embeddable flag.
        DocumentViewController viewController =
                viewerController.getDocumentViewController();
        // will add key event listeners
        viewerController.setIsEmbeddedComponent(embeddableComponent);

        // remove F6 focus management key from the splitpane
        splitpane.getActionMap().getParent().remove("toggleFocus");

        // add the viewControllers doc view container to the split pain
        splitpane.setRightComponent(viewController.getViewContainer());

        // apply previously set divider location, default is -1
        int dividerLocation = PropertiesManager.checkAndStoreIntegerProperty(
                propertiesManager,
                PropertiesManager.PROPERTY_DIVIDER_LOCATION, 260);
        splitpane.setDividerLocation(dividerLocation);

        // Add the split pan component to the view controller so that it can
        // manipulate the divider via the controller, hide, show, etc. for
        // utility pane.
        if (viewerController != null)
            viewerController.setUtilityAndDocumentSplitPane(splitpane);
        return splitpane;
    }

    public JTabbedPane buildUtilityTabbedPane() {
        JTabbedPane utilityTabbedPane = new JTabbedPane();
        utilityTabbedPane.setPreferredSize(new Dimension(250, 400));

        // Get a properties manager that can be used to configure utility pane visibility
        doubleCheckPropertiesManager();

        // Build the main set of tabs based on the property file configuration
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_UTILITYPANE_BOOKMARKS)) {
            utilityTabbedPane.add(
                    messageBundle.getString("viewer.utilityPane.bookmarks.tab.title"),
                    buildOutlineComponents());
        }
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_UTILITYPANE_ATTACHMENTS)) {
            utilityTabbedPane.add(
                    messageBundle.getString("viewer.utilityPane.attachments.tab.title"),
                    buildAttachmentPanle());
        }
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_UTILITYPANE_SEARCH)) {
            utilityTabbedPane.add(
                    messageBundle.getString("viewer.utilityPane.search.tab.title"),
                    buildSearchPanel());
        }
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_UTILITYPANE_THUMBNAILS)) {
            utilityTabbedPane.add(
                    messageBundle.getString("viewer.utilityPane.thumbs.tab.title"),
                    buildThumbsPanel());
        }
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_UTILITYPANE_LAYERS)) {
            utilityTabbedPane.add(
                    messageBundle.getString("viewer.utilityPane.layers.tab.title"),
                    buildLayersComponents());
        }
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_UTILITYPANE_SIGNATURES)) {
            utilityTabbedPane.add(
                    messageBundle.getString("viewer.utilityPane.signatures.tab.title"),
                    buildSignatureComponents());
        }
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_UTILITYPANE_ANNOTATION)) {
            utilityTabbedPane.add(
                    messageBundle.getString("viewer.utilityPane.annotation.tab.title"),
                    buildAnnotationPanel());
        }

        // Ensure something was added to the utility pane, otherwise reset it to null
        // By doing this we will stop the utility pane management buttons from displaying
        if (utilityTabbedPane.getComponentCount() == 0) {
            utilityTabbedPane = null;
        }

        if (viewerController != null)
            viewerController.setUtilityTabbedPane(utilityTabbedPane);

        return utilityTabbedPane;
    }

    public JComponent buildOutlineComponents() {
        JTree tree = new OutlinesTree();
        JScrollPane scroll = new JScrollPane(tree);
        if (viewerController != null)
            viewerController.setOutlineComponents(tree, scroll);
        return scroll;
    }

    public ThumbnailsPanel buildThumbsPanel() {
        ThumbnailsPanel thumbsPanel = new ThumbnailsPanel(viewerController,
                propertiesManager);
        if (viewerController != null) {
            viewerController.setThumbnailsPanel(thumbsPanel);
        }
        return thumbsPanel;
    }

    public LayersPanel buildLayersComponents() {
        LayersPanel layersPanel = new LayersPanel(viewerController);
        if (viewerController != null) {
            viewerController.setLayersPanel(layersPanel);
        }
        return layersPanel;
    }

    public JComponent buildSignatureComponents() {
        SignaturesPanel signaturesPanel = new SignaturesPanel(viewerController);
        if (viewerController != null) {
            viewerController.setSignaturesPanel(signaturesPanel);
        }
        return signaturesPanel;
    }

    public SearchPanel buildSearchPanel() {
        SearchPanel searchPanel = new SearchPanel(viewerController);
        if (viewerController != null)
            viewerController.setSearchPanel(searchPanel);
        return searchPanel;
    }

    public AttachmentPanel buildAttachmentPanle(){
        AttachmentPanel attachmentPanel = new AttachmentPanel(viewerController);
        if (viewerController != null)
            viewerController.setAttachmentPanel(attachmentPanel);
        return attachmentPanel;
    }

    public AnnotationPanel buildAnnotationPanel() {
        AnnotationPanel annotationPanel = new AnnotationPanel(viewerController, propertiesManager);
        annotationPanel.setAnnotationUtilityToolbar(buildAnnotationUtilityToolBar());
        if (viewerController != null)
            viewerController.setAnnotationPanel(annotationPanel);
        return annotationPanel;
    }

    /**
     * Builds the status bar panel containing a status label on the left and
     * view mode controls on the right.  The status bar can be shown or
     * hidden completely using the view property 'application.statusbar=true|false'
     * and the two child frame elements can be controlled using
     * 'application.statusbar.show.statuslabel=true|false' and
     * 'application.statusbar.show.viewmode=true|false'.  The default value
     * for all properties is 'true'.
     *
     * @return status panel JPanel if visible, null if the proeprty
     * 'application.statusbar=false' is set.
     */
    public JPanel buildStatusPanel() {
        // check to see if the status bars should be built.
        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_STATUSBAR)) {
            JPanel statusPanel = new JPanel(new BorderLayout());

            if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                    PropertiesManager.PROPERTY_SHOW_STATUSBAR_STATUSLABEL)) {
                JPanel pgPanel = new JPanel();
                JLabel lbl = new JLabel(" ");
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0)); // So text isn't at the very edge
                pgPanel.add(lbl);
                statusPanel.add(pgPanel, BorderLayout.WEST);
                // set status label callback
                if (viewerController != null) {
                    viewerController.setStatusLabel(lbl);
                }
            }

            JPanel viewPanel = new JPanel();
            // Only add actual buttons to the view panel if requested by the properties file
            // Regardless we'll add the parent JPanel, to preserve the same layout behaviour
            if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                    PropertiesManager.PROPERTY_SHOW_STATUSBAR_VIEWMODE)) {
                if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                        PropertiesManager.PROPERTY_SHOW_STATUSBAR_VIEWMODE_SINGLE))
                    viewPanel.add(buildPageViewSinglePageNonConToggleButton());
                if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                        PropertiesManager.PROPERTY_SHOW_STATUSBAR_VIEWMODE_SINGLE_CONTINUOUS))
                    viewPanel.add(buildPageViewSinglePageConToggleButton());
                if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                        PropertiesManager.PROPERTY_SHOW_STATUSBAR_VIEWMODE_DOUBLE))
                    viewPanel.add(buildPageViewFacingPageNonConToggleButton());
                if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                        PropertiesManager.PROPERTY_SHOW_STATUSBAR_VIEWMODE_DOUBLE_CONTINUOUS))
                    viewPanel.add(buildPageViewFacingPageConToggleButton());
            }
            statusPanel.add(viewPanel, BorderLayout.CENTER);
            viewPanel.setLayout(new ToolbarLayout(ToolbarLayout.RIGHT, 0, 1));

            JLabel lbl2 = new JLabel(" ");
            lbl2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5)); // So text isn't at the very edge
            statusPanel.add(lbl2, BorderLayout.EAST);

            return statusPanel;
        }
        return null;
    }

    public JToggleButton buildPageViewSinglePageConToggleButton() {
        JToggleButton btn = makeToolbarToggleButton(messageBundle.getString("viewer.toolbar.pageView.continuous.singlePage.label"),
                messageBundle.getString("viewer.toolbar.pageView.continuous.singlePage.tooltip"),
                "single_page_column", Images.SIZE_MEDIUM,
                buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setPageViewSinglePageConButton(btn);
        return btn;
    }

    public JToggleButton buildPageViewFacingPageConToggleButton() {
        JToggleButton btn = makeToolbarToggleButton(messageBundle.getString("viewer.toolbar.pageView.continuous.facingPage.label"),
                messageBundle.getString("viewer.toolbar.pageView.continuous.facingPage.tooltip"),
                "two_page_column", Images.SIZE_MEDIUM,
                buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setPageViewFacingPageConButton(btn);
        return btn;
    }

    public JToggleButton buildPageViewSinglePageNonConToggleButton() {
        JToggleButton btn = makeToolbarToggleButton(messageBundle.getString("viewer.toolbar.pageView.nonContinuous.singlePage.label"),
                messageBundle.getString("viewer.toolbar.pageView.nonContinuous.singlePage.tooltip"),
                "single_page", Images.SIZE_MEDIUM, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setPageViewSinglePageNonConButton(btn);
        return btn;
    }

    public JToggleButton buildPageViewFacingPageNonConToggleButton() {
        JToggleButton btn = makeToolbarToggleButton(messageBundle.getString("viewer.toolbar.pageView.nonContinuous.facingPage.label"),
                messageBundle.getString("viewer.toolbar.pageView.nonContinuous.facingPage.tooltip"),
                "two_page", Images.SIZE_MEDIUM, buttonFont);
        if (viewerController != null && btn != null)
            viewerController.setPageViewFacingPageNonConButton(btn);
        return btn;
    }


    /**
     * Utility method for creating a toolbar button.
     *
     * @param title     display text for the menu item
     * @param toolTip   tool tip text
     * @param imageName display image name
     * @param imageSize image size file extention constant
     * @param font      display font
     * @return a button with the specified characteristics.
     */
    protected JButton makeToolbarButton(
            String title, String toolTip, String imageName, final String imageSize,
            java.awt.Font font) {
        JButton tmp = new JButton(showButtonText ? title : "");
        tmp.setFont(font);
        tmp.setToolTipText(toolTip);
        tmp.setPreferredSize(new Dimension(32, 32));
        try {
            tmp.setIcon(new ImageIcon(Images.get(imageName + "_a" + imageSize + ".png")));
            tmp.setPressedIcon(new ImageIcon(Images.get(imageName + "_i" + imageSize + ".png")));
            tmp.setRolloverIcon(new ImageIcon(Images.get(imageName + "_r" + imageSize + ".png")));
            tmp.setDisabledIcon(new ImageIcon(Images.get(imageName + "_i" + imageSize + ".png")));
        } catch (NullPointerException e) {
            logger.warning("Failed to load toolbar button images: " + imageName + "_i" + imageSize + ".png");
        }
        tmp.setRolloverEnabled(true);
        tmp.setBorderPainted(false);
        tmp.setContentAreaFilled(false);
        tmp.setFocusPainted(true);

        return tmp;
    }

    /**
     * Utility method for creating toggle buttons.
     *
     * @param title     display text for the menu item
     * @param toolTip   tool tip text
     * @param imageName display image name
     * @param font      display font
     * @param imageSize imageSize image size constant
     * @return a toggle button with the specified characteristics.
     */
    protected JToggleButton makeToolbarToggleButton(
            String title, String toolTip, String imageName,
            final String imageSize, java.awt.Font font) {
        JToggleButton tmp = new JToggleButton(showButtonText ? title : "");
        tmp.setFont(font);
        tmp.setToolTipText(toolTip);
        tmp.setPreferredSize(new Dimension(32, 32));
        tmp.setRolloverEnabled(true);

        try {
            tmp.setIcon(new ImageIcon(Images.get(imageName + "_a" + imageSize + ".png")));
            tmp.setPressedIcon(new ImageIcon(Images.get(imageName + "_i" + imageSize + ".png")));
            tmp.setRolloverIcon(new ImageIcon(Images.get(imageName + "_r" + imageSize + ".png")));
            tmp.setDisabledIcon(new ImageIcon(Images.get(imageName + "_i" + imageSize + ".png")));
        } catch (NullPointerException e) {
            logger.warning("Failed to load toolbar toggle button images: " + imageName + "_i" + imageSize + ".png");
        }
        //tmp.setBorderPainted(false);
        tmp.setBorder(BorderFactory.createEmptyBorder());
        tmp.setContentAreaFilled(false);
        tmp.setFocusPainted(true);

        return tmp;
    }

    /**
     * Utility method for creating small toggle buttons (24x24) that also
     * have a selected icon state. .
     *
     * @param title     display text for the menu item
     * @param toolTip   tool tip text
     * @param imageName display image name
     * @param font      display font
     * @param imageSize imageSize image size constant
     * @return a toggle button with the specified characteristics.
     */
    protected JToggleButton makeToolbarToggleButtonSmall(
            String title, String toolTip, String imageName,
            final String imageSize, java.awt.Font font) {
        JToggleButton tmp = new JToggleButton(showButtonText ? title : "");
        tmp.setFont(font);
        tmp.setToolTipText(toolTip);
        tmp.setPreferredSize(new Dimension(24, 24));
        try {
            tmp.setIcon(new ImageIcon(Images.get(imageName + "_a" + imageSize + ".png")));
            tmp.setPressedIcon(new ImageIcon(Images.get(imageName + "_i" + imageSize + ".png")));
//            tmp.setSelectedIcon(new ImageIcon(Images.get(imageName + "_s" + imageSize + ".png")));
            tmp.setRolloverIcon(new ImageIcon(Images.get(imageName + "_r" + imageSize + ".png")));
            tmp.setDisabledIcon(new ImageIcon(Images.get(imageName + "_i" + imageSize + ".png")));
        } catch (NullPointerException e) {
            logger.warning("Failed to load toolbar toggle images: " + imageName + "_i" + imageSize + ".png");
        }
        //tmp.setBorderPainted(false);
        tmp.setBorder(BorderFactory.createEmptyBorder());
        tmp.setContentAreaFilled(false);
        tmp.setRolloverEnabled(true);
        tmp.setFocusPainted(true);

        return tmp;
    }


    protected JToggleButton makeToolbarToggleButton(
            String title, String toolTip, java.awt.Font font) {
        JToggleButton tmp = new JToggleButton(showButtonText ? title : "");
        tmp.setFont(font);
        tmp.setToolTipText(toolTip);
        tmp.setPreferredSize(new Dimension(30, 30));
        tmp.setText(title);
        tmp.setFocusPainted(true);
        return tmp;
    }


    protected JToggleButton makeToolbarToggleButton(
            String title, String toolTip, String imageName,
            int imageWidth, int imageHeight, java.awt.Font font) {
        JToggleButton tmp = new JToggleButton(showButtonText ? title : "");
        tmp.setFont(font);
        tmp.setToolTipText(toolTip);
        tmp.setRolloverEnabled(false);
        tmp.setPreferredSize(new Dimension(imageWidth, imageHeight));
        try {
            tmp.setIcon(new ImageIcon(Images.get(imageName + "_d.png")));
            tmp.setPressedIcon(new ImageIcon(Images.get(imageName + "_d.png")));
            tmp.setSelectedIcon(new ImageIcon(Images.get(imageName + "_n.png")));
            tmp.setDisabledIcon(new ImageIcon(Images.get(imageName + "_n.png")));
        } catch (NullPointerException e) {
            logger.warning("Failed to load toobar toggle button images: " + imageName + ".png");
        }
        tmp.setBorderPainted(false);
        tmp.setBorder(BorderFactory.createEmptyBorder());
        tmp.setContentAreaFilled(false);
        tmp.setFocusPainted(false);

        return tmp;
    }

    /**
     * Utility method for creating a menu item.
     *
     * @param text display text for the menu item
     * @return menu item complete with text and action listener
     */
    protected JMenuItem makeMenuItem(String text, KeyStroke accel) {
        JMenuItem jmi = new JMenuItem(text);
        if (accel != null)
            jmi.setAccelerator(accel);
        return jmi;
    }

    /**
     * Utility method for creating a menu item with an image.
     *
     * @param text      display text for the menu item
     * @param imageName display image for the menu item
     * @param imageSize size of the image.
     * @return memu item complete with text, image and action listener
     */
    protected JMenuItem makeMenuItem(String text, String imageName,
                                     final String imageSize, KeyStroke accel) {
        JMenuItem jmi = new JMenuItem(text);
        if (imageName != null) {
            try {
                jmi.setIcon(new ImageIcon(Images.get(imageName + "_a" + imageSize + ".png")));
                jmi.setDisabledIcon(new ImageIcon(Images.get(imageName + "_i" + imageSize + ".png")));
                jmi.setRolloverIcon(new ImageIcon(Images.get(imageName + "_r" + imageSize + ".png")));
            } catch (NullPointerException e) {
                logger.warning("Failed to load menu images: " + imageName + "_a" + imageSize + ".png");
            }
        } else {
            jmi.setIcon(new ImageIcon(Images.get("menu_spacer.gif")));
            jmi.setDisabledIcon(new ImageIcon(Images.get("menu_spacer.gif")));
            jmi.setRolloverIcon(new ImageIcon(Images.get("menu_spacer.gif")));
        }
        jmi.setBorder(BorderFactory.createEmptyBorder());
        jmi.setContentAreaFilled(false);
        jmi.setFocusPainted(true);
        if (accel != null)
            jmi.setAccelerator(accel);

        return jmi;
    }

    protected void commonToolBarSetup(JToolBar toolbar, boolean isMainToolBar) {
        if (!isMainToolBar) {
            toolbar.requestFocus();
            toolbar.setRollover(true);
        }
        if (toolbarStyle == TOOL_BAR_STYLE_FIXED) {
            toolbar.setFloatable(false);
            if (!isMainToolBar) {
                if (haveMadeAToolBar)
                    toolbar.addSeparator();
                haveMadeAToolBar = true;
            }
        }
    }

    /**
     * Method to try to get the properties manager from the window management callback,
     * if we don't already have a propertiesManager object
     */
    protected void doubleCheckPropertiesManager() {
        if ((propertiesManager == null) &&
                (viewerController != null) &&
                (viewerController.getWindowManagementCallback() != null)) {
            propertiesManager = viewerController.getWindowManagementCallback().getProperties();
        }
    }

    /**
     * Method to attempt to override the system property highlight color
     * If the current color is blank, we'll try to pull the same property from
     * our local propertiesManager and, if found, apply it to the system properties
     * This affects the search highlight coloring
     */
    protected void overrideHighlightColor() {
        // Attempt to override the default highlight color
        // We will only attempt this if a -D system parameter was not passed
        if (Defs.sysProperty(PropertiesManager.SYSPROPERTY_HIGHLIGHT_COLOR) == null) {
            doubleCheckPropertiesManager();

            // Try to pull the color from our local properties file
            // If we can find a value, then set it as the system property
            if (propertiesManager != null) {
                String newColor = propertiesManager.getString(PropertiesManager.SYSPROPERTY_HIGHLIGHT_COLOR, null);
                if (newColor != null) {
                    Defs.setSystemProperty(PropertiesManager.SYSPROPERTY_HIGHLIGHT_COLOR, newColor);
                }
            }
        }
    }

    protected Font buildButtonFont() {
        return new java.awt.Font("Helvetica", java.awt.Font.PLAIN, 9);
    }

    protected void addToToolBar(JToolBar toolbar, JComponent comp) {
        if (comp != null)
            toolbar.add(comp);
    }

    protected void addToMenu(JMenu menu, JMenuItem mi) {
        if (mi != null)
            menu.add(mi);
    }

    protected void addToMenuBar(JMenuBar menuBar, JMenu menu) {
        if (menu != null)
            menuBar.add(menu);
    }
}
