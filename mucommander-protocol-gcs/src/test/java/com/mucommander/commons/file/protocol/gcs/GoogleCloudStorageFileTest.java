package com.mucommander.commons.file.protocol.gcs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;

import com.mucommander.commons.file.FileURL;

public class GoogleCloudStorageFileTest {

    @Test
    public void getBlobPath() throws MalformedURLException {
        var file1 = FileURL.getFileURL("gcs://project-id/bucket-name1/folder/folder2/file.txt");
        var file2 = FileURL.getFileURL("gcs://project-id/bucket-name2/folder/folder2");
        var file3 = FileURL.getFileURL("gcs://project-id/bucket-name3/folder/");
        var file4 = FileURL.getFileURL("gcs://project-id/bucket-name4/");
        var file5 = FileURL.getFileURL("gcs://project-id/bucket-name5");
        var file6 = FileURL.getFileURL("gcs:///bucket-name6");
        var file7 = FileURL.getFileURL("gcs://project-id");
        var file8 = FileURL.getFileURL("gcs://");

        var bucket1 = new GoogleCloudStorageFile(file1);
        var bucket2 = new GoogleCloudStorageFile(file2);
        var bucket3 = new GoogleCloudStorageFile(file3);
        var bucket4 = new GoogleCloudStorageFile(file4);
        var bucket5 = new GoogleCloudStorageFile(file5);
        var bucket6 = new GoogleCloudStorageFile(file6);
        var bucket7 = new GoogleCloudStorageFile(file7);
        var bucket8 = new GoogleCloudStorageFile(file8);

        assertEquals("folder/folder2/file.txt", bucket1.getBlobPath());
        assertEquals("folder/folder2", bucket2.getBlobPath());
        assertEquals("folder/", bucket3.getBlobPath());
        // Paths without any blob names
        assertEquals("", bucket4.getBlobPath());
        assertEquals("", bucket5.getBlobPath());
        assertEquals("", bucket6.getBlobPath());
        assertEquals("", bucket7.getBlobPath());
        assertEquals("", bucket8.getBlobPath());
    }
}
