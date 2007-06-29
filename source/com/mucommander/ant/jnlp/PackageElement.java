package com.mucommander.ant.jnlp;

/**
 * @author Nicolas Rinaudo
 * @ant.type name="package" category="webstart"
 */
public class PackageElement {
    private String name;
    private String part;
    private boolean recursive;

    public String getName() {return name;}
    public String getPart() {return part;}
    public boolean getRecursive() {return recursive;}
    public void setName(String s) {name = s;}
    public void setPart(String s) {part = s;}
    public void setRecursive(boolean b) {recursive = b;}
}
