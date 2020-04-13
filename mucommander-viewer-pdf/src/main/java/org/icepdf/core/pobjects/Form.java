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
package org.icepdf.core.pobjects;

import org.icepdf.core.io.SeekableInputConstrainedWrapper;
import org.icepdf.core.pobjects.graphics.ExtGState;
import org.icepdf.core.pobjects.graphics.GraphicsState;
import org.icepdf.core.pobjects.graphics.Shapes;
import org.icepdf.core.util.Library;
import org.icepdf.core.util.content.ContentParser;
import org.icepdf.core.util.content.ContentParserFactory;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Form XObject class. Not currently part of the public api.
 * <p/>
 * Forms are grouped into the 'Resource' category and can be shared.  As a result we need to make sure
 * that the init method are synchronized as they can be accessed by different page loading threads.
 *
 * @since 1.0
 */
public class Form extends Stream {

    private static final Logger logger =
            Logger.getLogger(Form.class.toString());

    public static final Name TYPE_VALUE = new Name("XObject");
    public static final Name SUB_TYPE_VALUE = new Name("Form");
    public static final Name GROUP_KEY = new Name("Group");
    public static final Name I_KEY = new Name("I");
    public static final Name K_KEY = new Name("K");
    public static final Name MATRIX_KEY = new Name("Matrix");
    public static final Name BBOX_KEY = new Name("BBox");
    public static final Name RESOURCES_KEY = new Name("Resources");

    private AffineTransform matrix = new AffineTransform();
    private Rectangle2D bbox;
    private Shapes shapes;
    // Graphics state object to be used by content parser
    private GraphicsState graphicsState;
    private ExtGState extGState;
    private Resources resources;
    private Resources parentResource;
    // transparency grouping data
    private boolean transparencyGroup;
    private boolean isolated;
    private boolean knockOut;
    private boolean shading;
    private boolean inited = false;

    /**
     * Creates a new instance of the xObject.
     *
     * @param l                  document library
     * @param h                  xObject dictionary entries.
     * @param streamInputWrapper content stream of image or post script commands.
     */
    public Form(Library l, HashMap h, SeekableInputConstrainedWrapper streamInputWrapper) {
        super(l, h, streamInputWrapper);

        // check for grouping flags so we can do special handling during the
        // xform content stream parsing.
        HashMap group = library.getDictionary(entries, GROUP_KEY);
        if (group != null) {
            transparencyGroup = true;
            isolated = library.getBoolean(group, I_KEY);
            knockOut = library.getBoolean(group, K_KEY);
        }
    }

    public HashMap getGroup() {
        return library.getDictionary(entries, GROUP_KEY);
    }

    @SuppressWarnings("unchecked")
    public void setAppearance(Shapes shapes, AffineTransform matrix, Rectangle2D bbox) {
        inited = false;
        this.shapes = shapes;
        this.matrix = matrix;
        this.bbox = bbox;
        entries.put(Form.BBOX_KEY, PRectangle.getPRectangleVector(bbox));
        entries.put(Form.MATRIX_KEY, matrix);
    }

    /**
     * Sets the GraphicsState which should be used by the content parser when
     * parsing the Forms content stream.  The GraphicsState should be set
     * before init() is called, or it will have not effect on the rendered
     * content.
     *
     * @param graphicsState current graphic state
     */
    public void setGraphicsState(GraphicsState graphicsState) {
        if (graphicsState != null) {
            this.graphicsState = graphicsState;
            this.extGState = graphicsState.getExtGState();
        }
    }

    /**
     * Gets the associated graphic state instance for this form.
     *
     * @return external graphic state,  can be null.
     */
    public GraphicsState getGraphicsState() {
        return graphicsState;
    }

    /**
     * Gets the extended graphics state for the form at the time of creation.  This contains any masking and blending
     * data that might bet over written during the forms parsing.
     *
     * @return extended graphic state at the time of creation.
     */
    public ExtGState getExtGState() {
        return extGState;
    }

    /**
     * Utility method for parsing a vector of affinetranform values to an
     * affine transform.
     *
     * @param v vectory containing affine transform values.
     * @return affine tansform based on v
     */
    private static AffineTransform getAffineTransform(List v) {
        float f[] = new float[6];
        for (int i = 0; i < 6; i++) {
            f[i] = ((Number) v.get(i)).floatValue();
        }
        return new AffineTransform(f);
    }

    /**
     * As of the PDF 1.2 specification, a resource entry is not required for
     * a XObject and thus it needs to point to the parent resource to enable
     * to correctly load the content stream.
     *
     * @param parentResource parent objects resourse when available.
     */
    public void setParentResources(Resources parentResource) {
        this.parentResource = parentResource;
    }

    /**
     *
     */
    public synchronized void init() {
        if (inited) {
            return;
        }
        Object v = library.getObject(entries, MATRIX_KEY);
        if (v != null && v instanceof List) {
            matrix = getAffineTransform((List) v);
        } else if (v != null && v instanceof AffineTransform) {
            matrix = (AffineTransform) v;
        }
        bbox = library.getRectangle(entries, BBOX_KEY);
        // try and find the form's resources dictionary.
        Resources leafResources = library.getResources(entries, RESOURCES_KEY);
        // apply parent resource, if the current resources is null
        if (leafResources != null) {
            resources = leafResources;
        } else {
            leafResources = parentResource;
        }
        // Build a new content parser for the content streams and apply the
        // content stream of the calling content stream. 
        ContentParser cp = ContentParserFactory.getInstance()
                .getContentParser(library, leafResources);
        cp.setGraphicsState(graphicsState);
        byte[] in = getDecodedStreamBytes();
        if (in != null) {
            try {
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Parsing form " + getPObjectReference());
                }
                shapes = cp.parse(new byte[][]{in}, null).getShapes();
            } catch (Throwable e) {
                // reset shapes vector, we don't want to mess up the paint stack
                shapes = new Shapes();
                logger.log(Level.FINE, "Error parsing Form content stream.", e);
            }
        }
        inited = true;
    }

    public Resources getResources() {
        Resources leafResources = library.getResources(entries, RESOURCES_KEY);
        if (leafResources == null) {
            leafResources = new Resources(library, new HashMap());
        }
        return leafResources;
    }

    @SuppressWarnings("unchecked")
    public void setResources(Resources resources) {
        entries.put(RESOURCES_KEY, resources.getEntries());
    }


    /**
     * Gets the shapes that where parsed from the content stream.
     *
     * @return shapes object for xObject.
     */
    public Shapes getShapes() {
        return shapes;
    }

    /**
     * Gets the bounding box for the xObject.
     *
     * @return rectangle in PDF coordinate space representing xObject bounds.
     */
    public Rectangle2D getBBox() {
        return bbox;
    }

    /**
     * Gets the optional matrix which describes how to convert the coordinate
     * system in xObject space to the parent coordinates space.
     *
     * @return affine transform representing the xObject's pdf to xObject space
     * transform.
     */
    public AffineTransform getMatrix() {
        return matrix;
    }

    /**
     * If the xObject has a transparency group flag.
     *
     * @return true if a transparency group exists, false otherwise.
     */
    public boolean isTransparencyGroup() {
        return transparencyGroup;
    }

    /**
     * Only present if a transparency group is present.  Isolated groups are
     * composed on a fully transparent back drop rather then the groups.
     *
     * @return true if the transparency group is isolated.
     */
    public boolean isIsolated() {
        return isolated;
    }

    /**
     * Only present if a transparency group is present.  Knockout groups individual
     * elements composed with the groups initial back drop rather then the stack.
     *
     * @return true if the transparency group is a knockout.
     */
    public boolean isKnockOut() {
        return knockOut;
    }

    public boolean isShading() {
        return shading;
    }

    public void setShading(boolean shading) {
        this.shading = shading;
    }
}
