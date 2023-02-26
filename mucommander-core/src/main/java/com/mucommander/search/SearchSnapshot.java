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

import static com.mucommander.snapshot.MuSnapshot.SEARCH_SECTION;

import com.mucommander.commons.conf.Configuration;
import com.mucommander.snapshot.MuSnapshotable;

/**
 * Snapshot preferences for file-search
 * @author Arik Hadas
 */
public class SearchSnapshot extends MuSnapshotable<SearchProperty> {
    public SearchSnapshot() {
        super(SearchProperty::values,
                SearchProperty::getValue,
                SearchProperty::setValue,
                pref -> pref.getKey() != null ? SEARCH_SECTION + "." + pref.getKey() : null);
    }

    @Override
    protected void write(Configuration configuration, SearchProperty pref) {
        // do not persist properties that are set with their default value
        if (!pref.isDefault())
            super.write(configuration, pref);
    }
}
