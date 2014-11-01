/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * SizeFormat formats byte sizes into localized string representations.
 *
 * @author Maxence Bernard.
 */
public class SizeFormat {

    /** Bitmask for short digits, e.g. "15" */
    public final static int DIGITS_SHORT = 1;
    /** Bitmask for medium digits, e.g. "15,2" */
    public final static int DIGITS_MEDIUM = 2;
    /** Bitmask for full digits, e.g. "15,204,405" */
    public final static int DIGITS_FULL = 4;

    /** Bitmask for no unit string */
    public final static int UNIT_NONE = 0;
    /** Bitmask for short unit string, e.g. "b" */
    public final static int UNIT_SHORT = 8;
    /** Bitmask for short unit string, e.g. "bytes" */
    public final static int UNIT_LONG = 16;

    /** Byte unit */
    public final static int BYTE_UNIT = 0;
    /** Kilobyte unit */
    public final static int KILOBYTE_UNIT = 1;
    /** Megabyte unit */
    public final static int MEGABYTE_UNIT = 2;
    /** Gigabyte unit */
    public final static int GIGABYTE_UNIT = 3;
    /** Terabyte unit */
    public final static int TERABYTE_UNIT = 4;

    /** Bitmask to add '/s' (per second) to the returned String */
    public final static int UNIT_SPEED = 32;
    
    /** Bitmask to include a space character to separate the digits and unit parts */
    public final static int INCLUDE_SPACE = 64;

    /** Bitmask to round any size < 1KB to 1KB (except 0 which will be 0 KB) */
    public final static int ROUND_TO_KB = 128;

    /** One kilobyte: 2^10 */
    private final static int KB_1 = 1024;
    /** Ten kilobytes: (2^10)*10 */
    private final static int KB_10 = 10240;
    /** One megabyte: 2^20 */
    private final static int MB_1 = 1048576;
    /** Ten megabytes: (2^20)*10 */
    private final static int MB_10 = 10485760;
    /** One gigabyte: 2^30 */
    private final static int GB_1 = 1073741824;
    /** Ten gigabytes: (2^10)*10 */
    private final static long GB_10 = 10737418240l;
    /** One terabyte: 2^40 */
    private final static long TB_1 = 1099511627776l;
    /** Ten terabytes: (2^40)*10 */
    private final static long TB_10 = 10995116277760l;

    /** DecimalFormat instance to localize thousands separators */
    private final static DecimalFormat DECIMAL_FORMAT = (DecimalFormat)NumberFormat.getInstance();

    /** Localized decimal separator */
    private final static String DECIMAL_SEPARATOR = ""+DECIMAL_FORMAT.getDecimalFormatSymbols().getDecimalSeparator();


    /////////////////////
    // Dictionary keys //
    /////////////////////

    private final static String BYTE = Translator.get("unit.byte");
    private final static String BYTES = Translator.get("unit.bytes");
    private final static String B = Translator.get("unit.bytes_short");
    private final static String KB = Translator.get("unit.kb");
    private final static String MB = Translator.get("unit.mb");
    private final static String GB = Translator.get("unit.gb");
    private final static String TB = Translator.get("unit.tb");

    private final static String SPEED_KEY = "unit.speed";


    /**
     * Returns a String representation of the given byte size.
     *
     * @param size the size to format
     * @param format format bitmask, see constant fields for allowed values
     * @return a String representation of the given byte size
     */
    public static String format(long size, int format) {
        if(size<0)
            return "?";

        String digitsString;
        String unitString;
		
        // Whether the unit string should be long or not
        boolean unitLong = (format&UNIT_LONG)!=0;
        // Whether the unit string should be short or not
        boolean unitShort = (format&UNIT_SHORT)!=0;
        // Whether the unit string should be short or not
        boolean noUnit = !(unitLong||unitShort);
        // Whether the digits string should be short or not
        boolean digitsShort = (format&DIGITS_SHORT)!=0;
        // Whether any size < 1024 bytes should be rounded to a kilobyte
        boolean roundToKb = (format&ROUND_TO_KB)!=0;
		
        // size < 1KB
        if(size<KB_1) {
            if(roundToKb) {
                // Note: ROUND_TO_KB must have precedence over DIGITS_FULL
                digitsString = size==0?"0":"1";
                unitString = noUnit?"":KB;
            }
            else {
                digitsString = ""+size;
                unitString = unitLong?(size<=1?BYTE:BYTES):unitShort?B:"";
            }
        }
        else if((format&DIGITS_FULL)!=0) {
            // DecimalFormat localizes thousands separators

            // Calls to DecimalFormat must be synchronized.
            // Quote from DecimalFormat's Javadoc: "Decimal formats are generally not synchronized. It is recommended 
            // to create separate format instances for each thread. If multiple threads access a format concurrently,
            // it must be synchronized externally."
            synchronized(DECIMAL_FORMAT) {
                digitsString = DECIMAL_FORMAT.format(size);
            }
            unitString = unitLong?BYTES:unitShort?B:"";
        }
        else {
            // size < 10KB	-> "9.6 KB"
            if(size<KB_10 && !digitsShort) {
                int nKB = (int)size/KB_1;
                digitsString = nKB+DECIMAL_SEPARATOR+(int)((size-nKB*KB_1)/(float)KB_1*10);
                unitString = noUnit?"":KB;
            }
            // size < 1MB -> "436 KB"
            else if(size<MB_1) {
                digitsString = ""+size/KB_1;
                unitString = noUnit?"":KB;
            }
            // size < 10MB -> "4.3 MB"
            else if(size<MB_10 && !digitsShort) {
                int nMB = (int)size/MB_1;
                digitsString = nMB+DECIMAL_SEPARATOR+(int)((size-nMB*MB_1)/(float)MB_1*10);
                unitString = noUnit?"":MB;
            }
            // size < 1GB -> "548 MB"
            else if(size<GB_1) {
                digitsString = ""+size/MB_1;
                unitString = noUnit?"":MB;
            }	
            // size < 10GB -> "4.8 GB"
            else if(size<GB_10 && !digitsShort) {
                long nGB = size/GB_1;
                digitsString = nGB+DECIMAL_SEPARATOR+(int)((size-nGB*GB_1)/(double)GB_1*10);
                unitString = noUnit?"":GB;
            }
            // size < 1TB -> "216 GB"
            else if(size<TB_1) {
                digitsString = ""+size/GB_1;
                unitString = noUnit?"":GB;
            }
            // size < 10TB -> "4.8 TB"
            else if(size<TB_10 && !digitsShort) {
                long nTB = size/TB_1;
                digitsString = nTB+DECIMAL_SEPARATOR+(int)((size-nTB*TB_1)/(double)TB_1*10);
                unitString = noUnit?"":TB;
            }
            else {
                // Will I live long enough to see files that large ??
                digitsString = ""+size/TB_1;
                unitString = noUnit?"":TB;
            }
        }

        // Add localized '/s' to unit string if unit is speed
        if((format&UNIT_SPEED)!=0)
            unitString = Translator.get(SPEED_KEY, unitString);

        return digitsString+((format&INCLUDE_SPACE)!=0?" ":"")+unitString;
    }
    


    public static String getUnitString(int unit, boolean speedUnit) {
        String unitString;

        switch(unit) {
            case BYTE_UNIT:
                unitString = B;
                break;
            case KILOBYTE_UNIT:
                unitString = KB;
                break;
            case MEGABYTE_UNIT:
                unitString = MB;
                break;
            case GIGABYTE_UNIT:
                unitString = GB;
                break;
            case TERABYTE_UNIT:
                unitString = TB;
                break;
            default:
                return "";
        }

        return speedUnit?Translator.get(SPEED_KEY, unitString):unitString;
    }


    /**
     * Returns the size in bytes of the given byte unit, e.g. <code>1024</code> for {@link #KILOBYTE_UNIT}.
     *
     * @param unit a unit constant, see constant fields for allowed values
     * @return the size in bytes of the given byte unit
     */
    public static long getUnitBytes(int unit) {
        long bytes;

        switch(unit) {
            case BYTE_UNIT:
                bytes = 1;
                break;
            case KILOBYTE_UNIT:
                bytes = KB_1;
                break;
            case MEGABYTE_UNIT:
                bytes = MB_1;
                break;
            case GIGABYTE_UNIT:
                bytes = GB_1;
                break;
            case TERABYTE_UNIT:
                bytes = TB_1;
                break;
            default:
                return 0;
        }

        return bytes;
    }
}
