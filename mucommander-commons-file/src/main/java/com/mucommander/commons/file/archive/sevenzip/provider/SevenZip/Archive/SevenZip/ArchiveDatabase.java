package com.mucommander.commons.file.archive.sevenzip.provider.SevenZip.Archive.SevenZip;

import com.mucommander.commons.file.archive.sevenzip.provider.Common.BoolVector;
import com.mucommander.commons.file.archive.sevenzip.provider.Common.IntVector;
import com.mucommander.commons.file.archive.sevenzip.provider.Common.LongVector;
import com.mucommander.commons.file.archive.sevenzip.provider.Common.ObjectVector;

class ArchiveDatabase {
    public LongVector PackSizes = new LongVector();
    public BoolVector PackCRCsDefined = new BoolVector();
    public IntVector PackCRCs = new IntVector();
    public ObjectVector<Folder> Folders = new ObjectVector<Folder>();
    public IntVector NumUnPackStreamsVector = new IntVector();
    public ObjectVector<FileItem> Files = new ObjectVector<FileItem>();

    void Clear() {
        PackSizes.clear();
        PackCRCsDefined.clear();
        PackCRCs.clear();
        Folders.clear();
        NumUnPackStreamsVector.clear();
        Files.clear();
    }
}
