/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.ri.util;


import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * This is a utility class for ldisplaying internationalized dialogs.
 *
 * @since 1.0
 */
public final class Resources extends StringResource {

    public static void showMessageDialog(Component parent,
                                         final int dialogType,
                                         ResourceBundle messageBundle,
                                         String titleKey,
                                         String messageKey) {

        showMessageDialog(parent, dialogType, messageBundle, titleKey, messageKey,
                null, null, null, null);
    }

    public static void showMessageDialog(Component parent,
                                         final int dialogType,
                                         ResourceBundle messageBundle,
                                         String titleKey,
                                         String messageKey,
                                         Object messageArg1) {

        showMessageDialog(parent, dialogType, messageBundle, titleKey, messageKey,
                messageArg1, null, null, null);
    }

    public static void showMessageDialog(Component parent,
                                         final int dialogType,
                                         ResourceBundle messageBundle,
                                         String titleKey,
                                         String messageKey,
                                         Object messageArg1,
                                         Object messageArg2) {

        showMessageDialog(parent, dialogType, messageBundle, titleKey, messageKey,
                messageArg1, messageArg2, null, null);
    }

    public static void showMessageDialog(Component parent,
                                         final int dialogType,
                                         ResourceBundle messageBundle,
                                         String titleKey,
                                         String messageKey,
                                         Object messageArg1,
                                         Object messageArg2,
                                         Object messageArg3) {

        showMessageDialog(parent, dialogType, messageBundle, titleKey, messageKey,
                messageArg1, messageArg2, messageArg3, null);
    }

    public static void showMessageDialog(Component parent,
                                         final int dialogType,
                                         ResourceBundle messageBundle,
                                         String titleKey,
                                         String messageKey,
                                         Object messageArg1,
                                         Object messageArg2,
                                         Object messageArg3,
                                         Object messageArg4) {
        // setup a patterned message
        Object[] messageArguments = {messageArg1, messageArg2, messageArg3,
                messageArg4
        };

        MessageFormat formatter = new MessageFormat(
                messageBundle.getString(messageKey));

        JOptionPane.showMessageDialog(
                parent,
                formatter.format(messageArguments),
                messageBundle.getString(titleKey),
                dialogType);
    }

    public static boolean showConfirmDialog(Component parent,
                                            ResourceBundle messageBundle,
                                            String titleKey,
                                            String messageKey) {

        return showConfirmDialog(parent, messageBundle, titleKey, messageKey,
                null, null, null, null);
    }

    public static boolean showConfirmDialog(Component parent,
                                            ResourceBundle messageBundle,
                                            String titleKey,
                                            String messageKey,
                                            Object messageArg1) {

        return showConfirmDialog(parent, messageBundle, titleKey, messageKey,
                messageArg1, null, null, null);
    }

    public static boolean showConfirmDialog(Component parent,
                                            ResourceBundle messageBundle,
                                            String titleKey,
                                            String messageKey,
                                            Object messageArg1,
                                            Object messageArg2) {

        return showConfirmDialog(parent, messageBundle, titleKey, messageKey,
                messageArg1, messageArg2, null, null);
    }

    public static boolean showConfirmDialog(Component parent,
                                            ResourceBundle messageBundle,
                                            String titleKey,
                                            String messageKey,
                                            Object messageArg1,
                                            Object messageArg2,
                                            Object messageArg3) {

        return showConfirmDialog(parent, messageBundle, titleKey, messageKey,
                messageArg1, messageArg2, messageArg3, null);
    }

    public static boolean showConfirmDialog(Component parent,
                                            ResourceBundle messageBundle,
                                            String titleKey,
                                            String messageKey,
                                            Object messageArg1,
                                            Object messageArg2,
                                            Object messageArg3,
                                            Object messageArg4) {
        // setup a patterned message
        Object[] messageArguments = {messageArg1, messageArg2, messageArg3,
                messageArg4
        };

        MessageFormat formatter = new MessageFormat(
                messageBundle.getString(messageKey));

        return (JOptionPane.showConfirmDialog(
                parent,
                formatter.format(messageArguments),
                messageBundle.getString(titleKey),
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
    }


}

