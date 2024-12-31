package com.mucommander.commons.file.protocol.smb;

import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
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
import java.util.List;

public class SmbjFile extends ProtocolFile implements ConnectionHandlerFactory {

    private final SmbjFile parentFile;

    private final FileIdBothDirectoryInformation fileIdBothDirectoryInformation;

    private SmbjFile(FileURL url, SmbjFile parentFile, FileIdBothDirectoryInformation fileIdBothDirectoryInformation) {
        super(url);
        this.parentFile = parentFile;
        this.fileIdBothDirectoryInformation = fileIdBothDirectoryInformation;
    }

    public static SmbjFile create(FileURL url) {
        return new SmbjFile(url, null, null);
    }

    @Override
    public long getDate() {
        return fileIdBothDirectoryInformation != null ? fileIdBothDirectoryInformation.getChangeTime().toEpochMillis() : 0;
    }

    @Override
    public void changeDate(long lastModified) throws IOException, UnsupportedFileOperationException {
        System.out.println("changeDate"); // TODO - debug only
    }

    @Override
    public long getSize() {
        return fileIdBothDirectoryInformation != null ? fileIdBothDirectoryInformation.getAllocationSize() : 0;
    }

    @Override
    public AbstractFile getParent() {
        return this.parentFile;
    }

    @Override
    public void setParent(AbstractFile parent) {
        System.out.println("parent"); // TODO - debug only
    }

    @Override
    public boolean exists() {
        try {
            return doWithConnectionHandler(c -> {
                try {
                    c.getDiskShare();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace(); // TODO - log
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false; // TODO - find a better solution (for the startup case)
        }

    }

    @Override
    public FilePermissions getPermissions() {
        System.out.println("getPermissions"); // TODO - debug only
//        if (isParentShare()) {
//            return new SimpleFilePermissions(0, 0);
//        } else {
//            // TODO
//            System.out.println("child");
//        }
        return new SimpleFilePermissions(0, 0); // TODO
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
        if (this.parentFile == null) {
            return true;
        }

        // TODO - there are other cases!

        return false;
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
        return doWithConnectionHandler(c -> {
            if (parentFile == null) {
                List<FileIdBothDirectoryInformation> list = c.getDiskShare().list("");
                return list.stream()
                        .filter(f -> !f.getFileName().equals("."))
                        .filter(f -> !f.getFileName().equals(".."))
                        .map(f -> {
                            FileURL childURL = (FileURL)fileURL.clone();
                            childURL.setHost(fileURL.getHost());
                            childURL.setPath(fileURL.getPath() + "/" + f.getFileName());

                            return new SmbjFile(childURL, this, f); // TODO - propagate connection handler?
                        })
                        .toArray(AbstractFile[]::new);
            } else {
                // TODO
            }

            return new AbstractFile[0];
        });
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

    private <T> T doWithConnectionHandler(SmbjLogic<T> smbjLogic) {
        SmbjConnectionHandler connectionHandler = null;
        try {
            connectionHandler = (SmbjConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
            connectionHandler.checkConnection();
            return smbjLogic.doLogic(connectionHandler);
        } catch (Exception e) {
            throw new RuntimeException(e); // TODO - consider a special case for IOException
        }
        finally {
            if (connectionHandler != null) {
                connectionHandler.releaseLock();
            }
        }
    }

}
