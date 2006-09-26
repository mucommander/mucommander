package com.mucommander.ui.action;

import com.mucommander.text.Translator;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.icon.IconManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Maxence Bernard
 */
public abstract class MucoAction extends AbstractAction {

    protected MainFrame mainFrame;


//    public MucoAction(MainFrame mainFrame) {
//        this(mainFrame, null, null, null);
//    }
//
//    public MucoAction(MainFrame mainFrame, String labelKey) {
//        this(mainFrame, labelKey, null, null);
//    }
//
//    public MucoAction(MainFrame mainFrame, String labelKey, KeyStroke accelerator) {
//        this(mainFrame, labelKey, accelerator, null);
//    }
//
//    public MucoAction(MainFrame mainFrame, String labelKey, KeyStroke accelerator, String toolTipKey) {
//        this.mainFrame = mainFrame;
//
//        if(labelKey!=null)
//            setLabel(Translator.get(labelKey));
//
//        if(toolTipKey!=null)
//            setToolTipText(Translator.get(toolTipKey));
//
//        if(accelerator!=null)
//            setAccelerator(accelerator);
//
//        Class classInstance = getClass();
//        String className = classInstance.getName();
//        String iconPath = "/action/"+className+".png";
//
//        if(classInstance.getResource(iconPath)!=null)
//            putValue(Action.SMALL_ICON, IconManager.getIcon(iconPath));
//
//    }


    public MucoAction(MainFrame mainFrame) {
        this(mainFrame, null);
    }

    public MucoAction(MainFrame mainFrame, KeyStroke accelerator) {
        this.mainFrame = mainFrame;

        // Look for a label dictionary entry in the '<classname>.label' format and use it if it exists
        Class classInstance = getClass();
        String className = classInstance.getName();
        String key = className+".label";
        if(Translator.entryExists(key))
            setLabel(Translator.get(key));

        // Look for a tooltip dictionary entry in the '<action_classname>.tooltip' format and use it if it exists
        key = className+".tooltip";
        if(Translator.entryExists(key))
            setToolTipText(Translator.get(key));

        // Set the accelerator (if any)
        if(accelerator!=null)
            setAccelerator(accelerator);

        // Look for an icon file with the path /action/<classname>.png and use it if it exists
        String iconPath = "/action/"+className+".png";
        if(classInstance.getResource(iconPath)!=null)
            putValue(Action.SMALL_ICON, IconManager.getIcon(iconPath));
    }


    public MainFrame getMainFrame() {
        return this.mainFrame;
    }


    public String getLabel() {
        return (String)getValue(Action.NAME);
    }

    public void setLabel(String label) {
        putValue(Action.NAME, label);
    }


    public String getToolTipText() {
        return (String)getValue(Action.SHORT_DESCRIPTION);
    }

    public void setToolTipText(String toolTipText) {
        putValue(Action.SHORT_DESCRIPTION, toolTipText);
    }


    public KeyStroke getAccelerator() {
        return (KeyStroke)getValue(Action.ACCELERATOR_KEY);
    }

    public void setAccelerator(KeyStroke keyStroke) {
        putValue(Action.ACCELERATOR_KEY, keyStroke);
    }


    public ImageIcon getIcon() {
        return (ImageIcon)getValue(Action.SMALL_ICON);
    }

    public void setIcon(ImageIcon icon) {
        putValue(Action.SMALL_ICON, icon);
    }


    /**
     * Returns a String representation of the accelerator, in the [MODIFIER+]KEY format, for instance CTRL+S.
     * This method will return <code>null</code> if this action has no accelerator.
     *
     * @return a String representation of the accelerator, or <code>null</code> if this action has no accelerator.
     */
    public String getAcceleratorText() {
        KeyStroke accelerator = getAccelerator();
        if(accelerator==null)
            return null;

        String text = KeyEvent.getKeyText(accelerator.getKeyCode());
        int modifiers = accelerator.getModifiers();
        if(modifiers!=0)
            text = KeyEvent.getKeyModifiersText(modifiers)+"+"+text;

        return text;
    }


    public boolean ignoreEventsWhileInNoEventsMode() {
        return true;
    }


    ///////////////////////////////////
    // AbstractAction implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        // Discard this event while in 'no events mode'
        if(!(mainFrame.getNoEventsMode() && ignoreEventsWhileInNoEventsMode()))
            performAction(mainFrame);
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    public abstract void performAction(MainFrame mainFrame);
}
