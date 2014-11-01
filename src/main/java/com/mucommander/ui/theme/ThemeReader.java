/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Loads theme instances from properly formatted XML files.
 * @author Nicolas Rinaudo
 */
class ThemeReader extends DefaultHandler implements ThemeXmlConstants {
	private static final Logger LOGGER = LoggerFactory.getLogger(ThemeReader.class);
	
    // - XML parser states ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Parsing hasn't started yet. */
    private static final int STATE_UNKNOWN                  = 0;
    /** Parsing the root element. */
    private static final int STATE_ROOT                     = 1;
    /** Parsing the table element.*/
    private static final int STATE_TABLE                    = 2;
    /** Parsing the shell element. */
    private static final int STATE_SHELL                    = 3;
    /** Parsing the editor element. */
    private static final int STATE_EDITOR                   = 4;
    /** Parsing the location bar element. */
    private static final int STATE_LOCATION_BAR             = 5;
    /** Parsing the shell.normal element. */
    private static final int STATE_SHELL_NORMAL             = 6;
    /** Parsing the shell.selected element. */
    private static final int STATE_SHELL_SELECTED           = 7;
    /** Parsing the editor.normal element. */
    private static final int STATE_EDITOR_NORMAL            = 8;
    /** Parsing the location bar.normal element. */
    private static final int STATE_LOCATION_BAR_NORMAL      = 9;
    /** Parsing the editor.selected element. */
    private static final int STATE_EDITOR_SELECTED          = 10;
    /** Parsing the location bar.selected element. */
    private static final int STATE_LOCATION_BAR_SELECTED    = 11;
    /** Parsing the shell_history element. */
    private static final int STATE_SHELL_HISTORY            = 12;
    /** Parsing the shell_history.normal element. */
    private static final int STATE_SHELL_HISTORY_NORMAL     = 13;
    /** Parsing the shell_history.selected element. */
    private static final int STATE_SHELL_HISTORY_SELECTED   = 14;
    /** Parsing the volume_label element. */
    private static final int STATE_STATUS_BAR               = 15;
    private static final int STATE_HIDDEN                   = 16;
    private static final int STATE_HIDDEN_NORMAL            = 17;
    private static final int STATE_HIDDEN_SELECTED          = 18;
    private static final int STATE_FOLDER                   = 19;
    private static final int STATE_FOLDER_NORMAL            = 20;
    private static final int STATE_FOLDER_SELECTED          = 21;
    private static final int STATE_ARCHIVE                  = 22;
    private static final int STATE_ARCHIVE_NORMAL           = 23;
    private static final int STATE_ARCHIVE_SELECTED         = 24;
    private static final int STATE_SYMLINK                  = 25;
    private static final int STATE_SYMLINK_NORMAL           = 26;
    private static final int STATE_SYMLINK_SELECTED         = 27;
    private static final int STATE_MARKED                   = 28;
    private static final int STATE_MARKED_NORMAL            = 29;
    private static final int STATE_MARKED_SELECTED          = 30;
    private static final int STATE_FILE                     = 31;
    private static final int STATE_FILE_NORMAL              = 32;
    private static final int STATE_FILE_SELECTED            = 33;
    private static final int STATE_TABLE_NORMAL             = 34;
    private static final int STATE_TABLE_SELECTED           = 35;
    private static final int STATE_TABLE_ALTERNATE          = 36;
    private static final int STATE_TABLE_UNMATCHED          = 37;
    /** Parsing the quick list element. */
    private static final int STATE_QUICK_LIST               = 38;
    /** Parsing the quick list header element. */
    private static final int STATE_QUICK_LIST_HEADER        = 39;
    /** Parsing the quick list item element. */
    private static final int STATE_QUICK_LIST_ITEM          = 40;
    private static final int STATE_QUICK_LIST_ITEM_NORMAL   = 41;
    private static final int STATE_QUICK_LIST_ITEM_SELECTED = 42;


    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Theme template that is currently being built. */
    private ThemeData     template;
    /** Current state of the XML parser. */
    private int           state;
    /** Used to ignore the content of an unknown tag. */
    private String        unknownElement;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new theme reader.
     */
    private ThemeReader(ThemeData t) {
        template = t;
        state    = STATE_UNKNOWN;
    }

    /**
     * Attempts to read a theme from the specified input stream.
     * @param     in        where to read the theme from.
     * @param     template  template in which to store the data.
     * @exception Exception thrown if an error occured while reading the template.
     */
    public static void read(InputStream in, ThemeData template) throws Exception {SAXParserFactory.newInstance().newSAXParser().parse(in, new ThemeReader(template));}


    // - XML interaction -----------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Notifies the reader that a new XML element is starting.
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // Ignores the content of unknown elements.
        if(unknownElement != null) {
            LOGGER.debug("Ignoring element " + qName);
            return;
        }

        // XML root element.
        if(qName.equals(ELEMENT_ROOT)) {
            if(state != STATE_UNKNOWN)
                traceIllegalDeclaration(ELEMENT_ROOT);
            state = STATE_ROOT;
        }

        // File table declaration.
        else if(qName.equals(ELEMENT_TABLE)) {
            if(state != STATE_ROOT)
                traceIllegalDeclaration(qName);
            state = STATE_TABLE;
        }

        // Shell declaration.
        else if(qName.equals(ELEMENT_SHELL)) {
            if(state != STATE_ROOT)
                traceIllegalDeclaration(qName);
            state = STATE_SHELL;
        }

        // Editor declaration.
        else if(qName.equals(ELEMENT_EDITOR)) {
            if(state != STATE_ROOT)
                traceIllegalDeclaration(qName);
            state = STATE_EDITOR;
        }

        // Location bar declaration.
        else if(qName.equals(ELEMENT_LOCATION_BAR)) {
            if(state != STATE_ROOT)
                traceIllegalDeclaration(qName);
            state = STATE_LOCATION_BAR;
        }
        
        // Quick list declaration.
        else if(qName.equals(ELEMENT_QUICK_LIST)) {
            if(state != STATE_ROOT)
                traceIllegalDeclaration(qName);
            state = STATE_QUICK_LIST;
        }

        // Shell history declaration.
        else if(qName.equals(ELEMENT_SHELL_HISTORY)) {
            if(state != STATE_ROOT)
                traceIllegalDeclaration(qName);
            state = STATE_SHELL_HISTORY;
        }

        // Volume label declaration.
        else if(qName.equals(ELEMENT_STATUS_BAR)) {
            if(state != STATE_ROOT)
                traceIllegalDeclaration(qName);
            state = STATE_STATUS_BAR;
        }

        else if(qName.equals(ELEMENT_HIDDEN)) {
            if(state != STATE_TABLE)
                traceIllegalDeclaration(qName);
            state = STATE_HIDDEN;
        }

        else if(qName.equals(ELEMENT_FOLDER)) {
            if(state != STATE_TABLE)
                traceIllegalDeclaration(qName);
            state = STATE_FOLDER;
        }

        else if(qName.equals(ELEMENT_ARCHIVE)) {
            if(state != STATE_TABLE)
                traceIllegalDeclaration(qName);
            state = STATE_ARCHIVE;
        }

        else if(qName.equals(ELEMENT_SYMLINK)) {
            if(state != STATE_TABLE)
                traceIllegalDeclaration(qName);
            state = STATE_SYMLINK;
        }

        else if(qName.equals(ELEMENT_MARKED)) {
            if(state != STATE_TABLE)
                traceIllegalDeclaration(qName);
            state = STATE_MARKED;
        }

        else if(qName.equals(ELEMENT_FILE)) {
            if(state != STATE_TABLE)
                traceIllegalDeclaration(qName);
            state = STATE_FILE;
        }

        else if(qName.equals(ELEMENT_ALTERNATE)) {
            if(state != STATE_TABLE)
                traceIllegalDeclaration(qName);
            state = STATE_TABLE_ALTERNATE;
        }
        
        // Header declaration.
        else if(qName.equals(ELEMENT_HEADER)) {
        	if(state == STATE_QUICK_LIST)
                state = STATE_QUICK_LIST_HEADER;
            else
                traceIllegalDeclaration(qName);
        }
        
        // Item declaration.
        else if(qName.equals(ELEMENT_ITEM)) {
        	if(state == STATE_QUICK_LIST)
                state = STATE_QUICK_LIST_ITEM;
            else
                traceIllegalDeclaration(qName);
        }

        // Normal element declaration.
        else if(qName.equals(ELEMENT_NORMAL)) {
            if(state == STATE_SHELL)
                state = STATE_SHELL_NORMAL;
            else if(state == STATE_EDITOR)
                state = STATE_EDITOR_NORMAL;
            else if(state == STATE_LOCATION_BAR)
                state = STATE_LOCATION_BAR_NORMAL;
            else if(state == STATE_SHELL_HISTORY)
                state = STATE_SHELL_HISTORY_NORMAL;
            else if(state == STATE_HIDDEN)
                state = STATE_HIDDEN_NORMAL;
            else if(state == STATE_FOLDER)
                state = STATE_FOLDER_NORMAL;
            else if(state == STATE_ARCHIVE)
                state = STATE_ARCHIVE_NORMAL;
            else if(state == STATE_SYMLINK)
                state = STATE_SYMLINK_NORMAL;
            else if(state == STATE_MARKED)
                state = STATE_MARKED_NORMAL;
            else if(state == STATE_FILE)
                state = STATE_FILE_NORMAL;
            else if(state == STATE_TABLE)
                state = STATE_TABLE_NORMAL;
            else if(state == STATE_QUICK_LIST_ITEM)
            	state = STATE_QUICK_LIST_ITEM_NORMAL;
            else
                traceIllegalDeclaration(qName);
        }

        // Selected element declaration.
        else if(qName.equals(ELEMENT_SELECTED)) {
            if(state == STATE_SHELL)
                state = STATE_SHELL_SELECTED;
            else if(state == STATE_EDITOR)
                state = STATE_EDITOR_SELECTED;
            else if(state == STATE_LOCATION_BAR)
                state = STATE_LOCATION_BAR_SELECTED;
            else if(state == STATE_SHELL_HISTORY)
                state = STATE_SHELL_HISTORY_SELECTED;
            else if(state == STATE_HIDDEN)
                state = STATE_HIDDEN_SELECTED;
            else if(state == STATE_FOLDER)
                state = STATE_FOLDER_SELECTED;
            else if(state == STATE_ARCHIVE)
                state = STATE_ARCHIVE_SELECTED;
            else if(state == STATE_SYMLINK)
                state = STATE_SYMLINK_SELECTED;
            else if(state == STATE_MARKED)
                state = STATE_MARKED_SELECTED;
            else if(state == STATE_FILE)
                state = STATE_FILE_SELECTED;
            else if(state == STATE_TABLE)
                state = STATE_TABLE_SELECTED;
            else if(state == STATE_QUICK_LIST_ITEM)
            	state = STATE_QUICK_LIST_ITEM_SELECTED;
            else
                traceIllegalDeclaration(qName);
        }

        // Font creation.
        else if(qName.equals(ELEMENT_FONT)) {
            if(state == STATE_SHELL)
                template.setFont(ThemeData.SHELL_FONT, createFont(attributes));
            else if(state == STATE_EDITOR)
                template.setFont(ThemeData.EDITOR_FONT, createFont(attributes));
            else if(state == STATE_LOCATION_BAR)
                template.setFont(ThemeData.LOCATION_BAR_FONT, createFont(attributes));
            else if(state == STATE_SHELL_HISTORY)
                template.setFont(ThemeData.SHELL_HISTORY_FONT, createFont(attributes));
            else if(state == STATE_STATUS_BAR)
                template.setFont(ThemeData.STATUS_BAR_FONT, createFont(attributes));
            else if(state == STATE_TABLE)
                template.setFont(ThemeData.FILE_TABLE_FONT, createFont(attributes));
            else if(state == STATE_QUICK_LIST_HEADER)
            	template.setFont(ThemeData.QUICK_LIST_HEADER_FONT, createFont(attributes));
            else if(state == STATE_QUICK_LIST_ITEM)
            	template.setFont(ThemeData.QUICK_LIST_ITEM_FONT, createFont(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // Unfocused background color.
        else if(qName.equals(ELEMENT_INACTIVE_BACKGROUND)) {
            if(state == STATE_TABLE_NORMAL)
                template.setColor(ThemeData.FILE_TABLE_INACTIVE_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_TABLE_SELECTED)
                template.setColor(ThemeData.FILE_TABLE_INACTIVE_SELECTED_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_TABLE_ALTERNATE)
                template.setColor(ThemeData.FILE_TABLE_INACTIVE_ALTERNATE_BACKGROUND_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // Secondary background.
        else if(qName.equals(ELEMENT_SECONDARY_BACKGROUND)) {
            if(state == STATE_TABLE_SELECTED)
                template.setColor(ThemeData.FILE_TABLE_SELECTED_SECONDARY_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_QUICK_LIST_HEADER)
            	template.setColor(ThemeData.QUICK_LIST_HEADER_SECONDARY_BACKGROUND_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // Inactive secondary background.
        else if(qName.equals(ELEMENT_INACTIVE_SECONDARY_BACKGROUND)) {
            if(state == STATE_TABLE_SELECTED)
                template.setColor(ThemeData.FILE_TABLE_INACTIVE_SELECTED_SECONDARY_BACKGROUND_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // Unfocused foreground color.
        else if(qName.equals(ELEMENT_INACTIVE_FOREGROUND)) {
            if(state == STATE_FILE_NORMAL)
                template.setColor(ThemeData.FILE_INACTIVE_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_FOLDER_NORMAL)
                template.setColor(ThemeData.FOLDER_INACTIVE_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_ARCHIVE_NORMAL)
                template.setColor(ThemeData.ARCHIVE_INACTIVE_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_SYMLINK_NORMAL)
                template.setColor(ThemeData.SYMLINK_INACTIVE_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_HIDDEN_NORMAL)
                template.setColor(ThemeData.HIDDEN_FILE_INACTIVE_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_MARKED_NORMAL)
                template.setColor(ThemeData.MARKED_INACTIVE_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_FILE_SELECTED)
                template.setColor(ThemeData.FILE_INACTIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_FOLDER_SELECTED)
                template.setColor(ThemeData.FOLDER_INACTIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_ARCHIVE_SELECTED)
                template.setColor(ThemeData.ARCHIVE_INACTIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_SYMLINK_SELECTED)
                template.setColor(ThemeData.SYMLINK_INACTIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_HIDDEN_SELECTED)
                template.setColor(ThemeData.HIDDEN_FILE_INACTIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_MARKED_SELECTED)
                template.setColor(ThemeData.MARKED_INACTIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // File table border color.
        else if(qName.equals(ELEMENT_BORDER)) {
            if(state == STATE_TABLE)
                template.setColor(ThemeData.FILE_TABLE_BORDER_COLOR, createColor(attributes));

            else if(state == STATE_STATUS_BAR)
                template.setColor(ThemeData.STATUS_BAR_BORDER_COLOR, createColor(attributes));

            else
                traceIllegalDeclaration(qName);
        }

        // File table inactive border color.
        else if(qName.equals(ELEMENT_INACTIVE_BORDER)) {
            if(state == STATE_TABLE)
                template.setColor(ThemeData.FILE_TABLE_INACTIVE_BORDER_COLOR, createColor(attributes));

            else
                traceIllegalDeclaration(qName);
        }

        // File table outline color.
        else if(qName.equals(ELEMENT_OUTLINE)) {
            if(state == STATE_TABLE)
                template.setColor(ThemeData.FILE_TABLE_SELECTED_OUTLINE_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // File table inactive outline color.
        else if(qName.equals(ELEMENT_INACTIVE_OUTLINE)) {
            if(state == STATE_TABLE)
                template.setColor(ThemeData.FILE_TABLE_INACTIVE_SELECTED_OUTLINE_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // Unmatched file table.
        else if(qName.equals(ELEMENT_UNMATCHED)) {
            if(state != STATE_TABLE)
                traceIllegalDeclaration(qName);
            state = STATE_TABLE_UNMATCHED;
        }

        // Background color.
        else if(qName.equals(ELEMENT_BACKGROUND)) {
            if(state == STATE_TABLE_NORMAL)
                template.setColor(ThemeData.FILE_TABLE_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_TABLE_SELECTED)
                template.setColor(ThemeData.FILE_TABLE_SELECTED_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_TABLE_ALTERNATE)
                template.setColor(ThemeData.FILE_TABLE_ALTERNATE_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_TABLE_UNMATCHED)
                template.setColor(ThemeData.FILE_TABLE_UNMATCHED_BACKGROUND_COLOR, createColor(attributes));

            else if(state == STATE_SHELL_NORMAL)
                template.setColor(ThemeData.SHELL_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_SHELL_SELECTED)
                template.setColor(ThemeData.SHELL_SELECTED_BACKGROUND_COLOR, createColor(attributes));

            else if(state == STATE_EDITOR_NORMAL)
                template.setColor(ThemeData.EDITOR_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_EDITOR_SELECTED)
                template.setColor(ThemeData.EDITOR_SELECTED_BACKGROUND_COLOR, createColor(attributes));

            else if(state == STATE_LOCATION_BAR_NORMAL)
                template.setColor(ThemeData.LOCATION_BAR_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_LOCATION_BAR_SELECTED)
                template.setColor(ThemeData.LOCATION_BAR_SELECTED_BACKGROUND_COLOR, createColor(attributes));

            else if(state == STATE_SHELL_HISTORY_NORMAL)
                template.setColor(ThemeData.SHELL_HISTORY_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_SHELL_HISTORY_SELECTED)
                template.setColor(ThemeData.SHELL_HISTORY_SELECTED_BACKGROUND_COLOR, createColor(attributes));

            else if(state == STATE_STATUS_BAR)
                template.setColor(ThemeData.STATUS_BAR_BACKGROUND_COLOR, createColor(attributes));
            
            else if(state == STATE_QUICK_LIST_HEADER)
            	template.setColor(ThemeData.QUICK_LIST_HEADER_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_QUICK_LIST_ITEM_NORMAL)
            	template.setColor(ThemeData.QUICK_LIST_ITEM_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_QUICK_LIST_ITEM_SELECTED)
            	template.setColor(ThemeData.QUICK_LIST_SELECTED_ITEM_BACKGROUND_COLOR, createColor(attributes));

            else
                traceIllegalDeclaration(qName);
        }

        // Progress bar color.
        else if(qName.equals(ELEMENT_PROGRESS)) {
            if(state == STATE_LOCATION_BAR)
                template.setColor(ThemeData.LOCATION_BAR_PROGRESS_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // 'OK' color.
        else if(qName.equals(ELEMENT_OK)) {
            if(state == STATE_STATUS_BAR)
                template.setColor(ThemeData.STATUS_BAR_OK_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // 'WARNING' color.
        else if(qName.equals(ELEMENT_WARNING)) {
            if(state == STATE_STATUS_BAR)
                template.setColor(ThemeData.STATUS_BAR_WARNING_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // 'CRITICAL' color.
        else if(qName.equals(ELEMENT_CRITICAL)) {
            if(state == STATE_STATUS_BAR)
                template.setColor(ThemeData.STATUS_BAR_CRITICAL_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // Text color.
        else if(qName.equals(ELEMENT_FOREGROUND)) {
            if(state == STATE_HIDDEN_NORMAL)
                template.setColor(ThemeData.HIDDEN_FILE_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_HIDDEN_SELECTED)
                template.setColor(ThemeData.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if(state == STATE_TABLE_UNMATCHED)
                template.setColor(ThemeData.FILE_TABLE_UNMATCHED_FOREGROUND_COLOR, createColor(attributes));

            else if(state == STATE_FOLDER_NORMAL)
                template.setColor(ThemeData.FOLDER_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_FOLDER_SELECTED)
                template.setColor(ThemeData.FOLDER_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if(state == STATE_ARCHIVE_NORMAL)
                template.setColor(ThemeData.ARCHIVE_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_ARCHIVE_SELECTED)
                template.setColor(ThemeData.ARCHIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if(state == STATE_SYMLINK_NORMAL)
                template.setColor(ThemeData.SYMLINK_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_SYMLINK_SELECTED)
                template.setColor(ThemeData.SYMLINK_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if(state == STATE_MARKED_NORMAL)
                template.setColor(ThemeData.MARKED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_MARKED_SELECTED)
                template.setColor(ThemeData.MARKED_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if(state == STATE_FILE_NORMAL)
                template.setColor(ThemeData.FILE_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_FILE_SELECTED)
                template.setColor(ThemeData.FILE_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if(state == STATE_SHELL_NORMAL)
                template.setColor(ThemeData.SHELL_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_SHELL_SELECTED)
                template.setColor(ThemeData.SHELL_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if(state == STATE_SHELL_HISTORY_NORMAL)
                template.setColor(ThemeData.SHELL_HISTORY_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_SHELL_HISTORY_SELECTED)
                template.setColor(ThemeData.SHELL_HISTORY_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if(state == STATE_EDITOR_NORMAL)
                template.setColor(ThemeData.EDITOR_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_EDITOR_SELECTED)
                template.setColor(ThemeData.EDITOR_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if(state == STATE_LOCATION_BAR_NORMAL)
                template.setColor(ThemeData.LOCATION_BAR_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_LOCATION_BAR_SELECTED)
                template.setColor(ThemeData.LOCATION_BAR_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if(state == STATE_STATUS_BAR)
                template.setColor(ThemeData.STATUS_BAR_FOREGROUND_COLOR, createColor(attributes));
            
            else if(state == STATE_QUICK_LIST_HEADER)
            	template.setColor(ThemeData.QUICK_LIST_HEADER_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_QUICK_LIST_ITEM_NORMAL)
            	template.setColor(ThemeData.QUICK_LIST_ITEM_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_QUICK_LIST_ITEM_SELECTED)
            	template.setColor(ThemeData.QUICK_LIST_SELECTED_ITEM_FOREGROUND_COLOR, createColor(attributes));

            else
                traceIllegalDeclaration(qName);
        }

        else
            traceIllegalDeclaration(qName);
    }

    /**
     * Notifies the reader that the current element declaration is over.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // If we're in an unknown element....
        if(unknownElement != null) {
            // If it just closed, resume normal parsing.
            if(qName.equals(unknownElement))
                unknownElement = null;
            // Ignores all other tags.
            else
                return;
        }

        // XML root element.
        if(qName.equals(ELEMENT_ROOT))
            state = STATE_UNKNOWN;

        // File table declaration.
        else if(qName.equals(ELEMENT_TABLE))
            state = STATE_ROOT;

        else if(qName.equals(ELEMENT_ALTERNATE))
            state = STATE_TABLE;

        else if(qName.equals(ELEMENT_UNMATCHED))
            state = STATE_TABLE;

        else if(qName.equals(ELEMENT_HIDDEN))
            state = STATE_TABLE;

        else if(qName.equals(ELEMENT_FOLDER))
            state = STATE_TABLE;

        else if(qName.equals(ELEMENT_ARCHIVE))
            state = STATE_TABLE;

        else if(qName.equals(ELEMENT_SYMLINK))
            state = STATE_TABLE;

        else if(qName.equals(ELEMENT_MARKED))
            state = STATE_TABLE;

        else if(qName.equals(ELEMENT_FILE))
            state = STATE_TABLE;

        // Shell declaration.
        else if(qName.equals(ELEMENT_SHELL))
            state = STATE_ROOT;

        // Shell history declaration.
        else if(qName.equals(ELEMENT_SHELL_HISTORY))
            state = STATE_ROOT;

        // Editor declaration.
        else if(qName.equals(ELEMENT_EDITOR))
            state = STATE_ROOT;

        // Location bar declaration.
        else if(qName.equals(ELEMENT_LOCATION_BAR))
            state = STATE_ROOT;
        
        // Quick list declaration.
        else if(qName.equals(ELEMENT_QUICK_LIST))
            state = STATE_ROOT;

        // Volume label declaration
        else if(qName.equals(ELEMENT_STATUS_BAR))
            state = STATE_ROOT;
        
        // Header declaration.
        else if(qName.equals(ELEMENT_HEADER)) {
        	if(state == STATE_QUICK_LIST_HEADER)
                state = STATE_QUICK_LIST;
        }
        
        // Item declaration.
        else if(qName.equals(ELEMENT_ITEM)) {
        	if(state == STATE_QUICK_LIST_ITEM)
                state = STATE_QUICK_LIST;
        }

        // Normal element declaration.
        else if(qName.equals(ELEMENT_NORMAL)) {
            if(state == STATE_SHELL_NORMAL)
                state = STATE_SHELL;
            else if(state == STATE_SHELL_HISTORY_NORMAL)
                state = STATE_SHELL_HISTORY;
            else if(state == STATE_HIDDEN_NORMAL)
                state = STATE_HIDDEN;
            else if(state == STATE_FOLDER_NORMAL)
                state = STATE_FOLDER;
            else if(state == STATE_ARCHIVE_NORMAL)
                state = STATE_ARCHIVE;
            else if(state == STATE_SYMLINK_NORMAL)
                state = STATE_SYMLINK;
            else if(state == STATE_MARKED_NORMAL)
                state = STATE_MARKED;
            else if(state == STATE_FILE_NORMAL)
                state = STATE_FILE;
            else if(state == STATE_EDITOR_NORMAL)
                state = STATE_EDITOR;
            else if(state == STATE_LOCATION_BAR_NORMAL)
                state = STATE_LOCATION_BAR;
            else if(state == STATE_TABLE_NORMAL)
                state = STATE_TABLE;
            else if(state == STATE_QUICK_LIST_ITEM_NORMAL)
            	state = STATE_QUICK_LIST_ITEM;
        }

        // Selected element declaration.
        else if(qName.equals(ELEMENT_SELECTED)) {
            if(state == STATE_SHELL_SELECTED)
                state = STATE_SHELL;
            else if(state == STATE_SHELL_HISTORY_SELECTED)
                state = STATE_SHELL_HISTORY;
            else if(state == STATE_HIDDEN_SELECTED)
                state = STATE_HIDDEN;
            else if(state == STATE_FOLDER_SELECTED)
                state = STATE_FOLDER;
            else if(state == STATE_ARCHIVE_SELECTED)
                state = STATE_ARCHIVE;
            else if(state == STATE_SYMLINK_SELECTED)
                state = STATE_SYMLINK;
            else if(state == STATE_MARKED_SELECTED)
                state = STATE_MARKED;
            else if(state == STATE_FILE_SELECTED)
                state = STATE_FILE;
            else if(state == STATE_EDITOR_SELECTED)
                state = STATE_EDITOR;
            else if(state == STATE_LOCATION_BAR_SELECTED)
                state = STATE_LOCATION_BAR;
            else if(state == STATE_TABLE_SELECTED)
                state = STATE_TABLE;
            else if(state == STATE_QUICK_LIST_ITEM_SELECTED)
            	state = STATE_QUICK_LIST_ITEM;
        }
    }



    // - Helper methods ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Checks whether the specified font is available on the system.
     * @param  font name of the font to check for.
     * @return <code>true</code> if the font is available, <code>false</code> otherwise.
     */
    private static boolean isFontAvailable(String font)  {
	String[] availableFonts; // All available fonts.

	// Looks for the specified font.
	availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    for (String availableFont : availableFonts)
        if (availableFont.equalsIgnoreCase(font))
            return true;

	// Font doesn't exist on the system.
	return false;
    }

    /**
     * Creates a font from the specified XML attributes.
     * <p>
     * Ignored attributes will be set to their default values.
     * </p>
     * @param  attributes XML attributes describing the font to use.
     * @return            the resulting Font instance.
     */
    private static Font createFont(Attributes attributes) {
        String          buffer; // Buffer for attribute values.
        int             size;   // Font size.
        int             style;  // Font style.
        StringTokenizer parser; // Used to parse the font family.

        // Computes the font style.
        style = 0;
        if(((buffer = attributes.getValue(ATTRIBUTE_BOLD)) != null) && buffer.equals(VALUE_TRUE))
            style |= Font.BOLD;
        if(((buffer = attributes.getValue(ATTRIBUTE_ITALIC)) != null) && buffer.equals(VALUE_TRUE))
            style |= Font.ITALIC;

        // Computes the font size.
        if((buffer = attributes.getValue(ATTRIBUTE_SIZE)) == null) {
            LOGGER.debug("Missing font size attribute in theme, ignoring.");
            return null;
	    }
        size = Integer.parseInt(buffer);

            // Computes the font family.
            if((buffer = attributes.getValue(ATTRIBUTE_FAMILY)) == null) {
                LOGGER.debug("Missing font family attribute in theme, ignoring.");
                return null;
        }

        // Looks through the list of declared fonts to find one that is installed on the system.
        parser = new StringTokenizer(buffer, ",");
        while(parser.hasMoreTokens()) {
            buffer = parser.nextToken().trim();

            // Font was found, use it.
            if(isFontAvailable(buffer))
            return new Font(buffer, style, size);
        }

        // No font was found, instructs the ThemeManager to use the system default.
        LOGGER.debug("Requested font families are not installed on the system, using default.");
        return null;
    }

    /**
     * Creates a color from the specified XML attributes.
     * @param  attributes XML attributes describing the font to use.
     * @return            the resulting Color instance.
     */
    private static Color createColor(Attributes attributes) {
        String buffer;
        int    color;

        // Retrieves the color attribute's value.
        if((buffer = attributes.getValue(ATTRIBUTE_COLOR)) == null) {
            LOGGER.debug("Missing color attribute in theme, ignoring.");
            return null;
        }
        color = Integer.parseInt(buffer, 16);

        // Retrieves the transparency attribute's value..
        if((buffer = attributes.getValue(ATTRIBUTE_ALPHA)) == null)
            return new Color(color);
        return new Color(color | (Integer.parseInt(buffer, 16) << 24), true);
    }


    // - Error generation methods --------------------------------------------
    // -----------------------------------------------------------------------
    private void traceIllegalDeclaration(String element) {
        unknownElement = element;
        LOGGER.debug("Unexpected start of element " + element + ", ignoring.");
    }
}
