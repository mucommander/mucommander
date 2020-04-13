package org.icepdf.core.pobjects.graphics;
 /*
import org.icepdf.core.pobjects.ImageStream;
import org.icepdf.core.pobjects.Resources;
import org.icepdf.core.util.Library;

import javax.media.jai.*;
import javax.media.jai.operator.ErrorDiffusionDescriptor;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.renderable.ParameterBlock;
import java.util.logging.Logger;
  */

/**
 * @since 5.0.1
 */
public class FloydSteinbergImageReference{// extends CachedImageReference {
   /*
    private static final Logger logger =
            Logger.getLogger(ScaledImageReference.class.toString());

    // scaled image size.
    private int width;
    private int height;

    protected FloydSteinbergImageReference(ImageStream imageStream, Color fillColor, Resources resources) {
        super(imageStream, fillColor, resources);

        // get eh original image width.
        width = imageStream.getWidth();
        height = imageStream.getHeight();

        // kick off a new thread to load the image, if not already in pool.
        ImagePool imagePool = imageStream.getLibrary().getImagePool();
        if (useProxy && imagePool.get(reference) == null) {
            Library.executePainter(futureTask);
        } else if (!useProxy && imagePool.get(reference) == null) {
            call();
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public BufferedImage call() {
        try {
            image = imageStream.getImage(fillColor, resources);
//            DitherFloydSteinberg tmp = new DitherFloydSteinberg();
//            tmp.Process(image);
            if (image != null) {
                // JAI filter code
                PlanarImage surrogateImage = PlanarImage.wrapRenderedImage(image);
                ParameterBlock pb = new ParameterBlock();
                pb.addSource(surrogateImage);
                String opName = null;
                LookupTableJAI lut;
                if (true) {
                    opName = "errordiffusion";
                    lut = new LookupTableJAI(new byte[]{(byte) 0x00, (byte) 0xff});
                    pb.add(lut);
                    pb.add(KernelJAI.ERROR_FILTER_FLOYD_STEINBERG);
                } else {
                    opName = "ordereddither";
                    ColorCube cube = ColorCube.createColorCube(DataBuffer.TYPE_BYTE,
                            0, new int[]{2});
                    pb.add(cube);
                    pb.add(KernelJAI.DITHER_MASK_441);

                }
                ImageLayout layout = new ImageLayout();
                byte[] map = new byte[] {(byte)0x00, (byte)0xff};
                ColorModel cm = new IndexColorModel(1, 2, map, map, map);
                layout.setColorModel(cm);
                RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
                PlanarImage op = ErrorDiffusionDescriptor.create(surrogateImage, lut,
                        KernelJAI.ERROR_FILTER_FLOYD_STEINBERG, hints);
                BufferedImage dst = op.getAsBufferedImage();
//                PlanarImage dst = JAI.create(opName, pb, hints);

                image = dst;
            }
        } catch (Throwable e) {
            logger.warning("Error loading image: " + imageStream.getPObjectReference() +
                    " " + imageStream.toString());
        }
        return image;
    }

    public class DitherFloydSteinberg {
        int max_intensity, min_intensity;

        private int decode(int oldPixel) {
            int r = (oldPixel >> 16) & 0xff;
            int g = (oldPixel >> 8) & 0xff;
            int b = (oldPixel) & 0xff;
            return (int) ((r + g + b) / 3);
        }

        private int encode(int oldPixel) {
            return (0xff << 24) | (oldPixel << 16) | (oldPixel << 8) | (oldPixel);
        }

        private int ScaleIntensity(int i) {
            float scale = (float) (i - min_intensity) / (float) (max_intensity - min_intensity);
            return (int) (255 * scale);
        }

        private int find_closest_palette_color(int oldPixel) {
            int i = ScaleIntensity(oldPixel);
            return (i > 127) ? 255 : 0;
        }


        private void FindIntensityRange(BufferedImage img) {
            int w = img.getWidth();
            int h = img.getHeight();
            int x, y, i;

            max_intensity = 0;
            min_intensity = 255;
//            max_intensity=255;
//            min_intensity=000;
            for (y = 0; y < h; ++y) {
                // for each x from left to right
                for (x = 0; x < w; ++x) {
                    // oldpixel := pixel[x][y]
                    i = decode(img.getRGB(x, y));
                    if (max_intensity < i) max_intensity = i;
                    if (min_intensity > i) min_intensity = i;
                    img.setRGB(x, y, encode(i));
                }
            }
        }

        // Floyd-Steinberg dithering
        public void Process(BufferedImage img) {
            int x, y, oldPixel, newPixel;
            float quant_error;
            int w = img.getWidth();
            int h = img.getHeight();

            FindIntensityRange(img);

            // for each y from top to bottom
            for (y = 0; y < h; ++y) {
                // for each x from left to right
                for (x = 0; x < w; ++x) {
                    // oldpixel := pixel[x][y]
                    oldPixel = decode(img.getRGB(x, y));
                    // newpixel := find_closest_palette_color(oldpixel)
                    newPixel = find_closest_palette_color(oldPixel);
                    // pixel[x][y] := newpixel
                    img.setRGB(x, y, encode(newPixel));
                    // quant_error := oldpixel - newpixel
                    quant_error = oldPixel - newPixel;
                    // pixel[x+1][y  ] := pixel[x+1][y  ] + 7/16 * quant_error
                    // pixel[x-1][y+1] := pixel[x-1][y+1] + 3/16 * quant_error
                    // pixel[x  ][y+1] := pixel[x  ][y+1] + 5/16 * quant_error
                    // pixel[x+1][y+1] := pixel[x+1][y+1] + 1/16 * quant_error
                    if (x < w - 1)
                        img.setRGB(x + 1, y, encode((int) ((float) decode(img.getRGB(x + 1, y)) + (7.0 / 16.0 * quant_error))));
                    if (y < h - 1) {
                        if (x > 0)
                            img.setRGB(x - 1, y + 1, encode((int) ((float) decode(img.getRGB(x - 1, y + 1)) + (3.0 / 16.0 * quant_error))));
                        img.setRGB(x, y + 1, encode((int) ((float) decode(img.getRGB(x, y + 1)) + (5.0 / 16.0 * quant_error))));
                        if (x < w - 1)
                            img.setRGB(x + 1, y + 1, encode((int) ((float) decode(img.getRGB(x + 1, y + 1)) + (1.0 / 16.0 * quant_error))));
                    }
                }
            }
        }
    }
     */
}