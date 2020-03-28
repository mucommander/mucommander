package com.sun.jna.platform.mac;

import com.sun.jna.Memory;

public class XAttrUtils {
    public static byte[] read(String path, String name) {
        long bufferLength = XAttr.INSTANCE.getxattr(path, name, null, 0, 0, 0);
        Memory valueBuffer = new Memory(bufferLength);
        valueBuffer.clear();
        long valueLength = XAttr.INSTANCE.getxattr(path, name, valueBuffer, bufferLength, 0, 0);

        if (valueLength < 0) {
            return null;
        }
        return valueBuffer.getByteArray(0, (int)valueLength);
    }

    public static void write(String path, String name, byte[] value) {
        Memory valueBuffer = new Memory(value.length);
        valueBuffer.write(0, value, 0, value.length);
        XAttr.INSTANCE.setxattr(path, name, valueBuffer, valueBuffer.size(), 0, 0);
    }
}
