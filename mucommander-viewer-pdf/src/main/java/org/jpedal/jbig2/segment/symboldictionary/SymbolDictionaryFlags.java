/**
* ===========================================
* Java Pdf Extraction Decoding Access Library
* ===========================================
 *
* Project Info:  http://www.jpedal.org
* (C) Copyright 1997-2008, IDRsolutions and Contributors.
* Main Developer: Simon Barnett
*
* 	This file is part of JPedal
*
* Copyright (c) 2008, IDRsolutions
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*     * Redistributions of source code must retain the above copyright
*       notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above copyright
*       notice, this list of conditions and the following disclaimer in the
*       documentation and/or other materials provided with the distribution.
*     * Neither the name of the IDRsolutions nor the
*       names of its contributors may be used to endorse or promote products
*       derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY IDRsolutions ``AS IS'' AND ANY
* EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL IDRsolutions BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* Other JBIG2 image decoding implementations include
* jbig2dec (http://jbig2dec.sourceforge.net/)
* xpdf (http://www.foolabs.com/xpdf/)
* 
* The final draft JBIG2 specification can be found at http://www.jpeg.org/public/fcd14492.pdf
* 
* All three of the above resources were used in the writing of this software, with methodologies,
* processes and inspiration taken from all three.
*
* ---------------
* SymbolDictionaryFlags.java
* ---------------
 */
package org.jpedal.jbig2.segment.symboldictionary;

import org.jpedal.jbig2.decoders.JBIG2StreamDecoder;
import org.jpedal.jbig2.segment.Flags;

public class SymbolDictionaryFlags extends Flags {

    public static String SD_HUFF = "SD_HUFF";
    public static String SD_REF_AGG = "SD_REF_AGG";
    public static String SD_HUFF_DH = "SD_HUFF_DH";
    public static String SD_HUFF_DW = "SD_HUFF_DW";
    public static String SD_HUFF_BM_SIZE = "SD_HUFF_BM_SIZE";
    public static String SD_HUFF_AGG_INST = "SD_HUFF_AGG_INST";
    public static String BITMAP_CC_USED = "BITMAP_CC_USED";
    public static String BITMAP_CC_RETAINED = "BITMAP_CC_RETAINED";
    public static String SD_TEMPLATE = "SD_TEMPLATE";
    public static String SD_R_TEMPLATE = "SD_R_TEMPLATE";

    public void setFlags(int flagsAsInt) {
        this.flagsAsInt = flagsAsInt;

        /** extract SD_HUFF */
        flags.put(SD_HUFF, new Integer(flagsAsInt & 1));

        /** extract SD_REF_AGG */
        flags.put(SD_REF_AGG, new Integer((flagsAsInt >> 1) & 1));

        /** extract SD_HUFF_DH */
        flags.put(SD_HUFF_DH, new Integer((flagsAsInt >> 2) & 3));

        /** extract SD_HUFF_DW */
        flags.put(SD_HUFF_DW, new Integer((flagsAsInt >> 4) & 3));

        /** extract SD_HUFF_BM_SIZE */
        flags.put(SD_HUFF_BM_SIZE, new Integer((flagsAsInt >> 6) & 1));

        /** extract SD_HUFF_AGG_INST */
        flags.put(SD_HUFF_AGG_INST, new Integer((flagsAsInt >> 7) & 1));

        /** extract BITMAP_CC_USED */
        flags.put(BITMAP_CC_USED, new Integer((flagsAsInt >> 8) & 1));

        /** extract BITMAP_CC_RETAINED */
        flags.put(BITMAP_CC_RETAINED, new Integer((flagsAsInt >> 9) & 1));

        /** extract SD_TEMPLATE */
        flags.put(SD_TEMPLATE, new Integer((flagsAsInt >> 10) & 3));

        /** extract SD_R_TEMPLATE */
        flags.put(SD_R_TEMPLATE, new Integer((flagsAsInt >> 12) & 1));

        if (JBIG2StreamDecoder.debug)
            System.out.println(flags);
    }
}
