package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip;

public interface ICompressSetOutStream {
    public int SetOutStream(java.io.OutputStream inStream);
    public int ReleaseOutStream() throws java.io.IOException;
}

