package org.icepdf.core.pobjects.graphics.RasterOps;

import org.icepdf.core.pobjects.graphics.DeviceRGB;
import org.icepdf.core.pobjects.graphics.PColorSpace;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;

/**
 * Convert a rgb encoded raster to the specified colour space.
 *
 * @since 5.1
 */
public class PColorSpaceRasterOp implements RasterOp {

    private RenderingHints hints = null;
    private PColorSpace colorSpace;

    public PColorSpaceRasterOp(PColorSpace colorSpace, RenderingHints hints) {
        this.hints = hints;
        this.colorSpace = colorSpace;
    }

    public WritableRaster filter(Raster src, WritableRaster dest) {

        if (dest == null) dest = src.createCompatibleWritableRaster();

        // may have to add some instance of checks
        byte[] srcPixels = ((DataBufferByte) src.getDataBuffer()).getData();
        int[] destPixels = ((DataBufferInt) dest.getDataBuffer()).getData();

        // already RGB not much to do so we just build the colour
        if (colorSpace instanceof DeviceRGB) {
            int bands = src.getNumBands();
            int[] rgbValues = new int[3];
            for (int pixel = 0, intPixels = 0; pixel < srcPixels.length; pixel += bands, intPixels++) {

                rgbValues[0] = (srcPixels[pixel] & 0xff);
                rgbValues[1] = (srcPixels[pixel + 1] & 0xff);
                rgbValues[2] = (srcPixels[pixel + 2] & 0xff);

                // reverse after the normalization to avoid looking gray data as
                // array is trimmed above.
                destPixels[intPixels] = ((rgbValues[0] & 0xff) << 16) |
                        ((rgbValues[1] & 0xff) << 8) |
                        (rgbValues[2] & 0xff);
            }
        } else {
            int bands = src.getNumBands();
            float[] values = new float[3];
            for (int pixel = 0, intPixels = 0; pixel < srcPixels.length; pixel += bands, intPixels++) {

                for (int i = 0; i < bands; i++) {
                    values[i] = (srcPixels[pixel + i] & 0xff) / 255.0f;
                }
                // color space caching should help with the number of colors
                // objects created.
                PColorSpace.reverseInPlace(values);
                destPixels[intPixels] = colorSpace.getColor(values).getRGB();
            }
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
        if (dstPt == null) {
            dstPt = (Point2D) srcPt.clone();
        } else {
            dstPt.setLocation(srcPt);
        }
        return dstPt;
    }

    public RenderingHints getRenderingHints() {
        return hints;
    }
}
