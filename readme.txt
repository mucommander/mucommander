----------------
muCommander v0.7
----------------

muCommander is a cross-platform file manager featuring a Norton Commander
style interface and running on any operating system with Java support
(Mac OS X, Windows, Solaris, Linux, *BSD...).

Please use the muCommander forums (http://www.mucommander.com/forums)
to post your questions, suggestions or bug reports. Your feedback is
important and always welcome!

Official website: http://www.mucommander.com
(c) 2004 Maxence Bernard. All rights reserved.


Legal stuff
-----------

You must read and accept the licenses contained in this package prior
to using muCommander.

muCommander uses the jCIFS library released under the LGPL license
to provide SMB support. jCIFS can be found at http://jcifs.samba.org.

muCommander uses the CommonsNet library released under the Apache license
to provide FTP support. CommonsNet can be found at http://jakarta.apache.org.


Requirements
------------

Java Runtime Environment 1.3.0 (JRE) or later is required to run
muCommander. JRE 1.4 is recommended, you can download it at http://java.com.

Mac OS X users: Your favorite OS already comes with a Java runtime so you're good to go!

If you're having problems launching muCommander, make sure the
$JAVA_HOME environment variable points to the directory where your
JRE or JDK is installed.


What's new in v0.7 ?
--------------------

New features:
 - Localized interface, available in English and French
 - FTP support
 - Browse websites as folders to easily locate and download files
 - HTTP/HTTPS file download with resuming
 - Status bar which displays number and combined size of selected files 
 - Re-authentication mechanism when trying to access a folder without sufficient privileges
 - GIF/JPEG/PNG image viewer
 - Choice of a default action for copy/move/delete operations when a file exist in destination 

Improvements:
 - Overall application performance and stability improved
 - Better windows focus management
 - Increased SMB performance 
 - Window location and dimension automatically saved after quit
 - jCIFS library updated to version 1.1.0
 - Added shortcuts to user home, network connections, /etc/fstab (unix) and /Volumes (Mac OS X) in drive button 
 - All dialogs can now be closed with escape key
 - Improved progress bar, takes into account current file's progress in total
 - New 'Go to parent' toolbar button and menu item
 - Layout of main window reworked 
 - Improved properties window (faster and now displays progress)
 - 'Hide command bar' option
 - Removed 'Show toolbar on startup option', toolbar status is now automatically saved (same goes for command and status bars)
 - More accurate error messages and 'retry' option when copying or moving files fails 
 - Real 'resume' file option
 - Dialogs are now properly centered when they show up
 - Tooltips for command bar buttons
 - Command bar buttons now blink when triggered with F3-F10
 - Main window is maximized on first launch (works betten under Java 1.4 than 1.3) 
 - User now regains control faster after interrupting a file operation
 - Sender information now appears in 'email files' dialog
 - When renaming a file, filename without its extension is selected
 - Toolbar icons reworked
 - Application code has undergone a major rewrite

Bug fixes :
 - Fixed a deadlock which sometimes occurred while refreshing a folder
 - Application no longer hangs while waiting for an unavailable SMB folder
 - Symbolic links to ZIP or JAR files are now handled correctly
 - Fixed a bug when moving a folder to a folder containing a folder with the same name
 - Fixed minor focus problem with drive button (focus would not be released)
 - Fixed a minor bug in location field which would not always reflect current folder's path
 - Fixed a bug in ZIP/JAR file handling which caused some archived folders not to be displayed properly

Mac OS X:
 - Screen menu bar enabled
 - Brushed metal look and feel (requires Java 1.4.2 update 1)
 - Standard 'Preferences', 'About' and 'Quit' menu items
 - Quit confirmation dialog
 - Command+W closes windows

Known issues:
- Image viewer doesn't free memory properly under Mac OS X
- Folders cannot be renamed to upper/lower case variations of the same name
- Pipe '|' and other shell operators cannot be used in 'Run Command'

