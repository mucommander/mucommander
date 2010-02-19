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

import com.mucommander.runtime.OsFamilies;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;

/**
 * This class offers utility methods for converting KeyStrokes to texts.
 * 
 * @author Arik Hadas, Maxence Bernard
 */
public class KeyStrokeUtils {

	private final static String SHIFT_MODIFIER_STRING = KeyEvent.getKeyModifiersText(KeyEvent.SHIFT_MASK);
    private final static String CTRL_MODIFIER_STRING  = KeyEvent.getKeyModifiersText(KeyEvent.CTRL_MASK);
    private final static String ALT_MODIFIER_STRING   = KeyEvent.getKeyModifiersText(KeyEvent.ALT_MASK);
    private final static String META_MODIFIER_STRING  = KeyEvent.getKeyModifiersText(KeyEvent.META_MASK);
    
	
    /**
     * Returns a String representation for the given KeyStroke for display, in the following format:<br>
     * <code>modifier+modifier+...+key</code>
     *
     * <p>For example, <code>KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK|InputEvent.ALT_MASK)</code>
     * will return <code>Ctrl+Alt+C</code>.</p>
     *
     * @param ks the KeyStroke for which to return a String representation
     * @return a String representation of the given KeyStroke for display, in the <code>[modifier]+[modifier]+...+key</code> format
     */
	public static String getKeyStrokeRepresentation(KeyStroke ks) {
		return ks.toString().replaceFirst("(released )|(pressed )|(typed )", "");
	}
	
	/**
     * Returns a String representation for the given KeyStroke <bold>for display</bold>, in the following format:<br>
     * <code>modifier+modifier+...+key</code>
     *
     * <p>For example, <code>KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK|InputEvent.ALT_MASK)</code>
     * will return <code>Ctrl+Alt+C</code>.</p>
     *
     * @param ks the KeyStroke for which to return a String representation
     * @return a String representation of the given KeyStroke <bold>for display</bold>, in the <code>[modifier]+[modifier]+...+key</code> format
     */
    public static String getKeyStrokeDisplayableRepresentation(KeyStroke ks) {
    	if (ks == null)
    		return null;
    	
        int modifiers = ks.getModifiers();
        String keyText = KeyEvent.getKeyText(ks.getKeyCode());

        if(modifiers!=0) {
            return getModifiersDisplayableRepresentation(modifiers)+"+"+keyText;
        }
        return keyText;
    }

    /**
     * Returns a String representations of the given modifiers bitwise mask, in the following format:<br>
     * <code>modifier+...+modifier
     *
     * <p>The modifiers' order in the returned String tries to mimick the keyboard layout of the current platform as
     * much as possible:
     * <ul>
     *  <li>Under Mac OS X, the order is: <code>Shift, Ctrl, Alt, Meta</code>
     *  <li>Under other platforms, the order is <code>Shift, Ctrl, Meta, Alt</code>
     * </ul>
     *
     * @param modifiers a modifiers bitwise mask
     * @return a String representations of the given modifiers bitwise mask
     */
    public static String getModifiersDisplayableRepresentation(int modifiers) {
        String modifiersString = "";

        if((modifiers&KeyEvent.SHIFT_MASK)!=0)
            modifiersString += SHIFT_MODIFIER_STRING;

        if((modifiers&KeyEvent.CTRL_MASK)!=0)
            modifiersString += (modifiersString.equals("")?"":"+")+CTRL_MODIFIER_STRING;

        if(OsFamilies.MAC_OS_X.isCurrent()) {
            if((modifiers&KeyEvent.ALT_MASK)!=0)
                modifiersString += (modifiersString.equals("")?"":"+")+ALT_MODIFIER_STRING;

            if((modifiers&KeyEvent.META_MASK)!=0)
                modifiersString += (modifiersString.equals("")?"":"+")+META_MODIFIER_STRING;
        }
        else {
            if((modifiers&KeyEvent.META_MASK)!=0)
                modifiersString += (modifiersString.equals("")?"":"+")+META_MODIFIER_STRING;

            if((modifiers&KeyEvent.ALT_MASK)!=0)
                modifiersString += (modifiersString.equals("")?"":"+")+ALT_MODIFIER_STRING;
        }

        return modifiersString;
    }
}
