/*
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.viewer.binary.search;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.highlight.swing.SearchCodeAreaColorAssessor;
import org.exbin.bined.highlight.swing.SearchMatch;
import org.exbin.bined.swing.basic.CodeArea;
import org.exbin.bined.swing.basic.DefaultCodeAreaPainter;

/**
 * Binary search service.
 */
@ParametersAreNonnullByDefault
public class BinarySearchServiceImpl implements BinarySearchService {

    private final CodeArea codeArea;
    private final SearchCodeAreaColorAssessor searchAssessor;

    public BinarySearchServiceImpl(CodeArea codeArea) {
        this.codeArea = codeArea;
        searchAssessor = (SearchCodeAreaColorAssessor) ((DefaultCodeAreaPainter) codeArea.getPainter()).getColorAssessor();
    }

    @Override
    public void performFind(SearchParameters searchParameters, SearchStatusListener searchStatusListener) {
        SearchCondition condition = searchParameters.getCondition();
        searchStatusListener.clearStatus();
        if (condition.isEmpty()) {
            searchAssessor.clearMatches();
            codeArea.repaint();
            return;
        }

        long position;
        if (searchParameters.isSearchFromCursor()) {
            position = codeArea.getActiveCaretPosition().getDataPosition();
        } else {
            switch (searchParameters.getSearchDirection()) {
            case FORWARD: {
                position = 0;
                break;
            }
            case BACKWARD: {
                position = codeArea.getDataSize() - 1;
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(searchParameters.getSearchDirection());
            }
        }
        searchParameters.setStartPosition(position);

        switch (condition.getSearchMode()) {
        case TEXT: {
            searchForText(searchParameters, searchStatusListener);
            break;
        }
        case BINARY: {
            searchForBinaryData(searchParameters, searchStatusListener);
            break;
        }
        default:
            throw CodeAreaUtils.getInvalidTypeException(condition.getSearchMode());
        }
    }

    /**
     * Performs search by binary data.
     */
    private void searchForBinaryData(SearchParameters searchParameters, SearchStatusListener searchStatusListener) {
        SearchCondition condition = searchParameters.getCondition();
        long position = codeArea.getActiveCaretPosition().getDataPosition();
        SearchMatch currentMatch = searchAssessor.getCurrentMatch();

        if (currentMatch != null) {
            if (currentMatch.getPosition() == position) {
                position++;
            }
            searchAssessor.clearMatches();
        } else if (!searchParameters.isSearchFromCursor()) {
            position = 0;
        }

        BinaryData searchData = condition.getBinaryData();
        BinaryData data = codeArea.getContentData();

        List<SearchMatch> foundMatches = new ArrayList<>();

        long dataSize = data.getDataSize();
        while (position < dataSize - searchData.getDataSize()) {
            int matchLength = 0;
            while (matchLength < searchData.getDataSize()) {
                if (data.getByte(position + matchLength) != searchData.getByte(matchLength)) {
                    break;
                }
                matchLength++;
            }

            if (matchLength == searchData.getDataSize()) {
                SearchMatch match = new SearchMatch();
                match.setPosition(position);
                match.setLength(searchData.getDataSize());
                foundMatches.add(match);

                if (foundMatches.size() == 100 || !searchParameters.isMultipleMatches()) {
                    break;
                }
            }

            position++;
        }

        searchAssessor.setMatches(foundMatches);
        if (!foundMatches.isEmpty()) {
            searchAssessor.setCurrentMatchIndex(0);
            SearchMatch firstMatch = Objects.requireNonNull(searchAssessor.getCurrentMatch());
            codeArea.revealPosition(firstMatch.getPosition(), 0, codeArea.getActiveSection());
        }
        searchStatusListener.setStatus(new FoundMatches(foundMatches.size(), foundMatches.isEmpty() ? -1 : 0));
        codeArea.repaint();
    }

    /**
     * Performs search by text/characters.
     */
    private void searchForText(SearchParameters searchParameters, SearchStatusListener searchStatusListener) {
        SearchCondition condition = searchParameters.getCondition();

        long position = searchParameters.getStartPosition();
        String findText;
        if (searchParameters.isMatchCase()) {
            findText = condition.getSearchText();
        } else {
            findText = condition.getSearchText().toLowerCase();
        }
        BinaryData data = codeArea.getContentData();

        List<SearchMatch> foundMatches = new ArrayList<>();

        Charset charset = codeArea.getCharset();
        int maxBytesPerChar;
        try {
            CharsetEncoder encoder = charset.newEncoder();
            maxBytesPerChar = (int) encoder.maxBytesPerChar();
        } catch (UnsupportedOperationException ex) {
            maxBytesPerChar = 8; // CharsetStreamTranslator.DEFAULT_MAX_BYTES_PER_CHAR;
        }
        byte[] charData = new byte[maxBytesPerChar];
        long dataSize = data.getDataSize();
        while (position <= dataSize - findText.length()) {
            int matchCharLength = 0;
            int matchLength = 0;
            while (matchCharLength < findText.length()) {
                long searchPosition = position + matchLength;
                int bytesToUse = maxBytesPerChar;
                if (searchPosition + bytesToUse > dataSize) {
                    bytesToUse = (int) (dataSize - searchPosition);
                }
                data.copyToArray(searchPosition, charData, 0, bytesToUse);
                char singleChar = new String(charData, charset).charAt(0);
                String singleCharString = String.valueOf(singleChar);
                int characterLength = singleCharString.getBytes(charset).length;

                if (searchParameters.isMatchCase()) {
                    if (singleChar != findText.charAt(matchCharLength)) {
                        break;
                    }
                } else if (singleCharString.toLowerCase().charAt(0) != findText.charAt(matchCharLength)) {
                    break;
                }
                matchCharLength++;
                matchLength += characterLength;
            }

            if (matchCharLength == findText.length()) {
                SearchMatch match = new SearchMatch();
                match.setPosition(position);
                match.setLength(matchLength);
                foundMatches.add(match);

                if (foundMatches.size() == 100 || !searchParameters.isMultipleMatches()) {
                    break;
                }
            }

            switch (searchParameters.getSearchDirection()) {
            case FORWARD: {
                position++;
                break;
            }
            case BACKWARD: {
                position--;
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(searchParameters.getSearchDirection());
            }
        }

        searchAssessor.setMatches(foundMatches);
        if (!foundMatches.isEmpty()) {
            searchAssessor.setCurrentMatchIndex(0);
            SearchMatch firstMatch = searchAssessor.getCurrentMatch();
            codeArea.revealPosition(firstMatch.getPosition(), 0, codeArea.getActiveSection());
        }
        searchStatusListener.setStatus(new FoundMatches(foundMatches.size(), foundMatches.isEmpty() ? -1 : 0));
        codeArea.repaint();
    }

    @Override
    public int getMatchPosition() {
        return searchAssessor.getCurrentMatchIndex();
    }

    @Override
    public void setMatchPosition(int matchPosition) {
        searchAssessor.setCurrentMatchIndex(matchPosition);
        SearchMatch currentMatch = searchAssessor.getCurrentMatch();
        codeArea.revealPosition(currentMatch.getPosition(), 0, codeArea.getActiveSection());
        codeArea.repaint();
    }

    @Override
    public int getMatchesCount() {
        return searchAssessor.getMatches().size();
    }

    @Override
    public void performReplace(SearchParameters searchParameters, ReplaceParameters replaceParameters) {
        SearchCondition replaceCondition = replaceParameters.getCondition();
        SearchMatch currentMatch = searchAssessor.getCurrentMatch();
        if (currentMatch != null) {
            EditableBinaryData editableData = ((EditableBinaryData) codeArea.getContentData());
            editableData.remove(currentMatch.getPosition(), currentMatch.getLength());
            if (replaceCondition.getSearchMode() == SearchCondition.SearchMode.BINARY) {
                editableData.insert(currentMatch.getPosition(), replaceCondition.getBinaryData());
            } else {
                editableData.insert(currentMatch.getPosition(),
                        replaceCondition.getSearchText().getBytes(codeArea.getCharset()));
            }
            searchAssessor.getMatches().remove(currentMatch);
            codeArea.repaint();
        }
    }

    @Override
    public void clearMatches() {
        searchAssessor.clearMatches();
    }
}
