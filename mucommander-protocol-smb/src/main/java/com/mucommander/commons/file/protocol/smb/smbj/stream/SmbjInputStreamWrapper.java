package com.mucommander.commons.file.protocol.smb.smbj.stream;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class SmbjInputStreamWrapper extends InputStream {

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
        super.close();
        if (resourcesToClose != null) {
            for (Closeable closeable : resourcesToClose) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    e.printStackTrace(); // TODO - log
                }
            }
        }
    }
}
