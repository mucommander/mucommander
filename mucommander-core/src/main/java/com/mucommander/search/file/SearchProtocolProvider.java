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
package com.mucommander.search.file;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.protocol.ProtocolProvider;
import com.mucommander.search.SearchBuilder;
import com.mucommander.search.SearchJob;

/**
 * @author Arik Hadas
 */
public class SearchProtocolProvider implements ProtocolProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchProtocolProvider.class);

    /** Protocol for the virtual search file system. */
    public static final String SEARCH = "find";

    @Override
    public AbstractFile getFile(FileURL url, Map<String, Object> instantiationParams) throws IOException {
        SearchFile file = new SearchFile(url);
        String host = url.getHost();
        String path = url.getPath().substring(1);
        Map<String, String> properties = parseSearchProperties(url.getQuery());
        SearchJob job = SearchBuilder.newSearch()
                .what(path)
                .where(host)
                .searchArchives(properties)
                .searchHidden(properties)
                .searchSubfolders(properties)
                .searchDepth(properties)
                .matchCaseInsensitive(properties)
                .matchRegex(properties)
                .build();
        job.setSearchFile(file);
        file.setSearchJob(job);
        new Thread(() -> job.search()).start();
        return file;
    }

    private Map<String, String> parseSearchProperties(String str) {
        if (str == null)
            return Collections.emptyMap();
        return Arrays.stream(str.split("&"))
                .map(s -> s.split("="))
                .collect(Collectors.toMap(p -> p[0], p -> p[1]));
    }
}
