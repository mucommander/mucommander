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

package com.mucommander.file.impl.smb;

import com.mucommander.test.SingleConditionalTestSuite;
import junit.framework.TestSuite;

/**
 * A {@link SingleConditionalTestSuite} that executes {@link com.mucommander.file.impl.smb.SMBFileTestCase} only if the
 * {@link #ENABLED_PROPERTY} system property is set to <code>"true"</code>.
 *
 * @author Maxence Bernard
 */
public class SMBFileTest extends SingleConditionalTestSuite {

    /** Name of the system property that controls whether the test suite is enabled or not */
    public final static String ENABLED_PROPERTY = "test_properties.smb_test.enabled";

    /**
     * Allows JUnit's test runner to execute this test suite.
     *
     * @return an instance of this test suite
     */
    public static TestSuite suite() {
        return new SMBFileTest();
    }


    ///////////////////////////////////////////////
    // SingleConditionalTestSuite implementation //
    ///////////////////////////////////////////////

    protected boolean isEnabled() {
        return "true".equals(System.getProperty(ENABLED_PROPERTY));
    }

    protected Class getTestCaseClass() {
        return SMBFileTestCase.class;
    }
}
