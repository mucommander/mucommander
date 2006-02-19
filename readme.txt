-----------------------
muCommander v0.8 beta 2
-----------------------

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

muCommander uses the great following 3rd party works :

- the jCIFS library released under the LGPL license to provide SMB support.
 jCIFS can be found at http://jcifs.samba.org .

- the Jakarta Commons Net library released under the Apache license to provide FTP support. 
 Commons Net can be found at http://jakarta.apache.org .

- the J2SSH library released under the LGPL license to provide SFTP support.
 J2SSH can be found at http://www.sshtools.com

- Java TAR released under the Public Domain to provide TAR support.
 Java TAR can be found at http://www.trustice.com/java/tar/ .

- Icons by Mark James under the Creative Commons Attribution License (http://famfamfam.com)


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

