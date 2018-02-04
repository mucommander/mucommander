/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2016 Maxence Bernard
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

/**
 * Interface to be implemented by classes that wish to be notified of changes made to an {@link AlteredVector}.
 * <p>
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
     * @param nbAdded    number of elements added
     */
    void elementsAdded(int startIndex, int nbAdded);

    /**
     * This method is called when one or more elements has been removed from the AlteredVector.
     *
     * @param startIndex index at which the first element has been removed
     * @param nbRemoved  number of elements removed
     */
    void elementsRemoved(int startIndex, int nbRemoved);

    /**
     * This method is called when an element has been changed in the AlteredVector.
     *
     * @param index index of the element that has been changed
     */
    void elementChanged(int index);
}
