package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Archive.Common;

import com.mucommander.commons.file.impl.sevenzip.provider.Common.IntVector;
import com.mucommander.commons.file.impl.sevenzip.provider.Common.RecordVector;

public class BindInfo {
    public RecordVector<CoderStreamsInfo> Coders = new RecordVector<CoderStreamsInfo>();
    public RecordVector<BindPair> BindPairs = new RecordVector<BindPair>();
    public IntVector InStreams = new IntVector();
    public IntVector OutStreams = new IntVector();
    
    public void Clear() {
        Coders.clear();
        BindPairs.clear();
        InStreams.clear();
        OutStreams.clear();
    }
      
    public int FindBinderForInStream(int inStream) // const
    {
        for (int i = 0; i < BindPairs.size(); i++)
            if (BindPairs.get(i).InIndex == inStream)
                return i;
        return -1;
    }
    
    public int FindBinderForOutStream(int outStream) // const
    {
        for (int i = 0; i < BindPairs.size(); i++)
            if (BindPairs.get(i).OutIndex == outStream)
                return i;
        return -1;
    }
    
    public int GetCoderInStreamIndex(int coderIndex) // const
    {
        int streamIndex = 0;
        for (int i = 0; i < coderIndex; i++)
            streamIndex += Coders.get(i).NumInStreams;
        return streamIndex;
    }
    
    public int GetCoderOutStreamIndex(int coderIndex) // const
    {
        int streamIndex = 0;
        for (int i = 0; i < coderIndex; i++)
            streamIndex += Coders.get(i).NumOutStreams;
        return streamIndex;
    }
    
    public void FindInStream(int streamIndex,
            int [] coderIndex, // UInt32 &coderIndex
            int [] coderStreamIndex // UInt32 &coderStreamIndex
            )
            
    {
        for (coderIndex[0] = 0; coderIndex[0] < Coders.size(); coderIndex[0]++) {
            int curSize = Coders.get(coderIndex[0]).NumInStreams;
            if (streamIndex < curSize) {
                coderStreamIndex[0] = streamIndex;
                return;
            }
            streamIndex -= curSize;
        }
        throw new UnknownError("1");
    }
    
    public void FindOutStream(int streamIndex,
            int [] coderIndex, // UInt32 &coderIndex,
            int [] coderStreamIndex /* , UInt32 &coderStreamIndex */ ) {
        for (coderIndex[0] = 0; coderIndex[0] < Coders.size(); coderIndex[0]++) {
            int curSize = Coders.get(coderIndex[0]).NumOutStreams;
            if (streamIndex < curSize) {
                coderStreamIndex[0] = streamIndex;
                return;
            }
            streamIndex -= curSize;
        }
        throw new UnknownError("1");
    }
    
}

