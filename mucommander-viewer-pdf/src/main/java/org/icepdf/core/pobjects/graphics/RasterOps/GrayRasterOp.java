package org.icepdf.core.pobjects.graphics.RasterOps;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.RasterOp;
import java.awt.image.WritableRaster;

/**
 * Applies an algorithm to convert the Gray colour space to RGB.
 *
 * @since 5.1
 */
public class GrayRasterOp implements RasterOp {
    private RenderingHints hints = null;
    private float[] decode;

    public GrayRasterOp(float[] decode, RenderingHints hints) {
        this.hints = hints;
        this.decode = decode;
    }


    public WritableRaster filter(Raster src, WritableRaster dest) {

        if (dest == null) dest = src.createCompatibleWritableRaster();

        // may have to add some instance of checks
        byte[] srcPixels = ((DataBufferByte) src.getDataBuffer()).getData();
        byte[] destPixels = ((DataBufferByte) dest.getDataBuffer()).getData();
        boolean defaultDecode = 0.0f == decode[0];

        int Y;
        int bands = src.getNumBands();
        for (int pixel = 0; pixel < srcPixels.length; pixel += bands) {
            Y = srcPixels[pixel] & 0xff;
            Y = defaultDecode ? 255 - Y : Y;
            Y = (Y < 0) ? (byte) 0 : (Y > 255) ? (byte) 0xFF : (byte) Y;
            destPixels[pixel] = (byte) Y;
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
}
