package com.mucommander.ui.action;

import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;
import com.mucommander.Debug;

import javax.swing.*;
import java.util.Hashtable;


/**
 * @author Maxence Bernard
 */
public class ActionKeymap implements ContentHandler {

    private static Hashtable primaryActionKeymap = new Hashtable();
    private static Hashtable alternateActionKeymap = new Hashtable();

    private final static String ACTION_KEYMAP_FILE_PATH = "/action_keymap.xml";

    static {
        // Todo: move this call to Launcher
        parseActionKeymap();
    }

    public static void parseActionKeymap() {
        new ActionKeymap();
    }


    public static void registerAccelerator(Class mucoActionClass, KeyStroke ks, boolean primaryAccelerator) {
        if(Debug.ON) Debug.trace("associating "+ks+" accelerator with "+mucoActionClass);

        (primaryAccelerator?primaryActionKeymap:alternateActionKeymap).put(mucoActionClass, ks);
    }


    public static KeyStroke getAccelerator(Class mucoActionClass) {
        return (KeyStroke)primaryActionKeymap.get(mucoActionClass);
    }

    public static KeyStroke getAlternateAccelerator(Class mucoActionClass) {
        return (KeyStroke)alternateActionKeymap.get(mucoActionClass);
    }


    private ActionKeymap() {
        // Parse action keymap file
        try {
            new Parser().parse(getClass().getResourceAsStream(ACTION_KEYMAP_FILE_PATH), this, "UTF-8");
        }
        catch(Exception e) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Error: unable to load action_keymap.xml file "+e);
        }
    }


    ///////////////////////////////////
    // ContentHandler implementation //
    ///////////////////////////////////

    public void startDocument() throws Exception {
    }

    public void endDocument() throws Exception {
    }

    public void startElement(String uri, String name, Hashtable attValues, Hashtable attURIs) throws Exception {
        if(name.equals("action")) {
            String actionClassName = (String)attValues.get("class");
            if(actionClassName==null) {
                if(Debug.ON) Debug.trace("WARNING: no 'class' attribute specified in 'action' element");
                return;
            }

            Class actionClass;
            try {
                actionClass = Class.forName(actionClassName);
            }
            catch(ClassNotFoundException e) {
                if(Debug.ON) Debug.trace("WARNING: could not resolve class "+actionClassName);
                return;
            }

            // Primary keystroke
            String keyStrokeString = (String)attValues.get("keystroke");
            if(keyStrokeString==null) {
                if(Debug.ON) Debug.trace("WARNING: no 'keystroke' attribute specified in 'action' element");
                return;
            }

            KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeString);
            if(keyStroke==null) {
                if(Debug.ON) Debug.trace("WARNING: specified keystroke could not be resolved: "+keyStrokeString);
                return;
            }

            registerAccelerator(actionClass, keyStroke, true);

            // Alternate keystroke (if any)
            keyStrokeString = (String)attValues.get("alt_keystroke");
            if(keyStrokeString!=null) {
                keyStroke = KeyStroke.getKeyStroke(keyStrokeString);
                if(keyStroke==null) {
                    if(Debug.ON) Debug.trace("WARNING: specified alternate keystroke could not be resolved: "+keyStrokeString);
                    return;
                }

                registerAccelerator(actionClass, keyStroke, false);
            }
        }
    }

    public void endElement(String uri, String name) throws Exception {
    }

    public void characters(String s) throws Exception {
    }
}
