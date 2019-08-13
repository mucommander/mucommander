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
import java.util.Objects;

import org.ovirt.engine.sdk4.types.DataCenter;
import org.ovirt.engine.sdk4.types.StorageDomain;
import org.ovirt.engine.sdk4.types.StorageDomainType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.UnsupportedFileOperationException;

/**
 * This file represents a Data Center in the oVirt virtualization platform.
 * A Data Center is part of a {@link OvirtSystem} and comprises one or more {@link OvirtStorageDomain}s.
 *
 * @author Arik Hadas
 */
public class OvirtDataCenter extends OvirtFile {

    private static Logger log = LoggerFactory.getLogger(OvirtDataCenter.class);

    protected OvirtDataCenter(FileURL url, OvirtSystem parent) {
        super(url, parent);
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
        String dcName = fileURL.getFilename();
        log.debug("listing Data Center %s", dcName);
        try (OvirtConnHandler connHandler = getConnHandler()) {
            DataCenter dc = Utils.getDataCenter(connHandler, dcName);
            return Utils.getStorageDomains(connHandler, dc.id())
                    .stream()
                    .filter(sd -> sd.type() == StorageDomainType.DATA)
                    .map(this::toFile)
                    .filter(Objects::nonNull)
                    .toArray(AbstractFile[]::new);
        }
    }

    private AbstractFile toFile(StorageDomain sd) {
        try {
            return new OvirtStorageDomain(Utils.getChildUrl(this, sd), this);
        } catch (IOException e) {
            return null;
        }
    }
}
