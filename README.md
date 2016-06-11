# muCommander

[![Version](http://img.shields.io/badge/version-0.9.1-blue.svg?style=flat)](https://mucommander.atlassian.net/browse/MUC/?selectedTab=com.atlassian.jira.jira-projects-plugin:versions-panel)
[![License](http://img.shields.io/badge/License-GPL-blue.svg)](http://www.gnu.org/copyleft/gpl.html)
[![Build Status](https://travis-ci.org/mucommander/mucommander.svg)](https://travis-ci.org/mucommander/mucommander)
[![Dependency Status](https://www.versioneye.com/user/projects/545fe410868332aa70000093/badge.svg?style=flat)](https://www.versioneye.com/user/projects/545fe410868332aa70000093)
[![Coverity Scan](https://scan.coverity.com/projects/3642/badge.svg)](https://scan.coverity.com/projects/3642)

muCommander is a lightweight, cross-platform file manager with a dual-pane interface.  
It runs on any operating system with Java support (Mac OS X, Windows, Linux, *BSD, Solaris...).

Official website: http://www.mucommander.com  
Copyright (C) 2002-2016 Maxence Bernard.


Development
-----------

**Prerequisites**  
 - Java Development Kit (JDK) 1.6 or later  
 - Git  

**Code Editing**  
After cloning the source code repository from GitHub, you would probably want to import the project to an Integrated Development Environment (IDE) such as Eclipse or IntelliJ.

The code repository of muCommander is comprised of a main project that contains its core functionality and several sub-projects. These projects are defined as Gradle projects. Most of the popular IDEs today allow you to import Gradle projects out-of-the-box or via an IDE plugin. By importing the main project that is located at the root directory of the repository you will get all the required code in the IDE.

**Build from Command Line**  
The use of Gradle wrapper significantely simplifies the build from the command line. The following commands can be invoked from the root directory of the repositoring with no further installation.

You can run the application by typing:  
```
./gradlew run
```

It is recommended that whenever you get unclear compilation error and before submitting your change you do:  
```
./gradlew clean run
```    

License
-------

muCommander is released under the terms of the GNU General Public License.  
Please refer to the 'license.txt' file bundled with muCommander.


