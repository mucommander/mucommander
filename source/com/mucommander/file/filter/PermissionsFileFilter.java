package com.mucommander.file.filter;

import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;

/**
 * Filter on a file's permissions.
 * <p>
 * This {@link com.mucommander.file.filter.FileFilter} is used to match files against a given set of permissions.
 * </p>
 * <p>
 * Note that the <i>executable</i> flag of a file is not checked on versions of Java older than 1.6.
 * </p>
 * @author Nicolas Rinaudo
 */
public class PermissionsFileFilter extends FileFilter {
    // - Class fields -----------------------------------------------------------
    // --------------------------------------------------------------------------
    /** The associated property must be set to <i>false</i>. */
    public static final int NO         = 0;
    /** The associated property must be set to <i>true</i>. */
    public static final int YES        = 1;
    /** The associated property's value is irrelevant. */
    public static final int UNFILTERED = 2;



    // - Instance fields --------------------------------------------------------
    // --------------------------------------------------------------------------
    /** File's read mask. */
    private int read;
    /** File's write mask. */
    private int write;
    /** File's execute mask. */
    private int execute;



    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Builds a new permission file filter.
     * @param read    filter's <i>read</i> mask.
     * @param write   filter's <i>write</i> mask.
     * @param execute filter's <i>execute</i> mask.
     */
    public PermissionsFileFilter(int read, int write, int execute) {
        this.read    = read;
        this.write   = write;
        this.execute = execute;
    }



    // - Filter access ----------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Returns the filter's <i>read</i> mask.
     * <p>
     * The returned value can be any of:
     * <ul>
     *  <li>{@link #YES}: matched files must be readable.</li>
     *  <li>{@link #NO}: matched files must not be readable.</li>
     *  <li>{@link #UNFILTERED}: the file's <i>read</i> flag is not checked.</li>
     * </ul>
     * </p>
     * @see #getWriteFilter()
     * @see #getExecuteFilter()
     * @return the filter's <i>read</i> mask.
     */
    public int getReadFilter() {return read;}

    /**
     * Returns the filter's <i>write</i> mask.
     * <p>
     * The returned value can be any of:
     * <ul>
     *  <li>{@link #YES}: matched files must be writable.</li>
     *  <li>{@link #NO}: matched files must not be writable.</li>
     *  <li>{@link #UNFILTERED}: the file's <i>write</i> flag is not checked.</li>
     * </ul>
     * </p>
     * @see #getReadFilter()
     * @see #getExecuteFilter()
     * @return the filter's <i>read</i> mask.
     */
    public int getWriteFilter() {return write;}

    /**
     * Returns the filter's <i>execute</i> mask.
     * <p>
     * The returned value can be any of:
     * <ul>
     *  <li>{@link #YES}: matched files must be executable.</li>
     *  <li>{@link #NO}: matched files must not be executable.</li>
     *  <li>{@link #UNFILTERED}: the file's <i>execute</i> flag is not checked.</li>
     * </ul>
     * </p>
     * @see #getReadFilter()
     * @see #getWriteFilter()
     * @return the filter's <i>read</i> mask.
     */
    public int getExecuteFilter() {return execute;}


    // - File filter methods ----------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Checks whether the specified file matches this filter.
     * @param file file whose permissions should be checked.
     * @return <code>true</code> if <code>file</code> matches this filter, <code>false</code> otherwise.
     */
    public boolean accept(AbstractFile file) {
        // Filters on the file's read permissions.
        if(read == NO && file.canRead())
            return false;
        if(read == YES && !file.canRead())
            return false;

        // Filters on the file's write permissions.
        if(write == NO && file.canWrite())
            return false;
        if(write == YES && !file.canWrite())
            return false;

        // Execute permissions are only relevant in Java 1.6 and over.
        if(PlatformManager.JAVA_VERSION >= PlatformManager.JAVA_1_6) {
            if(execute == NO && file.canExecute())
                return false;
            if(execute == YES && !file.canExecute())
                return false;
        }
        return true;
    }
}
