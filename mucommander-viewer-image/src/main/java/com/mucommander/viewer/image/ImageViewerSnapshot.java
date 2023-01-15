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
package com.mucommander.viewer.image;

import com.mucommander.commons.conf.Configuration;
import com.mucommander.snapshot.MuSnapshotable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Snapshot preferences for image viewer.
 */
@ParametersAreNonnullByDefault
public final class ImageViewerSnapshot implements MuSnapshotable {

    @Override
    public void write(Configuration configuration) {
        for (ImageViewerPreferences pref : ImageViewerPreferences.values()) {
            if (pref.getPrefKey() != null) {
                configuration.setVariable(pref.getPrefKey(), pref.getValue());
            }
        }
    }
}
