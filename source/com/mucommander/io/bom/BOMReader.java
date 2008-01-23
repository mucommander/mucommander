/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.io.bom;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * <code>BOMReader</code> is a <code>Reader</code> which provides support for Byte-Order Marks (BOM).
 * A BOM is a byte sequence found at the beginning of a Unicode text stream which indicates the encoding of the text
 * that follows.
 *
 * <p>
 * This class uses a {@link BOMInputStream} for BOM handling and serves a dual purpose:<br>
 * 1) it allows to auto-detect the encoding when a BOM is present in the underlying stream and use it.<br>
 * 2) it allows to discard the BOM from a Unicode stream: the leading bytes corresponding to the BOM are swallowed by
 * the stream and never returned by the <code>read</code> methods.
 * </p>
 *
 * @see BOMInputStream
 * @author Maxence Bernard
 */
public class BOMReader extends InputStreamReader {

    /**
     * Creates a new <code>BOMReader</code>. A {@link BOMInputStream} is created on top of the specified
     * <code>InputStream</code> to auto-detect a potential {@link BOM} and use the associated encoding.
     * If no BOM is found at the beginning of the stream, <code>UTF-8</code> encoding is assumed.
     *
     * @param in the underlying InputStream
     * @throws IOException if an error occurred while detecting the BOM or initializing this reader.
     */
    public BOMReader(InputStream in) throws IOException {
        this(new BOMInputStream(in), "UTF-8");
    }

    /**
     * Creates a new <code>BOMReader</code>. A {@link BOMInputStream} is created on top of the specified
     * <code>InputStream</code> to auto-detect a potential {@link BOM} and use the associated encoding.
     * If no BOM is found at the beginning of the stream, the specified default encoding is assumed.
     *
     * @param in the underlying InputStream
     * @param defaultEncoding the encoding used if the stream doesn't contain a BOM
     * @throws IOException if an error occurred while detecting the BOM or initializing this reader.
     */
    public BOMReader(InputStream in, String defaultEncoding) throws IOException {
        this(new BOMInputStream(in), defaultEncoding);
    }

    /**
     * Creates a new <code>BOMReader</code> using the given {@link BOMInputStream}. If the <code>BOMInputStream</code>
     * does not contain a {@link BOM}, the specified default encoding is assumed.
     *
     * @param bomIn the underlying BOMInputStream
     * @param defaultEncoding the encoding used if the stream doesn't contain a BOM
     * @throws UnsupportedEncodingException if the encoding associated with the BOM or the default encoding is not
     * supported by the Java runtime 
     */
    public BOMReader(BOMInputStream bomIn, String defaultEncoding) throws UnsupportedEncodingException {
        super(bomIn, bomIn.getBOM()==null?defaultEncoding:bomIn.getBOM().getEncoding());
    }
}
