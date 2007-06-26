package com.mucommander.ant.jnlp;

/**
 * @author Nicolas Rinaudo
 * @ant.type name="installerdesc" category="jnlp"
 */
public class InstallerDescElement {
    private String mainClass;

    public String getMain() {return mainClass;}
    public void setMain(String s) {mainClass = s;}
}
