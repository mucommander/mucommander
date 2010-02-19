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

package com.mucommander.test;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * This class is a test runner that allows the @Test methods of a class to be performed only if a runtime condition is
 * satisfied. If the condition is satisfied, the actual test running is delegated to a {@link BlockJUnit4ClassRunner}.
 *
 * <p>The class to be tested must implement {@link ConditionalTest}. The condition is then determined at runtime by
 * calling {@link ConditionalTest#isEnabled()}: if <code>true</code> is returned, then the tests are performed
 * (and vice-versa). If the test class does not implement {@link ConditionalTest}, the tests are not performed.</p>
 *
 * <p>This test runner is used by annotating the test class with <code>@RunWith(ConditionalTestRunner.class)</code>.</p>
 *
 * @author Maxence Bernard
 */
public class ConditionalTestRunner extends Runner {

    private BlockJUnit4ClassRunner classRunner;
    private Class<?> testClass;

    public ConditionalTestRunner(Class<?> testClass) throws InitializationError {
        this.testClass = testClass;
        this.classRunner = new BlockJUnit4ClassRunner(testClass);
    }


    ///////////////////////////
    // Runner implementation //
    ///////////////////////////
                               
    @Override
    public Description getDescription() {
        return classRunner.getDescription();
    }

    @Override
    public void run(RunNotifier runNotifier) {
        boolean run = false;

        if(ConditionalTest.class.isAssignableFrom(testClass)) {
            try {
                if(!((ConditionalTest)testClass.newInstance()).isEnabled()) {
                    System.out.println(testClass +" not enabled, skipping tests");
                    return;
                }
            }
            catch(Exception e) {
                System.err.println("Error: failed to instantiate "+ testClass +", "+e.toString());
                return;
            }
        }
        else {
            System.err.println("Warning: "+testClass+" does not implement "+ConditionalTest.class+", skipping tests");
            return;
        }

        System.out.println(testClass +" enabled, running tests");

        classRunner.run(runNotifier);
    }
}
