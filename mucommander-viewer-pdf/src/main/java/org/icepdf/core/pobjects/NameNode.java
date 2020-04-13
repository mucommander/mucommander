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
 * NameNode used in building a name tree.
 *
 * @since 4.0
 */
public class NameNode extends Dictionary {

    public static final Name KIDS_KEY = new Name("Kids");
    public static final Name NAMES_KEY = new Name("Names");
    public static final Name LIMITS_KEY = new Name("Limits");

    private static Object NOT_FOUND = new Object();
    private static Object NOT_FOUND_IS_LESSER = new Object();
    private static Object NOT_FOUND_IS_GREATER = new Object();

    private boolean namesAreDecrypted;
    // flat tree, names and values only.
    private List<String> namesAndValues;
    // kids type of tree, need to build out the structure
    private List kidsReferences;
    private List<NameNode> kidsNodes;
    private String lowerLimit;
    private String upperLimit;

    /**
     * @param l
     * @param h
     */
    @SuppressWarnings("unchecked")
    public NameNode(Library l, HashMap h) {
        super(l, h);
        // root node can either be a Kids or Names
        Object o = library.getObject(entries, KIDS_KEY);
        if (o != null && o instanceof List) {
            // we have a kids array which can be composed of an intermediary
            // /limits/kids and or the leaf /limits/names
            kidsReferences = (List) o;
            int sz = kidsReferences.size();
            if (sz > 0) {
                kidsNodes = new ArrayList<NameNode>(sz);
                for (Object ref : kidsReferences) {
                    if (ref instanceof Reference) {
                        o = library.getObject((Reference) ref);
                        kidsNodes.add(new NameNode(library, (HashMap) o));
                    }
                }

            }
        }
        // if no kids[] then we must have a names array which is only one leaf.
        else if (o == null) {
            // process the names
            namesAreDecrypted = false;
            o = library.getObject(entries, NAMES_KEY);
            if (o != null && o instanceof List) {
                namesAndValues = (List) o;
            }
        }
        // assign the upper and lower limits if any.
        o = library.getObject(entries, LIMITS_KEY);
        if (o != null && o instanceof List) {
            List limits = (List) o;
            if (limits.size() >= 2) {
                lowerLimit = decryptIfText(limits.get(0));
                upperLimit = decryptIfText(limits.get(1));
            }
        }
    }

    public boolean isEmpty() {
        return kidsNodes.size() == 0;
    }

    public boolean hasLimits() {
        return library.getObject(entries, LIMITS_KEY) != null;
    }

    public List getNamesAndValues() {
        return namesAndValues;
    }

    public List getKidsReferences() {
        return kidsReferences;
    }

    public List<NameNode> getKidsNodes() {
        return kidsNodes;
    }

    public String getLowerLimit() {
        return lowerLimit;
    }

    public String getUpperLimit() {
        return upperLimit;
    }

    private void ensureNamesDecrypted() {
        if (namesAreDecrypted)
            return;
        namesAreDecrypted = true;
        // We need to look at each key and encrypt any Text objects which
        // is every second object
        for (int i = 0; i < namesAndValues.size(); i += 2) {
            namesAndValues.set(i,
                    decryptIfText(namesAndValues.get(i)));
        }
    }

    /**
     * Decyptes the node String object and returns a String value of the node
     * which is used to find names in the name tree. We only do this once
     * for the notes names vector.
     *
     * @param tmp object to decrypt.
     * @return decrypted string.
     */
    private String decryptIfText(Object tmp) {
        if (tmp instanceof StringObject) {
            StringObject nameText = (StringObject) tmp;
            return nameText.getDecryptedLiteralString(library.getSecurityManager());
        } else if (tmp instanceof String) {
            return (String) tmp;
        }
        return null;
    }

    /**
     * Search for the given name in the name tree.
     *
     * @param name name to search for
     * @return retrieved object if any otherwise, null.
     */
    Object searchName(String name) {
        Object ret = search(name);
        if (ret == NOT_FOUND || ret == NOT_FOUND_IS_LESSER || ret == NOT_FOUND_IS_GREATER) {
            ret = null;
        }
        return ret;
    }


    private Object search(String name) {
//System.out.println("search()  for: " + name + "  lowerLimit: " + lowerLimit + "  upperLimit: " + upperLimit + " " + name);
        if (kidsReferences != null) {
//System.out.print("search()  kids ... ");
            if (lowerLimit != null) {
                int cmp = lowerLimit.compareTo(name);
                if (cmp > 0) {
//System.out.println("skLESSER");
                    return NOT_FOUND_IS_LESSER;
                } else if (cmp == 0)
                    return getNode(0).search(name);
            }
            if (upperLimit != null) {
                int cmp = upperLimit.compareTo(name);
                if (cmp < 0) {
//System.out.println("skGREATER");
                    return NOT_FOUND_IS_GREATER;
                } else if (cmp == 0)
                    return getNode(kidsReferences.size() - 1).search(name);
            }
//System.out.println("skBETWEEN");

            return binarySearchKids(0, kidsReferences.size() - 1, name);
        } else if (namesAndValues != null) {
//System.out.print("search()  names ... ");
            int numNamesAndValues = namesAndValues.size();

            if (lowerLimit != null) {
                int cmp = lowerLimit.compareTo(name);
                if (cmp > 0) {
//System.out.println("snLESSER");
                    return NOT_FOUND_IS_LESSER;
                } else if (cmp == 0) {
                    ensureNamesDecrypted();
                    if (namesAndValues.get(0).equals(name)) {
                        Object ob = namesAndValues.get(1);
                        if (ob instanceof Reference)
                            ob = library.getObject((Reference) ob);
                        return ob;
                    }
                }
            }
            if (upperLimit != null) {
                int cmp = upperLimit.compareTo(name);
                if (cmp < 0) {
//System.out.println("snGREATER");
                    return NOT_FOUND_IS_GREATER;
                } else if (cmp == 0) {
                    ensureNamesDecrypted();
                    if (namesAndValues.get(numNamesAndValues - 2).equals(name)) {
                        Object ob = namesAndValues.get(numNamesAndValues - 1);
                        if (ob instanceof Reference)
                            ob = library.getObject((Reference) ob);
                        return ob;
                    }
                }
            }
//System.out.println("snBETWEEN");

            ensureNamesDecrypted();
            Object ret = binarySearchNames(0, numNamesAndValues - 1, name);
            if (ret == NOT_FOUND || ret == NOT_FOUND_IS_LESSER || ret == NOT_FOUND_IS_GREATER)
                ret = null;
            return ret;
        }
        return null;
    }

    private Object binarySearchKids(int firstIndex, int lastIndex, String name) {
        if (firstIndex > lastIndex)
            return NOT_FOUND;
        int pivot = firstIndex + ((lastIndex - firstIndex) / 2);
        Object ret = getNode(pivot).search(name);
//System.out.print("binarySearchKids  [ " + firstIndex + ", " + lastIndex + " ]  pivot: " + pivot + "  name: " + name + " ... ");
        if (ret == NOT_FOUND_IS_LESSER) {
//System.out.println("kLESSER");
            return binarySearchKids(firstIndex, pivot - 1, name);
        } else if (ret == NOT_FOUND_IS_GREATER) {
//System.out.println("kGREATER");
            return binarySearchKids(pivot + 1, lastIndex, name);
        } else if (ret == NOT_FOUND) {
//System.out.println("kNOT FOUND");
            // This shouldn't happen, so is either a bug, or a miss coded PDF file
            for (int i = firstIndex; i <= lastIndex; i++) {
                if (i == pivot)
                    continue;
                Object r = getNode(i).search(name);
                if (r != NOT_FOUND && r != NOT_FOUND_IS_LESSER && r != NOT_FOUND_IS_GREATER) {
                    ret = r;
                    break;
                }
            }
        }
        return ret;
    }

    private Object binarySearchNames(int firstIndex, int lastIndex, String name) {
        if (firstIndex > lastIndex)
            return NOT_FOUND;
        int pivot = firstIndex + ((lastIndex - firstIndex) / 2);
        pivot &= 0xFFFFFFFE; // Clear LSB to ensure even index
//System.out.print("binarySearchNames  [ " + firstIndex + ", " + lastIndex + " ]  pivot: " + pivot + "  size: " + namesAndValues.size() + "  compare  " + name + " to " + namesAndValues.get(pivot).toString() + " ... ");
        int cmp = namesAndValues.get(pivot).compareTo(name);
        if (cmp == 0) {
//System.out.println("nEQUAL");
            Object ob = namesAndValues.get(pivot + 1);
            if (ob instanceof Reference)
                ob = library.getObject((Reference) ob);
            return ob;
        } else if (cmp > 0) {
//System.out.println("nLESSER");
            return binarySearchNames(firstIndex, pivot - 1, name);
        } else if (cmp < 0) {
//System.out.println("nGREATER");
            return binarySearchNames(pivot + 2, lastIndex, name);
        }
        return NOT_FOUND;
    }

    public NameNode getNode(int index) {
        NameNode n = kidsNodes.get(index);
        if (n == null) {
            Reference r = (Reference) kidsReferences.get(index);
            HashMap nh = (HashMap) library.getObject(r);
            n = new NameNode(library, nh);
            kidsNodes.set(index, n);
        }
        return n;
    }
}
