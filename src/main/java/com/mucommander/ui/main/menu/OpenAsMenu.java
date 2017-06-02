/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2017 Maxence Bernard
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

package com.mucommander.ui.main.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.mucommander.commons.file.archive.ar.ArFormatProvider;
import com.mucommander.commons.file.archive.bzip2.Bzip2FormatProvider;
import com.mucommander.commons.file.archive.gzip.GzipFormatProvider;
import com.mucommander.commons.file.archive.iso.IsoFormatProvider;
import com.mucommander.commons.file.archive.lst.LstFormatProvider;
import com.mucommander.commons.file.archive.rar.RarFormatProvider;
import com.mucommander.commons.file.archive.sevenzip.SevenZipFormatProvider;
import com.mucommander.commons.file.archive.tar.TarFormatProvider;
import com.mucommander.commons.file.archive.zip.ZipFormatProvider;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.ActionParameters;
import com.mucommander.ui.action.impl.OpenAsAction;
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.main.MainFrame;

/**
 * Open as menu.
 * @author Arik Hadas
 */
public class OpenAsMenu extends JMenu {
    private MainFrame mainFrame;

    /**
     * Creates a new Open As menu.
     */
    public OpenAsMenu(MainFrame frame) {
        super(Translator.get("file_menu.open_as") + "...");
        this.mainFrame = frame;
        populate();
    }

    /**
     * Refreshes the content of the menu.
     */
    private synchronized void populate() {
        List<String> extensions = new ArrayList<>();
        extensions.addAll(Arrays.asList(ArFormatProvider.EXTENSIONS));
        extensions.addAll(Arrays.asList(Bzip2FormatProvider.EXTENSIONS));
        extensions.addAll(Arrays.asList(GzipFormatProvider.EXTENSIONS));
        extensions.addAll(Arrays.asList(IsoFormatProvider.EXTENSIONS));
        extensions.addAll(Arrays.asList(LstFormatProvider.EXTENSIONS));
        extensions.addAll(Arrays.asList(RarFormatProvider.EXTENSIONS));
        extensions.addAll(Arrays.asList(SevenZipFormatProvider.EXTENSIONS));
        extensions.addAll(Arrays.asList(TarFormatProvider.EXTENSIONS));
        extensions.addAll(Arrays.asList(ZipFormatProvider.EXTENSIONS));
        Collections.sort(extensions);
        for (String extension : extensions) {
            Action action = ActionManager.getActionInstance(new ActionParameters(OpenAsAction.Descriptor.ACTION_ID, Collections.singletonMap("extension", extension)), mainFrame);
            action.putValue(Action.NAME, extension.substring(1));
            add(action);
        }
    }

    @Override
    public final JMenuItem add(Action a) {
        JMenuItem item = super.add(a);
        MenuToolkit.configureActionMenuItem(item);
        return item;
    }

}
