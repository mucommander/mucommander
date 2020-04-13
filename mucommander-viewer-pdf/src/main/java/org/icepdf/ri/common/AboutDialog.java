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
import org.icepdf.ri.images.Images;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.util.ResourceBundle;

@SuppressWarnings("serial")
public class AboutDialog extends EscapeJDialog implements ActionListener, WindowListener {


    private JButton ok;
    private Timer timer;
    private int whichTimer;

    private static final int WAIT_TIME = 3000; //time to wait in milliseconds
    private static final String IMAGE = "icelogo.png"; //image to include in the dialog

    public static final int NO_BUTTONS = 0;
    public static final int OK = 2;
    public static final int NO_TIMER = 0;
    public static final int DISAPPEAR = 4;

    public AboutDialog(Frame frame, ResourceBundle messageBundle, boolean isModal,
                       int buttons, int whichTimer) {
        super(frame, isModal);
        this.whichTimer = whichTimer;

        // Show OK button instead of using display timer
        buttons = OK;
        whichTimer = NO_TIMER;

        setTitle(messageBundle.getString("viewer.dialog.about.title"));
        setResizable(false);

        JPanel panelImage = new javax.swing.JPanel();
        ImageIcon icon = new ImageIcon(Images.get(IMAGE));
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setBorder(BorderFactory.createEmptyBorder());
        panelImage.add(iconLabel);

        JLabel label;

        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
        panel1.add(Box.createVerticalStrut(10));
        panel1.add(panelImage);

        panel1.add(Box.createVerticalStrut(30));
        label = new JLabel(Document.getLibraryVersion());
        label.setAlignmentX(0.5f);
        panel1.add(label);


        String text = messageBundle.getString("viewer.dialog.about.pageNumber.label");
        int c2 = 0, c1;
        while ((c1 = text.indexOf("\n", c2)) > -1) {
            panel1.add(Box.createVerticalStrut(10));
            label = new JLabel(text.substring(c2, c1));
            label.setAlignmentX(0.5f);
            panel1.add(label);
            c2 = c1 + 1;
        }
        panel1.add(Box.createVerticalStrut(10));
        label = new JLabel(text.substring(c2, text.length()));
        label.setAlignmentX(0.5f);
        panel1.add(label);

        //insets: needs swing component in order to set the Insets
        JPanel pane = new JPanel();
        pane.setBorder(new EmptyBorder(5, 15, 5, 15));

        pane.setLayout(new BorderLayout());
        pane.add(panel1);

        if (buttons > NO_BUTTONS) {
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            if ((buttons & OK) > 0) {
                ok = new JButton(messageBundle.getString("viewer.button.ok.label"));
                ok.addActionListener(this);
                if (whichTimer > 0) {
                    ok.setEnabled(false);
                }
                panel2.add(ok);
            }
            pane.add(panel2, BorderLayout.SOUTH);
        }

        setContentPane(pane);

        pack();
        setLocationRelativeTo(frame);

        if (whichTimer > 0) {
            timer = new Timer(WAIT_TIME, this);
            timer.start();
        }
    }

    public void actionPerformed(ActionEvent ev) {
        if (ev.getSource() == timer) {
            timer.stop();
            if (whichTimer == OK) {
                ok.setEnabled(true);
            } else if (whichTimer == DISAPPEAR) {
                setVisible(false);
                dispose();
            }
        } else if (ev.getSource() == ok) {
            setVisible(false);
            dispose();
        }
    }

    public void windowClosing(java.awt.event.WindowEvent ev) {
        if (ok.isEnabled()) {
            setVisible(false);
            dispose();
        }
    }

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

