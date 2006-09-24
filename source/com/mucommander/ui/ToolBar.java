
package com.mucommander.ui;

import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;
import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.ui.comp.button.RolloverButton;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MucoAction;
import com.mucommander.ui.icon.IconManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Hashtable;


/**
 * This class is the icon toolbar attached to a MainFrame, triggering events when buttons are clicked.
 *
 * @author Maxence Bernard
 */
public class ToolBar extends JToolBar implements ConfigurationListener, ContentHandler, MouseListener {

    private MainFrame mainFrame;

    /** Path to the XML file specifying the toolbar buttons */
    private final static String TOOLBAR_XML_FILE_PATH = "/toolbar.xml";

    /** Dimension of button separators */
    private final static Dimension SEPARATOR_DIMENSION = new Dimension(10, 16);

    /** Name of the configuration variable that holds the toolbar's icon scale */
    public final static String TOOLBAR_ICON_SCALE_CONF_VAR = "prefs.toolbar.icon_scale";

    /** Current icon scale value */
    private static float scaleFactor = ConfigurationManager.getVariableFloat(TOOLBAR_ICON_SCALE_CONF_VAR, 1.0f);


    /**
     * Preloads icons if toolbar is to become visible after launch. 
     * Icons will then be in IconManager's cache, ready for use when the first ToolBar is created.
     */
/*
    public static void init() {
        if(com.mucommander.conf.ConfigurationManager.getVariableBoolean("prefs.toolbar.visible", true)) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Preloading toolbar icons");
			
            // For each icon
            int nbIcons = BUTTONS_DESC.length;
            for(int i=0; i<nbIcons; i++) {
                // Preload 'enabled' icon
                IconManager.getIcon(IconManager.TOOLBAR_ICON_SET, (String)BUTTONS_DESC[i][1]);
            }
        }
    }
*/


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
        ConfigurationManager.addConfigurationListener(this);

        try {
            // Use UTF-8 encoding
            new Parser().parse(getClass().getResourceAsStream(TOOLBAR_XML_FILE_PATH), this, "UTF-8");
        }
        catch(Exception e) {
            if(com.mucommander.Debug.ON) {
                com.mucommander.Debug.trace("Exception thrown while parsing Toolbar XML file "+TOOLBAR_XML_FILE_PATH+" :"+e);
                e.printStackTrace();
            }
        }
    }


    private void addButton(MucoAction action) {
        JButton button = new RolloverButton(action);

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

        add(button);
    }


    ////////////////////////////
    // ContentHandler methods //
    ////////////////////////////

    public void startDocument() throws Exception {
    }

    public void endDocument() throws Exception {
    }

    public void startElement(String uri, String name, Hashtable attValues, Hashtable attURIs) throws Exception {
        if(name.equals("button")) {
            addButton(ActionManager.getActionInstance((String)attValues.get("action"), mainFrame));
        }
        else if(name.equals("separator")) {
            addSeparator(SEPARATOR_DIMENSION);
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

        // Rescale buttons icon
        if (var.equals(TOOLBAR_ICON_SCALE_CONF_VAR)) {
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

        Object source = e.getSource();

        // Right clicking on the toolbar brings up a popup menu
        if(source == this) {
            int modifiers = e.getModifiers();
            if ((modifiers & MouseEvent.BUTTON2_MASK)!=0 || (modifiers & MouseEvent.BUTTON3_MASK)!=0 || e.isControlDown()) {
                //			if (e.isPopupTrigger()) {	// Doesn't work under Mac OS X (CTRL+click doesn't return true)
                JPopupMenu popupMenu = new JPopupMenu();
                popupMenu.add(ActionManager.getActionInstance(com.mucommander.ui.action.ToggleToolBarAction.class, mainFrame));
                popupMenu.show(this, e.getX(), e.getY());
                popupMenu.setVisible(true);
            }
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
