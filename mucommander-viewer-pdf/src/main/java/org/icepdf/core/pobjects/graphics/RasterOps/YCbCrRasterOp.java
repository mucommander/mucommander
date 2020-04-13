package org.icepdf.core.pobjects.graphics.RasterOps;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;

/**
 * Raster operation that convers the YCbCr to a RGB colour space.
 *
 * @since 5.1
 */
public class YCbCrRasterOp implements RasterOp {

    private RenderingHints hints = null;

    public YCbCrRasterOp(RenderingHints hints) {
        this.hints = hints;
    }

    public WritableRaster filter(Raster src, WritableRaster dest) {

        if (dest == null) dest = src.createCompatibleWritableRaster();

        // my have to add some instance of checks
        byte[] srcPixels = ((DataBufferByte) src.getDataBuffer()).getData();
        int[] destPixels = ((DataBufferInt) dest.getDataBuffer()).getData();

        int Y, Cb, Cr;
        int lastY = -1, lastCb = -1, lastCr = -1;
        int rVal = 0, gVal = 0, bVal = 0;
        int bands = src.getNumBands();
        for (int pixel = 0, intPixels = 0; pixel < srcPixels.length; pixel += bands, intPixels++) {

            Y = srcPixels[pixel] & 0xff;
            Cb = srcPixels[pixel + 1] & 0xff;
            Cr = srcPixels[pixel + 2] & 0xff;

            // no point recalculating if we are doing a band of colours.
            if (!(lastY == Y && lastCb == Cb && lastCr == Cr)) {
                // The Intel IPP color conversion functions specific for the JPEG codec
                rVal = clamp((int) (Y + 1.402 * Cr - 179.456));
                gVal = clamp((int) (Y - 0.34414 * Cb - 0.71414 * Cr + 135.45984));
                bVal = clamp((int) (Y + 1.772 * Cb - 226.816));
            }
            lastY = Y;
            lastCb = Cb;
            lastCr = Cr;

            destPixels[intPixels] = ((rVal & 0xff) << 16) | ((gVal & 0xff) << 8) | (bVal & 0xff);
        }
        return dest;
    }

    // clamp the input between 0 ... 255
    private static int clamp(int x) {
        return (x < 0) ? 0 : ((x > 255) ? 255 : x);
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
}
