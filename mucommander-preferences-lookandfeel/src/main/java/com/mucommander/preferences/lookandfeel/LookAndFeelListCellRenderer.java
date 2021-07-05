package com.mucommander.preferences.lookandfeel;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

class LookAndFeelListCellRenderer extends JLabel implements ListCellRenderer<Object> {

    private final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

    LookAndFeelListCellRenderer() {
        setOpaque(true);
        setBorder(noFocusBorder);
        setName("List.cellRenderer");
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        LAFInfo lafInfo = (LAFInfo) value;

        setText(lafInfo.name);
        setToolTipText(lafInfo.name + " (" + lafInfo.className + ")");

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        }
        else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());

        Border border = null;
        if (cellHasFocus) {
            if (isSelected) {
                border = UIManager.getBorder("List.focusSelectedCellHighlightBorder");
            }
            if (border == null) {
                border = UIManager.getBorder("List.focusCellHighlightBorder");
            }
        } else {
            border = noFocusBorder;
        }
        setBorder(border);

        return this;
    }
}
