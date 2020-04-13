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
import org.icepdf.core.pobjects.Stream;
import org.icepdf.core.pobjects.StringObject;
import org.icepdf.core.util.Library;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

/**
 * The class represents an indexed colour space.
 */
public class Indexed extends PColorSpace {

    public static final Name INDEXED_KEY = new Name("Indexed");
    public static final Name I_KEY = new Name("I");

    PColorSpace colorSpace;
    int hival;
    byte[] colors = {
            -1, -1, -1, 0, 0, 0
    };
    private boolean inited = false;
    private Color[] cols;

    /**
     * Constructs a new instance of the indexed colour space. Pares the indexed
     * colour pattern: [/Indexed base hival lookup] where base is an array or
     * name that identifies the base colour space. Hival parameter is an integer
     * that specifies the maximum valid index value. Lookup is the colour table
     * which must be of length m x (hival + 1) where m is the number of colour
     * components in base.
     *
     * @param library    document library.
     * @param entries    dictionary entries.
     * @param dictionary indexed colour dictionary.
     */
    Indexed(Library library, HashMap entries, List dictionary) {
        super(library, entries);
        // get the base colour space
        colorSpace = getColorSpace(library, dictionary.get(1));
        // get the hival
        hival = (((Number) (dictionary.get(2))).intValue());
        // check for an instance of a lookup table.
        if (dictionary.get(3) instanceof StringObject) {
            // peel and decrypt the literal string
            StringObject tmpText = (StringObject) dictionary.get(3);
            String tmp = tmpText.getDecryptedLiteralString(library.getSecurityManager());
            // build the colour lookup table.
            byte[] textBytes = new byte[colorSpace.getNumComponents() * (hival + 1)]; // m * (hival + 1)
            for (int i = 0; i < textBytes.length; i++) {
                textBytes[i] = (byte) tmp.charAt(i);
            }
            colors = textBytes;
        } else if (dictionary.get(3) instanceof Reference) {
            colors = new byte[colorSpace.getNumComponents() * (hival + 1)];
            // make sure the colors array is the correct length, so we'll copy
            // over the data from the stream just to be sure.
            Stream lookup = (Stream) (library.getObject((Reference) (dictionary.get(3))));
            if (lookup != null) {
                byte[] colorStream = lookup.getDecodedStreamBytes(0);
                int length = colors.length < colorStream.length ? colors.length : colorStream.length;
                System.arraycopy(colorStream, 0, colors, 0, length);
            }
        }
    }

    /**
     * Return the number of components in indexed colour.
     *
     * @return always returns 1.
     */
    public int getNumComponents() {
        return 1;
    }

    public String getDescription() {
        String desc = super.getDescription();
        if (colorSpace != null)
            desc = desc + ":" + colorSpace.getDescription();
        return desc;
    }

    /**
     * Initiate the Indexed Colour Object
     */
    public synchronized void init() {
        if (inited) {
            return;
        }
        int numCSComps = colorSpace.getNumComponents();
        int b1[] = new int[numCSComps];
        float f1[] = new float[numCSComps];
        cols = new Color[hival + 1];
        for (int j = 0; j <= hival; j++) {
            for (int i = 0; i < numCSComps; i++) {
                b1[numCSComps - 1 - i] = 0xFF & ((int) colors[j * numCSComps + i]);
            }
            colorSpace.normaliseComponentsToFloats(b1, f1, 255.0f);
            cols[j] = colorSpace.getColor(f1, true);
        }
        inited = true;
    }

    /**
     * Gets the colour for the array of float values
     *
     * @param f
     * @return
     */
    public Color getColor(float[] f, boolean fillAndStroke) {
        init();
        int index = (int) f[0];//(int) (f[0] * (cols.length - 1));
        if (index >= 0 && index <= hival) {
            return cols[index];
        } else if (index > hival) {
            return cols[hival];
        } else {
            return cols[0];
        }

    }

    public Color[] accessColorTable() {
        return cols;
    }
}
