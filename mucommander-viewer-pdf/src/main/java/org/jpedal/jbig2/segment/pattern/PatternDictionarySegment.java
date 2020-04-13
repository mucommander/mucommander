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
* PatternDictionarySegment.java
* ---------------
 */
package org.jpedal.jbig2.segment.pattern;

import org.jpedal.jbig2.JBIG2Exception;
import org.jpedal.jbig2.decoders.JBIG2StreamDecoder;
import org.jpedal.jbig2.image.JBIG2Bitmap;
import org.jpedal.jbig2.segment.Segment;
import org.jpedal.jbig2.util.BinaryOperation;

import java.io.IOException;

public class PatternDictionarySegment extends Segment {

    PatternDictionaryFlags patternDictionaryFlags = new PatternDictionaryFlags();
    private int width;
    private int height;
    private int grayMax;
    private JBIG2Bitmap[] bitmaps;
    private int size;

    public PatternDictionarySegment(JBIG2StreamDecoder streamDecoder) {
        super(streamDecoder);
    }

    public void readSegment() throws IOException, JBIG2Exception {
        /** read text region Segment flags */
        readPatternDictionaryFlags();

        width = decoder.readByte();
        height = decoder.readByte();

        if (JBIG2StreamDecoder.debug)
            System.out.println("pattern dictionary size = " + width + " , " + height);

        short[] buf = new short[4];
        decoder.readByte(buf);
        grayMax = BinaryOperation.getInt32(buf);

        if (JBIG2StreamDecoder.debug)
            System.out.println("grey max = " + grayMax);

        boolean useMMR = patternDictionaryFlags.getFlagValue(PatternDictionaryFlags.HD_MMR) == 1;
        int template = patternDictionaryFlags.getFlagValue(PatternDictionaryFlags.HD_TEMPLATE);

        if (!useMMR) {
            arithmeticDecoder.resetGenericStats(template, null);
            arithmeticDecoder.start();
        }

        short[] genericBAdaptiveTemplateX = new short[4], genericBAdaptiveTemplateY = new short[4];

        genericBAdaptiveTemplateX[0] = (short) -width;
        genericBAdaptiveTemplateY[0] = 0;
        genericBAdaptiveTemplateX[1] = -3;
        genericBAdaptiveTemplateY[1] = -1;
        genericBAdaptiveTemplateX[2] = 2;
        genericBAdaptiveTemplateY[2] = -2;
        genericBAdaptiveTemplateX[3] = -2;
        genericBAdaptiveTemplateY[3] = -2;

        size = grayMax + 1;

        JBIG2Bitmap bitmap = new JBIG2Bitmap(size * width, height, arithmeticDecoder, huffmanDecoder, mmrDecoder);
        bitmap.clear(0);
        bitmap.readBitmap(useMMR, template, false, false, null, genericBAdaptiveTemplateX, genericBAdaptiveTemplateY, segmentHeader.getSegmentDataLength() - 7);

        JBIG2Bitmap bitmaps[] = new JBIG2Bitmap[size];

        int x = 0;
        for (int i = 0; i < size; i++) {
            bitmaps[i] = bitmap.getSlice(x, 0, width, height);
            x += width;
        }

        this.bitmaps = bitmaps;
    }


    public JBIG2Bitmap[] getBitmaps() {
        return bitmaps;
    }

    private void readPatternDictionaryFlags() throws IOException {
        short patternDictionaryFlagsField = decoder.readByte();

        patternDictionaryFlags.setFlags(patternDictionaryFlagsField);

        if (JBIG2StreamDecoder.debug)
            System.out.println("pattern Dictionary flags = " + patternDictionaryFlagsField);
    }

    public PatternDictionaryFlags getPatternDictionaryFlags() {
        return patternDictionaryFlags;
    }

    public int getSize() {
        return size;
    }
}
