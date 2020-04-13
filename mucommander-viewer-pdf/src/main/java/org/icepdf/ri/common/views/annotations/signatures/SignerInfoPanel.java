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

import org.icepdf.core.pobjects.acroform.signature.SignatureValidator;
import org.icepdf.core.pobjects.annotations.SignatureWidgetAnnotation;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * Displays a summary info of the signer properties.
 */
public class SignerInfoPanel extends JPanel {
    // layouts constraint
    private GridBagConstraints constraints;


    public SignerInfoPanel(SignatureValidationStatus signatureValidationStatus, ResourceBundle messageBundle,
                           SignatureWidgetAnnotation signatureWidgetAnnotation, SignatureValidator signatureValidator) {

        setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                messageBundle.getString("viewer.annotation.signature.properties.dialog.signerInfo.title"),
                TitledBorder.LEFT,
                TitledBorder.DEFAULT_POSITION));

        // put it all together.
        setAlignmentY(JPanel.TOP_ALIGNMENT);
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 1.0;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets(5, 5, 5, 5);

        String validationMessage = "viewer.annotation.signature.properties.dialog.pathValidation.failure";
        if (signatureValidator.isCertificateChainTrusted()) {
            validationMessage = "viewer.annotation.signature.properties.dialog.pathValidation.success";
        }
        validationMessage = messageBundle.getString(validationMessage);
        String revocationsMessage = "viewer.annotation.signature.properties.dialog.revocation.success";
        if (!signatureValidator.isCertificateChainTrusted() || signatureValidator.isRevocation()) {
            revocationsMessage = "viewer.annotation.signature.properties.dialog.revocation.failure";
        }
        revocationsMessage = messageBundle.getString(revocationsMessage);
        String expiryMessage = null;
        if (!signatureValidator.isCertificateDateValid()) {
            expiryMessage = messageBundle.getString("viewer.annotation.signature.properties.dialog.certificateExpired.failure");
        }
        constraints.anchor = GridBagConstraints.WEST;
        addGB(new JLabel(validationMessage), 1, 0, 1, 1);
        addGB(new JLabel(revocationsMessage), 1, 1, 1, 1);
        if (!signatureValidator.isCertificateDateValid()) {
            addGB(new JLabel(expiryMessage), 1, 2, 1, 1);
        }
    }

    /**
     * Gridbag constructor helper
     *
     * @param component component to add to grid
     * @param x         row
     * @param y         col
     * @param rowSpan
     * @param colSpan
     */
    public void addGB(Component component,
                      int x, int y,
                      int rowSpan, int colSpan) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = rowSpan;
        constraints.gridheight = colSpan;
        this.add(component, constraints);
    }

    public GridBagConstraints getConstraints() {
        return constraints;
    }
}