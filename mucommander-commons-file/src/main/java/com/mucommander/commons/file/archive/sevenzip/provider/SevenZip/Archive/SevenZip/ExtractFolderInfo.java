package com.mucommander.commons.file.archive.sevenzip.provider.SevenZip.Archive.SevenZip;

import com.mucommander.commons.file.archive.sevenzip.provider.Common.BoolVector;


class ExtractFolderInfo {
    /* #ifdef _7Z_VOL
       int VolumeIndex;
    #endif */
    public int FileIndex;
    public int FolderIndex;
    public BoolVector ExtractStatuses = new BoolVector();
    public long UnPackSize;

    public ExtractFolderInfo(
    /* #ifdef _7Z_VOL
    int volumeIndex, 
    #endif */
            int fileIndex, int folderIndex)  // CNum fileIndex, CNum folderIndex
    {
    /* #ifdef _7Z_VOL
    VolumeIndex(volumeIndex),
    #endif */
        FileIndex = fileIndex;
        FolderIndex = folderIndex;
        UnPackSize = 0;

        if (fileIndex != InArchive.kNumNoIndex) {
            ExtractStatuses.Reserve(1);
            ExtractStatuses.add(true);
        }
    }
}

