package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Common;

import java.io.IOException;

import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.HRESULT;

public class StreamUtils
{    
    static public int  ReadStream(java.io.InputStream stream, byte [] data,int off, int size) throws IOException
    {
        int processedSize = 0;

        while(size != 0)
        {
             int processedSizeLoc = stream.read(data,off + processedSize,size);
             if (processedSizeLoc > 0)
             {
                processedSize += processedSizeLoc;
                size -= processedSizeLoc;
             }
             if (processedSizeLoc == -1) {
                 if (processedSize > 0) return processedSize;
                 return -1; // EOF
             }
        }
        return processedSize;
    }
    
    // HRESULT WriteStream(ISequentialOutStream *stream, const void *data, UInt32 size, UInt32 *processedSize);
}
