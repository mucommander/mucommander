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

package com.mucommander.desktop.macos;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.mucommander.commons.runtime.JavaVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apple.eawt.Application;
import com.apple.eawt.FullScreenUtilities;
import com.apple.eio.FileManager;
import com.dd.plist.BinaryPropertyListParser;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListFormatException;
import com.mucommander.command.Command;
import com.mucommander.command.CommandException;
import com.mucommander.command.CommandManager;
import com.mucommander.command.CommandType;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.protocol.local.LocalFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.util.Pair;
import com.mucommander.commons.util.StringUtils;
import com.mucommander.commons.util.ui.text.MultiLineLabel;
import com.mucommander.desktop.ActionShortcuts;
import com.mucommander.desktop.DefaultDesktopAdapter;
import com.mucommander.desktop.DesktopInitialisationException;
import com.mucommander.desktop.TrashProvider;
import com.mucommander.os.notifier.AbstractNotifier;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.icon.FileIcons;
import com.mucommander.ui.macos.AppleScript;
import com.mucommander.ui.macos.OSXIntegration;
import com.mucommander.ui.macos.TabbedPaneUICustomizer;
import com.mucommander.ui.notifier.GrowlNotifier;
import com.sun.jna.platform.mac.XAttrUtils;


/**
 * @author Nicolas Rinaudo, Arik Hadas
 */
public class OSXDesktopAdapter extends DefaultDesktopAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(OSXDesktopAdapter.class);

    private static final String OPENER_COMMAND = "open $f";
    /**
     * we use AppleScript for opening URLs rather than the 'open' command since in recent macOS
     * versions the latter always encodes the specified URL, even when it is already encoded
     * see https://github.com/mucommander/mucommander/issues/1417
     */
    private static final String OPENER_LOCATION_COMMAND = "osascript -e open\\ location\\ \"$f\"";
    private static final String FINDER_COMMAND = "open -R $f";
    private static final String FINDER_NAME    = "Finder";
    // HINT: will work almost for every directory BUT NOT for /tmp on MacOS
    private static final String CMD_OPENER_COMMAND = "open -a Terminal $f";

    private static final String DEFAULT_MACOS_SHELL = "/bin/bash";

    private static final String DEFAULT_SHELL_INTERACTIVE = "--login";

    // cached values
    private String dutiCmdPath;

    private Map<String, String> utiForExt = createSizeLimitedMap(200);

    private Map<String, List<String>> bundleIdsForUti = createSizeLimitedMap(100);

    private Map<String, Pair<String, String>> appPathsForBundleId = createSizeLimitedMap(100);
    // /cached values

    /** The key of the comment attribute in file metadata */
    public static final String COMMENT_PROPERTY_NAME = "com.apple.metadata:kMDItemFinderComment";
    public static final String TAGS_PROPERTY_NAME = "com.apple.metadata:_kMDItemUserTags";

    @Override
    public String toString() {
        return "macOS Desktop";
    }

    @Override
    public boolean isAvailable() {return OsFamily.getCurrent().equals(OsFamily.MAC_OS);}

    @Override
    public void init(boolean install) throws DesktopInitialisationException {
        // Registers OS X specific commands.
        try {
            CommandManager.registerDefaultCommand(new Command(CommandManager.FILE_OPENER_ALIAS,  OPENER_COMMAND, CommandType.SYSTEM_COMMAND, null));
            CommandManager.registerDefaultCommand(new Command(CommandManager.URL_OPENER_ALIAS,   OPENER_LOCATION_COMMAND, CommandType.SYSTEM_COMMAND, null));
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

    @Override
    public String getDefaultShell() {
        return getMacOsUserShell() + " " + DEFAULT_SHELL_INTERACTIVE;
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
        String comment = OSXFileUtils.getSpotlightComment(file);
        JLabel commentLabel = new JLabel(Translator.get("comment")+":");
        commentLabel.setAlignmentY(JLabel.TOP_ALIGNMENT);
        commentLabel.setVerticalAlignment(SwingConstants.TOP);
        return Collections.singletonList(new Pair<>(commentLabel, new MultiLineLabel(comment)));
    }

    @Override
    public ActionShortcuts getActionShortcuts() {
        return new com.mucommander.desktop.macos.ActionShortcuts();
    }

    /**
     * Tries to figure out a default shell for a current user under macOS.
     * @return a shell path
     */
    private String getMacOsUserShell() {
        StringBuilder result = new StringBuilder(DEFAULT_MACOS_SHELL);
        boolean success = runCommand(
                new String[]{"dscl", ".", "-read" , System.getProperty("user.home"), "UserShell"},
                false, 0, s -> {
            String prefix = "UserShell: ";
            if (s.startsWith(prefix)) {
                result.setLength(0);
                result.append(s.substring(prefix.length()));
                return true;    // we're good, no further searching needed
            }
            return false;       // continue searching
        });
        if (success) {
            LOGGER.info("Going to use the following shell: {}", result);
        } else {
            LOGGER.error("Error finding default shell for user, going to use a default one");
        }
        return result.toString();
    }

    @Override
    public boolean isOpenWithAppsAvailable() {
        AtomicBoolean result = new AtomicBoolean(false);
        var dutiCmdPath = getPathOfDutiCmd();
        if (dutiCmdPath == null) {
            return result.get();
        }
        // additional checking if 'duty' a) works b) is what it should be
        runCommand(new String[]{dutiCmdPath, "-h"}, true, 1, s -> {
            // a simple sanity check of 'duti' command output
            if (s.contains("bundle_id")) {
                result.set(true);
                return true;    // we're good, no further searching needed
            }
            return false;       // continue searching
        });
        LOGGER.info("Command 'duti' found in the system? {}", result);
        return result.get();
    }

    @Override
    public List<Command> getAppsForOpenWith(AbstractFile file) {
        List<Command> result = new ArrayList<>();
        if (getPathOfDutiCmd() == null) {
            return result;
        }
        String uti = getUtiForExtension(file.getExtension());
        Set<Command> sorted = new TreeSet<>(Comparator.comparing(o -> o.getDisplayName().toLowerCase()));
        for (String bundleId : getAppBundleIdsForUTI(uti)) {
            // Tried to fight with quotes around such bundle ids in Command and ProcessBuilder,
            // but I finally lost my patience - probably due to:
            // Command behavior: "It is important to remember that <code>"</code> characters are <b>not</b> removed from the resulting tokens."
            if (bundleId.contains(" ")) {
                LOGGER.error("Going to ignore {} as it contains spaces...", bundleId);
                continue;
            }
            Pair<String, String> appPair = getAppNameAndPathForBundleId(bundleId);
            String appName = appPair.first;
            Command cmd = new Command(
                    appName,
                    "open -b " + bundleId + " $f",
                    CommandType.NORMAL_COMMAND,
                    appName,
                    appPair.second != null ? FileIcons.getFileIcon(FileFactory.getFile(appPair.second)) : null
            );
            // default is the first, the rest is alpha sorted (mimicking Finder behavior)
            if (result.isEmpty()) {
                result.add(cmd);
            } else {
                sorted.add(cmd);
            }
        }

        result.addAll(sorted);
        LOGGER.info("For file: {} found the following commands: {}", file, result);
        return result;
    }

    @Override
    public boolean canEnableOpenWithApps() {
        return true;
    }

    @Override
    public void howToEnableOpenWithApps(Component parent) {
        if (canEnableOpenWithApps()) {
            InformationDialog.showDialog(InformationDialog.INFORMATION_DIALOG_TYPE, parent,
                    Translator.get("open_with_apps_dialog.title"),
                    Translator.get("open_with_apps_dialog.msg_macos"), null,null);
        }
    }

    /**
     * Returns UTI (https://en.wikipedia.org/wiki/Uniform_Type_Identifier) for a given extension (case insensitive).
     * @param ext the extension, can be null or empty
     * @return the UTI, never empty (will default to "public.data" for unknown or empty/null extension)
     */
    private String getUtiForExtension(String ext) {
        StringBuilder result = new StringBuilder("public.data");
        if (ext == null || ext.isBlank()) {
            return result.toString();
        }
        var dutiCmdPath = getPathOfDutiCmd();
        if (dutiCmdPath == null) {
            return result.toString();
        }
        ext = ext.toLowerCase();
        // tempted to use Map#computeIfAbsent, but it could introduce concurrency issues
        var cachedUti = utiForExt.get(ext);
        if (cachedUti != null) {
            result.setLength(0);
            result.append(cachedUti);
        } else {
            runCommand(new String[]{dutiCmdPath, "-e", ext}, false, 0, s -> {
                String typeIdentifier = "UTTypeIdentifier = ";
                int idx = s.indexOf(typeIdentifier);
                if (idx >= 0) {
                    String uti = s.substring(idx + typeIdentifier.length()).trim();
                    if (!uti.isBlank()) {
                        result.setLength(0);
                        result.append(uti);
                        return true;    // we're good, no further searching needed
                    }
                }
                return false;           // continue searching
            });
        }
        utiForExt.put(ext, result.toString());
        return result.toString();
    }

    /**
     * Method tries to find app name and its path from a given bundle id (the results are cached).
     * @param bundleId the bundle id
     * @return a pair, first is app name, second is its path (this can be null)
     */
    private Pair<String, String> getAppNameAndPathForBundleId(String bundleId) {
        Pair<String, String> cached = appPathsForBundleId.get(bundleId);
        if (cached != null) {
            return cached;
        }

        Pair result = new Pair();
        runCommand(new String[]{"mdfind", "kMDItemCFBundleIdentifier", "=", bundleId}, false,0, s -> {
            // a simple sanity check whether it is bundle id
            if (s.endsWith(".app")) {
                result.first = s.substring(s.lastIndexOf("/") + 1, s.lastIndexOf(".app"));
                result.second = s;
                return true;        // we're good, no further searching needed
            }
            return false;           // continue searching
        });
        if (result.first == null) {
            // if 'mdfind' way didn't work, try silly conversion of bundle id to app name
            result.first = StringUtils.capitalize(bundleId.substring(bundleId.lastIndexOf(".") + 1));
        }
        appPathsForBundleId.put(bundleId, result);
        return result;
    }

    /**
     * Returns the list of bundle ids for a given UTI (the results are cached).
     * @param uti the uti
     * @return the list of bundle ids
     */
    private List<String> getAppBundleIdsForUTI(String uti) {
        List<String> cached = bundleIdsForUti.get(uti);
        if (cached != null) {
            return cached;
        }

        List<String> result = new ArrayList<>();
        var dutiCmdPath = getPathOfDutiCmd();
        if (dutiCmdPath == null) {
            return result;
        }
        runCommand(new String[]{dutiCmdPath, "-l", uti}, false,0, s -> {
            // a simple sanity check whether it is bundle id
            if (s.contains(".")) {
                result.add(s.trim());
            }
            return false;           // continue searching
        });
        LOGGER.info("For UTI: {} found the following bundle ids: {}", uti, result);
        bundleIdsForUti.put(uti, result);
        return result;
    }

    /**
     * Tries to locate 'duty' command by invoking 'type', the result is cached.
     * @return the path of 'duty' command, or null if not located
     */
    private String getPathOfDutiCmd() {
        if (dutiCmdPath != null) {
            return dutiCmdPath;
        }
        Predicate<String> linePredicate = line -> {
            // a simple sanity check of 'duti' command output
            if (line.contains("duti")) {
                dutiCmdPath = line;
                return true;    // we're good, no further searching needed
            }
            return false;       // continue searching
        };
        var userShell = getMacOsUserShell();
        var findDutiCmd = "command -v duti";

        // first try well known locations per architecture (arm vs x86), then try without '-l' - it is ~10x faster, but
        // may not have a proper env settings though, later with -l (login, may have better env settings),
        // and finally with -i (interactive, it sources .zshrc)
        List<String[]> dutiCheckCmds = List.of(
                new String[]{ "ls",
                        JavaVersion.isArm64Architecture() ?  "/opt/homebrew/bin/duti" : "/usr/local/bin/duti" },
                new String[]{ userShell, "-c", findDutiCmd },
                new String[]{ userShell, "-l", "-c", findDutiCmd },
                new String[]{ userShell, "-i", "-c", findDutiCmd }

        );
        dutiCheckCmds.stream().anyMatch(cmd -> runCommand(cmd, false, 0, linePredicate));

        if (!StringUtils.isNullOrEmpty(dutiCmdPath)) {
            LOGGER.info("Command 'duti' found here: {}", dutiCmdPath);
        } else {
            dutiCmdPath = null; // nullify if was empty
            LOGGER.error("Command 'duti' not found");
        }
        return dutiCmdPath;
    }

    /**
     * A helper method to run a command with parameters.
     * @param commands command and its parameters
     * @param useStdErr whether to use Standard Error instead of Standard Input Stream
     * @param expectedExitCode the expected exit code
     * @param line a predicate receives a next line from the output, when it returns true no further line reading is done
     * @return true if command executed successfully
     */
    private boolean runCommand(String[] commands, boolean useStdErr, int expectedExitCode, Predicate<String> line) {
        if (commands == null || commands.length == 0) {
            throw new IllegalArgumentException("Given commands value is null or empty");
        }
        boolean result = false;
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(commands);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                    useStdErr ? proc.getErrorStream() : proc.getInputStream()));
            int exitCode = Integer.MIN_VALUE;
            boolean processExited;
            if (!(processExited = proc.waitFor(1000, TimeUnit.MILLISECONDS)) || (exitCode = proc.exitValue()) != expectedExitCode) {
                LOGGER.error("Unexpected result from running: '{}', timed out?: {}, exit code: {}", commands, !processExited, exitCode);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Stdout output of running the command: {}",
                            new BufferedReader(new InputStreamReader(proc.getInputStream())).lines().
                                    collect(Collectors.joining(System.lineSeparator())));
                    LOGGER.debug("Stderr output of running the command: {}",
                            new BufferedReader(new InputStreamReader(proc.getErrorStream())).lines().
                                    collect(Collectors.joining(System.lineSeparator())));
                }
                return result;
            }
            String s;
            while ((s = stdInput.readLine()) != null) {
                if (line.test(s)) {
                    break;
                }
            }
            result = true;
        } catch (Exception e) {
            LOGGER.error("Error executing command: {}. Error msg: {}", commands, e.getMessage(), e);
        }
        return result;
    }

    @Override
    public boolean setIconBadgeNumber(int numberOfJobs) {
        // Hmm, Taskbar.isTaskbarSupported returns false on my macOS 10.15.7 and java 14.0.2,
        // however on java 20 it works
        if (super.setIconBadgeNumber(numberOfJobs)) {
            return true;
        }
        if (numberOfJobs > 0) {
            Application.getApplication().setDockIconBadge(Integer.toString(numberOfJobs));
        } else {
            Application.getApplication().setDockIconBadge(null);
        }
        return true;
    }

    @Override
    public boolean setIconProgress(int progress) {
        // Hmm, Taskbar.isTaskbarSupported returns false on my macOS 10.15.7 and java 14.0.2,
        // however on java 20 it works
        if (super.setIconProgress(progress)) {
            return true;
        }
        if (progress >= 0 || progress <= 100) {
            Application.getApplication().setDockIconProgress(progress);
        } else {
            Application.getApplication().setDockIconProgress(-1);
        }
        return true;
    }

    public boolean requestUserAttention() {
        if (super.requestUserAttention()) {
            return true;
        }
        Application.getApplication().requestUserAttention(false);
        return true;
    }

    /**
     * Creates size limited map.
     * Idea taken from: https://stackoverflow.com/a/11469731/1715521
     *
     * @param maxSize max size
     * @return the size limited map
     * @param <K> key type
     * @param <V> value type
     */
    private static <K, V> Map<K, V> createSizeLimitedMap(int maxSize) {
        return new LinkedHashMap<K, V>(maxSize * 10 / 7, 0.7f, false) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxSize;
            }
        };
    }


}
