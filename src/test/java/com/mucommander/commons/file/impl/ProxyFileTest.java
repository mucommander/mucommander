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

package com.mucommander.commons.file.impl;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AbstractFileTest;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileOperation;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * An {@link AbstractFileTest} implementation for {@link ProxyFile}, with some additional
 * test methods.
 *
 * @author Maxence Bernard
 */
public class ProxyFileTest extends AbstractFileTest {

    ////////////////////////////////////
    // ConditionalTest implementation //
    ////////////////////////////////////

    public boolean isEnabled() {
        return true;
    }


    /////////////////////////////////////
    // AbstractFileTest implementation //
    /////////////////////////////////////

    @Override
    public AbstractFile getTemporaryFile() throws IOException {
        // Returns a ProxyFile instance proxying a LocalFile ; the kind of proxied file should not matter as long as it
        // passes AbstractFileTest.
        return new ProxyFile(FileFactory.getTemporaryFile(getClass().getName(), false)) {
            // Note: a ProxyFile with no overridden method serves absolutely no purpose whatsoever
        };
    }

    @Override
    public FileOperation[] getSupportedOperations() {
        return new FileOperation[] {
            FileOperation.READ_FILE,
            FileOperation.RANDOM_READ_FILE,
            FileOperation.WRITE_FILE,
            FileOperation.APPEND_FILE,
            FileOperation.RANDOM_WRITE_FILE,
            FileOperation.CREATE_DIRECTORY,
            FileOperation.LIST_CHILDREN,
            FileOperation.DELETE,
            FileOperation.RENAME,
            FileOperation.CHANGE_DATE,
            FileOperation.CHANGE_PERMISSION,
            FileOperation.GET_FREE_SPACE,
            FileOperation.GET_TOTAL_SPACE
        };
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    //@Test
    @Override
    public void testUnsupportedFileOperationAnnotations() throws Exception {
    }

    //@Test
    @Override
    public void testSupportedFileOperations() throws Exception {
    }

    //@Test
    @Override
    public void testFileInstanceCaching() throws Exception {
        // This test can't pass as ProxyFile instance are not cached, only the underlying protocol file.
    }

    
    /////////////////////////////
    // Additional test methods //
    /////////////////////////////

    /**
     * Asserts that all public, non-final and non-static <code>AbstractFile</code> methods are overridden by
     * <code>ProxyFile</code>.
     */
    @Test
    public void testAllMethodsOverridden() {
        Class<?> proxyFileClass = ProxyFile.class;
        Class<?> abstractFileClass = AbstractFile.class;

        // This array will contain all AbstractFile public methods, including the ones defined by parent classes
        // (java.lang.Object), and including static and final ones.
        Method abstractFileMethods[] = abstractFileClass.getMethods();
        Method proxyFileMethod;

        for (Method abstractFileMethod : abstractFileMethods) {
            // Skip:
            // - methods that are not declared by AbstractFile (e.g. java.lang.Object methods)
            // - static methods
            // - final methods
            if (!abstractFileMethod.getDeclaringClass().equals(abstractFileClass)
                    || (abstractFileMethod.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) != 0)
                continue;

            try {
                proxyFileMethod = proxyFileClass.getMethod(abstractFileMethod.getName(), abstractFileMethod.getParameterTypes());
            }
            catch (Exception e) {    // NoSuchMethodException, SecurityException
                proxyFileMethod = null;
            }

            assert proxyFileMethod != null && (proxyFileMethod.getDeclaringClass().equals(proxyFileClass)):
                    abstractFileMethod.getName() + " not overridden by " + proxyFileClass.getName();
        }

    }
}
