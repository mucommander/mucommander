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
import dev.barebones.commander.commons.file.FileFactory;
import dev.barebones.commander.commons.file.FilePermissions;
import dev.barebones.commander.commons.file.FileURL;
import dev.barebones.commander.commons.file.UnsupportedFileOperationException;

import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The S3 root: an "endpoint" pseudo-folder whose children are the
 * buckets visible to the current credentials. Mapped to the URL
 * {@code s3://endpoint/} (no path).
 */
public class S3Root extends S3File {

    public S3Root(FileURL url, S3Connection connection) {
        super(url, connection);
    }

    @Override
    public boolean isDirectory() { return true; }

    @Override
    public boolean exists() { return true; }

    @Override
    public long getDate() { return 0L; }

    @Override
    public long getSize() { return 0L; }

    @Override
    public FilePermissions getPermissions() {
        return READ_ONLY_PERMISSIONS;
    }

    @Override
    public AbstractFile[] ls() throws IOException {
        try {
            ListBucketsResponse resp = connection.client().listBuckets();
            List<Bucket> buckets = resp.buckets();
            List<AbstractFile> out = new ArrayList<>(buckets.size());
            for (Bucket b : buckets) {
                FileURL childURL = (FileURL) fileURL.clone();
                childURL.setPath("/" + b.name() + "/");
                out.add(FileFactory.getFile(childURL));
            }
            return out.toArray(new AbstractFile[0]);
        } catch (S3Exception e) {
            throw toIOException(e, fileURL);
        }
    }

    @Override
    public void mkdir() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(
            dev.barebones.commander.commons.file.FileOperation.CREATE_DIRECTORY);
    }

    @Override
    public InputStream getInputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(
            dev.barebones.commander.commons.file.FileOperation.READ_FILE);
    }

    @Override
    public OutputStream getOutputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(
            dev.barebones.commander.commons.file.FileOperation.WRITE_FILE);
    }

    @Override
    public void delete() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(
            dev.barebones.commander.commons.file.FileOperation.DELETE);
    }

    @Override
    public void renameTo(AbstractFile destFile) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(
            dev.barebones.commander.commons.file.FileOperation.RENAME);
    }
}
