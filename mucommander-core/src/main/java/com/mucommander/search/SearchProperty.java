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

import java.util.List;

import com.mucommander.commons.util.Pair;
import com.mucommander.text.Translator;

enum SearchProperty {
    SEARCH_IN_ARCHIVES("archives", "search_dialog.search_in_archives"),
    SEARCH_IN_HIDDEN("hidden", "search_dialog.search_in_hidden_files"),
    SEARCH_IN_SYMLINKS("symlinks", "search_dialog.search_in_symlinks"),
    SEARCH_IN_SUBFOLDERS("subfolders", "search_dialog.search_in_subfolders"),
    SEARCH_FOR_ARCHIVES("filter_archives", "search_dialog.search_for_archives"),
    SEARCH_FOR_HIDDEN("filter_hidden", "search_dialog.search_for_hidden_files"),
    SEARCH_FOR_SYMLINKS("filter_symlinks", "search_dialog.search_for_symlinks"),
    SEARCH_FOR_SUBFOLDERS("filter_subfolders", "search_dialog.search_for_folders"),
    SEARCH_DEPTH("depth", "search_dialog.search_depth"),
    SEARCH_THREADS("threads", "search_dialog.search_threads"),
    MATCH_CASESENSITIVE("case_sensitive", "search_dialog.case_sensitive"),
    MATCH_REGEX("regex", "search_dialog.matches_regexp"),
    TEXT_CASESENSITIVE("text-case_sensitive", "search_dialog.text_case_sensitive"),
    TEXT_MATCH_REGEX("text-regex", "search_dialog.text_matches_regexp"),
    SEARCH_SIZE("size", "search_dialog.size");

    private String key;
    private String i18nKey;

    SearchProperty(String key, String i18nKey) {
        this.key = key;
        this.i18nKey = i18nKey;
    }

    public String getKey() {
        return key;
    }

    public String getTranslation() {
        return Translator.get(i18nKey);
    }

    public String update(List<Pair<String, String>> properties) {
        return properties.stream()
                .filter(p -> p.first.equals(key))
                .map(p -> p.second)
                .findAny()
                .orElse(null);
    }
}
