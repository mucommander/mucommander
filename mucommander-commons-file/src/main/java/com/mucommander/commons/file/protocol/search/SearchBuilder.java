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

import java.util.List;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.util.Pair;

/**
 * An interface that the SearchBuilder should provide for mucommander-commons-file
 * @author Arik Hadas
 */
public interface SearchBuilder {
    SearchBuilder listener(SearchListener listener);
    SearchBuilder what(String searchStr);
    SearchBuilder where(AbstractFile entrypoint);
    SearchBuilder searchInArchives(List<Pair<String, String>> properties);
    SearchBuilder searchInHidden(List<Pair<String, String>> properties);
    SearchBuilder searchInSymlinks(List<Pair<String, String>> properties);
    SearchBuilder searchInSubfolders(List<Pair<String, String>> properties);
    SearchBuilder searchForArchives(List<Pair<String, String>> properties);
    SearchBuilder searchForHidden(List<Pair<String, String>> properties);
    SearchBuilder searchForSymlinks(List<Pair<String, String>> properties);
    SearchBuilder searchForSubfolders(List<Pair<String, String>> properties);
    SearchBuilder searchDepth(List<Pair<String, String>> properties);
    SearchBuilder searchThreads(List<Pair<String, String>> properties);
    SearchBuilder matchCaseInsensitive(List<Pair<String, String>> properties);
    SearchBuilder matchRegex(List<Pair<String, String>> properties);
    SearchBuilder searchText(List<Pair<String, String>> properties);
    SearchBuilder searchSize(List<Pair<String, String>> properties);
    SearchJob build();
}
