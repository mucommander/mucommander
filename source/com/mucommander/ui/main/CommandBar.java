/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.ui.main;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.util.ResourceLoader;
import com.mucommander.io.BackupInputStream;
import com.mucommander.io.StreamUtils;
import com.mucommander.runtime.JavaVersions;
import com.mucommander.runtime.OsFamilies;
import com.mucommander.runtime.OsVersions;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.button.NonFocusableButton;
import com.mucommander.ui.icon.IconManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.swing.*;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.util.Vector;

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
    private final static String DEFAULT_COMMAND_BAR_FILE_NAME = "command_bar.xml";

    /** Path to the command bar descriptor resource file within the application JAR file */
    private final static String COMMAND_BAR_RESOURCE_PATH = "/" + DEFAULT_COMMAND_BAR_FILE_NAME;

    /** Command bar descriptor file used when calling {@link #loadDescriptionFile()} */
    private static AbstractFile commandBarFile;


    /** Current icon scale factor */
    // The math.max(1.0f, ...) part is to workaround a bug which cause(d) this value to be set to 0.0 in the configuration file.
    private static float scaleFactor = Math.max(1.0f, MuConfiguration.getVariable(MuConfiguration.COMMAND_BAR_ICON_SCALE,
                                                                        MuConfiguration.DEFAULT_COMMAND_BAR_ICON_SCALE));


    /** Command bar actions */
    private static Class actions[];
    /** Command bar alternate actions */
    private static Class alternateActions[];

    /** Modifier key that triggers the display of alternate actions when pressed */
    private static KeyStroke modifier;

    /**
     * Sets the path to the command bar description file to be loaded when calling {@link #loadDescriptionFile()}.
     * By default, this file is {@link #DEFAULT_COMMAND_BAR_FILE_NAME} within the preferences folder.
     * @param  path                  path to the command bar descriptor file
     * @throws FileNotFoundException if the specified file is not accessible.
     */
    public static void setDescriptionFile(String path) throws FileNotFoundException {
        AbstractFile file;

        if((file = FileFactory.getFile(path)) == null)
            setDescriptionFile(new File(path));
        else
            setDescriptionFile(file);
    }

    /**
     * Sets the path to the command bar description file to be loaded when calling {@link #loadDescriptionFile()}.
     * By default, this file is {@link #DEFAULT_COMMAND_BAR_FILE_NAME} within the preferences folder.
     * @param  file                  path to the command bar descriptor file
     * @throws FileNotFoundException if the specified file is not accessible.
     */
    public static void setDescriptionFile(File file) throws FileNotFoundException {setDescriptionFile(FileFactory.getFile(file.getAbsolutePath()));}

    /**
     * Sets the path to the command bar description file to be loaded when calling {@link #loadDescriptionFile()}.
     * By default, this file is {@link #DEFAULT_COMMAND_BAR_FILE_NAME} within the preferences folder.
     * @param  file                  path to the command bar descriptor file
     * @throws FileNotFoundException if the specified file is not accessible.
     */
    public static void setDescriptionFile(AbstractFile file) throws FileNotFoundException {
        // Makes sure file can be used as a commandbar description file.
        if(file.isBrowsable())
            throw new FileNotFoundException("Not a valid file: " + file.getAbsolutePath());
        commandBarFile = file;
    }

    public static AbstractFile getDescriptionFile() throws IOException {
        if(commandBarFile == null)
            return PlatformManager.getPreferencesFolder().getChild(DEFAULT_COMMAND_BAR_FILE_NAME);
        return commandBarFile;
    }

    /**
     * Copies the default commandbar description file.
     * @param destination where to copy the default commandbar destination file.
     */
    private static void copyDefaultDescriptionFile(AbstractFile destination) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = ResourceLoader.getResourceAsStream(COMMAND_BAR_RESOURCE_PATH);
            out = destination.getOutputStream(false);

            StreamUtils.copyStream(in, out);
        }
        finally {
            if(in != null) {
                try {in.close();}
                catch(IOException e) {}
            }

            if(out != null) {
                try {out.close();}
                catch(Exception e) {}
            }
        }
    }

    /**
     * Parses the XML file describing the command bar's buttons and associated actions.
     * If the file doesn't exist yet, it is copied from the default resource file within the JAR.
     *
     * This method must be called before instanciating CommandBar for the first time.
     */
    public static void loadDescriptionFile() throws Exception {
        AbstractFile file;

        file = getDescriptionFile();

        // If the given file doesn't exist, copy the default one in the JAR file
        if(!file.exists()) {
            try {
                if(Debug.ON) Debug.trace("copying "+COMMAND_BAR_RESOURCE_PATH+" resource to "+file);

                copyDefaultDescriptionFile(file);
            }
            catch(IOException e) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Error: unable to copy "+COMMAND_BAR_RESOURCE_PATH+" resource to "+file+": "+e);
                // If an error occured, the description file is most likely corrupt.
                // Deletes it.
                if(file.exists())
                    file.delete();

                throw e;
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
        MuConfiguration.addConfigurationListener(this);

        // Create buttons and add them to this command bar
        int nbButtons = actions.length;
        buttons = new JButton[nbButtons];
        for(int i=0; i<nbButtons; i++) {
            MuAction action = ActionManager.getActionInstance(actions[i], mainFrame);
            JButton button = new NonFocusableButton();

            // Use new JButton decorations introduced in Mac OS X 10.5 (Leopard) with Java 1.5 and up
            if(OsFamilies.MAC_OS_X.isCurrent() && OsVersions.MAC_OS_X_10_5.isCurrentOrHigher() && JavaVersions.JAVA_1_5.isCurrentOrHigher()) {
                button.setMargin(new Insets(6,8,6,8));
                button.putClientProperty("JComponent.sizeVariant", "small");
                button.putClientProperty("JButton.buttonType", "textured");
            }
            else {
                button.setMargin(new Insets(3,4,3,4));
            }

            setButtonAction(button, action);

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
    private void setButtonAction(JButton button, MuAction action) {
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
    public void configurationChanged(ConfigurationEvent event) {
        String var = event.getVariable();

        // Reload butons icon if the icon scale factor has changed
        if (var.equals(MuConfiguration.COMMAND_BAR_ICON_SCALE)) {
            scaleFactor = event.getFloatValue();

            int nbButtons = buttons.length;
            for(int i=0; i<nbButtons; i++) {
                JButton button = buttons[i];
                // Change the button's icon but NOT the action's icon which has to remain in its original non-scaled size
                button.setIcon(IconManager.getScaledIcon(((MuAction)button.getAction()).getIcon(), scaleFactor));
            }
        }
    }


    ///////////////////////////
    // MouseListener methods //
    ///////////////////////////

    public void mouseClicked(MouseEvent e) {
        // Right clicking on the toolbar brings up a popup menu
        if (PlatformManager.isRightMouseButton(e)) {
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
    private static class CommandBarReader extends DefaultHandler {

        /** Temporarily used for XML parsing */
        private Vector actionsV;
        /** Temporarily used for XML parsing */
        private Vector alternateActionsV;


        /**
         * Starts parsing the XML description file.
         */
        private CommandBarReader() throws Exception {
            InputStream in;

            in = null;
            try {SAXParserFactory.newInstance().newSAXParser().parse(in = new BackupInputStream(getDescriptionFile()), this);}
            finally {
                if(in!=null)
                    try { in.close(); }
                    catch(IOException e) {}
            }
        }

        ////////////////////////////
        // ContentHandler methods //
        ////////////////////////////

        public void startDocument() {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(COMMAND_BAR_RESOURCE_PATH+" parsing started");

            actionsV = new Vector();
            /** Temporarily used for alternate actions parsing */
            alternateActionsV = new Vector();
        }

        public void endDocument() {
            int nbActions = actionsV.size();

            actions = new Class[nbActions];
            actionsV.toArray(actions);
            actionsV = null;

            alternateActions = new Class[nbActions];
            alternateActionsV.toArray(alternateActions);
            alternateActionsV = null;

            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(COMMAND_BAR_RESOURCE_PATH+" parsing finished");
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if(qName.equals("button")) {
                // Resolve action class
                String actionClassName = attributes.getValue("action");
                try {actionsV.add(Class.forName(actionClassName));}
                catch(Exception e) {if(Debug.ON) Debug.trace("Error in "+COMMAND_BAR_RESOURCE_PATH+": action class "+actionClassName+" not found: "+e);}

                // Resolve alternate action class (if any)
                actionClassName = attributes.getValue("alt_action");
                if(actionClassName==null)
                    alternateActionsV.add(null);
                else
                    try {alternateActionsV.add(Class.forName(actionClassName));}
                    catch(Exception e) {if(Debug.ON) Debug.trace("Error in "+COMMAND_BAR_RESOURCE_PATH+": action class "+actionClassName+" not found: "+e);}
            }
            else if(qName.equals("command_bar")) {
                // Retrieve modifier key (shift by default)
                // Note: early 0.8 beta3 nightly builds did not have this attribute, so the attribute may be null
                String modifierString = attributes.getValue("modifier");

                if(modifierString==null || (modifier=KeyStroke.getKeyStroke(modifierString))==null)
                    modifier = KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0);
            }
        }
    }
}
