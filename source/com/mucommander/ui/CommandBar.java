

package com.mucommander.ui;

import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.table.FileTableModel;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.comp.button.NonFocusableButton;
import com.mucommander.ui.action.MucoAction;
import com.mucommander.ui.action.ActionManager;

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

    private final static Object BUTTONS_DESC[][] =  {
        {com.mucommander.ui.action.ViewAction.class, "view.png"},
        {com.mucommander.ui.action.EditAction.class, "edit.png"},
        {COPY_ACTION, "copy.png"},
        {MOVE_ACTION, "move.png"},
        {com.mucommander.ui.action.MkdirAction.class, "mkdir.png"},
        {com.mucommander.ui.action.DeleteAction.class, "delete.png"},
        {com.mucommander.ui.action.RefreshAction.class, "refresh.png"},
        {com.mucommander.ui.action.CloseWindowAction.class, "close.png"}
    };

    /**
     * Preloads icons if command bar is to become visible after launch. 
     * Icons will then be in IconManager's cache, ready for use when the first command bar is created.
     */
    public static void init() {
        if(com.mucommander.conf.ConfigurationManager.getVariableBoolean("prefs.command_bar.visible", true)) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Preloading command bar icons");

            // For each button
            for(int i=0; i<NB_BUTTONS; i++)
                IconManager.getIcon(IconManager.COMMAND_BAR_ICON_SET, (String)BUTTONS_DESC[i][1]);
        }

    }


    /**
     * Creates a new CommandBar instance associated with the given MainFrame.
     */
    public CommandBar(MainFrame mainFrame) {
        super(new GridLayout(0,8));
        this.mainFrame = mainFrame;

        this.buttons = new JButton[NB_BUTTONS];
        for(int i=0; i<NB_BUTTONS; i++)
            buttons[i] = addButton((Class)BUTTONS_DESC[i][0]);

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

        button.setMargin(new Insets(3,4,3,4));
        // For Mac OS X whose default minimum width for buttons is enormous
        button.setMinimumSize(new Dimension(40, (int)button.getPreferredSize().getHeight()));
        button.addMouseListener(this);
        add(button);
        return button;
    }


    /**
     * Sets icons in command bar buttons, called when this command bar is about to become visible.
     */
    private void setButtonIcons() {
        // For each button
        for(int i=0; i<NB_BUTTONS; i++)
            buttons[i].setIcon(IconManager.getIcon(IconManager.COMMAND_BAR_ICON_SET, (String)BUTTONS_DESC[i][1]));
    }
	
    /**
     * Remove icons from command bar buttons, called when this command bar is about to become invisible in order to garbage-collect icon instances.
     */
    private void removeButtonIcons() {
        // For each button
        for(int i=0; i<NB_BUTTONS; i++)
            buttons[i].setIcon(null);
    }
	
	
    /**
     * Sets shift mode on or off : some buttons such as 'F6 Move' indicate
     * that action is different when shift is pressed.
     */
    public void setShiftMode(boolean on) {
        // Do nothing if command bar is not currently visible
        if(!isVisible())
            return;

        if(shiftDown!=on) {
            this.shiftDown = on;
            FileTable fileTable = mainFrame.getLastActiveTable();
            boolean singleFileMode = on&&((FileTableModel)(fileTable.getModel())).getNbMarkedFiles()<=1;

            // Change Copy/Local copy button's text and tooltip
            MucoAction action = ActionManager.getActionInstance(singleFileMode?LOCAL_COPY_ACTION:COPY_ACTION, mainFrame);
            Icon icon = buttons[COPY_INDEX].getIcon();      // Save icon
            buttons[COPY_INDEX].setAction(action);
            buttons[COPY_INDEX].setIcon(icon);              // Restore icon
            buttons[COPY_INDEX].setText(action.getLabel()+" ["+action.getAcceleratorText()+"]");

            // Change Move/Rename button's text and tooltip
            action = ActionManager.getActionInstance(singleFileMode?RENAME_ACTION:MOVE_ACTION, mainFrame);
            icon = buttons[MOVE_INDEX].getIcon();           // Save icon
            buttons[MOVE_INDEX].setAction(action);
            buttons[MOVE_INDEX].setIcon(icon);              // Restore icon
            buttons[MOVE_INDEX].setText(action.getLabel()+" ["+action.getAcceleratorText()+"]");
        }
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Overridden method to set/remove toolbar icons depending on the specified new visible state.
     */
    public void setVisible(boolean visible) {
        if(visible) {
            // Set icons to buttons
            setButtonIcons();
        }
        else {
            // Remove icon from buttons
            removeButtonIcons();
        }
		
        super.setVisible(visible);
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
        if (var.equals(IconManager.COMMAND_BAR_ICON_SCALE_CONF_VAR)) {
            if(isVisible())
                setButtonIcons();
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
