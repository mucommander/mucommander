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

import javax.print.DocPrintJob;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;

/**
 * Simple Print Job Watcher.
 *
 * @since 3.0
 */
public class PrintJobWatcher {
    // true if it is safe to close the print job's input stream
    private boolean done = false;

    public PrintJobWatcher() {

    }

    public PrintJobWatcher(DocPrintJob job) {
        setPrintJob(job);
    }

    public void setPrintJob(DocPrintJob job) {
        // Add a listener to the print job
        job.addPrintJobListener(
                new PrintJobAdapter() {
                    public void printJobCanceled(PrintJobEvent printJobEvent) {
                        allDone();
                    }

                    public void printJobCompleted(PrintJobEvent printJobEvent) {
                        allDone();
                    }

                    public void printJobFailed(PrintJobEvent printJobEvent) {
                        allDone();
                    }

                    public void printJobNoMoreEvents(PrintJobEvent printJobEvent) {
                        allDone();
                    }

                    void allDone() {
                        synchronized (PrintJobWatcher.this) {
                            done = true;
                            PrintJobWatcher.this.notify();
                        }
                    }
                });
    }

    public synchronized void waitForDone() {
        try {
            while (!done) {
                wait();
            }
        } catch (InterruptedException e) {
        }
    }
}

