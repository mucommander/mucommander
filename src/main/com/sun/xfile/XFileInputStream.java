/*
 * Copyright (c) 1999, 2007 Sun Microsystems, Inc. 
 * All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * -Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS
 * SHALL NOT BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE
 * AS A RESULT OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE
 * SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE
 * LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED
 * AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed,licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

package com.sun.xfile;

import java.io.*;

/**
 * An XFile input stream is an input stream for reading data from an 
 * <code>XFile</code>. 
 */
public class XFileInputStream extends InputStream {
    
    private long fp;	/* File Pointer */

    /**
     * File Accessor that implements the underlying filesystem
     */
    private XFileAccessor xfa;


    /**
     * Creates an input file stream to read from the specified 
     * <code>XFile</code> object. 
     *
     * @param      xfile   the file to be opened for reading.
     * @exception  java.io.FileNotFoundException if the file is
     *             not found.
     */
    public XFileInputStream(XFile xfile) throws IOException {
        xfa = xfile.newAccessor();
        if (! xfa.open(xfile, true, true))	// serial, read-only
            throw new FileNotFoundException("no file");

        if (!xfa.canRead())
            throw new IOException("no read permission");
    }


    /**
     * Creates an input file stream to read from a file with the 
     * specified name. 
     *
     * @param      name   the system-dependent file name.
     * @exception  java.io.FileNotFoundException if the file is
     *             not found.
     */
    public XFileInputStream(String name) throws IOException {
        this(new XFile(name));
    }
    

    /* 
     * Reads a subarray as a sequence of bytes. 
     *
     * @param b the data to be written
     * @param off the start offset in the data
     * @param len the number of bytes that are written
     * @exception java.io.IOException If an I/O error has occurred. 
     */ 
    synchronized private int XFAread(byte b[], int off, int len)
        throws IOException {

        if (b == null)
            throw new NullPointerException();

        if (len == 0)
            return 0;
        
	if (off < 0 || len < 0 || off >= b.length || (off + len) > b.length)
            throw new IllegalArgumentException("Invalid argument");

        int c = xfa.read(b, off, len, fp);

        if (c <= 0)
            return (-1);

        fp += c;

        return (c);
    }

    /**
     * Reads a byte of data from this XFile.
     *
     * @return the next byte of data, or <code>-1</code>
     *         if the end of the file is reached.
     * @exception  java.io.IOException if an I/O error occurs.
     */
    public int read() throws IOException {
        byte[] b = new byte[1];

        if (XFAread(b, 0, 1) != 1)
            return (-1);

        return b[0] & 0xff;
    }

    /**
     * Reads up to <code>b.length</code> bytes of data from this file
     * into an array of bytes.
     *
     * @param      b   the buffer into which the data is read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because
     *             the end of the file has been reached.
     * @exception  java.io.IOException if an I/O error occurs.
     */
    public int read(byte b[]) throws IOException {
	return XFAread(b, 0, b.length);
    }


    /**
     * Reads up to <code>len</code> bytes of data from this file
     * into an array of bytes.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset of the data.
     * @param      len   the maximum number of bytes read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because
     *             the end of the file has been reached.
     * @exception  java.io.IOException  if an I/O error occurs.
     */
    public int read(byte b[], int off, int len) throws IOException {
	return XFAread(b, off, len);
    }


    /**
     * Returns the number of bytes yet to be read from this file.
     *
     * @return the number of bytes yet to be read from this file
     *         without blocking.
     * @exception java.io.IOException  if an I/O error occurs.
     */
    public int available() throws IOException {
        return (int)(xfa.length() - fp);
    }


    /**
     * Skips over and discards <code>n</code> bytes of data from the 
     * file.
     *
     * The <code>skip</code> method may, for a variety of 
     * reasons, end up skipping over some smaller number of bytes, 
     * possibly <code>0</code>.
     * The actual number of bytes skipped is returned.
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  java.io.IOException  if an I/O error occurs.
     */
    public long skip(long n) throws IOException {
        if (n < 0)
            throw new IllegalArgumentException("illegal skip: " + n);

        fp += n;

        return n;
    }


    /**
     * Closes this file input stream and releases any system resources 
     * associated with the stream. 
     *
     * After the file is closed further I/O operations may
     * throw IOException.
     *
     * @exception  java.io.IOException  if an I/O error occurs.
     */
    public void close() throws IOException {
        xfa.close();
    }
}
