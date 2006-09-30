

package com.mucommander.ui;

import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MucoAction;
import com.mucommander.ui.comp.button.NonFocusableButton;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Hashtable;
import java.util.Vector;


/**
 * CommandBar is the button bar that sits at the bottom of the main window and provides access to
 * main commander actions (view, edit, copy, move...).
 *
 * @author Maxence Bernard
 */
public class CommandBar extends JPanel implements ConfigurationListener, MouseListener, ContentHandler {

    /** Parent MainFrame instance */
    private MainFrame mainFrame;

    /** True when Shift key is pressed */
    private boolean shiftDown;

    /** Command bar buttons */
    private JButton buttons[];


    /** Path to the XML file specifying the command bar */
    private final static String COMMAND_BAR_XML_FILE_PATH = "/command_bar.xml";

    /** Configuration variable that holds the command bar's icon scale factor */
    public final static String COMMAND_BAR_ICON_SCALE_CONF_VAR = "prefs.command_bar.icon_scale";

    /** Current icon scale factor */
    private static float scaleFactor = ConfigurationManager.getVariableFloat(COMMAND_BAR_ICON_SCALE_CONF_VAR, 1.0f);


    /** Command bar actions */
    private static Class actions[];
    /** Command bar alternate actions */
    private static Class alternateActions[];

    /** Temporarily used for XML parsing */
    private static Vector actionsV;
    /** Temporarily used for XML parsing */
    private static Vector alternateActionsV;


    /**
     * Creates a new CommandBar instance associated with the given MainFrame.
     */
    public CommandBar(MainFrame mainFrame) {
        super(new GridLayout(0,8));
        this.mainFrame = mainFrame;

        // Listen to mouse events to popup a menu when command bar is right clicked
        addMouseListener(this);

        // Listen to configuration changes to reload command bar buttons when icon size has changed
        ConfigurationManager.addConfigurationListener(this);

        // Parse the XML file describing the toolbar buttons and associated actions.
        // This file is parsed only once the first time ToolBar is instancied.
        if(actions==null)
            parseXMLDescriptor();

        // Create buttons and add them to this command bar
        int nbButtons = actions.length;
        buttons = new JButton[nbButtons];
        for(int i=0; i<nbButtons; i++) {
            MucoAction action = ActionManager.getActionInstance(actions[i], mainFrame);
            JButton button = new NonFocusableButton();

            setButtonAction(button, action);

// new ClickButtonAction(mainFrame, button);

            button.setMargin(new Insets(3,4,3,4));
            // For Mac OS X whose default minimum width for buttons is enormous
            button.setMinimumSize(new Dimension(40, (int)button.getPreferredSize().getHeight()));
            button.addMouseListener(this);
            add(button);

            buttons[i] = button;
        }
    }


    /**
     * Parses the XML file describing the toolbar's buttons and associated actions.
     */
    private void parseXMLDescriptor() {
        // Parse the XML file describing the command bar buttons and associated actions
        try {
            new Parser().parse(getClass().getResourceAsStream(COMMAND_BAR_XML_FILE_PATH), this, "UTF-8");
        }
        catch(Exception e) {
            if(com.mucommander.Debug.ON) {
                com.mucommander.Debug.trace("Exception thrown while parsing CommandBar XML file "+COMMAND_BAR_XML_FILE_PATH+": "+e);
                e.printStackTrace();
            }
        }
    }


    /**
     * Sets the given button's action, custom label showing the accelerator, and icon taking into account the scale factor.
     */
    private void setButtonAction(JButton button, MucoAction action) {
        button.setAction(action);

        // Append the action's shortcut to the button's label
        button.setText(action.getLabel()+" ["+action.getAcceleratorText()+"]");

        // Scale icon if scale factor is different from 1.0
        if(scaleFactor!=1.0f)
            button.setIcon(IconManager.getScaledIcon(action.getIcon(), scaleFactor));
    }


    /**
     * Sets the alternate actions mode on/off : some buttons have a different action when in alternate actions mode.
     */
    public void setAlternateActionsMode(boolean on) {
        // Do nothing if command bar is not currently visible
        if(!isVisible())
            return;

        if(this.shiftDown!=on) {
            this.shiftDown = on;

            int nbButtons = buttons.length;
            for(int i=0; i<nbButtons; i++)
                setButtonAction(buttons[i], ActionManager.getActionInstance(on && alternateActions[i]!=null?alternateActions[i]:actions[i], mainFrame));
        }
    }

    ////////////////////////////
    // ContentHandler methods //
    ////////////////////////////

    public void startDocument() throws Exception {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(COMMAND_BAR_XML_FILE_PATH+" parsing started");

        actionsV = new Vector();
        /** Temporarily used for alternate actions parsing */
        alternateActionsV = new Vector();
    }

    public void endDocument() throws Exception {
        int nbActions = actionsV.size();

        actions = new Class[nbActions];
        actionsV.toArray(actions);
        actionsV = null;

        alternateActions = new Class[nbActions];
        alternateActionsV.toArray(alternateActions);
        alternateActionsV = null;

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(COMMAND_BAR_XML_FILE_PATH+" parsing finished");
    }

    public void startElement(String uri, String name, Hashtable attValues, Hashtable attURIs) throws Exception {
        if(name.equals("button")) {
            String actionClassName = (String)attValues.get("action");
            try {
                actionsV.add(Class.forName(actionClassName));
            }
            catch(Exception e) {
                System.out.println("Error in "+COMMAND_BAR_XML_FILE_PATH+": action class "+actionClassName+" not found: "+e);
            }


            actionClassName = (String)attValues.get("alt_action");
            if(actionClassName==null)
                alternateActionsV.add(null);
            else
                try {
                    alternateActionsV.add(Class.forName(actionClassName));
                }
                catch(Exception e) {
                    System.out.println("Error in "+COMMAND_BAR_XML_FILE_PATH+": action class "+actionClassName+" not found: "+e);
                }
        }
    }

    public void endElement(String uri, String name) throws Exception {
    }

    public void characters(String s) throws Exception {
    }

    ///////////////////////////////////
    // ConfigurationListener methods //
    ///////////////////////////////////

    /**
     * Listens to certain configuration variables.
     */
    public boolean configurationChanged(ConfigurationEvent event) {
        String var = event.getVariable();

        // Reload butons icon if the icon scale factor has changed
        if (var.equals(COMMAND_BAR_ICON_SCALE_CONF_VAR)) {
            scaleFactor = event.getFloatValue();

            int nbButtons = buttons.length;
            for(int i=0; i<nbButtons; i++) {
                JButton button = buttons[i];
                // Change the button's icon but NOT the action's icon which has to remain in its original non-scaled size
                button.setIcon(IconManager.getScaledIcon(((MucoAction)button.getAction()).getIcon(), scaleFactor));
            }
        }

        return true;
    }


    ///////////////////////////
    // MouseListener methods //
    ///////////////////////////

    public void mouseClicked(MouseEvent e) {
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
