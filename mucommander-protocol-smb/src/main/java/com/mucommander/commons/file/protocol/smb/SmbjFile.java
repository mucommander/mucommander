package com.mucommander.commons.file.protocol.smb;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msdtyp.FileTime;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileBasicInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.share.File;
import com.mucommander.commons.file.*;
import com.mucommander.commons.file.connection.ConnectionHandler;
import com.mucommander.commons.file.connection.ConnectionHandlerFactory;
import com.mucommander.commons.file.connection.ConnectionPool;
import com.mucommander.commons.file.protocol.ProtocolFile;
import com.mucommander.commons.file.protocol.smb.smbj.permissions.SmbjFilePermissions;
import com.mucommander.commons.file.protocol.smb.smbj.stream.SmbjInputStreamWrapper;
import com.mucommander.commons.file.protocol.smb.smbj.stream.SmbjOutputStreamWrapper;
import com.mucommander.commons.file.protocol.smb.smbj.stream.random.SmbjRandomAccessInputStream;
import com.mucommander.commons.file.protocol.smb.smbj.stream.random.SmbjRandomAccessOutputStream;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.List;

public class SmbjFile extends ProtocolFile implements ConnectionHandlerFactory {

    private final FileIdBothDirectoryInformation fileIdBothDirectoryInformation;

    private SmbjFile parentFile;

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
        return fileIdBothDirectoryInformation != null ?
                fileIdBothDirectoryInformation.getChangeTime().toEpochMillis() : 0;
    }

    @Override
    public void changeDate(long lastModified) throws IOException, UnsupportedFileOperationException {
        if (parentFile == null) {
            // Don't try to change date for the actual share
            return;
        }

        doWithConnectionHandler(c -> {
            try (File file = openFileForWrite(c)) {
                FileBasicInformation basicInformation = file.getFileInformation().getBasicInformation();

                file.setFileInformation(new FileBasicInformation(
                        basicInformation.getCreationTime(),
                        basicInformation.getLastAccessTime(),
                        FileTime.ofEpochMillis(lastModified),
                        basicInformation.getChangeTime(),
                        basicInformation.getFileAttributes()
                ));
            }
            return null;
        });
    }

    @Override
    public long getSize() {
        return fileIdBothDirectoryInformation != null ?
                fileIdBothDirectoryInformation.getEndOfFile() : 0;
    }

    @Override
    public AbstractFile getParent() {
        return this.parentFile;
    }

    @Override
    public void setParent(AbstractFile parent) {
        if (parent instanceof SmbjFile smbjFileParent) {
            this.parentFile = smbjFileParent;
        } else {
            throw new RuntimeException(String.format("Parent is not a SmbjFile [parent = %s]", parent));
        }
    }

    @Override
    public boolean exists() {
        try {
            return doWithConnectionHandler(c -> {
                try {
                    if (parentFile == null) {
                        // Top level share
                        c.getDiskShare();
                        return true;
                    } else {
                        // Actual file
                        try (File f = openFileForRead(c)) {
                            return true;
                        }
                    }

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
        if (this.parentFile == null) {
            return new SmbjFilePermissions(true, true);
        } else {
            boolean canRead = false;
            boolean canWrite = false;
            try (InputStream is = getInputStream()) {
                canRead = true;
            } catch (Exception e) {
                e.printStackTrace(); // TODO - debug log
            }

            try (OutputStream os = getOutputStream()) {
                canWrite = true;
            } catch (Exception e) {
                e.printStackTrace(); // TODO - debug log
            }

            return new SmbjFilePermissions(canRead, canWrite);
        }
    }

    @Override
    public PermissionBits getChangeablePermissions() {
        return SmbjFilePermissions.EMPTY_MASK;
    }

    @Override
    public void changePermission(PermissionAccess access, PermissionType permission, boolean enabled) throws IOException, UnsupportedFileOperationException {
        // Not available for SMB
    }

    @Override
    public String getOwner() {
        // Not available for SMB
        return null;
    }

    @Override
    public boolean canGetOwner() {
        // Not available for SMB
        return false;
    }

    @Override
    public String getGroup() {
        // Not available for SMB
        return null;
    }

    @Override
    public boolean canGetGroup() {
        // Not available for SMB
        return false;
    }

    @Override
    public boolean isDirectory() {
        if (this.parentFile == null) {
            // This is a share root
            return true;
        } else if (fileIdBothDirectoryInformation != null) {
            return isDirectory(fileIdBothDirectoryInformation);
        } else {
            // Edge case - should not happen
            return false;
        }
    }

    @Override
    public boolean isSymlink() {
        if (fileIdBothDirectoryInformation != null) {
            return isSymlink(fileIdBothDirectoryInformation);
        } else {
            return false;
        }
    }

    @Override
    public boolean isSystem() {
        if (fileIdBothDirectoryInformation != null) {
            return isSystem(fileIdBothDirectoryInformation);
        } else {
            return false;
        }
    }

    @Override
    public AbstractFile[] ls() throws IOException, UnsupportedFileOperationException {
        return doWithConnectionHandler(c -> {
            String path = parentFile == null ? "" : getFileName();

            List<FileIdBothDirectoryInformation> list = c.getDiskShare().list(path);
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
        });
    }

    @Override
    public void mkdir() throws IOException, UnsupportedFileOperationException {
        doWithConnectionHandler(c -> {
            String filename = getFileName();
            c.getDiskShare().mkdir(filename);
            return null;
        });
    }

    @Override
    public InputStream getInputStream() throws IOException, UnsupportedFileOperationException {
        return doWithConnectionHandler(c -> {
            File file = openFileForRead(c);
            return new SmbjInputStreamWrapper(file.getInputStream(), file);
        }); // TODO - consider catching RuntimeException and examining internal exception for IOException or UnsupportedFileOperationException
    }

    @Override
    public OutputStream getOutputStream() throws IOException, UnsupportedFileOperationException {
        return getOutputStream(false);
    }

    @Override
    public OutputStream getAppendOutputStream() throws IOException, UnsupportedFileOperationException {
        return getOutputStream(true);
    }

    @Override
    public RandomAccessInputStream getRandomAccessInputStream() throws IOException, UnsupportedFileOperationException {
        return doWithConnectionHandler(c -> {
            File file = openFileForRead(c);
            return SmbjRandomAccessInputStream.create(file);
        });
    }

    @Override
    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException, UnsupportedFileOperationException {
        return doWithConnectionHandler(c -> {
            File file = openFileForWrite(c);
            return SmbjRandomAccessOutputStream.create(file);
        });
    }

    @Override
    public void delete() throws IOException, UnsupportedFileOperationException {
        doWithConnectionHandler(c -> {
            c.getDiskShare().rm(getFileName());
            return null;
        });
    }

    @Override
    public void renameTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
        doWithConnectionHandler(c -> {
            try (File file = c.getDiskShare().openFile(
                    getFileName(),
                    EnumSet.of(AccessMask.GENERIC_WRITE, AccessMask.DELETE),
                    EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                    SMB2ShareAccess.ALL,
                    SMB2CreateDisposition.FILE_OPEN,
                    null)) {
                file.rename(destFile.getName());
            }
            return null;
        });
    }

    @Override
    public void copyRemotelyTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
        checkCopyRemotelyPrerequisites(destFile, false, false);
        doWithConnectionHandler(c -> {
            try (File source = openFileForRead(c);
                File dest = openFileForWrite(c, destFile.getPath())) {
                source.remoteCopyTo(dest);
            }
            return null;
        });
    }

    @Override
    public long getFreeSpace() throws IOException, UnsupportedFileOperationException {
        return doWithConnectionHandler(c -> c.getDiskShare().getShareInformation().getFreeSpace());
    }

    @Override
    public long getTotalSpace() throws IOException, UnsupportedFileOperationException {
        return doWithConnectionHandler(c -> c.getDiskShare().getShareInformation().getTotalSpace());
    }

    @Override
    public Object getUnderlyingFileObject() {
        // No point externalizing file info as it useless without the DiskShare object
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

    private boolean isDirectory(FileIdBothDirectoryInformation info) {
        return (info.getFileAttributes() & com.hierynomus.msfscc.FileAttributes.FILE_ATTRIBUTE_DIRECTORY.getValue()) != 0;
    }

    private boolean isSymlink(FileIdBothDirectoryInformation info) {
        return (info.getFileAttributes() & com.hierynomus.msfscc.FileAttributes.FILE_ATTRIBUTE_REPARSE_POINT.getValue()) != 0;
    }

    private boolean isSystem(FileIdBothDirectoryInformation info) {
        return (info.getFileAttributes() & FileAttributes.FILE_ATTRIBUTE_SYSTEM.getValue()) != 0;
    }

    private OutputStream getOutputStream(boolean append) {
        return doWithConnectionHandler(c -> {
            File file = openFileForWrite(c);
            return new SmbjOutputStreamWrapper(file.getOutputStream(append), file);
        });
    }

    private File openFileForRead(SmbjConnectionHandler ch) {
        return ch.getDiskShare().openFile(
                getFileName(),
                EnumSet.of(AccessMask.GENERIC_READ),
                null,
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN,
                null);
    }

    private File openFileForWrite(SmbjConnectionHandler ch) {
        return openFileForWrite(ch, getFileName());
    }

    private File openFileForWrite(SmbjConnectionHandler ch, String path) {
        return ch.getDiskShare().openFile(
                path,
                EnumSet.of(AccessMask.GENERIC_WRITE, AccessMask.FILE_WRITE_DATA, AccessMask.FILE_READ_ATTRIBUTES),
                EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN_IF,
                null);
    }

    private String getFileName() {
        return fileIdBothDirectoryInformation != null ?
                fileIdBothDirectoryInformation.getFileName() : this.fileURL.getFilename();
    }

}
