/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.core.pobjects.filters;

/**
 * This is a utility class that aids the decoding of CCITT 4 2D encoded data.
 * CCITTFax is the parent class that uses this class to keep track of the
 * locations of the B & W bit locations with in the stream
 */
class G4State {
    int[] ref;
    int[] cur;
    boolean white = true; // colour reference
    int a0; // The reference element on the coding line
    int b1; // The next changing element on the reference line to the right of a0 and of opppsite color of a0
    int refIndex; // the previous scan line
    int curIndex; // the current scan line
    int runLength;
    int width;
    int longrun;

    /**
     * Greate a new instance of a G4State.
     *
     * @param w width of the line being looked at.
     */
    G4State(int w) {
        width = w;
        ref = new int[width + 1];
        cur = new int[width + 1];
        a0 = 0;
        b1 = width;
        ref[0] = width;
        ref[1] = 0;
        runLength = 0;
        longrun = 0;
        refIndex = 1;
        curIndex = 0;
    }
}
