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

package com.mucommander.ui.dialog.file;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.DummyFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;
import com.mucommander.ui.dialog.file.BatchRenameDialog.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.util.Calendar;


/**
 * Batch rename dialog test case.
 * @author Mariusz Jakubowski
 *
 */
public class BatchRenameTest {

    private TestFile abcdef;
    private TestFile abcdef_ghi;
    private TestFile _abcdef;
    private TestFile abcdef_ghi_jkl;

    private static class TestFile extends DummyFile {
        private static long         date;
        private        AbstractFile parent;

        static {
            Calendar c = Calendar.getInstance();
            c.set(2008, 2, 10, 13, 5, 37);
            date = c.getTimeInMillis();
        }

        public TestFile(String name, AbstractFile parent) throws MalformedURLException {
            super(FileURL.getFileURL(name));
            this.parent = parent;
        }

        @Override
        public long getSize() {
            return 0;
        }

        @Override
        public long getDate() {
            return date;
        }

        @Override
        public AbstractFile getParent() {
            return parent;
        }
    }

    @BeforeClass
    protected void setUp() throws Exception {
        TestFile parent = new TestFile(FileFactory.getTemporaryFolder() + "parent", null);
        
        abcdef = new TestFile(FileFactory.getTemporaryFolder() + "abcdef", parent);
        abcdef_ghi = new TestFile(FileFactory.getTemporaryFolder() + "abcdef.ghi", parent);
        abcdef_ghi_jkl = new TestFile(FileFactory.getTemporaryFolder() + "abcdef.ghi.jkl", parent);
        _abcdef = new TestFile(FileFactory.getTemporaryFolder() + ".abcdef", parent);
    }

    @Test
    public void testName() {
        NameToken full = new NameToken("N");
        full.parse();
        assert "abcdef".equals(full.apply(abcdef));
        assert "abcdef".equals(full.apply(abcdef_ghi));
        assert "abcdef.ghi".equals(full.apply(abcdef_ghi_jkl));
        assert "".equals(full.apply(_abcdef));

        NameToken sec = new NameToken("N2");
        sec.parse();
        assert "b".equals(sec.apply(abcdef));
        assert "b".equals(sec.apply(abcdef_ghi));
        assert "b".equals(sec.apply(abcdef_ghi_jkl));
        assert "".equals(sec.apply(_abcdef));

        NameToken secback = new NameToken("N-2");
        secback.parse();
        assert "e".equals(secback.apply(abcdef));
        assert "e".equals(secback.apply(abcdef_ghi));
        assert "h".equals(secback.apply(abcdef_ghi_jkl));
        assert "".equals(secback.apply(_abcdef));


        NameToken secthree = new NameToken("N2,3");
        secthree.parse();
        assert "bcd".equals(secthree.apply(abcdef));
        assert "bcd".equals(secthree.apply(abcdef_ghi));
        assert "bcd".equals(secthree.apply(abcdef_ghi_jkl));
        assert "".equals(secthree.apply(_abcdef));

        NameToken sectofifth = new NameToken("N2-5");
        sectofifth.parse();
        assert "bcde".equals(sectofifth.apply(abcdef));
        assert "bcde".equals(sectofifth.apply(abcdef_ghi));
        assert "bcde".equals(sectofifth.apply(abcdef_ghi_jkl));
        assert "".equals(sectofifth.apply(_abcdef));

        NameToken sectoend = new NameToken("N2-");
        sectoend.parse();
        assert "bcdef".equals(sectoend.apply(abcdef));
        assert "bcdef".equals(sectoend.apply(abcdef_ghi));
        assert "bcdef.ghi".equals(sectoend.apply(abcdef_ghi_jkl));
        assert "".equals(sectoend.apply(_abcdef));

        NameToken secbackthree = new NameToken("N-2,3");
        secbackthree.parse();
        assert "ef".equals(secbackthree.apply(abcdef));
        assert "ef".equals(secbackthree.apply(abcdef_ghi));
        assert "hi".equals(secbackthree.apply(abcdef_ghi_jkl));
        assert "".equals(secbackthree.apply(_abcdef));

        NameToken thirdbacksecback = new NameToken("N-3--2");
        thirdbacksecback.parse();
        assert "de".equals(thirdbacksecback.apply(abcdef));
        assert "de".equals(thirdbacksecback.apply(abcdef_ghi));
        assert "gh".equals(thirdbacksecback.apply(abcdef_ghi_jkl));
        assert "".equals(thirdbacksecback.apply(_abcdef));

        NameToken eightbackfourth = new NameToken("N-8-4");
        eightbackfourth.parse();
        assert "abcd".equals(eightbackfourth.apply(abcdef));
        assert "abcd".equals(eightbackfourth.apply(abcdef_ghi));
        assert "cd".equals(eightbackfourth.apply(abcdef_ghi_jkl));
        assert "".equals(eightbackfourth.apply(_abcdef));

    }

    @Test
    public void testExt() {
        NameToken full = new ExtToken("E");
        full.parse();
        assert "".equals(full.apply(abcdef));
        assert "ghi".equals(full.apply(abcdef_ghi));
        assert "jkl".equals(full.apply(abcdef_ghi_jkl));
        assert "abcdef".equals(full.apply(_abcdef));
    }

    @Test
    public void testCounter() {
        CounterToken one = new CounterToken("C", 1, 1, 1);
        one.parse();
        assert "1".equals(one.apply(abcdef));
        assert "2".equals(one.apply(abcdef));

        CounterToken start5 = new CounterToken("C5", 1, 1, 1);
        start5.parse();
        assert "5".equals(start5.apply(abcdef));
        assert "6".equals(start5.apply(abcdef));

        CounterToken step2 = new CounterToken("C5,2", 1, 1, 1);
        step2.parse();
        assert "5".equals(step2.apply(abcdef));
        assert "7".equals(step2.apply(abcdef));

        CounterToken digits2 = new CounterToken("C9,2,2", 1, 1, 1);
        digits2.parse();
        assert "09".equals(digits2.apply(abcdef));
        assert "11".equals(digits2.apply(abcdef));

        CounterToken digits3def = new CounterToken("C,,3", 10, 5, 1);
        digits3def.parse();
        assert "010".equals(digits3def.apply(abcdef));
        assert "015".equals(digits3def.apply(abcdef));

        CounterToken step3def = new CounterToken("C,3", 10, 1, 1);
        step3def.parse();
        assert "10".equals(step3def.apply(abcdef));
        assert "13".equals(step3def.apply(abcdef));

    }

    @Test
    public void testDate() {
        DateToken full = new DateToken("YMDhms");
        full.parse();
        assert "20080210130537".equals(full.apply(abcdef));
    }

    @Test
    public void testParent() {
        ParentDirToken p = new ParentDirToken("P");
        p.parse();
        assert "parent".equals(p.apply(abcdef));
    }

}
