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

package com.mucommander.file;

import junit.framework.TestCase;

/**
 * Tests {@link DefaultPathCanonizer}.
 *
 * @author Maxence Bernard
 * @see DefaultPathCanonizer
 */
public class DefaultPathCanonizerTest extends TestCase {

    private String getNormalizedPath(String path, String separator) {
        if(!separator.equals("/"))
            path = path.replace("/", separator);

        return path;
    }

    /**
     * Tests '.' and '..' factoring and tilde replacement if <code>tildeReplacement</code> is not <code>null</code>.
     *
     * @param separator path separator
     * @param tildeReplacement string to replace '~' path fragments with
     */
    private void testCanonizer(String separator, String tildeReplacement) {
        DefaultPathCanonizer canonizer = new DefaultPathCanonizer(separator, tildeReplacement);

        // Test '~' canonization (or the lack thereof)
        if(tildeReplacement==null) {
            assertEquals("~", canonizer.canonize("~"));
            assertEquals("~"+separator+"blah", canonizer.canonize("~"+separator+"blah"));
        }
        else {
            assertEquals(tildeReplacement, canonizer.canonize("~"));
            assertEquals(tildeReplacement+separator+"blah", canonizer.canonize("~"+separator+"blah"));
        }

        // Test '.' and '..' factoring

        assertEquals(separator, canonizer.canonize(getNormalizedPath("/home/maxence/../..", separator)));
        assertEquals(getNormalizedPath("/home/", separator), canonizer.canonize(getNormalizedPath("/home/maxence/..", separator)));
        assertEquals(getNormalizedPath("/home/maxence/", separator), canonizer.canonize(getNormalizedPath("/home/maxence/.", separator)));
        assertEquals(separator, canonizer.canonize(getNormalizedPath("/home/maxence/../..", separator)));
        assertEquals(getNormalizedPath("/home/maxence/", separator), canonizer.canonize(getNormalizedPath("/home//maxence//", separator)));
        assertEquals(separator, canonizer.canonize(getNormalizedPath("/././.", separator)));
        assertEquals("", canonizer.canonize(getNormalizedPath("/../../..", separator)));
        assertEquals(separator, canonizer.canonize(getNormalizedPath("/1/.././1/./2//./.././../", separator)));
    }

    /**
     * Tests '.' and '..' factoring, and tilde replacement if <code>tildeReplacement</code>, with a forward slash
     * path separator.
     */
    public void testForwardSlashCanonization() {
        testCanonizer("/", null);
        testCanonizer("/", "/home/maxence");
    }

    /**
     * Tests '.' and '..' factoring, and tilde replacement if <code>tildeReplacement</code>, with a backslash
     * path separator.
     */
    public void testBackSlashCanonization() {
        testCanonizer("\\", null);
        testCanonizer("\\", "C:\\Document and Settings\\maxence");
    }
}
