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

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

import org.ovirt.engine.sdk4.internal.containers.DiskContainer;
import org.ovirt.engine.sdk4.types.DataCenter;
import org.ovirt.engine.sdk4.types.Disk;
import org.ovirt.engine.sdk4.types.StorageDomain;
import org.ovirt.engine.sdk4.types.StorageDomainType;
import org.ovirt.engine.sdk4.types.StorageFormat;
import org.ovirt.engine.sdk4.types.StorageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.UnsupportedFileOperationException;

/**
 * This file represents a Storage Domain in the oVirt virtualization platform.
 * A Storage Domain is part of a {@link OvirtDataCenter} and comprises one or more {@link OvirtDisk}s.
 *
 * @author Arik Hadas
 */
public class OvirtStorageDomain extends OvirtFile {

    private static Logger log = LoggerFactory.getLogger(OvirtStorageDomain.class);

    private StorageDomain sd;

    protected OvirtStorageDomain(FileURL url, OvirtDataCenter parent) throws IOException {
        super(url, parent);
        init();
    }

    private void init() throws IOException {
        try (OvirtConnHandler connHandler = getConnHandler()) {
            String sdName = fileURL.getFilename();
            String dcName = fileURL.getParent().getFilename();
            DataCenter dc = Utils.getDataCenter(connHandler, dcName);
            sd = Utils.getStorageDomain(connHandler, dc.id(), sdName);
        }
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public void delete() throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.DELETE);
    }

    @Override
    public AbstractFile[] ls() throws IOException, UnsupportedFileOperationException {
        log.debug("listing Storage Domain %s", sd.name());
        try (OvirtConnHandler connHandler = getConnHandler()) {
            return Utils.getDisks(connHandler, sd.id())
                    .stream()
                    .map(this::toFile)
                    .filter(Objects::nonNull)
                    .toArray(AbstractFile[]::new);
        }
    }

    private OvirtDisk toFile(Disk disk) {
        try {
            return new OvirtDisk(Utils.getChildUrl(this, disk), this);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public long getFreeSpace() throws IOException, UnsupportedFileOperationException {
        return sd.available().longValue();
    }

    @Override
    public long getTotalSpace() throws IOException, UnsupportedFileOperationException {
        return sd.available().longValue() + sd.used().longValue();
    }

    @Override
    public AbstractFile getChild(String filename, AbstractFile template) throws IOException {
        if (template == null)
            return super.getChild(filename, null);
        return toDisk(filename, template);
    }

    private OvirtDisk toDisk(String name, AbstractFile file) throws UnsupportedFileOperationException, IOException {
        DiskContainer properties = Utils.toDiskProperties(file);
        properties.name(name);
        properties.storageDomains(Collections.singletonList(sd));
        properties.sparse(!Utils.isBlockStorage(sd.storage().type()));
        try {
            return new OvirtDisk(Utils.getChildUrl(this, properties), this)
                    .setProperties(properties);
        } catch (IOException e) {
            return null;
        }
    }
}
