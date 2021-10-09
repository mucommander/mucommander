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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JToolTip;

import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.EditMode;
import org.exbin.bined.EditOperation;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.SelectionRange;

import com.mucommander.text.Translator;
import com.mucommander.viewer.binary.BinaryStatusApi;
import com.mucommander.viewer.binary.StatusCursorPositionFormat;
import com.mucommander.viewer.binary.StatusDocumentSizeFormat;

/**
 * Binary editor status panel.
 */
@ParametersAreNonnullByDefault
public class BinaryStatusPanel extends javax.swing.JPanel implements BinaryStatusApi {

    public static int DEFAULT_OCTAL_SPACE_GROUP_SIZE = 4;
    public static int DEFAULT_DECIMAL_SPACE_GROUP_SIZE = 3;
    public static int DEFAULT_HEXADECIMAL_SPACE_GROUP_SIZE = 4;

    private static final String BR_TAG = "<br>";

    private StatusControlHandler statusControlHandler;

    private StatusCursorPositionFormat cursorPositionFormat = new StatusCursorPositionFormat();
    private StatusDocumentSizeFormat documentSizeFormat = new StatusDocumentSizeFormat();
    private int octalSpaceGroupSize = DEFAULT_OCTAL_SPACE_GROUP_SIZE;
    private int decimalSpaceGroupSize = DEFAULT_DECIMAL_SPACE_GROUP_SIZE;
    private int hexadecimalSpaceGroupSize = DEFAULT_HEXADECIMAL_SPACE_GROUP_SIZE;

    private EditOperation editOperation;
    private CodeAreaCaretPosition caretPosition;
    private SelectionRange selectionRange;
    private long documentSize;
    private long initialDocumentSize;

    private javax.swing.JMenu cursorPositionCodeTypeMenu;
    private javax.swing.JLabel cursorPositionLabel;
    private javax.swing.ButtonGroup cursorPositionModeButtonGroup;
    private javax.swing.JCheckBoxMenuItem cursorPositionShowOffsetCheckBoxMenuItem;
    private javax.swing.JRadioButtonMenuItem decimalCursorPositionModeRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem decimalDocumentSizeModeRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem deltaMemoryModeRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem directMemoryModeRadioButtonMenuItem;
    private javax.swing.JMenu documentSizeCodeTypeMenu;
    private javax.swing.JMenuItem documentSizeCopyMenuItem;
    private javax.swing.JLabel documentSizeLabel;
    private javax.swing.ButtonGroup documentSizeModeButtonGroup;
    private javax.swing.JPopupMenu documentSizePopupMenu;
    private javax.swing.JCheckBoxMenuItem documentSizeShowRelativeCheckBoxMenuItem;
    private javax.swing.JLabel editModeLabel;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JRadioButtonMenuItem hexadecimalCursorPositionModeRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem hexadecimalDocumentSizeModeRadioButtonMenuItem;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.ButtonGroup memoryModeButtonGroup;
    private javax.swing.JLabel memoryModeLabel;
    private javax.swing.JPopupMenu memoryModePopupMenu;
    private javax.swing.JRadioButtonMenuItem octalCursorPositionModeRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem octalDocumentSizeModeRadioButtonMenuItem;
    private javax.swing.JMenuItem positionCopyMenuItem;
    private javax.swing.JMenuItem positionGoToMenuItem;
    private javax.swing.JPopupMenu positionPopupMenu;
    private javax.swing.JRadioButtonMenuItem ramMemoryModeRadioButtonMenuItem;

    public BinaryStatusPanel() {
        initComponents();
    }

    private void initComponents() {
        positionPopupMenu = new javax.swing.JPopupMenu();
        cursorPositionCodeTypeMenu = new javax.swing.JMenu();
        octalCursorPositionModeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        decimalCursorPositionModeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        hexadecimalCursorPositionModeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        cursorPositionShowOffsetCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        positionCopyMenuItem = new javax.swing.JMenuItem();
        positionGoToMenuItem = new javax.swing.JMenuItem();
        documentSizePopupMenu = new javax.swing.JPopupMenu();
        documentSizeCodeTypeMenu = new javax.swing.JMenu();
        octalDocumentSizeModeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        decimalDocumentSizeModeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        hexadecimalDocumentSizeModeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        documentSizeShowRelativeCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        documentSizeCopyMenuItem = new javax.swing.JMenuItem();
        memoryModePopupMenu = new javax.swing.JPopupMenu();
        deltaMemoryModeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        directMemoryModeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        ramMemoryModeRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        memoryModeButtonGroup = new javax.swing.ButtonGroup();
        documentSizeModeButtonGroup = new javax.swing.ButtonGroup();
        cursorPositionModeButtonGroup = new javax.swing.ButtonGroup();
        memoryModeLabel = new javax.swing.JLabel();
        documentSizeLabel = new javax.swing.JLabel() {
            @Override
            public JToolTip createToolTip() {
                updateDocumentSizeToolTip();
                return super.createToolTip();
            }
        };

        cursorPositionLabel = new javax.swing.JLabel() {
            @Override
            public JToolTip createToolTip() {
                updateCursorPositionToolTip();
                return super.createToolTip();
            }
        };

        editModeLabel = new javax.swing.JLabel();
        encodingLabel = new javax.swing.JLabel();

        cursorPositionCodeTypeMenu.setText(Translator.get("binary_viewer.status.cursorPositionCodeTypeMenu.text"));

        cursorPositionModeButtonGroup.add(octalCursorPositionModeRadioButtonMenuItem);
        octalCursorPositionModeRadioButtonMenuItem.setText(Translator.get(
                "binary_viewer.status.octalCursorPositionModeRadioButtonMenuItem.text"));
        octalCursorPositionModeRadioButtonMenuItem
                .addActionListener(this::octalCursorPositionModeRadioButtonMenuItemActionPerformed);
        cursorPositionCodeTypeMenu.add(octalCursorPositionModeRadioButtonMenuItem);

        cursorPositionModeButtonGroup.add(decimalCursorPositionModeRadioButtonMenuItem);
        decimalCursorPositionModeRadioButtonMenuItem.setSelected(true);
        decimalCursorPositionModeRadioButtonMenuItem.setText(Translator.get(
                "binary_viewer.status.decimalCursorPositionModeRadioButtonMenuItem.text"));
        decimalCursorPositionModeRadioButtonMenuItem
                .addActionListener(this::decimalCursorPositionModeRadioButtonMenuItemActionPerformed);
        cursorPositionCodeTypeMenu.add(decimalCursorPositionModeRadioButtonMenuItem);

        cursorPositionModeButtonGroup.add(hexadecimalCursorPositionModeRadioButtonMenuItem);
        hexadecimalCursorPositionModeRadioButtonMenuItem.setText(Translator.get(
                "binary_viewer.status.hexadecimalCursorPositionModeRadioButtonMenuItem.text"));
        hexadecimalCursorPositionModeRadioButtonMenuItem
                .addActionListener(this::hexadecimalCursorPositionModeRadioButtonMenuItemActionPerformed);
        cursorPositionCodeTypeMenu.add(hexadecimalCursorPositionModeRadioButtonMenuItem);

        positionPopupMenu.add(cursorPositionCodeTypeMenu);

        cursorPositionShowOffsetCheckBoxMenuItem.setSelected(true);
        cursorPositionShowOffsetCheckBoxMenuItem.setText(Translator.get(
                "binary_viewer.status.cursorPositionShowOffsetCheckBoxMenuItem.text"));
        cursorPositionShowOffsetCheckBoxMenuItem
                .addActionListener(this::cursorPositionShowOffsetCheckBoxMenuItemActionPerformed);
        positionPopupMenu.add(cursorPositionShowOffsetCheckBoxMenuItem);

        positionPopupMenu.add(jSeparator2);

        positionCopyMenuItem.setText(Translator.get("binary_viewer.status.positionCopyMenuItem.text"));
        positionCopyMenuItem.addActionListener(this::positionCopyMenuItemActionPerformed);
        positionPopupMenu.add(positionCopyMenuItem);

        positionGoToMenuItem.setText(Translator.get("binary_viewer.status.positionGoToMenuItem.text"));
        positionGoToMenuItem.addActionListener(this::positionGoToMenuItemActionPerformed);
        positionPopupMenu.add(positionGoToMenuItem);

        documentSizeCodeTypeMenu.setText(Translator.get("binary_viewer.status.documentSizeCodeTypeMenu.text"));

        documentSizeModeButtonGroup.add(octalDocumentSizeModeRadioButtonMenuItem);
        octalDocumentSizeModeRadioButtonMenuItem.setText(Translator.get(
                "binary_viewer.status.octDocumentSizeModeRadioButtonMenuItem.text"));
        octalDocumentSizeModeRadioButtonMenuItem
                .addActionListener(this::octalDocumentSizeModeRadioButtonMenuItemActionPerformed);
        documentSizeCodeTypeMenu.add(octalDocumentSizeModeRadioButtonMenuItem);

        documentSizeModeButtonGroup.add(decimalDocumentSizeModeRadioButtonMenuItem);
        decimalDocumentSizeModeRadioButtonMenuItem.setSelected(true);
        decimalDocumentSizeModeRadioButtonMenuItem.setText(Translator.get(
                "binary_viewer.status.decDocumentSizeModeRadioButtonMenuItem.text"));
        decimalDocumentSizeModeRadioButtonMenuItem
                .addActionListener(this::decimalDocumentSizeModeRadioButtonMenuItemActionPerformed);
        documentSizeCodeTypeMenu.add(decimalDocumentSizeModeRadioButtonMenuItem);

        documentSizeModeButtonGroup.add(hexadecimalDocumentSizeModeRadioButtonMenuItem);
        hexadecimalDocumentSizeModeRadioButtonMenuItem.setText(Translator.get(
                "binary_viewer.status.hexDocumentSizeModeRadioButtonMenuItem.text"));
        hexadecimalDocumentSizeModeRadioButtonMenuItem
                .addActionListener(this::hexadecimalDocumentSizeModeRadioButtonMenuItemActionPerformed);
        documentSizeCodeTypeMenu.add(hexadecimalDocumentSizeModeRadioButtonMenuItem);

        documentSizePopupMenu.add(documentSizeCodeTypeMenu);

        documentSizeShowRelativeCheckBoxMenuItem.setSelected(true);
        documentSizeShowRelativeCheckBoxMenuItem.setText(Translator.get(
                "binary_viewer.status.showRelativeCheckBoxMenuItem.text"));
        documentSizeShowRelativeCheckBoxMenuItem
                .addActionListener(this::documentSizeShowRelativeCheckBoxMenuItemActionPerformed);
        documentSizePopupMenu.add(documentSizeShowRelativeCheckBoxMenuItem);

        documentSizePopupMenu.add(jSeparator1);

        documentSizeCopyMenuItem.setText(Translator.get("binary_viewer.status.documentSizeCopyMenuItem.text"));
        documentSizeCopyMenuItem.addActionListener(this::documentSizeCopyMenuItemActionPerformed);
        documentSizePopupMenu.add(documentSizeCopyMenuItem);

        memoryModeButtonGroup.add(deltaMemoryModeRadioButtonMenuItem);
        deltaMemoryModeRadioButtonMenuItem.setSelected(true);
        deltaMemoryModeRadioButtonMenuItem.setText(Translator.get(
                "binary_viewer.status.deltaMemoryModeRadioButtonMenuItem.text"));
        deltaMemoryModeRadioButtonMenuItem.addActionListener(this::deltaMemoryModeRadioButtonMenuItemActionPerformed);
        memoryModePopupMenu.add(deltaMemoryModeRadioButtonMenuItem);

        memoryModeButtonGroup.add(directMemoryModeRadioButtonMenuItem);
        directMemoryModeRadioButtonMenuItem.setSelected(true);
        directMemoryModeRadioButtonMenuItem.setText(Translator.get(
                "binary_viewer.status.directMemoryModeRadioButtonMenuItem.text"));
        directMemoryModeRadioButtonMenuItem.addActionListener(this::directMemoryModeRadioButtonMenuItemActionPerformed);
        memoryModePopupMenu.add(directMemoryModeRadioButtonMenuItem);

        memoryModeButtonGroup.add(ramMemoryModeRadioButtonMenuItem);
        ramMemoryModeRadioButtonMenuItem.setText(Translator.get(
                "binary_viewer.status.ramMemoryModeRadioButtonMenuItem.text"));
        ramMemoryModeRadioButtonMenuItem.addActionListener(this::ramMemoryModeRadioButtonMenuItemActionPerformed);
        memoryModePopupMenu.add(ramMemoryModeRadioButtonMenuItem);

        memoryModeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        memoryModeLabel.setText(Translator.get("binary_viewer.status.memoryModeLabel.text"));
        memoryModeLabel.setToolTipText(Translator.get("binary_viewer.status.memoryModeLabel.toolTipText"));
        memoryModeLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        memoryModeLabel.setComponentPopupMenu(memoryModePopupMenu);

        documentSizeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        documentSizeLabel.setText("0 (0)");
        documentSizeLabel.setToolTipText(Translator.get("binary_viewer.status.documentSizeLabel.toolTipText"));
        documentSizeLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        documentSizeLabel.setComponentPopupMenu(documentSizePopupMenu);

        cursorPositionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cursorPositionLabel.setText("0:0");
        cursorPositionLabel.setToolTipText(Translator.get("binary_viewer.status.cursorPositionLabel.toolTipText"));
        cursorPositionLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cursorPositionLabel.setComponentPopupMenu(positionPopupMenu);
        cursorPositionLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cursorPositionLabelMouseClicked(evt);
            }
        });

        editModeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        editModeLabel.setText(Translator.get("binary_viewer.edit_operation.overwrite"));
        editModeLabel.setToolTipText(Translator.get("binary_viewer.status.editModeLabel.toolTipText"));
        editModeLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        editModeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                editModeLabelMouseClicked(evt);
            }
        });

        encodingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        encodingLabel.setText("UTF-8");
        encodingLabel.setToolTipText(Translator.get("binary_viewer.status.encodingLabel.toolTipText"));
        encodingLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        encodingLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                encodingLabelMousePressed(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                encodingLabelMouseReleased(evt);
            }

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                encodingLabelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                layout.createSequentialGroup()
                                        .addContainerGap(195, Short.MAX_VALUE)
                                        .addComponent(encodingLabel,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                148,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(documentSizeLabel,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                168,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(cursorPositionLabel,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                168,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(memoryModeLabel,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                16,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(editModeLabel,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                35,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(editModeLabel,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                        .addComponent(documentSizeLabel,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                        .addComponent(memoryModeLabel,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                        .addComponent(cursorPositionLabel,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                        .addComponent(encodingLabel,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE));
    }

    private void editModeLabelMouseClicked(java.awt.event.MouseEvent evt) {
        if (statusControlHandler != null && evt.getButton() == MouseEvent.BUTTON1) {
            if (editOperation == EditOperation.INSERT) {
                statusControlHandler.changeEditOperation(EditOperation.OVERWRITE);
            } else if (editOperation == EditOperation.OVERWRITE) {
                statusControlHandler.changeEditOperation(EditOperation.INSERT);
            }
        }
    }

    private void cursorPositionLabelMouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() > 1) {
            statusControlHandler.changeCursorPosition();
        }
    }

    private void positionGoToMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        statusControlHandler.changeCursorPosition();
    }

    private void positionCopyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(cursorPositionLabel.getText()), null);
        } catch (IllegalStateException ex) {
            // ignore issues with clipboard
        }
    }

    private void documentSizeCopyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(documentSizeLabel.getText()), null);
        } catch (IllegalStateException ex) {
            // ignore issues with clipboard
        }
    }

    private void encodingLabelMouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON1) {
            statusControlHandler.cycleEncodings();
        } else {
            handleEncodingPopup(evt);
        }
    }

    private void encodingLabelMousePressed(java.awt.event.MouseEvent evt) {
        handleEncodingPopup(evt);
    }

    private void encodingLabelMouseReleased(java.awt.event.MouseEvent evt) {
        handleEncodingPopup(evt);
    }

    private void deltaMemoryModeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        statusControlHandler.changeMemoryMode(MemoryMode.DELTA_MODE);
    }

    private void directMemoryModeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        statusControlHandler.changeMemoryMode(MemoryMode.DIRECT_ACCESS);
    }

    private void ramMemoryModeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        statusControlHandler.changeMemoryMode(MemoryMode.RAM_MEMORY);
    }

    private void cursorPositionShowOffsetCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        cursorPositionFormat.setShowOffset(cursorPositionShowOffsetCheckBoxMenuItem.isSelected());
        updateCaretPosition();
    }

    private void documentSizeShowRelativeCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        documentSizeFormat.setShowRelative(documentSizeShowRelativeCheckBoxMenuItem.isSelected());
        updateDocumentSize();
        updateDocumentSizeToolTip();
    }

    private void octalCursorPositionModeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        cursorPositionFormat.setCodeType(PositionCodeType.OCTAL);
        updateCaretPosition();
    }

    private void decimalCursorPositionModeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        cursorPositionFormat.setCodeType(PositionCodeType.DECIMAL);
        updateCaretPosition();
    }

    private void hexadecimalCursorPositionModeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        cursorPositionFormat.setCodeType(PositionCodeType.HEXADECIMAL);
        updateCaretPosition();
    }

    private void octalDocumentSizeModeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        documentSizeFormat.setCodeType(PositionCodeType.OCTAL);
        updateDocumentSize();
    }

    private void decimalDocumentSizeModeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        documentSizeFormat.setCodeType(PositionCodeType.DECIMAL);
        updateDocumentSize();
    }

    private void hexadecimalDocumentSizeModeRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        documentSizeFormat.setCodeType(PositionCodeType.HEXADECIMAL);
        updateDocumentSize();
    }

    private void handleEncodingPopup(java.awt.event.MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            statusControlHandler.encodingsPopupEncodingsMenu(evt);
        }
    }

    @Override
    public void setCursorPosition(CodeAreaCaretPosition caretPosition) {
        this.caretPosition = caretPosition;
        updateCaretPosition();
        updateCursorPositionToolTip();
    }

    @Override
    public void setSelectionRange(SelectionRange selectionRange) {
        this.selectionRange = selectionRange;
        updateCaretPosition();
        updateCursorPositionToolTip();
        updateDocumentSize();
        updateDocumentSizeToolTip();
    }

    @Override
    public void setCurrentDocumentSize(long documentSize, long initialDocumentSize) {
        this.documentSize = documentSize;
        this.initialDocumentSize = initialDocumentSize;
        updateDocumentSize();
        updateDocumentSizeToolTip();
    }

    @Nonnull
    public String getEncoding() {
        return encodingLabel.getText();
    }

    public void setEncoding(String encodingName) {
        encodingLabel.setText(encodingName);
    }

    @Override
    public void setEditMode(EditMode editMode, EditOperation editOperation) {
        this.editOperation = editOperation;
        switch (editMode) {
        case READ_ONLY: {
            editModeLabel.setText(Translator.get("binary_viewer.edit_mode.read_only"));
            break;
        }
        case EXPANDING:
        case CAPPED: {
            switch (editOperation) {
            case INSERT: {
                editModeLabel.setText(Translator.get("binary_viewer.edit_operation.insert"));
                break;
            }
            case OVERWRITE: {
                editModeLabel.setText(Translator.get("binary_viewer.edit_operation.overwrite"));
                break;
            }
            default:
                throw CodeAreaUtils.getInvalidTypeException(editOperation);
            }
            break;
        }
        case INPLACE: {
            editModeLabel.setText(Translator.get("binary_viewer.edit_mode.in_place"));
            break;
        }
        default:
            throw CodeAreaUtils.getInvalidTypeException(editMode);
        }
    }

    @Override
    public void setControlHandler(StatusControlHandler statusControlHandler) {
        this.statusControlHandler = statusControlHandler;
    }

    @Override
    public void setMemoryMode(BinaryStatusApi.MemoryMode memoryMode) {
        memoryModeLabel.setText(memoryMode.getDisplayChar());
        boolean enabled = false; // TODO memoryMode != MemoryMode.READ_ONLY;
        deltaMemoryModeRadioButtonMenuItem.setEnabled(false); // Not available
        directMemoryModeRadioButtonMenuItem.setEnabled(enabled);
        ramMemoryModeRadioButtonMenuItem.setEnabled(enabled);
        if (memoryMode == MemoryMode.RAM_MEMORY) {
            ramMemoryModeRadioButtonMenuItem.setSelected(true);
        } else if (memoryMode == MemoryMode.DIRECT_ACCESS) {
            directMemoryModeRadioButtonMenuItem.setSelected(true);
        } else {
            deltaMemoryModeRadioButtonMenuItem.setSelected(true);
        }
    }

    private void updateCaretPosition() {
        if (caretPosition == null) {
            cursorPositionLabel.setText("-");
        } else {
            StringBuilder labelBuilder = new StringBuilder();
            if (selectionRange != null && !selectionRange.isEmpty()) {
                long first = selectionRange.getFirst();
                long last = selectionRange.getLast();
                labelBuilder.append(numberToPosition(first, cursorPositionFormat.getCodeType()));
                labelBuilder.append(" to ");
                labelBuilder.append(numberToPosition(last, cursorPositionFormat.getCodeType()));
            } else {
                labelBuilder.append(numberToPosition(caretPosition.getDataPosition(),
                        cursorPositionFormat.getCodeType()));
                if (cursorPositionFormat.isShowOffset()) {
                    labelBuilder.append(":");
                    labelBuilder.append(caretPosition.getCodeOffset());
                }
            }
            cursorPositionLabel.setText(labelBuilder.toString());
        }
    }

    private void updateCursorPositionToolTip() {
        StringBuilder builder = new StringBuilder();
        builder.append("<html>");
        if (caretPosition == null) {
            builder.append(Translator.get("binary_viewer.status.cursorPositionLabel.toolTipText"));
        } else {
            String octLabel = Translator.get("binary_viewer.code_type.oct") + ": ";
            String decLabel = Translator.get("binary_viewer.code_type.dec") + ": ";
            String hexLabel = Translator.get("binary_viewer.code_type.hex") + ": ";

            if (selectionRange != null && !selectionRange.isEmpty()) {
                long first = selectionRange.getFirst();
                long last = selectionRange.getLast();
                builder.append(Translator.get("binary_viewer.status.selectionFromLabel.toolTipText")).append(BR_TAG);
                builder.append(octLabel).append(numberToPosition(first, PositionCodeType.OCTAL)).append(BR_TAG);
                builder.append(decLabel).append(numberToPosition(first, PositionCodeType.DECIMAL)).append(BR_TAG);
                builder.append(hexLabel).append(numberToPosition(first, PositionCodeType.HEXADECIMAL)).append(BR_TAG);
                builder.append(BR_TAG);
                builder.append(Translator.get("binary_viewer.status.selectionToLabel.toolTipText")).append(BR_TAG);
                builder.append(octLabel).append(numberToPosition(last, PositionCodeType.OCTAL)).append(BR_TAG);
                builder.append(decLabel).append(numberToPosition(last, PositionCodeType.DECIMAL)).append(BR_TAG);
                builder.append(hexLabel).append(numberToPosition(first, PositionCodeType.HEXADECIMAL)).append(BR_TAG);
            } else {
                long dataPosition = caretPosition.getDataPosition();
                builder.append(Translator.get("binary_viewer.status.cursorPositionLabel.toolTipText")).append(BR_TAG);
                builder.append(octLabel).append(numberToPosition(dataPosition, PositionCodeType.OCTAL)).append(BR_TAG);
                builder.append(decLabel)
                        .append(numberToPosition(dataPosition, PositionCodeType.DECIMAL))
                        .append(BR_TAG);
                builder.append(hexLabel).append(numberToPosition(dataPosition, PositionCodeType.HEXADECIMAL));
                builder.append("</html>");
            }
        }

        cursorPositionLabel.setToolTipText(builder.toString());
    }

    private void updateDocumentSize() {
        if (documentSize == -1) {
            documentSizeLabel.setText(documentSizeFormat.isShowRelative() ? "0 (0)" : "0");
        } else {
            StringBuilder labelBuilder = new StringBuilder();
            if (selectionRange != null && !selectionRange.isEmpty()) {
                labelBuilder.append(numberToPosition(selectionRange.getLength(), documentSizeFormat.getCodeType()));
                labelBuilder.append(" of ");
                labelBuilder.append(numberToPosition(documentSize, documentSizeFormat.getCodeType()));
            } else {
                labelBuilder.append(numberToPosition(documentSize, documentSizeFormat.getCodeType()));
                if (documentSizeFormat.isShowRelative()) {
                    long difference = documentSize - initialDocumentSize;
                    labelBuilder.append(difference > 0 ? " (+" : " (");
                    labelBuilder.append(numberToPosition(difference, documentSizeFormat.getCodeType()));
                    labelBuilder.append(")");

                }
            }

            documentSizeLabel.setText(labelBuilder.toString());
        }
    }

    private void updateDocumentSizeToolTip() {
        StringBuilder builder = new StringBuilder();
        builder.append("<html>");
        String octLabel = Translator.get("binary_viewer.code_type.oct") + ": ";
        String decLabel = Translator.get("binary_viewer.code_type.dec") + ": ";
        String hexLabel = Translator.get("binary_viewer.code_type.hex") + ": ";

        if (selectionRange != null && !selectionRange.isEmpty()) {
            long length = selectionRange.getLength();
            builder.append(Translator.get("binary_viewer.status.selectionLengthLabel.toolTipText")).append(BR_TAG);
            builder.append(octLabel).append(numberToPosition(length, PositionCodeType.OCTAL)).append(BR_TAG);
            builder.append(decLabel).append(numberToPosition(length, PositionCodeType.DECIMAL)).append(BR_TAG);
            builder.append(hexLabel).append(numberToPosition(length, PositionCodeType.HEXADECIMAL)).append(BR_TAG);
            builder.append(BR_TAG);
        }

        builder.append(Translator.get("binary_viewer.status.documentSizeLabel.toolTipText")).append(BR_TAG);
        builder.append(octLabel).append(numberToPosition(documentSize, PositionCodeType.OCTAL)).append(BR_TAG);
        builder.append(decLabel).append(numberToPosition(documentSize, PositionCodeType.DECIMAL)).append(BR_TAG);
        builder.append(hexLabel).append(numberToPosition(documentSize, PositionCodeType.HEXADECIMAL));
        builder.append("</html>");
        documentSizeLabel.setToolTipText(builder.toString());
    }

    @Nonnull
    private String numberToPosition(long value, PositionCodeType codeType) {
        if (value == 0) {
            return "0";
        }

        int spaceGroupSize;
        switch (codeType) {
        case OCTAL: {
            spaceGroupSize = octalSpaceGroupSize;
            break;
        }
        case DECIMAL: {
            spaceGroupSize = decimalSpaceGroupSize;
            break;
        }
        case HEXADECIMAL: {
            spaceGroupSize = hexadecimalSpaceGroupSize;
            break;
        }
        default:
            throw CodeAreaUtils.getInvalidTypeException(codeType);
        }

        long remainder = value > 0 ? value : -value;
        StringBuilder builder = new StringBuilder();
        int base = codeType.getBase();
        int groupSize = spaceGroupSize == 0 ? -1 : spaceGroupSize;
        while (remainder > 0) {
            if (groupSize >= 0) {
                if (groupSize == 0) {
                    builder.insert(0, ' ');
                    groupSize = spaceGroupSize - 1;
                } else {
                    groupSize--;
                }
            }

            int digit = (int) (remainder % base);
            remainder = remainder / base;
            builder.insert(0, CodeAreaUtils.UPPER_HEX_CODES[digit]);
        }

        if (value < 0) {
            builder.insert(0, "-");
        }
        return builder.toString();
    }
}
