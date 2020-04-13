package org.icepdf.core.pobjects.graphics.RasterOps;

import org.icepdf.core.util.Defs;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;

/**
 * Raster operation for converting a CMYK colour to RGB using an a rough
 * algorithm which generally results in images that are darker then they should
 * be.  The black value can be configured using the system property
 * -Dorg.icepdf.core.cmyk.image.black=255.
 * <p/>
 * This colour conversion method should only be used if its not desirable to
 * use the more accurate ICC Color Profile for colour conversion.
 *
 * @since 5.1
 */
public class CMYKRasterOp implements RasterOp {

    // default cmyk value,  > 255 will lighten the image.
    private static float blackRatio;
    private RenderingHints hints = null;

    public CMYKRasterOp(RenderingHints hints) {
        this.hints = hints;
        blackRatio = Defs.intProperty("org.icepdf.core.cmyk.image.black", 255);
    }

    public static void setBlackRatio(float blackRatio) {
        CMYKRasterOp.blackRatio = blackRatio;
    }

    public WritableRaster filter(Raster src, WritableRaster dest) {

        if (dest == null) dest = src.createCompatibleWritableRaster();

        // may have to add some instance of checks
        byte[] srcPixels = ((DataBufferByte) src.getDataBuffer()).getData();
        int[] destPixels = ((DataBufferInt) dest.getDataBuffer()).getData();

        // this convoluted cymk->rgba method is from DeviceCMYK class.
        float inCyan, inMagenta, inYellow, inBlack;
        float lastCyan = -1, lastMagenta = -1, lastYellow = -1, lastBlack = -1;
        double c, m, y2, aw, ac, am, ay, ar, ag, ab;
        float outRed, outGreen, outBlue;
        int rValue = 0, gValue = 0, bValue = 0, alpha = 0;

        int bands = src.getNumBands();
        for (int pixel = 0, intPixels = 0; pixel < srcPixels.length; pixel += bands, intPixels++) {

            inCyan = (srcPixels[pixel] & 0xff) / 255.0f;
            inMagenta = (srcPixels[pixel + 1] & 0xff) / 255.0f;
            inYellow = (srcPixels[pixel + 2] & 0xff) / 255.0f;
            // lessen the amount of black, standard 255 fraction is too dark
            // increasing the denominator has the same affect of lighting up
            // the image.
            inBlack = (srcPixels[pixel + 3] & 0xff) / blackRatio;

            if (!(inCyan == lastCyan && inMagenta == lastMagenta &&
                    inYellow == lastYellow && inBlack == lastBlack)) {

                c = clip(0, 1, inCyan + inBlack);
                m = clip(0, 1, inMagenta + inBlack);
                y2 = clip(0, 1, inYellow + inBlack);
                aw = (1 - c) * (1 - m) * (1 - y2);
                ac = c * (1 - m) * (1 - y2);
                am = (1 - c) * m * (1 - y2);
                ay = (1 - c) * (1 - m) * y2;
                ar = (1 - c) * m * y2;
                ag = c * (1 - m) * y2;
                ab = c * m * (1 - y2);

                outRed = (float) clip(0, 1, aw + 0.9137 * am + 0.9961 * ay + 0.9882 * ar);
                outGreen = (float) clip(0, 1, aw + 0.6196 * ac + ay + 0.5176 * ag);
                outBlue = (float) clip(0, 1, aw + 0.7804 * ac + 0.5412 * am + 0.0667 * ar + 0.2118 * ag + 0.4863 * ab);
                rValue = (int) (outRed * 255);
                gValue = (int) (outGreen * 255);
                bValue = (int) (outBlue * 255);
                alpha = 0xFF;
            }
            lastCyan = inCyan;
            lastMagenta = inMagenta;
            lastYellow = inYellow;
            lastBlack = inBlack;

            destPixels[intPixels] = ((alpha & 0xff) << 24) |
                    ((rValue & 0xff) << 16) | ((gValue & 0xff) << 8) |
                    (bValue & 0xff);
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
    private static double clip(double floor, double ceiling, double value) {
        if (value < floor) {
            value = floor;
        }
        if (value > ceiling) {
            value = ceiling;
        }
        return value;
    }
}
