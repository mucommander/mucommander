------------------
muCommander v0.7.1
------------------

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

muCommander uses the following libraries:

- the jCIFS library released under the LGPL license to provide SMB support.
 jCIFS can be found at http://jcifs.samba.org .

- the Jakarta Commons Net library released under the Apache license to provide FTP support. 
 Commons Net can be found at http://jakarta.apache.org .

- Java TAR released under the Public Domain to provide TAR support.
 Java TAR can be found at http://www.trustice.com/java/tar/ .


Requirements
------------

Java Runtime Environment 1.3.0 (JRE) or later is required to run
muCommander. JRE 1.4 is recommended, you can download it at http://java.com.

Mac OS X users: Your favorite OS already comes with a Java runtime so you're good to go!

If you're having problems launching muCommander, make sure the
$JAVA_HOME environment variable points to the directory where your
JRE or JDK is installed.


What's new in v0.7.1 ?
----------------------

New features:
 - TAR, GZip, TGZ archives support (read-only for now)
 - German translation provided by Tony Klüver and Frank Berger
 - Spanish translation provided by Marcos Cobeña

Improvements:
 - Improved internal file attributes caching mechanism to lower CPU and memory usage
 - Control+C / Command+C now copies filename only instead of entire row information
 - File download dialog now contains opposite panel's path
 - Increased maximum size of some dialogs to accommodate new languages

Bug fixes :
 - Archive files over HTTP now correctly handled 
 - FTP Passive mode not turned on when entering an FTP URL in location text field
 - Fixed a bug in HTTP/HTML folders causing some files to be unaccessible
 - Fixed automatic folder refresh hogging CPU unnecessarily

Known issues:
 - Slow TAR and Zip extraction for small files because extraction is not performed sequentially (yet)
 - Mac OS X-specific files not transferred properly
 - Image viewer doesn't free memory properly under Mac OS X
 - Folders cannot be renamed to upper/lower case variations of the same name
 - Pipe '|' and other shell operators cannot be used in 'Run Command'

