package com.mucommander.commons.file.protocol.smb.smbj.stream.random;

import com.hierynomus.smbj.share.File;
import com.mucommander.commons.io.RandomAccessInputStream;

import java.io.IOException;

public class SmbjRandomAccessInputStream extends RandomAccessInputStream {

    private final File file;

    private final long fileLength;

    private long position = 0;

    private SmbjRandomAccessInputStream(File file, long fileLength) {
        this.file = file;
        this.fileLength = fileLength;
    }

    @Override
    public int read() throws IOException {
        byte[] buffer = new byte[1];
        int bytesRead = read(buffer, 0, 1);
        return bytesRead == -1 ? -1 : buffer[0] & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (position >= fileLength) {
            return -1; // End of file
        }

        // Adjust length if it goes beyond file length
        len = (int) Math.min(len, fileLength - position);

        // Read from file at the current position
        byte[] buffer = new byte[len];
        int bytesRead = file.read(buffer, position);

        if (bytesRead == -1) {
            return -1;
        }

        // Copy data into the provided array
        System.arraycopy(buffer, 0, b, off, bytesRead);
        position += bytesRead; // Update the position

        return bytesRead;
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
        position = offset; // Set the position to the specified offset
    }

    public static SmbjRandomAccessInputStream create(File file) {
        long fileLength = file.getFileInformation().getStandardInformation().getEndOfFile();
        return new SmbjRandomAccessInputStream(file, fileLength);
    }

}
