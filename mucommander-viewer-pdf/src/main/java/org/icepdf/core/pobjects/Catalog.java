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

import org.icepdf.core.pobjects.acroform.InteractiveForm;
import org.icepdf.core.util.Library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>The <code>Catalog</code> object represents the root of a PDF document's
 * object heirarchy.  The <code>Catalog</code> is located by means of the
 * <b>Root</b> entry in the trailer of the PDF file.  The catalog contains
 * references to other objects defining the document's contents, outline, names,
 * destinations, and other attributes.</p>
 * <p/>
 * <p>The <code>Catalog</code> class can be accessed from the {@see Document}
 * class for convenience, but can also be accessed via the {@see PTrailer} class.
 * Useful information about the document can be extracted from the Catalog
 * Dictionary, such as PDF version information and Viewer Preferences.  All
 * Catalog dictionary properties can be accesed via the getEntries method.
 * See section 3.6.1 of the PDF Reference version 1.6 for more information on
 * the properties available in the Catalog Object. </p>
 *
 * @since 1.0
 */
public class Catalog extends Dictionary {

    private static final Logger logger =
            Logger.getLogger(Catalog.class.toString());

    public static final Name TYPE = new Name("Catalog");
    public static final Name DESTS_KEY = new Name("Dests");
    public static final Name VIEWERPREFERENCES_KEY = new Name("ViewerPreferences");
    public static final Name NAMES_KEY = new Name("Names");
    public static final Name OUTLINES_KEY = new Name("Outlines");
    public static final Name OCPROPERTIES_KEY = new Name("OCProperties");
    public static final Name PAGES_KEY = new Name("Pages");
    public static final Name PAGELAYOUT_KEY = new Name("PageLayout");
    public static final Name PAGEMODE_KEY = new Name("PageMode");
    public static final Name ACRO_FORM_KEY = new Name("AcroForm");
    public static final Name COLLECTION_KEY = new Name("Collection");
    public static final Name METADATA_KEY = new Name("Metadata");
    public static final Name PERMS_KEY = new Name("Perms");

    public static final Name PAGE_MODE_USE_NONE_VALUE = new Name("UseNone");
    public static final Name PAGE_MODE_USE_OUTLINES_VALUE = new Name("UseOutlines");
    public static final Name PAGE_MODE_USE_THUMBS_VALUE = new Name("UseThumbs");
    public static final Name PAGE_MODE_FULL_SCREEN_VALUE = new Name("FullScreen");
    public static final Name PAGE_MODE_OPTIONAL_CONTENT_VALUE = new Name("UseOC");
    public static final Name PAGE_MODE_USE_ATTACHMENTS_VALUE = new Name("UseAttachments");

    private PageTree pageTree;
    private Outlines outlines;
    private Names names;
    private OptionalContent optionalContent;
    private NamedDestinations dests;
    private ViewerPreferences viewerPref;
    private InteractiveForm interactiveForm;

    private boolean outlinesInited = false;
    private boolean namesTreeInited = false;
    private boolean destsInited = false;
    private boolean viewerPrefInited = false;
    private boolean optionalContentInited = false;

    // Announce ICEpdf Core
    static {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("ICEsoft ICEpdf Core " + Document.getLibraryVersion());
        }
    }

    /**
     * Creates a new instance of a Catalog.
     *
     * @param l document library.
     * @param h Catalog dictionary entries.
     */
    public Catalog(Library l, HashMap<Object, Object> h) {
        super(l, h);
    }

    /**
     * Initiate the PageTree.
     */
    public synchronized void init() throws InterruptedException {
        Object tmp = library.getObject(entries, PAGES_KEY);
        pageTree = null;
        if (tmp instanceof PageTree) {
            pageTree = (PageTree) tmp;
        }
        // malformed core corner case, pages must not be references, but we
        // have a couple cases that break the spec.
        else if (tmp instanceof HashMap) {
            pageTree = new PageTree(library, (HashMap) tmp);
        }
        // malformed corner case, just have a page object, instead of tree.
        else if (tmp instanceof Page) {
            Page tmpPage = (Page) tmp;
            HashMap<String, Object> tmpPages = new HashMap<String, Object>();
            List<Reference> kids = new ArrayList<Reference>();
            kids.add(tmpPage.getPObjectReference());
            tmpPages.put("Kids", kids);
            tmpPages.put("Count", 1);
            pageTree = new PageTree(library, tmpPages);
        }

        // let any exception bubble up.
        if (pageTree != null) {
            pageTree.init();
        }

        // check for the collections dictionary for the presence of a portable collection
        tmp = library.getObject(entries, NAMES_KEY);
        if (tmp != null) {
            names = new Names(library, (HashMap) tmp);
            names.init();
        }

        // load the Acroform data.
        tmp = library.getObject(entries, ACRO_FORM_KEY);
        if (tmp instanceof HashMap) {
            interactiveForm = new InteractiveForm(library, (HashMap) tmp);
            interactiveForm.init();
        }
        // todo namesTree contains forms javascript, might need to be initialized here

    }

    /**
     * Gets PageTree node that is the root of the document's page tree.
     * The PageTree can be traversed to access child PageTree and Page objects.
     *
     * @return Catalogs PageTree.
     * @see org.icepdf.core.pobjects.Page
     */
    public PageTree getPageTree() {
        return pageTree;
    }

    /**
     * Gets the Outlines Dictionary that is the root of the document's outline
     * hierarchy. The Outline can be traversed to access child OutlineItems.
     *
     * @return Outlines object if one exists; null, otherwise.
     * @see org.icepdf.core.pobjects.OutlineItem
     */
    public Outlines getOutlines() {
        if (!outlinesInited) {
            outlinesInited = true;
            Object o = library.getObject(entries, OUTLINES_KEY);
            if (o != null)
                outlines = new Outlines(library, (HashMap) o);
        }
        return outlines;
    }

    /**
     * A collection dictionary that a conforming reader shall use to enhance the presentation of file attachments
     * stored in the PDF document.
     *
     * @return collection dictionary.
     */
    public HashMap getCollection() {
        return library.getDictionary(entries, COLLECTION_KEY);
    }

    /**
     * A name object specifying how the document shall be displayed when opened:
     *
     * @return one of the PageMode value contants,  default is Default value: UseNone.
     */
    public Name getPageMode() {
        Name name = library.getName(entries, PAGEMODE_KEY);
        if (name == null) {
            return PAGE_MODE_USE_NONE_VALUE;
        } else {
            return name;
        }
    }

    /**
     * Gets the document's Names dictionary.  The Names dictionary contains
     * a category of objects in a PDF file which can be referred to by name
     * rather than by object reference.
     *
     * @return names object entry.  If no names entries exists null
     * is returned.
     */
    public Names getNames() {
        return names;
    }

    /**
     * Gets the Names object's embedded files name tree if present.  The root node is also check to make sure
     * the tree has values.
     *
     * @return A name tree mapping name strings to file specifications for embedded
     * file streams.
     */
    public NameTree getEmbeddedFilesNameTree() {
        if (names != null) {
            NameTree nameTree = names.getEmbeddedFilesNameTree();
            if (nameTree != null && nameTree.getRoot() != null) {
                return nameTree;
            }
        }
        return null;
    }


    /**
     * Gets a dictionary of names and corresponding destinations.
     *
     * @return A Dictionary of Destinations; if none, null is returned.
     */
    @SuppressWarnings("unchecked")
    public NamedDestinations getDestinations() {
        if (!destsInited) {
            destsInited = true;
            Object o = library.getObject(entries, DESTS_KEY);
            if (o != null) {
                dests = new NamedDestinations(library, (HashMap<Object, Object>) o);
            }
        }
        return dests;
    }

    /**
     * Gets a dictionary of keys and corresponding viewer preferences
     * This can be used to pull information based on the PDF specification,
     * such as HideToolbar or FitWindow
     *
     * @return the constructed ViewerPreferences object
     */
    public ViewerPreferences getViewerPreferences() {
        if (!viewerPrefInited) {
            viewerPrefInited = true;
            Object o = library.getObject(entries, VIEWERPREFERENCES_KEY);
            if (o != null) {
                viewerPref = new ViewerPreferences(library, (HashMap) o);
                viewerPref.init();
            }
        }
        return viewerPref;
    }

    /**
     * Gets the the optional content properties dictionary if present.
     *
     * @return OptionalContent dictionary, null if none exists.
     */
    public OptionalContent getOptionalContent() {
        if (!optionalContentInited) {
            optionalContentInited = true;
            Object o = library.getObject(entries, OCPROPERTIES_KEY);
            if (o != null && o instanceof HashMap) {
                optionalContent = new OptionalContent(library, ((HashMap) o));
                optionalContent.init();
            } else {
                optionalContent = new OptionalContent(library, new HashMap());
                optionalContent.init();
            }
        }
        return optionalContent;
    }

    /**
     * A metadata stream that shall contain metadata for the document.  To
     * access the metadata stream data make a call to getMetData().getDecodedStreamBytes()
     * which can be used to create a String or open an InputStream.
     *
     * @return metadata stream if define,  otherwise null.
     */
    public Stream getMetaData() {
        Object o = library.getObject(entries, METADATA_KEY);
        if (o != null && o instanceof Stream) {
            return (Stream) o;
        }
        return null;
    }

    /**
     * Gets the permissions of the catalog if present. Perms key.
     *
     * @return permissions if present, otherwise false.
     */
    public Permissions getPermissions() {
        HashMap hashMap = library.getDictionary(entries, PERMS_KEY);
        if (hashMap != null) {
            return new Permissions(library, hashMap);
        } else {
            return null;
        }
    }

    /**
     * Gets the interactive form object that contains the form widgets for the given PDF.
     *
     * @return interactive form object,  null if no forms are pressent.
     */
    public InteractiveForm getInteractiveForm() {
        return interactiveForm;
    }

    /**
     * Returns a summary of the Catalog dictionary values.
     *
     * @return dictionary values.
     */
    public String toString() {
        return "CATALOG= " + entries.toString();
    }
}
