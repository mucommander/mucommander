/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Runs tests on {@link Command}.
 * @author Nicolas Rinaudo
 */
public class CommandTest {
    // - Constants -------------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Test command's alias. */
    private static final String ALIAS        = "alias";
    /** Test command's command. */
    private static final String COMMAND      = "ls -la";
    /** Test command's display name. */
    private static final String DISPLAY_NAME = "test";



    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Used while testing keyword substitution. */
    private AbstractFile[] files;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Creates a batch of files used for testing.
     */
    @BeforeClass
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
    @Test
    public void testPathSubstitution() {
        String[] tokens;

        // Makes sure single file substitution works.
        tokens = Command.getTokens("$f", files[0]);
        assert 1 == tokens.length;
        assert files[0].getAbsolutePath().equals(tokens[0]);

        // Makes sure multiple file substitution works.
        tokens = Command.getTokens("$f", files);
        assert files.length == tokens.length;
        for(int i = 0; i < 3; i++)
            assert files[i].getAbsolutePath().equals(tokens[i]);
    }

    /**
     * Returns the specified file's parent, or an empty string if it doesn't have one.
     * @param  file file whose parent should be returned.
     * @return      the specified file's parent, or an empty string if it doesn't have one.
     */
    private String getParent(AbstractFile file) {
        AbstractFile parent;

        if((parent = file.getParent()) == null)
            return "";
        return parent.getAbsolutePath();
    }

    /**
     * Tests the <code>$p</code> keyword.
     */
    @Test
    public void testParentSubstitution() {
        String[] tokens;

        // Makes sure single file substitution works.
        tokens = Command.getTokens("$p", files[0]);
        assert 1 == tokens.length;
        assert getParent(files[0]).equals(tokens[0]);

        // Makes sure multiple file substitution works.
        tokens = Command.getTokens("$p", files);
        assert files.length == tokens.length;
        for(int i = 0; i < 3; i++)
            assert getParent(files[i]).equals(tokens[i]);
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
    @Test
    public void testExtensionSubstitution() {
        String[] tokens;

        // Makes sure single file substitution works (on directory).
        tokens = Command.getTokens("$e", files[0]);
        assert 1 == tokens.length;
        assert getExtension(files[0]).equals(tokens[0]);

        // Makes sure single file substitution works (on file).
        tokens = Command.getTokens("$e", files[1]);
        assert 1 == tokens.length;
        assert getExtension(files[1]).equals(tokens[0]);

        // Makes sure multiple file substitution works.
        tokens = Command.getTokens("$e", files);
        assert files.length == tokens.length;
        for(int i = 0; i < 3; i++)
            assert getExtension(files[i]).equals(tokens[i]);
    }

    /**
     * Tests the <code>$b</code> keyword.
     */
    @Test
    public void testBasenameSubstitution() {
        String[] tokens;

        // Makes sure single file substitution works.
        tokens = Command.getTokens("$b", files[0]);
        assert 1 == tokens.length;
        assert files[0].getNameWithoutExtension().equals(tokens[0]);

        // Makes sure multiple file substitution works.
        tokens = Command.getTokens("$b", files);
        assert files.length == tokens.length;
        for(int i = 0; i < 3; i++)
            assert files[i].getNameWithoutExtension().equals(tokens[i]);
    }

    /**
     * Tests the <code>$n</code> keyword.
     */
    @Test
    public void testNameSubstitution() {
        String[] tokens;

        // Makes sure single file substitution works.
        tokens = Command.getTokens("$n", files[0]);
        assert 1 == tokens.length;
        assert files[0].getName().equals(tokens[0]);

        // Makes sure multiple file substitution works.
        tokens = Command.getTokens("$n", files);
        assert files.length == tokens.length;
        for(int i = 0; i < 3; i++)
            assert files[i].getName().equals(tokens[i]);
    }

    /**
     * Tests the <code>$j</code> keyword.
     */
    @Test
    public void testCurrentDirSubstitution() {
        String[] tokens;

        // Makes sure single file substitution works.
        tokens = Command.getTokens("$j", files[0]);
        assert 1 == tokens.length;
        assert new File(System.getProperty("user.dir")).getAbsolutePath().equals(tokens[0]);

        // Makes sure multiple file substitution works.
        tokens = Command.getTokens("$j", files);
        assert 1 == tokens.length;
        assert new File(System.getProperty("user.dir")).getAbsolutePath().equals(tokens[0]);
    }



    // - Tokenisation ----------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Runs tests on parsing behaviour with illegal keywords.
     */
    @Test
    public void testIllegalKeywords() {
        String[] tokens;

        // Makes sure unfinished keywords at the end of a command are kept.
        tokens = Command.getTokens("ls -la $", files);
        assert 3 == tokens.length;
        assert "ls".equals(tokens[0]);
        assert "-la".equals(tokens[1]);
        assert "$".equals(tokens[2]);

        // Makes sure illegal keywords are not replaced.
        tokens = Command.getTokens("ls $a");
        assert 2 == tokens.length;
        assert "ls".equals(tokens[0]);
        assert "$a".equals(tokens[1]);

        // Makes sure unfinished keywords are not replaced.
        tokens = Command.getTokens("ls $ la");
        assert 3 == tokens.length;
        assert "ls".equals(tokens[0]);
        assert "$".equals(tokens[1]);
        assert "la".equals(tokens[2]);
    }

    /**
     * Runs tests on command parsing (with keyword substitution).
     */
    @Test
    public void testParsingWithSubstitution() {
        String[]      tokens;
        StringBuilder buffer;

        // Makes sure keywords are tokenised when not escaped.
        tokens = Command.getTokens("ls $f", files);
        assert 1 + files.length == tokens.length;
        assert "ls".equals(tokens[0]);
        for(int i = 0; i < files.length; i++)
            assert tokens[i + 1].equals(files[i].getAbsolutePath());

        // Makes sure keywords are not tokenised when escaped.
        tokens = Command.getTokens("ls \"$f\"", files);
        buffer = new StringBuilder("\"");
        buffer.append(files[0].getAbsolutePath());
        for(int i = 1; i < files.length; i++) {
            buffer.append(' ');
            buffer.append(files[i].getAbsolutePath());
        }
        buffer.append("\"");
        assert 2 == tokens.length;
        assert "ls".equals(tokens[0]);
        assert buffer.toString().equals(tokens[1]);
        
        // Makes sure that keyword substitution happens even if the keyword
        // is not a single token.
        tokens = Command.getTokens("ls$fla", files[0]);
        assert 1 == tokens.length;
        assert ("ls" + files[0].getAbsolutePath() + "la").equals(tokens[0]);

        tokens = Command.getTokens("ls$fla", files);
        assert files.length == tokens.length;
        assert ("ls" + files[0].getAbsolutePath()).equals(tokens[0]);
        for(int i = 1; i < files.length - 1; i++)
            assert files[i].getAbsolutePath().equals(tokens[i]);
        assert (files[files.length - 1].getAbsolutePath() + "la").equals(tokens[tokens.length - 1]);
    }

    /**
     * Runs tests on command parsing (without keyword substitution).
     */
    @Test
    public void testParsingWithoutSubstitution() {
        String[] tokens;

        // Makes sure simple command parsing works.
        tokens = Command.getTokens("ls -la");
        assert 2 == tokens.length;
        assert "ls".equals(tokens[0]);
        assert "-la".equals(tokens[1]);

        // Makes sure spaces are trimmed when they're expected to.
        tokens = Command.getTokens("ls     -la     ");
        assert 2 == tokens.length;
        assert "ls".equals(tokens[0]);
        assert "-la".equals(tokens[1]);

        // Makes sure quotes:
        // - escape spaces.
        // - are not removed from the command.
        tokens = Command.getTokens("ls \"- l a\"");
        assert 2 == tokens.length;
        assert "ls".equals(tokens[0]);
        assert "\"- l a\"".equals(tokens[1]);

        // Makes sure spaces are not trimmed when they're not expected to.
        tokens = Command.getTokens("ls \"-    l    a   \"");
        assert 2 == tokens.length;
        assert "ls".equals(tokens[0]);
        assert "\"-    l    a   \"".equals(tokens[1]);

        // Makes sure \s:
        // - escape quotes.
        // - are removed from the command.
        tokens = Command.getTokens("ls \\\"- l a");
        assert 4 == tokens.length;
        assert "ls".equals(tokens[0]);
        assert "\"-".equals(tokens[1]);
        assert "l".equals(tokens[2]);
        assert "a".equals(tokens[3]);

        // Makes sure \s:
        // - escape spaces.
        // - are remoed from the command.
        tokens = Command.getTokens("ls My\\ Documents");
        assert 2 ==tokens.length;
        assert "ls".equals(tokens[0]);
        assert "My Documents".equals(tokens[1]);

        // Makes sure 'complex' tokenisation works.
        tokens = Command.getTokens("/usr/bin/find . -name \\\\*.java -exec sh -c \"echo {}; wc {}\" \\\\;");
        assert 9 == tokens.length;
        assert "/usr/bin/find".equals(tokens[0]);
        assert ".".equals(tokens[1]);
        assert "-name".equals(tokens[2]);
        assert "\\*.java".equals(tokens[3]);
        assert "-exec".equals(tokens[4]);
        assert "sh".equals(tokens[5]);
        assert "-c".equals(tokens[6]);
        assert "\"echo {}; wc {}\"".equals(tokens[7]);
        assert "\\;".equals(tokens[8]);
    }



    // - Constructors tests ----------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Makes sure the specified command matches the specified arguments.
     */
    private void checkCommand(Command command, CommandType type, boolean isDisplayNameSet) {
        // Tests common values.
        assert ALIAS.equals(command.getAlias());
        assert COMMAND.equals(command.getCommand());
        assert type == command.getType();

        // Tests context dependant values.
        if(isDisplayNameSet) {
            assert command.isDisplayNameSet();
            assert DISPLAY_NAME.equals(command.getDisplayName());
        }
        else {
            assert !command.isDisplayNameSet();
            assert ALIAS.equals(command.getDisplayName());
        }
    }

    /**
     * Makes sure all constructors initialise a command to the right values.
     */
    @Test
    public void testConstructors() {
        // Tests the 2 arguments constructor.
        checkCommand(new Command(ALIAS, COMMAND), CommandType.NORMAL_COMMAND, false);

        // Tests the 3 arguments constructor.
        checkCommand(new Command(ALIAS, COMMAND, CommandType.NORMAL_COMMAND), CommandType.NORMAL_COMMAND, false);
        checkCommand(new Command(ALIAS, COMMAND, CommandType.SYSTEM_COMMAND), CommandType.SYSTEM_COMMAND, false);
        checkCommand(new Command(ALIAS, COMMAND, CommandType.INVISIBLE_COMMAND), CommandType.INVISIBLE_COMMAND, false);

        // Tests the 4 arguments constructor.
        checkCommand(new Command(ALIAS, COMMAND, CommandType.NORMAL_COMMAND, DISPLAY_NAME), CommandType.NORMAL_COMMAND, true);
        checkCommand(new Command(ALIAS, COMMAND, CommandType.SYSTEM_COMMAND, DISPLAY_NAME), CommandType.SYSTEM_COMMAND, true);
        checkCommand(new Command(ALIAS, COMMAND, CommandType.INVISIBLE_COMMAND, DISPLAY_NAME), CommandType.INVISIBLE_COMMAND, true);
    }
}
