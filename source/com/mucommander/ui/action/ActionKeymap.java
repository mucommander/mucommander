/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.action;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.util.ResourceLoader;
import com.mucommander.io.BackupInputStream;
import com.mucommander.io.BackupOutputStream;
import com.mucommander.io.StreamUtils;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.xml.XmlAttributes;
import com.mucommander.xml.XmlWriter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.swing.*;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;


/**
 * This class manages keyboard associations with {@link MuAction} classes.
 * Proper documentation and cleaning of this class is pending.  
 *
 * @author Maxence Bernard
 */
public class ActionKeymap {

    /** Maps action Class onto Keystroke instances*/
    private static Hashtable primaryActionKeymap = new Hashtable();
    /** Maps action Class instances onto Keystroke instances*/
    private static Hashtable alternateActionKeymap = new Hashtable();

    /** Maps Keystroke instances onto action Class */
    private static Hashtable acceleratorMap = new Hashtable();

    public static KeyStroke getAccelerator(Class muActionClass) {
        return (KeyStroke)primaryActionKeymap.get(muActionClass);
    }

    public static KeyStroke getAlternateAccelerator(Class muActionClass) {
        return (KeyStroke)alternateActionKeymap.get(muActionClass);
    }

    public static boolean isKeyStrokeRegistered(KeyStroke ks) {
        return getRegisteredActionClassForKeystroke(ks)!=null;
    }

    public static Class getRegisteredActionClassForKeystroke(KeyStroke ks) {
        return (Class)acceleratorMap.get(ks);
    }

    public static void registerActions(MainFrame mainFrame) {
        JComponent leftTable = mainFrame.getLeftPanel().getFileTable();
        JComponent rightTable = mainFrame.getRightPanel().getFileTable();

        Enumeration actionClasses = primaryActionKeymap.keys();
        while(actionClasses.hasMoreElements()) {
            MuAction action = ActionManager.getActionInstance((Class)actionClasses.nextElement(), mainFrame);
            ActionKeymap.registerActionAccelerators(action, leftTable, JComponent.WHEN_FOCUSED);
            ActionKeymap.registerActionAccelerators(action, rightTable, JComponent.WHEN_FOCUSED);
        }

        actionClasses = alternateActionKeymap.keys();
        while(actionClasses.hasMoreElements()) {
            MuAction action = ActionManager.getActionInstance((Class)actionClasses.nextElement(), mainFrame);
            ActionKeymap.registerActionAccelerators(action, leftTable, JComponent.WHEN_FOCUSED);
            ActionKeymap.registerActionAccelerators(action, rightTable, JComponent.WHEN_FOCUSED);
        }
    }

    private static void registerAction(MainFrame mainFrame, MuAction action) {
        registerActionAccelerators(action, mainFrame.getLeftPanel().getFileTable(), JComponent.WHEN_FOCUSED);
        registerActionAccelerators(action, mainFrame.getRightPanel().getFileTable(), JComponent.WHEN_FOCUSED);
    }

    private static void unregisterAction(MainFrame mainFrame, MuAction action) {
        unregisterActionAccelerators(action, mainFrame.getLeftPanel().getFileTable(), JComponent.WHEN_FOCUSED);
        unregisterActionAccelerators(action, mainFrame.getRightPanel().getFileTable(), JComponent.WHEN_FOCUSED);
    }

    private static void registerActionAccelerator(MuAction action, KeyStroke accelerator, JComponent comp, int condition) {
        if(accelerator==null)
            return;
        InputMap inputMap = comp.getInputMap(condition);
        ActionMap actionMap = comp.getActionMap();
        Class muActionClass = action.getClass();
        inputMap.put(accelerator, muActionClass);
        actionMap.put(muActionClass, action);
    }

    private static void unregisterActionAccelerator(MuAction action, KeyStroke accelerator, JComponent comp, int condition) {
        if(accelerator==null)
            return;
        InputMap inputMap = comp.getInputMap(condition);
        ActionMap actionMap = comp.getActionMap();
        Class muActionClass = action.getClass();
        inputMap.remove(accelerator);
        actionMap.remove(muActionClass);
    }

    public static void registerActionAccelerators(MuAction action, JComponent comp, int condition) {
    	KeyStroke accelerator = action.getAccelerator();
    	if(accelerator!=null)
    		registerActionAccelerator(action, accelerator, comp, condition);

    	accelerator = action.getAlternateAccelerator();
    	if(accelerator!=null)
    		registerActionAccelerator(action, accelerator, comp, condition);
    }

    private static void unregisterActionAccelerators(MuAction action, JComponent comp, int condition) {
    	KeyStroke accelerator = action.getAccelerator();
    	if(accelerator!=null)
    		unregisterActionAccelerator(action, accelerator, comp, condition);

    	accelerator = action.getAlternateAccelerator();
    	if(accelerator!=null)
    		unregisterActionAccelerator(action, accelerator, comp, condition);
    }
    
    public static void registerActionAccelerators(Class muActionClass, KeyStroke accelerator, KeyStroke alternateAccelerator) {
    	if(accelerator!=null) { //&& !isKeyStrokeRegistered(accelerator)) {
    		primaryActionKeymap.put(muActionClass, accelerator);
    		acceleratorMap.put(accelerator, muActionClass);
    	}

    	if(alternateAccelerator!=null) { // && !isKeyStrokeRegistered(alternateAccelerator)) {
    		alternateActionKeymap.put(muActionClass, alternateAccelerator);
    		acceleratorMap.put(alternateAccelerator, muActionClass);
    	}
    }
    
    public static void changeActionAccelerators(Class muActionClass, KeyStroke accelerator, KeyStroke alternateAccelerator) {
    	// Remove old accelerators (primary and alternate) from accelerators map
    	KeyStroke oldAccelator = (KeyStroke)primaryActionKeymap.remove(muActionClass);
    	if(oldAccelator!=null)
    		acceleratorMap.remove(oldAccelator);

    	oldAccelator = (KeyStroke)alternateActionKeymap.remove(muActionClass);
    	if(oldAccelator!=null)
    		acceleratorMap.remove(oldAccelator);

    	// Register new accelerators
    	registerActionAccelerators(muActionClass, accelerator, alternateAccelerator);

    	// Update each MainFrame's action instance and input map
    	Vector actionInstances = ActionManager.getActionInstances(muActionClass);
    	int nbActionInstances = actionInstances.size();
    	for(int i=0; i<nbActionInstances; i++) {
    		MuAction action = (MuAction)actionInstances.elementAt(i);
    		MainFrame mainFrame = action.getMainFrame();

    		// Remove action from MainFrame's action and input maps
    		unregisterAction(mainFrame, action);

    		// Change action's accelerators
    		action.setAccelerator(accelerator);
    		action.setAlternateAccelerator(alternateAccelerator);

    		// Add updated action to MainFrame's action and input maps
    		registerAction(mainFrame, action);
    	}
    	
    	ActionIO.setModified();
    }
    
    public static Enumeration getCustomizedActions() { return primaryActionKeymap.keys(); }
}
