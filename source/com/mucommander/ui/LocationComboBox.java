
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
//		addKeyListener(this);
		locationField.addActionListener(this);
		folderPanel.addLocationListener(this);
	}


	public ProgressTextField getLocationField() {
		return this.locationField;
	}


	//////////////////////////////
	// LocationListener methods //
	//////////////////////////////
	
	public void locationChanged(LocationEvent e) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");

removeActionListener(this);
		removeAllItems();
		
		AbstractFile folder = e.getFolderPanel().getCurrentFolder();
		while((folder=folder.getParent())!=null) {
			addItem(folder);
		}

		setEnabled(true);
addActionListener(this);

	}

	public void locationChanging(LocationEvent e) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");
	}
	
	public void locationCancelled(LocationEvent e) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called");
		setEnabled(true);
	}
	

	////////////////////////////
	// ActionListener methods //
	////////////////////////////

	public void actionPerformed(ActionEvent e) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called, source="+e.getSource()+" selectedIndex="+getSelectedIndex()+", selectedItem="+getSelectedItem());

		Object source = e.getSource();

		if (source == locationField) {
			folderPanel.trySetCurrentFolder(locationField.getText(), true);
		}
		else {
			Object selectedItem = getSelectedItem();
			if(selectedItem!=null) {
				setEnabled(false);
				folderPanel.trySetCurrentFolder((AbstractFile)selectedItem, true);
			}
		}
	}


	/////////////////////////
	// KeyListener methods //
	/////////////////////////

	public void keyPressed(KeyEvent e) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("called, source="+e.getSource());
		if (e.getSource()==locationField) {
			// Restore current location string if ESC was pressed
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				locationField.setText(folderPanel.getCurrentFolder().getAbsolutePath());
				folderPanel.getFileTable().requestFocus();
			}
		}
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}
}