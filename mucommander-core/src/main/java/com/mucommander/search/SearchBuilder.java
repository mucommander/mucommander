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

import java.io.IOException;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.search.file.SearchListener;
import com.mucommander.ui.main.MainFrame;

/**
 * Builder of SearchJobs
 * @author Arik Hadas
 */
public class SearchBuilder {

    public static final String SEARCH_ARCHIVES = "archives";
    public static final String SEARCH_HIDDEN = "hidden";
    public static final String SEARCH_SUBFOLDERS = "subfolders";
    public static final String SEARCH_DEPTH = "depth";
    public static final String MATCH_CASEINSENSITIVE = "caseinsensitive";
    public static final String MATCH_REGEX = "regex";

    private AbstractFile entrypoint;
    private String searchStr;
    private SearchListener listener;
    private boolean matchCaseInsensitive;
    private boolean matchRegex;
    private boolean searchArchives;
    private boolean searchHidden;
    private boolean searchSubfolders;
    private int searchDepth;
    private MainFrame mainFrame;

    private SearchBuilder() {
        searchSubfolders = true;
        searchDepth = Integer.MAX_VALUE;
    }

    public static SearchBuilder newSearch() {
        return new SearchBuilder();
    }

    public SearchBuilder mainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        return this;
    }

    public SearchBuilder listener(SearchListener listener) {
        this.listener = listener;
        return this;
    }

    public SearchBuilder what(String searchStr) {
        this.searchStr = searchStr;
        return this;
    }

    public SearchBuilder where(AbstractFile entrypoint) {
        this.entrypoint = entrypoint;
        return this;
    }

    public SearchBuilder depth(int searchDepth) {
        this.searchDepth = searchDepth;
        return this;
    }

//    public SearchBuilder fromDate(Date date) {
//        return this;
//    }
//
//    public SearchBuilder toDate(Date date) {
//        return this;
//    }

    public SearchBuilder searchArchives(Map<String, String> properties) {
        String value = properties.get(SearchBuilder.SEARCH_ARCHIVES);
        if (value != null)
            searchArchives = Boolean.parseBoolean(value);
        return this;
    }

    public SearchBuilder searchHidden(Map<String, String> properties) {
        String value = properties.get(SearchBuilder.SEARCH_HIDDEN);
        if (value != null)
            searchHidden = Boolean.parseBoolean(value);
        return this;
    }

    public SearchBuilder searchSubfolders(Map<String, String> properties) {
        String value = properties.get(SearchBuilder.SEARCH_SUBFOLDERS);
        if (value != null)
            searchSubfolders = Boolean.parseBoolean(value);
        return this;
    }

    public SearchBuilder searchDepth(Map<String, String> properties) {
        String value = properties.get(SearchBuilder.SEARCH_DEPTH);
        if (value != null)
            searchDepth = Integer.parseInt(value);
        return this;
    }

    public SearchBuilder matchCaseInsensitive(Map<String, String> properties) {
        String value = properties.get(SearchBuilder.MATCH_CASEINSENSITIVE);
        if (value != null)
            matchCaseInsensitive = Boolean.parseBoolean(value);
        return this;
    }

    public SearchBuilder matchRegex(Map<String, String> properties) {
        String value = properties.get(SearchBuilder.MATCH_REGEX);
        if (value != null)
            matchRegex = Boolean.parseBoolean(value);
        return this;
    }

    public SearchJob build() {
        SearchJob job = new SearchJob(mainFrame, new FileSet(entrypoint, entrypoint));
        job.setListener(listener);
        job.setDepth(searchDepth);
        
        Predicate<AbstractFile> fileMatcher = createFilenamePredicate();
        job.setFileMatcher(fileMatcher);

        Predicate<AbstractFile> lsFilter = createListFilter();
        job.setListFilter(lsFilter);

        return job;
    }

    private Predicate<AbstractFile> createFilenamePredicate() {
        if (matchRegex) {
            int flags = matchCaseInsensitive ? Pattern.CASE_INSENSITIVE : 0;
            Pattern pattern = Pattern.compile(searchStr, flags);
            return file -> pattern.matcher(file.getName()).matches();
        }

        return matchCaseInsensitive ?
                file -> file.getName().equalsIgnoreCase(searchStr)
                : file -> file.getName().equals(searchStr);
    }

    private Predicate<AbstractFile> createListFilter() {
        Predicate<AbstractFile> listFilter = null;
        if (searchSubfolders)
            listFilter = AbstractFile::isDirectory;
        if (searchArchives) {
            listFilter = listFilter != null ?
                    listFilter.or(AbstractFile::isArchive)
                    : AbstractFile::isArchive;
        }

        if (listFilter != null && !searchHidden) {
            Predicate<AbstractFile> isHidden = AbstractFile::isHidden;
            listFilter = listFilter.and(isHidden.negate());
        }

        return listFilter != null ? listFilter : file -> false;
    }
}
