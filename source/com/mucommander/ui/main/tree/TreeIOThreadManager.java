package com.mucommander.ui.main.tree;

/**
 * Monitors thread that reads children and icons for the tree.
 * @author Mariusz Jakubowski
 *
 */
public class TreeIOThreadManager extends AbstractIOThreadManager {

    public final static TreeIOThreadManager instance = new TreeIOThreadManager();
    
    private TreeIOThreadManager() {
        super("TreeIOThreadManager", 5000);
    }
    
    public static TreeIOThreadManager getInstance() {
        return instance;
    }
    
    
}
