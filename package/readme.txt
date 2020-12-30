
	   _____                           _
 _____ _ _|     |___ _____ _____ ___ ___ _| |___ ___
|     | | |   --| . |     |     | .'|   | . | -_|  _|
|_|_|_|___|_____|___|_|_|_|_|_|_|__,|_|_|___|___|_| 


------------------
muCommander v0.9.6
------------------

muCommander is a lightweight, cross-platform file manager with a dual-pane interface.
It runs on any operating system with Java support (macOS, Windows, Linux, *BSD, Solaris...).

Please visit the muCommander project page on GitHub (https://github.com/mucommander) to ask questions,
suggest features, report a bug or browse documentation. Your feedback is always welcome!

Official website: http://www.mucommander.com


Requirements
------------

A Java Runtime Environment (JRE) 8 or later is required to run muCommander.
Java 11 is recommended, you can download it at https://www.oracle.com/technetwork/java/javase/downloads/.

If you're having problems launching muCommander, make sure the JAVA_HOME environment variable points to the directory
where your Java runtime is installed.


What's new since v0.9.5 ?
-----------------------

New features:
- Support for Google Drive as a file protocol (including browsing, uploading and downloading capabilities).
- Enhance file searching with the ability to specify text that the files should contain (similar to 'grep').
- Enable switching to the next or previous image found in the active folder from the image viewer.
- The 'View' action now displays the calculated size of the selected directories in the folder panel.

Improvements:
- File-search dialog is loaded with the previously selected search options instead of the default search options.
- The wildcard characters '*' and '?' can be used within filenames in file-search.
- Revert the bundled JRE on macOS to version 11.0.4 that appear to be more stable than 11.0.7.
- Add the replace_jre.sh script that replaces the bundled JRE with the one of JavaAppletPlugin on macOS/Mac OS X.

Localization:
- Korean translation updated.

Bug fixes:
- Moving files on SMB shares to trash moves them to SAMBA trash on macOS.
- Reverse sorting can be set using shortcut keys.

Known issues:
- Mac OS X: "muCommander damaged and cannot be opened" may appear after downloading muCommander from the browser. This
  can be solved by executing: xattr -d com.apple.quarantine <jar_name>.jar
- Some translations may not be up-to-date.
- Mac OS X: "Do you want the application "muCommander.app" to accept incoming network connections?" dialog keeps popping
  up on startup even if the dialog has been previously accepted (ticket #339), when 'Bonjour' support is enabled.
- SMB support may not work properly on non multi-language JRE.
- 'Copy files to clipboard' not working with some applications (files are not pasted).
- Mac OS X: some keyboard shortcuts may conflict with global system shortcuts.
- Authentication issues when using several sets of credentials (login/password) for the same server.
- Untrusted HTTPS connections are allowed without a warning.
- Windows Vista/7: "java.net.SocketException: Permission denied: recv failed" error can appear when trying to access FTP
  sites. This seems to be a Windows firewall problem, with a possible workaround:
  http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7077696
- Unpacking files from 7z archive files can be slow.
- Need to enable NTLM authentication manually in order to authenticate to Samba >= 4.5. This is done by adding 'ntlm auth = yes' to smb.conf.
- Version 5.0 of RAR is not supported. Such archives would appear empty.
- Issues with browsing Documents/Desktop/Downloads on macOS may be solved by resetting Security and Privacy settings.
  See https://github.com/mucommander/mucommander/wiki/Reset-Security-&-Privacy-Settings-on-macOS for more details.
- Mac OS X: muCommander may not be able to start on version <= 10.10 (OS X Yosemite) due to incompatibility of the bundled JRE.
  This can be solved by installing Java 8 and running '/Applications/muCommander.app/Contents/replace_jre.sh'.

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

