

package com.mucommander.ui;

import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.ui.comp.button.NonFocusableButton;
import com.mucommander.ui.action.MucoAction;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.icon.IconManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


/**
 * CommandBar is the button bar that sits at the bottom of the main window and provides access to
 * main commander actions (view, edit, copy, move...).
 *
 * @author Maxence Bernard
 */
public class CommandBar extends JPanel implements ConfigurationListener, MouseListener {

    /** Parent MainFrame instance */
    private MainFrame mainFrame;

    /** True when Shift key is pressed */ 
    private boolean shiftDown;
	
    /** Buttons */
    private JButton buttons[];

    ////////////////////
    // Button indexes //
    ////////////////////
    public final static int VIEW_INDEX = 0;
    public final static int EDIT_INDEX = 1;
    public final static int COPY_INDEX = 2;
    public final static int MOVE_INDEX = 3;
    public final static int MKDIR_INDEX = 4;
    public final static int DELETE_INDEX = 5;
    public final static int REFRESH_INDEX = 6;
    public final static int CLOSE_INDEX = 7;

    private final static int NB_BUTTONS = 8;

    ////////////////////////////////////
    // Buttons actions/images mapping //
    ////////////////////////////////////

    private final static Class COPY_ACTION = com.mucommander.ui.action.CopyAction.class;
    private final static Class LOCAL_COPY_ACTION = com.mucommander.ui.action.LocalCopyAction.class;

    private final static Class MOVE_ACTION = com.mucommander.ui.action.MoveAction.class;
    private final static Class RENAME_ACTION = com.mucommander.ui.action.RenameAction.class;

    private final static Class BUTTON_ACTIONS[] =  {
        com.mucommander.ui.action.ViewAction.class,
        com.mucommander.ui.action.EditAction.class,
        COPY_ACTION,
        MOVE_ACTION,
        com.mucommander.ui.action.MkdirAction.class,
        com.mucommander.ui.action.DeleteAction.class,
        com.mucommander.ui.action.RefreshAction.class,
        com.mucommander.ui.action.CloseWindowAction.class
    };


    public final static String COMMAND_BAR_ICON_SCALE_CONF_VAR = "prefs.command_bar.icon_scale";

    private static float scaleFactor = ConfigurationManager.getVariableFloat(COMMAND_BAR_ICON_SCALE_CONF_VAR, 1.0f);


    /**
     * Preloads icons if command bar is to become visible after launch. 
     * Icons will then be in IconManager's cache, ready for use when the first command bar is created.
     */
/*
    public static void init() {
        if(com.mucommander.conf.ConfigurationManager.getVariableBoolean("prefs.command_bar.visible", true)) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Preloading command bar icons");

            // For each button
            for(int i=0; i<NB_BUTTONS; i++)
                IconManager.getIcon(IconManager.COMMAND_BAR_ICON_SET, (String)BUTTON_ACTIONS[i][1]);
        }
    }
*/


    /**
     * Creates a new CommandBar instance associated with the given MainFrame.
     */
    public CommandBar(MainFrame mainFrame) {
        super(new GridLayout(0,8));
        this.mainFrame = mainFrame;

        this.buttons = new JButton[NB_BUTTONS];
        for(int i=0; i<NB_BUTTONS; i++)
            buttons[i] = addButton(BUTTON_ACTIONS[i]);

        addMouseListener(this);

        // Listen to configuration changes to reload command bar buttons when icon size has changed
        ConfigurationManager.addConfigurationListener(this);
    }


    /**
     * Returns the button correponding to the given index, to be used in conjunction
     * with final static fields.  
     */
    public JButton getButton(int buttonIndex) {
        return buttons[buttonIndex];
    }
	
	
    /**
     * Creates and adds a button to the command bar using the provided MucoAction's class.
     *
     * @param actionClass A MucoAction class
     */
    private JButton addButton(Class actionClass) {
        MucoAction action = ActionManager.getActionInstance(actionClass, mainFrame);

        JButton button = new NonFocusableButton(action);

        // Append the action's shortcut to the button's label
        button.setText(action.getLabel()+" ["+action.getAcceleratorText()+"]");

        // Scale icon if scale factor is different from 1.0
        if(scaleFactor!=1.0f)
            button.setIcon(IconManager.getScaledIcon(action.getIcon(), scaleFactor));

        button.setMargin(new Insets(3,4,3,4));
        // For Mac OS X whose default minimum width for buttons is enormous
        button.setMinimumSize(new Dimension(40, (int)button.getPreferredSize().getHeight()));
        button.addMouseListener(this);
        add(button);
        return button;
    }


    /**
     * Sets shift mode on or off : some buttons such as 'F6 Move' indicate
     * that action is different when shift is pressed.
     */
    public void setShiftMode(boolean on) {
        // Do nothing if command bar is not currently visible
        if(!isVisible())
            return;

        if(this.shiftDown!=on) {
            this.shiftDown = on;

            // Change Copy/Local copy button's text and tooltip
            MucoAction action = ActionManager.getActionInstance(on?LOCAL_COPY_ACTION:COPY_ACTION, mainFrame);
            buttons[COPY_INDEX].setAction(action);
            buttons[COPY_INDEX].setText(action.getLabel()+" ["+action.getAcceleratorText()+"]");

            // Change Move/Rename button's text and tooltip
            action = ActionManager.getActionInstance(on?RENAME_ACTION:MOVE_ACTION, mainFrame);
            buttons[MOVE_INDEX].setAction(action);
            buttons[MOVE_INDEX].setText(action.getLabel()+" ["+action.getAcceleratorText()+"]");
        }
    }


    ///////////////////////////////////
    // ConfigurationListener methods //
    ///////////////////////////////////
	
    /**
     * Listens to certain configuration variables.
     */
    public boolean configurationChanged(ConfigurationEvent event) {
    	String var = event.getVariable();

        // Reload toolbar icons if their size has changed and command bar is visible
        if (var.equals(COMMAND_BAR_ICON_SCALE_CONF_VAR)) {
            scaleFactor = event.getFloatValue();
            Component components[] = getComponents();
            int nbComponents = components.length;

            for(int i=0; i<nbComponents; i++) {
                if(components[i] instanceof JButton) {
                    JButton button = (JButton)components[i];
                    // Change the button's icon but NOT the action's icon which has to remain in its original non-scaled size
                    button.setIcon(IconManager.getScaledIcon(((MucoAction)button.getAction()).getIcon(), scaleFactor));
                }
            }
        }

        return true;
    }


    ///////////////////////////
    // MouseListener methods //
    ///////////////////////////
	
    public void mouseClicked(MouseEvent e) {
        // Discard mouse events while in 'no events mode'
        if(mainFrame.getNoEventsMode())
            return;

        // Right clicking on the toolbar brings up a popup menu
        int modifiers = e.getModifiers();
        if ((modifiers & MouseEvent.BUTTON2_MASK)!=0 || (modifiers & MouseEvent.BUTTON3_MASK)!=0 || e.isControlDown()) {
            //		if (e.isPopupTrigger()) {	// Doesn't work under Mac OS X (CTRL+click doesn't return true)
            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.add(ActionManager.getActionInstance(com.mucommander.ui.action.ToggleCommandBarAction.class, mainFrame));
            popupMenu.show(this, e.getX(), e.getY());
            popupMenu.setVisible(true);
        }
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }
	
    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }	
}
