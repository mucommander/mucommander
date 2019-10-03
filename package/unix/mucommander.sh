#!/bin/sh

# muCommander startup arguments
MUCOMMANDER_ARGS="@ARGS@"
JAVA_ARGS="@JAVA_ARGS@"

# if JAVA_HOME exists, use it
if [ -x "$JAVA_HOME/bin/java" ]
then
  JAVA="$JAVA_HOME/bin/java"
else
  if [ -x "$JAVA_HOME/jre/bin/java" ]
  then
    JAVA="$JAVA_HOME/jre/bin/java"
  else
    JAVACMD=`which java 2> /dev/null `
    if [ -z "$JAVACMD" ] ; then
      echo "Error: cannot find java VM."
      exit 1
    fi
    JAVA="java"
  fi
fi

# Resolve the path to the installation directory where this script is located in
if [ -h $0 ]
then
  # This script has been invoked from a symlink, resolve the link's target (i.e. the path to this script)
  BASE_FOLDER=`ls -l "$0"`
  BASE_FOLDER=${BASE_FOLDER#*-> }
else
  BASE_FOLDER=$0
fi

cd `dirname "$BASE_FOLDER"`

# Starts mucommander.
$JAVA -DGNOME_DESKTOP_SESSION_ID=$GNOME_DESKTOP_SESSION_ID -DKDE_FULL_SESSION=$KDE_FULL_SESSION -DKDE_SESSION_VERSION=$KDE_SESSION_VERSION -Djava.library.path=/usr/local/lib -cp mucommander-@MU_VERSION@.jar com.mucommander.main.muCommander $@
