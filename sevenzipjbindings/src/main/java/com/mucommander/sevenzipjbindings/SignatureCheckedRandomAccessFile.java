/**
 * This file is part of muCommander, http://www.mucommander.com
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

package com.mucommander.sevenzipjbindings;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.StreamUtils;
import com.mucommander.commons.util.StringUtils;

import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.ISequentialInStream;
import net.sf.sevenzipjbinding.SevenZipException;

/**
 * @author Oleg Trifonov
 */
public class SignatureCheckedRandomAccessFile implements IInStream, ISequentialInStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignatureCheckedRandomAccessFile.class);

    private final AbstractFile file;

    private InputStream stream;

    private long position;

    public SignatureCheckedRandomAccessFile(AbstractFile file, byte[] signature) throws UnsupportedFileOperationException {
        super();
        this.position = 0;
        this.file = file;
        try {
            this.stream = openStreamAndCheckSignature(file, signature);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.trace("Error", e);
            throw new UnsupportedFileOperationException(FileOperation.READ_FILE);
        }
    }


    @Override
    public synchronized long seek(long offset, int seekOrigin) throws SevenZipException {
        try {
            if (file.isFileOperationSupported(FileOperation.RANDOM_READ_FILE)) {
                seekOnRandomAccessFile(offset, seekOrigin);
            } else {
                seekOnSequentialFile(offset, seekOrigin);
            }
        } catch (IOException e) {
            throw new SevenZipException(e);
        }
        return position;
    }

    private void seekOnRandomAccessFile(long offset, int seekOrigin) throws IOException {
        RandomAccessInputStream randomAccessInputStream = (RandomAccessInputStream) stream;
        switch (seekOrigin) {
        case SEEK_SET:
            position = offset;
            break;
        case SEEK_CUR:
            position += offset;
            break;
        case SEEK_END:
            position = randomAccessInputStream.getLength() + offset;
            break;
        }
        randomAccessInputStream.seek(position);
    }

    /**
     * @param offset
     * @param seekOrigin
     * @throws IOException
     */
    private void seekOnSequentialFile(long offset, int seekOrigin) throws IOException {
        switch (seekOrigin) {
        case SEEK_SET:
            if (position != offset) {
                stream.close();
                stream = file.getInputStream();
                skip(offset);
                position = offset;
            }
            break;
        case SEEK_CUR:
            skip(offset);
            position += offset;
            break;
        case SEEK_END:
            long size = file.getSize();
            if (size == -1) {
                throw new IOException("can't seek from file end without knowing it's size");
            }
            long newPosition = size + (offset > 0 ? offset : 0);
            if (position != newPosition) {
                position = newPosition;
                stream.close();
                stream = file.getInputStream();
                stream.skip(position);
            }
            break;
        }
    }

    /**
     * @param skip
     * @throws IOException
     */
    private void skip(long skip) throws IOException {
        if (skip <= 0) {
            return;
        }
        long skipped = stream.skip(skip);
        if (skipped < 0) {
            throw new IOException("non reasonable number of bytes skipped");
        }
        position += skipped;
        while (skipped < skip) {
            int skipNow = (int) Long.min(skip - skipped, 1024);
            byte[] skipBuffer = new byte[skipNow];
            int read = stream.read(skipBuffer, 0, skipBuffer.length);
            if (read == -1) {
                break;
            } else {
                position += read;
                skipped += read;
            }
        }
    }

    @Override
    public synchronized int read(byte[] bytes) throws SevenZipException {
        if (bytes.length == 0) {
            return 0;
        }
        try {
            int read = stream.read(bytes);
            if (read != -1) {
                position += read;
                return read;
            }
            return 0;
        } catch (IOException e) {
            throw new SevenZipException(e);
        }
    }

    private InputStream openStreamAndCheckSignature(AbstractFile file, byte[] signature) throws IOException {
        byte[] buf = new byte[signature.length];

        InputStream is = null;

        int read = 0;
        try {
            if (file.isFileOperationSupported(FileOperation.RANDOM_READ_FILE)) {
                RandomAccessInputStream raiStream = file.getRandomAccessInputStream();
                is = raiStream;
                if (buf.length > 0) {
                    raiStream.seek(0);
                    read = StreamUtils.readUpTo(raiStream, buf);
                    raiStream.seek(0);
                }
            } else {
                PushbackInputStream pushbackInputStream = null;
                if (buf.length > 0) {
                    is = new PushbackInputStream(file.getInputStream(), buf.length);
                    read = StreamUtils.readUpTo(pushbackInputStream, buf);
                } else {
                    is = file.getInputStream();
                }
                // TODO sometimes reading from pushbackInputStream returns 0
                if (read <= 0 && file.getSize() > 0) {
                    return file.getInputStream();
                }
                pushbackInputStream.unread(buf, 0, read);
            }
            if (signature != null && !checkSignature(buf, signature)) {
                throw new IOException("Wrong file signature was " + StringUtils.bytesToHexStr(buf, 0, read)
                + " but should be " + StringUtils.bytesToHexStr(signature, 0, signature.length));
            }
        } catch (IOException e) {
            if (is != null)
                is.close();
            throw e;
        }

        return is;
    }

    private static boolean checkSignature(byte[] data, byte[] signature) {
        for (int i = 0; i < signature.length; i++) {
            if (data[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public synchronized void close() throws IOException {
        if (stream != null) {
            stream.close();
        }
    }
}