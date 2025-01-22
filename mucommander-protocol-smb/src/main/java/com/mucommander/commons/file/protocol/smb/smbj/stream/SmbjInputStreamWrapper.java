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
