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

import org.icepdf.core.pobjects.graphics.WatermarkCallback;
import org.icepdf.core.util.Library;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

/**
 * <p>This class represents a document's page tree which defines the ordering
 * of pages in the document.  The tree structure allows a user to quickly
 * open a document containing thousands of pages.  The tree contains nodes of
 * two types, page tree nodes and page nodes, where page tree nodes are
 * intermediate nodes and pages are leaves.  A simple example of this tree structure
 * is a single page tree node that references all of the document's page
 * objects directly.</p>
 * <p/>
 * <p>The page tree is accessible via the document catalog and can be traversed
 * to display a desired page or extracts its content.<p>
 *
 * @see org.icepdf.core.pobjects.Page
 * @see org.icepdf.core.pobjects.Catalog
 * @since 2.0
 */
public class PageTree extends Dictionary {

    public static final Name TYPE = new Name("Pages");
    public static final Name PARENT_KEY = new Name("Parent");
    public static final Name COUNT_KEY = new Name("Count");
    public static final Name MEDIABOX_KEY = new Name("MediaBox");
    public static final Name CROPBOX_KEY = new Name("CropBox");
    public static final Name KIDS_KEY = new Name("Kids");
    public static final Name ROTATE_KEY = new Name("Rotate");
    public static final Name RESOURCES_KEY = new Name("Resources");

    // Number of leaf nodes
    private int kidsCount = 0;
    // vector of references to leafs
    private List kidsReferences;
    // vector of the pages associated with tree
    private HashMap<Integer, WeakReference<Object>> kidsPageAndPages;
    // pointer to parent page tree
    private PageTree parent;
    // initiated flag
    private boolean inited;
    // inheritable page boundary data.
    private PRectangle mediaBox;
    private PRectangle cropBox;
    // inheritable Resources
    private Resources resources;
    // loaded resource flag, we can't use null check as some trees don't have
    // resources. 
    private boolean loadedResources;
    private WatermarkCallback watermarkCallback;

    /**
     * Inheritable rotation factor by child pages.
     */
    protected float rotationFactor = 0;

    /**
     * Indicates that the PageTree has a rotation factor which should be respected.
     */
    protected boolean isRotationFactor = false;

    /**
     * Creates a new instance of a PageTree.
     *
     * @param l document library.
     * @param h PageTree dictionary entries.
     */
    public PageTree(Library l, HashMap h) {
        super(l, h);
    }

    /**
     * Reset the pages initialized flag and as a result subsequent calls to
     * this PageTree may trigger a call to init().
     */
    public void resetInitializedState() {
        inited = false;
    }

    /**
     * Initiate the PageTree.
     */
    public synchronized void init() {
        if (inited) {
            return;
        }

        Object parentTree = library.getObject(entries, PARENT_KEY);
        if (parentTree instanceof PageTree) {
            parent = (PageTree) parentTree;
        }
        kidsCount = library.getNumber(entries, COUNT_KEY).intValue();
        kidsReferences = (List) library.getObject(entries, KIDS_KEY);
        kidsPageAndPages = new HashMap<Integer, WeakReference<Object>>(kidsReferences.size());
        // Rotation is only respected if child pages do not have their own
        // rotation value.
        Object tmpRotation = library.getObject(entries, ROTATE_KEY);
        if (tmpRotation != null) {
            rotationFactor = ((Number) tmpRotation).floatValue();
            // mark that we have an inheritable value
            isRotationFactor = true;
        }
        inited = true;

    }

    /**
     * Gets the media box boundary defined by this page tree.  The media box is a
     * required page entry and can be inherited from its parent page tree.
     *
     * @return media box boundary in user space units.
     */
    public PRectangle getMediaBox() {
        if (!inited) {
            init();
        }

        if (mediaBox != null) {
            return mediaBox;
        }
        // add all of the pages media box dimensions to a vector and process
        List boxDimensions = (List) (library.getObject(entries, MEDIABOX_KEY));
        if (boxDimensions != null) {
            mediaBox = new PRectangle(boxDimensions);
        }
        // If mediaBox is null check with the parent pages, as media box is inheritable
        if (mediaBox == null) {
            PageTree pageTree = getParent();
            while (pageTree != null && mediaBox == null) {
                mediaBox = pageTree.getMediaBox();
                if (mediaBox == null) {
                    pageTree = pageTree.getParent();
                }
            }
        }
        return mediaBox;
    }

    /**
     * Gets the crop box boundary defined by this page tree.  The media box is a
     * required page entry and can be inherited from its parent page tree.
     *
     * @return crop box boundary in user space units.
     */
    public PRectangle getCropBox() {
        if (!inited) {
            init();
        }

        if (cropBox != null) {
            return cropBox;
        }
        // add all of the pages crop box dimensions to a vector and process
        List boxDimensions = (List) (library.getObject(entries, CROPBOX_KEY));
        if (boxDimensions != null) {
            cropBox = new PRectangle(boxDimensions);
        }
        // Default value of the cropBox is the MediaBox if not set implicitly
        PRectangle mediaBox = getMediaBox();
        if (cropBox == null && mediaBox != null) {
            cropBox = (PRectangle) mediaBox.clone();
        } else if (mediaBox != null) {
            // PDF 1.5 spec states that the media box should be intersected with the
            // crop box to get the new box. But we only want to do this if the
            // cropBox is not the same as the mediaBox
            cropBox = mediaBox.createCartesianIntersection(cropBox);
        }
        return cropBox;
    }

    /**
     * Gets the Resources defined by this PageTree.  The Resources entry can
     * be inherited by the child Page objects.
     * <p/>
     * The caller is responsible for disposing of the returned Resources object.
     *
     * @return Resources associates with the PageTree
     */
    public synchronized Resources getResources() {
        // make sure we synchronize this to avoid a false resource grab.
        if (!loadedResources) {
            loadedResources = true;
            resources = library.getResources(entries, RESOURCES_KEY);
        }
        return resources;
    }

    /**
     * Gets the page tree node that is the immediate parent of this one.
     *
     * @return parent page tree;  null, if this is the root page tree.
     */
    public PageTree getParent() {
        return parent;
    }

    /**
     * Gets the page number of the page specifed by a reference.
     *
     * @param r reference to a page in the page tree.
     * @return page number of the specified reference.  If no page is found, -1
     * is returned.
     */
    public int getPageNumber(Reference r) {
        Page pg = (Page) library.getObject(r);
        if (pg == null)
            return -1;
//        pg.init();
        int globalIndex = 0;
        Reference currChildRef = r;
        Reference currParentRef = pg.getParentReference();
        PageTree currParent = pg.getParent();
        while (currParentRef != null && currParent != null) {
            currParent.init();
            int refIndex = currParent.indexOfKidReference(currChildRef);
            if (refIndex < 0)
                return -1;
            int localIndex = 0;
            for (int i = 0; i < refIndex; i++) {
                Object pageOrPages = currParent.getPageOrPagesPotentiallyNotInitedFromReferenceAt(i);
                if (pageOrPages instanceof Page) {
                    localIndex++;
                } else if (pageOrPages instanceof PageTree) {
                    PageTree peerPageTree = (PageTree) pageOrPages;
                    peerPageTree.init();
                    localIndex += peerPageTree.getNumberOfPages();
                }
            }
            globalIndex += localIndex;
            currChildRef = currParentRef;
            currParentRef = (Reference) currParent.entries.get(PARENT_KEY);
            currParent = currParent.parent;
        }
        return globalIndex;
    }

    /**
     * Utility method for getting kid index.
     *
     * @param r
     * @return
     */
    private int indexOfKidReference(Reference r) {
        for (int i = 0; i < kidsReferences.size(); i++) {
            Reference ref = (Reference) kidsReferences.get(i);
            if (ref.equals(r))
                return i;
        }
        return -1;
    }

    /**
     * Utility method for initializing a page in the page tree.
     *
     * @param index index in the kids vector to initialize
     * @return
     */
    private Object getPageOrPagesPotentiallyNotInitedFromReferenceAt(int index) {
        WeakReference<Object> pageOrPages = kidsPageAndPages.get(index);
        if (pageOrPages == null || pageOrPages.get() == null) {
            Reference ref = (Reference) kidsReferences.get(index);
            Object tmp = library.getObject(ref);
            pageOrPages = new WeakReference<Object>(tmp);
            kidsPageAndPages.put(index, pageOrPages);
            return tmp;
        }
        return pageOrPages.get();
    }

    /**
     * Utility method for initializing a page with its page number
     *
     * @param globalIndex
     * @return
     */
    private Page getPagePotentiallyNotInitedByRecursiveIndex(int globalIndex) {
        int globalIndexSoFar = 0;
        int numLocalKids = kidsReferences.size();
        Object pageOrPages;
        for (int i = 0; i < numLocalKids; i++) {
            pageOrPages = getPageOrPagesPotentiallyNotInitedFromReferenceAt(i);
            if (pageOrPages instanceof Page) {
                if (globalIndex == globalIndexSoFar)
                    return (Page) pageOrPages;
                globalIndexSoFar++;
            } else if (pageOrPages instanceof PageTree) {
                PageTree childPageTree = (PageTree) pageOrPages;
                childPageTree.init();
                int numChildPages = childPageTree.getNumberOfPages();
                if (globalIndex >= globalIndexSoFar && globalIndex < (globalIndexSoFar + numChildPages)) {
                    return childPageTree.getPagePotentiallyNotInitedByRecursiveIndex(
                            globalIndex - globalIndexSoFar);
                }
                globalIndexSoFar += numChildPages;
            }
            // corner case where pages didn't have "pages" key.
            else if (pageOrPages instanceof HashMap) {
                HashMap dictionary = (HashMap) pageOrPages;
                if (dictionary.containsKey(new Name("Kids"))) {
                    PageTree childPageTree = new PageTree(library, dictionary);
                    childPageTree.init();
                    int numChildPages = childPageTree.getNumberOfPages();
                    if (globalIndex >= globalIndexSoFar && globalIndex < (globalIndexSoFar + numChildPages)) {
                        return childPageTree.getPagePotentiallyNotInitedByRecursiveIndex(
                                globalIndex - globalIndexSoFar);
                    }
                    globalIndexSoFar += numChildPages;
                }

            }
        }
        return null;
    }

    /**
     * Sets a page watermark implementation to be painted on top of the page
     * content.  Watermark can be specified for each page or once by calling
     * document.setWatermark().
     *
     * @param watermarkCallback watermark implementation.
     */
    protected void setWatermarkCallback(WatermarkCallback watermarkCallback) {
        this.watermarkCallback = watermarkCallback;
    }

    /**
     * In a PDF file there is a root Pages object, which contains
     * children Page objects, as well as children PageTree objects,
     * all arranged in a tree.
     * getNumberOfPages() exists in every PageTree object, giving
     * the number of Page objects under it, recursively.
     * So, each PageTree object would have a different number of pages,
     * and only the root PageTree objects would have a number
     * representative of the whole Document.
     *
     * @return Total number of Page objects under this PageTree
     */
    public int getNumberOfPages() {
        return kidsCount;
    }

    /**
     * Gets a Page from the PDF file, locks it for the user,
     * initializes the Page, and returns it.
     * <p/>
     * ICEpdf uses a caching and memory management mechanism
     * to reduce the CPU, I/O, and time to access a Page,
     * which requires a locking and releasing protocol.
     * Calls to the <code>getPage</code> must be matched with
     * corresponding calls to <code>releasePage</code>.
     * Calls cannot be nested, meaning that <code>releasePage</code>
     * must be called before a subsequent invocation of
     * <code>getPage</code> for the same <code>pageIndex</code>.
     *
     * @param pageNumber Zero-based index of the Page to return.
     * @return The requested Page.
     */
    public Page getPage(int pageNumber) {
        if (pageNumber < 0)
            return null;
        Page page = getPagePotentiallyNotInitedByRecursiveIndex(pageNumber);
        // pass in the watermark, even null to wipe a previous watermark
        if (page != null) {
            page.setWatermarkCallback(watermarkCallback);
            page.setPageIndex(pageNumber);
        }
        return getPagePotentiallyNotInitedByRecursiveIndex(pageNumber);
    }

    /**
     * Get the page reference for the specified page number.
     *
     * @param pageNumber zero-based indox of page to find reference of.
     * @return found page reference or null if number could not be resolved.
     */
    public Reference getPageReference(int pageNumber) {
        if (pageNumber < 0)
            return null;
        Page p = getPagePotentiallyNotInitedByRecursiveIndex(pageNumber);
        if (p != null) {
            return p.getPObjectReference();
        }
        return null;
    }

    /**
     * Returns a summary of the PageTree dictionary values.
     *
     * @return dictionary values.
     */
    public String toString() {
        return "PAGES= " + entries.toString();
    }
}
