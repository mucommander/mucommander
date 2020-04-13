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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * As mentioned above, content may belong to a single optional content group and
 * shall be visible when the group is ON and invisible when it is OFF. To express
 * more complex visibility policies, content shall not declare itself to belong
 * directly to an optional content group but rather to an optional content
 * membership dictionary.
 * <p/>
 * Note: currently no support for the visibility expression (VE) array.
 *
 * @since 5.0
 */
public class OptionalContentMembership extends Dictionary implements OptionalContents {

    public static final Name TYPE = new Name("OCMD");

    public static final Name OCGs_KEY = new Name("OCGs");
    public static final Name P_KEY = new Name("P");
    public static final Name VE_KEY = new Name("VE");
    public static final Name ALL_ON_KEY = new Name("AllOn");
    public static final Name ALL_OFF_KEY = new Name("AllOff");
    public static final Name ANY_ON_KEY = new Name("AnyOn");
    public static final Name ANY_OFF_KEY = new Name("AnyOff");

    /**
     * A name specifying the visibility policy for content belonging to this
     * membership dictionary. Valid values shall be:
     * <ul>
     * <li>AllOn - visible only if all of the entries in OCGs are ON
     * <li>AnyOn - visible if any of the entries in OCGs are ON
     * <li>AnyOff - visible if any of the entries in OCGs are OFF
     * <li>AllOff - visible only if all of the entries in OCGs are OFF
     * </ul>
     * Default value: AnyOn
     */
    private VisibilityPolicy policy;

    /**
     * A dictionary or array of dictionaries specifying the optional content
     * groups whose states shall determine the visibility of content controlled
     * by this membership dictionary.
     * Null values or references to deleted objects shall be ignored. If this
     * entry is not present, is an empty array, or contains references only to
     * null or deleted objects, the membership dictionary shall have no effect
     * on the visibility of any content.
     */
    private List<OptionalContentGroup> ocgs = new ArrayList<OptionalContentGroup>();

    /**
     * An array specifying a visibility expression, used to compute visibility
     * of content based on a set of optional content groups
     */
    private List visibilityExpression;

    public OptionalContentMembership(Library library, HashMap entries) {
        super(library, entries);
    }

    @Override
    public void init() {
        if (inited) {
            return;
        }
        // build out the OCG entries.
        Object ocgObj = library.getObject(entries, OCGs_KEY);
        if (ocgObj instanceof OptionalContentGroup) {
            ocgs.add((OptionalContentGroup) ocgObj);
        } else if (ocgObj instanceof List) {
            List ocgList = (List) ocgObj;
            for (Object object : ocgList) {
                Object ocg = library.getObject(object);
                if (ocg instanceof OptionalContentGroup) {
                    ocgs.add((OptionalContentGroup) ocg);
                }
            }
        }
        policy = VisibilityPolicy.getPolicy(library.getName(entries, P_KEY));

        inited = true;
    }

    public boolean isOCG() {
        return true;
    }

    public VisibilityPolicy getPolicy() {
        return policy;
    }

    public List<OptionalContentGroup> getOcgs() {
        return ocgs;
    }

    public boolean isVisible() {
        return policy.isVisible(ocgs);
    }

    public static enum VisibilityPolicy {
        ALL_ON {
            @Override
            boolean isVisible(List<OptionalContentGroup> ocgs) {
                for (OptionalContentGroup ocg : ocgs) {
                    if (!ocg.isVisible()) {
                        return false;
                    }
                }
                return true;
            }
        },
        ANY_ON {
            @Override
            boolean isVisible(List<OptionalContentGroup> ocgs) {
                for (OptionalContentGroup ocg : ocgs) {
                    if (ocg.isVisible()) {
                        return true;
                    }
                }
                return false;
            }
        },
        ANY_OFF {
            @Override
            boolean isVisible(List<OptionalContentGroup> ocgs) {
                for (OptionalContentGroup ocg : ocgs) {
                    if (!ocg.isVisible()) {
                        return true;
                    }
                }
                return false;
            }
        },
        ALL_OFF {
            @Override
            boolean isVisible(List<OptionalContentGroup> ocgs) {
                for (OptionalContentGroup ocg : ocgs) {
                    if (ocg.isVisible()) {
                        return false;
                    }
                }
                return true;
            }
        };

        public static VisibilityPolicy getPolicy(Name p) {
            if (ALL_ON_KEY.equals(p)) {
                return ALL_ON;
            } else if (ALL_OFF_KEY.equals(p)) {
                return ALL_OFF;
            } else if (ANY_ON_KEY.equals(p)) {
                return ANY_ON;
            } else if (ANY_OFF_KEY.equals(p)) {
                return ANY_OFF;
            } else if (ANY_OFF_KEY.equals(p)) {
                return ALL_OFF;
            } else {
                return ANY_ON;
            }
        }

        abstract boolean isVisible(List<OptionalContentGroup> ocgs);
    }
}
