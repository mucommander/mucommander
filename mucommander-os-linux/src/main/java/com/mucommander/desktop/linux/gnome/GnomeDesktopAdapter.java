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

package com.mucommander.desktop.linux.gnome;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.command.Command;
import com.mucommander.command.CommandException;
import com.mucommander.command.CommandManager;
import com.mucommander.command.CommandType;
import com.mucommander.commons.file.filter.FileFilter;
import com.mucommander.commons.file.filter.RegexpFilenameFilter;
import com.mucommander.desktop.DefaultDesktopAdapter;
import com.mucommander.desktop.DesktopInitialisationException;
import com.mucommander.desktop.TrashProvider;

/**
 * @author Nicolas Rinaudo, Maxence Bernard
 */
abstract class GnomeDesktopAdapter extends DefaultDesktopAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(GnomeDesktopAdapter.class);
	
    private static final String FILE_MANAGER_NAME = "Nautilus";
    private static final String EXE_OPENER        = "$f";

    protected static final String GVFS_OPEN  = "gvfs-open";
    protected static final String GNOME_OPEN = "gnome-open";
    protected static final String XDG_OPEN   = "xdg-open";
    protected static final String CMD_OPENER_COMMAND = "gnome-terminal --working-directory $f";

    /** Multi-click interval, cached to avoid polling the value every time {@link #getMultiClickInterval()} is called */
    private int multiClickInterval;

    @Override
    public abstract boolean isAvailable();

    protected abstract String getFileOpenerCommand();

    @Override
    public void init(boolean install) throws DesktopInitialisationException {
        String fileOpener = String.format("%s $f", getFileOpenerCommand());
        try {
            CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_OPENER_ALIAS,  fileOpener, CommandType.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.URL_OPENER_ALIAS,   fileOpener, CommandType.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.EXE_OPENER_ALIAS,   EXE_OPENER, CommandType.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_MANAGER_ALIAS, fileOpener, CommandType.SYSTEM_COMMAND, FILE_MANAGER_NAME));
            CommandManager.registerDefaultCommand(new Command(CommandManager.CMD_OPENER_ALIAS, CMD_OPENER_COMMAND, CommandType.SYSTEM_COMMAND, null));

            // Disabled actual permissions checking as this will break normal +x files.
            // With this, a +x PDF file will not be opened.
            /*
            // Identifies which kind of filter should be used to match executable files.
            if(JavaVersion.JAVA_6.isCurrentOrHigher())
                filter = new PermissionsFileFilter(PermissionTypes.EXECUTE_PERMISSION, true);
            else
            */
            FileFilter filter = new RegexpFilenameFilter("[^.]+", true);

            CommandManager.registerDefaultAssociation(CommandManager.EXE_OPENER_ALIAS, filter);

            try {
                multiClickInterval = GSettings.getMultiClickInterval();
            } catch (Exception e1) {
                LOGGER.debug("Error while retrieving double-click interval from GSettings", e1);
                try {
                    multiClickInterval = GConfTool.getMultiClickInterval();
                } catch (Exception e2) {
                    LOGGER.debug("Error while retrieving double-click interval from GConfTool", e2);
                    multiClickInterval = super.getMultiClickInterval();
                }
            }
        }
        catch(CommandException e) {throw new DesktopInitialisationException(e);}
    }

    /**
     * Returns the <code>/desktop/gnome/peripherals/mouse/double_click</code> GNOME configuration value.
     * If the returned value is not defined or could not be retrieved, the value of
     * {@link DefaultDesktopAdapter#getMultiClickInterval()} is returned.<br/>
     * The value is retrieved on initialization and never updated thereafter.
     * <p>
     * Note under Java 1.6 or below, the returned value does not match the one used by Java for generating multi-clicks
     * (see {@link DefaultDesktopAdapter#getMultiClickInterval()}, as Java uses the multi-click speed declared in
     * X Window's configuration, not in GNOME's. See <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5076635">
     * Java Bug 5076635</a> for more information.
     * </p>
     * @return the <code>/desktop/gnome/peripherals/mouse/double_click</code> GNOME configuration value.
     */
    @Override
    public int getMultiClickInterval() {
        return multiClickInterval;
    }

    @Override
    public TrashProvider getTrash() {
        return new GnomeTrashProvider();
    }
}
