/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.commons.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
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
 * </p>
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
 * @author Maxence Bernard, Nicolas Rinaudo
 * @see com.mucommander.commons.io.StreamUtils
 */
public class BufferPool {
    /** Logger used by this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(BufferPool.class);

    /** List of BufferContainer instances that wraps available buffers */
    private static Vector<BufferContainer> bufferContainers = new Vector<BufferContainer>();

    /** The initial default buffer size */
    public final static int INITIAL_DEFAULT_BUFFER_SIZE = 65536;

    /** Size of buffers returned by get*Buffer methods without a size argument */
    public static int defaultBufferSize = INITIAL_DEFAULT_BUFFER_SIZE;

    /** The initial max pool size */
    public final static long INITIAL_POOL_LIMIT = 10485760;

    /** Maximum combined size of all pooled buffers, in bytes */
    public static long maxPoolSize = INITIAL_POOL_LIMIT;

    /** Current combined size of all pooled buffers, in bytes */
    public static long poolSize;


    /**
     * Convenience method that has the same effect as calling {@link #getByteArray(int)} with
     * a length equal to {@link #getDefaultBufferSize()}.
     *
     * @return a byte array with a length of {@link #getDefaultBufferSize()}
     */
    public static synchronized byte[] getByteArray() {
        return getByteArray(getDefaultBufferSize());
    }

    /**
     * Returns a byte array of the specified length. This method first checks if a byte array of the specified length
     * exists in the pool. If one is found, it is removed from the pool and returned. If not, a new instance is created
     * and returned.
     *
     * <p>This method won't return the same buffer instance until it has been released with
     * {@link #releaseByteArray(byte[])}.</p>
     *
     * <p>This method is a shorthand for {@link #getBuffer(com.mucommander.commons.io.BufferPool.BufferFactory,int)} called
     * with a {@link com.mucommander.commons.io.BufferPool.ByteArrayFactory} instance.</p>.
     *
     * @param length length of the byte array
     * @return a byte array of the specified size
     */
    public static synchronized byte[] getByteArray(int length) {
        return (byte[])getBuffer(new ByteArrayFactory(), length);
    }

    /**
     * Convenience method that has the same effect as calling {@link #getCharArray(int)} with
     * a length equal to {@link #getDefaultBufferSize()}.
     *
     * @return a char array with a length of {@link #getDefaultBufferSize()}
     */
    public static synchronized char[] getCharArray() {
        return getCharArray(getDefaultBufferSize());
    }

    /**
     * Returns a char array of the specified length. This method first checks if a char array of the specified length
     * exists in the pool. If one is found, it is removed from the pool and returned. If not, a new instance is created
     * and returned.
     *
     * <p>This method won't return the same buffer instance until it has been released with
     * {@link #releaseCharArray(char[])}.</p>
     *
     * <p>This method is a shorthand for {@link #getBuffer(com.mucommander.commons.io.BufferPool.BufferFactory,int)} called
     * with a {@link com.mucommander.commons.io.BufferPool.CharArrayFactory} instance.</p>.
     *
     * @param length length of the char array
     * @return a char array of the specified length
     */
    public static synchronized char[] getCharArray(int length) {
        return (char[])getBuffer(new CharArrayFactory(), length);
    }

    /**
     * Convenience method that has the same effect as calling {@link #getByteBuffer(int)} with
     * a buffer capacity of {@link #getDefaultBufferSize()}.
     *
     * @return a ByteBuffer with a capacity equal to {@link #getDefaultBufferSize()}
     */
    public static synchronized ByteBuffer getByteBuffer() {
        return getByteBuffer(getDefaultBufferSize());
    }

    /**
     * Returns a ByteBuffer of the specified capacity. This method first checks if a ByteBuffer instance of the
     * specified capacity exists in the pool. If one is found, it is removed from the pool and returned. If not,
     * a new instance is created and returned.
     *
     * <p>This method won't return the same buffer instance until it has been released with
     * {@link #releaseByteBuffer(ByteBuffer)}.</p>
     *
     * <p>This method is a shorthand for {@link #getBuffer(com.mucommander.commons.io.BufferPool.BufferFactory,int)} called
     * with a {@link com.mucommander.commons.io.BufferPool.ByteBufferFactory} instance.</p>.

     * @param capacity capacity of the ByteBuffer
     * @return a ByteBuffer with the specified capacity
     */
    public static synchronized ByteBuffer getByteBuffer(int capacity) {
        return (ByteBuffer)getBuffer(new ByteBufferFactory(), capacity);
    }


    /**
     * Convenience method that has the same effect as calling {@link #getCharBuffer(int)} with
     * a buffer capacity of {@link #getDefaultBufferSize()}.
     *
     * @return a CharBuffer with a capacity equal to {@link #getDefaultBufferSize()}
     */
    public static synchronized CharBuffer getCharBuffer() {
        return getCharBuffer(getDefaultBufferSize());
    }

    /**
     * Returns a CharBuffer of the specified capacity. This method first checks if a CharBuffer instance of the
     * specified capacity exists in the pool. If one is found, it is removed from the pool and returned. If not,
     * a new instance is created and returned.
     *
     * <p>This method won't return the same buffer instance until it has been released with
     * {@link #releaseCharBuffer(CharBuffer)}.</p>
     *
     * <p>This method is a shorthand for {@link #getBuffer(com.mucommander.commons.io.BufferPool.BufferFactory,int)} called
     * with a {@link com.mucommander.commons.io.BufferPool.CharBufferFactory} instance.</p>.

     * @param capacity capacity of the CharBuffer
     * @return a CharBuffer with the specified capacity
     */
    public static synchronized CharBuffer getCharBuffer(int capacity) {
        return (CharBuffer)getBuffer(new CharBufferFactory(), capacity);
    }


    /**
     * Convenience method that has the same effect as calling {@link #getBuffer(com.mucommander.commons.io.BufferPool.BufferFactory, int)}
     * with a size equal to {@link #getDefaultBufferSize()}.
     *
     * @param factory BufferFactory used to identify the target buffer class and create a new buffer (if necessary)
     * @return a buffer with a size equal to {@link #getDefaultBufferSize()}
     */
    public static synchronized Object getBuffer(BufferFactory factory) {
        return getBuffer(factory, getDefaultBufferSize());
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
     * @param factory BufferFactory used to identify the target buffer class and create a new buffer (if necessary)
     * @param size size of the buffer
     * @return a buffer of the specified size
     */
    public static synchronized Object getBuffer(BufferFactory factory, int size) {
        int nbBuffers = bufferContainers.size();
        BufferContainer bufferContainer;
        Object buffer;

        // Looks for a buffer container in the pool that matches the specified size and buffer class.
        for(int i=0; i<nbBuffers; i++) {
            bufferContainer = bufferContainers.elementAt(i);
            buffer = bufferContainer.getBuffer();

            // Caution: mind the difference between BufferContainer#getLength() and BufferContainer#getSize()
            if(bufferContainer.getLength()==size && (factory.matchesBufferClass(buffer.getClass()))) {
                bufferContainers.removeElementAt(i);
                poolSize -= bufferContainer.getSize();
                return buffer;
            }
        }

        LOGGER.trace("Creating new buffer with {} size=", factory, size);

        // No buffer with the same class and size found in the pool, create a new one and return it
        return factory.newBuffer(size);
    }


    /**
     * Makes the given buffer available for further calls to {@link #getByteArray(int)} with the same buffer length.
     * Returns <code>true</code> if the buffer was added to the pool, <code>false</code> if the buffer was already in
     * the pool.
     *
     * <p>After calling this method, the given buffer instance <b>must not be used</b>, otherwise it could get
     * corrupted if other threads were using it.</p>
     *
     * @param buffer the buffer instance to make available for further use
     * @return <code>true</code> if the buffer was added to the pool, <code>false</code> if the buffer was already in the pool
     * @throws IllegalArgumentException if specified buffer is null
     */
    public static synchronized boolean releaseByteArray(byte buffer[]) {
        return releaseBuffer(buffer, new ByteArrayFactory());
    }

    /**
     * Makes the given buffer available for further calls to {@link #getCharArray(int)} with the same buffer length.
     * Returns <code>true</code> if the buffer was added to the pool, <code>false</code> if the buffer was already in
     * the pool.
     *
     * <p>After calling this method, the given buffer instance <b>must not be used</b>, otherwise it could get
     * corrupted if other threads were using it.</p>
     *
     * @param buffer the buffer instance to make available for further use
     * @return <code>true</code> if the buffer was added to the pool, <code>false</code> if the buffer was already in the pool
     * @throws IllegalArgumentException if specified buffer is null
     */
    public static synchronized boolean releaseCharArray(char buffer[]) {
        return releaseBuffer(buffer, new CharArrayFactory());
    }

    /**
     * Makes the given buffer available for further calls to {@link #getByteBuffer(int)} with the same buffer capacity.
     * Returns <code>true</code> if the buffer was added to the pool, <code>false</code> if the buffer was already in
     * the pool.
     *
     * <p>After calling this method, the given buffer instance <b>must not be used</b>, otherwise it could get
     * corrupted if other threads were using it.</p>
     *
     * @param buffer the buffer instance to make available for further use
     * @return <code>true</code> if the buffer was added to the pool, <code>false</code> if the buffer was already in the pool
     * @throws IllegalArgumentException if specified buffer is null
     */
    public static synchronized boolean releaseByteBuffer(ByteBuffer buffer) {
        return releaseBuffer(buffer, new ByteBufferFactory());
    }

    /**
     * Makes the given buffer available for further calls to {@link #getCharBuffer(int)} with the same buffer capacity.
     * Returns <code>true</code> if the buffer was added to the pool, <code>false</code> if the buffer was already in
     * the pool.
     *
     * <p>After calling this method, the given buffer instance <b>must not be used</b>, otherwise it could get
     * corrupted if other threads were using it.</p>
     *
     * @param buffer the buffer instance to make available for further use
     * @return <code>true</code> if the buffer was added to the pool, <code>false</code> if the buffer was already in the pool
     * @throws IllegalArgumentException if specified buffer is null
     */
    public static synchronized boolean releaseCharBuffer(CharBuffer buffer) {
        return releaseBuffer(buffer, new CharBufferFactory());
    }

    /**
     * Makes the given buffer available for further calls to {@link #getBuffer(com.mucommander.commons.io.BufferPool.BufferFactory,int)} with the same buffer
     * size and factory.
     * Returns <code>true</code> if the buffer was added to the pool, <code>false</code> if the buffer was already in 
     * the pool or the pool size limit has been reached.
     *
     * <p>After calling this method, the given buffer instance <b>must not be used</b>, otherwise it could get
     * corrupted if other threads were using it.</p>
     *
     * @param buffer the buffer instance to make available for further use
     * @param factory the BufferFactory that was used to create the buffer
     * @return <code>true</code> if the buffer was added to the pool, <code>false</code> if the buffer was already in the pool or the pool size limit has been reached
     * @throws IllegalArgumentException if specified buffer is null
     */
    public static synchronized boolean releaseBuffer(Object buffer, BufferFactory factory) {
        if(buffer==null)
            throw new IllegalArgumentException("specified buffer is null");

        BufferContainer bufferContainer = factory.newBufferContainer(buffer);

        if(bufferContainers.contains(bufferContainer)) {
            LOGGER.info("Warning: specified buffer is already in the pool: {}", buffer);
            return false;
        }

        long bufferSize = bufferContainer.getSize();        // size in bytes (!= length)

        if(maxPoolSize!=-1 && poolSize+bufferSize>maxPoolSize) {
            LOGGER.info("Warning: maximum pool size reached, buffer not added to the pool: {}", buffer);
            return false;
        }

        bufferContainers.add(bufferContainer);
        poolSize += bufferSize;

        return true;
    }

    /**
     * Returns <code>true</code> if the specified buffer is currently in the pool.
     *
     * <p>Note that it is not necessary (and thus not recommended for performance reasons) to call this method before
     * calling <code>release*Buffer</code> as it already performs this test before adding a buffer to the pool.</p>
     *
     * @param buffer the buffer to look for in the pool
     * @param factory the BufferFactory that was used to create the buffer
     * @return <code>true</code> if the specified buffer is already in the pool
     */
    public static boolean containsBuffer(Object buffer, BufferFactory factory) {
        return bufferContainers.contains(factory.newBufferContainer(buffer));
    }


    /**
     * Returns the number of buffers that currently are in the pool. This method is provided for debugging
     * purposes only.
     *
     * @return the number of buffers currently in the pool
     */
    public static int getBufferCount() {
        return bufferContainers.size();
    }

    /**
     * Returns the number of buffers that currently are in the pool and whose Class are the same as the specified
     * factory's. This method is provided for debugging purposes only.
     *
     * @param factory the BufferFactory
     * @return the number of buffers currently in the pool
     */
    public static int getBufferCount(BufferFactory factory) {
        int count = 0;        
        int nbBuffers = bufferContainers.size();
        for(int i=0; i<nbBuffers; i++) {
            if(factory.matchesBufferClass(bufferContainers.elementAt(i).getBuffer().getClass())) {
                count ++;
            }
        }

        return count;
    }

    /**
     * Returns the default size of buffers returned by <code>get*Buffer</code> methods without a <code>size</code>
     * argument.
     *
     * <p>The default buffer size is initially set to {@link #INITIAL_DEFAULT_BUFFER_SIZE}.</p>
     *
     * @return the default size of buffers returned by <code>get*Buffer</code> methods without a <code>size</code> argument
     */
    public static int getDefaultBufferSize() {
        return defaultBufferSize;
    }

    /**
     * Sets the default size of buffers returned by <code>get*Buffer</code> methods without a <code>size</code> argument.
     *
     * @param bufferSize the new buffer size
     */
    public static synchronized void setDefaultBufferSize(int bufferSize) {
        BufferPool.defaultBufferSize = bufferSize;
    }


    /**
     * Returns the combined size in bytes of all buffers that are currently in the pool.
     *
     * @return the combined size in bytes of all buffers that are currenty in the pool
     */
    public static long getPoolSize() {
        return poolSize;
    }

    /**
     * Returns the maximum combined size in bytes for all buffers in the pool, <code>-1</code> for no limit.
     * Before adding a buffer to the pool, <code>release*Buffer</code> methods ensure that the pool size will
     * not be exceeded. If and only if that is the case, the buffer is added to the pool.
     *
     * <p>The max pool size is initially set to {@link #INITIAL_POOL_LIMIT}.</p>
     *
     * @return the maximum combined size in bytes for all buffers in the pool
     */
    public static long getMaxPoolSize() {
        return maxPoolSize;
    }

    /**
     * Sets the maximum combined size in bytes for all buffers in the pool, <code>-1</code> for no limit.
     * Before adding a buffer to the pool, <code>release*Buffer</code> methods ensure that the pool size will
     * not be exceeded. If and only if that is the case, the buffer is added to the pool.
     *
     * @param maxPoolSize the maximum combined size in bytes for all buffers in the pool
     */
    public static synchronized void setMaxPoolSize(long maxPoolSize) {
        BufferPool.maxPoolSize = maxPoolSize;
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
         * Returns the length of the wrapped buffer instance.
         *
         * @return the length of the wrapped buffer instance
         */
        protected abstract int getLength();

        /**
         * Returns the size of the wrapped buffer instance, expressed in bytes.
         *
         * @return the size of the wrapped buffer instance, expressed in bytes
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
        public boolean matchesBufferClass(Class<?> bufferClass) {
            return getBufferClass().isAssignableFrom(bufferClass);
        }

        /**
         * Creates and returns a buffer instance of the specified size.
         *
         * @param size size of the buffer to create
         * @return a buffer instance of the specified size
         */
        public abstract Object newBuffer(int size);

        /**
         * Creates and returns a {@link BufferContainer} for the specified buffer instance.
         *
         * @param buffer the buffer to wrap in a BufferContainer
         * @return returns a BufferContainer for the specified buffer instance
         */
        public abstract BufferContainer newBufferContainer(Object buffer);

        /**
         * Returns the Class of buffer instances this factory creates. 
         *
         * @return the Class of buffer instances this factory creates
         */
        public abstract Class<?> getBufferClass();
    }

    /**
     * This class is a {@link BufferFactory} implementation for byte array (<code>byte[]</code>) buffers.
     */
    public static class ByteArrayFactory extends BufferFactory {
        @Override
        public Object newBuffer(int size) {
            return new byte[size];
        }

        @Override
        public BufferContainer newBufferContainer(Object buffer) {
            return new BufferContainer(buffer) {
                @Override
                protected int getLength() {
                    return ((byte[])buffer).length;
                }

                @Override
                protected int getSize() {
                    return getLength();
                }
            };
        }

        @Override
        public Class<?> getBufferClass() {
            return byte[].class;
        }
    }

    /**
     * This class is a {@link BufferFactory} implementation for char array (<code>char[]</code>) buffers.
     */
    public static class CharArrayFactory extends BufferFactory {
        @Override
        public Object newBuffer(int size) {
            return new char[size];
        }

        @Override
        public BufferContainer newBufferContainer(Object buffer) {
            return new BufferContainer(buffer) {
                @Override
                protected int getLength() {
                    return ((char[])buffer).length;
                }

                @Override
                protected int getSize() {
                    return 2*getLength();
                }
            };
        }

        @Override
        public Class<?> getBufferClass() {
            return char[].class;
        }
    }

    /**
     * This class is a {@link BufferFactory} implementation for <code>java.nio.ByteBuffer</code> buffers.
     * ByteBuffer instances created by {@link #newBuffer(int)} are direct ; the actually Class of those instances may be actually
     * be <code>java.nio.DirectByteBuffer</code> and not <code>java.nio.ByteBuffer</code> as returned by
     * {@link #getBufferClass()}.
     */
    public static class ByteBufferFactory extends BufferFactory {
        @Override
        public Object newBuffer(int size) {
            // Note: the returned instance is actually a java.nio.DirectByteBuffer, this is why it's important to
            // compare classes using Class#isAssignableFrom(Class)
            return ByteBuffer.allocateDirect(size);
        }

        @Override
        public BufferContainer newBufferContainer(Object buffer) {
            return new BufferContainer(buffer) {
                @Override
                protected int getLength() {
                    return ((ByteBuffer)buffer).capacity();
                }

                @Override
                protected int getSize() {
                    return getLength();
                }
            };
        }

        @Override
        public Class<?> getBufferClass() {
            return ByteBuffer.class;
        }
    }

    /**
     * This class is a {@link BufferFactory} implementation for <code>java.nio.CharBuffer</code> buffers.
     */
    public static class CharBufferFactory extends BufferFactory {
        @Override
        public Object newBuffer(int size) {
            return CharBuffer.allocate(size);
        }

        @Override
        public BufferContainer newBufferContainer(Object buffer) {
            return new BufferContainer(buffer) {
                @Override
                protected int getLength() {
                    return ((CharBuffer)buffer).capacity();
                }

                @Override
                protected int getSize() {
                    return 2*getLength();
                }
            };
        }

        @Override
        public Class<?> getBufferClass() {
            return CharBuffer.class;
        }
    }
}
