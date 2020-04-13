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

import org.icepdf.core.util.Parser;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Mark Collette
 * @since 2.0
 */
public class ASCIIHexDecode extends ChunkingInputStream {
    public ASCIIHexDecode(InputStream input) {
        super();

        setInputStream(input);
        setBufferSize(4096);
    }

    protected int fillInternalBuffer() throws IOException {
        int numRead = 0;

        for (int i = 0; i < buffer.length; i++) {
            byte val = 0;
            int hi;
            int lo;
            do {
                hi = in.read();
            } while (Parser.isWhitespace((char) hi));
            if (hi < 0)
                break;
            do {
                lo = in.read();
            } while (Parser.isWhitespace((char) lo));

            if (hi >= '0' && hi <= '9') {
                hi -= '0';
                val |= ((byte) ((hi << 4) & 0xF0));
            } else if (hi >= 'a' && hi <= 'z') {
                hi = hi - 'a' + 10;
                val |= ((byte) ((hi << 4) & 0xF0));
            } else if (hi >= 'A' && hi <= 'Z') {
                hi = hi - 'A' + 10;
                val |= ((byte) ((hi << 4) & 0xF0));
            }

            if (lo >= 0) {
                if (lo >= '0' && lo <= '9') {
                    lo -= '0';
                    val |= ((byte) (lo & 0x0F));
                } else if (lo >= 'a' && lo <= 'z') {
                    lo = lo - 'a' + 10;
                    val |= ((byte) (lo & 0x0F));
                } else if (lo >= 'A' && lo <= 'Z') {
                    lo = lo - 'A' + 10;
                    val |= ((byte) (lo & 0x0F));
                }
            }
            buffer[numRead++] = val;
        }

        if (numRead == 0)
            return -1;
        return numRead;
    }
}
