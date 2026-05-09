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
import dev.barebones.commander.commons.file.FileURL;
import dev.barebones.commander.commons.file.UnsupportedFileOperationException;

import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * One S3 bucket. Path shape: {@code s3://endpoint/BUCKET/}.
 *
 * Listing uses {@code ListObjectsV2} with delimiter='/' which gives
 * pseudo-directory semantics: object keys ending in '/' or sharing
 * a prefix render as folders; other keys render as files.
 */
public class S3Bucket extends S3File {

    public S3Bucket(FileURL url, S3Connection connection) {
        super(url, connection);
    }

    @Override
    public boolean isDirectory() { return true; }

    @Override
    public boolean exists() {
        try {
            connection.client().headBucket(
                HeadBucketRequest.builder().bucket(parsed.bucket()).build());
            return true;
        } catch (NoSuchBucketException ignored) {
            return false;
        } catch (S3Exception e) {
            // Permission errors come back as 403 — the bucket may
            // exist but we can't see it. Treat as "does not exist
            // from our perspective" rather than fabricating an IO
            // failure on a stat-style check.
            return false;
        }
    }

    @Override
    public long getDate() { return 0L; }

    @Override
    public long getSize() { return 0L; }

    @Override
    public AbstractFile[] ls() throws IOException {
        return S3Listing.listChildrenAsFiles(connection, fileURL, parsed.bucket(), "");
    }

    @Override
    public void mkdir() throws IOException {
        try {
            connection.client().createBucket(
                CreateBucketRequest.builder().bucket(parsed.bucket()).build());
        } catch (S3Exception e) {
            throw toIOException(e, fileURL);
        }
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
    public void delete() throws IOException {
        try {
            connection.client().deleteBucket(
                DeleteBucketRequest.builder().bucket(parsed.bucket()).build());
        } catch (S3Exception e) {
            throw toIOException(e, fileURL);
        }
    }

    @Override
    public void renameTo(AbstractFile destFile) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(
            dev.barebones.commander.commons.file.FileOperation.RENAME);
    }
}
