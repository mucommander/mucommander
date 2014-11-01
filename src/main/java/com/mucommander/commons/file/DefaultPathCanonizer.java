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


package com.mucommander.commons.file;

import java.util.Vector;

/**
 * @author Maxence Bernard
 */
public class DefaultPathCanonizer implements PathCanonizer {

    /** Path separator */
    private String separator;

    /** The string replacement for '~' path fragments, null for no tilde replacement */
    private String tildeReplacement;


    /**
     * Creates a new path canonizer using the specified path separator and no tilde replacement.
     *
     * @param separator the path separator that delimits path fragments
     */
    public DefaultPathCanonizer(String separator) {
        this(separator, null);
    }

    /**
     * Creates a new path canonizer using the specified path separator and tilde replacement string
     * (if not <code>null</code>).
     *
     * @param separator the path separator that delimits path fragments
     * @param tildeReplacement if not <code>null</code>, path fragments equal to '~' will be replaced by this string.
     */
    public DefaultPathCanonizer(String separator, String tildeReplacement) {
        this.separator = separator;
        this.tildeReplacement = tildeReplacement;
    }


    //////////////////////////////////
    // PathCanonizer implementation //
    //////////////////////////////////

    /**
     * Returns a canonical value of the given path, where '.' and '..' path fragments are factored out,
     * and '~' fragments replaced by the string specified in the constructor (if not <code>null</code>).
     *
     * @param path the path to canonize
     * @return the canonized path
     */
    public String canonize(String path) {
        // Todo: use PathTokenizer?

        if(!path.equals("/")) {
            int pos;	    // position of current path separator
            int pos2 = 0;	// position of next path separator
            int separatorLen = separator.length();
            String dir;		// Current directory
            String dirWS;	// Current directory without trailing separator
            Vector<String> pathV = new Vector<String>();	// Will contain directory hierachy
            while((pos=pos2)!=-1) {
                // Get the index of the next path separator occurrence
                pos2 = path.indexOf(separator, pos);

                if(pos2==-1) {	// Last dir (or empty string)
                    dir = path.substring(pos);
                    dirWS = dir;
                }
                else {
                    pos2 += separatorLen;
                    dir = path.substring(pos, pos2);		// Dir name includes trailing separator
                    dirWS = dir.substring(0, dir.length()-separatorLen);
                }

                // Discard '.' and empty directories
                if((dirWS.equals("") && pathV.size()>0) || dirWS.equals(".")) {
                    continue;
                }
                // Remove last directory
                else if(dirWS.equals("..")) {
                    if(pathV.size()>0)
                        pathV.removeElementAt(pathV.size()-1);
                    continue;
                }
                // Replace '~' by the provided replacement string, only if one was specified
                else if(tildeReplacement!=null && dirWS.equals("~")) {
                    path = path.substring(0, pos) + tildeReplacement + path.substring(pos+1);
                    // Will perform another pass at the same position
                    pos2 = pos;
                    continue;
                }

                // Add directory to the end of the list
                pathV.add(dir);
            }

            // Reconstruct path from directory list
            path = "";
            int nbDirs = pathV.size();
            for(int i=0; i<nbDirs; i++)
                path += pathV.elementAt(i);

            // We now have a path free of "." and ".." (and "~" if tilde replacement is enabled)  
        }

        return path;
    }
}
