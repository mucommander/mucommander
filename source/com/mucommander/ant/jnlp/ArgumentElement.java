package com.mucommander.ant.jnlp;

/**
 * @author Nicolas Rinaudo
 * @ant.type name="argument" category="webstart"
 */
public class ArgumentElement {
    private StringBuffer text;

    public ArgumentElement() {text = new StringBuffer();}

    public void addText(String s) {text.append(s);}
    public String getText() {return text.toString().trim();}
}
