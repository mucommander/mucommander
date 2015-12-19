/*
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

package com.mucommander.commons.io.bom;

/**
 * This interface contains constants used by several classes of the BOM package.
 *
 * @author Maxence Bernard
 */
public interface BOMConstants {

    /** UTF-8 BOM: EF BB BF */
    public final static BOM UTF8_BOM = new BOM(
            new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF},
            "UTF-8",
            new String[]{}
    );

    /** UTF-16 Big Endian BOM: FE FF */
    public final static BOM UTF16_BE_BOM = new BOM(
            new byte[]{(byte)0xFE, (byte)0xFF},
            "UTF-16BE",
            new String[]{"UTF-16", "x-UTF-16BE-BOM" ,"UnicodeBig", "UnicodeBigUnmarked"}
    );

    /** UTF-16 Little Endian BOM: FF FE */
    public final static BOM UTF16_LE_BOM = new BOM(
            new byte[]{(byte)0xFF, (byte)0xFE},
            "UTF-16LE",
            new String[]{"x-UTF-16LE-BOM", "UnicodeLittle", "UnicodeLittleUnmarked"}
    );

    /** UTF-32 Big Endian BOM: 00 00 FE FF. */
    public final static BOM UTF32_BE_BOM = new BOM(
            new byte[]{(byte)0x00, (byte)0x00, (byte)0xFE, (byte)0xFF},
            "UTF-32BE",
            new String[]{"UTF-32", "x-UTF-32BE-BOM"}
    );

    /** UTF-32 Little Endian BOM: FF FE 00 00 */
    public final static BOM UTF32_LE_BOM = new BOM(
            new byte[]{(byte)0xFF, (byte)0xFE, (byte)0x00, (byte)0x00},
            "UTF-32LE",
            new String[]{"x-UTF-32LE-BOM"}
    );

    /** List of supported BOMs */
    final static BOM SUPPORTED_BOMS[] = new BOM[] {
        UTF8_BOM,
        UTF16_BE_BOM,
        UTF16_LE_BOM,
        UTF32_BE_BOM,
        UTF32_LE_BOM
    };
}
