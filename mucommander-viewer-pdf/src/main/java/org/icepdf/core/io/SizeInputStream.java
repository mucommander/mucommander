/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.core.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * SizeInputStream wraps inputs streams of know lengths so that the available
 * call can return a useful number.
 *
 * @sinece 5.0.3
 */
public class SizeInputStream extends InputStream {

    private InputStream in = null;

    private int size = 0;

    private int bytesRead = 0;

    public SizeInputStream(InputStream in, int size) {
        this.in = in;
        this.size = size;
    }

    public int available() {
        return (size - bytesRead);
    }

    public int read() throws IOException {
        int b = in.read();
        if (b != -1) {
            bytesRead++;
        }
        return b;
    }

    public int read(byte[] b) throws IOException {
        int read = in.read(b);
        bytesRead += read;
        return read;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int read = in.read(b, off, len);
        bytesRead += read;
        return read;
    }
}