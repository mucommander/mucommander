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



package com.mucommander.commons.file.protocol.smb.smbj.stream;

import com.hierynomus.smbj.share.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public class SmbjOutputStreamWrapper extends OutputStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmbjOutputStreamWrapper.class);

    private final OutputStream delegate;

    private final File file;

    private boolean firstWrite = true;

    public SmbjOutputStreamWrapper(final OutputStream delegate, File file) {
        this.delegate = delegate;
        this.file = file;
    }

    @Override
    public void write(int b) throws IOException {
        if (firstWrite) {
            // Truncate file on first write
            file.setLength(0);
            firstWrite = false;
        }
        this.delegate.write(b);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
        if (file != null) {
            try {
                file.close();
            } catch (Exception e) {
                LOGGER.error("Error closing resource", e);
            }
        }
    }
}
