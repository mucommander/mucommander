/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.extension;

import java.lang.reflect.Modifier;
import java.lang.reflect.Constructor;
import java.util.Vector;
import java.io.IOException;
import javax.swing.LookAndFeel;

/**
 * Class filter for look and feels.
 * <p>
 * This filter will only accept classes if:
 * <ul>
 *   <li>They subclass <code>javax.swing.LookAndFeel</code>.</li>
 *   <li>They are public and not abstract.</li>
 *   <li>They have a public, no-arg constructor.</li>
 *   <li>Their <code>isSupportedLookAndFeel</code> method returns <code>true</code>.</li>
 *   <li>They are not an inner class.</li>
 * </ul>
 * </p>
 * @author Nicolas Rinaudo
 */
public class LookAndFeelFilter implements ClassFilter {
    /**
     * Creates a new instance of <code>LookAndFeelFilter</code>.
     */
    public LookAndFeelFilter() {}

    /**
     * Filters out everything but available look and feels.
     * @param c class to check.
     * @return <code>true</code> if c is an available look and feel, <code>false</code> otherwise.
     */
    public boolean accept(Class c) {
        int         modifiers;   // Class' modifiers.
        Constructor constructor; // Public, no-arg constructor.
        Class       buffer;      // Used to explore c's ancestors.

        // Ignores inner classes.
        if(c.getDeclaringClass() != null)
            return false;

        // Makes sure the class is public and non abstract.
        modifiers = c.getModifiers();
        if(!Modifier.isPublic(modifiers) || Modifier.isAbstract(modifiers))
            return false;

        // Makes sure the class has a public, no-arg constructor.
        try {constructor = c.getDeclaredConstructor(new Class[0]);}
        catch(Exception e) {return false;}
        if(!Modifier.isPublic(constructor.getModifiers()))
            return false;

        // Makes sure the class extends javax.swing.LookAndFeel and that if it does,
        // it's supported by the system.
        buffer = c;
        while(buffer != null) {
            // c is a LookAndFeel, makes sure it's supported.
            if(buffer.equals(LookAndFeel.class)) {
                try {return ((LookAndFeel)c.newInstance()).isSupportedLookAndFeel();}
                catch(Throwable e) {e.printStackTrace();return false;}
            }
            buffer = buffer.getSuperclass();
        }
        return false;
    }
}
