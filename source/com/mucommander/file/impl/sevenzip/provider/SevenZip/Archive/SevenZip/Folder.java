package com.mucommander.file.impl.sevenzip.provider.SevenZip.Archive.SevenZip;

import java.io.IOException;

import com.mucommander.file.impl.sevenzip.provider.Common.IntVector;
import com.mucommander.file.impl.sevenzip.provider.Common.LongVector;
import com.mucommander.file.impl.sevenzip.provider.Common.RecordVector;
import com.mucommander.file.impl.sevenzip.provider.SevenZip.Archive.Common.BindPair;



class Folder {
    public RecordVector Coders = new RecordVector();
    RecordVector BindPairs = new RecordVector();
    IntVector PackStreams = new IntVector();
    LongVector UnPackSizes = new LongVector();
    int UnPackCRC;
    boolean UnPackCRCDefined;
    Folder() {
        UnPackCRCDefined = false;
    }

    long GetUnPackSize() throws IOException {
        if (UnPackSizes.isEmpty())
            return 0;
        for (int i = UnPackSizes.size() - 1; i >= 0; i--)
            if (FindBindPairForOutStream(i) < 0)
                return UnPackSizes.get(i);
        throw new IOException("1"); // throw 1  // TBD
    }
    
    int FindBindPairForInStream(int inStreamIndex) {
        for(int i = 0; i < BindPairs.size(); i++)
            if (((BindPair) BindPairs.get(i)).InIndex == inStreamIndex)
                return i;
        return -1;
    }
    
    int FindBindPairForOutStream(int outStreamIndex) {
        for(int i = 0; i < BindPairs.size(); i++)
            if (((BindPair) BindPairs.get(i)).OutIndex == outStreamIndex)
                return i;
        return -1;
    }
    
     int FindPackStreamArrayIndex(int inStreamIndex) {
        for(int i = 0; i < PackStreams.size(); i++)
            if (PackStreams.get(i) == inStreamIndex)
                return i;
        return -1;
    }
      
    int GetNumOutStreams() {
        int result = 0;
        for (int i = 0; i < Coders.size(); i++)
            result += ((CoderInfo) Coders.get(i)).NumOutStreams;
        return result;
    }
    
}