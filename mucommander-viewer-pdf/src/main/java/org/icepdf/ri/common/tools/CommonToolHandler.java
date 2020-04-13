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

import org.icepdf.core.pobjects.Page;
import org.icepdf.ri.common.views.AbstractPageViewComponent;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.common.views.DocumentViewModel;

import java.awt.*;
import java.awt.geom.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Common logic to all annotation handlers.
 *
 * @since 5.0
 */
public class CommonToolHandler {

    private static final Logger logger =
            Logger.getLogger(CommonToolHandler.class.toString());

    // parent page component
    protected AbstractPageViewComponent pageViewComponent;
    protected DocumentViewController documentViewController;
    protected DocumentViewModel documentViewModel;

    /**
     * Create a new common tool handler.  The tool handle can operate on a view or at the page level.  If the
     * handler only operates at the view level then pageViewComponent can be set to null;
     *
     * @param documentViewController parent view controller
     * @param pageViewComponent      page view component tool acts on, can be null for view tool handlers.
     * @param documentViewModel      parent document model
     */
    public CommonToolHandler(DocumentViewController documentViewController,
                             AbstractPageViewComponent pageViewComponent,
                             DocumentViewModel documentViewModel) {
        this.pageViewComponent = pageViewComponent;
        this.documentViewController = documentViewController;
        this.documentViewModel = documentViewModel;
    }

    protected AffineTransform getPageTransform() {
        return getPageTransform(pageViewComponent);
    }

    protected AffineTransform getPageTransform(AbstractPageViewComponent pageViewComponent) {
        Page currentPage = pageViewComponent.getPage();
        AffineTransform at = currentPage.getPageTransform(
                documentViewModel.getPageBoundary(),
                documentViewModel.getViewRotation(),
                documentViewModel.getViewZoom());
        try {
            at = at.createInverse();
        } catch (NoninvertibleTransformException e) {
            logger.log(Level.FINE, "Error page space transform", e);
        }
        return at;
    }


    /**
     * Convert the shapes that make up the annotation to page space so that
     * they will scale correctly at different zooms.
     *
     * @return transformed bBox.
     */
    protected Shape convertToPageSpace(Shape shape) {
        return convertToPageSpace(pageViewComponent, shape);
    }

    protected Shape convertToPageSpace(AbstractPageViewComponent pageViewComponent, Shape shape) {
        Page currentPage = pageViewComponent.getPage();
        AffineTransform at = currentPage.getPageTransform(
                documentViewModel.getPageBoundary(),
                documentViewModel.getViewRotation(),
                documentViewModel.getViewZoom());
        try {
            at = at.createInverse();
        } catch (NoninvertibleTransformException e) {
            logger.log(Level.FINE, "Error converting to page space", e);
        }
        shape = at.createTransformedShape(shape);
        return shape;

    }

    protected Point2D[] convertToPageSpace(Point2D start, Point2D end) {
        Page currentPage = pageViewComponent.getPage();
        AffineTransform at = currentPage.getPageTransform(
                documentViewModel.getPageBoundary(),
                documentViewModel.getViewRotation(),
                documentViewModel.getViewZoom());
        try {
            at = at.createInverse();
        } catch (NoninvertibleTransformException e) {
            logger.log(Level.FINE, "Error converting to page space", e);
        }
        at.transform(start, start);
        at.transform(end, end);

        return new Point2D[]{start, end};
    }

    /**
     * Convert the mouse coordinates to the space specified by the pageTransform
     * matrix.  This is a utility method for converting the mouse coordinates
     * to page space so that it can be used in a contains calculation for text
     * selection.
     *
     * @param mousePoint    point to convert space of
     * @param pageTransform transform
     * @return page space mouse coordinates.
     */
    protected Point2D.Float convertMouseToPageSpace(Point mousePoint,
                                                    AffineTransform pageTransform) {
        Point2D.Float pageMouseLocation = new Point2D.Float();
        try {
            pageTransform.createInverse().transform(
                    mousePoint, pageMouseLocation);
        } catch (NoninvertibleTransformException e) {
            logger.log(Level.SEVERE,
                    "Error converting mouse point to page space.", e);
        }
        return pageMouseLocation;
    }

    /**
     * Converts the rectangle to the space specified by the page transform. This
     * is a utility method for converting a selection rectangle to page space
     * so that an intersection can be calculated to determine a selected state.
     *
     * @param mouseRect     rectangle to convert space of
     * @param pageTransform page transform
     * @return converted rectangle.
     */
    protected Rectangle2D convertRectangleToPageSpace(Rectangle mouseRect,
                                                      AffineTransform pageTransform) {
        GeneralPath shapePath;
        try {
            AffineTransform transform = pageTransform.createInverse();
            shapePath = new GeneralPath(mouseRect);
            shapePath.transform(transform);
            return shapePath.getBounds2D();
        } catch (NoninvertibleTransformException e) {
            logger.log(Level.SEVERE,
                    "Error converting mouse point to page space.", e);
        }
        return null;
    }
}
