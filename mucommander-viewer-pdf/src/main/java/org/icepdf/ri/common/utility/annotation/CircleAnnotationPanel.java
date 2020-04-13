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
import org.icepdf.core.pobjects.annotations.CircleAnnotation;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.views.AnnotationComponent;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * CircleAnnotationPanel is a configuration panel for changing the properties
 * of a CircleAnnotationComponent and the underlying annotation component.
 *
 * @since 5.0
 */
@SuppressWarnings("serial")
public class CircleAnnotationPanel extends AnnotationPanelAdapter implements ItemListener, ChangeListener,
        ActionListener {

    // default list values.
    private static final int DEFAULT_LINE_THICKNESS = 0;
    private static final int DEFAULT_LINE_STYLE = 0;
    private static final int DEFAULT_STROKE_TYPE = 0;
    private static final Color DEFAULT_BORDER_COLOR = Color.RED;
    private static final int DEFAULT_FILL_TYPE = 1;
    private static final Color DEFAULT_INTERIOR_COLOR = new Color(1, 1, 1);

    // link action appearance properties.
    private JComboBox lineThicknessBox;
    private JComboBox lineStyleBox;
    private JComboBox fillTypeBox;
    private JButton colorFillButton;
    private JButton colorBorderButton;
    private JSlider transparencySlider;

    private CircleAnnotation annotation;

    public CircleAnnotationPanel(SwingController controller) {
        super(controller);

        setLayout(new GridBagLayout());

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
        annotation = (CircleAnnotation)
                currentAnnotationComponent.getAnnotation();

        applySelectedValue(lineThicknessBox, annotation.getLineThickness());
        applySelectedValue(lineStyleBox, annotation.getLineStyle());
        applySelectedValue(fillTypeBox, annotation.isFillColor());
        setButtonBackgroundColor(colorBorderButton, annotation.getColor());
        setButtonBackgroundColor(colorFillButton, annotation.getFillColor());
        transparencySlider.setValue(Math.round(annotation.getOpacity() * 255));

        // disable appearance input if we have a invisible rectangle
        safeEnable(lineThicknessBox, true);
        safeEnable(lineStyleBox, true);
        safeEnable(colorFillButton, true);
        safeEnable(fillTypeBox, true);
        safeEnable(colorBorderButton, true);
        safeEnable(transparencySlider, true);

        setStrokeFillColorButtons();
    }

    private void setStrokeFillColorButtons() {
        CircleAnnotation circleAnnotation = (CircleAnnotation)
                currentAnnotationComponent.getAnnotation();
        if (annotation.isFillColor()) {
            setButtonBackgroundColor(colorFillButton, circleAnnotation.getFillColor());
            safeEnable(colorFillButton, true);
        } else {
            safeEnable(colorFillButton, false);
        }
    }

    public void itemStateChanged(ItemEvent e) {
        ValueLabelItem item = (ValueLabelItem) e.getItem();
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (e.getSource() == lineThicknessBox) {
                annotation.getBorderStyle().setStrokeWidth((Float) item.getValue());
            } else if (e.getSource() == lineStyleBox) {
                annotation.getBorderStyle().setBorderStyle((Name) item.getValue());
            } else if (e.getSource() == fillTypeBox) {
                annotation.setFillColor((Boolean) item.getValue());
                setStrokeFillColorButtons();
            }
            // save the action state back to the document structure.
            updateCurrentAnnotation();
            currentAnnotationComponent.resetAppearanceShapes();
            currentAnnotationComponent.repaint();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == colorBorderButton) {
            Color chosenColor =
                    JColorChooser.showDialog(colorBorderButton,
                            messageBundle.getString(
                                    "viewer.utilityPane.annotation.circle.colorBorderChooserTitle"),
                            colorBorderButton.getBackground());
            if (chosenColor != null) {
                // change the colour of the button background
                setButtonBackgroundColor(colorBorderButton, chosenColor);
                annotation.setColor(chosenColor);
            }
        } else if (e.getSource() == colorFillButton) {
            Color chosenColor =
                    JColorChooser.showDialog(colorFillButton,
                            messageBundle.getString(
                                    "viewer.utilityPane.annotation.circle.colorInteriorChooserTitle"),
                            colorFillButton.getBackground());
            if (chosenColor != null) {
                // change the colour of the button background
                setButtonBackgroundColor(colorFillButton, chosenColor);
                annotation.setFillColor(chosenColor);
            }
        }
        // save the action state back to the document structure.
        updateCurrentAnnotation();
        currentAnnotationComponent.resetAppearanceShapes();
        currentAnnotationComponent.repaint();
    }

    public void stateChanged(ChangeEvent e) {
        alphaSliderChange(e, annotation);
    }

    /**
     * Method to create link annotation GUI.
     */
    private void createGUI() {

        // Create and setup an Appearance panel
        setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                messageBundle.getString("viewer.utilityPane.annotation.circle.appearance.title"),
                TitledBorder.LEFT,
                TitledBorder.DEFAULT_POSITION));

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(1, 2, 1, 2);

        // Line thickness
        lineThicknessBox = new JComboBox(LINE_THICKNESS_LIST);
        lineThicknessBox.setSelectedIndex(DEFAULT_LINE_THICKNESS);
        lineThicknessBox.addItemListener(this);
        JLabel label = new JLabel(messageBundle.getString("viewer.utilityPane.annotation.circle.lineThickness"));
        addGB(this, label, 0, 0, 1, 1);
        addGB(this, lineThicknessBox, 1, 0, 1, 1);

        // Line style
        lineStyleBox = new JComboBox(LINE_STYLE_LIST);
        lineStyleBox.setSelectedIndex(DEFAULT_LINE_STYLE);
        lineStyleBox.addItemListener(this);
        label = new JLabel(messageBundle.getString("viewer.utilityPane.annotation.circle.lineStyle"));
        addGB(this, label, 0, 1, 1, 1);
        addGB(this, lineStyleBox, 1, 1, 1, 1);

        // border colour
        colorBorderButton = new JButton(" ");
        colorBorderButton.addActionListener(this);
        colorBorderButton.setOpaque(true);
        colorBorderButton.setBackground(DEFAULT_BORDER_COLOR);
        label = new JLabel(messageBundle.getString("viewer.utilityPane.annotation.circle.colorBorderLabel"));
        addGB(this, label, 0, 2, 1, 1);
        addGB(this, colorBorderButton, 1, 2, 1, 1);

        // fill type options
        fillTypeBox = new JComboBox(VISIBLE_TYPE_LIST);
        fillTypeBox.setSelectedIndex(DEFAULT_FILL_TYPE);
        fillTypeBox.addItemListener(this);
        label = new JLabel(messageBundle.getString("viewer.utilityPane.annotation.circle.fillTypeLabel"));
        addGB(this, label, 0, 3, 1, 1);
        addGB(this, fillTypeBox, 1, 3, 1, 1);

        // interior colour
        colorFillButton = new JButton(" ");
        colorFillButton.addActionListener(this);
        colorFillButton.setOpaque(true);
        colorFillButton.setBackground(DEFAULT_INTERIOR_COLOR);
        label = new JLabel(messageBundle.getString("viewer.utilityPane.annotation.circle.colorInteriorLabel"));
        addGB(this, label, 0, 4, 1, 1);
        addGB(this, colorFillButton, 1, 4, 1, 1);

        // transparency slider
        transparencySlider = buildAlphaSlider();
        transparencySlider.setMajorTickSpacing(255);
        transparencySlider.setPaintLabels(true);
        transparencySlider.addChangeListener(this);
        label = new JLabel(messageBundle.getString("viewer.utilityPane.annotation.circle.transparencyLabel"));
        addGB(this, label, 0, 5, 1, 1);
        addGB(this, transparencySlider, 1, 5, 1, 1);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        safeEnable(lineThicknessBox, enabled);
        safeEnable(lineStyleBox, enabled);
        safeEnable(fillTypeBox, enabled);
        safeEnable(colorBorderButton, enabled);
        safeEnable(colorFillButton, enabled);
        safeEnable(transparencySlider, enabled);
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