
package com.mucommander.text;

import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * SizeFormat formats byte sizes into localized string reprensentations.
 *
 * @author Maxence Bernard.
 */
public class SizeFormat {

    /** Bit mask for short digits string, e.g. "15" */
    public final static int DIGITS_SHORT = 0;	
    /** Bit mask for medium digits string, e.g. "15,2" */
    public final static int DIGITS_MEDIUM = 1;	
    /** Bit mask for full digits string, e.g. "15,204,405" */
    public final static int DIGITS_FULL = 2;

    /** Bit mask for no unit string */
    public final static int UNIT_NONE = 0;
    /** Bit mask for short unit string, e.g. "b" */
    public final static int UNIT_SHORT = 4;
    /** Bit mask for short unit string, e.g. "bytes" */
    public final static int UNIT_LONG = 8;

    /** Bit mask to add '/s' (per second) to the returned String */
    public final static int UNIT_SPEED = 16;
    
    /** Bit mask to include a space character between digits and unit parts */
    public final static int INCLUDE_SPACE = 32;

    /** Bit mask to round any size < 1KB to 1KB (except 0 which will be 0 KB) */
    public final static int ROUND_TO_KB = 64;

    private final static int KB_1 = 1024;
    private final static int KB_10 = 10240;
    private final static int MB_1 = 1048576;
    private final static int MB_10 = 10485760;
    private final static int GB_1 = 1073741824;
    private final static long GB_10 = 10737418240l;
    private final static long TB_1 = 1099511627776l;
    private final static long TB_10 = 10995116277760l;

    public final static int BYTE_UNIT = 0;
    public final static int KILO_BYTE_UNIT = 1;
    public final static int MEGA_BYTE_UNIT = 2;
    public final static int GIGA_BYTE_UNIT = 3;
    public final static int TERA_BYTE_UNIT = 4;

    
    /** DecimalFormat instance to localize thousands separators */
    private final static DecimalFormat DECIMAL_FORMAT = (DecimalFormat)NumberFormat.getInstance();

    /** Localized decimal separator */
    private final static String DECIMAL_SEPARATOR = ""+DECIMAL_FORMAT.getDecimalFormatSymbols().getDecimalSeparator();


    //////////////////////////
    // Localized text keys //
    //////////////////////////

    private final static String BYTE = Translator.get("unit.byte");
    private final static String BYTES = Translator.get("unit.bytes");
    private final static String B = Translator.get("unit.bytes_short");
    private final static String KB = Translator.get("unit.kb");
    private final static String MB = Translator.get("unit.mb");
    private final static String GB = Translator.get("unit.gb");
    private final static String TB = Translator.get("unit.tb");

    private final static String SPEED_KEY = "unit.speed";

	
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
		
        if((format&DIGITS_FULL)!=0) {
            // DecimalFormat localizes thousands separators
            digitsString = DECIMAL_FORMAT.format(size);
            unitString = unitLong?BYTES:unitShort?B:"";
        }
        else {
            // size < 1KB
            if(size<KB_1) {
                if(roundToKb) {
                    digitsString = size==0?"0":"1";
                    unitString = noUnit?"":KB;
                }
                else {
                    digitsString = ""+size;
                    unitString = unitLong?(size<=1?BYTE:BYTES):unitShort?B:"";
                }
            }
            // size < 10KB	-> "9.6 KB"
            else if(size<KB_10 && !digitsShort) {
                int nKB = (int)size/KB_1;
                digitsString = nKB+DECIMAL_SEPARATOR+((""+(size-nKB*KB_1)/(float)KB_1).charAt(2));
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
                digitsString = nMB+DECIMAL_SEPARATOR+((""+(size-nMB*MB_1)/(float)MB_1).charAt(2));
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
                digitsString = nGB+DECIMAL_SEPARATOR+((""+(size-nGB*GB_1)/(float)GB_1).charAt(2));
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
                digitsString = nTB+DECIMAL_SEPARATOR+((""+(size-nTB*TB_1)/(float)TB_1).charAt(2));
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
            case KILO_BYTE_UNIT:
                unitString = KB;
                break;
            case MEGA_BYTE_UNIT:
                unitString = MB;
                break;
            case GIGA_BYTE_UNIT:
                unitString = GB;
                break;
            case TERA_BYTE_UNIT:
                unitString = TB;
                break;
            default:
                return "";
        }

        return speedUnit?Translator.get(SPEED_KEY, unitString):unitString;
    }
}
