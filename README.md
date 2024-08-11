# muCommander

[![Version](http://img.shields.io/badge/version-1.6.0-blue.svg?style=flat)](https://github.com/mucommander/mucommander/releases/tag/nightly)
[![License](http://img.shields.io/badge/License-GPL-blue.svg)](http://www.gnu.org/copyleft/gpl.html)
[![Build Status](https://github.com/mucommander/mucommander/actions/workflows/nightly.yml/badge.svg)](https://github.com/mucommander/mucommander/actions/workflows/nightly.yml)
[![Coverity Scan](https://scan.coverity.com/projects/3642/badge.svg)](https://scan.coverity.com/projects/3642)
[![Gitter](https://img.shields.io/gitter/room/nwjs/nw.js.svg)](https://gitter.im/mucommander/Lobby)

muCommander is a lightweight, cross-platform file manager with a dual-pane interface.
It runs on any operating system with Java support (macOS, Windows, Linux, *BSD, Solaris...).

Official website: https://www.mucommander.com

## Contribution


There are several ways to contribute to muCommander:

- Found a bug or thinking about a useful feature that is missing? [File an issue](https://github.com/mucommander/mucommander/issues)
- Want to fix a bug or implement a feature? We are using the standard [GitHub flow](https://guides.github.com/introduction/flow/): fork, make the changes, and submit a pull request. Changes are merged to the *master* branch. See the next section for tips for developing muCommander.
- If you happen to speak a language that muCommander is not available in or able to improve existing translations, you can help translate the interface, see more details [here](https://github.com/mucommander/mucommander/wiki/Translate).

If you want to get involved in muCommander or have any question or issue to discuss, you are more than welcome to join our rooms on [Gitter](https://gitter.im/mucommander).  

## Development

### Prerequisites

  - Java Development Kit (JDK) 11 or later
  - Git


### Forks and pull requests

If you would like to contribute code, it is required to fork the repository and submit a [pull request](https://help.github.com/en/articles/about-pull-requests).
Within pull requests, it is possible to review, discuss, and improve the changes until they are ready for production. 

### Code Editing
After cloning the source code repository from GitHub, you would probably want to import the project to an Integrated Development Environment (IDE) such as Eclipse or IntelliJ.

The code repository of muCommander is comprised of a main project that contains its core functionality and several sub-projects. These projects are Gradle projects. Most of the popular IDEs today allow you to import Gradle projects out-of-the-box or via an IDE plugin. By importing the main project that is located at the root directory of the repository you will get all the required code in the IDE.

### How to Run
The use of the Gradle wrapper significantly simplifies the build from the command line. The following commands can be invoked from the root directory of the repository with no further installation.

You can run the application by typing:
```
./gradlew run
```

It is recommended to run the following command when getting an unclear compilation error, or to be sure running the just modified code:
```
./gradlew clean run
```    

You can also run directly from within you IDE by executing gradlew `run` task.


### How to Debug
In order to debug muCommander, you need to add `-Pdebug=PORT_NUMBER` while running muComander. To control whether you want suspend JVM or not, please use `-Psuspend=n|y` parameter (suspend=y is a default).
Example of running muCommander in debug mode:
```
./gradlew run -Pdebug=5005 -Psuspend=n
```
Then, you can run a debugger that connects to this port using your favorite IDE (see [an example for doing this with IntelliJ](https://github.com/mucommander/mucommander/wiki/Debug-from-IntelliJ)).


### Packaging
The creation of a DMG file for macOS (produced in build/distributions):
```
./gradlew clean dmg -PskipDmgSign
```

Note: as the application is not signed, the following error may appear when trying to start it on macOS: "muCommander damaged and cannot be opened".
This can be solved by executing: `sudo xattr -r -d com.apple.quarantine /Applications/muCommander.app`

The creation of an EXE file for Windows (produced in build/launch4j):
```
./gradlew clean createExe
```

The creation of TGZ distribution for Linux/Unix (produced in build/distributions):
```
./gradlew clean tgz
```

The creation of RPM distribution for Red Hat Linux flavors:
```
./gradlew clean rpm
```

The creation of DEB distribution for Debian Linux flavors:
```
./gradlew clean deb
```

More packaging options are described in [our wiki](https://github.com/mucommander/mucommander/wiki/Packaging).


