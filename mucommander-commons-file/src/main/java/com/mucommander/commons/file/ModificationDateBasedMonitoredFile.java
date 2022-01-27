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
package com.mucommander.commons.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is an implementation of {@link MonitoredFile} that is based
 * on checking the file's modication date to detect changes to its content.
 * @author Arik Hadas
 */
public class ModificationDateBasedMonitoredFile extends MonitoredFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModificationDateBasedMonitoredFile.class);
    /** the original modification date of the monitored file */
    protected long originalModificationDate;

    public ModificationDateBasedMonitoredFile(AbstractFile file) {
        super(file);
    }

    @Override
    public boolean isChanged(boolean periodicCheck) {
        // Note that date will be 0 if the folder is no longer available, and thus yield a refresh: this is exactly
        // what we want (the folder will be changed to a 'workable' folder).
        long currentModificationDate = getDate();
        boolean modificationDateChanged = originalModificationDate != currentModificationDate;
        LOGGER.debug("isChanged = {}", modificationDateChanged);
        return modificationDateChanged;
    }

    @Override
    public void startWatch() {
        originalModificationDate = getDate();
    }
}
