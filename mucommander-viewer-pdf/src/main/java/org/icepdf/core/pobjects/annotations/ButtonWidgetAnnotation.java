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

package org.icepdf.core.pobjects.annotations;

import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.PObject;
import org.icepdf.core.pobjects.StateManager;
import org.icepdf.core.pobjects.acroform.ButtonFieldDictionary;
import org.icepdf.core.pobjects.acroform.FieldDictionary;
import org.icepdf.core.util.Library;

import java.awt.geom.AffineTransform;
import java.util.HashMap;

/**
 * Represents a Acroform Button widget and manages the appearance streams
 * for the various appearance states.
 *
 * @since 5.1
 */
public class ButtonWidgetAnnotation extends AbstractWidgetAnnotation<ButtonFieldDictionary> {

    private ButtonFieldDictionary fieldDictionary;

    protected Name originalAppearance;

    public ButtonWidgetAnnotation(Library l, HashMap h) {
        super(l, h);
        fieldDictionary = new ButtonFieldDictionary(library, entries);
    }

    public ButtonWidgetAnnotation(Annotation widgetAnnotation){
        super(widgetAnnotation.getLibrary(), widgetAnnotation.getEntries());
        fieldDictionary = new ButtonFieldDictionary(library, entries);
        // copy over the reference number.
        setPObjectReference(widgetAnnotation.getPObjectReference());
    }

    /**
     * Button appearance streams are fixed, all that is done in this method is appearance selected state
     * is set and the change persisted to the StateManager.
     * @param dx current location of the annotation
     * @param dy current location of the annotation
     * @param pageSpace current page space.
     */
    public void resetAppearanceStream(double dx, double dy, AffineTransform pageSpace) {
        // update the appearanceState in the state manager so the change will persist.
        Appearance appearance = appearances.get(currentAppearance);
        if (appearance != null) {
            appearance.updateAppearanceDictionary(entries);
        }
        // add this annotation to the state manager.
        StateManager stateManager = library.getStateManager();
        stateManager.addChange(new PObject(this, this.getPObjectReference()));

        Name selectedName = appearance.getSelectedName();
        // check boxes will have a V entry which
        if (getFieldDictionary().hasFieldValue()){
            // update the value
            entries.put(FieldDictionary.V_KEY, selectedName);
            // object already in state manager, nothing further to to.
        } // radio buttons will store the value in the parent.
        else if (getFieldDictionary().getParent() != null &&
                getFieldDictionary().getParent().hasFieldValue()){
            // update the value
            //getFieldDictionary().getParent().getEntries().put(FieldDictionary.V_KEY, selectedName);
            // add to state manager.
            stateManager.addChange(new PObject(getFieldDictionary().getParent(),
                    getFieldDictionary().getParent().getPObjectReference()));
        }

        if (originalAppearance == null){
            originalAppearance = currentAppearance;
        }
    }

    public void reset() {
        Object oldValue = fieldDictionary.getFieldValue();
        Object defaultFieldValue = fieldDictionary.getDefaultFieldValue();
        FieldDictionary parentFaultFieldValue = fieldDictionary.getParent();
        if (defaultFieldValue != null) {
            // apply the default value
            changeSupport.firePropertyChange("valueFieldReset", oldValue, defaultFieldValue);
        }else if (parentFaultFieldValue != null) {
            // apply the default value
            changeSupport.firePropertyChange("valueFieldReset", oldValue,
                    parentFaultFieldValue.getDefaultFieldValue());
        }else{
            // otherwise we remove the key
            changeSupport.firePropertyChange("valueFieldReset", oldValue, "");
        }
    }

    public void turnOff() {
        Appearance appearance = appearances.get(currentAppearance);
        if (appearance != null && appearance.hasAlternativeAppearance()) {
            appearance.setSelectedName(appearance.getOffName());
        }
    }

    public boolean isOn() {
        Appearance appearance = appearances.get(currentAppearance);
        Name selectedNormalAppearance = appearance.getSelectedName();
        return !selectedNormalAppearance.equals(appearance.getOffName());
    }

    public Name toggle() {
        Appearance appearance = appearances.get(currentAppearance);
        Name selectedNormalAppearance = appearance.getSelectedName();
        if (appearance.hasAlternativeAppearance()) {
            if (!selectedNormalAppearance.equals(appearance.getOffName())) {
                appearance.setSelectedName(appearance.getOffName());
            } else {
                appearance.setSelectedName(appearance.getOnName());
            }
        }
        return appearance.getSelectedName();
    }


    public void turnOn() {
        // first check if there are more then one normal streams.
        Appearance appearance = appearances.get(currentAppearance);
        if (appearance.hasAlternativeAppearance()) {
            appearance.setSelectedName(appearance.getOnName());
        }
    }

    @Override
    public ButtonFieldDictionary getFieldDictionary() {
        return fieldDictionary;
    }
}


