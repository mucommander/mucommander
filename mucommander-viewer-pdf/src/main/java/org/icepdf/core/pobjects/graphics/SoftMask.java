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
import org.icepdf.core.pobjects.Form;
import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.functions.Function;
import org.icepdf.core.util.Library;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Soft-mask Dictionary specifies the current soft mask in the graphics
 * state.  The mask values shall be derived from those of the transparency group,
 * using one of the two methods in "11.5.2 Deriving a Soft Mask from Group Alpha",
 * and "11.5.3 Deriving a Soft Mask from Group Luminosity".
 * <p/>
 * The S subtype entry shall specify which of the two derivation methods to use:
 * <ul>
 * <li>If the subtype is Alpha, the transparency group XObject G shall be
 * evaluated to compute a group alpha only. The colours of the constituent
 * objects shall be ignored and the colour compositing computations shall
 * not be performed. The transfer function TR shall then be applied to the
 * computed group alpha to produce the mask values. Outside the bounding box
 * of the transparency group, the mask value shall be the result of applying
 * the transfer function to the input value 0.0.</li>
 * <li>If the subtype is Luminosity, the transparency group XObject G shall
 * be composited with a fully opaque backdrop whose colour is everywhere
 * defined by the soft-mask dictionary's BC entry. The computed result colour
 * shall then be converted to a single-component luminosity value, and the
 * transfer function TR shall be applied to this luminosity to produce the
 * mask values. Outside the transparency group's bounding box, the mask value
 * shall be derived by transforming the BC colour to luminosity and applying
 * the transfer function to the result.</li>
 * </ul>
 */
public class SoftMask extends Dictionary {

    private static final Logger logger =
            Logger.getLogger(SoftMask.class.toString());

    public static final Name S_KEY = new Name("S");
    public static final Name G_KEY = new Name("G");
    public static final Name BC_KEY = new Name("BC");
    public static final String SOFT_MASK_TYPE_ALPHA = "Alpha";
    public static final String SOFT_MASK_TYPE_LUMINOSITY = "Luminosity";

    private Form softMask;

    public SoftMask(Library library, HashMap dictionary) {
        super(library, dictionary);
    }

    /**
     * A subtype specifying the method to be used in deriving the mask values
     * from the transparency group specified by the G entry:
     * <ul>
     * <li><b>Alpha</b> - The group's computed alpha shall be used, disregarding
     * its colour (see "Deriving a Soft Mask from Group Alpha").</li>
     * <li>LuminosityThe group's computed colour shall be converted to a
     * single-component luminosity value (see "Deriving a Soft Mask from
     * Group Luminosity").</li>
     * </ul>
     *
     * @return subtype of the soft-mask dictionary.
     */
    public Name getS() {
        return library.getName(entries, S_KEY);
    }

    /**
     * A transparency group XObject (see "Transparency Group XObjects") to be
     * used as the source of alpha or colour values for deriving the mask. If
     * the subtype S is Luminosity, the group attributes dictionary shall
     * contain a CS entry defining the colour space in which the compositing
     * computation is to be performed.
     *
     * @return Xobject associated with G, null otherwise.
     */
    public Form getG() {
        if (softMask != null) {
            return softMask;
        }
        Object GKey = library.getObject(entries, G_KEY);
        if (GKey != null && GKey instanceof Form) {
            softMask = (Form) GKey;
            softMask.init();
            return softMask;
        }
        return null;
    }

    /**
     * An array of component values specifying the colour to be used as the
     * backdrop against which to composite the transparency group XObject G.
     * This entry shall be consulted only if the subtype S is Luminosity. The
     * array shall consist of n numbers, where n is the number of components in
     * the colour space specified by the CS entry in the group attributes
     * dictionary (see "Transparency Group XObjects").
     * Default value: the colour space's initial value, representing black.
     *
     * @return componet colours
     */
    @SuppressWarnings("unchecked")
    public List<Number> getBC() {
        Object BCKey = library.getObject(entries, BC_KEY);
        if (BCKey instanceof List) {
            return (List<Number>) BCKey;
        }
        return null;
    }

    // todo handle TR
    /**
     * (Optional) A function object (see "Functions") specifying the transfer
     * function to be used in deriving the mask values. The function shall
     * accept one input, the computed group alpha or luminosity (depending on
     * the value of the subtype S), and shall return one output, the resulting
     * mask value. The input shall be in the range 0.0 to 1.0. The computed
     * output shall be in the range 0.0 to 1.0; if it falls outside this range,
     * it shall be forced to the nearest valid value. The name Identitymay be
     * specified in place of a function object to designate the identity
     * function. Default value: Identity.
     *
     * Type: function or name.
     */
    public Object getTR() {
        Object object = library.getObject(entries, BC_KEY);
        if (object != null) {
            return Function.getFunction(library, object);
        }
        return null;
    }

}
