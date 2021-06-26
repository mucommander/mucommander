/*
 * This file is part of muCommander, http://www.mucommander.com
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
package com.mucommander.job.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.protocol.search.SearchListener;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.job.FileJob;
import com.mucommander.job.FileJobState;
import com.mucommander.ui.main.MainFrame;

/**
 * This job executes a file search.
 *
 * @author Arik Hadas
 */
public class SearchJob extends FileJob implements com.mucommander.commons.file.protocol.search.SearchJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchJob.class);

    private Predicate<AbstractFile> fileMatcher;
    private Predicate<AbstractFile> lsFilter;
    private List<AbstractFile> findings;
    private SearchListener listener;
    private int depth;

    private static final SearchListener nullListener = () -> {};

    public SearchJob(MainFrame mainFrame, FileSet files) {
        super(mainFrame, files);
        findings = new CopyOnWriteArrayList<>();
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setFileMatcher(Predicate<AbstractFile> fileMatcher) {
        this.fileMatcher = fileMatcher;
    }

    public void setListFilter(Predicate<AbstractFile> browseMatcher) {
        this.lsFilter = browseMatcher;
    }

    private List<AbstractFile> search(List<AbstractFile> files, boolean subfolder) {
        return files.parallelStream()
                .filter(subfolder ? lsFilter : file -> true)
                .map(this::search)
                .flatMap(stream -> stream)
                .collect(Collectors.toList());
    }

    private Stream<AbstractFile> search(AbstractFile file) {
        AbstractFile[] children;
        try {
            children = file.ls();
        } catch (IOException e) {
            LOGGER.debug("failed to list: " + file, e);
            return Stream.empty();
        }
        if (getState() != FileJobState.INTERRUPTED)
            examine(children);
        return Stream.of(children);
    }

    private void examine(AbstractFile[] files) {
        if (files.length == 0)
            return;
        List<AbstractFile> passed = Stream.of(files)
                .filter(fileMatcher)
                .collect(Collectors.toList());
        if (!passed.isEmpty()) {
            findings.addAll(passed);
            listener.searchChanged();
        }
    }

    public List<AbstractFile> getFindings() {
        return findings;
    }

    @Override
    public void interrupt() {
        setListener(null);
        super.interrupt();
    }

    public void setListener(SearchListener listener) {
        this.listener = listener != null ? listener : nullListener;
    }

    @Override
    protected boolean hasFolderChanged(AbstractFile folder) {
        return false;
    }

    @Override
    protected boolean processFile(AbstractFile file, Object recurseParams) {
        LOGGER.info("start searching {}", file);
        List<AbstractFile> files = Collections.singletonList(file);
        for (int i=0; getState() != FileJobState.INTERRUPTED && i<depth && !files.isEmpty(); i++) {
            files = search(files, i > 0);
        }
        LOGGER.info("completed searching {}", file);
        listener = null;
        return true;
    }
}
