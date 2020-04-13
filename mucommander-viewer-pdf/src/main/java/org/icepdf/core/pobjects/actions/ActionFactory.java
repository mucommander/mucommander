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
package org.icepdf.core.pobjects.actions;

import org.icepdf.core.pobjects.*;
import org.icepdf.core.util.Library;

import java.util.HashMap;

/**
 * Factory for build actions
 *
 * @since 4.0
 */
public class ActionFactory {

    public static final int GOTO_ACTION = 1;
    public static final int URI_ACTION = 2;
    public static final int LAUNCH_ACTION = 3;

    private ActionFactory() {
    }

    /**
     * Creates a new ACTION object of the type specified by the type constant.
     * Currently there are only two supporte action types; GoTo, Launch and URI.
     * <p/>
     * This call adds the new action object to the document library as well
     * as the document StateManager.
     *
     * @param library library to register action with
     * @param type    type of action to create
     * @return new action object of the specified type.
     */
    public static Action buildAction(Library library,
                                     int type) {
        // state manager
        StateManager stateManager = library.getStateManager();

        // create a new entries to hold the annotation properties
        HashMap<Name, Object> entries = new HashMap<Name, Object>();
        if (GOTO_ACTION == type) {
            // set default link annotation values.
            entries.put(Dictionary.TYPE_KEY, Action.ACTION_TYPE);
            entries.put(Action.ACTION_TYPE_KEY, Action.ACTION_TYPE_GOTO);
            // add a null destination entry
            entries.put(GoToAction.DESTINATION_KEY, new Destination(library, null));
            GoToAction action = new GoToAction(library, entries);
            action.setPObjectReference(stateManager.getNewReferencNumber());
            return action;
        } else if (URI_ACTION == type) {
            // set default link annotation values.
            entries.put(Dictionary.TYPE_KEY, Action.ACTION_TYPE);
            entries.put(Action.ACTION_TYPE_KEY, Action.ACTION_TYPE_URI);
            // add a null uri string entry
            Reference pObjectReference = stateManager.getNewReferencNumber();
            entries.put(URIAction.URI_KEY, new LiteralStringObject("", pObjectReference, library.getSecurityManager()));
            URIAction action = new URIAction(library, entries);
            action.setPObjectReference(stateManager.getNewReferencNumber());
            return action;
        } else if (LAUNCH_ACTION == type) {
            // set default link annotation values.
            entries.put(Dictionary.TYPE_KEY, Action.ACTION_TYPE);
            entries.put(Action.ACTION_TYPE_KEY, Action.ACTION_TYPE_LAUNCH);
            // add a null file string entry
            Reference pObjectReference = stateManager.getNewReferencNumber();
            entries.put(LaunchAction.FILE_KEY, new LiteralStringObject("", pObjectReference, library.getSecurityManager()));
            LaunchAction action = new LaunchAction(library, entries);
            action.setPObjectReference(stateManager.getNewReferencNumber());
            return action;
        }
        return null;
    }
}
