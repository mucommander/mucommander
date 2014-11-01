/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import com.mucommander.job.ui.DialogResult;
import com.mucommander.ui.button.ButtonChoicePanel;
import com.mucommander.ui.layout.InformationPane;
import com.mucommander.ui.layout.YBoxPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * 
 *
 * @author Maxence Bernard
 */
public class QuestionDialog extends FocusDialog implements ActionListener, DialogResult {
	
    /** Dialog owner */
    private JButton buttons[];
    private int actionValues[];
	
    private int retValue = DIALOG_DISPOSED_ACTION;

    private YBoxPanel mainPanel;

    /** This value is returned by {@link #getActionValue()} when the dialog has been disposed without the user
     * selecting a custom action */
    public final static int DIALOG_DISPOSED_ACTION = -1;

    /** Minimum dialog size */
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(360, 0);

    /** Maximum dialog size */
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(480, 10000);


    /**
     *
     * @param actionValues values for actions, each of them must be >= 0
     */
    public QuestionDialog(Frame owner, String title, String msg, Component locationRelative, String actionText[], int actionValues[], int maxNbCols) {
        super(owner, title, locationRelative);
        init(new InformationPane(msg, null, Font.PLAIN, InformationPane.QUESTION_ICON), actionText, actionValues, maxNbCols);
    }

    /**
     *
     * @param actionValues values for actions, each of them must be >= 0
     */
    public QuestionDialog(Dialog owner, String title, String msg, Component locationRelative, String actionText[], int actionValues[], int maxNbCols) {
        super(owner, title, locationRelative);
        init(new InformationPane(msg, null, Font.PLAIN, InformationPane.QUESTION_ICON), actionText, actionValues, maxNbCols);
    }

    /**
     *
     * @param actionValues values for actions, each of them must be >= 0
     */
    public QuestionDialog(Frame owner, String title, Component msgComp, Component locationRelative, String actionText[], int actionValues[], int maxNbCols) {
        super(owner, title, locationRelative);
        init(msgComp, actionText, actionValues, maxNbCols);
    }

    /**
     *
     * @param actionValues values for actions, each of them must be >= 0
     */
    public QuestionDialog(Dialog owner, String title, Component msgComp, Component locationRelative, String actionText[], int actionValues[], int maxNbCols) {
        super(owner, title, locationRelative);
        init(msgComp, actionText, actionValues, maxNbCols);
    }

	
    protected QuestionDialog(Frame owner, String title, Component locationRelative) {
        super(owner, title, locationRelative);
    }

    protected QuestionDialog(Dialog owner, String title, Component locationRelative) {
        super(owner, title, locationRelative);
    }
	
	
    protected void init(Component comp, String actionText[], int actionValues[], int maxNbCols) {
        this.actionValues = actionValues;

        // Sets minimum and maximum dimensions for this dialog
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);

        mainPanel = new YBoxPanel();

        if(comp!=null) {
            mainPanel.addSpace(5);
            mainPanel.add(comp);
            mainPanel.addSpace(10);
        }
		
        int nbButtons = actionText.length;
        buttons = new JButton[nbButtons];
        String text;
		
        for(int i=0; i<nbButtons; i++) {
            text = actionText[i];

            buttons[i] = new JButton(text);
            buttons[i].addActionListener(this);
        }
		
        setInitialFocusComponent(buttons[0]);
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
    public int getActionValue() {
//        // Beep !
//        Toolkit.getDefaultToolkit().beep();
        // Returns only when this dialog has been disposed
        // by actionPerformed or if window has been closed (-1)
        super.showDialog();
        return retValue;
    }


    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
		
        for(int i=0; i<buttons.length; i++)
            if (buttons[i]==source) {
                retValue = actionValues[i];
                break;
            }
	
        dispose();
    }

    public Object getUserInput() {
        super.showDialog();
        return retValue;
    }
}
