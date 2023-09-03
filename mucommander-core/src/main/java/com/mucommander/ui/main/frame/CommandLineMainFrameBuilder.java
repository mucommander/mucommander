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

package com.mucommander.ui.main.frame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.mucommander.ui.main.FolderPanel.FolderPanelType;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.tabs.ConfFileTableTab;

/**
 * @author Arik Hadas
 */
public class CommandLineMainFrameBuilder extends MainFrameBuilder {

    private final List<String> folders;

    public CommandLineMainFrameBuilder(final List<String> folders) {
        this.folders = new ArrayList<>(folders);
    }

    @Override
    public Collection<MainFrame> build() {
        final List<MainFrame> mainFrames = new LinkedList<>();

        Iterator<String> iterator = folders.iterator();
        while (iterator.hasNext()) {
            int nbMainFrames = mainFrames.size();
            MainFrame newMainFrame = new MainFrame(
                    new ConfFileTableTab(iterator.next()),
                    getFileTableConfiguration(FolderPanelType.LEFT, nbMainFrames),
                    new ConfFileTableTab(iterator.hasNext() ? iterator.next() : null),
                    getFileTableConfiguration(FolderPanelType.RIGHT, nbMainFrames));

            newMainFrame.getJFrame().setBounds(getDefaultSize());
            mainFrames.add(newMainFrame);
        }
        return mainFrames;
    }
}
