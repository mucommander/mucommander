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

package com.mucommander.commons.file.protocol.onedrive;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.ModificationDateBasedMonitoredFile;

public class OneDriveMonitoredFile extends ModificationDateBasedMonitoredFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(OneDriveMonitoredFile.class);

    private OneDriveFile file;
    private Integer numOfChildren;

    public OneDriveMonitoredFile(OneDriveFile file) {
        super(file);
        this.file = file;
    }

    @Override
    public boolean isChanged(boolean periodicCheck) {
        return false;
    }

    @Override
    public long getDate() {
        try (OneDriveConnHandler connHandler = file.getConnHandler()) {
            AbstractFile[] children = file.ls();
            if (numOfChildren != null && numOfChildren != children.length)
                return System.currentTimeMillis();
            numOfChildren = children.length;
            return Arrays.stream(children)
                    .map(AbstractFile::getDate)
                    .max(Long::compareTo)
                    .orElse(0l);
        } catch (IOException e) {
            LOGGER.error("failed to retrieve folder modification date", e);
            return 0;
        } catch (RuntimeException e) {
            LOGGER.error("runtime exception while retrieving folder modification date", e);
            return 0;
        }
    }

    @Override
    public OneDriveFile getUnderlyingFileObject() {
        return file;
    }
}
