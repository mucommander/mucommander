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
 * Named actions that the conformign reader shall support.  Names can be but not limited too.
 * <ul>
 * <li>NextPage</li>
 * <li>PrevPage</li>
 * <li>FirstPage</li>
 * <li>LastPage</li>
 * <li>Print</li>
 * </ul>
 *
 * @since 5.2
 */
public class NamedAction extends Action {

    /**
     * Go to the next page of the document.
     */
    public static final Name NEXT_PAGE_KEY = new Name("NextPage");

    /**
     * Go to the previous page of the document.
     */
    public static final Name PREV_PAGE_KEY = new Name("PrevPage");

    /**
     * Go to the first page of the document.
     */
    public static final Name FIRST_PAGE_KEY = new Name("FirstPage");

    /**
     * Print the document
     */
    public static final Name LAST_PAGE_KEY = new Name("LastPage");

    /**
     * Print the current document
     */
    public static final Name PRINT_KEY = new Name("Print");

    /**
     * Save document as command
     */
    public static final Name SAVE_AS_KEY = new Name("SaveAs");


    public static final Name N_KEY = new Name("N");

    private Name name;

    public NamedAction(Library library, HashMap entries) {
        super(library, entries);

        Object tmp = library.getObject(entries, N_KEY);
        if (tmp != null && tmp instanceof Name) {
            name = (Name) tmp;
        }
    }

    public Name getNamedAction(){
        return name;
    }
}
