package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip;

public interface ICompressProgressInfo {
    public static final long INVALID = -1;
    int SetRatioInfo(long inSize, long outSize);
}
