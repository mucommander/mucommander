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

package com.mucommander.ui.action;

import com.mucommander.commons.file.util.ResourceLoader;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Hashtable;
import java.util.Map;

/**
 * MuAction extends <code>AbstractAction</code> to add more functionalities and make it easier to integrate within
 * muCommander. The biggest difference with <code>AbstractAction</code> is that MuAction instances are bound to a
 * specific {@link MainFrame}.<br>
 * Note that by being an Action, MuAction can be used in every Swing components that accept Action instances.
 *
 * <p>The MuAction class is abstract. MuAction subclasses must implement the {@link #performAction()} method
 * to provide a response to the action trigger, and must provide a constructor with the
 * {@link #MuAction(MainFrame, Map)} signature.
 *
 * <p>MuAction subclasses should not be instantiated directly, {@link ActionManager}'s <code>getActionInstance</code>
 * methods should be used instead. Using {@link ActionManager} to retrieve a MuAction ensures that only one instance
 * exists for a given {@link com.mucommander.ui.main.MainFrame}. This is particularly important because actions are stateful and can be used
 * in several components of a MainFrame at the same time; if an action's state changes, the change must be reflected
 * everywhere the action is used. It is also important for performance reasons: sharing one action throughout a
 * {@link MainFrame} saves some memory and also CPU cycles as some actions listen to particular events to change
 * their state accordingly.
 *
 * @see ActionManager
 * @see ActionKeymap
 * @author Maxence Bernard
 */
public abstract class MuAction extends AbstractAction {

    /** The MainFrame associated with this MuAction */
    protected MainFrame mainFrame;

    /** if true, action events are ignored while the MainFrame is in 'no events mode'. Enabled by default. */
    private boolean honourNoEventsMode = true;

    /** if true, #performAction() is called from a separate thread (and not from the event thread) when this action is
     * performed. Disabled by default. */
    private boolean performActionInSeparateThread = false;

    /** Name of the alternate accelerator KeyStroke property */
    public final static String ALTERNATE_ACCELERATOR_PROPERTY_KEY = "alternate_accelerator";
    
    /**
     * Creates a new <code>MuAction</code> associated with the specified {@link MainFrame}. The properties contained by
     * the given {@link Hashtable} are used to initialize this action's property map.
     *
     * @param mainFrame the MainFrame to associate with this new MuAction
     * @param properties the initial properties to use in this action. The Hashtable may simply be empty if no initial
     * properties are specified.
     */
    public MuAction(MainFrame mainFrame, Map<String,Object> properties) {
        this.mainFrame = mainFrame;
        
        // Add properties to this Action.
        for(String key : properties.keySet())
            putValue(key, properties.get(key));
    }

    /**
     * Return the {@link MainFrame} this MuAction is associated.
     *
     * @return the MainFrame this action is associated with
     */
    public MainFrame getMainFrame() {
        return this.mainFrame;
    }

    /**
     * Returns the label of this action, <code>null</code> if this action has no label.
     * The label value is stored in the {@link #NAME} property.
     *
     * @return the label of this action, <code>null</code> if this action has no label
     */
    public String getLabel() {
        return (String)getValue(Action.NAME);
    }

    /**
     * Sets the label for this action, <code>null</code> for no label.
     * The label value is stored in the {@link #NAME} property.
     *
     * @param label the new text label for this action, replacing the previous one (if any)
     */
    public void setLabel(String label) {
        putValue(Action.NAME, label);
    }


    /**
     * Returns the tooltip text of this action, <code>null</code> if this action has no tooltip.
     * The tooltip value is stored in the {@link #SHORT_DESCRIPTION} property.
     *
     * @return the tooltip text of this action, <code>null</code> if this action has no tooltip
     */
    public String getToolTipText() {
        return (String)getValue(Action.SHORT_DESCRIPTION);
    }

    /**
     * Sets the tooltip for this action, <code>null</code> for no tooltip.
     * The tooltip value is stored in the {@link #SHORT_DESCRIPTION} property.
     *
     * @param toolTipText the new tooltip text for this action replacing the previous one (if any)
     */
    public void setToolTipText(String toolTipText) {
        putValue(Action.SHORT_DESCRIPTION, toolTipText);
    }


    /**
     * Return the icon of this action, <code>null</code> if this action has no icon.
     * The icon value is stored in the {@link #SMALL_ICON} property.
     *
     * @return the icon of this action, <code>null</code> if this action has no icon
     */
    public ImageIcon getIcon() {
        return (ImageIcon)getValue(Action.SMALL_ICON);
    }

    /**
     * Sets the icon for this action, <code>null</code> if this action has no icon.
     * The icon value is stored in the {@link #SMALL_ICON} property.
     *
     * @param icon the new image icon for this action, replacing the previous one (if any)
     */
    public void setIcon(ImageIcon icon) {
        putValue(Action.SMALL_ICON, icon);
    }


    /**
     * Returns the accelerator KeyStroke of this action, <code>null</code> if this action has no accelerator.
     * The accelerator value is stored in the <code>Action.ACCELERATOR_KEY</code> property.
     *
     * @return the accelerator KeyStroke of this action, <code>null</code> if this action has no accelerator
     */
    public KeyStroke getAccelerator() {
        return (KeyStroke)getValue(Action.ACCELERATOR_KEY);
    }

    /**
     * Sets the accelerator KeyStroke for this action, <code>null</code> for no accelerator.
     * The tooltip value is stored in the <code>Action.ACCELERATOR_KEY</code> property.
     *
     * @param keyStroke the new accelerator KeyStroke for this action, replacing the previous one (if any)
     */
    public void setAccelerator(KeyStroke keyStroke) {
        putValue(Action.ACCELERATOR_KEY, keyStroke);
    }


    /**
     * Returns the alternate accelerator KeyStroke of this action, <code>null</code> if it doesn't have any.
     * The accelerator accelerator value is stored in the {@link #ALTERNATE_ACCELERATOR_PROPERTY_KEY} property.
     *
     * @return the alternate accelerator KeyStroke of this action, <code>null</code> if it doesn't have any
     */
    public KeyStroke getAlternateAccelerator() {
        return (KeyStroke)getValue(ALTERNATE_ACCELERATOR_PROPERTY_KEY);
    }

    /**
     * Sets the alternate accelerator KeyStroke for this action, <code>null</code> for none.
     * The accelerator accelerator value is stored in the {@link #ALTERNATE_ACCELERATOR_PROPERTY_KEY} property.
     *
     * @param keyStroke the new alternate accelerator KeyStroke for this action, replacing the previous one (if any)
     */
    public void setAlternateAccelerator(KeyStroke keyStroke) {
        putValue(ALTERNATE_ACCELERATOR_PROPERTY_KEY, keyStroke);
    }

    /**
     * Returns <code>true</code> if both keystrokes' {@link KeyStroke#getKeyChar() char},
     * {@link KeyStroke#getKeyCode() code} and {@link KeyStroke#getModifiers() modifiers} are equal.
     * Unlike {@link KeyStroke#equals(Object)}, this method does not take into account the
     * {@link KeyStroke#isOnKeyRelease() onKeyRelease} flag.
     *
     * @param ks1 first keystroke to test
     * @param ks2 second keystroke to test
     * @return <code>true</code> if both keystrokes' char, code and modifiers are equal
     */
    protected boolean acceleratorsEqual(KeyStroke ks1, KeyStroke ks2) {
        return ks1.getKeyChar()==ks2.getKeyChar()
            && ks1.getKeyCode()==ks2.getKeyCode()
            && ks1.getModifiers()==ks2.getModifiers();
    }

    /**
     * Returns <code>true</code> if the given KeyStroke is one of this action's accelerators. Keystrokes are compared
     * using {@link #acceleratorsEqual(KeyStroke, KeyStroke)}, so that the {@link KeyStroke#isOnKeyRelease()} flag
     * is not taken into account. This method always returns <code>false</code> if this method has no accelerator.
     *
     * @param keyStroke the KeyStroke to test against this action's accelerators
     * @return true if the given KeyStroke is one of this action's accelerators
     */
    public boolean isAccelerator(KeyStroke keyStroke) {
        KeyStroke accelerator = getAccelerator();
        if(accelerator!=null && acceleratorsEqual(accelerator, keyStroke))
            return true;

        accelerator = getAlternateAccelerator();
        return accelerator!=null && acceleratorsEqual(accelerator, keyStroke);
    }


    /**
     * Returns a displayable String representation of this action's accelerator, in the
     * <code>[modifier]+[modifier]+...+key</code> format.
     * This method returns <code>null</code> if this action has no accelerator.
     *
     * @return a String representation of the accelerator, or <code>null</code> if this action has no accelerator.
     */
    public String getAcceleratorText() {
        KeyStroke accelerator = getAccelerator();
        if(accelerator==null)
            return null;

        String text = KeyEvent.getKeyText(accelerator.getKeyCode());
        int modifiers = accelerator.getModifiers();
        if(modifiers!=0)
            text = KeyEvent.getKeyModifiersText(modifiers)+"+"+text;

        return text;
    }


    /**
     * Return <code>true</code> if action events are ignored while the <code>MainFrame</code> associated with this
     * action is in 'no events mode' (see {@link MainFrame} for an explanation about this mode).
     * By default, this method returns <code>true</code>.
     *
     * @return <code>true</code> if action events are ignored while the <code>MainFrame</code> associated with this
     * action is in 'no events' mode
     */
    public boolean honourNoEventsMode() {
        return honourNoEventsMode;
    }

    /**
     * Sets whether action events are to be ignored while the <code>MainFrame</code> associated with this action is in
     * 'no events mode' (see {@link MainFrame} for an explanation about this mode).
     * By default (unless this method has been called), 'no events mode' is honoured.
     *
     * @param honourNoEventsMode if true, actions events will be ignored while the <code>MainFrame</code> associated
     * with this action is in 'no events mode'
     */
    public void setHonourNoEventsMode(boolean honourNoEventsMode) {
        this.honourNoEventsMode = honourNoEventsMode;
    }


    /**
     * Returns <code>true</code> if {@link #performAction()} is called from a separate thread (and not from the event
     * thread) when this action is performed. By default, <code>false</code> is returned, i.e. actions are performed
     * from the main event thread.
     *
     * <p>Actions that have the potential to hold the caller thread for a substantial amount of time should perform the
     * action in a separate thread, to avoid locking the event thread.</p>
     *
     * @return <code>true</code> if {@link #performAction()} is called from a separate thread (and not from the event
     * thread) when this action is performed
     */
    public boolean performActionInSeparateThread() {
        return performActionInSeparateThread;
    }

    /**
     * Sets whether {@link #performAction()} is called from a separate thread (and not from the event thread) when this
     * action is performed. By default (unless this method has been called), actions are performed from the main event
     * thread.
     *
     * <p>Actions that have the potential to hold the caller thread for a substantial amount of time should perform the
     * action in a separate thread, to avoid locking the event thread.</p>
     *
     * @param performActionInSeparateThread <code>true</code> to have {@link #performAction()} called from a separate
     * thread (and not from the event thread) when this action is performed
     */
    public void setPerformActionInSeparateThread(boolean performActionInSeparateThread) {
        this.performActionInSeparateThread = performActionInSeparateThread;
    }

    /**
     * Shorthand for {@link #getStandardIcon(Class)} called with the Class instance returned by {@link #getClass()}.
     *
     * @return the standard icon corresponding to this MuAction class, <code>null</code> if none was found
     */
    public ImageIcon getStandardIcon() {
        return getStandardIcon(getClass());
    }

    /**
     * Shorthand for {@link #getStandardIconPath(Class)} called with the Class instance returned by {@link #getClass()}.
     *
     * @return the standard path for this action's image icon
     */
    public String getStandardIconPath() {
        return getStandardIconPath(getClass());
    }


    ////////////////////
    // Static methods //
    ////////////////////

    /**
     * Queries {@link IconManager} for an image icon corresponding to the specified action using standard icon path
     * conventions. Returns the image icon, <code>null</code> if none was found.
     *
     * @param action a MuAction class descriptor
     * @return the standard icon image corresponding to the specified MuAction class, <code>null</code> if none was found
     */
    public static ImageIcon getStandardIcon(Class<? extends MuAction> action) {
        // Look for an icon image file with the /action/<classname>.png path and use it if it exists
    	String iconPath = getStandardIconPath(action);
        if(ResourceLoader.getResourceAsURL(iconPath) == null)
            return null;
        return IconManager.getIcon(iconPath);
    }

    /**
     * Returns the standard path to the icon image for the specified {@link MuAction} class. The returned path is
     * relative to the application's JAR file.
     *
     * @param action a MuAction class descriptor
     * @return the standard path to the icon image corresponding to the specified MuAction class
     */
    public static String getStandardIconPath(Class<? extends MuAction> action) {
        return IconManager.getIconSetFolder(IconManager.ACTION_ICON_SET) + getActionName(action) + ".png";
    }

    private static String getActionName(Class<? extends MuAction> action) {
    	return action.getSimpleName().replace("Action", "");
    }

    ///////////////////////////////////
    // AbstractAction implementation //
    ///////////////////////////////////

    /**
     * Intercepts action events and filters them out when the {@link MainFrame} associated with this action is in
     * 'no events' mode and {@link #honourNoEventsMode()} returns <code>true</code>.
     * If the action event is not filtered out, {@link #performAction()} is called to provide a response to the action event.
     */
    public void actionPerformed(ActionEvent e) {
        // Discard this event while in 'no events mode'
        if(!(mainFrame.getNoEventsMode() && honourNoEventsMode())) {
            if(performActionInSeparateThread()) {
                new Thread() {
                    @Override
                    public void run() {
                        performAction();
                    }
                }.start();
            }
            else {
                performAction();
            }
        }
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Called when this action has been triggered. This method provides a response to the action trigger.
     */
    public abstract void performAction();

    /**
     * Returns the <code>ActionDescriptor</code> of the action.
     * @return the <code>ActionDescriptor</code> of the action.
     */
    public abstract ActionDescriptor getDescriptor();
}
