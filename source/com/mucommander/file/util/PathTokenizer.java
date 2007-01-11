package com.mucommander.file.util;

import com.mucommander.Debug;

import java.util.StringTokenizer;
import java.util.Enumeration;
import java.util.NoSuchElementException;


/**
 * @author Maxence Bernard
 */
public class PathTokenizer implements Enumeration {

    private StringTokenizer st;
    private String separators; 

    private String currentPath = "";

    private String nextToken;


    public PathTokenizer(String path) {
        this(path, "/\\");
    }

    public PathTokenizer(String path, String separators) {
        st = new StringTokenizer(path, separators, true);
        this.separators = separators;
    }


//    private void advance() {
//
//    }


    public boolean hasMoreTokens() {
        return st.hasMoreTokens();
    }

    public boolean hasMoreElements() {
        return st.hasMoreTokens();
    }


    public String nextToken() throws NoSuchElementException {
        String token = st.nextToken();
        currentPath += token;

        while(separators.contains(token) && st.hasMoreTokens()) {
            token = st.nextToken();
            currentPath += token;
        }

        return token;
    }

    public Object nextElement() throws NoSuchElementException {
        return st.nextToken();
    }


    public String getCurrentPath() {
        return currentPath;
    }

    public static void main(String args[]) {
        test("/Users/maxence/Temp");
        test("/Users/maxence/Temp/");
        test("/");
        test("C:\\temp");
        test("C:\\temp\\");
        test("C:\\");
        test("C:");
    }

    private static void test(String path) {
        PathTokenizer pt = new PathTokenizer(path);
        if(Debug.ON) Debug.trace("tokenizing: "+path);
        while(pt.hasMoreTokens()) {
            if(Debug.ON) Debug.trace("nexToken(): "+pt.nextToken());
            if(Debug.ON) Debug.trace("getCurrentPath(): "+pt.getCurrentPath());
        }

        if(Debug.ON) Debug.trace("");
    }
}
