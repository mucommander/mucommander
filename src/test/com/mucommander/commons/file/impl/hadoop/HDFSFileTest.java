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

package com.mucommander.commons.file.impl.hadoop;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AbstractFileTest;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileOperation;
import org.testng.annotations.BeforeClass;

import java.io.IOException;

/**
 * @author Maxence Bernard
 */
public class HDFSFileTest extends AbstractFileTest {

    /** The system property that holds the URI to the temporary HDFS folder */
    public final static String TEMP_FOLDER_PROPERTY = "test_properties.hdfs_test.temp_folder";

    /** Base temporary folder */
    private static AbstractFile tempFolder;

    @BeforeClass
    public static void setupTemporaryFolder() {
        tempFolder = FileFactory.getFile(System.getProperty(TEMP_FOLDER_PROPERTY));
    }



    /////////////////////////////////////
    // AbstractFileTest implementation //
    /////////////////////////////////////

    @Override
    public AbstractFile getTemporaryFile() throws IOException {
        return tempFolder.getDirectChild(getPseudoUniqueFilename(HDFSFileTest.class.getName()));
    }

    @Override
    public FileOperation[] getSupportedOperations() {
        return new FileOperation[] {
            FileOperation.READ_FILE,
            FileOperation.RANDOM_READ_FILE,
            FileOperation.WRITE_FILE,
            FileOperation.CREATE_DIRECTORY,
            FileOperation.LIST_CHILDREN,
            FileOperation.DELETE,
            FileOperation.RENAME,
            FileOperation.CHANGE_DATE,
            FileOperation.CHANGE_PERMISSION,
        };
    }
}
