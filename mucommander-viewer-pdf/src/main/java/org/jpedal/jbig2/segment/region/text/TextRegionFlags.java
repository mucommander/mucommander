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
* TextRegionFlags.java
* ---------------
 */
package org.jpedal.jbig2.segment.region.text;

import org.jpedal.jbig2.decoders.JBIG2StreamDecoder;
import org.jpedal.jbig2.segment.Flags;

public class TextRegionFlags extends Flags {

    public static String SB_HUFF = "SB_HUFF";
    public static String SB_REFINE = "SB_REFINE";
    public static String LOG_SB_STRIPES = "LOG_SB_STRIPES";
    public static String REF_CORNER = "REF_CORNER";
    public static String TRANSPOSED = "TRANSPOSED";
    public static String SB_COMB_OP = "SB_COMB_OP";
    public static String SB_DEF_PIXEL = "SB_DEF_PIXEL";
    public static String SB_DS_OFFSET = "SB_DS_OFFSET";
    public static String SB_R_TEMPLATE = "SB_R_TEMPLATE";

    public void setFlags(int flagsAsInt) {
        this.flagsAsInt = flagsAsInt;

        /** extract SB_HUFF */
        flags.put(SB_HUFF, new Integer(flagsAsInt & 1));

        /** extract SB_REFINE */
        flags.put(SB_REFINE, new Integer((flagsAsInt >> 1) & 1));

        /** extract LOG_SB_STRIPES */
        flags.put(LOG_SB_STRIPES, new Integer((flagsAsInt >> 2) & 3));

        /** extract REF_CORNER */
        flags.put(REF_CORNER, new Integer((flagsAsInt >> 4) & 3));

        /** extract TRANSPOSED */
        flags.put(TRANSPOSED, new Integer((flagsAsInt >> 6) & 1));

        /** extract SB_COMB_OP */
        flags.put(SB_COMB_OP, new Integer((flagsAsInt >> 7) & 3));

        /** extract SB_DEF_PIXEL */
        flags.put(SB_DEF_PIXEL, new Integer((flagsAsInt >> 9) & 1));

        int sOffset = (flagsAsInt >> 10) & 0x1f;
        if ((sOffset & 0x10) != 0) {
            sOffset |= -1 - 0x0f;
        }
        flags.put(SB_DS_OFFSET, new Integer(sOffset));

        /** extract SB_R_TEMPLATE */
        flags.put(SB_R_TEMPLATE, new Integer((flagsAsInt >> 15) & 1));

        if (JBIG2StreamDecoder.debug)
            System.out.println(flags);
    }
}
