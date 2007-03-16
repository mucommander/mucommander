           _____                           _        
 _____ _ _|     |___ _____ _____ ___ ___ _| |___ ___
|     | | |   --| . |     |     | .'|   | . | -_|  _|
|_|_|_|___|_____|___|_|_|_|_|_|_|__,|_|_|___|___|_| 

-----------------------
muCommander v0.8 beta 3 
-----------------------

muCommander is a cross-platform file manager featuring a Norton Commander
style interface and running on any operating system with Java support
(Mac OS X, Windows, Linux, *BSD, Solaris...).

Please use the muCommander forums (http://www.mucommander.com/forums)
to post your questions, suggestions or bug reports.
Your feedback is important and always welcome!

Official website: http://www.mucommander.com
(c) 2002-2007 Maxence Bernard. All rights reserved.



Requirements
------------

Java Runtime Environment 1.4.0 (JRE) or later is required to run
muCommander. JRE 1.6 is recommended, you can download it at http://java.com.

Mac OS X users: your favorite OS already comes with a Java runtime so you're good to go!

If you're having problems launching muCommander, make sure the $JAVA_HOME environment
variable points to the directory where your Java runtime is installed.



Legal stuff
-----------

You must read and accept the license located in the 'license.txt' file
in this package prior to using muCommander.

muCommander uses the following great 3rd party works :

- the jCIFS library released under the LGPL license to provide SMB support.
 jCIFS can be found at http://jcifs.samba.org .

- the Jakarta Commons Net library released under the Apache license to provide FTP support. 
 Commons Net can be found at http://jakarta.apache.org .

- the J2SSH library released under the LGPL license to provide SFTP support.
 J2SSH can be found at http://www.sshtools.com .

- Java TAR released under the Public Domain to provide TAR support.
 Java TAR can be found at http://www.trustice.com/java/tar/ .

- JmDNS released under the LGPL license to provide Bonjour/Zeroconf support.
JmDNS can be found at http://jmdns.sourceforge.net .

- Toolbar, command bar and preferences icons by Mark James (http://famfamfam.com) under the Creative Commons Attribution License.



Credits
-------

Lead developers: 
- Maxence Bernard
- Nicolas Rinaudo

Code contributors:
- Xavier Martin (ISO and NRG archives support)

Translators: 
- Tony Klüver and Frank Berger (German)
- Marcos Cobeña (Spanish)
- Peter Vasko (Czech)
- Kent Hsu (Traditional Chinese)
- Andrzej Kosiński (Polish)
- Tamás Balogh-Walder (Hungarian)
- XXXX Pro (Russian)
- Woodie (Simplified Chinese)
- Joze Kovacic (Slovenian)
- Catalin Hritcu (Romanian)
- Roberto Angeletti (Italian)

Many thanks to all of you who suggested new features, reported bugs, sent warm emails or generously donated to the project !



Command Line Interface
----------------------

muCommander comes with a few command line switches.
The following options are available:
 -a FILE, --assoc FILE             Load associations from FILE.
 -b FILE, --bookmarks FILE         muCommander bookmarks will be read from and written to FILE.
 -c FILE, --configuration FILE     muCommander configuration will be read from and written to FILE.
 -C FILE, --commandbar FILE        muCommander command bar description will be read from and written to FILE.
 -f FILE, --commands FILE          Load custom commands from FILE.
 -k FILE, --keymap FILE            muCommander shortcuts will be read from and written to FILE.
 -p FOLDER, --preferences FOLDER   Store configuration files in FOLDER.
 -s FILE, --shell-history FILE     muCommander shell history will be read from and written to FILE.
 -t FILE, --toolbar FILE           muCommander toolbar configuration will be read from and written to FILE.
 -h, --help                        Print the help text and exit
 -v, --version                     Print the version and exit

In addition to these, muCommander will interpret anything that comes after the last switch as a URI and load it
in its windows. So for example:

 mucommander -b ~/.bookmarks.xml ftp://user@myftp.com ~/dev http://slashdot.org

Will:
 - read bookmarks from ~/bookmarks.xml
 - load a connection to myftp.com in the left panel of the main window
 - load ~/dev in the right panel of the main window
 - open a second window and load http://slashdot.org in its left panel
 - load the default directory in the second window's fourth panel



Advanced configuration
----------------------

muCommander has some advanced configuration options which cannot (yet) be accessed through the GUI.
After having booted the application for the first time, the following files will be created in muCommander's preferences
folder:
 - action_keymap.xml (keyboard shortcuts description file).
 - command_bar.xml   (commandbar description file).
 - toolbar.xml       (toolbar description file).

Brave users can edit these XML files and tune muCommander to their own needs. Here are a few hints on how to edit these files.


 - action_keymap.xml
All customisable actions are listed in that file, using the following format:
<action class="com.mucommander.ui.action.CopyFileNamesAction" keystroke="alt C" alt_keystroke="meta C"/>

It's probably safer not to mess around with the class argument, as this could actually remove features from muCommander.
keystroke and alt_keystroke should be fairly safe explanatory. It's important to note, however, that due to Java's capricious
nature, the case is important. CONTROL C will not be understood, and neither will control c.


 - command_bar.xml
This file describes the content of your command bar (the bit will all the buttons on the lower part of the window).
Each item in the file corresponds to a button in the bar. You can edit them, add some or remove some.

The syntax is as follows:
<button action="com.mucommander.ui.action.CopyAction" alt_action="com.mucommander.ui.action.LocalCopyAction"/>
Where:
 - action is the main action executed by the button
 - alt_action is the action executed by the button when the shift key is held down

For a list of legal actions, please refer to action_keymap.xml


 - toolbar.xml
This file controls the content of your toolbar. It works in the same way as command_bar.xml, with two notable differences:
  - you can use a <separator/> element to add a separator in the toolbar
  - alt_action is not available


- commands.xml
This file controls the various system commands that muCommander can call.
Documentation on how to customize those commands can be found here: http://www.mucommander.com/forums/viewtopic.php?t=420


- associations.xml
This file controls muCommander's custom file associations. They will be initialised according to the system muCommander
is running on and depend on commands.xml.

This file is not (yet) written by muCommander, so you have to create it manually for the time being.

It looks something like this:
<?xml version="1.0" encoding="UTF-8"?>
<associations>
    <association mask=".*" command="open"/>
</associations>

Each association has the following attributes:
- mask:    regular expression that the file name must match in order to be managed by the association.
- read:    if set to 'yes', a file must be readable in order to be matched by the association.
- write:   if set to 'yes', a file must be writable in order to be matched by the association.
- execute: if set to 'yes', a file must be executable in order to be matched by the association.
- command: the alias of the command to execute for this type of files.

You must be extremely careful when editing this file, as it's quite easy to break muCommander by fiddling with
it. Make sure that the system's default associations are always present, and that you do not change their attributes.
- Windows 9x:
<association mask=".*" command="open"/>

- Windows NT:
<association mask=".*\\.[eE][xX][eE]" command="openEXE"/>
<association mask=".*" command="open"/>

- Mac OS X:
<association mask=".*" command="open"/>

- Gnome:
<association mask="[^.]+" command="execute"/> <!-- Only if running on Java prior to 1.6 -->
<association mask=".*"    command="execute" execute="yes"/>
<association mask=".*"    command="open"/>

- KDE:
<association mask="^https?:\\/\\/.+" command="openURL"/>
<association mask=".*"               command="open"/>

- Anything else:
<association mask="[^.]+" command="execute"/> <!-- Only if running on Java prior to 1.6 -->
<association mask=".*"    command="execute"/>



What's new since v0.8 beta 2 ?
------------------------------

New features:
 - Keyboard shortcuts, toolbar and command bar buttons can now be customized (no GUI yet, see .xml files in preferences folder)
 - New theme engine with predefined themes allowing more customization options (GUI not updated yet)
 - Custom file opener and file associations (no GUI yet)
 - Drag and Drop support: files can be copied or moved to and from muCommander windows and other applications, current folder can be changed by dropping a file or folder on the location field
 - Clipboard support: files can be copied and pasted to/from the clipboard (Ctrl+C / Ctrl+V by default), works with other applications
 - New quick search with visual feedback, editable search string, jump to next/previous matches and ability to cancel it
 - Connection pooling (SFTP,FTP) and automatic disconnection after timeout, active connections can be listed and closed (Shift+Ctrl+K by default)
 - New file transfer progress window with 'Pause'/'Resume', 'Skip' current file and speed limit options, current/total remaining time, elapsed time, current speed and speed graph, and option to leave the window open when finished
 - Zip, Tar, Gzip, Bzip2 archives can now be created
 - Read support for ISO and NRG images over local and SMB filesystems (contributed by Xavier Martin)
 - Read/write support for Bzip2 archives (.bz2, .tbz2, .tar.bz2 extensions)
 - Read support for AR and Debian archives, GNU and BSD variants supported (.ar, .deb extensions)
 - Read support for LST catalog files
 - Support for HTTP Basic Authentication
 - Bonjour/Zeroconf services discovery
 - New permissions column in table, sort by permissions option
 - File permissions are properly preserved during transfers (Java 1.6 required for local files)
 - New 'Change permissions' and 'Change date' dialogs
 - 'Run command' is now interactive, allows shell customization, quick recall of previous commands, color customization, uses the customisable shell to parse commands.
 - Command line options
 - Ability to execute remote or archive entries files: these are copied to a temporary local directory and executed
 - Location combo box now allows to quickly recall parent folders
 - Icons magnification option for toolbar, command bar and files
 - New credentials management: authentication dialog allows to choose between credentials matching a location, persistent credentials can be edited using the 'Edit credentials' dialog, passwords are (weakly) encrypted when stored to disk
 - New and improved 'Edit bookmarks' dialog
 - New 'Go' menu
 - New 'Copy name(s)' and 'Copy path(s)' actions to copy marked files names / paths (Alt+C / Shift+Alt+C by default)
 - New 'Mkfile' action (Shift+F7 by default)
 - New 'Mark page up/page down' action (Shift+Page Up/Shift+Page Down by default)
 - New 'Mark up to first/last row' action (Shift+Home/Shift+End by default)
 - New 'Split horizontally', 'Split vertically', 'Split equally' actions in 'Window' menu
 - Added show/hide columns to 'View' menu
 - 'Reveal in desktop' now available for Windows, KDE, GNOME (on top of Mac OS X)
 - Ability to open files with native file associations under KDE and GNOME
 - Ability to open URLs under KDE and GNOME
 - 'Show .DS_Store files' option under Mac OS X
 - 'Show system folders' option under Mac OS X
 - 'Change current location' keyboard shortcut (Ctrl+G by default)
 - 'Reveal in desktop' keyboard shortcut (Ctrl+L by default)
 - New file icons for Mac OS X and Windows executables, libraries, Java, Powerpoint and Visual studio files
 - New toolbar icon for 'Reveal in desktop'
 - New 'Report a bug' item in 'Help' menu
 - Windows: new mucommander.exe launcher replacing both mucommander.bat and mucommander.jar

Improvements:
 - Major code cleanup before source code release
 - Improved file handling performance, especially under Windows
 - Optimized handling of Zip and Tar archives which can now be accessed much faster
 - Faster table rendering
 - Toolbar and command bar now contextually show whether the action can be triggered or not
 - Free/total volume space now automatically refreshes (every 60 seconds)
 - Status bar shows a graphical representation of the amount of free space on the current volume (where available) 
 - 'File collision' dialog improved, attempts to transfer a folder to one of its subfolders are now detected
 - KB/MB/GB/TB are now calculated accurately by multiples of 1024 instead of 1000
 - Decimal and thousands separators are now localized
 - Environment variables are now retained when using 'Run command'
 - In-table file renaming now used everywhere instead of 'Rename' dialog
 - In-table file renaming must now to be validated by ENTER for safety reasons
 - 'Quit' action with confirmation dialog added for platforms other than OS X (Ctrl+Q by default)
 - Right-click menu reworked, added 'Copy name(s)' / 'Copy path(s)' actions
 - Improved Windows launcher
 - Splash screen shows loading progress
 - Currently selected file is displayed in a different color (configurable) when focus is out of the file table or application
 - 'Go back' and 'Go forward' toolbar buttons display a list of back/forward folders when the mouse button is held down
 - Left/Right keyboard shortcuts added for 'Go to parent' and 'Open' (customizable)
 - Window number displayed in title if more than one window is opened
 - Marking/unmarking stops when mark key is kept pressed and last file is reached
 - Mark key on '..' now selects next file instead of doing nothing
 - Shortcuts dialog uses tabs to show shortcut topics
 - Bookmark and root folder names can be entered in the location field to change the current folder
 - Location field now allows to correct paths that have failed or have been cancelled
 - 'Local copy' dialog now selects filename without extension just like 'Rename'
 - Added 'Open', 'Open natively' and 'Reveal in Desktop' actions to 'File' menu
 - Added 'Copy name(s)' / 'Copy path(s)' actions to 'Mark' menu
 - Added 'Recall previous/next window' actions to 'Window' menu
 - Folder pane divider can now be double-clicked to restore an equal split
 - Preference tabs now have icons, layout improved a bit
 - View and Editor windows now have icons
 - New font preview field to 'Appearance' preferences tab
 - New ascending / descending icons in sort tabs
 - New about screen
 - New extensions associated with Zip archives: .war, .wal, .wmz, .xpi, .ear, .sar, .odt, .ods, .odp, .odf, .odg
 - SMB performance improved when changing folders
 - Root folders of SMB shares now have a parent (smb://) to more easily browse workgroups/shares
 - SMB resources that are not browsable (printers, IPC...) are not displayed
 - SMB library (jCIFS) upgraded to version 1.2.13 which includes many bug fixes
 - Mac OS X: disabled progress bar animation in file transfer progress dialog which hogged CPU unnecessarily
 - Mac OS X: new 'Universal Binary' application stub, muCommander no longer uses Rosetta to start up
 - New muCommander icon (thanks to Stefano Perelli) and splash logo
 - Shell management: users can now choose the encoding used by their shell of choice.

Localization:
 - Romanian translation (contributed by Catalin Hritcu)
 - Italian translation (contributed by Roberto Angeletti)

Bug fixes:
 - Fixed SFTP connections which could not be established to some servers not supporting the 'password' authentication method
 - Windows UNC network paths (those starting with \\) are now properly handled, fixing startup problems under Windows if the user home is located on a network share
 - Paths to archive entries can now be properly resolved when entered in the location field
 - Paths to SMB folders entered in the location field do not need to end with '/' anymore
 - Improved focus management, fixing some previous focus issues
 - Fixed drive popup which doesn't immediately reflect bookmark changes
 - Fixed .GZ files not opened when extension is upper-cased
 - GZ files inside Tar archives can now be properly opened
 - Archive entries properly refreshed when an archive is being browsed and the archive file has changed
 - Fixed archive file staying locked when creation was interrupted because of an error
 - Fixed 'Sort by date'/'Sort by size' not properly sorting files that have the same date or size, these are now sorted by name
 - Preferences XML file now properly indented
 - Removed Preferences from 'File' menu under Mac OS X (already present in the Apple menu) 
 - Fixed same directory which could be added several times in a row to history
 - Fixed HTTP errors and redirections not handled properly when browsing a website
 - Fixed improper error message when trying to create a file in an HTTP folder
 - Fixed drive button shrinking when browsing a folder with a long path
 - Email addresses can now be used as passwords
 - Fixed FTP overwrite problem which would cause the destination file to be renamed to filename.1
 - Fixed a bug in menu bar which could prevent some shortcuts from working properly
 - Added missing keyboard shortcut to 'Go to parent' menu item
 - Fixed a display bug which could lead to both panes showing a selected file
 - Fixed annoyance with drive popup under Mac OS X which redirected startup partition to /Volumes/PartitionName instead of /
 - Fixed muCommander cancelling Mac OS X shutdown / restart / logout
 - Fixed file editor not asking for save when quitting muCommander under OS X
 - Files/folders with extended characters can now properly be executed/revealed under Mac OS X (was caused by a bug in the Java 1.5 runtime http://lists.apple.com/archives/java-dev/2004/Apr/msg00798.html)
 - Fixed folders failing to display when a filename contains a backslash under a forward slash-separated system (since 0.8 beta1)
 - Fixed volume space indicator not showing anything in certain local folders (since 0.8 beta1)
 - Fixed renamed file loses selection after renaming (since 0.8 beta1)
 - Fixed marked files not properly retained when changing sort order (since 0.8 beta1)
 - Fixed top-level directories which could not be created in sub-directories (since 0.8 beta1)
 - Fixed handling of files containing leading or trailing white space characters (since 0.8 beta1)
 - Fixed issue with filenames containing a '?' character (since 0.8 beta1)
 - Fixed 'Edit bookmarks' changes not taken into account under certain conditions (since beta1)
 - Fixed in-table renaming which could stop working under certain conditions (since 0.8 beta1)
 - Fixed total/free space not displayed for some locally mounted volumes  (since 0.8 beta1)
 - Fixed newly created folders not selected (since 0.8 beta2)
 - Fixed user-defined font not properly taken into account (since 0.8 beta2)
 - Fixed a repaint bug which could cause the folder pane display to be corrupted (since 0.8 beta2)

Known issues:
 - Some translations are not up-to-date
 - Executable permissions are not preserved properly for local files prior with Java <1.6
 - Zip files encoded in a charset other than UTF-8 will not display non-ASCII filenames properly due to a limitation of java.util.zip (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4244499)
 - Some BZ2 archives can not be properly opened
 - SMB support may not work properly on non multi-language JRE
 - 'Copy files to clipboard' not working with some applications (files are not pasted)
 - Mac OS X : 'Sort by' keyboard shortcuts in menu show Command modifier instead of Ctrl, OS X's keyboard navigation shortcuts must be disabled in 'System Preferences' for them to work properly
 - Mac OS X : text fields don't work correctly when using a fractionally-scaled font (known Java bug, http://lists.apple.com/archives/Java-dev/2005/May/msg00670.html)
 - Editable combo boxes have keyboard navigation issues under Java 1.6
