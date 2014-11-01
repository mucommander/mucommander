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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.conf.ConfigurationEvent;
import com.mucommander.commons.conf.ConfigurationListener;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.filter.AndFileFilter;
import com.mucommander.commons.file.filter.AttributeFileFilter;
import com.mucommander.commons.file.filter.AttributeFileFilter.FileAttribute;
import com.mucommander.commons.file.util.FileComparator;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreferences;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.RefreshAction;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.main.ConfigurableFolderFilter;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.theme.ColorChangedEvent;
import com.mucommander.ui.theme.FontChangedEvent;
import com.mucommander.ui.theme.ThemeCache;
import com.mucommander.ui.theme.ThemeListener;

/**
 * A panel which contains a directory tree. This panel is attached to the left
 * side of the files table. It allows for a quick navigation in a directory
 * tree. Selecting folder on the tree changes folder in files folder.
 * 
 * @author Mariusz Jakubowski
 * 
 */
public class FoldersTreePanel extends JPanel implements TreeSelectionListener, 
							LocationListener, FocusListener, ThemeListener, 
							TreeModelListener, ConfigurationListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(FoldersTreePanel.class);
	
    /** Directory tree */
    private JTree tree;

    /** Folder panel to which this tree is attached */
    private FolderPanel folderPanel;

    /** A model with a directory tree */
    private FilesTreeModel model;

    /** A timer that fires a directory change */
    private ChangeTimer changeTimer = new ChangeTimer();

    static {
        TreeIOThreadManager.getInstance().start();
    }

   
    /**
     * Creates a panel with directory tree attached to a specified folder panel.
     * @param folderPanel a folder panel to attach tree
     */
    public FoldersTreePanel(FolderPanel folderPanel) {
        super();
        this.folderPanel = folderPanel;
        
        setLayout(new BorderLayout());

        // Filters out the files that should not be displayed in the tree view
        AndFileFilter treeFileFilter = new AndFileFilter(
            new AttributeFileFilter(FileAttribute.DIRECTORY),
            new ConfigurableFolderFilter()
        );

        FileComparator sort = new FileComparator(FileComparator.NAME_CRITERION, true, true);
        model = new FilesTreeModel(treeFileFilter, sort);
        tree = new JTree(model);
		tree.setFont(ThemeCache.tableFont);
        tree.setBackground(ThemeCache.backgroundColors[ThemeCache.INACTIVE][ThemeCache.NORMAL]);

        tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setExpandsSelectedPaths(true);
        tree.getModel().addTreeModelListener(this);

        JScrollPane sp = new JScrollPane(tree);
        // JScrollPane usually comes with a tiny border, remove it
        sp.setBorder(null);

        add(sp, BorderLayout.CENTER);

        // Create tree renderer. We're not using default tree renderer, because
        // AbstractFile.toString method returns full path, and we want to
        // display only a file name.
        FoldersTreeRenderer renderer = new FoldersTreeRenderer(tree);
        tree.setCellRenderer(renderer);

        tree.addTreeSelectionListener(this);
        tree.addFocusListener(this);

        // add a popup menu
        final JPopupMenu popup = new JPopupMenu();
        // refresh action
        JMenuItem item = new JMenuItem(
        		ActionProperties.getActionLabel(RefreshAction.Descriptor.ACTION_ID),
                KeyEvent.VK_R);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.refresh(tree.getSelectionPath());
//                model.fireTreeStructureChanged(tree, tree.getSelectionPath());
            }
        });
        popup.add(item);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        
        ThemeCache.addThemeListener(this);
        
        MuConfigurations.addPreferencesListener(this);
    }

    
    
    /** 
     * Listens to certain configuration variables.
     */
    public void configurationChanged(ConfigurationEvent event) {
        String var = event.getVariable();
        if (var.equals(MuPreferences.SHOW_HIDDEN_FILES) ||
                var.equals(MuPreferences.SHOW_DS_STORE_FILES) ||
                var.equals(MuPreferences.SHOW_SYSTEM_FOLDERS)) {
            Object root = model.getRoot();
            if (root != null) {
                TreePath path = new TreePath(root);
                model.refresh(path);
            }
        }
    }

    /**
     * Adds or removes location change listeners depending on the tree
     * visibility.
     */
    @Override
    public void setVisible(boolean flag) {
        super.setVisible(flag);
        if (flag) {
            updateSelectedFolder();
            folderPanel.getLocationManager().addLocationListener(this);
            // tree.requestFocus();
        } else {
            folderPanel.getLocationManager().removeLocationListener(this);
        }
    }


	/**
     * Updates selection in a tree to the current folder. When necessary updates
     * the current root of a tree. Invoked when location on folder pane has changed or 
     * when a tree has been updated (when directories have been loaded).
     */
    private void updateSelectedFolder() {
        final AbstractFile currentFolder = folderPanel.getCurrentFolder();

        // get selected directory (ignore archives - TODO make archives browsable (option))
        AbstractFile tempFolder = currentFolder;
        AbstractFile tempParent;
        while (!tempFolder.isDirectory()) {
            tempParent = tempFolder.getParent();
            if(tempParent==null)
                break;

            tempFolder = tempParent;
        }

        // compare selection on tree and panel
        final AbstractFile selectedFolder = tempFolder;
        TreePath selectionPath = tree.getSelectionPath();
        if (selectionPath != null) {
            if (selectionPath.getLastPathComponent() == currentFolder)
                return;
        }

        // check if root has changed
        final AbstractFile currentRoot = selectedFolder.getRoot();
        if (!currentRoot.equals(model.getRoot())) {
            model.setRoot(currentRoot);
        }
        // refresh selection on tree
        SwingUtilities.invokeLater(new Runnable() {
           public void run() {
               try {
                   TreePath path = new TreePath(model.getPathToRoot(selectedFolder));
                   tree.expandPath(path);
                   tree.setSelectionPath(path);
                   tree.scrollPathToVisible(path);
               } catch (Exception e) {
                   LOGGER.debug("Caught exception", e);
               }
            }
        });
    }

    /**
     * Refreshes folder after a change (e.g. mkdir).
     * @param folder a folder to refresh on the tree
     */
    public void refreshFolder(AbstractFile folder) {
        if (!isVisible())
            return;
        model.fireTreeStructureChanged(tree, new TreePath(model.getPathToRoot(folder)));
    }
    
    /**
     * Changes focus to tree.
     */
    @Override
    public void requestFocus() {
        tree.requestFocus();
    }

    /**
     * Returns tree component.
     * @return tree component
     */
    public JTree getTree() {
        return tree;
    }

    

    // - TreeSelectionListener code --------------------------------------------
    // -------------------------------------------------------------------------
    
    /**
     * This class is used to change folder after a user selects a folder in
     * tree. This change occurs after small delay (1 sec) to allow a user to
     * navigate a tree using keyboard.
     * 
     * @author Mariusz Jakubowski
     * 
     */
    private class ChangeTimer extends Timer {
        private transient AbstractFile folder;

        public ChangeTimer() {
            super(1000, null);
            setRepeats(false);
        }

        @Override
        public void fireActionPerformed(ActionEvent ae) {
            if (!folderPanel.getCurrentFolder().equals(folder)) {
                folderPanel.tryChangeCurrentFolder(folder);
            }
        }

    }
    

    /**
     * Changes the current folder in an associated folder panel, depending on
     * the current selection in tree.
     */
    public void valueChanged(TreeSelectionEvent e) {
        TreePath path = e.getNewLeadSelectionPath();
        if (path != null) {
            AbstractFile f = (AbstractFile) path.getLastPathComponent();
            if (f != null && f.isBrowsable() && f != folderPanel.getCurrentFolder()) {
                changeTimer.folder = f;
                changeTimer.restart();
            }
        }
    }

    // - LocationListener code -------------------------------------------------
    // -------------------------------------------------------------------------

    public void locationCancelled(LocationEvent locationEvent) {
    }

    public void locationChanged(LocationEvent locationEvent) {
        updateSelectedFolder();
    }

    public void locationChanging(LocationEvent locationEvent) {
    }

    public void locationFailed(LocationEvent locationEvent) {
    }

    
    // - FocusListener code ----------------------------------------------------
    // -------------------------------------------------------------------------
    
    public void focusGained(FocusEvent e) {
		tree.setBackground(ThemeCache.backgroundColors[ThemeCache.ACTIVE][ThemeCache.NORMAL]);	
	}

	public void focusLost(FocusEvent e) {
		tree.setBackground(ThemeCache.backgroundColors[ThemeCache.INACTIVE][ThemeCache.NORMAL]);	
	}

	
    // - ThemeListener code ----------------------------------------------------
    // -------------------------------------------------------------------------
	
	public void colorChanged(ColorChangedEvent event) {
		if (tree.hasFocus()) {
			tree.setBackground(ThemeCache.backgroundColors[ThemeCache.ACTIVE][ThemeCache.NORMAL]);	
		} else {
			tree.setBackground(ThemeCache.backgroundColors[ThemeCache.INACTIVE][ThemeCache.NORMAL]);	
		}
		tree.repaint();
	}

	public void fontChanged(FontChangedEvent event) {
		tree.setFont(ThemeCache.tableFont);
		tree.repaint();
	}

    // - TreeModelListener code ------------------------------------------------
    // -------------------------------------------------------------------------

	public void treeNodesChanged(TreeModelEvent e) {
    }

    public void treeNodesInserted(TreeModelEvent e) {
    }

    public void treeNodesRemoved(TreeModelEvent e) {
    }

    public void treeStructureChanged(TreeModelEvent e) {
        // ensures that a selection is repainted correctly
        // after nodes have been inserted                
        if (!changeTimer.isRunning()) {        
            updateSelectedFolder();
            tree.repaint();
        }
    }

}
