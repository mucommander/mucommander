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
import org.icepdf.core.util.Library;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DeviceN colour spaces shall be defined in a similar way to Separation colour
 * spaces-in fact, a Separationcolour space can be defined as a DeviceN colour
 * space with only one component.
 * <p/>
 * A DeviceN colour space shall be specified as follows:
 * [/DeviceN names alternateSpace tintTransform]
 * or
 * [/DeviceN names alternateSpace tintTransform attributes]
 * <p/>
 * It is a four- or five-element array whose first element shall be the colour
 * space family name DeviceN. The remaining elements shall be parameters that a
 * DeviceN colour space requires.
 */
public class DeviceN extends PColorSpace {

    public static final Name DEVICEN_KEY = new Name("DeviceN");
    public static final Name COLORANTS_KEY = new Name("Colorants");

    List<Name> names;
    PColorSpace alternate;
    Function tintTransform;
    ConcurrentHashMap<Object, Object> colorants = new ConcurrentHashMap<Object, Object>();
    PColorSpace colorspaces[];

    boolean foundCMYK;

    @SuppressWarnings("unchecked")
    DeviceN(Library l, HashMap h, Object o1, Object o2, Object o3, Object o4) {
        super(l, h);
        names = (java.util.List) o1;
        alternate = getColorSpace(l, o2);
        tintTransform = Function.getFunction(l, l.getObject(o3));
        if (o4 != null) {
            HashMap h1 = (HashMap) library.getObject(o4);
            HashMap h2 = (HashMap) library.getObject(h1, COLORANTS_KEY);
            if (h2 != null) {
                Set e = h2.keySet();
                Object oo;
                for (Object o : e) {
                    oo = h2.get(o);
                    colorants.put(o, getColorSpace(library, library.getObject(oo)));
                }
            }
        }
        colorspaces = new PColorSpace[names.size()];
        for (int i = 0; i < colorspaces.length; i++) {
            colorspaces[i] = (PColorSpace) colorants.get(names.get(i).toString());
        }
        // check to see if cymk is specified int the names, if so we can
        // uses the cmyk colour space directly, otherwise we fallback to the alternative
        // and hope it was setup correctly.
        int cmykCount = 0;
        foundCMYK = true;
        for (Name name : names) {
            if (name.getName().toLowerCase().startsWith("c")) {
                cmykCount++;
            } else if (name.getName().toLowerCase().startsWith("m")) {
                cmykCount++;
            } else if (name.getName().toLowerCase().startsWith("y")) {
                cmykCount++;
            } else if (name.getName().toLowerCase().startsWith("k")) {
                cmykCount++;
            } else if (name.getName().toLowerCase().startsWith("b")) {
                cmykCount++;
            }
        }
        if (cmykCount < 1) {
            foundCMYK = false;
        }
    }

    public int getNumComponents() {
        return names.size();
    }

    private float[] assignCMYK(float[] f) {
        float[] f2 = new float[4];
        Name name;
        for (int i = 0, max = names.size(); i < max; i++) {
            name = names.get(i);
            if (name.getName().toLowerCase().startsWith("c")) {
                f2[0] = i < f.length ? f[i] : 0;
            } else if (name.getName().toLowerCase().startsWith("m")) {
                f2[1] = i < f.length ? f[i] : 0;
            } else if (name.getName().toLowerCase().startsWith("y")) {
                f2[2] = i < f.length ? f[i] : 0;
            } else if (name.getName().toLowerCase().startsWith("b") ||
                    name.getName().toLowerCase().startsWith("k")) {
                f2[3] = i < f.length ? f[i] : 0;
            }
        }
        if (f.length != 4) {
            f2 = reverse(f2);
        }
        return f2;
    }


    public Color getColor(float[] f, boolean fillAndStroke) {
        // calculate cmyk color
        if (foundCMYK && (f.length == 4 )) {
            f = assignCMYK(f);
            return new DeviceCMYK(null, null).getColor((f));
        }else if (foundCMYK && (f.length == 3)) {
            f = assignCMYK(reverse(f));
            return new DeviceCMYK(null, null).getColor((f));
        }
        // check order, mainly look for length > 1 and black not at the end
        // assumption on a few corner cases is that we are looking for cmyk ordering
        // and thus black last.
//        if (f.length > 4 && names.size() > 4) {
//            String name = names.get(names.size() - 1).getName().toLowerCase();
//            if (!name.startsWith("b")) {
//                f = reverse(f);
//            }
//        }
        // otherwise use the alternative colour space.
        float y[] = tintTransform.calculate(reverse(f));
        return alternate.getColor(reverse(y));
    }
}



