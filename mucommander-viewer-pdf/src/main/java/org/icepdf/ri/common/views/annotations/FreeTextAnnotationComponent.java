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
package org.icepdf.ri.common.views.annotations;

import org.icepdf.core.pobjects.annotations.Annotation;
import org.icepdf.core.pobjects.annotations.BorderStyle;
import org.icepdf.core.pobjects.annotations.FreeTextAnnotation;
import org.icepdf.core.pobjects.fonts.FontFile;
import org.icepdf.core.pobjects.fonts.FontManager;
import org.icepdf.core.pobjects.graphics.TextSprite;
import org.icepdf.core.pobjects.graphics.commands.DrawCmd;
import org.icepdf.core.pobjects.graphics.commands.TextSpriteDrawCmd;
import org.icepdf.ri.common.views.AbstractPageViewComponent;
import org.icepdf.ri.common.views.AnnotationCallback;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.common.views.DocumentViewModel;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * The FreeTextAnnotationComponent encapsulates a FreeTextAnnotation objects.  It
 * also provides basic editing functionality such as resizing, moving and change
 * the border color and style as well as the fill color.
 * <p/>
 * The Viewer RI implementation contains a FreeTextAnnotationPanel class which
 * can edit the various properties of this component.
 * <p/>
 * The FreeTextAnnotationComponent is slightly more complex then the other
 * annotations components.  Most annotations let the page pain the annotation
 * but in this cse FreeTextAnnotationComponent paints itself by creating a
 * JTextArea component that is made to look like the respective annotations
 * appearance stream.
 *
 * @see org.icepdf.ri.common.utility.annotation.FreeTextAnnotationPanel
 * @since 5.0
 */
@SuppressWarnings("serial")
public class FreeTextAnnotationComponent extends MarkupAnnotationComponent
        implements PropertyChangeListener {

    private static final Logger logger =
            Logger.getLogger(FreeTextAnnotation.class.toString());
    // font file cache.
    protected Font fontFile;
    private ScalableTextArea freeTextPane;
    private boolean contentTextChange;
    private FreeTextAnnotation freeTextAnnotation;

    public FreeTextAnnotationComponent(Annotation annotation, DocumentViewController documentViewController,
                                       final AbstractPageViewComponent pageViewComponent,
                                       final DocumentViewModel documentViewModel) {
        super(annotation, documentViewController, pageViewComponent, documentViewModel);
        isRollover = false;
        isShowInvisibleBorder = false;

        freeTextAnnotation = (FreeTextAnnotation) annotation;

        // update the shapes array pruning any text glyphs as well as
        // extra any useful font information for the editing of this annotation.
        if (annotation.getShapes() != null) {
            ArrayList<DrawCmd> shapes = annotation.getShapes().getShapes();
            DrawCmd cmd;
            for (int i = 0; i < shapes.size(); i++) {
                cmd = shapes.get(i);
                if (cmd instanceof TextSpriteDrawCmd) {
                    // grab the font reference
                    TextSprite tmp = ((TextSpriteDrawCmd) cmd).getTextSprite();
                    FontFile font = tmp.getFont();
                    freeTextAnnotation.setFontSize((int) font.getSize());
                    freeTextAnnotation.setFontColor(tmp.getStrokeColor());
                    // remove all text.
                    shapes.remove(i);
                }
            }
            ((FreeTextAnnotation) annotation).clearShapes();
        }
        // create the textArea to display the text.
        freeTextPane = new ScalableTextArea(documentViewModel);
        // line wrap false to force users to add line breaks.
        freeTextPane.setLineWrap(false);
        freeTextPane.setBackground(new Color(0, 0, 0, 0));
        freeTextPane.setMargin(new Insets(0, 0, 0, 0));
        // lock the field until the correct tool selects it.
        freeTextPane.setEditable(false);
        // clean up the contents make sure we have \n instead of \r
        String contents = freeTextAnnotation.getContents();
        if (contents != null) {
            contents = contents.replace('\r', '\n');
            freeTextPane.setText(contents);
        }

        // setup change listener so we now when to set the annotations AP stream
        freeTextPane.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                contentTextChange = true;
            }

            public void removeUpdate(DocumentEvent e) {
                contentTextChange = true;
            }

            public void changedUpdate(DocumentEvent e) {
                contentTextChange = true;
            }
        });

        GridLayout grid = new GridLayout(1, 1, 0, 0);
        this.setLayout(grid);
        this.add(freeTextPane);

        // add a focus management listener.
        KeyboardFocusManager focusManager =
                KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.addPropertyChangeListener(this);

        // set the default size hen building from external file
        if (annotation.getBbox() != null) {
            setBounds(annotation.getBbox().getBounds());
        }
        resetAppearanceShapes();
        revalidate();
    }

    public void setAppearanceStream() {
        // copy over annotation properties from the free text annotation.
        if (fontFile == null || freeTextAnnotation.isFontPropertyChanged()) {
            fontFile = FontManager.getInstance().initialize().getType1AWTFont(
                    freeTextAnnotation.getFontName(), freeTextAnnotation.getFontSize());
        }
        freeTextPane.setFont(fontFile);
        freeTextPane.setForeground(freeTextAnnotation.getFontColor());

        if (freeTextAnnotation.isFillType()) {
            freeTextPane.setOpaque(true);
            freeTextPane.setBackground(freeTextAnnotation.getFillColor());
        } else {
            freeTextPane.setOpaque(false);
        }
        if (freeTextAnnotation.isStrokeType()) {
            if (freeTextAnnotation.getBorderStyle().isStyleSolid()) {
                freeTextPane.setBorder(BorderFactory.createLineBorder(
                        freeTextAnnotation.getColor(),
                        (int) freeTextAnnotation.getBorderStyle().getStrokeWidth()));
            } else if (freeTextAnnotation.getBorderStyle().isStyleDashed()) {
                freeTextPane.setBorder(
                        new DashedBorder(freeTextAnnotation.getBorderStyle(),
                                freeTextAnnotation.getColor()));
            }
        } else {
            freeTextPane.setBorder(BorderFactory.createEmptyBorder());
        }

        String content = null;
        try {
            content = freeTextPane.getDocument().getText(0,
                    freeTextPane.getDocument().getLength());
        } catch (BadLocationException e) {
            logger.warning("Error getting rich text.");
        }
        Rectangle tBbox = convertToPageSpace(getBounds());

        // generate the shapes
        freeTextAnnotation.setBBox(tBbox);
        freeTextAnnotation.setContents(content);
        freeTextAnnotation.setRichText(freeTextPane.getText());
        freeTextPane.revalidate();
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        super.mouseDragged(me);
        resetAppearanceShapes();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        Object newValue = evt.getNewValue();
        Object oldValue = evt.getOldValue();

        if ("focusOwner".equals(prop) &&
                oldValue instanceof JTextArea) {
            JTextArea freeText = (JTextArea) oldValue;
            if (freeText.equals(freeTextPane)) {
                freeText.setEditable(false);
                if (contentTextChange) {
                    contentTextChange = false;
                    resetAppearanceShapes();
                    if (documentViewController.getAnnotationCallback() != null) {
                        AnnotationCallback annotationCallback =
                                documentViewController.getAnnotationCallback();
                        // notification that annotation was updated.
                        annotationCallback.updateAnnotation(this);
                    }
                }
                if (freeText instanceof ScalableTextArea) {
                    ((ScalableTextArea) freeText).setActive(false);
                }
            }
            repaint();
        } else if ("focusOwner".equals(prop) &&
                newValue instanceof JTextArea) {
            JTextArea freeText = (JTextArea) newValue;
            if (freeText.equals(freeTextPane) && !annotation.getFlagReadOnly()) {
                freeText.setEditable(true);
                if (freeText instanceof ScalableTextArea) {
                    ((ScalableTextArea) freeText).setActive(true);
                }
            }
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        super.mouseMoved(me);

    }

    @Override
    public void paintComponent(Graphics g) {
        // show a light border when in edit mode so component is easier to see.
        isShowInvisibleBorder = ((documentViewModel.getViewToolMode() ==
                DocumentViewModel.DISPLAY_TOOL_SELECTION ||
                documentViewModel.getViewToolMode() ==
                        DocumentViewModel.DISPLAY_TOOL_FREE_TEXT_ANNOTATION) &&
                !(annotation.getFlagReadOnly() || annotation.getFlagLocked() ||
                        annotation.getFlagInvisible() || annotation.getFlagHidden()));
    }

    @Override
    public void dispose() {
        super.dispose();
        KeyboardFocusManager focusManager =
                KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.removePropertyChangeListener(this);
    }

    @Override
    public void resetAppearanceShapes() {
        setAppearanceStream();
        annotation.resetAppearanceStream(getPageTransform());
    }

    public boolean isActive() {
        return this.freeTextPane.isActive();
    }

    public String clearXMLHeader(String strXML) {
        String regExp = "[<][?]\\s*[xml].*[?][>]";
        strXML = strXML.replaceFirst(regExp, "");
        return strXML;
    }

    public class MyHtml2Text extends HTMLEditorKit.ParserCallback {
        StringBuffer s;

        public MyHtml2Text() {
        }

        public void parse(Reader in) throws IOException {
            s = new StringBuffer();
            ParserDelegator delegator = new ParserDelegator();
            delegator.parse(in, this, Boolean.TRUE);
        }

        public void handleText(char[] text, int pos) {
            s.append(text);
            s.append("\n");
        }

        public String getText() {
            return s.toString();
        }
    }


    private class DashedBorder extends AbstractBorder {
        private BasicStroke stroke;
        private Color color;

        public DashedBorder(BorderStyle borderStyle, Color color) {
            int thickness = (int) borderStyle.getStrokeWidth();
            this.stroke = new BasicStroke(thickness,
                    BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
                    thickness * 2.0f,
                    freeTextAnnotation.getBorderStyle().getDashArray(),
                    0.0f);
            this.color = color;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            float size = this.stroke.getLineWidth();
            if (size > 0.0f) {
                g = g.create();
                if (g instanceof Graphics2D) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setStroke(this.stroke);
                    g2d.setPaint(color != null ? color : c == null ? null : c.getForeground());
                    g2d.draw(new Rectangle2D.Float(x + size / 2, y + size / 2, width - size, height - size));
                }
                g.dispose();
            }
        }

        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom =
                    (int) this.stroke.getLineWidth();
            return insets;
        }

    }
}