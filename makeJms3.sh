#!/bin/bash

# Generate JMS3 source from the JMS2:
#
# This script creates a copy of the Boot Starter code which is functionally identical
# but has the javax replaced by jakarta in most places. It also (for now) copies in some code that
# pretends to be the JMS Pooling implementation, so we don't have to pull that out
# when creating the JMS3 files. Hopefully, the messsaginghub/pooled-jms library will
# get updated at some point.

curdir=`pwd`
in="$1"
out="$2"

if [ ! -d $in ]
then
  echo "Cannot find input directory $in"
  exit 1
fi

mkdir $out >/dev/null 2>&1
cd $in
# Create the structure
find . -type f | cpio -upad $out
# And recopy the files doing the renaming as we go
find . -type f -name "*.java" | while read f
do
   # Change various package names, but the javax.naming seems to have to remain so we revert that
   # specific change in the final step of the filter.
   cat  $f |\
      sed "s/com.ibm.mq.jms/com.ibm.mq.jakarta.jms/g" |\
      sed "s/com.ibm.msg.client/com.ibm.msg.client.jakarta/g" |\
      sed "s/javax/jakarta/g" |
      sed "s/jakarta.naming/javax.naming/g">  $out/$f
done
