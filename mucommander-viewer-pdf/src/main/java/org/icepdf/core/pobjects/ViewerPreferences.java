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
package org.icepdf.core.pobjects;

import org.icepdf.core.util.Library;

import java.util.HashMap;

/**
 * The ViewerPreferences class is used to represent and provide access to the
 * ViewerPreference keys and values from the PDF Specification, such as HideToolbar
 */
public class ViewerPreferences extends Dictionary {

    // root node of the tree of names.
    private NameNode root;

    /**
     * Creates a new instance of a NameTree.
     *
     * @param l document library.
     * @param h NameTree dictionary entries.
     */
    public ViewerPreferences(Library l, HashMap h) {
        super(l, h);
    }

    /**
     * Initiate the NameTree.
     */
    public void init() {
        if (inited) {
            return;
        }
        root = new NameNode(library, entries);
        inited = true;
    }

    public NameNode getRoot() {
        return root;
    }

    public boolean hasHideToolbar() {
        return library.isValidEntry(entries, new Name("HideToolbar"));
    }

    public boolean hasHideMenubar() {
        return library.isValidEntry(entries, new Name("HideMenubar"));
    }

    public boolean hasFitWindow() {
        return library.isValidEntry(entries, new Name("FitWindow"));
    }

    public boolean getHideToolbar() {
        return library.getBoolean(entries, new Name("HideToolbar"));
    }

    public boolean getHideMenubar() {
        return library.getBoolean(entries, new Name("HideMenubar"));
    }

    public boolean getFitWindow() {
        return library.getBoolean(entries, new Name("FitWindow"));
    }
}
