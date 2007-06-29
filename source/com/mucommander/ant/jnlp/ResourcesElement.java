/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.ant.jnlp;

import java.util.*;

/**
 * @author Nicolas Rinaudo
 * @ant.type name="resources" category="webstart"
 */
public class ResourcesElement {
    private String os;
    private String arch;
    private String locale;
    private Vector j2ses;
    private Vector jars;
    private Vector nativeLibs;
    private Vector extensions;
    private Vector properties;
    private Vector packages;

    public ResourcesElement() {
        j2ses      = new Vector();
        jars       = new Vector();
        nativeLibs = new Vector();
        extensions = new Vector();
        properties = new Vector();
        packages   = new Vector();
    }

    public String getOs() {return os;}
    public String getArch() {return arch;}
    public String getLocale() {return locale;}
    public Iterator j2ses() {return j2ses.iterator();}
    public Iterator jars() {return jars.iterator();}
    public Iterator nativeLibs() {return nativeLibs.iterator();}
    public Iterator extensions() {return extensions.iterator();}
    public Iterator properties() {return properties.iterator();}
    public Iterator packages() {return packages.iterator();}

    public void setOs(String s) {os = s;}
    public void setArch(String s) {arch = s;}
    public void setLocale(String s) {locale = s;}
    public J2seElement createJ2se() {
        J2seElement buffer;

        buffer = new J2seElement();
        j2ses.add(buffer);

        return buffer;
    }

    public JarElement createJar() {
        JarElement buffer;

        buffer = new JarElement();
        jars.add(buffer);

        return buffer;
    }

    public NativeLibElement createNativeLib() {
        NativeLibElement buffer;

        buffer = new NativeLibElement();
        nativeLibs.add(buffer);

        return buffer;
    }

    public ExtensionElement createExtension() {
        ExtensionElement buffer;

        buffer = new ExtensionElement();
        extensions.add(buffer);

        return buffer;
    }

    public PropertyElement createProperty() {
        PropertyElement buffer;

        buffer = new PropertyElement();
        properties.add(buffer);

        return buffer;
    }

    public PackageElement createPackage() {
        PackageElement buffer;

        buffer = new PackageElement();
        packages.add(buffer);

        return buffer;
    }
}
