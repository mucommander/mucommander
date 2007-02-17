package com.mucommander.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * SinkOutputStream is an OutputStream which implements write() methods as no-ops, loosing data as it get written,
 * similarily to UNIX /dev/null.  
 *
 * @author Maxence Bernard
 */
public class SinkOutputStream extends OutputStream {

    public void write(int i) throws IOException {
    }

    /**
     * Overridden for performance reasons.
     */
    public void write(byte[] bytes) throws IOException {
    }

    /**
     * Overridden for performance reasons.
     */
    public void write(byte[] bytes, int off, int len) throws IOException {
    }
}
