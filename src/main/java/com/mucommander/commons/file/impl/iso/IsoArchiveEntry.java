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


package com.mucommander.commons.file.impl.iso;

import com.mucommander.commons.file.ArchiveEntry;

/**
 * This class represents an archive entry within an ISO archive.
 *
 * @author Maxence Bernard
 */
class IsoArchiveEntry extends ArchiveEntry {

    private long index;
    private int sectSize;
    private long shiftOffset;
    private boolean audio;

    IsoArchiveEntry(String path, boolean directory, long date, long size, long index, int sectSize, long shiftOffset, boolean audio) {
        super(path, directory, date, size, true);

        this.index = index;
        this.sectSize = sectSize;
        this.shiftOffset = shiftOffset;
        this.audio = audio;
    }

    long getIndex() {
        return index;
    }

    public int getSectSize() {
        return sectSize;
    }

    public long getShiftOffset() {
        return shiftOffset;
    }

    public boolean getAudio() {
        return audio;
    }

}
