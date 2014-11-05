/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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


package com.mucommander.commons.file.impl.local;

import com.mucommander.commons.file.DummyFile;
import com.mucommander.commons.file.FileURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;

/**
 * Represents special Windows locations such as 'My Computer', 'Network Neighborhood', 'Recycle Bin', ... as dummy
 * <code>AbstractFile</code> instances.
 *
 * <p>This class is totally useless on platforms other than Windows.</p>
 *
 * @author Maxence Bernard
 */
public class SpecialWindowsLocation extends DummyFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpecialWindowsLocation.class);

    /** Control Panel */
    public final static SpecialWindowsLocation CONTROL_PANEL = createLocation("::{20D04FE0-3AEA-1069-A2D8-08002B30309D}\\::{21EC2020-3AEA-1069-A2DD-08002B30309D}");

    /** My Computer */
    public final static SpecialWindowsLocation MY_COMPUTER = createLocation("::{20D04FE0-3AEA-1069-A2D8-08002B30309D}");

    /** My Documents */
    public final static SpecialWindowsLocation MY_DOCUMENTS = createLocation("::{450D8FBA-AD25-11D0-98A8-0800361B1103}");

    /** Network Neighborhood */
    public final static SpecialWindowsLocation NETWORK_NEIGHBORHOOD = createLocation("::{208D2C60-3AEA-1069-A2D7-08002B30309D}");

    /** Recycle Bin */
    public final static SpecialWindowsLocation RECYCLE_BIN = createLocation("::{645FF040-5081-101B-9F08-00AA002F954E}");

    /**
     * Creates a SpecialWindowsLocation using the specified class ID and returns it.
     *
     * @param clsid the class ID
     * @return the new SpecialWindowsLocation
     */
    private static SpecialWindowsLocation createLocation(String clsid) {
        try {
            return new SpecialWindowsLocation(clsid);
        }
        catch(MalformedURLException e) {
            LOGGER.warn("Unable to creation location {}", clsid, e);
        }

        return null;
    }

    /** A class ID */
    protected String clsid;

    /**
     * Creates a new special Windows location using the given CLSID (Class identifier).
     *
     * @param clsid a Windows class identifier
     * @throws java.net.MalformedURLException should not happen
     */
    public SpecialWindowsLocation(String clsid) throws MalformedURLException {
        super(FileURL.getFileURL("file:///"));    // dummy URL, '/' corresponds to nothing under Windows

        this.clsid = clsid;
    }

    /**
     * Implementation notes: returns the CLSID (Class identifier) passed to the constructor.
     */
    @Override
    public String getName() {
        return clsid;
    }

    /**
     * Implementation notes: returns the CLSID (Class identifier) passed to the constructor.
     */
    @Override
    public String getAbsolutePath() {
        return clsid;
    }

    /**
     * Implementation notes: always returns <code>true</code>.
     */
    @Override
    public boolean isDirectory() {
        return true;
    }
}
