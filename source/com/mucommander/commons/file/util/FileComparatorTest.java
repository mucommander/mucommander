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

package com.mucommander.commons.file.util;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.impl.TestFile;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 * A test case for {@link FileComparator}.
 * @author Mariusz Jakubowski
 *
 */
public class FileComparatorTest extends TestCase {
  
    AbstractFile[] files;
    private TestFile A;
    private TestFile B;
    private TestFile C;
    private TestFile D;
    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        A = new TestFile(FileFactory.getTemporaryFolder() + "A", false, 500, 1, null);
        B = new TestFile(FileFactory.getTemporaryFolder() + "B.e9.e1", true, 0, 2, null);
        C = new TestFile(FileFactory.getTemporaryFolder() + "C.e3", false, 200, 3, null);
        D = new TestFile(FileFactory.getTemporaryFolder() + "D.e2", true, 0, 4, null);
        files = new AbstractFile[4];
        files[0] = C;
        files[1] = D;
        files[2] = A;
        files[3] = B;
    }

    public void testCompareNameDir() {
        Arrays.sort(files, new FileComparator(FileComparator.NAME_CRITERION, true, true));
        assertEquals(B, files[0]);
        assertEquals(D, files[1]);
        assertEquals(A, files[2]);
        assertEquals(C, files[3]);
    }

    public void testCompareNameDirDesc() {
        Arrays.sort(files, new FileComparator(FileComparator.NAME_CRITERION, false, true));
        assertEquals(D, files[0]);
        assertEquals(B, files[1]);
        assertEquals(C, files[2]);
        assertEquals(A, files[3]);
    }
    
    public void testCompareName() {
        Arrays.sort(files, new FileComparator(FileComparator.NAME_CRITERION, true, false));
        assertEquals(A, files[0]);
        assertEquals(B, files[1]);
        assertEquals(C, files[2]);
        assertEquals(D, files[3]);
    }

    public void testCompareNameDesc() {
        Arrays.sort(files, new FileComparator(FileComparator.NAME_CRITERION, false, false));
        assertEquals(D, files[0]);
        assertEquals(C, files[1]);
        assertEquals(B, files[2]);
        assertEquals(A, files[3]);
    }
    
    public void testCompareSizeDir() {
        Arrays.sort(files, new FileComparator(FileComparator.SIZE_CRITERION, true, true));
        assertEquals(B, files[0]);
        assertEquals(D, files[1]);
        assertEquals(C, files[2]);
        assertEquals(A, files[3]);
    }

    public void testCompareSize() {
        Arrays.sort(files, new FileComparator(FileComparator.SIZE_CRITERION, true, false));
        assertEquals(B, files[0]);
        assertEquals(D, files[1]);
        assertEquals(C, files[2]);
        assertEquals(A, files[3]);
    }

    public void testCompareSizeDirDesc() {
        Arrays.sort(files, new FileComparator(FileComparator.SIZE_CRITERION, false, true));
        assertEquals(D, files[0]);
        assertEquals(B, files[1]);
        assertEquals(A, files[2]);
        assertEquals(C, files[3]);
    }

    public void testCompareSizeDesc() {
        Arrays.sort(files, new FileComparator(FileComparator.SIZE_CRITERION, false, false));
        assertEquals(A, files[0]);
        assertEquals(C, files[1]);
        assertEquals(D, files[2]);
        assertEquals(B, files[3]);
    }

    public void testCompareDateDir() {
        Arrays.sort(files, new FileComparator(FileComparator.DATE_CRITERION, true, true));
        assertEquals(B, files[0]);
        assertEquals(D, files[1]);
        assertEquals(A, files[2]);
        assertEquals(C, files[3]);
    }

    public void testCompareDate() {
        Arrays.sort(files, new FileComparator(FileComparator.DATE_CRITERION, true, false));
        assertEquals(A, files[0]);
        assertEquals(B, files[1]);
        assertEquals(C, files[2]);
        assertEquals(D, files[3]);
    }

    public void testCompareDateDirDesc() {
        Arrays.sort(files, new FileComparator(FileComparator.DATE_CRITERION, false, true));
        assertEquals(D, files[0]);
        assertEquals(B, files[1]);
        assertEquals(C, files[2]);
        assertEquals(A, files[3]);
    }

    public void testCompareDateDesc() {
        Arrays.sort(files, new FileComparator(FileComparator.DATE_CRITERION, false, false));
        assertEquals(D, files[0]);
        assertEquals(C, files[1]);
        assertEquals(B, files[2]);
        assertEquals(A, files[3]);
    }
    
    public void testCompareExtDir() {
        Arrays.sort(files, new FileComparator(FileComparator.EXTENSION_CRITERION, true, true));
        assertEquals(B, files[0]);
        assertEquals(D, files[1]);
        assertEquals(A, files[2]);
        assertEquals(C, files[3]);
    }

    public void testCompareExt() {
        Arrays.sort(files, new FileComparator(FileComparator.EXTENSION_CRITERION, true, false));
        assertEquals(A, files[0]);
        assertEquals(B, files[1]);
        assertEquals(D, files[2]);
        assertEquals(C, files[3]);
    }

    public void testCompareExtDirDesc() {
        Arrays.sort(files, new FileComparator(FileComparator.EXTENSION_CRITERION, false, true));
        assertEquals(D, files[0]);
        assertEquals(B, files[1]);
        assertEquals(C, files[2]);
        assertEquals(A, files[3]);
    }

    public void testCompareExtDesc() {
        Arrays.sort(files, new FileComparator(FileComparator.EXTENSION_CRITERION, false, false));
        assertEquals(C, files[0]);
        assertEquals(D, files[1]);
        assertEquals(B, files[2]);
        assertEquals(A, files[3]);
    }
    
}
