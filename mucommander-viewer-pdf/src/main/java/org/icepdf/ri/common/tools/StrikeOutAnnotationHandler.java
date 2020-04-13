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

import org.icepdf.core.pobjects.annotations.TextMarkupAnnotation;
import org.icepdf.ri.common.views.AbstractPageViewComponent;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.common.views.DocumentViewModel;

/**
 * StrikeOutAnnotationHandler tool extends TextSelectionPageHandler which
 * takes care visually selected text as the mouse is dragged across text on the
 * current page.
 * <p/>
 * Once the mouseReleased event is fired this handler will create new
 * StrikeOutAnnotation and respective AnnotationComponent.  The addition of the
 * Annotation object to the page is handled by the annotation callback. Once
 * create the handler will deselect the text and the newly created annotation
 * will be displayed.
 *
 * @since 5.0
 */
public class StrikeOutAnnotationHandler extends HighLightAnnotationHandler {

    public StrikeOutAnnotationHandler(DocumentViewController documentViewController,
                                      AbstractPageViewComponent pageViewComponent,
                                      DocumentViewModel documentViewModel) {
        super(documentViewController, pageViewComponent, documentViewModel);
        highLightType = TextMarkupAnnotation.SUBTYPE_STRIKE_OUT;
    }
}
