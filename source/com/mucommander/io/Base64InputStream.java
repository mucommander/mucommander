package com.mucommander.io;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
 * Base64InputStream allows to read and decode Base64-encoded data.
 * 
 * @author Maxence Bernard
 */
public class Base64InputStream extends InputStream {

    /** Underlying stream data is read from */
    private InputStream in;

    /** Decoded bytes available for reading */
    private int readBuffer[] = new int[3];

    /** Index of the next byte available for reading in the buffer */
    private int readOffset;

    /** Number of bytes left for reading in the buffer */
    private int bytesLeft;

    /** Buffer used temporarily for decoding */
    private int decodeBuffer[] = new int[4];

    /** Decoding table */
    private final static int BASE64_DECODING_TABLE[];
    

    // Create the base 64 decoding table
    static {
        BASE64_DECODING_TABLE = new int[256];
        int offset;
        char c;

        for(c=0; c<256; c++)
            BASE64_DECODING_TABLE[c] = -1;

        offset = 0;
        for(c='A'; c<='Z'; c++)
            BASE64_DECODING_TABLE[c] = offset++;

        for(c='a'; c<='z'; c++)
            BASE64_DECODING_TABLE[c] = offset++;

        for(c='0'; c<='9'; c++)
            BASE64_DECODING_TABLE[c] = offset++;

        BASE64_DECODING_TABLE['+'] = 62;
        BASE64_DECODING_TABLE['/'] = 63;
    }


    /**
     * Creates a new Base64InputStream that allows to decode Base64-encoded from the provided InputStream.
     *
     * @param in underlying InputStream the Base64-encoded data is read from
     */
    public Base64InputStream(InputStream in) {
        this.in = in;
    }


    /**
     * Convenience method that decodes the given Base64-encoded String and returns it decoded.
     * Throws an IOException if the given String wasn't properly Base64-encoded, or if an IOException occurred while
     * reading the underlying InputStream.
     *
     * @param s a Base64-encoded String
     * @return the decoded String
     * @throw IOException if the given String wasn't properly Base64-encoded, or if an IOException occurred
     * while accessing the underlying InputStream.
     */
    public static String decode(String s) throws IOException {
        Base64InputStream in64 = new Base64InputStream(new ByteArrayInputStream(s.getBytes()));

        try {
            StringBuffer sb = new StringBuffer();
            int i;
            while((i=in64.read())!=-1)
                sb.append((char)i);

            return sb.toString();
        }
        finally {
            in64.close();
        }
    }


    public int read() throws IOException {
        // Read buffer empty: read and decode a new base64-encoded 4-byte group
        if(bytesLeft==0) {
            int read;
            int nbRead = 0;

            while(nbRead<4) {
                read = in.read();
                // EOF reached
                if(read==-1) {
                    if(nbRead%4 != 0) {
                        // Base64 encoded data must come in a multiple of 4 bytes, throw an IOException if the underlying stream ended prematurely
                        throw new IOException("InputStream did not end on a multiple of 4 bytes");
                    }

                    if(nbRead==0)
                        return -1;
                    else    // nbRead==4
                        break;
                }

                decodeBuffer[nbRead] = BASE64_DECODING_TABLE[read];

                // Discard any character that's not a base64 character, without throwing an IOException.
                // In particular, '\r' and '\n' characters that are usually found in email attachments are simply ignored.
                if(decodeBuffer[nbRead]==-1 && read!='=') {
                    continue;
                }

                nbRead++;
            }

            // Decode byte 0
            readBuffer[bytesLeft++] = ((decodeBuffer[0]<<2)&0xFC | ((decodeBuffer[1]>>4)&0x03));

            // Test if the character is not padding ('=')
            if(decodeBuffer[2]!=-1) {
                // Decode byte 1
                readBuffer[bytesLeft++] = (decodeBuffer[1]<<4)&0xF0 | ((decodeBuffer[2]>>2)&0x0F);

                // Test if the character is not padding ('=')
                if(decodeBuffer[3]!=-1)
                    // Decode byte 2
                    readBuffer[bytesLeft++] = ((decodeBuffer[2]<<6)&0xC0) | (decodeBuffer[3]&0x3F);
            }

            readOffset = 0;
        }

        bytesLeft--;

        return readBuffer[readOffset++];
    }


//    /**
//     * Unit test that validates the fact that a string remains the same after a Base64 encoding and decoding.
//     */
//    public static void main(String args[]) throws IOException {
//        Random random = new Random();
//
//        int slen;
//        StringBuffer sb;
//        String s;
//        for(int i=0; i<200; i++) {
//            slen = random.nextInt(1000);
//
//            sb = new StringBuffer();
//            for(int j=0; j<slen; j++) {
//                sb.append((char)random.nextInt(256));
//            }
//
//            s = sb.toString();
//            System.out.println("Random string length="+slen+" Base64 encoding+decoding matches original= "+decode(Base64OutputStream.encode(s)).equals(s));
//        }
//    }
}
