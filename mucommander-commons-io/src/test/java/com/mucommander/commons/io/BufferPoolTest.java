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

import org.testng.annotations.Test;

/**
 * This class is a TestNG test case for {@link BufferPool}.
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class BufferPoolTest {

    public final static int TEST_BUFFER_SIZE_1 = 27;
    public final static int TEST_BUFFER_SIZE_2 = 28;
    public final static int TEST_MAX_POOL_SIZE = 1000;

    /**
     * Tests <code>BufferPool</code> with byte array (<code>byte[]</code>) buffers.
     *
     * <p>This method invokes {@link #testBuffer(com.mucommander.commons.io.BufferPool.BufferFactory)} with a
     * {@link BufferPool.ByteArrayFactory} instance.</p>
     */
    @Test
    public void testByteArrayBuffer() {
        testBuffer(new BufferPool.ByteArrayFactory());
    }

    /**
     * Tests <code>BufferPool</code> with char array (<code>char[]</code>) buffers.
     *
     * <p>This method invokes {@link #testBuffer(com.mucommander.commons.io.BufferPool.BufferFactory)} with a
     * {@link BufferPool.CharArrayFactory} instance.</p>
     */
    @Test
    public void testCharArrayBuffer() {
        testBuffer(new BufferPool.CharArrayFactory());
    }

    /**
     * Tests <code>BufferPool</code> with <code>ByteBuffer</code> buffers.
     *
     * <p>This method invokes {@link #testBuffer(com.mucommander.commons.io.BufferPool.BufferFactory)} with a
     * {@link BufferPool.ByteBufferFactory} instance.</p>
     */
    @Test
    public void testByteBuffer() {
        testBuffer(new BufferPool.ByteBufferFactory());
    }

    /**
     * Tests <code>BufferPool</code> with <code>CharBuffer</code> buffers.
     *
     * <p>This method invokes {@link #testBuffer(com.mucommander.commons.io.BufferPool.BufferFactory)} with a
     * {@link BufferPool.CharBufferFactory} instance.</p>
     */
    @Test
    public void testCharBuffer() {
        testBuffer(new BufferPool.CharBufferFactory());
    }

    /**
     * Tests <code>BufferPool</code> with <code>ByteBuffer</code> buffers.
     *
     * <p>This test assumes that no buffer with size=={@link #TEST_BUFFER_SIZE_1} or size=={@link #TEST_BUFFER_SIZE_2}
     * exist in the pool when the test starts. It also assumes that no other thread uses <code>BufferPool</code> while
     * the test is being performed. <code>BufferPool</code> will be left in the same state as it was right before the
     * test.</p>
     *
     * @param factory the factory corresponding to the kind of buffer to test
     */
    public void testBuffer(BufferPool.BufferFactory factory) {
        // Number of array buffers before we started the test
        int originalBufferCount = BufferPool.getBufferCount(factory);
        long originalPoolSize = BufferPool.getPoolSize();

        // Create a new buffer with size=TEST_BUFFER_SIZE_1
        Object buffer1 = BufferPool.getBuffer(factory, TEST_BUFFER_SIZE_1);
        assertBufferSize(buffer1, factory, TEST_BUFFER_SIZE_1);

        // Create a new buffer with size=TEST_BUFFER_SIZE_1, assert that it is different from the first one
        Object buffer2 = BufferPool.getBuffer(factory, TEST_BUFFER_SIZE_1);
        assertBufferSize(buffer2, factory, TEST_BUFFER_SIZE_1);
        assert buffer2!=buffer1;

        // Create a new buffer with size=TEST_BUFFER_SIZE_2
        Object buffer3 = BufferPool.getBuffer(factory, TEST_BUFFER_SIZE_2);
        assertBufferSize(buffer3, factory, TEST_BUFFER_SIZE_2);

        // Assert that the number of buffers in the pool and the pool size in bytes haven't changed
        assertBufferCount(originalBufferCount, factory);
        assert originalPoolSize == BufferPool.getPoolSize();

        // Assert that none of the buffers we created are in the pool
        assert !BufferPool.containsBuffer(buffer1, factory);
        assert !BufferPool.containsBuffer(buffer2, factory);
        assert !BufferPool.containsBuffer(buffer3, factory);

        // Assert that releasing buffer3 and requesting a buffer with size=TEST_BUFFER_SIZE_2 brings back buffer3
        BufferPool.releaseBuffer(buffer3, factory);
        assertBufferCount(originalBufferCount+1, factory);
        assert BufferPool.containsBuffer(buffer3, factory);
        assert buffer3==BufferPool.getBuffer(factory, TEST_BUFFER_SIZE_2);
        assertBufferCount(originalBufferCount, factory);
        assert !BufferPool.containsBuffer(buffer3, factory);

        // Release all buffer instances and assert that the buffer count grows accordingly
        Object[] buffers = new Object[]{buffer2, buffer3, buffer1};
        for(int b=0; b<buffers.length; b++) {
            // Call releaseBuffer twice and assert that the buffer is added to the pool only the first time
            assert BufferPool.releaseBuffer(buffers[b], factory);
            assert BufferPool.containsBuffer(buffers[b], factory);
            assertBufferCount(originalBufferCount+(b+1), factory);

            assert !BufferPool.releaseBuffer(buffers[b], factory);
            assert BufferPool.containsBuffer(buffers[b], factory);
            assertBufferCount(originalBufferCount+(b+1), factory);
        }

        // Retrieve all the buffers we created to leave BufferPool as it was before the test
        // and assert that the buffer count diminishes accordingly
        buffers = new Object[]{buffer3, buffer1, buffer2};
        for(int b=0; b<buffers.length; b++) {
            BufferPool.getBuffer(factory, getBufferLength(buffers[b], factory));
            assertBufferCount(originalBufferCount+(3-b-1), factory);
        }

        // Test the initial default buffer size
        assert BufferPool.INITIAL_DEFAULT_BUFFER_SIZE == BufferPool.getDefaultBufferSize();
        assertBufferSize(BufferPool.getBuffer(factory), factory, BufferPool.INITIAL_DEFAULT_BUFFER_SIZE);

        // Test a custom default buffer size
        BufferPool.setDefaultBufferSize(TEST_BUFFER_SIZE_1);
        assert TEST_BUFFER_SIZE_1 == BufferPool.getDefaultBufferSize();
        assertBufferSize(BufferPool.getBuffer(factory), factory, TEST_BUFFER_SIZE_1);

        // Reset the default buffer size to the initial value
        BufferPool.setDefaultBufferSize(BufferPool.INITIAL_DEFAULT_BUFFER_SIZE);

        // Test max pool size: max out the pool and verify that releaseBuffer fails (returns false) when trying
        // to add an extra buffer
        assert BufferPool.INITIAL_POOL_LIMIT == BufferPool.getMaxPoolSize();
        BufferPool.setMaxPoolSize(TEST_MAX_POOL_SIZE);
        assert TEST_MAX_POOL_SIZE == BufferPool.getMaxPoolSize();
        long bufferSize = getBufferSize(BufferPool.getBuffer(factory, TEST_BUFFER_SIZE_1), factory);    // in bytes
        int nbBuffers = (int)(TEST_MAX_POOL_SIZE/bufferSize);
        buffers = new Object[nbBuffers];
        for(int i=0; i<nbBuffers; i++)
            buffers[i] = BufferPool.getBuffer(factory, TEST_BUFFER_SIZE_1);

        Object extraBuffer = BufferPool.getBuffer(factory, TEST_BUFFER_SIZE_1);

        assert originalPoolSize == BufferPool.getPoolSize();
        long lastPoolSize = originalPoolSize;
        long newPoolSize;
        for(int i=0; i<nbBuffers; i++) {
            assert BufferPool.releaseBuffer(buffers[i], factory);
            newPoolSize = BufferPool.getPoolSize();
            assert lastPoolSize+bufferSize == newPoolSize;
            lastPoolSize = newPoolSize;
        }
        // At this point, the BufferPool should be maxed out, try adding one more buffer and assert that this fails
        assert !BufferPool.releaseBuffer(extraBuffer, factory);

        // Retrieve all the buffers we created to leave BufferPool as it was before the test
        // and assert that the pool returns to its original size
        for(int i=0; i<nbBuffers; i++)
            BufferPool.getBuffer(factory, TEST_BUFFER_SIZE_1);

        assert originalPoolSize == BufferPool.getPoolSize();

        BufferPool.setMaxPoolSize(BufferPool.INITIAL_POOL_LIMIT);
    }

    /**
     * Asserts that the given buffer's size matches the specified one.
     *
     * @param buffer the buffer to test
     * @param factory the factory that was used to create the buffer
     * @param expectedSize the expected buffer size
     */
    private void assertBufferSize(Object buffer, BufferPool.BufferFactory factory, int expectedSize) {
        assert expectedSize == getBufferLength(buffer, factory);
    }

    /**
     * Returns the length of the given buffer, as defined by {@link com.mucommander.commons.io.BufferPool.BufferContainer#getLength()}.
     *
     * @param buffer the buffer for which to return the length
     * @param factory the factory that was used to create the buffer
     * @return the length of the given buffer
     */
    private int getBufferLength(Object buffer, BufferPool.BufferFactory factory) {
        return factory.newBufferContainer(buffer).getLength();
    }

    /**
     * Returns the size in bytes of the given buffer, as defined by {@link com.mucommander.commons.io.BufferPool.BufferContainer#getSize()}.
     *
     * @param buffer the buffer for which to return the size
     * @param factory the factory that was used to create the buffer
     * @return the size in bytes of the given buffer
     */
    private int getBufferSize(Object buffer, BufferPool.BufferFactory factory) {
        return factory.newBufferContainer(buffer).getSize();
    }

    /**
     * Asserts that BufferPool contains <code>expectedCount</code> buffers of the kind corresponding to the factory.
     *
     * @param expectedCount the expected number of buffers
     * @param factory the factory corresponding to the kind of buffer to count
     */
    private void assertBufferCount(int expectedCount, BufferPool.BufferFactory factory) {
        assert expectedCount == BufferPool.getBufferCount(factory);
    }
}
