/*
 * Copyright (c) 1998, 2007 Sun Microsystems, Inc. 
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
 * An XFile output stream is an output stream for writing data to an 
 * <code>XFile</code>. 
 */
public class XFileOutputStream extends OutputStream {

    private long fp;	/* File Pointer */

    /*
     * File Accessor that implements the underlying filesystem
     */
    private XFileAccessor xfa;


    /**
     * Creates an XFile output stream to write to the specified 
     * <code>XFile</code> object. 
     *
     * @param      file the XFile to be opened for writing.
     * @exception  java.io.IOException if the XFile could not
     *             be opened for writing.
     */
    public XFileOutputStream(XFile xfile) throws IOException {

        xfa = xfile.newAccessor();

        if (xfa.open(xfile, true, false)) { // serial, not readonly
            if (!xfa.isFile())
                throw new IOException("not a file");

            if (!xfa.canWrite())
                throw new IOException("no write permission");
        }

        if (!xfa.mkfile())
            throw new IOException("no write permission");
    }

    /**
     * Creates an output XFile stream to write to the file with the 
     * specified name. 
     *
     * @param      name   the system-dependent filename.
     * @exception  java.io.IOException if the file could
     *             not be opened for writing.
     */
    public XFileOutputStream(String name) throws IOException {
        this(new XFile(name));
    }


    /**
     * Creates an output file for the specified XFile object.
     *
     * @param xfile the XFile to be opened for writing.
     * @param append true if writes begin at the end of the file
     * @exception java.io.IOException If the file is not found.
     */
    public XFileOutputStream(XFile xfile, boolean append)
        throws IOException {

	boolean isExist;

        xfa = xfile.newAccessor();

        if ((isExist = xfa.open(xfile, true, false))) { // serial, not readonly
            if (!xfa.isFile())
                throw new IOException("not a file");

            if (!xfa.canWrite())
                throw new IOException("no write permission");
        }

        /*
         * If file doesn't exist or append is False create the file
         */
        if (!isExist || !append) {
            if (!xfa.mkfile())
                throw new IOException("no write permission");
        }

        if (append)
            fp = xfa.length();
    }


    /**
     * Creates an output file with the specified name or URL.
     * 
     * @param name the native name or URL
     * @param append true if writes begin at the end of the file
     * @exception java.io.IOException If the file is not found.
     */
    public XFileOutputStream(String name, boolean append)
        throws IOException {

        this(new XFile(name), append);

    }

    
    /*
     * All writes to the Accessor go through here.
     */
    synchronized private void XFAwrite(byte b[], int off, int len)
        throws IOException {

        if (b == null)
            throw new NullPointerException();

        if (len == 0)
            return;
        
	if (off < 0 || len < 0 || off >= b.length || (off + len) > b.length)
            throw new IllegalArgumentException("Invalid argument");

        xfa.write(b, off, len, fp);
        fp += len;
    }


    /**
     * Writes the specified byte to this file output stream. 
     *
     * @param      b   the byte to be written.
     * @exception  java.io.IOException  if an I/O error occurs.
     */
    public void write(int b) throws IOException {
        XFAwrite(new byte[] {(byte)b}, 0, 1);
    }


    /**
     * Writes <code>b.length</code> bytes from the specified byte array 
     * to this file output stream. 
     *
     * @param      b   the data.
     * @exception  java.io.IOException  if an I/O error occurs.
     */
    public void write(byte b[]) throws IOException {
	XFAwrite(b, 0, b.length);
    }


    /**
     * Writes <code>len</code> bytes from the specified byte array 
     * starting at offset <code>off</code> to this XFile output stream. 
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  java.io.IOException  if an I/O error occurs.
     */
    public void write(byte b[], int off, int len) throws IOException {
	XFAwrite(b, off, len);
    }


    /**
     * Flushes this output stream and forces any buffered output bytes 
     * to be written out. 
     * <p>
     *
     * @exception  java.io.IOException  if an I/O error occurs.
     */
    public void flush() throws IOException {
        xfa.flush();
    }


    /**
     * Closes this file output stream, flushes any buffered, 
     * unwritten data, and releases any system resources 
     * associated with this stream. 
     *
     * After the file is closed further I/O operations may
     * throw IOException.
     *
     * @exception  java.io.IOException  if an I/O error occurs.
     */
    public void close() throws IOException {
        xfa.close();
    }

    
    /**
     * Ensures that the <code>close</code> method of this XFile
     * output stream is called when there are no more references
     * to this stream. 
     *
     * @exception  java.io.IOException  if an I/O error occurs.
     * @see        com.sun.xfile.XFileInputStream#close()
     */
    protected void finalize() throws IOException {
 	close();
    }
}
