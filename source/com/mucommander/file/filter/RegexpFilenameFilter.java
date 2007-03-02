package com.mucommander.file.filter;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Regular expressions based filename filter.

 * @author Nicolas Rinaudo
 */
public class RegexpFilenameFilter extends FilenameFilter {

    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Pattern against which file names will be compared. */
    private Pattern pattern;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Creates a new regular expression based file name filter.
     * @param  regexp                 regular expression against which to match file names.
     * @param  caseSensitive          whether the regular expression is case sensitive or not.
     * @throws PatternSyntaxException if the syntax of the regular expression is not correct.
     */
    public RegexpFilenameFilter(String regexp, boolean caseSensitive) throws PatternSyntaxException {
        if(caseSensitive)
            pattern = Pattern.compile(regexp);
        else
            pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
    }


    // - File name filter methods ----------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns <code>true</code> if the specified file name matches the filter's regular expression.
     * @param  fileName file name to match against the filter's regular expression.
     * @return          <code>true</code> if the specified file name matches the filter's regular expression, <code>false</code> otherwise.
     */
    public boolean accept(String fileName) {return pattern.matcher(fileName).matches();}



    // - Misc. -----------------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the regular expression used by this filter.
     * @return the regular expression used by this filter.
     */
    public String getRegularExpression() {return pattern.pattern();}
    public boolean isCaseSensitive() {return (pattern.flags() & Pattern.CASE_INSENSITIVE) == 0;}
}
