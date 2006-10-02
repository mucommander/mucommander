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
to post your questions, suggestions or bug reports. Your feedback is
important and always welcome!

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
- Nicolas Rinaudo  (shell improvements, configuration parser, XML writer, Ant build file)
- Xavier Martin (ISO and NRG support)
- Ewan Harrow (Webstart JNLP)

Translators: 
- Tony Klüver and Frank Berger (German translation)
- Marcos Cobeña (Spanish translation)
- Peter Vasko (Czech translation)
- Kent Hsu (Traditional Chinese translation)
- Andrzej Kosiński (Polish translation)
- Tamás Balogh-Walder (Hungarian translation)
- XXXX Pro (Russian translation)
- Woodie (Simplified Chinese translation)
- Joze Kovacic (Slovenian translation)

And all of you who suggested new features, reported bugs, sent warm emails or donated to the project !

What's new in v0.8 beta 3 (nightly build) ?
-------------------------------------------

New features:
- Keyboard shortcuts, toolbar and command bar buttons can now be customized (no GUI yet, see .xml files in preferences folder)
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

// Bug fixes //
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
- Fixed muCommander cancelling OS X shutdown/restart/logoff
- Fixed file editor not asking for save when quitting muCommander under OS X
- Files/folders with extended characters can now properly be executed/revealed under Mac OS X (was caused by a bug in the Java 1.5 runtime http://lists.apple.com/archives/java-dev/2004/Apr/msg00798.html)
- Fixed marked files not properly kept when changing sort order (since 0.8 beta1)
- Fixed top-level directories which could not be created in sub-directories (since 0.8 beta1)
- Fixed handling of files containing leading or trailing white space characters (since 0.8 beta1)
- Fixed issue with filenames containing a '?' character (since 0.8 beta1)
- Fixed 'Edit bookmarks' changes not taken into account under certain conditions (since beta1)
- Fixed in-table renaming which could stop working under certain conditions (since 0.8 beta1)
- Fixed repaint bug which could cause the folder display to corrupted (since 0.8 beta2)
- Fixed user-defined font not properly taken into account (since 0.8 beta2)


What's new in v0.8 beta 2 ?
---------------------------

New features:
 - New icon set for toolbar and command bar
 - New column that shows file type icons and allows to sort by extension
 - New toolbar shortcuts: 'Add bookmark', 'Edit bookmarks', 'Stop'
 - Drives system icons now displayed in the drive selection menu under Windows
 - Simplified Chinese translation by Woodie
 - Slovenian translation by Joze Kovacic

Improvements:
 - Major performance improvements
 - Improved behavior of right-click folder popup menus over beta1
 - Choice buttons for 'Delete', 'Quit' and 'New version available' can now be selected with arrow keys
 - Command bar and status bar now have a right-click popup menu to hide them
 - Bookmarks are now located before server shortcuts in drive selection menu
 - Added a checkbox in new version dialog to disable check on startup
 - Moved 'Check for new versions on startup' option to the 'Misc' preferences panel
 - Commons Net FTP library upgraded to version 1.4.1

Bug fixes:
 - Restored compatibility for Java 1.3 broken in beta1 (Note: SFTP not available under Java 1.3)
 - Fixed 'Drive not ready' Windows problem when last drive before app shutdown was a CD/floppy drive
 - Fixed character encoding issues in bookmarks and preferences files
 - Fixed 'Reveal in Finder' not working for regular files in beta1
 - Fixed some unwanted blinkings when changing current folder in beta1
 - Fixed unhandled error in beta1 when an empty folder was entered in location field
 - Fixed status bar not being refreshed under some rare circumstances

Known issues:
 - Incomplete Czech, German and Hungarian translations (new 0.8 features untranslated) 
 - SFTP unavailble under Java 1.3
 - Mac OS X-specific files not copied properly
 - Image viewer doesn't free up memory properly under Mac OS X
 - Slow TAR and Zip extraction for small files because extraction is not performed sequentially
 - Pipe '|' and other shell operators cannot be used in 'Run Command'


What's new in v0.8 beta 1 ?
---------------------------

New features:
 - SFTP support added
 - Bookmarks: can be added/edited to remember locations
 - Columns' width auto-adjust to maximize filename column's size
 - Changing a folder no longer locks the application and can be cancelled by pressing 'Escape'
 - Status bar shows free and total volume space (on certain filesystems/platforms only)
 - Right-click contextual menu on files and folders
 - Location field shows progress when connecting to a folder
 - Files can now renamed directly in the table by clicking on the filename 
 - Traditional Chinese translation by Kent Hsu
 - Polish translation by Andrzej Kosiński
 - Hungarian translation by Tamás Balogh-Walder
 - Russian translation by XXXX Pro

Improvements:
 - New file table spacing
 - 'Size' and 'Date' columns are now right-justified for clarity
 - Location field shows progress when changing current folder
 - Dates are preserved when copying/moving a file (except when writing to an SMB or FTP folder)
 - Date separator can now be customized in preferences
 - Marked color is also used for selected and marked files (selected color only was formerly used)
 - Improved hidden files detection for some filesystems: files starting with '.' are considered as hidden
 - 'Connect to server' dialog now uses tabs instead of a combo box
 - Improved location field behavior : no longer loses entered path when focus is lost
 - Custom date format is now used in 'File already exists' dialog
 - jCIFS library (SMB) upgraded to version 1.2.7
 - Commons Net library (FTP) upgraded to version 1.4.0

Bug fixes :
 - Fixed 'Enter' key not working properly in drive selection menu under Windows (http://mucommander.com/forums/viewtopic.php?p=172)
 - Fixed 'Refresh' button keeping focus after being clicked
 - Fixed file renaming not working properly with archive files over FTP and SMB
 - Archive files are now resumed correctly when downloaded
 - Fixed wrong sort order icon for reordered columns
 - FTP: improved FTP reconnection
 - FTP: fixed duplicate '.' and '..' files which happened with some FTP servers
 - Email files: fixed folders not being handled properly since v0.7

