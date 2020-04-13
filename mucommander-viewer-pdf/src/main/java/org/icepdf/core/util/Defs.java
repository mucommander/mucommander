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
package org.icepdf.core.util;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Defs {

    private static final Logger logger =
            Logger.getLogger(Defs.class.toString());

    /**
     * Equivalent to property(name, null)
     */
    public static String property(String name) {
        return property(name, null);
    }

    /**
     * Return value for system property <code>name</code> or *
     * <code>defaultValue</code> if the property does not exist * or a security
     * manager denies access to it
     */
    public static String property(String name, String defaultValue) {
        try {
            return System.getProperty(name, defaultValue);
        } catch (SecurityException ex) {
            // recal method so that property change takes effect
            logger.log(Level.FINE, "Security exception, property could not be set.", ex);
        }
        return defaultValue;
    }


    /**
     * Return value for system property <code>name</code> parsed as int or *
     * <code>defaultValue</code> if the property does not exist * or a security
     * manager denies access to it
     */
    public static int intProperty(String name, int defaultValue) {
        String value = property(name);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                logger.log(Level.FINE, "Failed to parse property.", ex);
            }
        }
        return defaultValue;
    }

    /**
     * Return value for system property <code>name</code> parsed as double or *
     * <code>defaultValue</code> if the property does not exist * or a security
     * manager denies access to it
     */
    public static double doubleProperty(String name, double defaultValue) {
        String value = property(name);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException ex) {
                logger.log(Level.FINE, "Failed to parse property.", ex);
            }
        }
        return defaultValue;
    }


    /**
     * Shortcut for <code>booleanProperty(name, false)</code>
     */
    public static boolean booleanProperty(String name) {
        return booleanProperty(name, false);
    }

    /**
     * If security manager allow access to the system property <b>name</b> * and
     * it exists, then return true if it is set to <i>yes</i>, <i>true</i> * and
     * false if set to <i>no</i>, <i>false</i>. Otherwise returns *
     * <b>defaultValue</b>
     */
    public static boolean booleanProperty(String name, boolean defaultValue) {
        String value = property(name);
        if (value != null) {
            switch (value.length()) {
                case 2:
                    if ("no".equals(value.toLowerCase())) return false;
                    break;
                case 3:
                    if ("yes".equals(value.toLowerCase())) return true;
                    break;
                case 4:
                    if ("true".equals(value.toLowerCase())) return true;
                    break;
                case 5:
                    if ("false".equals(value.toLowerCase())) return false;
                    break;
            }
        }
        return defaultValue;
    }

    /**
     * Alias to property(String name)
     */
    public static String sysProperty(String name) {
        return property(name);
    }

    /**
     * Alias to property(String name, String defaultValue)
     */
    public static String sysProperty(String name, String defaultValue) {
        return property(name, defaultValue);
    }

    /**
     * Alias to intProperty(String name, int defaultValue)
     */
    public static int sysPropertyInt(String name, int defaultValue) {
        return intProperty(name, defaultValue);
    }

    /**
     * Alias to doubleProperty(String name, double defaultValue)
     */
    public static double sysPropertyDouble(String name, double defaultValue) {
        return doubleProperty(name, defaultValue);
    }

    /**
     * Alias to booleanProperty(String name)
     */
    public static boolean sysPropertyBoolean(String name) {
        return booleanProperty(name);
    }

    /**
     * Alias to booleanProperty(String name, boolean defaultValue)
     */
    public static boolean sysPropertyBoolean(String name,
                                             boolean defaultValue) {
        return booleanProperty(name, defaultValue);
    }

    /**
     * Set system property to <code>value</code>. * If SecurityManager denies
     * property modification, silently ignore * property change. * if value is
     * null, property won't be set.
     */
    public static void setProperty(String property, Object value) {
        try {
            Properties prop = System.getProperties();
            if (value != null) {
                prop.put(property, value);
            }
        } catch (SecurityException ex) {
            // recall method so that property change takes effect
            logger.log(Level.FINE, "Security exception, property could not be set.", ex);
        }
    }


    /**
     * Set system property to <code>value</code>. * If SecurityManager denies
     * property modification, print debug trace
     */
    public static void setSystemProperty(String name, String value) {
        setProperty(name, value);
    }


}
