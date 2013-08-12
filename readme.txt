
		   _____                           _
 _____ _ _|     |___ _____ _____ ___ ___ _| |___ ___
|     | | |   --| . |     |     | .'|   | . | -_|  _|
|_|_|_|___|_____|___|_|_|_|_|_|_|__,|_|_|___|___|_| 


------------------
muCommander v0.9.1
------------------

muCommander is a lightweight, cross-platform file manager with a dual-pane interface.
It runs on any operating system with Java support (Mac OS X, Windows, Linux, *BSD, Solaris...).

Documentation can be found at http://trac.mucommander.com/ .

Please visit the muCommander forums (http://www.mucommander.com/forums) to ask questions, suggest features or
report a bug. Your feedback is always welcome!

Official website: http://www.mucommander.com
Copyright (C) 2002-2013 Maxence Bernard.


Requirements
------------

A Java Runtime Environment (JRE) 1.5 or later is required to run muCommander.
Java 1.6 is recommended, you can download it at http://java.com.

Mac OS X users: your favorite OS already comes with a Java runtime so you're good to go!

If you're having problems launching muCommander, make sure the JAVA_HOME environment variable points to the directory
where your Java runtime is installed.


What's new since v0.9 ?
-----------------------

New features:
- Lock tab capability, which prevents closing/moving the tab or changing its location.
- New quick list that presents open tabs in the current panel, mapped onto Alt+6 by default (ticket #450).
- Added the option to set fixed title for tab.
- Added the following actions: add tab, duplicate tab, clone tab to other panel.
- Added support for VMware vSphere virtual machines file system, contributed by Yuval Kohavi <yuval.kohavi@intigua.com>

Improvements:
- The state of all windows from last run is now restored on startup.
- Added the ability to copy the base name of files (ticket #462), contributed by Chen Rozenes.
- User can choose to always display tabs headers from preferences dialog (even when the panel contains single tab).
- Add the application name to window title on all OSs except Mac OS X (ticket #501).
- The visited locations history is now saved per-tab.
- The recently visited locations quick list now presents the visited locations on all tabs and windows.
- The content of recently visited locations quick list is now restored from previous run on startup (ticket #471).
- Added fullscreen support for Mac OS X Lion (ticket #468).
- Text file editor/viewer restore the full screen mode of last used (closed) editor/viewer on startup.
- 'Bonjour' support is now disabled by default on Mac OS (on fresh installation, i.e, with no previous 
  preferences) to prevent firewall dialog which keeps popping up on startup (workaround for ticket #339).
- Added 'ctrl+m' keystroke to toggle text file editor/viewer full screen mode.
- Tab can be closed by clicking on its header with middle mouse button.
- Assign 'ctrl+page_down' keystroke for switching to next tab, and 'ctrl+page_up' for switching to 
  previous tab (the keystrokes that were previously assigned to those actions remain as alternative keystrokes).
- Improved names and descriptions presented for tab-related actions.
- Added new category of actions in the 'shortcuts dialog' for tab-related actions.
- Changed tab's not-fixed-title to be in the pattern '<host>:<filename>'
- Show backward/forward locations list when pressing with right click on the back/forward buttons in the toolbar 
  instead of trigger back/forward actions
- Keyboard shortcuts can now be set for commands defined at commands.xml (ticket #456), contributed by Jarek Czekalski.
- Show empty name in the make file/directory dialog when it is opened (ticket #512), contributed by hclsiva.
- Mac OS X: enabled high-resolution rendering on Retina displays (ticket #518), contributed Alexey Lysiuk.
- Added Windows 8 and Mac OS X 10.8 to the OS versions.

Localization:
- Turkish translation has been updated.

Bug fixes:
- Prevent deadlock which caused the application to freeze while switching tabs on MAC OS.
- Recycle Bin is now working on Windows 64-bit with a 64-bit Java runtime (ticket #234).
- Key combinations that contain the TAB key can be set as shortcuts (ticket #465).
- Fix installation via software center on Ubuntu.
- Symbolic links cannot be opened (ticket #467).
- Encoding of text file is changed after being modified by the viewer/editor (ticket #438).
- Cannot connect to some FTP/SFTP bookmarks if there are more than 4 of them (ticket #525), contributed by Ondrej Dusek.
- Quick lists on the right panel sometimes not being focused (ticket #552), contributed by Jarek Czekalski.

Known issues:
- Some translations may not be up-to-date. Refer to http://trac.mucommander.com/wiki/Translations for more information.
- Mac OS X: "Do you want the application "muCommander.app" to accept incoming network connections?" dialog keeps popping
  up on startup even if the dialog has been previously accepted (ticket #339), when 'Bonjour' support is enabled.
- Executable permissions on local files are not properly preserved when running a unix-based OS with Java 1.5.
- SMB support may not work properly on non multi-language JRE.
- 'Copy files to clipboard' not working with some applications (files are not pasted).
- Mac OS X: some keyboard shortcuts may conflict with global system shortcuts.
- Authentication issues when using several sets of credentials (login/password) for the same server (see ticket #76).
- Untrusted HTTPS connections are allowed without a warning.
- Windows Vista/7: "java.net.SocketException: Permission denied: recv failed" error can appear when trying to access FTP
  sites. This seems to be a Windows firewall problem, with a possible workaround:
  http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7077696
- Unpacking files from 7z archive files can be slow.

License
-------

muCommander is released under the terms of the GNU General Public License.
Please refer to the 'license.txt' file bundled with muCommander.

muCommander uses the following great third party works :

- the Ant library released under the Apache License.
 Ant can be found at http://ant.apache.org .

- Apache Commons libraries released under the Apache License.
 Apache Commons can be found at http://commons.apache.org .

- Apache Hadoop released under the Apache License.
 Apache Hadoop can be found at http://hadoop.apache.org .

- the Furbelow library released under the GNU LGPL.
 Furbelow can be found at http://sourceforge.net/projects/furbelow .

- the ICU4J library released under the ICU License.
 the ICU project can be found at http://icu-project.org .

- the J2SSH library released under the GNU LGPL.
 J2SSH can be found at http://www.sshtools.com .

- the J7Zip library released under the GNU LGPL.
 J7Zip can be found at http://sourceforge.net/projects/p7zip/ .

- the jCIFS library released under the GNU LGPL.
 jCIFS can be found at http://jcifs.samba.org .

- the JetS3t library released under the Apache License.
 JetS3t can be found at http://jets3t.s3.amazonaws.com/index.html .

- the JmDNS library released under the GNU LGPL.
 JmDNS can be found at http://jmdns.sourceforge.net .

- the JNA library released under the GNU LGPL.
 JmDNS can be found at http://jna.dev.java.net .

- the JUnRar library released as Freeware.
 JUnRar can be found at http://sourceforge.net/projects/java-unrar .

- the Yanfs library released under the BSD license.
 Yanfs can be found at http://yanfs.dev.java.net .

- Icons by Mark James released under the Creative Commons Attribution License.
 Mark James can be found at http://famfamfam.com .


Credits
-------

Core developers: 
- Maxence Bernard
- Nicolas Rinaudo
- Arik Hadas
- Mariusz Jakubowski

Contributors:
- Ivan Baidakov
- Vassil Dichev
- Karel Klic
- David Kovar
- Joshua Lebo
- LeO
- Xavier Martin
- Alejandro Scandroli
- Alexander Yerenkow
- Johann Schmitz

Translators: 
- Frank Berger and Tony Klüver (German)
- Marcos Cobeña and Xavi Miró (Spanish)
- Jaromír Mára and Peter Vasko (Czech)
- Kent Hsu (Traditional Chinese)
- Jioh L. Jung (Korean)
- Andrzej Kosiński (Polish)
- György Varga and Tamás Balogh-Walder (Hungarian)
- 4X_Pro and Evgeny Morozov (Russian)
- whiteriver and Woodie (Simplified Chinese)
- Joze Kovacic (Slovenian)
- Catalin Hritcu (Romanian)
- Roberto Angeletti (Italian)
- Cristiano Duarte (Brazilian Portuguese)
- Pieter Kristensen (Dutch)
- Ján Ľudvík (Slovak)
- Jonathan Murphy (British English)
- Nardog (Japanese)
- Jakob Ekström (Swedish)
- Jeppe Toustrup (Danish)
- Mykola Bilovus (Ukrainian)
- ChArLoK_16 (Arabic)
- vboo (Belarusian)
- Ingrid Amundsen (Norwegian)
- Emre Aytaç (Turkish)
- Jordi Plantalech (Catalan)

Special thanks:
- Semyon Filippov (muCommander icon)
- Stefano Perelli (former muCommander icon)

Many thanks to all of you who suggested new features, reported bugs, sent warm emails or generously donated to the
project !


Command Line Interface
----------------------

muCommander comes with a few command line switches.
The following options are available:
 -a FILE, --assoc FILE             Load associations from FILE.
 -b FILE, --bookmarks FILE         Load bookmarks from FILE.
 -c FILE, --configuration FILE     Load configuration from FILE
 -C FILE, --commandbar FILE        Load command bar from FILE.
 -e FOLDER, --extensions FOLDER    Load extensions from FOLDER.
 -f FILE, --commands FILE          Load custom commands from FILE.
 -i, --ignore-warnings             Do not fail on warnings (default).
 -k FILE, --keymap FILE            Load keymap from FILE
 -p FOLDER, --preferences FOLDER   Store configuration files in FOLDER
 -S, --silent                      Do not print verbose error messages
 -s FILE, --shell-history FILE     Load shell history from FILE
 -t FILE, --toolbar FILE           Load toolbar from FILE
 -u FILE, --credentials FILE       Load credentials from FILE
 -h, --help                        Print the help text and exit
 -v, --version                     Print the version and exit
 -V, --verbose                     Print verbose error messages (default)
 -w, --fail-on-warnings            Quits when a warning is encountered during
                                   the boot process.

In addition to these, muCommander will interpret anything that comes after the last switch as a URI and load it in
its windows.
So for example:

 mucommander -b ~/.bookmarks.xml ftp://user@myftp.com ~/dev http://slashdot.org

Will:
 - read bookmarks from ~/bookmarks.xml
 - load a connection to myftp.com in the left panel of the main window
 - load ~/dev in the right panel of the main window
 - open a second window and load http://slashdot.org in its left panel
 - load the default directory in the second window's fourth panel


Documentation
-------------

Documentation on how to use, customize and extend muCommander is available at:
http://trac.mucommander.com
