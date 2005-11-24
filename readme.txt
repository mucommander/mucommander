-----------------------
muCommander v0.8 beta 1
-----------------------

muCommander is a cross-platform file manager featuring a Norton Commander
style interface and running on any operating system with Java support
(Mac OS X, Windows, Solaris, Linux, *BSD...).

Please use the muCommander forums (http://www.mucommander.com/forums)
to post your questions, suggestions or bug reports. Your feedback is
important and always welcome!

Official website: http://www.mucommander.com
(c) 2002-2005 Maxence Bernard. All rights reserved.


Legal stuff
-----------

You must read and accept the licenses contained in this package prior
to using muCommander.

muCommander uses the following libraries:

- the jCIFS library released under the LGPL license to provide SMB support.
 jCIFS can be found at http://jcifs.samba.org .

- the Jakarta Commons Net library released under the Apache license to provide FTP support. 
 Commons Net can be found at http://jakarta.apache.org .

- the J2SSH library released under the LGPL license to provide SFTP support.
 J2SSH can be found at http://www.sshtools.com

- Java TAR released under the Public Domain to provide TAR support.
 Java TAR can be found at http://www.trustice.com/java/tar/ .


Requirements
------------

Java Runtime Environment 1.3.0 (JRE) or later is required to run
muCommander. JRE 1.4 or 1.5 recommended, you can download it at http://java.com.

Mac OS X users: Your favorite OS already comes with a Java runtime so you're good to go!

If you're having problems launching muCommander, make sure the
$JAVA_HOME environment variable points to the directory where your
JRE or JDK is installed.


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

Improvements:
 - New file table spacing
 - 'Size' and 'Date' columns are now right-justified for clarity
 - Location field shows progress when changing current folder
 - Dates are preserved when copying/moving a file (doesn't work when writing to an SMB or FTP folder)
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

Known issues:
 - Slow TAR and Zip extraction for small files because extraction is not performed sequentially (yet)
 - Mac OS X-specific files not transferred properly
 - Image viewer doesn't free memory properly under Mac OS X
 - Pipe '|' and other shell operators cannot be used in 'Run Command'

