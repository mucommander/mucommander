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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.protocol.search.SearchListener;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.util.Pair;
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
    private int depth, threads;

    private ExecutorService customThreadPool;

    private static final SearchListener nullListener = () -> {};

    public SearchJob(MainFrame mainFrame, FileSet files) {
        super(mainFrame, files);
        findings = new CopyOnWriteArrayList<>();
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public void setFileMatcher(Predicate<AbstractFile> fileMatcher) {
        this.fileMatcher = fileMatcher;
    }

    public void setListFilter(Predicate<AbstractFile> browseMatcher) {
        this.lsFilter = browseMatcher;
    }

    private Pair<List<AbstractFile>, Boolean> search(List<AbstractFile> files, boolean lsFilter) {
        try {
            List<AbstractFile> children = customThreadPool.submit(() -> ls(files, lsFilter)).get();
            List<AbstractFile> matches = customThreadPool.submit(() -> match(children)).get();
            boolean searchChanged = findings.addAll(matches);
            return new Pair<>(children, searchChanged);
        } catch (Exception e) {
            return new Pair<>(Collections.emptyList(), false);
        }
    }

    private List<AbstractFile> ls(List<AbstractFile> files, boolean filter) {
        Stream<AbstractFile> stream = files.parallelStream();
        if (filter)
            stream = stream.filter(lsFilter);
        return stream.map(this::ls).flatMap(s -> s).collect(Collectors.toList());
    }

    private List<AbstractFile> match(List<AbstractFile> files) {
        return files.parallelStream().filter(this::match).collect(Collectors.toList());
    }

    private Stream<AbstractFile> ls(AbstractFile file) {
        if (getState() != FileJobState.INTERRUPTED) {
            try {
                return Stream.of(file.ls());
            } catch (IOException e) {
                LOGGER.debug("failed to list: " + file, e);
            }
        }
        return Stream.empty();
    }

    private boolean match(AbstractFile file) {
        return getState() != FileJobState.INTERRUPTED && fileMatcher.test(file);
    }

    public List<AbstractFile> getFindings() {
        return findings;
    }

    @Override
    public void interrupt() {
        setListener(null);
        if (customThreadPool != null)
            customThreadPool.shutdown();
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
        customThreadPool = threads > 0 ? new ForkJoinPool(threads) : new ForkJoinPool();
        try {
            List<AbstractFile> files = Collections.singletonList(file);
            for (int i=0; getState() != FileJobState.INTERRUPTED && i<depth && !files.isEmpty(); i++) {
                Pair<List<AbstractFile>, Boolean> result = search(files, i != 0);
                files = result.first;
                if (result.second)
                    listener.searchChanged();
            }
        } finally {
            LOGGER.info("completed searching {}", file);
            listener = null;
            customThreadPool.shutdown();
        }
        return true;
    }
}
