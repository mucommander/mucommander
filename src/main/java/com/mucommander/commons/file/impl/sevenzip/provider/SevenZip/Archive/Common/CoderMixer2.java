package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Archive.Common;

import com.mucommander.commons.file.impl.sevenzip.provider.Common.LongVector;

public interface CoderMixer2 {
    
    void ReInit();

    void SetBindInfo(BindInfo bindInfo);

    void SetCoderInfo(int coderIndex,LongVector inSizes, LongVector outSizes);
}
