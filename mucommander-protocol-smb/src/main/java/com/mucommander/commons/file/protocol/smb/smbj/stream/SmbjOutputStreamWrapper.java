package com.mucommander.commons.file.protocol.smb.smbj.stream;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

public class SmbjOutputStreamWrapper extends OutputStream {

    private final OutputStream delegate;

    private final Closeable[] resourcesToClose;

    public SmbjOutputStreamWrapper(final OutputStream delegate, Closeable... resourcesToClose) {
        this.delegate = delegate;
        this.resourcesToClose = resourcesToClose;
    }

    @Override
    public void write(int b) throws IOException {
        this.delegate.write(b);
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
