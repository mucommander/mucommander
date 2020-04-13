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

import org.icepdf.core.pobjects.acroform.FieldDictionary;
import org.icepdf.core.util.Library;

import java.awt.geom.AffineTransform;
import java.util.HashMap;

/**
 * Interactive forms (see 12.7, “Interactive Forms”) use widget annotations (PDF 1.2)
 * to represent the appearance of fields and to manage user interactions. As a
 * convenience, when a field has only a single associated widget annotation, the
 * contents of the field dictionary (12.7.3, “Field Dictionaries”) and the
 * annotation dictionary may be merged into a single dictionary containing
 * entries that pertain to both a field and an annotation.
 *
 * @since 5.0
 */
public class WidgetAnnotation extends AbstractWidgetAnnotation {


    private FieldDictionary fieldDictionary;


    public WidgetAnnotation(Library l, HashMap h) {
        super(l, h);
        fieldDictionary = new FieldDictionary(library, entries);
    }

    public void resetAppearanceStream(double dx, double dy, AffineTransform pageSpace) {

    }

    @Override
    public void reset() {

    }

    @Override
    public FieldDictionary getFieldDictionary() {
        return fieldDictionary;
    }
}
