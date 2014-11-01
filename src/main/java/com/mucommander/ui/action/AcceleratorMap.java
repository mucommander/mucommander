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

package com.mucommander.ui.action;

import javax.swing.*;

import com.mucommander.commons.util.Pair;

import java.util.HashMap;

/**
 * Data structure that maps KeyStroke (accelerator) to MuAction id.
 * 
 * @author Arik Hadas
 */
public class AcceleratorMap {
    
	// Accelerator types
	public final static int PRIMARY_ACCELERATOR = 1;
    public final static int ALTERNATIVE_ACCELERATOR = 2;
    
    // Maps KeyStrokes to MuAction id and accelerator type (PRIMARY_ACCELERATOR/ALTERNATIVE_ACCELERATOR) pair.
	private static HashMap<KeyStroke, Pair<String, Integer>> map = new HashMap<KeyStroke, Pair<String, Integer>>();
    
	/**
	 * Register KeyStroke to MuAction as primary accelerator.
	 * 
	 * @param ks - accelerator
	 * @param actionId - id of MuAction to which the given accelerator would be registered.
	 */
	public void putAccelerator(KeyStroke ks, String actionId) {
		put(ks, actionId, PRIMARY_ACCELERATOR);
	}
	
	/**
	 * Register KeyStroke to MuAction as alternative accelerator.
	 * 
	 * @param ks - alternative accelerator.
	 * @param actionId - id of MuAction to which the given accelerator would be registered.
	 */
	public void putAlternativeAccelerator(KeyStroke ks, String actionId) {
		put(ks, actionId, ALTERNATIVE_ACCELERATOR);
	}
	
	/**
	 * Return id of MuAction that accelerator is registered to.
	 * 
	 * @param ks - accelerator.
	 * @return id of MuAction that the given accelerator is registered to.
	 */
    public String getActionId(KeyStroke ks) {
    	Pair<String, Integer> idAndType = getActionIdAndAcceleratorTypeOfKeyStroke(ks);
    	return idAndType != null ? idAndType.first : null;
    }
    
    /**
     * Return accelerator type.
     * 
     * @param ks - accelerator.
     * @return the type of the given accelerator (primary(1)/alternative(2)/not registered(0)).
     */
    public int getAcceleratorType(KeyStroke ks) {
    	Pair<String, Integer> idAndType = getActionIdAndAcceleratorTypeOfKeyStroke(ks);
    	return idAndType != null ? idAndType.second : 0;
    }
    
    /**
     * Remove accelerator.
     * 
     * @param ks - accelerator.
     */
    public void remove(KeyStroke ks) {
    	map.remove(ks);
    }
    
    /**
     * Remove all accelerators.
     */
    public void clear() {
    	map.clear();
    }
    
    private void put(KeyStroke ks, String actionId, int acceleratorType) {
    	if (ks != null)
    		map.put(ks, new Pair<String, Integer>(actionId, acceleratorType));
    }
    
    private Pair<String, Integer> getActionIdAndAcceleratorTypeOfKeyStroke(KeyStroke ks) {
    	return map.get(ks);
    }
}
