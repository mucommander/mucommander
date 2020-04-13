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
import org.icepdf.core.pobjects.graphics.GraphicsState;
import org.icepdf.core.util.Library;

import java.util.HashMap;

/**
 * As mentioned in 12.5.2, “Annotation Dictionaries,” the meaning of an
 * annotation’s Contents entry varies by annotation type. Typically, it is the
 * text that shall be displayed for the annotation or, if the annotation does not
 * display text, an alternate description of the annotation’s contents in
 * human-readable form. In either case, the Contents entry is useful when
 * extracting the document’s contents in support of accessibility to users with
 * disabilities or for other purposes (see 14.9.3, “Alternate Descriptions”).
 * <p/>
 * Many annotation types are defined as markup annotations because they are used
 * primarily to mark up PDF documents (see Table 170). These annotations have
 * text that appears as part of the annotation and may be displayed in other ways
 * by a conforming reader, such as in a Comments pane.
 * <p/>
 * Markup annotations may be divided into the following groups:
 * <ul>
 * <li>Free text annotations display text directly on the page. The annotation’s
 * Contents entry specifies the displayed text.</li>
 * <li>Most other markup annotations have an associated pop-up window that may
 * contain text. The annotation’s Contents entry specifies the text that shall
 * be displayed when the pop-up window is opened. These include text, line,
 * square, circle, polygon, polyline, highlight, underline, squiggly-underline,
 * strikeout, rubber stamp, caret, ink, and file attachment annotations. </li>
 * <li>Sound annotations do not have a pop-up window but may also have associated
 * text specified by the Contents entry.</li>
 * </ul>
 *
 * @since 5.0
 */
public abstract class MarkupAnnotation extends Annotation {

    /**
     * Optional; PDF 1.1) The text label that shall be displayed in the title bar
     * of the annotation’s pop-up window when open and active. This entry shall
     * identify the user who added the annotation.
     */
    public static final Name T_KEY = new Name("T");

    /**
     * (Optional; PDF 1.4) The constant opacity value that shall be used in
     * painting the annotation (see Sections 11.2, “Overview of Transparency,”
     * and 11.3.7, “Shape and Opacity Computations”). This value shall apply to
     * all visible elements of the annotation in its closed state (including its
     * background and border) but not to the pop-up window that appears when the
     * annotation is opened.
     */
    public static final Name CA_KEY = new Name("CA");

    /**
     * (Optional; PDF 1.5) A rich text string (see 12.7.3.4, “Rich Text Strings”)
     * that shall be displayed in the pop-up window when the annotation is opened.
     */
    public static final Name RC_KEY = new Name("RC");

    /**
     * (Optional; PDF 1.5) The date and time (7.9.4, “Dates”) when the
     * annotation was created.
     */
    public static final Name CREATION_DATE_KEY = new Name("CreationDate");

    /**
     * (Required if an RT entry is present, otherwise optional; PDF 1.5) A
     * reference to the annotation that this annotation is “in reply to.” Both
     * annotations shall be on the same page of the document. The relationship
     * between the two annotations shall be specified by the RT entry.
     * <p/>
     * If this entry is present in an FDF file (see 12.7.7, “Forms Data Format”),
     * its type shall not be a dictionary but a text string containing the
     * contents of the NM entry of the annotation being replied to, to allow for
     * a situation where the annotation being replied to is not in the same FDF
     * file.
     */
    public static final Name IRT_KEY = new Name("IRT");

    /**
     * (Optional; PDF 1.5) Text representing a short description of the subject
     * being addressed by the annotation.
     */
    public static final Name SUBJ_KEY = new Name("Subj");

    /**
     * (Optional; PDF 1.3) An indirect reference to a pop-up annotation for
     * entering or editing the text associated with this annotation.
     */
    public static final Name POPUP_KEY = new Name("Popup");

    /**
     * Optional; meaningful only if IRT is present; PDF 1.6) A name specifying
     * the relationship (the “reply type”) between this annotation and one
     * specified by IRT. Valid values are:
     * <p/>
     * R - The annotation shall be considered a reply to the annotation specified
     * by IRT. Conforming readers shall not display replies to an annotation
     * individually but together in the form of threaded comments.
     * <p/>
     * Group - The annotation shall be grouped with the annotation specified by
     * IRT; see the discussion following this Table.
     * <p/>
     * Default value: R.
     */
    public static final Name RT_KEY = new Name("RT");

    /**
     * (Optional; PDF 1.6) A name describing the intent of the markup annotation.
     * Intents allow conforming readers to distinguish between different uses
     * and behaviors of a single markup annotation type. If this entry is not
     * present or its value is the same as the annotation type, the annotation
     * shall have no explicit intent and should behave in a generic manner in a
     * conforming reader.
     * <p/>
     * Free text annotations (Table 174), line annotations (Table 175), polygon
     * annotations (Table 178), and (PDF 1.7) polyline annotations (Table 178)
     * have defined intents, whose values are enumerated in the corresponding
     * tables.
     */
    public static final Name IT_KEY = new Name("IT");

    /**
     * (Optional; PDF 1.7) An external data dictionary specifying data that shall
     * be associated with the annotation. This dictionary contains the following
     * entries:
     * <p/>
     * Type - (optional) If present, shall be ExData.
     * <p/>
     * Subtype  - (required) a name specifying the type of data that the markup
     * annotation shall be associated with. The only defined value is Markup3D.
     * Table 298 lists the values that correspond to a subtype of Markup3D.
     */
    public static final Name EX_DATA_KEY = new Name("ExData");

    /**
     * Named graphics state name used to store transparency values.
     */
    public static final Name EXT_GSTATE_NAME = new Name("ip1");

    protected String titleText;
    protected PopupAnnotation popupAnnotation;
    protected float opacity = 1.0f;
    protected String richText;
    protected PDate creationDate;
    protected MarkupAnnotation inReplyToAnnotation;
    protected String subject;
    protected Name replyToRelation = new Name("R");
    protected Name intent;
    // exData not implemented

    public MarkupAnnotation(Library l, HashMap h) {
        super(l, h);
    }

    public void init() throws InterruptedException {
        super.init();
        // title text
        titleText = getString(T_KEY);

        // rich text
        richText = getString(RC_KEY);

        // subject text
        subject = getString(SUBJ_KEY);

        // creation date
        Object value = library.getObject(entries, CREATION_DATE_KEY);
        if (value != null && value instanceof StringObject) {
            creationDate = new PDate(securityManager, getString(CREATION_DATE_KEY));
        }

        // popup child
        value = library.getObject(entries, POPUP_KEY);
        if (value != null && value instanceof PopupAnnotation) {
            popupAnnotation = (PopupAnnotation) value;
        }

        // opacity
        float ca = library.getFloat(entries, CA_KEY);
        if (ca != 0.0f) {
            opacity = ca;
        }

        // in reply to annotation
        value = library.getObject(entries, IRT_KEY);
        if (value != null && value instanceof MarkupAnnotation) {
            inReplyToAnnotation = (MarkupAnnotation) value;
        }

        // in reply to annotation
        value = library.getName(entries, RT_KEY);
        if (value != null) {
            replyToRelation = (Name) value;
        }

        // intent of annotation
        value = library.getName(entries, IT_KEY);
        if (value != null) {
            intent = (Name) value;
        }
    }


    public String getTitleText() {
        return titleText;
    }

    public PopupAnnotation getPopupAnnotation() {
        return popupAnnotation;
    }

    protected static void generateExternalGraphicsState(Form form, float opacity) {
        // add the transparency graphic context settings.
        if (form != null) {
            Resources resources = form.getResources();
            HashMap<Object, Object> graphicsProperties = new HashMap<Object, Object>(2);
            HashMap<Object, Object> graphicsState = new HashMap<Object, Object>(1);
            graphicsProperties.put(GraphicsState.CA_STROKING_KEY, opacity);
            graphicsProperties.put(GraphicsState.CA_NON_STROKING_KEY, opacity);
            graphicsState.put(EXT_GSTATE_NAME, graphicsProperties);
            resources.getEntries().put(Resources.EXTGSTATE_KEY, graphicsState);
            form.setResources(resources);
        }
    }

    /**
     * Gets the opacity value for a markup annotation.  This value can be optionally used to apply a global
     * opacity value when painting or regenerating the contentStream.
     *
     * @return current opacity value in the range of 0.0 ... 1.0
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     * Get the opacity value in the range of 0 ... 255.
     *
     * @return current opacity value in the range of 0 ... 255.
     */
    public int getOpacityNormalized() {
        return Math.round(opacity * 255);
    }

    /**
     * Set the opacity value of the /CA key in the markup annotation dictionary.
     *
     * @param opacity opacity in the range of 0.0 ... 1.0.
     */
    public void setOpacity(float opacity) {
        if (this.opacity >= 0 && this.opacity <= 1.0) {
            this.opacity = opacity;
            entries.put(CA_KEY, this.opacity);
        }
    }

    /**
     * Set the opacity value of the /CA key in the markup annotation dictionary.
     *
     * @param opacity opacity in the range of 0 ... 255.
     */
    public void setOpacity(int opacity) {
        if (this.opacity >= 0 && this.opacity <= 255) {
            this.opacity = Math.round(opacity / 2.55f) / 100.0f;
            entries.put(CA_KEY, this.opacity);
        }
    }

    public String getRichText() {
        return richText;
    }

    public PDate getCreationDate() {
        return creationDate;
    }

    public MarkupAnnotation getInReplyToAnnotation() {
        return inReplyToAnnotation;
    }

    public String getSubject() {
        return subject;
    }

    public Name getReplyToRelation() {
        return replyToRelation;
    }

    public Name getIntent() {
        return intent;
    }

    public void setTitleText(String titleText) {
        this.titleText = setString(T_KEY, titleText);
    }

    public void setPopupAnnotation(PopupAnnotation popupAnnotation) {
        this.popupAnnotation = popupAnnotation;
        entries.put(POPUP_KEY, popupAnnotation.getPObjectReference());
    }

    public void setRichText(String richText) {
        this.richText = setString(RC_KEY, richText);
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = new PDate(securityManager, creationDate);
        setString(CREATION_DATE_KEY, creationDate);
    }

    public void setInReplyToAnnotation(MarkupAnnotation inReplyToAnnotation) {
        this.inReplyToAnnotation = inReplyToAnnotation;
        entries.put(IRT_KEY, inReplyToAnnotation.getPObjectReference());
    }

    public void setSubject(String subject) {
        this.subject = setString(SUBTYPE_KEY, subject);
    }

    public String toString() {
        return getTitleText();
    }
}
