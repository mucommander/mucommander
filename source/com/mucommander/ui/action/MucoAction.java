package com.mucommander.ui.action;

import com.mucommander.text.Translator;
import com.mucommander.ui.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Maxence Bernard
 */
public abstract class MucoAction extends AbstractAction {

    protected MainFrame mainFrame;


    public MucoAction(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public MucoAction(MainFrame mainFrame, String labelKey) {
        this.mainFrame = mainFrame;
        setLabel(Translator.get(labelKey));
    }

    public MucoAction(MainFrame mainFrame, String labelKey, KeyStroke accelerator) {
        this.mainFrame = mainFrame;
        setLabel(Translator.get(labelKey));
        setAccelerator(accelerator);
    }

    public MucoAction(MainFrame mainFrame, String labelKey, KeyStroke accelerator, String toolTipKey) {
        this.mainFrame = mainFrame;
        setLabel(Translator.get(labelKey));
        setToolTipText(Translator.get(toolTipKey));
        setAccelerator(accelerator);
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


    ///////////////////////////////////
    // AbstractAction implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        // Discard this event while in 'no events mode'
        if(!mainFrame.getNoEventsMode())
            performAction(mainFrame);
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    public abstract void performAction(MainFrame mainFrame);
}
