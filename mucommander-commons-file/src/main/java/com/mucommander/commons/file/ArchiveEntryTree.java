/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.commons.file;

import com.mucommander.commons.file.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Stores archive entries and organizes them in a tree structure that maps entries in the way they are organized
 * inside the archive. An instance of <code>ArchiveEntryTree</code> also acts as the root node: all entry nodes
 * are children of it (direct or indirect).
 *
 * @author Maxence Bernard
 */
public class ArchiveEntryTree extends DefaultMutableTreeNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveEntryTree.class);

    /**
     * Creates a new empty tree.
     */
    public ArchiveEntryTree() {
    }

    /**
     * Adds the given entry to the archive tree, creating parent nodes as necessary.
     *
     * @param entry the entry to add to the tree
     */
    public void addArchiveEntry(ArchiveEntry entry) {
        String entryPath = entry.getPath();
        int entryDepth = entry.getDepth();
        int slashPos = 0;
        DefaultMutableTreeNode node = this;
        for(int d=1; d<=entryDepth; d++) {
            if(d==entryDepth && !entry.isDirectory()) {
                // Create a leaf node for the entry
                entry.setExists(true);      // the entry has to exist
                node.add(new DefaultMutableTreeNode(entry, true));
                break;
            }

            String subPath = d==entryDepth?entryPath:entryPath.substring(0, (slashPos=entryPath.indexOf('/', slashPos)+1));

            int nbChildren = node.getChildCount();
            DefaultMutableTreeNode childNode = null;
            boolean matchFound = false;
            for(int c=0; c<nbChildren; c++) {
                childNode = (DefaultMutableTreeNode)node.getChildAt(c);
                // Path comparison is 'trailing slash insensitive'
                if(PathUtils.pathEquals(((ArchiveEntry)childNode.getUserObject()).getPath(), subPath, "/")) {
                    // Found a match
                    matchFound = true;
                    break;
                }
            }

            if(matchFound) {
                if(d==entryDepth) {
                    LOGGER.trace("Replacing entry for node "+childNode);
                    // Replace existing entry
                    childNode.setUserObject(entry);
                }
                else {
                    node = childNode;
                }
            }
            else {
                if(d==entryDepth) {
                    // Create a leaf node for the entry
                    entry.setExists(true);      // the entry has to exist
                    node.add(new DefaultMutableTreeNode(entry, true));
                }
                else {
                    LOGGER.trace("Creating node for "+subPath);
                    childNode = new DefaultMutableTreeNode(new ArchiveEntry(subPath, true, entry.getDate(), 0, true), true);
                    node.add(childNode);
                    node = childNode;
                }
            }
        }
    }


    /**
     * Finds and returns the node that corresponds to the specified entry path, <code>null</code> if no entry matching
     * the path could be found.
     *
     * <p>Important note: the given path's separator character must be '/' and the path must be relative to the
     * archive's root, i.e. not start with a leading '/', otherwise the entry will not be found. Trailing separators
     * are ignored when paths are compared, for example the path 'temp' will match the entry 'temp/'.
     *
     * @param entryPath the path to the entry to look up in this tree
     * @return the node that corresponds to the specified entry path
     */
    public DefaultMutableTreeNode findEntryNode(String entryPath) {
        int entryDepth = ArchiveEntry.getDepth(entryPath);
        int slashPos = 0;
        DefaultMutableTreeNode currentNode = this;
        for(int d=1; d<=entryDepth; d++) {
            String subPath = d==entryDepth?entryPath:entryPath.substring(0, (slashPos=entryPath.indexOf('/', slashPos)+1));

            int nbChildren = currentNode.getChildCount();
            DefaultMutableTreeNode matchNode = null;
            for(int c=0; c<nbChildren; c++) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)currentNode.getChildAt(c);

                // Path comparison is 'trailing slash insensitive'
                if(PathUtils.pathEquals(((ArchiveEntry)childNode.getUserObject()).getPath(), subPath, "/")) {
                    // Found the node, let's return it
                    matchNode = childNode;
                    break;
                }
            }

            if(matchNode==null)
                return null;    // No node maching the provided path, return null

            currentNode = matchNode;
        }

        return currentNode;
    }
}
