package com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.Archive.Common;


import java.io.IOException;

import com.mucommander.commons.file.impl.sevenzip.provider.Common.LongVector;
import com.mucommander.commons.file.impl.sevenzip.provider.Common.ObjectVector;
import com.mucommander.commons.file.impl.sevenzip.provider.Common.RecordVector;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.HRESULT;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.ICompressCoder;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.ICompressCoder2;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.ICompressProgressInfo;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.ICompressSetInStream;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.ICompressSetOutStream;
import com.mucommander.commons.file.impl.sevenzip.provider.SevenZip.ICompressSetOutStreamSize;



public class CoderMixer2ST implements ICompressCoder2 , CoderMixer2 {
    
    BindInfo _bindInfo = new BindInfo();
    ObjectVector<STCoderInfo> _coders = new ObjectVector<STCoderInfo>();
    int _mainCoderIndex;
    
    public CoderMixer2ST() {
    }
    
    
    public void SetBindInfo(BindInfo bindInfo) {
        _bindInfo = bindInfo;
    }
    
    public void AddCoderCommon(boolean isMain) {
        CoderStreamsInfo csi = _bindInfo.Coders.get(_coders.size());
        _coders.add(new STCoderInfo(csi.NumInStreams, csi.NumOutStreams, isMain));
    }
    
    public void AddCoder2(ICompressCoder2 coder, boolean isMain) {
        AddCoderCommon(isMain);
        _coders.Back().Coder2 = coder;
    }
    
    public void AddCoder(ICompressCoder coder, boolean isMain) {
        AddCoderCommon(isMain);
        _coders.Back().Coder = coder;
    }
    
    public void ReInit() {
    }
    
    public void SetCoderInfo(int coderIndex,LongVector inSizes, LongVector outSizes) {
        // _coders[coderIndex].SetCoderInfo(inSizes, outSizes);
        _coders.get(coderIndex).SetCoderInfo(inSizes, outSizes);
    }
    
    public int GetInStream(
            RecordVector<java.io.InputStream> inStreams,
            Object useless_inSizes, // const UInt64 **inSizes,
            int streamIndex,
            java.io.InputStream [] inStreamRes) {
        java.io.InputStream seqInStream;
        int i;
        for(i = 0; i < _bindInfo.InStreams.size(); i++)
            if (_bindInfo.InStreams.get(i) == streamIndex) {
            seqInStream = inStreams.get(i);
            inStreamRes[0] = seqInStream; // seqInStream.Detach();
            return  HRESULT.S_OK;
            }
        int binderIndex = _bindInfo.FindBinderForInStream(streamIndex);
        if (binderIndex < 0)
            return HRESULT.E_INVALIDARG;
        
        
        int tmp1 [] = new int[1]; // TBD
        int tmp2 [] = new int[1]; // TBD
        _bindInfo.FindOutStream(_bindInfo.BindPairs.get(binderIndex).OutIndex,
                tmp1 /* coderIndex */ , tmp2 /* coderStreamIndex */ );
        int coderIndex = tmp1[0], coderStreamIndex = tmp2[0];
        
        CoderInfo coder = _coders.get(coderIndex);
        if (coder.Coder == null)
            return HRESULT.E_NOTIMPL;
        
        seqInStream = (java.io.InputStream)coder.Coder; // coder.Coder.QueryInterface(IID_ISequentialInStream, &seqInStream);
        if (seqInStream == null)
            return HRESULT.E_NOTIMPL;
        
        int startIndex = _bindInfo.GetCoderInStreamIndex(coderIndex);
        
        if (coder.Coder == null)
            return HRESULT.E_NOTIMPL;
        
        ICompressSetInStream setInStream = (ICompressSetInStream)coder.Coder; //  coder.Coder.QueryInterface(IID_ICompressSetInStream, &setInStream);
        if (setInStream == null)
            return HRESULT.E_NOTIMPL;
        
        if (coder.NumInStreams > 1)
            return HRESULT.E_NOTIMPL;
        for (i = 0; i < (int)coder.NumInStreams; i++) {
            java.io.InputStream [] tmp = new java.io.InputStream[1];
            int res = GetInStream(inStreams, useless_inSizes, startIndex + i, tmp /* &seqInStream2 */ );
            if (res != HRESULT.S_OK) return res;
            java.io.InputStream seqInStream2 = tmp[0];
            res = setInStream.SetInStream(seqInStream2);
            if (res != HRESULT.S_OK) return res;
        }
        inStreamRes[0] = seqInStream; // seqInStream.Detach();
        return HRESULT.S_OK;
    }
    
    public int GetOutStream(
            RecordVector<java.io.OutputStream> outStreams,
            Object useless_outSizes, //  const UInt64 **outSizes,
            int streamIndex,
            java.io.OutputStream [] outStreamRes) {
        java.io.OutputStream seqOutStream;
        int i;
        for(i = 0; i < _bindInfo.OutStreams.size(); i++)
            if (_bindInfo.OutStreams.get(i) == streamIndex) {
            seqOutStream = outStreams.get(i);
            outStreamRes[0] = seqOutStream; // seqOutStream.Detach();
            return  HRESULT.S_OK;
            }
        int binderIndex = _bindInfo.FindBinderForOutStream(streamIndex);
        if (binderIndex < 0)
            return HRESULT.E_INVALIDARG;
        
        int tmp1[] = new int[1];
        int tmp2[] = new int[1];
        _bindInfo.FindInStream(_bindInfo.BindPairs.get(binderIndex).InIndex,
                tmp1 /* coderIndex*/ , tmp2 /* coderStreamIndex */ );
        int coderIndex = tmp1[0], coderStreamIndex = tmp2[0];
        
        CoderInfo coder = _coders.get(coderIndex);
        if (coder.Coder == null)
            return HRESULT.E_NOTIMPL;
        
        try
        {
            seqOutStream = (java.io.OutputStream)coder.Coder; // coder.Coder.QueryInterface(IID_ISequentialOutStream, &seqOutStream);
        } catch (java.lang.ClassCastException e) {
            return HRESULT.E_NOTIMPL;
        }
        
        int startIndex = _bindInfo.GetCoderOutStreamIndex(coderIndex);
        
        if (coder.Coder == null)
            return HRESULT.E_NOTIMPL;
        
        ICompressSetOutStream setOutStream = null;
        try {
            setOutStream = (ICompressSetOutStream)coder.Coder; // coder.Coder.QueryInterface(IID_ICompressSetOutStream, &setOutStream);
        } catch (java.lang.ClassCastException e) {     
            return HRESULT.E_NOTIMPL;
        }
        
        if (coder.NumOutStreams > 1)
            return HRESULT.E_NOTIMPL;
        for (i = 0; i < (int)coder.NumOutStreams; i++) {
            java.io.OutputStream [] tmp = new java.io.OutputStream[1];
            int res = GetOutStream(outStreams, useless_outSizes, startIndex + i, tmp /* &seqOutStream2 */ );
            if (res != HRESULT.S_OK) return res;
            java.io.OutputStream seqOutStream2 = tmp[0];
            res = setOutStream.SetOutStream(seqOutStream2);
            if (res != HRESULT.S_OK) return res;
        }
        outStreamRes[0] = seqOutStream; // seqOutStream.Detach();
        return HRESULT.S_OK;
    }
    
    public int Code(
            RecordVector<java.io.InputStream>  inStreams,
            Object useless_inSizes, // const UInt64 ** inSizes ,
            int numInStreams,
            RecordVector<java.io.OutputStream> outStreams,
            Object useless_outSizes, // const UInt64 ** /* outSizes */,
            int numOutStreams,
            ICompressProgressInfo progress) throws IOException {
        if (numInStreams != _bindInfo.InStreams.size() ||
                numOutStreams != _bindInfo.OutStreams.size())
            return HRESULT.E_INVALIDARG;
        
        
        // Find main coder
        int _mainCoderIndex = -1;
        int i;
        for (i = 0; i < _coders.size(); i++)
            if (_coders.get(i).IsMain) {
            _mainCoderIndex = i;
            break;
            }
        if (_mainCoderIndex < 0)
            for (i = 0; i < _coders.size(); i++)
                if (_coders.get(i).NumInStreams > 1) {
            if (_mainCoderIndex >= 0)
                return HRESULT.E_NOTIMPL;
            _mainCoderIndex = i;
                }
        if (_mainCoderIndex < 0)
            _mainCoderIndex = 0;
        
        // _mainCoderIndex = 0;
        // _mainCoderIndex = _coders.Size() - 1;
        CoderInfo mainCoder = _coders.get(_mainCoderIndex);
        
        ObjectVector< java.io.InputStream > seqInStreams = new ObjectVector< java.io.InputStream >(); // CObjectVector< CMyComPtr<ISequentialInStream> >
        ObjectVector< java.io.OutputStream > seqOutStreams = new ObjectVector< java.io.OutputStream >(); // CObjectVector< CMyComPtr<ISequentialOutStream> >
        int startInIndex = _bindInfo.GetCoderInStreamIndex(_mainCoderIndex);
        int startOutIndex = _bindInfo.GetCoderOutStreamIndex(_mainCoderIndex);
        for (i = 0; i < (int)mainCoder.NumInStreams; i++) {
            java.io.InputStream tmp [] = new  java.io.InputStream[1];
            int res = GetInStream(inStreams, useless_inSizes, startInIndex + i, tmp /* &seqInStream */ );
            if (res != HRESULT.S_OK) return res;
            java.io.InputStream seqInStream = tmp[0];
            seqInStreams.add(seqInStream);
        }
        for (i = 0; i < (int)mainCoder.NumOutStreams; i++) {
            java.io.OutputStream tmp [] = new  java.io.OutputStream[1];
            int res = GetOutStream(outStreams, useless_outSizes, startOutIndex + i, tmp);
            if (res != HRESULT.S_OK) return res;
            java.io.OutputStream seqOutStream = tmp[0];
            seqOutStreams.add(seqOutStream);
        }
        RecordVector< java.io.InputStream > seqInStreamsSpec = new RecordVector< java.io.InputStream >();
        RecordVector< java.io.OutputStream > seqOutStreamsSpec = new RecordVector< java.io.OutputStream >();
        for (i = 0; i < (int)mainCoder.NumInStreams; i++)
            seqInStreamsSpec.add(seqInStreams.get(i));
        for (i = 0; i < (int)mainCoder.NumOutStreams; i++)
            seqOutStreamsSpec.add(seqOutStreams.get(i));
        
        for (i = 0; i < _coders.size(); i++) {
            if (i == _mainCoderIndex)
                continue;
            CoderInfo coder = _coders.get(i);
            
            ICompressSetOutStreamSize setOutStreamSize = null;
            try
            {
                setOutStreamSize = (ICompressSetOutStreamSize)coder.Coder;
                
                int res = setOutStreamSize.SetOutStreamSize(coder.OutSizePointers.get(0));
                if (res != HRESULT.S_OK) return res;
            } catch (java.lang.ClassCastException e) {
                // nothing to do
            }
        }
        if (mainCoder.Coder != null) {
            int res = mainCoder.Coder.Code(
                    seqInStreamsSpec.get(0),
                    seqOutStreamsSpec.get(0),
                    // TBD mainCoder.InSizePointers.get(0),
                    mainCoder.OutSizePointers.get(0),
                    progress);
            if (res != HRESULT.S_OK) return res;
        } else {
            int res = mainCoder.Coder2.Code(
                    seqInStreamsSpec, // &seqInStreamsSpec.Front(
                    mainCoder.InSizePointers.Front(), // &mainCoder.InSizePointers.Front()
                    mainCoder.NumInStreams,
                    seqOutStreamsSpec, // &seqOutStreamsSpec.Front()
                    mainCoder.OutSizePointers.Front(), // &mainCoder.OutSizePointers.Front()
                    mainCoder.NumOutStreams,
                    progress);
            if (res != HRESULT.S_OK) return res;
        }

        
        java.io.OutputStream stream = seqOutStreams.Front();
        stream.flush();
            
        return HRESULT.S_OK;
    }
    
    public void close() {
        
    }
}

