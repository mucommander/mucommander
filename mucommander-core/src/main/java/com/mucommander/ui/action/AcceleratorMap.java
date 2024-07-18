/*
 * This file is part of muCommander, http://www.mucommander.com
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

import javax.swing.KeyStroke;

import com.mucommander.commons.util.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data structure that maps KeyStroke (accelerator) to MuAction id.
 * 
 * @author Arik Hadas
 */
public class AcceleratorMap {
    
	enum AcceleratorType {
	    PRIMARY,
	    ALTERNATIVE
	}
    
    // Maps KeyStrokes to MuAction id and accelerator type (PRIMARY_ACCELERATOR/ALTERNATIVE_ACCELERATOR) pair.
	private static Map<KeyStroke, List<Pair<ActionId, AcceleratorType>>> map = new HashMap<>();

	/**
	 * Register KeyStroke to MuAction as primary accelerator.
	 * 
	 * @param ks - accelerator
	 * @param actionId - id of MuAction to which the given accelerator would be registered.
	 */
	public void putAccelerator(KeyStroke ks, ActionId actionId) {
		put(ks, actionId, AcceleratorType.PRIMARY);
	}
	
	/**
	 * Register KeyStroke to MuAction as alternative accelerator.
	 * 
	 * @param ks - alternative accelerator.
	 * @param actionId - id of MuAction to which the given accelerator would be registered.
	 */
	public void putAlternativeAccelerator(KeyStroke ks, ActionId actionId) {
		put(ks, actionId, AcceleratorType.ALTERNATIVE);
	}
	
	/**
	 * Return id of MuAction that accelerator is registered to.
	 * 
	 * @param ks - accelerator.
	 * @return id of MuAction that the given accelerator is registered to.
	 */
    public ActionId getActionId(KeyStroke ks) {
        Pair<ActionId, ?> idAndType = getActionIdAndAcceleratorTypeOfKeyStroke(ks);
        return idAndType != null ? idAndType.first : null;
    }
    
    /**
     * Return accelerator type.
     * 
     * @param ks - accelerator.
     * @return the type of the given accelerator (primary(1)/alternative(2)/not registered(0)).
     */
    public AcceleratorMap.AcceleratorType getAcceleratorType(KeyStroke ks) {
        Pair<?, AcceleratorType> idAndType = getActionIdAndAcceleratorTypeOfKeyStroke(ks);
        return idAndType != null ? idAndType.second : null;
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
    
    private void put(KeyStroke ks, ActionId actionId, AcceleratorType acceleratorType) {
        if (ks != null) {
            var actions = map.getOrDefault(ks, Collections.emptyList());
            actions = actions.stream()
                    .filter(pair -> !ActionKeymap.isSameType(pair.first, actionId))
                    .collect(Collectors.toList());
            actions.add(new Pair<>(actionId, acceleratorType));
            map.put(ks, actions);
        }
    }
    
    private Pair<ActionId, AcceleratorType> getActionIdAndAcceleratorTypeOfKeyStroke(KeyStroke ks) {
        var actions = map.get(ks);
        return actions != null
                ? actions.stream().filter(a -> a.first.getType() != ActionId.ActionType.TERMINAL).findFirst().orElse(null)
                : null;
    }
}
