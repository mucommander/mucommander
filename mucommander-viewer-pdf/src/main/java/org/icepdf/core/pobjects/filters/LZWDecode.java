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

import org.icepdf.core.io.BitStream;
import org.icepdf.core.pobjects.Name;
import org.icepdf.core.util.Library;

import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

/**
 * @author Mark Collette
 * @since 2.0
 */
public class LZWDecode extends ChunkingInputStream {

    public static final Name DECODEPARMS_KEY = new Name("DecodeParms");
    public static final Name EARLYCHANGE_KEY = new Name("EarlyChange");


    private BitStream inb;
    private int earlyChange;
    private int code;
    private int old_code;
    private boolean firstTime;

    private int code_len;
    private int last_code;
    private Code[] codes;


    public LZWDecode(BitStream inb, Library library, HashMap entries) {
        this.inb = inb;

        this.earlyChange = 1; // Default value
        HashMap decodeParmsDictionary = library.getDictionary(entries, DECODEPARMS_KEY);
        if (decodeParmsDictionary != null) {
            Number earlyChangeNumber = library.getNumber(decodeParmsDictionary, EARLYCHANGE_KEY);
            if (earlyChangeNumber != null) {
                this.earlyChange = earlyChangeNumber.intValue();
            }
        }

        code = 0;
        old_code = 0;
        firstTime = true;
        initCodeTable();
        setBufferSize(4096);
    }

    protected int fillInternalBuffer() throws IOException {
        int numRead = 0;
        // start decompression,  haven't tried to optimized this one yet for
        // speed or for memory.

        if (firstTime) {
            firstTime = false;
            old_code = code = inb.getBits(code_len);
        } else if (inb.atEndOfFile())
            return -1;

        do {
            if (code == 256) {
                initCodeTable();
            } else if (code == 257) {
                break;
            } else {
                if (codes[code] != null) {
                    Stack stack = new Stack();
                    codes[code].getString(stack);
                    Code c = (Code) stack.pop();
                    addToBuffer(c.c, numRead);
                    numRead++;
                    //System.err.println((char)c.c);
                    byte first = c.c;
                    while (!stack.empty()) {
                        c = (Code) stack.pop();
                        addToBuffer(c.c, numRead);
                        numRead++;
                        //System.err.println((char)c.c);
                    }
                    //							while (codes[last_code]!=null) last_code++;
                    codes[last_code++] = new Code(codes[old_code], first);
                } else {
                    //System.err.println("MISS: "+last_code+" "+code);
                    if (code != last_code)
                        throw new RuntimeException("LZWDecode failure");
                    Stack stack = new Stack();
                    codes[old_code].getString(stack);
                    Code c = (Code) stack.pop();
                    addToBuffer(c.c, numRead);
                    numRead++;
                    //System.err.println((char)c.c);
                    byte first = c.c;
                    while (!stack.empty()) {
                        c = (Code) stack.pop();
                        addToBuffer(c.c, numRead);
                        numRead++;
                        //System.err.println((char)c.c);
                    }
                    addToBuffer(first, numRead);
                    numRead++;
                    codes[code] = new Code(codes[old_code], first);
                    last_code++;
                }
            }
            if (code_len < 12 && last_code == (1 << code_len) - earlyChange) {
                //System.err.println(last_code+" "+code_len);
                code_len++;
            }
            old_code = code;
            code = inb.getBits(code_len);

            if (inb.atEndOfFile())
                break;
        } while (numRead < buffer.length);

        return numRead;
    }

    private void initCodeTable() {
        code_len = 9;
        last_code = 257;
        codes = new Code[4096];
        for (int i = 0; i < 256; i++)
            codes[i] = new Code(null, (byte) i);
    }

    private void addToBuffer(byte b, int offset) {
        if (offset >= buffer.length) { // Should never happen
            byte[] bufferNew = new byte[buffer.length * 2];
            System.arraycopy(buffer, 0, bufferNew, 0, buffer.length);
            buffer = bufferNew;
        }
        buffer[offset] = b;
    }


    public void close() throws IOException {
        super.close();

        if (inb != null) {
            inb.close();
            inb = null;
        }
    }


    /**
     * Utility class for decode methods.
     */
    private static class Code {
        Code prefix;
        byte c;

        Code(Code p, byte cc) {
            prefix = p;
            c = cc;
        }

        @SuppressWarnings("unchecked")
        void getString(Stack s) {
            s.push(this);
            if (prefix != null)
                prefix.getString(s);
        }
    }
}
