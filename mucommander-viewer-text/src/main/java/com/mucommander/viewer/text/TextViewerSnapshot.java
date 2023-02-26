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
package com.mucommander.viewer.text;

import static com.mucommander.snapshot.MuSnapshot.FILE_PRESENTER_SECTION;

import com.mucommander.snapshot.MuSnapshotable;

/**
 * Snapshot preferences for text editor & viewer.
 *
 * @author Miroslav Hajda, Piotr Skowronek, Arik Hadas
 */
public final class TextViewerSnapshot extends MuSnapshotable<TextViewerPreferences> {
    /**
     * Section describing information specific to text file presenter.
     */
    public static final String TEXT_FILE_PRESENTER_SECTION = FILE_PRESENTER_SECTION + "." + "text";

    TextViewerSnapshot() {
        super(TextViewerPreferences::values,
                pref -> Boolean.toString(pref.getValue()),
                (pref, value) -> pref.setValue(Boolean.parseBoolean(value)),
                pref -> pref.getPrefKey() != null ? TEXT_FILE_PRESENTER_SECTION + "." + pref.getPrefKey() : null);
    }
}