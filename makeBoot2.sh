#!/bin/bash

# Generate JMS2 (Boot2) source from the JMS3 (Boot3):
#
# This script creates a copy of the Boot Starter code which is functionally (almost) identical
# but has the jakarta package names replaced by javax in most places.
#
# Note that from version 3.2.2, the JMS3 code cannot be simply replicated in this way. Classes
# that are unique to Spring 3 are now being used. However, this script is being left in here for now
# as an example; it should still be possible to convert the Spring 3 code to Spring 2, but additional
# filtering/changes are required. There is a minimal mockup of the SslBundles interface included here
# that at least permits the code to compile.

curdir=`pwd`
in="$1"
out="$2"

if [ -z "$in" -o -z "$out" ]
then
  echo "Usage: makeBoot2.sh inDir outDir"
  exit 1
fi
# echo "Copying files for Boot2 build from $in to $out"

if [ ! -d $in ]
then
  echo "Cannot find input directory $in"
  exit 1
fi

mkdir $out >/dev/null 2>&1
cd $in
# Create the structure
find . -type f | grep -v ".jms3" | cpio -upad $out
# And recopy the files doing the package renaming as we go
find . -type f -name "*.java" | while read f
do
   # Change various package names to replace jakarta with javax in most places.
   # But the original MQ package names have neither in there.
   cat  $f |\
      grep -v "DeprecatedConfigurationProperty" |\
      sed "s/jakarta/javax/g" |\
      sed "s/ibm.mq.javax/ibm.mq/g" |\
      sed "s/import org.springframework.boot.ssl.*;//g" |\
      sed "s/ibm.msg.client.javax/ibm.msg.client/g ">  $out/$f
done

find src -type f -name "*.jms2" | while read f
do
  # echo $f
  d=`dirname $f`
  n=`echo $f | sed "s/.jms2$//g"`
  cp $f $out/$n
done

# This creates a minimal mockup of the Spring Boot 3 SslBundles interface
# in an attempt to allow the JMS3 code to compile as JMS2.
# But it's not been seriously tested.
cat << EOF >$out/src/main/java/com/ibm/mq/spring/boot/SslBundles.java
package com.ibm.mq.spring.boot;
import javax.net.ssl.SSLContext;
import javax.net.ssl.KeyManager;

class NoSuchSslBundleException extends Exception {
}

class SslBundles {
   SslBundle getBundle(String s) throws NoSuchSslBundleException{
     return null;
   }
}

class SslManagerBundle {
  KeyManager[] getKeyManagers() {
    return null;
  }
}

class SslBundle {
   SSLContext createSslContext() {
     return null;
   }
   SslManagerBundle getManagers() {
     return null;
   }
   String getProtocol() {
     return "N/A";
   }
}

EOF

# One final piece of patching is for the SSLBundles support only available from Spring Boot 3.1
f="./src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
cat $f | grep -v "com.ibm.mq.spring.boot.MQConfigurationSslBundles" > $out/$f
