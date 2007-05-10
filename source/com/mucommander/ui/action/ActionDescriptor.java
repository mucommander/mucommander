package com.mucommander.ui.action;

import java.util.Hashtable;

/**
 * A descriptor class for {@link MucoAction} instances. An ActionDescriptor is the combination of a MucoAction
 * class (a class extending MucoAction and following its conventions) and a set of properties used for instanciation.
 * Thus, it not only identifies an action class but also the way it is instanciated.
 *
 * <p>Two ActionDescriptor instances are equal only if:
 * <ul>
 *  <li>they refer to the same MucoAction class
 *  <li>both sets of initialization properties are equal, i.e. they contain the same key/value pairs (deep equality)
 * </ul>
 * This means that two ActionDescriptor instances referring to the same MucoAction class but with a different set of
 * initialization properties will not be equal.
 *
 * <p>This class is used by ActionManager to instance MucoAction and allow several instances to live in memory only if
 * they have different initialization properties, which translated into action speak means a different appearance
 * and/or behavior.
 *
 * @see ActionManager
 * @see MucoAction
 *
 * @author Maxence Bernard
 */
public class ActionDescriptor {

    /** MucoAction class descriptor */
    private Class actionClass;

    /** Intialization properties, null if there are no initialization properties */
    private Hashtable properties;


    /**
     * Convenience constructor which has the same effect as calling {@link #ActionDescriptor(Class, Hashtable)}
     * with a null value for the <code>properties</code> parameter.
     *
     * @param actionClass a MucoAction Class descriptor
     */
    public ActionDescriptor(Class actionClass) {
        this(actionClass, null);
    }

    /**
     * Creates a new ActionDescriptor which identifies the specified combination of MucoAction class and initialization
     * properties. The <code>properties</code> parameter may be <code>null</code> if the action class is to be
     * instanciated without any initialization properties.
     *
     * <p>The specified Class *must* denote a class that extends {@link MucoAction} and follows its conventions
     * (provide the proper constructor), otherwise {@link ActionManager} will fail to instanciate it. However,
     * ActionDescriptor does not check if the specified Class is valid or not.
     * 
     * @param mucoActionClass a MucoAction Class descriptor
     * @param initProperties a Hashtable containing the properties that will be used to instanciate the specified MucoAction class
     */
    public ActionDescriptor(Class mucoActionClass, Hashtable initProperties) {
        this.actionClass = mucoActionClass;
        this.properties = initProperties;
    }

    /**
     * Returns a Class instance referring to a class that extends MucoAction.
     */
    public Class getActionClass() {
        return actionClass;
    }

    /**
     * Returns the list of properties that are to be used to instanciate the MucoAction class, or <code>null</code> if
     * there are none.
     */
    public Hashtable getInitProperties() {
        return properties;
    }


    /**
     * Returns <code>true</code> if the given object is an ActionDescriptor that is equal to this one.
     * ActionDescriptor instances are considered equal if they refer to the same {@link MucoAction} class and
     * set of initialization properties.
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
     * Returns a hash code value for this ActionDescriptor, making this class suitable for use as a key in a Hashtable.
     */
    public int hashCode() {
        return actionClass.hashCode() + 27*(properties==null?0:properties.hashCode());
    }

    /**
     * Returns a String representation of this ActionDescriptor. 
     */
    public String toString() {
        return super.toString()+" class="+actionClass.getName()+" properties="+properties;
    }
}
