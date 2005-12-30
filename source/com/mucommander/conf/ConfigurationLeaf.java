package com.mucommander.conf;

/**
 * Holds the name and value of a configuration tree leaf.
 * <p>
 * Note that this class should never be accessed directly, except on very specific
 * exceptions. It was made public in order to allow configuration tree writers to use an iterator
 * on leafs instead of the slow sequential access(eg the infamous vector exploration loop).
 * </p>
 * @author Nicolas Rinaudo
 */
class ConfigurationLeaf {

    /** Leaf's name. */
    private String name;
    /** Leaf's value. */
    private String value;

    /* ----------------------- */
    /*      Initilisation      */
    /* ----------------------- */
    /**
     * Builds a new configuration leaf with the specified name and value.
     * @param name  leaf's name.
     * @param value leaf's value.
     */
    public ConfigurationLeaf(String name, String value) {
        setName(name);
        setValue(value);
    }

    /* ----------------------- */
    /*       Name access       */
    /* ----------------------- */
    /**
     * Returns the leaf's name.
     * @return the leaf's name.
     */
    public String getName() {return name;}

    /**
     * Sets the leaf's name.
     * @param name leaf's name.
     */
    public void setName(String name) {this.name = name;}

    /* ----------------------- */
    /*       Value access      */
    /* ----------------------- */
    /**
     * Returns the leaf's value.
     * @return the leaf's value.
     */
    public String getValue() {return value;}

    /**
     * Sets the leaf's value.
     * @param value leaf's value.
     */
    public void setValue(String value) {this.value = value;}
}
