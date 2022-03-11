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
package com.mucommander.commons.file.protocol.gdrive;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.mucommander.commons.file.ModificationDateBasedMonitoredFile;
import com.mucommander.commons.file.MonitoredFile;

/**
 * This class is an implementation of {@link MonitoredFile} that enables
 * to detect changes to a folder in Google Drive. In Google Drive, the modification date
 * of a folder does not change when files are added to or removed from the folder. This
 * mechanism checks the last modification date of the files in this folder and this way
 * can detect when a file is added to the folder or a file in this folder is changed.
 * This mechanism is unable to detect if a file is removed from the folder though.
 * @author Arik Hadas
 */
public class GoogleDriveMonitoredFile extends ModificationDateBasedMonitoredFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDriveMonitoredFile.class);

    private GoogleDriveFile file;

    public GoogleDriveMonitoredFile(GoogleDriveFile file) {
        super(file);
        this.file = file;
    }

    @Override
    public long getDate() {
        try (GoogleDriveConnHandler connHandler = file.getConnHandler()) {
            FileList result = connHandler.getConnection().files().list()
                    .setFields("files(id,name,parents,size,modifiedTime,mimeType)")
                    .setQ(String.format("'%s' in parents", file.getId()))
                    .execute();
            return result.getFiles().stream()
                    .filter(this::isNotFolder)
                    .map(File::getModifiedTime)
                    .map(DateTime::getValue)
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

    protected boolean isNotFolder(File file) {
        return !GoogleDriveFile.isFolder(file);
    }

    public GoogleDriveFile getUnderlyingFile() {
        return file;
    }
}
