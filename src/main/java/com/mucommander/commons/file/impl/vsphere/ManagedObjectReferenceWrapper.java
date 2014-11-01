/**
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

package com.mucommander.commons.file.impl.vsphere;

import com.vmware.vim25.ManagedObjectReference;

/**
 * A wrapper for ManagedObjectReferenceWrapper that adds equals and hashCode for it.
 * 
 * @author Yuval Kohavi <yuval.kohavi@intigua.com>
 *
 */
public class ManagedObjectReferenceWrapper {
	private ManagedObjectReference mor;

	public ManagedObjectReferenceWrapper(ManagedObjectReference mor) {
		this.mor = mor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mor.getValue() == null) ? 0 : mor.getValue().hashCode());
		result = prime * result
				+ ((mor.getType() == null) ? 0 : mor.getType().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ManagedObjectReferenceWrapper other = (ManagedObjectReferenceWrapper) obj;
		if (mor.getValue() == null) {
			if (other.mor.getValue() != null)
				return false;
		} else if (!mor.getValue().equals(other.mor.getValue()))
			return false;
		if (mor.getType() == null) {
			if (other.mor.getType() != null)
				return false;
		} else if (!mor.getType().equals(other.mor.getType()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ManagedObjectReferenceWrapper [type=" + mor.getType() + ", value=" + mor.getValue() + "]";
	}

	/**
	 * @return the mor
	 */
	public ManagedObjectReference getMor() {
		return mor;
	}

}
