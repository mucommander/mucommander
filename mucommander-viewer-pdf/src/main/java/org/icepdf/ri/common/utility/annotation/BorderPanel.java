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
package org.icepdf.ri.common.utility.annotation;

import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.annotations.Annotation;
import org.icepdf.core.pobjects.annotations.BorderStyle;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.views.AnnotationComponent;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * BorderPanel allows the configuration of an annotation's BorderStyle properties.
 *
 * @since 5.0
 */
@SuppressWarnings("serial")
public class BorderPanel extends AnnotationPanelAdapter implements ItemListener,
        ActionListener {

    // default list values.
    private static final int DEFAULT_LINK_TYPE = 1;
    private static final int DEFAULT_LINE_THICKNESS = 0;
    private static final int DEFAULT_LINE_STYLE = 0;
    private static final Color DEFAULT_BORDER_COLOR = Color.BLACK;

    // line styles.
    private static ValueLabelItem[] LINE_STYLE_LIST;

    // link action appearance properties.
    private JComboBox linkTypeBox;
    private JComboBox lineThicknessBox;
    private JComboBox lineStyleBox;
    private JButton colorButton;

    public BorderPanel(SwingController controller) {
        super(controller);
        setLayout(new GridLayout(4, 2, 5, 2));

        // Setup the basics of the panel
        setFocusable(true);

        // Add the tabbed pane to the overall panel
        createGUI();

        // Start the panel disabled until an action is clicked
        setEnabled(false);

        revalidate();
    }

    /**
     * Method that should be called when a new AnnotationComponent is selected by the user
     * The associated object will be stored locally as currentAnnotation
     * Then all of it's properties will be applied to the UI pane
     * For example if the border was red, the color of the background button will
     * be changed to red
     *
     * @param newAnnotation to set and apply to this UI
     */
    public void setAnnotationComponent(AnnotationComponent newAnnotation) {

        if (newAnnotation == null || newAnnotation.getAnnotation() == null) {
            setEnabled(false);
            return;
        }
        // assign the new action instance.
        this.currentAnnotationComponent = newAnnotation;

        // For convenience grab the Annotation object wrapped by the component
        Annotation annotation = currentAnnotationComponent.getAnnotation();

        // apply annotation values.
        if (annotation.getLineThickness() == 0) {
            applySelectedValue(linkTypeBox, Annotation.INVISIBLE_RECTANGLE);
        } else {
            applySelectedValue(linkTypeBox, Annotation.VISIBLE_RECTANGLE);
        }
        applySelectedValue(lineThicknessBox, annotation.getLineThickness());
        applySelectedValue(lineStyleBox, annotation.getLineStyle());
        setButtonBackgroundColor(colorButton, annotation.getColor());

        // disable appearance input if we have a invisible rectangle
        enableAppearanceInputComponents(annotation.getBorderType() == Annotation.VISIBLE_RECTANGLE);
    }

    public void itemStateChanged(ItemEvent e) {

        // For convenience grab the Annotation object wrapped by the component
        Annotation annotation = currentAnnotationComponent.getAnnotation();

        ValueLabelItem item = (ValueLabelItem) e.getItem();
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (e.getSource() == linkTypeBox) {
                boolean linkVisible = (Boolean) item.getValue();
                if (linkVisible) {
                    annotation.getBorderStyle().setStrokeWidth(1f);
                    // on visible set a default colour so we can see it.
                    if (annotation.getColor() == null) {
                        annotation.setColor(Color.BLACK);
                    }
                } else {
                    annotation.getBorderStyle().setStrokeWidth(0f);
                }
                applySelectedValue(lineThicknessBox, annotation.getLineThickness());
                // enable/disable fields based on types
                enableAppearanceInputComponents(linkVisible);
            } else if (e.getSource() == lineThicknessBox) {
                float lineThickness = (Float) item.getValue();
                annotation.getBorderStyle().setStrokeWidth(lineThickness);
            } else if (e.getSource() == lineStyleBox) {
                Name lineStyle = (Name) item.getValue();
                annotation.getBorderStyle().setBorderStyle(lineStyle);
            }
            // save the action state back to the document structure.
            updateCurrentAnnotation();

            currentAnnotationComponent.repaint();
        }
    }

    public void actionPerformed(ActionEvent e) {
        // For convenience grab the Annotation object wrapped by the component
        Annotation annotation = currentAnnotationComponent.getAnnotation();
        if (e.getSource() == colorButton) {
            Color chosenColor =
                    JColorChooser.showDialog(colorButton,
                            messageBundle.getString("viewer.utilityPane.annotation.border.colorChooserTitle"),
                            colorButton.getBackground());
            if (chosenColor != null) {
                // change the colour of the button background
                colorButton.setBackground(chosenColor);
                annotation.setColor(chosenColor);

                // save the action state back to the document structure.
                updateCurrentAnnotation();
                currentAnnotationComponent.repaint();
            }
        }
    }

    /**
     * Method to create link annotation GUI.
     */
    private void createGUI() {

        // line styles.
        if (LINE_STYLE_LIST == null) {
            LINE_STYLE_LIST = new ValueLabelItem[]{
                    new ValueLabelItem(BorderStyle.BORDER_STYLE_SOLID,
                            messageBundle.getString("viewer.utilityPane.annotation.border.solid")),
                    new ValueLabelItem(BorderStyle.BORDER_STYLE_DASHED,
                            messageBundle.getString("viewer.utilityPane.annotation.border.dashed")),
                    new ValueLabelItem(BorderStyle.BORDER_STYLE_BEVELED,
                            messageBundle.getString("viewer.utilityPane.annotation.border.beveled")),
                    new ValueLabelItem(BorderStyle.BORDER_STYLE_INSET,
                            messageBundle.getString("viewer.utilityPane.annotation.border.inset")),
                    new ValueLabelItem(BorderStyle.BORDER_STYLE_UNDERLINE,
                            messageBundle.getString("viewer.utilityPane.annotation.border.underline"))};
        }
        // Create and setup an Appearance panel
        setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                messageBundle.getString("viewer.utilityPane.annotation.border.title"),
                TitledBorder.LEFT,
                TitledBorder.DEFAULT_POSITION));
        // border type box
        linkTypeBox = new JComboBox(VISIBLE_TYPE_LIST);
        linkTypeBox.setSelectedIndex(DEFAULT_LINK_TYPE);
        linkTypeBox.addItemListener(this);
        add(new JLabel(
                messageBundle.getString("viewer.utilityPane.annotation.border.linkType")));
        add(linkTypeBox);
        // border thickness
        lineThicknessBox = new JComboBox(LINE_THICKNESS_LIST);
        lineThicknessBox.setSelectedIndex(DEFAULT_LINE_THICKNESS);
        lineThicknessBox.addItemListener(this);
        add(new JLabel(messageBundle.getString(
                "viewer.utilityPane.annotation.border.lineThickness")));
        add(lineThicknessBox);
        // border style
        lineStyleBox = new JComboBox(LINE_STYLE_LIST);
        lineStyleBox.setSelectedIndex(DEFAULT_LINE_STYLE);
        lineStyleBox.addItemListener(this);
        add(new JLabel(
                messageBundle.getString("viewer.utilityPane.annotation.border.lineStyle")));
        add(lineStyleBox);
        // border colour
        colorButton = new JButton(" ");
        colorButton.addActionListener(this);
        colorButton.setOpaque(true);
        colorButton.setBackground(DEFAULT_BORDER_COLOR);
        add(new JLabel(
                messageBundle.getString("viewer.utilityPane.annotation.border.colorLabel")));
        add(colorButton);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        safeEnable(linkTypeBox, enabled);
        safeEnable(lineThicknessBox, enabled);
        safeEnable(lineStyleBox, enabled);
        safeEnable(colorButton, enabled);
    }

    /**
     * Method to enable appearance input fields for an invisible rectangle
     *
     * @param visible invisible rectangle or visible, your pick.
     */
    private void enableAppearanceInputComponents(boolean visible) {
        if (!visible) {
            // everything but highlight style and link type
            safeEnable(linkTypeBox, true);
            safeEnable(lineThicknessBox, false);
            safeEnable(lineStyleBox, false);
            safeEnable(colorButton, false);
        } else {
            // enable all fields.
            safeEnable(linkTypeBox, true);
            safeEnable(lineThicknessBox, true);
            safeEnable(lineStyleBox, true);
            safeEnable(colorButton, true);
        }
    }

    /**
     * Convenience method to ensure a component is safe to toggle the enabled state on
     *
     * @param comp    to toggle
     * @param enabled the status to use
     * @return true on success
     */
    protected boolean safeEnable(JComponent comp, boolean enabled) {
        if (comp != null) {
            comp.setEnabled(enabled);
            return true;
        }
        return false;
    }

    private void applySelectedValue(JComboBox comboBox, Object value) {
        comboBox.removeItemListener(this);
        ValueLabelItem currentItem;
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            currentItem = (ValueLabelItem) comboBox.getItemAt(i);
            if (currentItem.getValue().equals(value)) {
                comboBox.setSelectedIndex(i);
                break;
            }
        }
        comboBox.addItemListener(this);
    }

}