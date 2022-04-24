/*
 * This file is part of muCommander, http://www.mucommander.com
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;

import com.mucommander.commons.util.ui.button.ButtonChoicePanel;
import com.mucommander.commons.util.ui.dialog.FocusDialog;
import com.mucommander.commons.util.ui.layout.YBoxPanel;
import com.mucommander.job.ui.DialogResult;
import com.mucommander.ui.layout.InformationPane;


/**
 * A generic dialog box issued to users if muCommander is in doubt how to continue i.e.
 * dialog boxes that ask questions or require confirmations.
 *
 * @author Maxence Bernard
 */
public class QuestionDialog extends FocusDialog implements DialogResult {

    /**
     * This value is returned by {@link #getActionValue()} when the dialog has been disposed without the user
     * selecting a custom action
     */
    public static final DialogAction DIALOG_DISPOSED_ACTION = null;

    /**
     * Dialog owner
     */
    private final List<JButton> buttons = new ArrayList<>();
    private final List<DialogAction> actionChoices = new ArrayList<>();


    private DialogAction retValue = DIALOG_DISPOSED_ACTION;

    private YBoxPanel mainPanel;


    /**
     * Minimum dialog size
     */
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(360, 0);

    /**
     * Maximum dialog size
     */
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(480, 10000);


    public QuestionDialog(Frame owner, String title, String msg, Component locationRelative, List<DialogAction> actionChoices, int maxNbCols) {
        super(owner, title, locationRelative);
        init(new InformationPane(msg, null, Font.PLAIN, InformationPane.QUESTION_ICON), actionChoices, maxNbCols);
    }

    public QuestionDialog(Dialog owner, String title, String msg, Component locationRelative, List<DialogAction> actionChoices, int maxNbCols) {
        super(owner, title, locationRelative);
        init(new InformationPane(msg, null, Font.PLAIN, InformationPane.QUESTION_ICON), actionChoices, maxNbCols);
    }

    public QuestionDialog(Frame owner, String title, Component msgComp, Component locationRelative, List<DialogAction> actionChoices, int maxNbCols) {
        super(owner, title, locationRelative);
        init(msgComp, actionChoices, maxNbCols);
    }

    public QuestionDialog(Dialog owner, String title, Component msgComp, Component locationRelative, List<DialogAction> actionChoices, int maxNbCols) {
        super(owner, title, locationRelative);
        init(msgComp, actionChoices, maxNbCols);
    }


    protected QuestionDialog(Frame owner, String title, Component locationRelative) {
        super(owner, title, locationRelative);
    }

    protected QuestionDialog(Dialog owner, String title, Component locationRelative) {
        super(owner, title, locationRelative);
    }


    protected void init(Component comp, List<DialogAction> actionChoices, int maxNbCols) {
        this.actionChoices.addAll(actionChoices);

        // Sets minimum and maximum dimensions for this dialog
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);

        mainPanel = new YBoxPanel();

        if (comp != null) {
            mainPanel.addSpace(5);
            mainPanel.add(comp);
            mainPanel.addSpace(10);
        }

        for (DialogAction action : actionChoices) {
            JButton button = new JButton(action.getActionName());
            button.setToolTipText(action.getActionName());
            button.addActionListener((ActionEvent event) -> {
                retValue = action;
                dispose();
            });
            buttons.add(button);
        }

        if (!buttons.isEmpty()) {
            setInitialFocusComponent(buttons.get(0));
        }
        mainPanel.add(new ButtonChoicePanel(buttons, maxNbCols, getRootPane()));

        getContentPane().add(mainPanel, BorderLayout.NORTH);
    }

    /**
     * Adds a component to this dialog, under the buttons panel.
     *
     * @param comp the component to add
     */
    protected void addComponent(JComponent comp) {
        mainPanel.add(comp);
    }


    /**
     * Shows this dialog, waits for an action/button to be selected and returns the selected action's value.
     * The dialog may be closed without the user selecting a custom action. In this case,
     * {@link #DIALOG_DISPOSED_ACTION} (-1) will be returned.
     */
    public DialogAction getActionValue() {
//        // Beep !
//        Toolkit.getDefaultToolkit().beep();
        // Returns only when this dialog has been disposed
        // by actionPerformed or if window has been closed (-1)
        super.showDialog();
        return retValue;
    }

    public Object getUserInput() {
        super.showDialog();
        return retValue;
    }
}
