/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.event;

/**
 * An abstract adapter class for receiving location events.
 * The methods in this class are empty. This class exists as
 * convenience for creating listener objects.
 * 
 * @author Arik Hadas
 */
public abstract class LocationAdapter implements LocationListener {

	/**
     * {@inheritDoc}
     */
	public void locationChanging(LocationEvent locationEvent) { }

	/**
     * {@inheritDoc}
     */
	public void locationChanged(LocationEvent locationEvent) { }

	/**
     * {@inheritDoc}
     */
	public void locationCancelled(LocationEvent locationEvent) { }

	/**
     * {@inheritDoc}
     */
	public void locationFailed(LocationEvent locationEvent) { }
}
