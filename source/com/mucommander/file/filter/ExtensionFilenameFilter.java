package com.mucommander.file.filter;

/**
 * This filename filter only accepts filenames ending with an extension specified at creation time of this filter.
 * The case is ignored when testing filenames: all case variations of an extension will be accepted by {@link #accept(String)}.
 *
 * <p>The extension(s) may be any string, but when used in the traditional sense of a file extension (e.g. zip extension)
 * the '.' character must be included in the specified extension (e.g. ".zip" must be used, not "zip").
 * 
 * @author Maxence Bernard
 */
public class ExtensionFilenameFilter extends FilenameFilter {

    private String extensions[];

    public ExtensionFilenameFilter(String extensions[]) {
        this.extensions = extensions;

        // Convert extensions to lower-case
        int nbExtensions = extensions.length;
        for(int i=0; i<nbExtensions; i++)
            extensions[i] = extensions[i].toLowerCase();
    }

    public ExtensionFilenameFilter(String extension) {
        this(new String[]{extension});
    }


    public boolean accept(String filename) {
        // Convert filename to lower-case, as extensions already are lower-cased.
        String filenameLC = filename.toLowerCase();
        int nbExtensions = extensions.length;

        for(int i=0; i<nbExtensions; i++)
            if(filenameLC.endsWith(extensions[i]))
                return true;

        return false;
    }
}
