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
import org.icepdf.core.pobjects.annotations.TextAnnotation;
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
 * TextAnnotationPanel is a configuration panel for changing the properties
 * of a TextAnnotationComponent and the underlying annotation component.
 *
 * @since 5.0
 */
@SuppressWarnings("serial")
public class TextAnnotationPanel extends AnnotationPanelAdapter implements ItemListener,
        ActionListener, ChangeListener {

    // default list values.
    private static final int DEFAULT_ICON_NAME = 0;
    private static final Color DEFAULT_COLOR = new Color(1f, 1f, 0f);

    // line thicknesses.
    private static ValueLabelItem[] TEXT_ICON_LIST;

    // link action appearance properties.
    private JComboBox iconNameBox;
    private JButton colorButton;
    private JSlider transparencySlider;

    private TextAnnotation annotation;

    public TextAnnotationPanel(SwingController controller) {
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
        annotation = (TextAnnotation)
                currentAnnotationComponent.getAnnotation();

        applySelectedValue(iconNameBox, annotation.getIconName());
        setButtonBackgroundColor(colorButton, annotation.getColor());
        transparencySlider.setValue(Math.round(annotation.getOpacity() * 255));

        // disable appearance input if we have a invisible rectangle
        safeEnable(iconNameBox, true);
        safeEnable(colorButton, true);
        safeEnable(transparencySlider, true);
    }

    public void itemStateChanged(ItemEvent e) {
        ValueLabelItem item = (ValueLabelItem) e.getItem();
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (e.getSource() == iconNameBox) {
                annotation.setIconName((Name) item.getValue());
            }
            // save the action state back to the document structure.
            updateCurrentAnnotation();
            currentAnnotationComponent.resetAppearanceShapes();
            currentAnnotationComponent.repaint();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == colorButton) {
            Color chosenColor =
                    JColorChooser.showDialog(colorButton,
                            messageBundle.getString("viewer.utilityPane.annotation.textMarkup.colorChooserTitle"),
                            colorButton.getBackground());
            if (chosenColor != null) {
                // change the colour of the button background
                colorButton.setBackground(chosenColor);
                annotation.setColor(chosenColor);

                // save the action state back to the document structure.
                updateCurrentAnnotation();
                currentAnnotationComponent.resetAppearanceShapes();
                currentAnnotationComponent.repaint();
            }
        }
    }

    public void stateChanged(ChangeEvent e) {
        alphaSliderChange(e, annotation);
    }

    /**
     * Method to create link annotation GUI.
     */
    private void createGUI() {
        if (TEXT_ICON_LIST == null) {
            TEXT_ICON_LIST = new ValueLabelItem[]{
                    new ValueLabelItem(TextAnnotation.COMMENT_ICON,
                            messageBundle.getString("viewer.utilityPane.annotation.text.iconName.comment")),
                    new ValueLabelItem(TextAnnotation.CHECK_ICON,
                            messageBundle.getString("viewer.utilityPane.annotation.text.iconName.check")),
                    new ValueLabelItem(TextAnnotation.CHECK_MARK_ICON,
                            messageBundle.getString("viewer.utilityPane.annotation.text.iconName.checkMark")),
                    new ValueLabelItem(TextAnnotation.CIRCLE_ICON,
                            messageBundle.getString("viewer.utilityPane.annotation.text.iconName.circle")),
                    new ValueLabelItem(TextAnnotation.CROSS_ICON,
                            messageBundle.getString("viewer.utilityPane.annotation.text.iconName.cross")),
                    new ValueLabelItem(TextAnnotation.CROSS_HAIRS_ICON,
                            messageBundle.getString("viewer.utilityPane.annotation.text.iconName.crossHairs")),
                    new ValueLabelItem(TextAnnotation.HELP_ICON,
                            messageBundle.getString("viewer.utilityPane.annotation.text.iconName.help")),
                    new ValueLabelItem(TextAnnotation.INSERT_ICON,
                            messageBundle.getString("viewer.utilityPane.annotation.text.iconName.insert")),
                    new ValueLabelItem(TextAnnotation.KEY_ICON,
                            messageBundle.getString("viewer.utilityPane.annotation.text.iconName.key")),
                    new ValueLabelItem(TextAnnotation.NEW_PARAGRAPH_ICON,
                            messageBundle.getString("viewer.utilityPane.annotation.text.iconName.newParagraph")),
                    new ValueLabelItem(TextAnnotation.PARAGRAPH_ICON,
                            messageBundle.getString("viewer.utilityPane.annotation.text.iconName.paragraph")),
                    new ValueLabelItem(TextAnnotation.RIGHT_ARROW_ICON,
                            messageBundle.getString("viewer.utilityPane.annotation.text.iconName.rightArrow")),
                    new ValueLabelItem(TextAnnotation.RIGHT_POINTER_ICON,
                            messageBundle.getString("viewer.utilityPane.annotation.text.iconName.rightPointer")),
                    new ValueLabelItem(TextAnnotation.STAR_ICON,
                            messageBundle.getString("viewer.utilityPane.annotation.text.iconName.star")),
                    new ValueLabelItem(TextAnnotation.UP_LEFT_ARROW_ICON,
                            messageBundle.getString("viewer.utilityPane.annotation.text.iconName.upLeftArrow")),
                    new ValueLabelItem(TextAnnotation.UP_ARROW_ICON,
                            messageBundle.getString("viewer.utilityPane.annotation.text.iconName.upArrow"))};
        }

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(1, 2, 1, 2);

        // Create and setup an Appearance panel
        setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                messageBundle.getString("viewer.utilityPane.annotation.text.appearance.title"),
                TitledBorder.LEFT,
                TitledBorder.DEFAULT_POSITION));
        // Line thickness
        iconNameBox = new JComboBox(TEXT_ICON_LIST);
        iconNameBox.setSelectedIndex(DEFAULT_ICON_NAME);
        iconNameBox.addItemListener(this);
        JLabel label = new JLabel(messageBundle.getString("viewer.utilityPane.annotation.text.iconName"));
        addGB(this, label, 0, 0, 1, 1);
        addGB(this, iconNameBox, 1, 0, 1, 1);
        // fill colour
        colorButton = new JButton(" ");
        colorButton.addActionListener(this);
        colorButton.setOpaque(true);
        colorButton.setBackground(DEFAULT_COLOR);
        label = new JLabel(messageBundle.getString("viewer.utilityPane.annotation.textMarkup.colorLabel"));
        addGB(this, label, 0, 1, 1, 1);
        addGB(this, colorButton, 1, 1, 1, 1);
        // transparency slider
        transparencySlider = buildAlphaSlider();
        transparencySlider.setMajorTickSpacing(255);
        transparencySlider.setPaintLabels(true);
        transparencySlider.addChangeListener(this);
        label = new JLabel(messageBundle.getString("viewer.utilityPane.annotation.textMarkup.transparencyLabel"));
        addGB(this, label, 0, 5, 1, 1);
        addGB(this, transparencySlider, 1, 5, 1, 1);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        safeEnable(iconNameBox, enabled);
        safeEnable(colorButton, enabled);
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