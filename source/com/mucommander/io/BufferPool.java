package com.mucommander.io;

import com.mucommander.Debug;

import java.util.Vector;

/**
 * This class allows to share and reuse byte array buffers to avoid excessive memory allocation and garbage collection.
 * Methods that use byte buffers and that are called repeatedly will benefit from using this class. 
 *
 * <p>Usage:
 * <ul>
 * <li>Call {@link #getBuffer(int)} to retrieve a buffer instance of a specified size: if one already exists, it will be
 * returned, if not a new buffer will be created and returned.
 * <li>When finished with the buffer, call {@link #releaseBuffer(byte[])} to make it available for further calls
 * to {@link #getBuffer(int)}. After calling this method, the buffer instance must not be used anymore.
 * </ul>
 *
 * <p>Note: this class is thread safe and thus can safely be called by concurrent threads.
 *
 * @author Maxence Bernard
 * @see com.mucommander.file.AbstractFile#copyStream(java.io.InputStream, java.io.OutputStream)
 */
public class BufferPool {

    /** List of available buffer instances */
    private static Vector buffers = new Vector();

    /**
     * Returns a buffer of the specified size. This method first checks if a buffer instance of the specified size
     * exists. If one exists, it is returned. If not, a new instance is created and returned.
     *
     * <p>The returned buffer will not be further returned by this method until {@link #releaseBuffer(byte[])} has been
     * called with the same instance. 
     *
     * @param size length of the desired byte array
     * @return a byte array of the specified size
     */
    public static synchronized byte[] getBuffer(int size) {
        int nbBuffers = buffers.size();
        byte buffer[];
        // Looks for an existing buffer instance of the specified size
        for(int i=0; i<nbBuffers; i++) {
            buffer = (byte[])buffers.elementAt(i);
            if(buffer.length==size) {
                // Found one, remove it from vector and return it 
                buffers.removeElementAt(i);
//                if(Debug.ON) Debug.trace("Returning buffer "+buffer+", size="+size);
                return buffer;
            }
        }

        if(Debug.ON) Debug.trace("Creating new buffer, size="+size);

        // No existing buffer found with the same size, create a new one and return it
        return new byte[size];
    }


    /**
     * Makes the given buffer available to further calls to {@link #getBuffer(int)} with the same buffer size.
     * After calling this method, the given buffer instance <b>must not be used anymore</b>, otherwise it could get
     * corrupted if other threads use it.
     *
     * @param buffer the buffer instance to make available for further use
     */
    public static synchronized void releaseBuffer(byte buffer[]) {
        if(Debug.ON) Debug.trace("Adding buffer "+buffer+", size="+buffer.length);
        buffers.add(buffer);
    }
}
