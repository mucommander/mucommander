/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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


package com.mucommander.file;

import com.mucommander.file.util.ResourceLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * This Hashtable maps file extensions to their mime type.
 *
 * @author Maxence Bernard
 */
public class MimeTypes extends Hashtable {

    private final static MimeTypes mimeTypes = new MimeTypes();
    
	
    private MimeTypes() {
        BufferedReader br;

        br = null;
        try {
            br = new BufferedReader(new InputStreamReader(ResourceLoader.getResourceAsStream("/mime.types")));

            String line;
            StringTokenizer st;
            String description;

            while((line=br.readLine())!=null) {
                try {
                    st = new StringTokenizer(line);
                    description = st.nextToken();

                    while(st.hasMoreTokens())
                        put(st.nextToken(), description);
                }
                catch(Exception e) {
                    // If a line contains an error, catch the exception and go to the next line
                }
            }
        }
        catch(IOException e) {}
        // Makes sure the stream is closed.
        // This might not be strictly necessary as streams on internal resources are a bit of an unknown,
        // but since the ClassLoader.getResourceAsStream documentation doesn't explicitly say that such
        // streams do not need closing, it's safer to assume they do.
        finally {
            if(br != null) {
                try {br.close();}
                catch(IOException e) {}
            }
        }
    }

	
    /**
     * Returns the MIME type of the given file (determined by the file extension), <code>null</code>
     * if the type is unknown (unknown or no extension) or if the file is a folder.
     */
    public static String getMimeType(AbstractFile file) {
        if(file.isDirectory())
            return null;
        
        String name = file.getName();
        int pos = name.lastIndexOf('.');
        if(pos==-1)
            return null;

        return (String)mimeTypes.get(name.substring(pos+1, name.length()).toLowerCase());        
    }
    
}
