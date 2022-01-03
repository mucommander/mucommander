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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unix4j.Unix4j;
import org.unix4j.unix.Grep;
import org.unix4j.unix.grep.GrepOptionSet_Fcilnvx;
import org.unix4j.unix.grep.GrepOptions;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.protocol.search.SearchListener;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.job.impl.SearchJob;
import com.mucommander.ui.main.MainFrame;

/**
 * Builder of SearchJobs
 * @author Arik Hadas
 */
public class SearchBuilder implements com.mucommander.commons.file.protocol.search.SearchBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchBuilder.class);

    public static final String SEARCH_IN_ARCHIVES = "archives";
    public static final String SEARCH_IN_HIDDEN = "hidden";
    public static final String SEARCH_IN_SYMLINKS = "symlinks";
    public static final String SEARCH_IN_SUBFOLDERS = "subfolders";
    public static final String SEARCH_FOR_ARCHIVES = "filter_archives";
    public static final String SEARCH_FOR_HIDDEN = "filter_hidden";
    public static final String SEARCH_FOR_SYMLINKS = "filter_symlinks";
    public static final String SEARCH_FOR_SUBFOLDERS = "filter_subfolders";
    public static final String SEARCH_DEPTH = "depth";
    public static final String SEARCH_THREADS = "threads";
    public static final String MATCH_CASEINSENSITIVE = "caseinsensitive";
    public static final String MATCH_REGEX = "regex";
    public static final String SEARCH_TEXT = "text";
    public static final String TEXT_CASEINSENSITIVE = "text-caseinsensitive";
    public static final String TEXT_MATCH_REGEX= "text-regex";
    public static final String SEARCH_SIZE = "sz";
    public static final String SEARCH_SIZE_REL = "sz-rel";
    public static final String SEARCH_SIZE_UNIT = "sz-unit";
    public static final int DEFAULT_THREADS = 2;

    private AbstractFile entrypoint;
    private String searchStr;
    private SearchListener listener;
    private boolean matchCaseInsensitive;
    private boolean matchRegex;
    private boolean searchInArchives;
    private boolean searchInHidden;
    private boolean searchInSymlinks;
    private boolean searchInSubfolders;
    private boolean searchForArchives;
    private boolean searchForHidden;
    private boolean searchForSymlinks;
    private boolean searchForSubfolders;
    private int searchDepth;
    private int searchThreads;
    private MainFrame mainFrame;
    private String searchText;
    private boolean textCaseInsensitive;
    private boolean textMatchRegex;
    private Long searchSize;
    private SizeRelation searchSizeRelation;
    private SizeUnit searchSizeUnit;

    private SearchJob searchJob;

    private SearchBuilder() {
        searchInSubfolders = true;
        searchForArchives = true;
        searchForHidden = true;
        searchForSymlinks = true;
        searchForSubfolders = true;
        searchDepth = Integer.MAX_VALUE;
        searchThreads = DEFAULT_THREADS;
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

    public SearchBuilder searchInArchives(Map<String, String> properties) {
        String value = properties.get(SearchBuilder.SEARCH_IN_ARCHIVES);
        if (value != null)
            searchInArchives = Boolean.parseBoolean(value);
        return this;
    }

    public SearchBuilder searchInHidden(Map<String, String> properties) {
        String value = properties.get(SearchBuilder.SEARCH_IN_HIDDEN);
        if (value != null)
            searchInHidden = Boolean.parseBoolean(value);
        return this;
    }

    public SearchBuilder searchInSymlinks(Map<String, String> properties) {
        String value = properties.get(SearchBuilder.SEARCH_IN_SYMLINKS);
        if (value != null)
            searchInSymlinks = Boolean.parseBoolean(value);
        return this;
    }

    public SearchBuilder searchInSubfolders(Map<String, String> properties) {
        String value = properties.get(SearchBuilder.SEARCH_IN_SUBFOLDERS);
        if (value != null)
            searchInSubfolders = Boolean.parseBoolean(value);
        return this;
    }

    public SearchBuilder searchForArchives(Map<String, String> properties) {
        String value = properties.get(SearchBuilder.SEARCH_FOR_ARCHIVES);
        if (value != null)
            searchForArchives = Boolean.parseBoolean(value);
        return this;
    }

    public SearchBuilder searchForHidden(Map<String, String> properties) {
        String value = properties.get(SearchBuilder.SEARCH_FOR_HIDDEN);
        if (value != null)
            searchForHidden = Boolean.parseBoolean(value);
        return this;
    }

    public SearchBuilder searchForSymlinks(Map<String, String> properties) {
        String value = properties.get(SearchBuilder.SEARCH_FOR_SYMLINKS);
        if (value != null)
            searchForSymlinks = Boolean.parseBoolean(value);
        return this;
    }

    public SearchBuilder searchForSubfolders(Map<String, String> properties) {
        String value = properties.get(SearchBuilder.SEARCH_FOR_SUBFOLDERS);
        if (value != null)
            searchForSubfolders = Boolean.parseBoolean(value);
        return this;
    }

    public SearchBuilder searchDepth(Map<String, String> properties) {
        String value = properties.get(SearchBuilder.SEARCH_DEPTH);
        if (value != null)
            searchDepth = Integer.parseInt(value);
        return this;
    }

    public SearchBuilder searchThreads(Map<String, String> properties) {
        String value = properties.get(SearchBuilder.SEARCH_THREADS);
        if (value != null)
            searchThreads = Integer.parseInt(value);
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

    @Override
    public SearchBuilder searchText(Map<String, String> properties) {
        String value = properties.get(SearchBuilder.SEARCH_TEXT);
        if (value != null) {
            searchText = value;
            textCaseInsensitive = Boolean.parseBoolean(properties.get(SearchBuilder.TEXT_CASEINSENSITIVE));
            textMatchRegex = Boolean.parseBoolean(properties.get(SearchBuilder.TEXT_MATCH_REGEX));
        }
        return this;
    }

    @Override
    public SearchBuilder searchSize(Map<String, String> properties) {
        String value = properties.get(SearchBuilder.SEARCH_SIZE);
        if (value != null) {
            searchSize = Long.parseLong(value);
            searchSizeRelation = SizeRelation.valueOf(properties.get(SearchBuilder.SEARCH_SIZE_REL));
            searchSizeUnit = SizeUnit.valueOf((properties.get(SearchBuilder.SEARCH_SIZE_UNIT)));
        }
        return this;
    }

    public SearchJob build() {
        if (searchJob == null) {
            searchJob = new SearchJob(mainFrame, new FileSet(entrypoint, entrypoint));
            searchJob.setListener(listener);
            searchJob.setDepth(searchDepth);
            searchJob.setThreads(searchThreads);

            Predicate<AbstractFile> fileMatcher = createFilePredicate();
            searchJob.setFileMatcher(fileMatcher);

            Predicate<AbstractFile> lsFilter = createListFilter();
            searchJob.setListFilter(lsFilter);
        }
        return searchJob;
    }

    private Predicate<AbstractFile> createFilePredicate() {
        Predicate<AbstractFile> predicate = createFilenamePredicate();
        if (!searchForSubfolders) {
            Predicate<AbstractFile> isDirectory = AbstractFile::isDirectory;
            Predicate<AbstractFile> isNotDirectory = isDirectory.negate();
            predicate = predicate.and(isNotDirectory);
        }
        if (!searchForArchives) {
            Predicate<AbstractFile> isArchive = AbstractFile::isArchive;
            Predicate<AbstractFile> isNotArchive = isArchive.negate();
            predicate = predicate.and(isNotArchive);
        }
        if (!searchForHidden) {
            Predicate<AbstractFile> isHidden = AbstractFile::isHidden;
            Predicate<AbstractFile> isNotHidden = isHidden.negate();
            predicate = predicate.and(isNotHidden);
        }
        if (!searchForSymlinks) {
            Predicate<AbstractFile> isSymlink = AbstractFile::isSymlink;
            Predicate<AbstractFile> isNotSymlink = isSymlink.negate();
            predicate = predicate.and(isNotSymlink);
        }
        if (searchSize != null)
            predicate =  predicate.and(createFileSizePredicate());

        // text should be the last predicate because it is the most expensive
        if (searchText != null)
            predicate = predicate.and(createFileContentPredicate());

        return predicate;
    }

    private Predicate<AbstractFile> createFilenamePredicate() {
        if (!matchRegex) {
            String regex = SearchUtils.wildcardToRegex(searchStr);
            if (!searchStr.equals(regex)) {
                searchStr = regex;
                matchRegex = true;
            }
        }

        if (matchRegex) {
            int flags = matchCaseInsensitive ? Pattern.CASE_INSENSITIVE : 0;
            Pattern pattern = Pattern.compile(searchStr, flags);
            return file -> pattern.matcher(file.getName()).matches();
        }

        return matchCaseInsensitive ?
                file -> file.getName().equalsIgnoreCase(searchStr)
                : file -> file.getName().equals(searchStr);
    }

    private Predicate<AbstractFile> createFileContentPredicate() {
        GrepOptionSet_Fcilnvx grepOptions = Grep.Options.l;
        if (textCaseInsensitive)
            grepOptions = grepOptions.i;
        if (!textMatchRegex)
            grepOptions = grepOptions.F;
        final GrepOptions options = grepOptions;
        return file -> {
            try {
                return !file.isDirectory() && !Unix4j.from(file.getInputStream()).grep(options, searchText).toStringResult().isEmpty();
            } catch (IOException e) {
                LOGGER.debug("failed to search content of " + file.getAbsolutePath(), e);
                return false;
            }
        };
    }

    private Predicate<AbstractFile> createFileSizePredicate() {
        return file -> searchSizeRelation.matches(file.getSize(), searchSize, searchSizeUnit);
    }

    private Predicate<AbstractFile> createListFilter() {
        Predicate<AbstractFile> listFilter = null;
        if (searchInSubfolders)
            listFilter = AbstractFile::isDirectory;
        if (searchInArchives) {
            listFilter = listFilter != null ?
                    listFilter.or(AbstractFile::isArchive)
                    : AbstractFile::isArchive;
        }

        if (!searchInSymlinks) {
            Predicate<AbstractFile> isSymlink = AbstractFile::isSymlink;
            Predicate<AbstractFile> isNotSymlink = isSymlink.negate();
            listFilter = listFilter != null ? listFilter.and(isNotSymlink) : isNotSymlink;
        }
        if (listFilter != null && !searchInHidden) {
            Predicate<AbstractFile> isHidden = AbstractFile::isHidden;
            Predicate<AbstractFile> isNotHidden = isHidden.negate();
            listFilter = listFilter.and(isNotHidden);
        }

        return listFilter != null ? listFilter : file -> false;
    }
}
