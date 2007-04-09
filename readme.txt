           _____                           _        
 _____ _ _|     |___ _____ _____ ___ ___ _| |___ ___
|     | | |   --| . |     |     | .'|   | . | -_|  _|
|_|_|_|___|_____|___|_|_|_|_|_|_|__,|_|_|___|___|_| 

-------------------------------
muCommander v0.8 (nighly build) 
-------------------------------

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
- Peter Vasko and Jaromír Mára (Czech)
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



What's new since v0.8 beta 3 ?
------------------------------

New features:
 - Added 'Use system file icons' option to 'Appearance' preferences, allowing to display file icons as they are displayed on the system's desktop
 - Added 'Show folders first' option to 'View' menu, allowing to control whether folders are displayed first (default) or mixed with regular files
 - Added support for Growl: notifications are sent when transfers are complete or an error has occurred

Improvements:
 - Added 'Go to' button to 'Edit bookmarks' and 'Edit credentials' dialogs to go to the selected location
 - Added 'Show seconds' and 'Show century' options to 'Date & time' preferences, to show seconds/century in file dates
 - FTP encoding can be changed in the 'Connect to server' dialog ; connection properties are now saved with credentials

Localization:
 - Russian translation update

Bug fixes:
 - FTP connections now default to UTF-8 encoding (instead of ISO-8859-1) and send the 'OPTS UTF8 ON' command to the server to enable UTF-8

Known issues:
 - Hungarian translation is not up-to-date
 - Executable permissions are not preserved properly for local files prior with Java <1.6
 - Zip files encoded in a charset other than UTF-8 will not display non-ASCII filenames properly due to a limitation of java.util.zip (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4244499)
 - Some BZ2 archives can not be properly opened
 - SMB support may not work properly on non multi-language JRE
 - 'Copy files to clipboard' not working with some applications (files are not pasted)
 - Mac OS X : 'Sort by' keyboard shortcuts in menu show Command modifier instead of Ctrl, OS X's keyboard navigation shortcuts must be disabled in 'System Preferences' for them to work properly
 - Mac OS X : text fields don't work correctly when using a fractionally-scaled font (known Java bug, http://lists.apple.com/archives/Java-dev/2005/May/msg00670.html)
 - Editable combo boxes have keyboard navigation issues under Java 1.6
