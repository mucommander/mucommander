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
 * Lattice-Form Gouraud-shaded Triangle Meshes support.
 *
 * Note: currently only parsing data and returning the first colour of the first vertex.
 *
 * @since 6.2
 */
public class ShadingType5Pattern extends ShadingMeshPattern {

    private static final Logger logger =
            Logger.getLogger(ShadingType5Pattern.class.toString());

    private ArrayList<Point2D.Float> coordinates = new ArrayList<Point2D.Float>();
    private ArrayList<Color> colorComponents = new ArrayList<Color>();

    public ShadingType5Pattern(Library l, HashMap h, Stream meshDataStream) {
        super(l, h, meshDataStream);
    }

    public void init(GraphicsState graphicsState) {
        coordinates = new ArrayList<Point2D.Float>();
        colorComponents = new ArrayList<Color>();
        try {
            while (vertexBitStream.available() > 0) {
                coordinates.add(readCoord());
                colorComponents.add(readColor());
            }
        } catch (IOException e) {
            logger.warning("Error parsing Shading type 5 pattern vertices.");
        }
    }

    public Paint getPaint() throws InterruptedException {
        return colorComponents.get(0);
    }
}
