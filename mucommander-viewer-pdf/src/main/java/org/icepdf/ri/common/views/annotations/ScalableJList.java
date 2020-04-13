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

import org.icepdf.ri.common.views.DocumentViewModel;

import javax.swing.*;
import java.awt.*;

/**
 *
 */
public class ScalableJList extends JList implements ScalableField {

    private static final long serialVersionUID = 1434627181898233990L;
    private boolean active;

    public ScalableJList(ListModel dataModel, final DocumentViewModel documentViewModel) {
        super(dataModel);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setEditable(boolean editable) {
        super.setEnabled(editable);
    }

    @Override
    protected void paintBorder(Graphics g) {
        if (!active) {
            return;
        }
        super.paintBorder(g);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!active) {
            return;
        }
        super.paintComponent(g);
    }

    @Override
    public void repaint(int x, int y, int width, int height) {
        super.repaint();
    }

}
