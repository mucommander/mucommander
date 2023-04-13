/*
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.search;

/**
 * A class to keep the last search query and its options.
 */
public final class LastSearchQuery {

    private static final LastSearchQuery INSTANCE = new LastSearchQuery();

    private String searchString;
    private boolean searchCaseSensitive;
    private boolean searchMatchRegex;

    private boolean forward = true; // default
    private boolean wholeWords;

    private LastSearchQuery() {
    }

    public static LastSearchQuery getInstance() {
        return INSTANCE;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public boolean isSearchCaseSensitive() {
        return searchCaseSensitive;
    }

    public void setSearchCaseSensitive(boolean searchCaseSensitive) {
        this.searchCaseSensitive = searchCaseSensitive;
    }

    public boolean isSearchMatchRegex() {
        return searchMatchRegex;
    }

    public void setSearchMatchRegex(boolean searchMatchRegex) {
        this.searchMatchRegex = searchMatchRegex;
    }

    public boolean isForward() {
        return forward;
    }

    public void setForward(boolean forward) {
        this.forward = forward;
    }

    public boolean isWholeWords() {
        return wholeWords;
    }

    public void setWholeWords(boolean wholeWords) {
        this.wholeWords = wholeWords;
    }
}
