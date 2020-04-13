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
import java.awt.color.ColorSpace;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 1.0
 */
public class DeviceGray extends PColorSpace {

    public static final Name DEVICEGRAY_KEY = new Name("DeviceGray");
    public static final Name G_KEY = new Name("G");
    private static final ColorSpace RGB_COLOR_SPACE = ColorSpace.getInstance(ColorSpace.CS_sRGB);

    private static ConcurrentHashMap<Float, Color> colorHashMap = new ConcurrentHashMap<Float, Color>(255);

    public DeviceGray(Library l, HashMap h) {
        super(l, h);
    }


    public int getNumComponents() {
        return 1;
    }

    public Color getColor(float[] f, boolean fillAndStroke) {
        float gray = f[0] > 1.0 ? f[0] / 255.f : f[0];
        Color color = colorHashMap.get(f[0]);
        if (color != null) {
            return color;
        } else {
            color = new Color(RGB_COLOR_SPACE,
                    new Color(gray, gray, gray).getRGBComponents(null),
                    1);
            colorHashMap.put(f[0], color);
            return color;
        }
    }
}
