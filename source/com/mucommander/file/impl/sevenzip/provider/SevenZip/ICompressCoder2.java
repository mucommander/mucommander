package com.mucommander.file.impl.sevenzip.provider.SevenZip;

import com.mucommander.file.impl.sevenzip.provider.Common.RecordVector;

public interface ICompressCoder2 {
    public int Code(
            RecordVector inStreams,
            Object useless1, // const UInt64 ** /* inSizes */,
            int numInStreams,
            RecordVector outStreams,
            Object useless2, // const UInt64 ** /* outSizes */,
            int numOutStreams,
            ICompressProgressInfo progress) throws java.io.IOException;
    
    public void close() throws java.io.IOException ; // destructor
}
