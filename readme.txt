           _____                           _        
 _____ _ _|     |___ _____ _____ ___ ___ _| |___ ___
|     | | |   --| . |     |     | .'|   | . | -_|  _|
|_|_|_|___|_____|___|_|_|_|_|_|_|__,|_|_|___|___|_| 

---------------------------------------
muCommander v0.8 beta 3 (nightly build)
---------------------------------------

muCommander is a cross-platform file manager featuring a Norton Commander
style interface and running on any operating system with Java support
(Mac OS X, Windows, Linux, *BSD, Solaris, OS/2...).

Please use the muCommander forums (http://www.mucommander.com/forums)
to post your questions, suggestions or bug reports.
Your feedback is important and always welcome!

Official website: http://www.mucommander.com
(c) 2002-2006 Maxence Bernard. All rights reserved.


Requirements
------------

Java Runtime Environment 1.3.0 (JRE) or later is required to run
muCommander. JRE 1.5 is recommended, you can download it at http://java.com.

Mac OS X users: your favorite OS already comes with a Java runtime so you're good to go!

If you're having problems launching muCommander, make sure the $JAVA_HOME environment
variable points to the directory where your JRE or JDK is installed.


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
 J2SSH can be found at http://www.sshtools.com

- Java TAR released under the Public Domain to provide TAR support.
 Java TAR can be found at http://www.trustice.com/java/tar/ .

- Toolbar, command bar and preferences icons by Mark James (http://famfamfam.com) under the Creative Commons Attribution License


Credits
-------

Countless thanks to the many people who participated in this project and made it what it is today:

Code contributors:
- Nicolas Rinaudo  (shell improvements, configuration parser, XML writer, Ant build file and much more)
- Xavier Martin (ISO and NRG support)

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

And all of you who suggested new features, reported bugs, sent warm emails or generously donated to the project !


Command Line Interface
----------------------

v0.8 beta 3 (nightly build) comes with a few command line switches.
The following options are available:
 -b FILE, --bookmarks FILE       muCommander bookmarks will be read from and written to FILE.
 -c FILE, --configuration FILE   muCommander configuration will be read from and written to FILE.
 -C FILE, --commandbar FILE      muCommander command bar description will be read from and written to FILE.
 -k FILE, --keymap FILE          muCommander shortcuts will be read from and written to FILE.
 -s FILE, --shell-history FILE   muCommander shell history will be read from and written to FILE.
 -t FILE, --toolbar FILE         muCommander toolbar configuration will be read from and written to FILE.
 -h, --help                      Print the help text and exit
 -v, --version                   Print the version and exit

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

v0.8 beta 3 (nightly build) has some advanced configuration options which cannot (yet) be accessed through the GUI.
After having booted the application for the first time, the following files will be created in muCommander's preference
folder:
 - action_keymap.xml (keyboard shortcuts description file).
 - command_bar.xml   (commandbar description file).
 - toolbar.xml       (toolbar description file).

Brave users can edit these XML files and tune muCommander to their own needs. Here are a few hints on how to edit these files.

 - action_keymap.xml
All customisable actions are listed in that file, using the following format:
<action class="com.mucommander.ui.action.CopyFileNamesAction" keystroke="control C" alt_keystroke="meta C"/>

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
  - alt_actions are not available


What's new since v0.8 beta 2 ?
------------------------------

New features:
 - Keyboard shortcuts, toolbar and command bar buttons can now be customized (no GUI yet, see .xml files in preferences folder)
 - Drag and Drop support: files can be copied or moved to and from muCommander windows and other applications, current folder can be changed by dropping a file or folder on the location field
 - Zip, Tar, Gzip, Bzip2 archives can now be created
 - Support for ISO and NRG images over local and SMB filesystems (contributed by Xavier Martin)
 - Support for Bzip2 archives (.bz2, .tbz2, .tar.bz2 extensions)
 - Support for AR and Debian archives, GNU and BSD variants supported (.ar, .deb extensions)
 - New quick search with visual feedback, editable search string, jump to next/previous matches and ability to cancel it
 - 'Run command' allows shell customization, quick recall of previous commands and uses the system's default shell to parse commands under UNIX platforms (contributed by Nicolas Rinaudo)
 - Command line options (contributed by Nicolas Rinaudo)
 - Ability to execute non local files
 - Location combo box now allows to quickly recall parent folders
 - Icons magnification option for toolbar, command bar and files
 - New 'Copy name(s)' and 'Copy path(s)' actions to copy marked files names / paths (Ctrl+C / Shift+Ctrl+C by default) 
 - 'Reveal in desktop' for Windows, KDE, GNOME (on top of Mac OS X)
 - Ability to open files with native file associations under KDE and GNOME
 - Ability to open URLs under KDE and GNOME
 - 'Show/Hide .DS_Store' option under Mac OS X
 - New 'change current location' keyboard shortcut (Ctrl+G by default)
 - New 'reveal in desktop' keyboard shortcut (Ctrl+L by default)
 - New file type icons for Mac OS X and Windows executables, libraries, Java, Powerpoint and Visual studio files
 - New toolbar icon for 'Reveal in desktop'
 - Files, folders and paths from an external application can be 'dropped' (as in Drag and Drop) to the folder panes to change the current folder

Improvements:
 - Major code cleanup before source code release
 - Optimized table rendering for better display performance
 - Optimized file handling for better file access performance
 - Optimized Zip and Tar archive handling files for much quicker access
 - SMB performance improved when changing folders
 - Roots of SMB hosts now have a parent (smb://) to more easily browse workgroups/shares 
 - Free/total volume space now automatically refreshes (every 60 seconds)
 - Toolbar and command bar now contextually show whether the action can be triggered or not
 - KB/MB/GB/TB are now calculated accurately by multiples of 1024 instead of 1000
 - In-table file renaming now used everywhere instead of 'Rename' dialog
 - In-table file renaming must now to be validated by ENTER for safety reasons
 - 'Quit' action with confirmation dialog added for platforms other than OS X (Ctrl+Q by default)
 - Right-click menu reworked with 'Copy name(s)' / 'Copy path(s)' actions
 - Improved Windows launcher
 - Splash screen shows loading progress
 - Left/Right keyboard shortcuts added for 'Go to parent' and 'Open' (customizable)
 - Window number displayed in title if more than one window is opened
 - Marking/unmarking stops when mark key is kept pressed and last file is reached
 - Mark key on '..' now selects next file instead of doing nothing
 - Shortcuts dialog uses tabs to show shortcut topics
 - Bookmark and root folder names can be entered in the location field to change the current folder
 - 'Local copy' dialog now selects filename without extension just like 'Rename'
 - Added 'Open', 'Open natively' and 'Reveal in Desktop' items to 'File' menu
 - Added 'Copy name(s)' / 'Copy path(s)' items to 'Mark' menu
 - Preference tabs now have icons, layout improved a bit
 - New font preview field to 'Appearance' preferences tab
 - New ascending / descending icons in sort tabs
 - SMB library (jCIFS) upgraded to version 1.2.8
 - Mac OS X: new 'Universal Binary' application stub, muCommander no longer uses Rosetta to start up

Bug fixes:
 - Fixed drive popup which doesn't immediately reflect bookmark changes
 - Fixed .GZ files not opened when extension is upper-cased
 - GZ files inside Tar archives can now be properly opened
 - Fixed 'Sort by date'/'Sort by size' not properly sorting files that have the same date or size, these are now sorted by name
 - Preferences XML file now properly indented
 - Removed Preferences from 'File' menu under Mac OS X 
 - Fixed same directory which could be added several times in a row to history
 - Fixed HTTP errors and redirections not handled properly when browsing a website
 - Fixed drive button shrinking when browsing a folder with a long path
 - Fixed FTP overwrite problem which would cause the destination file to be renamed to filename.1
 - Fixed a bug in menu bar which could prevent some shortcuts from working properly
 - Added missing keyboard shortcut to 'Go to parent' menu item
 - Fixed annoyance with drive popup under Mac OS X which redirected startup partition to /Volumes/PartitionName instead of /
 - Fixed muCommander cancelling Mac OS X shutdown / restart / logout
 - Fixed file editor not asking for save when quitting muCommander under OS X
 - Files/folders with extended characters can now properly be executed/revealed under Mac OS X (was caused by a bug in the Java 1.5 runtime http://lists.apple.com/archives/java-dev/2004/Apr/msg00798.html)
 - Fixed marked files not properly kept when changing sort order (since 0.8 beta1)
 - Fixed top-level directories which could not be created in sub-directories (since 0.8 beta1)
 - Fixed handling of files containing leading or trailing white space characters (since 0.8 beta1)
 - Fixed issue with filenames containing a '?' character (since 0.8 beta1)
 - Fixed 'Edit bookmarks' changes not taken into account under certain conditions (since beta1)
 - Fixed in-table renaming which could stop working under certain conditions (since 0.8 beta1)
 - Fixed repaint bug which could cause the folder pane display to be corrupted (since 0.8 beta2)
 - Fixed user-defined font not properly taken into account (since 0.8 beta2)

Known issues:
 - Translations not up-to-date (some of the new 0.8 features are untranslated) 
 - Write/execute modes are not preserved when a file is copied, this will be solved with Java 1.6
 - SFTP folders limited to 98 files
 - SFTP unavailable under Java 1.3
 - Mac OS X : 'Sort by' keyboard shortcuts in menu show Command modifier instead of Ctrl, OS X's keyboard navigation shortcuts must be disabled in 'System Preferences' for them to work properly
 - Mac OS X : text fields don't work correctly when using a fractionally-scaled font (Known Java bug, http://lists.apple.com/archives/Java-dev/2005/May/msg00670.html)
