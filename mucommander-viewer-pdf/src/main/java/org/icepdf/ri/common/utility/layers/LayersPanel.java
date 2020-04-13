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
package org.icepdf.ri.common.utility.layers;

import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.OptionalContent;
import org.icepdf.core.pobjects.OptionalContentGroup;
import org.icepdf.core.util.PropertyConstants;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.views.AbstractDocumentView;
import org.icepdf.ri.common.views.AbstractPageViewComponent;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.common.views.DocumentViewModel;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ResourceBundle;

/**
 * LayersPanel contains a LayersTree for manipulation of the PDF's optional
 * content if present.  The panel should only be enabled if the the Document's
 * catalog contains a OCProperties entry.
 */
@SuppressWarnings("serial")
public class LayersPanel extends JPanel {

    protected DocumentViewController documentViewController;

    protected Document currentDocument;

    private SwingController controller;

    protected LayersTreeNode nodes;
    protected DocumentViewModel documentViewModel;
    // message bundle for internationalization
    ResourceBundle messageBundle;

    public LayersPanel(SwingController controller) {
        super(true);
        setFocusable(true);
        this.controller = controller;
        this.messageBundle = this.controller.getMessageBundle();
    }

    private void buildUI() {

        JTree tree = new LayersTree(nodes);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        tree.addMouseListener(new NodeSelectionListener(tree));

        this.setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(tree,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        this.add(scrollPane,
                BorderLayout.CENTER);
    }

    public void setDocument(Document document) {
        this.currentDocument = document;
        documentViewController = controller.getDocumentViewController();
        documentViewModel = documentViewController.getDocumentViewModel();

        if (this.currentDocument != null) {
            OptionalContent optionalContent = currentDocument.getCatalog().getOptionalContent();
            List<Object> layersOrder = optionalContent.getOrder();
            if (layersOrder != null) {
                // check for radio buttons
                boolean hasRadioButtons = optionalContent.getRbGroups() != null
                        && optionalContent.getRbGroups().size() > 0;

                nodes = new LayersTreeNode("Layers");
                nodes.setAllowsChildren(true);
                buildTree(layersOrder, nodes, hasRadioButtons);
                buildUI();
            }
        } else {
            // tear down the old container.
            this.removeAll();
        }
    }

    @SuppressWarnings("unchecked")
    public void buildTree(List<Object> layersOrder, LayersTreeNode parent, boolean radioGroup) {

        LayersTreeNode tmp = null;
        boolean selected = true;
        // tod recursive build with parent checking.
        for (Object obj : layersOrder) {
            if (obj instanceof List) {
                LayersTreeNode newParent;
                if (parent.getChildCount() > 0) {
                    newParent = (LayersTreeNode) parent.getLastChild();
                } else {
                    newParent = parent;
                }
                buildTree((List<Object>) obj, newParent, radioGroup);
            } else if (obj instanceof String) {
                // sets the node as selected if children are all selected.
                if (tmp != null && selected) {
                    tmp.setSelected(true);
                }
                tmp = new LayersTreeNode(obj);
                tmp.setAllowsChildren(true);
                nodes.add(tmp);
                selected = true;
            } else if (obj instanceof OptionalContentGroup) {
                LayersTreeNode node = new LayersTreeNode(obj);
                node.setAllowsChildren(true);
                if (radioGroup) {
                    node.setSelectionMode(LayersTreeNode.RADIO_SELECTION);
                }
                parent.add(node);
                // check for an unselected state, goal is to select the parent
                // if all children are selected.
                if (!node.isSelected()) {
                    selected = false;
                }
            }
        }
    }


    public void dispose() {
        this.removeAll();
    }


    class NodeSelectionListener extends MouseAdapter {
        JTree tree;

        NodeSelectionListener(JTree tree) {
            this.tree = tree;
        }

        public void mouseClicked(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int row = tree.getRowForLocation(x, y);
            TreePath path = tree.getPathForRow(row);
            if (path != null) {
                LayersTreeNode node = (LayersTreeNode) path.getLastPathComponent();
                boolean isSelected = !(node.isSelected());
                node.setSelected(isSelected);
                // the current page and repaint
                List<AbstractPageViewComponent> pages = documentViewModel.getPageComponents();
                AbstractPageViewComponent page = pages.get(documentViewModel.getViewCurrentPageIndex());
                // resort page text as layer visibility will have changed.
                try {
                    page.getPage().getText().sortAndFormatText();
                } catch (InterruptedException e1) {
                    // silent running for now.
                }
                // fire change  event.
                ((AbstractDocumentView)documentViewController.getDocumentView()).firePropertyChange(
                        PropertyConstants.DOCUMENT_VIEW_REFRESH_CHANGE, false, true);
                // repaint the page.
                page.repaint();
                // repaint the tree so the checkbox states are show correctly.
                tree.repaint();
                ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
                if (row == 0) {
                    tree.revalidate();
                    tree.repaint();
                }
            }
        }
    }
}
