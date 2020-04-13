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

import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.graphics.text.LineText;
import org.icepdf.core.pobjects.graphics.text.PageText;
import org.icepdf.core.pobjects.graphics.text.WordText;
import org.icepdf.core.search.DocumentSearchController;
import org.icepdf.core.search.SearchTerm;
import org.icepdf.ri.common.SwingController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Document search controller used to manage document searches.  This class
 * class takes care of many of the performance issues of doing searches on
 * larges documents and is also used by PageViewComponentImpl to highlight
 * search results.
 * <p/>
 * This implementation uses simple search algorithm that will work well for most
 * users. This class can be extended and the method {@link #searchHighlightPage(int)}
 * can be overridden for custom search implementations.
 * <p/>
 * The DocumentSearchControllerImpl can be constructed to be used with the
 * Viewer RI source code via the constructor that takes a SwingController as
 * a parameter.  The second variation is ended for a headless environment where
 * Swing is not needed, the constructor for this instance takes a Document
 * as a parameter.
 *
 * @since 4.0
 */
public class DocumentSearchControllerImpl implements DocumentSearchController {

    private static final Logger logger =
            Logger.getLogger(DocumentSearchControllerImpl.class.toString());

    // search model contains caching and memory optimizations.
    protected DocumentSearchModelImpl searchModel;
    // parent controller used to get at RI controllers and models.
    protected SwingController viewerController;
    // assigned document for headless searching.
    protected Document document;

    /**
     * Create a news instance of search controller. A search model is created
     * for this instance.
     *
     * @param viewerController parent controller/mediator.
     */
    public DocumentSearchControllerImpl(SwingController viewerController) {
        this.viewerController = viewerController;
        searchModel = new DocumentSearchModelImpl();
    }

    /**
     * Create a news instance of search controller intended to be used in a
     * headless environment.  A search model is created for this instance.
     *
     * @param document document to search.
     */
    public DocumentSearchControllerImpl(Document document) {
        searchModel = new DocumentSearchModelImpl();
        this.document = document;
    }

    /**
     * Searches the given page using the specified term and properties.  The
     * search model is updated to store the pages Page text as a weak reference
     * which can be queried using isSearchHighlightNeeded to efficiently make
     * sure that a pages text is highlighted even after a dispose/init cycle.
     * If the text state is no longer present then the search should be executed
     * again.
     * <p/>
     * This method clears the search results for the page before it searches. If
     * you wish to have cumulative search results then searches terms should
     * be added with {@link #addSearchTerm(String, boolean, boolean)} and the
     * method {@link #searchPage(int)} should be called after each term is
     * added or after all have been added.
     *
     * @param pageIndex     page to search
     * @param caseSensitive if true use case sensitive searches
     * @param wholeWord     if true use whole word searches
     * @param term          term to search for
     * @return number for hits for this page.
     */
    public int searchHighlightPage(int pageIndex, String term,
                                   boolean caseSensitive, boolean wholeWord) {
        // clear previous search
        clearSearchHighlight(pageIndex);
        // add the search term
        addSearchTerm(term, caseSensitive, wholeWord);
        // start the search and return the hit count.
        return searchHighlightPage(pageIndex);
    }

    /**
     * Searches the page index given the search terms that have been added
     * with {@link #addSearchTerm(String, boolean, boolean)}.  If search
     * hits where detected then the Page's PageText is added to the cache.
     * <p/>
     * This method represent the core search algorithm for this
     * DocumentSearchController implementation.  This method can be over riden
     * if a different search algorithm or functionality is needed.
     *
     * @param pageIndex page index to search
     * @return number of hits found for this page.
     */
    public int searchHighlightPage(int pageIndex) {

        // get search terms from model and search for each occurrence.
        Collection<SearchTerm> terms = searchModel.getSearchTerms();

        // search hit count
        int hitCount = 0;

        // get our our page text reference
        PageText pageText = getPageText(pageIndex);

        // some pages just don't have any text. 
        if (pageText == null) {
            return 0;
        }

        // we need to do the search for  each term.
        for (SearchTerm term : terms) {

            // found word index to keep track of when we have found a hit
            int searchPhraseHitCount = 0;
            int searchPhraseFoundCount = term.getTerms().size();
            // list of found words for highlighting, as hits can span
            // lines and pages
            ArrayList<WordText> searchPhraseHits =
                    new ArrayList<WordText>(searchPhraseFoundCount);

            // start iteration over words.
            ArrayList<LineText> pageLines = pageText.getPageLines();
            if (pageLines != null) {
                for (LineText pageLine : pageLines) {
                    java.util.List<WordText> lineWords = pageLine.getWords();
                    // compare words against search terms.
                    String wordString;
                    for (WordText word : lineWords) {
                        // apply case sensitivity rule.
                        wordString = term.isCaseSensitive() ? word.toString() :
                                word.toString().toLowerCase();
                        // word matches, we have to match full word hits
                        if (term.isWholeWord()) {
                            if (wordString.equals(
                                    term.getTerms().get(searchPhraseHitCount))) {
                                // add word to potentials
                                searchPhraseHits.add(word);
                                searchPhraseHitCount++;
                            }
                            //                                else if (wordString.length() == 1 &&
                            //                                        WordText.isPunctuation(wordString.charAt(0))){
                            //                                    // ignore punctuation
                            //                                    searchPhraseHitCount++;
                            //                                }
                            // reset the counters.
                            else {
                                searchPhraseHits.clear();
                                searchPhraseHitCount = 0;
                            }
                        }
                        // otherwise we look for an index of hits
                        else {
                            // found a potential hit, depends on the length
                            // of searchPhrase.
                            if (wordString.contains(term.getTerms().get(searchPhraseHitCount))) {
                                // add word to potentials
                                searchPhraseHits.add(word);
                                searchPhraseHitCount++;
                            }
                            //                                else if (wordString.length() == 1 &&
                            //                                        WordText.isPunctuation(wordString.charAt(0))){
                            //                                    // ignore punctuation
                            //                                    searchPhraseHitCount++;
                            //                                }
                            // reset the counters.
                            else {
                                searchPhraseHits.clear();
                                searchPhraseHitCount = 0;
                            }

                        }
                        // check if we have found what we're looking for
                        if (searchPhraseHitCount == searchPhraseFoundCount) {
                            // iterate of found, highlighting words
                            for (WordText wordHit : searchPhraseHits) {
                                wordHit.setHighlighted(true);
                                wordHit.setHasHighlight(true);
                            }

                            // rest counts and start over again.
                            hitCount++;
                            searchPhraseHits.clear();
                            searchPhraseHitCount = 0;
                        }

                    }
                }
            }
        }

        // if we have a hit we'll add it to the model cache
        if (hitCount > 0) {
            searchModel.addPageSearchHit(pageIndex, pageText);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Found search hits on page " + pageIndex +
                        " hit count " + hitCount);
            }
        }

        return hitCount;
    }

    /**
     * Searches the page index given the search terms that have been added
     * with {@link #addSearchTerm(String, boolean, boolean)}.  If search
     * hits where detected then the Page's PageText is added to the cache.
     * <p/>
     * This class differences from {@link #searchHighlightPage(int)} in that
     * is returns a list of lineText fragments for each hit but the LinText
     * is padded by pre and post words that surround the hit in the page
     * context.
     * <p/>
     * This method represent the core search algorithm for this
     * DocumentSearchController implementation.  This method can be over riden
     * if a different search algorithm or functionality is needed.
     *
     * @param pageIndex   page index to search
     * @param wordPadding word padding on either side of hit to give context
     *                    to found words in the returned LineText
     * @return list of contextual hits for the give page.  If no hits an empty
     * list is returned.
     */
    public ArrayList<LineText> searchHighlightPage(int pageIndex, int wordPadding) {
        // get search terms from model and search for each occurrence.
        Collection<SearchTerm> terms = searchModel.getSearchTerms();

        // search hit list
        ArrayList<LineText> searchHits = new ArrayList<LineText>();

        // get our our page text reference
        PageText pageText = getPageText(pageIndex);

        // some pages just don't have any text.
        if (pageText == null) {
            return searchHits;
        }

        // we need to do the search for  each term.
        for (SearchTerm term : terms) {

            // found word index to keep track of when we have found a hit
            int searchPhraseHitCount = 0;
            int searchPhraseFoundCount = term.getTerms().size();
            // list of found words for highlighting, as hits can span
            // lines and pages
            ArrayList<WordText> searchPhraseHits =
                    new ArrayList<WordText>(searchPhraseFoundCount);

            // start iteration over words.
            ArrayList<LineText> pageLines = pageText.getPageLines();
            if (pageLines != null) {
                for (LineText pageLine : pageLines) {
                    java.util.List<WordText> lineWords = pageLine.getWords();
                    // compare words against search terms.
                    String wordString;
                    WordText word;
                    for (int i = 0, max = lineWords.size(); i < max; i++) {
                        word = lineWords.get(i);

                        // apply case sensitivity rule.
                        wordString = term.isCaseSensitive() ? word.toString() :
                                word.toString().toLowerCase();
                        // word matches, we have to match full word hits
                        if (term.isWholeWord()) {
                            if (wordString.equals(
                                    term.getTerms().get(searchPhraseHitCount))) {
                                // add word to potentials
                                searchPhraseHits.add(word);
                                searchPhraseHitCount++;
                            }
                            // reset the counters.
                            else {
                                searchPhraseHits.clear();
                                searchPhraseHitCount = 0;
                            }
                        }
                        // otherwise we look for an index of hits
                        else {
                            // found a potential hit, depends on the length
                            // of searchPhrase.
                            if (wordString.contains(term.getTerms().get(searchPhraseHitCount))) {
                                // add word to potentials
                                searchPhraseHits.add(word);
                                searchPhraseHitCount++;
                            }
                            // reset the counters.
                            else {
                                searchPhraseHits.clear();
                                searchPhraseHitCount = 0;
                            }

                        }
                        // check if we have found what we're looking for
                        if (searchPhraseHitCount == searchPhraseFoundCount) {

                            LineText lineText = new LineText();
                            int lineWordsSize = lineWords.size();
                            java.util.List<WordText> hitWords = lineText.getWords();
                            // add pre padding
                            int start = i - searchPhraseHitCount - wordPadding + 1;
                            start = start < 0 ? 0 : start;
                            int end = i - searchPhraseHitCount + 1;
                            end = end < 0 ? 0 : end;
                            for (int p = start; p < end; p++) {
                                hitWords.add(lineWords.get(p));
                            }

                            // iterate of found, highlighting words
                            for (WordText wordHit : searchPhraseHits) {
                                wordHit.setHighlighted(true);
                                wordHit.setHasHighlight(true);
                            }
                            hitWords.addAll(searchPhraseHits);

                            // add word padding to front of line
                            start = i + 1;
                            start = start > lineWordsSize ? lineWordsSize : start;
                            end = start + wordPadding;
                            end = end > lineWordsSize ? lineWordsSize : end;
                            for (int p = start; p < end; p++) {
                                hitWords.add(lineWords.get(p));
                            }

                            // add the hits to our list.
                            searchHits.add(lineText);

                            searchPhraseHits.clear();
                            searchPhraseHitCount = 0;
                        }

                    }
                }
            }
        }

        // if we have a hit we'll add it to the model cache
        if (searchHits.size() > 0) {
            searchModel.addPageSearchHit(pageIndex, pageText);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Found search hits on page " + pageIndex +
                        " hit count " + searchHits.size());
            }
        }

        return searchHits;
    }

    /**
     * Search page but only return words that are hits.  Highlighting is till
     * applied but this method can be used if other data needs to be extracted
     * from the found words.
     *
     * @param pageIndex page to search
     * @return list of words that match the term and search properties.
     */
    public ArrayList<WordText> searchPage(int pageIndex) {

        int hits = searchHighlightPage(pageIndex);
        if (hits > 0) {
            PageText searchText = searchModel.getPageTextHit(pageIndex);
            if (searchText != null) {
                ArrayList<WordText> words = new ArrayList<WordText>(hits);
                ArrayList<LineText> pageLines = searchText.getPageLines();
                if (pageLines != null) {
                    for (LineText pageLine : pageLines) {
                        java.util.List<WordText> lineWords = pageLine.getWords();
                        if (lineWords != null) {
                            for (WordText word : lineWords) {
                                if (word.isHighlighted()) {
                                    words.add(word);
                                }
                            }
                        }
                    }
                }
                return words;
            }
        }
        return null;
    }

    /**
     * Add the search term to the list of search terms.  The term is split
     * into words based on white space and punctuation. No checks are done
     * for duplication.
     * <p/>
     * A new search needs to be executed for this change to take place.
     *
     * @param term          single word or phrase to search for.
     * @param caseSensitive is search case sensitive.
     * @param wholeWord     is search whole word sensitive.
     * @return searchTerm newly create search term.
     */
    public SearchTerm addSearchTerm(String term, boolean caseSensitive,
                                    boolean wholeWord) {
        // keep origional copy
        String origionalTerm = String.valueOf(term);

        // check criteria for case sensitivity.
        if (!caseSensitive) {
            term = term.toLowerCase();
        }
        // parse search term out into words, so we can match
        // them against WordText
        ArrayList<String> searchPhrase = searchPhraseParser(term);
        // finally add the search term to the list and return it for management
        SearchTerm searchTerm =
                new SearchTerm(origionalTerm, searchPhrase, caseSensitive, wholeWord);
        searchModel.addSearchTerm(searchTerm);
        return searchTerm;
    }

    /**
     * Removes the specified search term from the search. A new search needs
     * to be executed for this change to take place.
     *
     * @param searchTerm search term to remove.
     */
    public void removeSearchTerm(SearchTerm searchTerm) {
        searchModel.removeSearchTerm(searchTerm);
    }

    /**
     * Clear all searched items for specified page.
     *
     * @param pageIndex page indext to clear
     */
    public void clearSearchHighlight(int pageIndex) {
        // clear cache and terms list 
        searchModel.clearSearchResults(pageIndex);
    }

    /**
     * Clears all highlighted text states for this this document.  This optimized
     * to use the the SearchHighlightModel to only clear pages that still have
     * selected states.
     */
    public void clearAllSearchHighlight() {
        searchModel.clearSearchResults();
    }

    /**
     * Test to see if a search highlight is needed.  This is done by first
     * check if there is a hit for this page and if the PageText object is the
     * same as the one specified as a param.  If they are not the same PageText
     * object then we need to do refresh as the page was disposed and
     * reinitialized with new content.
     *
     * @param pageIndex page index to text for results.
     * @param pageText  current pageText object associated with the pageIndex.
     * @return true if refresh is needed, false otherwise.
     */
    public boolean isSearchHighlightRefreshNeeded(int pageIndex, PageText pageText) {

        // check model to see if pages pagTex still has reference
        return searchModel.isPageTextMatch(pageIndex, pageText);
    }

    /**
     * Disposes controller clearing resources.
     */
    public void dispose() {
        searchModel.clearSearchResults();
    }

    /**
     * Gets teh page text for the given page index.
     *
     * @param pageIndex page index of page to extract text.
     * @return page's page text,  can be null.
     */
    protected PageText getPageText(int pageIndex) {
        PageText pageText = null;
        try {
            if (viewerController != null) {
                // get access to currently open document instance.
                pageText = viewerController.getDocument().getPageViewText(pageIndex);
            } else if (document != null) {
                pageText = document.getPageViewText(pageIndex);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.fine("PageText extraction thread was interrupted.");
        }
        return pageText;
    }

    /**
     * Utility for breaking the pattern up into searchable words.  Breaks are
     * done on white spaces and punctuation.
     *
     * @param phrase pattern to search words for.
     * @return list of words that make up phrase, words, spaces, punctuation.
     */
    protected ArrayList<String> searchPhraseParser(String phrase) {
        // trim white space, not really useful.
        phrase = phrase.trim();
        // found words. 
        ArrayList<String> words = new ArrayList<String>();
        char c;
        char cPrev = 0;
        for (int start = 0, curs = 0, max = phrase.length(); curs < max; curs++) {
            c = phrase.charAt(curs);
            if (WordText.isWhiteSpace(c) || (WordText.isPunctuation(c) && !WordText.isDigit(cPrev))) {
                // add word segment
                if (start != curs) {
                    words.add(phrase.substring(start, curs));
                }
                // add white space  as word too.
                words.add(phrase.substring(curs, curs + 1));
                // start
                start = curs + 1 < max ? curs + 1 : start;
            } else if (curs + 1 == max) {
                words.add(phrase.substring(start, curs + 1));
            }
            cPrev = c;
        }
        return words;
    }
}
