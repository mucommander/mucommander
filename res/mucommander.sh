#! /bin/sh
if [ "$JAVA_HOME" != "" ]; then
    JAVA=$JAVA_HOME/bin/java
else
    echo JAVA_HOME environment variable not set, trying default java VM
    JAVA=java
fi

$JAVA -DGNOME_DESKTOP_SESSION_ID=$GNOME_DESKTOP_SESSION_ID -DKDE_FULL_SESSION=$KDE_FULL_SESSION -jar mucommander.jar
