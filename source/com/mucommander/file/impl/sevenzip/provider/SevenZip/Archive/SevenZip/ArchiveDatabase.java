package com.mucommander.file.impl.sevenzip.provider.SevenZip.Archive.SevenZip;

import com.mucommander.file.impl.sevenzip.provider.Common.BoolVector;
import com.mucommander.file.impl.sevenzip.provider.Common.IntVector;
import com.mucommander.file.impl.sevenzip.provider.Common.LongVector;
import com.mucommander.file.impl.sevenzip.provider.Common.ObjectVector;

class ArchiveDatabase {
    public LongVector PackSizes = new LongVector();
    public BoolVector PackCRCsDefined = new BoolVector();
    public IntVector PackCRCs = new IntVector();
    public ObjectVector Folders = new ObjectVector();
    public IntVector NumUnPackStreamsVector = new IntVector();
    public ObjectVector Files = new ObjectVector();
    
    void Clear() {
        PackSizes.clear();
        PackCRCsDefined.clear();
        PackCRCs.clear();
        Folders.clear();
        NumUnPackStreamsVector.clear();
        Files.clear();
    }
}