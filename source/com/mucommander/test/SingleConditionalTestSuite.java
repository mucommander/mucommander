/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

/**
 * <code>SingleConditionalTestSuite</code> is a <code>ConditionalTestSuite</code> that operates on a single test case.
 * It implements {@link #populateTestSuite()} by adding the TestCase class returned by {@link #getTestCaseClass()} to
 * the the test suite, making subclassing a bit easier.
 *
 * @author Maxence Bernard
 */
public abstract class SingleConditionalTestSuite extends ConditionalTestSuite {
    
    //////////////////////////////////
    // ConditionalTestSuite methods //
    //////////////////////////////////

    protected void populateTestSuite() {
        if(Debug.ON) Debug.trace("Adding "+getTestCaseClass().getName()+" to test suite");
        addTestSuite(getTestCaseClass());
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Returns the <code>Class</code> of the <code>TestCase</code> to be added to this test suite. Although it is
     * not enforced, the returned class must extend <code>junit.framework.TestCase</code>.  
     *
     * @return the TestCase class to be added to this test suite
     */
    protected abstract Class getTestCaseClass();
//    protected abstract Class<? extends TestCase> getTestCaseClass();      // For the day we allow Java 1.5 code

}
