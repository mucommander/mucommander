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

import java.awt.Frame;
import java.awt.Image;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.protocol.local.LocalFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.viewer.FileEditorService;
import com.mucommander.osgi.FileEditorServiceTracker;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.viewer.WarnUserException;
import java.util.List;

/**
 * EditorRegistrar maintains a list of registered file editors and provides
 * methods to dynamically register file editors and create appropriate
 * FileEditor (Panel) and EditorFrame (Window) instances for a given
 * AbstractFile.
 *
 * @author Maxence Bernard
 */
public class EditorRegistrar {

    /**
     * Creates and returns an EditorFrame to start viewing the given file. The
     * EditorFrame will be monitored so that if it is the last window on screen
     * when it is closed by the user, it will trigger the shutdown sequence.
     *
     * @param mainFrame the parent MainFrame instance
     * @param file the file that will be displayed by the returned EditorFrame
     * @param icon editor frame's icon.
     * @return the created EditorFrame
     */
    public static FileFrame createEditorFrame(MainFrame mainFrame, AbstractFile file, Image icon) {
        EditorFrame frame = new EditorFrame(mainFrame, file, icon);

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
     * Registers all available editor services for the given file type.
     *
     * @param file the file that will be displayed by the returned FileEditor
     * @param presenter file editor presenter to register to
     * @param frame the frame in which the FileEditor is shown
     * @return number of editor services registered
     * @throws UserCancelledException if the user has been asked to confirm the
     * operation and canceled
     */
    public static int registerFileEditors(AbstractFile file, FileEditorPresenter presenter, EditorFrame frame) throws UserCancelledException {
        int counter = 0;
        boolean editorCanceled = false;
        List<FileEditorService> editorServices = FileEditorServiceTracker.getEditorServices();

        for (FileEditorService service : editorServices) {
            try {
                if (service.canEditFile(file)) {
                    presenter.addEditorService(service);
                    counter++;
                }
            } catch (WarnUserException e) {
                QuestionDialog dialog = new QuestionDialog((Frame) null, Translator.get("warning"), Translator.get(e.getMessage()), frame.getMainFrame(),
                        new String[]{Translator.get("file_editor.open_anyway"), Translator.get("cancel")},
                        new int[]{0, 1},
                        0);

                int ret = dialog.getActionValue();
                if (ret == 1 || ret == -1) {
                    // User canceled the operation
                    editorCanceled = true;
                } else {
                    // User confirmed the operation
                    presenter.addEditorService(service);
                    counter++;
                }
            }
        }

        if (counter == 0 && editorCanceled) {
            throw new UserCancelledException();
        }

        return counter;
    }
}
