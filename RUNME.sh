#!/bin/bash

# Copyright 2022, 2025 IBM Corp. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
# except in compliance with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the
# License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
# either express or implied. See the License for the specific language governing permissions
# and limitations under the License.
#

# This script is used to build the MQ Spring Boot Starter modules. 
#
# When called with the -r option, it will try to upload the modules to Maven Central. Although you
# would need to change a few things in build.gradle to reset the artifact coordinates, and create a settings.gradle
# file with your credentials.
#
# Version numbers for the generated files are in the jms*.properties files, along with a few other
# variables that need to be set differently.
# 
# The gradle scripts need to be run with a Java 17 JDK or later.

function printSyntax {
  cat << EOF
Usage: RUNME.sh [-b bootVersion] [-r]
Options:
   -b Spring Boot Version ("boot2", "boot3" or "boot4"): can be repeated to get combinations. 
      Default builds boot3 only.
   -r Release to Maven staging or snapshot area
Note that after pushing files to the STAGING area they will
still require a manual release.
EOF
  exit 1
}

function makeBoot4Source {
  $curdir/makeBoot4.sh $1 $2
  majors="$majors 61"
}

function makeBoot3Source {
  $curdir/makeBoot3.sh $1 $2
  majors="$majors 61"
}

function makeBoot2Source {
  $curdir/makeBoot2.sh $1 $2
  majors="$majors 52"
}

curdir=`pwd`
gaRelease=false
bootVersions=""
project="mq-jms-spring-boot-starter"

timeStamp="/tmp/springBuild.time"
buildLog="/tmp/springBuild.log"
rcFile="/tmp/springBuild.rc"
majorsFile="/tmp/springBuild.majors"

# Definitions for how to create the variation 
in="$curdir/mq-jms-spring-boot-starter"
out2="$curdir/mq-boot2-spring-boot-starter"
out3="$curdir/mq-boot3-spring-boot-starter"
out4="$curdir/mq-boot4-spring-boot-starter"

majors=""

touch $timeStamp
# Git sometimes loses permissions
chmod +x makeBoot*.sh

# git seems to lose permissions for this one sometimes so force it
chmod +x prereqCheck.sh

while getopts :b:r o
do
  case $o in
  r)
    gaRelease=true
    ;;
  b)
    case $OPTARG in
    2|3|4)
      bootVersions="$bootVersions boot$OPTARG"
      ;;
    boot2|boot3|boot4)
      bootVersions="$bootVersions $OPTARG"
      ;;
    *)
      printSyntax
      ;;
    esac
    ;;
  *)
    printSyntax
    ;;
  esac
done

# Check for no further parameters
shift $((OPTIND-1))
if [ "$1" != "" ]
then
  printSyntax
fi

if [ -z "$bootVersions" ]
then
  bootVersions="boot3"
fi

bootVersionCount=`echo $bootVersions | wc -w`
if [ $bootVersionCount -lt 1 -o $bootVersionCount -gt 3 ]
then
  print "ERROR: Incorrect number of boot versions requested: $bootVersions"
  exit 1
fi

# Clean out a bunch of stuff so we can tell that we've actually built it during this process
rm -f $rcFile
rm -rf  $out2 $out4 $in/build
rm -f $buildLog

rm -rf $HOME/.m2/repository/com/ibm/mq/$project
find $HOME/.gradle | grep $project | xargs rm -rf
unset JAVA_HOME

cd $curdir

for vers in $bootVersions
do
  case $vers in 
  boot2)
    makeBoot2Source $in $out2
    ;;
  boot3)
    # This is the primary for now, but we'll copy it
    # to a new tree for symmetry. And for when Boot4 becomes
    # the primary.
    makeBoot3Source  $in $out3
    ;;
  boot4)
    makeBoot4Source  $in $out4
    ;;
  esac

  cd $curdir
  export BOOTVERSION=$vers
  args="" # "--stacktrace --debug" # Gradle debugging options

  if $gaRelease
  then
    export prereqCheck=true
    target=publishAllPublicationsToMavenRepository
  else
    target=publishToMavenLocal
  fi

  if [ -r $HOME/.creds/gradle.properties ]
  then
    cp $HOME/.creds/gradle.properties ./gradle.properties
  else
    cp -p gradle.properties.template gradle.properties
  fi

  if [ ! -r gradle.properties ]
  then
    print "ERROR: Need to provide a gradle.properties file with credentials"
    exit 1
  fi  

  # Possible Targets are publishAllPublicationsToMavenRepository publishToMavenLocal
  (./gradlew $args --warning-mode all clean jar $target 2>&1;echo $? > $rcFile) | tee -a $buildLog

  # Always make sure we've got a dummy properties file - the values are not needed from here on
  cp -p gradle.properties.template gradle.properties

  # Now we can look for errors
  rc=`cat $rcFile`
  if [ $rc -ne "0" ]
  then
    exit 1
  fi
done

# And see what we've created - this was useful debug during devt of the build scripts
find . -newer $timeStamp -type f -name "mq*.jar" -ls
#find $curdir/mq-jms*-boot-starter/build -name "*.asc"
#find $HOME/.gradle/ $HOME/.m2 -type f -ls | grep mq-jms-spring
#find $HOME/.gradle/ $HOME/.m2 -name "$project.*.pom" | xargs more

# Expect to see major versions 52 (java 8) and 61 (java 17) if we've done a local build. This validates
# the compiler options used
find mq-*-spring-boot-starter/build -name "*.class" | xargs javap -v | grep major | sort -u > $majorsFile
cat $majorsFile
for v in $majors
do
  grep -q $v $majorsFile
  if [ $? -ne 0 ]
  then
    echo "ERROR: Cannot find expected Java major version $v"
    exit 1
  fi
done

if $gaRelease
then
  cat << EOF

The files appear to have been successfully uploaded to Maven Central.
You now need to log on to Sonatype Nexus, and check the newly-created repository or
repositories. There might be one for each of BOOT2, BOOT3 and BOOT4, or they might be combined in a single repo.
If it all looks OK, then CLOSE and RELEASE the repo.

EOF
fi
