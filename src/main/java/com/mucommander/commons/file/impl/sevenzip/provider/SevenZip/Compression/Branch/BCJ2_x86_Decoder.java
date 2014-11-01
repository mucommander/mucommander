
package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.Branch;

import java.io.IOException;
import java.io.PrintStream;

import com.mucommander.commons.file.impl.sevenzip.provider.Common.RecordVector;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.HRESULT;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.ICompressCoder2;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.ICompressProgressInfo;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Common.InBuffer;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.LZ.OutWindow;


public class BCJ2_x86_Decoder implements ICompressCoder2 {

    public static final int kNumMoveBits = 5;
    
    InBuffer _mainInStream = new InBuffer();
    InBuffer _callStream = new InBuffer();
    InBuffer _jumpStream = new InBuffer();
    
    com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.BitDecoder _statusE8Decoder[] = new com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.BitDecoder[256];
    com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.BitDecoder _statusE9Decoder = new com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.BitDecoder(kNumMoveBits);
    com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.BitDecoder _statusJccDecoder = new com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.BitDecoder(kNumMoveBits);
    
    OutWindow _outStream = new OutWindow();
    com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.Decoder _rangeDecoder = new com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.Decoder();
    
    
    // static final boolean IsJcc(int b0, int b1) {
    //     return ((b0 == 0x0F) && ((b1 & 0xF0) == 0x80));
    // }
    
    int CodeReal(
            RecordVector<java.io.InputStream>  inStreams,
            Object useless1, // const UInt64 ** /* inSizes */,
            int numInStreams,
            RecordVector<java.io.OutputStream> outStreams,
            Object useless2, // const UInt64 ** /* outSizes */,
            int numOutStreams,
            ICompressProgressInfo progress) throws java.io.IOException {
        
        if (numInStreams != 4 || numOutStreams != 1)
            return HRESULT.E_INVALIDARG;
        
        _mainInStream.Create(1 << 16);
        _callStream.Create(1 << 20);
        _jumpStream.Create(1 << 16);
        _rangeDecoder.Create(1 << 20);
        _outStream.Create(1 << 16);
        
        _mainInStream.SetStream(inStreams.get(0));
        _callStream.SetStream(inStreams.get(1));
        _jumpStream.SetStream(inStreams.get(2));
        _rangeDecoder.SetStream(inStreams.get(3));
        _outStream.SetStream(outStreams.get(0));
        
        _mainInStream.Init();
        _callStream.Init();
        _jumpStream.Init();
        _rangeDecoder.Init();
        _outStream.Init();
        
        for (int i = 0; i < 256; i++) {
            _statusE8Decoder[i] = new com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.BitDecoder(kNumMoveBits);
            _statusE8Decoder[i].Init();
        }
        _statusE9Decoder.Init();
        _statusJccDecoder.Init();
        
        int prevByte = 0;
        int processedBytes = 0;
        for (;;) {
            
            if (processedBytes > (1 << 20) && progress != null) {
                long nowPos64 = _outStream.GetProcessedSize();
                int res = progress.SetRatioInfo(ICompressProgressInfo.INVALID, nowPos64);
                if (res != HRESULT.S_OK) return res;
                
                processedBytes = 0;
            }
            
            processedBytes++;
            int b = _mainInStream.read();
            if (b == -1)
                return Flush();
            _outStream.WriteByte(b); // System.out.println("0:"+b);
            // if ((b != 0xE8) && (b != 0xE9) && (!IsJcc(prevByte, b))) {
            if ((b != 0xE8) && (b != 0xE9) && (!((prevByte == 0x0F) && ((b & 0xF0) == 0x80)))) {
                prevByte = b;
                continue;
            }
            
            boolean status;
            if (b == 0xE8)
                status = (_statusE8Decoder[prevByte].Decode(_rangeDecoder) == 1);
            else if (b == 0xE9)
                status = (_statusE9Decoder.Decode(_rangeDecoder) == 1);
            else
                status = (_statusJccDecoder.Decode(_rangeDecoder) == 1);
            
            if (status) {
                int src;
                if (b == 0xE8) {
                    int b0 = _callStream.read();
                    // if(b0 == -1) return HRESULT.S_FALSE;
                    src = ((int)b0) << 24;
                    
                    b0 = _callStream.read();
                    // if(b0 == -1) return HRESULT.S_FALSE;
                    src |= ((int)b0) << 16;
                    
                    b0 = _callStream.read();
                    // if(b0 == -1) return HRESULT.S_FALSE;
                    src |= ((int)b0) << 8;
                    
                    b0 = _callStream.read();
                    if(b0 == -1) return HRESULT.S_FALSE;
                    src |= ((int)b0);
                    
                } else {
                    int b0 = _jumpStream.read();
                    // if(b0 == -1) return HRESULT.S_FALSE;
                    src = ((int)b0) << 24;
                    
                    b0 = _jumpStream.read();
                    // if(b0 == -1) return HRESULT.S_FALSE;
                    src |= ((int)b0) << 16;
                    
                    b0 = _jumpStream.read();
                    // if(b0 == -1) return HRESULT.S_FALSE;
                    src |= ((int)b0) << 8;
                    
                    b0 = _jumpStream.read();
                    if(b0 == -1) return HRESULT.S_FALSE;
                    src |= ((int)b0);
                    
                }
                int dest = src - ((int)_outStream.GetProcessedSize() + 4) ;
                _outStream.WriteByte(dest);
                _outStream.WriteByte((dest >> 8));
                _outStream.WriteByte((dest >> 16));
                _outStream.WriteByte((dest >> 24));
                prevByte = (int)(dest >> 24) & 0xFF;
                processedBytes += 4;
            } else
                prevByte = b;
        }
    }
    
    public int Flush() throws java.io.IOException {
        _outStream.Flush();
        return HRESULT.S_OK;
    }
    
    public int Code(
            RecordVector<java.io.InputStream>  inStreams, // ISequentialInStream **inStreams,
            Object useless_inSizes, // const UInt64 ** /* inSizes */,
            int numInStreams,
            RecordVector<java.io.OutputStream> outStreams, // ISequentialOutStream **outStreams
            Object useless_outSizes, // const UInt64 ** /* outSizes */,
            int numOutStreams,
            ICompressProgressInfo progress) throws java.io.IOException {
        
        try {
            return CodeReal(inStreams, useless_inSizes, numInStreams,
                    outStreams, useless_outSizes,numOutStreams, progress);
        } catch(java.io.IOException e) {
            throw e;
        } finally {
            ReleaseStreams();
        }
    }
    
    void ReleaseStreams() throws java.io.IOException {
        _mainInStream.ReleaseStream();
        _callStream.ReleaseStream();
        _jumpStream.ReleaseStream();
        _rangeDecoder.ReleaseStream();
        _outStream.ReleaseStream();
    }
    
    public void close() throws java.io.IOException {
        ReleaseStreams();       
    }
    
}
