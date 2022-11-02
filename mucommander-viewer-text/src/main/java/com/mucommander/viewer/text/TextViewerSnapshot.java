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

import com.mucommander.commons.conf.Configuration;
import static com.mucommander.snapshot.MuSnapshot.FILE_PRESENTER_SECTION;
import com.mucommander.snapshot.MuSnapshotable;

/**
 * Snapshot preferences for text editor.
 *
 * @author Miroslav Hajda
 */
public final class TextViewerSnapshot implements MuSnapshotable {

    /**
     * Section describing information specific to text file presenter.
     */
    private static final String TEXT_FILE_PRESENTER_SECTION = FILE_PRESENTER_SECTION + "." + "text";
    /**
     * Whether or not to wrap long lines.
     */
    public static final String TEXT_FILE_PRESENTER_LINE_WRAP = TEXT_FILE_PRESENTER_SECTION + "." + "line_wrap";
    /**
     * Default wrap value.
     */
    public static final boolean DEFAULT_LINE_WRAP = false;
    /**
     * Whether or not to show line numbers.
     */
    public static final String TEXT_FILE_PRESENTER_LINE_NUMBERS = TEXT_FILE_PRESENTER_SECTION + "." + "line_numbers";
    /**
     * Default line numbers value.
     */
    public static final boolean DEFAULT_LINE_NUMBERS = true;

    @Override
    public void read(Configuration configuration) {
    }

    @Override
    public void write(Configuration configuration) {
        configuration.setVariable(TEXT_FILE_PRESENTER_LINE_WRAP, TextViewer.isLineWrap());
        configuration.setVariable(TEXT_FILE_PRESENTER_LINE_NUMBERS, TextViewer.isLineNumbers());
    }
}
