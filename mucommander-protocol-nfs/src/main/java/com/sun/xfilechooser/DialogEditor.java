/*
 * Copyright (c) 1998, 2007 Sun Microsystems, Inc. 
 * All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * -Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS
 * SHALL NOT BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE
 * AS A RESULT OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE
 * SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE
 * LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED
 * AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed,licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

package com.sun.xfilechooser;

import java.beans.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.*;
import com.sun.xfilechooser.*;

/**
 * An editor to set the Dialog Type of XFileChooser. Used during
 * customization via a bean editor.
 * @see #XFileChooserBeanInfo
 */
public class DialogEditor extends PropertyEditorSupport {

    /* For I18N */
    private static ResourceBundle rb =
	ResourceBundle.getBundle("com.sun.xfilechooser.EditorResource"/*NOI18N*/); 
    int[] dialogValues = {JFileChooser.OPEN_DIALOG, JFileChooser.SAVE_DIALOG,
			  JFileChooser.CUSTOM_DIALOG};
    
    String[] dialogNames = {rb.getString("Open"), rb.getString("Save"),
			    rb.getString("Custom")};

    /**
     *  Provides the valid dialog type: Open, Save, or Custom
     *  @return String name of the valid dialog type
     */
    public String[] getTags() {
	return dialogNames;
    }

    /**
     *  Gets the integer value of current selected dialog type and returns the
     *  corresponding string of dialog type.
     *  @return String name of type of dialog
     */
    public String getAsText() {
	int s = ((Integer)getValue()).intValue();
	for (int i=0; i<dialogNames.length; i++) {
	    if (s == dialogValues[i]) {
		return dialogNames[i];
	    }
	}
	return null;
    }

    /**
     *  Sets the selected dialog type
     *	@param text name of selected dialog type
     */
    public void setAsText(String text) throws IllegalArgumentException {
	for (int i=0; i<dialogNames.length; i++) {
	    if (text.equals(dialogNames[i])) {
		setValue(new Integer(dialogValues[i]));
		return;
	    }
	}
	throw new IllegalArgumentException(text);
    }
    
}
