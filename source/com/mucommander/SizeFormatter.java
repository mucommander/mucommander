
package com.mucommander.text;


public class SizeFormatter {

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


    public static String format(long size, int format) {
        String digitsString;
		String unitString;

		if(size==0) {
			digitsString = "0";
			unitString = (format&UNIT_LONG)!=0?"byte":(format&UNIT_SHORT)!=0?"b":"";
		}
		else if((format&DIGITS_FULL)!=0) {
			String s = ""+size;
			int len = s.length();
			digitsString = "";
			for(int i=len; i>0; i-=3)
				digitsString = s.substring(Math.max(i-3, 0), i)+(i==len?"":","+digitsString);

			unitString = (format&UNIT_LONG)!=0?"bytes":(format&UNIT_SHORT)!=0?"b":"";
		}
		else {
			// size < 1KB
			if(size<1000) {
				digitsString = ""+size;
				unitString = (format&UNIT_LONG)!=0?(size==1?"byte":"bytes"):(format&UNIT_SHORT)!=0?"b":"";
			}
			// size < 10KB	-> "9,6 KB"
			else if(size<10000 && (format&DIGITS_SHORT)==0) {
				int nKB = (int)size/1000;
				digitsString = nKB+","+((""+(size-nKB*1000)).charAt(0));
				unitString = (format&UNIT_LONG)!=0?"KB":(format&UNIT_SHORT)!=0?"KB":"";;
			}
			// size < 1MB -> "436 KB"
			else if(size<1000000) {
				digitsString = ""+size/1000;
				unitString = (format&UNIT_LONG)!=0?"KB":(format&UNIT_SHORT)!=0?"KB":"";
			}
			// size < 10MB -> "4,3 MB"
			else if(size<10000000 && (format&DIGITS_SHORT)==0) {
				int nMB = (int)size/1000000;
				digitsString = nMB+","+((""+(size-nMB*1000000)).charAt(0));
				unitString = (format&UNIT_LONG)!=0?"MB":(format&UNIT_SHORT)!=0?"MB":"";
			}
			// size < 1GB -> "548 MB"
			else if(size<1000000000) {
				digitsString = ""+size/1000000;
				unitString = (format&UNIT_LONG)!=0?"MB":(format&UNIT_SHORT)!=0?"MB":"";
			}
	
			// size < 10GB -> "4,8 GB"
			else if(size<10000000000l && (format&DIGITS_SHORT)==0) {
				long nGB = size/1000000000;
				digitsString = nGB+","+((""+(size-nGB*1000000000)).charAt(0));
				unitString = (format&UNIT_LONG)!=0?"GB":(format&UNIT_SHORT)!=0?"GB":"";
			}
			// size > 1TB -> "216 GB"
			else {
				digitsString = ""+size/1000000000;
				unitString = (format&UNIT_LONG)!=0?"GB":(format&UNIT_SHORT)!=0?"GB":"";
			}
		}

        return digitsString+((format&INCLUDE_SPACE)!=0?" ":"")+unitString;
    }
    
    
    
}