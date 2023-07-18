#!/bin/bash

# Generate JMS2 source from the JMS3:
#
# This script creates a copy of the Boot Starter code which is functionally (almost) identical
# but has the jakarta package names replaced by javax in most places. 

curdir=`pwd`
in="$1"
out="$2"

if [ -z "$in" -o -z "$out" ]
then
  echo "Usage: makeJms2.sh inDir outDir"
  exit 1
fi
# echo "Copying files for JMS2 build from $in to $out"

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
      sed "s/ibm.msg.client.javax/ibm.msg.client/g ">  $out/$f
done

find src -type f -name "*.jms2" | while read f
do
  # echo $f
  d=`dirname $f`
  n=`echo $f | sed "s/.jms2$//g"`
  cp $f $out/$n
done

# One final piece of patching is for the SSLBundles support only available from Spring Boot 3.1
f="./src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
cat $f | grep -v "com.ibm.mq.spring.boot.MQConfigurationSslBundles" > $out/$f
