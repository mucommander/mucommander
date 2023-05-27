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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.UnsupportedFileOperation;
import com.mucommander.commons.file.UnsupportedFileOperationException;

/**
 * Folder of trashed files
 *
 * @author Arik Hadas
 */
public class GoogleDriveTrash extends GoogleDriveFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDriveTrash.class);

    static final String PATH = "/Trash/";

    protected GoogleDriveTrash(FileURL url) {
        super(url);
    }

    @Override
    public GoogleDriveFile[] ls() throws IOException, UnsupportedFileOperationException {
        try (GoogleDriveConnHandler connHandler = getConnHandler()) {
            FileList result = connHandler.getConnection().files().list()
                    .setFields("files(id,name,parents,size,modifiedTime,mimeType,trashed)")
                    .setQ("trashed")
                    .setPageSize(1000)
                    .execute();
            List<File> files = result.getFiles();
            if (files == null || files.isEmpty()) {
                LOGGER.info("No files found.");
                return new GoogleDriveFile[0];
            }

            return files.stream()
                    .map(this::toFile)
                    .toArray(GoogleDriveFile[]::new);
        }
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    @UnsupportedFileOperation
    public void delete() throws IOException, UnsupportedFileOperationException {
    }
}
