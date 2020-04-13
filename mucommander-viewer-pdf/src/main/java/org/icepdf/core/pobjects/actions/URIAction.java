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

import org.icepdf.core.pobjects.LiteralStringObject;
import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.StringObject;
import org.icepdf.core.util.Library;

import java.util.HashMap;

/**
 * <p>The uniform resource identifier (URI) action represents destination
 * that is a hypertext link</p>
 * <p/>
 * <p>The URI can be extracted from this object so that the content can
 * be loaded in a web browser.  ICEpdf does not currently support image map
 * URI's.</p>
 *
 * @author ICEsoft Technologies, Inc.
 * @since 2.6
 */
public class URIAction extends Action {

    public static final Name URI_KEY = new Name("URI");

    // uniform resource identifier to be resolved.
    private StringObject URI;

    // specifies whether to track the mouse position.
    private boolean isMap;

    /**
     * Creates a new instance of a Action.
     *
     * @param l document library.
     * @param h Action dictionary entries.
     */
    public URIAction(Library l, HashMap h) {
        super(l, h);
    }

    /**
     * Sets the URI string associated witht this action.
     *
     * @param URI an string value except null.
     */
    public void setURI(String URI) {
        StringObject tmp = new LiteralStringObject(
                URI, getPObjectReference(), library.getSecurityManager());
        // StringObject detection should allow writer to pick on encryption.
        entries.put(URIAction.URI_KEY, tmp);
        this.URI = tmp;
    }

    /**
     * Gets the Uniform resource identifier to resolve, encoded in 7-bit ASCII.
     *
     * @return uniform resouce.
     */
    public String getURI() {
        // URI should always be text, but there have been examples of
        // Hex encoded uri values.
        Object actionURI = getObject(URI_KEY);
        if (actionURI instanceof StringObject) {
            URI = (StringObject) actionURI;
        }
        return URI.getDecryptedLiteralString(library.getSecurityManager());
    }

    /**
     * Gets a flag specifying whether to track thee mouse poisition when the
     * URI is resolved.  Default value is false.
     *
     * @return true if tmouse poiiin is to be called.
     */
    public boolean isMap() {
        return isMap;
    }

}
