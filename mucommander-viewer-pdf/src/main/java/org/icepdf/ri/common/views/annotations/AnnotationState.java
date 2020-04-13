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

import org.icepdf.core.Memento;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.PageTree;
import org.icepdf.core.pobjects.annotations.Annotation;
import org.icepdf.core.pobjects.annotations.BorderStyle;
import org.icepdf.ri.common.views.AnnotationComponent;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Stores state paramaters for annotation objects to be used in conjuction
 * with a care taker as part of the memento pattern.
 *
 * @since 4.0
 */
public class AnnotationState implements Memento {

    // annotation bounding rectangle in user space.
    protected Rectangle2D.Float userSpaceRectangle;

    // original rectangle reference.
    protected AnnotationComponent annotationComponent;

    /**
     * Stores the annotation state associated with the AnnotationComponents
     * annotation object.  When a new instance of this object is created
     * the annotation's proeprties are saved.
     *
     * @param annotationComponent annotation component who's state will be stored.
     */
    public AnnotationState(AnnotationComponent annotationComponent) {
        // reference to component so we can apply the state parameters if
        // restore() is called.
        this.annotationComponent = annotationComponent;
    }


    public void apply(AnnotationState applyState) {

        // store user space rectangle SpaceRectangle.
        Rectangle2D.Float rect = applyState.userSpaceRectangle;
        if (rect != null) {
            userSpaceRectangle = new Rectangle2D.Float(rect.x, rect.y,
                    rect.width, rect.height);
        }

        // apply the new state to the annotation and schedule a sync
        restore();

    }

    /**
     * Restores the AnnotationComponents state to the state stored during the
     * construction of this object.
     */
    public void restore() {
        if (annotationComponent != null &&
                annotationComponent.getAnnotation() != null) {
            // get reference to annotation
            Annotation annotation = annotationComponent.getAnnotation();

            restore(annotation);

            // update the document with current state.
            synchronizeState();
        }
    }

    /**
     * Restores the annotation state in this instance to the Annotation
     * specified as a param. This method is ment to bue used in
     *
     * @param annotation annotation to retore state to.
     */
    public void restore(Annotation annotation) {
        // create a new Border style entry as an inline dictionary
        if (annotation.getBorderStyle() == null) {
            annotation.setBorderStyle(new BorderStyle());
        }

        // apply old user rectangle
        annotation.setUserSpaceRectangle(userSpaceRectangle);
    }

    public void synchronizeState() {
        // update the document with this change.
        int pageIndex = annotationComponent.getPageIndex();
        Document document = annotationComponent.getDocument();
        Annotation annotation = annotationComponent.getAnnotation();
        PageTree pageTree = document.getPageTree();
        Page page = pageTree.getPage(pageIndex);
        // state behind draw state.
        if (!annotation.isDeleted()) {
            page.updateAnnotation(annotation);
            // refresh bounds for any resizes
            annotationComponent.refreshDirtyBounds();
        }
        // special case for an undelete as we need to to make the component
        // visible again.
        else {
            // mark it as not deleted
            annotation.setDeleted(false);
            // re-add it to the page
            page.addAnnotation(annotation);
            // finally update the pageComponent so we can see it again.
            ((Component) annotationComponent).setVisible(true);
            // refresh bounds for any resizes
            annotationComponent.refreshDirtyBounds();
        }
    }


}
