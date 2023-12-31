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
package com.mucommander.viewer.binary.search.ui;

import com.mucommander.text.Translator;
import com.mucommander.viewer.binary.search.ReplaceParameters;
import com.mucommander.viewer.binary.search.SearchCondition;
import com.mucommander.viewer.binary.search.SearchParameters;
import org.exbin.auxiliary.binary_data.BinaryData;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.auxiliary.binary_data.EditableBinaryData;
import org.exbin.bined.RowWrappingMode;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.border.BevelBorder;
import javax.annotation.Nullable;
import java.awt.BorderLayout;

/**
 * Find text/hexadecimal data panel.
 */
@ParametersAreNonnullByDefault
public class FindBinaryPanel extends javax.swing.JPanel {

    private javax.swing.JRadioButton findBinaryRadioButton;
    private javax.swing.ButtonGroup findButtonGroup;
    private org.exbin.bined.swing.basic.CodeArea findCodeArea;
    private javax.swing.JPanel findPanel;
    private javax.swing.JTextField findTextField;
    private javax.swing.JRadioButton findTextRadioButton;
    private javax.swing.JCheckBox matchCaseCheckBox;
    private javax.swing.JCheckBox multipleMatchesCheckBox;
    private javax.swing.JCheckBox performReplaceCheckBox;
    private javax.swing.JCheckBox replaceAllMatchesCheckBox;
    private javax.swing.JRadioButton replaceBinaryRadioButton;
    private javax.swing.ButtonGroup replaceButtonGroup;
    private org.exbin.bined.swing.basic.CodeArea replaceCodeArea;
    private javax.swing.JLabel replaceLabel;
    private javax.swing.JPanel replacePanel;
    private javax.swing.JTextField replaceTextField;
    private javax.swing.JRadioButton replaceTextRadioButton;
    private javax.swing.JCheckBox searchFromCursorCheckBox;
    private javax.swing.JSplitPane splitPane;
    private boolean closedByAction = false;

    public FindBinaryPanel() {
        initComponents();
        init();
    }

    private void init() {
        findCodeArea.setRowWrapping(RowWrappingMode.WRAPPING);
        findCodeArea.setWrappingBytesGroupSize(0);
        findCodeArea.setBorder(new BevelBorder(BevelBorder.LOWERED));
        findCodeArea.setContentData(new ByteArrayEditableData());

        replaceCodeArea.setRowWrapping(RowWrappingMode.WRAPPING);
        replaceCodeArea.setWrappingBytesGroupSize(0);
        replaceCodeArea.setBorder(new BevelBorder(BevelBorder.LOWERED));
        replaceCodeArea.setContentData(new ByteArrayEditableData());
    }

    private void initComponents() {
        findButtonGroup = new javax.swing.ButtonGroup();
        replaceButtonGroup = new javax.swing.ButtonGroup();
        splitPane = new javax.swing.JSplitPane();
        findPanel = new javax.swing.JPanel();
        findTextRadioButton = new javax.swing.JRadioButton();
        findTextField = new javax.swing.JTextField();
        findBinaryRadioButton = new javax.swing.JRadioButton();
        findCodeArea = new org.exbin.bined.swing.basic.CodeArea();
        searchFromCursorCheckBox = new javax.swing.JCheckBox();
        matchCaseCheckBox = new javax.swing.JCheckBox();
        multipleMatchesCheckBox = new javax.swing.JCheckBox();
        replacePanel = new javax.swing.JPanel();
        performReplaceCheckBox = new javax.swing.JCheckBox();
        replaceLabel = new javax.swing.JLabel();
        replaceTextRadioButton = new javax.swing.JRadioButton();
        replaceTextField = new javax.swing.JTextField();
        replaceBinaryRadioButton = new javax.swing.JRadioButton();
        replaceCodeArea = new org.exbin.bined.swing.basic.CodeArea();
        replaceAllMatchesCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        splitPane.setDividerLocation(400);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        findPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(Translator.get("binary_viewer.find.findPanel.title")));

        findButtonGroup.add(findTextRadioButton);
        findTextRadioButton.setSelected(true);
        findTextRadioButton.setText(Translator.get("binary_viewer.find.textVariant"));
        findTextRadioButton.addChangeListener(l -> updateFindCondition());

        findButtonGroup.add(findBinaryRadioButton);
        findBinaryRadioButton.setText(Translator.get("binary_viewer.find.binaryVariant"));
        findBinaryRadioButton.addChangeListener(l -> updateFindCondition());

        findCodeArea.setEnabled(false);

        searchFromCursorCheckBox.setSelected(true);
        searchFromCursorCheckBox.setText(Translator.get("binary_viewer.find.searchFromCursor"));

        matchCaseCheckBox.setText(Translator.get("binary_viewer.find.matchCase"));

        multipleMatchesCheckBox.setSelected(true);
        multipleMatchesCheckBox.setText(Translator.get("binary_viewer.find.multipleMatches"));

        javax.swing.GroupLayout findPanelLayout = new javax.swing.GroupLayout(findPanel);
        findPanel.setLayout(findPanelLayout);
        findPanelLayout.setHorizontalGroup(
                findPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(findPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(findPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(findTextField)
                                        .addComponent(findBinaryRadioButton,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(findTextRadioButton,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(findCodeArea,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(matchCaseCheckBox,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(searchFromCursorCheckBox,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(multipleMatchesCheckBox,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE))
                                .addContainerGap()));
        findPanelLayout.setVerticalGroup(
                findPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                findPanelLayout.createSequentialGroup()
                                        .addComponent(findTextRadioButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(findTextField,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(findBinaryRadioButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(findCodeArea,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(searchFromCursorCheckBox)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(matchCaseCheckBox)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(multipleMatchesCheckBox)
                                        .addContainerGap()));

        splitPane.setLeftComponent(findPanel);

        replacePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(Translator.get("binary_viewer.find.replacePanel.title")));

        performReplaceCheckBox.setText(Translator.get("binary_viewer.find.replaceOnMatch"));
        performReplaceCheckBox.addChangeListener(l -> updateReplaceEnablement());

        replaceLabel.setText(Translator.get("binary_viewer.find.replaceText"));
        replaceLabel.setEnabled(false);

        replaceButtonGroup.add(replaceTextRadioButton);
        replaceTextRadioButton.setSelected(true);
        replaceTextRadioButton.setText(Translator.get("binary_viewer.find.textVariant"));
        replaceTextRadioButton.setEnabled(false);
        replaceTextRadioButton.addChangeListener(l -> updateReplaceEnablement());

        replaceTextField.setEnabled(false);

        replaceButtonGroup.add(replaceBinaryRadioButton);
        replaceBinaryRadioButton.setText(Translator.get("binary_viewer.find.binaryVariant"));
        replaceBinaryRadioButton.setEnabled(false);
        replaceBinaryRadioButton.addChangeListener(l -> updateReplaceEnablement());

        replaceCodeArea.setEnabled(false);

        replaceAllMatchesCheckBox.setText(Translator.get("binary_viewer.find.replaceAllMatches"));
        replaceAllMatchesCheckBox.setEnabled(false);

        javax.swing.GroupLayout replacePanelLayout = new javax.swing.GroupLayout(replacePanel);
        replacePanel.setLayout(replacePanelLayout);
        replacePanelLayout.setHorizontalGroup(
                replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(replacePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(replacePanelLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(replaceCodeArea,
                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(performReplaceCheckBox,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(replaceTextField)
                                        .addComponent(replaceBinaryRadioButton,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(replaceTextRadioButton,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addGroup(replacePanelLayout.createSequentialGroup()
                                                .addComponent(replaceLabel)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
                        .addComponent(replaceAllMatchesCheckBox,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE));
        replacePanelLayout.setVerticalGroup(
                replacePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(replacePanelLayout.createSequentialGroup()
                                .addComponent(performReplaceCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(replaceLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(replaceTextRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(replaceTextField,
                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(replaceBinaryRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(replaceCodeArea,
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(replaceAllMatchesCheckBox)
                                .addContainerGap()));

        splitPane.setRightComponent(replacePanel);

        add(splitPane, java.awt.BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public void initFocus() {
        findTextField.requestFocus();
        findTextField.selectAll();
    }

    public void hideReplaceOptions() {
        remove(splitPane);
        add(findPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    @Nonnull
    public SearchParameters getSearchParameters() {
        SearchParameters result = new SearchParameters();
        result.setCondition(getSearchCondition());
        result.setSearchFromCursor(searchFromCursorCheckBox.isSelected());
        result.setMatchCase(matchCaseCheckBox.isSelected());
        result.setMultipleMatches(multipleMatchesCheckBox.isSelected());
        return result;
    }

    public void setSearchParameters(SearchParameters parameters) {
        searchFromCursorCheckBox.setSelected(parameters.isSearchFromCursor());
        matchCaseCheckBox.setSelected(parameters.isMatchCase());
        multipleMatchesCheckBox.setSelected(parameters.isMultipleMatches());
        SearchCondition condition = parameters.getCondition();
        if (condition.getSearchMode() == SearchCondition.SearchMode.TEXT) {
            findTextRadioButton.setSelected(true);
        } else {
            findBinaryRadioButton.setSelected(true);
        }
        findTextField.setText(condition.getSearchText());
        ByteArrayEditableData contentData = (ByteArrayEditableData) findCodeArea.getContentData();
        contentData.clear();
        BinaryData conditionData = condition.getBinaryData();
        if (conditionData != null) {
            contentData.insert(0, conditionData);
        }
    }

    @Nullable
    public ReplaceParameters getReplaceParameters() {
        if (!performReplaceCheckBox.isSelected())
            return null;

        ReplaceParameters result = new ReplaceParameters();
        result.setCondition(getReplaceCondition());
        result.setPerformReplace(performReplaceCheckBox.isSelected());
        result.setReplaceAll(replaceAllMatchesCheckBox.isSelected());
        return result;
    }

    public void setReplaceParameters(ReplaceParameters parameters) {
        performReplaceCheckBox.setSelected(parameters.isPerformReplace());
        replaceAllMatchesCheckBox.setSelected(parameters.isReplaceAll());
        SearchCondition condition = parameters.getCondition();
        if (condition.getSearchMode() == SearchCondition.SearchMode.TEXT) {
            replaceTextRadioButton.setSelected(true);
        } else {
            replaceBinaryRadioButton.setSelected(true);
        }
        replaceTextField.setText(condition.getSearchText());
        ByteArrayEditableData contentData = (ByteArrayEditableData) replaceCodeArea.getContentData();
        contentData.clear();
        BinaryData conditionData = condition.getBinaryData();
        if (conditionData != null) {
            contentData.insert(0, conditionData);
        }
    }

    @Nonnull
    private SearchCondition getSearchCondition() {
        SearchCondition searchCondition = new SearchCondition();
        if (findTextRadioButton.isSelected()) {
            searchCondition.setSearchText(findTextField.getText());
        } else {
            searchCondition.setSearchMode(SearchCondition.SearchMode.BINARY);
            searchCondition.setBinaryData((EditableBinaryData) findCodeArea.getContentData());
        }

        return searchCondition;
    }

    @Nonnull
    private SearchCondition getReplaceCondition() {
        SearchCondition searchCondition = new SearchCondition();
        if (replaceTextRadioButton.isSelected()) {
            searchCondition.setSearchText(replaceTextField.getText());
        } else {
            searchCondition.setSearchMode(SearchCondition.SearchMode.BINARY);
            searchCondition.setBinaryData((EditableBinaryData) replaceCodeArea.getContentData());
        }

        return searchCondition;
    }

    private void updateReplaceEnablement() {
        boolean replaceEnabled = performReplaceCheckBox.isSelected();
        replaceTextRadioButton.setEnabled(replaceEnabled);
        replaceBinaryRadioButton.setEnabled(replaceEnabled);
        replaceAllMatchesCheckBox.setEnabled(replaceEnabled);
        replaceLabel.setEnabled(replaceEnabled);
        updateReplaceCondition();
    }

    private void updateFindCondition() {
        boolean textMode = findTextRadioButton.isSelected();
        findTextField.setEnabled(textMode);
        findCodeArea.setEnabled(!textMode);
        matchCaseCheckBox.setEnabled(textMode);
    }

    private void updateReplaceCondition() {
        boolean replaceEnabled = performReplaceCheckBox.isSelected();
        boolean textMode = replaceTextRadioButton.isSelected();
        replaceTextField.setEnabled(replaceEnabled && textMode);
        replaceCodeArea.setEnabled(replaceEnabled && !textMode);
    }

    public boolean isClosedByAction() {
        return closedByAction;
    }

    public void setClosedByAction(boolean closedByAction) {
        this.closedByAction = closedByAction;
    }
}
