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
package com.mucommander.commons.file.protocol.search;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.protocol.ProtocolProvider;
import com.mucommander.commons.util.Pair;

/**
 * @author Arik Hadas
 */
public class SearchProtocolProvider implements ProtocolProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchProtocolProvider.class);

    @Override
    public AbstractFile getFile(FileURL url, Map<String, Object> instantiationParams) throws IOException {
        AbstractFile searchPlace = FileFactory.getFile(url.getHost());
        if (!searchPlace.exists()) {
            LOGGER.debug("search place doesn't exist {}", searchPlace);
            throw new IOException("search place doesn't exist");
        }
        return new SearchFile(url)
                .setProperties(parseSearchProperties(url.getQuery()))
                .setSearchStr(url.getPath().substring(1))
                .setSearchPlace(searchPlace);
    }

    private List<Pair<String, String>> parseSearchProperties(String str) {
        if (str == null)
            return Collections.emptyList();
        return Arrays.stream(str.split("&"))
                .map(s -> s.split("="))
                .map(p -> new Pair<>(p[0], p[1]))
                .collect(Collectors.toList());
    }
}
