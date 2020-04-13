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
* HuffmanDecoder.java
* ---------------
 */
package org.jpedal.jbig2.decoders;

import org.jpedal.jbig2.io.StreamReader;

import java.io.IOException;

public class HuffmanDecoder {

    public static int jbig2HuffmanLOW = 0xfffffffd;
    public static int jbig2HuffmanOOB = 0xfffffffe;
    public static int jbig2HuffmanEOT = 0xffffffff;

    private StreamReader reader;

    private static HuffmanDecoder ref;

    private HuffmanDecoder() {
    }

    public HuffmanDecoder(StreamReader reader) {
        this.reader = reader;
    }

    public DecodeIntResult decodeInt(int[][] table) throws IOException {
        int length = 0, prefix = 0;

        for (int i = 0; table[i][2] != jbig2HuffmanEOT; i++) {
            for (; length < table[i][1]; length++) {
                int bit = reader.readBit();
                prefix = (prefix << 1) | bit;
            }

            if (prefix == table[i][3]) {
                if (table[i][2] == jbig2HuffmanOOB) {
                    return new DecodeIntResult(-1, false);
                }
                int decodedInt;
                if (table[i][2] == jbig2HuffmanLOW) {
                    int readBits = reader.readBits(32);
                    decodedInt = table[i][0] - readBits;
                } else if (table[i][2] > 0) {
                    int readBits = reader.readBits(table[i][2]);
                    decodedInt = table[i][0] + readBits;
                } else {
                    decodedInt = table[i][0];
                }
                return new DecodeIntResult(decodedInt, true);
            }
        }

        return new DecodeIntResult(-1, false);
    }

    public static int[][] buildTable(int[][] table, int length) {
        int i, j, k, prefix;
        int[] tab;

        for (i = 0; i < length; i++) {
            for (j = i; j < length && table[j][1] == 0; j++) ;

            if (j == length) {
                break;
            }
            for (k = j + 1; k < length; k++) {
                if (table[k][1] > 0 && table[k][1] < table[j][1]) {
                    j = k;
                }
            }
            if (j != i) {
                tab = table[j];
                for (k = j; k > i; k--) {
                    table[k] = table[k - 1];
                }
                table[i] = tab;
            }
        }
        table[i] = table[length];

        i = 0;
        prefix = 0;
        table[i++][3] = prefix++;
        for (; table[i][2] != jbig2HuffmanEOT; i++) {
            prefix <<= table[i][1] - table[i - 1][1];
            table[i][3] = prefix++;
        }

        return table;
    }

    public static int huffmanTableA[][] = {{0, 1, 4, 0x000}, {16, 2, 8, 0x002}, {272, 3, 16, 0x006}, {65808, 3, 32, 0x007}, {0, 0, jbig2HuffmanEOT, 0}};

    public static int huffmanTableB[][] = {{0, 1, 0, 0x000}, {1, 2, 0, 0x002}, {2, 3, 0, 0x006}, {3, 4, 3, 0x00e}, {11, 5, 6, 0x01e}, {75, 6, 32, 0x03e}, {0, 6, jbig2HuffmanOOB, 0x03f}, {0, 0, jbig2HuffmanEOT, 0}};

    public static int huffmanTableC[][] = {{0, 1, 0, 0x000}, {1, 2, 0, 0x002}, {2, 3, 0, 0x006}, {3, 4, 3, 0x00e}, {11, 5, 6, 0x01e}, {0, 6, jbig2HuffmanOOB, 0x03e}, {75, 7, 32, 0x0fe}, {-256, 8, 8, 0x0fe}, {-257, 8, jbig2HuffmanLOW, 0x0ff}, {0, 0, jbig2HuffmanEOT, 0}};

    public static int huffmanTableD[][] = {{1, 1, 0, 0x000}, {2, 2, 0, 0x002}, {3, 3, 0, 0x006}, {4, 4, 3, 0x00e}, {12, 5, 6, 0x01e}, {76, 5, 32, 0x01f}, {0, 0, jbig2HuffmanEOT, 0}};

    public static int huffmanTableE[][] = {{1, 1, 0, 0x000}, {2, 2, 0, 0x002}, {3, 3, 0, 0x006}, {4, 4, 3, 0x00e}, {12, 5, 6, 0x01e}, {76, 6, 32, 0x03e}, {-255, 7, 8, 0x07e}, {-256, 7, jbig2HuffmanLOW, 0x07f}, {0, 0, jbig2HuffmanEOT, 0}};

    public static int huffmanTableF[][] = {{0, 2, 7, 0x000}, {128, 3, 7, 0x002}, {256, 3, 8, 0x003}, {-1024, 4, 9, 0x008}, {-512, 4, 8, 0x009}, {-256, 4, 7, 0x00a}, {-32, 4, 5, 0x00b}, {512, 4, 9, 0x00c}, {1024, 4, 10, 0x00d}, {-2048, 5, 10, 0x01c}, {-128, 5, 6, 0x01d}, {-64, 5, 5, 0x01e}, {-2049, 6, jbig2HuffmanLOW, 0x03e}, {2048, 6, 32, 0x03f}, {0, 0, jbig2HuffmanEOT, 0}};

    public static int huffmanTableG[][] = {{-512, 3, 8, 0x000}, {256, 3, 8, 0x001}, {512, 3, 9, 0x002}, {1024, 3, 10, 0x003}, {-1024, 4, 9, 0x008}, {-256, 4, 7, 0x009}, {-32, 4, 5, 0x00a}, {0, 4, 5, 0x00b}, {128, 4, 7, 0x00c}, {-128, 5, 6, 0x01a}, {-64, 5, 5, 0x01b}, {32, 5, 5, 0x01c}, {64, 5, 6, 0x01d}, {-1025, 5, jbig2HuffmanLOW, 0x01e}, {2048, 5, 32, 0x01f}, {0, 0, jbig2HuffmanEOT, 0}};

    public static int huffmanTableH[][] = {{0, 2, 1, 0x000}, {0, 2, jbig2HuffmanOOB, 0x001}, {4, 3, 4, 0x004}, {-1, 4, 0, 0x00a}, {22, 4, 4, 0x00b}, {38, 4, 5, 0x00c}, {2, 5, 0, 0x01a}, {70, 5, 6, 0x01b}, {134, 5, 7, 0x01c}, {3, 6, 0, 0x03a}, {20, 6, 1, 0x03b}, {262, 6, 7, 0x03c}, {646, 6, 10, 0x03d}, {-2, 7, 0, 0x07c}, {390, 7, 8, 0x07d}, {-15, 8, 3, 0x0fc}, {-5, 8, 1, 0x0fd}, {-7, 9, 1, 0x1fc}, {-3, 9, 0, 0x1fd}, {-16, 9, jbig2HuffmanLOW, 0x1fe}, {1670, 9, 32, 0x1ff}, {0, 0, jbig2HuffmanEOT, 0}};

    public static int huffmanTableI[][] = {{0, 2, jbig2HuffmanOOB, 0x000}, {-1, 3, 1, 0x002}, {1, 3, 1, 0x003}, {7, 3, 5, 0x004}, {-3, 4, 1, 0x00a}, {43, 4, 5, 0x00b}, {75, 4, 6, 0x00c}, {3, 5, 1, 0x01a}, {139, 5, 7, 0x01b}, {267, 5, 8, 0x01c}, {5, 6, 1, 0x03a}, {39, 6, 2, 0x03b}, {523, 6, 8, 0x03c}, {1291, 6, 11, 0x03d}, {-5, 7, 1, 0x07c}, {779, 7, 9, 0x07d}, {-31, 8, 4, 0x0fc}, {-11, 8, 2, 0x0fd}, {-15, 9, 2, 0x1fc}, {-7, 9, 1, 0x1fd}, {-32, 9, jbig2HuffmanLOW, 0x1fe}, {3339, 9, 32, 0x1ff}, {0, 0, jbig2HuffmanEOT, 0}};

    public static int huffmanTableJ[][] = {{-2, 2, 2, 0x000}, {6, 2, 6, 0x001}, {0, 2, jbig2HuffmanOOB, 0x002}, {-3, 5, 0, 0x018}, {2, 5, 0, 0x019}, {70, 5, 5, 0x01a}, {3, 6, 0, 0x036}, {102, 6, 5, 0x037}, {134, 6, 6, 0x038}, {198, 6, 7, 0x039}, {326, 6, 8, 0x03a}, {582, 6, 9, 0x03b}, {1094, 6, 10, 0x03c}, {-21, 7, 4, 0x07a}, {-4, 7, 0, 0x07b}, {4, 7, 0, 0x07c}, {2118, 7, 11, 0x07d}, {-5, 8, 0, 0x0fc}, {5, 8, 0, 0x0fd}, {-22, 8, jbig2HuffmanLOW, 0x0fe}, {4166, 8, 32, 0x0ff}, {0, 0, jbig2HuffmanEOT, 0}};

    public static int huffmanTableK[][] = {{1, 1, 0, 0x000}, {2, 2, 1, 0x002}, {4, 4, 0, 0x00c}, {5, 4, 1, 0x00d}, {7, 5, 1, 0x01c}, {9, 5, 2, 0x01d}, {13, 6, 2, 0x03c}, {17, 7, 2, 0x07a}, {21, 7, 3, 0x07b}, {29, 7, 4, 0x07c}, {45, 7, 5, 0x07d}, {77, 7, 6, 0x07e}, {141, 7, 32, 0x07f}, {0, 0, jbig2HuffmanEOT, 0}};

    public static int huffmanTableL[][] = {{1, 1, 0, 0x000}, {2, 2, 0, 0x002}, {3, 3, 1, 0x006}, {5, 5, 0, 0x01c}, {6, 5, 1, 0x01d}, {8, 6, 1, 0x03c}, {10, 7, 0, 0x07a}, {11, 7, 1, 0x07b}, {13, 7, 2, 0x07c}, {17, 7, 3, 0x07d}, {25, 7, 4, 0x07e}, {41, 8, 5, 0x0fe}, {73, 8, 32, 0x0ff}, {0, 0, jbig2HuffmanEOT, 0}};

    public static int huffmanTableM[][] = {{1, 1, 0, 0x000}, {2, 3, 0, 0x004}, {7, 3, 3, 0x005}, {3, 4, 0, 0x00c}, {5, 4, 1, 0x00d}, {4, 5, 0, 0x01c}, {15, 6, 1, 0x03a}, {17, 6, 2, 0x03b}, {21, 6, 3, 0x03c}, {29, 6, 4, 0x03d}, {45, 6, 5, 0x03e}, {77, 7, 6, 0x07e}, {141, 7, 32, 0x07f}, {0, 0, jbig2HuffmanEOT, 0}};

    public static int huffmanTableN[][] = {{0, 1, 0, 0x000}, {-2, 3, 0, 0x004}, {-1, 3, 0, 0x005}, {1, 3, 0, 0x006}, {2, 3, 0, 0x007}, {0, 0, jbig2HuffmanEOT, 0}};

    public static int huffmanTableO[][] = {{0, 1, 0, 0x000}, {-1, 3, 0, 0x004}, {1, 3, 0, 0x005}, {-2, 4, 0, 0x00c}, {2, 4, 0, 0x00d}, {-4, 5, 1, 0x01c}, {3, 5, 1, 0x01d}, {-8, 6, 2, 0x03c}, {5, 6, 2, 0x03d}, {-24, 7, 4, 0x07c}, {9, 7, 4, 0x07d}, {-25, 7, jbig2HuffmanLOW, 0x07e}, {25, 7, 32, 0x07f}, {0, 0, jbig2HuffmanEOT, 0}};
}
