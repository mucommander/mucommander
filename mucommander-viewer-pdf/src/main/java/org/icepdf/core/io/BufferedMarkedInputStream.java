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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * BufferedMarkedInputStream extends BufferedInputStream and keeps count for the
 * total number bytes read between buffer fills.  This class is used by the
 * linearTraversal loading mechanism so that the byte offset of found objects
 * can be stored.  The offset is then used to correct the cross reference
 * table object offset.
 *
 * @since 5.0
 */
public class BufferedMarkedInputStream extends BufferedInputStream {

    private int fillCount;

    public BufferedMarkedInputStream(InputStream in) {
        super(in);
    }

    public BufferedMarkedInputStream(InputStream in, int size) {
        super(in, size);
    }

    public int getMarkedPosition() {
        return fillCount;
    }

    @Override
    public int read() throws IOException {
        fillCount++;
        return super.read();
    }

    @Override
    public synchronized void reset() throws IOException {
        if (markpos > 0)
            fillCount -= (pos - markpos);
        super.reset();
    }
}
