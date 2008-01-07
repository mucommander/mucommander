#! /bin/sh

# muCommander startup arguments
MUCOMMANDER_ARGS="@ARGS@"
JAVA_ARGS="@JAVA_ARGS@"

# Locates the java executable.
if [ "$JAVA_HOME" != "" ] ; then
    JAVA=$JAVA_HOME/bin/java
else
    JAVACMD=`which java 2> /dev/null `
    if [ -z "$JAVACMD" ] ; then
        echo "Error: cannot find java VM."
        exit 1
    else
        JAVA=java
    fi
fi

# Resolve the path to the mucommander.jar located in the same directory as this script
if [ -h $0 ]
then
    # This script has been invoked from a symlink, resolve the link's target (i.e. the path to this script)
    MUCOMMANDER_SH=`ls -l "$0"`
    MUCOMMANDER_SH=${MUCOMMANDER_SH#*-> }
else
    MUCOMMANDER_SH=$0
fi

CURRENT_DIR=`dirname "$MUCOMMANDER_SH"`
MUCOMMANDER_JAR=$CURRENT_DIR/mucommander.jar

if [ ! -f $MUCOMMANDER_JAR ]
then
    echo "Error: cannot find file mucommander.jar in directory $CURRENT_DIR"
    exit 1
fi

# Starts mucommander.
$JAVA $JAVA_ARGS -DGNOME_DESKTOP_SESSION_ID=$GNOME_DESKTOP_SESSION_ID -DKDE_FULL_SESSION=$KDE_FULL_SESSION -jar $MUCOMMANDER_JAR $MUCOMMANDER_ARGS $@
