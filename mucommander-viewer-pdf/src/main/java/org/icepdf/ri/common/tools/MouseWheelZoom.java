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
package org.icepdf.ri.common.tools;

import org.icepdf.ri.common.views.DocumentViewController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * The MouseWheelZoom allows the zoom any any page view to be controlled by
 * holding down the ctr key and rotating the mouse wheel.
 *
 * @since 4.0
 */
public class MouseWheelZoom implements MouseWheelListener {

    protected DocumentViewController documentViewController;
    protected JScrollPane documentScrollPane;

    public MouseWheelZoom(DocumentViewController documentViewController,
                          JScrollPane documentScrollPane) {
        this.documentScrollPane = documentScrollPane;
        this.documentViewController = documentViewController;
    }

    /**
     * Handles ctl-wheel mouse for document zooming.
     *
     * @param e mouse wheel event.
     */
    public void mouseWheelMoved(MouseWheelEvent e) {
        int rotation = e.getWheelRotation();
        // turn off scroll on zoom and then back on again next time
        // the wheel is used with out the ctrl mask.
        if ((e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK
                ||
                (e.getModifiers() & InputEvent.META_MASK) == InputEvent.META_MASK) {
            documentScrollPane.setWheelScrollingEnabled(false);
            Point offset = documentScrollPane.getViewport().getViewPosition();
            int viewWidth = documentScrollPane.getViewport().getWidth() / 2;
            int viewHeight = documentScrollPane.getViewport().getHeight() / 2;
            offset.setLocation(offset.x + viewWidth, offset.y + viewHeight);
            if (rotation > 0) {
                documentViewController.setZoomOut(offset);
            } else {
                documentViewController.setZoomIn(offset);
            }
        } else {
            documentScrollPane.setWheelScrollingEnabled(true);
        }
    }


}
