
           _____                           _
 _____ _ _|     |___ _____ _____ ___ ___ _| |___ ___
|     | | |   --| . |     |     | .'|   | . | -_|  _|
|_|_|_|___|_____|___|_|_|_|_|_|_|__,|_|_|___|___|_| 


------------------
muCommander v1.6.0
------------------

muCommander is a lightweight, cross-platform file manager with a dual-pane interface.
It runs on any operating system with Java support (macOS, Windows, Linux, *BSD, Solaris...).

Please visit the muCommander project page on GitHub (https://github.com/mucommander) to ask questions,
suggest features, report a bug or browse documentation. Your feedback is always welcome!

Official website: https://www.mucommander.com


Requirements
------------

It is recommended to install muCommander from a package that comes with Java Runtime
Environment (JRE). The version of JRE that is included for macOS (dmg), Windows (msi) and
Linux  (rpm / deb), in this version of muCommander is 24, 20, and 20, respectively.

However, in case the included JRE is not supported on your machine, you can check the
noarch-dmg, portable, or tgz packages that require Java 11+ to be installed separately.
If you are having problems launching muCommander from these packages, make sure the JAVA_HOME
environment variable points to the directory where your Java runtime is installed.


What's new since v1.5.2 ?
-----------------------

New features:
- Added support for SMBv2 and SMBv3 protocols.
- Added an optional lexicographical sorting mode for filenames in the file table.
- Added support for the KAR (KAraf aRchive) file format.
- We now provide native .deb, .rpm, and .tgz packages for AArch64 Linux systems.

Improvements:
- Shell configuration changes are now reflected immediately in the terminal.
- Custom command settings are now stored in YAML format instead of XML.
- Enhanced "Open With" stability on macOS.
- Reduced log noise by removing unnecessary stack-traces during the startup process.
- Users can now provide paths with spaces as startup arguments on Linux.
- The application icon and name now display correctly in the GNOME taskbar.
- Updated the bundled Java Runtime Environment (JRE) to version 25.
- Updated JediTerm version.
- Updated RSyntaxTextArea.

Localization:
-

Bug fixes:
- Updated the Windows installer to ensure the Start Menu group is set to 'muCommander'.
- Fixed a bug that occasionally prevented the app from restarting after a forced exit or crash.
- Timestamp comparisons for SFTP folders are now limited to second-level precision.
- When copying files to Windows, invalid characters in filenames are now replaced with underscores.
- Restored the ability to copy filenames, basenames, and full paths to the clipboard.
- Fixed undesired beep and character when leaving Terminal with F12.
- Fixed unstable file panel after dragging a file and dropping it on a separate application.
- Fixed data encoding when launching the "Report a bug" action on macOS to ensure content displays properly.

Known issues:
- Some translations may not be up-to-date.
- Authentication issues when using several sets of credentials (login/password) for the same server.
- Untrusted HTTPS connections are allowed without a warning.
- Need to enable NTLM authentication manually in order to authenticate to Samba >= 4.5. This is done by adding 'ntlm auth = yes' to smb.conf.
- macOS: issues with browsing Documents/Desktop/Downloads may be solved by resetting Security and Privacy settings.
  See https://github.com/mucommander/mucommander/wiki/Reset-Security-&-Privacy-Settings-on-macOS for more details.
- macOS: muCommander may not start on older versions of macOS due to incompatibility of the bundled JRE.
- macOS: "Do you want the application "muCommander.app" to accept incoming network connections?" dialog keeps popping
  up on startup even if the dialog has been previously accepted (ticket #339), when 'Bonjour' support is enabled.
- macOS: some keyboard shortcuts may conflict with global system shortcuts.
- Installing the deb package on Debian < 12 fails with: "error: archive ... uses unknown compression for member 'control.tar.zst', giving up".
  A possible workaround is described in: https://unix.stackexchange.com/a/745467.

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
 JSCH can be found at https://github.com/mwiede/jsch .

- the 7-Zip-JBinding library released under the GNU LGPL.
 7-Zip-JBinding can be found at http://sevenzipjbind.sourceforge.net .

- the jCIFS library released under the GNU LGPL.
 jCIFS can be found at http://jcifs.samba.org .

- the SMBJ library released under the Apache License.
 SMBJ can be found at https://github.com/hierynomus/smbj .

- JediTerm library released under dual GNU LGPL and Apache License.
 JediTerm can be found at https://github.com/JetBrains/jediterm .

- the JetS3t library released under the Apache License.
 JetS3t can be found at http://jets3t.s3.amazonaws.com/index.html .

- the JmDNS library released under the GNU LGPL.
 JmDNS can be found at http://jmdns.sourceforge.net .

- the JNA library released under the GNU LGPL.
 JmDNS can be found at http://jna.dev.java.net .

- the JUnRar library released as Freeware.
 JUnRar can be found at http://sourceforge.net/projects/java-unrar .

- the RSyntaxTextArea library under the terms of BSD 3-Clause "New" or "Revised" License
 RSyntaxTextArea can be found at https://bobbylight.github.io/RSyntaxTextArea .

- the Yanfs library released under the BSD license.
 Yanfs can be found at http://yanfs.dev.java.net .

- the JCommander library released under the Apache License.
 JCommander can be found at http://jcommander.org .

- the SnakeYAML library released under the Apache License.
 SnakeYAML can be found at https://bitbucket.org/snakeyaml .

- the ICEpdf library released under the Apache License.
 ICEpdf can be found at https://github.com/pcorless/icepdf .

- the Unix4j library released under the MIT License.
 Unix4j can be found at http://unix4j.org/ .

- the FlatLaf library released under the Apache License.
 FlatLaf can be found at https://www.formdev.com/flatlaf .

- Icons by Mark James released under the Creative Commons Attribution License.
 Mark James can be found at http://famfamfam.com .


Credits
-------

Many thanks to all active and past contributors (https://github.com/mucommander/mucommander/graphs/contributors) and all of you who suggested new features, reported bugs, sent warm emails or generously donated (https://github.com/sponsors/mucommander) to the project !


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

