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
 * @author Mark Collette
 * @since 2.0
 */
public interface SeekableInput {
    //
    // Methods from InputStream
    // Since Java does not have multiple inheritance, we have to
    //  explicitly expose InputStream's methods as part of our interface
    //
    public int read() throws IOException;

    public int read(byte[] buffer) throws IOException;

    public int read(byte[] buffer, int offset, int length) throws IOException;

    public void close() throws IOException;

    public int available();

    public void mark(int readLimit);

    public boolean markSupported();

    public void reset() throws IOException;

    public long skip(long n) throws IOException;


    //
    // Special methods that make this truly seekable
    //

    public void seekAbsolute(long absolutePosition) throws IOException;

    public void seekRelative(long relativeOffset) throws IOException;

    public void seekEnd() throws IOException;

    public long getAbsolutePosition() throws IOException;

    public long getLength() throws IOException;

    // To access InputStream methods, call this instead of casting
    // This InputStream has to support mark(), reset(), and obviously markSupported()
    public InputStream getInputStream();


    //
    // For regulating competing Threads' access to our state and I/O
    //

    public void beginThreadAccess();

    public void endThreadAccess();
}
