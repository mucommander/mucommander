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

package com.mucommander.commons.file.impl.smb;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AbstractFileTest;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileOperation;
import org.testng.annotations.BeforeClass;

import java.io.IOException;

/**
 * An {@link AbstractFileTest} implementation for {@link com.mucommander.commons.file.impl.smb.SMBFile}.
 * The SMB temporary folder where test files are created is defined by the {@link #TEMP_FOLDER_PROPERTY} system property.
 *
 * @author Maxence Bernard
 */
public class SMBFileTest extends AbstractFileTest {

    /** The system property that holds the URI to the temporary SMB folder */
    public final static String TEMP_FOLDER_PROPERTY = "test_properties.smb_test.temp_folder";

    /** Base temporary folder */
    private static AbstractFile tempFolder;

    static {
        // Configure jCIFS for maximum compatibility
        SMBProtocolProvider.setLmCompatibility(0);
        SMBProtocolProvider.setExtendedSecurity(false);

        // Turn off attribute caching completely, otherwise tests will fail
        SMBFile.setAttributeCachingPeriod(0);
    }

    @BeforeClass
    public static void setupTemporaryFolder() {
        tempFolder = FileFactory.getFile(System.getProperty(TEMP_FOLDER_PROPERTY));
    }



    /////////////////////////////////////
    // AbstractFileTest implementation //
    /////////////////////////////////////

    @Override
    public AbstractFile getTemporaryFile() throws IOException {
        return tempFolder.getDirectChild(getPseudoUniqueFilename(SMBFileTest.class.getName()));
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
            FileOperation.COPY_REMOTELY,
            FileOperation.RENAME,
            FileOperation.CHANGE_DATE,
            FileOperation.CHANGE_PERMISSION,
            FileOperation.GET_FREE_SPACE,
        };
    }
}
