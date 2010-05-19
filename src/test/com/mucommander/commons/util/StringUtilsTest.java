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

import org.testng.annotations.Test;

/**
 * Tests the {@link StringUtils} class.
 * @author Nicolas Rinaudo
 */
public class StringUtilsTest {
    /**
     * Runs through a variety of {@link StringUtils#endsWithIgnoreCase(String,String)} test cases.
     */
    @Test
    public void testEndsWithString() {
        assert StringUtils.endsWithIgnoreCase("this is a test", "a test");
        assert StringUtils.endsWithIgnoreCase("this is a test", "a TeSt");
        assert StringUtils.endsWithIgnoreCase("this is a test", "A TEST");

        assert StringUtils.endsWithIgnoreCase("THIS IS A TEST", "a test");
        assert StringUtils.endsWithIgnoreCase("THIS IS A TEST", "a TeSt");
        assert StringUtils.endsWithIgnoreCase("THIS IS A TEST", "A TEST");

        assert StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "a test");
        assert StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "a TeSt");
        assert StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "A TEST");

        assert StringUtils.endsWithIgnoreCase("this is a test", "this is a test");
        assert StringUtils.endsWithIgnoreCase("this is a test", "ThIs Is A TeSt");
        assert StringUtils.endsWithIgnoreCase("this is a test", "THIS IS A TEST");

        assert StringUtils.endsWithIgnoreCase("THIS IS A TEST", "this is a test");
        assert StringUtils.endsWithIgnoreCase("THIS IS A TEST", "ThIs Is A TeSt");
        assert StringUtils.endsWithIgnoreCase("THIS IS A TEST", "THIS IS A TEST");

        assert StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "this is a test");
        assert StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "ThIs Is A TeSt");
        assert StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "THIS IS A TEST");

        assert StringUtils.endsWithIgnoreCase("this is a test", "");

        assert !StringUtils.endsWithIgnoreCase("this is a test", "test a is this");
        assert !StringUtils.endsWithIgnoreCase("this is a test", "tEsT a Is ThIs");
        assert !StringUtils.endsWithIgnoreCase("this is a test", "TEST A IS THIS");

        assert !StringUtils.endsWithIgnoreCase("THIS IS A TEST", "test a is this");
        assert !StringUtils.endsWithIgnoreCase("THIS IS A TEST", "tEsT a Is ThIs");
        assert !StringUtils.endsWithIgnoreCase("THIS IS A TEST", "TEST A IS THIS");

        assert !StringUtils.endsWithIgnoreCase("ThIs Is A tEst", "test a is this");
        assert !StringUtils.endsWithIgnoreCase("ThIs Is A tEst", "tEsT a Is ThIs");
        assert !StringUtils.endsWithIgnoreCase("ThIs Is A tEst", "TEST A IS THIS");
    }

    /**
     * Runs through a variety of {@link StringUtils#endsWithIgnoreCase(String,char[])} test cases.
     */
    @Test
    public void testEndsWithCharArray() {
        assert StringUtils.endsWithIgnoreCase("this is a test", "a test".toCharArray());
        assert StringUtils.endsWithIgnoreCase("this is a test", "a TeSt".toCharArray());
        assert StringUtils.endsWithIgnoreCase("this is a test", "A TEST".toCharArray());

        assert StringUtils.endsWithIgnoreCase("THIS IS A TEST", "a test".toCharArray());
        assert StringUtils.endsWithIgnoreCase("THIS IS A TEST", "a TeSt".toCharArray());
        assert StringUtils.endsWithIgnoreCase("THIS IS A TEST", "A TEST".toCharArray());

        assert StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "a test".toCharArray());
        assert StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "a TeSt".toCharArray());
        assert StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "A TEST".toCharArray());

        assert StringUtils.endsWithIgnoreCase("this is a test", "this is a test".toCharArray());
        assert StringUtils.endsWithIgnoreCase("this is a test", "ThIs Is A TeSt".toCharArray());
        assert StringUtils.endsWithIgnoreCase("this is a test", "THIS IS A TEST".toCharArray());

        assert StringUtils.endsWithIgnoreCase("THIS IS A TEST", "this is a test".toCharArray());
        assert StringUtils.endsWithIgnoreCase("THIS IS A TEST", "ThIs Is A TeSt".toCharArray());
        assert StringUtils.endsWithIgnoreCase("THIS IS A TEST", "THIS IS A TEST".toCharArray());

        assert StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "this is a test".toCharArray());
        assert StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "ThIs Is A TeSt".toCharArray());
        assert StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "THIS IS A TEST".toCharArray());

        assert StringUtils.endsWithIgnoreCase("this is a test", "".toCharArray());

        assert !StringUtils.endsWithIgnoreCase("this is a test", "test a is this".toCharArray());
        assert !StringUtils.endsWithIgnoreCase("this is a test", "tEsT a Is ThIs".toCharArray());
        assert !StringUtils.endsWithIgnoreCase("this is a test", "TEST A IS THIS".toCharArray());

        assert !StringUtils.endsWithIgnoreCase("THIS IS A TEST", "test a is this".toCharArray());
        assert !StringUtils.endsWithIgnoreCase("THIS IS A TEST", "tEsT a Is ThIs".toCharArray());
        assert !StringUtils.endsWithIgnoreCase("THIS IS A TEST", "TEST A IS THIS".toCharArray());

        assert !StringUtils.endsWithIgnoreCase("ThIs Is A tEst", "test a is this".toCharArray());
        assert !StringUtils.endsWithIgnoreCase("ThIs Is A tEst", "tEsT a Is ThIs".toCharArray());
        assert !StringUtils.endsWithIgnoreCase("ThIs Is A tEst", "TEST A IS THIS".toCharArray());
    }

    /**
     * Runs through a variety of {@link StringUtils#startsWithIgnoreCase(String,String)} test cases.
     */
    @Test
    public void testStartsWith() {
        assert StringUtils.startsWithIgnoreCase("this is a test", "this is");
        assert StringUtils.startsWithIgnoreCase("this is a test", "ThIs Is");
        assert StringUtils.startsWithIgnoreCase("this is a test", "THIS IS");

        assert StringUtils.startsWithIgnoreCase("THIS IS A TEST", "this is");
        assert StringUtils.startsWithIgnoreCase("THIS IS A TEST", "ThIs Is");
        assert StringUtils.startsWithIgnoreCase("THIS IS A TEST", "THIS IS");

        assert StringUtils.startsWithIgnoreCase("ThIs Is A tEsT", "this is");
        assert StringUtils.startsWithIgnoreCase("ThIs Is A tEsT", "ThIs Is");
        assert StringUtils.startsWithIgnoreCase("ThIs Is A tEsT", "THIS IS");

        assert StringUtils.startsWithIgnoreCase("this is a test", "this is a test");
        assert StringUtils.startsWithIgnoreCase("this is a test", "THIS IS A TEST");
        assert StringUtils.startsWithIgnoreCase("this is a test", "ThIs Is A tEsT");

        assert StringUtils.startsWithIgnoreCase("THIS IS A TEST", "this is a test");
        assert StringUtils.startsWithIgnoreCase("THIS IS A TEST", "THIS IS A TEST");
        assert StringUtils.startsWithIgnoreCase("THIS IS A TEST", "ThIs Is A tEsT");

        assert StringUtils.startsWithIgnoreCase("ThIs Is A tEsT", "this is a test");
        assert StringUtils.startsWithIgnoreCase("ThIs Is A tEsT", "THIS IS A TEST");
        assert StringUtils.startsWithIgnoreCase("ThIs Is A tEsT", "ThIs Is A tEsT");

        assert StringUtils.startsWithIgnoreCase("this is a test", "");
        assert StringUtils.startsWithIgnoreCase("THIS IS A TEST", "");
        assert StringUtils.startsWithIgnoreCase("ThIs Is A tEsT", "");

        assert !StringUtils.startsWithIgnoreCase("this is a test", "test a is this");
        assert !StringUtils.startsWithIgnoreCase("this is a test", "TEST A IS THIS");
        assert !StringUtils.startsWithIgnoreCase("this is a test", "TeSt A iS tHiS");

        assert !StringUtils.startsWithIgnoreCase("THIS IS A TEST", "test a is this");
        assert !StringUtils.startsWithIgnoreCase("THIS IS A TEST", "TEST A IS THIS");
        assert !StringUtils.startsWithIgnoreCase("THIS IS A TEST", "TeSt A iS tHiS");

        assert !StringUtils.startsWithIgnoreCase("ThIs Is A tEsT", "test a is this");
        assert !StringUtils.startsWithIgnoreCase("ThIs Is A tEsT", "TEST A IS THIS");
        assert !StringUtils.startsWithIgnoreCase("ThIs Is A tEsT", "TeSt A iS tHiS");
    }

    /**
     * Tests {@link StringUtils#equals(String, String, boolean)}.
     */
    @Test
    public void testEquals() {
        assert StringUtils.equals("a", "a", true);
        assert StringUtils.equals("A", "A", true);
        assert StringUtils.equals("a", "a", false);
        assert StringUtils.equals("A", "A", false);
        assert StringUtils.equals("A", "a", false);
        assert StringUtils.equals("a", "A", false);

        assert !StringUtils.equals("a", "A", true);
        assert !StringUtils.equals("A", "a", true);
        assert !StringUtils.equals("a", "z", false);
        assert !StringUtils.equals("a", "z", true);
    }

    /**
     * Tests {@link StringUtils#capitalize(String)}.
     */
    @Test
    public void testCapitalize() {
        assert "Bob".equals(StringUtils.capitalize("bob"));
        assert "Bob".equals(StringUtils.capitalize("BOB"));
        assert "Bob".equals(StringUtils.capitalize("bOB"));
        assert "Bob".equals(StringUtils.capitalize("bOB"));
        assert "Bob servant".equals(StringUtils.capitalize("Bob Servant"));

        assert "B".equals(StringUtils.capitalize("b"));

        assert "".equals(StringUtils.capitalize(""));
        assert "".equals(StringUtils.capitalize(null));

        assert "7".equals(StringUtils.capitalize("7"));
    }

    /**
     * Tests {@link StringUtils#flatten(String[], String)} and {@link StringUtils#flatten(String[])}.
     */
    @Test
    public void testFlatten() {
        assert "a b c".equals(StringUtils.flatten(new String[]{"a", "b", "c"}));
        assert "a b c".equals(StringUtils.flatten(new String[]{"a", "b", "c"}, " "));

        assert "a*b*c".equals(StringUtils.flatten(new String[]{"a", "b", "c"}, "*"));

        assert "a*c".equals(StringUtils.flatten(new String[]{"a", "", "c"}, "*"));

        assert "a*c".equals(StringUtils.flatten(new String[]{"a", null, "c"}, "*"));

        assert "b".equals(StringUtils.flatten(new String[]{null, "b", null}, "*"));

        assert "".equals(StringUtils.flatten(new String[]{null, null, null}, "*"));

        assert null == StringUtils.flatten(null, "*");
    }
}
