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
package org.icepdf.core.pobjects.graphics;

import org.icepdf.core.pobjects.OptionalContents;

import java.util.LinkedList;

/**
 * OptionalContentState stores the state of the currently in play OptionalContent
 * if specified. If now optional content is present then this class will simply
 * return true when isVisible() is called.  This class handles embedded
 * optionalContentState correctly.
 *
 * @since 5.0
 */
public class OptionalContentState {

    private LinkedList<OptionalContents> optionContents = new LinkedList<OptionalContents>();

    private boolean isEmpty = true;

    public void add(OptionalContents optionContent) {
        optionContents.add(optionContent);
        isEmpty = false;
    }

    public void remove() {
        optionContents.removeLast();
        isEmpty = optionContents.isEmpty();
    }

    public boolean isVisible() {
        return isEmpty || optionContents.getLast().isVisible();
    }
}
