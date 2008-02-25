/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.ui.list;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.FileSet;

import javax.swing.*;
import java.awt.*;

/**
 * FileList is a <code>JList</code> that displays information about a list of files: each row displays a file's name
 * and icon.
 *
 * <p>Since all <code>AbstractFile</code> methods are I/O bound and potentially lock-prone, it is not a good idea to
 * call them on request from the main event thread. To work around this, the constructor can preload all the information
 * subsequently needed by this list. This has a cost since all files will have to queried at init time, even if some
 * are not used (displayed) afterwards. On the other hand, navigation throughout the list will be faster.
 * Preloading can be enabled or disabled in the constructor but it should always enabled unless it is
 * known for certain that the underlying files are not I/O bound and cannot lock.</p>
 *
 * @author Maxence Bernard
 */
public class FileList extends JList {

    /** Files to display */
    protected FileSet files;
    /** True if file attribute preloading has been enabled */
    protected boolean fileAttributesPreloaded;

    /** Preloaded filenames, null if preloading is not enabled */
    protected String[] filenames;
    /** Preloaded file icons, null if preloading is not enabled */
    protected Icon[] icons;

    /** Custom font by the JLabel */
    protected Font customFont;


    /**
     * Creates a new FileList where each file in the given {@link FileSet} is displayed on a separate row. 
     *
     * @param files the set of files to display
     * @param preloadFileAttributes enables/disables file attribute preloading. It should always enabled unless it is known
     * for certain that the underlying files are not I/O bound and cannot lock.
     */
    public FileList(final FileSet files, boolean preloadFileAttributes) {
        this.files = files;

        int nbFiles = files.size();

        // Very important: allows the JList to operate in fixed cell height mode, which makes it substantially faster
        // to initialize when there is a large number of rows.
        if(nbFiles>0)
            setPrototypeCellValue(files.fileAt(0));

        if(preloadFileAttributes) {
            filenames = new String[nbFiles];
            icons = new Icon[nbFiles];
            AbstractFile file;
            for(int i=0; i<nbFiles; i++) {
                file = files.fileAt(i);
                filenames[i] = file.getName();
                icons[i] = file.getIcon();
            }

            this.fileAttributesPreloaded = true;
        }

        // Use a custom ListModel
        setModel(new AbstractListModel() {
            public int getSize() {
                return files.size();
            }

            public Object getElementAt(int index) {
                return files.fileAt(index);
            }
        });

        customFont = new JLabel().getFont();
        customFont = customFont.deriveFont(customFont.getStyle(), customFont.getSize()-2);

        // Use a custom ListCellRenderer
        setCellRenderer(new DefaultListCellRenderer() {

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(customFont);

                if(FileList.this.fileAttributesPreloaded) {
                    label.setText(filenames[index]);
                    label.setIcon(icons[index]);
                }
                else {
                    AbstractFile file = (AbstractFile)value;
                    label.setText(file.getName());
                    label.setIcon(file.getIcon());
                }

                return label;
            }
        });
    }

}
