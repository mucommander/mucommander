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
import java.io.OutputStream;

/**
 * Keeps track of how many bytes have been written out
 *
 * @since 4.0
 */

public class CountingOutputStream extends OutputStream {
    private OutputStream wrapped;
    private long count;

    public CountingOutputStream(OutputStream wrap) {
        wrapped = wrap;
        count = 0L;
    }

    public long getCount() {
        return count;
    }

    public void write(int i) throws IOException {
        wrapped.write(i);
        count++;
    }

    public void write(byte[] bytes) throws IOException {
        wrapped.write(bytes);
        count += bytes.length;
    }

    public void write(byte[] bytes, int offset, int len) throws IOException {
        wrapped.write(bytes, offset, len);
        int num = Math.min(len, bytes.length - offset);
        count += num;
    }

    public void flush() throws IOException {
        wrapped.flush();
    }

    public void close() throws IOException {
        wrapped.close();
    }
}
