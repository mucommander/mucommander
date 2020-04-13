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
package org.icepdf.core.pobjects.filters;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Mark Collette
 * @since 2.0
 */
public class RunLengthDecode extends ChunkingInputStream {
    public RunLengthDecode(InputStream input) {
        super();

        setInputStream(input);
        setBufferSize(4096);
    }

    protected int fillInternalBuffer() throws IOException {
        int numRead = 0;

        while (numRead < (buffer.length - 260)) { // && i != 128) {
            int i = in.read();
            if (i < 0)
                break;
            if (i < 128) {
                numRead += fillBufferFromInputStream(numRead, i + 1);
            } else {
                int count = (257 - i);
                int j = in.read();
                byte jj = (byte) (j & 0xFF);
                for (int k = 0; k < count; k++) {
                    buffer[numRead++] = jj;
                }
            }
        }

        if (numRead == 0)
            return -1;
        return numRead;
    }
}
