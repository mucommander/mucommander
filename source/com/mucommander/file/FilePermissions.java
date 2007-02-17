package com.mucommander.file;

/**
 * Contains file permissions and access types.
 *
 * @author Maxence Bernard
 */
public interface FilePermissions {

    /** Bit mask for 'execute' permission */
    public final static int EXECUTE_PERMISSION = 1;
    /** Bit mask for 'write' permission */
    public final static int WRITE_PERMISSION = 2;
    /** Bit mask for 'read' permission */
    public final static int READ_PERMISSION = 4;

    /** Bit mask for 'other' permissions */
    public final static int OTHER_ACCESS = 0;
    /** Bit mask for 'group' permissions */
    public final static int GROUP_ACCESS = 1;
    /** Bit mask for 'owner' permissions */
    public final static int USER_ACCESS = 2;
}
