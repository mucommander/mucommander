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

import com.mucommander.file.FileFactory;
import com.mucommander.file.impl.TestFile;
import com.mucommander.ui.dialog.file.BatchRenameDialog.*;
import junit.framework.TestCase;

import java.util.Calendar;


/**
 * Batch rename dialog test case.
 * @author Mariusz Jakubowski
 *
 */
public class BatchRenameTest extends TestCase {
    
    private TestFile abcdef;
    private TestFile abcdef_ghi;
    private TestFile _abcdef;
    private TestFile abcdef_ghi_jkl;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestFile parent = new TestFile(FileFactory.getTemporaryFolder() + "parent", false, 0, 0, null);
        Calendar c = Calendar.getInstance();
        c.set(2008, 2, 10, 13, 5, 37);
        long date = c.getTimeInMillis();        
        abcdef = new TestFile(FileFactory.getTemporaryFolder() + "abcdef", false, 0, date, parent);
        abcdef_ghi = new TestFile(FileFactory.getTemporaryFolder() + "abcdef.ghi", false, 0, date, parent);
        abcdef_ghi_jkl = new TestFile(FileFactory.getTemporaryFolder() + "abcdef.ghi.jkl", false, 0, date, parent);
        _abcdef = new TestFile(FileFactory.getTemporaryFolder() + ".abcdef", false, 0, date, parent);
    }
        
    public void testName() {
        NameToken full = new NameToken("N");
        full.parse();
        assertEquals("abcdef", full.apply(abcdef));
        assertEquals("abcdef", full.apply(abcdef_ghi));
        assertEquals("abcdef.ghi", full.apply(abcdef_ghi_jkl));
        assertEquals("", full.apply(_abcdef));

        NameToken sec = new NameToken("N2");
        sec.parse();
        assertEquals("b", sec.apply(abcdef));
        assertEquals("b", sec.apply(abcdef_ghi));
        assertEquals("b", sec.apply(abcdef_ghi_jkl));
        assertEquals("", sec.apply(_abcdef));
    
        NameToken secback = new NameToken("N-2");
        secback.parse();
        assertEquals("e", secback.apply(abcdef));
        assertEquals("e", secback.apply(abcdef_ghi));
        assertEquals("h", secback.apply(abcdef_ghi_jkl));
        assertEquals("", secback.apply(_abcdef));

    
        NameToken secthree = new NameToken("N2,3");
        secthree.parse();
        assertEquals("bcd", secthree.apply(abcdef));
        assertEquals("bcd", secthree.apply(abcdef_ghi));
        assertEquals("bcd", secthree.apply(abcdef_ghi_jkl));
        assertEquals("", secthree.apply(_abcdef));

        NameToken sectofifth = new NameToken("N2-5");
        sectofifth.parse();
        assertEquals("bcde", sectofifth.apply(abcdef));
        assertEquals("bcde", sectofifth.apply(abcdef_ghi));
        assertEquals("bcde", sectofifth.apply(abcdef_ghi_jkl));
        assertEquals("", sectofifth.apply(_abcdef));

        NameToken sectoend = new NameToken("N2-");
        sectoend.parse();
        assertEquals("bcdef", sectoend.apply(abcdef));
        assertEquals("bcdef", sectoend.apply(abcdef_ghi));
        assertEquals("bcdef.ghi", sectoend.apply(abcdef_ghi_jkl));
        assertEquals("", sectoend.apply(_abcdef));
        
        NameToken secbackthree = new NameToken("N-2,3");
        secbackthree.parse();
        assertEquals("ef", secbackthree.apply(abcdef));
        assertEquals("ef", secbackthree.apply(abcdef_ghi));
        assertEquals("hi", secbackthree.apply(abcdef_ghi_jkl));
        assertEquals("", secbackthree.apply(_abcdef));
        
        NameToken thirdbacksecback = new NameToken("N-3--2");
        thirdbacksecback.parse();
        assertEquals("de", thirdbacksecback.apply(abcdef));
        assertEquals("de", thirdbacksecback.apply(abcdef_ghi));
        assertEquals("gh", thirdbacksecback.apply(abcdef_ghi_jkl));
        assertEquals("", thirdbacksecback.apply(_abcdef));

        NameToken eightbackfourth = new NameToken("N-8-4");
        eightbackfourth.parse();
        assertEquals("abcd", eightbackfourth.apply(abcdef));
        assertEquals("abcd", eightbackfourth.apply(abcdef_ghi));
        assertEquals("cd", eightbackfourth.apply(abcdef_ghi_jkl));
        assertEquals("", eightbackfourth.apply(_abcdef));
    
    }
    
    public void testExt() {
        NameToken full = new ExtToken("E");
        full.parse();
        assertEquals("", full.apply(abcdef));
        assertEquals("ghi", full.apply(abcdef_ghi));
        assertEquals("jkl", full.apply(abcdef_ghi_jkl));
        assertEquals("abcdef", full.apply(_abcdef));
    }
    
    public void testCounter() {
        CounterToken one = new CounterToken("C", 1, 1, 1);
        one.parse();
        assertEquals("1", one.apply(abcdef));
        assertEquals("2", one.apply(abcdef));
        
        CounterToken start5 = new CounterToken("C5", 1, 1, 1);
        start5.parse();
        assertEquals("5", start5.apply(abcdef));
        assertEquals("6", start5.apply(abcdef));

        CounterToken step2 = new CounterToken("C5,2", 1, 1, 1);
        step2.parse();
        assertEquals("5", step2.apply(abcdef));
        assertEquals("7", step2.apply(abcdef));

        CounterToken digits2 = new CounterToken("C9,2,2", 1, 1, 1);
        digits2.parse();
        assertEquals("09", digits2.apply(abcdef));
        assertEquals("11", digits2.apply(abcdef));

        CounterToken digits3def = new CounterToken("C,,3", 10, 5, 1);
        digits3def.parse();
        assertEquals("010", digits3def.apply(abcdef));
        assertEquals("015", digits3def.apply(abcdef));

        CounterToken step3def = new CounterToken("C,3", 10, 1, 1);
        step3def.parse();
        assertEquals("10", step3def.apply(abcdef));
        assertEquals("13", step3def.apply(abcdef));
        
    }
    
    public void testDate() {
        DateToken full = new DateToken("YMDhms");
        full.parse();
        assertEquals("20080210130537", full.apply(abcdef));
    }
    
    public void testParent() {
        ParentDirToken p = new ParentDirToken("P");
        p.parse();
        assertEquals("parent", p.apply(abcdef));
    }

}
