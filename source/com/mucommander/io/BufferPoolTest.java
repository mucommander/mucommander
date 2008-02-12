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

import junit.framework.TestCase;

import java.nio.ByteBuffer;

/**
 * This class is a JUnit test case for {@link BufferPool}.
 *
 * @author Maxence Bernard
 */
public class BufferPoolTest extends TestCase {

    /**
     * Tests <code>BufferPool</code> with byte array (<code>byte[]</code>) buffers.
     *
     * <p>This test assumes that no buffer with size==27 or size==28 exist in the pool when the test starts.
     * BufferPool is left is the same state as it was before the test.</p>
     */
    public void testArrayBuffer() {
        BufferPool.BufferFactory bufferFactory = new BufferPool.ArrayBufferFactory();
        // Number of array buffers before we started the test
        int originalPoolCount = BufferPool.getBufferCount(bufferFactory);

        // Create a new buffer with size=27
        byte[] buffer1 = BufferPool.getArrayBuffer(27);
        assertEquals(27, buffer1.length);
        assertBufferCount(originalPoolCount, bufferFactory);

        // Create a new buffer with size=27, assert that it is different from the first one
        byte[] buffer2 = BufferPool.getArrayBuffer(27);
        assertEquals(27, buffer2.length);
        assertFalse(buffer2==buffer1);
        assertBufferCount(originalPoolCount, bufferFactory);

        // Create a new buffer with size=28
        byte[] buffer3 = BufferPool.getArrayBuffer(28);
        assertEquals(28, buffer3.length);
        assertBufferCount(originalPoolCount, bufferFactory);

        // Assert that releasing buffer3 and requesting a buffer with size=28 brings back buffer3
        BufferPool.releaseArrayBuffer(buffer3);
        assertBufferCount(originalPoolCount+1, bufferFactory);
        assertTrue(buffer3==BufferPool.getArrayBuffer(28));
        assertBufferCount(originalPoolCount, bufferFactory);

        // Release all buffer instances and assert that the buffer count grows accordingly
        byte[][] buffers = new byte[][]{buffer2, buffer3, buffer1};
        for(int b=0; b<buffers.length; b++) {
            // - This time, use releaseBuffer rather than the specialized release*Buffer
            // - Call releaseBuffer twice just to make sure nothing evil happens
            for(int i=0; i<2; i++) {
                BufferPool.releaseBuffer(buffers[b], bufferFactory);
                assertBufferCount(originalPoolCount+(b+1), bufferFactory);
            }
        }

        // Retrieve all the buffers we created to leave BufferPool as it was before the test and assert that the buffer
        // count diminishes accordingly
        buffers = new byte[][]{buffer3, buffer1, buffer2};
        for(int b=0; b<buffers.length; b++) {
            // This time, use getBuffer rather than the specialized get*Buffer
            BufferPool.getBuffer(buffers[b].length, bufferFactory);
            assertBufferCount(originalPoolCount+(3-b-1), bufferFactory);
        }
    }

    /**
     * Tests <code>BufferPool</code> with <code>ByteBuffer</code> buffers.
     *
     * <p>This test assumes that no buffer with size==27 or size==28 exist in the pool when the test starts.
     * BufferPool is left is the same state as it was before the test.</p>
     */
    public void testByteBuffer() {
        BufferPool.BufferFactory bufferFactory = new BufferPool.ByteBufferFactory();
        // Number of array buffers before we started the test
        int originalPoolCount = BufferPool.getBufferCount(bufferFactory);

        // Create a new buffer with size=27
        ByteBuffer buffer1 = BufferPool.getByteBuffer(27);
        assertEquals(27, buffer1.capacity());
        assertBufferCount(originalPoolCount, bufferFactory);

        // Create a new buffer with size=27, assert that it is different from the first one
        ByteBuffer buffer2 = BufferPool.getByteBuffer(27);
        assertEquals(27, buffer2.capacity());
        assertFalse(buffer2==buffer1);
        assertBufferCount(originalPoolCount, bufferFactory);

        // Create a new buffer with size=28
        ByteBuffer buffer3 = BufferPool.getByteBuffer(28);
        assertEquals(28, buffer3.capacity());
        assertBufferCount(originalPoolCount, bufferFactory);

        // Assert that releasing buffer3 and requesting a buffer with size=28 brings back buffer3
        BufferPool.releaseByteBuffer(buffer3);
        assertBufferCount(originalPoolCount+1, bufferFactory);
        assertTrue(buffer3==BufferPool.getByteBuffer(28));
        assertBufferCount(originalPoolCount, bufferFactory);

        // Release all buffer instances and assert that the buffer count grows accordingly
        ByteBuffer[] buffers = new ByteBuffer[]{buffer2, buffer3, buffer1};
        for(int b=0; b<buffers.length; b++) {
            // - This time, use releaseBuffer rather than the specialized release*Buffer
            // - Call releaseBuffer twice just to make sure nothing evil happens
            for(int i=0; i<2; i++) {
                BufferPool.releaseBuffer(buffers[b], bufferFactory);
                assertBufferCount(originalPoolCount+(b+1), bufferFactory);
            }
        }

        // Retrieve all the buffers we created to leave BufferPool as it was before the test and assert that the buffer
        // count diminishes accordingly
        buffers = new ByteBuffer[]{buffer3, buffer1, buffer2};
        for(int b=0; b<buffers.length; b++) {
            // This time, use getBuffer rather than the specialized get*Buffer
            BufferPool.getBuffer(buffers[b].capacity(), bufferFactory);
            assertBufferCount(originalPoolCount+(3-b-1), bufferFactory);
        }
    }

    private void assertBufferCount(int expectedCount, BufferPool.BufferFactory factory) {
        assertEquals(expectedCount, BufferPool.getBufferCount(factory));
    }
}
