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
package org.icepdf.ri.common.tools;

import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.graphics.text.GlyphText;
import org.icepdf.core.pobjects.graphics.text.LineText;
import org.icepdf.core.pobjects.graphics.text.PageText;
import org.icepdf.core.pobjects.graphics.text.WordText;
import org.icepdf.core.util.Defs;
import org.icepdf.core.util.PropertyConstants;
import org.icepdf.ri.common.views.AbstractPageViewComponent;
import org.icepdf.ri.common.views.DocumentViewController;
import org.icepdf.ri.common.views.DocumentViewModel;
import org.icepdf.ri.common.views.PageViewComponentImpl;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.icepdf.ri.common.tools.TextSelection.enableMarginExclusion;

/**
 * TextSelection is a utility class that captures most of the work needed to do basic text, word and line selection.
 */
public class TextSelection extends SelectionBoxHandler {

    protected static final Logger logger =
            Logger.getLogger(TextSelection.class.toString());

    public int selectedCount;

    protected Point lastMousePressedLocation;
    protected Point lastMouseLocation;

    private GlyphLocation glyphStartLocation;
    private GlyphLocation glyphEndLocation;

    private GlyphLocation lastGlyphStartLocation;
    private GlyphLocation lastGlyphEndLocation;

    // todo configurable system property to switch to rightToLeft.
    private boolean leftToRight = true;

    // todo make configurable
    protected int topMargin = 75;
    protected int bottomMargin = 75;
    protected static boolean enableMarginExclusion;
    protected static boolean enableMarginExclusionBorder;
    protected Rectangle2D topMarginExclusion;
    protected Rectangle2D bottomMarginExclusion;

    // Pointer to make sure the GC doesn't collect a page while selection state is present
    protected Page pageLock;


    static {
        try {
            enableMarginExclusion = Defs.booleanProperty(
                    "org.icepdf.core.views.page.marginExclusion.enabled", false);
        } catch (NumberFormatException e) {
            logger.warning("Error reading margin exclusion enabled property.");
        }
        try {
            enableMarginExclusionBorder = Defs.booleanProperty(
                    "org.icepdf.core.views.page.marginExclusionBorder.enabled", false);
        } catch (NumberFormatException e) {
            logger.warning("Error reading margin exclusion boarder enabled property.");
        }
    }

    // first page that was selected
    private boolean isFirst;

    public TextSelection(DocumentViewController documentViewController, AbstractPageViewComponent pageViewComponent,
                         DocumentViewModel documentViewModel) {
        super(documentViewController, pageViewComponent, documentViewModel);
    }

    /**
     * Handles double and triple left mouse clicks to select a word or line of text respectively.
     *
     * @param clickCount number of mouse clicks to interpret for line or word selection.
     * @param clickPoint point that mouse was clicked.
     */
    public void wordLineSelection(int clickCount, Point clickPoint, AbstractPageViewComponent pageViewComponent) {
        // double click we select the whole line.
        try {
            if (clickCount == 3) {
                Page currentPage = pageViewComponent.getPage();
                // handle text selection mouse coordinates
                Point mouseLocation = (Point) clickPoint.clone();
                lineSelectHandler(currentPage, mouseLocation);
            }
            // single click we select word that was clicked.
            else if (clickCount == 2) {
                Page currentPage = pageViewComponent.getPage();
                // handle text selection mouse coordinates
                Point mouseLocation = (Point) clickPoint.clone();
                wordSelectHandler(currentPage, mouseLocation);
            }
            if (pageViewComponent != null) {
                pageViewComponent.requestFocus();
            }
        } catch (InterruptedException e) {
            logger.fine("Text selection page access interrupted");
        }
    }

    /**
     * Selection started so we want to record the position and update the selection rectangle.
     *
     * @param startPoint starting selection position.
     */
    public void selectionStart(Point startPoint, AbstractPageViewComponent pageViewComponent, boolean isFirst) {
        try {
            Page currentPage = pageViewComponent.getPage();
            this.isFirst = isFirst;
            if (currentPage != null) {
                // get page text
                PageText pageText = currentPage.getViewText();
                // get page transform, same for all calculations
                AffineTransform pageTransform = currentPage.getPageTransform(
                        documentViewModel.getPageBoundary(),
                        documentViewModel.getViewRotation(),
                        documentViewModel.getViewZoom());

                // create exclusion boxes
                calculateTextSelectionExclusion(pageTransform);

                ArrayList<LineText> pageLines = pageText.getPageLines();
                Point2D.Float dragStartLocation = convertMouseToPageSpace(startPoint, pageTransform);
                glyphStartLocation = GlyphLocation.findGlyphLocation(pageLines, dragStartLocation, true, false, null,
                        topMarginExclusion, bottomMarginExclusion);
                glyphEndLocation = null;
            }

            // text selection box.
            currentRect = new Rectangle(startPoint.x, startPoint.y, 0, 0);
            updateDrawableRect(pageViewComponent.getWidth(), pageViewComponent.getHeight());
            pageViewComponent.repaint();
        } catch (InterruptedException e) {
            logger.fine("Text selection page access interrupted");
        }
    }

    /**
     * Selection ended so we want to stop record the position and update the selection.
     */
    public void selectionEnd(Point endPoint, AbstractPageViewComponent pageViewComponent) {

        try {
            // write out selected text.
            if (pageViewComponent != null && logger.isLoggable(Level.FINE)) {
                Page currentPage = pageViewComponent.getPage();
                // handle text selection mouse coordinates
                if (currentPage.getViewText() != null) {
                    logger.fine(currentPage.getViewText().getSelected().toString());
                }
            }

            if (selectedCount > 0) {

                // add the page to the page as it is marked for selection
                documentViewModel.addSelectedPageText(pageViewComponent);
                documentViewController.firePropertyChange(
                        PropertyConstants.TEXT_SELECTED,
                        null, null);
            }

            // clear the rectangle
            clearRectangle(pageViewComponent);

            if (pageViewComponent != null) {
                pageViewComponent.repaint();
            }
        } catch (InterruptedException e) {
            logger.fine("Text selection page access interrupted");
        }
    }

    public void clearSelection() {

        // release the page lock so the Reference API can take care of collecting the page post selection.
        pageLock = null;

        lastGlyphStartLocation = null;
        lastGlyphEndLocation = null;

        glyphStartLocation = null;
        glyphEndLocation = null;

        selectedCount = 0;
    }

    public void clearSelectionState() {
        java.util.List<AbstractPageViewComponent> pages = documentViewModel.getPageComponents();
        for (AbstractPageViewComponent page : pages) {
            ((PageViewComponentImpl)page).getTextSelectionPageHandler().clearSelection();
        }
    }

    public void selection(Point dragPoint, AbstractPageViewComponent pageViewComponent,
                          boolean isDown, boolean isMovingRight) {
        try {
            if (pageViewComponent != null) {

                // acquire a page lock.
                pageLock = pageViewComponent.getPage();

                boolean isLocalDown;
                if (lastMouseLocation != null) {
                    // double check we're actually moving down
                    isLocalDown = lastMouseLocation.y <= dragPoint.y;
                } else {
                    isLocalDown = isDown;
                }
                multiLineSelectHandler(pageViewComponent, dragPoint, isDown, isLocalDown, isMovingRight);

                lastMouseLocation = dragPoint;
            }
        } catch (InterruptedException e) {
            logger.fine("Text selection page access interrupted");
        }
    }

    public void selectionIcon(Point mouseLocation, AbstractPageViewComponent pageViewComponent) {
        try {
            Page currentPage = pageViewComponent.getPage();
            if (currentPage != null) {
                // get page text
                PageText pageText = currentPage.getViewText();
                if (pageText != null) {

                    // get page transform, same for all calculations
                    AffineTransform pageTransform = currentPage.getPageTransform(
                            Page.BOUNDARY_CROPBOX,
                            documentViewModel.getViewRotation(),
                            documentViewModel.getViewZoom());

                    // create exclusion boxes
                    calculateTextSelectionExclusion(pageTransform);

                    ArrayList<LineText> pageLines = pageText.getPageLines();
                    if (pageLines != null) {
                        boolean found = false;
                        Point2D.Float pageMouseLocation =
                                convertMouseToPageSpace(mouseLocation, pageTransform);

                        for (LineText pageLine : pageLines) {
                            // check for containment, if so break into words.
                            if (pageLine.getBounds().contains(pageMouseLocation)
                                    && ((topMarginExclusion == null || bottomMarginExclusion == null)
                                    || (!topMarginExclusion.contains(pageMouseLocation)
                                    && !bottomMarginExclusion.contains(pageMouseLocation)))) {
                                found = true;
                                documentViewController.setViewCursor(
                                        DocumentViewController.CURSOR_TEXT_SELECTION);
                                break;
                            }
                        }
                        if (!found) {
                            documentViewController.setViewCursor(
                                    DocumentViewController.CURSOR_SELECT);
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            logger.fine("Text selection page access interrupted");
        }
    }

    protected void calculateTextSelectionExclusion(AffineTransform pageTransform) {
        if (enableMarginExclusion) {
            Rectangle2D mediaBox = pageViewComponent.getPage().getCropBox();
            topMarginExclusion = new Rectangle2D.Float(
                    (int) mediaBox.getX(),
                    (int) mediaBox.getY() - topMargin,
                    (int) mediaBox.getWidth(), topMargin);
            bottomMarginExclusion = new Rectangle2D.Float(
                    (int) mediaBox.getX(),
                    (int) (mediaBox.getY() - mediaBox.getHeight()),
                    (int) mediaBox.getWidth(), bottomMargin);
        }
    }

    /**
     * Paints any text that is selected in the page wrapped by a pageViewComponent.
     *
     * @param g                 graphics context to paint to.
     * @param pageViewComponent page view component to paint selected to on.
     * @param documentViewModel document model contains view properties such as zoom and rotation.
     */
    public static void paintSelectedText(Graphics g,
                                         AbstractPageViewComponent pageViewComponent,
                                         DocumentViewModel documentViewModel) throws InterruptedException{
        // ready outline paint
        Graphics2D gg = (Graphics2D) g;
        AffineTransform prePaintTransform = gg.getTransform();
        Color oldColor = gg.getColor();
        Stroke oldStroke = gg.getStroke();
//        gg.setComposite(BlendComposite.getInstance(BlendComposite.BlendingMode.MULTIPLY, 1.0f));
        gg.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER,
                Page.SELECTION_ALPHA));
        gg.setColor(Page.selectionColor);
        gg.setStroke(new BasicStroke(1.0f));

        Page currentPage = pageViewComponent.getPage();
        if (currentPage != null) {
            PageText pageText = currentPage.getViewText();
            if (pageText != null) {
                // get page transformation
                AffineTransform pageTransform = currentPage.getPageTransform(
                        documentViewModel.getPageBoundary(),
                        documentViewModel.getViewRotation(),
                        documentViewModel.getViewZoom());
                // paint the sprites
                GeneralPath textPath;
                ArrayList<LineText> visiblePageLines = pageText.getPageLines();
                if (visiblePageLines != null) {
                    for (LineText lineText : visiblePageLines) {
                        for (WordText wordText : lineText.getWords()) {
                            // paint whole word
                            if (wordText.isSelected() || wordText.isHighlighted()) {
                                textPath = new GeneralPath(wordText.getBounds());
                                textPath.transform(pageTransform);
                                // paint highlight over any selected
                                if (wordText.isSelected()) {
                                    gg.setColor(Page.selectionColor);
                                    gg.fill(textPath);
                                }
                                if (wordText.isHighlighted()) {
                                    gg.setColor(Page.highlightColor);
                                    gg.fill(textPath);
                                }
                            }
                            // check children
                            else {
                                for (GlyphText glyph : wordText.getGlyphs()) {
                                    if (glyph.isSelected()) {
                                        textPath = new GeneralPath(glyph.getBounds());
                                        textPath.transform(pageTransform);
                                        gg.setColor(Page.selectionColor);
                                        gg.fill(textPath);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
//        gg.setComposite(BlendComposite.getInstance(BlendComposite.BlendingMode.NORMAL, 1.0f));
        // restore graphics state to where we left it.
        gg.setTransform(prePaintTransform);
        gg.setStroke(oldStroke);
        gg.setColor(oldColor);

        // paint words for bounds test.
//        paintTextBounds(g);

    }

    /**
     * Utility for painting text bounds.
     *
     * @param g graphics context to paint to.
     */
    protected void paintTextBounds(Graphics g) throws InterruptedException{
        Page currentPage = pageViewComponent.getPage();
        // get page transformation
        AffineTransform pageTransform = currentPage.getPageTransform(
                documentViewModel.getPageBoundary(),
                documentViewModel.getViewRotation(),
                documentViewModel.getViewZoom());
        Graphics2D gg = (Graphics2D) g;
        Color oldColor = g.getColor();
        g.setColor(Color.red);

        PageText pageText = currentPage.getViewText();
        if (pageText != null) {
            ArrayList<LineText> pageLines = pageText.getPageLines();
            if (pageLines != null) {
                for (LineText lineText : pageLines) {

                    for (WordText wordText : lineText.getWords()) {
                        for (GlyphText glyph : wordText.getGlyphs()) {
                            g.setColor(Color.black);
                            GeneralPath glyphSpritePath =
                                    new GeneralPath(glyph.getBounds());
                            glyphSpritePath.transform(pageTransform);
                            gg.draw(glyphSpritePath);
                        }

                        //                if (!wordText.isWhiteSpace()) {
                        //                    g.setColor(Color.blue);
                        //                    GeneralPath glyphSpritePath =
                        //                            new GeneralPath(wordText.getBounds());
                        //                    glyphSpritePath.transform(pageTransform);
                        //                    gg.draw(glyphSpritePath);
                        //                }
                    }
                    g.setColor(Color.red);
                    GeneralPath glyphSpritePath =
                            new GeneralPath(lineText.getBounds());
                    glyphSpritePath.transform(pageTransform);
                    gg.draw(glyphSpritePath);
                }
            }
        }
        g.setColor(oldColor);
    }

    /**
     * Entry point for multiline text selection.  Contains logic for moving from once page to the next which boils
     * down to defining a start position when a new page is entered.
     *
     * @param pageViewComponent page view that is being acted.
     * @param mouseLocation     current mouse location already normalized to page space. .
     * @param isDown            general selection trent is down, if false it's up.
     * @param isMovingRight     general selection trent is right, if alse it's left.
     */
    protected void multiLineSelectHandler(AbstractPageViewComponent pageViewComponent, Point mouseLocation,
                                          boolean isDown, boolean isLocalDown, boolean isMovingRight) throws InterruptedException{
        Page currentPage = pageViewComponent.getPage();
        selectedCount = 0;

        if (currentPage != null) {
            // get page text
            PageText pageText = currentPage.getViewText();
            if (pageText != null) {

                // clear the currently selected state, ignore highlighted.
                pageText.clearSelected();

                // get page transform, same for all calculations
                AffineTransform pageTransform = currentPage.getPageTransform(
                        documentViewModel.getPageBoundary(),
                        documentViewModel.getViewRotation(),
                        documentViewModel.getViewZoom());

                ArrayList<LineText> pageLines = pageText.getPageLines();

                // create exclusion boxes
                calculateTextSelectionExclusion(pageTransform);

                // normalize the mouse coordinates to page space
                Point2D.Float draggingMouseLocation = convertMouseToPageSpace(mouseLocation, pageTransform);

                // dragging mouse into a page or from white space, neither glyphStart or glyphEnd will be initialized.
                if (glyphStartLocation == null) {
                    glyphStartLocation = GlyphLocation.findFirstGlyphLocation(pageLines, draggingMouseLocation, isDown, isLocalDown,
                            lastGlyphEndLocation, topMarginExclusion, bottomMarginExclusion);
                    // if we're close to something mark the isFirst=false.
                    if (glyphStartLocation != null) {
                        glyphEndLocation = new GlyphLocation(glyphStartLocation);
                        isFirst = false;
                    }
                } else if (glyphStartLocation != null) {
                    // should already have start but no end.
                    glyphEndLocation = GlyphLocation.findGlyphLocation(pageLines, draggingMouseLocation, isDown, isLocalDown,
                            lastGlyphEndLocation, topMarginExclusion, bottomMarginExclusion);
                }

                // normal page selection,  fill in the the highlight between start and end.
                if (glyphStartLocation != null && glyphEndLocation != null) {
                    selectedCount = GlyphLocation.highLightGlyphs(pageLines, glyphStartLocation, glyphEndLocation, leftToRight,
                            isDown, isLocalDown, isMovingRight, topMarginExclusion, bottomMarginExclusion);
                    lastGlyphStartLocation = glyphStartLocation;
                    lastGlyphEndLocation = glyphEndLocation;
                }
                // check if last draw are still around and draw them.
                else if (lastGlyphStartLocation != null && lastGlyphEndLocation != null) {
                    selectedCount = GlyphLocation.highLightGlyphs(pageLines, lastGlyphStartLocation, lastGlyphEndLocation, leftToRight,
                            isDown, isLocalDown, isMovingRight, topMarginExclusion, bottomMarginExclusion);
                }
            }
            pageViewComponent.repaint();
        }
    }

    /**
     * Utility for selecting multiple lines via rectangle like tool. The
     * selection works based on the intersection of the rectangle and glyph
     * bounding box.
     * <p>
     * This method should only be called from within a locked page content
     *
     * @param currentPage   page to looking for text intersection on.
     * @param mouseLocation location of mouse.
     */
    protected void wordSelectHandler(Page currentPage, Point mouseLocation) throws InterruptedException {

        if (currentPage != null) {
            // get page text
            PageText pageText = currentPage.getViewText();
            if (pageText != null) {

                // clear the currently selected state, ignore highlighted.
                pageText.clearSelected();

                // get page transform, same for all calculations
                AffineTransform pageTransform = currentPage.getPageTransform(
                        Page.BOUNDARY_CROPBOX,
                        documentViewModel.getViewRotation(),
                        documentViewModel.getViewZoom());

                Point2D.Float pageMouseLocation =
                        convertMouseToPageSpace(mouseLocation, pageTransform);
                ArrayList<LineText> pageLines = pageText.getPageLines();
                if (pageLines != null) {
                    for (LineText pageLine : pageLines) {
                        // check for containment, if so break into words.
                        if (pageLine.getBounds().contains(pageMouseLocation)) {
                            pageLine.setHasSelected(true);
                            java.util.List<WordText> lineWords = pageLine.getWords();
                            for (WordText word : lineWords) {
                                //                            if (word.contains(pageTransform, mouseLocation)) {
                                if (word.getBounds().contains(pageMouseLocation)) {
                                    word.selectAll();
                                    // let the ri know we have selected text.
                                    documentViewModel.addSelectedPageText(pageViewComponent);
                                    documentViewController.firePropertyChange(
                                            PropertyConstants.TEXT_SELECTED,
                                            null, null);
                                    pageViewComponent.repaint();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Utility for selecting a LineText which is usually a sentence in the
     * document.   This is usually triggered by a triple click of the mouse
     *
     * @param currentPage   page to select
     * @param mouseLocation location of mouse
     */
    protected void lineSelectHandler(Page currentPage, Point mouseLocation) throws InterruptedException {
        if (currentPage != null) {
            // get page text
            PageText pageText = currentPage.getViewText();
            if (pageText != null) {

                // clear the currently selected state, ignore highlighted.
                pageText.clearSelected();

                // get page transform, same for all calculations
                AffineTransform pageTransform = currentPage.getPageTransform(
                        Page.BOUNDARY_CROPBOX,
                        documentViewModel.getViewRotation(),
                        documentViewModel.getViewZoom());

                Point2D.Float pageMouseLocation =
                        convertMouseToPageSpace(mouseLocation, pageTransform);
                ArrayList<LineText> pageLines = pageText.getPageLines();
                if (pageLines != null) {
                    for (LineText pageLine : pageLines) {
                        // check for containment, if so break into words.
                        if (pageLine.getBounds().contains(pageMouseLocation)) {
                            pageLine.selectAll();

                            // let the ri know we have selected text.
                            documentViewModel.addSelectedPageText(pageViewComponent);
                            documentViewController.firePropertyChange(
                                    PropertyConstants.TEXT_SELECTED,
                                    null, null);

                            pageViewComponent.repaint();
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setSelectionRectangle(Point cursorLocation, Rectangle selection) {
    }

    /**
     * Sets the top margin used to define an exclusion zone for text selection.  For this value
     * to be applied the system property -Dorg.icepdf.core.views.page.marginExclusion.enabled=true
     * must be set.
     *
     * @param topMargin top margin height in pixels.
     */
    public void setTopMargin(int topMargin) {
        this.topMargin = topMargin;
    }

    /**
     * Sets the bottom margin used to define an exclusion zone for text selection.  For this value
     * to be applied the system property -Dorg.icepdf.core.views.page.marginExclusion.enabled=true
     * must be set.
     *
     * @param bottomMargin bottom margin height in pixels.
     */
    public void setBottomMargin(int bottomMargin) {
        this.bottomMargin = bottomMargin;
    }
}

class GlyphLocation {

    private int line, word, glyph;

    public GlyphLocation(int line, int word, int glyph) {
        this.line = line;
        this.word = word;
        this.glyph = glyph;
    }

    public GlyphLocation(GlyphLocation glyphLocation) {
        this.line = glyphLocation.line;
        this.word = glyphLocation.word;
        this.glyph = glyphLocation.glyph;
    }

    @Override
    public String toString() {
        return "GlyphLocation{" +
                "line=" + line +
                ", word=" + word +
                ", glyph=" + glyph +
                '}';
    }

    public static WordText getWord(ArrayList<LineText> pageLines, GlyphLocation location) {
        return pageLines.get(location.line).getWords().get(location.word);
    }

    public static GlyphText getGlyph(ArrayList<LineText> pageLines, GlyphLocation location) {
        return pageLines.get(location.line).getWords().get(location.word).getGlyphs().get(location.glyph);
    }

    public static GlyphLocation multiPageSelectGlyphLocation(ArrayList<LineText> pageLines,
                                                             Point2D.Float mouseLocation,
                                                             boolean isDown, boolean leftToRight,
                                                             Shape topMarginExclusion, Shape bottomMarginExclusion) {
        if (pageLines == null) return null;
        // find first glyph of first line
        if (isDown && leftToRight) {
            for (int i = 0, max = pageLines.size(); i < max; i++) {
                if (isLineTextIncluded(pageLines.get(i), topMarginExclusion, bottomMarginExclusion) &&
                        findMouseContainedWithInLine(mouseLocation, pageLines.get(i))) {
                    return new GlyphLocation(i, 0, 0);
                }
            }
        }
        // first line and last glyph
        else if (isDown) {
            for (int i = 0, max = pageLines.size(); i < max; i++) {
                if (isLineTextIncluded(pageLines.get(i), topMarginExclusion, bottomMarginExclusion) &&
                        findMouseContainedWithInLine(mouseLocation, pageLines.get(i))) {
                    int lastWordIndex = pageLines.get(i).getWords().size() - 1;
                    WordText lastWord = pageLines.get(i).getWords().get(lastWordIndex);
                    return new GlyphLocation(i, lastWordIndex, lastWord.getGlyphs().size() - 1);
                }
            }
        }
        // going up is always right to left.
        else {
            for (int i = pageLines.size() - 1; i >= 0; i--) {
                if (isLineTextIncluded(pageLines.get(i), topMarginExclusion, bottomMarginExclusion)
                        && findMouseContainedWithInLine(mouseLocation, pageLines.get(i))) {
                    int lastWordIndex = pageLines.get(i).getWords().size() - 1;
                    WordText lastWord = pageLines.get(i).getWords().get(lastWordIndex);
                    return new GlyphLocation(i, lastWordIndex, lastWord.getGlyphs().size() - 1);
                }
            }
        }
        return null;
    }

    public static boolean isLineTextIncluded(LineText lineText, Shape topMarginExclusion, Shape bottomMarginExclusion) {
        if (enableMarginExclusion) {
            return !(topMarginExclusion.contains(lineText.getBounds()) ||
                    bottomMarginExclusion.contains(lineText.getBounds()));
        }
        return true;
    }

    public static GlyphLocation findGlyphLocation(ArrayList<LineText> pageLines, Point2D.Float cursorLocation,
                                                  boolean isDown, boolean isLocalDown, GlyphLocation lastGlyphEndLocation,
                                                  Shape topMarginExclusion, Shape bottomMarginExclusion) {
        if (pageLines != null) {
            // check for a direct intersection.
            GlyphLocation glyphLocation =
                    findGlyphIntersection(pageLines, cursorLocation, topMarginExclusion, bottomMarginExclusion);
            if (glyphLocation != null) return glyphLocation;

            // check mouse location against y-coordinate of a line  and grab the last line
            // this is buggy if the lines aren't sorted via !org.icepdf.core.views.page.text.preserveColumns.
            if ((isLocalDown && isDown) || isLocalDown) {
                int lastGlyphEndLine = 0;
                if (lastGlyphEndLocation != null) {
                    lastGlyphEndLine = lastGlyphEndLocation.line;
                }
                // get the next line last word.
                int lineIndex = lastGlyphEndLine;
                for (int lineMax = pageLines.size(); lineIndex < lineMax - 1; lineIndex++) {
                    float y1 = pageLines.get(lineIndex).getBounds().y;
                    float y2 = pageLines.get(lineIndex + 1).getBounds().y;
                    if (cursorLocation.y < y1 && cursorLocation.y >= y2) {
                        LineText lineText = pageLines.get(lineIndex + 1);
                        if (isLineTextIncluded(lineText, topMarginExclusion, bottomMarginExclusion)) {
                            java.util.List<WordText> words = lineText.getWords();
                            return new GlyphLocation(lineIndex + 1, words.size() - 1,
                                    words.get(words.size() - 1).getGlyphs().size() - 1);
                        }
                    }
                }
                // else fill the line
                if (lastGlyphEndLine < pageLines.size() && lastGlyphEndLine > pageLines.size() - 5) {
                    LineText lineText = pageLines.get(lastGlyphEndLine);
                    if (isLineTextIncluded(lineText, topMarginExclusion, bottomMarginExclusion)) {
                        java.util.List<WordText> words = lineText.getWords();
                        return new GlyphLocation(lastGlyphEndLine, words.size() - 1,
                                words.get(words.size() - 1).getGlyphs().size() - 1);
                    }
                }
            } else {
                if (lastGlyphEndLocation != null) {
                    // find left most world.
                    int lineIndex = lastGlyphEndLocation.line;
                    for (; lineIndex > 0; lineIndex--) {
                        float y1 = pageLines.get(lineIndex).getBounds().y;
                        float y2 = pageLines.get(lineIndex - 1).getBounds().y;
                        if (cursorLocation.y >= y1 && cursorLocation.y < y2) {
                            LineText lineText = pageLines.get(lineIndex - 1);
                            if (isLineTextIncluded(lineText, topMarginExclusion, bottomMarginExclusion)) {
                                return new GlyphLocation(lineIndex - 1, 0, 0);
                            }
                        }
                    }
                    // else fill the line
                    // todo setup loop to find line closed to the exclusion, not just the first line.
                    if (lastGlyphEndLocation.line < 5) {
                        LineText lineText = pageLines.get(lastGlyphEndLocation.line);
                        if (isLineTextIncluded(lineText, topMarginExclusion, bottomMarginExclusion)) {
                            return new GlyphLocation(lastGlyphEndLocation.line, 0, 0);
                        }
                    }
                }
            }
        }
        return null;
    }

    public static GlyphLocation findGlyphIntersection(ArrayList<LineText> pageLines, Point2D.Float cursorLocation,
                                                      Shape topMarginExclusion, Shape bottomMarginExclusion) {
        LineText pageLine;
        // check for a direct intersection.
        for (int lineIndex = 0, lineMax = pageLines.size(); lineIndex < lineMax; lineIndex++) {
            pageLine = pageLines.get(lineIndex);
            if (pageLine.intersects(cursorLocation) && isLineTextIncluded(pageLine, topMarginExclusion, bottomMarginExclusion)) {
                java.util.List<WordText> lineWords = pageLines.get(lineIndex).getWords();
                WordText currentWord;
                for (int wordIndex = 0, wordMax = lineWords.size(); wordIndex < wordMax; wordIndex++) {
                    currentWord = lineWords.get(wordIndex);
                    if (currentWord.intersects(cursorLocation)) {
                        ArrayList<GlyphText> glyphs = currentWord.getGlyphs();
                        for (int glyphIndex = 0, glyphMax = glyphs.size(); glyphIndex < glyphMax; glyphIndex++) {
                            GlyphText currentGlyph = glyphs.get(glyphIndex);
                            if (currentGlyph.intersects(cursorLocation)) {
                                return new GlyphLocation(lineIndex, wordIndex, glyphIndex);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static GlyphLocation findFirstGlyphLocation(ArrayList<LineText> pageLines, Point2D.Float cursorLocation,
                                                       boolean isDown, boolean isLocalDown, GlyphLocation lastGlyphEndLocation,
                                                       Shape topMarginExclusion, Shape bottomMarginExclusion) {
        if (pageLines != null) {
            // check mouse location against y-coordinate of a line and depending on direction pick
            // first or last world a line.
            if (isDown) {
                // find first word of first y line
                int lineIndex = 0;
                for (int lineMax = pageLines.size() - 1; lineIndex < lineMax; lineIndex++) {
                    float y1 = pageLines.get(lineIndex).getBounds().y;
                    float y2 = pageLines.get(lineIndex + 1).getBounds().y;
                    if (cursorLocation.y < (y1 + pageLines.get(lineIndex).getBounds().height)
                            && cursorLocation.y >= (y2 + pageLines.get(lineIndex + 1).getBounds().height)) {
                        LineText lineText = pageLines.get(lineIndex);
                        if (isLineTextIncluded(lineText, topMarginExclusion, bottomMarginExclusion)) {
                            return new GlyphLocation(lineIndex, 0, 0);
                        }
                    }
                }
            }
            // going up so check against y bottom up and then pick last work.
            else {
                // find left most world.
                int lineIndex = pageLines.size() - 1;
                for (; lineIndex > 0; lineIndex--) {
                    float y1 = pageLines.get(lineIndex - 1).getBounds().y;
                    float y2 = pageLines.get(lineIndex).getBounds().y;
                    if (cursorLocation.y < (y1 + pageLines.get(lineIndex - 1).getBounds().height)
                            && cursorLocation.y >= (y2 + pageLines.get(lineIndex).getBounds().height)) {
                        LineText lineText = pageLines.get(lineIndex);
                        if (isLineTextIncluded(lineText, topMarginExclusion, bottomMarginExclusion)) {
                            java.util.List<WordText> words = lineText.getWords();
                            return new GlyphLocation(lineIndex, words.size() - 1,
                                    words.get(words.size() - 1).getGlyphs().size() - 1);
                        }
                    }
                }
            }
        }
        return null;
    }

    public static boolean findMouseContainedWithInLine(Point2D.Float mouseLocation, LineText pageLines) {
        return pageLines.getBounds().contains(mouseLocation);
    }

    public static int highLightGlyphs(ArrayList<LineText> pageLines, GlyphLocation start, GlyphLocation end,
                                      boolean leftToRight, boolean isDown, boolean isLocalDown, boolean isRight,
                                      Shape topMarginExclusion, Shape bottomMarginExclusion) {
        if (pageLines == null) return 0;
        int selectedCount = fillFirstLine(pageLines.get(start.line), start, end, isDown, isRight, leftToRight);
        // fill middle, if any
        selectedCount += fillMiddleLines(pageLines, start, end, topMarginExclusion, bottomMarginExclusion);
        // fill last line, last line if any
        selectedCount += fillLastLine(pageLines.get(end.line), start, end, isDown, isRight, leftToRight);
        return selectedCount;
    }


    public static int fillFirstLine(LineText pageLine, GlyphLocation start, GlyphLocation end,
                                    boolean isDown, boolean isRight, boolean isLTR) {
        pageLine.setHasHighlight(true);
        java.util.List<WordText> lineWords = pageLine.getWords();
        int selectedCount = 0;
        // last half of the first word
        selectedCount += fillFirstWord(lineWords, start, end, isRight, isDown);
        // first half of the last word
        selectedCount += fillLastWord(lineWords, start, end, isRight, isDown);

        if (start.line == end.line) {
            if (isRight && end.word > start.word) {
                // fill left to right
                for (int wordIndex = start.word + 1; wordIndex <= end.word - 1; wordIndex++) {
                    lineWords.get(wordIndex).selectAll();
                    selectedCount++;
                }
            } else {
                // fill right to left
                for (int wordIndex = start.word - 1; wordIndex >= end.word + 1; wordIndex--) {
                    lineWords.get(wordIndex).selectAll();
                    selectedCount++;
                }
            }
        } else if ((isRight && isDown) || (!isRight && isDown)) {
            // fill right to end of line
            for (int wordIndex = start.word + 1; wordIndex < lineWords.size(); wordIndex++) {
                lineWords.get(wordIndex).selectAll();
                selectedCount++;
            }
        } else {// if ((isRight && !isDown) || (!isRight && !isDown)){
            // fill left to start of line
            for (int wordIndex = start.word - 1; wordIndex >= 0; wordIndex--) {
                lineWords.get(wordIndex).selectAll();
                selectedCount++;
            }
        }
        return selectedCount;
    }

    public static int fillFirstWord(java.util.List<WordText> words, GlyphLocation start, GlyphLocation end,
                                    boolean isRight, boolean isDown) {
        int selectedCount = 0;
        if (end != null && start.line == end.line) {
            if (start.word == end.word) {
                if (isRight) {
                    // same word so we move to select start->end.
                    WordText word = words.get(start.word);
                    word.setHasSelected(true);
                    for (int glyphIndex = start.glyph; glyphIndex <= end.glyph; glyphIndex++) {
                        word.getGlyphs().get(glyphIndex).setSelected(true);
                        selectedCount++;
                    }

                } else {
                    WordText word = words.get(start.word);
                    word.setHasSelected(true);
                    for (int glyphIndex = start.glyph; glyphIndex >= end.glyph; glyphIndex--) {
                        word.getGlyphs().get(glyphIndex).setSelected(true);
                        selectedCount++;
                    }
                }
            } else {
                if (isRight && end.word > start.word) {
                    WordText word = words.get(start.word);
                    word.setHasSelected(true);
                    for (int glyphIndex = start.glyph; glyphIndex < word.getGlyphs().size(); glyphIndex++) {
                        word.getGlyphs().get(glyphIndex).setSelected(true);
                        selectedCount++;
                    }
                } else {
                    WordText word = words.get(start.word);
                    word.setHasSelected(true);
                    for (int glyphIndex = start.glyph; glyphIndex >= 0; glyphIndex--) {
                        word.getGlyphs().get(glyphIndex).setSelected(true);
                        selectedCount++;
                    }
                }
            }
        } else if ((isRight && isDown) || (!isRight && isDown)) {
            WordText word = words.get(start.word);
            word.setHasSelected(true);
            for (int glyphIndex = start.glyph; glyphIndex < word.getGlyphs().size(); glyphIndex++) {
                word.getGlyphs().get(glyphIndex).setSelected(true);
                selectedCount++;
            }
        } else {//if ((isRight && !isDown) || (!isRight && !isDown)) {
            WordText word = words.get(start.word);
            word.setHasSelected(true);
            for (int glyphIndex = start.glyph; glyphIndex >= 0; glyphIndex--) {
                word.getGlyphs().get(glyphIndex).setSelected(true);
            }
        }
        return selectedCount;

    }

    public static int fillLastWord(java.util.List<WordText> words, GlyphLocation start, GlyphLocation end,
                                   boolean isRight, boolean isDown) {
        int selectedCount = 0;
        if (isRight && end.word > start.word) {
            // same word so we move to select start->end.
            if (start.word == end.word && start.line == end.line) {
                // nothing to do handled by the first word.
            }
            // at least two words so we can do the last half of start and first half of end.
            else if (start.line == end.line) {
                WordText word = words.get(end.word);
                word.setHasSelected(true);
                for (int glyphIndex = 0; glyphIndex <= end.glyph; glyphIndex++) {
                    word.getGlyphs().get(glyphIndex).setSelected(true);
                    selectedCount++;
                }
            }
        } else {
            // same word so we move to select start->end.
            if (start.word == end.word && start.line == end.line) {
                WordText word = words.get(end.word);
                word.setHasSelected(true);
                for (int glyphIndex = start.glyph; glyphIndex >= end.glyph; glyphIndex--) {
                    word.getGlyphs().get(glyphIndex).setSelected(true);
                    selectedCount++;
                }
            }
            // at least two words so we can do the last half of start and first half of end.
            else if (start.line == end.line) {
                WordText word = words.get(end.word);
                word.setHasSelected(true);
                for (int glyphIndex = word.getGlyphs().size() - 1; glyphIndex >= end.glyph; glyphIndex--) {
                    word.getGlyphs().get(glyphIndex).setSelected(true);
                    selectedCount++;
                }
            }
        }
        return selectedCount;
    }

    public static int fillLastLine(LineText pageLine, GlyphLocation start, GlyphLocation end,
                                   boolean isDown, boolean isRight, boolean isLTR) {
        java.util.List<WordText> lineWords = pageLine.getWords();
        int selectedCount = 0;
        if (start.line != end.line) {
            pageLine.setHasHighlight(true);
            if (isDown) {
                WordText word = lineWords.get(end.word);
                word.setHasSelected(true);
                for (int glyphIndex = 0; glyphIndex <= end.glyph; glyphIndex++) {
                    word.getGlyphs().get(glyphIndex).setSelected(true);
                    selectedCount++;
                }
                for (int wordIndex = 0; wordIndex < end.word; wordIndex++) {
                    lineWords.get(wordIndex).selectAll();
                    selectedCount++;
                }
            } else {
                selectedCount += fillFirstWord(lineWords, end, null, isRight, true);
                //
                for (int wordIndex = end.word + 1; wordIndex < lineWords.size(); wordIndex++) {
                    lineWords.get(wordIndex).selectAll();
                    selectedCount++;
                }
            }
        }
        return selectedCount;
    }

    public static int fillMiddleLines(ArrayList<LineText> pageLines, GlyphLocation start, GlyphLocation end,
                                      Shape topMarginExclusion, Shape bottomMarginExclusion) {
        GlyphLocation startLocal = new GlyphLocation(start);
        GlyphLocation endLocal = new GlyphLocation(end);
        if (startLocal.line > endLocal.line) {
            GlyphLocation tmp = startLocal;
            startLocal = end;
            endLocal = tmp;
        }
        int selectedCount = 0;
        for (int lineIndex = startLocal.line + 1; lineIndex < endLocal.line; lineIndex++) {
            if (isLineTextIncluded(pageLines.get(lineIndex), topMarginExclusion, bottomMarginExclusion)) {
                pageLines.get(lineIndex).selectAll();
                selectedCount++;
            }
        }
        return selectedCount;
    }

}
