/*
 * This file is part of muCommander, http://www.mucommander.com
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


package com.mucommander.protocol.ui;

import java.awt.Insets;
import java.net.MalformedURLException;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.util.ui.layout.XAlignedComponentPanel;
import com.mucommander.commons.util.ui.spinner.IntEditor;


/**
 * This abstract class represents a panel that helps the user initiate a connection to a servers using a certain file
 * protocol. This class is agnostic with respect to the file protocol used -- subclasses implement a specific file 
 * protocol.
 *
 * @author Maxence Bernard
 */
public abstract class ServerPanel extends XAlignedComponentPanel {

    protected ServerPanelListener listener;
    protected JFrame mainFrame;
	
	
    protected ServerPanel(ServerPanelListener listener, JFrame mainFrame) {
        // Add a 10-pixel gap label and text component
        super(10);
		
        this.listener = listener;
        this.mainFrame = mainFrame;
    }
	
	
    @Override
    public Insets getInsets() {
        return new Insets(8, 6, 8, 6);
    }

    protected JSpinner createPortSpinner(int portValue) {
        return createIntSpinner(portValue, 1, 65535, 1);
    }

    protected JSpinner createIntSpinner(int value, int minValue, int maxValue, int step) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, minValue, maxValue, step));

        // Left-aligns the text within the text field, and use a simple decimal format with no thousand separator
        spinner.setEditor(new IntEditor(spinner, "#####"));

        // Any changes made to the spinner will update the URL label
        spinner.addChangeListener(e -> listener.updateURLLabel());

        return spinner;
    }
	
    protected void addTextFieldListeners(JTextField textField, boolean updateLabel) {
        textField.addActionListener(listener);
        if(updateLabel) {
            textField.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    listener.updateURLLabel();
                }

                public void insertUpdate(DocumentEvent e) {
                    listener.updateURLLabel();
                }

                public void removeUpdate(DocumentEvent e) {
                    listener.updateURLLabel();
                }
            });
        }
    }

    protected void addComboBoxListeners(JComboBox comboBox) {
        comboBox.addItemListener(e -> listener.updateURLLabel());
    }


    ///////////////////////
    // Abstract methoods //
    ///////////////////////

    /** 
     * Returns the current server URL represented by this panel, <code>null</code> if it is not available.
     * This method may be called at any time by {@link ServerConnectDialog}.
     *
     * @return the current server URL represented by this panel, <code>null</code> if it is not available
     * @throws MalformedURLException if an exception was thrown while creating the FileURL instance
     */
    public abstract FileURL getServerURL() throws MalformedURLException;

    /**
     * Returns <code>true</code> if this panel allows the user to specify credentials for the file protocol.
     *
     * @return <code>true</code> if this panel allows the user to specify credentials for the file protocol
     */
    public abstract boolean usesCredentials();

    /**
     * This method is called by {@link ServerConnectDialog} when the dialog has been validated by the user
     * ('OK' button or 'Enter' key pressed). This is where component values should be saved for when a
     * new instance of the panel is created.
     */
    public abstract void dialogValidated();

    /**
     * This method returns the applicability of the privacy policy to the remote server.
     *
     * @return true when our privacy policy is applicable to the remote server, false otherwise
     */
    public boolean privacyPolicyApplicable() {
        return false;
    }
}
