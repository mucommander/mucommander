/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.util;

import java.text.Collator;
import java.util.Locale;

/**
 * This class contains convenience methods for working with strings.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public final class StringUtils {

	public static final String EMPTY = "";

    /**
     * Prevents instantiation of this class.
     */
    private StringUtils() {
    }

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
        return matchesIgnoreCase(a, b, a.length());
    }

    /**
     * Returns <code>true</code> if the substring of <code>a</code> starting at <code>posA</code> matches <code>b</code> regardless of the case.
     * <p>
     * This method has a known bug under some alphabets with peculiar capitalisation rules such as the Georgian one,
     * where <code>Character.toUpperCase(a) == Character.toUpperCase(b)</code> doesn't necessarily imply that
     * <code>Character.toLowerCase(a) == Character.toLowerCase(b)</code>. The performance hit of testing for this
     * exceptions is so huge that it was deemed an acceptable issue.
     * </p>
     * <p>
     * Note that this method will return <code>true</code> if <code>b</code> is an emptry string.
     * </p>
     * @param  a                              string to test.
     * @param  b                              suffix to test for.
     * @param  posA                           position in <code>a</code> at which to look for <code>b</code>
     * @return                                <code>true</code> if <code>a</code> ends with <code>b</code> regardless of the case, <code>false</code> otherwise.
     * @throws ArrayIndexOutOfBoundsException if <code>a.length</code> is smaller than <code>posA</code>.
     */
    public static boolean matchesIgnoreCase(String a, String b, int posA) {
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
    public static boolean endsWithIgnoreCase(String a, char[] b) {return matchesIgnoreCase(a, b, a.length());}

    /**
     * Returns <code>true</code> if the substring of <code>a</code> starting at <code>posA</code> matches <code>b</code> regardless of the case.
     * <p>
     * This method has a known bug under some alphabets with peculiar capitalisation rules such as the Georgian one,
     * where <code>Character.toUpperCase(a) == Character.toUpperCase(b)</code> doesn't necessarily imply that
     * <code>Character.toLowerCase(a) == Character.toLowerCase(b)</code>. The performance hit of testing for this
     * exceptions is so huge that it was deemed an acceptable issue.
     * </p>
     * <p>
     * Note that this method will return <code>true</code> if <code>b</code> is an emptry string.
     * </p>
     * @param  a                              string to test.
     * @param  b                              suffix to test for.
     * @param  posA                           position in <code>a</code> at which to look for <code>b</code>
     * @return                                <code>true</code> if <code>a</code> ends with <code>b</code> regardless of the case, <code>false</code> otherwise.
     * @throws ArrayIndexOutOfBoundsException if <code>a.length</code> is smaller than <code>posA</code>.
     */
    public static boolean matchesIgnoreCase(String a, char[] b, int posA) {
        int  posB; // Position in b.
        char cA;   // Current character in a.
        char cB;   // Current character in b.

        // Checks whether there's any point in testing the strings.
        if(posA < (posB = b.length))
            return false;

        while(posB > 0) {
            if(!Character.isLowerCase(cA = a.charAt(--posA)))
                cA = Character.toLowerCase(cA);
            if(!Character.isLowerCase(cB = b[--posB]))
                cB = Character.toLowerCase(cB);
            if(cA != cB)
                return false;
        }
        return true;
    }

    /**
     * Equivalent of <code>String.endsWith(String)</code> using a <code>char[]</code>.
     * @param  a String to test.
     * @param  b suffix to test.
     * @return   <code>true</code> if <code>a</code> ends with <code>b</code>.
     */
    public static boolean endsWith(String a, char[] b) {
        return matches(a, b, a.length());
    }

    /**
     * Returns <code>true</code> if the substring of <code>a</code> ending at <code>posA</code> matches <code>b</code>.
     * @param  a    String to test.
     * @param  b    substring to look for.
     * @param  posA position in <code>a</code> at which to look for <code>b</code>
     * @return      <code>true</code> if <code>a</code> contains <code>b</code> at position <code>posA - b.length()</code>, <code>false</code> otherwise..
     */
    public static boolean matches(String a, char[] b, int posA) {
        int posB;

        if(posA < (posB = b.length))
            return false;
        while(posB > 0)
            if(a.charAt(--posA) != b[--posB])
                return false;
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
     * This method is a locale-aware version of <code>java.lang.String#equals(Object)</code>. It returns
     * <code>true</code> if the two given <code>String</code> are equal in the specified <code>Locale</code>.
     *
     * <p>This method is useful for testing text expressed in a language where two strings with an identical
     * written representation can have a different <code>String</code> representation according to
     * <code>java.lang.String#equals(Object)</code>. Japanese is such a language for instance.
     * This method uses the <code>java.text.Collator</code> class under the hood.
     *
     * @param s1 a String to compare
     * @param s2 a String to compare
     * @param locale the Locale to consider for testing the String
     * @return true if the two given String are equal in the specified Locale
     */
    public static boolean equals(String s1, String s2, Locale locale) {
        return Collator.getInstance(locale).equals(s1, s2);
    }
    
    /**
     * Compares the two specified strings and returns <code>true</code> if both strings are equal. This method handles
     * <code>null</code> values with no risk of a <code>NullPointerException</code>. The comparison is case-sensitive
     * only if requested.
     *
     * <p>In other words, this method returns <code>true</code> if strings are either both <code>null</code>
     * or equal according to <code>String#equals(String)</code> for case-sensitive comparison, or
     * <code>String#equalsIgnoreCase(String)</code> for case-insensitive comparison.</p> 
     *
     * @param s1 string to compare, potentially <code>null</code>
     * @param s2 string to compare, potentially <code>null</code>
     * @param caseSensitive <code>true</code> for case-sensitive comparison, <code>false</code> for case-insensitive comparison
     * @return <code>true</code> if strings are equal or both null
     */
    public static boolean equals(String s1, String s2, boolean caseSensitive) {
        if(s1 == null && s2 == null)
            return true;

        if(caseSensitive)
            return s1 != null && s1.equals(s2);
        return s1 != null && s1.equalsIgnoreCase(s2);
    }

    /**
     * Parses the string argument as a signed decimal integer. If the string cannot be
     * parsed a default value is returned.
     * @param s a String containing the int representation to be parsed
     * @param def a default value if string cannot be parsed
     * @return the integer value represented by the argument or default value if it cannot be parsed 
     */
    public static int parseIntDef(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Capitalizes the given string, making its first character upper case, and the rest of them lower case.
     * This method reeturns an empty string if <code>null</code> or an empty string is passed.
     *
     * @param s the string to capitalize
     * @return the capitalized string
     */
    public static String capitalize(String s) {
        if(isNullOrEmpty(s))
            return EMPTY;

        StringBuilder out;

        out = new StringBuilder(s.length());
        out.append(Character.toUpperCase(s.charAt(0)));
        if(s.length() > 1)
            out.append(s.substring(1).toLowerCase());
        return out.toString();
    }


    /**
     * Shorthand for {@link #flatten(String[], String)} invoked with a <code>" "</code> separator.
     *
     * @param s the string array to flatten
     * @return the flattened string array
     */
    public static String flatten(String s[]) {
        return flatten(s, " ");
    }


    /**
     * Concatenates all of the given string array's elements into a single string, separating each element
     * by the specified separator string. <code>null</code> and empty string values are simply skipped and not
     * reflected in the returned string. If the string array is <code>null</code>, the returned string will also be
     * <code>null</code>.
     *
     * @param s the string array to flatten
     * @param separator the String that separates each
     * @return the flattened string array
     */
    public static String flatten(String s[], String separator) {
        if(s==null)
            return null;

        StringBuilder sb = new StringBuilder();
        int sLen = s.length;
        boolean first = true;
        String el;

        for(int i=0; i<sLen; i++) {
            el = s[i];
            if (isNullOrEmpty(el))
                continue;

            if(first)
                first = false;
            else
                sb.append(separator);

            sb.append(el);
        }

        return sb.toString();
    }

    /**
     * Returns true if the given string is null or empty (i.e. it's length is 0)
     *
     * @param string - the given String to check
     * @return true if the given string is null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(String string) {
    	return string == null || string.isEmpty();
    }
}
