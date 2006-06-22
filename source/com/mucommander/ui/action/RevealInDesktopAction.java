
package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.PlatformManager;
import com.mucommander.text.Translator;

import javax.swing.*;
import java.awt.event.KeyEvent;


/**
 * This action reveals the currently selected file or folder in the native Desktop's file manager
 * (e.g. Finder for Mac OS X, Explorer for Windows, etc...).
 *
 * @author Maxence Bernard
 */
public class RevealInDesktopAction extends MucoAction {

    public RevealInDesktopAction(MainFrame mainFrame) {
        super(mainFrame);
        setLabel(Translator.get("file_menu.reveal_in_desktop", PlatformManager.getDefaultDesktopFMName()));
        setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_MASK));
    }

    public void performAction(MainFrame mainFrame) {
        PlatformManager.openInDesktop(mainFrame.getLastActiveTable().getFolderPanel().getCurrentFolder());
    }
}
