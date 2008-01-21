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

package com.mucommander.test;

import com.mucommander.Debug;
import junit.framework.TestSuite;

/**
 * <code>ConditionalTestSuite</code> is an abstract JUnit test suite which can be conditionally enabled or disabled at
 * runtime, depending on the value returned by {@link #isEnabled()}.
 * The test suite can comprise one or several test cases: the {@link #populateTestSuite()} method populates the test
 * suite before it is executed. {@link SingleConditionalTestSuite} is a specialized
 * <code>ConditionalTestSuite</code> for test suites with a single test case.
 *
 * <p>Once implemented, test suites can easily be run by JUnit's test runner by implementing a
 * <code>public static TestSuite suite()</code> method and have it return an instance of itself.</p>.
 *
 * @author Maxence Bernard
 */
public abstract class ConditionalTestSuite extends TestSuite {

    public ConditionalTestSuite() {
        if(isEnabled()) {
            if(Debug.ON) Debug.trace("Test suite enabled, populating test suite.");
            populateTestSuite();
        }
        else {
            if(Debug.ON) Debug.trace("Test suite disabled, tests not performed.");
        }
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Returns <code>true</code> if this test case is enabled. This method is called before each test to determine
     * if it should be executed or skipped. It allows to conditionally enable or disable the test case, for example
     * depending on some configuration property.
     *
     * @return true if this test case is enabled
     */
    protected abstract boolean isEnabled();

    /**
     * Populates this test suite with the JUnit tests to be performed. This method is called by
     * {@link ConditionalTestSuite#ConditionalTestSuite()} only if this test suite is enabled, as
     * reported by {@link #isEnabled()}.
     */
    protected abstract void populateTestSuite();
}
