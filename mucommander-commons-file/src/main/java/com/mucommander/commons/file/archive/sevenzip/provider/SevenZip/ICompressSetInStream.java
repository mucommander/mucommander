package com.mucommander.commons.file.archive.sevenzip.provider.SevenZip;

public interface ICompressSetInStream {
    public int SetInStream(java.io.InputStream inStream);

    public int ReleaseInStream() throws java.io.IOException;
}

