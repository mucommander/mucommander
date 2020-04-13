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
* RefinementRegionSegment.java
* ---------------
 */
package org.jpedal.jbig2.segment.region.refinement;

import org.jpedal.jbig2.JBIG2Exception;
import org.jpedal.jbig2.decoders.JBIG2StreamDecoder;
import org.jpedal.jbig2.image.JBIG2Bitmap;
import org.jpedal.jbig2.segment.pageinformation.PageInformationFlags;
import org.jpedal.jbig2.segment.pageinformation.PageInformationSegment;
import org.jpedal.jbig2.segment.region.RegionFlags;
import org.jpedal.jbig2.segment.region.RegionSegment;

import java.io.IOException;

public class RefinementRegionSegment extends RegionSegment {
    private RefinementRegionFlags refinementRegionFlags = new RefinementRegionFlags();

    private boolean inlineImage;

    private int noOfReferedToSegments;

    int[] referedToSegments;

    public RefinementRegionSegment(JBIG2StreamDecoder streamDecoder, boolean inlineImage, int[] referedToSegments, int noOfReferedToSegments) {
        super(streamDecoder);

        this.inlineImage = inlineImage;
        this.referedToSegments = referedToSegments;
        this.noOfReferedToSegments = noOfReferedToSegments;
    }

    public void readSegment() throws IOException, JBIG2Exception {
        if (JBIG2StreamDecoder.debug)
            System.out.println("==== Reading Generic Refinement Region ====");

        super.readSegment();

        /** read text region segment flags */
        readGenericRegionFlags();

        short[] genericRegionAdaptiveTemplateX = new short[2];
        short[] genericRegionAdaptiveTemplateY = new short[2];

        int template = refinementRegionFlags.getFlagValue(RefinementRegionFlags.GR_TEMPLATE);
        if (template == 0) {
            genericRegionAdaptiveTemplateX[0] = readATValue();
            genericRegionAdaptiveTemplateY[0] = readATValue();
            genericRegionAdaptiveTemplateX[1] = readATValue();
            genericRegionAdaptiveTemplateY[1] = readATValue();
        }

        if (noOfReferedToSegments == 0 || inlineImage) {
            PageInformationSegment pageSegment = decoder.findPageSegement(segmentHeader.getPageAssociation());
            JBIG2Bitmap pageBitmap = pageSegment.getPageBitmap();

            if (pageSegment.getPageBitmapHeight() == -1 && regionBitmapYLocation + regionBitmapHeight > pageBitmap.getHeight()) {
                pageBitmap.expand(regionBitmapYLocation + regionBitmapHeight, pageSegment.getPageInformationFlags().getFlagValue(PageInformationFlags.DEFAULT_PIXEL_VALUE));
            }
        }

        if (noOfReferedToSegments > 1) {
            if (JBIG2StreamDecoder.debug)
                System.out.println("Bad reference in JBIG2 generic refinement Segment");

            return;
        }

        JBIG2Bitmap referedToBitmap;
        if (noOfReferedToSegments == 1) {
            referedToBitmap = decoder.findBitmap(referedToSegments[0]);
        } else {
            PageInformationSegment pageSegment = decoder.findPageSegement(segmentHeader.getPageAssociation());
            JBIG2Bitmap pageBitmap = pageSegment.getPageBitmap();

            referedToBitmap = pageBitmap.getSlice(regionBitmapXLocation, regionBitmapYLocation, regionBitmapWidth, regionBitmapHeight);
        }

        arithmeticDecoder.resetRefinementStats(template, null);
        arithmeticDecoder.start();

        boolean typicalPredictionGenericRefinementOn = refinementRegionFlags.getFlagValue(RefinementRegionFlags.TPGDON) != 0;

        JBIG2Bitmap bitmap = new JBIG2Bitmap(regionBitmapWidth, regionBitmapHeight, arithmeticDecoder, huffmanDecoder, mmrDecoder);

        bitmap.readGenericRefinementRegion(template, typicalPredictionGenericRefinementOn, referedToBitmap, 0, 0, genericRegionAdaptiveTemplateX, genericRegionAdaptiveTemplateY);

        if (inlineImage) {
            PageInformationSegment pageSegment = decoder.findPageSegement(segmentHeader.getPageAssociation());
            JBIG2Bitmap pageBitmap = pageSegment.getPageBitmap();

            int extCombOp = regionFlags.getFlagValue(RegionFlags.EXTERNAL_COMBINATION_OPERATOR);

            pageBitmap.combine(bitmap, regionBitmapXLocation, regionBitmapYLocation, extCombOp);
        } else {
            bitmap.setBitmapNumber(getSegmentHeader().getSegmentNumber());
            decoder.appendBitmap(bitmap);
        }
    }

    private void readGenericRegionFlags() throws IOException {
        /** extract text region Segment flags */
        short refinementRegionFlagsField = decoder.readByte();

        refinementRegionFlags.setFlags(refinementRegionFlagsField);

        if (JBIG2StreamDecoder.debug)
            System.out.println("generic region Segment flags = " + refinementRegionFlagsField);
    }

    public RefinementRegionFlags getGenericRegionFlags() {
        return refinementRegionFlags;
    }
}
