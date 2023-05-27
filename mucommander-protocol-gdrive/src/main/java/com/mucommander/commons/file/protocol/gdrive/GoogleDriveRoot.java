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
import java.io.InputStream;
import java.io.OutputStream;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.PermissionAccess;
import com.mucommander.commons.file.PermissionBits;
import com.mucommander.commons.file.PermissionType;
import com.mucommander.commons.file.UnsupportedFileOperation;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;

/**
 * Root path of Google Drive, presenting: "My Drive", "Shared with me" and "Trash" folders.
 *
 * @author Arik Hadas
 */
public class GoogleDriveRoot extends GoogleDriveFile {

    protected GoogleDriveRoot(FileURL url) {
        super(url);
    }

    @Override
    public long getDate() {
        return 0;
    }

    @Override
    public void changeDate(long lastModified) throws IOException, UnsupportedFileOperationException {
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public GoogleDriveFile getParent() {
        return null;
    }

    @Override
    public void setParent(AbstractFile parent) {
        
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public FilePermissions getPermissions() {
        return null;
    }

    @Override
    public PermissionBits getChangeablePermissions() {
        return null;
    }

    @Override
    public void changePermission(PermissionAccess access, PermissionType permission, boolean enabled)
            throws IOException, UnsupportedFileOperationException {
        
    }

    @Override
    public String getOwner() {
        return null;
    }

    @Override
    public boolean canGetOwner() {
        return false;
    }

    @Override
    public String getGroup() {
        return null;
    }

    @Override
    public boolean canGetGroup() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public boolean isSymlink() {
        return false;
    }

    @Override
    public boolean isSystem() {
        return false;
    }

    @Override
    public GoogleDriveFile[] ls() throws IOException, UnsupportedFileOperationException {
        var url = (FileURL) getURL().clone();
        url.setPath(GoogleDriveMyDrive.PATH);
        var myDrive = new GoogleDriveMyDrive(url);

        url = (FileURL) getURL().clone();
        url.setPath(GoogleDriveSharedWithMe.PATH);
        var sharedWithMe = new GoogleDriveSharedWithMe(url);

        url = (FileURL) getURL().clone();
        url.setPath(GoogleDriveTrash.PATH);
        var trash = new GoogleDriveTrash(url);

        return new GoogleDriveFile[] { myDrive, sharedWithMe, trash };
    }

    @Override
    @UnsupportedFileOperation
    public void mkdir() throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.CREATE_DIRECTORY);
    }

    @Override
    public InputStream getInputStream() throws IOException, UnsupportedFileOperationException {
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException, UnsupportedFileOperationException {
        return null;
    }

    @Override
    public OutputStream getAppendOutputStream() throws IOException, UnsupportedFileOperationException {
        return null;
    }

    @Override
    public RandomAccessInputStream getRandomAccessInputStream() throws IOException, UnsupportedFileOperationException {
        return null;
    }

    @Override
    public RandomAccessOutputStream getRandomAccessOutputStream()
            throws IOException, UnsupportedFileOperationException {
        return null;
    }

    @Override
    public void delete() throws IOException, UnsupportedFileOperationException {
    }

    @Override
    public void renameTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
    }

    @Override
    public void copyRemotelyTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
    }

    @Override
    public long getFreeSpace() throws IOException, UnsupportedFileOperationException {
        return 0;
    }

    @Override
    public long getTotalSpace() throws IOException, UnsupportedFileOperationException {
        return 0;
    }

    @Override
    public Object getUnderlyingFileObject() {
        return null;
    }

    @Override
    public boolean isRoot() {
        return true;
    }
}
