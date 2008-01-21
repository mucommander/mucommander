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

package com.mucommander.file.impl.sftp;

import com.mucommander.Debug;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractFileTestCase;
import com.mucommander.file.FileFactory;

import java.io.IOException;

/**
 * An {@link com.mucommander.file.AbstractFileTestCase} implementation for {@link com.mucommander.file.impl.sftp.SFTPFile}.
 * The SFTP temporary folder where test files are created is defined by the {@link #TEMP_FOLDER_PROPERTY} system property.
 *
 * @author Maxence Bernard
 */
public class SFTPFileTestCase extends AbstractFileTestCase {

    /** The system property that holds the URI to the temporary SFTP folder */
    public final static String TEMP_FOLDER_PROPERTY = "test_properties.sftp_test.temp_folder";

    /** The base temporary folder */
    private static AbstractFile tempFolder;

    static {
        // Attribute caching can be enabled or disabled, it doesn't matter, tests should pass in both cases
        // Todo: use JUnit's DataPoint to test both cases (with and without caching) but it requires Java 1.5's
        // annotations which we don't use for java 1.4 backward compatibility.
//        SFTPFile.setAttributeCachingPeriod(5000);

        tempFolder = FileFactory.getFile(System.getProperty(TEMP_FOLDER_PROPERTY));
        if(Debug.ON) Debug.trace("Temp folder="+tempFolder);
    }


    /////////////////////////////////////////
    // AbstractFileTestCase implementation //
    /////////////////////////////////////////

    protected AbstractFile getTemporaryFile() throws IOException {
        return tempFolder.getDirectChild(getPseudoUniqueFilename(SFTPFileTestCase.class.getName()));
    }
}
