package com.mucommander.commons.io;

public enum FileTransferError {
    /** Reason not known / not specified */
    UNKNOWN,
    /** Source and destination files are identical */
    SOURCE_AND_DESTINATION_IDENTICAL,
    /** Source file could not be opened for read */
    OPENING_SOURCE,
    /** Destination file could not be opened for write */
    OPENING_DESTINATION,
    /** An error occurred while reading the source file */
    READING_SOURCE,
    /** An error occurred while writing the destination file */
    WRITING_DESTINATION,
    /** An error occurred while deleting the source file (used when moving a file) */
    DELETING_SOURCE,
    /** Source file could not be closed */
    CLOSING_SOURCE,
    /** Destination file could not be closed */
    CLOSING_DESTINATION,
    /** Destination file exists */
    DESTINATION_EXISTS,
    /** File not found (does not exist) */
    FILE_NOT_FOUND,
    /** Source file is a parent of the destination file */
    SOURCE_PARENT_OF_DESTINATION,
    /** An error occurred while reading the destination file */
    READING_DESTINATION,
    /** The checksum of the source and destination files don't match */
    CHECKSUM_MISMATCH;
}
