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

package dev.barebones.commander.ui.main.quicklist;

import javax.swing.Icon;

import dev.barebones.commander.commons.file.FileFactory;
import dev.barebones.commander.commons.file.FileURL;
import dev.barebones.commander.commons.file.protocol.local.LocalFile;
import dev.barebones.commander.commons.util.CollectionUtils;
import dev.barebones.commander.core.GlobalLocationHistory;
import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.text.Translator;
import dev.barebones.commander.ui.action.ActionProperties;
import dev.barebones.commander.ui.action.impl.ShowRecentLocationsQLAction;
import dev.barebones.commander.ui.main.FolderPanel;
import dev.barebones.commander.ui.quicklist.QuickListWithIcons;

/**
 * This quick list shows recently accessed locations.
 *
 * @author Arik Hadas
 */
public class RecentLocationsQL extends QuickListWithIcons<RecentLocationsQL.RecentLocation> {

    private FolderPanel folderPanel;

    public RecentLocationsQL(FolderPanel folderPanel) {
        super(folderPanel,
                ActionProperties.getActionLabel(ActionType.ShowRecentLocationsQL),
                Translator.get("recent_locations_quick_list.empty_message"));

        this.folderPanel = folderPanel;
    }

    @Override
    protected void acceptListItem(RecentLocation item) {
        folderPanel.tryChangeCurrentFolder(item.url);
    }

    @Override
    public RecentLocation[] getData() {
        return GlobalLocationHistory.Instance().getHistory().stream()
                // Don't include the currently presented location in the list
                .filter(url -> !folderPanel.getCurrentFolder().getURL().equals(url))
                .map(RecentLocation::new)
                .collect(CollectionUtils.reverse())
                .toArray(RecentLocation[]::new);
    }

    @Override
    protected Icon itemToIcon(RecentLocation item) {
        return getIconOfFile(FileFactory.getFile(item.url));
    }

    class RecentLocation {
        private FileURL url;

        RecentLocation(FileURL url) {
            this.url = url;
        }

        @Override
        public String toString() {
            switch(url.getScheme()) {
            case LocalFile.SCHEMA:
                String path = url.getPath();
                if (LocalFile.USES_ROOT_DRIVES && !path.isEmpty())
                    path = path.substring(1);
                return path;
            default:
                return url.toString();
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof RecentLocation)
                return url.equals(((RecentLocation) obj).url);
            return false;
        }
    }
}
