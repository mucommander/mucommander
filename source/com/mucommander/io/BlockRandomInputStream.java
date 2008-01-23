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

import java.io.IOException;

/**
 * <code>BlockRandomInputStream</code> is a specialized-yet-still-abstract <code>RandomAccessInputStream</code> that
 * is geared towards resources that are read block by block, either because of a particular constrain or for performance
 * reasons. This class typically comes in handy for network resources such as HTTP which have to request a block range
 * for reading the resource.
 *
 * <p>Seeking inside the file is implemented transparently by reading a block starting at the seek offset.
 * If {@link #seek(long)} is called with an offset that is within the current block, no read occurs.
 * The block size should be carefully chosen as it affects seek performance and thus overall performance greatly:
 * the larger the block size, the more data is fetched when seeking outside the current block and consequently the
 * longer it takes to reposition the stream. On the other hand, a larger block size will yield better performance when
 * reading the resource sequentially, as it lessens the overhead of requesting a particular block.</p>
 *
 * @author Maxence Bernard
 */
public abstract class BlockRandomInputStream extends RandomAccessInputStream {

    /** Block size, i.e. length of the {@link #block} array */
    protected final int blockSize;

    /** Contains the current file block. Data may end before the array does. */
    private final byte block[];

    /** Current offset within the block array to the next byte to return */
    private int blockOff;

    /** Length of the current block */
    private int blockLen;

    /** Global offset within the file */
    private long offset;


    /**
     * Creates a new <code>BlockRandomInputStream</code> using the specified block size.
     *
     * <p>The block size should be carefully chosen as it affects seek performance and thus overall performance greatly:
     * the larger the block size, the more data is fetched when seeking outside the current block and consequently the
     * longer it takes to reposition the stream. On the other hand, a larger block size will yield better performance
     * when reading the resource sequentially, as it lessens the overhead of requesting a particular block.</p>
     *
     * @param blockSize controls the amount of data requested when reading a block
     */
    protected BlockRandomInputStream(int blockSize) {
        this.blockSize = blockSize;
        block = new byte[blockSize];
    }

    /**
     * Returns <code>true</code> if the end of file has been reached.
     *
     * @return true if the end of file has been reached.
     * @throws IOException if an I/O error occurred
     */
    private boolean eofReached() throws IOException {
        return offset>=getLength();
    }

    /**
     * Checks if the current buffered block has been read completely (i.e. no more data is available) and if it has,
     * calls {@link #readBlock(long, byte[], int)} to fetch the next block.
     *
     * @throws IOException if an I/O error occurred
     */
    private void checkBuffer() throws IOException {
        if(blockOff >= blockLen)      // True initially
            readBlock();
    }

    /**
     * Calls {@link #readBlock(long, byte[], int)} to read a block of up to <code>blockSize</code>, less if the
     * the end of file is near.
     *
     * @throws IOException if an I/O error occurred
     */
    private void readBlock() throws IOException {
        int len = Math.min((int)(getLength()-offset), blockSize);
        // update len with the number of bytes actually read
        len = readBlock(offset, block, len);

        // Note: these fields won't be updated if an I/O error occurs
        this.blockOff = 0;
        this.blockLen = len;
    }


    ////////////////////////////////////////////
    // RandomAccessInputStream implementation //
    ////////////////////////////////////////////

    public int read() throws IOException {
        if(eofReached())
            return -1;

        checkBuffer();

        int ret = block[blockOff];

        blockOff++;
        offset ++;

        return ret;
    }

    public int read(byte b[], int off, int len) throws IOException {
        if(len==0)
            return 0;

        if(eofReached())
            return -1;

        checkBuffer();

        int nbBytes = Math.min(len, blockLen - blockOff);
        System.arraycopy(block, blockOff, b, off, nbBytes);

        blockOff += nbBytes;
        offset += nbBytes;

        return nbBytes;
    }

    public long getOffset() throws IOException {
        return offset;
    }

    public void seek(long newOffset) throws IOException {
        // If the new offset is within the current buffer's range, simply reposition the offsets
        if(newOffset>=offset && newOffset<offset+ blockLen) {
            blockOff += (int)(newOffset-offset);
            offset = newOffset;
        }
        // If not, retrieve a block of data starting at the new offset and fill the buffer with it
        else {
            offset = newOffset;
            readBlock();
        }
    }


    ///////////////////////
    // Abstract methods //
    ///////////////////////

    /**
     * Reads a block, that spawns from <code>fileOffset</code> to <code>fileOffset+blockLen</code>, an returns
     * the number of bytes that could be read, normally <code>blockLen</code> but can be less.
     *
     * <p>Note that <code>blockLen</code> may be smaller than {@link #blockSize} if the end of file is near, to prevent
     * <code>EOF</code> from being reached. In other words, <code>fileOffset+blockLen</code> should theorically not
     * exceed the file's length, but this could happen in the unlikely event that the file just shrinked after
     * {@link #getLength()} was last called. So this method's implementation should handle the case where
     * <code>EOF</code> is reached prematurely and return the number of bytes that were actually read.</p>
     *
     * @param fileOffset global file offset that marks the beginning of the block
     * @param block the array to fill with data, starting at 0
     * @param blockLen number of bytes to read
     * @return the number of bytes that were actually read, normally blockLen unless
     * @throws IOException if an I/O error occurred
     */
    protected abstract int readBlock(long fileOffset, byte block[], int blockLen) throws IOException;
}
