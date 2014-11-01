package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Archive.SevenZip;

import com.mucommander.commons.file.impl.sevenzip.provider.Common.ByteBuffer;

class AltCoderInfo {
    public MethodID MethodID;
    public ByteBuffer Properties;
    
    public AltCoderInfo() {
        MethodID = new MethodID();
        Properties = new ByteBuffer();
    } 
}
