package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * Marks or unmarks the current selected file (current row) and advance current row to the next one,
 * with the following exceptions:
 * <ul>
 * <li>if quick search is active, this method does nothing
 * <li>if '..' file is selected, file is not marked but current row is still advanced to the next one
 * <li>if the {@link com.mucommander.ui.action.MarkSelectedFileAction} key event is repeated and the last file has already
 * been marked/unmarked since the key was last released, the file is not marked in order to avoid
 * marked/unmarked flaps when the mark key is kept pressed.
 *
 * @author Maxence Bernard
 */
public class MarkSelectedFileAction extends MucoAction {

    public MarkSelectedFileAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }


    public void performAction() {
        mainFrame.getActiveTable().markSelectedFile();
    }
}
