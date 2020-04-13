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
package org.icepdf.core.pobjects;

import org.icepdf.core.util.Library;
import org.icepdf.core.util.Utils;

import java.util.HashMap;

/**
 * An optional content group is a dictionary representing a collection of graphics
 * that can be made visible or invisible dynamically by users of conforming
 * readers. The graphics belonging to such a group may reside anywhere in the
 * document: they need not be consecutive in drawing order, nor even belong to
 * the same content stream.
 *
 * @since 5.0
 */
public class OptionalContentGroup extends Dictionary implements OptionalContents {

    public static final Name TYPE = new Name("OCG");
    public static final Name NAME_KEY = new Name("Name");
    public static final Name USAGE_KEY = new Name("Usage");

    /**
     * The name of the optional content group, suitable for presentation in a
     * reader’s user interface.
     */
    private String name;

    /**
     * A usage dictionary describing the nature of the content controlled by the
     * group. It may be used by features that automatically control the state of
     * the group based on outside factors. See 8.11.4.4, "Usage and Usage
     * Application Dictionaries" for more information
     */
    private HashMap usage;

    /**
     * Indicates if this content groups and its child shapes should be displayed.
     * All OCG's are enabled by default.
     */
    private boolean visible = true;
    private boolean isOCG;

    public OptionalContentGroup(String name, boolean visible) {
        super(null, null);
        this.name = name;
        this.visible = visible;
    }

    public OptionalContentGroup(Library library, HashMap entries) {
        super(library, entries);
        // build from Parser
        isOCG = true;
    }

    public boolean isOCG() {
        return isOCG;
    }

    public String getName() {
        if (name == null) {
            name = Utils.convertStringObject(library,
                    (StringObject) library.getObject(entries, NAME_KEY));
        }
        return name;
    }

    public HashMap getUsage() {
        if (usage == null) {
            usage = library.getDictionary(entries, USAGE_KEY);
        }
        return usage;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
