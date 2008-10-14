/*
 * 
 * Original author: alpha_lam
 * Creation date: ?
 *
 * Source: $HeadURL$
 * Last changed: $LastChangedDate$
 * 
 * 
 * the unrar licence applies to all junrar source and binary distributions 
 * you are not allowed to use this source to re-create the RAR compression algorithm
 *
 * Here some html entities which can be used for escaping javadoc tags:
 * "&":  "&#038;" or "&amp;"
 * "<":  "&#060;" or "&lt;"
 * ">":  "&#062;" or "&gt;"
 * "@":  "&#064;" 
 */
package com.mucommander.file.impl.rar.provider.de.innosystec.unrar.rarfile;

import com.mucommander.file.impl.rar.provider.de.innosystec.unrar.unsigned.UnsignedByte;

public class FileNameDecoder {
	
	/*private static int getChar(byte [] name,int pos){ 
		return name[pos]&0xff; 
	}*/

	public static String decode(String name, byte[] encName){
		UnsignedByte flags = null;
	    int flagsBits = 0;
	    int flagsPos = 0;
	    int destSize = 0;
		int encPos = 0, decPos = 0;
		UnsignedByte highByte = new UnsignedByte(encName[encPos++]);
		//System.out.println("highByte<<8 = " + (highByte << 8)  + " encName[encPos++] = " + encName[encPos-1]);
		String nameW = "";
	
		while(encPos < encName.length) {
		    if (flagsBits == 0) {
		    	flags =  new UnsignedByte(encName[encPos++]); 
//		    	System.out.println("flags = " + flags);
		    	flagsBits = 8;
		    }
	
		    switch (flags.getValue() >> 6) {
		    case 0:
//		    	System.out.println("case 0");
			nameW += byteToChar(encName[encPos++]); //(char) (encName[encPos++] & 0xff);
			decPos++;
			break;
		    case 1:
		    {
//		    	System.out.println("case 1");
		    	UnsignedByte ubyte = new UnsignedByte(encName[encPos++]);
		    	ubyte.add((short) (highByte.getValue()<<8));
//		    	System.out.println("ubyte = " + ubyte.getValue());		    	
		    	nameW += ubyte.toChar();
		    	decPos++;
		    }
				break;
		    case 2:
//		    	System.out.println("case 2");
		    	nameW += (char) ((encName[encPos] + (encName[encPos+1] << 8)) & 0xff);
		    	decPos++;
		    	encPos += 2;
		    	break;
		    case 3:		    
			{
//				System.out.println("case 3");
			    int length = encName[encPos++];
			    if ((length & 0x80) != 0) {
			    	byte correction = encName[encPos++];
			    	for (length = (length&0x7f)+2; length > 0; length--, decPos++)
			    		nameW += ((name.charAt(decPos) + correction));			    	
			    }
			    else
			    	for (length+=2; length>0; length--, decPos++)
			    		nameW += name.charAt(decPos);			    				    
			}
			break;
		    }
		    flags = new UnsignedByte((byte) (flags.getValue() << 2));
		    flagsBits-=2;
		}
		return nameW; 
	}
	
	public static String UtfToWide(String src) {
		String result = "";
		int srcLength = src.length() - 1;
		int index = 0;
		
		while(index < src.length()) {
			byte c = charToByte(src.charAt(index++));
			byte d;
			if (c < 0x80)
				d = c;
			else {
				if ((c>>5)==6) {
					if ((charToByte(src.charAt(index)) & 0xc0) != 0x80)
						break;
					d = new Integer(((c&0x1f)<<6) | (charToByte(src.charAt(index)) &0x3f)).byteValue();
					++index;
				}
				else {
					if ((c>>4) == 14) {
						if ((charToByte(src.charAt(index)) & 0xc0) != 0x80 || (charToByte(src.charAt(index+1))&0xc0) != 0x80)
							break;
						d = new Integer(((c&0xf)<<12) | ((charToByte(src.charAt(index)) & 0x3f)<<6) | (charToByte(src.charAt(index+1))&0x3f)).byteValue();
						index += 2;
					}
					if ((c>>3) == 30) {
						if (((charToByte(src.charAt(index)) & 0xc0) != 0x80) || 
								(((charToByte(src.charAt(index+1)) & 0xc0) != 0x80) || ((charToByte(src.charAt(index+2)) & 0xc0) != 0x80)))
							break;
						d = new Integer(((c&7)<<18) | ((charToByte(src.charAt(index)) & 0x3f)<<12) | 
								((charToByte(src.charAt(index+1)) & 0x3f)<<6) | ((charToByte(src.charAt(index+2)) & 0x3f))).byteValue();
						index += 3;								
					}
					else
						break;					
				}
			}
			
			if (--srcLength < 0)
				break;
			if (d > 0xffff) {
				if (--srcLength < 0 || d > 0x10ffff)
					break;				
				result += String.valueOf(byteToChar((byte)(((d - 0x10000) >> 10) + 0xd800)));
				result += String.valueOf(byteToChar((byte)((d & 0x3ff) + 0xdc00)));
			}
			else
				result += String.valueOf(byteToChar((byte)d));
		}
		return result;
	}
	
	private static char byteToChar(byte input) {
		return (char) (input & 0xff);
	}
	
	private static byte charToByte(char input) {
		return (byte) (input & 0xff);
	}
}
