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
import org.icepdf.ri.util.jxlayer.JXLayer;
import org.icepdf.ri.util.jxlayer.plaf.LayerUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Vector;

/**
 * Scalable JComboBox that scales as the document zoom is changed.
 *
 * @since 5.1
 */
public class ScalableJComboBox extends JComboBox implements ScalableField {

    private static final long serialVersionUID = -353525405737762626L;
    private DocumentViewModel documentViewModel;
    private boolean active;

    public ScalableJComboBox(Vector items, final DocumentViewModel documentViewModel) {
        super(items);
        this.documentViewModel = documentViewModel;
        // enable more precise painting of glyphs.
        putClientProperty("i18n", Boolean.TRUE.toString());
        LayerUI<JComponent> layerUI = new LayerUI<JComponent>() {
            private static final long serialVersionUID = 1152416379916442539L;
            @SuppressWarnings("unchecked")
            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                // enable mouse motion events for the layer's sub components
                ((JXLayer<? extends JComponent>) c).setLayerEventMask(
                        AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void uninstallUI(JComponent c) {
                super.uninstallUI(c);
                // reset the layer event mask
                ((JXLayer<? extends JComponent>) c).setLayerEventMask(0);
            }

            @Override
            public void eventDispatched(AWTEvent ae, JXLayer<? extends JComponent> l) {
                MouseEvent e = (MouseEvent) ae;
                // transform the point in MouseEvent using the current zoom factor
                float zoom = documentViewModel.getViewZoom();
                MouseEvent newEvent = new MouseEvent((Component) e.getSource(),
                        e.getID(), e.getWhen(), e.getModifiers(),
                        (int) (e.getX() / zoom), (int) (e.getY() / zoom),
                        e.getClickCount(), e.isPopupTrigger(), e.getButton());
                // consume the MouseEvent and then process the modified event
                e.consume();
                ScalableJComboBox.this.processMouseEvent(newEvent);
                ScalableJComboBox.this.processMouseMotionEvent(newEvent);
            }
        };
        new JXLayer<JComponent>(this, layerUI);
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

    public void repaint(int x, int y, int width, int height) {
        super.repaint();
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
