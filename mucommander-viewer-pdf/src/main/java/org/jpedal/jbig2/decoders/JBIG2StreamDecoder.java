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
 * JBIG2StreamDecoder.java
 * ---------------
 */
package org.jpedal.jbig2.decoders;

import org.jpedal.jbig2.JBIG2Exception;
import org.jpedal.jbig2.image.JBIG2Bitmap;
import org.jpedal.jbig2.io.StreamReader;
import org.jpedal.jbig2.segment.Segment;
import org.jpedal.jbig2.segment.SegmentHeader;
import org.jpedal.jbig2.segment.extensions.ExtensionSegment;
import org.jpedal.jbig2.segment.pageinformation.PageInformationSegment;
import org.jpedal.jbig2.segment.pattern.PatternDictionarySegment;
import org.jpedal.jbig2.segment.region.generic.GenericRegionSegment;
import org.jpedal.jbig2.segment.region.halftone.HalftoneRegionSegment;
import org.jpedal.jbig2.segment.region.refinement.RefinementRegionSegment;
import org.jpedal.jbig2.segment.region.text.TextRegionSegment;
import org.jpedal.jbig2.segment.stripes.EndOfStripeSegment;
import org.jpedal.jbig2.segment.symboldictionary.SymbolDictionarySegment;
import org.jpedal.jbig2.util.BinaryOperation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class JBIG2StreamDecoder {

    private StreamReader reader;

    private boolean noOfPagesKnown;
    private boolean randomAccessOrganisation;

    private int noOfPages = -1;

    private List<Segment> segments = new ArrayList<Segment>();
    private List<JBIG2Bitmap> bitmaps = new ArrayList<JBIG2Bitmap>();

    private byte[] globalData;

    private ArithmeticDecoder arithmeticDecoder;

    private HuffmanDecoder huffmanDecoder;

    private MMRDecoder mmrDecoder;

    public static boolean debug = false;

    public void movePointer(int i) {
        reader.movePointer(i);
    }

    public void setGlobalData(byte[] data) {
        globalData = data;
    }

    public void decodeJBIG2(byte[] data) throws IOException, JBIG2Exception {
        reader = new StreamReader(data);

        resetDecoder();

        boolean validFile = checkHeader();
        if (JBIG2StreamDecoder.debug)
            System.out.println("validFile = " + validFile);

        if (!validFile) {
            /**
             * Assume this is a stream from a PDF so there is no file header,
             * end of page segments, or end of file segments. Organisation must
             * be sequential, and the number of pages is assumed to be 1.
             */

            noOfPagesKnown = true;
            randomAccessOrganisation = false;
            noOfPages = 1;

            /** check to see if there is any global data to be read */
            if (globalData != null) {
                /** set the reader to read from the global data */
                reader = new StreamReader(globalData);

                huffmanDecoder = new HuffmanDecoder(reader);
                mmrDecoder = new MMRDecoder(reader);
                arithmeticDecoder = new ArithmeticDecoder(reader);

                /** read in the global data segments */
                readSegments();

                /** set the reader back to the main data */
                reader = new StreamReader(data);
            } else {
                /**
                 * There's no global data, so move the file pointer back to the
                 * start of the stream
                 */
                reader.movePointer(-8);
            }
        } else {
            /**
             * We have the file header, so assume it is a valid stand-alone
             * file.
             */

            if (JBIG2StreamDecoder.debug)
                System.out.println("==== File Header ====");

            setFileHeaderFlags();

            if (JBIG2StreamDecoder.debug) {
                System.out.println("randomAccessOrganisation = " + randomAccessOrganisation);
                System.out.println("noOfPagesKnown = " + noOfPagesKnown);
            }

            if (noOfPagesKnown) {
                noOfPages = getNoOfPages();

                if (JBIG2StreamDecoder.debug)
                    System.out.println("noOfPages = " + noOfPages);
            }
        }

        huffmanDecoder = new HuffmanDecoder(reader);
        mmrDecoder = new MMRDecoder(reader);
        arithmeticDecoder = new ArithmeticDecoder(reader);

        /** read in the main segment data */
        readSegments();
    }

    public HuffmanDecoder getHuffmanDecoder() {
        return huffmanDecoder;
    }

    public MMRDecoder getMMRDecoder() {
        return mmrDecoder;
    }

    public ArithmeticDecoder getArithmeticDecoder() {
        return arithmeticDecoder;
    }

    private void resetDecoder() {
        noOfPagesKnown = false;
        randomAccessOrganisation = false;

        noOfPages = -1;

        segments.clear();
        bitmaps.clear();
    }

    public void cleanupPostDecode() {
        // Clear away everything but the page segments
        for (Iterator it = segments.iterator(); it.hasNext(); ) {
            Segment segment = (Segment) it.next();
            SegmentHeader segmentHeader = segment.getSegmentHeader();
            if (segmentHeader.getSegmentType() != Segment.PAGE_INFORMATION) {
                it.remove();
            }
        }

        bitmaps.clear();
    }

    private void readSegments() throws IOException, JBIG2Exception {

        if (JBIG2StreamDecoder.debug)
            System.out.println("==== Segments ====");

        boolean finished = false;
        while (!reader.isFinished() && !finished) {

            SegmentHeader segmentHeader = new SegmentHeader();

            if (JBIG2StreamDecoder.debug)
                System.out.println("==== Segment Header ====");

            readSegmentHeader(segmentHeader);

            // read the Segment data
            Segment segment = null;

            int segmentType = segmentHeader.getSegmentType();
            int[] referredToSegments = segmentHeader.getReferredToSegments();
            int noOfReferredToSegments = segmentHeader.getReferredToSegmentCount();

            switch (segmentType) {
                case Segment.SYMBOL_DICTIONARY:
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("==== Segment Symbol Dictionary ====");

                    segment = new SymbolDictionarySegment(this);

                    segment.setSegmentHeader(segmentHeader);

                    break;

                case Segment.INTERMEDIATE_TEXT_REGION:
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("==== Intermediate Text Region ====");

                    segment = new TextRegionSegment(this, false);

                    segment.setSegmentHeader(segmentHeader);

                    break;

                case Segment.IMMEDIATE_TEXT_REGION:
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("==== Immediate Text Region ====");

                    segment = new TextRegionSegment(this, true);

                    segment.setSegmentHeader(segmentHeader);

                    break;

                case Segment.IMMEDIATE_LOSSLESS_TEXT_REGION:
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("==== Immediate Lossless Text Region ====");

                    segment = new TextRegionSegment(this, true);

                    segment.setSegmentHeader(segmentHeader);

                    break;

                case Segment.PATTERN_DICTIONARY:
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("==== Pattern Dictionary ====");

                    segment = new PatternDictionarySegment(this);

                    segment.setSegmentHeader(segmentHeader);

                    break;

                case Segment.INTERMEDIATE_HALFTONE_REGION:
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("==== Intermediate Halftone Region ====");

                    segment = new HalftoneRegionSegment(this, false);

                    segment.setSegmentHeader(segmentHeader);

                    break;

                case Segment.IMMEDIATE_HALFTONE_REGION:
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("==== Immediate Halftone Region ====");

                    segment = new HalftoneRegionSegment(this, true);

                    segment.setSegmentHeader(segmentHeader);

                    break;

                case Segment.IMMEDIATE_LOSSLESS_HALFTONE_REGION:
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("==== Immediate Lossless Halftone Region ====");

                    segment = new HalftoneRegionSegment(this, true);

                    segment.setSegmentHeader(segmentHeader);

                    break;

                case Segment.INTERMEDIATE_GENERIC_REGION:
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("==== Intermediate Generic Region ====");

                    segment = new GenericRegionSegment(this, false);

                    segment.setSegmentHeader(segmentHeader);

                    break;

                case Segment.IMMEDIATE_GENERIC_REGION:
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("==== Immediate Generic Region ====");

                    segment = new GenericRegionSegment(this, true);

                    segment.setSegmentHeader(segmentHeader);

                    break;

                case Segment.IMMEDIATE_LOSSLESS_GENERIC_REGION:
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("==== Immediate Lossless Generic Region ====");

                    segment = new GenericRegionSegment(this, true);

                    segment.setSegmentHeader(segmentHeader);

                    break;

                case Segment.INTERMEDIATE_GENERIC_REFINEMENT_REGION:
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("==== Intermediate Generic Refinement Region ====");

                    segment = new RefinementRegionSegment(this, false, referredToSegments, noOfReferredToSegments);

                    segment.setSegmentHeader(segmentHeader);

                    break;

                case Segment.IMMEDIATE_GENERIC_REFINEMENT_REGION:
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("==== Immediate Generic Refinement Region ====");

                    segment = new RefinementRegionSegment(this, true, referredToSegments, noOfReferredToSegments);

                    segment.setSegmentHeader(segmentHeader);

                    break;

                case Segment.IMMEDIATE_LOSSLESS_GENERIC_REFINEMENT_REGION:
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("==== Immediate lossless Generic Refinement Region ====");

                    segment = new RefinementRegionSegment(this, true, referredToSegments, noOfReferredToSegments);

                    segment.setSegmentHeader(segmentHeader);

                    break;

                case Segment.PAGE_INFORMATION:
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("==== Page Information Dictionary ====");

                    segment = new PageInformationSegment(this);

                    segment.setSegmentHeader(segmentHeader);

                    break;

                case Segment.END_OF_PAGE:
                    continue;

                case Segment.END_OF_STRIPE:
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("==== End of Stripes ====");

                    segment = new EndOfStripeSegment(this);

                    segment.setSegmentHeader(segmentHeader);
                    break;

                case Segment.END_OF_FILE:
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("==== End of File ====");

                    finished = true;

                    continue;

                case Segment.PROFILES:
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("PROFILES UNIMPLEMENTED");
                    break;

                case Segment.TABLES:
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("TABLES UNIMPLEMENTED");
                    break;

                case Segment.EXTENSION:
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("==== Extensions ====");

                    segment = new ExtensionSegment(this);

                    segment.setSegmentHeader(segmentHeader);

                    break;

                default:
                    System.out.println("Unknown Segment type in JBIG2 stream");

                    break;
            }

            if (!randomAccessOrganisation && segment != null) {
                segment.readSegment();
            }

            if (segments != null) {
                segments.add(segment);
            }
        }

        if (randomAccessOrganisation && segments != null) {
            for (Object segment1 : segments) {
                Segment segment = (Segment) segment1;
                segment.readSegment();
            }
        }
    }

    public PageInformationSegment findPageSegement(int page) {
        for (Object segment1 : segments) {
            Segment segment = (Segment) segment1;
            SegmentHeader segmentHeader = segment.getSegmentHeader();
            if (segmentHeader.getSegmentType() == Segment.PAGE_INFORMATION && segmentHeader.getPageAssociation() == page) {
                return (PageInformationSegment) segment;
            }
        }

        return null;
    }

    public Segment findSegment(int segmentNumber) {
        for (Object segment1 : segments) {
            Segment segment = (Segment) segment1;
            if (segment.getSegmentHeader().getSegmentNumber() == segmentNumber) {
                return segment;
            }
        }

        return null;
    }

    private void readSegmentHeader(SegmentHeader segmentHeader) throws IOException, JBIG2Exception {
        handleSegmentNumber(segmentHeader);

        handleSegmentHeaderFlags(segmentHeader);

        handleSegmentReferredToCountAndRententionFlags(segmentHeader);

        handleReferedToSegmentNumbers(segmentHeader);

        handlePageAssociation(segmentHeader);

        if (segmentHeader.getSegmentType() != Segment.END_OF_FILE)
            handleSegmentDataLength(segmentHeader);
    }

    private void handlePageAssociation(SegmentHeader segmentHeader) throws IOException {
        int pageAssociation;

        boolean isPageAssociationSizeSet = segmentHeader.isPageAssociationSizeSet();
        if (isPageAssociationSizeSet) { // field is 4 bytes long
            short[] buf = new short[4];
            reader.readByte(buf);
            pageAssociation = BinaryOperation.getInt32(buf);
        } else { // field is 1 byte long
            pageAssociation = reader.readByte();
        }

        segmentHeader.setPageAssociation(pageAssociation);

        if (JBIG2StreamDecoder.debug)
            System.out.println("pageAssociation = " + pageAssociation);
    }

    private void handleSegmentNumber(SegmentHeader segmentHeader) throws IOException {
        short[] segmentBytes = new short[4];
        reader.readByte(segmentBytes);

        int segmentNumber = BinaryOperation.getInt32(segmentBytes);

        if (JBIG2StreamDecoder.debug)
            System.out.println("SegmentNumber = " + segmentNumber);
        segmentHeader.setSegmentNumber(segmentNumber);
    }

    private void handleSegmentHeaderFlags(SegmentHeader segmentHeader) throws IOException {
        short segmentHeaderFlags = reader.readByte();
        // System.out.println("SegmentHeaderFlags = " + SegmentHeaderFlags);
        segmentHeader.setSegmentHeaderFlags(segmentHeaderFlags);
    }

    private void handleSegmentReferredToCountAndRententionFlags(SegmentHeader segmentHeader) throws IOException, JBIG2Exception {
        short referedToSegmentCountAndRetentionFlags = reader.readByte();

        int referredToSegmentCount = (referedToSegmentCountAndRetentionFlags & 224) >> 5; // 224
        // =
        // 11100000

        short[] retentionFlags;
        /** take off the first three bits of the first byte */
        short firstByte = (short) (referedToSegmentCountAndRetentionFlags & 31); // 31 =
        // 00011111

        if (referredToSegmentCount <= 4) { // short form

            retentionFlags = new short[1];
            retentionFlags[0] = firstByte;

        } else if (referredToSegmentCount == 7) { // long form

            short[] longFormCountAndFlags = new short[4];
            /** add the first byte of the four */
            longFormCountAndFlags[0] = firstByte;

            for (int i = 1; i < 4; i++)
                // add the next 3 bytes to the array
                longFormCountAndFlags[i] = reader.readByte();

            /** get the count of the referred to Segments */
            referredToSegmentCount = BinaryOperation.getInt32(longFormCountAndFlags);

            /** calculate the number of bytes in this field */
            int noOfBytesInField = (int) Math.ceil(4 + ((referredToSegmentCount + 1) / 8d));
            // System.out.println("noOfBytesInField = " + noOfBytesInField);

            int noOfRententionFlagBytes = noOfBytesInField - 4;
            retentionFlags = new short[noOfRententionFlagBytes];
            reader.readByte(retentionFlags);

        } else { // error
            throw new JBIG2Exception("Error, 3 bit Segment count field = " + referredToSegmentCount);
        }

        segmentHeader.setReferredToSegmentCount(referredToSegmentCount);

        if (JBIG2StreamDecoder.debug)
            System.out.println("referredToSegmentCount = " + referredToSegmentCount);

        segmentHeader.setRententionFlags(retentionFlags);

        if (JBIG2StreamDecoder.debug)
            System.out.print("retentionFlags = ");

        if (JBIG2StreamDecoder.debug) {
            for (short retentionFlag : retentionFlags)
                System.out.print(retentionFlag + " ");
            System.out.println("");
        }
    }

    private void handleReferedToSegmentNumbers(SegmentHeader segmentHeader) throws IOException {
        int referredToSegmentCount = segmentHeader.getReferredToSegmentCount();
        int[] referredToSegments = new int[referredToSegmentCount];

        int segmentNumber = segmentHeader.getSegmentNumber();

        if (segmentNumber <= 256) {
            for (int i = 0; i < referredToSegmentCount; i++)
                referredToSegments[i] = reader.readByte();
        } else if (segmentNumber <= 65536) {
            short[] buf = new short[2];
            for (int i = 0; i < referredToSegmentCount; i++) {
                reader.readByte(buf);
                referredToSegments[i] = BinaryOperation.getInt16(buf);
            }
        } else {
            short[] buf = new short[4];
            for (int i = 0; i < referredToSegmentCount; i++) {
                reader.readByte(buf);
                referredToSegments[i] = BinaryOperation.getInt32(buf);
            }
        }

        segmentHeader.setReferredToSegments(referredToSegments);

        if (JBIG2StreamDecoder.debug) {
            System.out.print("referredToSegments = ");
            for (int referredToSegment : referredToSegments)
                System.out.print(referredToSegment + " ");
            System.out.println("");
        }
    }

    private int getNoOfPages() throws IOException {
        short[] noOfPages = new short[4];
        reader.readByte(noOfPages);

        return BinaryOperation.getInt32(noOfPages);
    }

    private void handleSegmentDataLength(SegmentHeader segmentHeader) throws IOException {
        short[] buf = new short[4];
        reader.readByte(buf);

        int dateLength = BinaryOperation.getInt32(buf);
        segmentHeader.setDataLength(dateLength);

        if (JBIG2StreamDecoder.debug)
            System.out.println("dateLength = " + dateLength);
    }

    private void setFileHeaderFlags() throws IOException {
        short headerFlags = reader.readByte();

        if ((headerFlags & 0xfc) != 0) {
            System.out.println("Warning, reserved bits (2-7) of file header flags are not zero " + headerFlags);
        }

        int fileOrganisation = headerFlags & 1;
        randomAccessOrganisation = fileOrganisation == 0;

        int pagesKnown = headerFlags & 2;
        noOfPagesKnown = pagesKnown == 0;
    }

    private boolean checkHeader() throws IOException {
        short[] controlHeader = new short[]{151, 74, 66, 50, 13, 10, 26, 10};
        short[] actualHeader = new short[8];
        reader.readByte(actualHeader);

        return Arrays.equals(controlHeader, actualHeader);
    }

    public int readBits(int num) throws IOException {
        return reader.readBits(num);
    }

    public int readBit() throws IOException {
        return reader.readBit();
    }

    public void readByte(short[] buff) throws IOException {
        reader.readByte(buff);
    }

    public void consumeRemainingBits() throws IOException {
        reader.consumeRemainingBits();
    }

    public short readByte() throws java.io.IOException {
        return reader.readByte();
    }

    public void appendBitmap(JBIG2Bitmap bitmap) {
        bitmaps.add(bitmap);
    }

    public JBIG2Bitmap findBitmap(int bitmapNumber) {
        for (Object bitmap1 : bitmaps) {
            JBIG2Bitmap bitmap = (JBIG2Bitmap) bitmap1;
            if (bitmap.getBitmapNumber() == bitmapNumber) {
                return bitmap;
            }
        }

        return null;
    }

    public JBIG2Bitmap getPageAsJBIG2Bitmap(int i) {
        return findPageSegement(i).getPageBitmap();
    }

    public boolean isNumberOfPagesKnown() {
        return noOfPagesKnown;
    }

    public int getNumberOfPages() {
        return noOfPages;
    }

    public boolean isRandomAccessOrganisationUsed() {
        return randomAccessOrganisation;
    }

    public List getAllSegments() {
        return segments;
    }
}
