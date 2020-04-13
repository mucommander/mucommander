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

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * Renders the appropriate root icon for a SignatureTreeNode.  Child elements of SigPropertyTreeNode will
 * have their icon set to null.
 */
public class SignatureCellRender extends DefaultTreeCellRenderer {

    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {

        super.getTreeCellRendererComponent(
                tree, value, sel,
                expanded, leaf, row,
                hasFocus);
        // dynamic as the validator status changes, so will the icon.
        if (value instanceof SignatureTreeNode) {
            SignatureTreeNode signatureTreeNode = (SignatureTreeNode) value;
            if (signatureTreeNode.getRootNodeValidityIcon() != null) {
                setIcon(signatureTreeNode.getRootNodeValidityIcon());
            }
        } else {
            setIcon(null);
        }
        return this;
    }

}
