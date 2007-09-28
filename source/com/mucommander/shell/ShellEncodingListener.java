/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.shell;

import com.mucommander.Debug;
import com.mucommander.process.ProcessListener;
import com.mucommander.io.EncodingDetector;
import com.mucommander.conf.impl.MuConfiguration;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

/**
 * Listens to shell output and tries to guess at its encoding.
 * @author Nicolas Rinaudo
 */
class ShellEncodingListener implements ProcessListener {
    private static ByteArrayOutputStream out = new ByteArrayOutputStream();

    public synchronized void processDied(int returnValue) {
        String encoding;
        String oldEncoding;

        // Abort if there is no need to identify the encoding anymore.
        if(out == null)
            return;

        // Attempts to guess at the encoding. If no guess can be made, ignore.
        if((encoding = EncodingDetector.detectEncoding(out.toByteArray())) == null)
            return;

        // Checks whether the detected charset is supported.
        if(Charset.isSupported(encoding)) {
            oldEncoding = MuConfiguration.getVariable(MuConfiguration.SHELL_ENCODING);

            // If no encoding was previously set, or we have found a new encoding, change the current shell encoding.
            if((oldEncoding == null) || !encoding.equals(oldEncoding))
                MuConfiguration.setVariable(MuConfiguration.SHELL_ENCODING, encoding);

            // Stop listening for new byte input if we have gathered a large enough sample set.
            if(out.size() >= EncodingDetector.MAX_RECOMMENDED_BYTE_SIZE)
                out = null;
        }
    }

    /**
     * Ignored.
     */
    public void processOutput(String output) {}

    public synchronized void processOutput(byte[] buff, int from, int len) {
        if(out != null && (len = Math.min(len, EncodingDetector.MAX_RECOMMENDED_BYTE_SIZE - out.size())) > 0)
            out.write(buff, from, len);
    }
}
