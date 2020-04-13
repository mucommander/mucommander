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

package org.icepdf.core.pobjects.actions;

import org.icepdf.core.pobjects.Name;
import org.icepdf.core.util.Library;

import java.util.HashMap;

/**
 * Execute interface for Form actions.
 *
 * @since 5.1
 */
public abstract class FormAction extends Action {

    /**
     * (Required) A URL file specification (see 7.11.5, "URL Specifications") giving the uniform resource locator
     * (URL) of the script at the Web server that will process the submission.
     */
    public static final Name F_KEY = new Name("F");

    /**
     * An array identifying which fields to reset or which to exclude from
     * resetting, depending on the setting of the Include/Exclude flag in the
     * Flags entry (see Table 239). Each element of the array shall be either
     * an indirect reference to a field dictionary or (PDF 1.3) a text string
     * representing the fully qualified name of a field. Elements of both kinds
     * may be mixed in the same array.
     * <p/>
     * If this entry is omitted, the Include/Exclude flag shall be ignored, and all
     * fields in the document’s interactive form shall be submitted except those whose
     * NoExport flag (see Table 221) is set. Fields with no values may also be excluded,
     * as dictated by the value of the IncludeNoValueFields flag; see Table 237.
     */
    public static final Name FIELDS_KEY = new Name("Fields");

    /**
     * (Optional; inheritable) A set of flags specifying various characteristics
     * of the action (see Table 239). Default value: 0.
     */
    public static final Name FLAGS_KEY = new Name("Flags");

    public FormAction(Library l, HashMap h) {
        super(l, h);
    }

    /**
     * (Optional; inheritable) A set of flags specifying various characteristics of the action (see Table 239).
     * Default value: 0.
     *
     * @return flag value
     */
    public int getFlags() {
        // behaviour flags
        return library.getInt(entries, FLAGS_KEY);
    }

    /**
     * Execute the form action and return the appropriate return code;
     *
     * @return determined by the implementation.
     */
    public abstract int executeFormAction(int x, int y);
}
