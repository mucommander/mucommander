
package com.mucommander.job;

import com.mucommander.file.AbstractFile;

import java.io.*;

/**
 *
 */
public class MIMEBase64Encoder {
    /*
     Base64 uses a 65 character subset of US-ASCII,
     allowing 6 bits for each character so the character
     "m" with a Base64 value of 38, when represented
     in binary form, is 100110.

     With a text string, let's say "men" is encoded this
     is what happens :

     The text string is converted into its US-ASCII value.

     The character "m" has the decimal value of 109
     The character "e" has the decimal value of 101
     The character "n" has the decimal value of 110

     When converted to binary the string looks like this :

     m   01101101
     e   01100101
     n   01101110

     These three "8-bits" are concatenated to make a
     24 bit stream
     011011010110010101101110

     This 24 bit stream is then split up into 4 6-bit
     sections
     011011 010110 010101 101110

     We now have 4 values. These binary values are
     converted to decimal form
     27     22     21     46

     And the corresponding Base64 character are :
     b      W       V     u

     The encoding is always on a three characters basis
     (to have a set of 4 Base64 characters). To encode one
     or two then, we use the special character "=" to pad
     until 4 base64 characters is reached.

     ex. encode "me"

     01101101  01100101
     0110110101100101
     011011 010110 0101
     111111    (AND to fill the missing bits)
     011011 010110 010100
     b     W      U
     b     W      U     =  ("=" is the padding character)

     so "bWU="  is the base64 equivalent.

     encode "m"

     01101101
     011011 01
     111111         (AND to fill the missing bits)
     011011 010000
     b     Q     =  =   (two paddings are added)

     Finally, MIME specifies that lines are 76 characters wide maximum.

     */

    private final static char BASE_TABLE[] = {
        'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P',
        'Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f',
        'g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v',
        'w','x','y','z','0','1','2','3','4','5','6','7','8','9','+','/'
    };

    private final static int BLOCK_SIZE = 1024;

    private static long totalRead;
    
    public MIMEBase64Encoder() {
    }
    
    static void encode(AbstractFile file, OutputStream out) throws IOException {
        InputStream in = file.getInputStream();
        byte bytes[] = new byte[BLOCK_SIZE];

        int n;
        totalRead = 0;
        int n3byt, nrest=0, k=0, linelength=0, i;
        byte buf[] = new byte[4];   // array of base64 characters
        
        while ((n=in.read(bytes, nrest, BLOCK_SIZE-nrest))!=-1) {
            System.out.println("nbread ="+n+" nrest="+nrest);

            totalRead += n;
            n += nrest;
            
            n3byt      = n / 3;     // how 3 bytes groups?
            nrest      = n % 3;     // the remaining bytes from the grouping
            k          = n3byt * 3; // we are doing 3 bytes at a time
            i          = 0;         // index

            // do the 3-bytes groups ...
            while ( i < k ) {
                buf[0] = (byte)(( bytes[i]   & 0xFC) >> 2);
                buf[1] = (byte)(((bytes[i]   & 0x03) << 4) |
                            ((bytes[i+1] & 0xF0) >> 4));
                buf[2] = (byte)(((bytes[i+1] & 0x0F) << 2) |
                            ((bytes[i+2] & 0xC0) >> 6));
                buf[3] = (byte)(  bytes[i+2] & 0x3F);
                out.write(BASE_TABLE[buf[0]]);
                out.write(BASE_TABLE[buf[1]]);
                out.write(BASE_TABLE[buf[2]]);
                out.write(BASE_TABLE[buf[3]]);

                if ((linelength += 4) >= 76) {
                    out.write('\r');
                    out.write('\n');
                    linelength = 0;
                }
                i += 3;
            }

            if(nrest>0  && n3byt>0) {
                // Move remaining bytes back to the beginning of the byte array
                for(int j=0; j<nrest; j++)
                    bytes[j] = bytes[BLOCK_SIZE-nrest+j];
//                System.arraycopy(bytes, BLOCK_SIZE-nrest, bytes, 0, nrest);
            }
        }

        // deals with with the padding ...
        if (nrest==2) {
            // 2 bytes left
            buf[0] = (byte)(( bytes[k] & 0xFC)   >> 2);
            buf[1] = (byte)(((bytes[k] & 0x03)   << 4) |
                            ((bytes[k+1] & 0xF0) >> 4));
            buf[2] = (byte)(( bytes[k+1] & 0x0F) << 2);
        }
        else if (nrest==1) {
            // 1 byte left
            buf[0] = (byte)((bytes[k] & 0xFC) >> 2);
            buf[1] = (byte)((bytes[k] & 0x03) << 4);
        }
        
        if (nrest > 0) {
            // send the padding
            if ((linelength += 4) >= 76) {
                out.write('\r');
                out.write('\n');
            }
            out.write(BASE_TABLE[buf[0]]);
            out.write(BASE_TABLE[buf[1]]);

            if (nrest==2) {
                out.write(BASE_TABLE[buf[2]]);
            }
            else {
                out.write('=');
            }
            out.write('=');
        }
        
        in.close();
        out.flush();
            
    }

    /**
     * Returns the number of bytes that have been read from the file that is currently being encoded
     */
    public long getTotalRead() {
        return totalRead;
    }
}
