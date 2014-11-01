package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Archive.SevenZip;

import com.mucommander.commons.file.impl.sevenzip.provider.Common.ObjectVector;

class CoderInfo {
    
    int NumInStreams;
    int NumOutStreams;
    public ObjectVector<AltCoderInfo> AltCoders = new com.mucommander.commons.file.impl.sevenzip.provider.Common.ObjectVector<AltCoderInfo>();
    
    boolean IsSimpleCoder() { return (NumInStreams == 1) && (NumOutStreams == 1); }
    
    public CoderInfo() {
        NumInStreams = 0;
        NumOutStreams = 0;
    }
}
