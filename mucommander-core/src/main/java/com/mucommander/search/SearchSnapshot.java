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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.conf.Configuration;
import com.mucommander.snapshot.MuSnapshot;
import com.mucommander.snapshot.MuSnapshotable;

public class SearchSnapshot implements MuSnapshotable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchSnapshot.class);

    @Override
    public void read(Configuration configuration) {
        LOGGER.info("Loading snapshot configuration for " + SearchSnapshot.class);
        for (var property : SearchProperty.values()) {
            var prefKey = MuSnapshot.SEARCH_SECTION + "." + property.getKey();
            property.setValue(MuSnapshot.getSnapshot().getVariable(prefKey, property.getValue()));
        }
    }

    @Override
    public void write(Configuration configuration) {
        for (var property : SearchProperty.values()) {
            if (!property.isDefault()) {
                var prefKey = MuSnapshot.SEARCH_SECTION + "." + property.getKey();
                configuration.setVariable(prefKey, property.getValue());
            }
        }
    }

}
