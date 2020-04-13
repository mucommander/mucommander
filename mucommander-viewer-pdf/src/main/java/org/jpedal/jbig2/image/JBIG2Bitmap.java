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
 * JBIG2Bitmap.java
 * ---------------
 */
package org.jpedal.jbig2.image;

import org.jpedal.jbig2.JBIG2Exception;
import org.jpedal.jbig2.decoders.*;
import org.jpedal.jbig2.util.BinaryOperation;

import java.awt.image.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;

public final class JBIG2Bitmap {

    private int width, height, line;
    private int bitmapNumber;
    //private FastBitSet data;

    private BitSet data;

    private static int counter = 0;

    private ArithmeticDecoder arithmeticDecoder;
    private HuffmanDecoder huffmanDecoder;
    private MMRDecoder mmrDecoder;

    public JBIG2Bitmap(int width, int height, ArithmeticDecoder arithmeticDecoder, HuffmanDecoder huffmanDecoder, MMRDecoder mmrDecoder) {
        this.width = width;
        this.height = height;
        this.arithmeticDecoder = arithmeticDecoder;
        this.huffmanDecoder = huffmanDecoder;
        this.mmrDecoder = mmrDecoder;

        this.line = (width + 7) >> 3;

        this.data = new BitSet(width * height);
    }

    public void readBitmap(boolean useMMR, int template, boolean typicalPredictionGenericDecodingOn, boolean useSkip, JBIG2Bitmap skipBitmap, short[] adaptiveTemplateX, short[] adaptiveTemplateY, int mmrDataLength) throws IOException, JBIG2Exception {

        if (useMMR) {

            //MMRDecoder mmrDecoder = MMRDecoder.getInstance();
            mmrDecoder.reset();

            int[] referenceLine = new int[width + 2];
            int[] codingLine = new int[width + 2];
            codingLine[0] = codingLine[1] = width;

            for (int row = 0; row < height; row++) {

                int i = 0;
                for (; codingLine[i] < width; i++) {
                    referenceLine[i] = codingLine[i];
                }
                referenceLine[i] = referenceLine[i + 1] = width;

                int referenceI = 0;
                int codingI = 0;
                int a0 = 0;

                do {
                    int code1 = mmrDecoder.get2DCode(), code2, code3;

                    switch (code1) {
                        case MMRDecoder.twoDimensionalPass:
                            if (referenceLine[referenceI] < width) {
                                a0 = referenceLine[referenceI + 1];
                                referenceI += 2;
                            }
                            break;
                        case MMRDecoder.twoDimensionalHorizontal:
                            if ((codingI & 1) != 0) {
                                code1 = 0;
                                do {
                                    code1 += code3 = mmrDecoder.getBlackCode();
                                } while (code3 >= 64);

                                code2 = 0;
                                do {
                                    code2 += code3 = mmrDecoder.getWhiteCode();
                                } while (code3 >= 64);
                            } else {
                                code1 = 0;
                                do {
                                    code1 += code3 = mmrDecoder.getWhiteCode();
                                } while (code3 >= 64);

                                code2 = 0;
                                do {
                                    code2 += code3 = mmrDecoder.getBlackCode();
                                } while (code3 >= 64);

                            }
                            if (code1 > 0 || code2 > 0) {
                                a0 = codingLine[codingI++] = a0 + code1;
                                a0 = codingLine[codingI++] = a0 + code2;

                                while (referenceLine[referenceI] <= a0 && referenceLine[referenceI] < width) {
                                    referenceI += 2;
                                }
                            }
                            break;
                        case MMRDecoder.twoDimensionalVertical0:
                            a0 = codingLine[codingI++] = referenceLine[referenceI];
                            if (referenceLine[referenceI] < width) {
                                referenceI++;
                            }

                            break;
                        case MMRDecoder.twoDimensionalVerticalR1:
                            a0 = codingLine[codingI++] = referenceLine[referenceI] + 1;
                            if (referenceLine[referenceI] < width) {
                                referenceI++;
                                while (referenceLine[referenceI] <= a0 && referenceLine[referenceI] < width) {
                                    referenceI += 2;
                                }
                            }

                            break;
                        case MMRDecoder.twoDimensionalVerticalR2:
                            a0 = codingLine[codingI++] = referenceLine[referenceI] + 2;
                            if (referenceLine[referenceI] < width) {
                                referenceI++;
                                while (referenceLine[referenceI] <= a0 && referenceLine[referenceI] < width) {
                                    referenceI += 2;
                                }
                            }

                            break;
                        case MMRDecoder.twoDimensionalVerticalR3:
                            a0 = codingLine[codingI++] = referenceLine[referenceI] + 3;
                            if (referenceLine[referenceI] < width) {
                                referenceI++;
                                while (referenceLine[referenceI] <= a0 && referenceLine[referenceI] < width) {
                                    referenceI += 2;
                                }
                            }

                            break;
                        case MMRDecoder.twoDimensionalVerticalL1:
                            a0 = codingLine[codingI++] = referenceLine[referenceI] - 1;
                            if (referenceI > 0) {
                                referenceI--;
                            } else {
                                referenceI++;
                            }

                            while (referenceLine[referenceI] <= a0 && referenceLine[referenceI] < width) {
                                referenceI += 2;
                            }

                            break;
                        case MMRDecoder.twoDimensionalVerticalL2:
                            a0 = codingLine[codingI++] = referenceLine[referenceI] - 2;
                            if (referenceI > 0) {
                                referenceI--;
                            } else {
                                referenceI++;
                            }

                            while (referenceLine[referenceI] <= a0 && referenceLine[referenceI] < width) {
                                referenceI += 2;
                            }

                            break;
                        case MMRDecoder.twoDimensionalVerticalL3:
                            a0 = codingLine[codingI++] = referenceLine[referenceI] - 3;
                            if (referenceI > 0) {
                                referenceI--;
                            } else {
                                referenceI++;
                            }

                            while (referenceLine[referenceI] <= a0 && referenceLine[referenceI] < width) {
                                referenceI += 2;
                            }

                            break;
                        default:
                            if (JBIG2StreamDecoder.debug)
                                System.out.println("Illegal code in JBIG2 MMR bitmap data");

                            break;
                    }
                } while (a0 < width);

                codingLine[codingI++] = width;

                for (int j = 0; codingLine[j] < width; j += 2) {
                    for (int col = codingLine[j]; col < codingLine[j + 1]; col++) {
                        setPixel(col, row, 1);
                    }
                }
            }

            if (mmrDataLength >= 0) {
                mmrDecoder.skipTo(mmrDataLength);
            } else {
                if (mmrDecoder.get24Bits() != 0x001001) {
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("Missing EOFB in JBIG2 MMR bitmap data");
                }
            }

        } else {

            //ArithmeticDecoder arithmeticDecoder = ArithmeticDecoder.getInstance();

            BitmapPointer cxPtr0 = new BitmapPointer(this), cxPtr1 = new BitmapPointer(this);
            BitmapPointer atPtr0 = new BitmapPointer(this), atPtr1 = new BitmapPointer(this), atPtr2 = new BitmapPointer(this), atPtr3 = new BitmapPointer(this);

            long ltpCX = 0;
            if (typicalPredictionGenericDecodingOn) {
                switch (template) {
                    case 0:
                        ltpCX = 0x3953;
                        break;
                    case 1:
                        ltpCX = 0x079a;
                        break;
                    case 2:
                        ltpCX = 0x0e3;
                        break;
                    case 3:
                        ltpCX = 0x18a;
                        break;
                }
            }

            boolean ltp = false;
            long cx, cx0, cx1, cx2;

            for (int row = 0; row < height; row++) {
                if (typicalPredictionGenericDecodingOn) {
                    int bit = arithmeticDecoder.decodeBit(ltpCX, arithmeticDecoder.genericRegionStats);
                    if (bit != 0) {
                        ltp = !ltp;
                    }

                    if (ltp) {
                        duplicateRow(row, row - 1);
                        continue;
                    }
                }

                int pixel;

                switch (template) {
                    case 0:

                        cxPtr0.setPointer(0, row - 2);
                        cx0 = cxPtr0.nextPixel();
                        cx0 = (BinaryOperation.bit32Shift(cx0, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr0.nextPixel();

                        cxPtr1.setPointer(0, row - 1);
                        cx1 = cxPtr1.nextPixel();

                        cx1 = (BinaryOperation.bit32Shift(cx1, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr1.nextPixel();
                        cx1 = (BinaryOperation.bit32Shift(cx1, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr1.nextPixel();

                        cx2 = 0;

                        atPtr0.setPointer(adaptiveTemplateX[0], row + adaptiveTemplateY[0]);
                        atPtr1.setPointer(adaptiveTemplateX[1], row + adaptiveTemplateY[1]);
                        atPtr2.setPointer(adaptiveTemplateX[2], row + adaptiveTemplateY[2]);
                        atPtr3.setPointer(adaptiveTemplateX[3], row + adaptiveTemplateY[3]);

                        for (int col = 0; col < width; col++) {

                            cx = (BinaryOperation.bit32Shift(cx0, 13, BinaryOperation.LEFT_SHIFT)) | (BinaryOperation.bit32Shift(cx1, 8, BinaryOperation.LEFT_SHIFT)) | (BinaryOperation.bit32Shift(cx2, 4, BinaryOperation.LEFT_SHIFT)) | (atPtr0.nextPixel() << 3) | (atPtr1.nextPixel() << 2) | (atPtr2.nextPixel() << 1) | atPtr3.nextPixel();

                            if (useSkip && skipBitmap.getPixel(col, row) != 0) {
                                pixel = 0;
                            } else {
                                pixel = arithmeticDecoder.decodeBit(cx, arithmeticDecoder.genericRegionStats);
                                if (pixel != 0) {
                                    setPixel(col, row, 1);
                                }
                            }

                            cx0 = ((BinaryOperation.bit32Shift(cx0, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr0.nextPixel()) & 0x07;
                            cx1 = ((BinaryOperation.bit32Shift(cx1, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr1.nextPixel()) & 0x1f;
                            cx2 = ((BinaryOperation.bit32Shift(cx2, 1, BinaryOperation.LEFT_SHIFT)) | pixel) & 0x0f;
                        }
                        break;

                    case 1:

                        cxPtr0.setPointer(0, row - 2);
                        cx0 = cxPtr0.nextPixel();
                        cx0 = (BinaryOperation.bit32Shift(cx0, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr0.nextPixel();
                        cx0 = (BinaryOperation.bit32Shift(cx0, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr0.nextPixel();

                        cxPtr1.setPointer(0, row - 1);
                        cx1 = cxPtr1.nextPixel();
                        cx1 = (BinaryOperation.bit32Shift(cx1, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr1.nextPixel();
                        cx1 = (BinaryOperation.bit32Shift(cx1, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr1.nextPixel();

                        cx2 = 0;

                        atPtr0.setPointer(adaptiveTemplateX[0], row + adaptiveTemplateY[0]);

                        for (int col = 0; col < width; col++) {

                            cx = (BinaryOperation.bit32Shift(cx0, 9, BinaryOperation.LEFT_SHIFT)) | (BinaryOperation.bit32Shift(cx1, 4, BinaryOperation.LEFT_SHIFT)) | (BinaryOperation.bit32Shift(cx2, 1, BinaryOperation.LEFT_SHIFT)) | atPtr0.nextPixel();

                            if (useSkip && skipBitmap.getPixel(col, row) != 0) {
                                pixel = 0;
                            } else {
                                pixel = arithmeticDecoder.decodeBit(cx, arithmeticDecoder.genericRegionStats);
                                if (pixel != 0) {
                                    setPixel(col, row, 1);
                                }
                            }

                            cx0 = ((BinaryOperation.bit32Shift(cx0, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr0.nextPixel()) & 0x0f;
                            cx1 = ((BinaryOperation.bit32Shift(cx1, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr1.nextPixel()) & 0x1f;
                            cx2 = ((BinaryOperation.bit32Shift(cx2, 1, BinaryOperation.LEFT_SHIFT)) | pixel) & 0x07;
                        }
                        break;

                    case 2:

                        cxPtr0.setPointer(0, row - 2);
                        cx0 = cxPtr0.nextPixel();
                        cx0 = (BinaryOperation.bit32Shift(cx0, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr0.nextPixel();

                        cxPtr1.setPointer(0, row - 1);
                        cx1 = cxPtr1.nextPixel();
                        cx1 = (BinaryOperation.bit32Shift(cx1, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr1.nextPixel();

                        cx2 = 0;

                        atPtr0.setPointer(adaptiveTemplateX[0], row + adaptiveTemplateY[0]);

                        for (int col = 0; col < width; col++) {

                            cx = (BinaryOperation.bit32Shift(cx0, 7, BinaryOperation.LEFT_SHIFT)) | (BinaryOperation.bit32Shift(cx1, 3, BinaryOperation.LEFT_SHIFT)) | (BinaryOperation.bit32Shift(cx2, 1, BinaryOperation.LEFT_SHIFT)) | atPtr0.nextPixel();

                            if (useSkip && skipBitmap.getPixel(col, row) != 0) {
                                pixel = 0;
                            } else {
                                pixel = arithmeticDecoder.decodeBit(cx, arithmeticDecoder.genericRegionStats);
                                if (pixel != 0) {
                                    setPixel(col, row, 1);
                                }
                            }

                            cx0 = ((BinaryOperation.bit32Shift(cx0, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr0.nextPixel()) & 0x07;
                            cx1 = ((BinaryOperation.bit32Shift(cx1, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr1.nextPixel()) & 0x0f;
                            cx2 = ((BinaryOperation.bit32Shift(cx2, 1, BinaryOperation.LEFT_SHIFT)) | pixel) & 0x03;
                        }
                        break;

                    case 3:

                        cxPtr1.setPointer(0, row - 1);
                        cx1 = cxPtr1.nextPixel();
                        cx1 = (BinaryOperation.bit32Shift(cx1, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr1.nextPixel();

                        cx2 = 0;

                        atPtr0.setPointer(adaptiveTemplateX[0], row + adaptiveTemplateY[0]);

                        for (int col = 0; col < width; col++) {

                            cx = (BinaryOperation.bit32Shift(cx1, 5, BinaryOperation.LEFT_SHIFT)) | (BinaryOperation.bit32Shift(cx2, 1, BinaryOperation.LEFT_SHIFT)) | atPtr0.nextPixel();

                            if (useSkip && skipBitmap.getPixel(col, row) != 0) {
                                pixel = 0;

                            } else {
                                pixel = arithmeticDecoder.decodeBit(cx, arithmeticDecoder.genericRegionStats);
                                if (pixel != 0) {
                                    setPixel(col, row, 1);
                                }
                            }

                            cx1 = ((BinaryOperation.bit32Shift(cx1, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr1.nextPixel()) & 0x1f;
                            cx2 = ((BinaryOperation.bit32Shift(cx2, 1, BinaryOperation.LEFT_SHIFT)) | pixel) & 0x0f;
                        }
                        break;
                }
            }
        }
    }

    public void readGenericRefinementRegion(int template, boolean typicalPredictionGenericRefinementOn, JBIG2Bitmap referredToBitmap, int referenceDX, int referenceDY, short[] adaptiveTemplateX, short[] adaptiveTemplateY) throws IOException, JBIG2Exception {

        //ArithmeticDecoder arithmeticDecoder = ArithmeticDecoder.getInstance();

        BitmapPointer cxPtr0, cxPtr1, cxPtr2, cxPtr3, cxPtr4, cxPtr5, cxPtr6, typicalPredictionGenericRefinementCXPtr0, typicalPredictionGenericRefinementCXPtr1, typicalPredictionGenericRefinementCXPtr2;

        long ltpCX;
        if (template != 0) {
            ltpCX = 0x008;

            cxPtr0 = new BitmapPointer(this);
            cxPtr1 = new BitmapPointer(this);
            cxPtr2 = new BitmapPointer(referredToBitmap);
            cxPtr3 = new BitmapPointer(referredToBitmap);
            cxPtr4 = new BitmapPointer(referredToBitmap);
            cxPtr5 = new BitmapPointer(this);
            cxPtr6 = new BitmapPointer(this);
            typicalPredictionGenericRefinementCXPtr0 = new BitmapPointer(referredToBitmap);
            typicalPredictionGenericRefinementCXPtr1 = new BitmapPointer(referredToBitmap);
            typicalPredictionGenericRefinementCXPtr2 = new BitmapPointer(referredToBitmap);
        } else {
            ltpCX = 0x0010;

            cxPtr0 = new BitmapPointer(this);
            cxPtr1 = new BitmapPointer(this);
            cxPtr2 = new BitmapPointer(referredToBitmap);
            cxPtr3 = new BitmapPointer(referredToBitmap);
            cxPtr4 = new BitmapPointer(referredToBitmap);
            cxPtr5 = new BitmapPointer(this);
            cxPtr6 = new BitmapPointer(referredToBitmap);
            typicalPredictionGenericRefinementCXPtr0 = new BitmapPointer(referredToBitmap);
            typicalPredictionGenericRefinementCXPtr1 = new BitmapPointer(referredToBitmap);
            typicalPredictionGenericRefinementCXPtr2 = new BitmapPointer(referredToBitmap);
        }

        long cx, cx0, cx2, cx3, cx4;
        long typicalPredictionGenericRefinementCX0, typicalPredictionGenericRefinementCX1, typicalPredictionGenericRefinementCX2;
        boolean ltp = false;

        for (int row = 0; row < height; row++) {

            if (template != 0) {

                cxPtr0.setPointer(0, row - 1);
                cx0 = cxPtr0.nextPixel();

                cxPtr1.setPointer(-1, row);

                cxPtr2.setPointer(-referenceDX, row - 1 - referenceDY);

                cxPtr3.setPointer(-1 - referenceDX, row - referenceDY);
                cx3 = cxPtr3.nextPixel();
                cx3 = (BinaryOperation.bit32Shift(cx3, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr3.nextPixel();

                cxPtr4.setPointer(-referenceDX, row + 1 - referenceDY);
                cx4 = cxPtr4.nextPixel();

                typicalPredictionGenericRefinementCX0 = typicalPredictionGenericRefinementCX1 = typicalPredictionGenericRefinementCX2 = 0;

                if (typicalPredictionGenericRefinementOn) {
                    typicalPredictionGenericRefinementCXPtr0.setPointer(-1 - referenceDX, row - 1 - referenceDY);
                    typicalPredictionGenericRefinementCX0 = typicalPredictionGenericRefinementCXPtr0.nextPixel();
                    typicalPredictionGenericRefinementCX0 = (BinaryOperation.bit32Shift(typicalPredictionGenericRefinementCX0, 1, BinaryOperation.LEFT_SHIFT)) | typicalPredictionGenericRefinementCXPtr0.nextPixel();
                    typicalPredictionGenericRefinementCX0 = (BinaryOperation.bit32Shift(typicalPredictionGenericRefinementCX0, 1, BinaryOperation.LEFT_SHIFT)) | typicalPredictionGenericRefinementCXPtr0.nextPixel();

                    typicalPredictionGenericRefinementCXPtr1.setPointer(-1 - referenceDX, row - referenceDY);
                    typicalPredictionGenericRefinementCX1 = typicalPredictionGenericRefinementCXPtr1.nextPixel();
                    typicalPredictionGenericRefinementCX1 = (BinaryOperation.bit32Shift(typicalPredictionGenericRefinementCX1, 1, BinaryOperation.LEFT_SHIFT)) | typicalPredictionGenericRefinementCXPtr1.nextPixel();
                    typicalPredictionGenericRefinementCX1 = (BinaryOperation.bit32Shift(typicalPredictionGenericRefinementCX1, 1, BinaryOperation.LEFT_SHIFT)) | typicalPredictionGenericRefinementCXPtr1.nextPixel();

                    typicalPredictionGenericRefinementCXPtr2.setPointer(-1 - referenceDX, row + 1 - referenceDY);
                    typicalPredictionGenericRefinementCX2 = typicalPredictionGenericRefinementCXPtr2.nextPixel();
                    typicalPredictionGenericRefinementCX2 = (BinaryOperation.bit32Shift(typicalPredictionGenericRefinementCX2, 1, BinaryOperation.LEFT_SHIFT)) | typicalPredictionGenericRefinementCXPtr2.nextPixel();
                    typicalPredictionGenericRefinementCX2 = (BinaryOperation.bit32Shift(typicalPredictionGenericRefinementCX2, 1, BinaryOperation.LEFT_SHIFT)) | typicalPredictionGenericRefinementCXPtr2.nextPixel();
                }

                for (int col = 0; col < width; col++) {

                    cx0 = ((BinaryOperation.bit32Shift(cx0, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr0.nextPixel()) & 7;
                    cx3 = ((BinaryOperation.bit32Shift(cx3, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr3.nextPixel()) & 7;
                    cx4 = ((BinaryOperation.bit32Shift(cx4, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr4.nextPixel()) & 3;

                    if (typicalPredictionGenericRefinementOn) {
                        typicalPredictionGenericRefinementCX0 = ((BinaryOperation.bit32Shift(typicalPredictionGenericRefinementCX0, 1, BinaryOperation.LEFT_SHIFT)) | typicalPredictionGenericRefinementCXPtr0.nextPixel()) & 7;
                        typicalPredictionGenericRefinementCX1 = ((BinaryOperation.bit32Shift(typicalPredictionGenericRefinementCX1, 1, BinaryOperation.LEFT_SHIFT)) | typicalPredictionGenericRefinementCXPtr1.nextPixel()) & 7;
                        typicalPredictionGenericRefinementCX2 = ((BinaryOperation.bit32Shift(typicalPredictionGenericRefinementCX2, 1, BinaryOperation.LEFT_SHIFT)) | typicalPredictionGenericRefinementCXPtr2.nextPixel()) & 7;

                        int decodeBit = arithmeticDecoder.decodeBit(ltpCX, arithmeticDecoder.refinementRegionStats);
                        if (decodeBit != 0) {
                            ltp = !ltp;
                        }
                        if (typicalPredictionGenericRefinementCX0 == 0 && typicalPredictionGenericRefinementCX1 == 0 && typicalPredictionGenericRefinementCX2 == 0) {
                            setPixel(col, row, 0);
                            continue;
                        } else if (typicalPredictionGenericRefinementCX0 == 7 && typicalPredictionGenericRefinementCX1 == 7 && typicalPredictionGenericRefinementCX2 == 7) {
                            setPixel(col, row, 1);
                            continue;
                        }
                    }

                    cx = (BinaryOperation.bit32Shift(cx0, 7, BinaryOperation.LEFT_SHIFT)) | (cxPtr1.nextPixel() << 6) | (cxPtr2.nextPixel() << 5) | (BinaryOperation.bit32Shift(cx3, 2, BinaryOperation.LEFT_SHIFT)) | cx4;

                    int pixel = arithmeticDecoder.decodeBit(cx, arithmeticDecoder.refinementRegionStats);
                    if (pixel == 1) {
                        setPixel(col, row, 1);
                    }
                }

            } else {

                cxPtr0.setPointer(0, row - 1);
                cx0 = cxPtr0.nextPixel();

                cxPtr1.setPointer(-1, row);

                cxPtr2.setPointer(-referenceDX, row - 1 - referenceDY);
                cx2 = cxPtr2.nextPixel();

                cxPtr3.setPointer(-1 - referenceDX, row - referenceDY);
                cx3 = cxPtr3.nextPixel();
                cx3 = (BinaryOperation.bit32Shift(cx3, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr3.nextPixel();

                cxPtr4.setPointer(-1 - referenceDX, row + 1 - referenceDY);
                cx4 = cxPtr4.nextPixel();
                cx4 = (BinaryOperation.bit32Shift(cx4, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr4.nextPixel();

                cxPtr5.setPointer(adaptiveTemplateX[0], row + adaptiveTemplateY[0]);

                cxPtr6.setPointer(adaptiveTemplateX[1] - referenceDX, row + adaptiveTemplateY[1] - referenceDY);

                typicalPredictionGenericRefinementCX0 = typicalPredictionGenericRefinementCX1 = typicalPredictionGenericRefinementCX2 = 0;
                if (typicalPredictionGenericRefinementOn) {
                    typicalPredictionGenericRefinementCXPtr0.setPointer(-1 - referenceDX, row - 1 - referenceDY);
                    typicalPredictionGenericRefinementCX0 = typicalPredictionGenericRefinementCXPtr0.nextPixel();
                    typicalPredictionGenericRefinementCX0 = (BinaryOperation.bit32Shift(typicalPredictionGenericRefinementCX0, 1, BinaryOperation.LEFT_SHIFT)) | typicalPredictionGenericRefinementCXPtr0.nextPixel();
                    typicalPredictionGenericRefinementCX0 = (BinaryOperation.bit32Shift(typicalPredictionGenericRefinementCX0, 1, BinaryOperation.LEFT_SHIFT)) | typicalPredictionGenericRefinementCXPtr0.nextPixel();

                    typicalPredictionGenericRefinementCXPtr1.setPointer(-1 - referenceDX, row - referenceDY);
                    typicalPredictionGenericRefinementCX1 = typicalPredictionGenericRefinementCXPtr1.nextPixel();
                    typicalPredictionGenericRefinementCX1 = (BinaryOperation.bit32Shift(typicalPredictionGenericRefinementCX1, 1, BinaryOperation.LEFT_SHIFT)) | typicalPredictionGenericRefinementCXPtr1.nextPixel();
                    typicalPredictionGenericRefinementCX1 = (BinaryOperation.bit32Shift(typicalPredictionGenericRefinementCX1, 1, BinaryOperation.LEFT_SHIFT)) | typicalPredictionGenericRefinementCXPtr1.nextPixel();

                    typicalPredictionGenericRefinementCXPtr2.setPointer(-1 - referenceDX, row + 1 - referenceDY);
                    typicalPredictionGenericRefinementCX2 = typicalPredictionGenericRefinementCXPtr2.nextPixel();
                    typicalPredictionGenericRefinementCX2 = (BinaryOperation.bit32Shift(typicalPredictionGenericRefinementCX2, 1, BinaryOperation.LEFT_SHIFT)) | typicalPredictionGenericRefinementCXPtr2.nextPixel();
                    typicalPredictionGenericRefinementCX2 = (BinaryOperation.bit32Shift(typicalPredictionGenericRefinementCX2, 1, BinaryOperation.LEFT_SHIFT)) | typicalPredictionGenericRefinementCXPtr2.nextPixel();
                }

                for (int col = 0; col < width; col++) {

                    cx0 = ((BinaryOperation.bit32Shift(cx0, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr0.nextPixel()) & 3;
                    cx2 = ((BinaryOperation.bit32Shift(cx2, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr2.nextPixel()) & 3;
                    cx3 = ((BinaryOperation.bit32Shift(cx3, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr3.nextPixel()) & 7;
                    cx4 = ((BinaryOperation.bit32Shift(cx4, 1, BinaryOperation.LEFT_SHIFT)) | cxPtr4.nextPixel()) & 7;

                    if (typicalPredictionGenericRefinementOn) {
                        typicalPredictionGenericRefinementCX0 = ((BinaryOperation.bit32Shift(typicalPredictionGenericRefinementCX0, 1, BinaryOperation.LEFT_SHIFT)) | typicalPredictionGenericRefinementCXPtr0.nextPixel()) & 7;
                        typicalPredictionGenericRefinementCX1 = ((BinaryOperation.bit32Shift(typicalPredictionGenericRefinementCX1, 1, BinaryOperation.LEFT_SHIFT)) | typicalPredictionGenericRefinementCXPtr1.nextPixel()) & 7;
                        typicalPredictionGenericRefinementCX2 = ((BinaryOperation.bit32Shift(typicalPredictionGenericRefinementCX2, 1, BinaryOperation.LEFT_SHIFT)) | typicalPredictionGenericRefinementCXPtr2.nextPixel()) & 7;

                        int decodeBit = arithmeticDecoder.decodeBit(ltpCX, arithmeticDecoder.refinementRegionStats);
                        if (decodeBit == 1) {
                            ltp = !ltp;
                        }
                        if (typicalPredictionGenericRefinementCX0 == 0 && typicalPredictionGenericRefinementCX1 == 0 && typicalPredictionGenericRefinementCX2 == 0) {
                            setPixel(col, row, 0);
                            continue;
                        } else if (typicalPredictionGenericRefinementCX0 == 7 && typicalPredictionGenericRefinementCX1 == 7 && typicalPredictionGenericRefinementCX2 == 7) {
                            setPixel(col, row, 1);
                            continue;
                        }
                    }

                    cx = (BinaryOperation.bit32Shift(cx0, 11, BinaryOperation.LEFT_SHIFT)) | (cxPtr1.nextPixel() << 10) | (BinaryOperation.bit32Shift(cx2, 8, BinaryOperation.LEFT_SHIFT)) | (BinaryOperation.bit32Shift(cx3, 5, BinaryOperation.LEFT_SHIFT)) | (BinaryOperation.bit32Shift(cx4, 2, BinaryOperation.LEFT_SHIFT)) | (cxPtr5.nextPixel() << 1) | cxPtr6.nextPixel();

                    int pixel = arithmeticDecoder.decodeBit(cx, arithmeticDecoder.refinementRegionStats);
                    if (pixel == 1) {
                        setPixel(col, row, 1);
                    }
                }
            }
        }
    }

    public void readTextRegion(boolean huffman, boolean symbolRefine, int noOfSymbolInstances, int logStrips, int noOfSymbols, int[][] symbolCodeTable, int symbolCodeLength, JBIG2Bitmap[] symbols, int defaultPixel, int combinationOperator, boolean transposed, int referenceCorner, int sOffset, int[][] huffmanFSTable, int[][] huffmanDSTable, int[][] huffmanDTTable, int[][] huffmanRDWTable, int[][] huffmanRDHTable, int[][] huffmanRDXTable, int[][] huffmanRDYTable, int[][] huffmanRSizeTable, int template, short[] symbolRegionAdaptiveTemplateX,
                               short[] symbolRegionAdaptiveTemplateY, JBIG2StreamDecoder decoder) throws JBIG2Exception, IOException {

        JBIG2Bitmap symbolBitmap;

        int strips = 1 << logStrips;

        clear(defaultPixel);

        //HuffmanDecoder huffDecoder = HuffmanDecoder.getInstance();
        //ArithmeticDecoder arithmeticDecoder = ArithmeticDecoder.getInstance();

        int t;
        if (huffman) {
            t = huffmanDecoder.decodeInt(huffmanDTTable).intResult();
        } else {
            t = arithmeticDecoder.decodeInt(arithmeticDecoder.iadtStats).intResult();
        }
        t *= -strips;

        int currentInstance = 0;
        int firstS = 0;
        int dt, tt, ds, s;
        while (currentInstance < noOfSymbolInstances) {

            if (huffman) {
                dt = huffmanDecoder.decodeInt(huffmanDTTable).intResult();
            } else {
                dt = arithmeticDecoder.decodeInt(arithmeticDecoder.iadtStats).intResult();
            }
            t += dt * strips;

            if (huffman) {
                ds = huffmanDecoder.decodeInt(huffmanFSTable).intResult();
            } else {
                ds = arithmeticDecoder.decodeInt(arithmeticDecoder.iafsStats).intResult();
            }
            firstS += ds;
            s = firstS;

            while (true) {

                if (strips == 1) {
                    dt = 0;
                } else if (huffman) {
                    dt = decoder.readBits(logStrips);
                } else {
                    dt = arithmeticDecoder.decodeInt(arithmeticDecoder.iaitStats).intResult();
                }
                tt = t + dt;

                long symbolID;
                if (huffman) {
                    if (symbolCodeTable != null) {
                        symbolID = huffmanDecoder.decodeInt(symbolCodeTable).intResult();
                    } else {
                        symbolID = decoder.readBits(symbolCodeLength);
                    }
                } else {
                    symbolID = arithmeticDecoder.decodeIAID(symbolCodeLength, arithmeticDecoder.iaidStats);
                }

                if (symbolID >= noOfSymbols) {
                    if (JBIG2StreamDecoder.debug)
                        System.out.println("Invalid symbol number in JBIG2 text region");
                } else {
                    // symbolBitmap = null;

                    int ri;
                    if (symbolRefine) {
                        if (huffman) {
                            ri = decoder.readBit();
                        } else {
                            ri = arithmeticDecoder.decodeInt(arithmeticDecoder.iariStats).intResult();
                        }
                    } else {
                        ri = 0;
                    }
                    if (ri != 0) {

                        int refinementDeltaWidth, refinementDeltaHeight, refinementDeltaX, refinementDeltaY;

                        if (huffman) {
                            refinementDeltaWidth = huffmanDecoder.decodeInt(huffmanRDWTable).intResult();
                            refinementDeltaHeight = huffmanDecoder.decodeInt(huffmanRDHTable).intResult();
                            refinementDeltaX = huffmanDecoder.decodeInt(huffmanRDXTable).intResult();
                            refinementDeltaY = huffmanDecoder.decodeInt(huffmanRDYTable).intResult();

                            decoder.consumeRemainingBits();
                            arithmeticDecoder.start();
                        } else {
                            refinementDeltaWidth = arithmeticDecoder.decodeInt(arithmeticDecoder.iardwStats).intResult();
                            refinementDeltaHeight = arithmeticDecoder.decodeInt(arithmeticDecoder.iardhStats).intResult();
                            refinementDeltaX = arithmeticDecoder.decodeInt(arithmeticDecoder.iardxStats).intResult();
                            refinementDeltaY = arithmeticDecoder.decodeInt(arithmeticDecoder.iardyStats).intResult();
                        }
                        refinementDeltaX = ((refinementDeltaWidth >= 0) ? refinementDeltaWidth : refinementDeltaWidth - 1) / 2 + refinementDeltaX;
                        refinementDeltaY = ((refinementDeltaHeight >= 0) ? refinementDeltaHeight : refinementDeltaHeight - 1) / 2 + refinementDeltaY;

                        symbolBitmap = new JBIG2Bitmap(refinementDeltaWidth + symbols[(int) symbolID].width, refinementDeltaHeight + symbols[(int) symbolID].height, arithmeticDecoder, huffmanDecoder, mmrDecoder);

                        symbolBitmap.readGenericRefinementRegion(template, false, symbols[(int) symbolID], refinementDeltaX, refinementDeltaY, symbolRegionAdaptiveTemplateX, symbolRegionAdaptiveTemplateY);

                    } else {
                        symbolBitmap = symbols[(int) symbolID];
                    }

                    int bitmapWidth = symbolBitmap.width - 1;
                    int bitmapHeight = symbolBitmap.height - 1;
                    if (transposed) {
                        switch (referenceCorner) {
                            case 0: // bottom left
                                combine(symbolBitmap, tt, s, combinationOperator);
                                break;
                            case 1: // top left
                                combine(symbolBitmap, tt, s, combinationOperator);
                                break;
                            case 2: // bottom right
                                combine(symbolBitmap, tt - bitmapWidth, s, combinationOperator);
                                break;
                            case 3: // top right
                                combine(symbolBitmap, tt - bitmapWidth, s, combinationOperator);
                                break;
                        }
                        s += bitmapHeight;
                    } else {
                        switch (referenceCorner) {
                            case 0: // bottom left
                                combine(symbolBitmap, s, tt - bitmapHeight, combinationOperator);
                                break;
                            case 1: // top left
                                combine(symbolBitmap, s, tt, combinationOperator);
                                break;
                            case 2: // bottom right
                                combine(symbolBitmap, s, tt - bitmapHeight, combinationOperator);
                                break;
                            case 3: // top right
                                combine(symbolBitmap, s, tt, combinationOperator);
                                break;
                        }
                        s += bitmapWidth;
                    }
                }

                currentInstance++;

                DecodeIntResult decodeIntResult;

                if (huffman) {
                    decodeIntResult = huffmanDecoder.decodeInt(huffmanDSTable);
                } else {
                    decodeIntResult = arithmeticDecoder.decodeInt(arithmeticDecoder.iadsStats);
                }

                if (!decodeIntResult.booleanResult()) {
                    break;
                }

                ds = decodeIntResult.intResult();

                s += sOffset + ds;
            }
        }
    }

    public void clear(int defPixel) {
        data.set(0, data.size(), defPixel == 1);
    }

    public void combine(JBIG2Bitmap bitmap, int x, int y, long combOp) {
        int srcWidth = bitmap.width;
        int srcHeight = bitmap.height;
        int srcRow = 0, srcCol = 0;

//		int maxRow = y + srcHeight;
//		int maxCol = x + srcWidth;
//
//		for (int row = y; row < maxRow; row++) {
//			for (int col = x; col < maxCol; srcCol += 8, col += 8) {
//
//				byte srcPixelByte = bitmap.getPixelByte(srcCol, srcRow);
//				byte dstPixelByte = getPixelByte(col, row);
//				byte endPixelByte;
//
//				switch ((int) combOp) {
//				case 0: // or
//					endPixelByte = (byte) (dstPixelByte | srcPixelByte);
//					break;
//				case 1: // and
//					endPixelByte = (byte) (dstPixelByte & srcPixelByte);
//					break;
//				case 2: // xor
//					endPixelByte = (byte) (dstPixelByte ^ srcPixelByte);
//					break;
//				case 3: // xnor
//					endPixelByte = (byte) ~(dstPixelByte ^ srcPixelByte);
//					break;
//				case 4: // replace
//				default:
//					endPixelByte = srcPixelByte;
//					break;
//				}
//				int used = maxCol - col;
//				if (used < 8) {
//					// mask bits
//					endPixelByte = (byte) ((endPixelByte & (0xFF >> (8 - used))) | (dstPixelByte & (0xFF << (used))));
//				}
//				setPixelByte(col, row, endPixelByte);
//			}
//
//			srcCol = 0;
//			srcRow++;

        for (int row = y; row < y + srcHeight; row++) {
            for (int col = x; col < x + srcWidth; col++) {

                int srcPixel = bitmap.getPixel(srcCol, srcRow);

                switch ((int) combOp) {
                    case 0: // or
                        setPixel(col, row, getPixel(col, row) | srcPixel);
                        break;
                    case 1: // and
                        setPixel(col, row, getPixel(col, row) & srcPixel);
                        break;
                    case 2: // xor
                        setPixel(col, row, getPixel(col, row) ^ srcPixel);
                        break;
                    case 3: // xnor
                        if ((getPixel(col, row) == 1 && srcPixel == 1) || (getPixel(col, row) == 0 && srcPixel == 0))
                            setPixel(col, row, 1);
                        else
                            setPixel(col, row, 0);

                        break;
                    case 4: // replace
                        setPixel(col, row, srcPixel);
                        break;
                }
                srcCol++;
            }

            srcCol = 0;
            srcRow++;
        }
    }

    /**
     * set a full byte of pixels
     */
//	private void setPixelByte(int col, int row, byte bits) {
    //data.setByte(row, col, bits);
//	}

    /**
     * get a byte of pixels
     */
//	public byte getPixelByte(int col, int row) {
    //return data.getByte(row, col);
//	}
    private void duplicateRow(int yDest, int ySrc) {
//		for (int i = 0; i < width;) {
//			setPixelByte(i, yDest, getPixelByte(i, ySrc));
//			i += 8;
//		}
        for (int i = 0; i < width; i++) {
            setPixel(i, yDest, getPixel(i, ySrc));
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public byte[] getData(boolean switchPixelColor) {
//		byte[] bytes = new byte[height * line];
//
//		for (int i = 0; i < height; i++) {
//			System.arraycopy(data.bytes[i], 0, bytes, line * i, line);
//		}
//
//		for (int i = 0; i < bytes.length; i++) {
//			// reverse bits
//
//			int value = bytes[i];
//			value = (value & 0x0f) << 4 | (value & 0xf0) >> 4;
//			value = (value & 0x33) << 2 | (value & 0xcc) >> 2;
//			value = (value & 0x55) << 1 | (value & 0xaa) >> 1;
//
//			if (switchPixelColor) {
//				value ^= 0xff;
//			}
//
//			bytes[i] = (byte) (value & 0xFF);
//		}
//
//		return bytes;
        byte[] bytes = new byte[height * line];
        getData(bytes, switchPixelColor);
        return bytes;
    }

    public void getData(byte[] bytes, boolean switchPixelColor) {
        int count = 0, offset = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (data.get(count)) {
                    int bite = (count + offset) / 8;
                    int bit = (count + offset) % 8;

                    bytes[bite] |= 1 << (7 - bit);
                }
                count++;
            }

            offset = (line * 8 * (row + 1)) - count;
        }

        if (switchPixelColor) {
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] ^= 0xff;
            }
        }
    }

    public JBIG2Bitmap getSlice(int x, int y, int width, int height) {
//		JBIG2Bitmap slice = new JBIG2Bitmap(width, height);
//
//		int sliceRow = 0, sliceCol = 0;
//		int maxCol = x + width;
//		
//		//ShowGUIMessage.showGUIMessage("x", this.getBufferedImage(), "xx");
//		
//		System.out.println(">>> getSlice x = "+x+" y = "+y+ " width = "+width+ " height = "+height);
//		System.out.println(">>> baseImage width = "+this.width+ " height = "+this.height);
//		
//		System.out.println("counter = "+counter);
//		if(counter == 17){
//			System.out.println();
//			//ShowGUIMessage.showGUIMessage("x", this.getBufferedImage(), "xx");
//		}
//		
//		ShowGUIMessage.showGUIMessage("x", this.getBufferedImage(), "xx");
//		
//		for (int row = y; row < height; row++) {
//			for (int col = x; col < maxCol; col += 8, sliceCol += 8) {
//				slice.setPixelByte(sliceCol, sliceRow, getPixelByte(col, row));
//				//if(counter > 10)
//					//ShowGUIMessage.showGUIMessage("new", slice.getBufferedImage(), "new");
//			}
//			sliceCol = 0;
//			sliceRow++;
//		}
//		counter++;
//
//		ShowGUIMessage.showGUIMessage("new", slice.getBufferedImage(), "new");
//		
//		return slice;

        JBIG2Bitmap slice = new JBIG2Bitmap(width, height, arithmeticDecoder, huffmanDecoder, mmrDecoder);

        int sliceRow = 0, sliceCol = 0;
        for (int row = y; row < height; row++) {
            for (int col = x; col < x + width; col++) {
                //System.out.println("row = "+row +" column = "+col);
                slice.setPixel(sliceCol, sliceRow, getPixel(col, row));
                sliceCol++;
            }
            sliceCol = 0;
            sliceRow++;
        }

        return slice;
    }

    private void setPixel(int col, int row, FastBitSet data, int value) {
        if (value == 1)
            data.set(row, col);
        else
            data.clear(row, col);
    }

//	private void setPixelByte(int col, int row, FastBitSet data, byte bits) {
//		data.setByte(row, col, bits);
//	}

//	public void setPixel(int col, int row, int value) {
//		setPixel(col, row, data, value);
//	}

//	public int getPixel(int col, int row) {
//		return data.get(row, col) ? 1 : 0;
//	}

    private void setPixel(int col, int row, BitSet data, int value) {
        int index = (row * width) + col;

        data.set(index, value == 1);
    }

    public void setPixel(int col, int row, int value) {
        setPixel(col, row, data, value);
    }

    public int getPixel(int col, int row) {
        // compensate for PDF-675
        if (row < 0) {
            row = 0;
        }
        return data.get((row * width) + col) ? 1 : 0;
    }

    public void expand(int newHeight, int defaultPixel) {
//		System.out.println("expand FastBitSet");
//		FastBitSet newData = new FastBitSet(width, newHeight);
//
//		for (int row = 0; row < height; row++) {
//			for (int col = 0; col < width; col += 8) {
//				setPixelByte(col, row, newData, getPixelByte(col, row));
//			}
//		}
//
//		this.height = newHeight;
//		this.data = newData;
        BitSet newData = new BitSet(newHeight * width);

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                setPixel(col, row, newData, getPixel(col, row));
            }
        }

        this.height = newHeight;
        this.data = newData;
    }

    public void setBitmapNumber(int segmentNumber) {
        this.bitmapNumber = segmentNumber;
    }

    public int getBitmapNumber() {
        return bitmapNumber;
    }

    public BufferedImage getBufferedImage() {
        byte[] bytes = getData(true);

        if (bytes == null)
            return null;

        // make a a DEEP copy so we can't alter
        int len = bytes.length;
        byte[] copy = new byte[len];
        System.arraycopy(bytes, 0, copy, 0, len);

        /** create an image from the raw data */
        DataBuffer db = new DataBufferByte(copy, copy.length);

        WritableRaster raster = Raster.createPackedRaster(db, width, height, 1, null);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        image.setData(raster);

        return image;
    }

    static final class FastBitSet {
        byte[][] bytes;

        int w, h;

        public FastBitSet(int width, int height) {
            bytes = new byte[height][(width + 7) / 8];
            this.w = width;
            this.h = height;
            //System.out.println("width = "+width+" height = "+height);
        }

//		public int getByte(int row, int col) {
//			
//			System.out.println("(width + 7) / 8 = " + (width + 7) / 8);
//			System.out.println("external width = " + width + " external height = " + height);
//			System.out.println("internal width = " + w + " internal height = " + h);
//			System.out.println("row = " + row + " column = " + col);
//			
//			int offset = col / 8;
//			int mod = col % 8;
//
//			System.out.println("offset = " + offset + " mod = " + mod+" bytes[row].length = "+bytes[row].length);
//			
//			if (mod == 0)
//				return bytes[row][offset];
//
//			if(offset == bytes[row].length - 1){
//				System.out.println("returning");
//				return ((bytes[row][offset] & 0xFF) >> mod);
//			}
//			
//			int left = ((bytes[row][offset] & 0xFF) >> mod);
//			int right = ((bytes[row][offset + 1] & 0xFF) << (8 - mod));
//			
//			return left | right;
//		}

//		public void setByte(int row, int col, int bits) {
//			int offset = col / 8;
//			int mod = col % 8;
//
//			System.out.println("setByte offset = " + offset + " mod = " + mod);
//			
//			
//			if (mod == 0)
//				bytes[row][offset] = (byte) bits;
//			else {
//				int mask = 0xFF >> (8 - mod);
//				System.out.println("setByte mask = " + mask);
//				bytes[row][offset] = (byte) ((bytes[row][offset] & mask) | ((bits & 0xFF) << mod));
//				bytes[row][offset + 1] = (byte) ((bytes[row][offset + 1] & ~mask) | ((bits & 0xFF) >> (8 - mod)));
//			}
//		}

        public byte getByte(int row, int col) {
//			System.out.println("(width + 7) / 8 = " + (width + 7) / 8);
//			System.out.println("external width = " + width + " external height = " + height);
//			System.out.println("internal width = " + w + " internal height = " + h);
//			System.out.println("row = " + row + " column = " + col);

            int offset = col / 8;
            int mod = col % 8;

//			System.out.println("offset = " + offset + " mod = " + mod+" bytes[row].length = "+bytes[row].length);

            if (mod == 0)
                return bytes[row][offset];

//			if(offset == bytes[row].length - 1){
//				System.out.println("returning");
//				return ((bytes[row][offset] & 0xFF) >> mod);
//			}

            byte leftMask = (byte) (0xFF >> (8 - mod));
            byte rightMask = (byte) (0xFF << mod);

            byte left = (byte) ((bytes[row][offset] & leftMask) << (8 - mod));

            if (offset + 1 >= bytes[row].length) {
                System.out.println("returning");
                return left;
            }

            byte right = (byte) ((bytes[row][offset + 1] & rightMask) >> mod);

            return (byte) (left | right);
        }

        public void setByte(int row, int col, byte bits) {
            int offset = col / 8;
            int mod = col % 8;

            //System.out.println("setByte offset = " + offset + " mod = " + mod);


            if (mod == 0)
                bytes[row][offset] = bits;
            else {

                byte left = (byte) (bits >> mod);
                byte leftMask = (byte) (0xFF << (8 - mod));

                bytes[row][offset] &= leftMask;
                bytes[row][offset] |= left;

                if (offset + 1 >= bytes[row].length)
                    return;

                byte right = (byte) (bits << (8 - mod));
                byte rightMask = (byte) (0xFF >> mod);

                bytes[row][offset + 1] &= rightMask;
                bytes[row][offset + 1] |= right;

//				int mask = 0xFF >> (8 - mod);
//				System.out.println("setByte mask = " + mask);
//				bytes[row][offset] = (byte) ((bytes[row][offset] & mask) | ((bits & 0xFF) << mod));
//				bytes[row][offset + 1] = (byte) ((bytes[row][offset + 1] & ~mask) | ((bits & 0xFF) >> (8 - mod)));
            }
        }

        public void set(int row, int col) {
            byte bit = (byte) (1 << (col % 8));
            bytes[row][col / 8] |= bit;
        }

        public void clear(int row, int col) {
            byte bit = (byte) (1 << (col % 8));
            bytes[row][col / 8] &= ~bit;
        }

        public boolean get(int row, int col) {
            byte bit = (byte) (1 << (col % 8));
            return (bytes[row][col / 8] & bit) != 0;
        }

        public void reset(boolean set) {
            for (byte[] aByte : bytes)
                Arrays.fill(aByte, set ? (byte) 0xFF : (byte) 0x00);
        }
    }
}
