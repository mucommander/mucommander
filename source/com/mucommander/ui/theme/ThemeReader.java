/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.ui.theme;

import com.mucommander.Debug;
import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;

import java.awt.*;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * Loads theme instances from properly formatted XML files.
 * @author Nicolas Rinaudo
 */
class ThemeReader implements ContentHandler, ThemeXmlConstants {
    // - XML parser states ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Parsing hasn't started yet. */
    private static final int STATE_UNKNOWN                = 0;
    /** Parsing the root element. */
    private static final int STATE_ROOT                   = 1;
    /** Parsing the table element.*/
    private static final int STATE_TABLE                  = 2;
    /** Parsing the shell element. */
    private static final int STATE_SHELL                  = 3;
    /** Parsing the editor element. */
    private static final int STATE_EDITOR                 = 4;
    /** Parsing the location bar element. */
    private static final int STATE_LOCATION_BAR           = 5;
    /** Parsing the shell.normal element. */
    private static final int STATE_SHELL_NORMAL           = 6;
    /** Parsing the shell.selected element. */
    private static final int STATE_SHELL_SELECTED         = 7;
    /** Parsing the editor.normal element. */
    private static final int STATE_EDITOR_NORMAL          = 8;
    /** Parsing the location bar.normal element. */
    private static final int STATE_LOCATION_BAR_NORMAL    = 9;
    /** Parsing the editor.selected element. */
    private static final int STATE_EDITOR_SELECTED        = 10;
    /** Parsing the location bar.selected element. */
    private static final int STATE_LOCATION_BAR_SELECTED  = 11;
    /** Parsing the shell_history element. */
    private static final int STATE_SHELL_HISTORY          = 12;
    /** Parsing the shell_history.normal element. */
    private static final int STATE_SHELL_HISTORY_NORMAL   = 13;
    /** Parsing the shell_history.selected element. */
    private static final int STATE_SHELL_HISTORY_SELECTED = 14;
    /** Parsing the volume_label element. */
    private static final int STATE_STATUS_BAR             = 15;
    private static final int STATE_HIDDEN                 = 16;
    private static final int STATE_HIDDEN_NORMAL          = 17;
    private static final int STATE_HIDDEN_SELECTED        = 18;
    private static final int STATE_FOLDER                 = 19;
    private static final int STATE_FOLDER_NORMAL          = 20;
    private static final int STATE_FOLDER_SELECTED        = 21;
    private static final int STATE_ARCHIVE                = 22;
    private static final int STATE_ARCHIVE_NORMAL         = 23;
    private static final int STATE_ARCHIVE_SELECTED       = 24;
    private static final int STATE_SYMLINK                = 25;
    private static final int STATE_SYMLINK_NORMAL         = 26;
    private static final int STATE_SYMLINK_SELECTED       = 27;
    private static final int STATE_MARKED                 = 28;
    private static final int STATE_MARKED_NORMAL          = 29;
    private static final int STATE_MARKED_SELECTED        = 30;
    private static final int STATE_FILE                   = 31;
    private static final int STATE_FILE_NORMAL            = 32;
    private static final int STATE_FILE_SELECTED          = 33;



    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Theme template that is currently being built. */
    private ThemeData template;
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
    public static void read(InputStream in, ThemeData template) throws Exception {new Parser().parse(in, new ThemeReader(template), "UTF-8");}


    // - XML interaction -----------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Notifies the reader that a new XML element is starting.
     */
    public void startElement(String uri, String name, Hashtable attributes, Hashtable attURIs) throws Exception {
        // Ignores the content of unknown elements.
        if(unknownElement != null) {
            if(Debug.ON) Debug.trace("Ignoring element " + name);
            return;
        }

        // XML root element.
        if(name.equals(ELEMENT_ROOT)) {
            if(state != STATE_UNKNOWN)
                traceIllegalDeclaration(ELEMENT_ROOT);
            state = STATE_ROOT;
        }

        // File table declaration.
        else if(name.equals(ELEMENT_TABLE)) {
            if(state != STATE_ROOT)
                traceIllegalDeclaration(name);
            state = STATE_TABLE;
        }

        // Shell declaration.
        else if(name.equals(ELEMENT_SHELL)) {
            if(state != STATE_ROOT)
                traceIllegalDeclaration(name);
            state = STATE_SHELL;
        }

        // Editor declaration.
        else if(name.equals(ELEMENT_EDITOR)) {
            if(state != STATE_ROOT)
                traceIllegalDeclaration(name);
            state = STATE_EDITOR;
        }

        // Location bar declaration.
        else if(name.equals(ELEMENT_LOCATION_BAR)) {
            if(state != STATE_ROOT)
                traceIllegalDeclaration(name);
            state = STATE_LOCATION_BAR;
        }

        // Shell history declaration.
        else if(name.equals(ELEMENT_SHELL_HISTORY)) {
            if(state != STATE_ROOT)
                traceIllegalDeclaration(name);
            state = STATE_SHELL_HISTORY;
        }

        // Volume label declaration.
        else if(name.equals(ELEMENT_STATUS_BAR)) {
            if(state != STATE_ROOT)
                traceIllegalDeclaration(name);
            state = STATE_STATUS_BAR;
        }

        else if(name.equals(ELEMENT_HIDDEN)) {
            if(state != STATE_TABLE)
                traceIllegalDeclaration(name);
            state = STATE_HIDDEN;
        }

        else if(name.equals(ELEMENT_FOLDER)) {
            if(state != STATE_TABLE)
                traceIllegalDeclaration(name);
            state = STATE_FOLDER;
        }

        else if(name.equals(ELEMENT_ARCHIVE)) {
            if(state != STATE_TABLE)
                traceIllegalDeclaration(name);
            state = STATE_ARCHIVE;
        }

        else if(name.equals(ELEMENT_SYMLINK)) {
            if(state != STATE_TABLE)
                traceIllegalDeclaration(name);
            state = STATE_SYMLINK;
        }

        else if(name.equals(ELEMENT_MARKED)) {
            if(state != STATE_TABLE)
                traceIllegalDeclaration(name);
            state = STATE_MARKED;
        }

        else if(name.equals(ELEMENT_FILE)) {
            if(state != STATE_TABLE)
                traceIllegalDeclaration(name);
            state = STATE_FILE;
        }

        // Normal element declaration.
        else if(name.equals(ELEMENT_NORMAL)) {
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
            else
                traceIllegalDeclaration(name);
        }

        // Selected element declaration.
        else if(name.equals(ELEMENT_SELECTED)) {
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
            else
                traceIllegalDeclaration(name);
        }

        // Font creation.
        else if(name.equals(ELEMENT_FONT)) {
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
            else
                traceIllegalDeclaration(name);
        }

        // Unfocused background color.
        else if(name.equals(ELEMENT_UNFOCUSED_BACKGROUND)) {
            if(state == STATE_FILE_NORMAL)
                template.setColor(ThemeData.FILE_UNFOCUSED_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_TABLE)
                template.setColor(ThemeData.FILE_TABLE_UNFOCUSED_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_FOLDER_NORMAL)
                template.setColor(ThemeData.FOLDER_UNFOCUSED_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_ARCHIVE_NORMAL)
                template.setColor(ThemeData.ARCHIVE_UNFOCUSED_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_SYMLINK_NORMAL)
                template.setColor(ThemeData.SYMLINK_UNFOCUSED_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_HIDDEN_NORMAL)
                template.setColor(ThemeData.HIDDEN_FILE_UNFOCUSED_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_MARKED_NORMAL)
                template.setColor(ThemeData.MARKED_UNFOCUSED_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_FILE_SELECTED)
                template.setColor(ThemeData.FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_FOLDER_SELECTED)
                template.setColor(ThemeData.FOLDER_SELECTED_UNFOCUSED_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_ARCHIVE_SELECTED)
                template.setColor(ThemeData.ARCHIVE_SELECTED_UNFOCUSED_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_SYMLINK_SELECTED)
                template.setColor(ThemeData.SYMLINK_SELECTED_UNFOCUSED_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_HIDDEN_SELECTED)
                template.setColor(ThemeData.HIDDEN_FILE_SELECTED_UNFOCUSED_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_MARKED_SELECTED)
                template.setColor(ThemeData.MARKED_SELECTED_UNFOCUSED_BACKGROUND_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(name);
        }

        // Unfocused foreground color.
        else if(name.equals(ELEMENT_UNFOCUSED_FOREGROUND)) {
            if(state == STATE_FILE_NORMAL)
                template.setColor(ThemeData.FILE_UNFOCUSED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_FOLDER_NORMAL)
                template.setColor(ThemeData.FOLDER_UNFOCUSED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_ARCHIVE_NORMAL)
                template.setColor(ThemeData.ARCHIVE_UNFOCUSED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_SYMLINK_NORMAL)
                template.setColor(ThemeData.SYMLINK_UNFOCUSED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_HIDDEN_NORMAL)
                template.setColor(ThemeData.HIDDEN_FILE_UNFOCUSED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_MARKED_NORMAL)
                template.setColor(ThemeData.MARKED_UNFOCUSED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_FILE_SELECTED)
                template.setColor(ThemeData.FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_FOLDER_SELECTED)
                template.setColor(ThemeData.FOLDER_SELECTED_UNFOCUSED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_ARCHIVE_SELECTED)
                template.setColor(ThemeData.ARCHIVE_SELECTED_UNFOCUSED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_SYMLINK_SELECTED)
                template.setColor(ThemeData.SYMLINK_SELECTED_UNFOCUSED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_HIDDEN_SELECTED)
                template.setColor(ThemeData.HIDDEN_FILE_SELECTED_UNFOCUSED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_MARKED_SELECTED)
                template.setColor(ThemeData.MARKED_SELECTED_UNFOCUSED_FOREGROUND_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(name);
        }

        // File table border color.
        else if(name.equals(ELEMENT_BORDER)) {
            if(state == STATE_TABLE)
                template.setColor(ThemeData.FILE_TABLE_BORDER_COLOR, createColor(attributes));

            else if(state == STATE_STATUS_BAR)
                template.setColor(ThemeData.STATUS_BAR_BORDER_COLOR, createColor(attributes));

            else
                traceIllegalDeclaration(name);
        }

        // Background color.
        else if(name.equals(ELEMENT_BACKGROUND)) {
            if(state == STATE_HIDDEN_NORMAL)
                template.setColor(ThemeData.HIDDEN_FILE_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_HIDDEN_SELECTED)
                template.setColor(ThemeData.HIDDEN_FILE_SELECTED_BACKGROUND_COLOR, createColor(attributes));

            else if(state == STATE_FOLDER_NORMAL)
                template.setColor(ThemeData.FOLDER_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_FOLDER_SELECTED)
                template.setColor(ThemeData.FOLDER_SELECTED_BACKGROUND_COLOR, createColor(attributes));

            else if(state == STATE_ARCHIVE_NORMAL)
                template.setColor(ThemeData.ARCHIVE_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_ARCHIVE_SELECTED)
                template.setColor(ThemeData.ARCHIVE_SELECTED_BACKGROUND_COLOR, createColor(attributes));

            else if(state == STATE_SYMLINK_NORMAL)
                template.setColor(ThemeData.SYMLINK_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_SYMLINK_SELECTED)
                template.setColor(ThemeData.SYMLINK_SELECTED_BACKGROUND_COLOR, createColor(attributes));

            else if(state == STATE_MARKED_NORMAL)
                template.setColor(ThemeData.MARKED_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_MARKED_SELECTED)
                template.setColor(ThemeData.MARKED_SELECTED_BACKGROUND_COLOR, createColor(attributes));

            else if(state == STATE_FILE_NORMAL)
                template.setColor(ThemeData.FILE_BACKGROUND_COLOR, createColor(attributes));
            else if(state == STATE_FILE_SELECTED)
                template.setColor(ThemeData.FILE_SELECTED_BACKGROUND_COLOR, createColor(attributes));

            else if(state == STATE_TABLE)
                template.setColor(ThemeData.FILE_TABLE_BACKGROUND_COLOR, createColor(attributes));

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

            else
                traceIllegalDeclaration(name);
        }

        // Progress bar color.
        else if(name.equals(ELEMENT_PROGRESS)) {
            if(state == STATE_LOCATION_BAR)
                template.setColor(ThemeData.LOCATION_BAR_PROGRESS_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(name);
        }

        // 'OK' color.
        else if(name.equals(ELEMENT_OK)) {
            if(state == STATE_STATUS_BAR)
                template.setColor(ThemeData.STATUS_BAR_OK_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(name);
        }

        // 'WARNING' color.
        else if(name.equals(ELEMENT_WARNING)) {
            if(state == STATE_STATUS_BAR)
                template.setColor(ThemeData.STATUS_BAR_WARNING_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(name);
        }

        // 'CRITICAL' color.
        else if(name.equals(ELEMENT_CRITICAL)) {
            if(state == STATE_STATUS_BAR)
                template.setColor(ThemeData.STATUS_BAR_CRITICAL_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(name);
        }

        // Text color.
        else if(name.equals(ELEMENT_FOREGROUND)) {
            if(state == STATE_HIDDEN_NORMAL)
                template.setColor(ThemeData.HIDDEN_FILE_FOREGROUND_COLOR, createColor(attributes));
            else if(state == STATE_HIDDEN_SELECTED)
                template.setColor(ThemeData.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR, createColor(attributes));

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

            else
                traceIllegalDeclaration(name);
        }

        else
            traceIllegalDeclaration(name);
    }

    /**
     * Notifies the reader that the current element declaration is over.
     */
    public void endElement(String uri, String name) throws Exception {
        // If we're in an unknown element....
        if(unknownElement != null) {
            // If it just closed, resume normal parsing.
            if(name.equals(unknownElement))
                unknownElement = null;
            // Ignores all other tags.
            else
                return;
        }

        // XML root element.
        if(name.equals(ELEMENT_ROOT)) {
            if(state != STATE_ROOT)
                if(Debug.ON) traceIllegalClosing(name);
            state = STATE_UNKNOWN;
        }

        // File table declaration.
        else if(name.equals(ELEMENT_TABLE)) {
            if(state != STATE_TABLE)
                if(Debug.ON) traceIllegalClosing(name);
            state = STATE_ROOT;
        }

        else if(name.equals(ELEMENT_HIDDEN)) {
            if(state != STATE_HIDDEN)
                if(Debug.ON) traceIllegalClosing(name);
            state = STATE_TABLE;
        }

        else if(name.equals(ELEMENT_FOLDER)) {
            if(state != STATE_FOLDER)
                if(Debug.ON) traceIllegalClosing(name);
            state = STATE_TABLE;
        }

        else if(name.equals(ELEMENT_ARCHIVE)) {
            if(state != STATE_ARCHIVE)
                if(Debug.ON) traceIllegalClosing(name);
            state = STATE_TABLE;
        }

        else if(name.equals(ELEMENT_SYMLINK)) {
            if(state != STATE_SYMLINK)
                if(Debug.ON) traceIllegalClosing(name);
            state = STATE_TABLE;
        }

        else if(name.equals(ELEMENT_MARKED)) {
            if(state != STATE_MARKED)
                if(Debug.ON) traceIllegalClosing(name);
            state = STATE_TABLE;
        }

        else if(name.equals(ELEMENT_FILE)) {
            if(state != STATE_FILE)
                if(Debug.ON) traceIllegalClosing(name);
            state = STATE_TABLE;
        }

        // Shell declaration.
        else if(name.equals(ELEMENT_SHELL)) {
            if(state != STATE_SHELL)
                if(Debug.ON) traceIllegalClosing(name);
            state = STATE_ROOT;
        }

        // Shell history declaration.
        else if(name.equals(ELEMENT_SHELL_HISTORY)) {
            if(state != STATE_SHELL_HISTORY)
                if(Debug.ON) traceIllegalClosing(name);
            state = STATE_ROOT;
        }

        // Editor declaration.
        else if(name.equals(ELEMENT_EDITOR)) {
            if(state != STATE_EDITOR)
                if(Debug.ON) traceIllegalClosing(name);
            state = STATE_ROOT;
        }

        // Location bar declaration.
        else if(name.equals(ELEMENT_LOCATION_BAR)) {
            if(state != STATE_LOCATION_BAR)
                if(Debug.ON) traceIllegalClosing(name);
            state = STATE_ROOT;
        }

        // Volume label declaration
        else if(name.equals(ELEMENT_STATUS_BAR)) {
            if(state != STATE_STATUS_BAR)
                if(Debug.ON) traceIllegalClosing(name);
            state = STATE_ROOT;
        }

        // Normal element declaration.
        else if(name.equals(ELEMENT_NORMAL)) {
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
            else
                if(Debug.ON) traceIllegalClosing(name);
        }

        // Selected element declaration.
        else if(name.equals(ELEMENT_SELECTED)) {
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
            else
                if(Debug.ON) traceIllegalClosing(name);
        }
    }



    // - Unused XML methods --------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Not used.
     */
    public void startDocument() {}

    /**
     * Not used.
     */
    public void endDocument() {}

    /**
     * Not used.
     */
    public void characters(String s) {}



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
	for(int i = 0; i < availableFonts.length; i++)
	    if(availableFonts[i].equalsIgnoreCase(font))
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
    private static Font createFont(Hashtable attributes) {
        String          buffer; // Buffer for attribute values.
        int             size;   // Font size.
        int             style;  // Font style.
	StringTokenizer parser; // Used to parse the font family.
	Font            font;   // Generated font.

        // Computes the font style.
        style = 0;
        if(((buffer = (String)attributes.get(ATTRIBUTE_BOLD)) != null) && buffer.equals(VALUE_TRUE))
            style |= Font.BOLD;
        if(((buffer = (String)attributes.get(ATTRIBUTE_ITALIC)) != null) && buffer.equals(VALUE_TRUE))
            style |= Font.ITALIC;

        // Computes the font size.
        if((buffer = (String)attributes.get(ATTRIBUTE_SIZE)) == null) {
            if(Debug.ON) Debug.trace("Missing font size attribute in theme, ignoring.");
            return null;
	}
	size = Integer.parseInt(buffer);

        // Computes the font family.
        if((buffer = (String)attributes.get(ATTRIBUTE_FAMILY)) == null) {
            if(Debug.ON) Debug.trace("Missing font family attribute in theme, ignoring.");
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
	if(Debug.ON) Debug.trace("Requested font families are not installed on the system, using default.");
        return null;
    }

    /**
     * Creates a color from the specified XML attributes.
     * @param  attributes XML attributes describing the font to use.
     * @return            the resulting Color instance.
     * @throws Exception  thrown if the {@link #ATTRIBUTE_COLOR} attribute is not set.
     */
    private static Color createColor(Hashtable attributes) throws Exception {
        String buffer;
        int    color;

        // Retrieves the color attribute's value.
        if((buffer = (String)attributes.get(ATTRIBUTE_COLOR)) == null) {
            if(Debug.ON) Debug.trace("Missing color attribute in theme, ignoring.");
            return null;
        }
        color = Integer.parseInt(buffer, 16);

        // Retrieves the transparency attribute's value..
        if((buffer = (String)attributes.get(ATTRIBUTE_ALPHA)) == null)
            return new Color(color);
        return new Color(color | (Integer.parseInt(buffer, 16) << 24), true);
    }


    // - Error generation methods --------------------------------------------
    // -----------------------------------------------------------------------
    private void traceIllegalDeclaration(String element) {
        unknownElement = element;
        if(Debug.ON)
            Debug.trace("Unexpected start of element " + element + ", ignoring.");
    }

    private static void traceIllegalClosing(String element) {if(Debug.ON) Debug.trace("Unexpected end of element " + element + ", ignoring.");}
}
