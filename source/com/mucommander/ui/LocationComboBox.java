
package com.mucommander.ui;

import com.mucommander.ui.comp.progress.ProgressTextField;
import com.mucommander.event.*;
import com.mucommander.file.AbstractFile;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import java.awt.*;
import java.awt.event.*;


public class LocationComboBox extends JComboBox implements LocationListener, ActionListener, KeyListener {

    private FolderPanel folderPanel;
    private ProgressTextField locationField;

    private boolean ignoreEvents;

    public LocationComboBox(FolderPanel folderPanel) {
        this.folderPanel = folderPanel;
        this.locationField = new ProgressTextField(0, new Color(0, 255, 255, 64));
		
        setEditable(true);

        setEditor(new BasicComboBoxEditor() {
                public Component getEditorComponent() {
                    return LocationComboBox.this.locationField;
                }
            });
		
        addActionListener(this);
        locationField.addKeyListener(this);

        folderPanel.addLocationListener(this);
    
        // Prevent up/down keys from firing ActionEvents 
        // Java 1.3
        putClientProperty("JComboBox.lightweightKeyboardNavigation","Lightweight");
        // Java 1.4 and up
        putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
    }


    public ProgressTextField getLocationField() {
        return this.locationField;
    }


    //////////////////////////////
    // LocationListener methods //
    //////////////////////////////
	
    public void locationChanged(LocationEvent e) {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");
        ignoreEvents = true;      // Necessary for first time

        removeAllItems();
		
        AbstractFile folder = e.getFolderPanel().getCurrentFolder();
        do {
            addItem(folder);
        }
        while((folder=folder.getParent())!=null);

        setEnabled(true);
        ignoreEvents = false;
    }

    public void locationChanging(LocationEvent e) {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");
    }
	
    public void locationCancelled(LocationEvent e) {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");

        setEnabled(true);
        ignoreEvents = false;
    }
	

    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////

    public void actionPerformed(ActionEvent e) {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called, "+"selectedIndex="+getSelectedIndex()+", selectedItem="+getSelectedItem()+" ignoreEvents="+ignoreEvents);

        if(ignoreEvents)
            return;

        Object selectedItem = getSelectedItem();
        if(selectedItem!=null) {
            ignoreEvents = true;
            setEnabled(false);
            hidePopup();    // Seems to be necessary under Windows/Java 1.5
            folderPanel.trySetCurrentFolder((AbstractFile)selectedItem);
        }
//        else {
//            setEnabled(false);
//            folderPanel.trySetCurrentFolder(locationField.getText());
//        }
    }


    /////////////////////////
    // KeyListener methods //
    /////////////////////////

    public void keyPressed(KeyEvent e) {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called, keyCode="+e.getKeyCode()+" ignoreEvents="+ignoreEvents);

        if(ignoreEvents)
            return;

        // Restore current location string if ESC was pressed
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE  && !isPopupVisible()) {
            locationField.setText(folderPanel.getCurrentFolder().getAbsolutePath());
            folderPanel.getFileTable().requestFocus();
        }
        else if(e.getKeyCode()==KeyEvent.VK_ENTER && !isPopupVisible()) {
            ignoreEvents = true;
            setEnabled(false);
            folderPanel.trySetCurrentFolder(locationField.getText());
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }
}
