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
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.logging.Logger;

/**
 * Handles dynamic zoom which picks up on the mouse wheel rotation to zoom
 * in or out depending on the direction.
 *
 * @since 5.0
 */
public class DynamicZoomHandler implements ToolHandler, MouseWheelListener {

    private static final Logger logger =
            Logger.getLogger(ZoomOutPageHandler.class.toString());

    private DocumentViewController documentViewController;
    protected JScrollPane documentScrollPane;

    public DynamicZoomHandler(DocumentViewController documentViewController,
                              JScrollPane documentScrollPane) {
        this.documentViewController = documentViewController;
        this.documentScrollPane = documentScrollPane;
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

    }


    public void mouseClicked(MouseEvent e) {

    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void paintTool(Graphics g) {

    }

    public void installTool() {
        documentScrollPane.setWheelScrollingEnabled(false);
        documentScrollPane.addMouseWheelListener(this);
    }

    public void uninstallTool() {
        documentScrollPane.setWheelScrollingEnabled(true);
        documentScrollPane.removeMouseWheelListener(this);
    }
}