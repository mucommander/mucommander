           _____                           _        
 _____ _ _|     |___ _____ _____ ___ ___ _| |___ ___
|     | | |   --| . |     |     | .'|   | . | -_|  _|
|_|_|_|___|_____|___|_|_|_|_|_|_|__,|_|_|___|___|_| 


------------------
muCommander v0.8.2
------------------

muCommander is a cross-platform file manager featuring a Norton Commander
style interface and running on any operating system with Java support
(Mac OS X, Windows, Linux, *BSD, Solaris...).

Please use the muCommander forums (http://www.mucommander.com/forums)
to post your questions, suggestions or bug reports.
Your feedback is important and always welcome!

Official website: http://www.mucommander.com
Copyright (C) 2002-2007 Maxence Bernard


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

- the jCIFS library released under the GNU LGPL.
 jCIFS can be found at http://jcifs.samba.org .

- the JmDNS library released under the GNU LGPL.
 JmDNS can be found at http://jmdns.sourceforge.net .

- the Yanfs library released under the BSD license.
 Yanfs can be found at http://yanfs.dev.java.net .

- Icons by Mark James released under the Creative Commons Attribution License.
 Mark James can be found at http://famfamfam.com .


Credits
-------

Lead developers: 
- Maxence Bernard
- Nicolas Rinaudo

Code contributors:
- Mariusz Jakubowski
- Xavier Martin
- Alejandro Scandroli

Translators: 
- Tony Klüver and Frank Berger (German)
- Marcos Cobeña (Spanish)
- Peter Vasko and Jaromír Mára (Czech)
- Kent Hsu (Traditional Chinese)
- Jioh L. Jung (Korean)
- Andrzej Kosiński (Polish)
- Tamás Balogh-Walder and György Varga (Hungarian)
- XXXX Pro (Russian)
- Woodie (Simplified Chinese)
- Joze Kovacic (Slovenian)
- Catalin Hritcu (Romanian)
- Roberto Angeletti (Italian)
- Cristiano Duarte (Brazilian Portuguese)
- Pieter Kristensen (Dutch)
- Ján Ľudvík (Slovak)
- Jonathan Murphy (British English)
- Nardog (Japanese)

Many thanks to all of you who suggested new features, reported bugs, sent warm
emails or generously donated to the project !


What's new since v0.8.1 ?
-------------------------

New features:
- Files can be marked/unmarked using a Regexp.
- New 'Owner' and 'Group' columns added to the folder pane (not visible by
  default). The owner and group information is available on FTP and SFTP folders
  and some TAR archives.

Improvements:
- Added an 'Explore bookmarks' item to the 'Bookmarks' menu.
- Job confirmation dialogs show the list of files to be processed.
- All OK/cancel choices can be navigated with the arrow keys.

Localization:
- Japanese translation contributed by Nardog.

Bug fixes:
- SMB passwords that contain '@' characters are now properly handled.
- Fixed a bug that occurred when sorting the folder pane by an invisible column 
  or when hiding the current column. This bug would leave the folder pane in an 
  inconsistent sort order and could prevent the application from restarting.
- Fixed a bug that allowed empty custom associations to be registered, breaking
  the whole file matching process.
- Fixed a regression affecting the opening of files on non-local filesystems.
- Fixed an issue with the drive button which would sometimes change the folder
  on the opposite panel.

Known issues:
- Korean translation is not up-to-date.
- Executable permissions are not preserved properly for local files prior to
  Java 1.6.
- SMB support may not work properly on non multi-language JRE.
- Access to public SMB shares require credentials.
- 'Copy files to clipboard' not working with some applications (files are not
  pasted).
- Mac OS X : 'Sort by' keyboard shortcuts in menu show Command modifier instead
  of Ctrl, OS X's keyboard navigation shortcuts must be disabled in 'System
  Preferences' for them to work properly.
- Mac OS X : text fields don't work correctly when using a fractionally-scaled
  font (known Java bug,
   http://lists.apple.com/archives/Java-dev/2005/May/msg00670.html).
- Editable combo boxes have keyboard navigation issues under Java 1.6.
- The bookmark file system doesn't allow users to specify which credentials
  they wish to use when selecting a bookmark that needs authentification, it
  will default to the first in the list.
- Authentication issues when using several sets of credentials (login/password)
  on the same server.


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
<action class="com.mucommander.ui.action.CopyFileNamesAction"
        keystroke="alt C" alt_keystroke="meta C"/>

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
