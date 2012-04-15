/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.dialog;

import com.mucommander.text.Translator;
import com.mucommander.ui.button.CollapseExpandButton;
import com.mucommander.ui.layout.InformationPane;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.text.FontUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This class provides static methods to display 'information' dialogs of different kinds:
 * {@link #ERROR_DIALOG_TYPE error}, {@link #INFORMATION_DIALOG_TYPE information}, {@link #WARNING_DIALOG_TYPE warning}
 * or {@link #QUESTION_DIALOG_TYPE question}.
 * <p>
 * While this class is very similar to {@link JOptionPane}, it extends the functionality by adding optional caption
 * message and exception details to the dialog. It also allows to use generic title and messages for certain
 * dialog types.
 * </p>
 * <p>
 * This class uses {@link InformationPane} to display the icon and the main and caption messages.
 * </p>
 *
 * @see InformationPane
 * @author Maxence Bernard
 */
public class InformationDialog {

    /** Minimum dialog size */
    private static final Dimension MIN_DIALOG_SIZE = new Dimension(360, 0);

    /** Maximum dialog size */
    private static final Dimension MAX_DIALOG_SIZE = new Dimension(480, 10000);

    /** Error dialog type */
    public static final int ERROR_DIALOG_TYPE = 1;

    /** Information dialog type */
    public static final int INFORMATION_DIALOG_TYPE = 2;

    /** Warning dialog type */
    public static final int WARNING_DIALOG_TYPE = 3;

    /** Question dialog type */
    public static final int QUESTION_DIALOG_TYPE = 4;


    /**
     * Brings up an error dialog with a generic localized title and message.
     *
     * @param parentComponent determines the <code>Frame</code> in which the dialog is displayed; if <code>null</code>,
     */
    public static void showErrorDialog(Component parentComponent) {
        showErrorDialog(parentComponent, null, null, null, null);
    }

    /**
     * Brings up an error dialog with the specified message and a generic localized title.
     *
     * @param parentComponent determines the <code>Frame</code> in which the dialog is displayed; if <code>null</code>,
     * or if the parentComponent has no <code>Frame</code>, a default <code>Frame</code> is used
     * @param message the error message to display in the dialog
     */
    public static void showErrorDialog(Component parentComponent, String message) {
        showErrorDialog(parentComponent, null, message, null, null);
    }

    /**
     * Brings up an error dialog with the specified title and message.
     *
     * @param parentComponent determines the <code>Frame</code> in which the dialog is displayed; if <code>null</code>,
     * or if the parentComponent has no <code>Frame</code>, a default <code>Frame</code> is used
     * @param title the dialog's title, <code>null</code> for a generic localized title.
     * @param message the error message to display in the dialog, <code>null</code> for a generic localized message.
     */
    public static void showErrorDialog(Component parentComponent, String title, String message) {
        showErrorDialog(parentComponent, title, message, null, null);
    }

    /**
     * Brings up an error dialog with the specified title, main and caption messages.
     *
     * @param parentComponent determines the <code>Frame</code> in which the dialog is displayed; if <code>null</code>,
     * or if the parentComponent has no <code>Frame</code>, a default <code>Frame</code> is used
     * @param title the dialog's title, <code>null</code> for a generic localized title.
     * @param message the error message to display in the dialog, <code>null</code> for a generic localized message.
     * @param captionMessage the caption message to display underneath the error message, <code>null</code> for none.
     */
    public static void showErrorDialog(Component parentComponent, String title, String message, String captionMessage) {
        showErrorDialog(parentComponent, title, message, captionMessage, null);
    }

    /**
     * Brings up an error dialog with the specified title, main and caption messages, and stack trace of the specified
     * exception inside an expandable panel.
     *
     * @param parentComponent determines the <code>Frame</code> in which the dialog is displayed; if <code>null</code>,
     * or if the parentComponent has no <code>Frame</code>, a default <code>Frame</code> is used
     * @param title the dialog's title, <code>null</code> for a generic localized title.
     * @param message the error message to display in the dialog, <code>null</code> for a generic localized message.
     * @param captionMessage the caption message to display underneath the error message, <code>null</code> for none.
     * @param throwable exception for which to show the stack trace, <code>null</code> for none.
     */
    public static void showErrorDialog(Component parentComponent, String title, String message, String captionMessage, Throwable throwable) {
        showDialog(ERROR_DIALOG_TYPE, parentComponent, title==null?Translator.get("error"):title, message==null?Translator.get("generic_error"):message, captionMessage, throwable);
    }

   /**
     * Brings up a warning dialog with the specified message and a generic localized title.
     *
     * @param parentComponent determines the <code>Frame</code> in which the dialog is displayed; if <code>null</code>,
     * or if the parentComponent has no <code>Frame</code>, a default <code>Frame</code> is used
     * @param message the main message to display in the dialog
     */
    public static void showWarningDialog(Component parentComponent, String message) {
        showWarningDialog(parentComponent, null, message, null);
    }

    /**
     * Brings up a warning dialog with the specified title and message.
     *
     * @param parentComponent determines the <code>Frame</code> in which the dialog is displayed; if <code>null</code>,
     * or if the parentComponent has no <code>Frame</code>, a default <code>Frame</code> is used
     * @param title the dialog's title, <code>null</code> for a generic localized title.
     * @param message the main message to display in the dialog.
     */
    public static void showWarningDialog(Component parentComponent, String title, String message) {
        showWarningDialog(parentComponent, title, message, null);
    }

    /**
     * Brings up a warning dialog with the specified title, main and caption messages.
     *
     * @param parentComponent determines the <code>Frame</code> in which the dialog is displayed; if <code>null</code>,
     * or if the parentComponent has no <code>Frame</code>, a default <code>Frame</code> is used
     * @param title the dialog's title, <code>null</code> for a generic localized title.
     * @param message the main message to display in the dialog, <code>null</code> for a generic localized message.
     * @param captionMessage the caption message to display underneath the main message, <code>null</code> for none.
     */
    public static void showWarningDialog(Component parentComponent, String title, String message, String captionMessage) {
        showDialog(WARNING_DIALOG_TYPE, parentComponent, title==null?Translator.get("warning"):title, message, captionMessage, null);
    }

    /**
     * Brings up a dialog of the specified type and with the specified title, main and caption messages, and stack trace
     * of the specified exception inside an expandable panel.
     *
     * @param dialogType type of dialog, see constant fields for allow values.
     * @param parentComponent determines the <code>Frame</code> in which the dialog is displayed; if <code>null</code>,
     * or if the parentComponent has no <code>Frame</code>, a default <code>Frame</code> is used
     * @param title the dialog's title, <code>null</code> for a generic localized title, if one exists for the
     * dialog type.
     * @param message the main message to display in the dialog, <code>null</code> for a generic localized message, if
     * one exists for the dialog type.
     * @param captionMessage the caption message to display underneath the main message, <code>null</code> for none.
     * @param throwable exception for which to show the stack trace, <code>null</code> for none.
     */
    public static void showDialog(int dialogType, Component parentComponent, String title, String message, String captionMessage, Throwable throwable) {
        Window owner = DialogToolkit.getWindowForComponent(parentComponent);

        final FocusDialog dialog;
        if(owner instanceof Frame)
            dialog = new FocusDialog((Frame)owner, title, parentComponent);
        else
            dialog = new FocusDialog((Dialog)owner, title, parentComponent);

        dialog.setMinimumSize(MIN_DIALOG_SIZE);
        dialog.setMaximumSize(MAX_DIALOG_SIZE);

        YBoxPanel mainPanel = new YBoxPanel();

        InformationPane informationPane = new InformationPane(message, captionMessage, captionMessage==null?Font.PLAIN:Font.BOLD, getInformationPaneIconId(dialogType));
        mainPanel.add(informationPane);
        mainPanel.addSpace(10);

        JButton okButton = new JButton(Translator.get("ok"));
        JPanel okPanel = DialogToolkit.createOKPanel(okButton, dialog.getRootPane(), new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        mainPanel.add(buttonPanel);

        // Show the exception's stack trace in an expandable/collapsible panel
        if(throwable !=null) {
            JTextArea detailsArea = new JTextArea();
            detailsArea.setEditable(false);

            // Get the stack trace as a string
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw, true);
            throwable.printStackTrace(pw);
            pw.close();
            // Fill the area with the stack trace.
            // Tabs by space characters to reduce the text's width
            detailsArea.setText(sw.toString().replace('\t', ' '));

            FontUtils.makeMini(detailsArea);

            JScrollPane scrollPane = new JScrollPane(detailsArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            buttonPanel.add(new CollapseExpandButton(Translator.get("details"), scrollPane, false));
            mainPanel.add(scrollPane);
        }

        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(okPanel);
        
        dialog.getContentPane().add(mainPanel);

        // Give initial keyboard focus to the 'OK' button
        dialog.setInitialFocusComponent(okButton);

        // Call dispose() when dialog is closed
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        dialog.showDialog();
    }

    /**
     * Returns an {@link InformationPane} icon id corresponding to the given dialog type.
     *
     * @param dialogType type of dialog, see constant fields for allow values.
     * @return an {@link InformationPane} icon id corresponding to the given dialog type.
     */
    private static int getInformationPaneIconId(int dialogType) {
        int iconId;
        switch(dialogType) {
            case ERROR_DIALOG_TYPE:
                iconId = InformationPane.ERROR_ICON;
                break;
            case INFORMATION_DIALOG_TYPE:
                iconId = InformationPane.INFORMATION_ICON;
                break;
            case WARNING_DIALOG_TYPE:
                iconId = InformationPane.WARNING_ICON;
                break;
            case QUESTION_DIALOG_TYPE:
                iconId = InformationPane.QUESTION_ICON;
                break;
            default:
                iconId = InformationPane.ERROR_ICON;
                break;
        }

        return iconId;
    }
}
