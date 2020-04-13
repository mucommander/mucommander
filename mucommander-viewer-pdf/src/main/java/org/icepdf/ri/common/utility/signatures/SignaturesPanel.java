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
package org.icepdf.ri.common.utility.signatures;

import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Reference;
import org.icepdf.core.pobjects.acroform.InteractiveForm;
import org.icepdf.core.pobjects.acroform.SignatureDictionary;
import org.icepdf.core.pobjects.acroform.SignatureFieldDictionary;
import org.icepdf.core.pobjects.acroform.signature.SignatureValidator;
import org.icepdf.core.pobjects.acroform.signature.exceptions.SignatureIntegrityException;
import org.icepdf.core.pobjects.annotations.SignatureWidgetAnnotation;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.common.views.DocumentViewModel;
import org.icepdf.ri.common.views.annotations.signatures.CertificatePropertiesDialog;
import org.icepdf.ri.common.views.annotations.signatures.SignaturePropertiesDialog;
import org.icepdf.ri.common.views.annotations.signatures.SignatureValidationDialog;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The SignaturesPanel lists all the digital signatures in a document as well as the signature fields components
 * that are just placeholders.  SwingWorkers are used to
 */
public class SignaturesPanel extends JPanel {

    private static final Logger logger =
            Logger.getLogger(SignaturesPanel.class.toString());

    protected DocumentViewController documentViewController;

    protected Document currentDocument;

    private SwingController controller;

    protected JTree signatureTree;
    private DefaultMutableTreeNode rootTreeNode;
    private DefaultTreeModel treeModel;
    // show progress of the signature validation process.
    protected JProgressBar progressBar;
    // task to complete in separate thread
    protected SigVerificationTask sigVerificationTask;
    // status label for validation progress reporting.
    protected JLabel progressLabel;

    // time class to manage gui updates
    protected Timer timer;
    // refresh rate of gui elements
    private static final int REFRESH_TIME = 100;

    protected JScrollPane scrollPane;
    private GridBagConstraints constraints;
    protected DocumentViewModel documentViewModel;

    // message bundle for internationalization
    protected ResourceBundle messageBundle;
    protected NodeSelectionListener nodeSelectionListener;

    public SignaturesPanel(SwingController controller) {
        super(true);
        setFocusable(true);
        this.controller = controller;
        this.messageBundle = this.controller.getMessageBundle();
        buildUI();
    }

    private void buildUI() {
        rootTreeNode = new DefaultMutableTreeNode(messageBundle.getString("viewer.utilityPane.signatures.tab.title"));
        rootTreeNode.setAllowsChildren(true);
        treeModel = new DefaultTreeModel(rootTreeNode);
        signatureTree = new SignaturesTree(treeModel);
        signatureTree.setRootVisible(false);
        signatureTree.setExpandsSelectedPaths(true);
        signatureTree.setShowsRootHandles(true);
        signatureTree.setScrollsOnExpand(true);
        nodeSelectionListener = new NodeSelectionListener(signatureTree);
        signatureTree.addMouseListener(nodeSelectionListener);

        this.setLayout(new BorderLayout());
        scrollPane = new JScrollPane(signatureTree,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(20);

        // setup validation progress bar and status label
        progressBar = new JProgressBar(0, 1);
        progressBar.setValue(0);
        progressBar.setVisible(false);
        progressLabel = new JLabel("");
        progressLabel.setVisible(false);
        timer = new Timer(REFRESH_TIME, new TimerListener());

        /**
         * Build signature tree GUI
         */
        GridBagLayout layout = new GridBagLayout();
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 1.0;
        constraints.weighty = 0;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(10, 5, 1, 5);

        JPanel signaturePanel = new JPanel(layout);
        this.add(signaturePanel);

        // add the lit to scroll pane
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(1, 5, 1, 5);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        addGB(signaturePanel, scrollPane, 0, 0, 1, 1);

        // add progress label
        constraints.insets = new Insets(1, 5, 1, 5);
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.EAST;
        progressLabel.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        addGB(signaturePanel, progressLabel, 0, 1, 1, 1);

        // add progress
        constraints.insets = new Insets(5, 5, 1, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        addGB(signaturePanel, progressBar, 0, 2, 1, 1);
    }

    /**
     * Called from the worker task to add a new signature node to the tree.  It is assumed that
     * this call is made from the AWT thread.
     *
     * @param signatureWidgetAnnotation annotation to add to tree.
     */
    public void addSignature(SignatureWidgetAnnotation signatureWidgetAnnotation) {
        if (signatureWidgetAnnotation != null) {
            SignatureDictionary signatureDictionary = signatureWidgetAnnotation.getSignatureDictionary();
            // filter any unsigned signer fields.
            if (signatureDictionary.getEntries().size() > 0) {
                SignatureTreeNode tmp = new SignatureTreeNode(signatureWidgetAnnotation, messageBundle);
                tmp.refreshSignerNode();
                tmp.setAllowsChildren(true);
                // insert and expand the root node.
                treeModel.insertNodeInto(tmp, rootTreeNode, rootTreeNode.getChildCount());
                signatureTree.expandPath(new TreePath(rootTreeNode));
            }
        }
    }

    /**
     * Called from the worker task to add a new unsigned signature node to the tree.  It is assumed that
     * this call is made from the AWT thread.
     *
     * @param signatures list off unsigned signatures annotation to add to tree.
     */
    public void addUnsignedSignatures(ArrayList<SignatureWidgetAnnotation> signatures) {
        DefaultMutableTreeNode unsignedFieldNode = new DefaultMutableTreeNode(
                messageBundle.getString("viewer.utilityPane.signatures.tab.certTree.unsigned.label"));
        treeModel.insertNodeInto(unsignedFieldNode, rootTreeNode,
                rootTreeNode.getChildCount());
        for (SignatureWidgetAnnotation signature : signatures) {
            SignatureDictionary signatureDictionary = signature.getSignatureDictionary();
            // filter for only unsigned signer fields.
            if (signatureDictionary.getEntries().size() == 0) {
                DefaultMutableTreeNode field =
                        new DefaultMutableTreeNode(signature.getFieldDictionary().getPartialFieldName());
                field.setAllowsChildren(false);
                unsignedFieldNode.add(field);
            }
        }
        signatureTree.expandPath(new TreePath(rootTreeNode));
        signatureTree.expandPath(new TreePath(unsignedFieldNode));
        revalidate();
    }

    /**
     * Updates the data fields on a signature tree node after verification has taken place.  It is assumed
     * this method is always called from the AWT thread.
     *
     * @param signatureWidgetAnnotation annotation to update
     * @param signatureTreeNode         node that will be updated.
     */
    public void updateSignature(SignatureWidgetAnnotation signatureWidgetAnnotation,
                                SignatureTreeNode signatureTreeNode) {
        if (signatureWidgetAnnotation != null) {
            try {
                TreePath treePath = new TreePath(signatureTreeNode.getPath());
                boolean isExpanded = signatureTree.isExpanded(treePath);
                signatureTreeNode.validateSignatureNode();
                signatureTreeNode.refreshSignerNode();
                treeModel.reload();
                if (isExpanded) {
                    signatureTree.expandPath(new TreePath(signatureTreeNode.getPath()));
                }
            } catch (SignatureIntegrityException e) {
                logger.log(Level.WARNING, "Could not build signature node.", e);
            }
        }
    }

    /**
     * Shows the signatureValidationDialog for the given SignatureWidgetAnnotation.  This method should
     * be called from the AWT thread.
     *
     * @param signatureWidgetAnnotation annotation to show the properties of.
     */
    public void showSignatureValidationDialog(SignatureWidgetAnnotation signatureWidgetAnnotation) {
        if (signatureWidgetAnnotation != null) {
            // show the dialog
            SignatureFieldDictionary fieldDictionary = signatureWidgetAnnotation.getFieldDictionary();
            if (fieldDictionary != null) {
                SignatureValidator signatureValidator = signatureWidgetAnnotation.getSignatureValidator();
                if (signatureValidator != null) {
                    new SignatureValidationDialog(controller.getViewerFrame(),
                            messageBundle, signatureWidgetAnnotation, signatureValidator).setVisible(true);
                }
            }

        }
    }

    /**
     * Set the current document instance and starts the validation process of any found signature annotations.
     *
     * @param document current document, can be null.
     */
    public void setDocument(Document document) {

        // First have to stop any existing validation processes.
        if (timer != null) {
            timer.stop();
        }
        if (sigVerificationTask != null) {
            sigVerificationTask.stop();
            while (sigVerificationTask.isCurrentlyVerifying()) {
                try {
                    Thread.sleep(50L);
                } catch (Exception e) {
                    // intentional
                }
            }
        }

        this.currentDocument = document;
        documentViewController = controller.getDocumentViewController();
        documentViewModel = documentViewController.getDocumentViewModel();

        // clear the previously loaded signature tree.
        if (rootTreeNode != null) {
            resetTree();
            // set title
            rootTreeNode.setAllowsChildren(true);
            signatureTree.setRootVisible(false);
        }

        if (this.currentDocument != null &&
                currentDocument.getCatalog().getInteractiveForm() != null) {
            InteractiveForm interactiveForm = currentDocument.getCatalog().getInteractiveForm();
            final ArrayList<SignatureWidgetAnnotation> signatures = interactiveForm.getSignatureFields();
            // build out the tree
            if (signatures.size() > 0) {
                if (!timer.isRunning()) {
                    // show the progress components.
                    progressLabel.setVisible(true);
                    progressBar.setVisible(true);
                    // clean the previous results and repaint the tree
                    resetTree();
                    // start a new verification task
                    sigVerificationTask = new SigVerificationTask(this, controller, messageBundle);
                    progressBar.setMaximum(sigVerificationTask.getLengthOfTask());
                    // start the task and the timer
                    sigVerificationTask.verifyAllSignatures();
                    timer.start();
                }
            }
        }
    }

    /**
     * Reset the tree for a new document or a new validation.
     */
    protected void resetTree() {
        signatureTree.setSelectionPath(null);
        rootTreeNode.removeAllChildren();
        treeModel.nodeStructureChanged(rootTreeNode);
    }

    /**
     * Component clean on on document window tear down.
     */
    public void dispose() {
        this.removeAll();
        sigVerificationTask = null;
        controller = null;
        documentViewModel = null;
        currentDocument = null;
        timer = null;
    }

    /**
     * NodeSelectionListener handles the root node context menu creation display and command execution.
     */
    class NodeSelectionListener extends MouseAdapter {
        protected JTree tree;
        protected JPopupMenu contextMenu;
        private SignatureTreeNode signatureTreeNode;

        NodeSelectionListener(JTree tree) {
            this.tree = tree;

            // add context menu for quick access to validating and signature properties.
            contextMenu = new JPopupMenu();
            JMenuItem validateMenu = new JMenuItem(messageBundle.getString(
                    "viewer.annotation.signature.menu.validateSignature.label"));
            validateMenu.addActionListener(new validationActionListener());
            contextMenu.add(validateMenu);
            contextMenu.add(new JPopupMenu.Separator());
            JMenuItem signaturePropertiesMenu = new JMenuItem(messageBundle.getString(
                    "viewer.annotation.signature.menu.signatureProperties.label"));
            signaturePropertiesMenu.addActionListener(new SignaturesPropertiesActionListener(tree));
            contextMenu.add(signaturePropertiesMenu);
            contextMenu.add(new JPopupMenu.Separator());
            JMenuItem signaturePageNavigationMenu = new JMenuItem(messageBundle.getString(
                    "viewer.annotation.signature.menu.signaturePageNavigation.label"));
            signaturePageNavigationMenu.addActionListener(new SignaturesPageNavigationListener());
            contextMenu.add(signaturePageNavigationMenu);
        }

        public void mouseClicked(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int row = tree.getRowForLocation(x, y);
            TreePath path = tree.getPathForRow(row);
            if (path != null) {
                Object node = path.getLastPathComponent();
                if (node instanceof SignatureCertTreeNode) {
                    // someone clicked on the show certificate node.
                    // create new dialog to show certificate properties.
                    SignatureCertTreeNode selectedSignatureCert = (SignatureCertTreeNode) node;
                    new CertificatePropertiesDialog(controller.getViewerFrame(), messageBundle,
                            selectedSignatureCert.getCertificateChain())
                            .setVisible(true);
                } else if (node instanceof SignatureTreeNode &&
                        (e.getButton() == MouseEvent.BUTTON3 || e.getButton() == MouseEvent.BUTTON2)) {
                    signatureTreeNode = (SignatureTreeNode) node;
                    // show context menu.
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }

            }
        }

        public SignatureTreeNode getSignatureTreeNode() {
            return signatureTreeNode;
        }
    }

    /**
     * GridBag constructor helper
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

    /**
     * Shows the SignatureValidationDialog dialog.
     */
    class validationActionListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            if (!sigVerificationTask.isCurrentlyVerifying()) {
                // validate the signature and show the summary dialog.
                final SignatureTreeNode signatureTreeNode = nodeSelectionListener.getSignatureTreeNode();
                SignatureWidgetAnnotation signatureWidgetAnnotation = signatureTreeNode.getOutlineItem();
                if (!timer.isRunning()) {
                    // update gui components
                    progressLabel.setVisible(true);
                    progressBar.setVisible(true);
                    progressBar.setMaximum(1);
                    // start the task and the timer
                    sigVerificationTask.verifySignature(signatureWidgetAnnotation, signatureTreeNode);
                    timer.start();
                }
            }
        }
    }

    /**
     * Navigates to the page the selected signature annotation exists on.
     */
    class SignaturesPageNavigationListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (nodeSelectionListener.getSignatureTreeNode() != null) {
                final SignatureTreeNode signatureTreeNode = nodeSelectionListener.getSignatureTreeNode();
                SignatureWidgetAnnotation signatureWidgetAnnotation = signatureTreeNode.getOutlineItem();
                // turn out the parent is seldom used correctly and generally just points to page zero.
                // so we should
                Document document = controller.getDocument();
                int pages = controller.getPageTree().getNumberOfPages();
                boolean found = false;
                for (int i = 0; i < pages && !found; i++) {
                    // check is page's annotation array for a matching reference.
                    ArrayList<Reference> annotationReferences = document.getPageTree().getPage(i).getAnnotationReferences();
                    if (annotationReferences != null) {
                        for (Reference reference : annotationReferences) {
                            if (reference.equals(signatureWidgetAnnotation.getPObjectReference())) {
                                controller.showPage(i);
                                found = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Command object for displaying the SignaturePropertiesDialog.
     */
    class SignaturesPropertiesActionListener implements ActionListener {
        protected JTree tree;

        public SignaturesPropertiesActionListener(JTree tree) {
            this.tree = tree;
        }

        public void actionPerformed(ActionEvent e) {
            if (nodeSelectionListener.getSignatureTreeNode() != null) {
                final SignatureTreeNode signatureTreeNode = nodeSelectionListener.getSignatureTreeNode();
                new SignaturePropertiesDialog(controller.getViewerFrame(),
                        messageBundle, signatureTreeNode.getOutlineItem()).setVisible(true);

            }
        }
    }

    /**
     * The actionPerformed method in this class is called each time the Timer "goes off".
     */
    class TimerListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            progressBar.setValue(sigVerificationTask.getCurrent());
            String s = sigVerificationTask.getMessage();
            if (s != null) {
                progressLabel.setText(s);
            }
            // update the text and stop the timer when the validation is completed or terminated.
            if (sigVerificationTask.isDone() || !sigVerificationTask.isCurrentlyVerifying()) {
                // update search status
                timer.stop();
                sigVerificationTask.stop();

                // update progress bar then hide it.
                progressBar.setValue(progressBar.getMinimum());
                progressBar.setVisible(false);
            }
        }
    }
}
