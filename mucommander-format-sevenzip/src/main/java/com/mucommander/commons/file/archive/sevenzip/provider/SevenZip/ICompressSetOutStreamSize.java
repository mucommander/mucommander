package com.mucommander.commons.file.archive.sevenzip.provider.SevenZip;

public interface ICompressSetOutStreamSize {
    public static final int INVALID_OUTSIZE=-1;
    public int SetOutStreamSize(long outSize);
}

