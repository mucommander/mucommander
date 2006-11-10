package com.mucommander.bookmark;

import com.mucommander.io.Base64InputStream;
import com.mucommander.io.Base64OutputStream;

import java.io.IOException;

/**
 * @author Maxence Bernard
 */
public class XORCipher {

    public final static char NOT_SO_PRIVATE_KEY[] = {
        161, 220, 156, 76, 177, 174, 56, 37, 98, 93, 224, 19, 160, 95, 69, 140,
        91, 138, 33, 114, 248, 57, 179, 17, 54, 172, 249, 58, 26, 181, 167, 231,
        241, 185, 218, 174, 37, 102, 100, 26, 16, 214, 119, 29, 118, 151, 135, 175,
        245, 247, 160, 188, 77, 173, 109, 255, 73, 44, 186, 211, 117, 236, 204, 58,
        246, 210, 128, 33, 234, 218, 82, 188, 78, 229, 180, 108, 247, 200, 3, 142,
        206, 45, 165, 111, 96, 72, 76, 81, 238, 186, 240, 167, 185, 152, 68, 228,
        87, 142, 145, 7, 74, 12, 106, 94, 15, 218, 155, 71, 87, 136, 58, 40,
        246, 94, 7, 89, 29, 0, 78, 204, 70, 220, 240, 127, 59, 184, 109, 106
    };


    private static String xor(String s) {
        StringBuffer sb = new StringBuffer();

        int len = s.length();
        int keyLen = NOT_SO_PRIVATE_KEY.length;

        for(int i=0; i<len; i++)
            sb.append((char)(s.charAt(i)^NOT_SO_PRIVATE_KEY[i%keyLen]));

        return(sb.toString());
    }


    public static String encodeXORBase64(String s) {
        return Base64OutputStream.encode(xor(s));
    }


    public static String decodeXORBase64(String s) throws IOException {
        return xor(Base64InputStream.decode(s));
    }
}
