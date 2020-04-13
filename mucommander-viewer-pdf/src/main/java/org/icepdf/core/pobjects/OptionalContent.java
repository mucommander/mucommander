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

import java.util.*;

/**
 * The optional OCProperties entry in the document catalog
 * (see 7.7.2, "Document Catalog") shall contain, when present, the optional
 * content properties dictionary, which contains a list of all the optional
 * content groups in the document, as well as information about the default
 * and alternate configurations for optional content. This dictionary shall be
 * present if the file contains any optional content; if it is missing, a
 * conforming reader shall ignore any optional content structures in the document.
 *
 * @since 5.0
 */
public class OptionalContent extends Dictionary {

    private Map<Reference, OptionalContentGroup> groups;

    public static final Name OCGs_KEY = new Name("OCGs");
    public static final Name OC_KEY = new Name("OC");
    public static final Name D_KEY = new Name("D");
    public static final Name BASE_STATE_KEY = new Name("BaseState");
    public static final Name INTENT_KEY = new Name("Intent");
    public static final Name AS_KEY = new Name("AS");
    public static final Name ORDER_KEY = new Name("Order");
    public static final Name LIST_MODE_KEY = new Name("ListMode");
    public static final Name RBGROUPS_KEY = new Name("RBGroups");
    public static final Name LOCKED_KEY = new Name("Locked");
    public static final Name OFF_VALUE = new Name("OFF");
    public static final Name ON_vALUE = new Name("ON");
    public static final Name UNCHANGED_KEY = new Name("Unchanged");
    public static final Name VIEW_VALUE = new Name("View");
    public static final Name DESIGN_VALUE = new Name("Design");
    public static final Name NONE_OC_FLAG = new Name("marked");
    private Name baseState = ON_vALUE;

    /**
     * A single intent name or an array containing any combination of names. it
     * shall be used to determine which optional content groups’ states to consider
     * and which to ignore in calculating the visibility of content
     * (see 8.11.2.3, "Intent").
     * <p/>
     * PDF defines two intent names, View and Design. In addition, the name All
     * shall indicate the set of all intents, including those not yet defined.
     * Default value: View. The value shall be View for the document’s default configuration.
     */
    private List<Name> intent = Arrays.asList(VIEW_VALUE);


    /**
     * An array specifying the order for presentation of optional content groups
     * in a conforming reader’s user interface. The array elements may include
     * the following objects:
     * <p/>
     * Optional content group dictionaries, whose Name entry shall be displayed in
     * the user interface by the conforming reader.
     * <p/>
     * Arrays of optional content groups which may be displayed by a conforming
     * reader in a tree or outline structure. Each nested array may optionally
     * have as its first element a text string to be used as a non-selectable
     * label in a conforming reader’s user interface.
     * <p/>
     * Text labels in nested arrays shall be used to present collections of
     * related optional content groups, and not to communicate actual nesting of
     * content inside multiple layers of groups (see EXAMPLE 1 in 8.11.4.3,
     * "Optional Content Configuration Dictionaries"). To reflect actual nesting
     * of groups in the content, such as for layers with sublayers, nested arrays
     * of groups without a text label shall be used (see EXAMPLE 2 in 8.11.4.3,
     * "Optional Content Configuration Dictionaries").
     * <p/>
     * An empty array [] explicitly specifies that no groups shall be presented.
     * In the default configuration dictionary, the default value shall be an empty
     * array; in other configuration dictionaries, the default shall be the Order
     * value from the default configuration dictionary.
     * <p/>
     * Any groups not listed in this array shall not be presented in any user
     * interface that uses the configuration.
     */
    private List<Object> order;

    private List<Object> rbGroups;

    // object was created but the PDF doesn't actually have optional content definition and optional content
    // properties may no longer be valid.
    private boolean emptyDefinition;

    public OptionalContent(Library l, HashMap h) {
        super(l, h);
        groups = new HashMap<Reference, OptionalContentGroup>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init() {
        if (inited) {
            return;
        }
        // test of a valid definition.
        if (entries == null || entries.size() == 0){
            emptyDefinition = true;
        }

        // build out the optionContentGroups from the OCGs array, array should always
        // be indirect references to the optionContentGroups.
        Object ogcs = library.getObject(entries, OCGs_KEY);
        if (ogcs instanceof List) {
            List ogcList = (List) ogcs;
            Reference ref;
            Object ocgObj;
            for (Object object : ogcList) {
                if (object instanceof Reference) {
                    ref = (Reference) object;
                    ocgObj = library.getObject(ref);
                    if (ocgObj instanceof OptionalContentGroup) {
                        OptionalContentGroup ogc = (OptionalContentGroup) ocgObj;
                        groups.put(ref, ogc);
                    }
                }
            }
        }

        // The default viewing optional content configuration dictionary.
        Object dObj = library.getObject(entries, D_KEY);
        if (dObj instanceof HashMap) {
            HashMap configurationDictionary = (HashMap) dObj;

            // apply the base state ON|OFF|Unchanged
            Object tmp = library.getName(configurationDictionary, BASE_STATE_KEY);
            if (tmp != null && tmp instanceof Name) {
                baseState = (Name) tmp;
            }

            // If the BaseState entry is ON, then we only need to look at the OFF
            // entries.
            boolean isBaseOn = baseState.equals(ON_vALUE);
            List toggle;
            if (isBaseOn) {
                toggle = library.getArray(configurationDictionary, OFF_VALUE);
            }
            // If the BaseState entry is OFF, then the ON entries are relevant.
            else {
                toggle = library.getArray(configurationDictionary, ON_vALUE);
            }
            // build out the
            if (toggle != null) {
                for (Object obj : toggle) {
                    OptionalContentGroup ocg = groups.get(obj);
                    if (ocg != null) {
                        if (isBaseOn) {
                            // remove the off entries
                            ocg.setVisible(false);
                        } else {
                            // otherwise we add the on entries.
                            ocg.setVisible(true);
                        }
                    }
                }
            }
            // check for an intent entry
            tmp = library.getName(configurationDictionary, INTENT_KEY);
            if (tmp != null) {
                if (tmp instanceof Name) {
                    intent = Arrays.asList(new Name[]{(Name) tmp});
                } else if (tmp instanceof List) {
                    intent = (List) tmp;
                }
            }
            // ignore AS for now.
            /**
             * An array of usage application dictionaries (see Table 103)
             * specifying which usage dictionary categories (see Table 102)
             * shall be consulted by conforming readers to automatically set the
             * states of optional content groups based on external factors,
             * such as the current system language or viewing magnification,
             * and when they shall be applied.Order
             */

            // get the ordering information used by the UI. resolve the ref
            //
            tmp = library.getObject(configurationDictionary, ORDER_KEY);
            if (tmp != null && tmp instanceof List) {
                List orderedOCs = (List) tmp;
                if (orderedOCs.size() > 0) {
                    order = new ArrayList<Object>(orderedOCs.size());
                    order = parseOrderArray(orderedOCs, null);
                }
            }

            // get the radio button group data for correct UI behavior .
            tmp = library.getObject(configurationDictionary, RBGROUPS_KEY);
            if (tmp != null && tmp instanceof List) {
                List orderedOCs = (List) tmp;
                if (orderedOCs.size() > 0) {
                    rbGroups = new ArrayList<Object>(orderedOCs.size());
                    rbGroups = parseOrderArray(orderedOCs, null);
                }
            }

            // ignore Locked for now
        }
        inited = true;
    }

    @SuppressWarnings("unchecked")
    private List<Object> parseOrderArray(List<Object> rawOrder, OptionalContentGroup parent) {
        List<Object> order = new ArrayList<Object>(5);
        OptionalContentGroup group = null;
        for (Object obj : rawOrder) {
            if (obj instanceof Reference) {
                Object refObject = getOCGs((Reference) obj);
                if (refObject != null) {
                    group = (OptionalContentGroup) refObject;
                    if (parent != null && !parent.isVisible()) {
                        group.setVisible(false);
                    }
                    order.add(group);
                } else {
                    obj = library.getObject((Reference) obj);
                }
            }
            if (obj instanceof List) {
                parent = group;
                order.add(parseOrderArray((List) obj, parent));
            } else if (obj instanceof StringObject) {
                order.add(Utils.convertStringObject(library, (StringObject) obj));
            }
        }
        return order;
    }

    public boolean isVisible(Reference ocgRef) {
        Object object = library.getObject(ocgRef);
        if (object instanceof OptionalContentGroup) {
            return isVisible((OptionalContentGroup) object);
        } else if (object instanceof OptionalContentMembership) {
            return isVisible((OptionalContentMembership) object);
        }
        return false;
    }

    public boolean isVisible(OptionalContentGroup ocg) {
        return groups.containsKey(ocg.getPObjectReference());
    }

    public boolean isVisible(OptionalContentMembership ocmd) {
        ocmd.init();
        return ocmd.isVisible();
    }

    /**
     * Test if an xForm object image or content is visible.
     *
     * @param object content to check visibility.
     * @return optional content groups currently visibility state, returns
     * true if no state can be found, better to show then to
     * hide by default.
     */
    public boolean isVisible(Object object) {
        if (object instanceof Reference) {
            return isVisible((Reference) object);
        } else if (object instanceof OptionalContentGroup) {
            return isVisible((OptionalContentGroup) object);
        } else if (object instanceof OptionalContentMembership) {
            return isVisible((OptionalContentMembership) object);
        }
        return true;
    }

    public List<Object> getOrder() {
        return order;
    }

    public List<Name> getIntent() {
        return intent;
    }

    public int getGroupsSize() {
        return groups.size();
    }

    public List<Object> getRbGroups() {
        return rbGroups;
    }

    public OptionalContentGroup getOCGs(Reference reference) {
        return groups.get(reference);
    }

    public boolean isEmptyDefinition() {
        return emptyDefinition;
    }
}
