
package com.mucommander.io;

import java.io.IOException;
import java.io.OutputStream;


/**
 * This OuputStream encodes data in MIME Base64. It is used to by SendMailJob to send email attachments.
 *
 * @author originally found on the net and then modified by Maxence Bernard
 */
public class Base64OutputStream extends OutputStream {
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

    /** Base 64 translation table */
    private final static char BASE_TABLE[] = {
        'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P',
        'Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f',
        'g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v',
        'w','x','y','z','0','1','2','3','4','5','6','7','8','9','+','/'
    };

	
    /** Underlying OutputStream encoded data is sent to */
    private OutputStream out;

    /** Array used to accumulate the first 2 bytes of a 3-byte group */
    private byte byteAcc[] = new byte[2];
	
    /** Number of bytes accumulated to form a 3-byte group */
    private int nbBytesWaiting;

    /** Current line length (to insert line return character after 80 chars)*/
    private int lineLength;
	
	
    public Base64OutputStream(OutputStream out) {
        this.out = out;
    }
    
	
    public void write(int i) throws IOException {
        // We have a 3-byte group
        if(nbBytesWaiting==2) {
            // Write 3 bytes as 4 base64 characters
            out.write(BASE_TABLE[(byte)((byteAcc[0] & 0xFC) >> 2)]);
            out.write(BASE_TABLE[(byte)(((byteAcc[0] & 0x03) << 4) | ((byteAcc[1] & 0xF0) >> 4))]);
            out.write(BASE_TABLE[(byte)(((byteAcc[1] & 0x0F) << 2) | ((i & 0xC0) >> 6))]);
            out.write(BASE_TABLE[(byte)(i & 0x3F)]);

            nbBytesWaiting = 0;

            if ((lineLength += 4) >= 76) {
                out.write('\r');
                out.write('\n');
                lineLength = 0;
            }
        }
        // Waiting for more bytes...
        else {
            byteAcc[nbBytesWaiting++] = (byte)i;
        }
    }

	
    /**
     * Writes padding if there currently is an unfinished 3-byte group.
     */
    public void writePadding() throws IOException {
        // No padding needed
        if(nbBytesWaiting==0)
            return;

        // deals with with the padding ...
        if (nbBytesWaiting==2) {
            // 2 bytes left
            out.write(BASE_TABLE[(byte)((byteAcc[0] & 0xFC) >> 2)]);
            out.write(BASE_TABLE[(byte)(((byteAcc[0] & 0x03) << 4) | ((byteAcc[1] & 0xF0) >> 4))]);
            out.write(BASE_TABLE[(byte)((byteAcc[1] & 0x0F) << 2)]);
            out.write('=');
        }
        else if (nbBytesWaiting==1) {
            // 1 byte left
            out.write(BASE_TABLE[(byte)((byteAcc[0] & 0xFC) >> 2)]);
            out.write(BASE_TABLE[(byte)((byteAcc[0] & 0x03) << 4)]);
            out.write('=');
            out.write('=');
        }
		
        nbBytesWaiting = 0;
    }	
	
	
    /**
     * Writes padding if necessary and closes the underlying stream.
     */
    public void close() throws IOException {
        writePadding();
        out.close();
    }
	
}
