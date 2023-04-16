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
import java.util.Objects;

import com.mucommander.commons.util.Pair;
import com.mucommander.text.Translator;

public enum SearchProperty {
    SEARCH_IN_ARCHIVES("archives", "search_dialog.search_in_archives", Boolean.FALSE.toString()),
    SEARCH_IN_HIDDEN("hidden", "search_dialog.search_in_hidden_files", Boolean.FALSE.toString()),
    SEARCH_IN_SYMLINKS("symlinks", "search_dialog.search_in_symlinks", Boolean.FALSE.toString()),
    SEARCH_IN_SUBFOLDERS("subfolders", "search_dialog.search_in_subfolders", Boolean.TRUE.toString()),
    SEARCH_FOR_ARCHIVES("filter_archives", "search_dialog.search_for_archives", Boolean.TRUE.toString()),
    SEARCH_FOR_HIDDEN("filter_hidden", "search_dialog.search_for_hidden_files", Boolean.TRUE.toString()),
    SEARCH_FOR_SYMLINKS("filter_symlinks", "search_dialog.search_for_symlinks", Boolean.TRUE.toString()),
    SEARCH_FOR_SUBFOLDERS("filter_subfolders", "search_dialog.search_for_folders", Boolean.TRUE.toString()),
    SEARCH_DEPTH("depth", "search_dialog.search_depth", "0"),
    SEARCH_THREADS("threads", "search_dialog.search_threads", "2"),
    MATCH_CASESENSITIVE("case_sensitive", "search_dialog.case_sensitive", Boolean.FALSE.toString()),
    MATCH_REGEX("regex", "search_dialog.matches_regexp", Boolean.FALSE.toString()),
    TEXT_CASESENSITIVE("text-case_sensitive", "search_dialog.text_case_sensitive", Boolean.FALSE.toString()),
    TEXT_MATCH_REGEX("text-regex", "search_dialog.text_matches_regexp", Boolean.FALSE.toString()),
    SEARCH_SIZE("size", "search_dialog.size", null),
    SEARCH_SIZE2("size-2", "search_dialog.size", null),
    SEARCH_TEXT("text", "search_dialog.search_text", ""),
    TEXT_WHOLE_WORDS("whole-words", "used-via-find-dialog1", Boolean.toString(false)),
    TEXT_SEARCH_FORWARD("forward", "used-via-find-dialog2", Boolean.toString(true)),
    ;

    private String key;
    private String i18nKey;
    private String defaultValue;
    private String value;

    SearchProperty(String key, String i18nKey, String defaultValue) {
        this.key = key;
        this.i18nKey = i18nKey;
        this.defaultValue = defaultValue;
        value = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getTranslation() {
        return Translator.get(i18nKey);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String get(List<Pair<String, String>> properties) {
        return properties.stream()
                .filter(p -> p.first.equals(key))
                .map(p -> p.second)
                .findAny()
                .orElse(defaultValue);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setValue(boolean value) {
        this.value = Boolean.toString(value);
    }

    public String getValue() {
        return value;
    }

    public boolean getBoolValue() {
        return Boolean.parseBoolean(value);
    }

    public boolean isDefault() {
        return Objects.equals(defaultValue, value);
    }

    public String toString() {
        return key + "=" + value;
    }
}
