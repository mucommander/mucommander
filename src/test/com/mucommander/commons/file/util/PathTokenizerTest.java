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

package com.mucommander.commons.file.util;

import org.testng.annotations.Test;

/**
 * Runs tests on {@link PathTokenizer}.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class PathTokenizerTest {
    @Test
    public void testEmtpy() {
        test("");
    }

    @Test
    public void testUnknown() {
        test("blah");
    }

    @Test
    public void testComplexMixed() {
        test("/C:\\\\this///is/not\\\\a\\valid//path//but/we\\let//it\\parse/");
    }

    @Test
    public void testSimpleMixed() {
        test("/C:\\temp");
    }

    @Test
    public void testWindowsRootWithoutTrailingSeparator() {
        test("C:");
    }

    @Test
    public void testWindowsRootWithTrailingSeparator() {
        test("C:\\");
    }

    @Test
    public void testWindowsWithTrailingSeparator() {
        test("C:\\temp\\");
    }

    @Test
    public void testWindowsWithoutTrailingSeparator() {
        test("C:\\temp");
    }

    @Test
    public void testUnixWithTrailingSeparator() {
        test("/Users/maxence/Temp/");
    }

    @Test
    public void testUnixWithoutTrailingSeparator() {
        test("/Users/maxence/Temp");
    }

    @Test
    public void testUnixRoot() {
        test("/");
    }

    private static void test(String path) {
        for(boolean reverseOrder=false; ; reverseOrder=true) {
            PathTokenizer pt = new PathTokenizer(path, PathTokenizer.DEFAULT_SEPARATORS, reverseOrder);
            
            String reconstructedPath = pt.getLastSeparator();

            while(pt.hasMoreFilenames()) {
                String nextToken = pt.nextFilename();
                String lastSeparator = pt.getLastSeparator();

                if(!reverseOrder)
                    reconstructedPath += nextToken+lastSeparator;
            }

            if(!reverseOrder)
                assert reconstructedPath.equals(path);

            if(reverseOrder)
                break;
        }
    }
}
