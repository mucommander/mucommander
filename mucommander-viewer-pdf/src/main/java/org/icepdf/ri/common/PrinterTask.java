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

import javax.print.CancelablePrintJob;
import javax.print.PrintException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>The <code>PrinterTask</code> class is responsible for starting a
 * PrinterJob's print function in a new thread.  This class assumes that the
 * PrinterJob is pre-configured and ready for its print() method to be called.</p>
 *
 * @since 2.0
 */
public class PrinterTask implements Runnable {

    private static final Logger logger =
            Logger.getLogger(PrinterTask.class.toString());

    // PrinterJob to print
    private PrintHelper printHelper;
    private CancelablePrintJob cancelablePrintJob;

    /**
     * Create a new instance of a PrinterTask.
     *
     * @param printHelper print helper
     */
    public PrinterTask(PrintHelper printHelper) {
        this.printHelper = printHelper;
    }

    /**
     * Threads Runnable method.
     */
    public void run() {
        try {
            if (printHelper != null) {
                cancelablePrintJob = printHelper.cancelablePrint();
            }
        } catch (PrintException ex) {
            logger.log(Level.FINE, "Error during printing.", ex);
        }
    }

    /**
     * Cancel the PrinterTask by calling the PrinterJob's cancel() method.
     */
    public void cancel() {
        try {
            if (cancelablePrintJob != null) {
                cancelablePrintJob.cancel();
            }
        } catch (PrintException ex) {
            logger.log(Level.FINE, "Error during printing, " + ex.getMessage());
        }
    }
}
