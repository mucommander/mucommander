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

import static com.mucommander.snapshot.MuSnapshot.FILE_PRESENTER_SECTION;

import javax.annotation.ParametersAreNonnullByDefault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.conf.Configuration;
import com.mucommander.snapshot.MuSnapshot;
import com.mucommander.snapshot.MuSnapshotable;

/**
 * Snapshot preferences for image viewer.
 */
@ParametersAreNonnullByDefault
public final class ImageViewerSnapshot implements MuSnapshotable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageViewerSnapshot.class);

    /**
     * Section describing information specific to image file presenter.
     */
    public static final String IMAGE_FILE_PRESENTER_SECTION = FILE_PRESENTER_SECTION + "." + "image";

    @Override
    public void read(Configuration configuration) {
        LOGGER.info("Loading snapshot configuration for " + ImageViewerSnapshot.class);
        for (var pref : ImageViewerPreferences.values()) {
            var prefKey = pref.getPrefKey();
            if (prefKey != null) {
                prefKey = IMAGE_FILE_PRESENTER_SECTION + "." + prefKey;
                pref.setValue(MuSnapshot.getSnapshot().getVariable(prefKey, pref.getValue()));
            }
        }
    }

    @Override
    public void write(Configuration configuration) {
        for (var pref : ImageViewerPreferences.values()) {
            var prefKey = pref.getPrefKey();
            if (prefKey != null) {
                prefKey = IMAGE_FILE_PRESENTER_SECTION + "." + prefKey;
                configuration.setVariable(prefKey, pref.getValue());
            }
        }
    }
}
