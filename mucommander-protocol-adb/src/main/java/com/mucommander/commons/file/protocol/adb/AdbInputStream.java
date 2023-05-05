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

package com.mucommander.commons.file.protocol.adb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.RemoteFile;

/**
 * @author Oleg Trifonov
 * Created on 29/12/15.
 */
public class AdbInputStream extends InputStream {

    private static final long MAX_CACHED_SIZE = 10*1024*1024;

    private final ByteArrayOutputStream bos;
    private InputStream is;
    private final File tempFile;

    AdbInputStream(AdbFile file) throws IOException {
        this.bos = file.getSize() <= MAX_CACHED_SIZE ? new ByteArrayOutputStream() : null;
        this.tempFile = bos == null ? File.createTempFile(file.getName(), ""+System.currentTimeMillis() + "-" + new Random().nextInt(0xffff)) : null;

        JadbDevice device = file.getDevice(file.getURL());
        if (device == null) {
            throw new IOException("file not found: " + file.getURL());
        }
        if (bos != null) {
            try {
                device.pull(new RemoteFile(file.getURL().getPath()), bos);
            } catch (JadbException e) {
                close();
                throw new IOException(e);
            }
        } else {
            try {
                device.pull(new RemoteFile(file.getURL().getPath()), new FileOutputStream(tempFile));
            } catch (JadbException e) {
                close();
                throw new IOException(e);
            }
        }
    }

    @Override
    public int read() throws IOException {
        if (is == null) {
            is = bos != null ? new ByteArrayInputStream(bos.toByteArray()) : new FileInputStream(tempFile);
        }
        return is.read();
    }

    @Override
    public synchronized void reset() throws IOException {
        super.reset();
        if (is != null) {
            is.reset();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (is != null) {
            is.close();
        }
        if (bos != null) {
            bos.close();
        }
        if (tempFile != null) {
            tempFile.delete();
        }
    }

}