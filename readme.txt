           _____                           _        
 _____ _ _|     |___ _____ _____ ___ ___ _| |___ ___
|     | | |   --| . |     |     | .'|   | . | -_|  _|
|_|_|_|___|_____|___|_|_|_|_|_|_|__,|_|_|___|___|_| 


----------------------------
muCommander v0.8.4 (nightly)
----------------------------

muCommander is a cross-platform file manager featuring a Norton Commander
style interface and running on any operating system with Java support
(Mac OS X, Windows, Linux, *BSD, Solaris...).

Please use the muCommander forums (http://www.mucommander.com/forums)
to post your questions, suggestions or bug reports.
Your feedback is important and always welcome!

Official website: http://www.mucommander.com
Copyright (C) 2002-2009 Maxence Bernard.


Requirements
------------

Java Runtime Environment 1.4.0 (JRE) or later is required to run
muCommander. JRE 1.6 is recommended, you can download it at http://java.com.

Mac OS X users: your favorite OS already comes with a Java runtime so you're
good to go!

If you're having problems launching muCommander, make sure the $JAVA_HOME
environment variable points to the directory where your Java runtime is
installed.


License
-------

muCommander is released under the terms of the GNU General Public License.
Please refer to the 'license.txt' file bundled with muCommander.

muCommander uses the following great third party works :

- the Ant library released under the Apache License.
 Ant can be found at http://ant.apache.org .

- the ICU4J library released under the ICU License.
 the ICU project can be found at http://icu-project.org .

- the J2SSH library released under the GNU LGPL license.
 J2SSH can be found at http://www.sshtools.com .

- the Jakarta Commons Net library released under the Apache License.
 Jarkata Commons Net can be found at http://jakarta.apache.org .

- the jCIFS library released under the GNU LGPL license.
 jCIFS can be found at http://jcifs.samba.org .

- the JmDNS library released under the GNU LGPL license.
 JmDNS can be found at http://jmdns.sourceforge.net .

- the JNA library released under the GNU LGPL license.
 JmDNS can be found at http://jna.dev.java.net .

- the JUnRar library released as Freeware.
 JUnRar can be found at http://sourceforge.net/projects/java-unrar .

- the Yanfs library released under the BSD license.
 Yanfs can be found at http://yanfs.dev.java.net .

- Icons by Mark James released under the Creative Commons Attribution License.
 Mark James can be found at http://famfamfam.com .

- the Furbelow library released under the GNU LGPL license.
Furbelow can be found at http://sourceforge.net/projects/furbelow .


Credits
-------

Core developers: 
- Maxence Bernard
- Nicolas Rinaudo
- Arik Hadas
- Mariusz Jakubowski

Contributors:
- Ivan Baidakov
- Vassil Dichev
- Karel Klic
- David Kovar
- Joshua Lebo
- LeO
- Xavier Martin
- Alejandro Scandroli
- Alexander Yerenkow
- Johann Schmitz

Translators: 
- Frank Berger and Tony Klüver (German)
- Marcos Cobeña (Spanish)
- Jaromír Mára and Peter Vasko (Czech)
- Kent Hsu (Traditional Chinese)
- Jioh L. Jung (Korean)
- Andrzej Kosiński (Polish)
- György Varga and Tamás Balogh-Walder (Hungarian)
- 4X_Pro (Russian)
- whiteriver and Woodie (Simplified Chinese)
- Joze Kovacic (Slovenian)
- Catalin Hritcu (Romanian)
- Roberto Angeletti (Italian)
- Cristiano Duarte (Brazilian Portuguese)
- Pieter Kristensen (Dutch)
- Ján Ľudvík (Slovak)
- Jonathan Murphy (British English)
- Nardog (Japanese)
- Jakob Ekström (Swedish)
- Jeppe Toustrup (Danish)
- Mykola Bilovus (Ukrainian)
- ChArLoK_16 (Arabic)

Special thanks:
- Semyon Filippov (muCommander icon)
- Stefano Perelli (former muCommander icon)

Many thanks to all of you who suggested new features, reported bugs, sent warm
emails or generously donated to the project !


What's new since v0.8.3 ?
-------------------------

New features:
- Added native support for 7z archives.
- Added a keyboard shortcuts editor. Originally contributed by Johann Schmitz
  and improved by Arik Hadas (ticket #84).
- Added a command bar editor.
- Added a debug console that shows recent log messages.
- New 'Skip errors' option added to copy/move/unpack/download file operations,
  allowing file transfer errors to be ignored silently (ticket #231).
- Files can be selected or marked one block at a time (ticket #116).
  Default shortcuts are Control+UP/DOWN and Shift+Control+UP/DOWN respectively.
- New split and combine files functionality.

Improvements:
- Tar/TGZ and Zip unpacking speed has been improved significantly (ticket #74).
- Improved RAR compatibility.
- jCIFS library upgraded to version 1.3.12.
- SMB domain-based authentication is now supported.
- Added shortcuts to browse nearby SMB network shares.
- JNA library upgraded to version 3.2.2.
- Character encodings can be more easily selected (ticket #15).
- Added icon mappings for Office 2007 file extensions.
- 'Mark/Unmark files' is no longer case-sensitive by default.
- Mac OS X: muCommander can now be interacted with from the Finder (Open with)
  or by dragging file/folders to the Dock icon (ticket #153).
- New and improved application icon, contributed by Semyon Filippov.
- Added a 'New window' item to the system tray menu.
- Improved support for NRG images ; audio tracks can now be dumped as WAV files.
  Contributed by Xavier Martin.
- Improved the toolbar's behavior when the window is too small for all buttons
  to be displayed (ticket #165). Contributed by LeO.
- File table's right-click menu now has visibility toggles for all columns
  instead of just the current one, and one for Auto-Size columns (ticket #226).
- Leading and trailing toolbar separators are no longer displayed (ticket #166).
- 'Permanent delete' no longer asks whether symlinks should be followed, they
  are now silently deleted without being followed (easier and safer).
- Improvements made to the filename selection algorithm used in
  Copy/Move/Unpack/Rename destination fields (ticket #167).
- Added support for KDE 4 (ticket #257).
- Startup failures are now handled gracefully by displaying a proper message
  and error details.
- Local UNIX volumes are now resolved using /proc/mounts (dynamic) rather rather
  than /etc/fstab (static). Contributed by Kowy (ticket #227).
- Added the Java runtime version to the 'Version information' section of the
  'About' dialog (ticket #274).
- 'mka', 'mkv' and 'divx' files now have proper audio/video icons. Suggested
  by Tenzer (ticket #279).

Localization:
- Arabic (AR) translation, contributed by ChArLoK_16.

Bug fixes:
- Zip entries using '\' as a path separator are now tolerated and handled
  appropriately (ticket #98).
- Fixed image viewer not updating the zoom level in the window title
  (ticket #102). Contributed by Joshua Lebo.
- Fixed date of Zip entries incorrectly displayed under certain conditions
  (ticket #107). Contributed by Oliver Burgmaier.
- Fixed SMB connection issues (ticket #106).
- Fixed connection issues with FTP servers not supporting the SYST command ;
  those are now considered as Unix servers (ticket #121).
- Fixed credentials ignored when setting a remote startup folder (ticket #108).
- Fixed unconsistent handling of destination when unpacking an archive 
  (ticket #93).
- Fixed the display of homonymous volumes in the drive popup button (ticket #138).
- Swap partitions declared in /etc/fstab are now properly handled (ticket #139).
  Contributed by Johann Schmitz.
- Fixed issues with Look & Feel (ticket #145).
- Fixed slow downs affecting the drive popup button under Windows (ticket #122).
  Contributed by Alexander Yerenkow. 
- Fixed an exception occuring in the 'Run dialog' when pressing Enter
  repeatedly (ticket #212).
- Fixed an issue with the panel separator being set incorrectly after a resize
  of the main window (ticket #163).
- Improved the toolbar's behavior when the window is too small for all buttons
  to be displayed (ticket #165). Contributed by LeO.
- Fixed flashing command windows under Windows 95/98/Me (ticket #63).
- The authentication dialog's login field is now prefilled with the current
  user's name (ticket #185). Contributed by LeO.
- Fixed exceptions occurring while copying text to the clipboard from an external
  application under certain conditions (ticket #164).
- Added support for forward slash-separated paths on backslash-separated OSes
  (Windows, OS/2) (ticket #94).
- Double-click speed system preference is now honoured under GNOME and KDE
  (ticket #221). Contributed by LeO.
- Folder auto-refresh can be disabled in the preferences by setting the
  prefs.auto_refresh.check_period value to -1 (requested in ticket #233).
- Fixed viewer/editor menus not appearing since Java for Mac OS X 10.5 Update 4
  (ticket #243).
- Fixed malfunctioning encoding detection in text viewer/editor (ticket #244).
- Text viewer/editor no longer treats the Unicode BOM as an editable character
  (ticket #245).
- UTF-16 and UTF-32 text files are no longer recognized as binary files
  (ticket #235).
- Fixed erratic current file selection when 'Refresh' is called repeatedly
  (ticket #250).
- Text editor now preserves Unicode BOMs when saving files (ticket #251).
- Fixed file selection issues when middle clicking and dragging the mouse
  (ticket #176).
- Fixed an issue causing icons not to appear on certain files when using
  system icons (ticket #225). Fix contributed by LeO.
- Fixed UI freezes when invoking Copy/Move/Unpack dialogs on a remote
  filesystem (ticket #12).
- Fixed 'open natively' opening all files when invoked on a remote filesystem
  or on archive entries (ticket #147).
- Fixed auto-refresh not picking changes changes made to a file that was edited
  (ticket #258).
- Fixed 'Move to trash' failures under GNOME (ticket #97).
- Credentials could end up not being saved upon application exit (ticket #173).
- Fixed connection issues when using a password containing a slash character
  (ticket #137).
- Fixed a case where the application would freeze when browsing HTTP shares
  with the tree view enabled (ticket #266).
- Fixed quick search issues under Mac OS X Snow Leopard (ticket #309).   

Known issues:
- Some translations are not up-to-date. Refer to
  http://trac.mucommander.com/wiki/Translations for more information.
- Recycle Bin not working on Windows 64-bit (ticket #234).
- RAR and SFTP support are only available under Java 1.5 or higher.
- Executable permissions are not preserved properly for local files prior to
  Java 1.6.
- SMB support may not work properly on non multi-language JRE.
- 'Copy files to clipboard' not working with some applications (files are not
  pasted).
- Mac OS X: some keyboard shortcuts may conflict with global system shortcuts.
- Mac OS X: text fields don't work correctly when using a fractionally-scaled
  font (known Java bug,
   http://lists.apple.com/archives/Java-dev/2005/May/msg00670.html).
- Authentication issues when using several sets of credentials (login/password)
  for the same server (see ticket #76).
- Untrusted HTTPS connections are allowed without a warning.


Command Line Interface
----------------------

muCommander comes with a few command line switches.
The following options are available:
 -a FILE, --assoc FILE             Load associations from FILE.
 -b FILE, --bookmarks FILE         Load bookmarks from FILE.
 -c FILE, --configuration FILE     Load configuration from FILE
 -C FILE, --commandbar FILE        Load command bar from FILE
 -e FOLDER, --extensions FOLDER    Load extensions from FOLDER.
 -f FILE, --commands FILE          Load custom commands from FILE.
 -i, --ignore-warnings             Do not fail on warnings (default).
 -k FILE, --keymap FILE            Load keymap from FILE
 -n, --no-debug                    Disable debug output to stdout
 -d, --debug                       Enable debug output to stdout (default)
 -p FOLDER, --preferences FOLDER   Store configuration files in FOLDER
 --no-splash                       Disable splashscreen on startup
 --splash                          Enable splashscreen on startup (default)
 -S, --silent                      Do not print verbose error messages
 -s FILE, --shell-history FILE     Load shell history from FILE
 -t FILE, --toolbar FILE           Load toolbar from FILE
 -u FILE, --credentials FILE       Load credentials from FILE
 -h, --help                        Print the help text and exit
 -v, --version                     Print the version and exit
 -V, --verbose                     Print verbose error messages (default)
 -w, --fail-on-warnings            Quits when a warning is encountered during
                                   the boot process.

In addition to these, muCommander will interpret anything that comes after the
last switch as a URI and load it in its windows.
So for example:

 mucommander -b ~/.bookmarks.xml ftp://user@myftp.com ~/dev http://slashdot.org

Will:
 - read bookmarks from ~/bookmarks.xml
 - load a connection to myftp.com in the left panel of the main window
 - load ~/dev in the right panel of the main window
 - open a second window and load http://slashdot.org in its left panel
 - load the default directory in the second window's fourth panel


Advanced configuration
----------------------

muCommander has some advanced configuration options which cannot (yet) be
accessed through the GUI.
After having booted the application for the first time, the following files will
be created in muCommander's preferences folder:
 - action_keymap.xml (keyboard shortcuts description file).
 - command_bar.xml   (commandbar description file).
 - toolbar.xml       (toolbar description file).

Brave users can edit these XML files and tune muCommander to their own needs.
Here are a few hints on how to edit these files.


 - action_keymap.xml
All customisable actions are listed in that file, using the following format:
<action class="com.mucommander.ui.action.CopyFilesToClipboardAction"
        keystroke="control C" alt_keystroke="meta C"/>

It's probably safer not to mess around with the class argument, as this could
actually remove features from muCommander.
keystroke and alt_keystroke should be fairly safe explanatory. It's important to
note, however, that due to Java's capricious nature, the case is important.
CONTROL C will not be understood, and neither will control c.


 - command_bar.xml
This file describes the content of your command bar (the bit will all the
buttons on the lower part of the window).
Each item in the file corresponds to a button in the bar. You can edit them, add
some or remove some.

The syntax is as follows:
<button action="com.mucommander.ui.action.CopyAction"
        alt_action="com.mucommander.ui.action.LocalCopyAction"/>
Where:
 - action is the main action executed by the button
 - alt_action is the action executed by the button when the shift key is held
   down

For a list of legal actions, please refer to action_keymap.xml


 - toolbar.xml
This file controls the content of your toolbar. It works in the same way as
command_bar.xml, with two notable differences:
  - you can use a <separator/> element to add a separator in the toolbar
  - alt_action is not available


- commands.xml
This file controls the various system commands that muCommander can call.
Documentation on how to customise those commands can be found here:
http://www.mucommander.com/forums/viewtopic.php?t=420


- associations.xml
This file controls custom command / file associations.
Documentation on how to customise those associations can be found here:
http://www.mucommander.com/forums/viewtopic.php?t=634
