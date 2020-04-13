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

package org.icepdf.core.pobjects.acroform;

import org.icepdf.core.pobjects.Name;
import org.icepdf.core.util.Library;

import java.util.HashMap;

/**
 * The FieldDictionaryFactory is responsible for building out the interactive form field tree.  When a none terminal
 * field is encountered this factor can be used to build an appropriate field dictionary for the given /FT key.
 *
 * @since 5.2
 */
public class FieldDictionaryFactory {

    public static final Name TYPE_BUTTON = new Name("Btn");
    public static final Name TYPE_TEXT = new Name("Tx");
    public static final Name TYPE_CHOICE= new Name("Ch");
    public static final Name TYPE_SIGNATURE = new Name("Sig");

    private FieldDictionaryFactory() {}

    /**
     * Creates a new field dictionary object of the type specified by the type constant.
     *
     * @param library library to register action with
     * @param entries field name value pairs.
     * @return new field dictionary object of the specified field type.
     */
    public static FieldDictionary buildField(Library library,
                                             HashMap entries) {
        FieldDictionary fieldDictionary = null;
        Name fieldType = library.getName(entries, FieldDictionary.FT_KEY);
        if (TYPE_BUTTON.equals(fieldType)) {
            fieldDictionary = new ButtonFieldDictionary(library, entries);
        } else if (TYPE_TEXT.equals(fieldType)) {
            fieldDictionary = new TextFieldDictionary(library, entries);
        } else if (TYPE_CHOICE.equals(fieldType)) {
            fieldDictionary = new ChoiceFieldDictionary(library, entries);
        } else if (TYPE_SIGNATURE.equals(fieldType)) {
            fieldDictionary = new SignatureFieldDictionary(library, entries);
        }else{
            fieldDictionary = new FieldDictionary(library, entries);
        }
        return fieldDictionary;
    }
}
