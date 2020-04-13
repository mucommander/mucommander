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
 * @since 5.1
 */
public class ScalableJScrollPane extends JScrollPane implements ScalableField {


    private static final long serialVersionUID = -7748761572295520052L;
    private boolean active;

    public ScalableJScrollPane(Component view, final DocumentViewModel documentViewModel) {
        super(view);
    }

    public boolean isActive() {
        return active;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setEditable(boolean editable) {
//        this.setEditable(editable);
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

    //    @Override
    protected void paintChildren(Graphics g) {
        if (!active) {
            return;
        }
        super.paintChildren(g);
    }
    @Override
    public void repaint(int x, int y, int width, int height) {
        super.repaint();
    }

}
