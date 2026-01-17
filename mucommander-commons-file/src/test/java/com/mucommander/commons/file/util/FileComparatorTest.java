/**
 * This file is part of muCommander, http://www.mucommander.com
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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.TestFile;
import com.mucommander.commons.file.util.FileComparator.CRITERION;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Locale;

/**
 * A test case for {@link FileComparator}.
 * @author Mariusz Jakubowski
 *
 */
public class FileComparatorTest {
  
    AbstractFile[] files;
    private TestFile A;
    private TestFile B;
    private TestFile C;
    private TestFile D;
    

    @BeforeMethod
    protected void setUp() throws Exception {
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

    @Test
    public void testCompareNameDir() {
        Arrays.sort(files, new FileComparator(CRITERION.NAME, true, true, AbstractFile::getName, Locale.getDefault(), FileComparator.FileComparatorModeEnum.NATURAL));
        assert B.equals(files[0]);
        assert D.equals(files[1]);
        assert A.equals(files[2]);
        assert C.equals(files[3]);
    }

    @Test
    public void testCompareNameDirDesc() {
        Arrays.sort(files, new FileComparator(CRITERION.NAME, false, true, AbstractFile::getName, Locale.getDefault(), FileComparator.FileComparatorModeEnum.NATURAL));
        assert D.equals(files[0]);
        assert B.equals(files[1]);
        assert C.equals(files[2]);
        assert A.equals(files[3]);
    }

    @Test
    public void testCompareName() {
        Arrays.sort(files, new FileComparator(CRITERION.NAME, true, false, AbstractFile::getName, Locale.getDefault(), FileComparator.FileComparatorModeEnum.NATURAL));
        assert A.equals(files[0]);
        assert B.equals(files[1]);
        assert C.equals(files[2]);
        assert D.equals(files[3]);
    }

    @Test
    public void testCompareNameDesc() {
        Arrays.sort(files, new FileComparator(CRITERION.NAME, false, false, AbstractFile::getName, Locale.getDefault(), FileComparator.FileComparatorModeEnum.NATURAL));
        assert D.equals(files[0]);
        assert C.equals(files[1]);
        assert B.equals(files[2]);
        assert A.equals(files[3]);
    }

    @Test
    public void testCompareSizeDir() {
        Arrays.sort(files, new FileComparator(CRITERION.SIZE, true, true, AbstractFile::getName, Locale.getDefault(), FileComparator.FileComparatorModeEnum.NATURAL));
        assert B.equals(files[0]);
        assert D.equals(files[1]);
        assert C.equals(files[2]);
        assert A.equals(files[3]);
    }

    @Test
    public void testCompareSize() {
        Arrays.sort(files, new FileComparator(CRITERION.SIZE, true, false, AbstractFile::getName, Locale.getDefault(), FileComparator.FileComparatorModeEnum.NATURAL));
        assert B.equals(files[0]);
        assert D.equals(files[1]);
        assert C.equals(files[2]);
        assert A.equals(files[3]);
    }

    @Test
    public void testCompareSizeDirDesc() {
        Arrays.sort(files, new FileComparator(CRITERION.SIZE, false, true, AbstractFile::getName, Locale.getDefault(), FileComparator.FileComparatorModeEnum.NATURAL));
        assert D.equals(files[0]);
        assert B.equals(files[1]);
        assert A.equals(files[2]);
        assert C.equals(files[3]);
    }

    @Test
    public void testCompareSizeDesc() {
        Arrays.sort(files, new FileComparator(CRITERION.SIZE, false, false, AbstractFile::getName, Locale.getDefault(), FileComparator.FileComparatorModeEnum.NATURAL));
        assert A.equals(files[0]);
        assert C.equals(files[1]);
        assert D.equals(files[2]);
        assert B.equals(files[3]);
    }

    @Test
    public void testCompareDateDir() {
        Arrays.sort(files, new FileComparator(CRITERION.DATE, true, true, AbstractFile::getName, Locale.getDefault(), FileComparator.FileComparatorModeEnum.NATURAL));
        assert B.equals(files[0]);
        assert D.equals(files[1]);
        assert A.equals(files[2]);
        assert C.equals(files[3]);
    }

    @Test
    public void testCompareDate() {
        Arrays.sort(files, new FileComparator(CRITERION.DATE, true, false, AbstractFile::getName, Locale.getDefault(), FileComparator.FileComparatorModeEnum.NATURAL));
        assert A.equals(files[0]);
        assert B.equals(files[1]);
        assert C.equals(files[2]);
        assert D.equals(files[3]);
    }

    @Test
    public void testCompareDateDirDesc() {
        Arrays.sort(files, new FileComparator(CRITERION.DATE, false, true, AbstractFile::getName, Locale.getDefault(), FileComparator.FileComparatorModeEnum.NATURAL));
        assert D.equals(files[0]);
        assert B.equals(files[1]);
        assert C.equals(files[2]);
        assert A.equals(files[3]);
    }

    @Test
    public void testCompareDateDesc() {
        Arrays.sort(files, new FileComparator(CRITERION.DATE, false, false, AbstractFile::getName, Locale.getDefault(), FileComparator.FileComparatorModeEnum.NATURAL));
        assert D.equals(files[0]);
        assert C.equals(files[1]);
        assert B.equals(files[2]);
        assert A.equals(files[3]);
    }

    @Test
    public void testCompareExtDir() {
        Arrays.sort(files, new FileComparator(CRITERION.EXTENSION, true, true, AbstractFile::getName, Locale.getDefault(), FileComparator.FileComparatorModeEnum.NATURAL));
        assert B.equals(files[0]);
        assert D.equals(files[1]);
        assert A.equals(files[2]);
        assert C.equals(files[3]);
    }

    @Test
    public void testCompareExt() {
        Arrays.sort(files, new FileComparator(CRITERION.EXTENSION, true, false, AbstractFile::getName, Locale.getDefault(), FileComparator.FileComparatorModeEnum.NATURAL));
        assert A.equals(files[0]);
        assert B.equals(files[1]);
        assert D.equals(files[2]);
        assert C.equals(files[3]);
    }

    @Test
    public void testCompareExtDirDesc() {
        Arrays.sort(files, new FileComparator(CRITERION.EXTENSION, false, true, AbstractFile::getName, Locale.getDefault(), FileComparator.FileComparatorModeEnum.NATURAL));
        assert D.equals(files[0]);
        assert B.equals(files[1]);
        assert C.equals(files[2]);
        assert A.equals(files[3]);
    }

    @Test
    public void testCompareExtDesc() {
        Arrays.sort(files, new FileComparator(CRITERION.EXTENSION, false, false, AbstractFile::getName, Locale.getDefault(), FileComparator.FileComparatorModeEnum.NATURAL));
        assert C.equals(files[0]);
        assert D.equals(files[1]);
        assert B.equals(files[2]);
        assert A.equals(files[3]);
    }

    @Test
    public void testFileComparatorMode_LEXICOGRAPHIC() throws Exception {
        TestFile fileA = new TestFile(FileFactory.getTemporaryFolder() + "20_test", false, 500, 1, null);
        TestFile fileB = new TestFile(FileFactory.getTemporaryFolder() + "60_test", false, 500, 1, null);
        TestFile fileC = new TestFile(FileFactory.getTemporaryFolder() + "201_test", false, 500, 1, null);

        AbstractFile[] files = new AbstractFile[]{fileA, fileB, fileC};
        Arrays.sort(files, new FileComparator(CRITERION.NAME, true, true, AbstractFile::getName, Locale.getDefault(), FileComparator.FileComparatorModeEnum.LEXICOGRAPHIC));

        Assert.assertEquals(files[0], fileA);
        Assert.assertEquals(files[1], fileC);
        Assert.assertEquals(files[2], fileB);
    }

    @Test
    public void testFileComparatorMode_NATURAL() throws Exception {
        TestFile fileA = new TestFile(FileFactory.getTemporaryFolder() + "20_test", false, 500, 1, null);
        TestFile fileB = new TestFile(FileFactory.getTemporaryFolder() + "60_test", false, 500, 1, null);
        TestFile fileC = new TestFile(FileFactory.getTemporaryFolder() + "201_test", false, 500, 1, null);

        AbstractFile[] files = new AbstractFile[]{fileA, fileB, fileC};
        Arrays.sort(files, new FileComparator(CRITERION.NAME, true, true, AbstractFile::getName, Locale.getDefault(), FileComparator.FileComparatorModeEnum.NATURAL));

        Assert.assertEquals(files[0], fileA);
        Assert.assertEquals(files[1], fileB);
        Assert.assertEquals(files[2], fileC);
    }
    
}
