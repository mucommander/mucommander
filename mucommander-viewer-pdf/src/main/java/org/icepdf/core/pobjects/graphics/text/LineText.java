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
package org.icepdf.core.pobjects.graphics.text;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Line text is make up WordText objects.  This structure is to aid the
 * the identification of words for text extraction, searching and selecting.
 *
 * @since 4.0
 */
public class LineText extends AbstractText implements TextSelect {

    private WordText currentWord;

    private List<WordText> words;

    public LineText() {
        words = new ArrayList<WordText>(16);
    }

    public Rectangle2D.Float getBounds() {
        // lazy load the bounds as the calculation is very expensive
        if (bounds == null) {
            // word bounds build from child word bounds.
            for (WordText word : words) {
                if (bounds == null) {
                    bounds = new Rectangle2D.Float();
                    bounds.setRect(word.getBounds());
                } else {
                    bounds.add(word.getBounds());
                }
            }
            // empty line text check, return empty bound.
            if (bounds == null) {
                bounds = new Rectangle2D.Float();
            }
        }
        return bounds;
    }

    /**
     * Add the sprite to the current word in this line/sentence.  This method
     * also candles white space detection and word division.
     *
     * @param sprite sprite to add to line.
     */
    protected void addText(GlyphText sprite) {

        // look for white space characters and insert whitespace word
        if (WordText.detectWhiteSpace(sprite) ||
                WordText.detectPunctuation(sprite, currentWord)) {
            // add as a new word, nothing special otherwise
            WordText newWord = new WordText();
            newWord.setWhiteSpace(true);
            newWord.addText(sprite);
            addWord(newWord);
            // ready new word
            currentWord = null;
        }
        // detect if there should be any spaces between the new sprite
        // and the last sprite.
        else if (getCurrentWord().detectSpace(sprite)) {
            // build space word.
            WordText spaceWord = currentWord.buildSpaceWord(sprite);
            spaceWord.setWhiteSpace(true);
            // add space word,
            addWord(spaceWord);
            // ready a new word
            currentWord = null;
            // add the text again to register the glyph
            addText(sprite);
        }
        // business as usual
        else {
            getCurrentWord().addText(sprite);
        }
    }

    public void clearCurrentWord(){
        // make sure we don't insert a new line if the previous has no words.
        if (currentWord != null &&
                currentWord.size() == 0) {
            return;
        }
        currentWord = null;
    }

    /**
     * Adds the specified word to the end of the line collection and makes
     * the new word the currentWord reference.
     *
     * @param wordText word to add
     */
    private void addWord(WordText wordText) {

        // add the word, text or white space.
        this.words.add(wordText);

        // word test 
        currentWord = wordText;

    }

    public void addAll(List<WordText> words) {
        this.words.addAll(words);
    }

    protected void setWords(List<WordText> words) {
        this.words = words;
    }

    /**
     * Gets the current word, if there is none, one is created.
     *
     * @return current word instance.
     */
    private WordText getCurrentWord() {
        if (currentWord == null) {
            currentWord = new WordText();
            words.add(currentWord);
        }
        return currentWord;
    }

    /**
     * Gets the words that make up this line.
     *
     * @return words in a line.
     */
    public List<WordText> getWords() {
        return words;
    }

    /**
     * Select all text in this line; all word and glyph children.
     */
    public void selectAll() {
        setSelected(true);
        setHasSelected(true);
        for (WordText word : words) {
            word.selectAll();
        }
    }

    /**
     * Deselects all text in this line; all word and glyph children.
     */
    public void clearSelected() {
        setSelected(false);
        setHasSelected(false);
        for (WordText word : words) {
            word.clearSelected();
        }
    }

    /**
     * Dehighlights all text in the line; all word and glyph children.
     */
    public void clearHighlighted() {
        setHighlighted(false);
        setHasHighlight(false);
        for (WordText word : words) {
            word.setHighlighted(false);
        }
    }

    /**
     * Interates over child elements getting the selected text as defined by
     * the child glyphs unicode value. Line breaks and spaces are preserved
     * where possible.
     *
     * @return StringBuffer of selected text in this line.
     */
    public StringBuilder getSelected() {
        StringBuilder selectedText = new StringBuilder();

        for (WordText word : words) {
            selectedText.append(word.getSelected());
        }
        return selectedText;
    }

    public String toString() {
        return words.toString();
    }
}
