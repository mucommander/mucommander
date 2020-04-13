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

import org.icepdf.core.pobjects.annotations.Annotation;
import org.icepdf.ri.common.views.AbstractPageViewComponent;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.common.views.DocumentViewModel;

import java.awt.*;
import java.util.logging.Logger;

/**
 * The TextAnnotationComponent encapsulates a TextAnnotation objects.  It
 * also provides basic editing functionality such as resizing, moving and change
 * the border color and style as well as the fill color.
 * <p/>
 * The Viewer RI implementation contains a TextAnnotationPanel class which
 * can edit the various properties of this component.
 *
 * @see org.icepdf.ri.common.utility.annotation.TextAnnotationPanel
 * @since 5.0
 */
@SuppressWarnings("serial")
public class TextAnnotationComponent extends MarkupAnnotationComponent {

    private static final Logger logger =
            Logger.getLogger(TextAnnotationComponent.class.toString());

    public TextAnnotationComponent(Annotation annotation, DocumentViewController documentViewController,
                                   AbstractPageViewComponent pageViewComponent, DocumentViewModel documentViewModel) {
        super(annotation, documentViewController, pageViewComponent, documentViewModel);
        isRollover = false;
        isMovable = true;
        isResizable = false;
        isShowInvisibleBorder = false;

    }

    @Override
    public void resetAppearanceShapes() {
        annotation.resetAppearanceStream(getPageTransform());
    }

    @Override
    public void paintComponent(Graphics g) {

    }
}
