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

import org.icepdf.core.pobjects.annotations.BorderStyle;
import org.icepdf.core.pobjects.annotations.MarkupAnnotation;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.views.AnnotationComponent;
import org.icepdf.ri.common.views.DocumentViewController;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * All annotation and action property panels have a common method for
 * assigning the current annotation component.
 *
 * @since 4.0
 */
public abstract class AnnotationPanelAdapter extends JPanel
        implements AnnotationProperties {

    // layouts constraint
    protected GridBagConstraints constraints;

    // action instance that is being edited
    protected AnnotationComponent currentAnnotationComponent;
    protected DocumentViewController documentViewController;

    protected SwingController controller;
    protected ResourceBundle messageBundle;

    // border styles types.
    protected static ValueLabelItem[] VISIBLE_TYPE_LIST;
    protected static ValueLabelItem[] LINE_THICKNESS_LIST;
    // line styles.
    protected static ValueLabelItem[] LINE_STYLE_LIST;

    protected static final int TRANSPARENCY_MIN = 0;
    protected static final int TRANSPARENCY_MAX = 255;
    protected static final int TRANSPARENCY_INIT = 255;

    protected AnnotationPanelAdapter(
            SwingController controller) {
        setDoubleBuffered(true);
        this.controller = controller;
        this.documentViewController = controller.getDocumentViewController();
        this.messageBundle = controller.getMessageBundle();

        // common selection lists.
        // line thicknesses.
        if (LINE_THICKNESS_LIST == null) {
            LINE_THICKNESS_LIST = new ValueLabelItem[]{
                    new ValueLabelItem(1f,
                            messageBundle.getString("viewer.common.number.one")),
                    new ValueLabelItem(2f,
                            messageBundle.getString("viewer.common.number.two")),
                    new ValueLabelItem(3f,
                            messageBundle.getString("viewer.common.number.three")),
                    new ValueLabelItem(4f,
                            messageBundle.getString("viewer.common.number.four")),
                    new ValueLabelItem(5f,
                            messageBundle.getString("viewer.common.number.five")),
                    new ValueLabelItem(10f,
                            messageBundle.getString("viewer.common.number.ten")),
                    new ValueLabelItem(15f,
                            messageBundle.getString("viewer.common.number.fifteen"))};
        }
        // setup the menu
        if (VISIBLE_TYPE_LIST == null) {
            VISIBLE_TYPE_LIST = new ValueLabelItem[]{
                    new ValueLabelItem(true,
                            messageBundle.getString("viewer.utilityPane.annotation.border.borderType.visibleRectangle")),
                    new ValueLabelItem(false,
                            messageBundle.getString("viewer.utilityPane.annotation.border.borderType.invisibleRectangle"))};
        }
        if (LINE_STYLE_LIST == null) {
            LINE_STYLE_LIST = new ValueLabelItem[]{
                    new ValueLabelItem(BorderStyle.BORDER_STYLE_SOLID,
                            messageBundle.getString("viewer.utilityPane.annotation.border.solid")),
                    new ValueLabelItem(BorderStyle.BORDER_STYLE_DASHED,
                            messageBundle.getString("viewer.utilityPane.annotation.border.dashed"))};
        }
    }

    /**
     * Utility to update the action annotation when changes have been made to
     * 'Dest' which has the same notation as 'GoTo'.  It's the pre action way
     * of doing things and is still very common of link Annotations. .
     */
    protected void updateCurrentAnnotation() {

        if (documentViewController.getAnnotationCallback() != null) {
            documentViewController.getAnnotationCallback()
                    .updateAnnotation(currentAnnotationComponent);
        }
    }

    /**
     * Utility to build the transparency bar slider for changing a markup annotations stroking and non-stroking
     * alpha values (/CA, /ca).
     *
     * @return new instance of a jSlider ranging from TRANSPARENCY_MIN to TRANSPARENCY_MAX.
     */
    protected JSlider buildAlphaSlider() {
        return new JSlider(JSlider.HORIZONTAL,
                TRANSPARENCY_MIN, TRANSPARENCY_MAX, TRANSPARENCY_INIT);
    }

    /**
     * Handler for the alpha value update for an annotation's opacity updated.
     * @param e change event.
     * @param annotation annotation to apply the opacity value to.
     */
    protected void alphaSliderChange(ChangeEvent e, MarkupAnnotation annotation){
        JSlider source = (JSlider)e.getSource();
        int alpha = source.getValue();
        if (!source.getValueIsAdjusting() && alpha != annotation.getOpacityNormalized()) {
            // set the annotation value
            annotation.setOpacity(alpha);
            // send update to callback
            updateCurrentAnnotation();
            // reset the appearance stream.
            currentAnnotationComponent.resetAppearanceShapes();
            currentAnnotationComponent.repaint();
        }
    }

    /**
     * Set the background colour of the various buttons that are used to show the colour picker as well as show
     * the selected colour.
     * @param button button to set colour of.
     * @param color color ot set the buttons background.
     */
    protected void setButtonBackgroundColor(JButton button, Color color){
        if (color != null) {
            if (color.getAlpha() < 255) {
                color = new Color(color.getRGB());
            }
            button.setBackground(color);
            button.setContentAreaFilled(false);
            button.setOpaque(true);
        }
    }

    /**
     * Gridbag constructor helper
     *
     * @param component component to add to grid
     * @param x         row
     * @param y         col
     * @param rowSpan
     * @param colSpan
     */
    protected void addGB(JPanel layout, Component component,
                       int x, int y,
                       int rowSpan, int colSpan) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = rowSpan;
        constraints.gridheight = colSpan;
        layout.add(component, constraints);
    }
}
