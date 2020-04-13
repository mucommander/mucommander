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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>The <code>Destination</code> class defines a particular view of a
 * PDF document consisting of the following items:</p>
 * <ul>
 * <li>The page of the document to be displayed.</li>
 * <li>The location of the document window on that page.</li>
 * <li>The magnification (zoom) factor to use when displaying the page
 * Destinations may be associated with outline items, annotations,
 * or actions. </li>
 * </ul>
 * <p>Destination can be associated with outline items, annotations, or actions.
 * In each case the destination specifies the view of the document to be presented
 * when one of the respective objects is activated.</p>
 * <p>The Destination class currently only supports the Destination syntaxes,
 * [page /XYZ left top zoom], other syntax will be added in future releases. The
 * syntax [page /XYZ left top zoom] is defined as follows:</p>
 * <ul>
 * <li>page - designated page to show (Reference to a page).</li>
 * <li>/XYZ - named format of destination syntax.</li>
 * <li>left - x value of the upper left-and coordinate.</li>
 * <li>top - y value of the upper left-hand coordinate.</li>
 * <li>zoom - zoom factor to magnify page by.</li>
 * </ul>
 * <p>A null value for left, top or zoom specifies that the current view values
 * will be unchanged when navigating to the specified page. </p>
 *
 * @see org.icepdf.core.pobjects.annotations.Annotation
 * @see org.icepdf.core.pobjects.OutlineItem
 * @see org.icepdf.core.pobjects.actions.Action
 * @since 1.0
 */
public class Destination {

    private static final Logger logger =
            Logger.getLogger(Destination.class.toString());

    public static final Name D_KEY = new Name("D");
    // Vector destination type formats.
    public static final Name TYPE_XYZ = new Name("XYZ");
    public static final Name TYPE_FIT = new Name("Fit");
    public static final Name TYPE_FITH = new Name("FitH");
    public static final Name TYPE_FITV = new Name("FitV");
    public static final Name TYPE_FITR = new Name("FitR");
    public static final Name TYPE_FITB = new Name("FitB");
    public static final Name TYPE_FITBH = new Name("FitBH");
    public static final Name TYPE_FITBV = new Name("FitBV");

    // library of all PDF document objects
    private Library library;

    // object containing all of the destinations parameters
    private Object object;

    // Reference object for destination
    private Reference ref;

    // type, /XYZ, /Fit, /FitH... 
    private Name type;

    // Specified by /XYZ in the core, /(left)(top)(zoom)
    private Float left = null;
    private Float bottom = null;
    private Float right = null;
    private Float top = null;
    private Float zoom = null;

    // named Destination name, can be a name or String
    private Name namedDestination;

    // initiated flag
    private boolean inited;

    /**
     * Creates a new instance of a Destination.
     *
     * @param l document library.
     * @param h Destination dictionary entries.
     */
    public Destination(Library l, Object h) {
        library = l;
        object = h;
        init();
    }

    /**
     * Initiate the Destination. Retrieve any needed attributes.
     */
    private void init() {

        // check for initiation
        if (inited) {
            return;
        }
        inited = true;

        // if vector we have found /XYZ
        if (object instanceof List) {
            parse((List) object);
        }

        // find named Destinations, this however is incomplete
        // @see #parser for more detailed information
        else if (object instanceof Name || object instanceof StringObject) {
            String s;
            // Make sure to decrypt this attribute
            if (object instanceof StringObject) {
                StringObject stringObject = (StringObject) object;
                s = stringObject.getDecryptedLiteralString(library.getSecurityManager());
            } else {
                s = object.toString();
            }

            // store the name
            namedDestination = new Name(s);

            boolean found = false;
            Catalog catalog = library.getCatalog();
            if (catalog != null && catalog.getNames() != null) {
                NameTree nameTree = catalog.getNames().getDestsNameTree();
                if (nameTree != null) {
                    Object o = nameTree.searchName(s);
                    if (o != null) {
                        if (o instanceof List) {
                            parse((List) o);
                            found = true;
                        } else if (o instanceof HashMap) {
                            HashMap h = (HashMap) o;
                            Object o1 = h.get(D_KEY);
                            if (o1 instanceof List) {
                                parse((List) o1);
                                found = true;
                            }
                        }
                    }
                }
                if (!found) {
                    Dictionary dests = catalog.getDestinations();
                    if (dests != null) {
                        Object ob = dests.getObject((Name) object);
                        // list of destinations name->Dest pairs.
                        if (ob instanceof List) {
                            parse((List) ob);
                        }
                        // corner case for d attached list.
                        else if (ob instanceof HashMap) {
                            parse((List) (((HashMap) ob).get(D_KEY)));
                        } else {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.warning("Destination type missed=" + ob);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get the dictionary object, name, string or array.
     *
     * @return
     */
    public Object getObject() {
        return object;
    }

    /**
     * Utility method for parsing the Destination attributes
     *
     * @param v vector of attributes associated with the Destination
     */
    private void parse(List v) {

        if (v == null) return;

        // Assign a Reference
        Object ob = getDestValue(0, v);
        if (ob instanceof Reference) {
            ref = (Reference) ob;
        }
        // store type.
        ob = getDestValue(1, v);
        if (ob instanceof Name) {
            type = (Name) ob;
        } else if (ob != null) {
            type = new Name(ob.toString());
        }
        // [page /XYZ left top zoom ]
        if (TYPE_XYZ.equals(type)) {
            ob = getDestValue(2, v);
            if (ob != null && !ob.equals("null")) {
                left = ((Number) ob).floatValue();
            }
            ob = getDestValue(3, v);
            if (ob != null && !ob.equals("null")) {
                top = ((Number) ob).floatValue();
            }
            // zoom can be a value but zero and null are treated as no zoom change.
            ob = getDestValue(4, v);
            if (ob != null && !ob.equals("null") && !ob.equals("0")) {
                zoom = ((Number) ob).floatValue();
            }
        }
        // [page /FitH top]
        else if (TYPE_FITH.equals(type)) {
            ob = getDestValue(2, v);
            if (ob != null && !ob.equals("null")) {
                top = ((Number) ob).floatValue();
            }
        }
        // [page /FitR left bottom right top]
        else if (TYPE_FITR.equals(type)) {
            ob = getDestValue(2, v);
            if (ob != null && !ob.equals("null")) {
                left = ((Number) ob).floatValue();
            }
            ob = getDestValue(3, v);
            if (ob != null && !ob.equals("null")) {
                bottom = ((Number) ob).floatValue();
            }
            ob = getDestValue(4, v);
            if (ob != null && !ob.equals("null")) {
                right = ((Number) ob).floatValue();
            }
            ob = getDestValue(5, v);
            if (ob != null && !ob.equals("null")) {
                top = ((Number) ob).floatValue();
            }
        }
        // [page /FitB]
        else if (TYPE_FITB.equals(type)) {
            // nothing to parse
        }
        // [page /FitBH top]
        else if (TYPE_FITBH.equals(type)) {
            ob = getDestValue(2, v);
            if (ob != null && !ob.equals("null")) {
                top = ((Number) ob).floatValue();
            }
        }
        // [page /FitBV left]
        else if (TYPE_FITBV.equals(type)) {
            ob = getDestValue(2, v);
            if (ob != null && !ob.equals("null")) {
                left = ((Number) ob).floatValue();
            }
        }
    }

    // utility to avoid indexing issues with malformed dest type formats.
    private static Object getDestValue(int index, List params) {
        if (params.size() > index) {
            return params.get(index);
        }
        return null;
    }

    /**
     * Gets the name of the named destination.
     *
     * @return name of destination if present, null otherwise.
     */
    public Name getNamedDestination() {
        return namedDestination;
    }

    /**
     * Sets the named destination as a Named destination.  It is assumed
     * the named destination already exists in the document.
     *
     * @param dest destination to associate with.
     */
    public void setNamedDestination(Name dest) {
        namedDestination = dest;
        // only write out destination as names so we don't have worry about
        // encryption.
        object = dest;
        // re-parse as object should point to a new destination.
        inited = false;
        init();
    }

    /**
     * Sets the destination syntax to the specified value.  The Destinatoin
     * object clears the named destination and re initializes itself after the
     * assignment has been made.
     *
     * @param destinationSyntax new vector of destination syntax.
     */
    public void setDestinationSyntax(List destinationSyntax) {
        // clear named destination
        namedDestination = null;
        object = destinationSyntax;
        // re-parse as object should point to a new destination.
        inited = false;
        init();
    }

    /**
     * Utility for creating a /Fit or FitB syntax vector.
     *
     * @param page destination page pointer.
     * @param type type of destionation
     * @return new instance of vector containing well formed destination syntax.
     */
    public static List<Object> destinationSyntax(
            Reference page, final Name type) {
        List<Object> destSyntax = new ArrayList<Object>(2);
        destSyntax.add(page);
        destSyntax.add(type);
        return destSyntax;
    }

    /**
     * Utility for creating a /FitH, /FitV, /FitBH or /FitBV syntax vector.
     *
     * @param page   destination page pointer.
     * @param type   type of destionation
     * @param offset offset coordinate value in page space for specified dest type.
     * @return new instance of vector containing well formed destination syntax.
     */
    public static List<Object> destinationSyntax(
            Reference page, final Name type, Object offset) {
        List<Object> destSyntax = new ArrayList<Object>(3);
        destSyntax.add(page);
        destSyntax.add(type);
        destSyntax.add(offset);
        return destSyntax;
    }

    /**
     * Utility for creating a /XYZ syntax vector.
     *
     * @param page destination page pointer.
     * @param type type of destionation
     * @param left offset coordinate value in page space for specified dest type.
     * @param top  offset coordinate value in page space for specified dest type.
     * @param zoom page zoom, 0 or null indicates no zoom.
     * @return new instance of vector containing well formed destination syntax.
     */
    public static List<Object> destinationSyntax(
            Reference page, final Object type, Object left, Object top, Object zoom) {
        List<Object> destSyntax = new ArrayList<Object>(5);
        destSyntax.add(page);
        destSyntax.add(type);
        destSyntax.add(left);
        destSyntax.add(top);
        destSyntax.add(zoom);
        return destSyntax;
    }

    /**
     * Utility for creating a /FitR syntax vector.
     *
     * @param page   destination page pointer.
     * @param type   type of destionation
     * @param left   offset coordinate value in page space for specified dest type.
     * @param top    offset coordinate value in page space for specified dest type.
     * @param bottom offset coordinate value in page space for specified dest type.
     * @param right  offset coordinate value in page space for specified dest type.
     * @return new instance of vector containing well formed destination syntax.
     */
    public static List<Object> destinationSyntax(
            Reference page, final Object type, Object left, Object bottom,
            Object right, Object top) {
        List<Object> destSyntax = new ArrayList<Object>(6);
        destSyntax.add(page);
        destSyntax.add(type);
        destSyntax.add(left);
        destSyntax.add(bottom);
        destSyntax.add(right);
        destSyntax.add(top);
        return destSyntax;
    }

    /**
     * Gets the Page Reference specified by the destination.
     *
     * @return a Reference to the Page Object associated with this destination.
     */
    public Reference getPageReference() {
        return ref;
    }

    /**
     * Gets the left offset from the top, left position of the page specified by
     * this destination.
     *
     * @return the left offset from the top, left position  of the page.  If not
     * specified Float.NaN is returned.
     */
    public Float getLeft() {
        return left;
    }

    /**
     * Gets the top offset from the top, left position of the page specified by
     * this destination.
     *
     * @return the top offset from the top, left position of the page.  If not
     * specified Float.NaN is returned.
     */
    public Float getTop() {
        return top;
    }

    /**
     * Gets the zoom level specifed by the destination.
     *
     * @return the specified zoom level, Float.NaN if not specified.
     */
    public Float getZoom() {
        return zoom;
    }

    /**
     * Gets the page reference represented by this destination
     *
     * @return reference of page that destination should show when executed.
     */
    public Reference getRef() {
        return ref;
    }

    /**
     * Gets the type used in a vector of destination syntax.  Will be null
     * if a named destination is used.
     *
     * @return type of destination syntax as defined by class constants.
     */
    public Name getType() {
        return type;
    }

    /**
     * Bottom coordinate of a zoom box, if present left, right and top should
     * also be available.
     *
     * @return bottom coordinate of magnifcation box.
     */
    public Float getBottom() {
        return bottom;
    }

    /**
     * Right coordinate of a zoom box, if present bottom, left and top should
     * also be available.
     *
     * @return rigth coordinate of zoom box
     */
    public Float getRight() {
        return right;
    }

    /**
     * Get the destination properties encoded in post script form.
     *
     * @return either a destination Name or a Vector representing the
     * destination
     */
    public Object getEncodedDestination() {
        // write out the destination name
        if (namedDestination != null) {
            return namedDestination;
        }
        // build and return a fector of changed valued.
        else if (object instanceof List) {
            List<Object> v = new ArrayList<Object>(7);
            if (ref != null) {
                v.add(ref);
            }
            // named dest type
            if (type != null) {
                v.add(type);
            }
            // left
            if (left != Float.NaN) {
                v.add(left);
            }
            // bottom
            if (bottom != Float.NaN) {
                v.add(bottom);
            }
            // right
            if (right != Float.NaN) {
                v.add(right);
            }
            // top
            if (top != Float.NaN) {
                v.add(top);
            }
            // zoom
            if (zoom != Float.NaN) {
                v.add(zoom);
            }
            return v;
        }
        return null;
    }

    /**
     * Returns a summary of the annotation dictionary values.
     *
     * @return dictionary values.
     */
    public String toString() {
        return "Destination  ref: " + getPageReference() + " ,  top: " +
                getTop() + " ,  left: " + getLeft() + " ,  zoom: " + getZoom();
    }
}



