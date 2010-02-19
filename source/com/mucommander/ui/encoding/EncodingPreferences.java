/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.ui.encoding;

import com.mucommander.conf.impl.MuConfiguration;

import java.nio.charset.Charset;
import java.util.Vector;

/**
 * This class allows to retrieve and set character encoding preferences. It is used by UI components that let the user
 * choose an encoding, thus limiting this list to a reasonable amount of choices instead of displaying all supported
 * character encodings.
 *
 * @see EncodingMenu
 * @see EncodingSelectBox
 * @author Maxence Bernard
 */
public class EncodingPreferences {

    /** Default list of preferred encodings, comma-separated. */
    public static final String[] DEFAULT_PREFERRED_ENCODINGS = new String[] {
        "UTF-8",
        "UTF-16",
        "ISO-8859-1",
        "windows-1252",
        "KOI8-R",
        "Big5",
        "GB18030",
        "EUC-KR",
        "Shift_JIS",
        "ISO-2022-JP",
        "EUC-JP",
    };


    /**
     * Returns a user-defined list of preferred encodings.
     *
     * @return a user-defined list of preferred encodings.
     */
    public static Vector<String> getPreferredEncodings() {
        Vector<String> vector = MuConfiguration.getListVariable(MuConfiguration.PREFERRED_ENCODINGS, ",");
        if(vector==null) {
            vector = getDefaultPreferredEncodings();
            MuConfiguration.setVariable(MuConfiguration.PREFERRED_ENCODINGS, vector, ",");
        }

        return vector;
    }

    /**
     * Returns a default list of preferred encodings, containing some of the most popular encodings.
     *
     * @return a default list of preferred encodings.
     */
    public static Vector<String> getDefaultPreferredEncodings() {
        Vector<String> encodingsV = new Vector<String>();
        for (String encoding : DEFAULT_PREFERRED_ENCODINGS) {
            // Ensure that the encoding is supported before adding it
            if (Charset.isSupported(encoding))
                encodingsV.add(encoding);
        }

        return encodingsV;
    }

    /**
     * Sets the user-defined list of preferred encodings.
     *
     * @param encodings the user-defined list of preferred encodings
     */
    public static void setPreferredEncodings(Vector<String> encodings) {
        MuConfiguration.setVariable(MuConfiguration.PREFERRED_ENCODINGS, encodings, ",");
    }
}
