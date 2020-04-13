package org.icepdf.core.pobjects.graphics;

import org.icepdf.core.pobjects.Stream;
import org.icepdf.core.util.Library;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Coons Patch Meshes support.
 *
 * Note: currently only parsing data and returning the first colour of the first vertex.
 *
 * @since 6.2
 */
public class ShadingType6Pattern  extends ShadingMeshPattern {

    private static final Logger logger =
            Logger.getLogger(ShadingType6Pattern.class.toString());

    private ArrayList<Point2D.Float> coordinates = new ArrayList<Point2D.Float>();
    private ArrayList<Color> colorComponents = new ArrayList<Color>();

    public ShadingType6Pattern(Library l, HashMap h, Stream meshDataStream) {
        super(l, h, meshDataStream);
    }

    public void init(GraphicsState graphicsState) {
        coordinates = new ArrayList<Point2D.Float>();
        colorComponents = new ArrayList<Color>();
        try {
            while (vertexBitStream.available() > 0) {
                int flag = readFlag();
                // read control points.
                for (int i = 0, ii = (flag != 0 ? 8 : 12); i < ii; i++) {
                    coordinates.add(readCoord());
                }
                for (int i = 0, ii = (flag != 0 ? 2 : 4); i < ii; i++) {
                    colorComponents.add(readColor());
                }
            }
        } catch (IOException e) {
            logger.warning("Error parsing Shading type 6 pattern vertices.");
        }
    }

    public Paint getPaint() throws InterruptedException {
        return colorComponents.get(0);
    }
}
