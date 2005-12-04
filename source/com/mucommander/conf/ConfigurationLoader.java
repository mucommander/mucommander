package com.mucommander.conf;

/**
 * Class used to load configuration data.
 * <p>
 * This class should in no case be used by anyone but the ConfigurationManager.
 * </p>
 * @author Nicolas Rinaudo
 */
class ConfigurationLoader implements ConfigurationTreeBuilder {
    /** Buffer for the configuration path. */
    private String variable = null;

    /**
     * Adds a new entry to the configuration path.
     * @param name name of the entry to add.
     */
    public void addNode(String name) {
        if(variable == null)
            variable = name;
        else
            variable += '.' + name;
    }
    /* End of method addNode(String) */

    /**
     * Removes the last entry in the path.
     * @param name name of the entry that was closed.
     */
    public void closeNode(String name) {
		int index;
		
		index = variable.lastIndexOf('.');
		if(index > 0)
			variable = variable.substring(0, index);
	}

    /**
     * Defines a new variable with the specified name and value.
     * @param name  variable's name.
     * @param value variable's value.
     */
    public void addLeaf(String name, String value) {
		ConfigurationManager.setVariable(variable + '.' + name, value);
	}
}
/* End of class ConfigurationLoader */
