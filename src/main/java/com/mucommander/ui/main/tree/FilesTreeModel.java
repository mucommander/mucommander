/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.main.tree;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.filter.FileFilter;
import com.mucommander.commons.file.util.FileComparator;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.icon.SpinningDial;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.Arrays;

/**
 * A tree model for files.
 * This class contains a tree structure defined by AbstractFile objects.

 * @author Mariusz Jakubowski
 * 
 */
public class FilesTreeModel implements TreeModel, CachedDirectoryListener {

    private DirectoryCache cache;
    
    /** Comparator used to sort folders */
    private FileComparator sort;
    
    /** Listeners. */
    protected EventListenerList listenerList = new EventListenerList();

    /** Root of the directory tree. */
    private AbstractFile root;

    /** number of caching children at the time, used to control spinning icon */
    private int cachingNum = 0;

    /** icon used to show that a children of a directory are being cached */
    private SpinningDial spinningIcon = new SpinningDial(16, 16, false);


    public FilesTreeModel(FileFilter filter, FileComparator sort) {
        super();
        this.sort = sort;
        cache = new DirectoryCache(filter, sort);
        cache.addCachedDirectoryListener(this);
    }

    /**
     * Changes the current root of a tree
     * Fires 'tree structure changed' event.
     * @param newRoot the new root of a tree
     */
    public void setRoot(AbstractFile newRoot) {
        final CachedDirectory cachedRoot = new CachedDirectory(newRoot, cache);
        cachedRoot.setCachedIcon(FileIcons.getFileIcon(newRoot));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                root = cachedRoot.getProxiedFile();
                cache.clear();
                cache.put(root, cachedRoot);
                TreePath path = new TreePath(root);
                fireTreeStructureChanged(this, path);
            }
        });
    }
    
    public Object getRoot() {
        return root;
    }
    

    /**
     * Returns children folders of a parent folder sorted by name.
     * @param parent parent folder
     * @return children folders
     */
    private AbstractFile[] getChildren(AbstractFile parent) {
        CachedDirectory cachedDir = cache.getOrAdd(parent);
        if (cachedDir.isCached())
            return cachedDir.get();
        else
            return null;
    }

    public Object getChild(Object parent, int index) {
        AbstractFile[] children = getChildren((AbstractFile) parent);
        if (children != null) {
            return children[index];
        }
        return null;
    }

    public int getChildCount(Object parent) {
        AbstractFile[] children = getChildren((AbstractFile) parent);
        if (children != null) {
            return children.length;
        }
        return 0;
    }

    public int getIndexOfChild(Object parent, Object child) {
        AbstractFile[] children = getChildren((AbstractFile) parent);
        if (children != null) {
            return Arrays.binarySearch(children, (AbstractFile)child, sort);
        }
        return 0;
    }

    public boolean isLeaf(Object node) {
        return false;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type.
     * @param source the node where the tree model has changed
     * @param path the path to the root node
     * @see EventListenerList
     */
    void fireTreeStructureChanged(Object source, TreePath path) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, path);
                }
                ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
            }
        }
    }

    /**
     * Builds the parents of node up to and including the root node,
     * where the original node is the last element in the returned array.
     * The length of the returned array gives the node's depth in the
     * tree.
     * 
     * @param aNode the TreeNode to get the path for
     */
    public AbstractFile[] getPathToRoot(AbstractFile aNode) {
        return getPathToRoot(aNode, 0);
    }

    /**
     * Builds the parents of node up to and including the root node,
     * where the original node is the last element in the returned array.
     * The length of the returned array gives the node's depth in the
     * tree.
     * 
     * @param aNode  the TreeNode to get the path for
     * @param depth  an int giving the number of steps already taken towards
     *        the root (on recursive calls), used to size the returned array
     * @return an array of TreeNodes giving the path from the root to the
     *         specified node 
     */
    protected AbstractFile[] getPathToRoot(AbstractFile aNode, int depth) {
        AbstractFile[]              retNodes;
    // This method recurses, traversing towards the root in order
    // size the array. On the way back, it fills in the nodes,
    // starting from the root and working back to the original node.

        /* Check for null, in case someone passed in a null node, or
           they passed in an element that isn't rooted at root. */
        if(aNode == null) {
            if(depth == 0)
                return null;
            else
                retNodes = new AbstractFile[depth];
        }
        else {
            depth++;
            if(aNode == root) {
                retNodes = new AbstractFile[depth];
            } else {
                retNodes = getPathToRoot(aNode.getParent(), depth);
            }
            retNodes[retNodes.length - depth] = aNode;
            cache.getOrAdd(aNode).isCached();       // ensures that a path is in cache
        }
        return retNodes;
    }
    
    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }

    /**
     * Refreshes tree model from given path.
     * @param path a path to refresh
     */
    public void refresh(TreePath path) {
        AbstractFile folder = (AbstractFile) path.getLastPathComponent();
        CachedDirectory cached = cache.get(folder);
        Icon cachedIcon = cached.getCachedIcon();        
        cache.removeWithChildren(folder);
        cached = cache.getOrAdd(folder);
        cached.setCachedIcon(cachedIcon);
        fireTreeStructureChanged(this, path);
    }

    public void cachingStarted(AbstractFile parent) {
        cachingNum++;
        if (cachingNum == 1) {
            spinningIcon.setAnimated(true);
        }
    }

    public void cachingEnded(AbstractFile parent) {
        cachingNum--;
        if (cachingNum == 0) {
            spinningIcon.setAnimated(false);
        }
        TreePath path = new TreePath(getPathToRoot(parent));
        fireTreeStructureChanged(this, path);
    }
    
    /**
     * Returns an icon of this directory or spinning icon if this directory is
     * being cached.
     * @return an icon of this directory or spinning icon if this directory is
     *         being cached.
     */
    public Icon getCurrentIcon(AbstractFile file) {
        CachedDirectory cached = cache.get(file);
        if (cached != null) {
            if (cached.isReadingChildren()) {
                return spinningIcon;
            }
            return cached.getCachedIcon();
        }
        return spinningIcon;
    }


}
