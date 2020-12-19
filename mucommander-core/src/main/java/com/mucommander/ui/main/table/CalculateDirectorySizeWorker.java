/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
 *
 * trolCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * trolCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.ui.main.table;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.main.table.FileTableModel;
// import com.mucommander.ui.main.table.views.BaseFileTableModel;

import javax.swing.SwingWorker;
import java.io.IOException;
import java.util.List;

/**
 * @author Oleg Trifonov
 * Created on 09/01/14.
 */
public class CalculateDirectorySizeWorker extends SwingWorker<Long, Long> {
    /** Refresh rate in milliseconds  */
    private static final long REFRESH_RATE_MS = 300;

    private final FileTableModel fileTableModel;
    private final AbstractFile path;
    private final FileTable table;
    private long size;
    private long lastRefreshTime;

    public CalculateDirectorySizeWorker(FileTableModel fileTableModel, FileTable table, AbstractFile path) {
        this.fileTableModel = fileTableModel;
        this.table = table;
        this.path = path;
    }

    @Override
    protected Long doInBackground() {
        size = 0;
        try {
            calcDirectorySize(path);
        } catch (Exception e) {
            e.printStackTrace();
            size = -1;
        }
        return size;
    }

    @Override
    protected void done() {
        fileTableModel.addProcessedDirectory(path, table, size, true);
        fileTableModel.fillCellCache();
        table.repaint();
    }

    @Override
    protected void process(List<Long> chunks) {
        fileTableModel.addProcessedDirectory(path, table, size, false);
        fileTableModel.fillCellCache();
        table.repaint();
        // table.updateSelectedFilesStatusBar();
    }

    private void calcDirectorySize(AbstractFile path) throws IOException {
        if (isCancelled()) {
            return;
        }
        long tm = System.currentTimeMillis();
        if (tm - lastRefreshTime > REFRESH_RATE_MS) {
            lastRefreshTime = tm;
            publish(size);
        }
        if (path.isSymlink() && path != this.path) {
            return;
        }
        AbstractFile[] childs;
        try {
            childs = path.ls();
        } catch (IOException e) {
            return;
        }
        for (AbstractFile f : childs) {
            if (isCancelled()) {
                return;
            }
            if (f.isDirectory()) {
                calcDirectorySize(f);
            } else if (!f.isSymlink()) {
                size += f.getSize();
            }
        }

    }


    public AbstractFile getFile() {
        return path;
    }

}
