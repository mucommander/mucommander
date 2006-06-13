package com.mucommander.ant.util;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Ant task meant to modify strings and store the results in properties.
 * @author Nicolas Rinaudo
 */
public class ReplaceTask extends Task {
    /** Describes the tokens that should be replaced. */
    private String what;
    /** What to replace occurences of {@link #regexp} with. */
    private String with;
    /** Value in which occurences of {@link #regexp} should be replaces. */
    private String from;
    /** Name of the property in which to store the task's output. */
    private String to;

    public ReplaceTask() {}

    public void init() {
        what = null;
        with = null;
        from = null;
        to   = null;
    }

    public void setWith(String s) {with = s;}
    public void setFrom(String s) {from = s;}
    public void setTo(String s) {to = s;}
    public void setWhat(String s) {what = s;}

    public void execute() throws BuildException {
        if(with == null)
            throw new BuildException("Unspecified with - please fill in the with attribute.");
        if(what == null)
            throw new BuildException("Unspecified what - please fill in the what attribute.");
        if(from == null)
            throw new BuildException("Unspecified from - please fill in the from attribute.");
        if(to == null)
            throw new BuildException("Unspecified to - please fill in the to attribute.");
        getProject().setProperty(to, from.replaceAll(what, with));
    }
}
