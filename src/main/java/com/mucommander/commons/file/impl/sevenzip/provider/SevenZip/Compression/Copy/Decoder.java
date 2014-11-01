
package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.Copy;

import java.io.IOException;

import com.mucommander.commons.file.impl.sevenzip.provider.Common.RecordVector;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.HRESULT;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.ICompressCoder;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.ICompressProgressInfo;


public class Decoder implements ICompressCoder {
    
    static final int kBufferSize = 1 << 17;
    
    public int Code(
            java.io.InputStream inStream, // , ISequentialInStream
            java.io.OutputStream outStream, // ISequentialOutStream
            long outSize, ICompressProgressInfo progress) throws java.io.IOException {
        
        byte [] _buffer = new byte[kBufferSize];
        long TotalSize = 0;
        
        for (;;) {
            int realProcessedSize;
            int size = kBufferSize;
            
            if (outSize != -1) // NULL
                if (size > (outSize - TotalSize))
                    size = (int)(outSize - TotalSize);
            
            realProcessedSize = inStream.read(_buffer, 0,size);
            if(realProcessedSize == -1) // EOF
                break;
            outStream.write(_buffer,0,realProcessedSize);
            TotalSize += realProcessedSize;
            if (progress != null) {
                int res = progress.SetRatioInfo(TotalSize, TotalSize);
                if (res != HRESULT.S_OK) return res;
            }
        }
        return HRESULT.S_OK;  
    }
}
