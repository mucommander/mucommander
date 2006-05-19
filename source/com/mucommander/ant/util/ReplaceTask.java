package com.mucommander.ant.util;

import org.apache.tools.ant.*;

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

    private void flush() {
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
        getProject().setProperty(to, from.replaceAll(what, with));
    }
}
