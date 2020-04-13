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
package org.icepdf.ri.common;

import org.icepdf.ri.util.TextExtractionTask;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Acts as glue between the TextExtractionTask and ProgressMonitor.
 *
 * @author Mark Collette
 * @since 2.0
 */
public class TextExtractionGlue implements ActionListener {
    private TextExtractionTask textExtractionTask;
    private ProgressMonitor progressMonitor;
    private Timer timer;

    TextExtractionGlue(TextExtractionTask task, ProgressMonitor prog) {
        textExtractionTask = task;
        progressMonitor = prog;
    }

    void setTimer(Timer t) {
        timer = t;
    }

    public void actionPerformed(ActionEvent evt) {
        // Update progressMonitor progress
        progressMonitor.setProgress(textExtractionTask.getCurrent());
        String s = textExtractionTask.getMessage();

        if (s != null) {
            progressMonitor.setNote(s);
        }

        if (progressMonitor.isCanceled() || textExtractionTask.isDone()) {
            progressMonitor.close();
            textExtractionTask.stop();
            Toolkit.getDefaultToolkit().beep();
            timer.stop();
        }
    }
}
