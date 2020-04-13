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

import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.annotations.Annotation;

/**
 * AnnotationComponent interfaces.  Oulines two main methods needed for
 * management and state saving but avoids having to load the Swing/awt libraries
 * unless necessary.
 *
 * @since 4.0
 */
public interface AnnotationComponent {

    /**
     * Gets wrapped annotation object.
     *
     * @return annotation that this component wraps.
     */
    public Annotation getAnnotation();

    /**
     * Refreshs the annotations bounds rectangle.  This method insures that
     * the bounds have been correctly adjusted for the current page transformation
     * In a none visual representation this method may not have to do anything.
     */
    public void refreshDirtyBounds();

    /**
     * Refreshed the annotation rectangle by inverting the components current
     * bounds with the current page transformation.
     */
    public void refreshAnnotationRect();

    /**
     * Component has focus.
     *
     * @return true if has focus, false otherwise.
     */
    public boolean hasFocus();

    /**
     * Component is editable, contents can be updated in ui
     */
    public boolean isEditable();

    /**
     * Component is editable, contents can be updated in ui
     */
    public boolean isShowInvisibleBorder();

    /**
     * Component highlight/select border is draw on mouse over.
     */
    public boolean isRollover();

    /**
     * Component is movable.
     */
    public boolean isMovable();

    /**
     * Component is resizable.
     */
    public boolean isResizable();

    /**
     * border has defined style.
     *
     * @return
     */
    public boolean isBorderStyle();

    public boolean isSelected();

    public Document getDocument();

    public int getPageIndex();

    public PageViewComponent getParentPageView();

    public void setSelected(boolean selected);

    public void repaint();

    public void resetAppearanceShapes();

    public PageViewComponent getPageViewComponent();

    public void dispose();

}
