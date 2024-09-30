package com.mucommander.commons.file.protocol.smb;

import com.mucommander.commons.file.*;
import com.mucommander.commons.file.protocol.ProtocolFile;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SmbjFile extends ProtocolFile {

    protected SmbjFile(FileURL url) {
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
    public AbstractFile getParent() {
        return null;
    }

    @Override
    public void setParent(AbstractFile parent) {

    }

    @Override
    public boolean exists() {
        return false;
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
    public void changePermission(PermissionAccess access, PermissionType permission, boolean enabled) throws IOException, UnsupportedFileOperationException {

    }

    @Override
    public String getOwner() {
        return "";
    }

    @Override
    public boolean canGetOwner() {
        return false;
    }

    @Override
    public String getGroup() {
        return "";
    }

    @Override
    public boolean canGetGroup() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return false;
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
    public AbstractFile[] ls() throws IOException, UnsupportedFileOperationException {
        return new AbstractFile[0];
    }

    @Override
    public void mkdir() throws IOException, UnsupportedFileOperationException {

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
    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException, UnsupportedFileOperationException {
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
}
