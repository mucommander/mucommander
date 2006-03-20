package com.mucommander.conf;

/**
 * Event used to notify registered listeners that a configuration variable has been modified.
 *
 * @author Nicolas Rinaudo
 */
public class ConfigurationEvent {

    /** Name of the variable that has been modified. */
    private String variable;
    /** New value of the variable that has been modified. */
    private String value;

    /* ------------------------ */
    /*      Initialisation      */
    /* ------------------------ */
    /**
     * Builds a new configuration event with the specified variable name and value.
     * @param variable name of the variable that has been modified.
     * @param value    new value of the variable that has been modified.
     */
    ConfigurationEvent(String variable, String value) {
        setVariable(variable);
        setValue(value);
    }

    /* ------------------------ */
    /*        Name access       */
    /* ------------------------ */
    /**
     * Sets the name of the variable that has been modified.
     * @param variable name of the variable that has been modified.
     */
    void setVariable(String variable) { 
		this.variable = variable;
	}

    /**
     * Returns the name of the variable that has been modified.
     * @return the name of the variable that has been modified.
     */
    public String getVariable() {
		return variable;
	}

    /* ------------------------ */
    /*       Value access       */
    /* ------------------------ */
    /**
     * Sets the new value of the variable that has been modified.
     * @param value new value of the variable that has been modified.
     */
    void setValue(String value) {
		this.value = value;
	}

    /**
     * Returns the new value of the variable that has been modified.
     * <p>
     * If the returned value is <i>null</i>, it means that the configuration variable
     * has been destroyed.
     * </p>
     * @return the new value of the variable that has been modified.
     */
    public String getValue() {
		return value;
	}


    /**
     * Returns the new value of the variable that has been modified, parsed as an int.
     * <p>
	 * <b>Warning: </b>this method will return <code>-1</code> if the variable has been destroyed. 
	 * Use {@link #getValue getValue} and test the value against <code>null</code> to know if it has been destroyed.
     * </p>
     * @return the int value of the variable that has been modified, or -1 if the variable has been destroyed or
	 * the value could not be parsed as an int.
     */
    public int getIntValue() {
		if(value==null)
			return -1;
		
		try {
			return Integer.parseInt(value);
		}
		catch(NumberFormatException e) {
			return -1;
		}
	}


    /**
     * Returns the new value of the variable that has been modified, parsed as a float.
     * <p>
	 * <b>Warning: </b>this method will return <code>-1</code> if the variable has been destroyed. 
	 * Use {@link #getValue getValue} and test the value against <code>null</code> to know if it has been destroyed.
     * </p>
     * @return the float value of the variable that has been modified, or -1 if the variable has been destroyed or
	 * the value could not be parsed as a float.
     */
    public float getFloatValue() {
		if(value==null)
			return -1;
		
		try {
			return Float.parseFloat(value);
		}
		catch(NumberFormatException e) {
			return -1;
		}
	}


    /**
     * Returns the new value of the variable that has been modified, parsed as a boolean.
     * <p>
	 * <b>Warning: </b>this method will return <code>false</code> if the value has been destroyed. 
	 * Use {@link #getValue getValue} and test the value against <code>null</code> to know if it has been destroyed.
     * </p>
     * @return the boolean value of the variable that has been modified.
     */
    public boolean getBooleanValue() {
		if(value==null)
			return false;
		
		return value.equals("true");
	}
}
