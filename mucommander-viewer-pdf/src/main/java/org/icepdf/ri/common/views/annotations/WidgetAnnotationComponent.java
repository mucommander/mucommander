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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 */
@SuppressWarnings("serial")
public class WidgetAnnotationComponent extends AbstractAnnotationComponent implements PropertyChangeListener {


    public WidgetAnnotationComponent(Annotation annotation, DocumentViewController documentViewController,
                                     AbstractPageViewComponent pageViewComponent, DocumentViewModel documentViewModel) {
        super(annotation, documentViewController, pageViewComponent, documentViewModel);
        if (annotation.allowScreenOrPrintRenderingOrInteraction()) {
            isShowInvisibleBorder = true;
            isResizable = true;
            isMovable = true;
            // assign property change listener so we can notification of annotation value change, via the
            // edit panel or form reset action.
            annotation.addPropertyChangeListener(this);
        }else{
            // border state flags.
            isEditable = false;
            isRollover = false;
            isMovable = false;
            isResizable = false;
            isShowInvisibleBorder = false;
        }
        isSelected = false;

    }

    @Override
    public void resetAppearanceShapes() {

    }

    @Override
    public void paintComponent(Graphics g) {

    }

    public boolean isActive() {
        return false;
    }

    public void propertyChange(PropertyChangeEvent evt) {

    }
}