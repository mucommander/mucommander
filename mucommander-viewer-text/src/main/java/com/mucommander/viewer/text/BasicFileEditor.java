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
package com.mucommander.viewer.text;

import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.protocol.local.LocalFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.util.ui.helper.MenuToolkit;
import com.mucommander.commons.util.ui.helper.MnemonicHelper;
import com.mucommander.text.Translator;
import com.mucommander.job.FileCollisionChecker;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.dialog.QuestionDialog;
import com.mucommander.ui.dialog.file.FileCollisionDialog;
import com.mucommander.viewer.CloseCancelledException;
import com.mucommander.viewer.EditorPresenter;
import com.mucommander.viewer.FileEditor;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * Abstract file editor with basic support for save operation.
 *
 * @author Miroslav Hajda
 */
public abstract class BasicFileEditor implements FileEditor {

    protected final JMenu fileMenu;
    protected final JMenuItem saveItem;
    protected final JMenuItem saveAsItem;

    protected EditorPresenter presenter;

    private AbstractFile currentFile;
    
    /**
     * Serves to indicate if saving is needed before closing the window, value
     * should only be modified using the setSaveNeeded() method
     */
    private boolean saveNeeded;

    public BasicFileEditor() {
        MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

        fileMenu = MenuToolkit.addMenu(Translator.get("file_editor.file_menu"), menuMnemonicHelper, null);
        saveItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_editor.save"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK), (e) -> {
            trySave(getCurrentFile());
        });
        saveAsItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_editor.save_as"), menuItemMnemonicHelper, null, (e) -> {
            trySaveAs();
        });
    }

    @Override
    public abstract void open(AbstractFile file) throws IOException;

    @Override
    public abstract void close() throws CloseCancelledException;

    @Override
    public abstract JComponent getUI();

    @Override
    public void setPresenter(EditorPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void extendMenu(JMenuBar menuBar) {
        menuBar.add(fileMenu);
    }

    public AbstractFile getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(AbstractFile currentFile) {
        this.currentFile = currentFile;
    }

    protected void setSaveNeeded(boolean saveNeeded) {
        if (getFrame() != null && this.saveNeeded != saveNeeded) {
            this.saveNeeded = saveNeeded;

            // Marks/unmarks the window as dirty under Mac OS X (symbolized by a dot in the window closing icon)
            if (OsFamily.MAC_OS.isCurrent()) {
                getFrame().getRootPane().putClientProperty("windowModified", saveNeeded ? Boolean.TRUE : Boolean.FALSE);
            }
        }
    }

    private void trySaveAs() {
        JFileChooser fileChooser = new JFileChooser();
        AbstractFile currentFile = getCurrentFile();
        // Sets selected file in JFileChooser to current file
        if (currentFile.getURL().getScheme().equals(LocalFile.SCHEMA)) {
            fileChooser.setSelectedFile(new java.io.File(currentFile.getAbsolutePath()));
        }
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        int ret = fileChooser.showSaveDialog(getFrame());

        if (ret == JFileChooser.APPROVE_OPTION) {
            AbstractFile destFile;
            try {
                destFile = FileFactory.getFile(fileChooser.getSelectedFile().getAbsolutePath(), true);
            } catch (IOException e) {
                InformationDialog.showErrorDialog(getFrame(), Translator.get("write_error"), Translator.get("file_editor.cannot_write"));
                return;
            }

            // Check for file collisions, i.e. if the file already exists in the destination
            int collision = FileCollisionChecker.checkForCollision(null, destFile);
            if (collision != FileCollisionChecker.NO_COLLOSION) {
                // File already exists in destination, ask the user what to do (cancel, overwrite,...) but
                // do not offer the multiple files mode options such as 'skip' and 'apply to all'.
                int action = new FileCollisionDialog(getFrame(), getFrame()/*mainFrame*/, collision, null, destFile, false, false).getActionValue();

                // User chose to overwrite the file
                if (action == FileCollisionDialog.OVERWRITE_ACTION) {
                    // Do nothing, simply continue and file will be overwritten
                } // User chose to cancel or closed the dialog
                else {
                    return;
                }
            }

            if (trySave(destFile)) {
                setCurrentFile(destFile);
            }
        }
    }

    // Returns false if an error occurred while saving the file.
    private boolean trySave(AbstractFile destFile) {
        try {
            saveAs(destFile);
            return true;
        } catch (IOException e) {
            InformationDialog.showErrorDialog(getFrame(), Translator.get("write_error"), Translator.get("file_editor.cannot_write"));
            return false;
        }
    }

    // Returns true if the file does not have any unsaved change or if the user refused to save the changes,
    // false if the user canceled the dialog or the save failed.
    public boolean askSave() {
        if (!saveNeeded) {
            return true;
        }

        QuestionDialog dialog = new QuestionDialog(getFrame(), null, Translator.get("file_editor.save_warning"), getFrame(),
                new String[]{Translator.get("save"), Translator.get("dont_save"), Translator.get("cancel")},
                new int[]{JOptionPane.YES_OPTION, JOptionPane.NO_OPTION, JOptionPane.CANCEL_OPTION},
                0);
        int ret = dialog.getActionValue();

        if ((ret == JOptionPane.YES_OPTION && trySave(getCurrentFile())) || ret == JOptionPane.NO_OPTION) {
            setSaveNeeded(false);
            return true;
        }

        return false;       // User canceled or the file couldn't be properly saved
    }

    private JFrame getFrame() {
        return presenter.getWindowFrame();
    }

    abstract void saveAs(AbstractFile destFile) throws IOException;
}
