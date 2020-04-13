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
package org.icepdf.ri.common.utility.signatures;

import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.acroform.InteractiveForm;
import org.icepdf.core.pobjects.acroform.SignatureDictionary;
import org.icepdf.core.pobjects.acroform.signature.exceptions.SignatureIntegrityException;
import org.icepdf.core.pobjects.annotations.SignatureWidgetAnnotation;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingWorker;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is a utility for verifying signature annotations off the AWT thread.  The are two main sub classes
 * VerifyAllSignatures and VerifySignature.  The RI uses both of these methods for validating all signatures and
 * refreshing an individual signature annotation state.
 */
public class SigVerificationTask {

    private static final Logger logger =
            Logger.getLogger(SigVerificationTask.class.toString());

    // total number of signatures to process.
    private int lengthOfTask;
    // current progress, used for the progress bar
    private int current = 0;
    // message displayed on progress bar
    private String dialogMessage;
    // flags for threading
    private boolean done = false;
    private boolean canceled = false;

    // parent swing controller
    SwingController controller;

    // append nodes for found text.
    private SignaturesPanel signaturesPanel;

    // message bundle for internationalization
    private ResourceBundle messageBundle;

    private boolean currentlyVerifying;

    private Container viewContainer;

    /**
     * Creates a new instance of the SigVerificationTask.
     *
     * @param signaturesPanel parent signature panel that start this task via an action
     * @param controller      root controller object
     * @param messageBundle   message bundle used for dialog text.
     */
    public SigVerificationTask(SignaturesPanel signaturesPanel,
                               SwingController controller,
                               ResourceBundle messageBundle) {
        this.controller = controller;
        this.signaturesPanel = signaturesPanel;
        lengthOfTask = controller.getDocument().getCatalog().getInteractiveForm().getSignatureFields().size();
        this.messageBundle = messageBundle;
        this.viewContainer = controller.getDocumentViewController().getViewContainer();
    }

    /**
     * Start the task, start verifying all the signatures annotations.
     */
    public void verifyAllSignatures() {
        final SwingWorker worker = new SwingWorker() {
            public Object construct() {
                current = 0;
                done = false;
                canceled = false;
                dialogMessage = null;
                return new VerifyAllSignatures();
            }
        };
        worker.setThreadPriority(Thread.NORM_PRIORITY);
        worker.start();
    }

    /**
     * Start the task, verify the specified signature annotation.
     */
    public void verifySignature(final SignatureWidgetAnnotation signatureWidgetAnnotation,
                                final SignatureTreeNode signatureTreeNode) {
        final SwingWorker worker = new SwingWorker() {
            public Object construct() {
                current = 0;
                done = false;
                canceled = false;
                dialogMessage = null;
                return new VerifySignature(signatureWidgetAnnotation, signatureTreeNode);
            }
        };
        worker.setThreadPriority(Thread.NORM_PRIORITY);
        worker.start();
    }

    /**
     * Number of signatures that has tobe validated.
     *
     * @return returns max number of signatures that need validation.
     */
    public int getLengthOfTask() {
        return lengthOfTask;
    }

    /**
     * Gets the signature number that is currently being validated by this task.
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

    public boolean isCurrentlyVerifying() {
        return currentlyVerifying;
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
     * Verify all signatures defined by the parent task.
     */
    class VerifyAllSignatures {
        VerifyAllSignatures() {
            currentlyVerifying = true;
            MessageFormat messageFormat = new MessageFormat(
                    messageBundle.getString("viewer.utilityPane.signatures.verify.initializingMessage.label"));
            try {
                current = 0;
                try {
                    Document document = controller.getDocument();
                    InteractiveForm interactiveForm = document.getCatalog().getInteractiveForm();
                    // checks and flags each annotation to indicate if the signatures cover the whole document
                    interactiveForm.isSignaturesCoverDocumentLength();
                    final ArrayList<SignatureWidgetAnnotation> signatures = interactiveForm.getSignatureFields();
                    boolean unsignedFields = false;
                    // build out the tree
                    if (signatures.size() > 0) {
                        // iterate over the signature in the document.
                        for (int i = 0, max = signatures.size(); i < max; i++) {
                            // break if needed
                            if (canceled || done) {
                                break;
                            }
                            // Update task information
                            current = i;

                            dialogMessage = messageFormat.format(new Object[]{i + 1, signatures.size()});

                            final SignatureWidgetAnnotation signatureWidgetAnnotation = signatures.get(i);
                            SignatureDictionary signatureDictionary = signatureWidgetAnnotation.getSignatureDictionary();
                            if (signatureDictionary.getEntries().size() > 0) {
                                try {
                                    signatureWidgetAnnotation.getSignatureValidator().validate();
                                    signatureWidgetAnnotation.getSignatureValidator().isSignaturesCoverDocumentLength();
                                    // add a new node to the tree.
                                } catch (SignatureIntegrityException e) {
                                    logger.log(Level.WARNING, "Error verifying signature.", e);
                                }
                                // add the node to the signature panel tree but on the
                                // awt thread.
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        // add the node
                                        signaturesPanel.addSignature(signatureWidgetAnnotation);
                                        // try repainting the container
                                        viewContainer.repaint();
                                    }
                                });
                            } else {
                                // found some unsigned fields.
                                unsignedFields = true;
                            }
                            Thread.yield();
                        }
                        // build out unsigned fields
                        if (unsignedFields) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    signaturesPanel.addUnsignedSignatures(signatures);
                                    viewContainer.repaint();
                                }
                            });
                        }
                    }
                    // update the dialog and end the task
                    dialogMessage = messageBundle.getString("viewer.utilityPane.signatures.verify.completeMessage.label");
                    done = true;
                } catch (Exception e) {
                    logger.log(Level.FINER, "Error verifying signatures.", e);
                }
            } finally {
                currentlyVerifying = false;
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
     * Verify the signature specified in the constructor.
     */
    class VerifySignature {
        /**
         * Verifies the given signature and update the respective tree node.
         *
         * @param signatureWidgetAnnotation annotation to verify
         * @param signatureTreeNode         node to update for display of new validation info.
         */
        VerifySignature(final SignatureWidgetAnnotation signatureWidgetAnnotation,
                        final SignatureTreeNode signatureTreeNode) {
            try {
                currentlyVerifying = true;
                current = 0;
                try {
                    dialogMessage = messageBundle.getString("viewer.utilityPane.signatures.verify.validating.label");
                    signaturesPanel.updateSignature(signatureWidgetAnnotation, signatureTreeNode);
                    // add the node to the signature panel tree but on the
                    // awt thread.
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            // add the node
                            signaturesPanel.showSignatureValidationDialog(signatureWidgetAnnotation);
                            // try repainting the container
                            viewContainer.repaint();
                        }
                    });
                    // update the dialog and end the task
                    dialogMessage = messageBundle.getString("viewer.utilityPane.signatures.verify.completeMessage.label");
                    done = true;
                } catch (Exception e) {
                    logger.log(Level.FINER, "Error verifying signature.", e);
                }
            } finally {
                currentlyVerifying = false;
            }
            // repaint the view container
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    viewContainer.validate();
                }
            });
        }
    }
}
