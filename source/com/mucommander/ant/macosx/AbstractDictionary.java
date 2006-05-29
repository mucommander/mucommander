package com.mucommander.ant.macosx;

import java.util.*;
import org.apache.tools.ant.BuildException;

public abstract class AbstractDictionary {
    private Vector elements;

    public AbstractDictionary() {elements = new Vector();}

    public Dictionary createDict() {
        Dictionary dict;

        dict = new Dictionary();
        elements.add(dict);

        return dict;
    }

    public InfoString createString() {
        InfoString string;

        string = new InfoString();
        elements.add(string);
        return string;
    }

    public Dictionary getDictionary(String name) {
        Iterator iterator;
        Object   element;

        iterator = elements.iterator();
        while(iterator.hasNext())
            if((element = iterator.next()) instanceof Dictionary)
                if(((Dictionary)element).getName().equals(name))
                    return (Dictionary)element;
        return null;
    }

    public abstract void check() throws BuildException;
    public Iterator elements() {return elements.iterator();}
}
