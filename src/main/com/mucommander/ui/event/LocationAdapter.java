package com.mucommander.ui.event;

/**
 * An abstract adapter class for receiving location events.
 * The methods in this class are empty. This class exists as
 * convenience for creating listener objects.
 * 
 * 
 * @author Arik Hadas
 */
public abstract class LocationAdapter implements LocationListener {
	/**
     * {@inheritDoc}
     */
	public void locationChanging(LocationEvent locationEvent){}
	
	/**
     * {@inheritDoc}
     */
    public void locationChanged(LocationEvent locationEvent){}
    
    /**
     * {@inheritDoc}
     */
    public void locationCancelled(LocationEvent locationEvent){}
    
    /**
     * {@inheritDoc}
     */
    public void locationFailed(LocationEvent locationEvent){}
}
