package com.mucommander.commons.file.impl.sevenzip.provider.Common;

public class RecordVector<E> extends java.util.Vector<E>
{
    public RecordVector() {
        super();
    }
    
    public void Reserve(int s) {
        ensureCapacity(s);
    }
    
    public E Back() {
        return get(elementCount-1);
    }
    
    public E Front() {
        return get(0);
    }
    
    public void DeleteBack() {
        remove(elementCount-1);
    }
}
