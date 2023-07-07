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
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.io.BufferPool;
import com.mucommander.commons.util.CircularByteBuffer;

import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.RemoteFile;

/**
 * @author Oleg Trifonov, Arik Hadas
 * Created on 29/12/15.
 */
public class AdbInputStream extends InputStream {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdbInputStream.class);

    private InputStream inputStream;

    AdbInputStream(AdbFile file) throws IOException {
        JadbDevice device = file.getDevice(file.getURL());
        if (device == null) {
            throw new IOException("file not found: " + file.getURL());
        }

        CircularByteBuffer cbb = new CircularByteBuffer(BufferPool.getDefaultBufferSize());
        new Thread(() -> {
            var out = cbb.getOutputStream();
            try {
                device.pull(new RemoteFile(file.getURL().getPath()), out);
            } catch (IOException | JadbException e) {
                LOGGER.error("failed to read from adb path %s", file.getURL());
                LOGGER.debug("failed to read from adb path", e);
            }
            try {
                out.close();
            } catch (IOException e) {
                LOGGER.error("failed to close output stream when writing to adb path %s", file.getURL());
                LOGGER.debug("failed to close output stream when writing to adb path", e);
            }
        }).start();
        this.inputStream = cbb.getInputStream();
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

}