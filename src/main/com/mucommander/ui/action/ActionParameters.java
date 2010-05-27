/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

import java.util.Hashtable;

/**
 * A descriptor class for {@link MuAction} instances. An ActionParameters is the combination of a MuAction
 * class (a class extending MuAction and following its conventions) and a set of properties used for instantiation.
 * Thus, it not only identifies an action class but also the way it is instantiated.
 *
 * <p>Two ActionParameters instances are equal only if:
 * <ul>
 *  <li>they refer to the same action ID
 *  <li>both sets of initialization properties are equal, i.e. they contain the same key/value pairs (deep equality)
 * </ul>
 * This means that two ActionParameters instances referring to the same MuAction ID but with a different set of
 * initialization properties will not be equal.
 *
 * <p>This class is used by ActionManager to instance MuAction and allow several instances to live in memory only if
 * they have different initialization properties, which translated into action speak means a different appearance
 * and/or behavior.
 *
 * @see ActionManager
 * @see MuAction
 *
 * @author Maxence Bernard
 */
public class ActionParameters {

    /** Action ID */
    private String actionId;

    /** Initialization properties, null if there are no initialization properties */
    private Hashtable<String,Object> properties;


    /**
     * Convenience constructor which has the same effect as calling {@link #ActionParameters(String, Hashtable)}
     * with a null value for the <code>properties</code> parameter.
     *
     * @param actionId a MuAction id
     */
    public ActionParameters(String actionId) {
        this(actionId, null);
    }

    /**
     * Creates a new ActionParameters which identifies the specified combination of MuAction action ID and
     * initialization properties. The <code>properties</code> parameter may be <code>null</code> if the action class is
     * to be instantiated without any initialization properties.
     *
     * @param actionId a MuAction id
     * @param initProperties a Hashtable containing the properties that will be used to instantiate the specified MuAction class
     */
    public ActionParameters(String actionId, Hashtable<String,Object> initProperties) {
        this.actionId = actionId;
        this.properties = initProperties;
    }

    /**
     * Returns the action ID that was used to create this object.
     *
     * @return the action ID that was used to create this object.
     */
    public String getActionId() {
        return actionId;
    }

    /**
     * Returns the list of properties that are to be used to instantiate the MuAction class, or <code>null</code> if
     * there are none.
     *
     * @return the list of properties that are to be used to instantiate the MuAction class, or <code>null</code> if
     * there are none
     */
    public Hashtable<String,Object> getInitProperties() {
        return properties;
    }


    /**
     * Returns <code>true</code> if the given object is an ActionParameters that is equal to this one.
     * ActionParameters instances are considered equal if they refer to the same {@link MuAction} class and
     * set of initialization properties.
     */
    public boolean equals(Object o) {
        if(!(o instanceof ActionParameters))
            return false;

        ActionParameters ad = (ActionParameters)o;

        return actionId.equals(ad.actionId)
            && (((properties==null || properties.isEmpty()) && (ad.properties==null || ad.properties.isEmpty()))
                || (properties!=null && ad.properties!=null && properties.equals(ad.properties)));
    }


    /**
     * Returns a hash code value for this ActionParameters, making this class suitable for use as a key in a Hashtable.
     */
    public int hashCode() {
        return actionId.hashCode() + 27*(properties==null?0:properties.hashCode());
    }

    /**
     * Returns a String representation of this ActionParameters. 
     */
    public String toString() {
        return super.toString()+" class="+actionId+" properties="+properties;
    }
}
