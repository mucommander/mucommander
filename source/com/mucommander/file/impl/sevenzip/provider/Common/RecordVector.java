package com.mucommander.file.impl.sevenzip.provider.Common;

public class RecordVector extends java.util.Vector
{
    public RecordVector() {
        super();
    }
    
    public void Reserve(int s) {
        ensureCapacity(s);
    }
    
    public Object Back() {
        return get(elementCount-1);
    }
    
    public Object Front() {
        return get(0);
    }
    
    public void DeleteBack() {
        remove(elementCount-1);
    }
}
