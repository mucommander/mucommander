package com.mucommander.file.impl.sevenzip.provider.SevenZip.Archive.SevenZip;
import com.mucommander.file.impl.sevenzip.provider.Common.RecordVector;
import com.mucommander.file.impl.sevenzip.provider.SevenZip.Archive.Common.BindInfo;



class BindInfoEx extends BindInfo {
    
    RecordVector CoderMethodIDs = new RecordVector();
    
    public void Clear() {
        super.Clear(); // CBindInfo::Clear();
        CoderMethodIDs.clear();
    }
}
