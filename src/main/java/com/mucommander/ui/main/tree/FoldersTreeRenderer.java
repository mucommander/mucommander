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
import com.mucommander.ui.theme.ThemeCache;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * A renderer for the directory tree. It renders model's items (which are
 * AbstractFiles), using file names. It also renders a correct icon for a folder.
 * 
 * @author Mariusz Jakubowski
 * 
 */
public class FoldersTreeRenderer extends DefaultTreeCellRenderer {

    private JTree tree;
    private FilesTreeModel model;
    
	public FoldersTreeRenderer(JTree tree) {
        super();
        this.tree = tree;
        this.model = (FilesTreeModel) tree.getModel();
    }
    
    @Override
    public Color getBackgroundSelectionColor() {
    	if (tree!=null && tree.hasFocus()) {
            return ThemeCache.backgroundColors[ThemeCache.ACTIVE][ThemeCache.SELECTED];    		
    	} else {
            return ThemeCache.backgroundColors[ThemeCache.INACTIVE][ThemeCache.SELECTED];    		
    	}
    }
    
    @Override
    public Color getBackgroundNonSelectionColor() {
    	if (tree!=null && tree.hasFocus()) {
    		return ThemeCache.backgroundColors[ThemeCache.ACTIVE][ThemeCache.NORMAL];
    	} else {
    		return ThemeCache.backgroundColors[ThemeCache.INACTIVE][ThemeCache.NORMAL];
    	}
    }
    
    @Override
    public Color getForeground() {
    	if (tree!=null && tree.hasFocus()) {
    		return selected ? ThemeCache.foregroundColors[ThemeCache.ACTIVE][ThemeCache.SELECTED][ThemeCache.FOLDER] : 
    			ThemeCache.foregroundColors[ThemeCache.ACTIVE][ThemeCache.NORMAL][ThemeCache.FOLDER];
    	} else {
    		return selected ? ThemeCache.foregroundColors[ThemeCache.INACTIVE][ThemeCache.SELECTED][ThemeCache.FOLDER] : 
    			ThemeCache.foregroundColors[ThemeCache.INACTIVE][ThemeCache.NORMAL][ThemeCache.FOLDER];
    	}
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        // get file name and create default component (JLabel) to display it
        AbstractFile file = (AbstractFile) value;
        String name = file.isRoot()?file.getAbsolutePath():file.getName();
        super.getTreeCellRendererComponent(tree, name, sel, expanded, leaf,
                row, hasFocus);
        setIcon(model.getCurrentIcon(file));
        return this;
    }

}
