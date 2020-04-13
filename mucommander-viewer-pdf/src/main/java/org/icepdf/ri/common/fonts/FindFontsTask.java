package org.icepdf.ri.common.fonts;

import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.Reference;
import org.icepdf.core.pobjects.Resources;
import org.icepdf.core.pobjects.fonts.Font;
import org.icepdf.core.util.Library;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingWorker;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * This class is a utility for finding and reporting all font types in a document.  Each page in the document
 * is checked for valid font resources, if found the fonts are added to the calling FontDialog for addition to
 * a JTree of know document fonts.
 *
 * {@link org.icepdf.ri.common.fonts.FontDialog}
 *
 * @since 6.1.3
 */
public class FindFontsTask {
    // total length of task, we keep track of the total number of pages processed.
    private int lengthOfTask;
    // current progress, used for the progress bar
    private int current = 0;
    // message displayed to user of progress made
    private String dialogMessage;
    // canned internationalized messages.
    private MessageFormat searchingMessageForm;

    // flags for threading
    private boolean done = false;
    private boolean canceled = false;

    // parent swing controller
    private SwingController controller;

    // append nodes for found fonts.
    private FontDialog fontDialog;

    private boolean currentlySearching;

    private Container viewContainer;

    /**
     * Creates a new instance of the SearchTextTask.
     *
     * @param fontDialog    parent search panel that start this task via an action
     * @param controller    root controller object
     * @param messageBundle message bundle used for dialog text.
     */
    public FindFontsTask(FontDialog fontDialog,
                         SwingController controller,
                         ResourceBundle messageBundle) {
        this.controller = controller;
        this.fontDialog = fontDialog;
        lengthOfTask = controller.getDocument().getNumberOfPages();
        this.viewContainer = controller.getDocumentViewController().getViewContainer();
        // setup searching format format.
        searchingMessageForm = new MessageFormat(messageBundle.getString("viewer.dialog.fonts.searching.label"));
    }

    /**
     * Start the task, start searching the document for the pattern.
     */
    public void go() {
        final SwingWorker worker = new SwingWorker() {
            public Object construct() {
                current = 0;
                done = false;
                canceled = false;
                dialogMessage = null;
                return new FindFontsTask.ActualTask();
            }
        };
        worker.setThreadPriority(Thread.NORM_PRIORITY);
        worker.start();
    }

    /**
     * Gets the page that is currently being searched by this task.
     *
     * @return current page being processed.
     */
    public int getCurrent() {
        return current;
    }

    /**
     * Stop the task.
     */
    public void stop() {
        canceled = true;
        dialogMessage = null;
    }

    /**
     * Find out if the task has completed.
     *
     * @return true if task is done, false otherwise.
     */
    public boolean isDone() {
        return done;
    }

    public boolean isCurrentlySearching() {
        return currentlySearching;
    }

    /**
     * Returns the most recent dialog message, or null
     * if there is no current dialog message.
     *
     * @return current message dialog text.
     */
    public String getMessage() {
        return dialogMessage;
    }

    /**
     * The actual long running task.  This runs in a SwingWorker thread.
     */
    private class ActualTask {
        ActualTask() {

            try {
                currentlySearching = true;
                current = 0;
                // little cache of fonts by reference so we don't load a font more then once.
                HashMap<Reference, Font> fontCache = new HashMap<Reference, Font>();

                Document document = controller.getDocument();
                // iterate over each page in the document
                for (int i = 0; i < document.getNumberOfPages(); i++) {
                    // break if needed
                    if (canceled || done) {
                        resetDialogMessage();
                        break;
                    }
                    // Update task information
                    current = i;

                    // update search message in results pane.
                    int percent = (int) ((i / (float) lengthOfTask) * 100);
                    Object[] messageArguments = {String.valueOf(percent)};
                    dialogMessage = searchingMessageForm.format(messageArguments);

                    Library library = document.getCatalog().getLibrary();
                    Page page = document.getPageTree().getPage(i);
                    page.initPageResources();
                    Resources pageResources = page.getResources();
                    if (pageResources != null) {
                        HashMap pageFonts = pageResources.getFonts();
                        if (pageFonts != null && pageFonts.size() > 0) {
                            Set fontKeys = pageFonts.keySet();
                            for (Object fontObjectReference : fontKeys) {
                                Object fontObject = pageFonts.get(fontObjectReference);
                                if (fontObject instanceof Reference) {
                                    Reference fontReference = (Reference) fontObject;
                                    // check if we already have this font
                                    if (!fontCache.containsKey(fontReference)) {
                                        fontObject = library.getObject(fontReference);
                                        if (fontObject instanceof org.icepdf.core.pobjects.fonts.Font) {
                                            final Font font = (Font) fontObject;
                                            font.init();
                                            fontCache.put(fontReference, font);
                                            SwingUtilities.invokeLater(new Runnable() {
                                                public void run() {
                                                    // add the node
                                                    fontDialog.addFoundEntry(font);
                                                    // try repainting the container
                                                    fontDialog.expandAllNodes();
                                                    viewContainer.repaint();
                                                }
                                            });
                                        }
                                    }
                                }
                            }

                        }
                    }
                    Thread.yield();
                }
                // update the dialog and end the task
                resetDialogMessage();

                done = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                currentlySearching = false;
            }

            // repaint the view container
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    viewContainer.validate();
                }
            });
        }
    }


    /**
     * Utility method for setting the dialog message.
     */
    private void resetDialogMessage() {
        dialogMessage = "";
    }
}
