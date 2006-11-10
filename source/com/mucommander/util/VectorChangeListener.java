package com.mucommander.util;

/**
 * Interface to be implemented by classes that wish to be notified of changes made to an {@link AlteredVector}.
 *
 * <p>Those classes need to be registered as listeners to receive those events, this can be done by calling
 * {@link AlteredVector#addVectorChangeListener(VectorChangeListener)}.
 *
 * @author Maxence Bernard
 */
public interface VectorChangeListener {

    /**
     * This method is called when one or more elements has been added to the AlteredVector.
     *
     * @param startIndex index at which the first element has been added
     * @param nbAdded number of elements added
     */
    public void elementsAdded(int startIndex, int nbAdded);

    /**
     * This method is called when one or more elements has been removed from the AlteredVector.
     *
     * @param startIndex index at which the first element has been removed
     * @param nbRemoved number of elements removed
     */
    public void elementsRemoved(int startIndex, int nbRemoved);

    /**
     * This method is called when an element has been changed in the AlteredVector.
     *
     * @param index index of the element that has been changed
     */
    public void elementChanged(int index);
}
