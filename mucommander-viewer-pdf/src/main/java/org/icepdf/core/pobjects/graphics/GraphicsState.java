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
package org.icepdf.core.pobjects.graphics;

import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.graphics.commands.*;
import org.icepdf.core.util.Defs;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>The PDF viewer application maintains an internal data structure called the
 * graphics state that holds current graphics control parameters. These
 * parameters define the global framework within which the graphics operators
 * execute.</p>
 * <p/>
 * <p>The graphics state is initialized at the beginning of each page, using the
 * default values specified in Tables 4.2 and 4.3. Table 4.2 lists those
 * graphics state parameters that are device-independent and are appropriate
 * to specify in page descriptions. The parameters listed in Table 4.3 control
 * details of the rendering (scan conversion) process and are device-dependent;
 * a page description that is intended to be device-independent should not
 * modify these parameters.</p>
 * <p/>
 * <h2>Graphics State Stack Info</h2>
 * <p>A well-structured PDF document typically contains many graphical elements
 * that are essentially independent of each other and sometimes nested to
 * multiple levels. The graphics state stack allows these elements to make local
 * changes to the graphics state without disturbing the graphics state of the
 * surrounding environment. The stack is a LIFO (last in, first out) data
 * structure in which the contents of the graphics state can be saved and later
 * restored using the following operators:
 * <ul>
 * <li><p>The q operator pushes a copy of the entire graphics state onto the
 * stack. This is handled by the save() method in this class</li>
 * <li><p>The Q operator restores the entire graphics state to its former
 * value by popping it from the stack.  This is handled by the
 * restore() method in this class</p><li>
 * </ul>
 * <p>When a graphics state is saved a new GraphicsState object is created and
 * a pointer is set to the origional graphics state.  This creates a linked
 * list of graphics states that can be easily restored when needed (LIFO staack)
 * </p>
 * <h2>Shapes Stack</h2>
 * <p>The graphics state also manipulates the Shapes stack that contains all of
 * the rendering components dealing with graphics states.  As the content parser
 * encounters different graphic state manipulators they are added to the stack
 * and then when the page is rendered the stack (actually a vector) is read
 * in a FIFO to generate the drawing commands.</p>
 * <p/>
 * <h2>Device-independent graphics state parameters - (Table 4.2)</h2>
 * <table border="1" cellpadding="1" cellspacing="1" >
 * <tr>
 * <td><b> Paramater </b></td>
 * <td><b> Type</b></td>
 * <td><b> Value</b></td>
 * </tr>
 * <tr>
 * <td valign="top" >CTM</td>
 * <td valign="top" >array</td>
 * <td>The current transformation matrix, which maps positions from user
 * coordinates to device coordinates. This matrix is modified by each
 * application of the coordinate transformation operator, cm. Initial
 * value: a matrix that transforms default user coordinates to device
 * coordinates.</td>
 * </tr>
 * <tr>
 * <td valign="top" >clipping path</td>
 * <td valign="top" >(internal)</td>
 * <td>The current clipping path, which defines the boundary against which
 * all output is to be cropped. Initial value: the boundary of the
 * entire imageable portion of the output page.</td>
 * </tr>
 * <tr>
 * <td valign="top" >color space</td>
 * <td valign="top" >name or array</td>
 * <td>The current color space in which color values are to be interpreted.
 * There are two separate color space parameters: one for stroking and
 * one for all other painting operations. Initial value: DeviceGray.</td>
 * </tr>
 * <tr>
 * <td valign="top" >color</td>
 * <td valign="top" >(various</td>
 * <td>The current color to be used during painting operations. The type
 * and interpretation of this parameter depend on the current color
 * space; for most color spaces, a color value consists of one to four
 * numbers. There are two separate color parameters: one for stroking
 * and one for all other painting operations. Initial value: black.</td>
 * </tr>
 * <tr>
 * <td valign="top" >text state</td>
 * <td valign="top" >(various)</td>
 * <td>A set of nine graphics state parameters that pertain only to the
 * painting of text. These include parameters that select the font,
 * scale the glyphs to an appropriate size, and accomplish other effects.
 * The text state parameters are described in Section 5.2, "Text State
 * Parameters and Operators."</td>
 * </tr>
 * <tr>
 * <td valign="top" >line width</td>
 * <td valign="top" >number</td>
 * <td>The thickness, in user space units, of paths to be stroked.</td>
 * </tr>
 * <tr>
 * <td valign="top" >line cap</td>
 * <td valign="top" >integer</td>
 * <td>A code specifying the shape of the endpoints for any open path that
 * is stroked (see "Line Cap Style" on page 186). Initial value: 0, for
 * square butt caps.</td>
 * </tr>
 * <tr>
 * <td valign="top" >line join</td>
 * <td valign="top" >integer</td>
 * <td>A code specifying the shape of joints between connected segments of
 * a stroked path. Initial value: 0, for mitered joins.</td>
 * </tr>
 * <tr>
 * <td valign="top" >miter limit</td>
 * <td valign="top" >number</td>
 * <td>The maximum length of mitered line joins for stroked paths. This
 * parameter limits the length of "spikes" produced when line segments
 * join at sharp angles. Initial value: 10.0, for a miter cutoff below
 * approximately 11.5 degrees.</td>
 * </tr>
 * <tr>
 * <td valign="top" >dash pattern</td>
 * <td valign="top" >array and number</td>
 * <td>A description of the dash pattern to be used when paths are stroked.
 * Initial value: a solid line.</td>
 * </tr>
 * <tr>
 * <td valign="top" >rendering intent (Not supported)</td>
 * <td valign="top" >name</td>
 * <td>The rendering intent to be used when converting CIE-based colors to
 * device colors. Default value: RelativeColorimetric.</td>
 * </tr>
 * <tr>
 * <td valign="top" >stroke adjustment (Not supported)</td>
 * <td valign="top" >boolean</td>
 * <td>(PDF 1.2) A flag specifying whether to compensate for possible
 * rasterization effects when stroking a path with a line width that is
 * small relative to the pixel resolution of the output device..
 * Note that this is considered a device-independent parameter, even
 * though the details of its effects are device-dependent.
 * Initial value: false.</td>
 * </tr>
 * <tr>
 * <td valign="top" >blend mode (Not supported)</td>
 * <td valign="top" >name or array</td>
 * <td>(PDF 1.4) The current blend mode to be used in the transparent
 * imaging model. This parameter is implicitly reset to its initial
 * value at the beginning of execution of a transparency group XObject.
 * Initial value: Normal.</td>
 * </tr>
 * <tr>
 * <td valign="top" >soft mask (Not supported)</td>
 * <td valign="top" >dictionary or name</td>
 * <td>(PDF 1.4) A soft-mask dictionary specifying the mask shape or mask
 * opacity values to be used in the transparent imaging model, or the
 * name None if no such mask is specified. This parameter is implicitly
 * reset to its initial value at the beginning of execution of a
 * transparency group XObject. Initial value: None.</td>
 * </tr>
 * <tr>
 * <td valign="top" >alpha constant</td>
 * <td valign="top" >number</td>
 * <td>(PDF 1.4) The constant shape or constant opacity value to be used in
 * the transparent imaging model. There are two separate alpha constant
 * parameters: one for stroking and one for all other painting
 * operations. This parameter is implicitly reset to its initial value
 * at the beginning of execution of a transparency group XObject.
 * Initial value: 1.0.</td>
 * </tr>
 * <tr>
 * <td valign="top" >alpha source</td>
 * <td valign="top" >boolean</td>
 * <td>(PDF 1.4) A flag specifying whether the current soft mask and alpha
 * constant parameters are to be interpreted as shape values (true) or
 * opacity values (false). This flag also governs the interpretation of
 * the SMask entry, if any, in an image dictionary.
 * Initial value: false.</td>
 * </tr>
 * </table>
 * <p/>
 * <h2>Device-Dependent graphics state parameters - (Table 4.3) </h2>
 * <p><b>Currently Not supported</b></p>
 *
 * @since 1.0
 */
public class GraphicsState {

    private static final Logger logger =
            Logger.getLogger(GraphicsState.class.toString());

    public static final Name CA_STROKING_KEY = new Name("CA");
    public static final Name CA_NON_STROKING_KEY = new Name("ca");

    // allow over paint support for fill and stroke.  Still experimental
    // enabled buy default but can be turned off if required.
    private static boolean enabledOverpaint;

    static {
        enabledOverpaint =
                Defs.sysPropertyBoolean("org.icepdf.core.overpaint",
                        true);
    }


    // Current transformation matrix.
    private AffineTransform CTM;

    private static ClipDrawCmd clipDrawCmd = new ClipDrawCmd();
    private static NoClipDrawCmd noClipDrawCmd = new NoClipDrawCmd();

    // Specifies the shape of the endpoint for any open path.
    private int lineCap;

    // Specifies the thickness in user parse of a path to be stroked.
    private float lineWidth;

    // Specifies the shape of the joints between connected segments.
    private int lineJoin;

    // Maximum length of mitered line join for stroked paths.
    private float miterLimit;

    // Stores the lengths of the dash segments
    private float[] dashArray;

    // Stores the current dash phase
    private float dashPhase;

    // color used to fill a stroked path.
    private Color fillColor;

    // The current color to be used during a painting operation.
    private Color strokeColor;

    // Stroking alpha constant for "CA"
    private float strokeAlpha;

    // Fill alpha constant for "ca" or non-stroking alpha
    private float fillAlpha;

    // Transparency grouping changes which transparency rule is applied.
    // Normally it is simply a SRC_OVER rule but the group can also have isolated
    // and knockout values that directly affect which rule is used for the
    // transparency.
    private ExtGState extGState;
    private int alphaRule;

    private boolean transparencyGroup;
    private boolean isolated;
    private boolean knockOut;

    // Color space of the fill color, non-stroking colour.
    private PColorSpace fillColorSpace;

    // Color space of the stroke color, stroking colour.
    private PColorSpace strokeColorSpace;

    // Set of graphics stat parameter  for painting text.
    private TextState textState;

    // parent graphics state if it exists.
    private GraphicsState parentGraphicState;

    // all shapes associated with this graphics state.
    private Shapes shapes;

    // current clipping area.
    private Area clip;
    private boolean clipChange;

    // over print mode
    private int overprintMode;
    // over printing stroking
    private boolean overprintStroking;
    // over printing everything other than stroking
    private boolean overprintOther;

    /**
     * Constructs a new <code>GraphicsState</code> which will have default
     * values and shapes specified by the shapes stack.
     *
     * @param shapes stack containing pages graphical elements.
     */
    public GraphicsState(Shapes shapes) {
        this.shapes = shapes;
        CTM = new AffineTransform();

        lineCap = BasicStroke.CAP_BUTT;
        lineWidth = 1;
        lineJoin = BasicStroke.JOIN_MITER;
        miterLimit = 10;

        fillColor = Color.black;
        strokeColor = Color.black;
        strokeAlpha = 1.0f;
        fillAlpha = 1.0f;

        alphaRule = AlphaComposite.SRC_OVER;
        fillColorSpace = new DeviceGray(null, null);
        strokeColorSpace = new DeviceGray(null, null);
        textState = new TextState();

    }

    /**
     * Constructs a new <code>GraphicsState</code> that is a copy of
     * the specified <code>GraphicsState</code> object.
     *
     * @param parentGraphicsState the <code>GraphicsState</code> object to copy
     */
    public GraphicsState(GraphicsState parentGraphicsState) {

        // copy/clone the parentGraphicsState and return the new object.

        CTM = new AffineTransform(parentGraphicsState.CTM);

        lineCap = parentGraphicsState.lineCap;
        lineWidth = parentGraphicsState.lineWidth;
        miterLimit = parentGraphicsState.miterLimit;
        lineJoin = parentGraphicsState.lineJoin;


        fillColor = new Color(parentGraphicsState.fillColor.getRGB(), true);

        strokeColor = new Color(parentGraphicsState.strokeColor.getRGB(), true);

        shapes = parentGraphicsState.shapes;
        if (parentGraphicsState.clip != null) {
            clip = (Area) parentGraphicsState.clip.clone();
        }

        fillColorSpace = parentGraphicsState.fillColorSpace;
        strokeColorSpace = parentGraphicsState.strokeColorSpace;
        textState = new TextState(parentGraphicsState.textState);
        dashPhase = parentGraphicsState.dashPhase;
        dashArray = parentGraphicsState.dashArray;

        // copy over printing attributes
        overprintMode = parentGraphicsState.overprintMode;
        overprintOther = parentGraphicsState.overprintOther;
        overprintStroking = parentGraphicsState.overprintStroking;

        // copy the alpha rules
        fillAlpha = parentGraphicsState.fillAlpha;
        strokeAlpha = parentGraphicsState.strokeAlpha;
        alphaRule = parentGraphicsState.alphaRule;

        // extra graphics
        if (parentGraphicsState.getExtGState() != null) {
            extGState = new ExtGState(parentGraphicsState.getExtGState().getLibrary(),
                    parentGraphicsState.getExtGState().getEntries());
        }

        // copy the parent too.
        this.parentGraphicState = parentGraphicsState.parentGraphicState;

    }

    /**
     * Sets the Shapes vector.
     *
     * @param shapes shapes for a given content stream.
     */
    public void setShapes(Shapes shapes) {
        this.shapes = shapes;
    }

    /**
     * Concatenates this transform with a translation transformation specified
     * by the graphics state current CTM.  An updated CTM is added to the
     * shapes stack.
     *
     * @param x the distance by which coordinates are translated in the
     *          X axis direction
     * @param y the distance by which coordinates are translated in the
     *          Y axis direction
     */
    public void translate(double x, double y) {
        CTM.translate(x, y);
        shapes.add(new TransformDrawCmd(new AffineTransform(CTM)));
    }

    /**
     * Concatenates this transform with a scaling transformation specified
     * by the graphics state current CTM.  An update CTM is added to the
     * shapes stack.
     *
     * @param x the factor by which coordinates are scaled along the
     *          X axis direction
     * @param y the factor by which coordinates are scaled along the
     *          Y axis direction
     */
    public void scale(double x, double y) {
        CTM.scale(x, y);
        shapes.add(new TransformDrawCmd(new AffineTransform(CTM)));
    }

    /**
     * Sets the graphics state CTM to a new transform, the old CTM transform is
     * lost.  The new CTM value is added to the shapes stack.
     *
     * @param af the AffineTranform object to set the CTM to.
     */
    public void set(AffineTransform af) {
        // appling a CTM can be expensive, so only do it if it's needed.
        if (!CTM.equals(af)) {
            CTM = new AffineTransform(af);
        }
        shapes.add(new TransformDrawCmd(new AffineTransform(CTM)));
    }

    /**
     * Saves the current graphics state.
     *
     * @return copy of the current graphics state.
     * @see #restore()
     */
    public GraphicsState save() {
        GraphicsState gs = new GraphicsState(this);
        gs.parentGraphicState = this;
        return gs;
    }

    /**
     * Concatenate the specified ExtGState to the current graphics state.
     * <b>Note: </b> currently only a few of the ExtGState attributes are
     * supported.
     *
     * @param extGState external graphics state.
     * @see org.icepdf.core.pobjects.graphics.ExtGState
     */
    public void concatenate(ExtGState extGState) {
        // keep a reference for our partial Transparency group support.
        this.extGState = new ExtGState(extGState.getLibrary(),
                extGState.getEntries());

        // Map over extGState attributes if present.
        // line width
        if (extGState.getLineWidth() != null) {
            setLineWidth(extGState.getLineWidth().floatValue());
        }
        // line cap style
        if (extGState.getLineCapStyle() != null) {
            setLineCap(extGState.getLineCapStyle().intValue());
        }
        // line join style
        if (extGState.getLineJoinStyle() != null) {
            setLineJoin(extGState.getLineJoinStyle().intValue());
        }
        // line miter limit
        if (extGState.getMiterLimit() != null) {
            setMiterLimit(extGState.getMiterLimit().floatValue());
        }
        // line dash pattern
        if (extGState.getLineDashPattern() != null) {
            List dasshPattern = extGState.getLineDashPattern();
            try {
                setDashArray((float[]) dasshPattern.get(0));
                setDashPhase(((Number) dasshPattern.get(1)).floatValue());
            } catch (ClassCastException e) {
                logger.log(Level.FINE, "Dash cast error: ", e);
            }
        }
        // Stroking alpha
        if (extGState.getNonStrokingAlphConstant() != -1) {
            setFillAlpha(extGState.getNonStrokingAlphConstant());
        }
        // none stroking alpha
        if (extGState.getStrokingAlphConstant() != -1) {
            setStrokeAlpha(extGState.getStrokingAlphConstant());
        }

        setOverprintMode(extGState.getOverprintMode());

        // apply over print logic
        processOverPaint(extGState.getOverprint(), extGState.getOverprintFill());
    }

    /**
     * Process the OP and op over printing attributes.
     *
     * @param OP OP graphics state param
     * @param op op graphics state param
     */
    private void processOverPaint(Boolean OP, Boolean op) {
        // PDF 1.2 and earlier, only a single overprint parameter
        if (enabledOverpaint &&
                OP != null && op == null && overprintMode == 1) {
            overprintStroking = OP;
            overprintOther = overprintStroking;
        }
        // PDF 1.2 and over
//        else if (OP != null) {
//            overprintStroking = OP.booleanValue();
//            overprintOther = op.booleanValue();
//        }
        // else default inits of false for each is fine.
    }


    /**
     * Restores the previously saved graphic state.
     *
     * @return the last saved graphics state.
     * @see #save()
     */
    public GraphicsState restore() {
        // make sure we have a parent to restore to
        if (parentGraphicState != null) {
            // Add the parents CTM to the stack,
            parentGraphicState.set(parentGraphicState.CTM);
            // Add the parents clip to the stack
            if (clipChange) {
                if (parentGraphicState.clip != null) {
                    if (!parentGraphicState.clip.equals(clip)) {
                        parentGraphicState.shapes.add(new ShapeDrawCmd(new Area(parentGraphicState.clip)));
                        parentGraphicState.shapes.add(clipDrawCmd);
                    }
                } else {
                    parentGraphicState.shapes.add(noClipDrawCmd);
                }
            }
            // Update the stack with the parentGraphicsState stack.
            parentGraphicState.shapes.add(new StrokeDrawCmd(
                    new BasicStroke(parentGraphicState.lineWidth,
                            parentGraphicState.lineCap,
                            parentGraphicState.lineJoin,
                            parentGraphicState.miterLimit,
                            parentGraphicState.dashArray,
                            parentGraphicState.dashPhase)));

            // Note the following aren't officially part of the graphic state parameters
            // but they need to be restored in order to show some PDF content correctly

            // restore the fill color of the last paint
            parentGraphicState.shapes.add(new ColorDrawCmd(parentGraphicState.getFillColor()));

            // apply the old alpha fill
            if (fillAlpha != parentGraphicState.getFillAlpha()) {
                parentGraphicState.shapes.add(new AlphaDrawCmd(
                        AlphaComposite.getInstance(parentGraphicState.getAlphaRule(), parentGraphicState.getFillAlpha())));
            }

            if (strokeAlpha != parentGraphicState.getStrokeAlpha()) {
                parentGraphicState.shapes.add(new AlphaDrawCmd(
                        AlphaComposite.getInstance(parentGraphicState.getAlphaRule(), parentGraphicState.getStrokeAlpha())));
            }
            // stroke Color
//            parentGraphicState.shapes.add(parentGraphicState.getStrokeColor());
        }

        return parentGraphicState;
    }

    /**
     * Updates the clip every time the tranformation matrix is updated.  This is
     * needed to insure that when a new shape is interesected with the
     * previous clip that they are both in the same space.
     *
     * @param af transform to be applied to the clip.
     */
    public void updateClipCM(AffineTransform af) {
        // we need to update the current clip with the new transform, which is
        // actually the inverse of the matrix defined by "cm" in the content
        // parser.

        if (clip != null) {
            // get the inverse of the transform and apply it to clipCM
            AffineTransform afInverse = new AffineTransform();
            try {
                afInverse = af.createInverse();
            } catch (Exception e) {
                logger.log(Level.FINER, "Error generating clip inverse.", e);
            }

            // transform the clip.
            clip.transform(afInverse);
        }
    }

    /**
     * Set the graphics state clipping area.  The new clipping area is
     * intersected with the current current clip to generate the new clipping
     * area which is saved to the stack.
     *
     * @param newClip new clip for graphic state.
     */
    public void setClip(Shape newClip) {
        if (newClip != null) {
            // intersect can only be calculated on a an area.
            Area area = new Area(newClip);
            // make sure the clip is not null
            if (clip != null) {
                area.intersect(clip);
            }
            // update the clip with the new value if it is new.
            if (clip == null || !clip.equals(area)) {
                clip = new Area(area);
                shapes.add(new ShapeDrawCmd(area));
                shapes.add(clipDrawCmd);
                clipChange = true;
                if (parentGraphicState != null) parentGraphicState.clipChange = true;
            } else {
                clip = new Area(area);
            }
        } else {
            // add a null clip for a null shape, should not normally happen
            clip = null;
            shapes.add(noClipDrawCmd);
            clipChange = true;
            if (parentGraphicState != null) parentGraphicState.clipChange = true;
        }

    }

    public Area getClip() {
        return clip;
    }

    public AffineTransform getCTM() {
        return CTM;
    }

    public void setCTM(AffineTransform ctm) {
        CTM = ctm;
    }

    public int getLineCap() {
        return lineCap;
    }

    public void setLineCap(int lineCap) {
        this.lineCap = lineCap;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        // Automatic Stroke Adjustment
        if (lineWidth <= Float.MIN_VALUE || lineWidth >= Float.MAX_VALUE ||
                lineWidth == 0) {
            // set line width to a very small none zero number. 
            this.lineWidth = 0.001f;
        } else {
            this.lineWidth = lineWidth;
        }

    }

    public int getLineJoin() {
        return lineJoin;
    }

    public void setLineJoin(int lineJoin) {
        this.lineJoin = lineJoin;
    }

    public float[] getDashArray() {
        return dashArray;
    }

    public void setDashArray(float[] dashArray) {
        this.dashArray = dashArray;
    }

    public float getDashPhase() {
        return dashPhase;
    }

    public void setDashPhase(float dashPhase) {
        this.dashPhase = dashPhase;
    }

    public float getMiterLimit() {
        return miterLimit;
    }

    public void setMiterLimit(float miterLimit) {
        this.miterLimit = miterLimit;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public Color getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeAlpha(float alpha) {
        if (alpha > 1.0f) {
            alpha = 1.0f;
        }
        strokeAlpha = alpha;
    }

    public float getStrokeAlpha() {
        return strokeAlpha;
    }

    public void setFillAlpha(float alpha) {
        if (alpha > 1.0f) {
            alpha = 1.0f;
        }
        fillAlpha = alpha;
    }

    public float getFillAlpha() {
        return fillAlpha;
    }

    public void setStrokeColor(Color strokeColor) {
        this.strokeColor = strokeColor;
    }

    public PColorSpace getFillColorSpace() {
        return fillColorSpace;
    }

    public void setFillColorSpace(PColorSpace fillColorSpace) {
        this.fillColorSpace = fillColorSpace;
    }

    public PColorSpace getStrokeColorSpace() {
        return strokeColorSpace;
    }

    public void setStrokeColorSpace(PColorSpace strokeColorSpace) {
        this.strokeColorSpace = strokeColorSpace;
    }

    public TextState getTextState() {
        return textState;
    }

    public void setTextState(TextState textState) {
        this.textState = textState;
    }

    public int getOverprintMode() {
        return overprintMode;
    }

    public boolean isOverprintStroking() {
        return overprintStroking;
    }

    public boolean isOverprintOther() {
        return overprintOther;
    }

    public void setOverprintMode(int overprintMode) {
        this.overprintMode = overprintMode;
    }

    public void setOverprintStroking(boolean overprintStroking) {
        this.overprintStroking = overprintStroking;
    }

    public void setOverprintOther(boolean overprintOther) {
        this.overprintOther = overprintOther;
    }

    public int getAlphaRule() {
        return alphaRule;
    }

    public void setAlphaRule(int alphaRule) {
        this.alphaRule = alphaRule;
    }

    public boolean isTransparencyGroup() {
        return transparencyGroup;
    }

    public void setTransparencyGroup(boolean transparencyGroup) {
        this.transparencyGroup = transparencyGroup;
    }

    public boolean isIsolated() {
        return isolated;
    }

    public void setIsolated(boolean isolated) {
        this.isolated = isolated;
    }

    public boolean isKnockOut() {
        return knockOut;
    }

    public void setKnockOut(boolean knockOut) {
        this.knockOut = knockOut;
    }

    public ExtGState getExtGState() {
        return extGState;
    }
}

