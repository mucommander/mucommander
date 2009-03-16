/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

package com.mucommander.ui.dialog;

import com.mucommander.text.Translator;

import javax.swing.*;
import java.awt.*;

/**
 * This class provides static methods that bring up error dialogs. A localized error title / message is used if none
 * is specified.
 * <p>
 * This class currently uses JOptionPane under the hood for creating dialogs.
 * </p>
 *
 * @see WarningDialog
 * @author Maxence Bernard
 */
public class ErrorDialog {

    /**
     * Brings up an error dialog with a generic localized title and message.
     *
     * @param parentComponent determines the <code>Frame</code> in which the dialog is displayed; if <code>null</code>,
     * or if the parentComponent has no <code>Frame</code>, a default <code>Frame</code> is used
     */
    public static void showErrorDialog(Component parentComponent) {
        showErrorDialog(parentComponent, null, null);
    }

    /**
     * Brings up an error dialog with the specified message and a generic localized title.
     *
     * @param parentComponent determines the <code>Frame</code> in which the dialog is displayed; if <code>null</code>,
     * or if the parentComponent has no <code>Frame</code>, a default <code>Frame</code> is used
     * @param message the error message to display in the dialog
     */
    public static void showErrorDialog(Component parentComponent, String message) {
        showErrorDialog(parentComponent, null, message);
    }

    /**
     * Brings up an error dialog with the specified title and message.
     *
     * @param parentComponent determines the <code>Frame</code> in which the dialog is displayed; if <code>null</code>,
     * or if the parentComponent has no <code>Frame</code>, a default <code>Frame</code> is used
     * @param title the dialog's title, <code>null</code> for a generic localized title.
     * @param message the error message to display in the dialog, <code>null</code> for a generic localized message.
     */
    public static void showErrorDialog(Component parentComponent, String title, String message) {
        if(title==null)
            title = Translator.get("error");

        if(message==null)
            message = Translator.get("generic_error");

        JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.ERROR_MESSAGE);
    }
}
