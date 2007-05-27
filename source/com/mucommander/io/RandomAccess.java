package com.mucommander.io;

import java.io.IOException;

/**
 * RandomAccess provides a common interface to random access streams, whether they be input or output streams.
 *
 * @author Maxence Bernard
 */
public interface RandomAccess {

    /**
     * Closes the random access file stream and releases any system resources associated with the stream.
     * A closed random access file cannot perform input or output operations and cannot be reopened.
     *
     * @throws IOException if an I/O error occurs
     */
    public abstract void close() throws IOException;

    /**
     * Returns the offset from the beginning of the file, in bytes, at which the next read or write occurs.
     *
     * @throws IOException if an I/O error occurs.
     */
    public abstract long getOffset() throws IOException;

    /**
     * Returns the length of the file, mesured in bytes.
     *
     * @throws IOException if an I/O error occurs
     */
    public abstract long getLength() throws IOException;

    /**
     * Sets the offset, measured from the beginning of the file, at which the next read or write occurs.
     * The offset may be set beyond the end of the file. Setting the offset beyond the end of the file does not change
     * the file length. The file length will change only by writing after the offset has been set beyond the end of the
     * file.
     *
     * @param offset the new offset position, measured in bytes from the beginning of the file
     * @throws IOException if an I/O error occurs
     */
    public abstract void seek(long offset) throws IOException;

}
