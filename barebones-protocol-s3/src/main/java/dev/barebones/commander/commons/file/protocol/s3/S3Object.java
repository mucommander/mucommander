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
import dev.barebones.commander.commons.file.FileURL;
import dev.barebones.commander.commons.file.UnsupportedFileOperationException;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * One S3 object (or a "directory" prefix). Path shape:
 * {@code s3://endpoint/BUCKET/path/to/object[/]}.
 *
 * Two flavours:
 *   - object (key does not end in '/') — backed by a real S3 object
 *   - directory (key ends in '/') — pure prefix; HEAD/GET don't work
 *
 * The flavour can be set explicitly by the listing path
 * (see {@link #setListingMetadata}) so a {@code ls()} call doesn't
 * pay an extra HEAD round-trip per child.
 */
public class S3Object extends S3File {

    private boolean metadataKnown;
    private boolean directory;
    private long size;
    private long lastModified;

    public S3Object(FileURL url, S3Connection connection) {
        super(url, connection);
        // If the URL ends with '/', the object is a directory by construction.
        if (parsed.key().endsWith("/")) {
            this.directory = true;
            this.metadataKnown = true;
        }
    }

    /**
     * Stash the metadata from a parent listing so we can answer
     * isDirectory / getSize / getDate without re-HEADing the object.
     */
    void setListingMetadata(long size, long lastModified, boolean directory) {
        this.size = size;
        this.lastModified = lastModified;
        this.directory = directory;
        this.metadataKnown = true;
    }

    private void ensureMetadata() throws IOException {
        if (metadataKnown) return;
        try {
            HeadObjectResponse h = connection.client().headObject(
                HeadObjectRequest.builder()
                    .bucket(parsed.bucket())
                    .key(parsed.key())
                    .build());
            this.size = h.contentLength() != null ? h.contentLength() : 0L;
            this.lastModified = h.lastModified() != null
                ? h.lastModified().toEpochMilli() : 0L;
            this.directory = false;
            this.metadataKnown = true;
        } catch (NoSuchKeyException ignored) {
            this.metadataKnown = true; // exists() answers via this state
        } catch (S3Exception e) {
            throw toIOException(e, fileURL);
        }
    }

    @Override
    public boolean isDirectory() {
        try {
            ensureMetadata();
        } catch (IOException ignored) {
            return false;
        }
        return directory;
    }

    @Override
    public boolean exists() {
        try {
            ensureMetadata();
        } catch (IOException ignored) {
            return false;
        }
        // metadataKnown == true after a HEAD; if directory or non-zero
        // size or non-zero lastModified, we got a real response.
        return directory || size > 0 || lastModified > 0;
    }

    @Override
    public long getDate() {
        try { ensureMetadata(); } catch (IOException ignored) {}
        return lastModified;
    }

    @Override
    public long getSize() {
        try { ensureMetadata(); } catch (IOException ignored) {}
        return size;
    }

    @Override
    public AbstractFile[] ls() throws IOException {
        if (!isDirectory()) {
            throw new IOException("not a directory: " + fileURL);
        }
        String prefix = parsed.key();
        if (!prefix.isEmpty() && !prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        return S3Listing.listChildrenAsFiles(connection, fileURL, parsed.bucket(), prefix);
    }

    @Override
    public void mkdir() throws IOException {
        // S3 has no real directories; create an empty object whose
        // key ends with '/'. That's what the AWS Console does and
        // it's what subsequent ListObjectsV2 with delimiter='/' picks
        // up as a CommonPrefix.
        String key = parsed.key();
        if (!key.endsWith("/")) key = key + "/";
        try {
            connection.client().putObject(
                PutObjectRequest.builder()
                    .bucket(parsed.bucket())
                    .key(key)
                    .build(),
                RequestBody.empty());
            this.directory = true;
            this.metadataKnown = true;
        } catch (S3Exception e) {
            throw toIOException(e, fileURL);
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            ResponseInputStream<GetObjectResponse> stream = connection.client().getObject(
                GetObjectRequest.builder()
                    .bucket(parsed.bucket())
                    .key(parsed.key())
                    .build());
            return stream;
        } catch (S3Exception e) {
            throw toIOException(e, fileURL);
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        // Buffering approach: callers expect an OutputStream they can
        // close at any time. PutObjectRequest needs the content length
        // up-front, so we buffer everything written to a
        // ByteArrayOutputStream and PUT on close().
        //
        // Streaming uploads (multipart) belong in S3TransferManager
        // and are deferred to a follow-up PR — they need an executor
        // and credentials provider that survive across calls.
        return new BufferingPutOutputStream();
    }

    @Override
    public void delete() throws IOException {
        try {
            connection.client().deleteObject(
                DeleteObjectRequest.builder()
                    .bucket(parsed.bucket())
                    .key(parsed.key())
                    .build());
        } catch (S3Exception e) {
            throw toIOException(e, fileURL);
        }
    }

    @Override
    public void renameTo(AbstractFile destFile) throws IOException {
        if (!(destFile instanceof S3Object dest)) {
            throw new UnsupportedFileOperationException(
                dev.barebones.commander.commons.file.FileOperation.RENAME);
        }
        // S3 has no native rename — copy + delete.
        try {
            connection.client().copyObject(
                CopyObjectRequest.builder()
                    .sourceBucket(parsed.bucket())
                    .sourceKey(parsed.key())
                    .destinationBucket(dest.parsed.bucket())
                    .destinationKey(dest.parsed.key())
                    .build());
        } catch (S3Exception e) {
            throw toIOException(e, fileURL);
        }
        delete();
    }

    /** OutputStream that buffers writes and PUTs everything on close. */
    private final class BufferingPutOutputStream extends OutputStream {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private boolean closed;

        @Override
        public void write(int b) {
            buffer.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            buffer.write(b, off, len);
        }

        @Override
        public void close() throws IOException {
            if (closed) return;
            closed = true;
            byte[] payload = buffer.toByteArray();
            try {
                connection.client().putObject(
                    PutObjectRequest.builder()
                        .bucket(parsed.bucket())
                        .key(parsed.key())
                        .contentLength((long) payload.length)
                        .build(),
                    RequestBody.fromBytes(payload));
                // The PUT just defined an object — refresh local metadata.
                size = payload.length;
                lastModified = System.currentTimeMillis();
                directory = false;
                metadataKnown = true;
            } catch (S3Exception e) {
                throw toIOException(e, fileURL);
            }
        }
    }
}
