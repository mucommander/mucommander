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

import org.icepdf.ri.common.views.AbstractPageViewComponent;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.common.views.DocumentViewModel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * The AnnotationSelectionHandler is responsible for deselecting all annotations
 * when the a mouse click event has been fired.
 *
 * @since 5.0
 */
public class AnnotationSelectionHandler extends MouseAdapter
        implements ToolHandler {

    protected DocumentViewController documentViewController;
    protected DocumentViewModel documentViewModel;
    protected AbstractPageViewComponent pageViewComponent;

    public AnnotationSelectionHandler(DocumentViewController documentViewController,
                                      AbstractPageViewComponent pageViewComponent,
                                      DocumentViewModel documentViewModel) {
        this.documentViewController = documentViewController;
        this.documentViewModel = documentViewModel;
        this.pageViewComponent = pageViewComponent;
    }

    public void mouseClicked(MouseEvent e) {
        documentViewController.clearSelectedAnnotations();
        if (pageViewComponent != null)
            pageViewComponent.requestFocus();
    }

    public void paintTool(Graphics g) {
        // nothing to paint
    }

    public void mouseDragged(MouseEvent e) {

    }

    public void mouseMoved(MouseEvent e) {

    }

    public void installTool() {

    }

    public void uninstallTool() {

    }
}
