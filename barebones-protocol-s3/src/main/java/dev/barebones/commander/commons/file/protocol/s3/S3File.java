/*
 * Copyright (C) 2002-2026 muCommander contributors
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.commons.file.protocol.s3;

import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.commons.file.AuthException;
import dev.barebones.commander.commons.file.FilePermissions;
import dev.barebones.commander.commons.file.FileURL;
import dev.barebones.commander.commons.file.PermissionAccess;
import dev.barebones.commander.commons.file.PermissionBits;
import dev.barebones.commander.commons.file.PermissionType;
import dev.barebones.commander.commons.file.UnsupportedFileOperationException;
import dev.barebones.commander.commons.file.protocol.ProtocolFile;
import dev.barebones.commander.commons.io.RandomAccessInputStream;
import dev.barebones.commander.commons.io.RandomAccessOutputStream;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Common base for {@link S3Root} (the bucket list), {@link S3Bucket}
 * (one bucket), and {@link S3Object} (one object). Holds the shared
 * {@link S3Connection} and parses the URL once on construction.
 *
 * Most {@link AbstractFile} contracts that don't map onto S3 (POSIX
 * permissions, owner / group, free / total space, random-access I/O)
 * resolve to {@link UnsupportedFileOperationException} — S3 simply
 * has no analogue and surfacing a "not supported" error is more
 * honest than fabricating values.
 */
public abstract class S3File extends ProtocolFile {

    /** Read-only permissions: anyone can read, no one can write. */
    static final FilePermissions READ_ONLY_PERMISSIONS =
        new dev.barebones.commander.commons.file.SimpleFilePermissions(0444);

    /** Read+write permissions: anyone can read, owner can write. */
    static final FilePermissions READ_WRITE_PERMISSIONS =
        new dev.barebones.commander.commons.file.SimpleFilePermissions(0644);

    final S3Connection connection;
    final S3FileURL parsed;
    AbstractFile parent;
    boolean parentSet;

    S3File(FileURL url, S3Connection connection) {
        super(url);
        this.connection = Objects.requireNonNull(connection, "connection");
        this.parsed = S3FileURL.parse(url);
    }

    @Override
    public AbstractFile getParent() {
        if (!parentSet) {
            FileURL parentURL = fileURL.getParent();
            if (parentURL != null) {
                parent = dev.barebones.commander.commons.file.FileFactory.getFile(parentURL);
            }
            parentSet = true;
        }
        return parent;
    }

    @Override
    public void setParent(AbstractFile parent) {
        this.parent = parent;
        this.parentSet = true;
    }

    @Override
    public FilePermissions getPermissions() {
        return READ_WRITE_PERMISSIONS;
    }

    @Override
    public PermissionBits getChangeablePermissions() {
        return PermissionBits.EMPTY_PERMISSION_BITS;
    }

    @Override
    public void changePermission(PermissionAccess access, PermissionType permission, boolean enabled)
            throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(
            dev.barebones.commander.commons.file.FileOperation.CHANGE_PERMISSION);
    }

    @Override
    public String getOwner() { return null; }

    @Override
    public boolean canGetOwner() { return false; }

    @Override
    public String getGroup() { return null; }

    @Override
    public boolean canGetGroup() { return false; }

    @Override
    public boolean isSymlink() { return false; }

    @Override
    public boolean isSystem() { return false; }

    @Override
    public boolean isArchive() { return false; }

    @Override
    public long getFreeSpace() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(
            dev.barebones.commander.commons.file.FileOperation.GET_FREE_SPACE);
    }

    @Override
    public long getTotalSpace() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(
            dev.barebones.commander.commons.file.FileOperation.GET_TOTAL_SPACE);
    }

    @Override
    public RandomAccessInputStream getRandomAccessInputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(
            dev.barebones.commander.commons.file.FileOperation.RANDOM_READ_FILE);
    }

    @Override
    public RandomAccessOutputStream getRandomAccessOutputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(
            dev.barebones.commander.commons.file.FileOperation.RANDOM_WRITE_FILE);
    }

    @Override
    public OutputStream getAppendOutputStream() throws UnsupportedFileOperationException {
        // S3 has no native append; simulate via copy + put would be a
        // correctness foot-gun. Better to surface "not supported".
        throw new UnsupportedFileOperationException(
            dev.barebones.commander.commons.file.FileOperation.APPEND_FILE);
    }

    @Override
    public void copyRemotelyTo(AbstractFile destFile) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(
            dev.barebones.commander.commons.file.FileOperation.COPY_REMOTELY);
    }

    @Override
    public void changeDate(long lastModified) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(
            dev.barebones.commander.commons.file.FileOperation.CHANGE_DATE);
    }

    @Override
    public Object getUnderlyingFileObject() {
        return connection.client();
    }

    /** Translates S3 SDK exceptions into AuthException (403/401) or generic IOException. */
    static IOException toIOException(AwsServiceException e, FileURL url) {
        int status = e.statusCode();
        if (status == 401 || status == 403) {
            return new AuthException(url, e.getMessage());
        }
        return new IOException(e.awsErrorDetails() != null
            ? e.awsErrorDetails().errorMessage()
            : e.getMessage(), e);
    }

    static IOException toIOException(S3Exception e, FileURL url) {
        return toIOException((AwsServiceException) e, url);
    }
}
