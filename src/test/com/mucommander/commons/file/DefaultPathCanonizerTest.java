/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.file;

import org.testng.annotations.Test;

/**
 * Tests {@link DefaultPathCanonizer}.
 *
 * @author Maxence Bernard
 * @see DefaultPathCanonizer
 */
public class DefaultPathCanonizerTest {

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
            assert "~".equals(canonizer.canonize("~"));
            assert ("~"+separator+"blah").equals(canonizer.canonize("~"+separator+"blah"));
        }
        else {
            assert tildeReplacement.equals(canonizer.canonize("~"));
            assert (tildeReplacement+separator+"blah").equals(canonizer.canonize("~"+separator+"blah"));
        }

        // Test '.' and '..' factoring

        assert separator.equals(canonizer.canonize(getNormalizedPath("/home/maxence/../..", separator)));
        assert getNormalizedPath("/home/", separator).equals(canonizer.canonize(getNormalizedPath("/home/maxence/..", separator)));
        assert getNormalizedPath("/home/maxence/", separator).equals(canonizer.canonize(getNormalizedPath("/home/maxence/.", separator)));
        assert separator.equals(canonizer.canonize(getNormalizedPath("/home/maxence/../..", separator)));
        assert getNormalizedPath("/home/maxence/", separator).equals(canonizer.canonize(getNormalizedPath("/home//maxence//", separator)));
        assert separator.equals(canonizer.canonize(getNormalizedPath("/././.", separator)));
        assert "".equals(canonizer.canonize(getNormalizedPath("/../../..", separator)));
        assert separator.equals(canonizer.canonize(getNormalizedPath("/1/.././1/./2//./.././../", separator)));
    }

    /**
     * Tests '.' and '..' factoring, and tilde replacement if <code>tildeReplacement</code>, with a forward slash
     * path separator.
     */
    @Test
    public void testForwardSlashCanonization() {
        testCanonizer("/", null);
        testCanonizer("/", "/home/maxence");
    }

    /**
     * Tests '.' and '..' factoring, and tilde replacement if <code>tildeReplacement</code>, with a backslash
     * path separator.
     */
    @Test
    public void testBackSlashCanonization() {
        testCanonizer("\\", null);
        testCanonizer("\\", "C:\\Document and Settings\\maxence");
    }
}
