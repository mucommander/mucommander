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
 * BitmapPointer.java
 * ---------------
 */
package org.jpedal.jbig2.image;

public class BitmapPointer {
    private int x, y, width, height, bits, count;
    private JBIG2Bitmap bitmap;

    public BitmapPointer(JBIG2Bitmap bitmap) {
        this.bitmap = bitmap;
        this.height = bitmap.getHeight();
        this.width = bitmap.getWidth();
    }

    public void setPointer(int x, int y) {
        this.x = x;
        this.y = y;
        count = 0;
    }

    public int nextPixel() {

        // fairly certain the byte can be cached here - seems to work fine. only
        // problem would be if cached pixel was modified, and the modified
        // version needed.
//		if (y < 0 || y >= height || x >= width) {
//			return 0;
//		} else if (x < 0) {
//			x++;
//			return 0;
//		}
//
//		if (count == 0 && width - x >= 8) {
//			bits = bitmap.getPixelByte(x, y);
//			count = 8;
//		} else {
//			count = 0;
//		}
//
//		if (count > 0) {
//			int b = bits & 0x01;
//			count--;
//			bits >>= 1;
//			x++;
//			return b;
//		}
//
//		int pixel = bitmap.getPixel(x, y);
//		x++;
//
//		return pixel;

        if (y < 0 || y >= height || x >= width) {
            return 0;
        } else if (x < 0) {
            x++;
            return 0;
        }

        int pixel = bitmap.getPixel(x, y);

        x++;

        return pixel;
    }
}
