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

import org.icepdf.core.pobjects.fonts.FontFactory;
import org.icepdf.core.pobjects.graphics.*;
import org.icepdf.core.util.Library;

import java.awt.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A resource is a dictionary type as defined by the PDF specification.  It
 * can contain fonts, xobjects, colorspaces, patterns, shading and
 * external graphic states.
 *
 * @since 1.0
 */
public class Resources extends Dictionary {

    public static final Name COLORSPACE_KEY = new Name("ColorSpace");
    public static final Name FONT_KEY = new Name("Font");
    public static final Name XOBJECT_KEY = new Name("XObject");
    public static final Name PATTERN_KEY = new Name("Pattern");
    public static final Name SHADING_KEY = new Name("Shading");
    public static final Name EXTGSTATE_KEY = new Name("ExtGState");
    public static final Name PROPERTIES_KEY = new Name("Properties");

    // shared resource counter. 
    private static int uniqueCounter = 0;

    private static synchronized int getUniqueId() {
        return uniqueCounter++;
    }

    private static final Logger logger =
            Logger.getLogger(Resources.class.toString());

    HashMap fonts;
    HashMap xobjects;
    HashMap colorspaces;
    HashMap patterns;
    HashMap shading;
    HashMap extGStates;
    HashMap properties;

    /**
     * @param l
     * @param h
     */
    public Resources(Library l, HashMap h) {
        super(l, h);
        colorspaces = library.getDictionary(entries, COLORSPACE_KEY);
        fonts = library.getDictionary(entries, FONT_KEY);
        xobjects = library.getDictionary(entries, XOBJECT_KEY);
        patterns = library.getDictionary(entries, PATTERN_KEY);
        shading = library.getDictionary(entries, SHADING_KEY);
        extGStates = library.getDictionary(entries, EXTGSTATE_KEY);
        properties = library.getDictionary(entries, PROPERTIES_KEY);
    }

    public HashMap getFonts() {
        return fonts;
    }

    /**
     * @param o
     * @return
     */
    public PColorSpace getColorSpace(Object o) {

        if (o == null) {
            return null;
        }

        try {
            Object tmp;
            // every resource has a color space entry and o can be tmp in it.
            if (colorspaces != null && colorspaces.get(o) != null) {
                tmp = colorspaces.get(o);
                PColorSpace cs = PColorSpace.getColorSpace(library, tmp);
                if (cs != null) {
                    cs.init();
                }
                return cs;
            }
            // look for our name in the pattern dictionary
            if (patterns != null && patterns.get(o) != null) {
                tmp = patterns.get(o);
                PColorSpace cs = PColorSpace.getColorSpace(library, tmp);
                if (cs != null) {
                    cs.init();
                }
                return cs;
            }

            // if its not in color spaces or pattern then its a plain old
            // named colour space.
            PColorSpace cs = PColorSpace.getColorSpace(library, o);
            if (cs != null) {
                cs.init();
            }
            return cs;
        } catch (InterruptedException e) {
            logger.fine("Colorspace parsing was interrupted");
        }
        return null;

    }

    /**
     * @param s
     * @return
     */
    public org.icepdf.core.pobjects.fonts.Font getFont(Name s) {
        org.icepdf.core.pobjects.fonts.Font font = null;
        if (fonts != null) {
            Object ob = fonts.get(s);
            // check to make sure the library contains a font
            if (ob instanceof org.icepdf.core.pobjects.fonts.Font) {
                font = (org.icepdf.core.pobjects.fonts.Font) ob;
            }
            // corner case where font is just a inline dictionary.
            else if (ob instanceof HashMap) {
                font = FontFactory.getInstance().getFont(library, (HashMap) ob);
            }
            // the default value is most likely Reference
            else if (ob instanceof Reference) {
                Reference ref = (Reference) ob;
                ob = library.getObject((Reference) ob);
                if (ob instanceof PObject) {
                    ob = ((PObject) ob).getObject();
                }
                if (ob instanceof org.icepdf.core.pobjects.fonts.Font) {
                    font = (org.icepdf.core.pobjects.fonts.Font) ob;
                } else {
                    font = FontFactory.getInstance().getFont(library, (HashMap) ob);
                }
                // cache the font for later use.
                if (font != null) {
                    library.addObject(font, ref);
                    font.setPObjectReference(ref);
                }
            }
            // if still null do a deeper search checking the base font name of
            // each font for a match to the needed font name.  We have a few
            // malformed documents that don't refer to a font by the base name
            // and not the font name found in the resource table.
            if (font == null) {
                for (Object tmp : fonts.values()) {
                    if (tmp instanceof Reference) {
                        ob = library.getObject((Reference) tmp);
                        if (ob instanceof PObject) {
                            ob = ((PObject) ob).getObject();
                        }
                        if (ob instanceof org.icepdf.core.pobjects.fonts.Font) {
                            font = (org.icepdf.core.pobjects.fonts.Font) ob;
                            String baseFont = font.getBaseFont();
                            if (s.getName().equals(baseFont) ||
                                    baseFont.contains(s.getName())) {
                                // cache the font for later use.
                                library.addObject(font, (Reference) tmp);
                                font.setPObjectReference((Reference) tmp);
                                break;
                            } else {
                                font = null;
                            }
                        }
                    }
                }
            }
        }
        if (font != null) {
            try {
                font.setParentResource(this);
                font.init();
            } catch (Exception e) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "Error initializing font, falling back to font substitution.");
                } else {
                    logger.log(Level.FINER, "Error initializing font, falling back to font substitution. " + font);
                }
            }
        }
        return font;
    }

    /**
     * @param s
     * @param graphicsState
     * @return
     */
    public Image getImage(Name s, GraphicsState graphicsState) {

        // check xobjects for stream
        ImageStream st = (ImageStream) library.getObject(xobjects, s);
        if (st == null) {
            return null;
        }
        // return null if the xobject is not an image
        if (!st.isImageSubtype()) {
            return null;
        }
        // lastly return the images.
        Image image = null;
        try {
            image = st.getImage(graphicsState, this);
        } catch (Exception e) {
            logger.log(Level.FINE, "Error getting image by name: " + s, e);
        }
        return image;
    }

    public ImageStream getImageStream(Name s) {
        // check xobjects for stream
        Object st = library.getObject(xobjects, s);
        if (st instanceof ImageStream) {
            return (ImageStream) st;
        }
        return null;
    }

    public Object getXObject(Name s) {
        return library.getObject(xobjects, s);
    }

    /**
     * Gets a rough count of the images resources associated with this page. Does
     * not include inline images.
     *
     * @return rough count of images resources.
     */
    public int getImageCount() {
        int count = 0;
        if (xobjects != null) {
            for (Object tmp : xobjects.values()) {
                if (tmp instanceof Reference) {
                    tmp = library.getObject((Reference) tmp);
                    if (tmp instanceof ImageStream) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * @param s
     * @return
     */
    public boolean isForm(Name s) {
        Object o = library.getObject(xobjects, s);
        return o instanceof Form;
    }

    /**
     * Gets the Form XObject specified by the named reference.
     *
     * @param nameReference name of resourse to retreive.
     * @return if the named reference is found return it, otherwise return null;
     */
    public Form getForm(Name nameReference) {
        Form formXObject = null;
        Object tempForm = library.getObject(xobjects, nameReference);
        if (tempForm instanceof Form) {
            formXObject = (Form) tempForm;
        }
        return formXObject;
    }

    /**
     * Retrieves a Pattern object given the named resource.  This can be
     * call for a fill, text fill or do image mask.
     *
     * @param name of object to find.
     * @return tiling or shading type pattern object.  If not constructor is
     * found, then null is returned.
     */
    public Pattern getPattern(Name name) {
        if (patterns != null) {

            Object attribute = library.getObject(patterns, name);
            // An instance of TilingPattern will always have a stream
            if (attribute != null && attribute instanceof TilingPattern) {
                return (TilingPattern) attribute;
            } else if (attribute != null && attribute instanceof Stream) {
                return new TilingPattern((Stream) attribute);
            }
            // ShaddingPatterns will not have a stream but still need to parsed
            else if (attribute != null && attribute instanceof HashMap) {
                return ShadingPattern.getShadingPattern(library,
                        (HashMap) attribute);
            }
        }
        return null;
    }

    /**
     * Gets the shadding pattern based on a shading dictionary name,  similar
     * to getPattern but is only called for the 'sh' token.
     *
     * @param name name of shading dictionary
     * @return associated shading pattern if any.
     */
    public ShadingPattern getShading(Name name) {
        // look for pattern name in the shading dictionary, used by 'sh' tokens
        if (shading != null) {
            Object shadingDictionary = library.getObject(shading, name);
            if (shadingDictionary != null && shadingDictionary instanceof HashMap) {
                return ShadingPattern.getShadingPattern(library, entries,
                        (HashMap) shadingDictionary);
            }
            else if (shadingDictionary != null && shadingDictionary instanceof Stream) {
                return ShadingPattern.getShadingPattern(library, null,
                        (Stream) shadingDictionary);
            }
        }
        return null;
    }

    /**
     * Returns the ExtGState object which has the specified reference name.
     *
     * @param namedReference name of ExtGState object to try and find.
     * @return ExtGState which contains the named references ExtGState attributes,
     * if the namedReference could not be found null is returned.
     */
    public ExtGState getExtGState(Name namedReference) {
        ExtGState gsState = null;
        if (extGStates != null) {
            Object attribute = library.getObject(extGStates, namedReference);
            if (attribute instanceof HashMap) {
                gsState = new ExtGState(library, (HashMap) attribute);
            } else if (attribute instanceof Reference) {
                gsState = new ExtGState(library,
                        (HashMap) library.getObject(
                                (Reference) attribute));
            }
        }
        return gsState;
    }

    /**
     * Looks for the specified key in the Properties dictionary.  If the dictionary
     * and corresponding value is found the object is returned otherwise null.
     *
     * @param key key to find a value of in the Properties dictionary.
     * @return key value if found, null otherwise.
     */
    public OptionalContents getPropertyEntry(Name key) {
        if (properties != null) {
            Object object = library.getObject(properties.get(key));
            if (object instanceof OptionalContents) {
                return (OptionalContents) library.getObject(properties.get(key));
            }
        }
        return null;
    }

    /**
     * Checks to see if the Shading key has value in this resource dictionary.
     *
     * @return true if there are shading values,  false otherwise.
     */
    public boolean isShading() {
        return shading != null;
    }
}
