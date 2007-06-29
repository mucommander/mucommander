package com.mucommander.ant.jnlp;

/**
 * @author Nicolas Rinaudo
 * @ant.type name="nativelib" category="webstart"
 */
public class NativeLibElement extends Downloadable {
    private String href;
    private String version;
    private int    size;
    private String part;

    public String getHref() {return href;}
    public String getVersion() {return version;}
    public int getSize() {return size;}
    public String getPart() {return part;}
    public void setHref(String s) {href = s;}
    public void setVersion(String s) {version = s;}
    public void setSize(int i) {size = i;}
    public void setPart(String s) {part = s;}
}
