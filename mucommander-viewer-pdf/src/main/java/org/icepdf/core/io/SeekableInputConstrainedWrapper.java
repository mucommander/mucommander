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
public class SeekableInputConstrainedWrapper extends InputStream {
    private SeekableInput streamDataInput;
    private long filePositionOfStreamData;
    private long lengthOfStreamData;
    private long filePositionBeforeUse;
    private boolean usedYet;

    public SeekableInputConstrainedWrapper(
            SeekableInput in, long offset, long length) {
        streamDataInput = in;
        filePositionOfStreamData = offset;
        lengthOfStreamData = length;
        filePositionBeforeUse = 0L;
        usedYet = false;
    }


    private void ensureReadyOnFirstUse() throws IOException {
        if (usedYet)
            return;
        usedYet = true;
        filePositionBeforeUse = streamDataInput.getAbsolutePosition();
        streamDataInput.seekAbsolute(filePositionOfStreamData);
    }

    private long getBytesRemaining() throws IOException {
        long absPos = streamDataInput.getAbsolutePosition();
        if (absPos < filePositionOfStreamData)
            return -1;
        long end = filePositionOfStreamData + lengthOfStreamData;
        return end - absPos;
    }


    //
    // Methods from InputStream
    // Since Java does not have multiple inheritance, we have to
    //  explicitly expose InputStream's methods as part of our interface
    //
    public int read() throws IOException {
        ensureReadyOnFirstUse();
        long remain = getBytesRemaining();
        if (remain <= 0)
            return -1;
        return streamDataInput.read();
    }

    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
        ensureReadyOnFirstUse();
        long remain = getBytesRemaining();

        if (remain <= 0)
            return -1;
        length = (int) Math.min(Math.min(remain, (long) length), (long) Integer.MAX_VALUE);
        return streamDataInput.read(buffer, offset, length);
    }

    public int available() {
        return 0;
    }

    public void mark(int readLimit) {
    }

    public boolean markSupported() {
        return false;
    }

    public void reset() throws IOException {
    }

    public long skip(long n) throws IOException {
        ensureReadyOnFirstUse();
        long remain = getBytesRemaining();
        if (remain <= 0)
            return -1;
        n = (int) Math.min(Math.min(remain, n), (long) Integer.MAX_VALUE);
        return streamDataInput.skip(n);
    }


    //
    // Special methods that make this truly seekable
    //

    public void seekAbsolute(long absolutePosition) throws IOException {
        ensureReadyOnFirstUse();
        // The wrapper exists in a different coordinate system,
        //   where its beginning is location 0
        if (absolutePosition < 0L)
            throw new IOException("Attempt to absolutely seek to negative location: " + absolutePosition);
        // It's alright to seek beyond the end, it's just that read operations will fail
        absolutePosition += filePositionOfStreamData;
        streamDataInput.seekAbsolute(absolutePosition);
    }

    public void seekRelative(long relativeOffset) throws IOException {
        ensureReadyOnFirstUse();
        long pos = streamDataInput.getAbsolutePosition();
        pos += relativeOffset;
        if (pos < filePositionOfStreamData)
            pos = filePositionOfStreamData;
        // It's alright to seek beyond the end, it's just that read operations will fail
        streamDataInput.seekAbsolute(pos);
    }

    public void seekEnd() throws IOException {
        ensureReadyOnFirstUse();
        streamDataInput.seekAbsolute(filePositionOfStreamData + lengthOfStreamData);
    }

    public long getAbsolutePosition() throws IOException {
        ensureReadyOnFirstUse();
        long absolutePosition = getAbsolutePosition();
        absolutePosition -= filePositionOfStreamData;
        return absolutePosition;
    }

    public long getLength() {
        return lengthOfStreamData;
    }

    // To access InputStream methods, call this instead of casting
    // This InputStream has to support mark(), reset(), and obviously markSupported()
    public InputStream getInputStream() {
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(" ( ");
        sb.append("pos=").append(filePositionOfStreamData).append(", ");
        sb.append("len=").append(lengthOfStreamData).append(", ");
        sb.append("posToRestore=").append(filePositionBeforeUse).append(", ");
        sb.append(" ) ");
        sb.append(": ");
        if (streamDataInput == null)
            sb.append("null ");
        else
            sb.append(streamDataInput.toString());
        return sb.toString();
    }
}
