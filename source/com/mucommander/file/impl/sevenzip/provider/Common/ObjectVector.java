package com.mucommander.file.impl.sevenzip.provider.Common;

public class ObjectVector extends java.util.Vector
{
    public ObjectVector() {
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
