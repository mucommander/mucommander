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

if [ "$(uname)" = "Darwin" ]; then
    EXTRA_OPTIONS='--add-exports java.desktop/com.apple.eawt=ALL-UNNAMED
      --add-exports java.desktop/com.apple.eio=ALL-UNNAMED
      --add-exports java.desktop/com.apple.laf=ALL-UNNAMED'
fi

cd `dirname "$BASE_FOLDER"`

# Starts mucommander.
$JAVA --add-opens java.desktop/javax.swing.plaf.basic=ALL-UNNAMED \
      --add-opens java.base/java.io=ALL-UNNAMED \
      --add-opens java.base/java.net=ALL-UNNAMED \
      --add-opens java.transaction.xa/javax.transaction.xa=ALL-UNNAMED \
      --add-opens java.management/javax.management=ALL-UNNAMED \
      --add-opens java.rmi/java.rmi=ALL-UNNAMED \
      --add-opens java.security.jgss/org.ietf.jgss=ALL-UNNAMED \
      --add-opens java.sql/java.sql=ALL-UNNAMED \
      --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED \
      --add-opens java.base/sun.net.www.protocol.https=ALL-UNNAMED \
      $EXTRA_OPTIONS \
      -Djava.library.path=/usr/local/lib -cp mucommander-@MU_VERSION@.jar com.mucommander.main.muCommander $@

