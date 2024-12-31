package com.mucommander.commons.file.protocol.smb;

import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.share.DiskShare;
import com.mucommander.commons.file.*;
import com.mucommander.commons.file.connection.ConnectionHandler;
import com.mucommander.commons.file.connection.ConnectionHandlerFactory;
import com.mucommander.commons.file.connection.ConnectionPool;
import com.mucommander.commons.file.protocol.ProtocolFile;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

public class SmbjFile extends ProtocolFile implements ConnectionHandlerFactory {

    private SmbjFile(FileURL url) {
        super(url);
    }

    public static SmbjFile create(FileURL url) {
        return new SmbjFile(url);
    }

    @Override
    public long getDate() {
        System.out.println("getDate"); // TODO - debug only
        return 0;
    }

    @Override
    public void changeDate(long lastModified) throws IOException, UnsupportedFileOperationException {
        System.out.println("changeDate"); // TODO - debug only
    }

    @Override
    public long getSize() {
        System.out.println("getSize"); // TODO - debug only
        return 0;
    }

    @Override
    public AbstractFile getParent() {
        System.out.println("getParent"); // TODO - debug only
        return null;
    }

    @Override
    public void setParent(AbstractFile parent) {
        System.out.println("parent"); // TODO - debug only
    }

    @Override
    public boolean exists() {
        try {
            boolean parentShare = isParentShare();

            // TODO - child
            return parentShare;
        } catch (Exception e) {
            e.printStackTrace(); // TODO - log
        }

        return false;
    }

    @Override
    public FilePermissions getPermissions() {
        System.out.println("getPermissions"); // TODO - debug only
        if (isParentShare()) {
            return new SimpleFilePermissions(0, 0);
        } else {
            // TODO
            System.out.println("child");
        }
        return null;
    }

    @Override
    public PermissionBits getChangeablePermissions() {
        System.out.println("getChangeablePermissions"); // TODO - debug only
        return null;
    }

    @Override
    public void changePermission(PermissionAccess access, PermissionType permission, boolean enabled) throws IOException, UnsupportedFileOperationException {
        System.out.println("changePermission"); // TODO - debug only
    }

    @Override
    public String getOwner() {
        System.out.println("getOwner"); // TODO - debug only
        return "";
    }

    @Override
    public boolean canGetOwner() {
        System.out.println("canGetOwner"); // TODO - debug only
        return false;
    }

    @Override
    public String getGroup() {
        System.out.println("getGroup"); // TODO - debug only
        return "";
    }

    @Override
    public boolean canGetGroup() {
        System.out.println("canGetGroup"); // TODO - debug only
        return false;
    }

    @Override
    public boolean isDirectory() {
        System.out.println("isDirectory"); // TODO - debug only
        boolean parentShare = isParentShare();
        return parentShare; // TODO - there are more cases
    }

    @Override
    public boolean isSymlink() {
        System.out.println("isSymlink"); // TODO - debug only
        return false;
    }

    @Override
    public boolean isSystem() {
        System.out.println("isSystem"); // TODO - debug only
        return false;
    }

    @Override
    public AbstractFile[] ls() throws IOException, UnsupportedFileOperationException {
        System.out.println("ls"); // TODO - debug only
        if (isParentShare()) {
            List<FileIdBothDirectoryInformation> list = getDiskShare().list("");
            return list.stream()
                    .filter(f -> !f.getFileName().equals("."))
                    .filter(f -> !f.getFileName().equals(".."))
                    .map(f -> {
                        FileURL childURL = (FileURL)fileURL.clone();
                        // childURL.setHost(f.getServer());
                        // childURL.setPath(file.getURL().getPath());
                        try {
                            return FileFactory.getFile(childURL, this, Collections.singletonMap("parentSmbFile", f));
                        } catch (IOException e) {
                            System.out.println("Error getting file: " + childURL);
                            return null;
                        }
                    })
                    .toArray(AbstractFile[]::new);
        } else {
            // TODO
        }

        return new AbstractFile[0];
    }

    @Override
    public void mkdir() throws IOException, UnsupportedFileOperationException {
        System.out.println("mkdir"); // TODO - debug only
    }

    @Override
    public InputStream getInputStream() throws IOException, UnsupportedFileOperationException {
        System.out.println("getInputStream"); // TODO - debug only
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException, UnsupportedFileOperationException {
        System.out.println("getOutputStream"); // TODO - debug only
        return null;
    }

    @Override
    public OutputStream getAppendOutputStream() throws IOException, UnsupportedFileOperationException {
        System.out.println("getAppendOutputStream"); // TODO - debug only
        return null;
    }

    @Override
    public RandomAccessInputStream getRandomAccessInputStream() throws IOException, UnsupportedFileOperationException {
        System.out.println("getRandomAccessInputStream"); // TODO - debug only
        return null;
    }

    @Override
    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException, UnsupportedFileOperationException {
        System.out.println("getRandomAccessOutputStream"); // TODO - debug only
        return null;
    }

    @Override
    public void delete() throws IOException, UnsupportedFileOperationException {
        System.out.println("delete"); // TODO - debug only
    }

    @Override
    public void renameTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
        System.out.println("renameTo"); // TODO - debug only
    }

    @Override
    public void copyRemotelyTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
        System.out.println("copyRemotelyTo"); // TODO - debug only
    }

    @Override
    public long getFreeSpace() throws IOException, UnsupportedFileOperationException {
        System.out.println("getFreeSpace"); // TODO - debug only
        return 0;
    }

    @Override
    public long getTotalSpace() throws IOException, UnsupportedFileOperationException {
        System.out.println("getTotalSpace"); // TODO - debug only
        return 0;
    }

    @Override
    public Object getUnderlyingFileObject() {
        System.out.println("getUnderlyingFileObject"); // TODO - debug only
        return null;
    }

    @Override
    public ConnectionHandler createConnectionHandler(FileURL location) {
        return new SmbjConnectionHandler(location);
    }

    private DiskShare getDiskShare() throws IOException {
        SmbjConnectionHandler connectionHandler = (SmbjConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, false /* TODO - should be true? */);
        if (!connectionHandler.checkConnection()) {
            throw new RuntimeException("Connection failed"); // TODO - log
        }
        return connectionHandler.getDiskShare();
    }

    private boolean isParentShare() {
        DiskShare diskShare = null;
        try {
            diskShare = getDiskShare();
            return diskShare != null &&
                    diskShare.getSmbPath().getHostname().equals(fileURL.getHost()) &&
                    diskShare.getSmbPath().getShareName().equals(fileURL.getFilename());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
