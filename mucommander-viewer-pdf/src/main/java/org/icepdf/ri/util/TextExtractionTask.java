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
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.graphics.text.LineText;
import org.icepdf.core.pobjects.graphics.text.WordText;
import org.icepdf.ri.common.SwingWorker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.ChoiceFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is a utility for extracting text from a PDF document.
 *
 * @since 1.1
 */
public class TextExtractionTask {

    private static final Logger logger =
            Logger.getLogger(TextExtractionTask.class.toString());

    // total length of task (total page count), used for progress bar
    private int lengthOfTask;

    // current progress, used for the progress bar
    private int current = 0;

    // message displayed on progress bar
    private String dialogMessage;

    // flags for threading
    private boolean done = false;
    private boolean canceled = false;

    // internationalization
    private ResourceBundle messageBundle;

    // PDF document pointer
    private Document document = null;

    // File used for text export
    private File file = null;

    /**
     * Create a new instance of the TextExtraction object.
     *
     * @param document document whose text will be extracted.
     * @param file     output file for extracted text.
     */
    public TextExtractionTask(Document document, File file, ResourceBundle messageBundle) {
        this.document = document;
        this.file = file;
        lengthOfTask = document.getNumberOfPages();
        this.messageBundle = messageBundle;
    }

    /**
     * Start the task,  created a new SwingWorker for the text extraction
     * process.
     */
    public void go() {
        final SwingWorker worker = new SwingWorker() {
            // reset all instance variables
            public Object construct() {
                current = 0;
                done = false;
                canceled = false;
                dialogMessage = null;
                return new ActualTask();
            }
        };
        worker.setThreadPriority(Thread.MIN_PRIORITY);
        worker.start();
    }

    /**
     * Find out how much work needs to be done.
     */
    public int getLengthOfTask() {
        return lengthOfTask;
    }

    /**
     * Find out how much has been done.
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
     */
    public boolean isDone() {
        return done;
    }

    /**
     * Returns the most recent dialog message, or null
     * if there is no current dialog message.
     */
    public String getMessage() {
        return dialogMessage;
    }

    /**
     * The actual long running task.  This runs in a SwingWorker thread.
     */
    class ActualTask {
        ActualTask() {
            // Extraction of text from pdf procedure
            try {
                // create file output stream
                BufferedWriter fileOutputStream = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
                // Print document information
                String pageNumber =
                        messageBundle.getString("viewer.exportText.fileStamp.msg");

                fileOutputStream.write(pageNumber);
                fileOutputStream.write(10); // line break

                for (int i = 0; i < document.getNumberOfPages(); i++) {
                    // break if needed
                    if (canceled || done) {
                        break;
                    }

                    // Update task information
                    current = i;

                    // Build Internationalized plural phrase.
                    MessageFormat messageForm =
                            new MessageFormat(messageBundle.getString(
                                    "viewer.exportText.fileStamp.progress.msg"));
                    double[] fileLimits = {0, 1, 2};
                    String[] fileStrings = {
                            messageBundle.getString(
                                    "viewer.exportText.fileStamp.progress.moreFile.msg"),
                            messageBundle.getString(
                                    "viewer.exportText.fileStamp.progress.oneFile.msg"),
                            messageBundle.getString(
                                    "viewer.exportText.fileStamp.progress.moreFile.msg"),
                    };
                    ChoiceFormat choiceForm = new ChoiceFormat(fileLimits,
                            fileStrings);
                    Format[] formats = {null, choiceForm, null};
                    messageForm.setFormats(formats);
                    Object[] messageArguments = {String.valueOf((current + 1)),
                            lengthOfTask, lengthOfTask};

                    dialogMessage = messageForm.format(messageArguments);

                    messageForm =
                            new MessageFormat(messageBundle.getString(
                                    "viewer.exportText.pageStamp.msg"));
                    messageArguments = new Object[]{String.valueOf((current + 1))};

                    pageNumber = messageForm.format(messageArguments);

                    fileOutputStream.write(pageNumber);
                    fileOutputStream.write(10); // line break

                    Page page = document.getPageTree().getPage(i);
                    List<LineText> pageLines;
                    if (page.isInitiated()) {
                        // get a pages already initialized text.
                        pageLines = document.getPageViewText(i).getPageLines();
                    } else {
                        // grap the text the fastest way possible.
                        pageLines = document.getPageText(i).getPageLines();
                    }
                    StringBuilder extractedText;
                    for (LineText lineText : pageLines) {
                        extractedText = new StringBuilder();
                        for (WordText wordText : lineText.getWords()) {
                            extractedText.append(wordText.getText());
                        }
                        extractedText.append('\n');
                        fileOutputStream.write(extractedText.toString());
                    }

                    Thread.yield();

                }

                done = true;
                current = 0;
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (Throwable e) {
                logger.log(Level.FINE, "Malformed URL Exception ", e);
            }
        }
    }
}
