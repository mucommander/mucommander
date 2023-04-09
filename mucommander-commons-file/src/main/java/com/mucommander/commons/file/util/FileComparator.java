/**
 * This file is part of muCommander, http://www.mucommander.com
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

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.mucommander.commons.file.AbstractFile;


/**
 * FileComparator compares {@link AbstractFile} instances using a comparison criterion order (ascending or descending).
 * Directories can either be mixed with regular files (compared just as regular files), or always precede regular files.

 * <p>FileComparator extends Comparator and thus can be used wherever a Comparator is accepted. In particular, it
 * can be used with <code>java.util.Arrays</code> sort methods to easily sort an array of files.
 *
 * <p>The following criteria are available:
 * <ul>
 * <li>{@link CRITERION#NAME}: compares filenames returned by {@link AbstractFile#getName()}
 * <li>{@link CRITERION#SIZE}: compares file sizes returned by {@link AbstractFile#getSize()}. Note: size for
 * directories is always considered as 0, even if {@link AbstractFile#getSize()} returns something else. 
 * <li>{@link CRITERION#DATE}: compares file dates returned by {@link AbstractFile#getDate()}
 * <li>{@link CRITERION#EXTENSION}: compares file extensions returned by {@link AbstractFile#getExtension()}
 * <li>{@link CRITERION#PERMISSIONS}: compares file permissions returned by {@link AbstractFile#getPermissions()}
 * <li>{@link CRITERION#OWNER}: compares file owners returned by {@link AbstractFile#getOwner()}
 * <li>{@link CRITERION#GROUP}: compares file groups returned by {@link AbstractFile#getGroup()}
 * </ul>
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class FileComparator implements Comparator<AbstractFile> {

    /** Comparison criterion */
    private CRITERION criterion;
    /** Ascending or descending order ? */
    private boolean ascending;
    /** Specifies whether directories should precede files or be handled as regular files */
    private boolean directoriesFirst;
    /** Returns the value for the 'name' column for a file */
    private Function<AbstractFile, String> nameFunc;
    /** Locale that is used to sort by filenames */
    private Locale locale;

    public enum CRITERION {
        /** Criterion for filename comparison. */
        NAME,
        /** Criterion for file size comparison. */
        SIZE,
        /** Criterion for file date comparison. */
        DATE,
        /** Criterion for file extension comparison. */
        EXTENSION,
        /** Criterion for file permissions comparison. */
        PERMISSIONS,
        /** Criterion for owner comparison. */
        OWNER,
        /** Criterion for group comparison. */
        GROUP
    }

    /** Matches filenames that contain a number, like "01 - Do the Joy.mp3" */
    private final static Pattern FILENAME_WITH_NUMBER_PATTERN = Pattern.compile("\\d+");

    /**
     * Creates a new FileComparator using the specified comparison criterion, order (ascending or descending) and
     * directory handling rule.
     *
     * @param criterion comparison criterion, see constant fields
     * @param ascending if true, ascending order will be used, descending order otherwise
     * @param directoriesFirst specifies whether directories should precede files or be handled as regular files
     * @param nameFunc function that returns the value for the 'name' column for a file
     * @param locale the local by which filenames are sorted
     */
    public FileComparator(CRITERION criterion, boolean ascending, boolean directoriesFirst, Function<AbstractFile, String> nameFunc, Locale locale) {
        this.criterion = criterion;
        this.ascending = ascending;
        this.directoriesFirst = directoriesFirst;
        this.nameFunc = nameFunc;
        this.locale = locale;
    }


    /**
     * Returns a <code>value</code> for the given character. Using this function in a comparator will separator
     * symbols for digits and letters and put in the following order:
     * <ul>
     *   <li>symbols first</li>
     *   <li>digits second</li>
     *   <li>letters third</li>
     * </ul>
     *
     * <p>This character order was suggested in ticket #282.</p> 
     *
     * @param c character for which to return a value
     * @return a <code>value</code> for the given character
     */
    private int getCharacterValue(int c) {
        // Note: max char value is 65535
        if(Character.isLetter(c))
            c += 131070;    // yields a value higher than any other symbol or digit
        else if(Character.isDigit(c))
            c += 65535;     // yields a value higher than any other symbol

        // else we have a symbol
        return c;
    }

    /**
     * Removes leading zeros ('0') from the given string (if it contains any), and returns the trimmed string.
     *
     * @param s the string from which to remove leading zeros
     * @return a string without leading zeros
     */
    private String removeLeadingZeros(String s) {
        int len = s.length();
        int i=0;
        while(i<len && s.charAt(i)=='0')
            i++;

        if(i>0)
            return s.substring(i, len);

        return s;
    }

    /**
     * Compare the specified files by their names, following the contract of {@link Comparator#compare(Object, Object)}.
     *
     * @param file1 first file to compare
     * @param file2 second file to compare.
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
     * than the second.
     */
    private int compareByFilename(AbstractFile file1, AbstractFile file2) {
        String s1 = nameFunc.apply(file1);
        String s2 = nameFunc.apply(file2);

        // Special treatment for strings that contain a number, so they are ordered by the number's value, e.g.:
        // 1 < 1a < 2 < 10, like Mac OS X Finder and Windows Explorer do.
        //
        // This special order applies only if both strings contain a number and have the same prefix. Otherwise, the general order applies.
        Matcher m1 = FILENAME_WITH_NUMBER_PATTERN.matcher(s1);
        if(m1.find()) {
            Matcher m2 = FILENAME_WITH_NUMBER_PATTERN.matcher(s2);
            if(m2.find()) {
                // So we got two filenames that both contain a number, check if they have the same prefix
                int start1 = m1.start();
                int start2 = m2.start();

                // Note: compare prefixes only if start indexes match, faster that way
                if(start1==start2 && (start1==0 || s1.substring(0, start1).equals(s2.substring(0, start2)))) {
                    String g1 = removeLeadingZeros(m1.group());
                    String g2 = removeLeadingZeros(m2.group());

                    int g1Len = g1.length();
                    int g2Len = g2.length();

                    if(g1Len!=g2Len)
                        return g1Len - g2Len;

                    int c1, c2;
                    for (int i=0; i<g1Len && i<g2Len; i++) {
                        c1 = g1.charAt(i);
                        c2 = g2.charAt(i);
                        if(c1 != c2)
                            return c1 - c2;
                    }
                }
            }
        }

        var collator = Collator.getInstance(locale);
        collator.setStrength(Collator.TERTIARY);
        int diff = collator.compare(s1, s2);
        if (diff == 0) {
            // This should never happen unless the current filesystem allows a directory to have
            // several files with different case variations of the same name.
            // AFAIK, no OS/filesystem allows this, but just to be safe.

            // Case-sensitive name comparison
            collator.setStrength(Collator.PRIMARY);
            diff = collator.compare(s1, s2);
        }

        return diff;
    }


    ///////////////////////////////
    // Comparator implementation //
    ///////////////////////////////
    
    public int compare(AbstractFile f1, AbstractFile f2) {
        if (directoriesFirst) {
            boolean is1Directory = f1.isDirectory();
            boolean is2Directory = f2.isDirectory();

            if (is1Directory && !is2Directory)
                return -1;	// ascending has no effect on the result (a directory is always first) so let's return

            if (is2Directory && !is1Directory)
                return 1;	// ascending has no effect on the result (a directory is always first) so let's return

            // At this point, either both files are directories or none of them are
        }

        long diff = 0;
        switch(criterion) {
        case SIZE:
            // Consider that directories have a size of 0
            long fileSize1 = f1.isDirectory() ? 0 : f1.getSize();
            long fileSize2 = f2.isDirectory() ? 0 : f2.getSize();

            // Returns file1 size - file2 size, file size of -1 (unavailable) is considered as enormous (max long value)
            diff = (fileSize1==-1?Long.MAX_VALUE:fileSize1)-(fileSize2==-1?Long.MAX_VALUE:fileSize2);
            break;
        case DATE:
            diff = f1.getDate()-f2.getDate();
            break;
        case PERMISSIONS:
            diff = f1.getPermissions().getIntValue() - f2.getPermissions().getIntValue();
            break;
        case EXTENSION:
            diff = StringUtils.compareIgnoreCase(f1.getExtension(), f2.getExtension());
            break;
        case OWNER:
            diff = StringUtils.compareIgnoreCase(f1.getOwner(), f2.getOwner());
            break;
        case GROUP:
            diff = StringUtils.compareIgnoreCase(f1.getGroup(), f2.getGroup());
            break;
        case NAME:
        default:
        }

        if (diff == 0)	// If both files have the same criterion's value, compare names
            diff = compareByFilename(f1, f2);

        // Cast long value to int, without overflowing the int if the long value exceeds the min or max int value
        int intValue;
        
        if(diff>Integer.MAX_VALUE)
            intValue = Integer.MAX_VALUE;   // 2147483647
        else if(diff<Integer.MIN_VALUE+1)   // Need that +1 so that the int is not overflowed if ascending order is enabled (i.e. int is negated)
            intValue = Integer.MIN_VALUE+1; // 2147483647
        else
            intValue = (int)diff;

        return ascending?intValue:-intValue; // Note: ascending is used more often, more efficient to negate for descending
    }


    /**
     * Returns true only if the given object is a FileComparator using the same criterion and ascending/descending order.
     */
    public boolean equals(Object o) {
        if(! (o instanceof FileComparator))
            return false;

        FileComparator fc = (FileComparator)o;
        return criterion ==fc.criterion && ascending==fc.ascending;
    }
}
