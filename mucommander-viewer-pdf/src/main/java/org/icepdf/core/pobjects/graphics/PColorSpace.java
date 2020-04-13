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

import org.icepdf.core.pobjects.Dictionary;
import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.Reference;
import org.icepdf.core.util.Library;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * put your documentation comment here
 */
public abstract class PColorSpace extends Dictionary {

    private static final Logger logger =
            Logger.getLogger(PColorSpace.class.toString());

    /**
     * @return
     */
    public abstract int getNumComponents();


    public String getDescription() {
        String name = getClass().getName();
        int index = name.lastIndexOf('.');
        return name.substring(index + 1);
    }

    /**
     * @param l
     * @param h
     */
    PColorSpace(Library l, HashMap h) {
        super(l, h);
    }

    /**
     * @param library
     * @param o
     * @return
     */
    public static PColorSpace getColorSpace(Library library, Object o) {
        if (o != null) {
            PColorSpace colorSpace = null;
            Reference ref = null;
            if (o instanceof Reference) {
                ref = (Reference) o;
                o = library.getObject(ref);
            }

            if (o instanceof PColorSpace) {
                colorSpace = (PColorSpace) o;
            } else if (o instanceof Name) {
                if (o.equals(DeviceGray.DEVICEGRAY_KEY) ||
                        o.equals(DeviceGray.G_KEY)) {
                    colorSpace = new DeviceGray(library, null);
                } else if (o.equals(DeviceRGB.DEVICERGB_KEY) ||
                        o.equals(DeviceRGB.RGB_KEY)) {
                    colorSpace = new DeviceRGB(library, null);
                } else if (o.equals(DeviceCMYK.DEVICECMYK_KEY) ||
                        o.equals(DeviceCMYK.CMYK_KEY)) {
                    colorSpace = new DeviceCMYK(library, null);
                } else if (o.equals(PatternColor.PATTERN_KEY)) {
                    colorSpace = new PatternColor(library, null);
                }
            } else if (o instanceof List) {
                List v = (List) o;
                Name colorant = (Name) v.get(0);
                if (colorant.equals(Indexed.INDEXED_KEY)
                        || colorant.equals(Indexed.I_KEY)) {
                    colorSpace = new Indexed(library, null, v);
                } else if (colorant.equals(CalRGB.CALRGB_KEY)) {
                    colorSpace = new CalRGB(library, getHashMap(library, v.get(1)));
                } else if (colorant.equals(CalGray.CAL_GRAY_KEY)) {
                    colorSpace = new CalGray(library, getHashMap(library, v.get(1)));
                } else if (colorant.equals(Lab.LAB_KEY)) {
                    colorSpace = new Lab(library, getHashMap(library, v.get(1)));
                } else if (colorant.equals(Separation.SEPARATION_KEY)) {
                    colorSpace = new Separation(
                            library,
                            null,
                            v.get(1),
                            v.get(2),
                            v.get(3));
                } else if (colorant.equals(DeviceN.DEVICEN_KEY)) {
                    colorSpace = new DeviceN(
                            library,
                            null,
                            v.get(1),
                            v.get(2),
                            v.get(3),
                            v.size() > 4 ? v.get(4) : null);
                } else if (colorant.equals(ICCBased.ICCBASED_KEY)) {
                    /*Stream st = (Stream)library.getObject((Reference)v.elementAt(1));
                     return  PColorSpace.getColorSpace(library, library.getObject(st.getEntries(),
                     "Alternate"));*/
                    colorSpace = library.getICCBased((Reference) v.get(1));
                } else if (colorant.equals(DeviceRGB.DEVICERGB_KEY)) {
                    colorSpace = new DeviceRGB(library, null);
                } else if (colorant.equals(DeviceCMYK.DEVICECMYK_KEY)) {
                    colorSpace = new DeviceCMYK(library, null);
                } else if (colorant.equals(DeviceGray.DEVICEGRAY_KEY)) {
                    colorSpace = new DeviceGray(library, null);
                } else if (colorant.equals(PatternColor.PATTERN_KEY)) {
                    PatternColor patternColour = new PatternColor(library, null);
                    if (v.size() > 1) {
                        Object tmp = v.get(1);
                        if (tmp instanceof Reference) {
                            tmp = library.getObject((Reference) v.get(1));
                            if (tmp instanceof PColorSpace) {
                                patternColour.setPColorSpace((PColorSpace) tmp);
                            } else if (tmp instanceof HashMap) {
                                patternColour.setPColorSpace(
                                        getColorSpace(library, tmp));
                            }
                        } else {
                            patternColour.setPColorSpace(
                                    getColorSpace(library,
                                            getHashMap(library, v.get(1))));
                        }

                    }
                    colorSpace = patternColour;
                }
            } else if (o instanceof HashMap) {
                colorSpace = new PatternColor(library, (HashMap) o);
            }
            if (colorSpace == null && logger.isLoggable(Level.FINE)) {
                logger.fine("Unsupported ColorSpace: " + o);
            }
            // cache the space proper.
            if (ref != null && colorSpace != null) {
                library.addObject(colorSpace, ref);
            }
            if (colorSpace != null) {
                return colorSpace;
            }
        }
        return new DeviceGray(library, null);
    }

    /**
     * Utility to get a valid hash map for the provided Object or Reference.
     *
     * @param obj object or Reference from color dictionary.
     * @return a dictionary or null if the object is not of type Reference or
     *         HashMap.
     */
    private static HashMap getHashMap(Library library, Object obj) {
        HashMap entries = null;
        if (obj instanceof HashMap) {
            entries = (HashMap) obj;
        } else if (obj instanceof Reference) {
            obj = library.getObject((Reference) obj);
            if (obj instanceof HashMap) {
                entries = (HashMap) obj;
            }
        }
        return entries;
    }

    /**
     * Gets the color space for the given n value. If n == 3 then the new color
     * space with be RGB,  if n == 4 then CMYK and finally if n == 1 then
     * Gray.
     *
     * @param library hash of all library objects
     * @param n       number of colours in colour space
     * @return a new PColorSpace given the value of n
     */
    public synchronized static PColorSpace getColorSpace(Library library, float n) {
        if (n == 3) {
            return new DeviceRGB(library, null);
        } else if (n == 4) {
            return new DeviceCMYK(library, null);
        } else {
            return new DeviceGray(library, null);
        }
    }

    /**
     * Gets the colour in RGB represented by the array of colour components
     *
     * @param components array of component colour data
     * @return new RGB colour composed from the components array.
     */
    public Color getColor(float[] components) {
        return getColor(components, false);
    }

    public abstract Color getColor(float[] components, boolean fillAndStroke);

    public void normaliseComponentsToFloats(int[] in, float[] out, float maxval) {
        int count = getNumComponents();
        for (int i = 0; i < count; i++)
            out[i] = (((float) in[i]) / maxval);
    }

    /**
     * @param f
     * @return
     */
    public static float[] reverse(float f[]) {
        float n[] = new float[f.length];
        //System.out.print("R ");
        for (int i = 0; i < f.length; i++) {
            n[i] = f[f.length - i - 1];
            //System.out.print( n[i] + ",");
        }
        //System.out.println();
        return n;
    }

    public static void reverseInPlace(float[] f) {
        int num = f.length / 2;
        for (int i = 0; i < num; i++) {
            float tmp = f[i];
            f[i] = f[f.length - 1 - i];
            f[f.length - 1 - i] = tmp;
        }
    }

    public static void reverseInPlace(int[] f) {
        int num = f.length / 2;
        for (int i = 0; i < num; i++) {
            int tmp = f[i];
            f[i] = f[f.length - 1 - i];
            f[f.length - 1 - i] = tmp;
        }
    }
}
