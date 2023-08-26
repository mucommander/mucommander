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

package com.mucommander.ui.theme;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Loads theme instances from properly formatted XML files.
 * 
 * @author Nicolas Rinaudo
 */
class ThemeReader extends DefaultHandler implements ThemeXmlConstants {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThemeReader.class);

    // - XML parser states ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Parsing hasn't started yet. */
    private static final int STATE_UNKNOWN = 0;
    /** Parsing the root element. */
    private static final int STATE_ROOT = 1;
    /** Parsing the table element. */
    private static final int STATE_TABLE = 2;
    /** Parsing the shell element. */
    private static final int STATE_TERMINAL = 3;
    /** Parsing the editor element. */
    private static final int STATE_EDITOR = 4;
    /** Parsing the location bar element. */
    private static final int STATE_LOCATION_BAR = 5;
    /** Parsing the shell.normal element. */
    private static final int STATE_TERMINAL_NORMAL = 6;
    /** Parsing the shell.selected element. */
    private static final int STATE_TERMINAL_SELECTED = 7;
    /** Parsing the editor.normal element. */
    private static final int STATE_EDITOR_NORMAL = 8;
    /** Parsing the location bar.normal element. */
    private static final int STATE_LOCATION_BAR_NORMAL = 9;
    /** Parsing the editor.selected element. */
    private static final int STATE_EDITOR_SELECTED = 10;
    /** Parsing the location bar.selected element. */
    private static final int STATE_LOCATION_BAR_SELECTED = 11;
    /** Parsing the volume_label element. */
    private static final int STATE_STATUS_BAR = 15;
    private static final int STATE_HIDDEN = 16;
    private static final int STATE_HIDDEN_NORMAL = 17;
    private static final int STATE_HIDDEN_SELECTED = 18;
    private static final int STATE_FOLDER = 19;
    private static final int STATE_FOLDER_NORMAL = 20;
    private static final int STATE_FOLDER_SELECTED = 21;
    private static final int STATE_ARCHIVE = 22;
    private static final int STATE_ARCHIVE_NORMAL = 23;
    private static final int STATE_ARCHIVE_SELECTED = 24;
    private static final int STATE_SYMLINK = 25;
    private static final int STATE_SYMLINK_NORMAL = 26;
    private static final int STATE_SYMLINK_SELECTED = 27;
    private static final int STATE_MARKED = 28;
    private static final int STATE_MARKED_NORMAL = 29;
    private static final int STATE_MARKED_SELECTED = 30;
    private static final int STATE_FILE = 31;
    private static final int STATE_FILE_NORMAL = 32;
    private static final int STATE_FILE_SELECTED = 33;
    private static final int STATE_TABLE_NORMAL = 34;
    private static final int STATE_TABLE_SELECTED = 35;
    private static final int STATE_TABLE_ALTERNATE = 36;
    private static final int STATE_TABLE_UNMATCHED = 37;
    /** Parsing the quick list element. */
    private static final int STATE_QUICK_LIST = 38;
    /** Parsing the quick list header element. */
    private static final int STATE_QUICK_LIST_HEADER = 39;
    /** Parsing the quick list item element. */
    private static final int STATE_QUICK_LIST_ITEM = 40;
    private static final int STATE_QUICK_LIST_ITEM_NORMAL = 41;
    private static final int STATE_QUICK_LIST_ITEM_SELECTED = 42;
    private static final int STATE_READ_ONLY = 43;
    private static final int STATE_READ_ONLY_NORMAL = 44;
    private static final int STATE_READ_ONLY_SELECTED = 45;

    /** Cache of available fonts (it is very slow to initialize):
     * https://www.mail-archive.com/java2d-interest@capra.eng.sun.com/msg02877.html,
     * https://stackoverflow.com/questions/3237941/swing-load-available-font-family-slow-down-the-performance
     */
    private static Set<String> availableFonts = null;

    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Theme template that is currently being built. */
    private final ThemeData template;
    /** Current state of the XML parser. */
    private int state;
    /** Used to ignore the content of an unknown tag. */
    private String unknownElement;

    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new theme reader.
     */
    private ThemeReader(ThemeData t) {
        template = t;
        state = STATE_UNKNOWN;
    }

    /**
     * Attempts to read a theme from the specified input stream.
     * 
     * @param in
     *            where to read the theme from.
     * @param template
     *            template in which to store the data.
     * @exception Exception
     *                thrown if an error occured while reading the template.
     */
    public static void read(InputStream in, ThemeData template) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        SAXParser parser = factory.newSAXParser();
        parser.parse(in, new ThemeReader(template));
    }

    // - XML interaction -----------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Notifies the reader that a new XML element is starting.
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // Ignores the content of unknown elements.
        if (unknownElement != null) {
            LOGGER.debug("Ignoring element: {}", qName);
            return;
        }

        // XML root element.
        switch (qName) {
        case ELEMENT_ROOT -> {
            if (state != STATE_UNKNOWN)
                traceIllegalDeclaration(ELEMENT_ROOT);
            state = STATE_ROOT;
        }

        // File table declaration.
        case ELEMENT_TABLE -> {
            if (state != STATE_ROOT)
                traceIllegalDeclaration(qName);
            state = STATE_TABLE;
        }

        // Shell declaration.
        case ELEMENT_TERMINAL -> {
            if (state != STATE_ROOT)
                traceIllegalDeclaration(qName);
            state = STATE_TERMINAL;
        }

        // Editor declaration.
        case ELEMENT_EDITOR -> {
            if (state != STATE_ROOT)
                traceIllegalDeclaration(qName);
            state = STATE_EDITOR;
        }

        // Location bar declaration.
        case ELEMENT_LOCATION_BAR -> {
            if (state != STATE_ROOT)
                traceIllegalDeclaration(qName);
            state = STATE_LOCATION_BAR;
        }

        // Quick list declaration.
        case ELEMENT_QUICK_LIST -> {
            if (state != STATE_ROOT)
                traceIllegalDeclaration(qName);
            state = STATE_QUICK_LIST;
        }

        // Volume label declaration.
        case ELEMENT_STATUS_BAR -> {
            if (state != STATE_ROOT)
                traceIllegalDeclaration(qName);
            state = STATE_STATUS_BAR;
        }
        case ELEMENT_HIDDEN -> {
            if (state != STATE_TABLE)
                traceIllegalDeclaration(qName);
            state = STATE_HIDDEN;
        }
        case ELEMENT_FOLDER -> {
            if (state != STATE_TABLE)
                traceIllegalDeclaration(qName);
            state = STATE_FOLDER;
        }
        case ELEMENT_ARCHIVE -> {
            if (state != STATE_TABLE)
                traceIllegalDeclaration(qName);
            state = STATE_ARCHIVE;
        }
        case ELEMENT_SYMLINK -> {
            if (state != STATE_TABLE)
                traceIllegalDeclaration(qName);
            state = STATE_SYMLINK;
        }
        case ELEMENT_READ_ONLY -> {
            if (state != STATE_TABLE)
                traceIllegalDeclaration(qName);
            state = STATE_READ_ONLY;
        }
        case ELEMENT_MARKED -> {
            if (state != STATE_TABLE)
                traceIllegalDeclaration(qName);
            state = STATE_MARKED;
        }
        case ELEMENT_FILE -> {
            if (state != STATE_TABLE)
                traceIllegalDeclaration(qName);
            state = STATE_FILE;
        }
        case ELEMENT_ALTERNATE -> {
            if (state != STATE_TABLE)
                traceIllegalDeclaration(qName);
            state = STATE_TABLE_ALTERNATE;
        }

        // Header declaration.
        case ELEMENT_HEADER -> {
            if (state == STATE_QUICK_LIST)
                state = STATE_QUICK_LIST_HEADER;
            else
                traceIllegalDeclaration(qName);
        }

        // Item declaration.
        case ELEMENT_ITEM -> {
            if (state == STATE_QUICK_LIST)
                state = STATE_QUICK_LIST_ITEM;
            else
                traceIllegalDeclaration(qName);
        }

        // Normal element declaration.
        case ELEMENT_NORMAL -> {
            switch (state) {
            case STATE_TERMINAL -> state = STATE_TERMINAL_NORMAL;
            case STATE_EDITOR -> state = STATE_EDITOR_NORMAL;
            case STATE_LOCATION_BAR -> state = STATE_LOCATION_BAR_NORMAL;
            case STATE_HIDDEN -> state = STATE_HIDDEN_NORMAL;
            case STATE_FOLDER -> state = STATE_FOLDER_NORMAL;
            case STATE_ARCHIVE -> state = STATE_ARCHIVE_NORMAL;
            case STATE_SYMLINK -> state = STATE_SYMLINK_NORMAL;
            case STATE_READ_ONLY -> state = STATE_READ_ONLY_NORMAL;
            case STATE_MARKED -> state = STATE_MARKED_NORMAL;
            case STATE_FILE -> state = STATE_FILE_NORMAL;
            case STATE_TABLE -> state = STATE_TABLE_NORMAL;
            case STATE_QUICK_LIST_ITEM -> state = STATE_QUICK_LIST_ITEM_NORMAL;
            default -> traceIllegalDeclaration(qName);
            }
        }

        // Selected element declaration.
        case ELEMENT_SELECTED -> {
            switch (state) {
            case STATE_TERMINAL -> state = STATE_TERMINAL_SELECTED;
            case STATE_EDITOR -> state = STATE_EDITOR_SELECTED;
            case STATE_LOCATION_BAR -> state = STATE_LOCATION_BAR_SELECTED;
            case STATE_HIDDEN -> state = STATE_HIDDEN_SELECTED;
            case STATE_FOLDER -> state = STATE_FOLDER_SELECTED;
            case STATE_ARCHIVE -> state = STATE_ARCHIVE_SELECTED;
            case STATE_SYMLINK -> state = STATE_SYMLINK_SELECTED;
            case STATE_READ_ONLY -> state = STATE_READ_ONLY_SELECTED;
            case STATE_MARKED -> state = STATE_MARKED_SELECTED;
            case STATE_FILE -> state = STATE_FILE_SELECTED;
            case STATE_TABLE -> state = STATE_TABLE_SELECTED;
            case STATE_QUICK_LIST_ITEM -> state = STATE_QUICK_LIST_ITEM_SELECTED;
            default -> traceIllegalDeclaration(qName);
            }
        }

        // Font creation.
        case ELEMENT_FONT -> {
            switch (state) {
            case STATE_TERMINAL -> template.setFont(ThemeData.TERMINAL_FONT, createFont(attributes));
            case STATE_EDITOR -> template.setFont(ThemeData.EDITOR_FONT, createFont(attributes));
            case STATE_LOCATION_BAR -> template.setFont(ThemeData.LOCATION_BAR_FONT, createFont(attributes));
            case STATE_STATUS_BAR -> template.setFont(ThemeData.STATUS_BAR_FONT, createFont(attributes));
            case STATE_TABLE -> template.setFont(ThemeData.FILE_TABLE_FONT, createFont(attributes));
            case STATE_QUICK_LIST_HEADER ->
                template.setFont(ThemeData.QUICK_LIST_HEADER_FONT, createFont(attributes));
            case STATE_QUICK_LIST_ITEM ->
                template.setFont(ThemeData.QUICK_LIST_ITEM_FONT, createFont(attributes));
            default -> traceIllegalDeclaration(qName);
            }
        }

        // Unfocused background color.
        case ELEMENT_INACTIVE_BACKGROUND -> {
            switch (state) {
            case STATE_TABLE_NORMAL ->
                template.setColor(ThemeData.FILE_TABLE_INACTIVE_BACKGROUND_COLOR, createColor(attributes));
            case STATE_TABLE_SELECTED ->
                template.setColor(ThemeData.FILE_TABLE_INACTIVE_SELECTED_BACKGROUND_COLOR, createColor(attributes));
            case STATE_TABLE_ALTERNATE ->
                template.setColor(ThemeData.FILE_TABLE_INACTIVE_ALTERNATE_BACKGROUND_COLOR, createColor(attributes));
            default -> traceIllegalDeclaration(qName);
            }
        }

        // Secondary background.
        case ELEMENT_SECONDARY_BACKGROUND -> {
            switch (state) {
            case STATE_TABLE_SELECTED ->
                template.setColor(ThemeData.FILE_TABLE_SELECTED_SECONDARY_BACKGROUND_COLOR, createColor(attributes));
            case STATE_QUICK_LIST_HEADER ->
                template.setColor(ThemeData.QUICK_LIST_HEADER_SECONDARY_BACKGROUND_COLOR, createColor(attributes));
            default -> traceIllegalDeclaration(qName);
            }
        }

        // Inactive secondary background.
        case ELEMENT_INACTIVE_SECONDARY_BACKGROUND -> {
            if (state == STATE_TABLE_SELECTED) {
                template.setColor(ThemeData.FILE_TABLE_INACTIVE_SELECTED_SECONDARY_BACKGROUND_COLOR,
                        createColor(attributes));
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // Unfocused foreground color.
        case ELEMENT_INACTIVE_FOREGROUND -> {
            switch (state) {
            case STATE_FILE_NORMAL ->
                template.setColor(ThemeData.FILE_INACTIVE_FOREGROUND_COLOR, createColor(attributes));
            case STATE_FOLDER_NORMAL ->
                template.setColor(ThemeData.FOLDER_INACTIVE_FOREGROUND_COLOR, createColor(attributes));
            case STATE_ARCHIVE_NORMAL ->
                template.setColor(ThemeData.ARCHIVE_INACTIVE_FOREGROUND_COLOR, createColor(attributes));
            case STATE_SYMLINK_NORMAL ->
                template.setColor(ThemeData.SYMLINK_INACTIVE_FOREGROUND_COLOR, createColor(attributes));
            case STATE_READ_ONLY_NORMAL ->
                template.setColor(ThemeData.READ_ONLY_INACTIVE_FOREGROUND_COLOR, createColor(attributes));
            case STATE_HIDDEN_NORMAL ->
                template.setColor(ThemeData.HIDDEN_FILE_INACTIVE_FOREGROUND_COLOR, createColor(attributes));
            case STATE_MARKED_NORMAL ->
                template.setColor(ThemeData.MARKED_INACTIVE_FOREGROUND_COLOR, createColor(attributes));
            case STATE_FILE_SELECTED ->
                template.setColor(ThemeData.FILE_INACTIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            case STATE_FOLDER_SELECTED ->
                template.setColor(ThemeData.FOLDER_INACTIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            case STATE_ARCHIVE_SELECTED ->
                template.setColor(ThemeData.ARCHIVE_INACTIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            case STATE_SYMLINK_SELECTED ->
                template.setColor(ThemeData.SYMLINK_INACTIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            case STATE_READ_ONLY_SELECTED ->
                template.setColor(ThemeData.READ_ONLY_INACTIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            case STATE_HIDDEN_SELECTED ->
                template.setColor(ThemeData.HIDDEN_FILE_INACTIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            case STATE_MARKED_SELECTED ->
                template.setColor(ThemeData.MARKED_INACTIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            default -> traceIllegalDeclaration(qName);
            }
        }

        // File table border color.
        case ELEMENT_BORDER -> {
            switch (state) {
            case STATE_TABLE -> template.setColor(ThemeData.FILE_TABLE_BORDER_COLOR, createColor(attributes));
            case STATE_STATUS_BAR ->
                template.setColor(ThemeData.STATUS_BAR_BORDER_COLOR, createColor(attributes));
            default -> traceIllegalDeclaration(qName);
            }
        }

        // File table inactive border color.
        case ELEMENT_INACTIVE_BORDER -> {
            if (state == STATE_TABLE) {
                template.setColor(ThemeData.FILE_TABLE_INACTIVE_BORDER_COLOR, createColor(attributes));
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // File table outline color.
        case ELEMENT_OUTLINE -> {
            if (state == STATE_TABLE) {
                template.setColor(ThemeData.FILE_TABLE_SELECTED_OUTLINE_COLOR, createColor(attributes));
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // File table inactive outline color.
        case ELEMENT_INACTIVE_OUTLINE -> {
            if (state == STATE_TABLE) {
                template.setColor(ThemeData.FILE_TABLE_INACTIVE_SELECTED_OUTLINE_COLOR, createColor(attributes));
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // Unmatched file table.
        case ELEMENT_UNMATCHED -> {
            if (state == STATE_TABLE) {
            } else {
                traceIllegalDeclaration(qName);
            }
            state = STATE_TABLE_UNMATCHED;
        }

        // Background color.
        case ELEMENT_BACKGROUND -> {
            switch (state) {
            case STATE_TABLE_NORMAL ->
                template.setColor(ThemeData.FILE_TABLE_BACKGROUND_COLOR, createColor(attributes));
            case STATE_TABLE_SELECTED ->
                template.setColor(ThemeData.FILE_TABLE_SELECTED_BACKGROUND_COLOR, createColor(attributes));
            case STATE_TABLE_ALTERNATE ->
                template.setColor(ThemeData.FILE_TABLE_ALTERNATE_BACKGROUND_COLOR, createColor(attributes));
            case STATE_TABLE_UNMATCHED ->
                template.setColor(ThemeData.FILE_TABLE_UNMATCHED_BACKGROUND_COLOR, createColor(attributes));
            case STATE_TERMINAL_NORMAL ->
                template.setColor(ThemeData.TERMINAL_BACKGROUND_COLOR, createColor(attributes));
            case STATE_TERMINAL_SELECTED ->
                template.setColor(ThemeData.TERMINAL_SELECTED_BACKGROUND_COLOR, createColor(attributes));
            case STATE_EDITOR_NORMAL ->
                template.setColor(ThemeData.EDITOR_BACKGROUND_COLOR, createColor(attributes));
            case STATE_EDITOR_SELECTED ->
                template.setColor(ThemeData.EDITOR_SELECTED_BACKGROUND_COLOR, createColor(attributes));
            case STATE_LOCATION_BAR_NORMAL ->
                template.setColor(ThemeData.LOCATION_BAR_BACKGROUND_COLOR, createColor(attributes));
            case STATE_LOCATION_BAR_SELECTED ->
                template.setColor(ThemeData.LOCATION_BAR_SELECTED_BACKGROUND_COLOR, createColor(attributes));
            case STATE_STATUS_BAR ->
                template.setColor(ThemeData.STATUS_BAR_BACKGROUND_COLOR, createColor(attributes));
            case STATE_QUICK_LIST_HEADER ->
                template.setColor(ThemeData.QUICK_LIST_HEADER_BACKGROUND_COLOR, createColor(attributes));
            case STATE_QUICK_LIST_ITEM_NORMAL ->
                template.setColor(ThemeData.QUICK_LIST_ITEM_BACKGROUND_COLOR, createColor(attributes));
            case STATE_QUICK_LIST_ITEM_SELECTED ->
                template.setColor(ThemeData.QUICK_LIST_SELECTED_ITEM_BACKGROUND_COLOR, createColor(attributes));
            default -> traceIllegalDeclaration(qName);
            }
        }

        // Progress bar color.
        case ELEMENT_PROGRESS -> {
            if (state == STATE_LOCATION_BAR) {
                template.setColor(ThemeData.LOCATION_BAR_PROGRESS_COLOR, createColor(attributes));
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // 'OK' color.
        case ELEMENT_OK -> {
            if (state == STATE_STATUS_BAR) {
                template.setColor(ThemeData.STATUS_BAR_OK_COLOR, createColor(attributes));
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // 'WARNING' color.
        case ELEMENT_WARNING -> {
            if (state == STATE_STATUS_BAR) {
                template.setColor(ThemeData.STATUS_BAR_WARNING_COLOR, createColor(attributes));
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // 'CRITICAL' color.
        case ELEMENT_CRITICAL -> {
            if (state == STATE_STATUS_BAR) {
                template.setColor(ThemeData.STATUS_BAR_CRITICAL_COLOR, createColor(attributes));
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // Text color.
        case ELEMENT_FOREGROUND -> {
            switch (state) {
            case STATE_HIDDEN_NORMAL ->
                template.setColor(ThemeData.HIDDEN_FILE_FOREGROUND_COLOR, createColor(attributes));
            case STATE_HIDDEN_SELECTED ->
                template.setColor(ThemeData.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            case STATE_TABLE_UNMATCHED ->
                template.setColor(ThemeData.FILE_TABLE_UNMATCHED_FOREGROUND_COLOR, createColor(attributes));
            case STATE_FOLDER_NORMAL ->
                template.setColor(ThemeData.FOLDER_FOREGROUND_COLOR, createColor(attributes));
            case STATE_FOLDER_SELECTED ->
                template.setColor(ThemeData.FOLDER_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            case STATE_ARCHIVE_NORMAL ->
                template.setColor(ThemeData.ARCHIVE_FOREGROUND_COLOR, createColor(attributes));
            case STATE_ARCHIVE_SELECTED ->
                template.setColor(ThemeData.ARCHIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            case STATE_SYMLINK_NORMAL ->
                template.setColor(ThemeData.SYMLINK_FOREGROUND_COLOR, createColor(attributes));
            case STATE_SYMLINK_SELECTED ->
                template.setColor(ThemeData.SYMLINK_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            case STATE_READ_ONLY_NORMAL ->
                template.setColor(ThemeData.READ_ONLY_FOREGROUND_COLOR, createColor(attributes));
            case STATE_READ_ONLY_SELECTED ->
                template.setColor(ThemeData.READ_ONLY_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            case STATE_MARKED_NORMAL ->
                template.setColor(ThemeData.MARKED_FOREGROUND_COLOR, createColor(attributes));
            case STATE_MARKED_SELECTED ->
                template.setColor(ThemeData.MARKED_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            case STATE_FILE_NORMAL ->
                template.setColor(ThemeData.FILE_FOREGROUND_COLOR, createColor(attributes));
            case STATE_FILE_SELECTED ->
                template.setColor(ThemeData.FILE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            case STATE_TERMINAL_NORMAL ->
                template.setColor(ThemeData.TERMINAL_FOREGROUND_COLOR, createColor(attributes));
            case STATE_TERMINAL_SELECTED ->
                template.setColor(ThemeData.TERMINAL_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            case STATE_EDITOR_NORMAL ->
                template.setColor(ThemeData.EDITOR_FOREGROUND_COLOR, createColor(attributes));
            case STATE_EDITOR_SELECTED ->
                template.setColor(ThemeData.EDITOR_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            case STATE_LOCATION_BAR_NORMAL ->
                template.setColor(ThemeData.LOCATION_BAR_FOREGROUND_COLOR, createColor(attributes));
            case STATE_LOCATION_BAR_SELECTED ->
                template.setColor(ThemeData.LOCATION_BAR_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            case STATE_STATUS_BAR ->
                template.setColor(ThemeData.STATUS_BAR_FOREGROUND_COLOR, createColor(attributes));
            case STATE_QUICK_LIST_HEADER ->
                template.setColor(ThemeData.QUICK_LIST_HEADER_FOREGROUND_COLOR, createColor(attributes));
            case STATE_QUICK_LIST_ITEM_NORMAL ->
                template.setColor(ThemeData.QUICK_LIST_ITEM_FOREGROUND_COLOR, createColor(attributes));
            case STATE_QUICK_LIST_ITEM_SELECTED ->
                template.setColor(ThemeData.QUICK_LIST_SELECTED_ITEM_FOREGROUND_COLOR, createColor(attributes));
            default -> traceIllegalDeclaration(qName);
            }
        }
        default -> traceIllegalDeclaration(qName);
        }
    }

    /**
     * Notifies the reader that the current element declaration is over.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // If we're in an unknown element....
        if (unknownElement != null) {
            // If it just closed, resume normal parsing.
            if (qName.equals(unknownElement))
                unknownElement = null;
            // Ignores all other tags.
            else
                return;
        }

        // XML root element.
        switch (qName) {
        case ELEMENT_ROOT -> state = STATE_UNKNOWN;

        // File table declaration.
        case ELEMENT_TABLE -> state = STATE_ROOT;
        case ELEMENT_ALTERNATE -> state = STATE_TABLE;
        case ELEMENT_UNMATCHED -> state = STATE_TABLE;
        case ELEMENT_HIDDEN -> state = STATE_TABLE;
        case ELEMENT_FOLDER -> state = STATE_TABLE;
        case ELEMENT_ARCHIVE -> state = STATE_TABLE;
        case ELEMENT_SYMLINK -> state = STATE_TABLE;
        case ELEMENT_READ_ONLY -> state = STATE_TABLE;
        case ELEMENT_MARKED -> state = STATE_TABLE;
        case ELEMENT_FILE -> state = STATE_TABLE;

        // Shell declaration.
        case ELEMENT_TERMINAL -> state = STATE_ROOT;

        // Editor declaration.
        case ELEMENT_EDITOR -> state = STATE_ROOT;

        // Location bar declaration.
        case ELEMENT_LOCATION_BAR -> state = STATE_ROOT;

        // Quick list declaration.
        case ELEMENT_QUICK_LIST -> state = STATE_ROOT;

        // Volume label declaration
        case ELEMENT_STATUS_BAR -> state = STATE_ROOT;

        // Header declaration.
        case ELEMENT_HEADER -> {
            if (state == STATE_QUICK_LIST_HEADER)
                state = STATE_QUICK_LIST;
        }

        // Item declaration.
        case ELEMENT_ITEM -> {
            if (state == STATE_QUICK_LIST_ITEM)
                state = STATE_QUICK_LIST;
        }

        // Normal element declaration.
        case ELEMENT_NORMAL -> {
            switch (state) {
            case STATE_TERMINAL_NORMAL -> state = STATE_TERMINAL;
            case STATE_HIDDEN_NORMAL -> state = STATE_HIDDEN;
            case STATE_FOLDER_NORMAL -> state = STATE_FOLDER;
            case STATE_ARCHIVE_NORMAL -> state = STATE_ARCHIVE;
            case STATE_SYMLINK_NORMAL -> state = STATE_SYMLINK;
            case STATE_READ_ONLY_NORMAL -> state = STATE_READ_ONLY;
            case STATE_MARKED_NORMAL -> state = STATE_MARKED;
            case STATE_FILE_NORMAL -> state = STATE_FILE;
            case STATE_EDITOR_NORMAL -> state = STATE_EDITOR;
            case STATE_LOCATION_BAR_NORMAL -> state = STATE_LOCATION_BAR;
            case STATE_TABLE_NORMAL -> state = STATE_TABLE;
            case STATE_QUICK_LIST_ITEM_NORMAL -> state = STATE_QUICK_LIST_ITEM;
            }
        }

        // Selected element declaration.
        case ELEMENT_SELECTED -> {
            switch (state) {
            case STATE_TERMINAL_SELECTED -> state = STATE_TERMINAL;
            case STATE_HIDDEN_SELECTED -> state = STATE_HIDDEN;
            case STATE_FOLDER_SELECTED -> state = STATE_FOLDER;
            case STATE_ARCHIVE_SELECTED -> state = STATE_ARCHIVE;
            case STATE_SYMLINK_SELECTED -> state = STATE_SYMLINK;
            case STATE_READ_ONLY_SELECTED -> state = STATE_READ_ONLY;
            case STATE_MARKED_SELECTED -> state = STATE_MARKED;
            case STATE_FILE_SELECTED -> state = STATE_FILE;
            case STATE_EDITOR_SELECTED -> state = STATE_EDITOR;
            case STATE_LOCATION_BAR_SELECTED -> state = STATE_LOCATION_BAR;
            case STATE_TABLE_SELECTED -> state = STATE_TABLE;
            case STATE_QUICK_LIST_ITEM_SELECTED -> state = STATE_QUICK_LIST_ITEM;
            }
        }
        }
    }

    public static void preSetAvailableFonts(Set<String> fontCache) {
        if (!fontCache.isEmpty()) {
            availableFonts = fontCache;
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                LOGGER.info("Going to refresh available fonts from OS....");
                availableFonts = getAvailableFontsFromOS();
            }
        }, 60 * 1000L);
    }

    public static Set<String> getAvailableFonts() {
        if (availableFonts == null) {
            availableFonts = getAvailableFontsFromOS();
        }
        return availableFonts;
    }

    // - Helper methods ------------------------------------------------------
    // -----------------------------------------------------------------------

    private static Set<String> getAvailableFontsFromOS() {
        return new HashSet<>(List.of(
                        // takes long time....
                        GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList()));
    }

    /**
     * Checks whether the specified font is available on the system.
     * 
     * @param font
     *            name of the font to check for.
     * @return <code>true</code> if the font is available, <code>false</code> otherwise.
     */
    private static boolean isFontAvailable(String font) {
        if (font == null) {
            return false;
        }
        return getAvailableFonts().contains(font.toLowerCase());
    }

    /**
     * Creates a font from the specified XML attributes.
     * <p>
     * Ignored attributes will be set to their default values.
     * </p>
     * 
     * @param attributes
     *            XML attributes describing the font to use.
     * @return the resulting Font instance.
     */
    private static Font createFont(Attributes attributes) {
        String buffer; // Buffer for attribute values.
        int size; // Font size.
        int style; // Font style.
        StringTokenizer parser; // Used to parse the font family.

        // Computes the font style.
        style = 0;
        if (((buffer = attributes.getValue(ATTRIBUTE_BOLD)) != null) && buffer.equals(VALUE_TRUE))
            style |= Font.BOLD;
        if (((buffer = attributes.getValue(ATTRIBUTE_ITALIC)) != null) && buffer.equals(VALUE_TRUE))
            style |= Font.ITALIC;

        // Computes the font size.
        if ((buffer = attributes.getValue(ATTRIBUTE_SIZE)) == null) {
            LOGGER.debug("Missing font size attribute in theme, ignoring.");
            return null;
        }
        size = Integer.parseInt(buffer);

        // Computes the font family.
        if ((buffer = attributes.getValue(ATTRIBUTE_FAMILY)) == null) {
            LOGGER.debug("Missing font family attribute in theme, ignoring.");
            return null;
        }

        // Looks through the list of declared fonts to find one that is installed on the system.
        parser = new StringTokenizer(buffer, ",");
        while (parser.hasMoreTokens()) {
            buffer = parser.nextToken().trim();

            // Font was found, use it.
            if (isFontAvailable(buffer))
                return new Font(buffer, style, size);
        }

        // No font was found, instructs the ThemeManager to use the system default.
        LOGGER.debug("Requested font families are not installed on the system, using default.");
        return null;
    }

    /**
     * Creates a color from the specified XML attributes.
     * 
     * @param attributes
     *            XML attributes describing the font to use.
     * @return the resulting Color instance.
     */
    private static Color createColor(Attributes attributes) {
        String buffer;
        int color;

        // Retrieves the color attribute's value.
        if ((buffer = attributes.getValue(ATTRIBUTE_COLOR)) == null) {
            LOGGER.debug("Missing color attribute in theme, ignoring.");
            return null;
        }
        color = Integer.parseInt(buffer, 16);

        // Retrieves the transparency attribute's value..
        if ((buffer = attributes.getValue(ATTRIBUTE_ALPHA)) == null)
            return new Color(color);
        return new Color(color | (Integer.parseInt(buffer, 16) << 24), true);
    }

    // - Error generation methods --------------------------------------------
    // -----------------------------------------------------------------------
    private void traceIllegalDeclaration(String element) {
        unknownElement = element;
        LOGGER.debug("Unexpected start of element {}, ignoring.", element);
    }
}
