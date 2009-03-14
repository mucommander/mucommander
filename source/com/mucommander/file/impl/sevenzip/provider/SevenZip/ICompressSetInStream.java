package com.mucommander.file.impl.sevenzip.provider.SevenZip;

public interface ICompressSetInStream {
    public int SetInStream(java.io.InputStream inStream);
    public int ReleaseInStream() throws java.io.IOException ;
}

