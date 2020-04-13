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
import org.icepdf.core.pobjects.annotations.LinkAnnotation;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.views.AnnotationComponent;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Link Annotation panel intended use is for the manipulation of LinkAnnotation
 * appearance properties.  This could be used with other annotation types but
 * it's not suggested.
 *
 * @since 4.0
 */
@SuppressWarnings("serial")
public class LinkAnnotationPanel extends AnnotationPanelAdapter implements ItemListener {

    // default list values.
    private static final int DEFAULT_HIGHLIGHT_STYLE = 1;

    // link action appearance properties.
    private JComboBox highlightStyleBox;

    // appearance properties to take care of.
    private Name highlightStyle;

    public LinkAnnotationPanel(SwingController controller) {
        super(controller);
        setLayout(new GridLayout(1, 2, 5, 2));

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

        if (newAnnotation == null || newAnnotation.getAnnotation() == null ||
                !(newAnnotation.getAnnotation() instanceof LinkAnnotation)) {
            setEnabled(false);
            return;
        }
        // assign the new action instance.
        this.currentAnnotationComponent = newAnnotation;

        // For convenience grab the Annotation object wrapped by the component
        LinkAnnotation linkAnnotation =
                (LinkAnnotation) currentAnnotationComponent.getAnnotation();

        // apply values to appears
        highlightStyle = linkAnnotation.getHighlightMode();
        applySelectedValue(highlightStyleBox, highlightStyle);

        // disable appearance input if we have a invisible rectangle
        enableAppearanceInputComponents(linkAnnotation.getBorderType());
    }

    public void itemStateChanged(ItemEvent e) {
        ValueLabelItem item = (ValueLabelItem) e.getItem();
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (e.getSource() == highlightStyleBox) {
                highlightStyle = (Name) item.getValue();
            }
            // save the action state back to the document structure.
            updateCurrentAnnotation();
            currentAnnotationComponent.repaint();
        }
    }

    /**
     * Method to create link annotation GUI.
     */
    private void createGUI() {

        // highlight styles.
        ValueLabelItem[] highlightStyleList = new ValueLabelItem[]{
                new ValueLabelItem(LinkAnnotation.HIGHLIGHT_NONE,
                        messageBundle.getString("viewer.utilityPane.annotation.link.none")),
                new ValueLabelItem(LinkAnnotation.HIGHLIGHT_INVERT,
                        messageBundle.getString("viewer.utilityPane.annotation.link.invert")),
                new ValueLabelItem(LinkAnnotation.HIGHLIGHT_OUTLINE,
                        messageBundle.getString("viewer.utilityPane.annotation.link.outline")),
                new ValueLabelItem(LinkAnnotation.HIGHLIGHT_PUSH,
                        messageBundle.getString("viewer.utilityPane.annotation.link.push"))};

        // Create and setup an Appearance panel
        setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                messageBundle.getString("viewer.utilityPane.annotation.link.appearance.title"),
                TitledBorder.LEFT,
                TitledBorder.DEFAULT_POSITION));
        // highlight style box.
        highlightStyleBox = new JComboBox(highlightStyleList);
        highlightStyleBox.setSelectedIndex(DEFAULT_HIGHLIGHT_STYLE);
        highlightStyleBox.addItemListener(this);
        add(new JLabel(
                messageBundle.getString("viewer.utilityPane.annotation.link.highlightType")));
        add(highlightStyleBox);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        safeEnable(highlightStyleBox, enabled);
    }

    /**
     * Method to enable appearance input fields for an invisible rectangle
     *
     * @param linkType invisible rectangle or visible, your pick.
     */
    private void enableAppearanceInputComponents(int linkType) {
        if (linkType == Annotation.INVISIBLE_RECTANGLE) {
            // everything but highlight style and link type
            safeEnable(highlightStyleBox, true);
        } else {
            // enable all fields.
            safeEnable(highlightStyleBox, true);
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
