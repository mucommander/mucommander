/**
 * This file is part of muCommander, http://www.mucommander.com
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.ovirt.engine.sdk4.internal.containers.DiskContainer;
import org.ovirt.engine.sdk4.internal.containers.ImageContainer;
import org.ovirt.engine.sdk4.internal.containers.ImageTransferContainer;
import org.ovirt.engine.sdk4.types.DataCenter;
import org.ovirt.engine.sdk4.types.Disk;
import org.ovirt.engine.sdk4.types.DiskContentType;
import org.ovirt.engine.sdk4.types.DiskFormat;
import org.ovirt.engine.sdk4.types.Identified;
import org.ovirt.engine.sdk4.types.ImageTransfer;
import org.ovirt.engine.sdk4.types.ImageTransferDirection;
import org.ovirt.engine.sdk4.types.StorageDomain;
import org.ovirt.engine.sdk4.types.StorageType;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.util.PathUtils;

/**
 * Utility methods
 *
 * @author Arik Hadas
 */
public class Utils {
    public static byte[] QCOW_MAGIC = new byte[] { 0x51, 0x46, 0x49, (byte) 0xfb };

    public static FileURL getChildUrl(OvirtFile parent, Identified child) {
        FileURL parentUrl = parent.getURL();
        String parentPath = PathUtils.removeTrailingSeparator(parentUrl.getPath()) + AbstractFile.DEFAULT_SEPARATOR;
        FileURL url = (FileURL) parentUrl.clone();
        url.setPath(parentPath + child.name());
        return url;
    }

    public static FileURL getChildUrl(OvirtFile parent, Disk disk) {
        FileURL url = getChildUrl(parent, (Identified) disk);
        url.setProperty("id", disk.id());
        return url;
    }

    public static DiskContainer toDiskProperties(AbstractFile file) throws UnsupportedFileOperationException, IOException {
        DiskContainer properties = new DiskContainer();
        properties.format(isQcow(file) ? DiskFormat.COW : DiskFormat.RAW);
        properties.sparse(true);
        properties.contentType(file.getName().endsWith(".iso") ? DiskContentType.ISO : DiskContentType.DATA);
        BigInteger size = BigInteger.valueOf(file.getSize());
        properties.actualSize(size);
        properties.provisionedSize(size);
        return properties;
    }

    private static boolean isQcow(AbstractFile file) throws UnsupportedFileOperationException, IOException {
        byte[] magic = new byte[4];
        try (InputStream in = file.getInputStream()) {
            in.read(magic);
        }
        return Arrays.equals(magic, QCOW_MAGIC);
    }

    public static DataCenter getDataCenter(OvirtConnHandler connHandler, String dcName) throws IOException {
        DataCenter dc = connHandler
                .getConnection()
                .systemService()
                .dataCentersService()
                .list()
                .send()
                .dataCenters()
                .stream()
                .filter(d -> d.name().equals(dcName))
                .findAny()
                .orElse(null);
        if (dc == null)
            throw new IOException("Could not find Data Center " + dcName);
        return dc;
    }

    public static StorageDomain getStorageDomain(OvirtConnHandler connHandler, String dcId, String sdName) throws IOException {
        StorageDomain sd = connHandler
                .getConnection()
                .systemService()
                .storageDomainsService()
                .list()
                .send()
                .storageDomains()
                .stream()
                .filter(s -> s.dataCenters().iterator().next().id().equals(dcId))
                .filter(s -> s.name().equals(sdName))
                .findAny()
                .orElse(null);
        if (sd == null)
            throw new IOException("Could not find Storage Domain " + sdName);
        return sd;
    }

    public static List<Disk> getDisks(OvirtConnHandler connHandler, String sdId) throws IOException {
        return connHandler
                .getConnection()
                .systemService()
                .storageDomainsService()
                .storageDomainService(sdId)
                .disksService()
                .list()
                .send()
                .disks();
    }

    public static List<StorageDomain> getStorageDomains(OvirtConnHandler connHandler, String dcId) throws IOException {
        return connHandler
                .getConnection()
                .systemService()
                .dataCentersService()
                .dataCenterService(dcId)
                .storageDomainsService()
                .list()
                .send()
                .storageDomains();
    }

    public static List<DataCenter> getDataCenters(OvirtConnHandler connHandler) throws IOException {
        return connHandler
                .getConnection()
                .systemService()
                .dataCentersService()
                .list()
                .send()
                .dataCenters();
    }

    public static Disk getDisk(OvirtConnHandler connHandler, String diskId) throws IOException {
        Disk disk = connHandler
                .getConnection()
                .systemService()
                .disksService()
                .list()
                .send()
                .disks()
                .stream()
                .filter(d -> d.id().equals(diskId))
                .findAny()
                .orElse(null);
        if (disk == null)
            throw new IOException("Could not find Disk " + diskId);
        return disk;
    }

    public static Disk getDisk(OvirtConnHandler connHandler, String sdId, String diskId) throws IOException {
        return connHandler
                .getConnection()
                .systemService()
                .disksService()
                .list()
                .send()
                .disks()
                .stream()
                .filter(d -> d.storageDomains().iterator().next().id().equals(sdId))
                .filter(d -> d.id().equals(diskId))
                .findAny()
                .orElse(null);
    }

    public static Disk addDisk(OvirtConnHandler connHandler, DiskContainer properties) throws IOException {
        return connHandler
                .getConnection()
                .systemService()
                .disksService()
                .add()
                .disk(properties)
                .send()
                .disk();
    }

    public static void deleteDisk(OvirtConnHandler connHandler, String diskId) throws IOException {
        connHandler
        .getConnection()
        .systemService()
        .disksService()
        .diskService(diskId)
        .remove()
        .send();
    }

    public static ImageTransfer getImageTransfer(OvirtConnHandler connHandler, String transferId) throws IOException {
        return connHandler
                .getConnection()
                .systemService()
                .imageTransfersService()
                .list()
                .send()
                .imageTransfer()
                .stream()
                .filter(t -> transferId.equals(t.id()))
                .findFirst()
                .orElse(null);
    }

    public static ImageTransfer addImageTransfer(OvirtConnHandler connHandler, String diskId, ImageTransferDirection direction) throws IOException {
        ImageContainer image = new ImageContainer();
        image.id(diskId);
        ImageTransferContainer imageTransfer = new ImageTransferContainer();
        imageTransfer.image(image);
        imageTransfer.direction(direction);
        return Utils.addImageTransfer(connHandler, imageTransfer);
    }

    private static ImageTransfer addImageTransfer(OvirtConnHandler connHandler, ImageTransferContainer properties) throws IOException {
        return connHandler
                .getConnection()
                .systemService()
                .imageTransfersService()
                .add()
                .imageTransfer(properties)
                .send()
                .imageTransfer();
    }

    public static void finalizeImageTransfer(OvirtConnHandler connHandler, String transferId) throws IOException  {
        connHandler
        .getConnection()
        .systemService()
        .imageTransfersService()
        .imageTransferService(transferId)
        .finalize_()
        .send();
    }

    public static SSLSocketFactory setTrustStore(String certificate) throws IOException {
        try {
            InputStream is = new ByteArrayInputStream(certificate.getBytes());
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate caCert = (X509Certificate)cf.generateCertificate(is);

            TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null); // You don't need the KeyStore instance to come from a file.
            ks.setCertificateEntry("caCert", caCert);

            tmf.init(ks);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException | KeyManagementException e) {
            throw new IOException("Failed to set ssl credentials");
        }
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {}
    }

    public static boolean isBlockStorage(StorageType storageType) {
        switch(storageType) {
        case FCP:
        case ISCSI:
            return true;
        default:
            return false;
        }
    }
}
