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

package com.mucommander.ui.macosx;

import com.mucommander.runtime.OsFamilies;
import junit.framework.TestCase;

/**
 * A test case for {@link com.mucommander.ui.macosx.AppleScript}.
 *
 * @author Maxence Bernard
 */
public class AppleScriptTest extends TestCase {

    /**
     * Tests a simple AppleScript that outputs something to stdout.
     */
    public void testScriptOutput() {
        StringBuffer output = new StringBuffer();
        boolean success = AppleScript.execute("count {\"How\", \"many\", \"items\", \"in\", \"this\", \"list\"}", output);

        if(OsFamilies.MAC_OS_X.isCurrent()) {
            // Assert that the script was executed successfully and that the output matches what is expected
            assertTrue(success);
            assertEquals("6", output.toString());
        }
        else {
            // We're not running Mac OS X, assert that execute returns false
            assertFalse(success);
        }
    }

    /**
     * Verifies that AppleScript allows extended characters in the script and that it outputs them properly, using
     * either <i>Unicode</i> or <i>MacRoman</i> depending on the
     * {@link com.mucommander.ui.macosx.AppleScript#getScriptEncoding() current AppleScript encoding}. 
     */
    public void testScriptEncoding() {
        StringBuffer output = new StringBuffer();
        String s = AppleScript.getScriptEncoding().equals(AppleScript.UTF8)?
                // Unicode test string
                "どうもありがとうミスターロボット":
                // MacRoman test string
                "mércî mr röbôt";

        AppleScript.execute("do shell script \"echo "+s+"\"", output);

        // Assert that we got the same text as the one we passed to the script
        assertEquals(s, output.toString());
    }

    /**
     * Tests a bogus script and asserts that it fails to be executed.
     */
    public void testScriptError() {
        // Should fail under all platforms
        assertFalse(AppleScript.execute("blah", new StringBuffer()));
    }
}
