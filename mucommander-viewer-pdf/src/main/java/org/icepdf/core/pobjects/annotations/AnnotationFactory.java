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
package org.icepdf.core.pobjects.annotations;

import org.icepdf.core.pobjects.Name;
import org.icepdf.core.util.Library;

import java.awt.*;
import java.util.logging.Logger;

/**
 * Factory for build annotations.
 * <p/>
 * Note: Currently only Link annotations are supported.
 *
 * @since 4.0
 */
public class AnnotationFactory {

    private static final Logger logger =
            Logger.getLogger(AnnotationFactory.class.toString());

    /**
     * Creates a new Annotation object using properties from the annotationState
     * paramater.  If no annotaitonState is provided a LinkAnnotation is returned
     * with with a black border.  The rect specifies where the annotation should
     * be located in user space.
     * <p/>
     * This call adds the new Annotation object to the document library as well
     * as the document StateManager.
     *
     * @param library library to register annotation with
     * @param subType type of annotation to create
     * @param rect    bounds of new annotation specified in user space.
     * @return new annotation object with the same properties as the one
     *         specified in annotaiton state.
     */
    public static Annotation buildAnnotation(Library library,
                                             final Name subType,
                                             Rectangle rect) {
        // build up a link annotation
        if (subType.equals(Annotation.SUBTYPE_LINK)) {
            return LinkAnnotation.getInstance(library, rect);
        }
        // highlight version of a TextMarkup annotation.
        else if (subType.equals(TextMarkupAnnotation.SUBTYPE_HIGHLIGHT) ||
                subType.equals(TextMarkupAnnotation.SUBTYPE_STRIKE_OUT) ||
                subType.equals(TextMarkupAnnotation.SUBTYPE_UNDERLINE)) {
            return TextMarkupAnnotation.getInstance(library, rect,
                    subType);
        } else if (subType.equals(Annotation.SUBTYPE_LINE)) {
            return LineAnnotation.getInstance(library, rect);
        } else if (subType.equals(Annotation.SUBTYPE_SQUARE)) {
            return SquareAnnotation.getInstance(library, rect);
        } else if (subType.equals(Annotation.SUBTYPE_CIRCLE)) {
            return CircleAnnotation.getInstance(library, rect);
        } else if (subType.equals(Annotation.SUBTYPE_INK)) {
            return InkAnnotation.getInstance(library, rect);
        } else if (subType.equals(Annotation.SUBTYPE_FREE_TEXT)) {
            return FreeTextAnnotation.getInstance(library, rect);
        } else if (subType.equals(Annotation.SUBTYPE_TEXT)) {
            return TextAnnotation.getInstance(library, rect);
        } else if (subType.equals(Annotation.SUBTYPE_POPUP)) {
            return PopupAnnotation.getInstance(library, rect);
        } else {
            logger.warning("Unsupported Annotation type. ");
            return null;
        }
    }
}
