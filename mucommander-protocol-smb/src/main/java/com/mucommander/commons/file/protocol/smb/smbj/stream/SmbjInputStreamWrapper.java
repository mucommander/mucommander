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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class SmbjInputStreamWrapper extends InputStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmbjInputStreamWrapper.class);

    private final InputStream delegate;

    private final Closeable[] resourcesToClose;

    public SmbjInputStreamWrapper(InputStream delegate, Closeable... resourcesToClose) {
        this.delegate = delegate;
        this.resourcesToClose = resourcesToClose;
    }

    @Override
    public int read() throws IOException {
        return delegate.read();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
        if (resourcesToClose != null) {
            for (Closeable closeable : resourcesToClose) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    LOGGER.error("Error closing resource", e);
                }
            }
        }
    }
}
