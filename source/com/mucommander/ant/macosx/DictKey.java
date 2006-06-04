package com.mucommander.ant.macosx;

/**
 *
 */
public class DictKey extends NamedInfoElement {
    public DictKey() {setValue(new DictValue());}

    public ArrayKey createArray() {return ((DictValue)getValue()).createArray();}
    public BooleanKey createBoolean() {return ((DictValue)getValue()).createBoolean();}
    public StringKey createString() {return ((DictValue)getValue()).createString();}
    public DictKey createDict() {return ((DictValue)getValue()).createDict();}
    public IntegerKey createInteger() {return ((DictValue)getValue()).createInteger();}
    public RealKey createReal() {return ((DictValue)getValue()).createReal();}
}
