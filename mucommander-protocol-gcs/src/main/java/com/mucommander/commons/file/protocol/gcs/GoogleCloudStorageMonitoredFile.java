/**
 * This file is part of muCommander, http://www.mucommander.com
 * <p>
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.commons.file.protocol.gcs;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.ModificationDateBasedMonitoredFile;
import com.mucommander.commons.file.MonitoredFile;

/**
 * This class is an implementation of {@link MonitoredFile} that enables to detect changes to a folder in Google Cloud
 * Storage. In GCS, the modification date of a folder does not change when files are added to or removed from the
 * folder. This mechanism checks the last modification date of the files in this folder and this way can detect when a
 * file is added to the folder or a file in this folder is changed. This mechanism is unable to detect if a file is
 * removed from the folder though.
 * <p>
 * Inspired from the GoogleDriveMonitoredFile class.
 *
 * @author Arik Hadas
 * @author miroslav.spak
 */
public class GoogleCloudStorageMonitoredFile extends ModificationDateBasedMonitoredFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCloudStorageMonitoredFile.class);

    private final GoogleCloudStorageAbstractFile file;

    public GoogleCloudStorageMonitoredFile(GoogleCloudStorageAbstractFile file) {
        super(file);
        this.file = file;
    }

    @Override
    public long getDate() {
        try {
            return file.listDir()
                    .filter(file -> !file.isDirectory())
                    .map(GoogleCloudStorageAbstractFile::getDate)
                    .max(Long::compareTo)
                    .orElse(0L);
        } catch (IOException ex) {
            LOGGER.error("Failed to retrieve modification date for file " + file.getURL(), ex);
            return 0;
        }
    }

    public GoogleCloudStorageAbstractFile getUnderlyingFile() {
        return file;
    }
}
