package com.mucommander.io.bom;
/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This interface contains constants used by several classes of the BOM package.
 *
 * @author Maxence Bernard
 */
public interface BOMConstants {

    /** UTF-8 BOM: EF BB BF */
    public final static BOM UTF8_BOM = new BOM(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF}, "UTF-8");

    /** UTF-16 Big Endian BOM: FE FF */
    public final static BOM UTF16_BE_BOM = new BOM(new byte[]{(byte)0xFE, (byte)0xFF}, "UTF-16BE");

    /** UTF-16 Little Endian BOM: FF FE */
    public final static BOM UTF16_LE_BOM = new BOM(new byte[]{(byte)0xFF, (byte)0xFE}, "UTF-16LE");

    /** UTF-32 Big Endian BOM: 00 00 FE FF. Note that  */
    public final static BOM UTF32_BE_BOM = new BOM(new byte[]{(byte)0x00, (byte)0x00, (byte)0xFE, (byte)0xFF}, "UTF-32BE");

    /** UTF-32 Little Endian BOM: FF FE 00 00 */
    public final static BOM UTF32_LE_BOM = new BOM(new byte[]{(byte)0xFF, (byte)0xFE, (byte)0x00, (byte)0x00}, "UTF-32LE");

    /** List of supported BOMs */
    final static BOM SUPPORTED_BOMS[] = new BOM[] {
        UTF8_BOM,
        UTF16_BE_BOM,
        UTF16_LE_BOM,
        UTF32_BE_BOM,
        UTF32_LE_BOM
    };
}
