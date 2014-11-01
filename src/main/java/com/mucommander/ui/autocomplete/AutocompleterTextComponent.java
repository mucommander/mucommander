/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.ui.autocomplete;

import com.mucommander.ui.combobox.EditableComboBox;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

/**
 * AutocompleterTextComponent convert any text component to auto-completion supported text component. 
 * In order to support auto-completion two abstract methods need to be implemented:
 * <ul>
 *   <li>OnEnterPressed - What should the text component do when enter key is pressed and there is no item selected on the auto-completion popup list</li>
 *   <li>OnEscPressed - What should the text component do when escape key is pressed and there is no item selected on the auto-completion popup list</li>
 * </ul>
 * 
 * @author Arik Hadas
 */

public abstract class AutocompleterTextComponent {
	private JTextComponent textComponent;
	private EditableComboBox editableComboBox = null;
	
	public AutocompleterTextComponent(JTextComponent textComp) {
		this.textComponent = textComp;	
	}
	
	protected AutocompleterTextComponent(EditableComboBox editableComboBox) {
		this.textComponent = editableComboBox.getTextField();
		this.editableComboBox = editableComboBox;
		
		// Remove all key listeners which are defined for the EditableCombobox
		removeAllKeyListeners();
	}	

	// Abstract methods:
	/**
	 * This function will be called when the text component has the focus and enter key is 
	 * pressed, while the auto-completion popup window is unvisible.
	 */
	public abstract void OnEnterPressed(KeyEvent keyEvent);
	
	/**
	 * This function will be called when the text component has the focus and escape key is 
	 * pressed, while the auto-completion popup window is unvisible.
	 */
	public abstract void OnEscPressed(KeyEvent keyEvent);
	
	private void removeAllKeyListeners() {
		KeyListener[] l = editableComboBox.getTextField().getKeyListeners();
		int nbKeyListeners = l.length;
		for (int i=0 ; i<nbKeyListeners; i++)
			editableComboBox.getTextField().removeKeyListener(l[i]);
	}
	
	// Methods of the text component which are used by the auto-completion mechanism:	
	public Document getDocument() { return textComponent.getDocument(); }
	
	public boolean isShowing() { return textComponent.isShowing(); }
	
	public void setText(String text) { textComponent.setText(text); }
	
	public String getText() { return textComponent.getText(); }
		
	public boolean hasFocus() { return textComponent.hasFocus(); }
	
	public boolean isEnabled() { return textComponent.isEnabled(); }
	
	public int getCaretPosition() { return textComponent.getCaretPosition(); }
	
	public void requestFocus() { textComponent.requestFocus(); }
	
	public int getHeight() { return textComponent.getHeight(); }
	
	public Rectangle modelToView() throws BadLocationException { return textComponent.getUI().modelToView(textComponent, textComponent.getCaretPosition()); }

	public void moveCarentToEndOfText() { textComponent.setCaretPosition(textComponent.getText().length()); }
	
	public boolean isCarentAtEndOfTextAtInsertion() { return textComponent.getCaretPosition() == textComponent.getText().length() - 1; }
	
	public boolean isCarentAtEndOfTextAtRemoval() { return textComponent.getCaretPosition() == textComponent.getText().length() + 1; }
	
	public JTextComponent getTextComponent() { return textComponent; }
	
	public void addKeyListener(KeyAdapter adapter) { textComponent.addKeyListener(adapter); }
	
	public void addFocusListener(FocusListener listener) { textComponent.addFocusListener(listener); }
	
	
	/**
	 * 	getItemsNames
	 * 
	 * @return empty Vector if component is not an EditableComboBox,
	 *  otherwise return Vector which contains the names of the combobox items.
	 */
	public Vector<String> getItemNames() {
		Vector<String> result = new Vector<String>();
		if (editableComboBox != null) {
			int nbItems = editableComboBox.getItemCount();
			for (int i=0; i < nbItems; i++)
				result.add(editableComboBox.getItemAt(i).toString());
		}
		return result;
	}
	
	/**
	 * isPopupVisible
	 * 
	 * @return false if component is not an EditableComboBox,
	 * 	otherwise, true if the combo-box list of items is visible.
	 */
	public boolean isComponentsPopupVisible() {
		return editableComboBox != null && editableComboBox.isPopupVisible();
	}
	
	/**
	 * setPopupUnvisibe - make the combo-box list of items unvisible.
	 */
	public void setComponentsPopupUnvisibe() {
		if (editableComboBox != null)
			editableComboBox.setPopupVisible(false);
	}
}
