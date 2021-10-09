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

import javax.annotation.ParametersAreNonnullByDefault;

import org.exbin.bined.CodeAreaUtils;

import com.mucommander.text.Translator;
import com.mucommander.viewer.binary.GoToBinaryPositionMode;

/**
 * Go-to position panel for binary editor.
 */
@ParametersAreNonnullByDefault
public class GoToBinaryPanel extends javax.swing.JPanel {

    private long cursorPosition;
    private long maxPosition;
    private GoToBinaryPositionMode goToMode = GoToBinaryPositionMode.FROM_START;

    private BaseSwitchableSpinnerPanel baseSwitchableSpinnerPanel;
    private javax.swing.JLabel currentPositionLabel;
    private javax.swing.JTextField currentPositionTextField;
    private javax.swing.JRadioButton fromCursorRadioButton;
    private javax.swing.JRadioButton fromEndRadioButton;
    private javax.swing.JRadioButton fromStartRadioButton;
    private javax.swing.JPanel goToPanel;
    private javax.swing.JLabel positionLabel;
    private javax.swing.ButtonGroup positionTypeButtonGroup;
    private javax.swing.JLabel targetPositionLabel;
    private javax.swing.JTextField targetPositionTextField;

    public GoToBinaryPanel() {
        initComponents();

        baseSwitchableSpinnerPanel.setMinimum(0L);
        baseSwitchableSpinnerPanel.addChangeListener((javax.swing.event.ChangeEvent evt) -> updateTargetPosition());
    }

    private void initComponents() {
        positionTypeButtonGroup = new javax.swing.ButtonGroup();
        currentPositionLabel = new javax.swing.JLabel();
        currentPositionTextField = new javax.swing.JTextField();
        goToPanel = new javax.swing.JPanel();
        fromStartRadioButton = new javax.swing.JRadioButton();
        fromEndRadioButton = new javax.swing.JRadioButton();
        fromCursorRadioButton = new javax.swing.JRadioButton();
        positionLabel = new javax.swing.JLabel();
        baseSwitchableSpinnerPanel = new BaseSwitchableSpinnerPanel();
        targetPositionLabel = new javax.swing.JLabel();
        targetPositionTextField = new javax.swing.JTextField();

        currentPositionLabel.setText(Translator.get("binary_viewer.go_to.currentPositionLabel.text"));

        currentPositionTextField.setEditable(false);
        currentPositionTextField.setText("0");

        goToPanel.setBorder(
                javax.swing.BorderFactory.createTitledBorder(Translator.get("binary_viewer.go_to.border.title")));

        positionTypeButtonGroup.add(fromStartRadioButton);
        fromStartRadioButton.setSelected(true);
        fromStartRadioButton.setText(Translator.get("binary_viewer.go_to.fromStartRadioButton.text"));
        fromStartRadioButton.addItemListener(this::fromStartRadioButtonItemStateChanged);

        positionTypeButtonGroup.add(fromEndRadioButton);
        fromEndRadioButton.setText(Translator.get("binary_viewer.go_to.fromEndRadioButton.text"));
        fromEndRadioButton.addItemListener(this::fromEndRadioButtonItemStateChanged);

        positionTypeButtonGroup.add(fromCursorRadioButton);
        fromCursorRadioButton.setText(Translator.get("binary_viewer.go_to.fromCursorRadioButton.text"));
        fromCursorRadioButton.addItemListener(this::fromCursorRadioButtonItemStateChanged);

        positionLabel.setText(Translator.get("binary_viewer.go_to.positionLabel.text"));

        javax.swing.GroupLayout goToPanelLayout = new javax.swing.GroupLayout(goToPanel);
        goToPanel.setLayout(goToPanelLayout);
        goToPanelLayout.setHorizontalGroup(
                goToPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(fromStartRadioButton,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                        .addComponent(fromCursorRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
                        .addComponent(fromEndRadioButton,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                        .addGroup(goToPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(goToPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(baseSwitchableSpinnerPanel,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addGroup(goToPanelLayout.createSequentialGroup()
                                                .addComponent(positionLabel)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap()));
        goToPanelLayout.setVerticalGroup(
                goToPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(goToPanelLayout.createSequentialGroup()
                                .addComponent(fromStartRadioButton,
                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                        22,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fromEndRadioButton,
                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                        22,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fromCursorRadioButton,
                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                        20,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(positionLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(baseSwitchableSpinnerPanel,
                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        targetPositionLabel.setText(Translator.get("binary_viewer.go_to.targetPositionLabel.text"));

        targetPositionTextField.setEditable(false);
        targetPositionTextField.setText("0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(currentPositionTextField)
                                        .addComponent(goToPanel,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(targetPositionTextField)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(currentPositionLabel)
                                                        .addComponent(targetPositionLabel))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(currentPositionLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(currentPositionTextField,
                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(goToPanel,
                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(targetPositionLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(targetPositionTextField,
                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    }

    private void fromStartRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {
        if (fromStartRadioButton.isSelected()) {
            switchGoToMode(GoToBinaryPositionMode.FROM_START);
        }
    }

    private void fromEndRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {
        if (fromEndRadioButton.isSelected()) {
            switchGoToMode(GoToBinaryPositionMode.FROM_END);
        }
    }

    private void fromCursorRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {
        if (fromCursorRadioButton.isSelected()) {
            switchGoToMode(GoToBinaryPositionMode.FROM_CURSOR);
        }
    }

    private void updateTargetPosition() {
        targetPositionTextField.setText(String.valueOf(getTargetPosition()));
    }

    public void initFocus() {
        baseSwitchableSpinnerPanel.initFocus();
    }

    public long getTargetPosition() {
        long absolutePosition;
        long position = getPositionValue();
        switch (goToMode) {
        case FROM_START:
            absolutePosition = position;
            break;
        case FROM_END:
            absolutePosition = maxPosition - position;
            break;
        case FROM_CURSOR:
            absolutePosition = cursorPosition + position;
            break;
        default:
            throw CodeAreaUtils.getInvalidTypeException(goToMode);
        }

        if (absolutePosition < 0) {
            absolutePosition = 0;
        } else if (absolutePosition > maxPosition) {
            absolutePosition = maxPosition;
        }
        return absolutePosition;
    }

    public void setTargetPosition(long absolutePosition) {
        if (absolutePosition < 0) {
            absolutePosition = 0;
        } else if (absolutePosition > maxPosition) {
            absolutePosition = maxPosition;
        }
        switch (goToMode) {
        case FROM_START:
            setPositionValue(absolutePosition);
            break;
        case FROM_END:
            setPositionValue(maxPosition - absolutePosition);
            break;
        case FROM_CURSOR:
            setPositionValue(absolutePosition - cursorPosition);
            break;
        default:
            throw CodeAreaUtils.getInvalidTypeException(goToMode);
        }
        updateTargetPosition();
    }

    public long getCursorPosition() {
        return cursorPosition;
    }

    public void setCursorPosition(long cursorPosition) {
        this.cursorPosition = cursorPosition;
        setPositionValue(cursorPosition);
        currentPositionTextField.setText(String.valueOf(cursorPosition));
    }

    public void setMaxPosition(long maxPosition) {
        this.maxPosition = maxPosition;
        baseSwitchableSpinnerPanel.setMaximum(maxPosition);
        updateTargetPosition();
    }

    public void setSelected() {
        baseSwitchableSpinnerPanel.requestFocusInWindow();
    }

    private void switchGoToMode(GoToBinaryPositionMode goToMode) {
        if (this.goToMode == goToMode) {
            return;
        }

        long absolutePosition = getTargetPosition();
        this.goToMode = goToMode;
        switch (goToMode) {
        case FROM_START:
        case FROM_END: {
            setPositionValue(0L);
            baseSwitchableSpinnerPanel.setMinimum(0L);
            baseSwitchableSpinnerPanel.setMaximum(maxPosition);
            baseSwitchableSpinnerPanel.revalidateSpinner();
            break;
        }
        case FROM_CURSOR: {
            setPositionValue(0L);
            baseSwitchableSpinnerPanel.setMinimum(-cursorPosition);
            baseSwitchableSpinnerPanel.setMaximum(maxPosition - cursorPosition);
            baseSwitchableSpinnerPanel.revalidateSpinner();
            break;
        }
        default:
            throw CodeAreaUtils.getInvalidTypeException(goToMode);
        }
        setTargetPosition(absolutePosition);
    }

    private long getPositionValue() {
        return baseSwitchableSpinnerPanel.getValue();
    }

    private void setPositionValue(long value) {
        baseSwitchableSpinnerPanel.setValue(value);
        updateTargetPosition();
    }

    public void acceptInput() {
        baseSwitchableSpinnerPanel.acceptInput();
    }
}
