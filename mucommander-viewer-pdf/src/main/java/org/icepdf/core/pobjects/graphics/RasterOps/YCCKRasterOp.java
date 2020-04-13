package org.icepdf.core.pobjects.graphics.RasterOps;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.RasterOp;
import java.awt.image.WritableRaster;

/**
 * The basic idea is that we do a fuzzy colour conversion from YCCK to
 * CMYK.  The conversion is not perfect but when converted again from
 * CMYK to RGB the result is much better then going directly from YCCK to
 * RGB.
 * NOTE: no masking here, as it is done later in the call to
 * {@see alterRasterCMYK2BGRA}
 *
 * @sine 5.1
 */
public class YCCKRasterOp implements RasterOp {

    private RenderingHints hints = null;

    public YCCKRasterOp(RenderingHints hints) {
        this.hints = hints;
    }

    public WritableRaster filter(Raster src, WritableRaster dest) {

        if (dest == null) dest = src.createCompatibleWritableRaster();

        // may have to add some instance of checks
        byte[] srcPixels = ((DataBufferByte) src.getDataBuffer()).getData();
        byte[] destPixels = ((DataBufferByte) dest.getDataBuffer()).getData();

        double Y, Cb, Cr, K;
        double lastY = -1, lastCb = -1, lastCr = -1, lastK = -1;
        int c = 0, m = 0, y2 = 0, k = 0;

        int bands = src.getNumBands();
        for (int pixel = 0; pixel < srcPixels.length; pixel += bands) {

            Y = (srcPixels[pixel] & 0xff);
            Cb = (srcPixels[pixel + 1] & 0xff);
            Cr = (srcPixels[pixel + 2] & 0xff);
            K = (srcPixels[pixel + 3] & 0xff);

            if (!(lastY == Y && lastCb == Cb && lastCr == Cr && lastK == K)) {

                // intel codecs, http://software.intel.com/sites/products/documentation/hpc/ipp/ippi/ippi_ch6/ch6_color_models.html
                // Intel IPP conversion for JPEG codec.
                c = 255 - (int) (Y + (1.402 * Cr) - 179.456);
                m = 255 - (int) (Y - (0.34414 * Cb) - (0.71413636 * Cr) + 135.45984);
                y2 = 255 - (int) (Y + (1.7718 * Cb) - 226.816);
                k = (int) K;

                c = clip(0, 255, c);
                m = clip(0, 255, m);
                y2 = clip(0, 255, y2);
            }

            lastY = Y;
            lastCb = Cb;
            lastCr = Cr;
            lastK = K;
            destPixels[pixel] = (byte) (c & 0xff);
            destPixels[pixel + 1] = (byte) (m & 0xff);
            destPixels[pixel + 2] = (byte) (y2 & 0xff);
            destPixels[pixel + 3] = (byte) (k & 0xff);
        }
        return dest;
    }

    public Rectangle2D getBounds2D(Raster src) {
        return null;
    }

    public WritableRaster createCompatibleDestRaster(Raster src) {
        return src.createCompatibleWritableRaster();
    }

    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null)
            dstPt = (Point2D) srcPt.clone();
        else
            dstPt.setLocation(srcPt);
        return dstPt;
    }

    public RenderingHints getRenderingHints() {
        return hints;
    }

    /**
     * Clips the value according to the specified floor and ceiling.
     *
     * @param floor   floor value of clip
     * @param ceiling ceiling value of clip
     * @param value   value to clip.
     * @return clipped value.
     */
    private static int clip(int floor, int ceiling, int value) {
        if (value < floor) {
            value = floor;
        }
        if (value > ceiling) {
            value = ceiling;
        }
        return value;
    }

    /*
    // older method for conversion,  keeping for historical context.

    protected static void alterRasterYCCK2BGRA(WritableRaster wr,
                                               BufferedImage smaskImage,
                                               BufferedImage maskImage,
                                               float[] decode,
                                               int bitsPerComponent) {
        Raster smaskRaster = null;
        int smaskWidth = 0;
        int smaskHeight = 0;
        if (smaskImage != null) {
            smaskRaster = smaskImage.getRaster();
            smaskWidth = smaskRaster.getWidth();
            smaskHeight = smaskRaster.getHeight();
        }

        Raster maskRaster = null;
        int maskWidth = 0;
        int maskHeight = 0;
        if (maskImage != null) {
            maskRaster = maskImage.getRaster();
            maskWidth = maskRaster.getWidth();
            maskHeight = maskRaster.getHeight();
        }

        byte[] dataValues = new byte[wr.getNumBands()];
        float[] origValues = new float[wr.getNumBands()];
        double[] rgbaValues = new double[4];

        int width = wr.getWidth();
        int height = wr.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // apply decode param.
                ImageUtility.getNormalizedComponents(
                        (byte[]) wr.getDataElements(x, y, dataValues),
                        decode,
                        origValues);

                float Y = origValues[0] * 255;
                float Cb = origValues[1] * 255;
                float Cr = origValues[2] * 255;
//                float K = origValues[3] * 255;

                // removing alteration for now as some samples are too dark.
                // Y *= .95; // gives a darker image,  as y approaches zero,
                // the image becomes darke

                float Cr_128 = Cr - 128;
                float Cb_128 = Cb - 128;

                // adobe conversion for CCIR Rec. 601-1 standard.
                // http://partners.adobe.com/public/developer/en/ps/sdk/5116.DCT_Filter.pdf
//                double rVal = Y + (1.4020 * Cr_128);
//                double gVal = Y - (.3441363 * Cb_128) - (.71413636 * Cr_128);
//                double bVal = Y + (1.772 * Cb_128);

                // intel codecs, http://software.intel.com/sites/products/documentation/hpc/ipp/ippi/ippi_ch6/ch6_color_models.html
                // Intel IPP conversion for JPEG codec.
//                double rVal = Y + (1.402 * Cr) - 179.456;
//                double gVal = Y - (0.34414 * Cb) - (.71413636 * Cr) + 135.45984;
//                double bVal = Y + (1.772 * Cb) - 226.816;

                // ICEsoft custom algorithm, results may vary, res are a little
                // off but over all a better conversion/ then the stoke algorithms.
                double rVal = Y + (1.4020 * Cr_128);
                double gVal = Y + (.14414 * Cb_128) + (.11413636 * Cr_128);
                double bVal = Y + (1.772 * Cb_128);

                // Intel IPP conversion for ITU-R BT.601 for video
                // default 16, higher more green and darker blacks, lower less
                // green hue and lighter blacks.
//                double kLight = (1.164 * (Y -16 ));
//                double rVal = kLight + (1.596 * Cr_128);
//                double gVal = kLight - (0.392 * Cb_128) - (0.813 * Cr_128);
//                double bVal = kLight + (1.017 * Cb_128);
                // intel PhotoYCC Color Model [0.1],  not a likely candidate for jpegs.
//                double y1 = Y/255.0;
//                double c1 = Cb/255.0;
//                double c2 = Cr/255.0;
//                double rVal = ((0.981 * y1) + (1.315 * (c2 - 0.537))) *255.0;
//                double gVal = ((0.981 * y1) - (0.311 * (c1 - 0.612))- (0.669 * (c2 - 0.537))) *255.0;
//                double bVal = ((0.981 * y1) + (1.601 * (c1 - 0.612))) *255.0;

                // check the range an convert as needed.
                byte rByte = (rVal < 0) ? (byte) 0 : (rVal > 255) ? (byte) 0xFF : (byte) rVal;
                byte gByte = (gVal < 0) ? (byte) 0 : (gVal > 255) ? (byte) 0xFF : (byte) gVal;
                byte bByte = (bVal < 0) ? (byte) 0 : (bVal > 255) ? (byte) 0xFF : (byte) bVal;
                int alpha = 0xFF;
                if (y < smaskHeight && x < smaskWidth && smaskRaster != null) {
                    alpha = (smaskRaster.getSample(x, y, 0) & 0xFF);
                } else if (y < maskHeight && x < maskWidth && maskRaster != null) {
                    // When making an ImageMask, the alpha channel is setup so that
                    //  it both works correctly for the ImageMask being painted,
                    //  and also for when it's used here, to determine the alpha
                    //  of an image that it's masking
                    alpha = (maskImage.getRGB(x, y) >>> 24) & 0xFF; // Extract Alpha from ARGB
                }

                rgbaValues[0] = bByte;
                rgbaValues[1] = gByte;
                rgbaValues[2] = rByte;
                rgbaValues[3] = alpha;

                wr.setPixel(x, y, rgbaValues);
            }
        }
    }
     */
}
