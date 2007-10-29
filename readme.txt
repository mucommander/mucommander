           _____                           _        
 _____ _ _|     |___ _____ _____ ___ ___ _| |___ ___
|     | | |   --| . |     |     | .'|   | . | -_|  _|
|_|_|_|___|_____|___|_|_|_|_|_|_|__,|_|_|___|___|_| 


------------------
muCommander v0.8.1
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

- the jCIFS library released under the GNU LGPL.
 jCIFS can be found at http://jcifs.samba.org .

- the Jakarta Commons Net library released under the Apache License.
 Jarkata Commons Net can be found at http://jakarta.apache.org .

- the J2SSH library released under the GNU LGPL license.
 J2SSH can be found at http://www.sshtools.com .

- the Ant library released under the Apache License.
 Ant can be found at http://ant.apache.org .

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
- Xavier Martin (ISO and NRG archives support)

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

Many thanks to all of you who suggested new features, reported bugs, sent warm
emails or generously donated to the project !


What's new since v0.8 ?
-----------------------

New features:
- New bookmark:// filesystem, mapped onto the alt+B shortcut by default.

Improvements:
- Non-automatic sizing of columns is now working properly and preserved in 
  the configuration.
- Columns order and visibility is now preserved in the configuration.
- Editors and viewers are now centered on the current window rather than set
  to the upper-left corner of the screen.
- Shell encoding is now auto-detected by default, unless a specific encoding
  is set in the preferences.
- Simplified and extended the theme editor, with new 'selection outline' and
  'alternate background' values.
- Added keyboard shortcuts for the items in the drive popup button.
- Windows: drives in the drive popup button now show extended names.
- Bzip2 read-access speed substantially improved.
- Improved icon representation of symbolic links

Localization:

Bug fixes:
- Fixed KDE detection when the Gnome libraries are installed.
- Fixed random display issues after changing the folder panel's font.
- Fixed theme color choosers not displaying the right font in their preview.
- Fixed incorrectly reported total and free space information under Unix-based
  platforms when the mount point of the volume would contain space characters.
- Fixed quicksearch refresh issue when the 'unmatched' background and regular
  background were the same.
- Fixed editable combo-boxes not reacting well to font change.
- Fixed folder panel borders failing to refresh properly when their color
  was changed.
- Fixed "Download or browse" dialog that appeared twice when opening HTTP files.
- Fixed issues with opening some bzip2 archives.
- A single click on the current row of the inactive table can no longer 
  trigger the filename/date/permission editor.
- Fixed issues when exploring the SMB 'network neighborhood' (smb://)
- SFTP: fixed symlinks to directories not properly handled

Known issues:
- Italian, Korean and Polish translations are not up-to-date.
- Executable permissions are not preserved properly for local files prior to
  Java 1.6.
- SMB support may not work properly on non multi-language JRE.
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


Command Line Interface
----------------------

muCommander comes with a few command line switches.
The following options are available:
 -a FILE, --assoc FILE             Load associations from FILE.
 -b FILE, --bookmarks FILE         Load bookmarks from FILE.
 -c FILE, --configuration FILE     Load configuration from FILE
 -C FILE, --commandbar FILE        Load command bar from FILE
 -f FILE, --commands FILE          Load custom commands from FILE.
 -i, --ignore-warnings             Do not fail on warnings (default).
 -k FILE, --keymap FILE            Load keymap from FILE
 -n, --no-debug                    Disable debug output to stdout
 -d, --debug                       Enable debug output to stdout (default)
 -p FOLDER, --preferences FOLDER   Store configuration files in FOLDER
 -o, --no-splash                   Disable splashscreen on startup
 -O, --splash                      Enable splashscreen on startup (default)
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
Documentation on how to customize those commands can be found here:
http://www.mucommander.com/forums/viewtopic.php?t=420


- associations.xml
This file controls muCommander's custom file associations. They will be
initialised according to the system muCommander is running on and depend on
commands.xml.

This file is not (yet) written by muCommander, so you have to create it
manually for the time being.

It looks something like this:
<?xml version="1.0" encoding="UTF-8"?>
<associations>
    <association mask=".*" command="open"/>
</associations>

Each association has the following attributes:
- mask:    regular expression that the file name must match in order to be
           managed by the association.
- read:    if set to 'yes', a file must be readable in order to be matched by
           the association.
- write:   if set to 'yes', a file must be writable in order to be matched by
           the association.
- execute: if set to 'yes', a file must be executable in order to be matched by
           the association.
- command: the alias of the command to execute for this type of files.

You must be extremely careful when editing this file, as it's quite easy to
break muCommander by fiddling with it. Make sure that the system's default
associations are always present, and that you do not change their attributes.
- Windows 9x:
<association mask=".*" command="open"/>

- Windows NT:
<association mask=".*\\.[eE][xX][eE]" command="openEXE"/>
<association mask=".*" command="open"/>

- Mac OS X:
<association mask=".*" command="open"/>

- Gnome:
<!-- Only if running on Java prior to 1.6 -->
<association mask="[^.]+" command="execute"/>

<association mask=".*"    command="execute" execute="yes"/>
<association mask=".*"    command="open"/>

- KDE:
<association mask="^https?:\\/\\/.+" command="openURL"/>
<association mask=".*"               command="open"/>

- Anything else:
<!-- Only if running on Java prior to 1.6 -->
<association mask="[^.]+" command="execute"/>

<association mask=".*"    command="execute"/>

