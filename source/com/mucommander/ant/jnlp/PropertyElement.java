package com.mucommander.ant.jnlp;

/**
 * @author Nicolas Rinaudo
 * @ant.type name="property" category="webstart"
 */
public class PropertyElement {
    private String name;
    private String value;

    public String getName() {return name;}
    public String getValue() {return value;}
    public void setName(String s) {name = s;}
    public void setValue(String s) {value = s;}
}
