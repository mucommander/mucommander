package com.mucommander.ant.jnlp;

/**
 * @author Nicolas Rinaudo
 */
public class ArgumentElement {
    private StringBuffer text;

    public ArgumentElement() {text = new StringBuffer();}

    public void addText(String s) {text.append(s);}
    public String getText() {return text.toString().trim();}
}
