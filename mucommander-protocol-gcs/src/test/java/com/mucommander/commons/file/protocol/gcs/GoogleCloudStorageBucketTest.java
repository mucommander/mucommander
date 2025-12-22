package com.mucommander.commons.file.protocol.gcs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;

import com.google.cloud.storage.BlobInfo;
import com.mucommander.commons.file.FileURL;

public class GoogleCloudStorageBucketTest {

    @Test
    public void getBucketName() throws MalformedURLException {
        var file1 = FileURL.getFileURL("gcs://project-id/bucket-name1/folder/folder2/file.txt");
        var file2 = FileURL.getFileURL("gcs://project-id/bucket-name2/folder/folder2");
        var file3 = FileURL.getFileURL("gcs://project-id/bucket-name3/");
        var file4 = FileURL.getFileURL("gcs://project-id/bucket-name4");
        var file5 = FileURL.getFileURL("gcs:///bucket-name5");
        var file6 = FileURL.getFileURL("gcs://project-id");
        var file7 = FileURL.getFileURL("gcs://");

        var bucket1 = new GoogleCloudStorageBucket(file1);
        var bucket2 = new GoogleCloudStorageBucket(file2);
        var bucket3 = new GoogleCloudStorageBucket(file3);
        var bucket4 = new GoogleCloudStorageBucket(file4);
        var bucket5 = new GoogleCloudStorageBucket(file5);
        var bucket6 = new GoogleCloudStorageBucket(file6);
        var bucket7 = new GoogleCloudStorageBucket(file7);

        assertEquals("bucket-name1", bucket1.getBucketName());
        assertEquals("bucket-name2", bucket2.getBucketName());
        assertEquals("bucket-name3", bucket3.getBucketName());
        assertEquals("bucket-name4", bucket4.getBucketName());
        assertEquals("bucket-name5", bucket5.getBucketName());
        assertEquals("", bucket6.getBucketName());
        assertEquals("", bucket7.getBucketName());
    }

    @Test
    public void getBlobName() {
        var file1 = BlobInfo.newBuilder("bucket-name", "/folder/folder2/file.txt").build();
        var file2 = BlobInfo.newBuilder("bucket-name", "rel-folder/folder2/file2.txt").build();
        var file3 = BlobInfo.newBuilder("bucket-name", "/folder/folder2/").build();
        var file4 = BlobInfo.newBuilder("bucket-name", "/folder/folder3").build();
        var file5 = BlobInfo.newBuilder("bucket-name", "/").build();
        var file6 = BlobInfo.newBuilder("bucket-name", "").build();

        assertEquals(GoogleCloudStorageBucket.getBlobName(file1), "file.txt");
        assertEquals(GoogleCloudStorageBucket.getBlobName(file2), "file2.txt");
        assertEquals(GoogleCloudStorageBucket.getBlobName(file3), "folder2");
        assertEquals(GoogleCloudStorageBucket.getBlobName(file4), "folder3");
        assertEquals(GoogleCloudStorageBucket.getBlobName(file5), "");
        assertEquals(GoogleCloudStorageBucket.getBlobName(file6), "");
    }
}
