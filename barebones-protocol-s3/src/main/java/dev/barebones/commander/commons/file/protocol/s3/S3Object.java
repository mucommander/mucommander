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
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletionException;

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
        // OutputStream contract: caller writes whatever, then close().
        // Two strategies bounded by SPILL_THRESHOLD:
        //   - Small writes (≤ SPILL_THRESHOLD) buffer in memory and
        //     PUT in a single request on close().
        //   - Large writes spill to a temp file beyond the threshold
        //     and on close() the file is uploaded via S3TransferManager
        //     (multipart). The temp file is deleted regardless.
        // This keeps memory bounded for arbitrary-size uploads without
        // forcing every small file through TransferManager.
        return new SpillingPutOutputStream();
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

    /**
     * OutputStream that uploads on close. Stays in-memory for small
     * payloads; spills to a temp file once a threshold is exceeded
     * and uploads from that file via {@link
     * software.amazon.awssdk.transfer.s3.S3TransferManager} (multipart).
     */
    private final class SpillingPutOutputStream extends OutputStream {

        /** Spill to disk when the in-memory buffer would exceed this. */
        private static final int SPILL_THRESHOLD = 32 * 1024 * 1024;

        private ByteArrayOutputStream memory = new ByteArrayOutputStream();
        private long bytesWritten;
        private Path spillFile;
        private OutputStream spillStream;
        private boolean closed;

        @Override
        public void write(int b) throws IOException {
            ensureCapacityForOneByte();
            target().write(b);
            bytesWritten++;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (len <= 0) return;
            ensureCapacityForExtra(len);
            target().write(b, off, len);
            bytesWritten += len;
        }

        private OutputStream target() {
            return spillStream != null ? spillStream : memory;
        }

        private void ensureCapacityForOneByte() throws IOException {
            ensureCapacityForExtra(1);
        }

        private void ensureCapacityForExtra(int len) throws IOException {
            if (spillStream != null) return;
            if (bytesWritten + len <= SPILL_THRESHOLD) return;
            // Switch to temp-file mode; copy current memory buffer into it.
            spillFile = Files.createTempFile("barebones-s3-upload-", ".bin");
            spillStream = Files.newOutputStream(spillFile);
            memory.writeTo(spillStream);
            memory = null;
        }

        @Override
        public void close() throws IOException {
            if (closed) return;
            closed = true;
            try {
                if (spillStream == null) {
                    putFromMemory();
                } else {
                    spillStream.close();
                    uploadSpilledFile();
                }
                // Whichever path: refresh local metadata.
                size = bytesWritten;
                lastModified = System.currentTimeMillis();
                directory = false;
                metadataKnown = true;
            } finally {
                if (spillFile != null) {
                    try {
                        Files.deleteIfExists(spillFile);
                    } catch (IOException ignored) {
                        // Temp dir cleanup is best-effort; the OS
                        // sweeps it eventually.
                    }
                }
            }
        }

        private void putFromMemory() throws IOException {
            byte[] payload = memory.toByteArray();
            try {
                connection.client().putObject(
                    PutObjectRequest.builder()
                        .bucket(parsed.bucket())
                        .key(parsed.key())
                        .contentLength((long) payload.length)
                        .build(),
                    RequestBody.fromBytes(payload));
            } catch (S3Exception e) {
                throw toIOException(e, fileURL);
            }
        }

        private void uploadSpilledFile() throws IOException {
            try {
                connection.transferManager()
                    .uploadFile(UploadFileRequest.builder()
                        .source(spillFile)
                        .putObjectRequest(PutObjectRequest.builder()
                            .bucket(parsed.bucket())
                            .key(parsed.key())
                            .build())
                        .build())
                    .completionFuture()
                    .join();
            } catch (CompletionException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                if (cause instanceof S3Exception se) {
                    throw toIOException(se, fileURL);
                }
                throw new IOException("S3 multipart upload failed: " + cause.getMessage(), cause);
            }
        }
    }
}
