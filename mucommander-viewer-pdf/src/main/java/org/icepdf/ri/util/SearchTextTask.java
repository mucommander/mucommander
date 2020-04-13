/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.ri.util;

import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.graphics.text.LineText;
import org.icepdf.core.search.DocumentSearchController;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingWorker;
import org.icepdf.ri.common.utility.search.SearchPanel;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

/**
 * This class is a utility for searching text in a PDF document.  This is only
 * a reference implementation; there is currently no support for regular
 * expression and other advanced search features.
 *
 * @since 1.1
 */
public class SearchTextTask {

    // total length of task (total page count), used for progress bar
    private int lengthOfTask;
    // current progress, used for the progress bar
    private int current = 0;
    // message displayed on progress bar
    private String dialogMessage;
    // canned internationalized messages.
    private MessageFormat searchingMessageForm;
    private MessageFormat searchResultMessageForm;
    private MessageFormat searchCompletionMessageForm;
    // flags for threading
    private boolean done = false;
    private boolean canceled = false;
    // keep track of total hits
    private int totalHitCount = 0;
    // String to search for and parameters from gui
    private String pattern = "";
    private boolean wholeWord;
    private boolean caseSensitive;
    private boolean cumulative;
    private boolean showPages;
    private boolean r2L;

    // parent swing controller
    SwingController controller;

    // append nodes for found text.
    private SearchPanel searchPanel;

    // message bundle for internationalization
    private ResourceBundle messageBundle;

    private boolean currentlySearching;

    private Container viewContainer;

    /**
     * Creates a new instance of the SearchTextTask.
     *
     * @param searchPanel   parent search panel that start this task via an action
     * @param controller    root controller object
     * @param pattern       pattern to search for
     * @param wholeWord     ture inticates whole word search
     * @param caseSensitive case sensitive indicates cases sensitive search
     * @param r2L           right left earch, not currently implemented.
     * @param messageBundle message bundle used for dialog text.
     */
    public SearchTextTask(SearchPanel searchPanel,
                          SwingController controller,
                          String pattern,
                          boolean wholeWord,
                          boolean caseSensitive,
                          boolean cumulative,
                          boolean showPages,
                          boolean r2L,
                          ResourceBundle messageBundle) {
        this.controller = controller;
        this.pattern = pattern;
        this.searchPanel = searchPanel;
        lengthOfTask = controller.getDocument().getNumberOfPages();
        this.messageBundle = messageBundle;
        this.viewContainer = controller.getDocumentViewController().getViewContainer();
        this.wholeWord = wholeWord;
        this.caseSensitive = caseSensitive;
        this.cumulative = cumulative;
        this.showPages = showPages;
        this.r2L = r2L;

        // setup searching format format.
        if (searchPanel != null) {
            searchingMessageForm = searchPanel.setupSearchingMessageForm();
            searchResultMessageForm = searchPanel.setupSearchResultMessageForm();
            searchCompletionMessageForm = searchPanel.setupSearchCompletionMessageForm();
        }
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
                return new ActualTask();
            }
        };
        worker.setThreadPriority(Thread.NORM_PRIORITY);
        worker.start();
    }

    /**
     * Number pages that search task has to iterate over.
     *
     * @return returns max number of pages in document being search.
     */
    public int getLengthOfTask() {
        return lengthOfTask;
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
    class ActualTask {
        ActualTask() {

            // break on bad input
            if ("".equals(pattern) || " ".equals(pattern)) {
                return;
            }

            try {
                currentlySearching = true;
                // Extraction of text from pdf procedure
                totalHitCount = 0;
                current = 0;

                // get instance of the search controller
                DocumentSearchController searchController =
                        controller.getDocumentSearchController();
                if (!cumulative) {
                    searchController.clearAllSearchHighlight();
                }
                searchController.addSearchTerm(pattern,
                        caseSensitive, wholeWord);

                Document document = controller.getDocument();
                // iterate over each page in the document
                for (int i = 0; i < document.getNumberOfPages(); i++) {
                    // break if needed
                    if (canceled || done) {
                        setDialogMessage();
                        break;
                    }
                    // Update task information
                    current = i;

                    // update search message in search pane.
                    Object[] messageArguments = {String.valueOf((current + 1)),
                            lengthOfTask, lengthOfTask};
                    dialogMessage = searchingMessageForm.format(messageArguments);

                    // hits per page count
                    final List<LineText> lineItems =
                            searchController.searchHighlightPage(current, 6);
                    int hitCount = lineItems.size();

                    // update total hit count
                    totalHitCount += hitCount;
                    if (hitCount > 0) {
                        // update search dialog
                        messageArguments = new Object[]{
                                String.valueOf((current + 1)),
                                hitCount, hitCount};
                        final String nodeText =
                                searchResultMessageForm.format(messageArguments);
                        final int currentPage = i;
                        // add the node to the search panel tree but on the
                        // awt thread.
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                // add the node
                                searchPanel.addFoundEntry(
                                        nodeText,
                                        currentPage,
                                        lineItems,
                                        showPages);
                                // try repainting the container
                                viewContainer.repaint();
                            }
                        });
                    }
                    Thread.yield();
                }
                // update the dialog and end the task
                setDialogMessage();

                done = true;
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
     * Gets the message that should be displayed when the task has completed.
     *
     * @return search completed or stoped final message.
     */
    public String getFinalMessage() {
        setDialogMessage();
        return dialogMessage;
    }

    /**
     * Utility method for setting the dialog message.
     */
    private void setDialogMessage() {

        // Build Internationalized plural phrase.

        Object[] messageArguments = {String.valueOf((current + 1)),
                (current + 1), totalHitCount};

        dialogMessage = searchCompletionMessageForm.format(messageArguments);
    }
}
