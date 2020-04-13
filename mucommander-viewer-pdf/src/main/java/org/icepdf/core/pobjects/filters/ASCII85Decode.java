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

public class ASCII85Decode extends ChunkingInputStream {
    private boolean eof = false;

    public ASCII85Decode(InputStream input) {
        super();

        setInputStream(input);
        setBufferSize(4);
    }

    protected int fillInternalBuffer() throws IOException {
        if (eof)
            return -1;
        long value = 0;
        int count = 0;
        long c = 0;
        while (true) {
            c = in.read();
            if (c < 0) {
                eof = true;
                break;
            }
            if (c == 0x00 || c == 0x09 || c == 0x0a || c == 0x0c || c == 0x0d || c == 0x20)
                continue;
            if (c == 126) { // '~'
                eof = true;
                break;
            }
            if (c == 122) { // 'z'
                buffer[0] = 0;
                buffer[1] = 0;
                buffer[2] = 0;
                buffer[3] = 0;
                count = 0;
                return 4;
            }
            count++;
            value = value * 85 + (c - 33);
            if (count == 5) {
                buffer[0] = (byte) ((value >> 24) & 0xFF);
                buffer[1] = (byte) ((value >> 16) & 0xFF);
                buffer[2] = (byte) ((value >> 8) & 0xFF);
                buffer[3] = (byte) (value & 0xFF);
                value = 0;
                count = 0;
                return 4;
            }
        }
        if (count == 2) {
            value = value * (85L * 85 * 85) + 0xFFFFFF;
        } else if (count == 3) {
            value = value * (85L * 85) + 0xFFFF;
        } else if (count == 4) {
            value = value * (85L) + 0xFF;
        }
        if (count >= 2)
            buffer[0] = (byte) ((value >> 24) & 0xFF);
        if (count >= 3)
            buffer[1] = (byte) ((value >> 16) & 0xFF);
        if (count >= 4)
            buffer[2] = (byte) ((value >> 8) & 0xFF);
        return count - 1;
    }
}
