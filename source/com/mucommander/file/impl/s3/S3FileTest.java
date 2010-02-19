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

package com.mucommander.file.impl.s3;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractFileTest;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileOperation;
import org.junit.BeforeClass;

import java.io.IOException;

/**
 * An {@link AbstractFileTest} implementation for the Amazon S3 file implementation.
 * The S3 temporary folder where test files are created is defined by the {@link #TEMP_FOLDER_PROPERTY} system property.
 *
 * @author Maxence Bernard
 */
public class S3FileTest extends AbstractFileTest {

    /** The system property that holds the URI to the temporary S3 folder */
    public final static String TEMP_FOLDER_PROPERTY = "test_properties.s3_test.temp_folder";

    /** Name of the system property that controls whether the test suite is enabled or not */
    public final static String ENABLED_PROPERTY = "test_properties.s3_test.enabled";

    /** Base temporary folder */
    private static AbstractFile tempFolder;

    @BeforeClass
    public static void setupTemporaryFolder() {
        tempFolder = FileFactory.getFile(System.getProperty(TEMP_FOLDER_PROPERTY));
    }


    ////////////////////////////////////
    // ConditionalTest implementation //
    ////////////////////////////////////

    public boolean isEnabled() {
        return "true".equals(System.getProperty(ENABLED_PROPERTY));
    }


    /////////////////////////////////////
    // AbstractFileTest implementation //
    /////////////////////////////////////

    @Override
    public AbstractFile getTemporaryFile() throws IOException {
        return tempFolder.getDirectChild(getPseudoUniqueFilename(S3FileTest.class.getName()));
    }

    @Override
    public FileOperation[] getSupportedOperations() {
        return new FileOperation[] {
            FileOperation.READ_FILE,
            FileOperation.RANDOM_READ_FILE,
            FileOperation.CREATE_DIRECTORY,
            FileOperation.LIST_CHILDREN,
            FileOperation.DELETE,
            FileOperation.RENAME,
            FileOperation.COPY_REMOTELY,
        };
    }
}
