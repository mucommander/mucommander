package org.icepdf.ri.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Base JDialog implementation that sets up an escape key listener which will close the the dialog.
 */
public class EscapeJDialog extends JDialog {

    public EscapeJDialog() {
    }

    public EscapeJDialog(Dialog owner, boolean modal) {
        super(owner, modal);
    }

    public EscapeJDialog(Frame owner) {
        super(owner);
    }

    public EscapeJDialog(Frame owner, boolean modal) {
        super(owner, modal);
    }

    public EscapeJDialog(Frame owner, String title) {
        super(owner, title);
    }

    public EscapeJDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    /**
     * Override createRootPane so that "escape" key can be used to
     * close this window.
     */
    protected JRootPane createRootPane() {
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                setVisible(false);
                dispose();
            }
        };
        JRootPane rootPane = new JRootPane();
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        return rootPane;
    }

}
