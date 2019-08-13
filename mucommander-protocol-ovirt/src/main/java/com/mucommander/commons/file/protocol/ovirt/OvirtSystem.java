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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.UnsupportedFileOperationException;

/**
 * This file represents the oVirt virtualization platform.
 * A System comprises one or more {@link OvirtDataCenter}s.
 *
 * @author Arik Hadas
 */
public class OvirtSystem extends OvirtFile {

    private static Logger log = LoggerFactory.getLogger(OvirtSystem.class);

    protected OvirtSystem(FileURL url) {
        super(url, null);
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public void delete() throws IOException, UnsupportedFileOperationException {
        // muCommander manages only virtual disks
        throw new UnsupportedFileOperationException(FileOperation.DELETE);
    }

    @Override
    public AbstractFile[] ls() throws IOException, UnsupportedFileOperationException {
        log.debug("listing data centers");
        try (OvirtConnHandler connHandler = getConnHandler()) {
            return Utils.getDataCenters(connHandler)
                    .stream()
                    .map(this::toFile)
                    .filter(Objects::nonNull)
                    .toArray(AbstractFile[]::new);
        }
    }

    private OvirtDataCenter toFile(DataCenter dc) {
        return new OvirtDataCenter(Utils.getChildUrl(this, dc), this);
    }

    @Override
    public AbstractFile getParent() {
        return null;
    }

    @Override
    public boolean isRoot() {
        return true;
    }

    @Override
    public void setParent(AbstractFile parent) {}
}
