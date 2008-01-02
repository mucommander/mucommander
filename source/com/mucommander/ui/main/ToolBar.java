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

package com.mucommander.ui.main;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileURL;
import com.mucommander.file.util.ResourceLoader;
import com.mucommander.io.BackupInputStream;
import com.mucommander.ui.action.*;
import com.mucommander.ui.button.NonFocusableButton;
import com.mucommander.ui.button.PopupButton;
import com.mucommander.ui.button.RolloverButtonAdapter;
import com.mucommander.ui.icon.IconManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.swing.*;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class is the icon toolbar attached to a MainFrame, triggering events when buttons are clicked.
 *
 * @author Maxence Bernard
 */
public class ToolBar extends JToolBar implements ConfigurationListener, MouseListener {

    private MainFrame mainFrame;

    /** Holds a reference to the RolloverButtonAdapter instance so that it doesn't get garbage-collected */
    private RolloverButtonAdapter rolloverButtonAdapter;

    /** Default toolbar descriptor filename */
    private final static String DEFAULT_TOOLBAR_FILE_NAME = "toolbar.xml";
    /** Path to the toolbar descriptor resource file within the application JAR file */
    private final static String TOOLBAR_RESOURCE_PATH = "/"+DEFAULT_TOOLBAR_FILE_NAME;

    /** Toolbar descriptor file used when calling {@link #loadDescriptionFile()} */
    private static AbstractFile descriptionFile;

    /** Dimension of button separators */
    private final static Dimension SEPARATOR_DIMENSION = new Dimension(10, 16);


    /** Current icon scale value */
    // The math.max(1.0f, ...) part is to workaround a bug which cause(d) this value to be set to 0.0 in the configuration file.
    private static float scaleFactor = Math.max(1.0f, MuConfiguration.getVariable(MuConfiguration.TOOLBAR_ICON_SCALE,
                                                                        MuConfiguration.DEFAULT_TOOLBAR_ICON_SCALE));

    /** Command bar actions: Class instances or null to signify a separator */
    private static Class actions[];


    /**
     * Sets the path to the toolbar description file to be loaded when calling {@link #loadDescriptionFile()}.
     * By default, this file is {@link #DEFAULT_TOOLBAR_FILE_NAME} within the preferences folder.
     * @param path path to the toolbar descriptor file
     */
    public static void setDescriptionFile(String path) throws FileNotFoundException {
        AbstractFile file;

        if((file = FileFactory.getFile(path)) == null)
            setDescriptionFile(new File(path));
        else
            setDescriptionFile(file);
    }

    /**
     * Sets the path to the toolbar description file to be loaded when calling {@link #loadDescriptionFile()}.
     * By default, this file is {@link #DEFAULT_TOOLBAR_FILE_NAME} within the preferences folder.
     * @param file path to the toolbar descriptor file
     */
    public static void setDescriptionFile(File file) throws FileNotFoundException {setDescriptionFile(FileFactory.getFile(file.getAbsolutePath()));}

    /**
     * Sets the path to the toolbar description file to be loaded when calling {@link #loadDescriptionFile()}.
     * By default, this file is {@link #DEFAULT_TOOLBAR_FILE_NAME} within the preferences folder.
     * @param file path to the toolbar descriptor file
     */
    public static void setDescriptionFile(AbstractFile file) throws FileNotFoundException {
        if(file.isBrowsable())
            throw new FileNotFoundException("Not a valid file: " + file);
        descriptionFile = file;
    }

    public static AbstractFile getDescriptionFile() throws IOException {
        if(descriptionFile == null)
            return PlatformManager.getPreferencesFolder().getChild(DEFAULT_TOOLBAR_FILE_NAME);
        return descriptionFile;
    }

    private static void copyDefaultDescriptionFile(AbstractFile destination) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = ResourceLoader.getResourceAsStream(TOOLBAR_RESOURCE_PATH);
            out = destination.getOutputStream(false);

            AbstractFile.copyStream(in, out);
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
     * Parses the XML file describing the toolbar's buttons and associated actions.
     * If the file doesn't exist yet, it is copied from the default resource file within the JAR.
     *
     * <p>This method must be called before instanciating ToolBar for the first time.
     */
    public static void loadDescriptionFile() throws Exception {
        AbstractFile file;

        file = getDescriptionFile();
        // If the given file doesn't exist, copy the default one in the JAR file
        if(!file.exists()) {
            try {
                if(Debug.ON) Debug.trace("copying " + TOOLBAR_RESOURCE_PATH + " resource to " + file);
                copyDefaultDescriptionFile(file);
            }
            catch(IOException e) {
                if(Debug.ON) Debug.trace("Error: unable to copy " + TOOLBAR_RESOURCE_PATH + " resource to " + file + ": " + e);
                // If an error occured, the description file is most likely corrupt.
                // Deletes it.
                if(file.exists())
                    file.delete();

                throw e;
            }
        }

        new ToolBarReader();
    }


    /**
     * Creates a new toolbar and attaches it to the given frame.
     */
    public ToolBar(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // Decoration properties
        setBorderPainted(false);
        setFloatable(false);
        putClientProperty("JToolBar.isRollover", Boolean.TRUE);

        // Listen to mouse events in order to popup a menu when toolbar is right-clicked
        addMouseListener(this);

        // Listen to configuration changes to reload toolbar buttons when icon size has changed
        MuConfiguration.addConfigurationListener(this);

        // Rollover-enable the button and hold a reference to the RolloverButtonAdapter instance so that it doesn't
        // get garbage-collected
        rolloverButtonAdapter = new RolloverButtonAdapter();

        // Create buttons for each actions and add them to the toolbar
        int nbActions = actions.length;
        for(int i=0; i<nbActions; i++) {
            Class actionClass = actions[i];
            if(actionClass==null)
                addSeparator(SEPARATOR_DIMENSION);
            else {
                // Get a MuAction instance
                MuAction action = ActionManager.getActionInstance(actionClass, mainFrame);
                // Do not add buttons for actions that do not have an icon
                if(action.getIcon()!=null)
                    addButton(action);
            }
        }

        // Use new JButton decorations introduced in Mac OS X 10.5 (Leopard)
        if(PlatformManager.MAC_OS_X_10_5.isCurrentOrHigher()) {
            int nbComponents = getComponentCount();
            Component comp;
            boolean hasPrevious, hasNext;

            // Set the 'segment position' required for the 'segmented capsule' style  
            for(int i=0; i<nbComponents; i++) {
                comp = getComponent(i);
                if(!(comp instanceof JButton))
                    continue;

                hasPrevious = i!=0 && (getComponent(i-1) instanceof JButton);
                hasNext = i!=nbComponents-1 && (getComponent(i+1) instanceof JButton);

                String segmentPosition;
                if(hasPrevious && hasNext)
                    segmentPosition = "middle";
                else if(hasPrevious)
                    segmentPosition = "last";
                else if(hasNext)
                    segmentPosition = "first";
                else
                    segmentPosition = "only";

                ((JButton)comp).putClientProperty("JButton.segmentPosition", segmentPosition);
             }
        }
    }


    /**
     * Adds a button to this toolbar using the given action.
     */
    private void addButton(MuAction action) {
        JButton button;

        if(action instanceof GoBackAction || action instanceof GoForwardAction)
            button = new HistoryPopupButton(action);
        else
            button = new NonFocusableButton(action);

        // Remove label
        button.setText(null);

        // Add tooltip using the action's label and accelerator
        String toolTipText = action.getLabel();
        String acceleratorText = action.getAcceleratorText();
        if(acceleratorText!=null)
            toolTipText += " ("+acceleratorText+")";
        button.setToolTipText(toolTipText);

        // Scale icon if scale factor is different from 1.0
        if(scaleFactor!=1.0f)
            button.setIcon(IconManager.getScaledIcon(action.getIcon(), scaleFactor));

        // Use new JButton decorations introduced in Mac OS X 10.5 (Leopard)
        if(PlatformManager.MAC_OS_X_10_5.isCurrentOrHigher()) {
            button.putClientProperty("JButton.buttonType", "segmentedTextured");
            button.setRolloverEnabled(true);
        }
        // On other platforms, use a custom rollover effect
        else {
            // Init rollover
            RolloverButtonAdapter.setButtonDecoration(button);
            button.addMouseListener(rolloverButtonAdapter);
        }

        add(button);
    }


    ///////////////////////////////////
    // ConfigurationListener methods //
    ///////////////////////////////////

    /**
     * Listens to certain configuration variables.
     */
    public void configurationChanged(ConfigurationEvent event) {
        String var = event.getVariable();

        // Rescale buttons icon
        if (var.equals(MuConfiguration.TOOLBAR_ICON_SCALE)) {
            scaleFactor = event.getFloatValue();
            Component components[] = getComponents();
            int nbComponents = components.length;

            for(int i=0; i<nbComponents; i++) {
                if(components[i] instanceof JButton) {
                    JButton button = (JButton)components[i];
                    // Change the button's icon but NOT the action's icon which has to remain in its original non-scaled size
                    button.setIcon(IconManager.getScaledIcon((ImageIcon)button.getAction().getValue(Action.SMALL_ICON), scaleFactor));
                }
            }
        }
    }


    ///////////////////////////
    // MouseListener methods //
    ///////////////////////////

    public void mouseClicked(MouseEvent e) {
        Object source = e.getSource();

        // Right clicking on the toolbar brings up a popup menu
        if(source == this) {
            if (PlatformManager.isRightMouseButton(e)) {
                //			if (e.isPopupTrigger()) {	// Doesn't work under Mac OS X (CTRL+click doesn't return true)
                JPopupMenu popupMenu = new JPopupMenu();
                popupMenu.add(ActionManager.getActionInstance(com.mucommander.ui.action.ToggleToolBarAction.class, mainFrame));
                popupMenu.show(this, e.getX(), e.getY());
                popupMenu.setVisible(true);
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
        Object source = e.getSource();
        if(source instanceof JButton)
            ((JButton)source).setBorderPainted(true);
    }

    public void mouseExited(MouseEvent e) {
        Object source = e.getSource();
        if(source instanceof JButton)
            ((JButton)source).setBorderPainted(false);
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }


    /**
     * PopupButton used for 'Go back' and 'Go forward' actions which displays the list of back/forward folders in the
     * popup menu and allows to recall them by clicking on them.
     */
    private class HistoryPopupButton extends PopupButton {

        private MuAction action;

        private HistoryPopupButton(MuAction action) {
            super(action);
            this.action = action;
        }

        public JPopupMenu getPopupMenu() {
            FileURL history[] = action instanceof GoBackAction?
                    mainFrame.getActiveTable().getFolderPanel().getFolderHistory().getBackFolders()
                    :mainFrame.getActiveTable().getFolderPanel().getFolderHistory().getForwardFolders();
            int historyLen = history.length;        

            // If no back/forward folder, do not display popup menu
            if(history.length==0)
                return null;

            JPopupMenu popupMenu = new JPopupMenu();
            for(int i=0; i<historyLen; i++)
                popupMenu.add(new OpenLocationAction(mainFrame, new Hashtable(), history[i]));

            return popupMenu;
        }
    }


    /**
     * This class parses the XML file describing the toolbar's buttons and associated actions.
     *
     * @author Maxence Bernard
     */
    private static class ToolBarReader extends DefaultHandler {

        /** Temporarily used for XML parsing */
        private Vector actionsV;


        /**
         * Starts parsing the XML description file.
         */
        private ToolBarReader() throws Exception {
            InputStream in;

            in = null;
            try {SAXParserFactory.newInstance().newSAXParser().parse(in = new BackupInputStream(getDescriptionFile()), this);}
            finally {
                if(in != null) {
                    try {in.close();}
                    catch(IOException e) {}
                }
            }
        }

        ////////////////////////////
        // ContentHandler methods //
        ////////////////////////////

        public void startDocument() {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(TOOLBAR_RESOURCE_PATH+" parsing started");

            actionsV = new Vector();
        }

        public void endDocument() {
            int nbActions = actionsV.size();
            actions = new Class[nbActions];
            actionsV.toArray(actions);
            actionsV = null;

            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(TOOLBAR_RESOURCE_PATH+" parsing finished");
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if(qName.equals("button")) {
                String actionClassName = attributes.getValue("action");
                try {
                    actionsV.add(Class.forName(actionClassName));
                }
                catch(Exception e) {if(Debug.ON) Debug.trace("Error in "+TOOLBAR_RESOURCE_PATH+": action class "+actionClassName+" not found: "+e);}
            }
            else if(qName.equals("separator")) {
                actionsV.add(null);
            }
        }
    }
}
