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
package org.icepdf.ri.common.views.annotations.signatures;

import org.icepdf.core.pobjects.acroform.SignatureFieldDictionary;
import org.icepdf.core.pobjects.acroform.SignatureHandler;
import org.icepdf.core.pobjects.acroform.signature.SignatureValidator;
import org.icepdf.core.pobjects.annotations.SignatureWidgetAnnotation;
import org.icepdf.ri.common.EscapeJDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * The SignatureValidationDialog shows a summary of the validation status of a signature.  This
 * is very similar to the signature tree view in the Signature utility tab.
 */
public class SignatureValidationDialog extends EscapeJDialog {

    private static final Logger logger =
            Logger.getLogger(SignatureValidationDialog.class.toString());

    private SignatureValidator signatureValidator;
    protected static ResourceBundle messageBundle;
    protected SignatureWidgetAnnotation signatureWidgetAnnotation;

    public SignatureValidationDialog(Frame parent, ResourceBundle messageBundle,
                                     SignatureWidgetAnnotation signatureWidgetAnnotation, SignatureValidator signatureValidator) {
        super(parent, true);
        this.messageBundle = messageBundle;
        this.signatureValidator = signatureValidator;
        this.signatureWidgetAnnotation = signatureWidgetAnnotation;
        buildUI();
    }

    protected void buildUI() {
        SignatureValidationStatus signatureValidationStatus =
                new SignatureValidationStatus(messageBundle, signatureWidgetAnnotation, signatureValidator);

        setTitle(messageBundle.getString("viewer.annotation.signature.validation.dialog.title"));
        // simple close
        final JButton closeButton = new JButton(messageBundle.getString(
                "viewer.annotation.signature.validation.dialog.close.button.label"));
        closeButton.setMnemonic(messageBundle.getString("viewer.button.cancel.mnemonic").charAt(0));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });

        // launch properties dialog showing all signature info.
        final JButton propertiesButton = new JButton(messageBundle.getString(
                "viewer.annotation.signature.validation.dialog.signerProperties.button.label"));
        final Dialog parent = this;
        propertiesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SignatureFieldDictionary fieldDictionary = signatureWidgetAnnotation.getFieldDictionary();
                if (fieldDictionary != null) {
                    SignatureHandler signatureHandler = fieldDictionary.getLibrary().getSignatureHandler();
                    SignatureValidator signatureValidator = signatureHandler.validateSignature(fieldDictionary);
                    if (signatureValidator != null) {
                        new SignaturePropertiesDialog(parent, messageBundle, signatureWidgetAnnotation)
                                .setVisible(true);
                    }
                }
            }
        });

        // put it all together.
        SignatureValidationPanel validityPanel =
                new SignatureValidationPanel(signatureValidationStatus, messageBundle, signatureWidgetAnnotation,
                        signatureValidator, true, false);
        GridBagConstraints constraints = validityPanel.getConstraints();

        constraints.insets = new Insets(15, 5, 5, 5);
        constraints.anchor = GridBagConstraints.WEST;
        validityPanel.addGB(propertiesButton, 0, 5, 1, 1);

        constraints.anchor = GridBagConstraints.EAST;
        validityPanel.addGB(closeButton, 1, 5, 1, 1);

        getContentPane().add(validityPanel);
        pack();
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }


}
