/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

import junit.framework.TestCase;

/**
 * Tests the {@link StringUtil} class.
 * @author Nicolas Rinaudo
 */
public class StringUtilsTest extends TestCase {
    /**
     * Runs through a variety of {@link StringUtils#endsWithIgnoreCase(String,String)} test cases.
     */
    public void testEndsWithString() {
        assertTrue(StringUtils.endsWithIgnoreCase("this is a test", "a test"));
        assertTrue(StringUtils.endsWithIgnoreCase("this is a test", "a TeSt"));
        assertTrue(StringUtils.endsWithIgnoreCase("this is a test", "A TEST"));

        assertTrue(StringUtils.endsWithIgnoreCase("THIS IS A TEST", "a test"));
        assertTrue(StringUtils.endsWithIgnoreCase("THIS IS A TEST", "a TeSt"));
        assertTrue(StringUtils.endsWithIgnoreCase("THIS IS A TEST", "A TEST"));

        assertTrue(StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "a test"));
        assertTrue(StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "a TeSt"));
        assertTrue(StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "A TEST"));

        assertTrue(StringUtils.endsWithIgnoreCase("this is a test", "this is a test"));
        assertTrue(StringUtils.endsWithIgnoreCase("this is a test", "ThIs Is A TeSt"));
        assertTrue(StringUtils.endsWithIgnoreCase("this is a test", "THIS IS A TEST"));

        assertTrue(StringUtils.endsWithIgnoreCase("THIS IS A TEST", "this is a test"));
        assertTrue(StringUtils.endsWithIgnoreCase("THIS IS A TEST", "ThIs Is A TeSt"));
        assertTrue(StringUtils.endsWithIgnoreCase("THIS IS A TEST", "THIS IS A TEST"));

        assertTrue(StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "this is a test"));
        assertTrue(StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "ThIs Is A TeSt"));
        assertTrue(StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "THIS IS A TEST"));

        assertTrue(StringUtils.endsWithIgnoreCase("this is a test", ""));

        assertFalse(StringUtils.endsWithIgnoreCase("this is a test", "test a is this"));
        assertFalse(StringUtils.endsWithIgnoreCase("this is a test", "tEsT a Is ThIs"));
        assertFalse(StringUtils.endsWithIgnoreCase("this is a test", "TEST A IS THIS"));

        assertFalse(StringUtils.endsWithIgnoreCase("THIS IS A TEST", "test a is this"));
        assertFalse(StringUtils.endsWithIgnoreCase("THIS IS A TEST", "tEsT a Is ThIs"));
        assertFalse(StringUtils.endsWithIgnoreCase("THIS IS A TEST", "TEST A IS THIS"));

        assertFalse(StringUtils.endsWithIgnoreCase("ThIs Is A tEst", "test a is this"));
        assertFalse(StringUtils.endsWithIgnoreCase("ThIs Is A tEst", "tEsT a Is ThIs"));
        assertFalse(StringUtils.endsWithIgnoreCase("ThIs Is A tEst", "TEST A IS THIS"));
    }

    /**
     * Runs through a variety of {@link StringUtils#endsWithIgnoreCase(String,char[])} test cases.
     */
    public void testEndsWithCharArray() {
        assertTrue(StringUtils.endsWithIgnoreCase("this is a test", "a test".toCharArray()));
        assertTrue(StringUtils.endsWithIgnoreCase("this is a test", "a TeSt".toCharArray()));
        assertTrue(StringUtils.endsWithIgnoreCase("this is a test", "A TEST".toCharArray()));

        assertTrue(StringUtils.endsWithIgnoreCase("THIS IS A TEST", "a test".toCharArray()));
        assertTrue(StringUtils.endsWithIgnoreCase("THIS IS A TEST", "a TeSt".toCharArray()));
        assertTrue(StringUtils.endsWithIgnoreCase("THIS IS A TEST", "A TEST".toCharArray()));

        assertTrue(StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "a test".toCharArray()));
        assertTrue(StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "a TeSt".toCharArray()));
        assertTrue(StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "A TEST".toCharArray()));

        assertTrue(StringUtils.endsWithIgnoreCase("this is a test", "this is a test".toCharArray()));
        assertTrue(StringUtils.endsWithIgnoreCase("this is a test", "ThIs Is A TeSt".toCharArray()));
        assertTrue(StringUtils.endsWithIgnoreCase("this is a test", "THIS IS A TEST".toCharArray()));

        assertTrue(StringUtils.endsWithIgnoreCase("THIS IS A TEST", "this is a test".toCharArray()));
        assertTrue(StringUtils.endsWithIgnoreCase("THIS IS A TEST", "ThIs Is A TeSt".toCharArray()));
        assertTrue(StringUtils.endsWithIgnoreCase("THIS IS A TEST", "THIS IS A TEST".toCharArray()));

        assertTrue(StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "this is a test".toCharArray()));
        assertTrue(StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "ThIs Is A TeSt".toCharArray()));
        assertTrue(StringUtils.endsWithIgnoreCase("ThIs Is A TeSt", "THIS IS A TEST".toCharArray()));

        assertTrue(StringUtils.endsWithIgnoreCase("this is a test", "".toCharArray()));

        assertFalse(StringUtils.endsWithIgnoreCase("this is a test", "test a is this".toCharArray()));
        assertFalse(StringUtils.endsWithIgnoreCase("this is a test", "tEsT a Is ThIs".toCharArray()));
        assertFalse(StringUtils.endsWithIgnoreCase("this is a test", "TEST A IS THIS".toCharArray()));

        assertFalse(StringUtils.endsWithIgnoreCase("THIS IS A TEST", "test a is this".toCharArray()));
        assertFalse(StringUtils.endsWithIgnoreCase("THIS IS A TEST", "tEsT a Is ThIs".toCharArray()));
        assertFalse(StringUtils.endsWithIgnoreCase("THIS IS A TEST", "TEST A IS THIS".toCharArray()));

        assertFalse(StringUtils.endsWithIgnoreCase("ThIs Is A tEst", "test a is this".toCharArray()));
        assertFalse(StringUtils.endsWithIgnoreCase("ThIs Is A tEst", "tEsT a Is ThIs".toCharArray()));
        assertFalse(StringUtils.endsWithIgnoreCase("ThIs Is A tEst", "TEST A IS THIS".toCharArray()));
    }

    /**
     * Runs through a variety of {@link StringUtils#startsWithIgnoreCase(String,String)} test cases.
     */ 
    public void testStartsWith() {
        assertTrue(StringUtils.startsWithIgnoreCase("this is a test", "this is"));
        assertTrue(StringUtils.startsWithIgnoreCase("this is a test", "ThIs Is"));
        assertTrue(StringUtils.startsWithIgnoreCase("this is a test", "THIS IS"));

        assertTrue(StringUtils.startsWithIgnoreCase("THIS IS A TEST", "this is"));
        assertTrue(StringUtils.startsWithIgnoreCase("THIS IS A TEST", "ThIs Is"));
        assertTrue(StringUtils.startsWithIgnoreCase("THIS IS A TEST", "THIS IS"));

        assertTrue(StringUtils.startsWithIgnoreCase("ThIs Is A tEsT", "this is"));
        assertTrue(StringUtils.startsWithIgnoreCase("ThIs Is A tEsT", "ThIs Is"));
        assertTrue(StringUtils.startsWithIgnoreCase("ThIs Is A tEsT", "THIS IS"));

        assertTrue(StringUtils.startsWithIgnoreCase("this is a test", "this is a test"));
        assertTrue(StringUtils.startsWithIgnoreCase("this is a test", "THIS IS A TEST"));
        assertTrue(StringUtils.startsWithIgnoreCase("this is a test", "ThIs Is A tEsT"));

        assertTrue(StringUtils.startsWithIgnoreCase("THIS IS A TEST", "this is a test"));
        assertTrue(StringUtils.startsWithIgnoreCase("THIS IS A TEST", "THIS IS A TEST"));
        assertTrue(StringUtils.startsWithIgnoreCase("THIS IS A TEST", "ThIs Is A tEsT"));

        assertTrue(StringUtils.startsWithIgnoreCase("ThIs Is A tEsT", "this is a test"));
        assertTrue(StringUtils.startsWithIgnoreCase("ThIs Is A tEsT", "THIS IS A TEST"));
        assertTrue(StringUtils.startsWithIgnoreCase("ThIs Is A tEsT", "ThIs Is A tEsT"));

        assertTrue(StringUtils.startsWithIgnoreCase("this is a test", ""));
        assertTrue(StringUtils.startsWithIgnoreCase("THIS IS A TEST", ""));
        assertTrue(StringUtils.startsWithIgnoreCase("ThIs Is A tEsT", ""));

        assertFalse(StringUtils.startsWithIgnoreCase("this is a test", "test a is this"));
        assertFalse(StringUtils.startsWithIgnoreCase("this is a test", "TEST A IS THIS"));
        assertFalse(StringUtils.startsWithIgnoreCase("this is a test", "TeSt A iS tHiS"));

        assertFalse(StringUtils.startsWithIgnoreCase("THIS IS A TEST", "test a is this"));
        assertFalse(StringUtils.startsWithIgnoreCase("THIS IS A TEST", "TEST A IS THIS"));
        assertFalse(StringUtils.startsWithIgnoreCase("THIS IS A TEST", "TeSt A iS tHiS"));

        assertFalse(StringUtils.startsWithIgnoreCase("ThIs Is A tEsT", "test a is this"));
        assertFalse(StringUtils.startsWithIgnoreCase("ThIs Is A tEsT", "TEST A IS THIS"));
        assertFalse(StringUtils.startsWithIgnoreCase("ThIs Is A tEsT", "TeSt A iS tHiS"));
    }
}
