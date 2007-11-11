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

package com.mucommander.command;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.util.FileSet;

import junit.framework.TestCase;

import java.io.File;

/**
 * Runs tests on {@link Command}.
 * @author Nicolas Rinaudo
 */
public class CommandTest extends TestCase {
    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Used while testing keyword substitution. */
    private AbstractFile[] files;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Creates a batch of files used for testing.
     */
    public void setUp() {
        try {
            files = new AbstractFile[6];

            files[0] = FileFactory.getFile(System.getProperty("user.dir"));
            files[1] = FileFactory.getFile(System.getProperty("user.dir") + System.getProperty("file.separator") + "test.txt");
            files[2] = FileFactory.getFile(System.getProperty("user.home"));
            files[3] = FileFactory.getFile(System.getProperty("user.home") + System.getProperty("file.separator") + "test.txt");
            files[4] = FileFactory.getFile(System.getProperty("java.home"));
            files[5] = FileFactory.getFile(System.getProperty("java.home") + System.getProperty("file.separator") + "test.txt");
        }
        // This is assumed never to happen.
        catch(Exception e) {}
    }



    // - Simple keyword substitution -------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Tests the <code>$f</code> keyword.
     */
    public void testPathSubstitution() {
        String[] tokens;

        // Makes sure single file substitution works.
        tokens = Command.getTokens("$f", files[0]);
        assertEquals(1, tokens.length);
        assertEquals(files[0].getAbsolutePath(), tokens[0]);

        // Makes sure multiple file substitution works.
        tokens = Command.getTokens("$f", files);
        assertEquals(files.length, tokens.length);
        for(int i = 0; i < 3; i++)
            assertEquals(files[i].getAbsolutePath(), tokens[i]);
    }

    /**
     * Tests the <code>$p</code> keyword.
     */
    public void testParentSubstitution() {
        String[] tokens;

        // Makes sure single file substitution works.
        tokens = Command.getTokens("$p", files[0]);
        assertEquals(1, tokens.length);
        assertEquals(files[0].getParent().getAbsolutePath(), tokens[0]);

        // Makes sure multiple file substitution works.
        tokens = Command.getTokens("$p", files);
        assertEquals(files.length, tokens.length);
        for(int i = 0; i < 3; i++)
            assertEquals(files[i].getParent().getAbsolutePath(), tokens[i]);
    }

    /**
     * Returns the specified file's extension, or <code>""</code> if it doesn't have one.
     * @return the specified file's extension.
     */
    private String getExtension(AbstractFile file) {
        String ext;

        if((ext = file.getExtension()) == null)
            return "";
        return ext;
    }

    /**
     * Tests the <code>$e</code> keyword.
     */
    public void testExtensionSubstitution() {
        String[] tokens;

        // Makes sure single file substitution works (on directory).
        tokens = Command.getTokens("$e", files[0]);
        assertEquals(1, tokens.length);
        assertEquals(getExtension(files[0]), tokens[0]);

        // Makes sure single file substitution works (on file).
        tokens = Command.getTokens("$e", files[1]);
        assertEquals(1, tokens.length);
        assertEquals(getExtension(files[1]), tokens[0]);

        // Makes sure multiple file substitution works.
        tokens = Command.getTokens("$e", files);
        assertEquals(files.length, tokens.length);
        for(int i = 0; i < 3; i++)
            assertEquals(getExtension(files[i]), tokens[i]);
    }

    /**
     * Tests the <code>$b</code> keyword.
     */
    public void testBasenameSubstitution() {
        String[] tokens;

        // Makes sure single file substitution works.
        tokens = Command.getTokens("$b", files[0]);
        assertEquals(1, tokens.length);
        assertEquals(files[0].getNameWithoutExtension(), tokens[0]);

        // Makes sure multiple file substitution works.
        tokens = Command.getTokens("$b", files);
        assertEquals(files.length, tokens.length);
        for(int i = 0; i < 3; i++)
            assertEquals(files[i].getNameWithoutExtension(), tokens[i]);
    }

    /**
     * Tests the <code>$n</code> keyword.
     */
    public void testNameSubstitution() {
        String[] tokens;

        // Makes sure single file substitution works.
        tokens = Command.getTokens("$n", files[0]);
        assertEquals(1, tokens.length);
        assertEquals(files[0].getName(), tokens[0]);

        // Makes sure multiple file substitution works.
        tokens = Command.getTokens("$n", files);
        assertEquals(files.length, tokens.length);
        for(int i = 0; i < 3; i++)
            assertEquals(files[i].getName(), tokens[i]);
    }

    /**
     * Tests the <code>$j</code> keyword.
     */
    public void testCurrentDirSubstitution() {
        String[] tokens;

        // Makes sure single file substitution works.
        tokens = Command.getTokens("$j", files[0]);
        assertEquals(1, tokens.length);
        assertEquals(new File(System.getProperty("user.dir")).getAbsolutePath(), tokens[0]);

        // Makes sure multiple file substitution works.
        tokens = Command.getTokens("$j", files);
        assertEquals(1, tokens.length);
        assertEquals(new File(System.getProperty("user.dir")).getAbsolutePath(), tokens[0]);
    }



    // - Tokenisation ----------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Runs tests on parsing behaviour with illegal keywords.
     */
    public void testIllegalKeywords() {
        String[] tokens;

        // Makes sure unfinished keywords at the end of a command are kept.
        tokens = Command.getTokens("ls -la $", files);
        assertEquals(3,     tokens.length);
        assertEquals("ls",  tokens[0]);
        assertEquals("-la", tokens[1]);
        assertEquals("$",   tokens[2]);

        // Makes sure illegal keywords are not replaced.
        tokens = Command.getTokens("ls $a");
        assertEquals(2,    tokens.length);
        assertEquals("ls", tokens[0]);
        assertEquals("$a", tokens[1]);

        // Makes sure unfinished keywords are not replaced.
        tokens = Command.getTokens("ls $ la");
        assertEquals(3,    tokens.length);
        assertEquals("ls", tokens[0]);
        assertEquals("$",  tokens[1]);
        assertEquals("la", tokens[2]);
    }

    /**
     * Runs tests on command parsing (with keyword substitution).
     */
    public void testParsingWithSubstitution() {
        String[]     tokens;
        StringBuffer buffer;

        // Makes sure keywords are tokenised when not escaped.
        tokens = Command.getTokens("ls $f", files);
        assertEquals(1 + files.length, tokens.length);
        assertEquals("ls", tokens[0]);
        for(int i = 0; i < files.length; i++)
            assertEquals(tokens[i + 1], files[i].getAbsolutePath());

        // Makes sure keywords are not tokenised when escaped.
        tokens = Command.getTokens("ls \"$f\"", files);
        buffer = new StringBuffer("\"");
        buffer.append(files[0].getAbsolutePath());
        for(int i = 1; i < files.length; i++) {
            buffer.append(' ');
            buffer.append(files[i].getAbsolutePath());
        }
        buffer.append("\"");
        assertEquals(2, tokens.length);
        assertEquals("ls", tokens[0]);
        assertEquals(buffer.toString(), tokens[1]);
        
        // Makes sure that keyword substitution happens even if the keyword
        // is not a single token.
        tokens = Command.getTokens("ls$fla", files[0]);
        assertEquals(1, tokens.length);
        assertEquals("ls" + files[0].getAbsolutePath() + "la", tokens[0]);

        tokens = Command.getTokens("ls$fla", files);
        assertEquals(files.length, tokens.length);
        assertEquals("ls" + files[0].getAbsolutePath(), tokens[0]);
        for(int i = 1; i < files.length - 1; i++)
            assertEquals(files[i].getAbsolutePath(), tokens[i]);
        assertEquals(files[files.length - 1].getAbsolutePath() + "la", tokens[tokens.length - 1]);
    }

    /**
     * Runs tests on command parsing (without keyword substitution).
     */
    public void testParsingWithoutSubstitution() {
        String[] tokens;

        // Makes sure simple command parsing works.
        tokens = Command.getTokens("ls -la");
        assertEquals(2,     tokens.length);
        assertEquals("ls",  tokens[0]);
        assertEquals("-la", tokens[1]);

        // Makes sure spaces are trimmed when they're expected to.
        tokens = Command.getTokens("ls     -la     ");
        assertEquals(2,     tokens.length);
        assertEquals("ls",  tokens[0]);
        assertEquals("-la", tokens[1]);

        // Makes sure quotes:
        // - escape spaces.
        // - are not removed from the command.
        tokens = Command.getTokens("ls \"- l a\"");
        assertEquals(2,           tokens.length);
        assertEquals("ls",        tokens[0]);
        assertEquals("\"- l a\"", tokens[1]);

        // Makes sure spaces are not trimmed when they're not expected to.
        tokens = Command.getTokens("ls \"-    l    a   \"");
        assertEquals(2,                    tokens.length);
        assertEquals("ls",                 tokens[0]);
        assertEquals("\"-    l    a   \"", tokens[1]);

        // Makes sure \s:
        // - escape quotes.
        // - are removed from the command.
        tokens = Command.getTokens("ls \\\"- l a");
        assertEquals(4,     tokens.length);
        assertEquals("ls",  tokens[0]);
        assertEquals("\"-", tokens[1]);
        assertEquals("l",   tokens[2]);
        assertEquals("a",   tokens[3]);

        // Makes sure \s:
        // - escape spaces.
        // - are remoed from the command.
        tokens = Command.getTokens("ls My\\ Documents");
        assertEquals(2,              tokens.length);
        assertEquals("ls",           tokens[0]);
        assertEquals("My Documents", tokens[1]);

        // Makes sure 'complex' tokenisation works.
        tokens = Command.getTokens("/usr/bin/find . -name \\\\*.java -exec sh -c \"echo {}; wc {}\" \\\\;");
        assertEquals(9,                    tokens.length);
        assertEquals("/usr/bin/find",      tokens[0]);
        assertEquals(".",                  tokens[1]);
        assertEquals("-name",              tokens[2]);
        assertEquals("\\*.java",           tokens[3]);
        assertEquals("-exec",              tokens[4]);
        assertEquals("sh",                 tokens[5]);
        assertEquals("-c",                 tokens[6]);
        assertEquals("\"echo {}; wc {}\"", tokens[7]);
        assertEquals("\\;",                tokens[8]);
    }
}
