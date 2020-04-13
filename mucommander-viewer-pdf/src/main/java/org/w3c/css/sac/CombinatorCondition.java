/*
 * (c) COPYRIGHT 1999 World Wide Web Consortium
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 *
 * $Id: CombinatorCondition.java,v 1.3 2000/03/08 20:55:41 plehegar Exp $
 */
package org.w3c.css.sac;

/**
 * @version $Revision: 1.3 $
 * @author  Philippe Le Hegaret
 * @see Condition#SAC_AND_CONDITION
 * @see Condition#SAC_OR_CONDITION
 */
public interface CombinatorCondition extends Condition {

    /**
     * Returns the first condition.
     */    
    public Condition getFirstCondition();

    /**
     * Returns the second condition.
     */    
    public Condition getSecondCondition();
}
