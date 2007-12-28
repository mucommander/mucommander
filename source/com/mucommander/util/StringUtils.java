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
 * @author Maxence Bernard
 */
public class StringUtils {
    /**
     * Returns <code>true</code> if <code>a</code> ends with <code>b</code> regardless of the case.
     * @param a string to test.
     * @param b suffix to test for.
     * Returns <code>true</code> if <code>a</code> ends with <code>b</code> regardless of the case, <code>false</code> otherwise.</code>.
     */
    public static boolean endsWithIgnoreCase(String a, String b) {
        int bLength;

        return a.regionMatches(true, a.length() - (bLength = b.length()), b, 0, bLength);
    }

    /**
     * Returns <code>true</code> if <code>a</code> starts with <code>b</code> regardless of the case.
     * @param a string to test.
     * @param b preffix to test for.
     * Returns <code>true</code> if <code>a</code> starts with <code>b</code> regardless of the case, <code>false</code> otherwise.</code>.
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
        if(PlatformManager.getJavaVersion()>= PlatformManager.JAVA_1_5) {
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
