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
import org.icepdf.core.util.Library;

import java.awt.*;
import java.util.HashMap;

/**
 * put your documentation comment here
 */
public class DeviceRGB extends PColorSpace {

    public static final Name DEVICERGB_KEY = new Name("DeviceRGB");
    public static final Name RGB_KEY = new Name("RGB");

    /**
     * @param l
     * @param h
     */
    DeviceRGB(Library l, HashMap h) {
        super(l, h);
    }

    /**
     * @return
     */
    public int getNumComponents() {
        return 3;
    }

    private float validateColorRange(float component) {
        if (component < 0.0f) {
            return 0.0f;
        } else if (component > 1.0f) {
            return 1.0f;
        } else {
            return component;
        }
    }

    /**
     * Get the awt Color value for for a given colours data.
     *
     * @param colours array containing the RGB colour values.
     * @return a awt colour based on the colours array RGB values.
     */
    public Color getColor(float[] colours, boolean fillAndStroke) {

        return new Color(validateColorRange(colours[2]),
                validateColorRange(colours[1]),
                validateColorRange(colours[0]));
    }
}
