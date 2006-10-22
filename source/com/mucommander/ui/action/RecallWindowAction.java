package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.WindowManager;
import com.mucommander.text.Translator;

import java.util.Vector;

/**
 * Recalls a window (brings it to front) with a specific window number, starting at 1.
 * This action is abstract and needs to be extended to specify the window number to recall.
 *
 * @see com.mucommander.ui.WindowManager
 * @author Maxence Bernard
 */
public abstract class RecallWindowAction extends MucoAction {

    private int windowNumber;

    /**
     * Creates a new RecallWindowAction instance for the specified window number and attached to the given MainFrame.
     *
     * @param mainFrame MainFrame this action is attached to
     * @param windowNumber the window number to recall, starts at 1
     */
    public RecallWindowAction(MainFrame mainFrame, int windowNumber) {
        super(mainFrame);
        this.windowNumber = windowNumber;

        setLabel(Translator.get(getClass().getName()+".label", ""+windowNumber));
    }


    public void performAction() {
        Vector mainFrames = WindowManager.getMainFrames();

        // Checks that the window number currently exists
        if(windowNumber<=0 || windowNumber>mainFrames.size())
            return;

        // Brings the MainFrame to front
        ((MainFrame)mainFrames.elementAt(windowNumber-1)).toFront();
    }
}
