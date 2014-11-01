/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.commons.file.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * This class allows to break a path into filename tokens. The default separator characters are '/' and '\', but any
 * other separator characters can be specified. The {@link #hasMoreFilenames()} and {@link #nextFilename()} methods
 * can be used to iterate through all filename tokens. The {@link #getLastSeparator()} returns the last separator
 * string that appeared before the last filename token returned by {@link #nextFilename()}. Initially, this method
 * returns any leading separators the path may contain.
 * The {@link #getCurrentPath()} returns the current part of the path that has been tokenized.
 *
 * <p>To illustrate the use of PathTokenizer, the following piece of code iterates through all filename tokens and
 * reconstructs the original path :
 * <code>
 * PathTokenizer pt = new PathTokenizer(path);
 * String reconstructedPath = pt.getLastSeparator();
 * while(pt.hasMoreFilenames()) {
 *   String nextToken = pt.nextFilename();
 *   String lastSeparator = pt.getLastSeparator();
 *   reconstructedPath += nextToken+lastSeparator;
 * }
 * </code>
 *
 * <p>Note that PathTokenizer does not enforce valid file paths, that means a path with an incorrect syntax can still
 * be tokenized. In particular:
 * <ul>
 * <li>A path can contain mixed separators, e.g. C:\temp/file
 * <li>Filename tokens can be separated by multiple separator characters, e.g. /usr//local, {@link #getLastSeparator()}
 * will return the complete separator string, in this case "//" for the 'usr' filename token.
 * </ul>
 *
 * @author Maxence Bernard
 */
public class PathTokenizer implements Enumeration<String> {

    /** Separator characters */
    private String separators;

    /** True if this PathTokenizer tokenizes the path in reverse order, from right to left. */
    private boolean reverseOrder;

    /** Path tokens: separators and filenames. */
    private String[] tokens;
    /** Current index in the token array. */
    private int currentIndex;
    /** Path part that has been tokenized. */
    private StringBuffer currentPath;
    /** Last separators token. */ 
    private String lastSeparator;

    /** Default separator characters. */
    public final static String DEFAULT_SEPARATORS = "/\\";


    /**
     * Creates a new PathTokenizer using the given path and default separator characters ({@link #DEFAULT_SEPARATORS}},
     * in forward order, tokenizing the path from left to right.
     *
     * @param path the path to break into tokens
     */
    public PathTokenizer(String path) {
        this(path, DEFAULT_SEPARATORS, false);
    }


    /**
     * Creates a new PathTokenizer using the given path and separator character(s).
     *
     * @param path the path to break into tokens
     * @param separators the character(s) that separate tokens
     * @param reverseOrder if true, the path will be tokenized in reverse order, from right to left 
     */
    public PathTokenizer(String path, String separators, boolean reverseOrder) {
        this.separators = separators;
        this.reverseOrder = reverseOrder;

        // Split the path into tokens
        StringTokenizer st = new StringTokenizer(path, separators, true);
        Vector<String> tokensV = new Vector<String>();
        while(st.hasMoreTokens()) {
            tokensV.add(st.nextToken());
        }

        // Convert Vector into array
        tokens = new String[tokensV.size()];
        int nbTokens = tokens.length;

        if(reverseOrder) {
            for(int i=0; i<nbTokens; i++)
                tokens[i] = tokensV.elementAt(nbTokens-i-1);
        }
        else {
            tokensV.toArray(tokens);
        }

        // Initialize current path
        if(reverseOrder)
            currentPath = new StringBuffer(path);
        else
            currentPath = new StringBuffer(path.length());

        // Skip leading separator
        skipSeparators();
    }


    /**
     * Skips separator tokens and advances the internal token index, until either a filename token has been found
     * or the end of the path has been reached.
     */
    private void skipSeparators() {
        lastSeparator = "";

        String token;
        while(currentIndex<tokens.length && separators.indexOf(token=tokens[currentIndex])!=-1) {
            // Update last separator
            lastSeparator += token;

            // Update current path
            handleToken(token);

            currentIndex++;
        }
    }


    /**
     * Returns <code>true</code> if this PathTokenizer has more filename tokens.
     * @return <code>true</code> if this PathTokenizer has more filename tokens, <code>false</code> otherwise.
     */
    public boolean hasMoreFilenames() {
        return currentIndex<tokens.length;
    }

    private void handleToken(String token) {
        if(reverseOrder)
            currentPath.setLength(currentPath.length()-token.length());
        else
            currentPath.append(token);
    }

    /**
     * Returns the next filename token from this PathTokenizer. Throws a NoSuchElementException if no more filename
     * tokens are available. After calling this method, the values returned by {@link #getLastSeparator()} and
     * {@link #getCurrentPath()} will be updated.
     *
     * @return the next filename token from this PathTokenizer
     * @throws NoSuchElementException if no more tokens are available
     */
    public String nextFilename() throws NoSuchElementException {
        if(currentIndex<tokens.length) {
            String token = tokens[currentIndex++];

            // Update current path
            handleToken(token);

            // Skip separators after the filename 
            skipSeparators();

            return token;
        }
        else
            throw new NoSuchElementException();
    }


    /**
     * Returns the current path part that has been tokenized, i.e. that ends with the last filename token returned by
     * {@link #nextFilename()} and separator string returned by {@link #getLastSeparator()}.<br>
     * If this PathTokenizer operates in reverse order, the returned path is the path part that has not yet been 
     * tokenized.
     * @return the current path part that has been tokenized.
     */
    public String getCurrentPath() {
        return currentPath.toString();
    }


    /**
     * Returns the last separator string that appeared in the path after the last filename token returned by
     * {@link #nextFilename()} and before the next filename, or an empty string "" if there isn't any separator
     * character after the filename (path ends without a trailing separator).<br>
     * Note: the returned string can be made of several consecutive separator characters.
     *
     * <p>Initially, before any calls to {@link #nextFilename()} have been made, this method will return any leading
     * separator string in the path string, or an empty string if the path doesn't start with a separator.
     * @return the last separator string that appeared in the path.
     */
    public String getLastSeparator() {
        return lastSeparator;
    }


    ////////////////////////////////
    // Enumeration implementation //
    ////////////////////////////////

    /**
     * Enumeration implementation, returns the same value as {@link #hasMoreFilenames()}.
     */
    public boolean hasMoreElements() {
        return hasMoreFilenames();
    }

    /**
     * Enumeration implementation, returns the same value as {@link #nextFilename()}.
     */
    public String nextElement() throws NoSuchElementException {
        return nextFilename();
    }
}
