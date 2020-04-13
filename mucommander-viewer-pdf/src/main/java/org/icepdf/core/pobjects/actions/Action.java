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

import org.icepdf.core.pobjects.Dictionary;
import org.icepdf.core.pobjects.Name;
import org.icepdf.core.util.Library;

import java.util.HashMap;


/**
 * <p>The <code>Action</code> class represents an <i>Action Dictionary</i> which defines
 * characteristics and behavior of an action.  A PDF action can be a wide
 * variety of standard action types.  This class is designed to help users
 * get needed attributes from the Action Dictionary.  The Dictionary classes
 * getEntries method can be used to find other attributes associated with this action.</p>
 * <p/>
 * <p>ICEpdf currently only uses the "GoTo" action when working with document
 * outlines.  If your application is interpreting a page's Annotations then you
 * can query the Annotation object to get its Action.  </p>
 *
 * @since 1.0
 */
public class Action extends Dictionary {

    public static final Name ACTION_TYPE = new Name("Action");

    public static final Name ACTION_TYPE_KEY = new Name("S");

    public static final Name NEXT_KEY = new Name("Next");

    public static final Name ACTION_TYPE_GOTO = new Name("GoTo");

    public static final Name ACTION_TYPE_GOTO_REMOTE = new Name("GoToR");

    public static final Name ACTION_TYPE_LAUNCH = new Name("Launch");

    public static final Name ACTION_TYPE_URI = new Name("URI");

    public static final Name ACTION_TYPE_RESET_SUBMIT = new Name("ResetForm");

    public static final Name ACTION_TYPE_SUBMIT_SUBMIT = new Name("SubmitForm");

    public static final Name ACTION_TYPE_NAMED = new Name("Named");

    public static final Name ACTION_TYPE_JAVA_SCRIPT = new Name("JavaScript");

    // type of annotation
    private String type;

    // todo implement next
    // private Object Next

    /**
     * Creates a new instance of a Action.
     *
     * @param l document library.
     * @param h Action dictionary entries.
     */
    public Action(Library l, HashMap h) {
        super(l, h);
        type = getObject(ACTION_TYPE_KEY).toString();
    }

    public static Action buildAction(Library library, HashMap hashMap) {
        Name actionType = (Name) hashMap.get(Action.ACTION_TYPE_KEY);
        if (actionType != null) {
            if (actionType.equals(Action.ACTION_TYPE_GOTO)) {
                return new GoToAction(library, hashMap);
            } else if (actionType.equals(Action.ACTION_TYPE_GOTO_REMOTE)) {
                return new GoToRAction(library, hashMap);
            } else if (actionType.equals(Action.ACTION_TYPE_LAUNCH)) {
                return new LaunchAction(library, hashMap);
            } else if (actionType.equals(Action.ACTION_TYPE_URI)) {
                return new URIAction(library, hashMap);
            } else if (actionType.equals(Action.ACTION_TYPE_RESET_SUBMIT)) {
                return new ResetFormAction(library, hashMap);
            } else if (actionType.equals(Action.ACTION_TYPE_SUBMIT_SUBMIT)) {
                return new SubmitFormAction(library, hashMap);
            } else if (actionType.equals(Action.ACTION_TYPE_NAMED)) {
                return new NamedAction(library, hashMap);
            } else if (actionType.equals(Action.ACTION_TYPE_JAVA_SCRIPT)) {
                return new JavaScriptAction(library, hashMap);
            }
        }
        return new Action(library, hashMap);
    }

    /**
     * <p>Gets the type of action that this dictionary describes.  The most
     * common actions can be found in the PDF Reference 1.6 in section
     * 8.5.3.  ICEpdf currently only takes advantage of the "GoTo" action
     * when a user clicks on a document outline. </p>
     *
     * @return The action type.
     */
    public String getType() {
        return type;
    }

    public boolean similar(Action obj) {
        // check if object references can be compared
        if (this.getPObjectReference() != null &&
                obj.getPObjectReference() != null) {
            return getPObjectReference().equals(obj.getPObjectReference());
        } else {
            // compare type
            return getType().equals(obj.getType());
        }
    }
}
