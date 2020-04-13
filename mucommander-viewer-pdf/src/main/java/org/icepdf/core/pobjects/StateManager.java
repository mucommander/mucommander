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

import java.util.*;
import java.util.logging.Logger;

/**
 * This class is responsible for keeping track of which object in the document
 * have change.  When a file is written to disk this class is used to find
 * the object that should be written in the body section of the file as part of
 * an incremental update.
 * <p/>
 * Once this object is created should be added to the library so that is
 * accessible by any PObject.
 *
 * @since 4.0
 */
public class StateManager {
    private static final Logger logger =
            Logger.getLogger(StateManager.class.getName());

    // a list is all we might need. 
    private HashMap<Reference, PObject> changes;

    // access to xref size and next revision number.
    private PTrailer trailer;

    private int nextReferenceNumber;

    /**
     * Creates a new instance of the state manager.
     *
     * @param trailer document trailer
     */
    public StateManager(PTrailer trailer) {
        this.trailer = trailer;
        // cache of objects that have changed.
        changes = new HashMap<Reference, PObject>();

        // number of objects is always one more then the current size and
        // thus the next available number.
        if (trailer != null) {
            nextReferenceNumber = trailer.getNumberOfObjects();
        }
    }

    /**
     * Gets the next available reference number from the trailer.
     *
     * @return valid reference number.
     */
    public Reference getNewReferencNumber() {
        // zero revision number for now but technically we can reuse
        // deleted references and increment the rev number.  For no we
        // keep it simple
        Reference newReference = new Reference(nextReferenceNumber, 0);
        nextReferenceNumber++;
        return newReference;
    }

    /**
     * Add a new PObject containing changed data to the cache.
     *
     * @param pObject object to add to cache.
     */
    public void addChange(PObject pObject) {
        changes.put(pObject.getReference(), pObject);
        int objectNumber = pObject.getReference().getObjectNumber();
        // check the reference numbers
        if (nextReferenceNumber <= objectNumber) {
            nextReferenceNumber = objectNumber + 1;
        }
    }

    /**
     * Checks the state manager to see if an instance of the specified reference
     * already exists in the cache.
     *
     * @param reference reference to look for an existing usage.
     * @return true if reference is already a key in the cache; otherwise, false.
     */
    public boolean contains(Reference reference) {
        return changes.containsKey(reference);
    }

    /**
     * Returns an instance of the specified reference
     *
     * @param reference reference to look for an existing usage
     * @return PObject of corresponding reference if present, false otherwise.
     */
    public Object getChange(Reference reference) {
        return changes.get(reference);
    }

    /**
     * Remove a PObject from the cache.
     *
     * @param pObject pObject to removed from the cache.
     */
    public void removeChange(PObject pObject) {
        changes.remove(pObject.getReference());
    }

    /**
     * @return If there are any changes
     */
    public boolean isChanged() {
        return !changes.isEmpty();
    }

    /**
     * Gets the number of change object in the state manager.
     *
     * @return zero or more changed object count.
     */
    public int getChangedSize() {
        return changes.size();
    }

    /**
     * @return An Iterator<PObject> for all the changes objects, sorted
     */
    public Iterator<PObject> iteratorSortedByObjectNumber() {
        Collection<PObject> coll = changes.values();
/*
 * This code allows me to force an object to be treated as modified,
 * so I can debug how we write out that kind of object, before we
 * add a ui to actually edit it.
Reference ref = new Reference(10,0);
Object ob = trailer.getLibrary().getObject(ref);
logger.severe("Object 10: " + ob + "  ob.class: " + ob.getClass().getName());
java.util.HashSet<PObject> hs = new java.util.HashSet<PObject>(coll);
hs.add(new PObject(ob, ref));
coll = hs;
*/
        PObject[] arr = coll.toArray(new PObject[coll.size()]);
        Arrays.sort(arr, new PObjectComparatorByReferenceObjectNumber());
        List<PObject> sortedList = Arrays.asList(arr);
        return sortedList.iterator();
    }

    public PTrailer getTrailer() {
        return trailer;
    }


    private static class PObjectComparatorByReferenceObjectNumber
            implements Comparator<PObject> {
        public int compare(PObject a, PObject b) {
            if (a == null && b == null)
                return 0;
            else if (a == null)
                return -1;
            else if (b == null)
                return 1;
            Reference ar = a.getReference();
            Reference br = b.getReference();
            if (ar == null && br == null)
                return 0;
            else if (ar == null)
                return -1;
            else if (br == null)
                return 1;
            int aron = ar.getObjectNumber();
            int bron = br.getObjectNumber();
            if (aron < bron)
                return -1;
            else if (aron > bron)
                return 1;
            return 0;
        }
    }
}

