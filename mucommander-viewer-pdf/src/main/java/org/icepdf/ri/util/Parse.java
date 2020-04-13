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
package org.icepdf.ri.util;

import javax.swing.*;
import java.util.ResourceBundle;


/**
 * Utility class for parsing Strings to alternative types.  Errors are represented
 * with internationalized dialogs and corresponding error messages.
 *
 * @since 1.0
 */
final class Parse {

    private final static String[] booleanNames = {"yes", "no", "true", "false"};
    private final static boolean[] booleans = {true, false, true, false};

    public static Integer parseInteger(String s, ResourceBundle messageBundle) {
        s = s.trim();
        try {
            return new Integer(s);
        } catch (NumberFormatException ex) {
            if (messageBundle != null) {
                Resources.showMessageDialog(null,
                        JOptionPane.INFORMATION_MESSAGE, messageBundle,
                        "parse.title",
                        "parse.integer",
                        s);
            }
        }
        return null;
    }

    public static Long parseLong(String s, ResourceBundle messageBundle) {
        s = s.trim();
        try {
            return new Long(s);
        } catch (NumberFormatException ex) {
            if (messageBundle != null) {
                Resources.showMessageDialog(null,
                        JOptionPane.INFORMATION_MESSAGE, messageBundle,
                        "parse.title",
                        "parse.float",
                        s);
            }
        }
        return null;
    }

    public static Float parseFloat(String s, ResourceBundle messageBundle) {
        s = s.trim();
        try {
            return new Float(s);
        } catch (NumberFormatException ex) {
            if (messageBundle != null) {
                Resources.showMessageDialog(null,
                        JOptionPane.INFORMATION_MESSAGE, messageBundle,
                        "parse.title",
                        "parse.float",
                        s);
            }
        }
        return null;
    }

    /**
     * Parse a string into a double number.  Error is added to errorShower.
     *
     * @param s string to be coverted to double if possible
     * @return a null if the string could not be converted to double, otherwise
     *         return the Double value of the string.
     */
    public static Double parseDouble(String s, ResourceBundle messageBundle) {
        s = s.trim();
        try {
            return new Double(s);
        } catch (NumberFormatException ex) {
            if (messageBundle != null) {
                Resources.showMessageDialog(null,
                        JOptionPane.INFORMATION_MESSAGE, messageBundle,
                        "parse.title",
                        "parse.double",
                        s);
            }
        }
        return null;
    }

    public static Boolean parseBoolean(String s, ResourceBundle messageBundle) {
        s = s.trim();
        for (int i = 0; i < booleanNames.length; i++) {
            if (s.equalsIgnoreCase(booleanNames[i])) {
                return booleans[i] ? Boolean.TRUE : Boolean.FALSE;
            }
        }
        if (messageBundle != null) {
            Resources.showMessageDialog(null,
                    JOptionPane.INFORMATION_MESSAGE, messageBundle,
                    "parse.title",
                    "parse.choice",
                    s);
        }
        return null;
    }

    public static String parseLookAndFeel(String s, ResourceBundle messageBundle) {
        s = s.trim();
        UIManager.LookAndFeelInfo[] looks = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo look : looks) {
            if (s.equalsIgnoreCase(look.getName())) {
                return look.getClassName();
            }
        }
        if (messageBundle != null) {
            Resources.showMessageDialog(null,
                    JOptionPane.INFORMATION_MESSAGE, messageBundle,
                    "parse.title",
                    "parse.laf",
                    s);
        }
        return null;
    }

}


