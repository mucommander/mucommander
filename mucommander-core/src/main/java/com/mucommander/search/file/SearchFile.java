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
package com.mucommander.search.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.PermissionAccess;
import com.mucommander.commons.file.PermissionBits;
import com.mucommander.commons.file.PermissionType;
import com.mucommander.commons.file.UnsupportedFileOperation;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.protocol.ProtocolFile;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;
import com.mucommander.job.FileJobState;
import com.mucommander.search.SearchBuilder;
import com.mucommander.search.SearchJob;
import com.mucommander.ui.main.MainFrame;

/**
 * @author Arik Hadas
 */
public class SearchFile extends ProtocolFile implements SearchListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchFile.class);
    private static final AbstractFile[] EMPTY_RESULTS = new AbstractFile[0];

    /** Time at which the search results were last modified. */
    private long lastModified;
    private Map<String, String> properties;
    private SearchJob search;
    private String searchStr;
    private AbstractFile searchPlace;

    protected SearchFile(FileURL url, Map<String, String> properties) throws IOException {
        super(url);
        this.properties = properties;
        searchStr = url.getPath().substring(1);
        searchPlace = FileFactory.getFile(url.getHost());
    }

    @Override
    public AbstractFile[] ls() throws IOException, UnsupportedFileOperationException {
        return search != null ? search.getFindings().toArray(EMPTY_RESULTS) : EMPTY_RESULTS;
    }

    @Override
    public long getDate() {
        return lastModified;
    }

    @Override
    public synchronized void searchChanged() {
        lastModified = System.currentTimeMillis();
    }

    @Override
    @UnsupportedFileOperation
    public void changeDate(long lastModified) throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.CHANGE_DATE);}
    @Override
    public long getSize() { return 0; }
    @Override
    public AbstractFile getParent() { return null; }
    @Override
    public void setParent(AbstractFile parent) {}
    @Override
    public boolean exists() { return true; }
    @Override
    public FilePermissions getPermissions() { return null; }
    @Override
    public PermissionBits getChangeablePermissions() { return null; }
    @Override
    @UnsupportedFileOperation
    public void changePermission(PermissionAccess access, PermissionType permission, boolean enabled) throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.CHANGE_PERMISSION);}
    @Override
    public String getOwner() { return null; }
    @Override
    public boolean canGetOwner() { return false; }
    @Override
    public String getGroup() { return null; }
    @Override
    public boolean canGetGroup() { return false; }
    @Override
    public boolean isDirectory() { return true; }
    @Override
    public boolean isSymlink() { return false; }
    @Override
    public boolean isSystem() { return false; }
    @Override
    @UnsupportedFileOperation
    public void mkdir() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.CREATE_DIRECTORY);}
    @Override
    @UnsupportedFileOperation
    public InputStream getInputStream() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.READ_FILE);}
    @Override
    @UnsupportedFileOperation
    public OutputStream getOutputStream() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.WRITE_FILE);}
    @Override
    @UnsupportedFileOperation
    public OutputStream getAppendOutputStream() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.APPEND_FILE);}
    @Override
    @UnsupportedFileOperation
    public RandomAccessInputStream getRandomAccessInputStream() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.RANDOM_READ_FILE);}
    @Override
    @UnsupportedFileOperation
    public RandomAccessOutputStream getRandomAccessOutputStream() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.RANDOM_WRITE_FILE);}
    @Override
    @UnsupportedFileOperation
    public void delete() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.DELETE);}
    @Override
    @UnsupportedFileOperation
    public void copyRemotelyTo(AbstractFile destFile) throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.COPY_REMOTELY);}
    @Override
    @UnsupportedFileOperation
    public void renameTo(AbstractFile destFile) throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.RENAME);}
    @Override
    @UnsupportedFileOperation
    public long getFreeSpace() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.GET_FREE_SPACE);}
    @Override
    @UnsupportedFileOperation
    public long getTotalSpace() throws UnsupportedFileOperationException {throw new UnsupportedFileOperationException(FileOperation.GET_TOTAL_SPACE);}
    @Override
    public Object getUnderlyingFileObject() {return null;}

    public boolean isSearchStarted() {
        // lastModified == 0 -> there was no search yet
        return lastModified != 0;
    }

    public boolean isSearchCompleted() {
        return search.getState() == FileJobState.FINISHED ||
                search.getState() == FileJobState.INTERRUPTED;
    }
    public void startSearch(MainFrame mainFrame) {
        lastModified = System.currentTimeMillis();
        search = createSearchJob(mainFrame);
        search.start();
    }

    public void stopSearch() {
        // TODO
    }

    public void retriggerSearch(MainFrame mainFrame) {
        if (isSearchCompleted())
            startSearch(mainFrame);
    }

    private SearchJob createSearchJob(MainFrame mainFrame) {
        return SearchBuilder.newSearch()
                .listener(SearchFile.this)
                .mainFrame(mainFrame)
                .what(searchStr)
                .where(searchPlace)
                .searchArchives(properties)
                .searchHidden(properties)
                .searchSubfolders(properties)
                .searchDepth(properties)
                .matchCaseInsensitive(properties)
                .matchRegex(properties)
                .build();
    }
}
