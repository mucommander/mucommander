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

import com.mucommander.commons.file.ModificationDateBasedMonitoredFile;
import com.mucommander.commons.file.MonitoredFile;

/**
 * //TODO do we need?
 * This class is an implementation of {@link MonitoredFile} that enables
 * to detect changes to a folder in Google Drive. In Google Drive, the modification date
 * of a folder does not change when files are added to or removed from the folder. This
 * mechanism checks the last modification date of the files in this folder and this way
 * can detect when a file is added to the folder or a file in this folder is changed.
 * This mechanism is unable to detect if a file is removed from the folder though.
 * @author Arik Hadas
 */
public class GoogleCloudStorageMonitoredFile extends ModificationDateBasedMonitoredFile {

    private final GoogleCloudStorageFile file;

    public GoogleCloudStorageMonitoredFile(GoogleCloudStorageFile file) {
        super(file);
        this.file = file;
    }

//    @Override
//    public long getDate() {
//        try (GoogleCloudStorageConnectionHandler connHandler = file.getConnHandler()) {
//            FileList result = connHandler.getConnection().files().list()
//                    .setFields("files(id,name,parents,size,modifiedTime,mimeType)")
//                    .setQ(String.format("'%s' in parents", file.getId()))
//                    .execute();
//            return result.getFiles().stream()
//                    .filter(this::isNotFolder)
//                    .map(File::getModifiedTime)
//                    .map(DateTime::getValue)
//                    .max(Long::compareTo)
//                    .orElse(0l);
//        } catch (IOException e) {
//            LOGGER.error("failed to retrieve folder modification date", e);
//            return 0;
//        } catch (RuntimeException e) {
//            LOGGER.error("runtime exception while retrieving folder modification date", e);
//            return 0;
//        }
//    }

//    protected boolean isNotFolder(File file) {
//        return !GoogleCloudStorageFile.isFolder(file);
//    }

    public GoogleCloudStorageFile getUnderlyingFile() {
        return file;
    }
}
