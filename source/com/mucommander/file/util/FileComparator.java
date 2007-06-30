/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.file.util;

import com.mucommander.file.AbstractFile;

import java.util.Comparator;


/**
 * FileComparator compares {@link AbstractFile} instances using a comparison criterion order (ascending or descending).
 * Directories can either be mixed with regular files (compared just as regular files), or always precede regular files.

 * <p>FileComparator extends Comparator and thus can be used wherever a Comparator is accepted. In particular, it
 * can be used with <code>java.util.Arrays</code> sort methods to easily sort an array of files.
 *
 * <p>The following criteria are available:
 * <ul>
 * <li>{@link #NAME_CRITERION}: compares filenames returned by {@link AbstractFile#getName()}
 * <li>{@link #SIZE_CRITERION}: compares file sizes returned by {@link AbstractFile#getSize()}. Note: size for
 * directories is always considered as 0, even if {@link AbstractFile#getSize()} returns something else. 
 * <li>{@link #DATE_CRITERION}: compares file dates returned by {@link AbstractFile#getDate()}
 * <li>{@link #EXTENSION_CRITERION}: compares file extensions returned by {@link AbstractFile#getExtension()}
 * <li>{@link #PERMISSIONS_CRITERION}: compares file permissions returned by {@link AbstractFile#getPermissions()}
 * </ul>
 *
 * @author Maxence Bernard
 */
public class FileComparator implements Comparator {

    /** Comparison criterion */
    private int criterion;
    /** Ascending or descending order ? */
    private boolean ascending;
    /** Specifies whether directories should precede files or be handled as regular files */
    private boolean directoriesFirst;

    /** Criterion for filename comparison */
    public final static int NAME_CRITERION = 0;
    /** Criterion for file size comparison */
    public final static int SIZE_CRITERION = 1;
    /** Criterion for file date comparison */
    public final static int DATE_CRITERION = 2;
    /** Criterion for file extension comparison */
    public final static int EXTENSION_CRITERION = 3;
    /** Criterion for file permissions comparison */
    public final static int PERMISSIONS_CRITERION = 4;


    /**
     * Creates a new FileComparator using the specified comparison criterion, order (ascending or descending) and
s     * directory handling rule.
     *
     * @param criterion comparison criterion, see constant fields
     * @param ascending if true, ascending order will be used, descending order otherwise
     * @param directoriesFirst specifies whether directories should precede files or be handled as regular files
     */
    public FileComparator(int criterion, boolean ascending, boolean directoriesFirst) {
        this.criterion = criterion;
        this.ascending = ascending;
        this.directoriesFirst = directoriesFirst;
    }


    ///////////////////////////////
    // Comparator implementation //
    ///////////////////////////////
    
    public int compare(Object o1, Object o2) {
        long diff;

        AbstractFile f1 = (AbstractFile)o1;
        AbstractFile f2 = (AbstractFile)o2;

        boolean is1Directory = f1.isDirectory();
        boolean is2Directory = f2.isDirectory();

        if(directoriesFirst) {
            if(is1Directory && !is2Directory)
                return 1;	// ascending has no effect on the result (a directory is always first) so let's return
            else if(is2Directory && !is1Directory)
                return -1;	// ascending has no effect on the result (a directory is always first) so let's return

            // At this point, either both files are directories or none of them are
        }

        if (criterion == SIZE_CRITERION)  {
            long fileSize1 = is1Directory?0:f1.getSize();   // consider directory size as always 0
            long fileSize2 = is2Directory?0:f2.getSize();

            // Returns file1 size - file2 size, file size of -1 (unavailable) is considered as enormous (max long value)
            diff = (fileSize1==-1?Long.MAX_VALUE:fileSize1)-(fileSize2==-1?Long.MAX_VALUE:fileSize2);

            if(diff==0)	// Same size, compare names
                diff = f2.getName().compareToIgnoreCase(f1.getName());
        }
        else if (criterion == DATE_CRITERION) {
            diff = f1.getDate()-f2.getDate();

            if(diff==0)	// Same date, compare names
                diff = f2.getName().compareToIgnoreCase(f1.getName());
        }
        else if (criterion == PERMISSIONS_CRITERION) {
            diff = f1.getPermissions() - f2.getPermissions();

            if(diff==0)	// Same permissions, compare names
                diff = f2.getName().compareToIgnoreCase(f1.getName());
        }
        else if (criterion == EXTENSION_CRITERION) {
            String ext1 = f1.getExtension();
            String ext2 = f2.getExtension();

            if(ext1==null && ext2!=null)	// ext1 is null, ext2 isn't
                diff = 1;
            else if(ext1!=null && ext2==null)	// ext2 is null, ext1 isn't
                diff = -1;
            // At this point, either both extensions are null, or none of them are
            else {
                if (ext1==null) {		// Both extensions are null, compare names
                    diff = f2.getName().compareToIgnoreCase(f1.getName());
                }
                else {			// Both extensions are not null
                    diff = ext2.compareToIgnoreCase(ext1);

                    if(diff==0)	// Same extensions, compare names
                        diff = f2.getName().compareToIgnoreCase(f1.getName());
                }
            }
        }
        else {      // criterion == NAME_CRITERION
            diff = f2.getName().compareToIgnoreCase(f1.getName());
            if(diff==0) {
                // This should never happen unless the current filesystem allows a directory to have
                // several files with different case variations of the same name.
                // AFAIK, no OS/filesystem allows this, but just to be safe.

                // Case-sensitive name comparison
                diff = f2.getName().compareTo(f1.getName());
            }
        }

        // Cast long value to int, without overflowing the int if the long value exceeds the min or max int value
        int intValue;
        
        if(diff>Integer.MAX_VALUE)
            intValue = Integer.MAX_VALUE;   // 2147483647
        else if(diff<Integer.MIN_VALUE+1)   // Need that +1 so that the int is not overflowed if ascending order is enabled (i.e. int is negated)
            intValue = Integer.MIN_VALUE+1; // 2147483647
        else
            intValue = (int)diff;

        return ascending?-intValue:intValue;
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
