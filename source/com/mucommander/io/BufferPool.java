/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.io;

import com.mucommander.Debug;

import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * This class allows to share and reuse byte buffers to avoid excessive memory allocation and garbage collection.
 * Methods that use byte buffers and that are called repeatedly will benefit from using this class.
 *
 * <p>This class works with two types of byte buffers indifferently:
 * <ul>
 *  <li>Byte array buffers (<code>byte[]</code>)</li>
 *  <li><code>java.nio.ByteBuffer</code></li>
 * </ul>
 *
 * <p>
 * Usage of this class is similar to malloc/free:
 * <ul>
 *  <li>Call <code>#get*Buffer(int)</code> to retrieve a buffer instance of a specified size</li>
 *  <li>Use the buffer</li>
 *  <li>When finished using the buffer, call <code>#release*Buffer(byte[])</code> to make this buffer available for
 * subsequent calls to <code>#get*Buffer(int)</code>. Failing to call this method will prevent the buffer from being
 * used again and from being garbage-collected.</li>
 * </ul>
 * </p>
 *
 * <p>Note: this class is thread safe and thus can safely be used by concurrent threads.</p>
 *
 * @author Maxence Bernard
 * @see com.mucommander.io.StreamUtils
 */
public class BufferPool {

    /** List of BufferContainer instances that wraps available buffers */
    private static Vector bufferContainers = new Vector();

    /** The default buffer size when not specified */
    public final static int DEFAULT_BUFFER_SIZE = 65536;


    /**
     * Convenience method that has the same effect as calling {@link #getArrayBuffer(int)} with
     * {@link #DEFAULT_BUFFER_SIZE}.
     *
     * @return a byte array with a length of DEFAULT_BUFFER_SIZE
     */
    public static synchronized byte[] getArrayBuffer() {
        return getArrayBuffer(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Returns a byte array of the specified size. This method first checks if a byte array of the specified size
     * exists in the pool. If one is found, it is removed from the pool and returned. If not, a new instance is created
     * and returned.
     *
     * <p>This method won't return the same buffer instance until it has been released with
     * {@link #releaseArrayBuffer(byte[])}.</p>
     *
     * <p>This method is a shorthand for {@link #getBuffer(int, com.mucommander.io.BufferPool.BufferFactory)} called
     * with a {@link com.mucommander.io.BufferPool.ArrayBufferFactory} instance.</p>.
     *
     * @param size size of the byte array
     * @return a byte array of the specified size
     */
    public static synchronized byte[] getArrayBuffer(int size) {
        return (byte[])getBuffer(size, new ArrayBufferFactory());
    }

    /**
     * Convenience method that has the same effect as calling {@link #getByteBuffer(int)} with
     * {@link #DEFAULT_BUFFER_SIZE}.
     *
     * @return a ByteBuffer with a capacity of DEFAULT_BUFFER_SIZE bytes
     */
    public static synchronized ByteBuffer getByteBuffer() {
        return getByteBuffer(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Returns a ByteBuffer of the specified capacity. This method first checks if a ByteBuffer instance of the
     * specified capacity exists in the pool. If one is found, it is removed from the pool and returned. If not,
     * a new instance is created and returned.
     *
     * <p>This method won't return the same buffer instance until it has been released with
     * {@link #releaseByteBuffer(ByteBuffer)}.</p>
     *
     * <p>This method is a shorthand for {@link #getBuffer(int, com.mucommander.io.BufferPool.BufferFactory)} called
     * with a {@link com.mucommander.io.BufferPool.ArrayBufferFactory} instance.</p>.

     * @param capacity capacity of the ByteBuffer
     * @return a ByteBuffer with the specified capacity
     */
    public static synchronized ByteBuffer getByteBuffer(int capacity) {
        return (ByteBuffer)getBuffer(capacity, new ByteBufferFactory());
    }

    /**
     * Returns a byte array of the specified size. This method first checks if a buffer the same size as the specified
     * one and a class compatible with the specified factory exists in the pool. If one is found, it is removed from the
     * pool and returned.
     * If not, a new instance is created and returned using {@link BufferFactory#newBuffer(int)}.
     *
     * <p>This method won't return the same buffer instance until it has been released with
     * {@link #releaseBuffer(Object, BufferFactory)}.</p>
     *
     * @param size size of the buffer
     * @param factory BufferFactory used to identify the target buffer class and create a new buffer (if necessary)
     * @return a buffer of the specified size
     */
    public static synchronized Object getBuffer(int size, BufferFactory factory) {
        int nbBuffers = bufferContainers.size();
        BufferContainer bufferContainer;
        Object buffer;

        // Looks for a buffer container in the pool that matches the specified size and buffer class.
        for(int i=0; i<nbBuffers; i++) {
            bufferContainer = (BufferContainer) bufferContainers.elementAt(i);
            buffer = bufferContainer.getBuffer();

            if(bufferContainer.getSize()==size && (factory.matchesBufferClass(buffer.getClass()))) {
                bufferContainers.removeElementAt(i);
//                if(Debug.ON) Debug.trace("Returning buffer "+buffer+", size="+size);
                return buffer;
            }
        }

        if(Debug.ON) Debug.trace("Creating new buffer with "+factory+" size="+size, 3);

        // No buffer with the same class and size found in the pool, create a new one and return it
        return factory.newBuffer(size);
    }


    /**
     * Makes the given buffer available for further calls to {@link #getArrayBuffer(int)} with the same buffer size.
     * Does nothing if the specified buffer already is in the pool. After calling this method, the given buffer
     * instance <b>must not be used</b>, otherwise it could get corrupted if some other threads use it.
     *
     * @param buffer the buffer instance to make available for further use
     * @throws IllegalArgumentException if specified buffer is null
     */
    public static synchronized void releaseArrayBuffer(byte buffer[]) {
        releaseBuffer(buffer, new ArrayBufferFactory());
    }

    /**
     * Makes the given buffer available for further calls to {@link #getByteBuffer(int)} with the same buffer size.
     * Does nothing if the specified buffer already is in the pool. After calling this method, the given buffer
     * instance <b>must not be used</b>, otherwise it could get corrupted if some other threads use it.
     *
     * @param buffer the buffer instance to make available for further use
     * @throws IllegalArgumentException if specified buffer is null
     */
    public static synchronized void releaseByteBuffer(ByteBuffer buffer) {
        releaseBuffer(buffer, new ByteBufferFactory());
    }

    /**
     * Makes the given buffer available for further calls to {@link #getBuffer(int, BufferFactory)} with the same buffer
     * size and factory. Does nothing if the specified buffer already is in the pool.
     * After calling this method, the given buffer instance <b>must not be used</b>, otherwise it could get
     * corrupted if some other threads use it.
     *
     * @param buffer the buffer instance to make available for further use
     * @param factory BufferFactory used create a buffer container
     * @throws IllegalArgumentException if specified buffer is null
     */
    public static synchronized void releaseBuffer(Object buffer, BufferFactory factory) {
        if(buffer==null)
            throw new IllegalArgumentException("specified buffer is null");

        BufferContainer bufferContainer = factory.newBufferContainer(buffer);

        if(bufferContainers.contains(bufferContainer)) {
            if(Debug.ON) Debug.trace("Warning: specified buffer is already in the pool: "+buffer, -1);
            return;
        }

//        if(Debug.ON) Debug.trace("Adding buffer to pool: "+buffer);

        bufferContainers.add(bufferContainer);
    }

    /**
     * Returns the number of buffers that currently are in the pool. This method is provided only for debugging
     * purposes only.
     *
     * @return the number of buffers currently in the pool
     */
    static int getBufferCount() {
        return bufferContainers.size();
    }

    /**
     * Returns the number of buffers that currently are in the pool and whose Class are the same as the provided
     * factory's. This method is provided only for debugging purposes only.
     *
     * @param factory the BufferFactory
     * @return the number of buffers currently in the pool
     */
    static int getBufferCount(BufferFactory factory) {
        int count = 0;        
        int nbBuffers = bufferContainers.size();
        for(int i=0; i<nbBuffers; i++) {
            if(factory.matchesBufferClass(((BufferContainer)bufferContainers.elementAt(i)).getBuffer().getClass())) {
                count ++;
            }
        }

        return count;
    }


    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * Wraps a buffer instance and provides information about the wrapped buffer.
     */
    public static abstract class BufferContainer {

        /** The wrapped buffer instance */
        protected Object buffer;

        /**
         * Creates a new BufferContainer that wraps the given buffer.
         *
         * @param buffer the buffer instance to wrap
         */
        protected BufferContainer(Object buffer) {
            this.buffer = buffer;
        }

        /**
         * Returns the wrapped buffer instance.
         *
         * @return the wrapped buffer instance
         */
        protected Object getBuffer() {
            return buffer;
        }

        /**
         * Implements a shallow equal comparison.
         */
        public boolean equals(Object o) {
            // Note: this method is used by Vector.contains()
            return (o instanceof BufferContainer) && buffer == ((BufferContainer)o).buffer;
        }

        /**
         * Returns the size of the wrapped buffer instance.
         *
         * @return the size of the wrapped buffer instance
         */
        protected abstract int getSize();
    }

    /**
     * A BufferFactory is responsible for creating buffer and {@link BufferContainer} instances, and for returning the buffer
     * Class. The Class returned by {@link #getBufferClass()} may be a superclass or superinterface of the actual
     * objects returned by {@link #newBuffer(int)}.
     */
    public static abstract class BufferFactory {

        /**
         * Returns <code>true</code> if the class returned by {@link #getBufferClass()} is equal or a
         * superclass/superinterface of the specified buffer class.
         *
         * @param bufferClass the buffer Class to test
         * @return true if the class returned by <code>#getBufferClass()</code> is equal or a superclass/superinterface
         * of the specified buffer class
         */
        protected boolean matchesBufferClass(Class bufferClass) {
            return getBufferClass().isAssignableFrom(bufferClass);
        }

        /**
         * Creates and returns a buffer instance of the specified size.
         *
         * @param size size of the buffer to create
         * @return a buffer instance of the specified size
         */
        protected abstract Object newBuffer(int size);

        /**
         * Creates and returns a {@link BufferContainer} for the specified buffer instance.
         *
         * @param buffer the buffer to wrap in a BufferContainer
         * @return returns a BufferContainer for the specified buffer instance
         */
        protected abstract BufferContainer newBufferContainer(Object buffer);

        /**
         * Returns the Class of buffer instances this factory creates. 
         *
         * @return the Class of buffer instances this factory creates
         */
        protected abstract Class getBufferClass();
    }

    /**
     * This class is a {@link BufferFactory} implementation for byte array (<code>byte[]</code>) buffers.
     */
    public static class ArrayBufferFactory extends BufferFactory {
        protected Object newBuffer(int size) {
            return new byte[size];
        }

        protected BufferContainer newBufferContainer(Object buffer) {
            return new BufferContainer(buffer) {
                protected int getSize() {
                    return ((byte[])buffer).length;
                }
            };
        }

        protected Class getBufferClass() {
            return byte[].class;
        }
    }

    /**
     * This class is a {@link BufferFactory} implementation for <code>java.nio.ByteBuffer</code> buffers. The ByteBuffer
     * instances created by {@link #newBuffer(int)} are direct ; the actually Class of those instances may be actually
     * be <code>java.nio.DirectByteBuffer</code> and not <code>java.nio.ByteBuffer</code> as returned by
     * {@link #getBufferClass()}.
     */
    public static class ByteBufferFactory extends BufferFactory {
        protected Object newBuffer(int size) {
            // Note: the returned instance is actually a java.nio.DirectByteBuffer, this is why it's important to
            // compare classes using Class#isAssignableFrom(Class)
            return ByteBuffer.allocateDirect(size);
        }

        protected BufferContainer newBufferContainer(Object buffer) {
            return new BufferContainer(buffer) {
                protected int getSize() {
                    return ((ByteBuffer)buffer).capacity();
                }
            };
        }

        protected Class getBufferClass() {
            return ByteBuffer.class;
        }
    }
}
