/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.ui.macos;

import org.testng.annotations.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

import static org.testng.Assert.assertEquals;

/**
 * Regression test for the AppleScript stdout decoding fix.
 *
 * Before: {@code ScriptOutputListener.processOutput(byte[], int, int)}
 * decoded each chunk independently — multi-byte UTF-8 codepoints
 * straddling a chunk boundary became U+FFFD replacement characters.
 * Manifested as {@code AppleScriptTest.testScriptEncoding} flaking on
 * the macOS-15 GitHub runner where pipe-buffer flushes are timing-
 * dependent.
 *
 * After: bytes accumulate in a {@code ByteArrayOutputStream}; decoded
 * once on {@code processDied}. This test feeds the listener Japanese
 * text split BETWEEN bytes of the same codepoint and asserts the
 * round-trip is exact.
 */
public class AppleScriptOutputDecodingTest {

    private static AppleScript.ScriptOutputListener newListener(StringBuilder out) throws Exception {
        // ScriptOutputListener is package-private; constructor is private.
        // Reflectively construct it.
        Constructor<AppleScript.ScriptOutputListener> ctor =
            AppleScript.ScriptOutputListener.class.getDeclaredConstructor(
                StringBuilder.class, String.class);
        ctor.setAccessible(true);
        try {
            return ctor.newInstance(out, "UTF-8");
        } catch (InvocationTargetException e) {
            throw e.getCause() instanceof Exception ex ? ex : new RuntimeException(e);
        }
    }

    @Test
    public void singleChunkRoundTrips() throws Exception {
        StringBuilder out = new StringBuilder();
        AppleScript.ScriptOutputListener listener = newListener(out);

        String text = "どうもありがとうミスターロボット\n";
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        listener.processOutput(bytes, 0, bytes.length);
        listener.processDied(0);

        // The listener strips the trailing newline (matches osascript's
        // post-process behaviour).
        assertEquals(out.toString(), "どうもありがとうミスターロボット");
    }

    /**
     * The actual bug-shape: each Japanese kana is 3 UTF-8 bytes; split
     * the input mid-codepoint and confirm the full text still decodes
     * correctly.
     */
    @Test
    public void chunkSplitMidCodepointStillDecodesCleanly() throws Exception {
        StringBuilder out = new StringBuilder();
        AppleScript.ScriptOutputListener listener = newListener(out);

        String text = "どうもありがとうミスターロボット";
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        // Cut squarely between byte 1 and byte 2 of the FIRST kana ('ど' = E3 81 A9).
        listener.processOutput(bytes, 0, 1);
        listener.processOutput(bytes, 1, bytes.length - 1);
        listener.processDied(0);

        assertEquals(out.toString(), text,
            "Splitting a 3-byte UTF-8 codepoint across two processOutput calls " +
            "must not corrupt the decoded string. If this fails, the listener is " +
            "decoding chunks independently again and the AppleScriptTest macOS-15 " +
            "flake is back.");
    }

    /**
     * Many small chunks, including ones that land between bytes 2 and 3 of
     * the same codepoint. Worst-case the old implementation would have
     * produced TWO replacement characters per split codepoint.
     */
    @Test
    public void manyTinyChunksAcrossEveryByteBoundary() throws Exception {
        StringBuilder out = new StringBuilder();
        AppleScript.ScriptOutputListener listener = newListener(out);

        String text = "どうもありがとうミスターロボット";
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < bytes.length; i++) {
            listener.processOutput(bytes, i, 1);
        }
        listener.processDied(0);

        assertEquals(out.toString(), text);
    }

    @Test
    public void asciiInputIsUnchanged() throws Exception {
        StringBuilder out = new StringBuilder();
        AppleScript.ScriptOutputListener listener = newListener(out);

        byte[] bytes = "hello world\n".getBytes(StandardCharsets.UTF_8);
        listener.processOutput(bytes, 0, bytes.length);
        listener.processDied(0);

        assertEquals(out.toString(), "hello world");
    }

    @Test
    public void emptyOutputProducesEmptyString() throws Exception {
        StringBuilder out = new StringBuilder();
        AppleScript.ScriptOutputListener listener = newListener(out);

        listener.processDied(0);

        assertEquals(out.toString(), "");
    }

    /**
     * Regression test for the SECOND bug uncovered by Phase-11
     * macOS-15 CI runs of {@code AppleScriptTest.testScriptOutput}.
     *
     * The earlier byte-buffer-and-decode-on-processDied attempt left
     * {@code outputBuffer} empty until {@code processDied} ran. But
     * {@code AppleScript.execute} reads {@code outputBuffer} after
     * {@code process.waitFor()} returns, and {@code processDied}
     * (called from a separate I/O thread) may not have run by that
     * point — race window where the caller reads "".
     *
     * The streaming-decoder rewrite must flush bytes to the buffer
     * as {@code processOutput} arrives. If this test fails, the
     * "buffer everything until processDied" anti-pattern is back
     * and {@code testScriptOutput} will flake on slow runners again.
     */
    @Test
    public void processOutputFlushesImmediatelyDoesNotWaitForProcessDied() throws Exception {
        StringBuilder out = new StringBuilder();
        AppleScript.ScriptOutputListener listener = newListener(out);

        byte[] bytes = "6\n".getBytes(StandardCharsets.UTF_8);
        listener.processOutput(bytes, 0, bytes.length);

        // Caller reads outputBuffer BEFORE processDied. Must already
        // contain the decoded bytes.
        assertEquals(out.toString(), "6\n",
            "processOutput must append to outputBuffer immediately, " +
            "not wait for processDied. If empty: " +
            "AppleScriptTest.testScriptOutput will flake.");

        // processDied later strips the trailing newline.
        listener.processDied(0);
        assertEquals(out.toString(), "6");
    }
}
