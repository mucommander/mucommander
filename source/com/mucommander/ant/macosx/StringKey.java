package com.mucommander.ant.macosx;

/**
 * @author Nicolas Rinaudo
 * @ant.type name="string" category="macosx"
 */
public class StringKey extends NamedInfoElement {
    public void setValue(String b) {setValue(new StringValue(b));}
}
