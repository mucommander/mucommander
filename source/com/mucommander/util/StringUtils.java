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

package com.mucommander.util;

import com.mucommander.PlatformManager;

/**
 * This class contains convenience methods for working with strings.
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class StringUtils {
    /**
     * Prevents instanciation of this class.
     */
    private StringUtils() {}

    /**
     * Returns <code>true</code> if <code>a</code> ends with <code>b</code> regardless of the case.
     * <p>
     * This method has a known bug under some alphabets with peculiar capitalisation rules such as the Georgian one,
     * where <code>Character.toUpperCase(a) == Character.toUpperCase(b)</code> doesn't necessarily imply that
     * <code>Character.toLowerCase(a) == Character.toLowerCase(b)</code>. The performance hit of testing for this
     * exceptions is so huge that it was deemed an acceptable issue.
     * </p>
     * <p>
     * Note that this method will return <code>true</code> if <code>b</code> is an emptry string.
     * </p>
     * @param a string to test.
     * @param b suffix to test for.
     * @return <code>true</code> if <code>a</code> ends with <code>b</code> regardless of the case, <code>false</code> otherwise.
     */
    public static boolean endsWithIgnoreCase(String a, String b) {
        return endsWithIgnoreCase(a, b, a.length());
    }

    /**
     * Returns <code>true</code> if <code>a</code> ends with <code>b</code> regardless of the case.
     * <p>
     * This method is meant for optimisation purposes when <code>a.length()</code> is known and re-used frequently.
     * An example of such a case is when <code>a</code> is meant to be matched against a number of other strings
     * until a match is found.
     * </p>
     * <p>
     * This method has a known bug under some alphabets with peculiar capitalisation rules such as the Georgian one,
     * where <code>Character.toUpperCase(a) == Character.toUpperCase(b)</code> doesn't necessarily imply that
     * <code>Character.toLowerCase(a) == Character.toLowerCase(b)</code>. The performance hit of testing for this
     * exceptions is so huge that it was deemed an acceptable issue.
     * </p>
     * <p>
     * Note that this method will return <code>true</code> if <code>b</code> is an emptry string.
     * </p>
     * @param a    string to test.
     * @param b    suffix to test for.
     * @param posA length of <code>a</code>.
     * Returns <code>true</code> if <code>a</code> ends with <code>b</code> regardless of the case, <code>false</code> otherwise.</code>.
     */
    public static boolean endsWithIgnoreCase(String a, String b, int posA) {
        int  posB; // Position in b.
        char cA;   // Current character in a.
        char cB;   // Current character in b.

        // Checks whether there's any point in testing the strings.
        if(posA < (posB = b.length()))
            return false;

        // Loops until we've tested the whole of b.
        while(posB > 0) {

            // Works on lower-case characters only. 
            if(!Character.isLowerCase(cA = a.charAt(--posA)))
                cA = Character.toLowerCase(cA);
            if(!Character.isLowerCase(cB = b.charAt(--posB)))
                cB = Character.toLowerCase(cB);
            if(cA != cB)
                return false;
        }
        return true;
    }

    /**
     * Returns <code>true</code> if <code>a</code> starts with <code>b</code> regardless of the case.
     * <p>
     * Note that this method will return <code>true</code> if <code>b</code> is an emptry string.
     * </p>
     * @param a string to test.
     * @param b prefix to test for.
     * @return <code>true</code> if <code>a</code> starts with <code>b</code> regardless of the case, <code>false</code> otherwise..
     */
    public static boolean startsWithIgnoreCase(String a, String b) {
        return a.regionMatches(true, 0, b, 0, b.length());
    }


    /**
     * Replaces each occurrence of the target string in the given string with the specified replacement string, and
     * returns the resulting string. This method mimicks <code>java.lang.String#replace(CharSequence, CharSequence)</code>
     * which was introduced in Java 1.5, but unlike the latter, this method can be used on any version of Java.   
     *
     * <p>On Java 1.5 and up, this method delegates to <code>java.lang.String#replace(CharSequence, CharSequence)</code>.
     * On Java 1.4 or below, a custom implementation (that doesn't use Regexp) is used.</p>
     *
     * @param s the string in which to replace ocurrences of target
     * @param target the string to be replaced
     * @param replacement the replacement for occurrences of target
     * @return the resulting string
     */
    public static String replaceCompat(String s, String target, String replacement) {
        if(PlatformManager.JAVA_1_5.isCurrentOrHigher()) {
            // Java 1.5 or later
            return s.replace(target, replacement);
        }
        else {
            // Java 1.4 or below
            // Do not use Regexp because:
            // a) it's faster
            // b) the target string would have to be escaped
            StringBuffer sb = new StringBuffer();
            int pos = 0, lastPos = 0;
            while((pos=s.indexOf(target, pos))!=-1) {
                if(lastPos!=pos)
                    sb.append(s.substring(lastPos, pos));
                sb.append(replacement);

                pos += target.length();
                lastPos = pos;
            }

            int len = s.length();
            if(lastPos<len)
                sb.append(s.substring(lastPos, len));
            
            return sb.toString();
        }

    }
}
