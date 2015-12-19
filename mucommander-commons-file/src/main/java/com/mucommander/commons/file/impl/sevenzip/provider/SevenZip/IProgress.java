package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip;

public interface IProgress {
    public int SetTotal(long total);
    public int SetCompleted(long completeValue);
}

