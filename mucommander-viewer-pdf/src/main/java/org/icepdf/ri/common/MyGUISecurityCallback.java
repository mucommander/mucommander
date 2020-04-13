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

import org.icepdf.core.SecurityCallback;
import org.icepdf.core.pobjects.Document;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.util.ResourceBundle;

/**
 * This class is reference code for displaying a password dialog box for
 * encrypted PDF documents.
 *
 * @since 1.1
 */
public class MyGUISecurityCallback implements SecurityCallback {

    private JFrame parentFrame;
    private ResourceBundle messageBundle;

    /**
     * Create a new instance of a SecurityCallback.  This class displays a
     * dialog when the requestPassword method is called to retrieve the document's
     * password from the user.
     *
     * @param frame frame that the dialog will be centered on.
     */
    public MyGUISecurityCallback(JFrame frame, ResourceBundle messageBundle) {
        parentFrame = frame;
        this.messageBundle = messageBundle;
    }

    public String requestPassword(Document document) {
        // get password from dialog
        PasswordDialog passwordDialog = new PasswordDialog(parentFrame);
        // show the dialog in blocking mode
        passwordDialog.setVisible(true);

        // if the dialog was cancelled return null
        if (passwordDialog.isCanceled) {
            return null;
        }
        // otherwise return the password
        else {
            return passwordDialog.getPassword();
        }
    }

    /**
     * Builds a new JDialog which displays a gui for entering a password.
     */
    @SuppressWarnings("serial")
    class PasswordDialog extends EscapeJDialog implements WindowListener {

        // layouts constraint
        private GridBagConstraints constraints;

        // capture password information
        private JPasswordField passwordField;

        // dialog was canceled
        private boolean isCanceled = false;


        /**
         * Creates the permissions dialog.
         */
        public PasswordDialog(JFrame frame) {
            super(frame, true);
            setTitle(messageBundle.getString("viewer.dialog.security.title"));

            // Create GUI elements
            // ok button
            final JButton okButton = new JButton(messageBundle.getString("viewer.dialog.security.okButton.label"));
            okButton.setMnemonic(messageBundle.getString("viewer.dialog.security.okButton.mnemonic").charAt(0));
            okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (e.getSource() == okButton) {
                        setVisible(false);
                        dispose();
                    }

                }
            });

            // cancel button
            final JButton cancelButton = new JButton(messageBundle.getString("viewer.dialog.security.cancelButton.label"));
            cancelButton.setMnemonic(messageBundle.getString("viewer.dialog.security.cancelButton.mnemonic").charAt(0));
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (e.getSource() == cancelButton) {
                        setVisible(false);
                        isCanceled = true;
                        dispose();
                    }

                }
            });
            // setup password field
            passwordField = new JPasswordField(30);
            passwordField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (e.getSource() == passwordField) {
                        setVisible(false);
                        dispose();
                    }

                }
            });
            JLabel msg1 = new JLabel(messageBundle.getString("viewer.dialog.security.msg"));
            JLabel msg2 = new JLabel(messageBundle.getString("viewer.dialog.security.password.label"));

            /**
             * Place GUI elements on dialog
             */

            JPanel passwordPanel = new JPanel();

            passwordPanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
            passwordPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
            GridBagLayout layout = new GridBagLayout();
            passwordPanel.setLayout(layout);
            this.getContentPane().add(passwordPanel);

            constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.NONE;
            constraints.weightx = 1.0;
            constraints.anchor = GridBagConstraints.NORTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = new Insets(1, 10, 1, 1);

            // add components
            addGB(passwordPanel, msg1, 0, 0, 3, 1);

            addGB(passwordPanel, msg2, 0, 1, 1, 1);
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(1, 10, 1, 10);
            addGB(passwordPanel, passwordField, 1, 1, 2, 1);

            constraints.insets = new Insets(10, 1, 1, 1);
            constraints.fill = GridBagConstraints.NONE;
            addGB(passwordPanel, okButton, 1, 2, 1, 1);
            addGB(passwordPanel, cancelButton, 2, 2, 1, 1);

            pack();
            setLocationRelativeTo(frame);
            setResizable(false);
            setSize(306, 150);

            this.addWindowListener(this);
        }

        /**
         * Gets the password string from the dialogs password text field.
         *
         * @return password that was type in the dialog password text field.
         */
        public String getPassword() {
            return new String(passwordField.getPassword());
        }

        /**
         * Has the dialog been cancelled by either the cancel button or
         * by closing the window.
         *
         * @return true if the dialog was closed, true if the OK button was
         * pressed.
         */
        public boolean isCancelled() {
            return isCanceled;
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
                           int colSpan, int rowSpan) {
            constraints.gridx = x;
            constraints.gridy = y;
            constraints.gridwidth = colSpan;
            constraints.gridheight = rowSpan;
            layout.add(component, constraints);
        }

        /**
         * Make sure that the isCanceled flag is set on closing of the window
         *
         * @param ev window closing event
         */
        public void windowClosing(java.awt.event.WindowEvent ev) {
            setVisible(false);
            isCanceled = true;
            dispose();
        }

        // not currently used
        public void windowActivated(java.awt.event.WindowEvent ev) {
        }

        public void windowClosed(java.awt.event.WindowEvent ev) {
        }

        public void windowDeactivated(java.awt.event.WindowEvent ev) {
        }

        public void windowDeiconified(java.awt.event.WindowEvent ev) {
        }

        public void windowIconified(java.awt.event.WindowEvent ev) {
        }

        public void windowOpened(java.awt.event.WindowEvent ev) {
        }

    }
}
