package com.mucommander.conf;

/**
 * Class used to build a configuration tree.
 * <p>
 * When passed to the {@link net.muwire.common.manager.ConfigurationManager#buildConfigurationTree(ConfigurationTreeBuilder)}
 * method, an instance of the ConfigurationTreeBuilder interface will receive a description of the whole configuration tree.<br>
 * For example, a client software used to modify the server configuration could use this interface to create a graphical
 * representation of the configuration tree.
 * </p>
 * @author Nicolas Rinaudo
 */
public interface ConfigurationTreeBuilder {
    /**
     * Method called when a new node is found in the tree.
     * @param name node's name.
     */
    public void addNode(String name);

    /**
     * Method called when a node is closed.
     * @param name node's name.
     */
    public void closeNode(String name);

    /**
     * Method called when a new leaf is found.
     * @param name  leaf's name.
     * @param value leaf's value.
     */
    public void addLeaf(String name, String value);
}
/* End of interface ConfigurationTreeBuilder */
