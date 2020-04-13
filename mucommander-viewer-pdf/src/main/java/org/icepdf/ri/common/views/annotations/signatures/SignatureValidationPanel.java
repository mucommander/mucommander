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
 * SignatureValidationPanel shows a summary of the the validation results.
 */
public class SignatureValidationPanel extends JPanel {
    // layouts constraint
    private GridBagConstraints constraints;


    public SignatureValidationPanel(SignatureValidationStatus signatureValidationStatus, ResourceBundle messageBundle,
                                    SignatureWidgetAnnotation signatureWidgetAnnotation, SignatureValidator signatureValidator,
                                    boolean showIcon, boolean showBorder) {
        if (showBorder) {
            setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                    messageBundle.getString("viewer.annotation.signature.properties.dialog.validity.title"),
                    TitledBorder.LEFT,
                    TitledBorder.DEFAULT_POSITION));
        }

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
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets(5, 5, 5, 5);
        if (showIcon) {
            addGB(validityIconLabel, 0, 0, 1, 4);
        }

        constraints.anchor = GridBagConstraints.WEST;
        addGB(new JLabel(signatureValidationStatus.getValidity()), 1, 0, 1, 1);
        addGB(new JLabel(signatureValidationStatus.getSingedBy()), 1, 1, 1, 1);
        addGB(new JLabel(signatureValidationStatus.getDocumentModified()), 1, 2, 1, 1);
        addGB(new JLabel(signatureValidationStatus.getCertificateTrusted()), 1, 3, 1, 1);
        addGB(new JLabel(signatureValidationStatus.getSignatureTime()), 1, 4, 1, 1);

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
