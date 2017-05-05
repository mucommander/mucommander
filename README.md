# muCommander

[![Version](http://img.shields.io/badge/version-0.9.3-blue.svg?style=flat)](https://github.com/mucommander/mucommander/releases)
[![License](http://img.shields.io/badge/License-GPL-blue.svg)](http://www.gnu.org/copyleft/gpl.html)
[![Build Status](https://travis-ci.org/mucommander/mucommander.svg)](https://travis-ci.org/mucommander/mucommander)
[![Dependency Status](https://www.versioneye.com/user/projects/545fe410868332aa70000093/badge.svg?style=flat)](https://www.versioneye.com/user/projects/545fe410868332aa70000093)
[![Coverity Scan](https://scan.coverity.com/projects/3642/badge.svg)](https://scan.coverity.com/projects/3642)
[![Gitter](https://img.shields.io/gitter/room/nwjs/nw.js.svg)](https://gitter.im/mucommander/Lobby)

muCommander is a lightweight, cross-platform file manager with a dual-pane interface.  
It runs on any operating system with Java support (Mac OS X, Windows, Linux, *BSD, Solaris...).

Official website: http://www.mucommander.com  
Copyright (C) 2002-2016 Maxence Bernard.

Contribution
------------

There are several ways to contribute to muCommander:  

- Found a bug or thinking about a useful feature that is missing? [File an issue](https://github.com/mucommander/mucommander/issues)
- Want to fix a bug or implement a feature? We are using the standard [GitHub flow](https://guides.github.com/introduction/flow/): fork, make the changes and submit a pull request. Changes are merged to the *master* branch. See the next section for tips for developing muCommander.
- If you happen to speak a language that muCommander is not available in or able to improve existing tranlations, you can help translate the interface using the [zanata platform](https://translate.zanata.org/project/view/mucommander).  

If you want to get involved in muCommander or have any question or issue to discuss, you are more than welcome to join our rooms on [Gitter](https://gitter.im/mucommander).  

Development
-----------

**Prerequisites**

  - Java Development Kit (JDK) 1.8 or later  
  - Git

**Code Editing**  
After cloning the source code repository from GitHub, you would probably want to import the project to an Integrated Development Environment (IDE) such as Eclipse or IntelliJ.

The code repository of muCommander is comprised of a main project that contains its core functionality and several sub-projects. These projects are Gradle projects. Most of the popular IDEs today allow you to import Gradle projects out-of-the-box or via an IDE plugin. By importing the main project that is located at the root directory of the repository you will get all the required code in the IDE.

**Build from Command Line**  
The use of Gradle wrapper significantly simplifies the build from the command line. The following commands can be invoked from the root directory of the repositoring with no further installation.

You can run the application by typing:  
```
./gradlew run
```

It is recommended that whenever you get unclear compilation error and before submitting your change you do:  
```
./gradlew clean run
```    

**Packaging**  
The different distributions of muCommander are based on a shadow-jar, standalone jar that contains all the required dependencies and filter their unneeded parts. The creation of the shadow-jar is done by the following command:  
```
./gradlew shadowJar
```

The creation of a DMG file for MAC OS (produced in build/distributions):  
```
./gradlew createDmg
```

The creation of an EXE file for Windows (produced in build/launch4j):  
```
./gradlew createExe
```

The creation of a TGZ file for Linux/Unix (produced in build/distributions):  
```
./gradlew tgz
```

More packaging options are described in [our wiki](https://github.com/mucommander/mucommander/wiki/Packaging).

License
-------

muCommander is released under the terms of the GNU General Public License.  
Please refer to the [license.txt file](https://github.com/mucommander/mucommander/blob/master/package/license.txt) bundled with muCommander.


