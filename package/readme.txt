
	   _____                           _
 _____ _ _|     |___ _____ _____ ___ ___ _| |___ ___
|     | | |   --| . |     |     | .'|   | . | -_|  _|
|_|_|_|___|_____|___|_|_|_|_|_|_|__,|_|_|___|___|_| 


------------------
muCommander v1.0.0
------------------

muCommander is a lightweight, cross-platform file manager with a dual-pane interface.
It runs on any operating system with Java support (macOS, Windows, Linux, *BSD, Solaris...).

Please visit the muCommander project page on GitHub (https://github.com/mucommander) to ask questions,
suggest features, report a bug or browse documentation. Your feedback is always welcome!

Official website: https://www.mucommander.com


Requirements
------------

A Java Runtime Environment (JRE) 8 or later is required to run muCommander.
Java 11 is recommended, you can download it at https://www.oracle.com/technetwork/java/javase/downloads/.

If you're having problems launching muCommander, make sure the JAVA_HOME environment variable points to the directory
where your Java runtime is installed.


What's new since v1.0.0 ?
-----------------------

New features:
- Added new looks and feels: FlatLaf Light, FlatLaf IntelliJ, FlatLaf Dark, FlatLaf Darcula, VAqua
- Added an option to overwrite files only if size differs
- Display Desktop folder in drive menu (just under home folder)

Improvements:
- Added a keyboard shortcut (ALT+DELETE) to Open Trash
- Added a keyboard shortcut (CTRL+SPACE) to file contextual menu
- Contextual menu is shown for file search results
- Local file search results can now be revealed in the native Desktop's file manager
- Added back the 'Go to forums' action that now points to the GitHub discussions page
- Speed up collecting file-system roots when Windows network shares disconnect
- Added an option to use system icons (if available) for Folders in Drive button
- Sort criterion/column and sort order are presented on files tables when using a non-native look and feel on macOS
- Added the 'Paste file(s)' action, when enabled, to the context menu of file tables.
- Changed the rendering of the text field which is used when renaming a file/folder.
- When searching a file that contains specified text, that text would be used by following 'Find next/previous' action within a text viewer/editor.
- The tab headers within the Preferences dialog are placed at the left side (instead of top) of the dialog on Windows and Linux.

Localization:
- Korean translation updated.

Bug fixes:
- Fixed broken links to: 'Online documentation', 'Report a bug' and 'Make a donation'
- If 'open -a Finder $f' is found in commands.xml, it is replaced with 'open -R $f' to preserve the behavior of 'Reveal in Finder' action
- Make folder operation creates the folder within the correct path rather than always within the root folder of Google Drive.
- Fixed copying multiple files to Google Drive.
- Fixed opening the authentication page for Google Drive in latest Chrome on Linux.
- The headers of the file table(s) are now translated and no longer appear in uppercase letters.
- Fixed possible IndexOutOfBoundsException that could have prevented file searching from being completed.

Known issues:
- Some translations may not be up-to-date.
- SMB support may not work properly on non multi-language JRE.
- 'Copy files to clipboard' not working with some applications (files are not pasted).
- Authentication issues when using several sets of credentials (login/password) for the same server.
- Untrusted HTTPS connections are allowed without a warning.
- Windows Vista/7: "java.net.SocketException: Permission denied: recv failed" error can appear when trying to access FTP
  sites. This seems to be a Windows firewall problem, with a possible workaround:
  http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7077696
- Unpacking files from 7z archive files can be slow.
- Need to enable NTLM authentication manually in order to authenticate to Samba >= 4.5. This is done by adding 'ntlm auth = yes' to smb.conf.
- Version 5.0 of RAR is not supported. Such archives would appear empty.
- macOS: issues with browsing Documents/Desktop/Downloads may be solved by resetting Security and Privacy settings.
  See https://github.com/mucommander/mucommander/wiki/Reset-Security-&-Privacy-Settings-on-macOS for more details.
- macOS: muCommander may not be able to start on version <= 10.10 (OS X Yosemite) due to incompatibility of the bundled JRE.
  This can be solved by installing Java 8 and running '/Applications/muCommander.app/Contents/replace_jre.sh'.
- macOS: "muCommander is damaged and cannot be opened" may appear when trying to start the application for the first time.
  This can be solved by executing: sudo xattr -r -d com.apple.quarantine /Applications/muCommander.app
- macOS: "Do you want the application "muCommander.app" to accept incoming network connections?" dialog keeps popping
  up on startup even if the dialog has been previously accepted (ticket #339), when 'Bonjour' support is enabled.
- macOS: some keyboard shortcuts may conflict with global system shortcuts.

License
-------

muCommander is released under the terms of the GNU General Public License.
Please refer to the 'license.txt' file bundled with muCommander.

muCommander uses the following great third party works :

- the Gradle library released under the Apache License.
 Gradle can be found at http:/gradle.org .

- Apache Commons libraries released under the Apache License.
 Apache Commons can be found at http://commons.apache.org .

- Apache Hadoop released under the Apache License.
 Apache Hadoop can be found at http://hadoop.apache.org .

- the Furbelow library released under the GNU LGPL.
 Furbelow can be found at http://sourceforge.net/projects/furbelow .

- the ICU4J library released under the ICU License.
 the ICU project can be found at http://icu-project.org .

- the JSCH library released under the BSD license.
 JSCH can be found at http://www.jcraft.com/jsch/ .

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

- the JCommander library released under the Apache License.
 JCommander can be found at http://jcommander.org .

- the ICEpdf library released under the Apache License.
 ICEpdf can be found at http://www.icesoft.com/icepdf .

- the Unix4j library released under the MIT License.
 Unix4j can be found at http://unix4j.org/.

- the FlatLaf library released under the Apache License.
 FlatLaf cab be found at https://www.formdev.com/flatlaf.

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
- Thomas Uebel
- Tomer Gabel

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
https://github.com/mucommander/mucommander/wiki

