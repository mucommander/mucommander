----------------
muCommander v0.6
----------------

muCommander is a cross-platform file manager.  It features a Norton
Commander style interface and runs on any operating system that has
support for the Java 2 Platform, Standard Edition.

Please use the muCommander forums (http://www.mucommander.com/forums)
to post your questions, suggestions or bug reports. Your feedback is
important and always welcome!

muCommander uses the jCIFS library released under the LGPL to provide
SMB support. jCIFS can be found at http://jcifs.samba.org.

Official website: http://www.mucommander.com
(c) 2003 Maxence Bernard. All rights reserved.


Requirements
------------

You must read and accept the license contained in this package prior
to using muCommander.

- Java Runtime Environment 1.3.0 (JRE) or later is required to run
  muCommander. JRE 1.4 is recommended, you can download it at
  http://java.sun.com/j2se/1.4/.  Mac OS X users: JRE 1.3.1 already
  comes preinstalled on Mac OS X.

- If you're having problems launching muCommander, make sure the
  $JAVA_HOME environment variable points to the directory where your
  JRE or JDK is installed.


What's new in v0.6?
-------------------

New features:
- Send files as email attachments [ctrl+S]
- Automatic refresh when folder content has changed
- New progress bar with more information and improved refresh rate
- Option to hide toolbar
- Option to display file size in a compact manner
- [shift+F6] renames selected file
- [shift+F5] copies selected file to local directory
- 'File -> Run command' to run a command in local directory [ctrl+R]
- Mac OS X : 'About', 'Preferences' and 'Quit' menu items, dirty window indication in text editor
- Symbolic link support: special color and specific handling during file operations
- 'Apply' button in preferences dialog
- New logo :)

Improvements:
- Reworked all windows and dialogs for cleaner and roomier layout
- Increased file access performance
- jCIFS library (for SMB support) updated to 0.7.15
- Improved Properties dialog refresh rate
- Mac OS X : preferences file moved to ~/Library/Preferences/mucommander instead of ~/.mucommander/ (backward-compatible)
- Keyboard shortcuts added to toolbar tooltips
- Sound beep on question dialogs
- Improved toolbar icons management
- Redesigned sort headers
- Optimized 'About' dialog
- 'Quit' button and menu item renamed to 'Close' and 'Close window' [F10]

Bug fixes
- Fixed bug when moving a file and cancelling, original of the file currently being moved would be deleted
- Fixed a memory leak when closing main window
- Fixed bug which occurred when 'show hidden files' option was disabled and caused the application to fail on certain folders
- Fixed minor bug in question dialogs which sometimes led to an extra row of buttons
- Fixed bug when copying files and canceling, current file unselected even though file/folder not completely copied
- Fixed annoying delay on startup when last folder was an SMB share
- Fixed bug when copying a folder to a folder containing a folder with the same, copied folder would get nested (reported by Pixel)
- '..' date used to be that current folder's parent instead of being current folder's
- Fixed cryptic error message when a folder cannot be created during a Copy/Move file operation
- Fixed no title in Zip file overwrite dialog

Known issues:
- Symbolic links are not detected in SMB folders
- Folder cannot be renamed to upper/lower case variations of the same name
- Pipe '|' and other shell operators cannot be used in 'Run Command'
- 'Sound beep on question dialog' doesn't work under Mac OS X + Java 1.3.1


History
-------

v0.5 [12/31/02]
New features:
- Look-and-Feel, font, colors, time/date format and startup folders can now be configured
- Option to show/hide hidden files
- Sort files by extension
- Compare directories: marks files that are missing from a directory or that are newer
- Links to the muCommander homepage & forums (Windows & Mac OS X only)

Improvements:
- Current sort criteria is now indicated in view menu
- Shift+enter forces native association even for folders
- jCIFS library (for SMB support) updated to v0.6.7
- File selection dialog can now select files whose filename either contains, starts with or ends with a given keyword
- Buttons in dialogs are now all of the same size

Bug fixes:
- Fixed weird behavior when manipulating files that have been changed since they were last refreshed 
- Fixed several bugs in the file selection dialog
- Fixed a bug which caused files to no longer be marked after F9/Refresh
- Fixed a bug in copy/move operation: 'cancel' did not stop current file to be copied/moved
- Fixed a bug which crashed the app when accessing a remote SMB file whose filename contains a '@' symbol
- Fixed a rare bug which occurred when trying to access an unreachable SMB folder and resulted in the app to display the full parent folder's path (including login and password) instead of '..'


v0.4 [8/22/02]
New features:
- SMB support: 'File->Connect to Server' to browse a remote SMB server
- Add to zip: mark files and select 'File->Zip'
- Batch-unzip: mark .zip files and select 'File->Unzip'

Improvements:
- default 'enter' button in dialogs
- Arrow keys to navigate through dialog choice
- Button shortcuts in dialogs do not require pressing ALT anymore
- All dialogs can be closed with ESC
- Files can be marked with Shift+UP/DOWN, Shift+Left-click and CTRL+Right-click
- Shift+ENTER forces native file association
- Initial folders are now set to current drive (Win32) or '/' (Unix) and to user's home directory
- Copy and Move/Rename dialogs now include filename to make it easier to rename a file
- Files which have successfully been copied/moved/deleted/... are now unmarked
- Selecting a folder with ALT+F1/F2 sets focus to the corresponding panel
- Improved quicksearch (error-tolerant)
- 'Hide' button in progress dialogs to minimize muCommander
- ESC in location field now restores current location
- Viewer and Editor windows size
- Default 'Save As' filename in editor
- Wait mouse cursor when changing folder
- 'Command prompt' window under Win32 doesn't show up anymore
- Toolbar icons' border not painted anymore under OS X
- Performance improved on local filesystems

Bug fixes:
- "Drive Not Ready" Windows bug on startup fixed, application loads much faster now
- Location path inside zip/jar archives fixed
- Properties dialog display bug
- Bug in "File may be too large to be viewed... Yes/No" dialog
- Fixed bug with directories ending with .zip or .jar
- Fixed initial size problem under OS X on 800x600 or smaller displays
- Fixed minimum size problem under OS X


v0.3 [5/8/02]
New features:
- Simple text viewer and editor
- Icon toolbar
- 'Go Back' and 'Go Forward' actions
- ALT+Enter displays file properties
- New 'Mark' and 'View' menus
- 'Swap panels' action
- 'Set same folder' action
- 'Mark all', 'Unmark all' actions
- Arrow showing sort order on panel
- Panels can be expanded/collapsed
- Tooltip shows full name/size/date when string is truncated
- CTRL+Left/CTRL+Right cycles through windows
- Backspace key goes up to parent folder
- New 'About' window with useless animation

Improvements:
- Root folders combo box's behavior improved under JDK/JRE 1.4
- Parent folder gets selected after going up (..)
- '..' parent folder can now be quick-searched
- Escape key location field cancels operation
- 'Keys' list categorized
- +, -, * now works only on num keypad

Bug fixes:
- Fixed initial divider location bug under JDK/JRE 1.4
- Fixed focus lost when switching window under JDK/JRE 1.4
- Fixed bug when copying/moving a single file and the destination is a file which already exists
- Fixed focus problems in dialogs
- Fixed uncaught exception when entering a non-existent file path in location field
- Turned off screen menu bar under Mac OS X that seems muCommander to crash when switching windows


v0.2 [3/14/02]
New features:
- Multiple windows support: open a new window with ALT+N or File->New Window and recall opened windows with ALT+1, ALT+2... or through the 'Window' menu.
- Quicksearch: type in the first letter(s) of a filename to select it.
- Files can be marked/unmarked with the right mouse button.
- New options: Skip all, Append all, Overwrite all, Overwrite older when a file already exists in destination.
- Enabled hardware acceleration under Mac OS X with Java 1.3.1 update 1.

Improvements:
- Keyboard focus in dialogs largely improved (especially under MacOS X).
- Escape key can be used in dialogs to cancel operation.
- 'File already exists' dialog gives information (size, date) about source and destination files.
- Current file path shortened in Move/Copy/Delete progress window (relative path is now used instead of absolute).
- File size formatted with comma characters.

Bug fixes:
- Fixed move bug that happened when cancelling a move operation after a 'file already exists' dialog, it would still try to move parent folders.
- Fixed repaint bug that sometimes happened when a folder contents was updated or changed and contained less files than before.
- Prevents user from moving files to a folder which is itself marked (inconsistent operation).
- Dialogs size now constrained to more rational values.


v0.1 [2/17/02]
- Initial release. Fully functional version with basic commander features.
  ZIP and JAR file support (read-only).
