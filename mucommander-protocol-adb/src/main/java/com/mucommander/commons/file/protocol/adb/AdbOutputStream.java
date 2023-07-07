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

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.io.BufferPool;
import com.mucommander.commons.util.CircularByteBuffer;

import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.RemoteFile;

/**
 * An output stream for writing to an {@link AdbFile}
 * @author Arik Hadas
 */
public class AdbOutputStream extends OutputStream {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdbOutputStream.class);

    private OutputStream outputStream;
    private int fileMode = 0644;

    public AdbOutputStream(AdbFile file) throws IOException {
        JadbDevice device = file.getDevice(file.getURL());
        if (device == null) {
            throw new IOException("file not found: " + file.getURL());
        }

        CircularByteBuffer cbb = new CircularByteBuffer(BufferPool.getDefaultBufferSize());
        new Thread(() -> {
            var in = cbb.getInputStream();
            try {
                device.push(in, System.currentTimeMillis() / 1000, fileMode, new RemoteFile(file.getURL().getPath()));
            } catch (IOException | JadbException e) {
                LOGGER.error("failed to write to adb path %s", file.getURL());
                LOGGER.debug("failed to write to adb path", e);
            }
            try {
                in.close();
            } catch (IOException e) {
                LOGGER.error("failed to close input stream when writing to adb path %s", file.getURL());
                LOGGER.debug("failed to close input stream when writing to adb path", e);
            }
        }).start();
        this.outputStream = cbb.getOutputStream();
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
}
