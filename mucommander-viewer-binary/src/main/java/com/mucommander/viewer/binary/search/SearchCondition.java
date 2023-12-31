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
package com.mucommander.viewer.binary.search;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.CodeAreaUtils;

/**
 * Parameters for action to search for occurrences of text or data.
 */
@ParametersAreNonnullByDefault
public class SearchCondition {

    private SearchMode searchMode = SearchMode.TEXT;
    private String searchText = "";
    private EditableBinaryData binaryData;

    public SearchCondition() {
    }

    /**
     * This is copy constructor.
     *
     * @param source
     *            source condition
     */
    public SearchCondition(SearchCondition source) {
        searchMode = source.getSearchMode();
        searchText = source.getSearchText();
        binaryData = new ByteArrayEditableData();
        if (source.getBinaryData() != null) {
            binaryData.insert(0, source.getBinaryData());
        }
    }

    @Nonnull
    public SearchMode getSearchMode() {
        return searchMode;
    }

    public void setSearchMode(SearchMode searchMode) {
        this.searchMode = searchMode;
    }

    @Nonnull
    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    @Nullable
    public BinaryData getBinaryData() {
        return binaryData;
    }

    public void setBinaryData(@Nullable EditableBinaryData binaryData) {
        this.binaryData = binaryData;
    }

    public boolean isEmpty() {
        switch (searchMode) {
        case TEXT: {
            return searchText == null || searchText.isEmpty();
        }
        case BINARY: {
            return binaryData == null || binaryData.isEmpty();
        }
        default:
            throw CodeAreaUtils.getInvalidTypeException(searchMode);
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SearchCondition other = (SearchCondition) obj;
        if (this.searchMode != other.searchMode) {
            return false;
        }
        if (searchMode == SearchMode.TEXT) {
            return Objects.equals(this.searchText, other.searchText);
        } else {
            return Objects.equals(this.binaryData, other.binaryData);
        }
    }

    public void clear() {
        searchText = "";
        if (binaryData != null) {
            binaryData.clear();
        }
    }

    public enum SearchMode {
        TEXT,
        BINARY
    }
}
