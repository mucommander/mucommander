package com.mucommander.ui.action;

import com.mucommander.Debug;
import com.mucommander.text.Translator;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.WindowManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Brings a MainFrame window to the front. The window number must be specified in the
 * {@link #WINDOW_NUMBER_PROPERTY_KEY} property, and must exist (i.e. must refer to an existing window number).
 *
 * @see com.mucommander.ui.WindowManager
 * @author Maxence Bernard
 */
public class RecallWindowAction extends MucoAction implements PropertyChangeListener {

    public final static String WINDOW_NUMBER_PROPERTY_KEY = "window_number";
    

    public RecallWindowAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        // Set label in case the window number was set in the initial properties
        int windowNumber = getWindowNumber();
        if(windowNumber!=-1)
            updateLabel(windowNumber);

        // Listen to window number property change
        addPropertyChangeListener(this);
    }


    public void performAction() {
        Vector mainFrames = WindowManager.getMainFrames();

        // Checks that the window number currently exists
        int windowNumber = getWindowNumber();
        if(windowNumber<=0 || windowNumber>mainFrames.size()) {
            if(Debug.ON) Debug.trace("Specified window does not exist: "+getValue(WINDOW_NUMBER_PROPERTY_KEY));
            return;
        }

        // Brings the MainFrame to front
        ((MainFrame)mainFrames.elementAt(windowNumber-1)).toFront();
    }


    /**
     * Returns the window number contained by the {@link #WINDOW_NUMBER_PROPERTY_KEY} property or -1 if the property
     * doesn't contain any value, or a value that cannot be parsed as an int.
     *
     * @returns the window number's property value or -1 if the property doesn't contain any value, or a value that cannot be parsed as an int.
     */
    private int getWindowNumber() {
        try {
            Object windowNumberValue = getValue(WINDOW_NUMBER_PROPERTY_KEY);
            if(windowNumberValue==null || !(windowNumberValue instanceof String))
                return -1;
            
            return Integer.parseInt((String)windowNumberValue);
        }
        catch(Exception e) {
            return -1;
        }
    }


    /**
     * Updates the label using the given window number.
     */
    private void updateLabel(int windowNumber) {
        // Update the action's label
        setLabel(Translator.get(getClass().getName()+".label", ""+windowNumber));
    }


    ///////////////////////////////////////////
    // PropertyChangeListener implementation //
    ///////////////////////////////////////////

    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

        if(propertyChangeEvent.getPropertyName().equals(WINDOW_NUMBER_PROPERTY_KEY)) {
            int windowNumber = getWindowNumber();
            if(windowNumber==-1) {
                if(Debug.ON) Debug.trace("Invalid "+WINDOW_NUMBER_PROPERTY_KEY+" property="+getValue(WINDOW_NUMBER_PROPERTY_KEY));
            }
            else {
                updateLabel(windowNumber);
            }
        }
    }
}
