
package com.mucommander.file;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.io.*;

/**
 * This Hashtable maps file extensions to their mime type, e.g. txt -> 
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
     * Returns the MIME type of the given file (determined by the file extension), <code>text/plain</code>
     * if it is unknown (no or unknown extension), and <code>null</code> if it is a folder.
     */
    public static String getMimeType(AbstractFile file) {
        if(file.isFolder() && !(file instanceof ArchiveFile))
            return null;
        
        String name = file.getName();
        int pos = name.lastIndexOf('.');
        if(pos==-1)
            return "text.plain";

        return (String)mimeTypes.get(name.substring(pos+1, name.length()).toLowerCase());        
    }
    
}
