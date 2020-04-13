package org.icepdf.core.pobjects;

import org.icepdf.core.util.Library;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Instead of being defined directly with the explicit syntax a destination may
 * be referred to indirectly by means of a name object (PDF 1.1) or a byte
 * string (PDF 1.2). This capability is especially useful when the destination
 * is located in another PDF document.
 * <p/>
 * In PDF 1.1, the correspondence between name objects and destinations shall be
 * defined by the Dests entry in the document catalogue (see 7.7.2, “Document Catalog”).
 * The value of this entry shall be a dictionary in which each key is a destination
 * name and the corresponding value is either an array defining the destination
 *
 * @since 5.2.0
 */
public class NamedDestinations extends Dictionary {

    public NamedDestinations(Library library, HashMap entries) {
        super(library, entries);
    }

    /**
     * Gets a Destination object for the given Name. If now corresponding
     * Destination value if found null is returned.
     *
     * @param name Name to looking up in NamedDestinations list.
     * @return Destination for the given name, null otherwise.
     */
    public Destination getDestination(Name name) {
        Object tmp = entries.get(name);
        if (tmp != null && tmp instanceof ArrayList) {
            return new Destination(library, tmp);
        }
        return null;
    }
}
