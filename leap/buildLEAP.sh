#! /bin/sh 
#
# Build script for LEAP under *nix systems (it works under NT/cygwin too).
# Sets up environment for calling LEAP ant build file
# Author: Jerome Picault, Motorola Labs
#
# Configuration variables
#
# JAVA_HOME  
#   Home of Java installation. This needs to be set as this script 
#   will not look for it.
#
# JAVA
#   Command to invoke Java. If not set, $JAVA_HOME/bin/java will be
#   used.
#
echo
echo "   This is the build script for LEAP under *nix systems." 
echo "   Copyright (c) 2002 Motorola."
echo "   Sets up environment for calling LEAP ant build file." 
echo "   Works under NT/Cygwin too :-)"
echo
##################################################
# Set home of LEAP files
##################################################

if [ -z "$LEAP_HOME" ]
then
  echo "LEAP_HOME not set."
  LEAP_HOME=`pwd`
  echo "Automatically set LEAP_HOME to $LEAP_HOME."
  # export LEAP_HOME (used by ant)
  export LEAP_HOME
else
  echo "LEAP_HOME is currently set to $LEAP_HOME."
  if [ -d "$LEAP_HOME" ]
  then
    echo "Valid directory." 
    echo "Warning: if it does not correspond to the current version of LEAP, please update the LEAP_HOME environment variable or delete it."
    export LEAP_HOME
  else
    echo "Not a valid directory."
    echo "Please set LEAP_HOME environment variable to your leap directory or remove it to enable auto-detection."
    exit 1
  fi
fi

##################################################
TMPJ=/tmp/j$$
##################################################
# Set home of ant files for building LEAP
##################################################
ANT_HOME=$LEAP_HOME/resources/build/ant
echo "ANT_HOME set to $ANT_HOME."
##################################################
# Get the command line args
##################################################
ARGS="$*"
##################################################
# Check for JAVA_HOME
##################################################
if [ -z "$JAVA_HOME" ]
then
    # If a java runtime is not defined, search the following
    # directories for a JVM and sort by version. Use the highest
    # version number.

    # Java default search path
    JAVA_LOCATIONS="\
        /usr/bin \
        /usr/local/bin \
        /usr/local/java \
        /usr/local/java/bin \
        /usr/local/jdk \
	/usr/local/jdk/bin \
        /opt/java \
	/opt/java/bin \
        /opt/jdk \
	/opt/jdk/bin \
	/home \
    " 
    JAVA_NAMES="java jre"
    for N in $JAVA_NAMES ; do
        for L in $JAVA_LOCATIONS ; do
            [ -d $L ] || continue 
            find $L -name "$N" ! -type d | grep -v threads | while read J ; do
                [ -x $J ] || continue
                VERSION=`eval $J -version 2>&1`       
                [ $? = 0 ] || continue
                VERSION=`expr "$VERSION" : '.*"\(1.[0-9\.]*\)"'`
                [ "$VERSION" = "" ] && continue
                expr $VERSION \< 1.2 >/dev/null && continue
                echo $VERSION:$J
            done
        done
    done | sort | tail -1 > $TMPJ
    JAVA=`cat $TMPJ | cut -d: -f2`
    JVERSION=`cat $TMPJ | cut -d: -f1`

    if [ -z "$JAVA" ]
    then
      echo "Warning: Cannot find $JAVA_NAMES in default locations."
    else
      JAVA_HOME=`dirname $JAVA`
      # export JAVA_HOME to be used by ant
      export JAVA_HOME
      while [ ! -z "$JAVA_HOME" -a "$JAVA_HOME" != "/" -a ! -f "$JAVA_HOME/lib/tools.jar" ] ; do
          JAVA_HOME=`dirname $JAVA_HOME`
      done
      [ "$JAVA_HOME" = "" ] && JAVA_HOME=
      echo "Found JAVA=$JAVA in JAVA_HOME=$JAVA_HOME"
    fi
fi


##################################################
# Determine which JVM of version >1.2
# Try to use JAVA_HOME
##################################################
if [ "$JAVA" = "" -a "$JAVA_HOME" != "" ]
then
  if [ ! -z "$JAVACMD" ] 
  then
     JAVA="$JAVACMD" 
  else
    [ -x $JAVA_HOME/bin/jre -a ! -d $JAVA_HOME/bin/jre ] && JAVA=$JAVA_HOME/bin/jre
    [ -x $JAVA_HOME/bin/java -a ! -d $JAVA_HOME/bin/java ] && JAVA=$JAVA_HOME/bin/java
  fi
fi

if [ "$JAVA" = "" ]
then
    echo "Warning: JAVA_HOME environment variable is not set." 2>&2
    echo "  If build fails because sun.*classes could not be found" 2>&2
    echo "  you will need to set the JAVA_HOME environment variable" 2>&2
    echo "  to the installation directory of java." 2>&2
    exit 1
fi

PATH=$PATH:$JAVA_HOME/bin
export PATH

#####################################################
# Are we running on Windows? Could be, with Cygwin/NT.
#####################################################
if [ ! "$OSTYPE" = "cygwin" ] 
then
  PATH_SEPARATOR=":" 
else
  PATH_SEPARATOR="\;" 
fi

#####################################################
# Build the local classpath with ANT's bundled libraries.
#####################################################
ANT_JAR=`ls ${ANT_HOME}/lib/*.jar`
LOCALCLASSPATH=`for N in $ANT_JAR ; do echo $N$PATH_SEPARATOR; done | paste -s -d "" -`
if [ -f $JAVA_HOME/lib/tools.jar ]
  then
    LOCALCLASSPATH=${JAVA_HOME}/lib/tools.jar${PATH_SEPARATOR}${LOCALCLASSPATH}
fi

if [ -f $JAVA_HOME/lib/classes.zip ]
  then
    LOCALCLASSPATH=${JAVA_HOME}/lib/classes.zip${PATH_SEPARATOR}${LOCALCLASSPATH}
fi

##################################################
# Do the action (run Ant)
##################################################
if [ ! -z "$JIKESPATH" ]
then
# run ANT with jikes
${JAVA} -classpath ${LOCALCLASSPATH} -Dant.home=${ANT_HOME} \
-Djikes.class.path=${JIKESPATH} ${ANT_OPTS} \
org.apache.tools.ant.Main -buildfile ${LEAP_HOME}/resources/build/build.xml \
${ARGS}
else
# run ANT
${JAVA} -classpath ${LOCALCLASSPATH} -Dant.home=${ANT_HOME} ${ANT_OPTS}\
org.apache.tools.ant.Main -buildfile ${LEAP_HOME}/resources/build/build.xml \
${ARGS}
fi
exit 0
