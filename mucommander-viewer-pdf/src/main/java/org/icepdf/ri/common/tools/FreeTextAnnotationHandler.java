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

import org.icepdf.core.pobjects.PDate;
import org.icepdf.core.pobjects.annotations.Annotation;
import org.icepdf.core.pobjects.annotations.AnnotationFactory;
import org.icepdf.core.pobjects.annotations.FreeTextAnnotation;
import org.icepdf.ri.common.views.AbstractPageViewComponent;
import org.icepdf.ri.common.views.AnnotationCallback;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.common.views.DocumentViewModel;
import org.icepdf.ri.common.views.annotations.AbstractAnnotationComponent;
import org.icepdf.ri.common.views.annotations.AnnotationComponentFactory;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.logging.Logger;

/**
 * FreeTextAnnotationHandler tool is responsible for painting representation of
 * a FreeTextAnnotationHandler on the screen during a click and drag mouse event.
 * The box created by this mouse event will be used be used as the bounding
 * box of the annotation that will be created.
 * <p/>
 * Once the mouseReleased event is fired this handler will create new
 * FreeTextAnnotationHandler and respective AnnotationComponent.  The addition
 * of the Annotation object to the page is handled by the annotation callback.
 *
 * @since 5.0
 */
public class FreeTextAnnotationHandler extends SelectionBoxHandler
        implements ToolHandler {

    private static final Logger logger =
            Logger.getLogger(LineAnnotationHandler.class.toString());

    /**
     * New Text selection handler.  Make sure to correctly and and remove
     * this mouse and text listeners.
     *
     * @param pageViewComponent page component that this handler is bound to.
     * @param documentViewModel view model.
     */
    public FreeTextAnnotationHandler(DocumentViewController documentViewController,
                                     AbstractPageViewComponent pageViewComponent,
                                     DocumentViewModel documentViewModel) {
        super(documentViewController, pageViewComponent, documentViewModel);
    }

    @Override
    public void setSelectionRectangle(Point cursorLocation, Rectangle selection) {

    }

    public void paintTool(Graphics g) {
        paintSelectionBox(g, rectToDraw);
    }

    public void mouseClicked(MouseEvent e) {
        if (pageViewComponent != null) {
            pageViewComponent.requestFocus();
        }
    }

    public void mousePressed(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {
        updateSelectionSize(e.getX(),e.getY(), pageViewComponent);

        // check the bounds on rectToDraw to try and avoid creating
        // an annotation that is very small.
        if (rectToDraw.getWidth() < 5 || rectToDraw.getHeight() < 5) {
            rectToDraw.setSize(new Dimension(15, 25));
        }

        // create a fixed sized box based on the default font size.
        Rectangle tBbox = convertToPageSpace(rectToDraw).getBounds();

        // create annotations types that that are rectangle based;
        // which is actually just link annotations
        FreeTextAnnotation annotation = (FreeTextAnnotation)
                AnnotationFactory.buildAnnotation(
                        documentViewModel.getDocument().getPageTree().getLibrary(),
                        Annotation.SUBTYPE_FREE_TEXT,
                        tBbox);
        annotation.setCreationDate(PDate.formatDateTime(new Date()));
        annotation.setTitleText(System.getProperty("user.name"));
        annotation.setFontSize(24);
        annotation.setFontName("Helvetica");
        annotation.setContents(" ");

        // create the annotation object.
        AbstractAnnotationComponent comp =
                AnnotationComponentFactory.buildAnnotationComponent(
                        annotation,
                        documentViewController,
                        pageViewComponent, documentViewModel);
        // set the bounds and refresh the userSpace rectangle
        comp.setBounds(rectToDraw);
        // resets user space rectangle to match bbox converted to page space
        comp.refreshAnnotationRect();

        // add them to the container, using absolute positioning.
        if (documentViewController.getAnnotationCallback() != null) {
            AnnotationCallback annotationCallback =
                    documentViewController.getAnnotationCallback();
            annotationCallback.newAnnotation(pageViewComponent, comp);
        }

        // request focus so that editing can take place.
        comp.requestFocus();

        // set the annotation tool to he select tool
        documentViewController.getParentController().setDocumentToolMode(
                DocumentViewModel.DISPLAY_TOOL_SELECTION);

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void mouseDragged(MouseEvent e) {
        updateSelectionSize(e.getX(),e.getY(), pageViewComponent);
    }

    public void mouseMoved(MouseEvent e) {

    }

    public void installTool() {

    }

    public void uninstallTool() {

    }

}
