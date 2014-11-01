package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.LZMA;


import java.io.IOException;

import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.HRESULT;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.ICompressProgressInfo;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.LZ.OutWindow;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.LZMA.Base;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.BitTreeDecoder;


/*
  public ICompressCoder,
  public ICompressSetDecoderProperties2,
  public ICompressGetInStreamProcessedSize,
  #ifdef _ST_MODE
  public ICompressSetInStream,
  public ICompressSetOutStreamSize,
  public ISequentialInStream,
  #endif
 */
// OLD CODE public class Decoder implements SevenZip.ICompressCoder , SevenZip.ICompressSetDecoderProperties2
public class Decoder
        extends java.io.InputStream // _ST_MODE
        implements com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.ICompressCoder , com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.ICompressSetDecoderProperties2 ,
        com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.ICompressGetInStreamProcessedSize,
        // #ifdef _ST_MODE
        com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.ICompressSetInStream,
        com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.ICompressSetOutStreamSize
        // #endif
        
{
    class LenDecoder {
        short[] m_Choice = new short[2];
        BitTreeDecoder[] m_LowCoder = new BitTreeDecoder[Base.kNumPosStatesMax];
        BitTreeDecoder[] m_MidCoder = new BitTreeDecoder[Base.kNumPosStatesMax];
        BitTreeDecoder m_HighCoder = new BitTreeDecoder(Base.kNumHighLenBits);
        int m_NumPosStates = 0;
        
        public void Create(int numPosStates) {
            for (; m_NumPosStates < numPosStates; m_NumPosStates++) {
                m_LowCoder[m_NumPosStates] = new BitTreeDecoder(Base.kNumLowLenBits);
                m_MidCoder[m_NumPosStates] = new BitTreeDecoder(Base.kNumMidLenBits);
            }
        }
        
        public void Init() {
            com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.Decoder.InitBitModels(m_Choice);
            for (int posState = 0; posState < m_NumPosStates; posState++) {
                m_LowCoder[posState].Init();
                m_MidCoder[posState].Init();
            }
            m_HighCoder.Init();
        }
        
        public int Decode(com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.Decoder rangeDecoder, int posState) throws IOException {
            if (rangeDecoder.DecodeBit(m_Choice, 0) == 0)
                return m_LowCoder[posState].Decode(rangeDecoder);
            int symbol = Base.kNumLowLenSymbols;
            if (rangeDecoder.DecodeBit(m_Choice, 1) == 0)
                symbol += m_MidCoder[posState].Decode(rangeDecoder);
            else
                symbol += Base.kNumMidLenSymbols + m_HighCoder.Decode(rangeDecoder);
            return symbol;
        }
    }
    
    class LiteralDecoder {
        class Decoder2 {
            short[] m_Decoders = new short[0x300];
            
            public void Init() {
                com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.Decoder.InitBitModels(m_Decoders);
            }
            
            public byte DecodeNormal(com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.Decoder rangeDecoder) throws IOException {
                int symbol = 1;
                do
                    symbol = (symbol << 1) | rangeDecoder.DecodeBit(m_Decoders, symbol);
                while (symbol < 0x100);
                return (byte)symbol;
            }
            
            public byte DecodeWithMatchByte(com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.Decoder rangeDecoder, byte matchByte) throws IOException {
                int symbol = 1;
                do
                {
                    int matchBit = (matchByte >> 7) & 1;
                    matchByte <<= 1;
                    int bit = rangeDecoder.DecodeBit(m_Decoders, ((1 + matchBit) << 8) + symbol);
                    symbol = (symbol << 1) | bit;
                    if (matchBit != bit) {
                        while (symbol < 0x100)
                            symbol = (symbol << 1) | rangeDecoder.DecodeBit(m_Decoders, symbol);
                        break;
                    }
                }
                while (symbol < 0x100);
                return (byte)symbol;
            }
        }
        
        Decoder2[] m_Coders;
        int m_NumPrevBits;
        int m_NumPosBits;
        int m_PosMask;
        
        public void Create(int numPosBits, int numPrevBits) {
            if (m_Coders != null && m_NumPrevBits == numPrevBits && m_NumPosBits == numPosBits)
                return;
            m_NumPosBits = numPosBits;
            m_PosMask = (1 << numPosBits) - 1;
            m_NumPrevBits = numPrevBits;
            int numStates = 1 << (m_NumPrevBits + m_NumPosBits);
            m_Coders = new Decoder2[numStates];
            for (int i = 0; i < numStates; i++)
                m_Coders[i] = new Decoder2();
        }
        
        public void Init() {
            int numStates = 1 << (m_NumPrevBits + m_NumPosBits);
            for (int i = 0; i < numStates; i++)
                m_Coders[i].Init();
        }
        
        Decoder2 GetDecoder(int pos, byte prevByte) {
            return m_Coders[((pos & m_PosMask) << m_NumPrevBits) + ((prevByte & 0xFF) >>> (8 - m_NumPrevBits))];
        }
    }
    
    OutWindow m_OutWindow = new OutWindow();
    com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.Decoder m_RangeDecoder = new com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.Decoder();
    
    short[] m_IsMatchDecoders = new short[Base.kNumStates << Base.kNumPosStatesBitsMax];
    short[] m_IsRepDecoders = new short[Base.kNumStates];
    short[] m_IsRepG0Decoders = new short[Base.kNumStates];
    short[] m_IsRepG1Decoders = new short[Base.kNumStates];
    short[] m_IsRepG2Decoders = new short[Base.kNumStates];
    short[] m_IsRep0LongDecoders = new short[Base.kNumStates << Base.kNumPosStatesBitsMax];
    
    BitTreeDecoder[] m_PosSlotDecoder = new BitTreeDecoder[Base.kNumLenToPosStates];
    short[] m_PosDecoders = new short[Base.kNumFullDistances - Base.kEndPosModelIndex];
    
    BitTreeDecoder m_PosAlignDecoder = new BitTreeDecoder(Base.kNumAlignBits);
    
    LenDecoder m_LenDecoder = new LenDecoder();
    LenDecoder m_RepLenDecoder = new LenDecoder();
    
    LiteralDecoder m_LiteralDecoder = new LiteralDecoder();
    
    int m_DictionarySize = -1;
    int m_DictionarySizeCheck =  -1;
    
    int m_posStateMask;
    
    public Decoder() {
        for (int i = 0; i < Base.kNumLenToPosStates; i++)
            m_PosSlotDecoder[i] = new BitTreeDecoder(Base.kNumPosSlotBits);
    }
    
    boolean SetDictionarySize(int dictionarySize) {
        if (dictionarySize < 0)
            return false;
        if (m_DictionarySize != dictionarySize) {
            m_DictionarySize = dictionarySize;
            m_DictionarySizeCheck = Math.max(m_DictionarySize, 1);
            m_OutWindow.Create(Math.max(m_DictionarySizeCheck, (1 << 12)));
            m_RangeDecoder.Create(1 << 20);
        }
        return true;
    }
    
    boolean SetLcLpPb(int lc, int lp, int pb) {
        if (lc > Base.kNumLitContextBitsMax || lp > 4 || pb > Base.kNumPosStatesBitsMax)
            return false;
        m_LiteralDecoder.Create(lp, lc);
        int numPosStates = 1 << pb;
        m_LenDecoder.Create(numPosStates);
        m_RepLenDecoder.Create(numPosStates);
        m_posStateMask = numPosStates - 1;
        return true;
    }
    
    
    public long GetInStreamProcessedSize() {
        throw new UnknownError("GetInStreamProcessedSize");
        // return m_RangeDecoder.GetProcessedSize();
    }
    
    public int ReleaseInStream() throws IOException {
        m_RangeDecoder.ReleaseStream();
        return com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.HRESULT.S_OK;
    }
    
    public int SetInStream(java.io.InputStream inStream) { // Common.ISequentialInStream
        m_RangeDecoder.SetStream(inStream);
        return com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.HRESULT.S_OK;
    }
    
    long _outSize = 0;
    boolean _outSizeDefined = false;
    int _remainLen; // -1 means end of stream. // -2 means need Init
    static final int kLenIdFinished = -1;
    static final int kLenIdNeedInit = -2;
    int _rep0;
    int _rep1;
    int _rep2;
    int _rep3;
    int _state;
    
    public int SetOutStreamSize(long outSize /* const UInt64 *outSize*/ ) {
        _outSizeDefined = (outSize != com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.ICompressSetOutStreamSize.INVALID_OUTSIZE);
        if (_outSizeDefined)
            _outSize = outSize;
        _remainLen = kLenIdNeedInit;
        m_OutWindow.Init();
        return com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.HRESULT.S_OK;
    }
    
    // #ifdef _ST_MODE
    public int read() throws java.io.IOException {
        throw new java.io.IOException("LZMA Decoder - read() not implemented");
    }
    
    public int read(byte [] data, int off, int size) throws IOException  {
        if (off  != 0)throw new java.io.IOException("LZMA Decoder - read(byte [] data, int off != 0, int size)) not implemented");
        
        long startPos = m_OutWindow.GetProcessedSize();
        m_OutWindow.SetMemStream(data);
        int res = CodeSpec(size);
        if (res != HRESULT.S_OK) throw new IOException("Read - CodeSpec = " + res);
        
        res = Flush();
        if (res != HRESULT.S_OK) throw new IOException("Read - Flush = " + res);
        int ret = (int)(m_OutWindow.GetProcessedSize() - startPos);
        if (ret == 0) ret = -1;
        return ret;
    }
    
    // #endif // _ST_MODE
    
    void Init() throws IOException {
        m_OutWindow.Init(false);
        
        com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.Decoder.InitBitModels(m_IsMatchDecoders);
        com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.Decoder.InitBitModels(m_IsRep0LongDecoders);
        com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.Decoder.InitBitModels(m_IsRepDecoders);
        com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.Decoder.InitBitModels(m_IsRepG0Decoders);
        com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.Decoder.InitBitModels(m_IsRepG1Decoders);
        com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.Decoder.InitBitModels(m_IsRepG2Decoders);
        com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Compression.RangeCoder.Decoder.InitBitModels(m_PosDecoders);
        
        _rep0 = _rep1 = _rep2 = _rep3 = 0;
        _state = Base.StateInit();
        
        m_LiteralDecoder.Init();
        int i;
        for (i = 0; i < Base.kNumLenToPosStates; i++)
            m_PosSlotDecoder[i].Init();
        m_LenDecoder.Init();
        m_RepLenDecoder.Init();
        m_PosAlignDecoder.Init();
    }
    
    public int Flush() throws IOException {
        m_OutWindow.Flush();
        return HRESULT.S_OK;
    }
    
    void ReleaseStreams() throws IOException  {
        m_OutWindow.ReleaseStream();
        ReleaseInStream();
    }
    
    public int CodeReal(
            java.io.InputStream inStream, // , ISequentialInStream
            java.io.OutputStream outStream, // ISequentialOutStream
            long outSize,
            ICompressProgressInfo progress // useless_progress
            ) throws IOException {
        
        SetInStream(inStream);
        m_OutWindow.SetStream(outStream);
        SetOutStreamSize(outSize);
        
        for (;;) {
            int curSize = 1 << 18;
            int res = CodeSpec(curSize);
            if (res != HRESULT.S_OK) {
                return res;
            }
            if (_remainLen == kLenIdFinished)
                break;
            
            if (progress != null) {
                long inSize = m_RangeDecoder.GetProcessedSize();
                long nowPos64 = m_OutWindow.GetProcessedSize();
                res = progress.SetRatioInfo(inSize, nowPos64);
                if (res != HRESULT.S_OK) {
                    return res;
                }
            }
            
            if (_outSizeDefined)
                if (m_OutWindow.GetProcessedSize() >= _outSize)
                    break;
        }
        return Flush();
    }
    
    public int Code(
            java.io.InputStream inStream, // , ISequentialInStream
            java.io.OutputStream outStream, // ISequentialOutStream
            long outSize,
            ICompressProgressInfo progress // useless_progress
            ) throws IOException {
        
        int ret = HRESULT.S_FALSE;
        try {
            ret = CodeReal(inStream,outStream,outSize,progress);
        } catch (IOException e) {
            e.printStackTrace(); // TBD
            this.Flush();
            this.ReleaseStreams();
            throw e;
        } finally {
            this.Flush();
            this.ReleaseStreams();
        }
        return ret;
    }
    
    int CodeSpec(int curSize)  throws IOException // UInt32
    {
        if (_outSizeDefined) {
            long rem = _outSize - m_OutWindow.GetProcessedSize();
            if (curSize > rem)
                curSize = (int)rem;
        }
        
        if (_remainLen == kLenIdFinished)
            return HRESULT.S_OK;
        if (_remainLen == kLenIdNeedInit) {
            m_RangeDecoder.Init();
            Init();
            _remainLen = 0;
        }
        if (curSize == 0)
            return HRESULT.S_OK;
        
        int rep0 = _rep0;
        int rep1 = _rep1;
        int rep2 = _rep2;
        int rep3 = _rep3;
        int state = _state;
        byte prevByte;
        
        while(_remainLen > 0 && curSize > 0) {
            prevByte = m_OutWindow.GetByte(rep0);
            m_OutWindow.PutByte(prevByte);
            _remainLen--;
            curSize--;
        }
        long nowPos64 = m_OutWindow.GetProcessedSize();
        if (nowPos64 == 0)
            prevByte = 0;
        else
            prevByte = m_OutWindow.GetByte(0);
        
        while(curSize > 0) {
            
            if (m_RangeDecoder.bufferedStream.WasFinished())
                return HRESULT.S_FALSE;
            int posState = (int)nowPos64 & m_posStateMask;
            
            if (m_RangeDecoder.DecodeBit(m_IsMatchDecoders, (state << Base.kNumPosStatesBitsMax) + posState) == 0) {
                LiteralDecoder.Decoder2 decoder2 = m_LiteralDecoder.GetDecoder((int)nowPos64, prevByte);
                if (!Base.StateIsCharState(state))
                    prevByte = decoder2.DecodeWithMatchByte(m_RangeDecoder, m_OutWindow.GetByte(rep0));
                else
                    prevByte = decoder2.DecodeNormal(m_RangeDecoder);
                m_OutWindow.PutByte(prevByte);
                state = Base.StateUpdateChar(state);
                curSize--;
                nowPos64++;
            } else {
                int len;
                if (m_RangeDecoder.DecodeBit(m_IsRepDecoders, state) == 1) {
                    len = 0;
                    if (m_RangeDecoder.DecodeBit(m_IsRepG0Decoders, state) == 0) {
                        if (m_RangeDecoder.DecodeBit(m_IsRep0LongDecoders, (state << Base.kNumPosStatesBitsMax) + posState) == 0) {
                            state = Base.StateUpdateShortRep(state);
                            len = 1;
                        }
                    } else {
                        int distance;
                        if (m_RangeDecoder.DecodeBit(m_IsRepG1Decoders, state) == 0)
                            distance = rep1;
                        else {
                            if (m_RangeDecoder.DecodeBit(m_IsRepG2Decoders, state) == 0)
                                distance = rep2;
                            else {
                                distance = rep3;
                                rep3 = rep2;
                            }
                            rep2 = rep1;
                        }
                        rep1 = rep0;
                        rep0 = distance;
                    }
                    if (len == 0) {
                        len = m_RepLenDecoder.Decode(m_RangeDecoder, posState) + Base.kMatchMinLen;
                        state = Base.StateUpdateRep(state);
                    }
                } else {
                    rep3 = rep2;
                    rep2 = rep1;
                    rep1 = rep0;
                    len = Base.kMatchMinLen + m_LenDecoder.Decode(m_RangeDecoder, posState);
                    state = Base.StateUpdateMatch(state);
                    int posSlot = m_PosSlotDecoder[Base.GetLenToPosState(len)].Decode(m_RangeDecoder);
                    if (posSlot >= Base.kStartPosModelIndex) {
                        int numDirectBits = (posSlot >> 1) - 1;
                        rep0 = ((2 | (posSlot & 1)) << numDirectBits);
                        if (posSlot < Base.kEndPosModelIndex)
                            rep0 += BitTreeDecoder.ReverseDecode(m_PosDecoders,
                                    rep0 - posSlot - 1, m_RangeDecoder, numDirectBits);
                        else {
                            rep0 += (m_RangeDecoder.DecodeDirectBits(
                                    numDirectBits - Base.kNumAlignBits) << Base.kNumAlignBits);
                            rep0 += m_PosAlignDecoder.ReverseDecode(m_RangeDecoder);
                            if (rep0 < 0) {
                                if (rep0 == -1)
                                    break;
                                return HRESULT.S_FALSE;
                            }
                        }
                    } else
                        rep0 = posSlot;
                }
                if (rep0 >= nowPos64 || rep0 >= m_DictionarySizeCheck) {
                    // m_OutWindow.Flush();
                    _remainLen = kLenIdFinished;
                    return HRESULT.S_FALSE;
                }
                
                
                int locLen = len;
                if (len > curSize)
                    locLen = curSize;
                // if (!m_OutWindow.CopyBlock(rep0, locLen))
                //    return HRESULT.S_FALSE;
                m_OutWindow.CopyBlock(rep0, locLen);
                prevByte = m_OutWindow.GetByte(0);
                curSize -= locLen;
                nowPos64 += locLen;
                len -= locLen;
                if (len != 0) {
                    _remainLen = len;
                    break;
                }
            }
        }
        if (m_RangeDecoder.bufferedStream.WasFinished())
            return HRESULT.S_FALSE;
        
        _rep0 = rep0;
        _rep1 = rep1;
        _rep2 = rep2;
        _rep3 = rep3;
        _state = state;
        
        return HRESULT.S_OK;
    }
    
    public boolean SetDecoderProperties2(byte[] properties) {
        if (properties.length < 5)
            return false;
        int val = properties[0] & 0xFF;
        int lc = val % 9;
        int remainder = val / 9;
        int lp = remainder % 5;
        int pb = remainder / 5;
        int dictionarySize = 0;
        for (int i = 0; i < 4; i++)
            dictionarySize += ((int)(properties[1 + i]) & 0xFF) << (i * 8);
        if (!SetLcLpPb(lc, lp, pb))
            return false;
        return SetDictionarySize(dictionarySize);
    }
}
