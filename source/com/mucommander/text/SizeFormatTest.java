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

package com.mucommander.text;

import junit.framework.TestCase;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;

/**
 * A test case for {@link SizeFormat}.
 *
 * @author Maxence Bernard
 */
public class SizeFormatTest extends TestCase {

    private final static long KB_1 = (long)Math.pow(2, 10);
    private final static long MB_1 = (long)Math.pow(2, 20);
    private final static long GB_1 = (long)Math.pow(2, 30);
    private final static long TB_1 = (long)Math.pow(2, 40);

    private final static long[] UNITS = { KB_1, MB_1, GB_1, TB_1 };

    private final static DecimalFormat DECIMAL_FORMAT = (DecimalFormat)NumberFormat.getInstance();


    static {
        // SizeFormat uses localized strings
        try { Translator.loadDictionaryFile(); }
        catch(Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Tests strings returned by {@link SizeFormat#format(long, int)} with the {@link SizeFormat#DIGITS_SHORT} option.
     */
    public void testDigitsShort() {
        testDigitsShort("1", 1);
        testDigitsShort("9", 9);
        testDigitsShort("10", 10);
        testDigitsShort("11", 11);
        testDigitsShort("99", 99);
        testDigitsShort("100", 100);
        testDigitsShort("101", 101);
        testDigitsShort("999", 999);
        testDigitsShort("1023", 1023);

        long unit;
        for(int i=0; i<UNITS.length; i++) {
            unit = UNITS[i];

            testDigitsShort("1", unit);
            testDigitsShort("1", unit+1);
            testDigitsShort("9", unit*9);
            testDigitsShort("9", unit*10-1);
            testDigitsShort("10", unit*10);
            testDigitsShort("10", unit*10+1);
            testDigitsShort("11", unit*11);
            testDigitsShort("99", unit*99);
            testDigitsShort("99", unit*100-1);
            testDigitsShort("100", unit*100);
            testDigitsShort("100", unit*100+1);
            testDigitsShort("101", unit*101);
            testDigitsShort("999", unit*999);
            testDigitsShort("999", unit*1000-1);
        }
    }

    private void testDigitsShort(String expected, long size) {
        assertEquals(expected, SizeFormat.format(size, SizeFormat.DIGITS_SHORT | SizeFormat.UNIT_NONE));
    }

    /**
     * Tests strings returned by {@link SizeFormat#format(long, int)} with the {@link SizeFormat#DIGITS_MEDIUM} option.
     */
    public void testDigitsMedium() {
        testDigitsMedium("1", 1);
        testDigitsMedium("9", 9);
        testDigitsMedium("10", 10);
        testDigitsMedium("11", 11);
        testDigitsMedium("99", 99);
        testDigitsMedium("100", 100);
        testDigitsMedium("101", 101);
        testDigitsMedium("999", 999);
        testDigitsMedium("1023", 1023);

        long unit;
        for(int i=0; i<UNITS.length; i++) {
            unit = UNITS[i];

            testDigitsMedium("1.0", unit);
            testDigitsMedium("1.0", unit+1);
            testDigitsMedium("9.0", unit*9);
            testDigitsMedium("9.9", unit*10-1);
            testDigitsMedium("10", unit*10);
            testDigitsMedium("10", unit*10+1);
            testDigitsMedium("11", unit*11);
            testDigitsMedium("99", unit*99);
            testDigitsMedium("99", unit*100-1);
            testDigitsMedium("100", unit*100);
            testDigitsMedium("100", unit*100+1);
            testDigitsMedium("101", unit*101);
            testDigitsMedium("999", unit*999);
            testDigitsMedium("999", unit*1000-1);
        }
    }

    private void testDigitsMedium(String expected, long size) {
        assertEquals(expected, SizeFormat.format(size, SizeFormat.DIGITS_MEDIUM | SizeFormat.UNIT_NONE));
    }

    /**
     * Tests strings returned by {@link SizeFormat#format(long, int)} with the {@link SizeFormat#DIGITS_FULL} option.
     */
    public void testDigitsFull() {
        testDigitsFull(1);
        testDigitsFull(9);
        testDigitsFull(10);
        testDigitsFull(11);
        testDigitsFull(99);
        testDigitsFull(100);
        testDigitsFull(101);
        testDigitsFull(999);
        testDigitsFull(1023);

        long unit;
        for(int i=0; i<UNITS.length; i++) {
            unit = UNITS[i];

            testDigitsFull(unit);
            testDigitsFull(unit+1);
            testDigitsFull(unit*9);
            testDigitsFull(unit*10-1);
            testDigitsFull(unit*10);
            testDigitsFull(unit*10+1);
            testDigitsFull(unit*11);
            testDigitsFull(unit*99);
            testDigitsFull(unit*100-1);
            testDigitsFull(unit*100);
            testDigitsFull(unit*100+1);
            testDigitsFull(unit*101);
            testDigitsFull(unit*999);
            testDigitsFull(unit*1000-1);
        }
    }

    private void testDigitsFull(long size) {
        assertEquals(Double.valueOf(size),
                     Double.valueOf(DECIMAL_FORMAT.parse(SizeFormat.format(size, SizeFormat.DIGITS_FULL | SizeFormat.UNIT_NONE), new ParsePosition(0)).longValue()));
    }

    /**
     * Tests strings returned by {@link SizeFormat#format(long, int)} with the {@link SizeFormat#ROUND_TO_KB} option
     * combined successively with {@link SizeFormat#DIGITS_SHORT}, {@link SizeFormat#DIGITS_MEDIUM} and
     * {@link SizeFormat#DIGITS_FULL}.
     */
    public void testRoundToKb() {
        testRoundToKb(SizeFormat.DIGITS_SHORT);
        testRoundToKb(SizeFormat.DIGITS_MEDIUM);
        testRoundToKb(SizeFormat.DIGITS_FULL);
    }

    private void testRoundToKb(int digitFormat) {
        assertEquals("0", SizeFormat.format(0, digitFormat | SizeFormat.ROUND_TO_KB | SizeFormat.UNIT_NONE));
        testRoundToKb(1, digitFormat);
        testRoundToKb(9, digitFormat);
        testRoundToKb(10, digitFormat);
        testRoundToKb(11, digitFormat);
        testRoundToKb(99, digitFormat);
        testRoundToKb(100, digitFormat);
        testRoundToKb(101, digitFormat);
        testRoundToKb(999, digitFormat);
        testRoundToKb(1023, digitFormat);
    }

    private void testRoundToKb(long size, int digitFormat) {
        assertEquals("1", SizeFormat.format(size, digitFormat | SizeFormat.ROUND_TO_KB | SizeFormat.UNIT_NONE));
    }
}
