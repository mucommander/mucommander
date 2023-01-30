package com.mucommander.ui.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.util.ui.dialog.DialogToolkit;
import com.mucommander.commons.util.ui.helper.FocusRequester;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.layout.AsyncPanel;
import com.mucommander.ui.main.MainFrame;

/**
 * This class is used as an abstraction for the {@link EditorFrame} and {@link ViewerFrame}.
 * 
 * @author Arik Hadas
 */
public abstract class FileFrame extends JFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileFrame.class);

    private final static Dimension WAIT_DIALOG_SIZE = new Dimension(400, 350);

    // The file presenter within this frame
    private FilePresenter filePresenter;

    // The main frame from which this frame was initiated
    private MainFrame mainFrame;

    FileFrame(MainFrame mainFrame, AbstractFile file, Image icon) {
        this.mainFrame = mainFrame;

        setIconImage(icon);

        // Call #dispose() on close (default is hide)
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        setResizable(true);

        initContentPane(file);
    }

    public MainFrame getMainFrame() {
        return mainFrame;
    }

    protected void initContentPane(final AbstractFile file) {
        try {
            filePresenter = createFilePresenter(file);
        } catch (UserCancelledException e) {
            // May get a UserCancelledException if the user canceled (refused to confirm the operation after a warning)
            return;
        }

        // If not suitable presenter was found for the given file
        if (filePresenter == null) {
            LOGGER.debug("FilePresenter was null i.e. not suitable presenter was found for a given file!");
            showGenericErrorDialog();
            return;
        }

        AsyncPanel asyncPanel = new AsyncPanel() {

            @Override
            public JComponent getTargetComponent() {
                try {
                    // Ask the presenter to present the file
                    filePresenter.open(file);
                } catch(Exception e) {
                    LOGGER.error("Exception caught", e);
                    showGenericErrorDialog();
                    dispose();
                    return filePresenter == null ? new JPanel() : filePresenter;
                }

                setJMenuBar(filePresenter.getMenuBar());

                return filePresenter;
            }

            @Override
            protected void updateLayout() {
                // Request focus on the viewer when it is visible
                FocusRequester.requestFocus(filePresenter);
            }
        };

        // Add the AsyncPanel to the content pane
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(asyncPanel, BorderLayout.CENTER);
        setContentPane(contentPane);

        setSize(WAIT_DIALOG_SIZE);
        DialogToolkit.centerOnWindow(this, mainFrame);

        setVisible(true);
    }

    private void showGenericErrorDialog() {
        InformationDialog.showErrorDialog(mainFrame, getGenericErrorDialogTitle(), getGenericErrorDialogMessage());
    }

    /**
     * Returns whether this frame is set to be displayed in full screen mode
     *
     * @return true if the frame is set to full screen, false otherwise
     */
    public boolean isFullScreen() {
        return (getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
    }

    /**
     * Sets this file presenter to full screen
     */
    public void setFullScreen(boolean on) {
        int currentExtendedState = getExtendedState();
        setExtendedState(
                on ? currentExtendedState | Frame.MAXIMIZED_BOTH : currentExtendedState & ~Frame.MAXIMIZED_BOTH);
    }

    public void setDefaultBounds() {
        DialogToolkit.fitToScreen(this);
        DialogToolkit.fitToMinDimension(this, getMinimumSize());

        DialogToolkit.centerOnWindow(this, mainFrame);
    }

    //////////////////////
    // Abstract methods //
    //////////////////////

    protected abstract String getGenericErrorDialogTitle();

    protected abstract String getGenericErrorDialogMessage();

    protected abstract FilePresenter createFilePresenter(AbstractFile file) throws UserCancelledException;
}
