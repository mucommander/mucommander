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

import org.icepdf.core.pobjects.PDate;
import org.icepdf.core.pobjects.acroform.signature.SignatureValidator;
import org.icepdf.core.pobjects.annotations.SignatureWidgetAnnotation;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Simple panel that shows a summary of signature data.
 */
public class SignerSummaryPanel extends JPanel {
    // layouts constraint
    private GridBagConstraints constraints;

    public SignerSummaryPanel(SignatureValidationStatus signatureValidationStatus, ResourceBundle messageBundle,
                              SignatureWidgetAnnotation signatureWidgetAnnotation, SignatureValidator signatureValidator,
                              boolean showIcon) {

        String validity = signatureValidationStatus.getValidity();
        MessageFormat formatter = new MessageFormat(messageBundle.getString(
                "viewer.annotation.signature.properties.dialog.signingTime.label"));
        String signingDate = signatureValidationStatus.getDictionaryDate();
        signingDate = formatter.format(new Object[]{
                new PDate(signatureWidgetAnnotation.getLibrary().getSecurityManager(), signingDate).toString()});
        formatter.applyPattern(messageBundle.getString("viewer.annotation.signature.properties.dialog.reason.label"));
        String reason = formatter.format(new Object[]{signatureValidationStatus.getDictionaryReason()});
        formatter.applyPattern(messageBundle.getString("viewer.annotation.signature.properties.dialog.location.label"));
        String location = formatter.format(new Object[]{signatureValidationStatus.getDictionaryLocation()});

        // get the respective image.
        JLabel validityIconLabel = new JLabel(new ImageIcon(signatureValidationStatus.getValidityIconPath()));

        // put it all together.
        setAlignmentY(JPanel.TOP_ALIGNMENT);
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 1.0;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 5, 5);
        if (showIcon) {
            addGB(validityIconLabel, 0, 0, 1, 4);
        }

        constraints.anchor = GridBagConstraints.WEST;
        addGB(new JLabel(validity), 1, 0, 1, 1);
        addGB(new JLabel(signingDate), 1, 1, 1, 1);
        addGB(new JLabel(reason), 1, 2, 1, 1);
        addGB(new JLabel(location), 1, 3, 1, 1);
        ;

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
