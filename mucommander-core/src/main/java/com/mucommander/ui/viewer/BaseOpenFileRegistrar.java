/*
 * This file is part of muCommander, http://www.mucommander.com
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
package com.mucommander.ui.viewer;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.protocol.local.LocalFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogAction;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.viewer.FileOpenService;

import java.awt.Frame;
import java.awt.Image;
import java.util.Arrays;
import java.util.List;

import static com.mucommander.ui.dialog.QuestionDialog.DIALOG_DISPOSED_ACTION;

/**
 * A based for ViewerRegistrar or EditorRegistrar to maintain a list of
 * registered file viewers/editors, provide methods to dynamically
 * register file viewers/editors, create appropriate
 * FileViewer/FileEditor (Panel) and ViewerFrame/EditorFrame (Window) instances for a given
 * AbstractFile.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public abstract class BaseOpenFileRegistrar {

    public enum OpenFileRegistrarAction implements DialogAction {

        OPEN_ANYWAY("file_editor.open_anyway"),
        CANCEL("cancel");

        private final String actionName;

        OpenFileRegistrarAction(String actionKey) {
            // here or when in #getActionName
            this.actionName = Translator.get(actionKey);
        }

        @Override
        public String getActionName() {
            return actionName;
        }
    }

    /**
     * Creates and returns a ViewerFrame to start viewing the given file. The
     * ViewerFrame will be monitored so that if it is the last window on screen
     * when it is closed by the user, it will trigger the shutdown sequence.
     *
     * @param mainFrame the parent MainFrame instance
     * @param file the file that will be displayed by the returned ViewerFrame
     * @param fromSearchWithContent whether the file is from File Search with Content
     * @param icon window's icon.
     * @return the created FileFrame (ViewerFrame or EditorFrame for example)
     */
    public FileFrame createOpenFileFrame(MainFrame mainFrame, AbstractFile file,
            boolean fromSearchWithContent, Image icon) {
        FileFrame frame = createFrame(mainFrame, file, fromSearchWithContent, icon);

        // Use new Window decorations introduced in Mac OS X 10.5 (Leopard)
        if (OsFamily.MAC_OS.isCurrent()) {
            // Displays the document icon in the window title bar, works only for local files
            if (file.getURL().getScheme().equals(LocalFile.SCHEMA)) {
                frame.getRootPane().putClientProperty("Window.documentFile", file.getUnderlyingFileObject());
            }
        }

        // WindowManager will listen to window closed events to trigger shutdown sequence
        // if it is the last window visible
        frame.addWindowListener(WindowManager.getInstance());

        return frame;
    }

    /**
     * Registers all available viewer/editor services for the given file type.
     *
     * @param file the file that will be displayed by the returned FileViewer
     * @param presenter file presenter to register to
     * @param frame frame the frame in which the FilePresenter is shown
     * @return number of viewer services registered
     * @throws UserCancelledException if the user has been asked to confirm the
     * operation and canceled
     */
    public int registerFileOpeners(AbstractFile file, FilePresenter presenter, FileFrame frame) throws UserCancelledException {
        int counter = 0;
        boolean openFileCanceled = false;
        List<? extends FileOpenService> openFileServices = getOpenFileServices();

        for (FileOpenService service : openFileServices) {
            switch (service.canOpenFile(file)) {
                case YES_USER_CONSENT:
                    if (counter == 0) {
                        // TODO move it to FileViewerPresenter/FileEditorPresenter,
                        // so it could be presented also when manually switching FileOpenServices
                        QuestionDialog dialog = new QuestionDialog((Frame) null, Translator.get("warning"),
                                Translator.get(service.getConfirmationMsg()), frame.getMainFrame().getJFrame(),
                                Arrays.asList(OpenFileRegistrarAction.OPEN_ANYWAY, OpenFileRegistrarAction.CANCEL),
                                0);

                        DialogAction ret = dialog.getActionValue();
                        if (ret == OpenFileRegistrarAction.CANCEL || ret == DIALOG_DISPOSED_ACTION) {
                            // User canceled the operation
                            openFileCanceled = true;
                            break;
                        } // else // User confirmed the operation - continue adding service
                    }
                case YES:
                    addService(presenter, service);
                    counter++;
                    break;
                case NO:
                    break;
            }
        }

        if (counter == 0 && openFileCanceled) {
            throw new UserCancelledException();
        }

        return counter;
    }

    abstract List<? extends FileOpenService> getOpenFileServices();

    abstract FileFrame createFrame(MainFrame mainFrame, AbstractFile file,
            boolean fromSearchWithContent, Image icon);

    abstract void addService(FilePresenter presenter, FileOpenService service) throws UserCancelledException;
}
