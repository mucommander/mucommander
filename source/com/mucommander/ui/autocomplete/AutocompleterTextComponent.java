package com.mucommander.ui.autocomplete;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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
	
	public AutocompleterTextComponent(JTextComponent textComp) {
		this.textComponent = textComp;	
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
}
