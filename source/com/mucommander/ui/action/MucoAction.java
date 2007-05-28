package com.mucommander.ui.action;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.file.util.ResourceLoader;
import com.mucommander.text.Translator;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.icon.IconManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.Hashtable;


/**
 * MucoAction extends <code>AbstractAction</code> to add more functionalities and make it easier to integrate within
 * muCommander. The biggest difference with <code>AbstractAction</code> is that MucoAction instances are bound to a
 * specific {@link MainFrame}.<br>
 * Note that by being an Action, MucoAction can be used in every Swing components that accept Action instances.
 *
 * <p>The MucoAction class is abstract. MucoAction subclasses must implement the {@link #performAction()} method
 * to provide a response to the action trigger, and must provide a constructor with the
 * {@link #MucoAction(MainFrame, java.util.Hashtable)} signature.
 *
 * <p>MucoAction subclasses should not be instanciated directly, {@link ActionManager}'s <code>getActionInstance</code>
 * methods should be used instead. Using {@link ActionManager} to retrieve a MucoAction ensures that only one instance
 * exists for a given {@link MainFrame}. This is particularly important because actions are stateful and can be used
 * in several components of a MainFrame at the same time; if an action's state changes, the change must be reflected
 * everywhere the action is used. It is also important for performance reasons: sharing one action throughout a
 * {@link MainFrame} saves some memory and also CPU cycles as some actions listen to particular events to change
 * their state accordingly.
 *
 * @see ActionManager
 * @see ActionKeymap
 * @author Maxence Bernard
 */
public abstract class MucoAction extends AbstractAction {

    /** The MainFrame associated with this MucoAction */
    protected MainFrame mainFrame;

    /** Name of the alternate accelerator KeyStroke property */
    public final static String ALTERNATE_ACCELERATOR_PROPERTY_KEY = "alternate_accelerator";


    private final static String SHIFT_MODIFIER_STRING = KeyEvent.getKeyModifiersText(KeyEvent.SHIFT_MASK);
    private final static String CTRL_MODIFIER_STRING = KeyEvent.getKeyModifiersText(KeyEvent.CTRL_MASK);
    private final static String ALT_MODIFIER_STRING = KeyEvent.getKeyModifiersText(KeyEvent.ALT_MASK);
    private final static String META_MODIFIER_STRING = KeyEvent.getKeyModifiersText(KeyEvent.META_MASK);
    

    /**
     * Convenience constructor which has the same effect as calling {@link #MucoAction(MainFrame, Hashtable, boolean, boolean, boolean )}
     * with these parameters and all lookups enabled.
     * 
     * @param mainFrame the MainFrame to associate with this new MucoAction
     * @param properties the initial properties to use in this action. The Hashtable may simply be empty if no initial
     * properties are specified.
     */
    public MucoAction(MainFrame mainFrame, Hashtable properties) {
        this(mainFrame, properties, true, true, true);
    }

    /**
     * Convenience constructor which has the same effect as calling {@link #MucoAction(MainFrame, Hashtable, boolean, boolean, boolean)}
     * with these parameters and icon and accelerators lookups enabled.
     *
     * @param mainFrame the MainFrame to associate with this new MucoAction
     * @param properties the initial properties to use in this action. The Hashtable may simply be empty if no initial
     * properties are specified.
     */
    public MucoAction(MainFrame mainFrame, Hashtable properties, boolean lookupTranslator) {
        this(mainFrame, properties, lookupTranslator, true, true);
    }

    /**
     * Creates a new MucoAction associated with the specified {@link MainFrame}. The properties contained by the given
     * <code>Hashtable</code> will be used to initialize this action's property map.
     *
     * <p>If the <code>lookupTranslator</code> parameter is <code>true</code>, {@link Translator} will be
     * automatically queried to look for dictionary entries for the label and tooltip matching the following
     * naming convention:
     * <ul>
     *  <li><action_class>.label
     *  <li><action_class>.tooltip
     * </ul>
     * where <action_class> is the name of this class as returned by <code>Class.getName()</code>. If a value for the
     * label/tooltip is found, it will be used as the action's label/tooltip.
     *
     * <p>Similarly, if the <code>lookupIconManager</code> parameter is <code>true<code>, IconManager will be queried
     * to look for an image resource in the action icon folder with a name matching <code><action_class>.png</code>.
     *
     * <p>Finally, if the <code>lookupActionKeymap</code> parameter is <code>true</code>, {@link ActionKeymap} will
     * be queried to look for an accelerator <code>KeyStroke</code> matching this class. If an accelerator was found,
     * the operation will be repeated for the alternate (secondary) accelerator KeyStroke.
     *
     * @param mainFrame the MainFrame to associate with this new MucoAction
     * @param properties the initial properties to use in this action. The Hashtable may simply be empty if no initial
     * properties are specified.
     * @param lookupTranslator if <code>true</code>, {@link Translator} will be looked up to find a label and tooltip
     * for this action class
     * @param lookupIconManager if <code>true</code>, {@link IconManager} will be looked up to find an icon image
     * matching this action class
     * @param lookupActionKeymap if <code>true</code>, {@link ActionKeymap} will be looked up to find accelerator
     * KeyStrokes matching this action class
     */
    public MucoAction(MainFrame mainFrame, Hashtable properties, boolean lookupTranslator, boolean lookupIconManager, boolean lookupActionKeymap) {
        this.mainFrame = mainFrame;

        Class classInstance = getClass();
        String className = classInstance.getName();

        // Add properties to this Action.
        // Property keys are expected to be String instances, those that are not will not be added.
        Enumeration keys = properties.keys();
        while(keys.hasMoreElements()) {
            Object key = keys.nextElement();

            if(key instanceof String)
                putValue((String)key, properties.get(key));
            else
                if(Debug.ON) Debug.trace("Key is not a String, property ignored for key="+key);
        }

        if(lookupTranslator) {
            // Looks for a dictionary entry in the '<action_class>.label' format and use as a label if it is defined
            String label = Translator.get(className+".label");
            // Append '...' to the label if this action invokes a dialog when performed
            if(this instanceof InvokesDialog)
                label += "...";
            setLabel(label);

            // Looks for a dictionary entry in the '<action_class>.tooltip' format and use as a tooltip if it is defined
            String key = className+".tooltip";
            if(Translator.entryExists(key))
                setToolTipText(Translator.get(key));
        }

        if(lookupActionKeymap) {
            // Look for an accelerator registered in ActionKeymap for this action class
            KeyStroke accelerator = ActionKeymap.getAccelerator(classInstance);
            if(accelerator!=null) {
                setAccelerator(accelerator);

                // Look for an alternate accelerator registered in ActionKeymap for this action class
                accelerator = ActionKeymap.getAlternateAccelerator(classInstance);
                if(accelerator!=null) {
                    setAlternateAccelerator(accelerator);
                }
            }
        }

        if(lookupIconManager) {
            // Look for an icon image file with the /action/<classname>.png path and use it if it exists
            String iconPath = getIconPath(classInstance);
            if(ResourceLoader.getResource(iconPath)!=null)
                setIcon(IconManager.getIcon(iconPath));
        }
    }


    /**
     * Returns the path to the icon image within the application's JAR file corresponding to the specified
     * {@link MucoAction} class descriptor.  
     */
    protected static String getIconPath(Class action) {
        return IconManager.getIconSetFolder(IconManager.ACTION_ICON_SET) + action.getName() + ".png";
    }


    /**
     * Return the {@link MainFrame} instance that is associated with this MucoAction.
     */
    public MainFrame getMainFrame() {
        return this.mainFrame;
    }


    /**
     * Returns the label of this action, <code>null</code> if this action has no label.
     * The label value is stored in the {@link #NAME} property.
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
     * Returns a String representation for the given KeyStroke, in the following format:<br>
     * <code>modifier+modifier+...+key</code>
     *
     * <p>For example, <code>KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK|InputEvent.ALT_MASK)</code>
     * will return <code>Ctrl+Alt+C</code>.</p>
     *
     * @param ks the KeyStroke for which to return a String representation
     * @return a String representation of the given KeyStroke, in the <code>[modifier]+[modifier]+...+key</code> format
     */
    public static String getKeyStrokeRepresentation(KeyStroke ks) {
        int modifiers = ks.getModifiers();
        String keyText = KeyEvent.getKeyText(ks.getKeyCode());

        if(modifiers!=0) {
            return getModifiersRepresentation(modifiers)+"+"+keyText;
        }

        return keyText;
    }


    /**
     * Returns a String representations of the given modifiers bitwise mask, in the following format:<br>
     * <code>modifier+...+modifier
     *
     * <p>The modifiers' order in the returned String tries to mimick the keyboard layout of the current platform as
     * much as possible:
     * <ul>
     *  <li>Under Mac OS X, the order is: <code>Shift, Ctrl, Alt, Meta</code>
     *  <li>Under other platforms, the order is <code>Shift, Ctrl, Meta, Alt</code>
     * </ul>
     *
     * @param modifiers a modifiers bitwise mask
     * @return a String representations of the given modifiers bitwise mask
     */
    public static String getModifiersRepresentation(int modifiers) {
        String modifiersString = "";

        if((modifiers&KeyEvent.SHIFT_MASK)!=0)
            modifiersString += SHIFT_MODIFIER_STRING;

        if((modifiers&KeyEvent.CTRL_MASK)!=0)
            modifiersString += (modifiersString.equals("")?"":"+")+CTRL_MODIFIER_STRING;

        if(PlatformManager.OS_FAMILY==PlatformManager.MAC_OS_X) {
            if((modifiers&KeyEvent.ALT_MASK)!=0)
                modifiersString += (modifiersString.equals("")?"":"+")+ALT_MODIFIER_STRING;

            if((modifiers&KeyEvent.META_MASK)!=0)
                modifiersString += (modifiersString.equals("")?"":"+")+META_MODIFIER_STRING;
        }
        else {
            if((modifiers&KeyEvent.META_MASK)!=0)
                modifiersString += (modifiersString.equals("")?"":"+")+META_MODIFIER_STRING;

            if((modifiers&KeyEvent.ALT_MASK)!=0)
                modifiersString += (modifiersString.equals("")?"":"+")+ALT_MODIFIER_STRING;
        }

        return modifiersString;
    }



    /**
     * Returns true if the given KeyStroke is one of this action's accelerators.
     * This method always returns false if this method has no accelerator.
     *
     * @param keyStroke the KeyStroke to test against this action's acccelerators
     * @return true if the given KeyStroke is one of this action's accelerators
     */
    public boolean isAccelerator(KeyStroke keyStroke) {
        KeyStroke accelerator = getAccelerator();
        if(accelerator!=null && accelerator.equals(keyStroke))
            return true;

        accelerator = getAlternateAccelerator();
        return accelerator!=null && accelerator.equals(keyStroke);
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
     * action is in 'no events' mode (see {@link MainFrame} for an explanation of this mode).
     *
     * <p>This method always returns <code>true</code> (action events are ignored) and should be overridden to change
     * the default behavior.
     */
    public boolean ignoreEventsWhileInNoEventsMode() {
        return true;
    }


    ///////////////////////////////////
    // AbstractAction implementation //
    ///////////////////////////////////

    /**
     * Intercepts action events and filters them out when the {@link MainFrame} associated with this action is in
     * 'no events' mode and {@link #ignoreEventsWhileInNoEventsMode()} returns <code>true</code>.
     * If the action event is not filtered out, {@link #performAction()} is called to provide a response to the action event.
     */
    public void actionPerformed(ActionEvent e) {
        // Discard this event while in 'no events mode'
        if(!(mainFrame.getNoEventsMode() && ignoreEventsWhileInNoEventsMode()))
            performAction();
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Called when this action has been triggered. This method provides a response to the action trigger.
     */
    public abstract void performAction();
}
