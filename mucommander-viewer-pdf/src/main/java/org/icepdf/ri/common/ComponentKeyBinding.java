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
package org.icepdf.ri.common;

import org.icepdf.core.pobjects.Document;
import org.icepdf.ri.common.views.DocumentViewController;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Utility for adding key bindings to a view container for common functionality
 * usually handled by the existence of menu key listeners.  This class currently
 * only adds the copy text keyboard command (ctr-c) to view container but can
 * be easily extended to handle other keyboard mappings.
 *
 * @since 4.2.2
 */
@SuppressWarnings("serial")
public class ComponentKeyBinding {

    /**
     * Installs the component key binding on the specified JComponent.
     *
     * @param controller      SwingController used by various keyboard commands
     * @param viewerContainer view container to add keyboard mappings too
     */
    public static void install(final SwingController controller, final JComponent viewerContainer) {
        Action copyText = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Document document = controller.getDocument();
                DocumentViewController documentViewController =
                        controller.getDocumentViewController();
                if (document != null &&
                        controller.havePermissionToExtractContent() &&
                        !(documentViewController.getDocumentViewModel().isSelectAll() &&
                                document.getNumberOfPages() > 250)) {
                    // get the text.
                    StringSelection stringSelection = new StringSelection(
                            documentViewController.getSelectedText());
                    Toolkit.getDefaultToolkit().getSystemClipboard().
                            setContents(stringSelection, null);
                } else {
                    Runnable doSwingWork = new Runnable() {
                        public void run() {
                            org.icepdf.ri.util.Resources.showMessageDialog(
                                    viewerContainer,
                                    JOptionPane.INFORMATION_MESSAGE,
                                    controller.getMessageBundle(),
                                    "viewer.dialog.information.copyAll.title",
                                    "viewer.dialog.information.copyAll.msg",
                                    250);
                        }
                    };
                    SwingUtilities.invokeLater(doSwingWork);
                }
            }
        };

        // add copy text command to input map
        InputMap inputMap = viewerContainer.getInputMap(
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK),
                "copyText");
        viewerContainer.getActionMap().put("copyText",
                copyText);
    }
}
