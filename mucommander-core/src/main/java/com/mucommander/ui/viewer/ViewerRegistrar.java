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

import java.awt.Image;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.protocol.local.LocalFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.ui.dialog.DialogAction;
import com.mucommander.viewer.FileViewerService;
import com.mucommander.osgi.FileViewerServiceTracker;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.viewer.WarnUserException;
import java.awt.Frame;
import java.util.Arrays;
import java.util.List;

import static com.mucommander.ui.dialog.QuestionDialog.DIALOG_DISPOSED_ACTION;

/**
 * ViewerRegistrar maintains a list of registered file viewers and provides
 * methods to dynamically register file viewers and create appropriate
 * FileViewer (Panel) and ViewerFrame (Window) instances for a given
 * AbstractFile.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class ViewerRegistrar {

    public enum ViewerRegistrarAction implements DialogAction {

        OPEN_ANYWAY("file_editor.open_anyway"),
        CANCEL("cancel");

        private final String actionName;

        ViewerRegistrarAction(String actionKey) {
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
     * @return the created ViewerFrame
     */
    public static FileFrame createViewerFrame(MainFrame mainFrame, AbstractFile file, boolean fromSearchWithContent, Image icon) {
        ViewerFrame frame = new ViewerFrame(mainFrame, file, fromSearchWithContent, icon);

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
     * Registers all available viewer services for the given file type.
     *
     * @param file the file that will be displayed by the returned FileViewer
     * @param presenter file viewer presenter to register to
     * @return number of viewer services registered
     * @throws UserCancelledException if the user has been asked to confirm the
     * operation and canceled
     */
    public static int registerFileViewers(AbstractFile file, FileViewerPresenter presenter, ViewerFrame viewerFrame) throws UserCancelledException {
        int counter = 0;
        boolean viewerCanceled = false;
        List<FileViewerService> viewerServices = FileViewerServiceTracker.getViewerServices();

        for (FileViewerService service : viewerServices) {
            try {
                if (service.canViewFile(file)) {
                    presenter.addViewerService(service);
                    counter++;
                }
            } catch (WarnUserException e) {
                // TODO: question the user how does he want to open the file (as image, text..)
                // Todo: display a proper warning dialog with the appropriate icon

                QuestionDialog dialog = new QuestionDialog((Frame) null, Translator.get("warning"), Translator.get(e.getMessage()), viewerFrame.getMainFrame().getJFrame(),
                        Arrays.asList(ViewerRegistrarAction.OPEN_ANYWAY, ViewerRegistrarAction.CANCEL),
                        0);

                DialogAction ret = dialog.getActionValue();
                if (ret == ViewerRegistrarAction.CANCEL || ret == DIALOG_DISPOSED_ACTION) {
                    // User canceled the operation
                    viewerCanceled = true;
                } else {
                    // User confirmed the operation
                    presenter.addViewerService(service);
                    counter++;
                }
            }
        }

        if (counter == 0 && viewerCanceled) {
            throw new UserCancelledException();
        }

        return counter;
    }
}
