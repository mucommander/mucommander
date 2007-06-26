package com.mucommander.ant.jnlp;

/**
 * @author Nicolas Rinaudo
 * @ant.type name="extdownload" category="jnlp"
 */
public class ExtDownloadElement extends Downloadable {
    private String extPart;
    private String part;

    public String getExtPart() {return extPart;}
    public String getPart() {return part;}
    public void setExtPart(String s) {extPart = s;}
    public void setPart(String s) {part = s;}
}
