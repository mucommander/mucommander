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
package org.icepdf.ri.common.search;

import org.icepdf.core.pobjects.graphics.text.PageText;
import org.icepdf.core.search.SearchTerm;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * Document search model is used by the DocumentSearchController as a way to
 * cache search results.  PDF document in general can be to big to keep all
 * data in memory at once.  ICEpdf uses a dispose/initialization on Page data
 * as memory is needed.  When this happens the selected text data will be lost
 * and we have to research the page in question the next time it is viewed.
 * <p/>
 * This model can be used in two ways; the first is to store search terms and
 * the second is to keep a record of page indexes that have search results.  The
 * later uses weak references to make sure we don't leak any memory.  If the
 * page has no data available in its weak reference then we can get it.  If
 * a there is no value for a given page index then we know the page had no
 * search hits and we can ignore it.
 *
 * @since 4.0
 */
public class DocumentSearchModelImpl {

    // cache to detect page dispose/initialize cycle so that we can research when
    // needed.
    private HashMap<Integer, WeakReference<PageText>> searchResultCache;

    // list of terms that made up the full search, usually just one, but
    // you never know.
    private ArrayList<SearchTerm> searchTerms;

    /**
     * Creates a new instance with empty search terms and search result caches.
     */
    public DocumentSearchModelImpl() {
        searchResultCache = new HashMap<Integer, WeakReference<PageText>>(256);
        searchTerms = new ArrayList<SearchTerm>();
    }

    /**
     * Gets a list of search terms that make up a given search.
     *
     * @return list of search term, maybe empty but not null.
     */
    public ArrayList<SearchTerm> getSearchTerms() {
        return searchTerms;
    }

    /**
     * Add a search term to the model.
     *
     * @param searchTerm search term, no checking is done for invalid data.
     */
    public void addSearchTerm(SearchTerm searchTerm) {
        searchTerms.add(searchTerm);
    }

    /**
     * Remove the specified search term from the model.
     *
     * @param searchTerm search term to remove.
     */
    public void removeSearchTerm(SearchTerm searchTerm) {
        searchTerms.remove(searchTerm);
    }

    /**
     * Add a search result hit for a given page.  A page can have 1 or more
     * hits but all that matters is that there is at least one hit to manage.
     * The index and PageText Object is stored in the cache.
     *
     * @param pageIndex page index of search hit(s)
     * @param pageText  PageText for the given page index.
     */
    public void addPageSearchHit(int pageIndex, PageText pageText) {
        searchResultCache.put(pageIndex, new WeakReference<PageText>(pageText));
    }

    /**
     * Gets a set of page hit page indexes.  That is to say a list of all page
     * indexes that have search hits.  This list can be used to clear searches
     * or to iterate more quickly of the results set.
     *
     * @return set of page indexes that have a least one search result hit.
     */
    public Set<Integer> getPageSearchHits() {
        return searchResultCache.keySet();
    }

    /**
     * Check the page index to see if there is a search result.
     *
     * @param pageIndex index of page to search
     * @return true if page has search result, false otherwise.
     */
    public boolean isPageSearchHit(int pageIndex) {
        return searchResultCache.get(pageIndex) != null;
    }

    public PageText getPageTextHit(int pageIndex) {
        WeakReference<PageText> ref = searchResultCache.get(pageIndex);
        if (ref.get() != null) {
            return ref.get();
        } else {
            return null;
        }
    }

    /**
     * When we know a page has a hit but aren't sure if pageText is still in a
     * highlighted state we can use this method to check. If the weak reference
     * for the pageIndex exists we can compare it against the parameter pageText.
     * If the objects are equal there is no need to search again.  If reference
     * is null or not equal then we have to do the search again to get the
     * highlight state back.  Pages searches in general are extremely fast and
     * performs better then trying to keep everything in memory.
     * <p/>
     *
     * @param pageIndex page index to look at PageText results
     * @param pageText  current Page objects PageText object we want to check
     *                  against whats in the cache.
     * @return false if the search for this page should be done again, otherwise
     *         true then we should be ok and don't need to refresh the text state.
     */
    public boolean isPageTextMatch(int pageIndex, PageText pageText) {
        WeakReference<PageText> ref = searchResultCache.get(pageIndex);
        if (ref == null) {
            return false;
        }
        PageText matchText = ref.get();
        return matchText == null || !matchText.equals(pageText);
    }

    /**
     * Clears cached search results for this page index and clears the highlighted
     * state for this page.
     *
     * @param page page index to clear search results from.
     */
    public void clearSearchResults(int page) {
        // clear highlighted state for this page index. 
        WeakReference<PageText> pageReference = searchResultCache.get(page);
        if (pageReference != null) {
            PageText currentPageText = pageReference.get();
            if (currentPageText != null) {
                currentPageText.clearHighlighted();
            }
        }
        // clear caches.
        searchResultCache.remove(page);
    }

    /**
     * Clears all search results and highlight states found in the research
     * results cache. This method is especially useful for large documents.
     */
    public void clearSearchResults() {

        // reset highlights
        // get list of searched results and clear pages.
        Collection<WeakReference<PageText>> pagTextHits = searchResultCache.values();
        PageText currentPageText;
        for (WeakReference<PageText> pageIndex : pagTextHits) {
            currentPageText = pageIndex.get();
            if (currentPageText != null) {
                currentPageText.clearHighlighted();
            }
        }
        // clear caches.
        searchResultCache.clear();
        searchTerms.clear();
    }
}
