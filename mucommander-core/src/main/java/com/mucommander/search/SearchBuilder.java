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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.mucommander.commons.file.AbstractFile;

/**
 * Builder of SearchJobs
 * @author Arik Hadas
 */
public class SearchBuilder {

    private List<AbstractFile> entrypoints;
    private Predicate<AbstractFile> fileMatcher;
    private Predicate<AbstractFile> browseMatcher;

    private SearchBuilder(List<AbstractFile> entrypoints) {
        this.entrypoints = entrypoints;
        fileMatcher = file -> true;
        browseMatcher = AbstractFile::isDirectory;
    }

    public static SearchBuilder newSearch(AbstractFile entrypoint) {
        return newSearch(Collections.singletonList(entrypoint));
    }

    public static SearchBuilder newSearch(List<AbstractFile> entrypoints) {
        return new SearchBuilder(entrypoints);
    }

    public SearchBuilder name(String regex) {
        fileMatcher = fileMatcher.and(file -> Pattern.matches(regex, file.getName()));
        return this;
    }

    public SearchBuilder fromDate(Date date) {
        fileMatcher = fileMatcher.and(file -> date.getTime() <= file.getDate());
        return this;
    }

    public SearchBuilder toDate(Date date) {
        fileMatcher = fileMatcher.and(file -> date.getTime() >= file.getDate());
        return this;
    }

    public SearchBuilder searchArchives() {
        browseMatcher = browseMatcher.or(AbstractFile::isArchive);
        return this;
    }

    public SearchJob build() {
        SearchJob job = new SearchJob();
        job.setEntrypoints(entrypoints);
        job.setFileMatcher(fileMatcher);
        job.setBrowseMatcher(browseMatcher);
        return job;
    }
}
