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
import org.icepdf.core.pobjects.functions.Function;
import org.icepdf.core.util.ColorUtil;
import org.icepdf.core.util.Library;

import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Separation Color Space background:</p>
 * <ul>
 * <p>Color output devices produce full color by combining primary or process
 * colorants in varying amounts. On an additive color device such as a display,
 * the primary colorants consist of red, green, and blue phosphors; on a
 * subtractive device such as a printer, they typically consist of cyan, magenta,
 * yellow, and sometimes black inks. In addition, some devices can apply special
 * colorants, often called spot colorants, to produce effects that cannot be
 * achieved with the standard process colorants alone. Examples include metallic
 * and fluorescent colors and special textures.</p>
 * </ul>
 * <p>A Separation color space (PDF 1.2) provides a means for specifying the use
 * of additional colorants or for isolating the control of individual color
 * components of a device color space for a subtractive device. When such a space
 * is the current color space, the current color is a single-component value,
 * called a tint, that controls the application of the given colorant or color
 * components only.</p>
 * <p>A Separation color space is defined as follows:<br />
 * [/Separation name alternateSpace tintTransform]
 * </p>
 * <ul>
 * <li>The <i>alternateSpace</i> parameter must be an array or name object that
 * identifies the alternate color space, which can be any device or
 * CIE-based color space but not another special color space (Pattern,
 * Indexed, Separation, or DeviceN).</li>
 * <li>The <i>tintTransform</i> parameter must be a function.
 * During subsequent painting operations, an application
 * calls this function to transform a tint value into color component values
 * in the alternate color space. The function is called with the tint value
 * and must return the corresponding color component values. That is, the
 * number of components and the interpretation of their values depend on the
 * alternate color space.</li>
 * </ul>
 *
 * @since 1.0
 */
public class Separation extends PColorSpace {

    public static final Name SEPARATION_KEY = new Name("Separation");

    // named colour reference if valid conversion took place
    protected Color namedColor;
    // alternative colour space, named colour can not be resolved.
    protected PColorSpace alternate;
    // transform for colour tint, named function type
    protected Function tintTransform;
    // The special colorant name All shall refer collectively to all colorants
    // available on an output device, including those for the standard process
    // colorants. When a Separation space with this colorant name is the current
    // colour space, painting operators shall apply tint values to all available
    // colorants at once.
    private boolean isAll;
    public static final String COLORANT_ALL = "all";
    // The special colorant name None shall not produce any visible output.
    // Painting operations in a Separationspace with this colorant name shall
    // have no effect on the current page.
    private boolean isNone;
    public static final String COLORANT_NONE = "none";
    private float tint = 1.0f;
    // basic cache to speed up the lookup.
    private ConcurrentHashMap<Integer, Color> colorTable1B;
    private ConcurrentHashMap<Integer, Color> colorTable3B;
    private ConcurrentHashMap<Integer, Color> colorTable4B;

    /**
     * Create a new Seperation colour space.  Separation is specified using
     * [/Seperation name alternateSpace tintTransform]
     *
     * @param l              library
     * @param h              dictionary entries
     * @param name           name of colourspace, always seperation
     * @param alternateSpace name of alternative colour space
     * @param tintTransform  function which defines the tint transform
     */
    protected Separation(Library l, HashMap h, Object name, Object alternateSpace, Object tintTransform) {
        super(l, h);
        alternate = getColorSpace(l, alternateSpace);
        colorTable1B = new ConcurrentHashMap<Integer, Color>(256);
        colorTable3B = new ConcurrentHashMap<Integer, Color>(256);
        colorTable4B = new ConcurrentHashMap<Integer, Color>(256);

        this.tintTransform = Function.getFunction(l, l.getObject(tintTransform));
        // see if name can be converted to a known colour.
        if (name instanceof Name) {
            String colorName = ((Name) name).getName().toLowerCase();
            // check for additive colours we can work with .
            if (!(colorName.equals("red") || colorName.equals("blue")
                    || colorName.equals("blue") || colorName.equals("black")
                    || colorName.equals("cyan") || colorName.equals("brown")
                    || colorName.equals("auto"))) {
                // sniff out All or Null
                if (colorName.equals(COLORANT_ALL)) {
                    isAll = true;
                } else if (colorName.equals(COLORANT_NONE)) {
                    isNone = true;
                }
                // return as we don't care about the namedColor if subtractive.
                return;
            }
            // get colour value if any
            int colorVaue = ColorUtil.convertNamedColor(colorName.toLowerCase());
            if (colorVaue != -1) {
                namedColor = new Color(colorVaue);
            }
            // quick check for auto color which we'll paint as black
            if (colorName.equalsIgnoreCase("auto")) {
                namedColor = Color.BLACK;
            }
        }
    }

    /**
     * Returns the number of components in this colour space.
     *
     * @return number of components
     */
    public int getNumComponents() {
        return 1;
    }

    public boolean isNamedColor() {
        return namedColor != null;
    }

    /**
     * Gets the colour in RGB represented by the array of colour components
     *
     * @param components    array of component colour data
     * @param fillAndStroke true indicates a fill or stroke operation, so we
     *                      will try to used the named colour and tint. This
     *                      is generally not do for images.
     * @return new RGB colour composed from the components array.
     */
    public Color getColor(float[] components, boolean fillAndStroke) {
        // there are couple notes in the spec that say that even know namedColor
        // is for subtractive color devices, if the named colour can be represented
        // in a additive device then it should be used over the alternate colour.
        if (namedColor != null) {
            // apply tint
            tint = components[0];
            // apply tint as an alpha value.
            float[] colour = namedColor.getComponents(null);
//                namedColor = new Color(colour[0] * tint, colour[1] * tint, colour[2] * tint);
            Color namedColor = new Color(colour[0], colour[1], colour[2], tint);
            // The color model doesn't actually have transparency, so white with an alpha of 0.
            // is still just white, not transparent.
            if (tint < 0.1f && colour[0] == 0 && colour[1] == 0 && colour[2] == 0) {
                return Color.WHITE;
            }
            return namedColor;
        }

        // the function couldn't be initiated then use the alternative colour
        // space.  The alternate colour space can be any device or CIE-based
        // colour space. However Separation is usually specified using only one
        // component so we must generate the output colour
        if (tintTransform == null) {
            float colour = components[0];
            // copy the colour values into the needed length of the alternate colour
            float[] alternateColour = new float[alternate.getNumComponents()];
            for (int i = 0, max = alternate.getNumComponents(); i < max; i++) {
                alternateColour[i] = colour;
            }
            return alternate.getColor(alternateColour);
        }
        if (alternate != null && !isNone) {
            // component is our key which we can use to avoid doing the tintTransform.
            int key = 0;
            int bands = components.length;
            for (int i = 0, bit = 0; i < bands; i++, bit += 8) {
                key |= (((int) (components[i] * 255) & 0xff) << bit);
            }
            if (bands == 1) {
                return addColorToCache(colorTable1B, key, alternate, tintTransform, components);
            } else if (bands == 3) {
                return addColorToCache(colorTable3B, key, alternate, tintTransform, components);
            } else if (bands == 4) {
                return addColorToCache(colorTable4B, key, alternate, tintTransform, components);
            }
        }
        if (isNone) {
            return new Color(0, 0, 0, 0);
        }
        // return the named colour if it was resolved, otherwise assemble the
        // alternative colour.
        // -- Only applies to subtractive devices, screens are additive but I'm
        // leaving this in encase something goes horribly wrong.
        return namedColor;
    }

    private static Color addColorToCache(
            ConcurrentHashMap<Integer, Color> colorCache, int key,
            PColorSpace alternate, Function tintTransform, float[] f) {
        Color color = colorCache.get(key);
        if (color == null) {
            float y[] = tintTransform.calculate(reverse(f));
            color = alternate.getColor(reverse(y));
            colorCache.put(key, color);
            return color;
        } else {
            return color;
        }
    }

    public float getTint() {
        return tint;
    }
}
