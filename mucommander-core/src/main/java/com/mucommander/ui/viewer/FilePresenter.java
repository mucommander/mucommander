package com.mucommander.ui.viewer;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.runtime.OsFamily;
import java.awt.BorderLayout;
import javax.swing.JPanel;

/**
 * Abstract class that serves as a common base for the file presenter objects
 * (FileViewer, FileEditor).
 *
 * @author Arik Hadas
 */
public abstract class FilePresenter extends JPanel {

    /**
     * FileFrame instance that contains this presenter (may be null).
     */
    private FileFrame frame;

    /**
     * File currently being presented.
     */
    private AbstractFile file;
    private JComponent component;

    protected final static String CUSTOM_FULL_SCREEN_EVENT = "CUSTOM_FULL_SCREEN_EVENT";
    private final static String CUSTOM_DISPOSE_EVENT = "CUSTOM_DISPOSE_EVENT";

    public FilePresenter() {
        super(new BorderLayout());
        init();
    }

    private void init() {
        addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
            }

            @Override
            public void focusGained(FocusEvent e) {
                // Delegate the focus to the JComponent that actually present the file
                if (component != null) {
                    component.requestFocus();
                }
            }
        });

        // Catch Apple+W keystrokes under Mac OS X to close the window
        if (OsFamily.MAC_OS.isCurrent()) {
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.META_MASK), CUSTOM_DISPOSE_EVENT);
            getActionMap().put(CUSTOM_DISPOSE_EVENT, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    getFrame().dispose();
                }
            });
        }
    }

    /**
     * Set component to be presented in the ScrollPane viewport
     *
     * @param component the component to be presented
     */
    protected void setComponentToPresent(JComponent component) {
        clearComponentToPresent();
        add(component, BorderLayout.CENTER);
        this.component = component;
        revalidate();
    }

    /**
     * Clears component to be presented in the ScrollPane viewport.
     */
    protected void clearComponentToPresent() {
        if (component != null) {
            remove(component);
            revalidate();
        }
    }

    /**
     * Returns the frame which contains this presenter.
     * <p>
     * This method may return <code>null</code>if the presenter is not inside a
     * FileFrame.
     * </p>
     *
     * @return the frame which contains this presenter.
     * @see #setFrame(FileFrame)
     */
    protected FileFrame getFrame() {
        return frame;
    }

    /**
     * Sets the FileFrame (separate window) that contains this FilePresenter.
     *
     * @param frame frame that contains this <code>FilePresenter</code>.
     * @see #getFrame()
     */
    public void setFrame(FileFrame frame) {
        this.frame = frame;
    }

    /**
     * Returns a description of the file currently being presented which will be
     * used as a window title. This method returns the file's name but it can be
     * overridden to provide more information.
     *
     * @return this dialog's title.
     */
    protected String getTitle() {
        return file.getAbsolutePath();
    }

    /**
     * Returns the file that is being presented.
     *
     * @return the file that is being presented.
     */
    protected AbstractFile getCurrentFile() {
        return file;
    }

    /**
     * Sets the file that is to be presented. This method will automatically be
     * called after a file presenter is created and should not be called
     * directly.
     *
     * @param file file that is to be presented.
     */
    protected final void setCurrentFile(AbstractFile file) {
        this.file = file;
        // Update frame's title
        getFrame().setTitle(getTitle());
    }

    /**
     * Open a given AbstractFile for display.
     *
     * @param file the file to be presented
     * @throws IOException in case of an I/O problem
     */
    public void open(AbstractFile file) throws IOException {
        show(file);
        setCurrentFile(file);
    }

    //////////////////////
    // Abstract methods //
    //////////////////////
    /**
     * This method is invoked when the specified file is about to be opened.
     * This method should retrieve the file and do the necessary so that this
     * component can be displayed.
     *
     * @param file the file that is about to be viewed.
     * @throws IOException if an I/O error occurs.
     */
    protected abstract void show(AbstractFile file) throws IOException;

    /**
     * Returns the menu bar that controls the presenter's frame. The menu bar
     * should be retrieved using this method and not by calling
     * {@link JFrame#getJMenuBar()}, which may return <code>null</code>.
     *
     * @return the menu bar that controls the presenter's frame.
     */
    protected abstract JMenuBar getMenuBar();
}
