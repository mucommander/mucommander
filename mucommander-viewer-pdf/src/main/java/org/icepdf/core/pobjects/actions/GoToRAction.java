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

import org.icepdf.core.pobjects.Destination;
import org.icepdf.core.pobjects.FileSpecification;
import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.StringObject;
import org.icepdf.core.util.Library;

import java.util.HashMap;

/**
 * <p>A remote go-to action is similar to an ordinary go-to action but jumps to
 * a destination in another PDF file instead of the current file. </p>
 *
 * @author ICEsoft Technologies, Inc.
 * @since 2.6
 */
public class GoToRAction extends Action {

    public static final Name F_KEY = new Name("F");
    public static final Name NEW_WINDOW_KEY = new Name("NewWindow");

    // path to external file, see section 3.10.1 for more details on
    // resolving paths
    private String externalFile;
    private FileSpecification fileSpecification;

    // location in document that should be loaded.
    private Destination externalDestination;

    // new window?
    private Boolean isNewWindow;

    /**
     * Creates a new instance of a Action.
     *
     * @param l document library.
     * @param h Action dictionary entries.
     */
    public GoToRAction(Library l, HashMap h) {
        super(l, h);

        externalDestination =
                new Destination(library, library.getObject(entries, Destination.D_KEY));
        Object tmp = library.getObject(entries, F_KEY);
        if (tmp instanceof HashMap) {
            fileSpecification =
                    new FileSpecification(library, (HashMap) tmp);
        } else if (tmp instanceof StringObject) {
            externalFile =
                    ((StringObject) tmp)
                            .getDecryptedLiteralString(
                                    library.getSecurityManager());
        }

        isNewWindow = library.getBoolean(entries, NEW_WINDOW_KEY);

    }

    /**
     * Gets the destination associated with the external file path.
     *
     * @return destination object if any to be resolved.
     */
    public Destination getDestination() {
        return externalDestination;
    }

    /**
     * Gets the external file path
     *
     * @return file path of document to be opened.
     */
    public String getFile() {
        return externalFile;
    }

    /**
     * Gets the file specification of the destination file.  This objects should
     * be interigated to deside what should be done
     *
     * @return file specification, maybe nukll if external file was specified.
     */
    public FileSpecification getFileSpecification() {
        return fileSpecification;
    }

    /**
     * Indicates if the external document should be loaded in a new window or if
     * it should be loaded in the current.
     *
     * @return true indicates a new windows should be launched for the remote
     *         document; otherwise, false.
     */
    public Boolean isNewWindow() {
        return isNewWindow;
    }
}
