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
package org.icepdf.ri.common.views.annotations;

import org.icepdf.core.pobjects.Reference;
import org.icepdf.core.pobjects.annotations.Annotation;
import org.icepdf.core.pobjects.annotations.MarkupAnnotation;
import org.icepdf.core.pobjects.annotations.PopupAnnotation;
import org.icepdf.core.pobjects.annotations.TextAnnotation;
import org.icepdf.ri.common.tools.TextAnnotationHandler;
import org.icepdf.ri.common.views.*;
import org.icepdf.ri.util.PropertiesManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * The PopupAnnotationComponent encapsulates a PopupAnnotation objects.  It
 * also provides basic editing of the parent MarkupAnnotation's review state:
 * accepted, rejected, cancelled, completed, none. The component can also add
 * replyTo text annotations as well as delete comments.
 * <p/>
 * The PopupAnnotationComponent is slightly more complex then the other
 * annotations components.  Most annotations let the page pain the annotation
 * but in this case PopupAnnotationComponent paints itself along with controls
 * for editing, replying and deleting TextAnnotation comments.
 * appearance stream.
 *
 * @see org.icepdf.ri.common.utility.annotation.FreeTextAnnotationPanel
 * @since 5.0
 */
@SuppressWarnings("serial")
public class PopupAnnotationComponent extends AbstractAnnotationComponent
        implements TreeSelectionListener, ActionListener, DocumentListener {

    public static Color backgroundColor = new Color(252, 253, 227);
    public static Color borderColor = new Color(153, 153, 153);

    protected PopupAnnotation popupAnnotation;

    // layouts constraint
    private GridBagConstraints constraints;

    protected JPanel commentPanel;
    protected JTextArea textArea;
    protected JLabel creationLabel;
    protected JButton minimizeButton;
    protected JTree commentTree;
    protected JScrollPane commentTreeScrollPane;
    protected MarkupAnnotation selectedMarkupAnnotation;
    // add and remove commands
    protected JMenuItem replyMenuItem;
    protected JMenuItem deleteMenuItem;
    // status change commands.
    protected JMenuItem statusNoneMenuItem;
    protected JMenuItem statusAcceptedItem;
    protected JMenuItem statusCancelledMenuItem;
    protected JMenuItem statusCompletedMenuItem;
    protected JMenuItem statusRejectedMenuItem;
    // generic commands, open/minimize all
    protected JMenuItem openAllMenuItem;
    protected JMenuItem minimizeAllMenuItem;
    protected JPopupMenu contextMenu;

    public PopupAnnotationComponent(Annotation annotation, DocumentViewController documentViewController,
                                    AbstractPageViewComponent pageViewComponent, DocumentViewModel documentViewModel) {
        super(annotation, documentViewController, pageViewComponent, documentViewModel);

        isEditable = true;
        isRollover = false;
        isMovable = true;
        isResizable = true;
        isShowInvisibleBorder = false;

        if (annotation instanceof PopupAnnotation) {
            popupAnnotation = (PopupAnnotation) annotation;
            try {
                popupAnnotation.init();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.fine("Popup annotation component instance creation was interrupted");
            }
        }

        boolean isVisible = popupAnnotation.isOpen();
        setVisible(isVisible);

        buildGUI();
    }

    public void mouseMoved(MouseEvent me) {
        if (!(annotation.getFlagLocked() || annotation.getFlagReadOnly())) {
            ResizableBorder border = (ResizableBorder) getBorder();
            setCursor(Cursor.getPredefinedCursor(border.getCursor(me)));
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

        // setup visual effect when the mouse button is pressed or held down
        // inside the active area of the annotation.
        isMousePressed = true;

        if (isInteractiveAnnotationsEnabled &&
                !annotation.getFlagReadOnly()) {
            initiateMouseMoved(e);
        }
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void buildGUI() {

        List<Annotation> annotations = pageViewComponent.getPage().getAnnotations();
        MarkupAnnotation parentAnnotation = popupAnnotation.getParent();

        // check first if there are anny annotation that point to this one as
        // an IRT.  If there aren't any then the selectedAnnotation is the parent
        // other wise we need to build out
        DefaultMutableTreeNode root =
                new DefaultMutableTreeNode("Root");
        boolean isIRT = buildCommentTree(parentAnnotation, annotations, root);
        commentTree = new JTree(root);
        commentTree.setRootVisible(true);
        commentTree.setExpandsSelectedPaths(true);
        commentTree.setShowsRootHandles(true);
        commentTree.setScrollsOnExpand(true);
        commentTree.setRootVisible(false);
        commentTree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        commentTree.addTreeSelectionListener(this);
        // expand the tree
        refreshTree(commentTree);
        // set look and feel to match outline style
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);
        renderer.setLeafIcon(null);
        commentTree.setCellRenderer(renderer);
        commentTree.addMouseListener(new PopupTreeListener());
        commentTreeScrollPane = new JScrollPane(commentTree);
        // make sure the root node is selected by default.
        commentTree.setSelectionRow(0);

        // Set the
        selectedMarkupAnnotation = parentAnnotation;

        // minimize button
        minimizeButton = new JButton("  _  ");
        minimizeButton.addActionListener(this);
        minimizeButton.setBackground(backgroundColor);
        minimizeButton.setOpaque(true);
        minimizeButton.setContentAreaFilled(false);
        minimizeButton.setBorder(BorderFactory.createLineBorder(borderColor));
        minimizeButton.setBorderPainted(true);
        minimizeButton.addActionListener(this);

        // text area edited the selected annotation markup contents.
        String contents = popupAnnotation.getParent() != null ?
                popupAnnotation.getParent().getContents() : "";
        textArea = new JTextArea(contents != null ? contents : "");
        textArea.setFont(new JLabel().getFont());
        textArea.setBorder(BorderFactory.createLineBorder(borderColor));

        textArea.setLineWrap(true);
        textArea.getDocument().addDocumentListener(this);

        // creation date
        creationLabel = new JLabel();
        if (selectedMarkupAnnotation != null &&
                selectedMarkupAnnotation.getCreationDate() != null) {
            creationLabel.setText(
                    selectedMarkupAnnotation.getCreationDate().toString());
        }

        // main layout panel
        GridBagLayout layout = new GridBagLayout();
        commentPanel = new JPanel(layout);
        commentPanel.setBackground(backgroundColor);
        commentPanel.setBorder(BorderFactory.createLineBorder(borderColor));
        this.setLayout(new BorderLayout());
        this.add(commentPanel);

        /**
         * Build search GUI
         */
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 1.0;
        constraints.weighty = 0;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(1, 5, 1, 5);

        // currently selected title
        constraints.fill = GridBagConstraints.EAST;
        constraints.weightx = 0;
        String title = selectedMarkupAnnotation != null ?
                selectedMarkupAnnotation.getTitleText() != null ?
                        selectedMarkupAnnotation.getTitleText() : "" : "";
        addGB(commentPanel, new JLabel(title), 0, 0, 1, 1);

        // add minimize button
        constraints.fill = GridBagConstraints.REMAINDER;
        constraints.weightx = 0;
        addGB(commentPanel, minimizeButton, 2, 0, 1, 1);

        // add comment tree if there are any IRT's
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(1, 5, 1, 5);
        constraints.weightx = 1.0;
        constraints.weighty = .6;
        commentTreeScrollPane.setVisible(isIRT);
        addGB(commentPanel, commentTreeScrollPane, 0, 1, 3, 1);

        // creation date of selected comment
        constraints.insets = new Insets(1, 5, 1, 5);
        constraints.weightx = 1.0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.EAST;
        addGB(commentPanel,
                creationLabel,
                0, 2, 1, 1);

        // add the text area
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(1, 5, 5, 5);
        constraints.weightx = 1.0;
        constraints.weighty = .4;
        addGB(commentPanel, textArea, 0, 3, 3, 1);

        // command test
        buildContextMenu();
    }

    private void refreshTree(JTree tree) {
        ((DefaultTreeModel) (tree.getModel())).reload();
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    public void buildContextMenu() {

        ResourceBundle messages =
                documentViewController.getParentController().getMessageBundle();
        PropertiesManager propertiesManager = documentViewController.getParentController().getPropertiesManager();

        //Create the popup menu.
        contextMenu = new JPopupMenu();

        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_ANNOTATION_MARKUP_REPLY_TO)) {
            replyMenuItem = new JMenuItem(
                    messages.getString("viewer.annotation.popup.reply.label"));
            // build out reply and delete
            replyMenuItem.addActionListener(this);
            contextMenu.add(replyMenuItem);
        }
        deleteMenuItem = new JMenuItem(
                messages.getString("viewer.annotation.popup.delete.label"));
        // status change commands.
        statusNoneMenuItem = new JMenuItem(
                messages.getString("viewer.annotation.popup.status.none.label"));
        statusAcceptedItem = new JMenuItem(
                messages.getString("viewer.annotation.popup.status.accepted.label"));
        statusCancelledMenuItem = new JMenuItem(
                messages.getString("viewer.annotation.popup.status.cancelled.label"));
        statusCompletedMenuItem = new JMenuItem(
                messages.getString("viewer.annotation.popup.status.completed.label"));
        statusRejectedMenuItem = new JMenuItem(
                messages.getString("viewer.annotation.popup.status.rejected.label"));
        // generic commands, open/minimize all
        openAllMenuItem = new JMenuItem(
                messages.getString("viewer.annotation.popup.openAll.label"));
        minimizeAllMenuItem = new JMenuItem(
                messages.getString("viewer.annotation.popup.minimizeAll.label"));

        // build out delete
        deleteMenuItem.addActionListener(this);
        contextMenu.add(deleteMenuItem);
        contextMenu.addSeparator();

        if (PropertiesManager.checkAndStoreBooleanProperty(propertiesManager,
                PropertiesManager.PROPERTY_SHOW_ANNOTATION_MARKUP_SET_STATUS)) {
            // addition of set status menu
            JMenu submenu = new JMenu(
                    messages.getString("viewer.annotation.popup.status.label"));
//        ButtonGroup group = new ButtonGroup();
            statusNoneMenuItem.addActionListener(this);
//        group.add(statusNoneMenuItem);
            submenu.add(statusNoneMenuItem);
            statusAcceptedItem.addActionListener(this);
//        group.add(statusAcceptedItem);
            submenu.add(statusAcceptedItem);
            statusCancelledMenuItem.addActionListener(this);
//        group.add(statusCancelledMenuItem);
            submenu.add(statusCancelledMenuItem);
            statusCompletedMenuItem.addActionListener(this);
//        group.add(statusCompletedMenuItem);
            submenu.add(statusCompletedMenuItem);
            statusRejectedMenuItem.addActionListener(this);
//        group.add(statusRejectedMenuItem);
            submenu.add(statusRejectedMenuItem);
            contextMenu.add(submenu);
            contextMenu.addSeparator();
        }

        // generic commands, open/minimize all
        openAllMenuItem.addActionListener(this);
        contextMenu.add(openAllMenuItem);
        minimizeAllMenuItem.addActionListener(this);
        contextMenu.add(minimizeAllMenuItem);

        // Add listener to components that can bring up popup menus.
        MouseListener popupListener = new PopupListener();
        textArea.addMouseListener(popupListener);
        commentPanel.addMouseListener(popupListener);
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == null)
            return;
        // hide the window on minimize
        if (source == minimizeButton) {
            this.setVisible(false);
            popupAnnotation.setOpen(false);
        } else if (source == replyMenuItem) {

            // setup title message
            Object[] argument = new Object[]{selectedMarkupAnnotation.getTitleText()};
            MessageFormat formatter = new MessageFormat(
                    messageBundle.getString("viewer.annotation.popup.replyTo.label"));
            String annotationTitle = formatter.format(argument);

            createNewTextAnnotation(
                    annotationTitle,
                    "",
                    TextAnnotation.STATE_MODEL_REVIEW,
                    TextAnnotation.STATE_REVIEW_NONE);
        } else if (source == deleteMenuItem) {
            // remove the annotation
            AnnotationComponent annotationComponent = findAnnotationComponent(selectedMarkupAnnotation);
            documentViewController.deleteAnnotation(annotationComponent);
            // remove the annotations popup
            annotationComponent = findAnnotationComponent(selectedMarkupAnnotation.getPopupAnnotation());
            documentViewController.deleteAnnotation(annotationComponent);

            // check if any annotations have an IRT reference and delete
            // the markup component chain
            removeMarkupInReplyTo(selectedMarkupAnnotation.getPObjectReference());

            // rebuild the tree, which is easier then pruning at this point
            List<Annotation> annotations = pageViewComponent.getPage().getAnnotations();
            MarkupAnnotation parentAnnotation = popupAnnotation.getParent();

            // check first if there are anny annotation that point to this one as
            // an IRT.  If there aren't any then the selectedAnnotation is the parent
            // other wise we need to build out
            DefaultMutableTreeNode root =
                    new DefaultMutableTreeNode("Root");
            boolean isIRT = buildCommentTree(parentAnnotation, annotations, root);
            commentTree.removeTreeSelectionListener(this);
            ((DefaultTreeModel) (commentTree.getModel())).setRoot(root);
            commentTree.addTreeSelectionListener(this);
            // reload the tree model
            refreshTree(commentTree);
            if (!isIRT) {
                commentTreeScrollPane.setVisible(false);
            }
            commentPanel.revalidate();

        } else if (source == statusNoneMenuItem) {
            // setup title message
            Object[] argument = new Object[]{selectedMarkupAnnotation.getTitleText()};
            MessageFormat formatter = new MessageFormat(
                    messageBundle.getString("viewer.annotation.popup.status.none.title"));
            String title = formatter.format(argument);
            // setup content message
            argument = new Object[]{selectedMarkupAnnotation.getTitleText()};
            formatter = new MessageFormat(
                    messageBundle.getString("viewer.annotation.popup.status.none.msg"));
            String content = formatter.format(argument);
            createNewTextAnnotation(title, content,
                    TextAnnotation.STATE_MODEL_REVIEW,
                    TextAnnotation.STATE_REVIEW_NONE);
        } else if (source == statusAcceptedItem) {
            // setup title message
            Object[] argument = new Object[]{selectedMarkupAnnotation.getTitleText()};
            MessageFormat formatter = new MessageFormat(
                    messageBundle.getString("viewer.annotation.popup.status.accepted.title"));
            String title = formatter.format(argument);
            // setup content message
            argument = new Object[]{selectedMarkupAnnotation.getTitleText()};
            formatter = new MessageFormat(
                    messageBundle.getString("viewer.annotation.popup.status.accepted.msg"));
            String content = formatter.format(argument);
            createNewTextAnnotation(title, content,
                    TextAnnotation.STATE_MODEL_REVIEW,
                    TextAnnotation.STATE_REVIEW_NONE);
        } else if (source == statusCancelledMenuItem) {
            // setup title message
            Object[] argument = new Object[]{selectedMarkupAnnotation.getTitleText()};
            MessageFormat formatter = new MessageFormat(
                    messageBundle.getString("viewer.annotation.popup.status.cancelled.title"));
            String title = formatter.format(argument);
            // setup content message
            argument = new Object[]{selectedMarkupAnnotation.getTitleText()};
            formatter = new MessageFormat(
                    messageBundle.getString("viewer.annotation.popup.status.cancelled.msg"));
            String content = formatter.format(argument);
            createNewTextAnnotation(title, content,
                    TextAnnotation.STATE_MODEL_REVIEW,
                    TextAnnotation.STATE_REVIEW_NONE);
        } else if (source == statusCompletedMenuItem) {
            // setup title message
            Object[] argument = new Object[]{selectedMarkupAnnotation.getTitleText()};
            MessageFormat formatter = new MessageFormat(
                    messageBundle.getString("viewer.annotation.popup.status.completed.title"));
            String title = formatter.format(argument);
            // setup content message
            argument = new Object[]{selectedMarkupAnnotation.getTitleText()};
            formatter = new MessageFormat(
                    messageBundle.getString("viewer.annotation.popup.status.completed.msg"));
            String content = formatter.format(argument);
            createNewTextAnnotation(title, content,
                    TextAnnotation.STATE_MODEL_REVIEW,
                    TextAnnotation.STATE_REVIEW_NONE);
        } else if (source == statusRejectedMenuItem) {
            // setup title message
            Object[] argument = new Object[]{selectedMarkupAnnotation.getTitleText()};
            MessageFormat formatter = new MessageFormat(
                    messageBundle.getString("viewer.annotation.popup.status.rejected.title"));
            String title = formatter.format(argument);
            // setup content message
            argument = new Object[]{selectedMarkupAnnotation.getTitleText()};
            formatter = new MessageFormat(
                    messageBundle.getString("viewer.annotation.popup.status.rejected.msg"));
            String content = formatter.format(argument);
            createNewTextAnnotation(title, content,
                    TextAnnotation.STATE_MODEL_REVIEW,
                    TextAnnotation.STATE_REVIEW_NONE);
        } else if (source == openAllMenuItem) {
            showHidePopupAnnotations(true);

        } else if (source == minimizeAllMenuItem) {
            showHidePopupAnnotations(false);
        }
    }

    private void showHidePopupAnnotations(boolean visible) {
        ArrayList<AbstractAnnotationComponent> annotationComponents =
                pageViewComponent.getAnnotationComponents();
        for (AnnotationComponent annotationComponent : annotationComponents) {
            if (annotationComponent instanceof PopupAnnotationComponent) {
                PopupAnnotationComponent popupAnnotationComponent = (PopupAnnotationComponent) annotationComponent;
                if (popupAnnotationComponent.getAnnotation() != null) {
                    PopupAnnotation popupAnnotation = (PopupAnnotation)
                            popupAnnotationComponent.getAnnotation();
                    if (popupAnnotation.getParent() != null &&
                            popupAnnotation.getParent().getInReplyToAnnotation() == null) {
                        popupAnnotationComponent.setVisible(visible);
                    }
                }
            }
        }
    }

    private void createNewTextAnnotation(String title, String content,
                                         String stateModel, String state) {
        // on reply we need to create a new textAnnotation/popup combo
        // and setup the IRT references for display
        TextAnnotation markupAnnotation =
                TextAnnotationHandler.createTextAnnotation(
                        documentViewModel.getDocument().getPageTree().getLibrary(),
                        selectedMarkupAnnotation.getUserSpaceRectangle().getBounds(),
                        getPageTransform());
        markupAnnotation.setTitleText(title);
        markupAnnotation.setContents(content);
        markupAnnotation.setState(state);
        markupAnnotation.setStateModel(stateModel);
        markupAnnotation.setInReplyToAnnotation(selectedMarkupAnnotation);
        addAnnotationComponent(markupAnnotation);

        // create the new text and popup annotations
        PopupAnnotation popupAnnotation =
                TextAnnotationHandler.createPopupAnnotation(
                        documentViewModel.getDocument().getPageTree().getLibrary(),
                        this.popupAnnotation.getUserSpaceRectangle().getBounds(),
                        markupAnnotation, getPageTransform());
        popupAnnotation.setOpen(false);
        addAnnotationComponent(popupAnnotation);

        // finally add the node as child to the selected node
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                commentTree.getLastSelectedPathComponent();

        DefaultMutableTreeNode replyToNode =
                new DefaultMutableTreeNode(markupAnnotation);
        if (node == null) {
            node = ((DefaultMutableTreeNode) commentTree.getModel().getRoot()).getFirstLeaf();
        }
        node.insert(replyToNode, node.getChildCount());

        commentTree.expandRow(replyToNode.getDepth() - 1);
        selectedMarkupAnnotation = markupAnnotation;

        // reload the tree model
        refreshTree(commentTree);

        // finally check the view and make sure the treePanel is visible.
        commentTreeScrollPane.setVisible(true);
        commentPanel.revalidate();
    }

    public void insertUpdate(DocumentEvent e) {
        updateContent(e);
    }

    public void removeUpdate(DocumentEvent e) {
        updateContent(e);
    }

    public void changedUpdate(DocumentEvent e) {
        updateContent(e);
    }

    private void updateContent(DocumentEvent e) {
        // get the next text and save it to the selected markup annotation.
        Document document = e.getDocument();
        try {
            if (document.getLength() > 0) {
                selectedMarkupAnnotation.setContents(
                        document.getText(0, document.getLength()));
                // add them to the container, using absolute positioning.
                if (documentViewController.getAnnotationCallback() != null) {
                    AnnotationCallback annotationCallback =
                            documentViewController.getAnnotationCallback();
                    AnnotationComponent annotationComponent = findAnnotationComponent(popupAnnotation.getParent());
                    annotationCallback.updateAnnotation(annotationComponent);
                }
            }
        } catch (BadLocationException ex) {
            logger.log(Level.FINE, "Error updating markup annotation content", ex);
        }
    }

    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                commentTree.getLastSelectedPathComponent();

        // Nothing is selected.
        if (node == null)
            return;

        Object userObject = node.getUserObject();
        if (userObject instanceof MarkupAnnotation) {
            selectedMarkupAnnotation = (MarkupAnnotation) userObject;
            if (textArea != null) {
                textArea.getDocument().removeDocumentListener(this);
                textArea.setText(selectedMarkupAnnotation.getContents());
                textArea.getDocument().addDocumentListener(this);
            }
            if (creationLabel != null) {
                creationLabel.setText(selectedMarkupAnnotation.getCreationDate().toString());
            }
        }
    }

    public boolean isActive() {
        return false;
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

    private boolean buildCommentTree(MarkupAnnotation parentAnnotation,
                                     List<Annotation> annotations,
                                     DefaultMutableTreeNode root) {
        boolean foundIRT = checkForIRT(parentAnnotation, annotations);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(parentAnnotation);
        root.add(node);
        if (!foundIRT) {
            // simple test add a new node for the parent annotation.
            return false;
        } else {
            // find every IRT and add it to the tree.
            buildRecursiveCommentTree(node, annotations);
            return true;
        }

    }

    private void buildRecursiveCommentTree(DefaultMutableTreeNode root,
                                           List<Annotation> annotations) {
        MarkupAnnotation currentMarkup = (MarkupAnnotation) root.getUserObject();
        Reference reference = currentMarkup.getPObjectReference();
        for (Annotation annotation : annotations) {
            if (annotation != null && annotation instanceof MarkupAnnotation) {
                MarkupAnnotation markupAnnotation = (MarkupAnnotation) annotation;
                MarkupAnnotation inReplyToAnnotation =
                        markupAnnotation.getInReplyToAnnotation();
                if (inReplyToAnnotation != null &&
                        inReplyToAnnotation.getPObjectReference().equals(reference)) {
                    // found one no were to attach it to.
                    root.add(new DefaultMutableTreeNode(markupAnnotation));
                }
            }
        }
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            buildRecursiveCommentTree((DefaultMutableTreeNode) root.getChildAt(i),
                    annotations);
        }

    }

    private void removeMarkupInReplyTo(Reference reference) {
        if (reference != null) {
            ArrayList<AbstractAnnotationComponent> annotationComponents =
                    pageViewComponent.getAnnotationComponents();
            MarkupAnnotationComponent markupAnnotationComponent;
            MarkupAnnotation markupAnnotation;
            AnnotationComponent annotationComponent;
            for (int i = 0; i < annotationComponents.size(); i++) {
                annotationComponent = annotationComponents.get(i);
                if (annotationComponent instanceof MarkupAnnotationComponent) {
                    markupAnnotationComponent = (MarkupAnnotationComponent) annotationComponent;
                    markupAnnotation = (MarkupAnnotation)
                            markupAnnotationComponent.getAnnotation();
                    if (markupAnnotation.getInReplyToAnnotation() != null &&
                            markupAnnotation.getInReplyToAnnotation()
                                    .getPObjectReference().equals(reference)) {
                        // recursive check if there are any object that refer
                        // to this IRT annotation being deleted.
                        removeMarkupInReplyTo(markupAnnotation.getPObjectReference());
                        documentViewController.deleteAnnotation(markupAnnotationComponent);
                    }
                }
            }
        }
    }

    private boolean checkForIRT(MarkupAnnotation parentAnnotation,
                                List<Annotation> annotations) {
        if (parentAnnotation != null) {
            Reference reference = parentAnnotation.getPObjectReference();
            for (Annotation annotation : annotations) {
                if (annotation instanceof MarkupAnnotation) {
                    MarkupAnnotation markupAnnotation = (MarkupAnnotation) annotation;
                    MarkupAnnotation inReplyToAnnotation =
                            markupAnnotation.getInReplyToAnnotation();
                    if (inReplyToAnnotation != null &&
                            inReplyToAnnotation.getPObjectReference().equals(reference))
                        return true;
                }
            }
        }
        return false;
    }

    private void addAnnotationComponent(Annotation annotation) {
        // draw them off screen
        Rectangle bBox = new Rectangle(-20, -20, 20, 20);
        // create the annotation object.
        AbstractAnnotationComponent comp =
                AnnotationComponentFactory.buildAnnotationComponent(
                        annotation,
                        documentViewController,
                        pageViewComponent, documentViewModel);
        // set the bounds and refresh the userSpace rectangle
        comp.setBounds(bBox);
        // resets user space rectangle to match bbox converted to page space
        comp.refreshAnnotationRect();

        // add them to the container, using absolute positioning.
        if (documentViewController.getAnnotationCallback() != null) {
            AnnotationCallback annotationCallback =
                    documentViewController.getAnnotationCallback();
            annotationCallback.newAnnotation(pageViewComponent, comp);
        }
    }

    private AnnotationComponent findAnnotationComponent(Annotation annotation) {
        ArrayList<AbstractAnnotationComponent> annotationComponents =
                pageViewComponent.getAnnotationComponents();
        Reference compReference;
        Reference annotationReference = annotation.getPObjectReference();
        for (AnnotationComponent annotationComponent : annotationComponents) {
            compReference = annotationComponent.getAnnotation().getPObjectReference();
            // find the component and toggle it's visibility.
            if (compReference != null && compReference.equals(annotationReference)) {
                return annotationComponent;
            }
        }
        return null;
    }

    @Override
    public void paintComponent(Graphics g) {

    }

    @Override
    public void resetAppearanceShapes() {

    }

    class PopupTreeListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                int row = commentTree.getClosestRowForLocation(e.getX(), e.getY());
                commentTree.setSelectionRow(row);
                contextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    class PopupListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                contextMenu.show(e.getComponent(),
                        e.getX(), e.getY());
            }
        }
    }
}