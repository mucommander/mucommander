package com.mucommander.ui.action;

import java.util.Hashtable;

/**
 * 
 *
 * @author Maxence Bernard
 */
public class ActionDescriptor {

    private Class actionClass;
    private Hashtable properties;


    public ActionDescriptor(Class actionClass) {
        this(actionClass, null);
    }


    public ActionDescriptor(Class actionClass, Hashtable properties) {
        this.actionClass = actionClass;
        this.properties = properties;
    }


    public Class getActionClass() {
        return actionClass;
    }


    public Hashtable getActionProperties() {
        return properties;
    }


    /**
     * Returns true if the given object is an ActionDescriptior that is equal to this one. ActionDescriptor instances
     * are considered as equal if they have the same class and properties.
     */
    public boolean equals(Object o) {
        if(!(o instanceof ActionDescriptor))
            return false;

        ActionDescriptor ad = (ActionDescriptor)o;

        return actionClass.getName().equals(ad.actionClass.getName())
            && (((properties==null || properties.isEmpty()) && (ad.properties==null || ad.properties.isEmpty()))
                || (properties!=null && ad.properties!=null && properties.equals(ad.properties)));
    }


    /**
     * Returns a hash code value for this ActionDescriptor, making this class suitable for use as a key in a Map.
     */
    public int hashCode() {
        return actionClass.hashCode() + 27*(properties==null?0:properties.hashCode());
    }

    public String toString() {
        return super.toString()+" class="+actionClass.getName()+" properties="+properties;
    }
}