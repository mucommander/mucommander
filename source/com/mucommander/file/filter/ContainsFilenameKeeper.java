package com.mucommander.file.filter;

/**
 * Filters out filenames that don't contain a specified string. The case is ignored when comparing filenames against
 * the string.
 *
 * @author Maxence Bernard
 */
public class ContainsFilenameKeeper extends FilenameFilter {

    private String match;

    public ContainsFilenameKeeper(String match) {
        this.match = match.toLowerCase();
    }


    ///////////////////////////////////
    // FilenameFilter implementation //
    ///////////////////////////////////

    public boolean accept(String filename) {
        return filename.toLowerCase().indexOf(match)!=-1;
    }
}
