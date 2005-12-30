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
    void setVariable(String variable) {this.variable = variable;}

    /**
     * Returns the name of the variable that has been modified.
     * @return the name of the variable that has been modified.
     */
    public String getVariable() {return variable;}

    /* ------------------------ */
    /*       Value access       */
    /* ------------------------ */
    /**
     * Sets the new value of the variable that has been modified.
     * @param value new value of the variable that has been modified.
     */
    void setValue(String value) {this.value = value;}

    /**
     * Returns the new value of the variable that has been modified.
     * <p>
     * If the returned value is <i>null</i>, it means that the configuration variable
     * has been destroyed.
     * </p>
     * @return the new value of the variable that has been modified.
     */
    public String getValue() {return value;}
}
