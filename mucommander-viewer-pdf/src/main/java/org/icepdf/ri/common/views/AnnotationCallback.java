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
package org.icepdf.ri.common.views;

import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.actions.Action;
import org.icepdf.core.pobjects.annotations.Annotation;

/**
 * <p>Annotation callback allows developers to control how Annotation and
 * their actions are executed.  Developers also have have the option to
 * change annotation visibility attributes such as border style, border color
 * and border stroke width before the annotation is painted.</p>
 *
 * @author ICEsoft Technologies, Inc.
 * @see org.icepdf.ri.common.views.DocumentViewController#setAnnotationCallback(AnnotationCallback)
 * @since 2.6
 */
public interface AnnotationCallback {

    /**
     * <p>Implemented Annotation Callback method.  When an annotation is activated
     * in a PageViewComponent it passes the annotation to this method for
     * processing.  The PageViewComponent take care of drawing the annotation
     * states but it up to this method to process the annotation.</p>
     * <p>Form Widgets submit and reset actions are also handled by this callback.</p>
     *
     * @param annotation annotation that was activated by a user via the
     *                   PageViewComponent.
     * @param action     the action event that was fired when the annotation was clicked.  This can be
     *                   the A or AA entry.
     * @param x          x-coordinate of input device click that initiated the annotation action.
     * @param y          y-coordinate of input device click that initiated the annotation action.
     */
    public void processAnnotationAction(Annotation annotation, Action action, int x, int y);

    /**
     * <p>Implemented Annotation Callback method.  This method is called when a
     * pages annotations been initialized but before the page has been painted.
     * This method blocks the </p>
     *
     * @param page page that has been initialized.  The pages annotations are
     *             available via an accessor method.
     */
    public void pageAnnotationsInitialized(Page page);


    /**
     * New annotation created with view tool.
     *
     * @param page                page that annotation was added to.
     * @param annotationComponent annotation component to be added to page.
     */
    public void newAnnotation(PageViewComponent page,
                              AnnotationComponent annotationComponent);

    /**
     * Update the annotation and ready state for save.
     *
     * @param annotationComponent annotation component to be added to page.
     */
    public void updateAnnotation(AnnotationComponent annotationComponent);

    /**
     * Remove the annotation and ready state for save.
     *
     * @param pageComponent       page that annotation should be removed.
     * @param annotationComponent annotation component to be added to page.
     */
    public void removeAnnotation(PageViewComponent pageComponent,
                                 AnnotationComponent annotationComponent);

}
