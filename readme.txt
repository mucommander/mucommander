           _____                           _        
 _____ _ _|     |___ _____ _____ ___ ___ _| |___ ___
|     | | |   --| . |     |     | .'|   | . | -_|  _|
|_|_|_|___|_____|___|_|_|_|_|_|_|__,|_|_|___|___|_| 


----------------------------
muCommander v0.8.5 (nightly)
----------------------------

muCommander is a cross-platform file manager featuring a Norton Commander
style interface and running on any operating system with Java support
(Mac OS X, Windows, Linux, *BSD, Solaris...).

Please use the muCommander forums (http://www.mucommander.com/forums)
to post your questions, suggestions or bug reports.
Your feedback is important and always welcome!

Official website: http://www.mucommander.com
Copyright (C) 2002-2009 Maxence Bernard.


Requirements
------------

A Java Runtime Environment (JRE) 1.5 or later is required to run muCommander.
Java 1.6 is recommended, you can download it at http://java.com.

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

- the jCIFS library released under the GNU LGPL license.
 jCIFS can be found at http://jcifs.samba.org .

- the JmDNS library released under the GNU LGPL license.
 JmDNS can be found at http://jmdns.sourceforge.net .

- the JNA library released under the GNU LGPL license.
 JmDNS can be found at http://jna.dev.java.net .

- the JUnRar library released as Freeware.
 JUnRar can be found at http://sourceforge.net/projects/java-unrar .

- the Yanfs library released under the BSD license.
 Yanfs can be found at http://yanfs.dev.java.net .

- Icons by Mark James released under the Creative Commons Attribution License.
 Mark James can be found at http://famfamfam.com .

- the Furbelow library released under the GNU LGPL license.
Furbelow can be found at http://sourceforge.net/projects/furbelow .


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
- Marcos Cobeña (Spanish)
- Jaromír Mára and Peter Vasko (Czech)
- Kent Hsu (Traditional Chinese)
- Jioh L. Jung (Korean)
- Andrzej Kosiński (Polish)
- György Varga and Tamás Balogh-Walder (Hungarian)
- 4X_Pro (Russian)
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

Special thanks:
- Semyon Filippov (muCommander icon)
- Stefano Perelli (former muCommander icon)

Many thanks to all of you who suggested new features, reported bugs, sent warm
emails or generously donated to the project !


What's new since v0.8.4 ?
-------------------------

New features:
-

Improvements:
- A proper error message is displayed when muCommander is started in a
  headless environment.

Localization:
-

Bug fixes:
-

Known issues:
- Some translations are not up-to-date. Refer to
  http://trac.mucommander.com/wiki/Translations for more information.
- Recycle Bin not working on Windows 64-bit (ticket #234).
- Executable permissions are not preserved properly for local files prior to
  Java 1.6.
- SMB support may not work properly on non multi-language JRE.
- 'Copy files to clipboard' not working with some applications (files are not
  pasted).
- Mac OS X: some keyboard shortcuts may conflict with global system shortcuts.
- Mac OS X: text fields don't work correctly when using a fractionally-scaled
  font (known Java bug,
   http://lists.apple.com/archives/Java-dev/2005/May/msg00670.html).
- Authentication issues when using several sets of credentials (login/password)
  for the same server (see ticket #76).
- Untrusted HTTPS connections are allowed without a warning.


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


Documentation
-------------

Documentation on how to use, customize and extend muCommander is available at:
http://trac.mucommander.com
