/*
 * This file is part of muCommander, http://www.mucommander.com
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
package com.mucommander.viewer.binary.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.PositionCodeType;

import com.mucommander.text.Translator;

/**
 * Spinner supporting multiple bases.
 */
@ParametersAreNonnullByDefault
public class BaseSwitchableSpinnerPanel extends javax.swing.JPanel {

    private boolean adjusting;
    private final PositionSpinnerEditor spinnerEditor;
    private static final String SPINNER_PROPERTY = "value";

    private javax.swing.JButton baseSwitchButton;
    private javax.swing.JPopupMenu baseSwitchPopupMenu;
    private javax.swing.JMenuItem decimalMenuItem;
    private javax.swing.JMenuItem hexadecimalMenuItem;
    private javax.swing.JMenuItem octalMenuItem;
    private javax.swing.JSpinner spinner;

    public BaseSwitchableSpinnerPanel() {
        initComponents();
        spinnerEditor = new PositionSpinnerEditor(spinner);
        spinner.setEditor(spinnerEditor);
        init();
    }

    private void init() {
        // Spinner selection workaround from http://forums.sun.com/thread.jspa?threadID=409748&forumID=57
        spinnerEditor.getTextField().addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (e.getSource() instanceof JTextComponent) {
                    final JTextComponent textComponent = ((JTextComponent) e.getSource());
                    SwingUtilities.invokeLater(textComponent::selectAll);
                }
            }
        });

        Dimension preferredSize = baseSwitchButton.getPreferredSize();
        setPreferredSize(new Dimension(preferredSize.width * 4, preferredSize.height));
    }

    private void initComponents() {
        baseSwitchPopupMenu = new javax.swing.JPopupMenu();
        octalMenuItem = new javax.swing.JMenuItem();
        decimalMenuItem = new javax.swing.JMenuItem();
        hexadecimalMenuItem = new javax.swing.JMenuItem();
        baseSwitchButton = new javax.swing.JButton();
        spinner = new javax.swing.JSpinner();

        octalMenuItem.setText(Translator.get("binary_viewer.code_type.oct"));
        octalMenuItem.setToolTipText(Translator.get("binary_viewer.code_type.octal"));
        octalMenuItem.addActionListener(this::octalMenuItemActionPerformed);
        baseSwitchPopupMenu.add(octalMenuItem);

        decimalMenuItem.setText(Translator.get("binary_viewer.code_type.dec"));
        decimalMenuItem.setToolTipText(Translator.get("binary_viewer.code_type.decimal"));
        decimalMenuItem.addActionListener(this::decimalMenuItemActionPerformed);
        baseSwitchPopupMenu.add(decimalMenuItem);

        hexadecimalMenuItem.setText(Translator.get("binary_viewer.code_type.hex"));
        hexadecimalMenuItem.setToolTipText(Translator.get("binary_viewer.code_type.hexadecimal"));
        hexadecimalMenuItem.addActionListener(this::hexadecimalMenuItemActionPerformed);
        baseSwitchPopupMenu.add(hexadecimalMenuItem);

        setPreferredSize(new java.awt.Dimension(400, 300));

        baseSwitchButton.setText(Translator.get("binary_viewer.code_type.dec"));
        baseSwitchButton.setToolTipText(Translator.get("binary_viewer.code_type.decimal"));
        baseSwitchButton.setComponentPopupMenu(baseSwitchPopupMenu);
        baseSwitchButton.addActionListener(this::baseSwitchButtonActionPerformed);

        spinner.setModel(new javax.swing.SpinnerNumberModel(0L, null, null, 1L));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(baseSwitchButton,
                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                        65,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinner, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(spinner)
                        .addComponent(baseSwitchButton,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE));
    }

    private void baseSwitchButtonActionPerformed(java.awt.event.ActionEvent evt) {
        PositionCodeType positionCodeType = spinnerEditor.getPositionCodeType();
        switch (positionCodeType) {
        case OCTAL: {
            switchNumBase(PositionCodeType.DECIMAL);
            break;
        }
        case DECIMAL: {
            switchNumBase(PositionCodeType.HEXADECIMAL);
            break;
        }
        case HEXADECIMAL: {
            switchNumBase(PositionCodeType.OCTAL);
            break;
        }
        default:
            throw CodeAreaUtils.getInvalidTypeException(positionCodeType);
        }
    }

    private void octalMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        switchNumBase(PositionCodeType.OCTAL);
    }

    private void decimalMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        switchNumBase(PositionCodeType.DECIMAL);
    }

    private void hexadecimalMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        switchNumBase(PositionCodeType.HEXADECIMAL);
    }

    private void switchNumBase(PositionCodeType codeType) {
        adjusting = true;
        long value = getValue();
        int position = codeType.ordinal();
        baseSwitchButton.setText(codeType.name().substring(0, 3));
        baseSwitchButton.setToolTipText(((JMenuItem) baseSwitchPopupMenu.getComponent(position)).getToolTipText());
        spinnerEditor.setPositionCodeType(codeType);
        setValue(value);
        adjusting = false;
    }

    public long getValue() {
        return (Long) spinner.getValue();
    }

    public void setValue(long value) {
        spinnerEditor.setPositionValue(value);
    }

    public void acceptInput() {
        try {
            spinner.commitEdit();
        } catch (ParseException ex) {
            // Ignore parse exception
        }
    }

    public void initFocus() {
        /* ((JSpinner.DefaultEditor) positionSpinner.getEditor()) */
        spinnerEditor.getTextField().requestFocusInWindow();
    }

    public void setMinimum(long minimum) {
        ((SpinnerNumberModel) spinner.getModel()).setMinimum(minimum);
    }

    public void setMaximum(long maximum) {
        ((SpinnerNumberModel) spinner.getModel()).setMaximum(maximum);
    }

    public void revalidateSpinner() {
        spinner.revalidate();
    }

    public void addChangeListener(ChangeListener changeListener) {
        spinner.addChangeListener(changeListener);
    }

    public void removeChangeListener(ChangeListener changeListener) {
        spinner.removeChangeListener(changeListener);
    }

    @ParametersAreNonnullByDefault
    private class PositionSpinnerEditor extends JPanel implements ChangeListener, PropertyChangeListener, LayoutManager {

        private static final int LENGTH_LIMIT = 21;

        private PositionCodeType positionCodeType = PositionCodeType.DECIMAL;

        private final char[] cache = new char[LENGTH_LIMIT];

        private final JTextField textField;
        private final JSpinner spinner;

        public PositionSpinnerEditor(JSpinner spinner) {
            this.spinner = spinner;
            textField = new JTextField();

            init();
        }

        private void init() {
            textField.setText(getPositionAsString((Long) spinner.getValue()));
            textField.addPropertyChangeListener(this);
            textField.getDocument().addDocumentListener(new DocumentListener() {
                private final PropertyChangeEvent changeEvent =
                        new PropertyChangeEvent(textField, SPINNER_PROPERTY, null, null);

                @Override
                public void changedUpdate(DocumentEvent e) {
                    notifyChanged();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    notifyChanged();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    notifyChanged();
                }

                public void notifyChanged() {
                    propertyChange(changeEvent);
                }
            });
            textField.setEditable(true);
            textField.setInheritsPopupMenu(true);

            String toolTipText = spinner.getToolTipText();
            if (toolTipText != null) {
                textField.setToolTipText(toolTipText);
            }

            add(textField);

            setLayout(this);
            spinner.addChangeListener(this);
        }

        @Nonnull
        private JTextField getTextField() {
            return textField;
        }

        @Nonnull
        private JSpinner getSpinner() {
            return spinner;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (adjusting) {
                return;
            }

            JSpinner sourceSpinner = (JSpinner) (e.getSource());
            SwingUtilities.invokeLater(() -> textField.setText(getPositionAsString((Long) sourceSpinner.getValue())));
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (adjusting) {
                return;
            }

            JSpinner sourceSpinner = getSpinner();

            Object source = e.getSource();
            String name = e.getPropertyName();
            if ((source instanceof JTextField) && SPINNER_PROPERTY.equals(name)) {
                Long lastValue = (Long) sourceSpinner.getValue();

                // Try to set the new value
                try {
                    sourceSpinner.setValue(valueOfPosition(getTextField().getText()));
                } catch (IllegalArgumentException iae) {
                    // SpinnerModel didn't like new value, reset
                    try {
                        sourceSpinner.setValue(lastValue);
                    } catch (IllegalArgumentException iae2) {
                        // Still bogus, nothing else we can do, the
                        // SpinnerModel and JFormattedTextField are now out
                        // of sync.
                    }
                }
            }
        }

        public void setPositionValue(long positionValue) {
            textField.setText(getPositionAsString(positionValue));
            spinner.setValue(positionValue);
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        /**
         * Returns the size of the parents insets.
         */
        @Nonnull
        private Dimension insetSize(Container parent) {
            Insets insets = parent.getInsets();
            int width = insets.left + insets.right;
            int height = insets.top + insets.bottom;
            return new Dimension(width, height);
        }

        @Nonnull
        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Dimension preferredSize = insetSize(parent);
            if (parent.getComponentCount() > 0) {
                Dimension childSize = getComponent(0).getPreferredSize();
                preferredSize.width += childSize.width;
                preferredSize.height += childSize.height;
            }
            return preferredSize;
        }

        @Nonnull
        @Override
        public Dimension minimumLayoutSize(Container parent) {
            Dimension minimumSize = insetSize(parent);
            if (parent.getComponentCount() > 0) {
                Dimension childSize = getComponent(0).getMinimumSize();
                minimumSize.width += childSize.width;
                minimumSize.height += childSize.height;
            }
            return minimumSize;
        }

        @Override
        public void layoutContainer(Container parent) {
            if (parent.getComponentCount() > 0) {
                Insets insets = parent.getInsets();
                int width = parent.getWidth() - (insets.left + insets.right);
                int height = parent.getHeight() - (insets.top + insets.bottom);
                getComponent(0).setBounds(insets.left, insets.top, width, height);
            }
        }

        @Nonnull
        public PositionCodeType getPositionCodeType() {
            return positionCodeType;
        }

        public void setPositionCodeType(PositionCodeType positionCodeType) {
            this.positionCodeType = positionCodeType;
        }

        @Nonnull
        private String getPositionAsString(long position) {
            if (position < 0) {
                return "-" + getNonNegativePositionAsString(-position);
            }
            return getNonNegativePositionAsString(position);
        }

        @Nonnull
        private String getNonNegativePositionAsString(long position) {
            Arrays.fill(cache, ' ');
            CodeAreaUtils.longToBaseCode(cache,
                    0,
                    position,
                    positionCodeType.getBase(),
                    LENGTH_LIMIT,
                    false,
                    CodeCharactersCase.LOWER);
            return new String(cache).trim();
        }

        private long valueOfPosition(String position) {
            return Long.parseLong(position, positionCodeType.getBase());
        }
    }
}
