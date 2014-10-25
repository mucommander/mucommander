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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tests the {@link StringUtils} class.
 * @author Nicolas Rinaudo
 */
public class StringUtilsTest {
    // - endsWith tests ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Provides test cases for {@link #testEndsWith(String, String, boolean)}.
     * @return test cases for {@link #testEndsWith(String, String, boolean)}.
     */
    @DataProvider(name = "endsWith")
    public Iterator<Object[]> endsWithTestCases() {
      List<Object[]> data;

        data = new ArrayList<Object[]>();

        data.add(new Object[] {"abc", "c",   true});
        data.add(new Object[] {"abc", "bc",  true});
        data.add(new Object[] {"abc", "abc", true});
        data.add(new Object[] {"abc", "",    true});

        data.add(new Object[] {"abc", "C",   false});
        data.add(new Object[] {"abc", "BC",  false});
        data.add(new Object[] {"abc", "ABC", false});

        data.add(new Object[] {"abc", "d",   false});
        data.add(new Object[] {"abc", "de",  false});
        data.add(new Object[] {"abc", "def", false});

        return data.iterator();
    }

    /**
     * Tests the {@link StringUtils#endsWith(String, char[])} method.
     * @param a        string to compare.
     * @param b        char array to test.
     * @param expected expected return value of {@link StringUtils#endsWith(String, char[])}.
     */
    @Test(dataProvider = "endsWith")
    public void testEndsWith(String a, String b, boolean expected) {
        assert StringUtils.endsWith(a, b.toCharArray()) == expected;
    }



    // - matchesIgnoreCase tests ---------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    @DataProvider(name = "matchesIgnoreCase")
    public Iterator<Object[]> matchesIgnoreCaseTestCases() {
      List<Object[]> data;

        data = new ArrayList<Object[]>();

        data.add(new Object[] {"abc", "C", 3, true});
        data.add(new Object[] {"abc", "B", 2, true});
        data.add(new Object[] {"abc", "A", 1, true});
        data.add(new Object[] {"abc", "",  0, true});

        data.add(new Object[] {"abc", "ABC", 3, true});
        data.add(new Object[] {"abc", "AB",  2, true});
        data.add(new Object[] {"abc", "A",   1, true});
        data.add(new Object[] {"abc", "",    0, true});

        data.add(new Object[] {"abc", "abc", 2, false});

        data.add(new Object[] {"abc", "123", 3, false});
        data.add(new Object[] {"abc", "123", 2, false});
        data.add(new Object[] {"abc", "123", 1, false});
        data.add(new Object[] {"abc", "123", 0, false});

        return data.iterator();
    }

    @Test(dataProvider = "matchesIgnoreCase")
    public void testMatchesIgnoreCaseCharArray(String a, String b, int pos, boolean expected) {
        assert StringUtils.matchesIgnoreCase(a, b.toCharArray(), pos) == expected;
    }

    @Test(dataProvider = "matchesIgnoreCase")
    public void testMatchesIgnoreCase(String a, String b, int pos, boolean expected) {
        assert StringUtils.matchesIgnoreCase(a, b, pos) == expected;
    }



    // - matches tests -------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Provides test cases for {@link #testMatches(String, String, int, boolean)}.
     * @return test cases for {@link #testMatches(String, String, int, boolean)}.
     */
    @DataProvider(name = "matches")
    public Iterator<Object[]> matchesTestCases() {
      List<Object[]> data;

        data = new ArrayList<Object[]>();

        data.add(new Object[] {"abc", "c", 3, true});
        data.add(new Object[] {"abc", "b", 2, true});
        data.add(new Object[] {"abc", "a", 1, true});
        data.add(new Object[] {"abc", "",  0, true});

        data.add(new Object[] {"abc", "abc", 3, true});
        data.add(new Object[] {"abc", "ab",  2, true});
        data.add(new Object[] {"abc", "a",   1, true});
        data.add(new Object[] {"abc", "",    0, true});

        data.add(new Object[] {"abc", "abc", 2, false});
        
        data.add(new Object[] {"abc", "aBC", 3, false});
        data.add(new Object[] {"abc", "ABC", 2, false});
        data.add(new Object[] {"abc", "aBc", 1, false});
        data.add(new Object[] {"abc", "ABc", 0, false});

        return data.iterator();
    }

    /**
     * Tests the {@link StringUtils#matches(String, char[], int)} method.
     * @param a        first string.
     * @param b        second string.
     * @param pos      position at which to start the comparison.
     * @param expected expected return value of {@link StringUtils#matches(String, char[], int)}
     */
    @Test(dataProvider = "matches")
    public void testMatches(String a, String b, int pos, boolean expected) {
        assert StringUtils.matches(a, b.toCharArray(), pos) == expected;
    }



    // - parseIntDef tests ---------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Provides test cases for {@link #testParseIntDef(String, int, int)}.
     * @return test cases for {@link #testParseIntDef(String, int, int)}.
     */
    @DataProvider(name = "parseIntDef")
    public Iterator<Object[]> parseIntDefTestCases() {
      List<Object[]> data;

        data = new ArrayList<Object[]>();

        for(int i = 0; i < 10; i++) {
            data.add(new Object[] {Integer.toString(i), 0, i});
            data.add(new Object[] {"foobar", i, i});
        }

        return data.iterator();
    }

    /**
     * Tests the {@link StringUtils#parseIntDef(String, int)} method.
     * @param input    string to parse.
     * @param def      default value.
     * @param expected expected return value of {@link StringUtils#parseIntDef(String, int)}.
     */
    @Test(dataProvider = "parseIntDef")
    public void testParseIntDef(String input, int def, int expected) {
        assert StringUtils.parseIntDef(input, def) == expected;
    }


    // - endsWithIgnoreCase tests --------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Provides test cases for {@link #testEndsWithIgnoreCaseCharArray(String, String, boolean)} and {@link #testEndsWithIgnoreCase(String, String, boolean)}.
     * @return test cases for {@link #testEndsWithIgnoreCaseCharArray(String, String, boolean)} and {@link #testEndsWithIgnoreCase(String, String, boolean)}.
     */
    @DataProvider(name = "endsWithIgnoreCase")
    public Iterator<Object[]> endsWithIgnoreCaseTestCases() {
        List<Object[]> data;
        
        data = new ArrayList<Object[]>();
        
        data.add(new Object[] {"this is a test", "a test", true});
        data.add(new Object[] {"this is a test", "a TeSt", true});
        data.add(new Object[] {"this is a test", "A TEST", true});

        data.add(new Object[] {"THIS IS A TEST", "a test", true});
        data.add(new Object[] {"THIS IS A TEST", "a TeSt", true});
        data.add(new Object[] {"THIS IS A TEST", "A TEST", true});

        data.add(new Object[] {"ThIs Is A TeSt", "a test", true});
        data.add(new Object[] {"ThIs Is A TeSt", "a TeSt", true});
        data.add(new Object[] {"ThIs Is A TeSt", "A TEST", true});

        data.add(new Object[] {"this is a test", "this is a test", true});
        data.add(new Object[] {"this is a test", "ThIs Is A TeSt", true});
        data.add(new Object[] {"this is a test", "THIS IS A TEST", true});

        data.add(new Object[] {"THIS IS A TEST", "this is a test", true});
        data.add(new Object[] {"THIS IS A TEST", "ThIs Is A TeSt", true});
        data.add(new Object[] {"THIS IS A TEST", "THIS IS A TEST", true});

        data.add(new Object[] {"ThIs Is A TeSt", "this is a test", true});
        data.add(new Object[] {"ThIs Is A TeSt", "ThIs Is A TeSt", true});
        data.add(new Object[] {"ThIs Is A TeSt", "THIS IS A TEST", true});

        data.add(new Object[] {"this is a test", "", true});
        
        data.add(new Object[] {"this is a test", "test a is this", false});
        data.add(new Object[] {"this is a test", "tEsT a Is ThIs", false});
        data.add(new Object[] {"this is a test", "TEST A IS THIS", false});

        data.add(new Object[] {"THIS IS A TEST", "test a is this", false});
        data.add(new Object[] {"THIS IS A TEST", "tEsT a Is ThIs", false});
        data.add(new Object[] {"THIS IS A TEST", "TEST A IS THIS", false});

        data.add(new Object[] {"ThIs Is A tEst", "test a is this", false});
        data.add(new Object[] {"ThIs Is A tEst", "tEsT a Is ThIs", false});
        data.add(new Object[] {"ThIs Is A tEst", "TEST A IS THIS", false});
        
        return data.iterator();
    }

    /**
     * Test the {@link StringUtils#endsWithIgnoreCase(String, String)} method.
     * @param a        first string to compare.
     * @param b        second string to compare.
     * @param expected expected return value of {@link StringUtils#endsWithIgnoreCase(String, String)}
     */
    @Test(dataProvider = "endsWithIgnoreCase")
    public void testEndsWithIgnoreCase(String a, String b, boolean expected) {
        assert StringUtils.endsWithIgnoreCase(a, b) == expected;
    }

    /**
     * Test the {@link StringUtils#endsWithIgnoreCase(String, char[])} method.
     * @param a        first string to compare.
     * @param b        second string to compare.
     * @param expected expected return value of {@link StringUtils#endsWithIgnoreCase(String, char[])}
     */
    @Test(dataProvider = "endsWithIgnoreCase")
    public void testEndsWithIgnoreCaseCharArray(String a, String b, boolean expected) {
        assert StringUtils.endsWithIgnoreCase(a, b.toCharArray()) == expected;
    }



    // - startsWithIgnoreCase tests ------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Provides test cases for {@link #testStartsWithIgnoreCase(String, String, boolean)}.
     * @return test cases for {@link #testStartsWithIgnoreCase(String, String, boolean)}.
     */
    @DataProvider(name = "startsWithIgnoreCase")
    public Iterator<Object[]> startsWithIgnoreCaseTestCases() {
        List<Object[]> data;

        data = new ArrayList<Object[]>();

        data.add(new Object[] {"this is a test", "this is", true});
        data.add(new Object[] {"this is a test", "ThIs Is", true});
        data.add(new Object[] {"this is a test", "THIS IS", true});

        data.add(new Object[] {"THIS IS A TEST", "this is", true});
        data.add(new Object[] {"THIS IS A TEST", "ThIs Is", true});
        data.add(new Object[] {"THIS IS A TEST", "THIS IS", true});

        data.add(new Object[] {"ThIs Is a tEsT", "this is", true});
        data.add(new Object[] {"ThIs Is a tEsT", "ThIs Is", true});
        data.add(new Object[] {"ThIs Is a tEsT", "THIS IS", true});

        data.add(new Object[] {"this is a test", "this is a test", true});
        data.add(new Object[] {"this is a test", "THIS IS A TEST", true});
        data.add(new Object[] {"this is a test", "ThIs Is a tEsT", true});

        data.add(new Object[] {"THIS IS A TEST", "this is a test", true});
        data.add(new Object[] {"THIS IS A TEST", "THIS IS A TEST", true});
        data.add(new Object[] {"THIS IS A TEST", "ThIs Is a tEsT", true});

        data.add(new Object[] {"ThIs Is a tEsT", "this is a test", true});
        data.add(new Object[] {"ThIs Is a tEsT", "THIS IS A TEST", true});
        data.add(new Object[] {"ThIs Is a tEsT", "ThIs Is a tEsT", true});

        data.add(new Object[] {"ThIs Is a tEsT", "", true});
        data.add(new Object[] {"THIS IS A TEST", "", true});
        data.add(new Object[] {"ThIs Is a tEsT", "", true});

        data.add(new Object[] {"this is a test", "test a is this", false});
        data.add(new Object[] {"this is a test", "TEST A IS THIS", false});
        data.add(new Object[] {"this is a test", "TeSt A iS tHiS", false});

        data.add(new Object[] {"THIS IS A TEST", "test a is this", false});
        data.add(new Object[] {"THIS IS A TEST", "TEST A IS THIS", false});
        data.add(new Object[] {"THIS IS A TEST", "TeSt A iS tHiS", false});

        data.add(new Object[] {"ThIs Is A tEsT", "test a is this", false});
        data.add(new Object[] {"ThIs Is A tEsT", "TEST A IS THIS", false});
        data.add(new Object[] {"ThIs Is A tEsT", "TeSt A iS tHiS", false});

        return data.iterator();
    }

    /**
     * Tests {@link StringUtils#startsWithIgnoreCase(String, String)}.
     * @param a        first string to compare.
     * @param b        second string to compare.
     * @param expected expected return value of {@link StringUtils#startsWithIgnoreCase(String, String)}.
     */
    @Test(dataProvider = "startsWithIgnoreCase")
    public void testStartsWithIgnoreCase(String a, String b, boolean expected) {
        assert StringUtils.startsWithIgnoreCase(a, b) == expected;
    }



    // - equals tests --------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Provides test cases for {@link #testCaseInsensitiveEquals(String, String, boolean)}.
     * @return test cases for {@link #testCaseInsensitiveEquals(String, String, boolean)}.
     */
    @DataProvider(name = "caseInsensitiveEquals")
    public Iterator<Object[]> caseInsensitiveEqualsTestCases() {
        List<Object[]> data;

        data = new ArrayList<Object[]>();

        data.add(new Object[] {"a",  "a",  true});
        data.add(new Object[] {"A",  "A",  true});
        data.add(new Object[] {"a",  "A",  true});
        data.add(new Object[] {"A",  "a",  true});
        data.add(new Object[] {null, null, true});
        data.add(new Object[] {null, "a",  false});
        data.add(new Object[] {"a",  null, false});
        data.add(new Object[] {"a",  "z",  false});

        return data.iterator();
    }

    /**
     * Provides test cases for {@link #testCaseSensitiveEquals(String, String, boolean)}.
     * @return test cases for {@link #testCaseSensitiveEquals(String, String, boolean)}.
     */
    @DataProvider(name = "caseSensitiveEquals")
    public Iterator<Object[]> caseSensitiveEqualsTestCases() {
        List<Object[]> data;

        data = new ArrayList<Object[]>();

        data.add(new Object[] {"a",  "a",  true});
        data.add(new Object[] {"A",  "A",  true});
        data.add(new Object[] {null, null, true});
        data.add(new Object[] {null, "a",  false});
        data.add(new Object[] {"a",  null, false});
        data.add(new Object[] {"a",  "A",  false});
        data.add(new Object[] {"A",  "a",  false});


        return data.iterator();
    }

    /**
     * Tests the {@link StringUtils#equals(String, String, boolean)} method (case insensitive).
     * @param a        first string to compare.
     * @param b        second string to compare
     * @param expected expected return value of {@link StringUtils#equals(String, String, boolean)}
     */
    @Test(dataProvider = "caseSensitiveEquals")
    public void testCaseSensitiveEquals(String a, String b, boolean expected) {
        assert StringUtils.equals(a, b, true) == expected;
    }

    /**
     * Tests the {@link StringUtils#equals(String, String, boolean)} method (case sensitive).
     * @param a        first string to compare.
     * @param b        second string to compare
     * @param expected expected return value of {@link StringUtils#equals(String, String, boolean)}
     */
    @Test(dataProvider = "caseInsensitiveEquals")
    public void testCaseInsensitiveEquals(String a, String b, boolean expected) {
        assert StringUtils.equals(a, b, false) == expected;
    }



    // - capitalize tests ----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Provides test cases for {@link #testCapitalize(String, String)}.
     * @return test cases for {@link #testCapitalize(String, String)}.
     */
    @DataProvider(name = "capitalize")
    public Iterator<String[]> capitalizeTestCases() {
        List<String[]> data;

        data = new ArrayList<String[]>();

        data.add(new String[] {"bob",         "Bob"});
        data.add(new String[] {"BOB",         "Bob"});
        data.add(new String[] {"bOB",         "Bob"});
        data.add(new String[] {"boB",         "Bob"});
        data.add(new String[] {"Bob",         "Bob"});
        data.add(new String[] {"Bob Servant", "Bob servant"});
        data.add(new String[] {"b",           "B"});
        data.add(new String[] {"",            ""});
        data.add(new String[] {null,          ""});
        data.add(new String[] {"7",           "7"});

        return data.iterator();
    }

    /**
     * Tests the {@link StringUtils#capitalize(String)} method.
     * @param input    string to capitalize.
     * @param expected expected result of the capitalization.
     */
    @Test(dataProvider = "capitalize")
    public void testCapitalize(String input, String expected) {
        assert expected.equals(StringUtils.capitalize(input));
    }



    // - flatten tests -------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Provides test cases for {@link #testFlatten(String, String[], String)}.
     * @return test cases for {@link #testFlatten(String, String[], String)}.
     */
    @DataProvider(name = "flatten")
    public Iterator<Object[]> flattenTestCases() {
        List<Object[]> data;

        data = new ArrayList<Object[]>();

        data.add(new Object[] {"a b c", new String[] {"a", "b", "c"}, " "});
        data.add(new Object[] {"a*b*c", new String[] {"a", "b", "c"}, "*"});
        data.add(new Object[] {"a*c", new String[] {"a", "", "c"}, "*"});
        data.add(new Object[] {"a*c", new String[] {"a", null, "c"}, "*"});
        data.add(new Object[] {"b", new String[] {null, "b", null}, "*"});
        data.add(new Object[] {"", new String[] {null, null, null}, "*"});

        return data.iterator();
    }

    /**
     * Tests {@link StringUtils#flatten(String[], String)}.
     * @param expected  expected returned value of {@link StringUtils#flatten(String[], String)}.
     * @param data      data to flatten.
     * @param separator separator to use when flattening.
     */
    @Test(dataProvider = "flatten")
    public void testFlatten(String expected, String[] data, String separator) {
        assert expected.equals(StringUtils.flatten(data, separator));
        if(separator.equals(" "))
            assert expected.equals(StringUtils.flatten(data));
    }

    /**
     * Tests {@link StringUtils#flatten(String[], String)}
     */
    @Test
    public void testFlatten() {
        assert null == StringUtils.flatten(null, "*");
        assert null == StringUtils.flatten(null);
    }
}
