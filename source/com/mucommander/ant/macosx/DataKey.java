package com.mucommander.ant.macosx;

/**
 * @author Nicolas Rinaudo
 */
public class DataKey extends NamedInfoElement {
    public DataKey() {setValue(new DataValue());}

    public void addText(String str) {((DataValue)getValue()).addText(str);}
}
