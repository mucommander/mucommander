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
import java.util.List;
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
import com.mucommander.commons.util.Pair;
import com.mucommander.job.impl.SearchJob;
import com.mucommander.ui.main.MainFrame;

/**
 * Builder of SearchJobs
 * @author Arik Hadas
 */
public class SearchBuilder implements com.mucommander.commons.file.protocol.search.SearchBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchBuilder.class);

    public static final String SEARCH_TEXT = "text";
    public static final int DEFAULT_THREADS = 2;

    private AbstractFile entrypoint;
    private String searchStr;
    private SearchListener listener;
    private boolean matchCaseSensitive;
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
    private boolean textCaseSensitive;
    private boolean textMatchRegex;
    private Predicate<AbstractFile> sizePredicate;

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

    @Override
    public SearchBuilder searchInArchives(List<Pair<String, String>> properties) {
        String value = SearchProperty.SEARCH_IN_ARCHIVES.update(properties);
        if (value != null)
            searchInArchives = Boolean.parseBoolean(value);
        return this;
    }

    @Override
    public SearchBuilder searchInHidden(List<Pair<String, String>> properties) {
        String value = SearchProperty.SEARCH_IN_HIDDEN.update(properties);
        if (value != null)
            searchInHidden = Boolean.parseBoolean(value);
        return this;
    }

    @Override
    public SearchBuilder searchInSymlinks(List<Pair<String, String>> properties) {
        String value = SearchProperty.SEARCH_IN_SYMLINKS.update(properties);
        if (value != null)
            searchInSymlinks = Boolean.parseBoolean(value);
        return this;
    }

    @Override
    public SearchBuilder searchInSubfolders(List<Pair<String, String>> properties) {
        String value = SearchProperty.SEARCH_IN_SUBFOLDERS.update(properties);
        if (value != null)
            searchInSubfolders = Boolean.parseBoolean(value);
        return this;
    }

    @Override
    public SearchBuilder searchForArchives(List<Pair<String, String>> properties) {
        String value = SearchProperty.SEARCH_FOR_ARCHIVES.update(properties);
        if (value != null)
            searchForArchives = Boolean.parseBoolean(value);
        return this;
    }

    @Override
    public SearchBuilder searchForHidden(List<Pair<String, String>> properties) {
        String value = SearchProperty.SEARCH_FOR_HIDDEN.update(properties);
        if (value != null)
            searchForHidden = Boolean.parseBoolean(value);
        return this;
    }

    @Override
    public SearchBuilder searchForSymlinks(List<Pair<String, String>> properties) {
        String value = SearchProperty.SEARCH_FOR_SYMLINKS.update(properties);
        if (value != null)
            searchForSymlinks = Boolean.parseBoolean(value);
        return this;
    }

    @Override
    public SearchBuilder searchForSubfolders(List<Pair<String, String>> properties) {
        String value = SearchProperty.SEARCH_FOR_SUBFOLDERS.update(properties);
        if (value != null)
            searchForSubfolders = Boolean.parseBoolean(value);
        return this;
    }

    @Override
    public SearchBuilder searchDepth(List<Pair<String, String>> properties) {
        String value = SearchProperty.SEARCH_DEPTH.update(properties);
        if (value != null)
            searchDepth = Integer.parseInt(value);
        return this;
    }

    @Override
    public SearchBuilder searchThreads(List<Pair<String, String>> properties) {
        String value = SearchProperty.SEARCH_THREADS.update(properties);
        if (value != null)
            searchThreads = Integer.parseInt(value);
        return this;
    }

    @Override
    public SearchBuilder matchCaseInsensitive(List<Pair<String, String>> properties) {
        String value = SearchProperty.MATCH_CASESENSITIVE.update(properties);
        if (value != null)
            matchCaseSensitive = Boolean.parseBoolean(value);
        return this;
    }

    @Override
    public SearchBuilder matchRegex(List<Pair<String, String>> properties) {
        String value = SearchProperty.MATCH_REGEX.update(properties);
        if (value != null)
            matchRegex = Boolean.parseBoolean(value);
        return this;
    }

    @Override
    public SearchBuilder searchText(List<Pair<String, String>> properties) {
        String value = getProperty(properties, SearchBuilder.SEARCH_TEXT);
        if (value != null) {
            searchText = value;
            textCaseSensitive = Boolean.parseBoolean(SearchProperty.TEXT_CASESENSITIVE.update(properties));
            textMatchRegex = Boolean.parseBoolean(SearchProperty.TEXT_MATCH_REGEX.update(properties));
        }
        return this;
    }

    @Override
    public SearchBuilder searchSize(List<Pair<String, String>> properties) {
        properties.stream()
        .filter(p -> p.first.equals(SearchProperty.SEARCH_SIZE.getKey()))
        .map(p -> p.second)
        .forEach(value -> {
            String[] sizeProperties = SearchUtils.splitSearchSizeClause(value);
            SizeRelation searchSizeRelation = SizeRelation.valueOf(sizeProperties[0]);
            long searchSize = Long.parseLong(sizeProperties[1]);
            SizeUnit searchSizeUnit = SizeUnit.valueOf(sizeProperties[2]);
            Predicate<AbstractFile> predicate = file -> searchSizeRelation.matches(file.getSize(), searchSize, searchSizeUnit);
            sizePredicate = sizePredicate == null ? predicate : sizePredicate.and(predicate);
        });
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

            searchJob.setSearchText(searchText);
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
        if (sizePredicate != null)
            predicate =  predicate.and(sizePredicate);

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
            int flags = matchCaseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
            Pattern pattern = Pattern.compile(searchStr, flags);
            return file -> pattern.matcher(file.getName()).matches();
        }

        return matchCaseSensitive ?
                file -> file.getName().equals(searchStr)
                : file -> file.getName().equalsIgnoreCase(searchStr);
    }

    private Predicate<AbstractFile> createFileContentPredicate() {
        GrepOptionSet_Fcilnvx grepOptions = Grep.Options.l;
        if (!textCaseSensitive)
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

    private Predicate<AbstractFile> createListFilter() {
        Predicate<AbstractFile> listFilter = file -> false;
        if (searchInSubfolders)
            listFilter = listFilter.or(AbstractFile::isDirectory);
        if (searchInArchives)
            listFilter = listFilter.or(AbstractFile::isArchive);

        if (!searchInSymlinks) {
            Predicate<AbstractFile> isSymlink = AbstractFile::isSymlink;
            Predicate<AbstractFile> isNotSymlink = isSymlink.negate();
            listFilter = listFilter.and(isNotSymlink);
        }
        if (!searchInHidden) {
            Predicate<AbstractFile> isHidden = AbstractFile::isHidden;
            Predicate<AbstractFile> isNotHidden = isHidden.negate();
            listFilter = listFilter.and(isNotHidden);
        }

        return listFilter;
    }

    private String getProperty(List<Pair<String, String>> properties, String property) {
        return properties.stream()
                .filter(p -> p.first.equals(property))
                .map(p -> p.second)
                .findAny()
                .orElse(null);
    }
}
