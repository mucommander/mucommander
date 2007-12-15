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

# Locates the mucommander jar file.
if [ -f ./mucommander.jar ] ; then
    MUCOMMANDER_JAR=./mucommander.jar
elif [ -f /usr/share/mucommander/mucommander.jar ] ; then
    MUCOMMANDER_JAR=/usr/share/mucommander/mucommander.jar
else
    echo "Error: cannot find mucommander.jar"
    exit 1
fi

# Starts mucommander.
$JAVA $JAVA_ARGS -DGNOME_DESKTOP_SESSION_ID=$GNOME_DESKTOP_SESSION_ID -DKDE_FULL_SESSION=$KDE_FULL_SESSION -jar $MUCOMMANDER_JAR $MUCOMMANDER_ARGS $@
