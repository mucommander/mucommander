package com.mucommander.conf;

import java.util.*;

/**
 * Holds a tree representation of the configuration file.
 * <p>
 * Each tree is composed of sub-trees(nodes) and variables(leafs).
 * </p>
 * <p>
 * Nodes are accessed through the {@link #getNode(String)} and
 * {@link #addNode(String)} method.<br>
 * The ConfigurationTree class also has a {@link #createNode(String)} method
 * to insert new nodes in the tree. This method should however be used cautiously.
 * It was written for optimisation purposes: in the case where a class is absolutely
 * sure that a node doesn't already exist in the tree, this method will be faster
 * than the regular {@link #addNode(String)} as it won't check for its presence.<br>
 * It is also possible to explore the current tree through the {@link #getNodeCount()},
 * {@link #getNodeAt(int)} and {@link #getNodes()} methods.
 * </p>
 * <p>
 * Leafs are retrieved and created in the same way as nodes, with the exception that
 * there is no <i>createLeaf</i> method(it was not deemed usefull, but may be added
 * in later releases).
 * </p>
 * @author Nicolas Rinaudo, Maxence Bernard
 */
class ConfigurationTree {
    /** Name of the root node. */
    private String     name;
    /** Different subnodes. */
    private LinkedList nodes;
    /** Different leafs. */
    private LinkedList leafs;

    /* ------------------------ */
    /*      Initialisation      */
    /* ------------------------ */
    /**
     * Builds a new ConfigurationTree with the specified name.
     * @param name name of the tree.
     */
    public ConfigurationTree(String name) {
        setName(name);
        nodes = new LinkedList();
        leafs = new LinkedList();
    }
    /* End of constructor ConfigurationTree(String) */

    /* ------------------------ */
    /*        Name access       */
    /* ------------------------ */
    /**
     * Returns the tree's name.
     * @return the tree's name.
     */
    public String getName() {return name;}

    /**
     * Sets the tree's name.
     * @param name tree's name.
     */
    public void setName(String name) {this.name = name;}

    /* ------------------------ */
    /*        Leaf access       */
    /* ------------------------ */
    /**
     * Returns the ConfigurationLeaf instance corresponding to the specified name.
     * @return the ConfigurationLeaf instance corresponding to the specified name, null if not found.x
     */
    ConfigurationLeaf getLeafInstance(String name) {
        Iterator          iterator;
        ConfigurationLeaf buffer;

        iterator = leafs.iterator();
        while(iterator.hasNext()) {
            buffer = (ConfigurationLeaf)iterator.next();
            if(buffer.getName().equals(name))
                return buffer;
        }
        return null;
    }
    /* End of method getLeafInstance(String) */

    /**
     * Returns the value of the specified leaf.
     * @return the value of the specified leaf, null if not found.
     */
    public String getLeaf(String name) {
        ConfigurationLeaf leaf;

        leaf = getLeafInstance(name);
        return leaf == null ? null : leaf.getValue();
    }
    /* End of method getLeaf(String) */

    /**
     * Sets the specified leaf's value.
     * <p>
     * If the specified leaf is not found, it will be created.
     * </p>
     * @param  name  leaf's name.
     * @param  value new value for the leaf.
     * @return true if the leaf has been modified as a result of this call, false otherwise.
     */
    public boolean setLeaf(String name, String value) {
        ConfigurationLeaf leaf;

        leaf = getLeafInstance(name);
        if(leaf == null) {
//            if(!nullValue) {
//                leafs.add(new ConfigurationLeaf(name, value));
//                return true;
//            }
//            return false;

			if(value==null || value.trim().equals(""))
				return false;
			leafs.add(new ConfigurationLeaf(name, value));
			return true;
        }
        else {
/*
            if(nullValue) {
                leafs.remove(leaf);
                return true;
            }
            else {
                if(leaf.getValue().equals(value))
                    return false;
                leaf.setValue(value);
                return true;
            }
*/
            if(value==null) {
                leafs.remove(leaf);
                return true;
            }
            else {
                if(leaf.getValue().equals(value))
                    return false;
                leaf.setValue(value);
                return true;
            }

        }
    }
    /* End of method setLeaf(String, String) */

    /**
     * Returns the number of leafs contained by this tree.
     * @return the number of leafs contained by this tree.
     */
    public int getLeafCount() {return leafs.size();}

    /**
     * Returns the leaf found at the specified index.
     * @param  index index of the leaf to retrieve.
     * @return the leaf found at the specified index.
     */
    public ConfigurationLeaf getLeafAt(int index) {return (ConfigurationLeaf)leafs.get(index);}

    /**
     * Returns an iterator on the leafs contained by this tree.
     * @return an iterator on the leafs contained by this tree.
     */
    public Iterator getLeafs() {return leafs.iterator();}

    /* ------------------------ */
    /*        Node access       */
    /* ------------------------ */
    /**
     * Creates a subnode with the specified name.
     * <p>
     * Developers should be carefull with this method, as it may create
     * duplicate nodes in the tree. It should only be called when one is sure
     * that the node doesn't already exist. Otherwise, the {@link #addNode(String)}
     * method should be used instead.
     * </p>
     * @param name name of the node to create.
     * @return the newly created node.
     */
    public ConfigurationTree createNode(String name) {
        ConfigurationTree node;

        nodes.add(node = new ConfigurationTree(name));
        return node;
    }
    /* End of method createNode(String) */

    /**
     * Adds a node with the specified name.
     * @param name name of the node to add.
     * @return the created node if one was created, the old node otherwise.
     */
    public ConfigurationTree addNode(String name) {
        ConfigurationTree node;

        if((node = getNode(name)) == null)
            nodes.add(node = new ConfigurationTree(name));
        return node;
    }
    /* End of method addNode(String) */

    /**
     * Returns the specified node.
     * @return the specified node if found, null otherwise.
     */
    public ConfigurationTree getNode(String node) {
        Iterator          iterator;
        ConfigurationTree buffer;

        iterator = nodes.iterator();
        while(iterator.hasNext()) {
            buffer = (ConfigurationTree)iterator.next();
            if(buffer.getName().equals(node))
                return buffer;
        }
        return null;
    }
    /* End of method getNode(String) */

    /**
     * Returns the number of nodes contained in this tree.
     * @return the number of nodes contained in this tree.
     */
    public int getNodeCount() {return nodes.size();}

    /**
     * Returns the configuration tree found at the specified index.
     * @param  index index of the node to retrieve.
     * @return the configuration tree found at the specified index.
     */
    public ConfigurationTree getNodeAt(int index) {return (ConfigurationTree)nodes.get(index);}

    /**
     * Returns an iterator on the tree's subnodes.
     * @return an iterator on the tree's subnodes.
     */
    public Iterator getNodes() {return nodes.iterator();}

    /**
     * Removes the specified node from the configuration tree.
     * @param node node to be removed.
     */
    public void removeNode(ConfigurationTree node) {nodes.remove(node);}

    /**
     * This method was writen for the sole use of the configuration manager and
     * should never be used in any other context.
     */
    void addNode(ConfigurationTree node) {nodes.add(node);}
}
/* End of class ConfigurationTree */
