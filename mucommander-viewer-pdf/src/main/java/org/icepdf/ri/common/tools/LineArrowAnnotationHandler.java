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

import org.icepdf.core.pobjects.annotations.LineAnnotation;
import org.icepdf.ri.common.views.AbstractPageViewComponent;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.common.views.DocumentViewModel;

import java.awt.*;

/**
 * LineArrowAnnotationHandler tool is responsible for painting representation of
 * a line arrow on the screen during a click and drag mouse event.  The first point
 * is recorded on mousePressed and the line is drawn from first point the current
 * location of the mouse.  An open arrow is drawn at the starting point.
 * <p/>
 * Once the mouseReleased event is fired this handler will create new
 * LineArrowAnnotation and respective AnnotationComponent.  The addition of the
 * Annotation object to the page is handled by the annotation callback.
 *
 * @since 5.0
 */
public class LineArrowAnnotationHandler extends LineAnnotationHandler {


    public LineArrowAnnotationHandler(DocumentViewController documentViewController,
                                      AbstractPageViewComponent pageViewComponent,
                                      DocumentViewModel documentViewModel) {
        super(documentViewController, pageViewComponent, documentViewModel);

        startLineEnding = LineAnnotation.LINE_END_OPEN_ARROW;
        endLineEnding = LineAnnotation.LINE_END_NONE;
    }

    public void paintTool(Graphics g) {
        if (startOfLine != null && endOfLine != null) {
            Graphics2D gg = (Graphics2D) g;
            Color oldColor = gg.getColor();
            Stroke oldStroke = gg.getStroke();
            g.setColor(lineColor);
            gg.setStroke(stroke);

            // draw the line
            gg.drawLine((int) startOfLine.getX(), (int) startOfLine.getY(),
                    (int) endOfLine.getX(), (int) endOfLine.getY());
            // draw start cap
            if (!startLineEnding.equals(LineAnnotation.LINE_END_NONE)) {
                LineAnnotation.drawLineStart(gg, startLineEnding, startOfLine,
                        endOfLine, lineColor, internalColor);
            }
            // draw end cap
            if (!endLineEnding.equals(LineAnnotation.LINE_END_NONE)) {
                LineAnnotation.drawLineEnd(gg, endLineEnding, endOfLine,
                        endOfLine, lineColor, internalColor);
            }
            g.setColor(oldColor);
            gg.setStroke(oldStroke);
        }
    }


}
