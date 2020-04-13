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
package org.icepdf.core.pobjects.acroform;

import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.Resources;
import org.icepdf.core.pobjects.Stream;
import org.icepdf.core.pobjects.StringObject;
import org.icepdf.core.pobjects.fonts.Font;
import org.icepdf.core.pobjects.graphics.GraphicsState;
import org.icepdf.core.util.Library;
import org.icepdf.core.util.Utils;
import org.icepdf.core.util.content.ContentParser;
import org.icepdf.core.util.content.ContentParserFactory;

import java.awt.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.icepdf.core.pobjects.acroform.InteractiveForm.DR_KEY;

/**
 * When the contents and properties of a field are known in advance, its visual
 * appearance can be specified by an appearance stream defined in the PDF file
 * (see 12.5.5, “Appearance Streams,” and 12.5.6.19, “Widget Annotations”). In
 * some cases, however, the field may contain text whose value is not known
 * until viewing time.
 *
 * @since 5.1
 */
public class VariableTextFieldDictionary extends FieldDictionary {

    private static final Logger logger =
            Logger.getLogger(VariableTextFieldDictionary.class.toString());

    public enum Quadding {
        LEFT_JUSTIFIED, CENTERED, RIGHT_JUSTIFIED
    }

    /**
     * The default appearance string containing a sequence of valid page-content
     * graphics or text state operators that define such properties as the field’s
     * text size and colour.
     */
    public static final Name DA_KEY = new Name("DA");

    /**
     * A code specifying the form of quadding (justification) that shall be used
     * in displaying the text:
     * 0 Left-justified
     * 1 Centered
     * 2 Right-justified
     * Default value: 0 (left-justified).
     */
    public static final Name Q_KEY = new Name("Q");

    /**
     * A default style string, as described in 12.7.3.4, “Rich Text Strings.”
     */
    public static final Name DS_KEY = new Name("DS");

    /**
     * Variable text fields.
     */
    private String defaultAppearance;
    private String defaultStyle;
    private String defaultRichText;

    /**
     * A rich text string, as described in 12.7.3.4, “Rich Text Strings.”
     */
    public static final Name RV_KEY = new Name("RV");

    protected Quadding quadding = Quadding.LEFT_JUSTIFIED;
    protected float size = 12;
    protected Name fontName = new Name("Helv");
    protected Font font = null;
    protected Color color = Color.BLACK;

    public VariableTextFieldDictionary(Library library, HashMap entries) {
        super(library, entries);

        // parse out quadding
        Number value = library.getInt(entries, Q_KEY);
        int quad = value.intValue();
        switch (quad) {
            case 0:
                quadding = Quadding.LEFT_JUSTIFIED;
                break;
            case 1:
                quadding = Quadding.CENTERED;
                break;
            case 2:
                quadding = Quadding.RIGHT_JUSTIFIED;
                break;
            default:
                quadding = Quadding.LEFT_JUSTIFIED;
                break;
        }
        // get the default style string
        Object tmp = library.getObject(entries, DS_KEY);
        if (tmp != null) {
            defaultStyle = Utils.convertStringObject(library, (StringObject) tmp);
        }

        tmp = library.getObject(entries, RV_KEY);
        if (tmp != null) {
            if (tmp instanceof StringObject) {
                defaultStyle = Utils.convertStringObject(library, (StringObject) tmp);
            } else if (tmp instanceof Stream) {
                defaultStyle = new String(((Stream) tmp).getDecodedStreamBytes());
            }
        }

        // parse out fontName, size and color.
        // /ZaDb 12 Tf 0 g
        tmp = library.getObject(entries, DA_KEY);
        if (tmp instanceof StringObject) {
            defaultAppearance = Utils.convertStringObject(library, (StringObject) tmp);
            Resources resources = library.getResources(entries, DR_KEY);
            // use the DA and DR dictionary to get a valid graphics state and thus the
            // the font and colour information we need to generate a new content stream
            if (resources != null) {
                try {
                    ContentParser cp = ContentParserFactory.getInstance()
                            .getContentParser(library, resources);
                    cp.parseTextBlocks(new byte[][]{defaultAppearance.getBytes()});
                    GraphicsState gs = cp.getGraphicsState();
                    if (gs != null) {
                        color = gs.getFillColor();
                        size = gs.getTextState().tsize;
                        if (gs.getTextState().font != null &&
                                gs.getTextState().font.getSubTypeFormat() != Font.CID_FORMAT) {
                            font = gs.getTextState().font;
                            fontName = gs.getTextState().fontName;
                        }
                    }
                } catch (Throwable e) {
                    logger.warning("Could not validate default appearance, defaulting.");
                }
            }
        }

    }

    /**
     * If the DA key is present the appearance stream is generated as is,  however if not then the content
     * is passed and we try to pull the color, size, font, and font name.
     *
     * @param content
     * @return
     */
    public String generateDefaultAppearance(String content, Resources resources) {
        try {
            String possibleContent;
            if (library.getObject(entries, DA_KEY) != null) {
                possibleContent = library.getString(entries, DA_KEY);
            } else if (parentField != null && library.getObject(parentField.getEntries(), DA_KEY) != null) {
                possibleContent = library.getString(parentField.getEntries(), DA_KEY);
            } else {
                possibleContent = content != null ? content : "";
            }
            if (resources == null) {
                resources = library.getCatalog().getInteractiveForm().getResources();
            }
            ContentParser cp = ContentParserFactory.getInstance()
                    .getContentParser(library, resources);
            cp.parseTextBlocks(new byte[][]{possibleContent.getBytes()});
            GraphicsState gs = cp.getGraphicsState();
            if (gs != null) {
                if (gs.getFillColor() != null) color = gs.getFillColor();
                if (gs.getTextState().tsize > 0) {
                    size = gs.getTextState().tsize;
                } else {
                    // default to basic size, as last resort.
                    size = 10;
                }
                // further work is needed here to add font mapping support when CID fonts are detected,
                // this may also be a fix for our asian font write support problem.
                if (gs.getTextState().font != null &&
                        gs.getTextState().font.getSubTypeFormat() != Font.CID_FORMAT) {
                    if (gs.getTextState().font != null) font = gs.getTextState().font;
                    if (gs.getTextState().fontName != null) fontName = gs.getTextState().fontName;
                }
            }
        } catch (Throwable e) {
            logger.warning("Could not generate default appearance stream.");
            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "Error parsing text feld content stream", e);
            }
        }
        return color.getRed() / 255.0f + " " + color.getGreen() / 255.0f + " " + color.getBlue() / 255.0f + " rg " +
                "/" + fontName + " " +
                size + " Tf ";
    }

    public String getDefaultAppearance() {
        return defaultAppearance;
    }

    public float getDefaultFontSize() {
        return size;
    }

    public Name getFontName() {
        return fontName;
    }

    public float getSize() {
        return size;
    }

    public Font getFont() {
        return font;
    }

    public Color getColor() {
        return color;
    }

    public Quadding getQuadding() {
        return quadding;
    }


}
