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


package com.mucommander.commons.file.archive.zip.provider;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class ExtendedTimestampExtraField implements ZipExtraField {

    private static final ZipShort ID = new ZipShort(0x5455);
    private static final byte FLAG_MODTIME_PRESENT = 0x01;

    private byte[] localData;

    private Long javaTime;

    public Long getJavaTime() {
        return javaTime;
    }

    @Override
    public ZipShort getHeaderId() {
        return ID;
    }

    @Override
    public ZipShort getLocalFileDataLength() {
        return new ZipShort(localData.length);
    }

    @Override
    public ZipShort getCentralDirectoryLength() {
        return new ZipShort(localData.length);
    }

    @Override
    public byte[] getLocalFileDataData() {
        return localData;
    }

    @Override
    public byte[] getCentralDirectoryData() {
        return localData;
    }

    @Override
    public void parseFromLocalFileData(byte[] data, int offset, int length) {
        byte[] tmp = new byte[length];
        System.arraycopy(data, offset, tmp, 0, length);
        this.localData = tmp;

        if (this.localData.length == 5 && this.localData[0] == FLAG_MODTIME_PRESENT) {
            this.javaTime = parseTimestamp(Arrays.copyOfRange(this.localData, 1, this.localData.length));
        }
    }

    private Long parseTimestamp(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int unixTimestamp = buffer.getInt();
        return (long)unixTimestamp * 1_000;
    }

}
