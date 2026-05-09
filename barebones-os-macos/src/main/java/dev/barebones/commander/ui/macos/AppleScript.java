/*
 * This file is part of muCommander, http://www.mucommander.com
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

package dev.barebones.commander.ui.macos;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.barebones.commander.commons.runtime.OsFamily;
import dev.barebones.commander.process.AbstractProcess;
import dev.barebones.commander.process.ProcessListener;
import dev.barebones.commander.process.ProcessRunner;

/**
 * This class allows to run AppleScript code under Mac OS X, relying on the <code>osacript</code> command available
 * that comes with any install of Mac OS X. This command is used instead of the Cocoa-Java library which has been
 * deprecated by Apple.<br/>
 * Calls to {@link #execute(String, StringBuilder)} on any OS other than Mac OS X will always fail.
 *
 * <p>
 * <b>Important notes about character encoding</b>:
 * <ul>
 *   <li>AppleScript 1.10- (Mac OS X 10.4 or lower) expects <i>MacRoman</i> encoding, not <i>UTF-8</i>. <b>That
 *       means the script should only contain characters that are part of the MacRoman charset</b>; any character
 *       that cannot be expressed in MacRoman will not be propertly interpreted.<br/>
 *       The only way to pass Unicode text to a script is by reading it from a file.
 *       See <a href="http://www.satimage.fr/software/en/unicode_and_applescript.html">http://www.satimage.fr/software/en/unicode_and_applescript.html</a>
 *       for more information on how to do so.
 *   </li>
 *   <li>AppleScript 2.0+ (Mac OS X 10.5 and up) is fully Unicode-aware and will properly interpret any Unicode
 *       character: "AppleScript is now entirely Unicode-based. Comments and text constants in scripts may contain
 *       any Unicode characters, and all text processing is done in Unicode".<br/>
 *       See <a href="http://www.apple.com/applescript/features/unicode.html">http://www.apple.com/applescript/features/unicode.html</a>
 *       for more information.
 *   </li>
 * </ul>
 * </p>
 *
 * @author Maxence Bernard
 */
public class AppleScript {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppleScript.class);
	
    /** The UTF-8 encoding */
    public final static String UTF8 = "UTF-8";

    /**
     * Executes the given AppleScript and returns <code>true</code> if it completed its execution normally, i.e. without
     * any error.
     * The script's output is accumulated in the given <code>StringBuilder</code>. If the script completed its execution
     * normally, the buffer will contain the script's standard output. If the script failed because of an error in it,
     * the buffer will contain details about the error.
     *
     * <p>If the caller is not interested in the script's output, a <code>null</code> value can be passed which will
     * speed the execution up a little.</p>
     *
     * @param appleScript the AppleScript to execute
     * @param outputBuffer the StringBuilder that will hold the script's output, <code>null</code> for no output
     * @return true if the script was successfully executed, false if the
     */
    public static boolean execute(String appleScript, StringBuilder outputBuffer) {
        // No point in going any further if the current OS is not macOS
        if(!OsFamily.MAC_OS.isCurrent())
            return false;

        LOGGER.debug("Executing AppleScript: "+appleScript);

        // Use the 'osascript' command to execute the AppleScript. The '-s o' flag tells osascript to print errors to
        // stdout rather than stderr. The AppleScript is piped to the process instead of passing it as an argument
        // ('-e' flag), for better control over the encoding and to remove any limitations on the maximum script size.
        String tokens[] = new String[] {
            "osascript",
            "-s",
            "o",
        };

        OutputStreamWriter pout = null;
        try {
            // Execute the osascript command.
            AbstractProcess process = ProcessRunner.execute(tokens, outputBuffer==null?null:new ScriptOutputListener(outputBuffer, AppleScript.getScriptEncoding()));

            // Pipe the script to the osascript process.
            pout = new OutputStreamWriter(process.getOutputStream(), getScriptEncoding());
            pout.write(appleScript);
            pout.close();

            // Wait for the process to die
            int returnCode = process.waitFor();

            LOGGER.debug("osascript returned code="+returnCode+", output="+ outputBuffer);

            if(returnCode!=0) {
            	LOGGER.debug("osascript terminated abnormally");
                return false;
            }

            return true;
        }
        catch(Exception e) {        // IOException, InterruptedException
            // Shouldn't normally happen
        	LOGGER.debug("Unexcepted exception while executing AppleScript", e);

            try {
                if(pout!=null)
                    pout.close();
            }
            catch(IOException e1) {
                // Can't do much about it
            }

            return false;
        }
    }

    /**
     * Returns the encoding that AppleScript uses on the current runtime environment:
     * <ul>
     *   <li>{@link #UTF8} for AppleScript 2.0+ (macOS 10.5 and up)</li>
     * </ul>
     *
     * If {@link #MACROMAN} is used, the scripts passed to {@link #execute(String, StringBuilder)} should not contain
     * characters that are not part of the <i>MacRoman</i> charset or they will not be properly interpreted.
     *
     * @return the encoding that AppleScript uses on the current runtime environment
     */
    public static String getScriptEncoding() {
        return UTF8;
    }


    /**
     * Streaming UTF-8 (or MacRoman) decoder for osascript's stdout.
     *
     * Two correctness constraints, both confirmed by tests:
     *
     *  1. Bytes must be appended to {@code outputBuffer} as
     *     {@code processOutput} calls arrive — the caller reads
     *     {@code outputBuffer} after {@code process.waitFor()} returns,
     *     and {@code processDied} may not have run yet by that point.
     *     Buffering everything until {@code processDied} (an earlier
     *     attempt) caused {@code AppleScriptTest.testScriptOutput} to
     *     read an empty buffer on the macOS-15 GitHub runner.
     *
     *  2. Multi-byte UTF-8 codepoints (Japanese kana = 3 bytes,
     *     emoji = 4 bytes) may straddle pipe-flush boundaries.
     *     Decoding each chunk independently as a fresh {@code String}
     *     turns the split codepoint into {@code U+FFFD}. That
     *     manifested as {@code testScriptEncoding} flaking on macOS-15.
     *
     * Both constraints are satisfied by a stateful {@link CharsetDecoder}.
     * Partial multi-byte sequences at the end of a chunk are kept in
     * {@code carryover} and prepended to the next chunk's bytes; the
     * decoder fills {@code outputBuffer} as it goes. {@code processDied}
     * just flushes any final state and trims the trailing newline.
     */
    // Package-private so AppleScriptOutputDecodingTest can drive
    // processOutput / processDied directly.
    static class ScriptOutputListener implements ProcessListener {

        private final StringBuilder outputBuffer;
        private final CharsetDecoder decoder;
        // Buffer for partial multi-byte sequences that span chunk boundaries.
        // UTF-8 codepoints are at most 4 bytes; 8 leaves headroom.
        private final ByteBuffer carryover = ByteBuffer.allocate(8);

        private ScriptOutputListener(StringBuilder outputBuffer, String outputEncoding) {
            this.outputBuffer = outputBuffer;
            this.decoder = Charset.forName(outputEncoding).newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
        }

        public synchronized void processOutput(byte[] buffer, int offset, int length) {
            ByteBuffer in;
            if (carryover.position() > 0) {
                int carrySize = carryover.position();
                ByteBuffer combined = ByteBuffer.allocate(carrySize + length);
                carryover.flip();
                combined.put(carryover);
                combined.put(buffer, offset, length);
                combined.flip();
                in = combined;
                carryover.clear();
            } else {
                in = ByteBuffer.wrap(buffer, offset, length);
            }
            CharBuffer out = CharBuffer.allocate(Math.max(256, length));
            while (true) {
                CoderResult result = decoder.decode(in, out, false);
                appendCharBuffer(out);
                if (result.isUnderflow()) {
                    if (in.hasRemaining()) {
                        carryover.put(in);
                    }
                    break;
                }
                if (result.isOverflow()) {
                    out = CharBuffer.allocate(out.capacity() * 2);
                    continue;
                }
                // Malformed / unmappable: REPLACE policy already applied
                // a U+FFFD; skip the offending byte(s) and continue.
                in.position(in.position() + result.length());
            }
        }

        private void appendCharBuffer(CharBuffer out) {
            out.flip();
            if (out.hasRemaining()) {
                outputBuffer.append(out);
            }
            out.clear();
        }

        public void processOutput(String s) {
        }

        public synchronized void processDied(int returnValue) {
            // Flush any state still held by the decoder (e.g. a final
            // partial multi-byte sequence that turned out to be invalid).
            CharBuffer out = CharBuffer.allocate(8);
            ByteBuffer in;
            if (carryover.position() > 0) {
                carryover.flip();
                in = carryover;
            } else {
                in = ByteBuffer.allocate(0);
            }
            decoder.decode(in, out, true);
            decoder.flush(out);
            appendCharBuffer(out);
            carryover.clear();

            // Strip the trailing "\n" osascript adds.
            int len = outputBuffer.length();
            if (len > 0 && outputBuffer.charAt(len - 1) == '\n') {
                outputBuffer.setLength(len - 1);
            }
        }
    }


}
