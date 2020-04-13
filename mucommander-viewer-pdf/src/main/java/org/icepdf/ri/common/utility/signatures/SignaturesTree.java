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

import org.icepdf.ri.common.utility.outline.OutlineItemTreeNode;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

/**
 * SignaturesTree is a JTree derivative whose nodes are SignatureTreeNode objects,
 * each of which refers to a document signature.  The signature can be used to
 * authenticate the identity of user and the documents contents.  A node can be
 * clicked on for more information pertaining to the signatures validity.
 *
 * @see OutlineItemTreeNode
 * @since 6.1
 */
@SuppressWarnings("serial")
public class SignaturesTree extends JTree {

    public SignaturesTree(TreeModel newModel) {
        super(newModel);
        getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        setRootVisible(true);
        setScrollsOnExpand(true);

        // setup a custom cell render
        setCellRenderer(new SignatureCellRender());

        // old font was Arial with is no go for linux.
        setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 13));
        setRowHeight(18);
    }
}
