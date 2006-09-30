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

//    protected KeyStroke alternateAccelerator;

    public final static String ALTERNATE_ACCELERATOR_KEY = "alternate_accelerator";


    public MucoAction(MainFrame mainFrame) {
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

        // Look for an accelerator registered in ActionKeymap for this action class
        KeyStroke accelerator = ActionKeymap.getAccelerator(classInstance);
        if(accelerator!=null) {
            setAccelerator(accelerator);

            // Look for an alternate accelerator registered in ActionKeymap for this action class
            accelerator = ActionKeymap.getAlternateAccelerator(classInstance);
            if(accelerator!=null) {
                setAlternateAccelerator(accelerator);
            }
        }

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
//        // Remove a previously set accelerator
//        KeyStroke currentAccelerator = getAccelerator();
//        if(currentAccelerator!=null)
//            mainFrame.unregisterActionAccelerator(this, currentAccelerator);
//
//        // Register the new accelerator
//        mainFrame.registerActionAccelerator(this, keyStroke);
//
        putValue(Action.ACCELERATOR_KEY, keyStroke);
    }


    public KeyStroke getAlternateAccelerator() {
//        return alternateAccelerator;
        return (KeyStroke)getValue(ALTERNATE_ACCELERATOR_KEY);
    }

    public void setAlternateAccelerator(KeyStroke keyStroke) {
//        // Remove a previously set accelerator
//        KeyStroke currentAccelerator = getAlternateAccelerator();
//        if(currentAccelerator!=null)
//            mainFrame.unregisterActionAccelerator(this, currentAccelerator);
//
//        // Register the new accelerator
//        mainFrame.registerActionAccelerator(this, keyStroke);
//
//        this.alternateAccelerator = keyStroke;

        putValue(ALTERNATE_ACCELERATOR_KEY, keyStroke);
    }


    /**
     * Returns true if the given KeyStroke is one of this action's accelerators.
     * Always returns false if this method has no accelerator.
     *
     * @param keyStroke the KeyStroke to test against this action's acccelerators
     * @return true if the given KeyStroke is one of this action's accelerators
     */
    public boolean isAccelerator(KeyStroke keyStroke) {
        KeyStroke accelerator = getAccelerator();
        if(accelerator!=null && accelerator.equals(keyStroke))
            return true;

        accelerator = getAlternateAccelerator();
        return accelerator!=null && accelerator.equals(keyStroke);
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
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called, action="+getClass().getName());

        // Discard this event while in 'no events mode'
        if(!(mainFrame.getNoEventsMode() && ignoreEventsWhileInNoEventsMode()))
            performAction(mainFrame);
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    public abstract void performAction(MainFrame mainFrame);
}
