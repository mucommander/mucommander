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
package org.icepdf.core.pobjects.graphics;

import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.Reference;
import org.icepdf.core.util.Library;

import java.awt.*;
import java.util.HashMap;

/**
 * <p>Pattern colour implements PColorSpace but is more of a parser placeholder
 * for dealing with 'cs' token which sets a pattern Colour space.  The pattern
 * color space can either define a Pattern dictionary which contains valid
 * pattern object which are then specified by the 'scn' or 'SCN' tokens.  The
 * pattern can also define straight up color space rgb, gray, N etc.</p>
 * <p>If the PatternColor contains dictionary of Pattern Object from the
 * pages resources then this object is created with the corrisponding
 * dictionary reference. </p>
 *
 * @since 1.0
 */
public class PatternColor extends PColorSpace {

    public static final Name PATTERN_KEY = new Name("Pattern");

    private Pattern pattern;

    private PColorSpace PColorSpace;

    /**
     * Creates a new instance of PatternColor.
     *
     * @param library document library.
     * @param entries dictionary entries.
     */
    public PatternColor(Library library, HashMap entries) {
        super(library, entries);
    }

    /**
     * Not applicable to a Pattern Colour space.
     *
     * @return value of zero
     */
    public int getNumComponents() {
        if (PColorSpace != null) {
            return PColorSpace.getNumComponents();
        }
        return 0;
    }

    /**
     * Not applicable to a Pattern Colour space.
     *
     * @param f any value.
     * @return always returns null.
     */
    public Color getColor(float[] f, boolean fillAndStroke) {
        if (PColorSpace != null) {
            return PColorSpace.getColor(f);
        }
        return Color.black;
    }

    public Pattern getPattern(Reference reference) {
        if (entries != null) {
            return (Pattern) entries.get(reference);
        }
        return null;
    }

    public PColorSpace getPColorSpace() {
        return PColorSpace;
    }

    public void setPColorSpace(PColorSpace PColorSpace) {
        this.PColorSpace = PColorSpace;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }
}
