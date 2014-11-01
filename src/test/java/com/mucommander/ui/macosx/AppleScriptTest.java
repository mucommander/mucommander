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

package com.mucommander.ui.macosx;

import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.util.StringUtils;
import org.testng.annotations.Test;

import java.util.Locale;

/**
 * A test case for {@link com.mucommander.ui.macosx.AppleScript}.
 *
 * @author Maxence Bernard
 */
public class AppleScriptTest {

    /**
     * Tests a simple AppleScript that outputs something to stdout.
     */
    @Test
    public void testScriptOutput() {
        StringBuilder output = new StringBuilder();
        boolean success = AppleScript.execute("count {\"How\", \"many\", \"items\", \"in\", \"this\", \"list\"}", output);

        if(OsFamily.MAC_OS_X.isCurrent()) {
            // Assert that the script was executed successfully and that the output matches what is expected
            assert success;
            assert "6".equals(output.toString());
        }
        else {
            // We're not running Mac OS X, assert that execute returns false
            assert !success;
        }
    }

    /**
     * Verifies that AppleScript allows extended characters in the script and that it outputs them properly, using
     * either <i>Unicode</i> or <i>MacRoman</i> depending on the
     * {@link com.mucommander.ui.macosx.AppleScript#getScriptEncoding() current AppleScript encoding}. 
     */
    @Test
    public void testScriptEncoding() {
        StringBuilder output = new StringBuilder();

        String nonAsciiString;
        Locale stringLocale;        // for locale-aware String comparison

        if(AppleScript.getScriptEncoding().equals(AppleScript.UTF8)) {      // Under AppleScript 2.0 and up
            nonAsciiString = "どうもありがとうミスターロボット";
            stringLocale = Locale.JAPANESE;
        }
        else {                                                              // MacRoman under AppleScript 1.10 and lower
            // This String must only contain MacRoman characters
            nonAsciiString = "mércî mr röbôt";
            stringLocale = Locale.FRENCH;
        }


        boolean success = AppleScript.execute("do shell script \"echo "+nonAsciiString+"\"", output);

        if(OsFamily.MAC_OS_X.isCurrent()) {
            // Assert that the script was executed successfully and that we got the same text as the one we passed
            assert success;
            assert StringUtils.equals(nonAsciiString, output.toString(), stringLocale);
        }
        else {
            // We're not running Mac OS X, assert that execute returns false
            assert !success;
        }
    }

    /**
     * Tests a bogus script and asserts that it fails to be executed.
     */
    @Test
    public void testScriptError() {
        // Should fail under all platforms
        assert !AppleScript.execute("blah", new StringBuilder());
    }
}
