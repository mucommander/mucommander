package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Archive.Common;

import com.mucommander.commons.file.impl.sevenzip.provider.Common.LongVector;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.ICompressCoder;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.ICompressCoder2;



public class CoderInfo {
    ICompressCoder Coder;
    ICompressCoder2 Coder2;
    int NumInStreams;
    int NumOutStreams;
    
    LongVector InSizes = new LongVector();
    LongVector OutSizes = new LongVector();
    LongVector InSizePointers = new LongVector();
    LongVector OutSizePointers = new LongVector();
    
    public CoderInfo(int numInStreams, int numOutStreams) {
        NumInStreams = numInStreams;
        NumOutStreams = numOutStreams;
        InSizes.Reserve(NumInStreams);
        InSizePointers.Reserve(NumInStreams);
        OutSizePointers.Reserve(NumOutStreams);
        OutSizePointers.Reserve(NumOutStreams);
    }
    
    static public void SetSizes(
            LongVector srcSizes,
            LongVector sizes,
            LongVector sizePointers,
            int numItems)
    {
        sizes.clear();
        sizePointers.clear();
        for(int i = 0; i < numItems; i++) {
            if (srcSizes == null || srcSizes.get(i) == -1)  // TBD null => -1
            {
                sizes.add(new Long(0));
                sizePointers.add(-1);
            } else {
                sizes.add(srcSizes.get(i)); // sizes.Add(*srcSizes[i]);
                sizePointers.add(sizes.Back()); // sizePointers.Add(&sizes.Back());
            }
        }
    }
    
    public void SetCoderInfo(
            LongVector inSizes, //  const UInt64 **inSizes,
            LongVector outSizes) //const UInt64 **outSizes)
    {
        SetSizes(inSizes, InSizes, InSizePointers, NumInStreams);
        SetSizes(outSizes, OutSizes, OutSizePointers, NumOutStreams);
    }
}
