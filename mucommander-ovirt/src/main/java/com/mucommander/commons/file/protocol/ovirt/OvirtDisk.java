/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2018 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.file.protocol.ovirt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.ovirt.engine.sdk4.internal.containers.DiskContainer;
import org.ovirt.engine.sdk4.types.DataCenter;
import org.ovirt.engine.sdk4.types.Disk;
import org.ovirt.engine.sdk4.types.DiskStatus;
import org.ovirt.engine.sdk4.types.ImageTransfer;
import org.ovirt.engine.sdk4.types.ImageTransferDirection;
import org.ovirt.engine.sdk4.types.ImageTransferPhase;
import org.ovirt.engine.sdk4.types.StorageDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.UnsupportedFileOperationException;

/**
 * A virtual Disk in the oVirt virtualization platform.
 * A Disk is part of a {@link OvirtStorageDomain}.
 * Currently, it is the lowest-level entity of oVirt in muCommander.
 *
 * @author Arik Hadas
 */
public class OvirtDisk extends OvirtFile {

    private static Logger log = LoggerFactory.getLogger(OvirtDisk.class);

    private Disk disk;
    private DiskContainer properties;

    protected OvirtDisk(FileURL url, OvirtStorageDomain parent) throws IOException {
        super(url, parent);
        init();
    }

    private void init() throws IOException {
        try (OvirtConnHandler connHandler = getConnHandler()) {
            String diskName = fileURL.getFilename();
            String sdName = fileURL.getParent().getFilename();
            String dcName = fileURL.getParent().getParent().getFilename();
            DataCenter dc = Utils.getDataCenter(connHandler, dcName);
            StorageDomain sd = Utils.getStorageDomain(connHandler, dc.id(), sdName);
            disk = Utils.getDisk(connHandler, sd.id(), diskName);
        }
    }

    public OvirtDisk setProperties(DiskContainer properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public void delete() throws IOException, UnsupportedFileOperationException {
        try (OvirtConnHandler connHandler = getConnHandler()) {
            disk = Utils.getDisk(connHandler, disk.id());

            if (disk.status() != DiskStatus.OK)
                throw new IOException("Cannot remove disk");

            Utils.deleteDisk(connHandler, disk.id());
        }
    }

    @Override
    public AbstractFile[] ls() throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.LIST_CHILDREN);
    }

    @Override
    public long getSize() {
        return disk != null ?
                disk.actualSize().longValue()
                : properties != null ?
                        properties.actualSize().longValue()
                        : 0;
    }

    @Override
    public boolean exists() {
        return disk != null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException, UnsupportedFileOperationException {
        try (OvirtConnHandler connHandler = getConnHandler()) {
            Disk disk = Utils.addDisk(connHandler, properties);

            Utils.sleep(500);
            do {
                disk = connHandler
                        .getConnection()
                        .systemService()
                        .disksService()
                        .diskService(disk.id())
                        .get()
                        .send()
                        .disk();
            } while(!isDiskReady(disk));

            log.debug("disk has been created");

            ImageTransfer transfer = Utils.addImageTransfer(connHandler, disk.id(), ImageTransferDirection.UPLOAD);
            final String transferId = transfer.id();

            Utils.sleep(500);
            do {
                transfer = Utils.getImageTransfer(connHandler, transferId);
            } while(isTransferInitializing(transfer));

            log.debug("Transfer session has been created!");

            System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

            URL url = getDestinationUrl(transfer);
            HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
            https.setSSLSocketFactory(Utils.setTrustStore(connHandler.getCertificate()));
            https.setRequestProperty("PUT", url.getPath());
            https.setDoOutput(true);
            https.setRequestMethod("PUT");
            https.setFixedLengthStreamingMode(properties.actualSizeAsLong());

            return new BufferedOutputStream(https.getOutputStream()) {
                public void close() throws IOException {
                    try {
                        int responseCode = https.getResponseCode();
                        log.info("Finished uploading disk " + (responseCode == 200 ? "successfully" : "with failure") + " (response code  = " + responseCode +")");
                        if (responseCode != 200)
                            throw new IOException("Failed to upload disk");
                    } catch (Exception e) {
                        log.error("Failed to upload file", e);
                    } finally {
                        try (OvirtConnHandler connHandler = getConnHandler()) {
                            super.close();
                            Utils.finalizeImageTransfer(connHandler, transferId);
                            https.disconnect();
                        }
                    }
                }
            };
        }
    }

    private URL getDestinationUrl(ImageTransfer transfer) throws MalformedURLException {
        boolean useProxy = Boolean.parseBoolean(fileURL.getProperty("proxy"));
        return new URL(useProxy ? transfer.proxyUrl() : transfer.transferUrl());
    }

    private boolean isTransferInitializing(ImageTransfer transfer) {
        if (transfer.phase() != ImageTransferPhase.INITIALIZING)
            return false;

        Utils.sleep(1000);
        return true;
    }

    private boolean isDiskReady(Disk disk) {
        if (disk.status() == DiskStatus.OK)
            return true;

        Utils.sleep(2000);
        return false;
    }

    @Override
    public InputStream getInputStream() throws IOException, UnsupportedFileOperationException {
        try (OvirtConnHandler connHandler = getConnHandler()) {
            ImageTransfer transfer = Utils.addImageTransfer(connHandler, disk.id(), ImageTransferDirection.DOWNLOAD);
            final String transferId = transfer.id();

            Utils.sleep(500);
            do {
                transfer = Utils.getImageTransfer(connHandler, transferId);
            } while(isTransferInitializing(transfer));

            log.debug("Transfer session has been created!");

            System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

            URL url = getDestinationUrl(transfer);
            HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
            https.setSSLSocketFactory(Utils.setTrustStore(connHandler.getCertificate()));
            https.setDoInput(true);
            https.setRequestProperty("GET", url.getPath());
            https.setRequestMethod("GET");

            return new BufferedInputStream(https.getInputStream()) {
                public void close() throws IOException {
                    try {
                        int responseCode = https.getResponseCode();
                        log.info("Finished downloading disk " + (responseCode == 200 ? "successfully" : "with failure") + " (response code  = " + responseCode +")");
                        if (responseCode != 200)
                            throw new IOException("Failed to download disk");
                    } catch (Exception e) {
                        log.error("Failed to download file", e);
                    } finally {
                        try (OvirtConnHandler connHandler = getConnHandler()) {
                            super.close();
                            Utils.finalizeImageTransfer(connHandler, transferId);
                            https.disconnect();
                        }
                    }
                }
            };
        }
    }
}