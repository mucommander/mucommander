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

import com.mucommander.snapshot.MuSnapshotable;

/**
 * Snapshot preferences for image viewer.
 */
public final class ImageViewerSnapshot extends MuSnapshotable<ImageViewerPreferences> {
    /**
     * Section describing information specific to image file presenter.
     */
    public static final String IMAGE_FILE_PRESENTER_SECTION = FILE_PRESENTER_SECTION + "." + "image";

    public ImageViewerSnapshot() {
        super(ImageViewerPreferences::values,
                ImageViewerPreferences::getValue,
                ImageViewerPreferences::setValue,
                pref -> pref.getPrefKey() != null ?
                        IMAGE_FILE_PRESENTER_SECTION + "." + pref.getPrefKey() : null);
    }
}
