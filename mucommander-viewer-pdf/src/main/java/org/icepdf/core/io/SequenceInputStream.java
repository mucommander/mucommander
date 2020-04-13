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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Mark Collette
 * @since 2.0
 */
public class SequenceInputStream extends InputStream {
    private Iterator<InputStream> m_itInputStreams;
    private InputStream m_isCurrent;

    public SequenceInputStream(InputStream... in) {
        this(Arrays.asList(in));
    }

    public SequenceInputStream(List<InputStream> inputStreams) {
        this(inputStreams, -1);
    }

    public SequenceInputStream(List<InputStream> inputStreams, int streamSwitchValue) {
        List<InputStream> in = new ArrayList<InputStream>();
        for (int i = 0; i < inputStreams.size(); i++) {
            if (i > 0 && streamSwitchValue != -1) {
                in.add(new ByteArrayInputStream(new byte[]{(byte) streamSwitchValue}));
            }
            in.add(inputStreams.get(i));
        }
        m_itInputStreams = in.iterator();

        try {
            useNextInputStream();
        } catch (IOException e) {
            throw new java.lang.IllegalStateException("Could not use first InputStream in SequenceInputStream(List) : " + e);
        }
    }

    private InputStream getCurrentInputStream() {
        return m_isCurrent;
    }

    private void useNextInputStream() throws IOException {
        closeCurrentInputStream();

        m_isCurrent = null;
        while (m_itInputStreams.hasNext()) {
            InputStream in = m_itInputStreams.next();
            if (in != null) {
                m_isCurrent = in;
                break;
            }
        }
    }

    private void closeCurrentInputStream() throws IOException {
        InputStream in = getCurrentInputStream();
        if (in != null)
            in.close();
    }


    public int available() throws IOException {
        InputStream in = getCurrentInputStream();
        if (in != null)
            return in.available();
        return 0;
    }

    public int read() throws IOException {
        while (true) {
            InputStream in = getCurrentInputStream();
            if (in == null) {
                useNextInputStream();
                in = getCurrentInputStream();
                if (in == null)
                    return -1;
            }

            int readByte = in.read();
            if (readByte >= 0)
                return readByte;
            useNextInputStream();
        }
    }

    public int read(byte buffer[], int off, int len) throws IOException {
        if (buffer == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off >= buffer.length) ||
                (len < 0) || ((off + len) > buffer.length) ||
                ((off + len) < 0)) {
            throw new IndexOutOfBoundsException("Offset: " + off + ", Length: " + len + ", Buffer length: " + buffer.length);
        } else if (len == 0)
            return 0;

        int totalRead = 0;
        while (totalRead < len) {
            InputStream in = getCurrentInputStream();
            if (in == null) {
                useNextInputStream();
                in = getCurrentInputStream();
                if (in == null) {
                    if (totalRead > 0) {
                        break;
                    }
                    return -1;
                }
            }

            int currRead = in.read(buffer, off + totalRead, len - totalRead);
            if (currRead > 0) {
                totalRead += currRead;
            } else {
                useNextInputStream();
            }
        }

        return totalRead;
    }

    public void close() throws IOException {
        do {
            useNextInputStream();
        } while (getCurrentInputStream() != null);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append(": ");

        List<InputStream> inputStreams = new ArrayList<InputStream>();
        while (m_itInputStreams.hasNext()) {
            InputStream in = m_itInputStreams.next();
            sb.append("\n  ");
            sb.append(in.toString());
            sb.append(",");
            inputStreams.add(in);
        }
        m_itInputStreams = inputStreams.iterator();

        sb.append('\n');
        return sb.toString();
    }
}
