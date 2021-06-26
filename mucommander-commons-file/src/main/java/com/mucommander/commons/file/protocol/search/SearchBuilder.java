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

import java.util.Map;

import com.mucommander.commons.file.AbstractFile;

/**
 * An interface that the SearchBuilder should provide for mucommander-commons-file
 * @author Arik Hadas
 */
public interface SearchBuilder {
    SearchBuilder listener(SearchListener listener);
    SearchBuilder what(String searchStr);
    SearchBuilder where(AbstractFile entrypoint);
    SearchBuilder searchArchives(Map<String, String> properties);
    SearchBuilder searchHidden(Map<String, String> properties);
    SearchBuilder searchSubfolders(Map<String, String> properties);
    SearchBuilder searchDepth(Map<String, String> properties);
    SearchBuilder matchCaseInsensitive(Map<String, String> properties);
    SearchBuilder matchRegex(Map<String, String> properties);
    SearchBuilder searchText(Map<String, String> properties);
    SearchJob build();
}
