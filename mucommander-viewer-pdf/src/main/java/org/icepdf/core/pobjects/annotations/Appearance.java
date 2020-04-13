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

import java.util.HashMap;

/**
 * An appearance dictionary dictionary entry for N, R or D can be associated
 * with one or more appearance streams.  For example a Widget btn annotation
 * can have an /ON an /Off state.  This class sill store one or more
 * named appearance streams for a dictionary entry.
 *
 * @since 5.1
 */
public class Appearance {

    private HashMap<Name, AppearanceState> appearance;

    private Name selectedName = Annotation.APPEARANCE_STREAM_NORMAL_KEY;
    private Name offName = new Name("Off");
    private Name onName = new Name("Yes");

    /**
     * Create a new instance of an Appearance stream.
     */
    public Appearance() {
        appearance = new HashMap<Name, AppearanceState>(2);
    }

    public boolean hasAlternativeAppearance() {
        return offName != null || onName != null;
    }

    public void addAppearance(Name name, AppearanceState appearanceState) {
        appearance.put(name, appearanceState);
        if (name.getName().toLowerCase().equals("off")) {
            offName = name;
        } else {
            onName = name;
        }
    }

    public Name getOffName() {
        return offName;
    }

    public Name getOnName() {
        return onName;
    }

    public Name getSelectedName() {
        return selectedName;
    }

    public void setSelectedName(Name selectedName) {
        this.selectedName = selectedName;
    }

    public AppearanceState getSelectedAppearanceState() {
        AppearanceState state = appearance.get(selectedName);
        return state;
    }

    public AppearanceState getAppearanceState(Name name) {
        AppearanceState state = appearance.get(name);
        return state;
    }

    /**
     * Updates or adds the APPEARANCE_STATE_KEY with the currently selected state.
     *
     * @param entries parent annotation dictionary to update.
     */
    protected void updateAppearanceDictionary(HashMap<Object, Object> entries) {
        entries.put(Annotation.APPEARANCE_STATE_KEY, selectedName);
    }
}
