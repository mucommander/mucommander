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

package dev.barebones.commander.desktop.macos;

import java.awt.Window;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apple.eawt.FullScreenUtilities;
import com.apple.eio.FileManager;
import com.dd.plist.BinaryPropertyListParser;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListFormatException;
import dev.barebones.commander.command.Command;
import dev.barebones.commander.command.CommandException;
import dev.barebones.commander.command.CommandManager;
import dev.barebones.commander.command.CommandType;
import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.commons.file.protocol.local.LocalFile;
import dev.barebones.commander.commons.runtime.OsFamily;
import dev.barebones.commander.commons.runtime.OsVersion;
import dev.barebones.commander.commons.util.Pair;
import dev.barebones.commander.commons.util.ui.text.MultiLineLabel;
import dev.barebones.commander.desktop.DefaultDesktopAdapter;
import dev.barebones.commander.desktop.DesktopInitialisationException;
import dev.barebones.commander.desktop.TrashProvider;
import dev.barebones.commander.os.notifier.AbstractNotifier;
import dev.barebones.commander.text.Translator;
import dev.barebones.commander.ui.macos.AppleScript;
import dev.barebones.commander.ui.macos.OSXIntegration;
import dev.barebones.commander.ui.macos.TabbedPaneUICustomizer;
import dev.barebones.commander.ui.notifier.GrowlNotifier;
import com.sun.jna.platform.mac.XAttrUtils;

/**
 * @author Nicolas Rinaudo, Arik Hadas
 */
public class OSXDesktopAdapter extends DefaultDesktopAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(OSXDesktopAdapter.class);

    private static final String OPENER_COMMAND = "open $f";
    private static final String FINDER_COMMAND = "open -a Finder $f";
    private static final String FINDER_NAME    = "Finder";
    // HINT: will work almost for every directory BUT NOT for /tmp on MacOS
    private static final String CMD_OPENER_COMMAND = "open -a Terminal $f";

    /** The key of the comment attribute in file metadata */
    public static final String COMMENT_PROPERTY_NAME = "com.apple.metadata:kMDItemFinderComment";
    public static final String TAGS_PROPERTY_NAME = "com.apple.metadata:_kMDItemUserTags";

    public String toString() {return "macOS Desktop";}

    @Override
    public boolean isAvailable() {return OsFamily.getCurrent().equals(OsFamily.MAC_OS);}

    @Override
    public void init(boolean install) throws DesktopInitialisationException {
        // Registers OS X specific commands.
        try {
            CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_OPENER_ALIAS,  OPENER_COMMAND, CommandType.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.URL_OPENER_ALIAS,   OPENER_COMMAND, CommandType.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_MANAGER_ALIAS, FINDER_COMMAND, CommandType.SYSTEM_COMMAND, FINDER_NAME));
            CommandManager.registerDefaultCommand(new Command(CommandManager.CMD_OPENER_ALIAS, CMD_OPENER_COMMAND, CommandType.SYSTEM_COMMAND, null));

            new OSXIntegration();
        }
        catch(CommandException e) {throw new DesktopInitialisationException(e);}
    }

    @Override
    public boolean isLeftMouseButton(MouseEvent e) {
        int modifiers = e.getModifiers();
        return (modifiers & MouseEvent.BUTTON1_MASK) != 0 && !e.isControlDown();
    }

    @Override
    public boolean isRightMouseButton(MouseEvent e) {
        int modifiers = e.getModifiers();
        return (modifiers & MouseEvent.BUTTON3_MASK) != 0 || ((modifiers & MouseEvent.BUTTON1_MASK) != 0 && e.isControlDown());
    }

    /**
     * Returns <code>true</code> for directories with an <code>app</code> extension (case-insensitive comparison).
     *
     * @param file the file to test
     * @return <code>true</code> for directories with an <code>app</code> extension (case-insensitive comparison).
     */
    @Override
    public boolean isApplication(AbstractFile file) {
        String extension = file.getExtension();

        // the isDirectory() test comes last as it is I/O bound
        return extension!=null && extension.equalsIgnoreCase("app") && file.isDirectory();
    }

    @Override
    public TrashProvider getTrash() {
        return new OSXTrashProvider();
    }

    @Override
    public AbstractNotifier getNotifier() {
        return GrowlNotifier.isGrowlRunning() ? new GrowlNotifier() : null;
    }

    @Override
    public Consumer<JTabbedPane> getTabbedPaneCustomizer() {
        return TabbedPaneUICustomizer::customizeTabbedPaneUI;
    }

    @Override
    public void postCopy(AbstractFile sourceFile, AbstractFile destFile) {
        if (sourceFile.hasAncestor(LocalFile.class) && destFile.hasAncestor(LocalFile.class)) {
            String sourcePath = sourceFile.getAbsolutePath();
            String destPath = destFile.getAbsolutePath();
            copyFileUserTags(sourcePath, destPath);
            copyFileTypeAndCreator(sourcePath, destPath);
            copyFileComment(sourcePath, destPath);
        }
    }

    private void copyFileUserTags(String sourcePath, String destPath) {
        byte[] bytes = XAttrUtils.read(sourcePath, TAGS_PROPERTY_NAME);
        if (bytes != null)
            XAttrUtils.write(destPath, TAGS_PROPERTY_NAME, bytes);
    }

    private void copyFileTypeAndCreator(String sourcePath, String destPath) {
        try {
            FileManager.setFileTypeAndCreator(destPath, FileManager.getFileType(sourcePath), FileManager.getFileCreator(sourcePath));
        } catch(IOException e) {
            // Swallow the exception and do not interrupt the transfer
            LOGGER.debug("Error while setting macOS file type and creator on destination", e);
        }
    }

    private void copyFileComment(String sourcePath, String destPath) {
        byte[] bytes = XAttrUtils.read(sourcePath, COMMENT_PROPERTY_NAME);
        if (bytes == null)
            return;

        String comment = null;
        try {
            NSString value = (NSString) BinaryPropertyListParser.parse(bytes);
            if (value != null)
                comment = value.getContent();
        } catch (IOException | PropertyListFormatException e) {
            // Swallow the exception and do not interrupt the transfer
            LOGGER.debug("Error while parsing macOS file comment of source", e);
        }
        if (comment != null && !"".equals(comment = comment.trim()) && !setFileComment(destPath, comment))
            LOGGER.error("Error while copying macOS file comment to %s", destPath);
    }

    private boolean setFileComment(String path, String comment) {
        String script = String.format(OSXFileUtils.SET_COMMENT_APPLESCRIPT, path, comment);
        return AppleScript.execute(script, null);
    }

    public void customizeMainFrame(Window window) {
        FullScreenUtilities.setWindowCanFullScreen(window, true);
    }

    @Override
    public List<Pair<JLabel, JComponent>> getExtendedFileProperties(AbstractFile file) {
        if (OsVersion.MAC_OS_10_4.isCurrentOrHigher()) {
            String comment = OSXFileUtils.getSpotlightComment(file);
            JLabel commentLabel = new JLabel(Translator.get("comment")+":");
            commentLabel.setAlignmentY(JLabel.TOP_ALIGNMENT);
            commentLabel.setVerticalAlignment(SwingConstants.TOP);
            return Collections.singletonList(new Pair<>(commentLabel, new MultiLineLabel(comment)));
        }
        return Collections.emptyList();
    }
}
