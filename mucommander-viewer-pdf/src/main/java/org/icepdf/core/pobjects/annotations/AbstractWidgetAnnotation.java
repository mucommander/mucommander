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

import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.Resources;
import org.icepdf.core.pobjects.acroform.FieldDictionary;
import org.icepdf.core.pobjects.acroform.InteractiveForm;
import org.icepdf.core.util.ColorUtil;
import org.icepdf.core.util.Defs;
import org.icepdf.core.util.Library;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract base class for Widget annotations types, button, choice and text.
 *
 * @since 5.1
 */
public abstract class AbstractWidgetAnnotation<T extends FieldDictionary> extends Annotation {

    /**
     * Indicates that the annotation has no highlight effect.
     */
    public static final Name HIGHLIGHT_NONE = new Name("N");

    protected static final Logger logger =
            Logger.getLogger(AbstractWidgetAnnotation.class.toString());

    /**
     * Transparency value used to simulate text highlighting.
     */
    protected static float highlightAlpha = 0.1f;

    // text selection colour
    protected static Color highlightColor;

    private boolean enableHighlightedWidget;

    static {
        // sets the background colour of the annotation highlight
        try {
            String color = Defs.sysProperty(
                    "org.icepdf.core.views.page.annotation.widget.highlight.color", "#CC00FF");
            int colorValue = ColorUtil.convertColor(color);
            highlightColor =
                    new Color(colorValue >= 0 ? colorValue :
                            Integer.parseInt("0077FF", 16));
        } catch (NumberFormatException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Error reading widget highlight colour.");
            }
        }

        try {
            highlightAlpha = (float) Defs.doubleProperty(
                    "org.icepdf.core.views.page.annotation.widget.highlight.alpha", 0.1f);
        } catch (NumberFormatException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Error reading widget highlight alpha.");
            }
        }
    }

    protected Name highlightMode;

    public AbstractWidgetAnnotation(Library l, HashMap h) {
        super(l, h);
        Object possibleName = getObject(LinkAnnotation.HIGHLIGHT_MODE_KEY);
        if (possibleName instanceof Name) {
            Name name = (Name) possibleName;
            if (HIGHLIGHT_NONE.equals(name.getName())) {
                highlightMode = HIGHLIGHT_NONE;
            } else if (LinkAnnotation.HIGHLIGHT_OUTLINE.equals(name.getName())) {
                highlightMode = LinkAnnotation.HIGHLIGHT_OUTLINE;
            } else if (LinkAnnotation.HIGHLIGHT_PUSH.equals(name.getName())) {
                highlightMode = LinkAnnotation.HIGHLIGHT_PUSH;
            }
        } else {
            highlightMode = LinkAnnotation.HIGHLIGHT_INVERT;
        }

    }

    @Override
    public void init() throws InterruptedException {
        super.init();
        // check to make sure the field value matches the content stream.
        InteractiveForm interactiveForm = library.getCatalog().getInteractiveForm();
        if (interactiveForm != null && interactiveForm.needAppearances()) {
            resetAppearanceStream(new AffineTransform());
        }
        // todo check if we have content value but no appearance stream.
    }

    public abstract void reset();

    @Override
    public abstract void resetAppearanceStream(double dx, double dy, AffineTransform pageSpace);

    @Override
    protected void renderAppearanceStream(Graphics2D g) {

        Appearance appearance = appearances.get(currentAppearance);
        if (appearance != null) {
            AppearanceState appearanceState = appearance.getSelectedAppearanceState();
            if (appearanceState != null &&
                    appearanceState.getShapes() != null) {
                // render the main annotation content
                super.renderAppearanceStream(g);
            }
        }
        // check the highlight widgetAnnotation field and if true we draw a light background colour to mark
        // the widgets on a page.
        if (enableHighlightedWidget) {
            AffineTransform preHighLightTransform = g.getTransform();
            g.setColor(highlightColor);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, highlightAlpha));
            g.fill(getBbox() != null ? getBbox() : getRectangle());
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g.setTransform(preHighLightTransform);
        }
    }

    private Rectangle2D getRectangle() {
        Rectangle2D origRect = getBbox() != null ? getBbox() : getUserSpaceRectangle();
        Rectangle2D.Float jrect = new Rectangle2D.Float(0, 0,
                (float) origRect.getWidth(), (float) origRect.getHeight());
        return jrect;
    }

    public abstract T getFieldDictionary();

    /**
     * Generally immediately after the BMC there is a rectangle that defines the actual size of the annotation.  If
     * found we can use this to make many assumptions and regenerate the content stream.
     *
     * @param markedContent content stream of the marked content.
     * @return a rectangle either way, if the q # # # # re isn't found then we use the bbox as a potential bound.
     */
    protected Rectangle2D.Float findBoundRectangle(String markedContent) {
        int selectionStart = markedContent.indexOf("q") + 1;
        int selectionEnd = markedContent.indexOf("re");
        if (selectionStart < selectionEnd && selectionEnd > 0) {
            String potentialNumbers = markedContent.substring(selectionStart, selectionEnd);
            float[] points = parseRectanglePoints(potentialNumbers);
            if (points != null) {
                return new Rectangle2D.Float(points[0], points[1], points[2], points[3]);
            }
        }
        // default to the bounding box.
        Rectangle2D bbox = getBbox();
        return new Rectangle2D.Float(1, 1, (float) bbox.getWidth(), (float) bbox.getHeight());
    }

    /**
     * Finds a rectangle  in the marked content.
     *
     * @param markedContent content to search for a rectangle.
     * @return rectangle if found,  otherwise bbox is used.
     */
    protected Rectangle2D.Float findRectangle(String markedContent) {
        int selectionEnd = markedContent.indexOf("re");
        if (selectionEnd >= 0) {
            String potentialNumbers = markedContent.substring(0, selectionEnd);
            float[] points = parseRectanglePoints(potentialNumbers);
            if (points != null) {
                return new Rectangle2D.Float(points[0], points[1], points[2], points[3]);
            }
            // default to the bounding box.
            Rectangle2D bbox = getBbox();
            return new Rectangle2D.Float(1, 1, (float) bbox.getWidth(), (float) bbox.getHeight());
        } else {
            return null;
        }
    }

    /**
     * Get the line height as specified by Th or the font size.
     *
     * @param defaultAppearance searchable stream
     * @return line height, or 13.87 if no reasonable approximation can be found.
     */
    protected double getLineHeight(String defaultAppearance) {
        if (defaultAppearance != null && checkAppearance(defaultAppearance)) {
            String sub = defaultAppearance.substring(0, defaultAppearance.indexOf("Tf"));
            StringTokenizer toker = new StringTokenizer(sub);
            while (toker.hasMoreTokens()) {
                Object obj = toker.nextElement();
                if (obj instanceof String) {
                    try {
                        double tmp = Double.parseDouble((String) obj);
                        tmp *= 1.15;
                        if (tmp > 0) {
                            return tmp;
                        }
                    } catch (NumberFormatException e) {
                        // intentionally blank.
                    }
                }
            }
        }
        return 13.87;
    }

    protected double getFontSize(String content) {
        // try and find text size
        double size = 12;

        if (content != null) {
            Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?\\s+Tf");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String fontDef = content.substring(matcher.start(), matcher.end());
                fontDef = fontDef.split(" ")[0];
                try {
                    size = Double.parseDouble(fontDef);
                } catch (NumberFormatException e) {
                    // ignore and move on
                }
                if (size < 2) {
                    size = 12;
                }
            }
        }
        return size;
    }

    /**
     * Encodes the given cotents string into a valid postscript string that is literal encoded.
     *
     * @param content  current content stream to append literal string to.
     * @param contents string to be encoded into '(...)' literal format.
     * @return original content stream with contents encoded in the literal string format.
     */
    protected StringBuilder encodeLiteralString(StringBuilder content, String contents) {
        String[] lines = contents.split("\n|\r|\f");
        for (String line : lines) {
            content.append('(').append(line.replaceAll("(?=[()\\\\])", "\\\\")
                    .replaceAll("Ã¿", "")).append(")' ");
        }
        return content;
    }

    /**
     * Encodes the given contents string into a valid postscript hex string.
     *
     * @param content  current content stream to append literal string to.
     * @param contents string to be encoded into '<...></...>' hex format.
     * @return original content stream with contents encoded in the hex string format.
     */
    protected StringBuilder encodeHexString(StringBuilder content, String contents) {
        String[] lines = contents.split("\n|\r|\f");
        for (String line : lines) {
            char[] chars = line.toCharArray();
            StringBuffer hex = new StringBuffer();
            for (int i = 0; i < chars.length; i++) {
                hex.append(Integer.toHexString((int) chars[i]));
            }
            content.append('<').append(hex).append(">' ");
        }
        return content;
    }

    /**
     * Utility to try and determine if the appearance is valid.
     *
     * @param appearance appearance ot test.
     * @return true if valid, false otherwise.
     */
    protected boolean checkAppearance(String appearance) {
        // example of a bad appearance, /TiBo 0 Tf 0 g
        // size is zero and the font can't be found.
        StringTokenizer toker = new StringTokenizer(appearance);
        if (toker.hasMoreTokens()) {
            String fontName = toker.nextToken().substring(1);
            String fontSize = toker.nextToken();
            Appearance appearance1 = appearances.get(currentAppearance);
            AppearanceState appearanceState = appearance1.getSelectedAppearanceState();
            org.icepdf.core.pobjects.fonts.Font font = null;
            Resources resources = appearanceState.getResources();
            if (resources != null) {
                font = resources.getFont(new Name(fontName));
            }
            return !(font == null || library.getInteractiveFormFont(fontName) == null ||
                    fontSize.equals("0"));
        }
        return false;
    }


    /**
     * The selection rectangle if present will help define the line height of the text.  If not present we can use
     * the default value 13.87 later which seems to be very common in the samples.
     *
     * @param markedContent content to look for "rg # # # # re".
     * @return selection rectangle, null if not found.
     */
    protected Rectangle2D.Float findSelectionRectangle(String markedContent) {
        int selectionStart = markedContent.indexOf("rg") + 2;
        int selectionEnd = markedContent.lastIndexOf("re");
        if (selectionStart < selectionEnd && selectionEnd > 0) {
            String potentialNumbers = markedContent.substring(selectionStart, selectionEnd);
            float[] points = parseRectanglePoints(potentialNumbers);
            if (points != null) {
                return new Rectangle2D.Float(points[0], points[1], points[2], points[3]);
            }
        }
        return null;
    }

    /**
     * Simple utility to write Rectangle2D.Float in postscript.
     *
     * @param rect Rectangle2D.Float to convert to postscript. Null value with throw null pointer exception.
     * @return postscript representation of the rect.
     */
    protected String generateRectangle(Rectangle2D.Float rect) {
        return rect.x + " " + rect.y + " " + rect.width + " " + rect.height + " re ";
    }

    /**
     * Converts a given string of four numbers into an array of floats. If a conversion error is encountered
     * null value is returned.
     *
     * @param potentialNumbers space separated string of four numbers.
     * @return list of four numbers, null if string can not be converted.
     */
    protected float[] parseRectanglePoints(String potentialNumbers) {
        StringTokenizer toker = new StringTokenizer(potentialNumbers);
        float[] points = new float[4];
        int max = toker.countTokens();
        Object[] tokens = new Object[max];
        for (int i = 0; i < max; i++) {
            tokens[i] = toker.nextElement();
        }
        boolean notFound = false;
        for (int i = 3, j = 0; j < 4; j++, i--) {
            try {
                points[j] = Float.parseFloat((String) tokens[max - i - 1]);
            } catch (NumberFormatException e) {
                notFound = true;
            }
        }
        if (!notFound) {
            return points;
        } else {
            return null;
        }
    }

    /**
     * Set the static highlight color used to highlight widget annotations.
     *
     * @param highlightColor colour of
     */
    public static void setHighlightColor(Color highlightColor) {
        AbstractWidgetAnnotation.highlightColor = highlightColor;
    }

    /**
     * Set enable highlight on an individual widget.
     *
     * @param enableHighlightedWidget true to enable highlight mode, otherwise false.
     */
    public void setEnableHighlightedWidget(boolean enableHighlightedWidget) {
        this.enableHighlightedWidget = enableHighlightedWidget;
    }

    /**
     * Set the static alpha value uses to paint a color over a widget annotation.
     *
     * @param highlightAlpha
     */
    public static void setHighlightAlpha(float highlightAlpha) {
        AbstractWidgetAnnotation.highlightAlpha = highlightAlpha;
    }

    /**
     * Is enable highlight enabled.
     *
     * @return return true if highlight is enabled, false otherwise.
     */
    public boolean isEnableHighlightedWidget() {
        return enableHighlightedWidget;
    }
}
