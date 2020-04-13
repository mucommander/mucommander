/**
* ===========================================
* Java Pdf Extraction Decoding Access Library
* ===========================================
 *
* Project Info:  http://www.jpedal.org
* (C) Copyright 1997-2008, IDRsolutions and Contributors.
* Main Developer: Simon Barnett
*
* 	This file is part of JPedal
*
* Copyright (c) 2008, IDRsolutions
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*     * Redistributions of source code must retain the above copyright
*       notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above copyright
*       notice, this list of conditions and the following disclaimer in the
*       documentation and/or other materials provided with the distribution.
*     * Neither the name of the IDRsolutions nor the
*       names of its contributors may be used to endorse or promote products
*       derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY IDRsolutions ``AS IS'' AND ANY
* EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL IDRsolutions BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* Other JBIG2 image decoding implementations include
* jbig2dec (http://jbig2dec.sourceforge.net/)
* xpdf (http://www.foolabs.com/xpdf/)
* 
* The final draft JBIG2 specification can be found at http://www.jpeg.org/public/fcd14492.pdf
* 
* All three of the above resources were used in the writing of this software, with methodologies,
* processes and inspiration taken from all three.
*
* ---------------
* StreamReader.java
* ---------------
 */
package org.jpedal.jbig2.io;

import org.jpedal.jbig2.examples.pdf.PDFSegment;

import java.io.IOException;

public class StreamReader {
    private byte[] data;

    private int bitPointer = 7;

    private int bytePointer = 0;

    public StreamReader(byte[] data) {
        this.data = data;
    }

    public short readByte(PDFSegment pdfSeg) {
        short bite = (short) (data[bytePointer++] & 255);

        if (pdfSeg != null)
            pdfSeg.writeToHeader(bite);

        return bite;
    }

    public void readByte(short[] buf, PDFSegment pdfSeg) throws IOException {
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (short) (data[bytePointer++] & 255);
        }

        if (pdfSeg != null)
            pdfSeg.writeToHeader(buf);
    }

    public short readByte() {
        short bite = (short) (data[bytePointer++] & 255);

        return bite;
    }

    public void readByte(short[] buf) {
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (short) (data[bytePointer++] & 255);
        }
    }

    public int readBit() {
        short buf = readByte();
        short mask = (short) (1 << bitPointer);

        int bit = (buf & mask) >> bitPointer;

        bitPointer--;
        if (bitPointer == -1) {
            bitPointer = 7;
        } else {
            movePointer(-1);
        }

        return bit;
    }

    public int readBits(int num) {
        int result = 0;

        for (int i = 0; i < num; i++) {
            result = (result << 1) | readBit();
        }

        return result;
    }

    public void movePointer(int ammount) {
        bytePointer += ammount;
    }

    public void consumeRemainingBits() {
        if (bitPointer != 7)
            readBits(bitPointer + 1);
    }

    public boolean isFinished() {
        return bytePointer == data.length;
    }
}
