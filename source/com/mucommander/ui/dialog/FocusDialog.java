/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

import com.mucommander.PlatformManager;
import com.mucommander.ui.helper.FocusRequester;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


/**
 * FocusDialog is a modal dialog which extends JDialog to provide the following additional functionalities :
 * <ul>
 *   <li>focus can be requested on a specified JComponent once the dialog has been made visible</li>
 *   <li>the screen location of the window can be set relatively to a Component specified in the constructor</li>
 *   <li>a minimum and/or maximum size can be specified and will be used by {@link #pack()} to calculate the effective dialog size</li>
 *   <li>by default, the 'Escape' key disposes the dialog, this can be disabled using {@link #setKeyboardDisposalEnabled(boolean)}</li>
 * </ul>
 * @author Maxence Bernard
 */
public class FocusDialog extends JDialog implements WindowListener {

    private JComponent initialFocusComponent;
    private Dimension maximumDimension;
    private Dimension minimumDimension;

    private boolean firstTimeActivated;
	
    private Component locationRelativeComp;

    private boolean keyboardDisposalEnabled = true;

    private final static String CUSTOM_DISPOSE_EVENT = "CUSTOM_DISPOSE_EVENT";

    
    public FocusDialog(Frame owner, String title, Component locationRelativeComp) {
        super(owner, title, true);
        init(owner, locationRelativeComp);
    }

    public FocusDialog(Dialog owner, String title, Component locationRelativeComp) {
        super(owner, title, true);
        init(owner, locationRelativeComp);
    }


    private void init(Window owner, Component locationRelativeComp) {
        this.locationRelativeComp = locationRelativeComp;
        setLocationRelativeTo(locationRelativeComp);

        JPanel contentPane = (JPanel)getContentPane();
        contentPane.setBorder(new EmptyBorder(6, 8, 6, 8));
        setResizable(true);

        // Important: dispose (release resources) window on close, default is HIDE_ON_CLOSE
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	
        // Catch escape key presses and have them close the dialog by mapping the escape keystroke to a custom dispose Action
        InputMap inputMap = contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = contentPane.getActionMap();
        AbstractAction disposeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e){
                if(keyboardDisposalEnabled)
                    cancel();
            }
        };
	
        // Maps the dispose action to the 'Escape' keystroke
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CUSTOM_DISPOSE_EVENT);
        actionMap.put(CUSTOM_DISPOSE_EVENT, disposeAction);
		
        // Maps the dispose action to the 'Apple+W' keystroke under Mac OS X
        if(PlatformManager.getOsFamily()==com.mucommander.PlatformManager.MAC_OS_X)
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.META_MASK), CUSTOM_DISPOSE_EVENT);

        // Under Windows, Alt+F4 automagically disposes the dialog, nothing to do
    }

    /**
     * Method called when the user has canceled through the escape key.
     * <p>
     * This method is equivalent to a call to {@link #dispose()}. It's meant to be
     * overriden by those implementations of <code>FocusDialog</code> that need to run
     * code before canceling the dialog.
     * </p>
     */
    public void cancel() {dispose();}


    /**
     * Sets the component that will receive focus once this dialog has been made visible.
     *
     * @param initialFocusComponent the component that will receive focus once this dialog has been made visible, if
     * null, the first component in the dialog will receive focus.
     */
    public void setInitialFocusComponent(JComponent initialFocusComponent) {
        this.initialFocusComponent = initialFocusComponent;
        addWindowListener(this);
    }
	
	
    /**
     * Sets a maximum width and height for this dialog.
     */
    public void setMaximumSize(Dimension dimension) {
        this.maximumDimension = dimension;
    }

    /**
     * Sets a minium width and height for this dialog.
     */
    public void setMinimumSize(Dimension dimension) {
        this.minimumDimension = dimension;
    }


    /**
     * Specifies whether this dialog can be automatically disposed using the 'Escape' key and 'Apple+W' under Mac OS X.
     * If enabled, {@link #dispose()} will be called when one of those keystrokes is pressed from any component
     * within this dialog.
     *
     * @param enabled true to enable automatic keyboard disposal, false to disable it
     */
    public void setKeyboardDisposalEnabled(boolean enabled) {
        this.keyboardDisposalEnabled = enabled;
    }


    /**
     * Overrides Window.pack() to take into account minimum and maximum dialog size (if specified).
     */
    public void pack()  {
        super.pack();
        if(maximumDimension!=null)
            DialogToolkit.fitToMaxDimension(this, maximumDimension);
        else
            DialogToolkit.fitToScreen(this);
		
        if(minimumDimension!=null)
            DialogToolkit.fitToMinDimension(this, minimumDimension);
    }

	
    /**
     * Packs this dialog, makes it non-resizable and visible.
     */
    public void showDialog() {
        pack();

        if(locationRelativeComp==null)
            DialogToolkit.centerOnScreen(this);
        else
            setLocation(locationRelativeComp.getX()+(locationRelativeComp.getWidth()-getWidth())/2, locationRelativeComp.getY()+(locationRelativeComp.getHeight()-getHeight())/2);
        setVisible(true);
    }

    /**
     * Return <code>true</code> if the dialog has been activated (see WindowListener.windowActivated()).
     */
    public boolean isActivated() {
        return firstTimeActivated;
    }


    ////////////////////////////
    // WindowListener methods //
    ////////////////////////////

    public void windowOpened(WindowEvent e) {
        // Requests focus first time the dialog is activated
        // (this method is called first time the dialog is made visible)
        if (!firstTimeActivated && initialFocusComponent!=null) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("requesting focus on initial focus component");

            FocusRequester.requestFocus(initialFocusComponent);
            firstTimeActivated = true;
        }
    }

    public void windowActivated(WindowEvent e) {
        // (this method is called each time the dialog is activated)
    }

    public void windowClosing(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }
}
