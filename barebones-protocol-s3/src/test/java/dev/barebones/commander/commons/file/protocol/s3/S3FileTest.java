/**
 * This file is part of muCommander, http://www.mucommander.com
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

package dev.barebones.commander.commons.file.protocol.s3;

import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.commons.file.AbstractFileTest;
import dev.barebones.commander.commons.file.FileFactory;
import dev.barebones.commander.commons.file.FileOperation;
import org.testng.annotations.BeforeClass;

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

    /** Base temporary folder */
    private static AbstractFile tempFolder;

    @BeforeClass
    public static void setupTemporaryFolder() {
        String tempFolderUri = System.getProperty(TEMP_FOLDER_PROPERTY);
        if (tempFolderUri == null) {
            // Integration test — needs a live S3 endpoint and credentials
            // exposed via the system property. CI doesn't have either, so
            // skip the whole class instead of NPE-ing in FileFactory.
            throw new org.testng.SkipException(
                    "S3FileTest requires -D" + TEMP_FOLDER_PROPERTY
                            + "=<s3-uri> at JVM start; skipping in CI.");
        }
        tempFolder = FileFactory.getFile(tempFolderUri);
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
