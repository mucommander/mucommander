package com.mucommander.ant.jnlp;

import java.util.*;

/**
 * @author Nicolas Rinaudo
 * @ant.type name="applicationdesc" category="webstart"
 */
public class ApplicationDescElement {
    private String mainClass;
    private Vector arguments;

    public ApplicationDescElement() {arguments = new Vector();}

    public void setMain(String s) {mainClass = s;}
    public String getMain() {return mainClass;}
    public boolean hasArguments() {return !arguments.isEmpty();}
    public Iterator arguments() {return arguments.iterator();}
    public ArgumentElement createArgument() {
        ArgumentElement buffer;

        buffer = new ArgumentElement();
        arguments.add(buffer);

        return buffer;
    }
}
