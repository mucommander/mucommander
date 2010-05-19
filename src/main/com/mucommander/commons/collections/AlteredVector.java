/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.collections;

import java.util.Collection;
import java.util.Vector;
import java.util.WeakHashMap;

/**
 * AlteredVector is a Vector that is able to notify registered listeners whenever its contents has changed.
 * <p>
 * Events are triggered when:
 * <ul>
 * <li>one or more elements has been added
 * <li>one or more elements has been removed
 * <li>an element has been changed
 * </ul>
 * </p>
 * <p>It is however not aware of modifications that are made to the contained objects themselves.</p>
 *
 * @author Maxence Bernard
 */
public class AlteredVector<E> extends Vector<E> {

    /** Contains all registered listeners, stored as weak references */
    private WeakHashMap<VectorChangeListener, Object> listeners = new WeakHashMap<VectorChangeListener, Object>();


    public AlteredVector() {
        super();
    }

    public AlteredVector(Collection<? extends E> collection) {
        super(collection);
    }

    public AlteredVector(int initialCapacity, int capacityIncrement) {
        super(initialCapacity, capacityIncrement);
    }

    public AlteredVector(int initialCapacity) {
        super(initialCapacity);
    }


    /**
     * Adds the specified VectorChangeListener to the list of registered listeners.
     *
     * <p>Listeners are stored as weak references so {@link #removeVectorChangeListener(VectorChangeListener)}
     * doesn't need to be called for listeners to be garbage collected when they're not used anymore.</p>
     *
     * @param listener the VectorChangeListener to add to the list of registered listeners.
     * @see            #removeVectorChangeListener(VectorChangeListener)
     */
    public void addVectorChangeListener(VectorChangeListener listener) {
        listeners.put(listener, null);
    }

    /**
     * Removes the specified VectorChangeListener from the list of registered listeners.
     *
     * @param listener the VectorChangeListener to remove from the list of registered listeners.
     * @see            #addVectorChangeListener(VectorChangeListener)
     */
    public void removeVectorChangeListener(VectorChangeListener listener) {
        listeners.remove(listener);
    }


    /**
     * This method is called when one or more elements has been added to this AlteredVector to notify listeners.
     *
     * @param startIndex index at which the first element has been added
     * @param nbAdded number of elements added
     */
    private void fireElementsAddedEvent(int startIndex, int nbAdded) {
        for(VectorChangeListener listener : listeners.keySet())
            listener.elementsAdded(startIndex, nbAdded);
    }

    /**
     * This method is called when one or more elements has been removed from this AlteredVector to notify listeners.
     *
     * @param startIndex index at which the first element has been removed
     * @param nbRemoved number of elements removed
     */
    private void fireElementsRemovedEvent(int startIndex, int nbRemoved) {
        for(VectorChangeListener listener : listeners.keySet())
            listener.elementsRemoved(startIndex, nbRemoved);
    }

    /**
     * This method is called when an element has been changed in this AlteredVector to notify listeners.
     *
     * @param index index of the element that has been changed
     */
    private void fireElementChangedEvent(int index) {
        for(VectorChangeListener listener : listeners.keySet())
            listener.elementChanged(index);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public void setElementAt(E o, int i) {
        super.setElementAt(o, i);

        fireElementChangedEvent(i);
    }

    @Override
    public E set(int i, E o) {
        o = super.set(i, o);

        fireElementChangedEvent(i);

        return o;
    }

    @Override
    public void insertElementAt(E o, int i) {
        super.insertElementAt(o, i);

        fireElementsAddedEvent(i, 1);
    }

    @Override
    public void add(int i, E o) {
        insertElementAt(o, i);

        fireElementsAddedEvent(i, 1);
    }

    @Override
    public void addElement(E o) {
        super.addElement(o);

        fireElementsAddedEvent(size()-1, 1);
    }

    @Override
    public boolean add(E o) {
        addElement(o);

        fireElementsAddedEvent(size()-1, 1);

        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        int sizeBefore = size();

        boolean b = super.addAll(collection);

        fireElementsAddedEvent(sizeBefore, size()-sizeBefore);

        return b;
    }

    @Override
    public boolean addAll(int i, Collection<? extends E> collection) {
        int sizeBefore = size();

        boolean b = super.addAll(i, collection);

        fireElementsAddedEvent(i, size()-sizeBefore);

        return b;
    }

    @Override
    public void removeElementAt(int i) {
        super.removeElementAt(i);

        fireElementsRemovedEvent(i, 1);
    }

    @Override
    public E remove(int i) {
        E o = super.remove(i);

        fireElementsRemovedEvent(i, 1);

        return o;
    }

    @Override
    public boolean removeElement(Object o) {
        int index = indexOf(o);

        if(index==-1)
            return false;

        removeElementAt(index);

        return true;
    }

    @Override
    public boolean remove(Object o) {
        return removeElement(o);
    }

    @Override
    public void removeAllElements() {
        int sizeBefore = size();

        super.removeAllElements();

        fireElementsRemovedEvent(0, sizeBefore);
    }

    @Override
    public void clear() {
        removeAllElements();
    }
}
