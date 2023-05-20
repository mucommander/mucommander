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

package com.mucommander.commons.file.protocol.adb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.RemoteFile;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.PermissionAccess;
import com.mucommander.commons.file.PermissionBits;
import com.mucommander.commons.file.PermissionType;
import com.mucommander.commons.file.UnsupportedFileOperation;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.protocol.ProtocolFile;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;

/**
 * @author Oleg Trifonov, Arik Hadas
 * Created on 09/09/15.
 */
public class AdbFile extends ProtocolFile {

    private final RemoteFile remoteFile;
    private List<RemoteFile> childs;
    private AbstractFile parent;
    private JadbConnection jadbConnection;
    private String rootFolder;

    private static FileURL lastModifiedPath;        // FIXME that's a bad way to detect directory changes

    /**
     * Creates a new file instance with the given URL.
     *
     * @param url the FileURL instance that represents this file's location
     */
    private AdbFile(FileURL url, RemoteFile remoteFile) throws IOException {
        super(url);

        if (remoteFile == null) {
            JadbDevice device = getDevice(url);
            if (device == null) {
                throw new IOException("ADB file error");
            }

            String path = url.getPath();
            if (path.isEmpty() || "\\".equals(path)) {
                path = "/";
            }
            remoteFile = tryLs(device, path);
            if (remoteFile == null && "/".equals(path)) {
                remoteFile = tryLs(device, "/sdcard/");
                if (remoteFile != null) {
                    rootFolder = "/sdcard/";
                }
            }
            if (remoteFile == null && "/".equals(path)) {
                remoteFile = tryLs(device, "/mnt/sdcard/");
                if (remoteFile != null) {
                    rootFolder = "/mnt/sdcard/";
                }
            }
            closeConnection();
        } else {
            if (remoteFile.isDirectory()) {
                rebuildChildrenList(url);
            }
        }
        if (rootFolder == null) {
            rootFolder = "/";
        }
        this.remoteFile = remoteFile;
    }

    private RemoteFile tryLs(JadbDevice device, String path) throws IOException {
        RemoteFile result = null;
        try {
            List<RemoteFile> files = device.list(path);
            childs = new ArrayList<>();
            for (RemoteFile rf : files) {
                if (".".equals(rf.getPath())) {
                    result = rf;
                } else {
                    childs.add(rf);
                }
            }
        } catch (JadbException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void rebuildChildrenList(FileURL url) throws IOException {
        try {
            JadbDevice device = getDevice(url);
            List<RemoteFile> files = device.list("/" + url.getPath());
            childs = new ArrayList<>();
            for (RemoteFile rf : files) {
                if (!".".equals(rf.getPath())) {
                    childs.add(rf);
                }
            }
        } catch (JadbException e) {
            e.printStackTrace();
        }
        closeConnection();
    }

    JadbDevice getDevice(FileURL url) throws IOException {
        closeConnection();
        jadbConnection = new JadbConnection();
        JadbDevice device = null;
        try {
            List<JadbDevice> devices = jadbConnection.getDevices();
            final String host = url.getHost();
            for (JadbDevice dev : devices) {
                if (dev.getSerial().equalsIgnoreCase(host)) {
                    device = dev;
                    break;
                }
            }
        } catch (JadbException e) {
            e.printStackTrace();
        }
        return device;
    }

    private void closeConnection() {
        if (jadbConnection != null) {
            jadbConnection = null;
        }
    }


    AdbFile(FileURL url) throws IOException {
        this(url, null);
    }


    @Override
    public long getDate() {
        if (remoteFile == null) {
            return 0;
        }
        return remoteFile.getLastModified();
    }

    @Override
    public long getSize() {
        if (remoteFile == null) {
            return 0;
        }
        return remoteFile.getSize();
    }

    @Override
    public AbstractFile getParent() {
        if (parent == null && !"/".equals(getURL().getPath())) {
            try {
                parent = new AdbFile(getURL().getParent(), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return parent;
    }

    @Override
    public void setParent(AbstractFile parent) {

    }

    @Override
    public boolean exists() {
        AdbFile adbParent = (AdbFile) getParent();
        if (adbParent == null || adbParent.childs == null) {
            String path = getURL().getPath();
            return "/".equals(path);
        }
        for (RemoteFile rf : adbParent.childs) {
            if (getName().equals(rf.getPath())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public FilePermissions getPermissions() {
        return childs == null ? FilePermissions.DEFAULT_FILE_PERMISSIONS : FilePermissions.DEFAULT_DIRECTORY_PERMISSIONS;
        // TODO !!!
    }

    @Override
    public PermissionBits getChangeablePermissions() {
        return null;
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
        return remoteFile == null || remoteFile.isDirectory();
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
    public AbstractFile[] ls() throws IOException {
        if (getURL().equals(lastModifiedPath)) {
            rebuildChildrenList(lastModifiedPath);
            lastModifiedPath = null;
        }
        if (childs == null) {
            return null;
        }
        return childs.stream()
                .filter(rf -> !"..".equals(rf.getPath()))
                .map(rf -> {
                    FileURL url;
                    try {
                        url = FileURL.getFileURL(getURL() + rootFolder + rf.getPath());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        return null;
                    }
                    AdbFile adbFile;
                    try {
                        adbFile = new AdbFile(url, rf);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                    adbFile.parent = AdbFile.this;
                    return adbFile;
                })
                .filter(Objects::nonNull)
                .toArray(AbstractFile[]::new);
    }

    @Override
    public void mkdir() throws IOException {
        JadbDevice device = getDevice(getURL());
        if (device == null) {
            closeConnection();
            throw new IOException("file not found: " + getURL());
        }
        try {
            device.executeShell("mkdir", getURL().getPath());
        } catch (JadbException e) {
            throw new IOException(e);
        }
        // TODO    doesn't work without this delay    FIXME
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        closeConnection();
        if (getParent() instanceof AdbFile) {
            AdbFile parent = (AdbFile) getParent();
            lastModifiedPath = parent.getURL();
            parent.rebuildChildrenList(parent.getURL());
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new AdbInputStream(this);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public OutputStream getAppendOutputStream() {
        return null;
    }

    @Override
    public RandomAccessInputStream getRandomAccessInputStream() {
        return null;
    }

    @Override
    public RandomAccessOutputStream getRandomAccessOutputStream() {
        return null;
    }

    private void finishFileOperation() throws IOException {
        // TODO    doesn't work without this delay    FIXME
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        closeConnection();
        if (getParent() instanceof AdbFile) {
            AdbFile parent = (AdbFile) getParent();
            lastModifiedPath = parent.getURL();
            parent.rebuildChildrenList(parent.getURL());
        }
    }


    @Override
    public void delete() throws IOException {
        JadbDevice device = getDevice(getURL());
        if (device == null) {
            closeConnection();
            throw new IOException("file not found: " + getURL());
        }
        try {
            if (isDirectory()) {
                device.executeShell("rmdir", getURL().getPath());
            } else {
                device.executeShell("rm", getURL().getPath());
            }
        } catch (JadbException e) {
            closeConnection();
            e.printStackTrace();
            throw new IOException(e);
        }
        finishFileOperation();
    }

    @Override
    public void renameTo(AbstractFile destFile) throws IOException {
        JadbDevice device = getDevice(getURL());
        if (device == null) {
            closeConnection();
            throw new IOException("file not found: " + getURL());
        }
        try {
            device.executeShell("mv", getURL().getPath(), destFile.getURL().getPath());
        } catch (JadbException e) {
            throw new IOException(e);
        }
        finishFileOperation();
    }

    @Override
    @UnsupportedFileOperation
    public void copyRemotelyTo(AbstractFile destFile) {
    }

    @Override
    public long getFreeSpace() {
        return 0;
    }

    @Override
    public long getTotalSpace() {
        return 0;
    }

    @Override
    public Object getUnderlyingFileObject() {
        return null;
    }


    @Override
    public boolean isFileOperationSupported(FileOperation op) {
        return op != FileOperation.WRITE_FILE && super.isFileOperationSupported(op);
    }

    public void pushTo(AbstractFile destFile) throws IOException {
        JadbDevice device = getDevice(getURL());
        if (device == null) {
            closeConnection();
            throw new IOException("file not found: " + getURL());
        }
        try {
            device.pull(new RemoteFile(getURL().getPath()), destFile.getOutputStream());
        } catch (JadbException e) {
            throw new IOException(e);
        }
        closeConnection();
    }

    public void pullFrom(AbstractFile sourceFile) throws IOException {
        JadbDevice device = getDevice(getURL());
        if (device == null) {
            closeConnection();
            throw new IOException("file not found: " + getURL());
        }
        long lastModified = sourceFile.getDate();
        int mode = 0664;
        try {
            device.push(sourceFile.getInputStream(), lastModified, mode, new RemoteFile(getURL().getPath()));
        } catch (JadbException e) {
            closeConnection();
            e.printStackTrace();
            throw new IOException(e);
        }
        closeConnection();
        finishFileOperation();
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        if (getParent() instanceof AdbFile) {
//            AdbFile parent = (AdbFile)getParent();
//            lastModifiedPath = parent.getURL();
//            parent.rebuildChildrenList(parent.getURL());
//        }
    }

    @Override
    public void changeDate(long lastModified) throws IOException, UnsupportedFileOperationException {
    }

    @Override
    public void changePermission(PermissionAccess access, PermissionType permission, boolean enabled)
            throws IOException, UnsupportedFileOperationException {
    }
}