
package com.mucommander.file;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.io.*;

/**
 * This Hashtable maps file extensions to their mime type.
 */
public class MimeTypes extends Hashtable {

    private final static MimeTypes mimeTypes = new MimeTypes();
    
	
    private MimeTypes() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/mime.types")));

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
