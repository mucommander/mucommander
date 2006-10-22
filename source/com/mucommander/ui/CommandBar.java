

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
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileToolkit;
import com.mucommander.PlatformManager;
import com.mucommander.Debug;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Hashtable;
import java.util.Vector;
import java.io.IOException;
import java.io.InputStream;


/**
 * CommandBar is the button bar that sits at the bottom of the main window and provides access to
 * main commander actions (view, edit, copy, move...).
 *
 * @author Maxence Bernard
 */
public class CommandBar extends JPanel implements ConfigurationListener, KeyListener, MouseListener {

    /** Parent MainFrame instance */
    private MainFrame mainFrame;

    /** True when modifier key is pressed */
    private boolean modifierDown;

    /** Command bar buttons */
    private JButton buttons[];


    /** Default command bar descriptor filename */
    private final static String DEFAULT_COMMAND_BAR_FILENAME = "command_bar.xml";
    /** Path to the command bar descriptor resource file within the application JAR file */
    private final static String COMMAND_BAR_RESOURCE_PATH = "/"+DEFAULT_COMMAND_BAR_FILENAME;

    /** Command bar descriptor file used when calling {@link #loadDescriptionFile()} */
    private static AbstractFile commandBarDescriptorFile = FileFactory.getFile(PlatformManager.getPreferencesFolder().getAbsolutePath()+"/"+DEFAULT_COMMAND_BAR_FILENAME);


    /** Configuration variable that holds the command bar's icon scale factor */
    public final static String COMMAND_BAR_ICON_SCALE_CONF_VAR = "prefs.command_bar.icon_scale";

    /** Current icon scale factor */
    private static float scaleFactor = ConfigurationManager.getVariableFloat(COMMAND_BAR_ICON_SCALE_CONF_VAR, 1.0f);


    /** Command bar actions */
    private static Class actions[];
    /** Command bar alternate actions */
    private static Class alternateActions[];

    /** Modifier key that triggers the display of alternate actions when pressed */
    private static KeyStroke modifier;


    /**
     * Sets the path to the command bar description file to be loaded when calling {@link #loadDescriptionFile()}.
     * By default, this file is {@link #DEFAULT_COMMAND_BAR_FILENAME} within the preferences folder.
     *
     * @param filePath path to the command bar descriptor file
     */
    public static void setDescriptionFile(String filePath) {
        AbstractFile file = FileFactory.getFile(filePath);
        if(file!=null)
            commandBarDescriptorFile = file;
    }

    /**
     * Parses the XML file describing the command bar's buttons and associated actions.
     * If the file doesn't exist yet, it is copied from the default resource file within the JAR.
     *
     * This method must be called before instanciating CommandBar for the first time.
     */
    public static void loadDescriptionFile() {
        // If the given file doesn't exist, copy the default one in the JAR file
        if(!commandBarDescriptorFile.exists()) {
            try {
                if(Debug.ON) Debug.trace("copying "+COMMAND_BAR_RESOURCE_PATH+" resource to "+commandBarDescriptorFile);

                FileToolkit.copyResource(COMMAND_BAR_RESOURCE_PATH, commandBarDescriptorFile);
            }
            catch(IOException e) {
                System.out.println("Error: unable to copy "+COMMAND_BAR_RESOURCE_PATH+" resource to "+commandBarDescriptorFile+": "+e);
                return;
            }
        }

        new CommandBarReader();
    }


    /**
     * Creates a new CommandBar instance associated with the given MainFrame.
     */
    public CommandBar(MainFrame mainFrame) {
        super(new GridLayout(0,actions.length));
        this.mainFrame = mainFrame;

        // Listen to modifier key events to display alternate actions
        mainFrame.getFolderPanel1().getFileTable().addKeyListener(this);
        mainFrame.getFolderPanel2().getFileTable().addKeyListener(this);

        // Listen to mouse events to popup a menu when command bar is right clicked
        addMouseListener(this);

        // Listen to configuration changes to reload command bar buttons when icon size has changed
        ConfigurationManager.addConfigurationListener(this);

        // Create buttons and add them to this command bar
        int nbButtons = actions.length;
        buttons = new JButton[nbButtons];
        for(int i=0; i<nbButtons; i++) {
            MucoAction action = ActionManager.getActionInstance(actions[i], mainFrame);
            JButton button = new NonFocusableButton();

            setButtonAction(button, action);

            button.setMargin(new Insets(3,4,3,4));
            // For Mac OS X whose default minimum width for buttons is enormous
            button.setMinimumSize(new Dimension(40, (int)button.getPreferredSize().getHeight()));
            button.addMouseListener(this);
            add(button);

            buttons[i] = button;
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
     * Displays/hides alternate actions: buttons that have an alternate action show it when the command bar's
     * modifier is pressed (Shift by default).
     */
    public void setAlternateActionsMode(boolean on) {
        // Do nothing if command bar is not currently visible
        if(!isVisible())
            return;

        if(this.modifierDown !=on) {
            this.modifierDown = on;

            int nbButtons = buttons.length;
            for(int i=0; i<nbButtons; i++)
                setButtonAction(buttons[i], ActionManager.getActionInstance(on && alternateActions[i]!=null?alternateActions[i]:actions[i], mainFrame));
        }
    }


    /////////////////////////
    // KeyListener methods //
    /////////////////////////

    public void keyPressed(KeyEvent e) {
        // Display alternate actions when the modifier key is pressed
        if(e.getKeyCode() == modifier.getKeyCode())
            setAlternateActionsMode(true);
    }

    public void keyReleased(KeyEvent e) {
        // Display regular actions when the modifier key is released
        if(e.getKeyCode() == modifier.getKeyCode())
            setAlternateActionsMode(false);
    }

    public void keyTyped(KeyEvent e) {
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


    /**
     * This class parses the XML file describing the command bar's buttons and associated actions.
     *
     * @author Maxence Bernard
     */
    private static class CommandBarReader implements ContentHandler {

        /** Temporarily used for XML parsing */
        private Vector actionsV;
        /** Temporarily used for XML parsing */
        private Vector alternateActionsV;


        /**
         * Starts parsing the XML description file.
         */
        private CommandBarReader() {
            // Parse the XML file describing the command bar buttons and associated actions
            InputStream in = null;
            try {
                in = commandBarDescriptorFile.getInputStream();
                new Parser().parse(in, this, "UTF-8");
            }
            catch(Exception e) {
                if(com.mucommander.Debug.ON) {
                    System.out.println("Exception thrown while parsing CommandBar XML file "+COMMAND_BAR_RESOURCE_PATH+": "+e);
                }
            }
            finally {
                if(in!=null)
                    try { in.close(); }
                    catch(IOException e) {}
            }
        }

        ////////////////////////////
        // ContentHandler methods //
        ////////////////////////////

        public void startDocument() throws Exception {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(COMMAND_BAR_RESOURCE_PATH+" parsing started");

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

            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(COMMAND_BAR_RESOURCE_PATH+" parsing finished");
        }

        public void startElement(String uri, String name, Hashtable attValues, Hashtable attURIs) throws Exception {
            if(name.equals("button")) {
                // Resolve action class
                String actionClassName = (String)attValues.get("action");
                try {
                    actionsV.add(Class.forName(actionClassName));
                }
                catch(Exception e) {
                    System.out.println("Error in "+COMMAND_BAR_RESOURCE_PATH+": action class "+actionClassName+" not found: "+e);
                }

                // Resolve alternate action class (if any)
                actionClassName = (String)attValues.get("alt_action");
                if(actionClassName==null)
                    alternateActionsV.add(null);
                else
                    try {
                        alternateActionsV.add(Class.forName(actionClassName));
                    }
                    catch(Exception e) {
                        System.out.println("Error in "+COMMAND_BAR_RESOURCE_PATH+": action class "+actionClassName+" not found: "+e);
                    }
            }
            else if(name.equals("command_bar")) {
                // Retrieve modifier key (shift by default)
                // Note: early 0.8 beta3 nightly builds did not have this attribute, so the attribute may be null
                String modifierString = (String)attValues.get("modifier");

                if(modifierString==null || (modifier=KeyStroke.getKeyStroke(modifierString))==null)
                    modifier = KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0);
            }
        }

        public void endElement(String uri, String name) throws Exception {
        }

        public void characters(String s) throws Exception {
        }
    }
}
