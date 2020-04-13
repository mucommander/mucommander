package org.icepdf.ri.common.fonts;

import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.fonts.Font;
import org.icepdf.ri.common.EscapeJDialog;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingWorker;
import org.icepdf.ri.images.Images;
import org.icepdf.ri.util.FontPropertiesManager;
import org.icepdf.ri.util.PropertiesManager;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;


/**
 * This class is a reference implementation for displaying a PDF file's
 * font information.   The dialog will start a worker thread that will read all the document's font objects and
 * build a tree view of the all the fonts.  This font view is primarily for debug purposes to make it easier to track
 * font substitution results.  The dialog also provides an easy way to refresh the
 * "\.icesoft\icepdf-viewer\pdfviewerfontcache.properties' with out manually deleting the file and restarted the viewer.
 *
 * {@link org.icepdf.ri.common.fonts.FindFontsTask}
 * @since 6.1.3
 */
@SuppressWarnings("serial")
public class FontDialog extends EscapeJDialog implements ActionListener, WindowListener {

    // refresh rate of gui elements
    private static final int TIMER_REFRESH = 20;

    // pointer to document which will be searched
    private Document document;
    private SwingController controller;

    // list box to hold search results
    private JTree tree;
    private DefaultMutableTreeNode rootTreeNode;
    private DefaultTreeModel treeModel;
    // font look up start on creation, but ok button will kill the the process and close the dialog.
    private JButton okButton;
    // clear and rescan system for fonts and rewrite file.
    private JButton resetFontCacheButton;

    // task to complete in separate thread
    protected FindFontsTask findFontsTask;

    // status label for font search
    protected JLabel findMessage = new JLabel();

    // time class to manage gui updates
    protected Timer timer;

    // flag indicating if search is under way.
    private boolean isFindignFonts;

    // message bundle for internationalization
    private ResourceBundle messageBundle;
    private MessageFormat typeMessageForm;
    private MessageFormat encodingMessageForm;
    private MessageFormat actualTypeMessageForm;
    private MessageFormat actualFontMessageForm;

    // layouts constraint
    private GridBagConstraints constraints;

    /**
     * Create a new instance of SearchPanel.
     *
     * @param controller root SwingController
     */
    public FontDialog(Frame frame, SwingController controller, boolean isModal) {
        super(frame, isModal);
        setFocusable(true);
        this.controller = controller;
        this.messageBundle = this.controller.getMessageBundle();
        setGui();
        setDocument(controller.getDocument());
    }

    public void setDocument(Document doc) {
        // First have to stop any existing font searches,  this shouldn't happen...
        if (timer != null)
            timer.stop();
        if (findFontsTask != null) {
            findFontsTask.stop();
            while (findFontsTask.isCurrentlySearching()) {
                try {
                    Thread.sleep(50L);
                } catch (Exception e) {
                    // intentional
                }
            }
        }
        document = doc;
        if (rootTreeNode != null) {
            resetTree();
            // set title
            String docTitle = getDocumentTitle();
            rootTreeNode.setUserObject(docTitle);
            rootTreeNode.setAllowsChildren(true);
            tree.setRootVisible((docTitle != null));
        }
        // setup the new worker task.
        if (findMessage != null) {
            findMessage.setText("");
        }

        // start the task and the timer
        findFontsTask = new FindFontsTask(this, controller, messageBundle);
        findFontsTask.go();
        timer.start();
        isFindignFonts = true;
    }

    /**
     * Construct the GUI layout.
     */
    private void setGui() {

        typeMessageForm =
                new MessageFormat(messageBundle.getString("viewer.dialog.fonts.info.type.label"));
        encodingMessageForm =
                new MessageFormat(messageBundle.getString("viewer.dialog.fonts.info.encoding.label"));
        actualTypeMessageForm =
                new MessageFormat(messageBundle.getString("viewer.dialog.fonts.info.substitution.type.label"));
        actualFontMessageForm =
                new MessageFormat(messageBundle.getString("viewer.dialog.fonts.info.substitution.path.label"));

        setTitle(messageBundle.getString("viewer.dialog.fonts.title"));
        setResizable(true);

        addWindowListener(this);

        // build the supporting tree objects
        rootTreeNode = new DefaultMutableTreeNode();
        treeModel = new DefaultTreeModel(rootTreeNode);

        // build and customize the JTree
        tree = new JTree(treeModel);
        tree.setRootVisible(true);
        tree.setExpandsSelectedPaths(true);
        tree.setShowsRootHandles(true);
        tree.setScrollsOnExpand(true);
        tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);

        // set look and feel to match outline style, consider revising with font type icons.
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setOpenIcon(new ImageIcon(Images.get("page.gif")));
        renderer.setClosedIcon(new ImageIcon(Images.get("page.gif")));
        renderer.setLeafIcon(new ImageIcon(Images.get("page.gif")));
        tree.setCellRenderer(renderer);

        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setPreferredSize(new Dimension(150, 75));

        // setup refresh timer for the font scan progress.
        timer = new Timer(TIMER_REFRESH, new TimerListener());

        /**
         * Build search GUI
         */
        // content Panel
        JPanel fontPropertiesPanel = new JPanel(new GridBagLayout());
        fontPropertiesPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                messageBundle.getString("viewer.dialog.fonts.border.label"),
                TitledBorder.LEFT,
                TitledBorder.DEFAULT_POSITION));
        this.setLayout(new BorderLayout(15, 15));
        this.add(fontPropertiesPanel, BorderLayout.CENTER);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 1.0;
        constraints.weighty = 0;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(10, 15, 1, 15);

        // add the lit to scroll pane
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(10, 15, 10, 15);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        addGB(fontPropertiesPanel, scrollPane, 0, 1, 2, 1);

        // add find message
        constraints.insets = new Insets(2, 10, 2, 10);
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        findMessage.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        addGB(fontPropertiesPanel, findMessage, 0, 2, 2, 1);

        resetFontCacheButton = new JButton(messageBundle.getString("viewer.dialog.fonts.resetCache.label"));
        resetFontCacheButton.setToolTipText(messageBundle.getString("viewer.dialog.fonts.resetCache.tip"));
        resetFontCacheButton.addActionListener(this);
        constraints.insets = new Insets(2, 10, 2, 10);
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        addGB(fontPropertiesPanel, resetFontCacheButton, 0, 3, 1, 1);

        okButton = new JButton(messageBundle.getString("viewer.button.ok.label"));
        okButton.addActionListener(this);
        constraints.insets = new Insets(2, 10, 2, 10);
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.EAST;
        addGB(fontPropertiesPanel, okButton, 1, 4, 1, 1);

        setSize(640, 480);
        setLocationRelativeTo(getOwner());
    }

    /**
     * Adds a new node item to the treeModel.
     *
     * @param font font used to build node properties.
     */
    public void addFoundEntry(Font font) {
        DefaultMutableTreeNode fontNode = new DefaultMutableTreeNode(font.getBaseFont(), true);
        // add type sub node for type
        insertNode(font.getSubType(), typeMessageForm, fontNode);
        // add encoding.
        insertNode(font.getEncoding(), encodingMessageForm, fontNode);
        // add font substitution info.
        if (font.isFontSubstitution() && font.getFont() != null) {
            insertNode(font.getFont().getName(), actualTypeMessageForm, fontNode);
            insertNode(font.getFont().getSource(), actualFontMessageForm, fontNode);
        }
        addObject(rootTreeNode, fontNode);

        // expand the root node, we only do this once.
        tree.expandPath(new TreePath(rootTreeNode));
    }

    /**
     * Utility to aid in the creation of a new font properties node.
     * @param label label for node.
     * @param messageFormat message formatter
     * @param parent parent node.
     */
    private void insertNode(Object label, MessageFormat messageFormat, DefaultMutableTreeNode parent) {
        if (label != null) {
            Object[] messageArguments = {label.toString()};
            label = messageFormat.format(messageArguments);
            DefaultMutableTreeNode encodingNode = new DefaultMutableTreeNode(label, true);
            addObject(parent, encodingNode);
        }
    }

    /**
     * Utility for adding a tree node.
     *
     * @param parent    parent to add the node too.
     * @param childNode node to add.
     */
    private void addObject(DefaultMutableTreeNode parent,
                           DefaultMutableTreeNode childNode) {
        if (parent == null) {
            parent = rootTreeNode;
        }
        //It is key to invoke this on the TreeModel, and NOT DefaultMutableTreeNode
        treeModel.insertNodeInto(childNode, parent,
                parent.getChildCount());
    }

    // quick and dirty expand all.
    protected void expandAllNodes() {
        int rowCount = tree.getRowCount();
        int i = 0;
        while (i < rowCount) {
            tree.expandRow(i);
            i += 1;
            rowCount = tree.getRowCount();
        }
    }

    /**
     * Reset the tree, insuring it's empty
     */
    protected void resetTree() {
        tree.setSelectionPath(null);
        rootTreeNode.removeAllChildren();
        treeModel.nodeStructureChanged(rootTreeNode);
    }

    /**
     * Utility for getting the document title.
     *
     * @return document title, if non title then a simple search results
     * label is returned;
     */
    private String getDocumentTitle() {
        String documentTitle = null;
        if (document != null && document.getInfo() != null) {
            documentTitle = document.getInfo().getTitle();
        }
        if ((documentTitle == null) || (documentTitle.trim().length() == 0)) {
            return null;
        }
        return documentTitle;
    }

    /**
     * Insure the font search process is killed when the dialog is closed via the 'esc' key.
     */
    @Override
    public void dispose() {
        super.dispose();
        closeWindowOperations();
    }

    /**
     * Two main actions are handle here, search and clear search.
     *
     * @param event awt action event.
     */
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == okButton) {
            // clean up the timer and worker thread.
            closeWindowOperations();
            dispose();
        } else if (event.getSource() == resetFontCacheButton) {
            // reset the font properties cache.
            resetFontCacheButton.setEnabled(false);
            SwingWorker worker = new SwingWorker() {
                public Object construct() {
                    PropertiesManager properties = new PropertiesManager(
                            System.getProperties(),
                            ResourceBundle.getBundle(PropertiesManager.DEFAULT_MESSAGE_BUNDLE));
                    FontPropertiesManager fontPropertiesManager = new FontPropertiesManager(properties,
                            System.getProperties(), messageBundle);
                    fontPropertiesManager.clearProperties();
                    fontPropertiesManager.readDefaulFontPaths(null);
                    fontPropertiesManager.saveProperties();
                    resetFontCacheButton.setEnabled(true);

                    Runnable doSwingWork = new Runnable() {
                        public void run() {
                            resetFontCacheButton.setEnabled(true);
                        }
                    };
                    SwingUtilities.invokeLater(doSwingWork);
                    return null;
                }
            };
            worker.setThreadPriority(Thread.MIN_PRIORITY);
            worker.start();
        }
    }

    protected void closeWindowOperations() {
        // clean up the timer and worker thread.
        if (timer != null && timer.isRunning()) timer.stop();
        if (findFontsTask != null && findFontsTask.isCurrentlySearching()) findFontsTask.stop();
        setVisible(false);
    }


    /**
     * Gridbag constructor helper
     *
     * @param panel     parent adding component too.
     * @param component component to add to grid
     * @param x         row
     * @param y         col
     * @param rowSpan   rowspane of field
     * @param colSpan   colspane of field.
     */
    private void addGB(JPanel panel, Component component,
                       int x, int y,
                       int rowSpan, int colSpan) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = rowSpan;
        constraints.gridheight = colSpan;
        panel.add(component, constraints);
    }


    public void windowOpened(WindowEvent e) {

    }

    public void windowClosing(WindowEvent e) {
        closeWindowOperations();
    }

    public void windowClosed(WindowEvent e) {
        closeWindowOperations();
    }

    public void windowIconified(WindowEvent e) {

    }

    public void windowDeiconified(WindowEvent e) {

    }

    public void windowActivated(WindowEvent e) {

    }

    public void windowDeactivated(WindowEvent e) {

    }

    /**
     * The actionPerformed method in this class
     * is called each time the Timer "goes off".
     */
    class TimerListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            String s = findFontsTask.getMessage();
            if (s != null) {
                findMessage.setText(s);
            }
            // update the text when the search is completed
            if (findFontsTask.isDone() || !isFindignFonts) {
                // update search status, blank it.
                findMessage.setText("");
                timer.stop();
                findFontsTask.stop();
            }
        }
    }

    /**
     * An Entry objects represents the found pages
     */

    @SuppressWarnings("serial")
    class FontEntry extends DefaultMutableTreeNode {

        // The text to be displayed on the screen for this item.
        String title;


        /**
         * Creates a new instance of a FindEntry.
         *
         * @param title      display title
         * @param pageNumber page number where the hit(s) occured
         */
        FontEntry(String title, int pageNumber) {
            super();
            this.title = title;
            setUserObject(title);
        }

    }
}
