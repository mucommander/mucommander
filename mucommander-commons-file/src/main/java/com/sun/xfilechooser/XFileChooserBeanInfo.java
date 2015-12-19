/*
 * Copyright (c) 1999, 2007 Sun Microsystems, Inc. 
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
import com.sun.xfilechooser.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.*;

/**
 * Describes the XFileChooser properties that is modifiable by
 * user in the beans editor. Also will get the icon of
 * XFileChooser that will be shown on the palette of editor.
 */
public class XFileChooserBeanInfo extends SimpleBeanInfo {

    /* For I18N */
    private static ResourceBundle rb =
	ResourceBundle.getBundle("com.sun.xfilechooser.EditorResource"/*NOI18N*/); 
    PropertyDescriptor[] beanProps;

    /*
     * Properties that are modifiable via a bean editor during
     * customization
     */
    private static Object xfBeanPropInits[][] = {
	{rb.getString("Dialog Type"), "getDialogType", "setDialogType", DialogEditor.class},
	{rb.getString("Dialog Title"), "getDialogTitle", "setDialogTitle", XFileChooser.class},
	{rb.getString("File Selection Mode"), "getFileSelectionMode", "setFileSelectionMode", FileModeEditor.class},
	{rb.getString("Show Hidden Files"), "isFileHidingEnabled", "setFileHidingEnabled", XFileChooser.class},
	{rb.getString("Approve Button Text"), "getApproveButtonText", "setApproveButtonText", XFileChooser.class},
	{rb.getString("Approve Button Tooltip"), "getApproveButtonToolTipText", "setApproveButtonToolTipText", XFileChooser.class},
	/* Currently commented out until bug 4206915 is fixed in the introspector */
	//{rb.getString("Approve Button Mnemonic"), "getApproveButtonMnemonic", "setApproveButtonMnemonic", XFileChooser.class},
	{rb.getString("Set Current Directory"), "getCurrentXDirectory", "setCurrentXDirectory", XFileChooserEditor.class},
	{rb.getString("Set Background Color"), "getBackground", "setBackground", XFileChooser.class},
	{rb.getString("Set Foreground Color"), "getForeground", "setForeground", XFileChooser.class}, 
    };

    public PropertyDescriptor[] getPropertyDescriptors() {
	beanProps = new PropertyDescriptor[xfBeanPropInits.length];
	for (int i=0; i < xfBeanPropInits.length; i++) {
	    try {
		beanProps[i] = new PropertyDescriptor(
			        (String) xfBeanPropInits[i][0],
				(Class) XFileChooser.class,
				(String) xfBeanPropInits[i][1],
				(String) xfBeanPropInits[i][2]);
	    } catch (IntrospectionException fatal) {
		System.out.println("name " + (String) xfBeanPropInits[i][0]);
		
		System.err.println("getProps() is flawed! " + i);
	    }

	    if (xfBeanPropInits[i][3] != null)
		beanProps[i].setPropertyEditorClass((Class) xfBeanPropInits[i][3]);
	}
	return beanProps;
    }
    
    public java.awt.Image getIcon(int iconKind) {
	if (iconKind == BeanInfo.ICON_COLOR_16x16) {
	    java.awt.Image img = loadImage("images/422LOGO5_16x16.gif");
	    return img;
	}

	if (iconKind == BeanInfo.ICON_MONO_32x32) {
	    java.awt.Image img = loadImage("images/422LOGO5_32x32.gif");
	    return img;
	}
	return null;
    }
}

