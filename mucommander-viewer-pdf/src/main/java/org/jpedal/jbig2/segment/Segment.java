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
* Segment.java
* ---------------
 */
package org.jpedal.jbig2.segment;

import org.jpedal.jbig2.JBIG2Exception;
import org.jpedal.jbig2.decoders.ArithmeticDecoder;
import org.jpedal.jbig2.decoders.HuffmanDecoder;
import org.jpedal.jbig2.decoders.JBIG2StreamDecoder;
import org.jpedal.jbig2.decoders.MMRDecoder;

import java.io.IOException;

public abstract class Segment {

    public static final int SYMBOL_DICTIONARY = 0;
    public static final int INTERMEDIATE_TEXT_REGION = 4;
    public static final int IMMEDIATE_TEXT_REGION = 6;
    public static final int IMMEDIATE_LOSSLESS_TEXT_REGION = 7;
    public static final int PATTERN_DICTIONARY = 16;
    public static final int INTERMEDIATE_HALFTONE_REGION = 20;
    public static final int IMMEDIATE_HALFTONE_REGION = 22;
    public static final int IMMEDIATE_LOSSLESS_HALFTONE_REGION = 23;
    public static final int INTERMEDIATE_GENERIC_REGION = 36;
    public static final int IMMEDIATE_GENERIC_REGION = 38;
    public static final int IMMEDIATE_LOSSLESS_GENERIC_REGION = 39;
    public static final int INTERMEDIATE_GENERIC_REFINEMENT_REGION = 40;
    public static final int IMMEDIATE_GENERIC_REFINEMENT_REGION = 42;
    public static final int IMMEDIATE_LOSSLESS_GENERIC_REFINEMENT_REGION = 43;
    public static final int PAGE_INFORMATION = 48;
    public static final int END_OF_PAGE = 49;
    public static final int END_OF_STRIPE = 50;
    public static final int END_OF_FILE = 51;
    public static final int PROFILES = 52;
    public static final int TABLES = 53;
    public static final int EXTENSION = 62;
    public static final int BITMAP = 70;

    protected SegmentHeader segmentHeader;

    protected HuffmanDecoder huffmanDecoder;

    protected ArithmeticDecoder arithmeticDecoder;

    protected MMRDecoder mmrDecoder;

    protected JBIG2StreamDecoder decoder;

    public Segment(JBIG2StreamDecoder streamDecoder) {
        this.decoder = streamDecoder;

//		try {
        //huffDecoder = HuffmanDecoder.getInstance();
//			arithmeticDecoder = ArithmeticDecoder.getInstance();

        huffmanDecoder = decoder.getHuffmanDecoder();
        arithmeticDecoder = decoder.getArithmeticDecoder();
        mmrDecoder = decoder.getMMRDecoder();

//		} catch (JBIG2Exception e) {
//			e.printStackTrace();
//		}
    }

    protected short readATValue() throws IOException {
        short atValue;
        short c0 = atValue = decoder.readByte();

        if ((c0 & 0x80) != 0) {
            atValue |= -1 - 0xff;
        }

        return atValue;
    }

    public SegmentHeader getSegmentHeader() {
        return segmentHeader;
    }

    public void setSegmentHeader(SegmentHeader segmentHeader) {
        this.segmentHeader = segmentHeader;
    }

    public abstract void readSegment() throws IOException, JBIG2Exception;
}
