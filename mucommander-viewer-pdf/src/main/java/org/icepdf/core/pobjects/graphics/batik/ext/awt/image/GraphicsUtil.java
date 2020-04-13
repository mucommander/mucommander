/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.icepdf.core.pobjects.graphics.batik.ext.awt.image;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.*;


/**
 * Set of utility methods for Graphics.
 * These generally bypass broken methods in Java2D or provide tweaked
 * implementations.
 *
 * @author <a href="mailto:Thomas.DeWeeese@Kodak.com">Thomas DeWeese</a>
 * @version $Id: GraphicsUtil.java,v 1.1 2008/09/30 20:44:16 patrickc Exp $
 */
public class GraphicsUtil {

    public static AffineTransform IDENTITY = new AffineTransform();

    /**
     * Standard prebuilt Linear_sRGB color model with no alpha
     */
    public static final ColorModel Linear_sRGB =
            new DirectColorModel(ColorSpace.getInstance
                    (ColorSpace.CS_LINEAR_RGB), 24,
                    0x00FF0000, 0x0000FF00,
                    0x000000FF, 0x0, false,
                    DataBuffer.TYPE_INT);
    /**
     * Standard prebuilt Linear_sRGB color model with premultiplied alpha.
     */
    public static final ColorModel Linear_sRGB_Pre =
            new DirectColorModel(ColorSpace.getInstance
                    (ColorSpace.CS_LINEAR_RGB), 32,
                    0x00FF0000, 0x0000FF00,
                    0x000000FF, 0xFF000000, true,
                    DataBuffer.TYPE_INT);
    /**
     * Standard prebuilt Linear_sRGB color model with unpremultiplied alpha.
     */
    public static final ColorModel Linear_sRGB_Unpre =
            new DirectColorModel(ColorSpace.getInstance
                    (ColorSpace.CS_LINEAR_RGB), 32,
                    0x00FF0000, 0x0000FF00,
                    0x000000FF, 0xFF000000, false,
                    DataBuffer.TYPE_INT);
    /**
     * Standard prebuilt sRGB color model with no alpha.
     */
    public static final ColorModel sRGB =
            new DirectColorModel(ColorSpace.getInstance
                    (ColorSpace.CS_sRGB), 24,
                    0x00FF0000, 0x0000FF00,
                    0x000000FF, 0x0, false,
                    DataBuffer.TYPE_INT);
    /**
     * Standard prebuilt sRGB color model with premultiplied alpha.
     */
    public static final ColorModel sRGB_Pre =
            new DirectColorModel(ColorSpace.getInstance
                    (ColorSpace.CS_sRGB), 32,
                    0x00FF0000, 0x0000FF00,
                    0x000000FF, 0xFF000000, true,
                    DataBuffer.TYPE_INT);
    /**
     * Standard prebuilt sRGB color model with unpremultiplied alpha.
     */
    public static final ColorModel sRGB_Unpre =
            new DirectColorModel(ColorSpace.getInstance
                    (ColorSpace.CS_sRGB), 32,
                    0x00FF0000, 0x0000FF00,
                    0x000000FF, 0xFF000000, false,
                    DataBuffer.TYPE_INT);

    /**
     * Method that returns either Linear_sRGB_Pre or Linear_sRGB_UnPre
     * based on premult flag.
     *
     * @param premult True if the ColorModel should have premultiplied alpha.
     * @return a ColorMdoel with Linear sRGB colorSpace and
     *         the alpha channel set in accordance with
     *         <tt>premult</tt>
     */
    public static ColorModel makeLinear_sRGBCM(boolean premult) {

        return premult ? Linear_sRGB_Pre : Linear_sRGB_Unpre;
    }

    /**
     * Constructs a BufferedImage with a linear sRGB colorModel, and alpha.
     *
     * @param width   The desired width of the BufferedImage
     * @param height  The desired height of the BufferedImage
     * @param premult The desired state of alpha premultiplied
     * @return The requested BufferedImage.
     */
    public static BufferedImage makeLinearBufferedImage(int width,
                                                        int height,
                                                        boolean premult) {
        ColorModel cm = makeLinear_sRGBCM(premult);
        WritableRaster wr = cm.createCompatibleWritableRaster(width, height);
        return new BufferedImage(cm, wr, premult, null);
    }

    /**
     * An internal optimized version of copyData designed to work on
     * Integer packed data with a SinglePixelPackedSampleModel.  Only
     * the region of overlap between src and dst is copied.
     * <p/>
     * Calls to this should be preflighted with is_INT_PACK_Data
     * on both src and dest (requireAlpha can be false).
     *
     * @param src The source of the data
     * @param dst The destination for the data.
     */
    public static void copyData_INT_PACK(Raster src, WritableRaster dst) {
        // System.out.println("Fast copyData");
        int x0 = dst.getMinX();
        if (x0 < src.getMinX()) x0 = src.getMinX();

        int y0 = dst.getMinY();
        if (y0 < src.getMinY()) y0 = src.getMinY();

        int x1 = dst.getMinX() + dst.getWidth() - 1;
        if (x1 > src.getMinX() + src.getWidth() - 1)
            x1 = src.getMinX() + src.getWidth() - 1;

        int y1 = dst.getMinY() + dst.getHeight() - 1;
        if (y1 > src.getMinY() + src.getHeight() - 1)
            y1 = src.getMinY() + src.getHeight() - 1;

        int width = x1 - x0 + 1;
        int height = y1 - y0 + 1;

        SinglePixelPackedSampleModel srcSPPSM;
        srcSPPSM = (SinglePixelPackedSampleModel) src.getSampleModel();

        final int srcScanStride = srcSPPSM.getScanlineStride();
        DataBufferInt srcDB = (DataBufferInt) src.getDataBuffer();
        final int[] srcPixels = srcDB.getBankData()[0];
        final int srcBase =
                (srcDB.getOffset() +
                        srcSPPSM.getOffset(x0 - src.getSampleModelTranslateX(),
                                y0 - src.getSampleModelTranslateY()));


        SinglePixelPackedSampleModel dstSPPSM;
        dstSPPSM = (SinglePixelPackedSampleModel) dst.getSampleModel();

        final int dstScanStride = dstSPPSM.getScanlineStride();
        DataBufferInt dstDB = (DataBufferInt) dst.getDataBuffer();
        final int[] dstPixels = dstDB.getBankData()[0];
        final int dstBase =
                (dstDB.getOffset() +
                        dstSPPSM.getOffset(x0 - dst.getSampleModelTranslateX(),
                                y0 - dst.getSampleModelTranslateY()));

        if ((srcScanStride == dstScanStride) &&
                (srcScanStride == width)) {
            // System.out.println("VERY Fast copyData");

            System.arraycopy(srcPixels, srcBase, dstPixels, dstBase,
                    width * height);
        } else if (width > 128) {
            int srcSP = srcBase;
            int dstSP = dstBase;
            for (int y = 0; y < height; y++) {
                System.arraycopy(srcPixels, srcSP, dstPixels, dstSP, width);
                srcSP += srcScanStride;
                dstSP += dstScanStride;
            }
        } else {
            for (int y = 0; y < height; y++) {
                int srcSP = srcBase + y * srcScanStride;
                int dstSP = dstBase + y * dstScanStride;
                for (int x = 0; x < width; x++)
                    dstPixels[dstSP++] = srcPixels[srcSP++];
            }
        }
    }

    public static void copyData_FALLBACK(Raster src, WritableRaster dst) {
        // System.out.println("Fallback copyData");

        int x0 = dst.getMinX();
        if (x0 < src.getMinX()) x0 = src.getMinX();

        int y0 = dst.getMinY();
        if (y0 < src.getMinY()) y0 = src.getMinY();

        int x1 = dst.getMinX() + dst.getWidth() - 1;
        if (x1 > src.getMinX() + src.getWidth() - 1)
            x1 = src.getMinX() + src.getWidth() - 1;

        int y1 = dst.getMinY() + dst.getHeight() - 1;
        if (y1 > src.getMinY() + src.getHeight() - 1)
            y1 = src.getMinY() + src.getHeight() - 1;

        int width = x1 - x0 + 1;
        int[] data = null;

        for (int y = y0; y <= y1; y++) {
            data = src.getPixels(x0, y, width, 1, data);
            dst.setPixels(x0, y, width, 1, data);
        }
    }

    /**
     * Copies data from one raster to another. Only the region of
     * overlap between src and dst is copied.  <tt>Src</tt> and
     * <tt>Dst</tt> must have compatible SampleModels.
     *
     * @param src The source of the data
     * @param dst The destination for the data.
     */
    public static void copyData(Raster src, WritableRaster dst) {
        if (is_INT_PACK_Data(src.getSampleModel(), false) &&
                is_INT_PACK_Data(dst.getSampleModel(), false)) {
            copyData_INT_PACK(src, dst);
            return;
        }

        copyData_FALLBACK(src, dst);
    }

    /**
     * Creates a new raster that has a <b>copy</b> of the data in
     * <tt>ras</tt>.  This is highly optimized for speed.  There is
     * no provision for changing any aspect of the SampleModel.
     * <p/>
     * This method should be used when you need to change the contents
     * of a Raster that you do not "own" (ie the result of a
     * <tt>getData</tt> call).
     *
     * @param ras The Raster to copy.
     * @return A writable copy of <tt>ras</tt>
     */
    public static WritableRaster copyRaster(Raster ras) {
        return copyRaster(ras, ras.getMinX(), ras.getMinY());
    }


    /**
     * Creates a new raster that has a <b>copy</b> of the data in
     * <tt>ras</tt>.  This is highly optimized for speed.  There is
     * no provision for changing any aspect of the SampleModel.
     * However you can specify a new location for the returned raster.
     * <p/>
     * This method should be used when you need to change the contents
     * of a Raster that you do not "own" (ie the result of a
     * <tt>getData</tt> call).
     *
     * @param ras  The Raster to copy.
     * @param minX The x location for the upper left corner of the
     *             returned WritableRaster.
     * @param minY The y location for the upper left corner of the
     *             returned WritableRaster.
     * @return A writable copy of <tt>ras</tt>
     */
    public static WritableRaster copyRaster(Raster ras, int minX, int minY) {
        WritableRaster ret = Raster.createWritableRaster
                (ras.getSampleModel(),
                        new Point(0, 0));
        ret = ret.createWritableChild
                (ras.getMinX() - ras.getSampleModelTranslateX(),
                        ras.getMinY() - ras.getSampleModelTranslateY(),
                        ras.getWidth(), ras.getHeight(),
                        minX, minY, null);

        // Use System.arraycopy to copy the data between the two...
        DataBuffer srcDB = ras.getDataBuffer();
        DataBuffer retDB = ret.getDataBuffer();
        if (srcDB.getDataType() != retDB.getDataType()) {
            throw new IllegalArgumentException
                    ("New DataBuffer doesn't match original");
        }
        int len = srcDB.getSize();
        int banks = srcDB.getNumBanks();
        int[] offsets = srcDB.getOffsets();
        for (int b = 0; b < banks; b++) {
            switch (srcDB.getDataType()) {
                case DataBuffer.TYPE_BYTE: {
                    DataBufferByte srcDBT = (DataBufferByte) srcDB;
                    DataBufferByte retDBT = (DataBufferByte) retDB;
                    System.arraycopy(srcDBT.getData(b), offsets[b],
                            retDBT.getData(b), offsets[b], len);
                    break;
                }
                case DataBuffer.TYPE_INT: {
                    DataBufferInt srcDBT = (DataBufferInt) srcDB;
                    DataBufferInt retDBT = (DataBufferInt) retDB;
                    System.arraycopy(srcDBT.getData(b), offsets[b],
                            retDBT.getData(b), offsets[b], len);
                    break;
                }
                case DataBuffer.TYPE_SHORT: {
                    DataBufferShort srcDBT = (DataBufferShort) srcDB;
                    DataBufferShort retDBT = (DataBufferShort) retDB;
                    System.arraycopy(srcDBT.getData(b), offsets[b],
                            retDBT.getData(b), offsets[b], len);
                    break;
                }
                case DataBuffer.TYPE_USHORT: {
                    DataBufferUShort srcDBT = (DataBufferUShort) srcDB;
                    DataBufferUShort retDBT = (DataBufferUShort) retDB;
                    System.arraycopy(srcDBT.getData(b), offsets[b],
                            retDBT.getData(b), offsets[b], len);
                    break;
                }
            }
        }

        return ret;
    }

    /**
     * Coerces <tt>ras</tt> to be writable.  The returned Raster continues to
     * reference the DataBuffer from ras, so modifications to the returned
     * WritableRaster will be seen in ras.<p>
     * <p/>
     * This method should only be used if you need a WritableRaster due to
     * an interface (such as to construct a BufferedImage), but have no
     * intention of modifying the contents of the returned Raster.  If
     * you have any doubt about other users of the data in <tt>ras</tt>,
     * use copyRaster (above).
     *
     * @param ras The raster to make writable.
     * @return A Writable version of ras (shares DataBuffer with
     *         <tt>ras</tt>).
     */
    public static WritableRaster makeRasterWritable(Raster ras) {
        return makeRasterWritable(ras, ras.getMinX(), ras.getMinY());
    }

    /**
     * Coerces <tt>ras</tt> to be writable.  The returned Raster continues to
     * reference the DataBuffer from ras, so modifications to the returned
     * WritableRaster will be seen in ras.<p>
     * <p/>
     * You can specify a new location for the returned WritableRaster, this
     * is especially useful for constructing BufferedImages which require
     * the Raster to be at (0,0).
     * <p/>
     * This method should only be used if you need a WritableRaster due to
     * an interface (such as to construct a BufferedImage), but have no
     * intention of modifying the contents of the returned Raster.  If
     * you have any doubt about other users of the data in <tt>ras</tt>,
     * use copyRaster (above).
     *
     * @param ras  The raster to make writable.
     * @param minX The x location for the upper left corner of the
     *             returned WritableRaster.
     * @param minY The y location for the upper left corner of the
     *             returned WritableRaster.
     * @return A Writable version of <tT>ras</tt> with it's upper left
     *         hand coordinate set to minX, minY (shares it's DataBuffer
     *         with <tt>ras</tt>).
     */
    public static WritableRaster makeRasterWritable(Raster ras,
                                                    int minX, int minY) {
        WritableRaster ret = Raster.createWritableRaster
                (ras.getSampleModel(),
                        ras.getDataBuffer(),
                        new Point(0, 0));
        ret = ret.createWritableChild
                (ras.getMinX() - ras.getSampleModelTranslateX(),
                        ras.getMinY() - ras.getSampleModelTranslateY(),
                        ras.getWidth(), ras.getHeight(),
                        minX, minY, null);
        return ret;
    }

    /**
     * Create a new ColorModel with it's alpha premultiplied state matching
     * newAlphaPreMult.
     *
     * @param cm              The ColorModel to change the alpha premult state of.
     * @param newAlphaPreMult The new state of alpha premult.
     * @return A new colorModel that has isAlphaPremultiplied()
     *         equal to newAlphaPreMult.
     */
    public static ColorModel
    coerceColorModel(ColorModel cm, boolean newAlphaPreMult) {
        if (cm.isAlphaPremultiplied() == newAlphaPreMult)
            return cm;

        // Easiest way to build proper colormodel for new Alpha state...
        // Eventually this should switch on known ColorModel types and
        // only fall back on this hack when the CM type is unknown.
        WritableRaster wr = cm.createCompatibleWritableRaster(1, 1);
        return cm.coerceData(wr, newAlphaPreMult);
    }

    /**
     * Coerces data within a bufferedImage to match newAlphaPreMult,
     * Note that this can not change the colormodel of bi so you
     *
     * @param wr              The raster to change the state of.
     * @param cm              The colormodel currently associated with data in wr.
     * @param newAlphaPreMult The desired state of alpha Premult for raster.
     * @return A new colormodel that matches newAlphaPreMult.
     */
    public static ColorModel
    coerceData(WritableRaster wr, ColorModel cm, boolean newAlphaPreMult) {

        // System.out.println("CoerceData: " + cm.isAlphaPremultiplied() +
        //                    " Out: " + newAlphaPreMult);
        if (!cm.hasAlpha())
            // Nothing to do no alpha channel
            return cm;

        if (cm.isAlphaPremultiplied() == newAlphaPreMult)
            // nothing to do alpha state matches...
            return cm;

        // System.out.println("CoerceData: " + wr.getSampleModel());

        if (newAlphaPreMult) {
            multiplyAlpha(wr);
        } else {
            divideAlpha(wr);
        }

        return coerceColorModel(cm, newAlphaPreMult);
    }

    public static void multiplyAlpha(WritableRaster wr) {
        if (is_BYTE_COMP_Data(wr.getSampleModel()))
            mult_BYTE_COMP_Data(wr);
        else if (is_INT_PACK_Data(wr.getSampleModel(), true))
            mult_INT_PACK_Data(wr);
        else {
            int[] pixel = null;
            int bands = wr.getNumBands();
            float norm = 1.0f / 255f;
            int x0, x1, y0, y1, a, b;
            float alpha;
            x0 = wr.getMinX();
            x1 = x0 + wr.getWidth();
            y0 = wr.getMinY();
            y1 = y0 + wr.getHeight();
            for (int y = y0; y < y1; y++)
                for (int x = x0; x < x1; x++) {
                    pixel = wr.getPixel(x, y, pixel);
                    a = pixel[bands - 1];
                    if ((a >= 0) && (a < 255)) {
                        alpha = a * norm;
                        for (b = 0; b < bands - 1; b++)
                            pixel[b] = (int) (pixel[b] * alpha + 0.5f);
                        wr.setPixel(x, y, pixel);
                    }
                }
        }
    }

    public static void divideAlpha(WritableRaster wr) {
        if (is_BYTE_COMP_Data(wr.getSampleModel()))
            divide_BYTE_COMP_Data(wr);
        else if (is_INT_PACK_Data(wr.getSampleModel(), true))
            divide_INT_PACK_Data(wr);
        else {
            int x0, x1, y0, y1, a, b;
            float ialpha;
            int bands = wr.getNumBands();
            int[] pixel = null;

            x0 = wr.getMinX();
            x1 = x0 + wr.getWidth();
            y0 = wr.getMinY();
            y1 = y0 + wr.getHeight();
            for (int y = y0; y < y1; y++)
                for (int x = x0; x < x1; x++) {
                    pixel = wr.getPixel(x, y, pixel);
                    a = pixel[bands - 1];
                    if ((a > 0) && (a < 255)) {
                        ialpha = 255 / (float) a;
                        for (b = 0; b < bands - 1; b++)
                            pixel[b] = (int) (pixel[b] * ialpha + 0.5f);
                        wr.setPixel(x, y, pixel);
                    }
                }
        }
    }

    /**
     * Copies data from one bufferedImage to another paying attention
     * to the state of AlphaPreMultiplied.
     *
     * @param src The source
     * @param dst The destination
     */
    public static void
    copyData(BufferedImage src, BufferedImage dst) {
        Rectangle srcRect = new Rectangle(0, 0,
                src.getWidth(), src.getHeight());
        copyData(src, srcRect, dst, new Point(0, 0));
    }


    /**
     * Copies data from one bufferedImage to another paying attention
     * to the state of AlphaPreMultiplied.
     *
     * @param src     The source
     * @param srcRect The Rectangle of source data to be copied
     * @param dst     The destination
     * @param destP   The Place for the upper left corner of srcRect in dst.
     */
    public static void
    copyData(BufferedImage src, Rectangle srcRect,
             BufferedImage dst, Point destP) {

        /*
        if (srcCS != dstCS)
            throw new IllegalArgumentException
                ("Images must be in the same ColorSpace in order "+
                 "to copy Data between them");
        */
        boolean srcAlpha = src.getColorModel().hasAlpha();
        boolean dstAlpha = dst.getColorModel().hasAlpha();

        // System.out.println("Src has: " + srcAlpha +
        //                    " is: " + src.isAlphaPremultiplied());
        //
        // System.out.println("Dst has: " + dstAlpha +
        //                    " is: " + dst.isAlphaPremultiplied());

        if (srcAlpha == dstAlpha)
            if ((!srcAlpha) ||
                    (src.isAlphaPremultiplied() == dst.isAlphaPremultiplied())) {
                // They match one another so just copy everything...
                copyData(src.getRaster(), dst.getRaster());
                return;
            }

        // System.out.println("Using Slow CopyData");

        int[] pixel = null;
        Raster srcR = src.getRaster();
        WritableRaster dstR = dst.getRaster();
        int bands = dstR.getNumBands();

        int dx = destP.x - srcRect.x;
        int dy = destP.y - srcRect.y;

        int w = srcRect.width;
        int x0 = srcRect.x;
        int y0 = srcRect.y;
        int y1 = y0 + srcRect.height - 1;

        if (!srcAlpha) {
            // Src has no alpha dest does so set alpha to 1.0 everywhere.
            // System.out.println("Add Alpha");
            int[] oPix = new int[bands * w];
            int out = (w * bands) - 1; // The 2 skips alpha channel
            while (out >= 0) {
                // Fill alpha channel with 255's
                oPix[out] = 255;
                out -= bands;
            }

            int b, in;
            for (int y = y0; y <= y1; y++) {
                pixel = srcR.getPixels(x0, y, w, 1, pixel);
                in = w * (bands - 1) - 1;
                out = (w * bands) - 2; // The 2 skips alpha channel on last pix
                switch (bands) {
                    case 4:
                        while (in >= 0) {
                            oPix[out--] = pixel[in--];
                            oPix[out--] = pixel[in--];
                            oPix[out--] = pixel[in--];
                            out--;
                        }
                        break;
                    default:
                        while (in >= 0) {
                            for (b = 0; b < bands - 1; b++)
                                oPix[out--] = pixel[in--];
                            out--;
                        }
                }
                dstR.setPixels(x0 + dx, y + dy, w, 1, oPix);
            }
        } else if (dstAlpha && dst.isAlphaPremultiplied()) {
            // Src and dest have Alpha but we need to multiply it for dst.
            // System.out.println("Mult Case");
            int a, b, alpha, in, fpNorm = (1 << 24) / 255, pt5 = 1 << 23;
            for (int y = y0; y <= y1; y++) {
                pixel = srcR.getPixels(x0, y, w, 1, pixel);
                in = bands * w - 1;
                switch (bands) {
                    case 4:
                        while (in >= 0) {
                            a = pixel[in];
                            if (a == 255)
                                in -= 4;
                            else {
                                in--;
                                alpha = fpNorm * a;
                                pixel[in] = (pixel[in] * alpha + pt5) >>> 24;
                                in--;
                                pixel[in] = (pixel[in] * alpha + pt5) >>> 24;
                                in--;
                                pixel[in] = (pixel[in] * alpha + pt5) >>> 24;
                                in--;
                            }
                        }
                        break;
                    default:
                        while (in >= 0) {
                            a = pixel[in];
                            if (a == 255)
                                in -= bands;
                            else {
                                in--;
                                alpha = fpNorm * a;
                                for (b = 0; b < bands - 1; b++) {
                                    pixel[in] = (pixel[in] * alpha + pt5) >>> 24;
                                    in--;
                                }
                            }
                        }
                }
                dstR.setPixels(x0 + dx, y + dy, w, 1, pixel);
            }
        } else if (dstAlpha && !dst.isAlphaPremultiplied()) {
            // Src and dest have Alpha but we need to divide it out for dst.
            // System.out.println("Div Case");
            int a, b, ialpha, in, fpNorm = 0x00FF0000, pt5 = 1 << 15;
            for (int y = y0; y <= y1; y++) {
                pixel = srcR.getPixels(x0, y, w, 1, pixel);
                in = (bands * w) - 1;
                switch (bands) {
                    case 4:
                        while (in >= 0) {
                            a = pixel[in];
                            if ((a <= 0) || (a >= 255))
                                in -= 4;
                            else {
                                in--;
                                ialpha = fpNorm / a;
                                pixel[in] = (pixel[in] * ialpha + pt5) >>> 16;
                                in--;
                                pixel[in] = (pixel[in] * ialpha + pt5) >>> 16;
                                in--;
                                pixel[in] = (pixel[in] * ialpha + pt5) >>> 16;
                                in--;
                            }
                        }
                        break;
                    default:
                        while (in >= 0) {
                            a = pixel[in];
                            if ((a <= 0) || (a >= 255))
                                in -= bands;
                            else {
                                in--;
                                ialpha = fpNorm / a;
                                for (b = 0; b < bands - 1; b++) {
                                    pixel[in] = (pixel[in] * ialpha + pt5) >>> 16;
                                    in--;
                                }
                            }
                        }
                }
                dstR.setPixels(x0 + dx, y + dy, w, 1, pixel);
            }
        } else if (src.isAlphaPremultiplied()) {
            int[] oPix = new int[bands * w];
            // Src has alpha dest does not so unpremult and store...
            // System.out.println("Remove Alpha, Div Case");
            int a, b, ialpha, in, out, fpNorm = 0x00FF0000, pt5 = 1 << 15;
            for (int y = y0; y <= y1; y++) {
                pixel = srcR.getPixels(x0, y, w, 1, pixel);
                in = (bands + 1) * w - 1;
                out = (bands * w) - 1;
                while (in >= 0) {
                    a = pixel[in];
                    in--;
                    if (a > 0) {
                        if (a < 255) {
                            ialpha = fpNorm / a;
                            for (b = 0; b < bands; b++)
                                oPix[out--] = (pixel[in--] * ialpha + pt5) >>> 16;
                        } else
                            for (b = 0; b < bands; b++)
                                oPix[out--] = pixel[in--];
                    } else {
                        in -= bands;
                        for (b = 0; b < bands; b++)
                            oPix[out--] = 255;
                    }
                }
                dstR.setPixels(x0 + dx, y + dy, w, 1, oPix);
            }
        } else {
            // Src has unpremult alpha, dest does not have alpha,
            // just copy the color channels over.
            Rectangle dstRect = new Rectangle(destP.x, destP.y,
                    srcRect.width, srcRect.height);
            for (int b = 0; b < bands; b++)
                copyBand(srcR, srcRect, b,
                        dstR, dstRect, b);
        }
    }

    public static void copyBand(Raster src, int srcBand,
                                WritableRaster dst, int dstBand) {

        Rectangle sR = src.getBounds();
        Rectangle dR = dst.getBounds();
        Rectangle cpR = sR.intersection(dR);

        copyBand(src, cpR, srcBand, dst, cpR, dstBand);
    }

    public static void copyBand(Raster src, Rectangle sR, int sBand,
                                WritableRaster dst, Rectangle dR, int dBand) {
        int dy = dR.y - sR.y;
        int dx = dR.x - sR.x;
        sR = sR.intersection(src.getBounds());
        dR = dR.intersection(dst.getBounds());
        int width, height;
        if (dR.width < sR.width) width = dR.width;
        else width = sR.width;
        if (dR.height < sR.height) height = dR.height;
        else height = sR.height;

        int x = sR.x + dx;
        int[] samples = null;
        for (int y = sR.y; y < sR.y + height; y++) {
            samples = src.getSamples(sR.x, y, width, 1, sBand, samples);
            dst.setSamples(x, y + dy, width, 1, dBand, samples);
        }
    }

    public static boolean is_INT_PACK_Data(SampleModel sm,
                                           boolean requireAlpha) {
        // Check ColorModel is of type DirectColorModel
        if (!(sm instanceof SinglePixelPackedSampleModel)) return false;

        // Check transfer type
        if (sm.getDataType() != DataBuffer.TYPE_INT) return false;

        SinglePixelPackedSampleModel sppsm;
        sppsm = (SinglePixelPackedSampleModel) sm;

        int[] masks = sppsm.getBitMasks();
        if (masks.length == 3) {
            if (requireAlpha) return false;
        } else if (masks.length != 4)
            return false;

        if (masks[0] != 0x00ff0000) return false;
        if (masks[1] != 0x0000ff00) return false;
        if (masks[2] != 0x000000ff) return false;
        if ((masks.length == 4) &&
                (masks[3] != 0xff000000)) return false;

        return true;
    }

    public static boolean is_BYTE_COMP_Data(SampleModel sm) {
        // Check ColorModel is of type DirectColorModel
        if (!(sm instanceof ComponentSampleModel)) return false;

        // Check transfer type
        if (sm.getDataType() != DataBuffer.TYPE_BYTE) return false;

        return true;
    }

    protected static void divide_INT_PACK_Data(WritableRaster wr) {
        // System.out.println("Divide Int");

        SinglePixelPackedSampleModel sppsm;
        sppsm = (SinglePixelPackedSampleModel) wr.getSampleModel();

        final int width = wr.getWidth();

        final int scanStride = sppsm.getScanlineStride();
        DataBufferInt db = (DataBufferInt) wr.getDataBuffer();
        final int base
                = (db.getOffset() +
                sppsm.getOffset(wr.getMinX() - wr.getSampleModelTranslateX(),
                        wr.getMinY() - wr.getSampleModelTranslateY()));

        // Access the pixel data array
        final int[] pixels = db.getBankData()[0];
        for (int y = 0; y < wr.getHeight(); y++) {
            int sp = base + y * scanStride;
            final int end = sp + width;
            while (sp < end) {
                int pixel = pixels[sp];
                int a = pixel >>> 24;
                if (a <= 0) {
                    pixels[sp] = 0x00FFFFFF;
                } else if (a < 255) {
                    int aFP = (0x00FF0000 / a);
                    pixels[sp] =
                            ((a << 24) |
                                    (((((pixel & 0xFF0000) >> 16) * aFP) & 0xFF0000)) |
                                    (((((pixel & 0x00FF00) >> 8) * aFP) & 0xFF0000) >> 8) |
                                    (((((pixel & 0x0000FF)) * aFP) & 0xFF0000) >> 16));
                }
                sp++;
            }
        }
    }

    protected static void mult_INT_PACK_Data(WritableRaster wr) {
        // System.out.println("Multiply Int: " + wr);

        SinglePixelPackedSampleModel sppsm;
        sppsm = (SinglePixelPackedSampleModel) wr.getSampleModel();

        final int width = wr.getWidth();

        final int scanStride = sppsm.getScanlineStride();
        DataBufferInt db = (DataBufferInt) wr.getDataBuffer();
        final int base
                = (db.getOffset() +
                sppsm.getOffset(wr.getMinX() - wr.getSampleModelTranslateX(),
                        wr.getMinY() - wr.getSampleModelTranslateY()));
        // Access the pixel data array
        final int[] pixels = db.getBankData()[0];
        for (int y = 0; y < wr.getHeight(); y++) {
            int sp = base + y * scanStride;
            final int end = sp + width;
            while (sp < end) {
                int pixel = pixels[sp];
                int a = pixel >>> 24;
                if ((a >= 0) && (a < 255)) {   // this does NOT include a == 255 (0xff) !
                    pixels[sp] = ((a << 24) |
                            ((((pixel & 0xFF0000) * a) >> 8) & 0xFF0000) |
                            ((((pixel & 0x00FF00) * a) >> 8) & 0x00FF00) |
                            ((((pixel & 0x0000FF) * a) >> 8) & 0x0000FF));
                }
                sp++;
            }
        }
    }


    protected static void divide_BYTE_COMP_Data(WritableRaster wr) {
        // System.out.println("Multiply Int: " + wr);

        ComponentSampleModel csm;
        csm = (ComponentSampleModel) wr.getSampleModel();

        final int width = wr.getWidth();

        final int scanStride = csm.getScanlineStride();
        final int pixStride = csm.getPixelStride();
        final int[] bandOff = csm.getBandOffsets();

        DataBufferByte db = (DataBufferByte) wr.getDataBuffer();
        final int base
                = (db.getOffset() +
                csm.getOffset(wr.getMinX() - wr.getSampleModelTranslateX(),
                        wr.getMinY() - wr.getSampleModelTranslateY()));

        int aOff = bandOff[bandOff.length - 1];
        int bands = bandOff.length - 1;

        // Access the pixel data array
        final byte[] pixels = db.getBankData()[0];
        for (int y = 0; y < wr.getHeight(); y++) {
            int sp = base + y * scanStride;
            final int end = sp + width * pixStride;
            while (sp < end) {
                int a = pixels[sp + aOff] & 0xFF;
                if (a == 0) {
                    for (int b = 0; b < bands; b++)
                        pixels[sp + bandOff[b]] = (byte) 0xFF;
                } else if (a < 255) {         // this does NOT include a == 255 (0xff) !
                    int aFP = (0x00FF0000 / a);
                    for (int b = 0; b < bands; b++) {
                        int i = sp + bandOff[b];
                        pixels[i] = (byte) (((pixels[i] & 0xFF) * aFP) >>> 16);
                    }
                }
                sp += pixStride;
            }
        }
    }

    protected static void mult_BYTE_COMP_Data(WritableRaster wr) {
        // System.out.println("Multiply Int: " + wr);

        ComponentSampleModel csm;
        csm = (ComponentSampleModel) wr.getSampleModel();

        final int width = wr.getWidth();

        final int scanStride = csm.getScanlineStride();
        final int pixStride = csm.getPixelStride();
        final int[] bandOff = csm.getBandOffsets();

        DataBufferByte db = (DataBufferByte) wr.getDataBuffer();
        final int base
                = (db.getOffset() +
                csm.getOffset(wr.getMinX() - wr.getSampleModelTranslateX(),
                        wr.getMinY() - wr.getSampleModelTranslateY()));


        int aOff = bandOff[bandOff.length - 1];
        int bands = bandOff.length - 1;

        // Access the pixel data array
        final byte[] pixels = db.getBankData()[0];
        for (int y = 0; y < wr.getHeight(); y++) {
            int sp = base + y * scanStride;
            final int end = sp + width * pixStride;
            while (sp < end) {
                int a = pixels[sp + aOff] & 0xFF;
                if (a != 0xFF)
                    for (int b = 0; b < bands; b++) {
                        int i = sp + bandOff[b];
                        pixels[i] = (byte) (((pixels[i] & 0xFF) * a) >> 8);
                    }
                sp += pixStride;
            }
        }
    }

/*
  This is skanky debugging code that might be useful in the future:

            if (count == 33) {
                String label = "sub [" + x + ", " + y + "]: ";
                org.ImageDisplay.showImage
                    (label, subBI);
                org.ImageDisplay.printImage
                    (label, subBI,
                     new Rectangle(75-iR.x, 90-iR.y, 32, 32));

            }


            // if ((count++ % 50) == 10)
            //     org.ImageDisplay.showImage("foo: ", subBI);


            Graphics2D realG2D = g2d;
            while (realG2D instanceof sun.java2d.ProxyGraphics2D) {
                realG2D = ((sun.java2d.ProxyGraphics2D)realG2D).getDelegate();
            }
            if (realG2D instanceof sun.awt.image.BufferedImageGraphics2D) {
                count++;
                if (count == 34) {
                    RenderedImage ri;
                    ri = ((sun.awt.image.BufferedImageGraphics2D)realG2D).bufImg;
                    // g2d.setComposite(SVGComposite.OVER);
                    // org.ImageDisplay.showImage("Bar: " + count, cr);
                    org.ImageDisplay.printImage("Bar: " + count, cr,
                                                new Rectangle(75, 90, 32, 32));

                    org.ImageDisplay.showImage ("Foo: " + count, ri);
                    org.ImageDisplay.printImage("Foo: " + count, ri,
                                                new Rectangle(75, 90, 32, 32));

                    System.out.println("BI: "   + ri);
                    System.out.println("BISM: " + ri.getSampleModel());
                    System.out.println("BICM: " + ri.getColorModel());
                    System.out.println("BICM class: " + ri.getColorModel().getClass());
                    System.out.println("BICS: " + ri.getColorModel().getColorSpace());
                    System.out.println
                        ("sRGB CS: " +
                         ColorSpace.getInstance(ColorSpace.CS_sRGB));
                    System.out.println("G2D info");
                    System.out.println("\tComposite: " + g2d.getComposite());
                    System.out.println("\tTransform" + g2d.getTransform());
                    java.awt.RenderingHints rh = g2d.getRenderingHints();
                    java.util.Set keys = rh.keySet();
                    java.util.Iterator iter = keys.iterator();
                    while (iter.hasNext()) {
                        Object o = iter.next();

                        System.out.println("\t" + o.toString() + " -> " +
                                           rh.get(o).toString());
                    }

                    ri = cr;
                    System.out.println("RI: "   + ri);
                    System.out.println("RISM: " + ri.getSampleModel());
                    System.out.println("RICM: " + ri.getColorModel());
                    System.out.println("RICM class: " + ri.getColorModel().getClass());
                    System.out.println("RICS: " + ri.getColorModel().getColorSpace());
                }
            }
*/

}
