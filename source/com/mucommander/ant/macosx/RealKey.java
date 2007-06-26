package com.mucommander.ant.macosx;

/**
 * @author Nicolas Rinaudo
 * @ant.type name="real" category="macosx"
 */
public class RealKey extends NamedInfoElement {
    public void setValue(float f) {setValue(new RealValue(f));}
}
