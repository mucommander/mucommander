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
package org.icepdf.core.pobjects.annotations;

import org.icepdf.core.pobjects.*;
import org.icepdf.core.pobjects.graphics.Shapes;
import org.icepdf.core.util.Library;
import org.icepdf.core.util.content.ContentParser;
import org.icepdf.core.util.content.ContentParserFactory;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A text annotation represents a “sticky note” attached to a point in the PDF
 * document. When closed, the annotation shall appear as an icon; when open, it
 * shall display a pop-up window containing the text of the note in a font and
 * size chosen by the conforming reader. Text annotations shall not scale and
 * rotate with the page; they shall behave as if the NoZoom and NoRotate annotation
 * flags (see Table 165) were always set. Table 172shows the annotation dictionary
 * entries specific to this type of annotation.
 *
 * @since 5.0
 */
public class TextAnnotation extends MarkupAnnotation {

    private static final Logger logger =
            Logger.getLogger(TextAnnotation.class.toString());

    /**
     * (Optional) A flag specifying whether the annotation shall initially be
     * displayed open. Default value: false (closed).
     */
    public static final Name OPEN_KEY = new Name("Open");
    /**
     * Optional) The name of an icon that shall be used in displaying the annotation.
     * Conforming readers shall provide predefined icon appearances for at least
     * the following standard names:
     * <p/>
     * Comment, Key, Note, Help, NewParagraph, Paragraph, Insert
     * <p/>
     * Additional names may be supported as well. Default value: Note.
     * <p/>
     * The annotation dictionary’s AP entry, if present, shall take precedence
     * over the Name entry; see Table 168 and 12.5.5, “Appearance Streams.”
     */
    public static final Name NAME_KEY = new Name("Name");
    /**
     * (Optional; PDF 1.5) The state to which the original annotation shall be
     * set; see 12.5.6.3, “Annotation States.”
     * <p/>
     * Default: “Unmarked” if StateModel is “Marked”; “None” if StateModel is “Review”.
     */
    public static final Name STATE_KEY = new Name("State");
    /**
     * (Required if State is present, otherwise optional; PDF 1.5) The state model
     * corresponding to State; see 12.5.6.3, “Annotation States.”
     */
    public static final Name STATE_MODEL_KEY = new Name("StateModel");
    /**
     * Named text icon times.
     */

    public static final Name COMMENT_ICON = new Name("Comment");
    public static final Name CHECK_ICON = new Name("Check");
    public static final Name CHECK_MARK_ICON = new Name("CheckMark");
    public static final Name CIRCLE_ICON = new Name("Circle");
    public static final Name CROSS_ICON = new Name("Cross");
    public static final Name CROSS_HAIRS_ICON = new Name("CrossHairs");
    public static final Name HELP_ICON = new Name("Help");
    public static final Name INSERT_ICON = new Name("Insert");
    public static final Name KEY_ICON = new Name("Key");
    public static final Name NEW_PARAGRAPH_ICON = new Name("NewParagraph");
    public static final Name PARAGRAPH_ICON = new Name("Paragraph");
    public static final Name RIGHT_ARROW_ICON = new Name("RightArrow");
    public static final Name RIGHT_POINTER_ICON = new Name("RightPointer");
    public static final Name STAR_ICON = new Name("Star");
    public static final Name UP_ARROW_ICON = new Name("UpArrow");
    public static final Name UP_LEFT_ARROW_ICON = new Name("UpLeftArrow");
    /**
     * State Models
     */
    public static final String STATE_MODEL_MARKED = "Marked";
    public static final String STATE_MODEL_REVIEW = "Review";
    /**
     * State names.
     */
    public static final String STATE_MARKED = "Marked";
    public static final String STATE_UNMARKED = "Unmarked";
    public static final String STATE_ACCEPTED = "Accepted";
    public static final String STATE_REJECTED = "Rejected";
    public static final String STATE_CANCELLED = "Cancelled";
    public static final String STATE_COMPLETED = "Completed";
    public static final String STATE_REVIEW_NONE = "None";

    protected boolean open;
    protected Name iconName = COMMENT_ICON;
    protected String state;
    protected String stateModel;
    public TextAnnotation(Library l, HashMap h) {
        super(l, h);
    }

    public void init() throws InterruptedException {
        super.init();
        // open state
        open = library.getBoolean(entries, OPEN_KEY);

        // state
        Object value = library.getObject(entries, STATE_KEY);
        if (value != null && value instanceof StringObject) {
            StringObject text = (StringObject) value;
            state = text.getDecryptedLiteralString(securityManager);
        } else if (value instanceof String) {
            state = (String) value;
        }

        // icon name
        value = library.getName(entries, NAME_KEY);
        if (value != null) {
            iconName = (Name) value;
        }

        // state model
        value = library.getObject(entries, STATE_MODEL_KEY);
        if (value != null && value instanceof StringObject) {
            StringObject text = (StringObject) value;
            stateModel = text.getDecryptedLiteralString(securityManager);
        } else if (value instanceof String) {
            stateModel = (String) value;
        }

        // try and generate an appearance stream.
        resetNullAppearanceStream();
    }

    /**
     * Gets an instance of a TextAnnotation that has valid Object Reference.
     *
     * @param library document library
     * @param rect    bounding rectangle in user space
     * @return new TextAnnotation Instance.
     */
    public static TextAnnotation getInstance(Library library,
                                             Rectangle rect) {
        // state manager
        StateManager stateManager = library.getStateManager();

        // create a new entries to hold the annotation properties
        HashMap<Name, Object> entries = new HashMap<Name, Object>();
        // set default link annotation values.
        entries.put(Dictionary.TYPE_KEY, Annotation.TYPE_VALUE);
        entries.put(Dictionary.SUBTYPE_KEY, Annotation.SUBTYPE_TEXT);
        // rotation and scale locking
        entries.put(Annotation.FLAG_KEY, 28);
        // coordinates
        if (rect != null) {
            entries.put(Annotation.RECTANGLE_KEY,
                    PRectangle.getPRectangleVector(rect));
        } else {
            entries.put(Annotation.RECTANGLE_KEY, new Rectangle2D.Float(10, 10, 50, 100));
        }

        // create the new instance
        TextAnnotation textAnnotation = null;
        try {
            textAnnotation = new TextAnnotation(library, entries);
            textAnnotation.init();
            textAnnotation.setPObjectReference(stateManager.getNewReferencNumber());
            textAnnotation.setNew(true);

            // set default flags.
            textAnnotation.setFlag(Annotation.FLAG_READ_ONLY, false);
            textAnnotation.setFlag(Annotation.FLAG_NO_ROTATE, true);
            textAnnotation.setFlag(Annotation.FLAG_NO_ZOOM, true);
            textAnnotation.setFlag(Annotation.FLAG_PRINT, true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.fine("Text annotation instance creation was interrupted");
        }

        return textAnnotation;
    }

    /**
     * Resets the annotations appearance stream.
     */
    public void resetAppearanceStream(double dx, double dy, AffineTransform pageTransform) {
        // setup the context
        Appearance appearance = appearances.get(currentAppearance);
        AppearanceState appearanceState = appearance.getSelectedAppearanceState();

        appearanceState.setMatrix(new AffineTransform());
        appearanceState.setShapes(new Shapes());

        Rectangle2D bbox = appearanceState.getBbox();
        bbox.setRect(0, 0, bbox.getWidth(), bbox.getHeight());
        // setup the AP stream.
        setModifiedDate(PDate.formatDateTime(new Date()));

        AffineTransform matrix = appearanceState.getMatrix();
        Shapes shapes;

        String iconContentString;
        // get the correct icon content
        if (iconName.equals(COMMENT_ICON)) {
            iconContentString = COMMENT_CONTENT_STREAM;
        } else if (iconName.equals(CHECK_ICON)) {
            iconContentString = CHECK_CONTENT_STREAM;
        } else if (iconName.equals(CHECK_MARK_ICON)) {
            iconContentString = CHECK_MARK_CONTENT_STREAM;
        } else if (iconName.equals(CIRCLE_ICON)) {
            iconContentString = CIRCLE_CONTENT_STREAM;
        } else if (iconName.equals(CROSS_ICON)) {
            iconContentString = CROSS_CONTENT_STREAM;
        } else if (iconName.equals(CROSS_HAIRS_ICON)) {
            iconContentString = CROSS_HAIRS_CONTENT_STREAM;
        } else if (iconName.equals(HELP_ICON)) {
            iconContentString = HELP_CONTENT_STREAM;
        } else if (iconName.equals(INSERT_ICON)) {
            iconContentString = INSERT_CONTENT_STREAM;
        } else if (iconName.equals(KEY_ICON)) {
            iconContentString = KEY_CONTENT_STREAM;
        } else if (iconName.equals(NEW_PARAGRAPH_ICON)) {
            iconContentString = NEW_PARAGRAPH_CONTENT_STREAM;
        } else if (iconName.equals(PARAGRAPH_ICON)) {
            iconContentString = PARAGRAPH_CONTENT_STREAM;
        } else if (iconName.equals(RIGHT_ARROW_ICON)) {
            iconContentString = RIGHT_ARROW_CONTENT_STREAM;
        } else if (iconName.equals(RIGHT_POINTER_ICON)) {
            iconContentString = RIGHT_POINTER_CONTENT_STREAM;
        } else if (iconName.equals(STAR_ICON)) {
            iconContentString = STAR_CONTENT_STREAM;
        } else if (iconName.equals(UP_ARROW_ICON)) {
            iconContentString = UP_ARROW_CONTENT_STREAM;
        } else if (iconName.equals(UP_LEFT_ARROW_ICON)) {
            iconContentString = UP_LEFT_ARROW_CONTENT_STREAM;
        } else {
            iconContentString = COMMENT_CONTENT_STREAM;
        }
        //  need to make sure we have a colour so we can generate the content stream.
        if (color == null) {
            color = Color.YELLOW;
        }
        float[] compArray = new float[3];
        color.getColorComponents(compArray);
        StringBuilder colorString = new StringBuilder()
                .append(compArray[0]).append(" ")
                .append(compArray[1]).append(" ")
                .append(compArray[2]);
        // apply the colour
        Object[] colorArgument = new Object[]{EXT_GSTATE_NAME, colorString};
        MessageFormat formatter = new MessageFormat(iconContentString);
        iconContentString = formatter.format(colorArgument);

        Form form = updateAppearanceStream(null, bbox, matrix, null);
        generateExternalGraphicsState(form, opacity);
        // parse the shapes and assign to this instance
        try {
            Resources resources = form.getResources();
            ContentParser cp = ContentParserFactory.getInstance().getContentParser(library, resources);
            shapes = cp.parse(new byte[][]{iconContentString.getBytes()}, null).getShapes();
        } catch (Exception e) {
            shapes = new Shapes();
            logger.log(Level.FINEST, "Error building named icon.", e);
        }

        // update the appearance stream
        // create/update the appearance stream of the xObject.
        form = updateAppearanceStream(shapes, bbox, matrix, iconContentString.getBytes());
//        generateExternalGraphicsState(form, opacity);
        if (form != null) {
            appearanceState.setShapes(shapes);
        }
    }

    public boolean isOpen() {
        return open;
    }

    public Name getIconName() {
        return iconName;
    }

    public String getState() {
        return state;
    }

    public String getStateModel() {
        return stateModel;
    }

    public void setOpen(boolean open) {
        this.open = open;
        entries.put(OPEN_KEY, open);
    }

    public void setIconName(Name iconName) {
        this.iconName = iconName;
        entries.put(NAME_KEY, iconName);
    }

    public void setState(String state) {
        this.state = state;
        setString(STATE_KEY, state);
    }

    public void setStateModel(String stateModel) {
        this.stateModel = stateModel;
        setString(STATE_KEY, stateModel);
    }

    // comment name streams.

    public static final String COMMENT_CONTENT_STREAM =
            "q 1 1 1 rg 0 i 1 w 4 M 1 j 0 J []0 d 1 0 0 1 9 5.0908 cm /{0} gs 7.74 12.616 m -7.74 12.616 l -8.274 12.616 -8.707 12.184 -8.707 11.649 c -8.707 -3.831 l -8.707 -4.365 -8.274 -4.798 -7.74 -4.798 c 7.74 -4.798 l 8.274 -4.798 8.707 -4.365 8.707 -3.831 c 8.707 11.649 l 8.707 12.184 8.274 12.616 7.74 12.616 c h f Q 0 G {1} rg 0 i 0.60 w 4 M 1 j 0 J []0 d {1} rg 0 G 0 i 0.59 w 4 M 1 j 0 J []0 d /{0} gs 1 0 0 1 9 5.0908 cm 0 0 m -0.142 0 -0.28 0.008 -0.418 0.015 c -2.199 -1.969 -5.555 -2.242 -4.642 -1.42 c -4.024 -0.862 -3.916 0.111 -3.954 0.916 c -5.658 1.795 -6.772 3.222 -6.772 4.839 c -6.772 7.509 -3.74 9.674 0 9.674 c 3.74 9.674 6.772 7.509 6.772 4.839 c 6.772 2.167 3.74 0 0 0 c 7.74 12.616 m -7.74 12.616 l -8.274 12.616 -8.707 12.184 -8.707 11.649 c -8.707 -3.831 l -8.707 -4.365 -8.274 -4.798 -7.74 -4.798 c 7.74 -4.798 l 8.274 -4.798 8.707 -4.365 8.707 -3.831 c 8.707 11.649 l 8.707 12.184 8.274 12.616 7.74 12.616 c b";
    public static final String CHECK_CONTENT_STREAM =
            "{1} rg 0 G 0 i 0.59 w 4 M 1 j 0 J []0 d  1 0 0 1 7.1836 1.2061 cm /{0} gs 0 0 m 6.691 11.152 11.31 14.196 v 10.773 15.201 9.626 16.892 8.155 17.587 c 2.293 10.706 -0.255 4.205 y -4.525 9.177 l -6.883 5.608 l h b";
    public static final String CHECK_MARK_CONTENT_STREAM =
            "q 0.396 0.396 0.396 rg 1 0 0 1 13.5151 16.5 cm /{0} gs 0 0 m -6.7 -10.23 l -8.81 -7 l -13.22 -7 l -6.29 -15 l 4.19 0 l h f Q ";
    public static final String CIRCLE_CONTENT_STREAM =
            "q 1 1 1 rg 0 i 1 w 4 M 1 j 0 J []0 d /{0} gs 1 0 0 1 9.999 3.6387 cm 0 0 m -3.513 0 -6.36 2.85 -6.36 6.363 c -6.36 9.875 -3.513 12.724 0 12.724 c 3.514 12.724 6.363 9.875 6.363 6.363 c 6.363 2.85 3.514 0 0 0 c h f Q {1} rg 0 G 0 i 0.59 w 4 M 1 j 0 J []0 d /{0} gs 1 0 0 1 9.999 3.6387 cm 0 0 m -3.513 0 -6.36 2.85 -6.36 6.363 c -6.36 9.875 -3.513 12.724 0 12.724 c 3.514 12.724 6.363 9.875 6.363 6.363 c 6.363 2.85 3.514 0 0 0 c 0 16.119 m -5.388 16.119 -9.756 11.751 -9.756 6.363 c -9.756 0.973 -5.388 -3.395 0 -3.395 c 5.391 -3.395 9.757 0.973 9.757 6.363 c 9.757 11.751 5.391 16.119 0 16.119 c b";
    public static final String CROSS_CONTENT_STREAM =
            "{1} rg 0 G 0 i 0.59 w 4 M 1 j 0 J []0 d /{0} gs 1 0 0 1 18.6924 3.1357 cm 0 0 m -6.363 6.364 l 0 12.728 l -2.828 15.556 l -9.192 9.192 l -15.556 15.556 l -18.384 12.728 l -12.02 6.364 l -18.384 0 l -15.556 -2.828 l -9.192 3.535 l -2.828 -2.828 l h b";
    public static final String CROSS_HAIRS_CONTENT_STREAM =
            "q 1 1 1 rg 0 i 1 w 4 M 1 j 0 J []0 d /{0} gs 1 0 0 1 9.9771 1.9443 cm 0 0 m -4.448 0 -8.053 3.604 -8.053 8.053 c -8.053 12.5 -4.448 16.106 0 16.106 c 4.447 16.106 8.054 12.5 8.054 8.053 c 8.054 3.604 4.447 0 0 0 c h f Q {1} rg 0 G 0 i 0.61 w 4 M 0 j 0 J []0 d /{0} gs q 1 0 0 1 9.9771 1.9443 cm 0 0 m -4.448 0 -8.053 3.604 -8.053 8.053 c -8.053 12.5 -4.448 16.106 0 16.106 c 4.447 16.106 8.054 12.5 8.054 8.053 c 8.054 3.604 4.447 0 0 0 c 0 17.716 m -5.336 17.716 -9.663 13.39 -9.663 8.053 c -9.663 2.716 -5.336 -1.61 0 -1.61 c 5.337 -1.61 9.664 2.716 9.664 8.053 c 9.664 13.39 5.337 17.716 0 17.716 c b Q q 1 0 0 1 10.7861 14.8325 cm 0 0 m -1.611 0 l -1.611 -4.027 l -5.638 -4.027 l -5.638 -5.638 l -1.611 -5.638 l -1.611 -9.665 l 0 -9.665 l 0 -5.638 l 4.026 -5.638 l 4.026 -4.027 l 0 -4.027 l h b Q";
    public static final String HELP_CONTENT_STREAM =
            "q 1 1 1 rg 0 i 1 w 4 M 1 j 0 J []0 d /{0} gs 1 0 0 1 12.1465 10.5137 cm -2.146 9.403 m -7.589 9.403 -12.001 4.99 -12.001 -0.453 c -12.001 -5.895 -7.589 -10.309 -2.146 -10.309 c 3.296 -10.309 7.709 -5.895 7.709 -0.453 c 7.709 4.99 3.296 9.403 -2.146 9.403 c h f Q {1} rg 0 G 0 i 0.59 w 4 M 1 j 0 J []0 d /{0} gs 1 0 0 1 12.1465 10.5137 cm 0 0 m -0.682 -0.756 -0.958 -1.472 -0.938 -2.302 c -0.938 -2.632 l -3.385 -2.632 l -3.403 -2.154 l -3.459 -1.216 -3.147 -0.259 -2.316 0.716 c -1.729 1.433 -1.251 2.022 -1.251 2.647 c -1.251 3.291 -1.674 3.715 -2.594 3.751 c -3.202 3.751 -3.937 3.531 -4.417 3.2 c -5.041 5.205 l -4.361 5.591 -3.274 5.959 -1.968 5.959 c 0.46 5.959 1.563 4.616 1.563 3.089 c 1.563 1.691 0.699 0.771 0 0 c -2.227 -6.863 m -2.245 -6.863 l -3.202 -6.863 -3.864 -6.146 -3.864 -5.189 c -3.864 -4.196 -3.182 -3.516 -2.227 -3.516 c -1.233 -3.516 -0.589 -4.196 -0.57 -5.189 c -0.57 -6.146 -1.233 -6.863 -2.227 -6.863 c -2.146 9.403 m -7.589 9.403 -12.001 4.99 -12.001 -0.453 c -12.001 -5.895 -7.589 -10.309 -2.146 -10.309 c 3.296 -10.309 7.709 -5.895 7.709 -0.453 c 7.709 4.99 3.296 9.403 -2.146 9.403 c b";
    public static final String INSERT_CONTENT_STREAM =
            "0 G {1} rg 0 i 0.59 w 4 M 0 j 0 J []0 d /{0} gs 1 0 0 1 8.5386 19.8545 cm 0 0 m -8.39 -19.719 l 8.388 -19.719 l h B";
    public static final String KEY_CONTENT_STREAM =
            "q 1 1 1 rg 0 i 1 w 4 M 1 j 0 J []0 d /{0} gs 1 0 0 1 6.5 12.6729 cm 0.001 5.138 m -2.543 5.138 -4.604 3.077 -4.604 0.534 c -4.604 -1.368 -3.449 -3.001 -1.802 -3.702 c -1.802 -4.712 l -0.795 -5.719 l -1.896 -6.82 l -0.677 -8.039 l -1.595 -8.958 l -0.602 -9.949 l -1.479 -10.829 l -0.085 -12.483 l 1.728 -10.931 l 1.728 -3.732 l 1.737 -3.728 1.75 -3.724 1.76 -3.721 c 3.429 -3.03 4.604 -1.385 4.604 0.534 c 4.604 3.077 2.542 5.138 0.001 5.138 c f Q {1} rg 0 G 0 i 0.59 w 4 M 1 j 0 J []0 d /{0} gs 1 0 0 1 6.5 12.6729 cm 0 0 m -1.076 0 -1.95 0.874 -1.95 1.95 c -1.95 3.028 -1.076 3.306 0 3.306 c 1.077 3.306 1.95 3.028 1.95 1.95 c 1.95 0.874 1.077 0 0 0 c 0.001 5.138 m -2.543 5.138 -4.604 3.077 -4.604 0.534 c -4.604 -1.368 -3.449 -3.001 -1.802 -3.702 c -1.802 -4.712 l -0.795 -5.719 l -1.896 -6.82 l -0.677 -8.039 l -1.595 -8.958 l -0.602 -9.949 l -1.479 -10.829 l -0.085 -12.483 l 1.728 -10.931 l 1.728 -3.732 l 1.737 -3.728 1.75 -3.724 1.76 -3.721 c 3.429 -3.03 4.604 -1.385 4.604 0.534 c 4.604 3.077 2.542 5.138 0.001 5.138 c b";
    public static final String NEW_PARAGRAPH_CONTENT_STREAM =
            "{1} rg 0 G 0 i 0.58 w 4 M 0 j 0 J []0 d /{0} gs  {1} rg 0 G 0 i 0.59 w 4 M 1 j 0 J []0 d /{0} gs  q 1 0 0 1 6.4995 20 cm 0 0 m -6.205 -12.713 l 6.205 -12.713 l h b Q q 1 0 0 1 1.1909 6.2949 cm 0 0 m 1.278 0 l 1.353 0 1.362 -0.02 1.391 -0.066 c 2.128 -1.363 3.78 -4.275 3.966 -4.713 c 3.985 -4.713 l 3.976 -4.453 3.957 -3.91 3.957 -3.137 c 3.957 -0.076 l 3.957 -0.02 3.976 0 4.041 0 c 4.956 0 l 5.021 0 5.04 -0.029 5.04 -0.084 c 5.04 -6.049 l 5.04 -6.113 5.021 -6.133 4.947 -6.133 c 3.695 -6.133 l 3.621 -6.133 3.611 -6.113 3.574 -6.066 c 3.052 -4.955 1.353 -2.063 0.971 -1.186 c 0.961 -1.186 l 0.999 -1.68 0.999 -2.146 1.008 -3.025 c 1.008 -6.049 l 1.008 -6.104 0.989 -6.133 0.933 -6.133 c 0.009 -6.133 l -0.046 -6.133 -0.075 -6.123 -0.075 -6.049 c -0.075 -0.066 l -0.075 -0.02 -0.056 0 0 0 c f Q q 1 0 0 1 9.1367 3.0273 cm 0 0 m 0.075 0 0.215 -0.008 0.645 -0.008 c 1.4 -0.008 2.119 0.281 2.119 1.213 c 2.119 1.969 1.633 2.381 0.737 2.381 c 0.354 2.381 0.075 2.371 0 2.361 c h -1.146 3.201 m -1.146 3.238 -1.129 3.268 -1.082 3.268 c -0.709 3.275 0.02 3.285 0.729 3.285 c 2.613 3.285 3.248 2.314 3.258 1.232 c 3.258 -0.27 2.007 -0.914 0.607 -0.914 c 0.327 -0.914 0.057 -0.914 0 -0.904 c 0 -2.789 l 0 -2.836 -0.019 -2.865 -0.074 -2.865 c -1.082 -2.865 l -1.119 -2.865 -1.146 -2.846 -1.146 -2.799 c h f Q";
    public static final String PARAGRAPH_CONTENT_STREAM =
            "q 1 1 1 rg 0 i 1 w 4 M 1 j 0 J []0 d /{0} gs 1 0 0 1 19.6973 10.0005 cm 0 0 m 0 -5.336 -4.326 -9.662 -9.663 -9.662 c -14.998 -9.662 -19.324 -5.336 -19.324 0 c -19.324 5.335 -14.998 9.662 -9.663 9.662 c -4.326 9.662 0 5.335 0 0 c h f Q {1} rg 0 G 0 i 0.59 w 4 M 1 j 0 J []0 d /{0} gs q 1 0 0 1 19.6973 10.0005 cm 0 0 m 0 -5.336 -4.326 -9.662 -9.663 -9.662 c -14.998 -9.662 -19.324 -5.336 -19.324 0 c -19.324 5.335 -14.998 9.662 -9.663 9.662 c -4.326 9.662 0 5.335 0 0 c h S Q q 1 0 0 1 11.6787 2.6582 cm 0 0 m -1.141 0 l -1.227 0 -1.244 0.052 -1.227 0.139 c -0.656 1.157 -0.52 2.505 -0.52 3.317 c -0.52 3.594 l -2.833 3.783 -5.441 4.838 -5.441 8.309 c -5.441 10.778 -3.714 12.626 -0.57 13.024 c -0.535 13.508 -0.381 14.129 -0.242 14.389 c -0.207 14.44 -0.174 14.475 -0.104 14.475 c 1.088 14.475 l 1.156 14.475 1.191 14.458 1.175 14.372 c 1.105 14.095 0.881 13.127 0.881 12.402 c 0.881 9.431 0.932 7.324 0.95 4.06 c 0.95 2.298 0.708 0.813 0.189 0.07 c 0.155 0.034 0.103 0 0 0 c b Q";
    public static final String RIGHT_ARROW_CONTENT_STREAM =
            "q 1 1 1 rg 0 i 1 w 4 M 1 j 0 J []0 d /{0} gs 1 0 0 1 3.7856 11.1963 cm 6.214 -10.655 m 11.438 -10.655 15.673 -6.42 15.673 -1.196 c 15.673 4.027 11.438 8.262 6.214 8.262 c 0.991 8.262 -3.244 4.027 -3.244 -1.196 c -3.244 -6.42 0.991 -10.655 6.214 -10.655 c h f Q {1} rg 0 G 0 i 0.59 w 4 M 0 j 0 J []0 d /{0} gs 1 0 0 1 3.7856 11.1963 cm 0 0 m 8.554 0 l 6.045 2.51 l 7.236 3.702 l 12.135 -1.197 l 7.236 -6.096 l 6.088 -4.949 l 8.644 -2.394 l 0 -2.394 l h 6.214 -10.655 m 11.438 -10.655 15.673 -6.42 15.673 -1.196 c 15.673 4.027 11.438 8.262 6.214 8.262 c 0.991 8.262 -3.244 4.027 -3.244 -1.196 c -3.244 -6.42 0.991 -10.655 6.214 -10.655 c b";
    public static final String RIGHT_POINTER_CONTENT_STREAM =
            "{1} rg 0 G 0.59 w 4 M 0 j 0 J []0 d /{0} gs 1 0 0 1 1.1871 17.0000 cm 0 0 m 4.703 -8.703 l 0 -17 l 18.813 -8.703 l b";
    public static final String STAR_CONTENT_STREAM =
            "{1} rg 0 G 0 i 0.59 w 4 M 1 j 0 J []0 d /{0} gs 1 0 0 1 9.999 18.8838 cm 0 0 m 3.051 -6.178 l 9.867 -7.168 l 4.934 -11.978 l 6.099 -18.768 l 0 -15.562 l -6.097 -18.768 l -4.933 -11.978 l -9.866 -7.168 l -3.048 -6.178 l b";
    public static final String UP_ARROW_CONTENT_STREAM =
            "{1} rg 0 G 0 i 0.59 w 4 M 1 j 0 J []0 d /{0} gs 1 0 0 1 1.1007 6.7185 cm 0 0 m 4.009 0 l 4.009 -6.719 l 11.086 -6.719 l 11.086 0 l 14.963 0 l 7.499 13.081 l b";
    public static final String UP_LEFT_ARROW_CONTENT_STREAM =
            "{1} rg 0 G 0 i 0.59 w 4 M 1 j 0 J []0 d /{0} gs 1 0 0 1 2.8335 1.7627 cm 0 0 m -2.74 15.16 l 12.345 12.389 l 9.458 9.493 l 14.027 4.91 l 7.532 -1.607 l 2.964 2.975 l b";

}
