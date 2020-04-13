package org.icepdf.core.pobjects.graphics.RasterOps;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.RasterOp;
import java.awt.image.WritableRaster;

/**
 * Applies the decode value array tot he specified raster.  The decode array
 * is specific to PDF and describes how to map image samples into the range of
 * values appropriate for the image’s colour space.
 * <p/>
 * This Raster Operation should be applied to any image type before colour
 * conversion takes place.
 *
 * @since 5.1
 */
public class DecodeRasterOp implements RasterOp {

    private static final float NORMAL_DECODE_CEIL = 1.0f / 255;

    private RenderingHints hints = null;
    private float[] decode;

    public DecodeRasterOp(float[] decode, RenderingHints hints) {
        this.hints = hints;
        this.decode = decode;
    }

    /**
     * Simple test to see if the decode is none standard where standard is
     * [0,1,0,1,0,1].
     *
     * @param decode decode array to check
     * @return true if the decode is not normal, otherwise false.
     */
    private static boolean isNormalDecode(float[] decode) {
        // normal decode is always [0,1,0,1....]
        for (int i = 0, max = decode.length; i < max; i += 2) {
            if (decode[i] != 0.0f || decode[i + 1] != NORMAL_DECODE_CEIL) {
                return false;
            }
        }
        return true;
    }

    public WritableRaster filter(Raster src, WritableRaster dest) {

        // check if we have none 0-1 decode, if so continue if not return
        if (isNormalDecode(decode)) {
            return (WritableRaster) src;
        }

        if (dest == null) dest = src.createCompatibleWritableRaster();

        // may have to add some instance of checks
        byte[] srcPixels = ((DataBufferByte) src.getDataBuffer()).getData();
        byte[] destPixels = ((DataBufferByte) dest.getDataBuffer()).getData();

        int bands = src.getNumBands();
        for (int pixel = 0; pixel < srcPixels.length; pixel += bands) {
            // apply decode param.
            for (int i = 0; i < bands; i++) {
                destPixels[pixel + i] = normalizeComponents(srcPixels[pixel + i], decode, i);
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
     * Apply the Decode Array domain for each colour component. Assumes output
     * range is 0-255 for each value in out.
     *
     * @param pixels colour to process by decode
     * @param decode decode array for colour space
     * @return decoded value..
     */
    private static byte normalizeComponents(
            byte pixels,
            float[] decode,
            int i) {
        // interpolate each colour component for the given decode domain.
        return (byte) ((decode[i * 2] * 255) + (pixels & 0xff) * (decode[(i * 2) + 1] * 255));
    }
}
