
package com.mucommander.ui.action;

import com.mucommander.PlatformManager;
import com.mucommander.text.Translator;
import com.mucommander.ui.MainFrame;


/**
 * This action reveals the currently selected file or folder in the native Desktop's file manager
 * (e.g. Finder for Mac OS X, Explorer for Windows, etc...).
 *
 * @author Maxence Bernard
 */
public class RevealInDesktopAction extends MucoAction {

    public RevealInDesktopAction(MainFrame mainFrame) {
        super(mainFrame);
        setLabel(Translator.get(getClass().getName()+".label", PlatformManager.getDefaultDesktopFMName()));

        // Disable this action if the platform is not capable of opening files in the default file manager
        if(!PlatformManager.canOpenInDesktop())
            setEnabled(false);
    }

    public void performAction() {
        PlatformManager.openInDesktop(mainFrame.getActiveTable().getFolderPanel().getCurrentFolder());
    }
}
