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

import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.PInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

/**
 * This class is a reference implementation for displaying a PDF file's
 * document information
 *
 * @since 1.1
 */
@SuppressWarnings("serial")
public class DocumentInformationDialog extends EscapeJDialog {

    // layouts constraint
    private GridBagConstraints constraints;


    /**
     * Creates the document information  dialog.
     */
    public DocumentInformationDialog(JFrame frame, Document document,
                                     ResourceBundle messageBundle) {
        super(frame, true);
        setTitle(messageBundle.getString("viewer.dialog.documentInformation.title"));

        // Do some work on information to get display values
        String title = "";
        String author = "";
        String subject = "";
        String keyWords = "";
        String creator = "";
        String producer = "";
        String creationDate = "";
        String modDate = "";

        // get duplicate names from message bundle
        String notAvailable =
                messageBundle.getString("viewer.dialog.documentInformation.notAvailable");

        // get information values if available
        PInfo documentInfo = document.getInfo();
        if (documentInfo != null) {
            title = documentInfo.getTitle();
            author = documentInfo.getAuthor();
            subject = documentInfo.getSubject();
            keyWords = documentInfo.getKeywords();
            creator = documentInfo.getCreator() != null ?
                    documentInfo.getCreator() : notAvailable;
            producer = documentInfo.getProducer() != null ?
                    documentInfo.getProducer() : notAvailable;
            creationDate = documentInfo.getCreationDate() != null ?
                    documentInfo.getCreationDate().toString() : notAvailable;
            modDate = documentInfo.getModDate() != null ?
                    documentInfo.getModDate().toString() : notAvailable;
        }

        // Create GUI elements
        final JButton okButton = new JButton(messageBundle.getString("viewer.button.ok.label"));
        okButton.setMnemonic(messageBundle.getString("viewer.button.ok.mnemonic").charAt(0));
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == okButton) {
                    setVisible(false);
                    dispose();
                }

            }
        });

        /**
         * Place GUI elements on dialog
         */

        JPanel permissionsPanel = new JPanel();

        permissionsPanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
        GridBagLayout layout = new GridBagLayout();
        permissionsPanel.setLayout(layout);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 1.0;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets(5, 5, 5, 5);

        // add labels
        addGB(permissionsPanel, new JLabel(
                        messageBundle.getString("viewer.dialog.documentInformation.title.label")),
                0, 0, 1, 1);
        addGB(permissionsPanel, new JLabel(
                        messageBundle.getString("viewer.dialog.documentInformation.subject.label")),
                0, 1, 1, 1);
        addGB(permissionsPanel, new JLabel(
                        messageBundle.getString("viewer.dialog.documentInformation.author.label")),
                0, 2, 1, 1);
        addGB(permissionsPanel, new JLabel(
                        messageBundle.getString("viewer.dialog.documentInformation.keywords.label")),
                0, 3, 1, 1);
        addGB(permissionsPanel, new JLabel(
                        messageBundle.getString("viewer.dialog.documentInformation.creator.label")),
                0, 4, 1, 1);
        addGB(permissionsPanel, new JLabel(
                        messageBundle.getString("viewer.dialog.documentInformation.producer.label")),
                0, 5, 1, 1);
        addGB(permissionsPanel, new JLabel(
                        messageBundle.getString("viewer.dialog.documentInformation.created.label")),
                0, 6, 1, 1);
        addGB(permissionsPanel, new JLabel(
                        messageBundle.getString("viewer.dialog.documentInformation.modified.label")),
                0, 7, 1, 1);
        constraints.insets = new Insets(15, 5, 5, 5);
        constraints.anchor = GridBagConstraints.CENTER;
        addGB(permissionsPanel, okButton, 0, 8, 2, 1);

        // add values
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.anchor = GridBagConstraints.WEST;
        addGB(permissionsPanel, new JLabel(title), 1, 0, 1, 1);
        addGB(permissionsPanel, new JLabel(subject), 1, 1, 1, 1);
        addGB(permissionsPanel, new JLabel(author), 1, 2, 1, 1);
        addGB(permissionsPanel, new JLabel(keyWords), 1, 3, 1, 1);
        addGB(permissionsPanel, new JLabel(creator), 1, 4, 1, 1);
        addGB(permissionsPanel, new JLabel(producer), 1, 5, 1, 1);
        addGB(permissionsPanel, new JLabel(creationDate), 1, 6, 1, 1);
        addGB(permissionsPanel, new JLabel(modDate), 1, 7, 1, 1);

        this.getContentPane().add(permissionsPanel);

        pack();
        setLocationRelativeTo(frame);
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
    private void addGB(JPanel layout, Component component,
                       int x, int y,
                       int rowSpan, int colSpan) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = rowSpan;
        constraints.gridheight = colSpan;
        layout.add(component, constraints);
    }
}
