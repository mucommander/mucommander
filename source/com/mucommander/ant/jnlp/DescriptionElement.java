package com.mucommander.ant.jnlp;

import org.apache.tools.ant.BuildException;

/**
 * @author Nicolas Rinaudo
 * @ant.type name="description" category="jnlp"
 */
public class DescriptionElement {
    public static final int KIND_UNSPECIFIED = 0;
    public static final int KIND_ONE_LINE    = 1;
    public static final int KIND_SHORT       = 2;
    public static final int KIND_TOOLTIP     = 3;
    private int kind;
    private StringBuffer description;

    public DescriptionElement() {description = new StringBuffer();}

    public void setKind(String s) {
        if(s.equals("one-line"))
            kind = KIND_ONE_LINE;
        else if(s.equals("short"))
            kind = KIND_SHORT;
        else if(s.equals("tooltip"))
            kind = KIND_TOOLTIP;
        else
            throw new BuildException("Unknown description kind: " + s);
    }
    public String getText() {return description.toString().trim();}
    public int getKind() {return kind;}
    public void addText(String s) {description.append(s);}
}
