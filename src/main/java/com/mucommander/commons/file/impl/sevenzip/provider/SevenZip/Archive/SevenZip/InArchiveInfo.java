package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Archive.SevenZip;

import com.mucommander.commons.file.impl.sevenzip.provider.Common.LongVector;

class InArchiveInfo
{
    public byte ArchiveVersion_Major;
    public byte ArchiveVersion_Minor;
    
    public long StartPosition;
    public long StartPositionAfterHeader;
    public long DataStartPosition;
    public long DataStartPosition2;    
    LongVector FileInfoPopIDs = new LongVector();
    
    void Clear()
    {
        FileInfoPopIDs.clear();
    }    
}