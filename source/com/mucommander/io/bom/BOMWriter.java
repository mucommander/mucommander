/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * <code>BOMWriter</code> is a <code>Writer</code> that writes a Byte-Order Mark (BOM) at the beginning of a Unicode
 * stream and subsequently encodes characters in the requested encoding.
 *
 * @author Maxence Bernard
 */
public class BOMWriter extends OutputStreamWriter {

    /** The underlying InputStream. */
    protected OutputStream out;

    /** The BOM to write at the beginning of the stream, <code>null</code> for none. */
    protected BOM bom;

    /** True if the BOM has already been written if there was one */
    protected boolean bomWriteChecked;


    /**
     * Creates a new <code>BOMWriter</code> that will write the specified BOM at the beginning of the stream and
     * subsequently encode characters in the encoding returned by {@link BOM#getEncoding()}.
     *
     * @param out the <code>OutputStream</code> to write the encoded data to
     * @param bom the byte-order mark to write at the beginning of the stream.
     * @throws UnsupportedEncodingException if the BOM's encoding is not a character encoding supported by the Java runtime.
     */
    public BOMWriter(OutputStream out, BOM bom) throws UnsupportedEncodingException {
        this(out, bom.getEncoding(), bom);
    }

    /**
     * Creates a new <code>BOMWriter</code> that will encode characters in the specified encoding and, if the
     * encoding has a corresponding {@link BOM}, write at the beginning of the stream. If a Non-Unicode encoding
     * is passed, no BOM will be written to the stream and this <code>Writer</code> will act as a regular
     * {@link OutputStreamWriter}.
     *
     * <p>It is important to note that a BOM will always be written for Unicode encodings,
     * even if the particular encoding specifies that no BOM should be written (<code>UnicodeLittleUnmarked</code> for
     * instance). See {@link BOM#getInstance(String)} for more information about this.</p>
     *
     * @param out the <code>OutputStream</code> to write the encoded data to
     * @param encoding character encoding to use for encoding characters.
     * @throws UnsupportedEncodingException if the specified encoding is not a character encoding supported by the Java runtime.
     * @see BOM#getInstance(String)
     */
    public BOMWriter(OutputStream out, String encoding) throws UnsupportedEncodingException {
        this(out, encoding, BOM.getInstance(encoding));
    }

    /**
     * Creates a new <code>BOMWriter</code> that will write the specified BOM at the beginning of the stream and
     * subsequently encode characters in the specified encoding.
     *
     * @param out the <code>OutputStream</code> to write the encoded data to
     * @param bom the byte-order mark to write at the beginning of the stream.
     * @param encoding character encoding to use for encoding characters.
     * @throws UnsupportedEncodingException if the specified encoding is not a character encoding supported by the Java runtime.
     */
    protected BOMWriter(OutputStream out, String encoding, BOM bom) throws UnsupportedEncodingException {
        super(out, encoding);

        this.out = out;
        this.bom = bom;
    }

    /**
     * Checks if a BOM is waiting for being written, and if there is, writes it to the underlying output stream.
     *
     * @throws IOException if an error occurred while writing the BOM
     */
    protected void checkWriteBOM() throws IOException {
        if(!bomWriteChecked) {
            if(bom!=null)
                out.write(bom.getSignature());

            bomWriteChecked = true;
        }
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public void write(int c) throws IOException {
        checkWriteBOM();
        super.write(c);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        checkWriteBOM();
        super.write(cbuf, off, len);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        checkWriteBOM();
        super.write(str, off, len);
    }
}
