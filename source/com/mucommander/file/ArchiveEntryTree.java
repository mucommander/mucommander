package com.mucommander.file;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Maxence Bernard
 */
public class ArchiveEntryTree extends DefaultMutableTreeNode {

    public ArchiveEntryTree() {
    }


    public void addArchiveEntry(ArchiveEntry entry) {

        String entryPath = entry.getPath();
        int entryDepth = entry.getDepth();
        int slashPos = 0;
        DefaultMutableTreeNode node = this;
        for(int d=0; d<=entryDepth; d++) {
            if(d==entryDepth && !entry.isDirectory()) {
                // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Creating leaf for "+entryPath);
                node.add(new DefaultMutableTreeNode(entry, true));
                break;
            }

            String subPath = d==entryDepth?entryPath:entryPath.substring(0, (slashPos=entryPath.indexOf('/', slashPos)+1));
            // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("subPath="+subPath+" depth="+d+"("+entryDepth+")");

            int nbChildren = node.getChildCount();
            DefaultMutableTreeNode childNode = null;
            boolean matchFound = false;
            for(int c=0; c<nbChildren; c++) {
                childNode = (DefaultMutableTreeNode)node.getChildAt(c);
                if(((ArchiveEntry)childNode.getUserObject()).getPath().equals(subPath)) {
                    // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Found match for "+subPath);
                    matchFound = true;
                    break;
                }
            }

            if(matchFound) {
                if(d==entryDepth) {
                    // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Replacing entry for node "+childNode);
                    // Replace existing entry
                    childNode.setUserObject(entry);
                }
                else {
                    node = childNode;
                }
            }
            else {
                if(d==entryDepth) {		// Leaf
                    // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Creating node for "+entryPath);
                    node.add(new DefaultMutableTreeNode(entry, true));
                }
                else {
                    // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Creating node for "+subPath);
                    childNode = new DefaultMutableTreeNode(new SimpleArchiveEntry(subPath, entry.getDate(), 0, true), true);
                    node.add(childNode);
                    node = childNode;
                }
            }
        }
    }


    /**
     * Finds and returns the node that corresponds to the specified entry path, null if no entry matching the path
     * could be found.
     *
     * <p>Important note: the given path's separator character must be '/' and the path must be relative to the
     * archive's root, i.e. not start with a leading '/', otherwise the entry will not be found. Trailing separators
     * are ignored when paths are compared, for example the path 'temp' will match the entry 'temp/'.
     */
    public DefaultMutableTreeNode findEntryNode(String entryPath) {
        int entryDepth = ArchiveEntry.getDepth(entryPath);
        int slashPos = 0;
        DefaultMutableTreeNode currentNode = this;
        for(int d=0; d<=entryDepth; d++) {
            String subPath = d==entryDepth?entryPath:entryPath.substring(0, (slashPos=entryPath.indexOf('/', slashPos)+1));
            if(subPath.charAt(subPath.length()-1)=='/')     // Remove any trailing slash to compare paths without trailing slashs
                subPath = subPath.substring(0, subPath.length()-1);

            // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("subPath="+subPath+" depth="+d+"("+entryDepth+")");

            int nbChildren = currentNode.getChildCount();
            DefaultMutableTreeNode matchNode = null;
            for(int c=0; c<nbChildren; c++) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)currentNode.getChildAt(c);

                String childNodePath = ((ArchiveEntry)childNode.getUserObject()).getPath();
                if(childNodePath.charAt(childNodePath.length()-1)=='/')     // Remove any trailing slash to compare paths without trailing slashs
                    childNodePath = childNodePath.substring(0, childNodePath.length()-1);

                if(childNodePath.equals(subPath)) {
                    //					if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Found match for "+subPath);
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
