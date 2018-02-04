package com.mucommander.commons.file.archive.sevenzip.provider.SevenZip;

public interface IProgress {
    public int SetTotal(long total);

    public int SetCompleted(long completeValue);
}

