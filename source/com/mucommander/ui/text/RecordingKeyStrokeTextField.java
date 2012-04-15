/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.ui.text;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * <code>RecordingKeyStrokeTextField</code> is a text field that record a KeyStroke entered by the user.
 * 
 * @author Arik Hadas
 */
public class RecordingKeyStrokeTextField extends JTextField implements FocusListener, KeyListener {

	/** The last KeyStroke that was entered to the field. */
	private KeyStroke lastKeyStroke;
	
	/** The default border of JTextField */
	private final Border defaultTextFieldBorder = getBorder();
	
	public RecordingKeyStrokeTextField(int columns, KeyStroke keyStroke) {
		// set text field's length
		setColumns(columns);
		// The text will be shown at the center of the text field
		setHorizontalAlignment(JTextField.CENTER);
		// The text field should not be editable
		setEditable(false);
		// Change colors to prevent the user from marking the field's text
		setSelectionColor(UIManager.getColor("jtextfield.background"));
		setSelectedTextColor(getForeground());
		// Use JTextField's "setText" method to set the initial KeyStroke in the text field
		super.setText(KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(lastKeyStroke = keyStroke));
		
		// Add listeners:
		addFocusListener(this);
		addKeyListener(this);
	}
	
	/**
	 * This method is used to fetch the KeyStroke in the text-field.
	 * The returned KeyStrole is the last KeyStroke entered to the field by the user, or
	 * the initial KeyStroke that was loaded to the field if the user didn't entered anything.
	 * 
	 * @return the KeyStroke in the text-field
	 */
	public KeyStroke getKeyStroke() { return lastKeyStroke; }
	
	//////////////////////////////////
	/////  FocusListener methods  ////
	/////////////////////////////////

	public void focusGained(FocusEvent e) {
		// change border to indicate this field gained the focus
		// and the user can type
		setBorder(BorderFactory.createLineBorder(Color.orange, 2));
	}

	public void focusLost(FocusEvent e) {
		// change border to indicate this field lost the focus
		setBorder(defaultTextFieldBorder);
	}
	
	//////////////////////////////// 
	/////  KeyListener methods  ////
	////////////////////////////////
	
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() != KeyEvent.VK_ESCAPE)
			setText(KeyStrokeUtils.getKeyStrokeDisplayableRepresentation(lastKeyStroke = KeyStroke.getKeyStroke(e.getKeyCode(), 0)));
		e.consume();
	}

	public void keyReleased(KeyEvent e) {}

	public void keyTyped(KeyEvent e) {}
}
