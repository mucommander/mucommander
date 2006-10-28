
package com.mucommander.text;


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

    /** Bit mask to include a space character between digits and unit parts */
    public final static int INCLUDE_SPACE = 16;

    /** Bit mask to round any size < 1KB to 1KB (except 0 which will be 0 KB) */
    public final static int ROUND_TO_KB = 32;

    private final static int KB_1 = 1024;
    private final static int KB_10 = 10240;
    private final static int MB_1 = 1048576;
    private final static int MB_10 = 10485760;
    private final static int GB_1 = 1073741824;
    private final static long GB_10 = 10737418240l;
    private final static long TB_1 = 1099511627776l;
    private final static long TB_10 = 10995116277760l;
    

    //////////////////////////
    // Locatlized text keys //
    //////////////////////////

    private final static String BYTE = Translator.get("unit.byte");
    private final static String BYTES = Translator.get("unit.bytes");
    private final static String B = Translator.get("unit.bytes_short");
    private final static String KB = Translator.get("unit.kb");
    private final static String MB = Translator.get("unit.mb");
    private final static String GB = Translator.get("unit.gb");
    private final static String TB = Translator.get("unit.tb");
	
	
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
            String s = ""+size;
            int len = s.length();
            digitsString = "";
            for(int i=len; i>0; i-=3)
                digitsString = s.substring(Math.max(i-3, 0), i)+(i==len?"":","+digitsString);

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
            // size < 10KB	-> "9,6 KB"
            else if(size<KB_10 && !digitsShort) {
                int nKB = (int)size/KB_1;
                digitsString = nKB+","+((""+(size-nKB*KB_1)/(float)KB_1).charAt(2));
                unitString = noUnit?"":KB;
            }
            // size < 1MB -> "436 KB"
            else if(size<MB_1) {
                digitsString = ""+size/KB_1;
                unitString = noUnit?"":KB;
            }
            // size < 10MB -> "4,3 MB"
            else if(size<MB_10 && !digitsShort) {
                int nMB = (int)size/MB_1;
                digitsString = nMB+","+((""+(size-nMB*MB_1)/(float)MB_1).charAt(2));
                unitString = noUnit?"":MB;
            }
            // size < 1GB -> "548 MB"
            else if(size<GB_1) {
                digitsString = ""+size/MB_1;
                unitString = noUnit?"":MB;
            }	
            // size < 10GB -> "4,8 GB"
            else if(size<GB_10 && !digitsShort) {
                long nGB = size/GB_1;
                digitsString = nGB+","+((""+(size-nGB*GB_1)/(float)GB_1).charAt(2));
                unitString = noUnit?"":GB;
            }
            // size < 1TB -> "216 GB"
            else if(size<TB_1) {
                digitsString = ""+size/GB_1;
                unitString = noUnit?"":GB;
            }
            // size < 10TB -> "4,8 TB"
            else if(size<TB_10 && !digitsShort) {
                long nTB = size/TB_1;
                digitsString = nTB+","+((""+(size-nTB*TB_1)/(float)TB_1).charAt(2));
                unitString = noUnit?"":TB;
            }
            else {
                // Will I live long enough to see files that large ??
                digitsString = ""+size/TB_1;
                unitString = noUnit?"":TB;
            }
        }

        return digitsString+((format&INCLUDE_SPACE)!=0?" ":"")+unitString;
    }
    
    
    
}
