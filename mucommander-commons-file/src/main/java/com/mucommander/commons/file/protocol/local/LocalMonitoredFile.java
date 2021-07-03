/**
 * This file is part of muCommander, http://www.mucommander.com
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
package com.mucommander.commons.file.protocol.local;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.ModificationDateBasedMonitoredFile;
import com.mucommander.commons.file.MonitoredFile;

/**
 * This class is an implementation of {@link MonitoredFile} that is based on a {@link WatchService}
 * if it is supported or falls back to the mechanism of {@link ModificationDateBasedMonitoredFile} otherwise.
 * This is useful for local files that reside in file systems in which the modification date of a folder
 * doesn't change upon changes to its content.
 * @author Arik Hadas
 */
public class LocalMonitoredFile extends ModificationDateBasedMonitoredFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalMonitoredFile.class);

    private WatchService watchService;
    private WatchKey watchKey;

    private static WatchEvent.Kind<?>[] kinds = new WatchEvent.Kind<?>[] {
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_DELETE,
        StandardWatchEventKinds.ENTRY_MODIFY};

    public LocalMonitoredFile(AbstractFile file) {
        super(file);
    }

    @Override
    public void startWatch() {
        Path path = Paths.get(getAbsolutePath());
        LOGGER.debug("start watching {}", this);
        if (isDirectory()) {
            try {
                watchService = FileSystems.getDefault().newWatchService();
                watchKey = path.register(watchService, kinds);
            } catch (IOException e) {
                watchService = null;
                LOGGER.error("failed to register WatchService", e);
                LOGGER.warn("fallback to monitor {} by polling", this);
            }
        }
        super.startWatch();
    }

    @Override
    public void stopWatch() {
        if (watchService != null) {
            LOGGER.debug("stop watching {}", this);
            watchKey.cancel();
            try {
                watchService.close();
            } catch (IOException e) {
                LOGGER.error("failed to close WatchService", e);
            }
        }
    }

    @Override
    public boolean isChanged(boolean periodicCheck) {
        if (watchService == null) {
            return super.isChanged(periodicCheck);
        }

        try {
            // if this is not a periodic check then we want to first check by
            // the modicifation date of the file as it appears to be faster
            // than getting events from WatchService on macOS
            if (!periodicCheck && super.isChanged(false)) {
                return true;
            }
            WatchKey watchKey = watchService.poll();
            if (watchKey != null) {
                watchKey.reset();
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.warn("unable to check changes in {}", this);
            LOGGER.error("exception while polling WatchService", e);
            return false;
        }
    }

}
