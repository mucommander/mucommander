package com.mucommander.commons.file.protocol.smb.smbj.stream.random;

import com.hierynomus.smbj.share.File;
import com.mucommander.commons.io.RandomAccessOutputStream;

import java.io.IOException;

public class SmbjRandomAccessOutputStream extends RandomAccessOutputStream {

    private final File file;

    private long fileLength;

    private long position = 0;

    private SmbjRandomAccessOutputStream(final File file, final long fileLength) {
        this.file = file;
        this.fileLength = fileLength;
    }


    @Override
    public void write(int b) throws IOException {
        byte[] singleByte = {(byte) b};
        write(singleByte, 0, 1);
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (len < 0 || off < 0 || off + len > b.length) {
            throw new IOException(String.format("Invalid offset or length [off = %d, len = %d]", off, len));
        }

        byte[] buffer = new byte[len];
        System.arraycopy(b, off, buffer, 0, len);
        file.write(buffer, position);

        position += len;
        fileLength = Math.max(fileLength, position);
    }

    @Override
    public void setLength(long newLength) throws IOException {
        if (newLength < 0) {
            throw new IOException(String.format("New length cannot be negative [newLength = %d]", newLength));
        }

        file.setLength(newLength);
        fileLength = newLength;

        // Adjust the current position if it is beyond the new length
        if (position > newLength) {
            position = newLength;
        }
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    @Override
    public long getOffset() throws IOException {
        return position;
    }

    @Override
    public long getLength() throws IOException {
        return fileLength;
    }

    @Override
    public void seek(long offset) throws IOException {
        if (offset < 0 || offset > fileLength) {
            throw new IOException("Invalid offset: " + offset);
        }
        position = offset;
    }

    public static SmbjRandomAccessOutputStream create(final File file) {
        long fileLength = file.getFileInformation().getStandardInformation().getEndOfFile();
        return new SmbjRandomAccessOutputStream(file, fileLength);
    }

}
