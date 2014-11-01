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


package com.mucommander.commons.file.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.io.StreamUtils;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.runtime.OsVersion;

/**
 * This class contains methods for file operations that are specific to Mac OS X.
 *
 * @author Maxence Bernard
 */
public class OSXFileUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(OSXFileUtils.class);

    /** Pattern matching the Spotlight comment in the output of the 'mdls' command */
    private final static Pattern MDLS_COMMENT_PATTERN = Pattern.compile("\".*\"$");

    /**
     * Returns the Spotlight/Finder comment of the given file. The specified file must be a LocalFile,
     * or have a LocalFile as an ancestor.
     *
     * <p>
     *  <code>null</code> is returned in any of the following cases:
     *  <ul>
     *   <li>if the current OS is not Mac OS X or if the version is not 10.4 or higher (i.e. Spotlight is not available)</li>
     *   <li>if the specified file is not a LocalFile and does not have a LocalFile ancestor</li>
     *   <li>if the specified file has no comment</li>
     *   <li>if the comment could not be retrieved (for any reason)</li>
     *  </ul>
     * </p>
     *
     * @param file a local file
     * @return the Spotlight/Finder comment of the specified file
     */
    public static String getSpotlightComment(AbstractFile file) {
        if(!(OsFamily.MAC_OS_X.isCurrent() && OsVersion.MAC_OS_X_10_4.isCurrentOrHigher()))
            return null;

        InputStream pin = null;
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"mdls", "-name", "kMDItemFinderComment", file.getAbsolutePath()});
            process.waitFor();

            if(process.exitValue()!=0)
                return null;

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            pin = process.getInputStream();
            StreamUtils.copyStream(pin, bout);

            // Sample output when there is a comment:
            // kMDItemFinderComment = "This is a spotlight comment"
            //
            // Sample output when there is no comment:
            // kMDItemFinderComment = (null)
            String output = new String(bout.toByteArray());
            Matcher matcher = MDLS_COMMENT_PATTERN.matcher(output);

            if(matcher.find()) {
                // Strip off the quotes surrounding the comment
                String group = matcher.group();
                return group.substring(1, group.length()-1);
            }

            return null;
        }
        catch(Exception e) {
            LOGGER.info("Caught exception", e);

            return null;
        }
        finally {
            if(pin!=null)
                try { pin.close(); }
                catch(IOException e) {}
        }
    }
}
