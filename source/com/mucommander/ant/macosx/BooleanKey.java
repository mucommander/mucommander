package com.mucommander.ant.macosx;

/**
 * @author Nicolas Rinaudo
 * @ant.type name="boolean" category="macosx"
 */
public class BooleanKey extends NamedInfoElement {
    public void setValue(boolean b) {setValue(new BooleanValue(b));}
}
